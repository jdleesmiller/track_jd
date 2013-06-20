package org.thereflectproject.trackjd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

import org.thereflectproject.trackjd.data.AbstractPoint;

import com.loopj.android.http.RequestParams;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Generic logging of CSV fragments from multiple sensors. To keep things
 * simple, we just put the data from all the sensors into one big table.
 * 
 * The main advantage of doing it this way is that it decouples the logging and
 * uploading logic from the particular sensor types and exactly what we record
 * from them. Once we know more about exactly what we want to collect, we should
 * probably have one table per sensor. The main issues with the current approach
 * are:
 * 
 * 1. We do more work than strictly necessary upon recording each data point,
 * because we convert to CSV. We could instead store some sort of binary blob,
 * or we could break each sensor into its own table; these options would also be
 * more storage-efficient.
 * 
 * 2. We can't easily query the points from the app, so if we're making more
 * complicated decisions on the app based on more than very recent historical
 * data, we'll have to look at some other solution. This could be a problem for
 * on-device mode annotation.
 * 
 * For efficiency reasons, we do not write points to the database immediately;
 * instead the class buffers them in memory. It flushes the buffer when the
 * buffer is full, or when we're about to upload (just to simplify the upload
 * code -- we could instead write the upload code so that it took data from the
 * buffer directly). This allows us to use transactions, which make inserts much
 * faster, and so hopefully saves some power (see
 * http://blog.quibb.org/2010/08/fast-bulk-inserts-into-sqlite/).
 */
public class DataLogger {

  public static final String TABLE = "data";

  /**
   * Maximum number of points to keep in memory before writing them to the
   * database. Setting this larger results in fewer writes and (presumably)
   * lower power consumption. However, it also means that the application uses
   * more memory, takes longer to write when it is necessary to flush the
   * buffer, and also that it will lose more data if the application crashes.
   */
  private static final int POINTS_TO_BUFFER = 200;

  /**
   * Called when initializing the database.
   * 
   * @param db
   */
  public static void createTable(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + TABLE + " (" + //
        "id INTEGER PRIMARY KEY AUTOINCREMENT," + //
        "time INTEGER," + //
        "tag TEXT," + //
        "csv TEXT)");
  }

  /**
   * Called when recreating the database.
   * 
   * @param db
   */
  public static void dropTable(SQLiteDatabase db) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE);
  }

  private final Collection<AbstractPoint> buffer;

  private final TrackJDService service;

  public DataLogger(TrackJDService service) {
    this.service = service;
    this.buffer = new ArrayList<AbstractPoint>(POINTS_TO_BUFFER);
  }

  /**
   * 
   * @param params
   * 
   * @param maxRecords
   * 
   * @return the id of the last record uploaded, or 0 if no records were
   *         uploaded
   */
  public long addToUpload(RequestParams params, int maxRecords) {
    flushBufferedPoints();
    SQLiteDatabase db = this.service.getReadableDb();
    if (db != null) {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(os);
      long lastId = -1;

      Cursor c = db.rawQuery(//
          "SELECT tag, id, time, csv FROM (" + //
              "  SELECT * FROM " + TABLE + " ORDER BY id ASC LIMIT ?)" + //
              "ORDER BY tag", new String[] { Integer.toString(maxRecords) });
      String currentTag = null;
      while (c.moveToNext()) {
        String tag = c.getString(0);
        if (!tag.equals(currentTag)) {
          // starting a new file; store the previous file, if any
          if (currentTag != null) {
            ps.flush();
            params.put(currentTag, new ByteArrayInputStream(os.toByteArray()));
            os.reset();
          }
          currentTag = tag;
        }

        lastId = c.getLong(1);

        ps.print(lastId);
        ps.print(',');
        ps.print(c.getLong(2)); // UTC time
        ps.print(',');
        ps.print(c.getString(3)); // rest of CSV
        ps.print('\n');
      }

      // upload the last file
      if (currentTag != null) {
        ps.flush();
        params.put(currentTag, new ByteArrayInputStream(os.toByteArray()));
      }

      return lastId;
    } else {
      return 0;
    }
  }

  /**
   * Delete all records with id less than or equal to the given id. Called after
   * addToUpload to clear data that the device has successfully uploaded.
   * 
   * @param lastId
   */
  public void clearLastUpload(long lastId) {
    SQLiteDatabase db = this.service.getWritableDb();
    if (db != null) {
      db.delete(TABLE, "id <= ?", new String[] { Long.toString(lastId) });
    }
  }

  /**
   * The number of points currently stored on the device.
   * 
   * @return non-negative
   */
  public int countPoints() {
    SQLiteDatabase db = this.service.getReadableDb();
    if (db != null) {
      Cursor c = db.rawQuery("SELECT COUNT(id) FROM " + TABLE, null);
      if (!c.moveToNext() || c.isNull(0)) {
        return buffer.size();
      } else {
        return c.getInt(0) + buffer.size();
      }
    } else {
      return 0;
    }
  }

  /**
   * Store a data point in the database.
   * 
   * @param point
   */
  public void log(AbstractPoint point) {
    if (buffer.size() >= POINTS_TO_BUFFER) {
      flushBufferedPoints();
    }
    buffer.add(point);
  }

  /**
   * Called when the application is shutting down. This gives the logger a
   * chance to flush its buffer.
   */
  public void stop() {
    flushBufferedPoints();
  }

  /**
   * Store any buffered points into the database and clear the buffer. We use a
   * transaction here, because it is faster -- it lets us combine all of the
   * inserts into a single write.
   */
  private void flushBufferedPoints() {
    Log.d("DataLogger", "flush " + buffer.size());
    SQLiteDatabase db = this.service.getWritableDb();
    if (db != null) {
      db.beginTransaction();
      try {
        for (AbstractPoint point : buffer) {
          storePointInDatabase(db, point);
        }
        db.setTransactionSuccessful();
        buffer.clear();
      } finally {
        db.endTransaction();
      }
    }
  }

  /**
   * Store a single point in the database
   * 
   * @param db
   *          not null
   * @param point
   */
  private void storePointInDatabase(SQLiteDatabase db, AbstractPoint point) {
    ContentValues values = new ContentValues();
    values.put("time", point.getUTCTime());
    values.put("tag", point.getTag());

    StringBuilder sb = new StringBuilder();
    point.printCsvData(sb);
    values.put("csv", sb.toString());

    db.insert("data", null, values);
  }
}
