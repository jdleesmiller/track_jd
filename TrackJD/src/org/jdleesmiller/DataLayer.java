package org.jdleesmiller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

/**
 * Runs queries against database.
 */
public class DataLayer {
  private SQLiteHelper dbHelper;
  private SQLiteDatabase db;

  public DataLayer(Context context) {
    dbHelper = new SQLiteHelper(context);
  }

  public void open() throws SQLException {
    db = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  /**
   * @param location
   * 
   * @return id of inserted row
   */
  public long logGPS(Location location) {
    ContentValues values = new ContentValues(4);
    if (location.hasAccuracy())
      values.put("accuracy", location.getAccuracy());
    if (location.hasAltitude())
      values.put("altitude", location.getAltitude());
    values.put("latitude", location.getLatitude());
    values.put("longitude", location.getLongitude());
    values.put("time", location.getTime());
    Log.d("DataLayer", "time" + location.getTime());
    return db.insert(SQLiteHelper.GPS_TABLE, null, values);
  }

  /**
   * @return number of GPS records currently in the database
   */
  public long countGPS() {
    return DatabaseUtils.queryNumEntries(db, SQLiteHelper.GPS_TABLE);
  }

  /**
   * Dump rows recorded with logGPS to CSV.
   * 
   * @param buf
   * @param lastId
   * @param maxRecords
   * @return gps_id of last record in the returned CSV
   */
  public long getGPSAsCSV(StringBuffer buf, long lastId, int maxRecords) {
    buf.append("accuracy,altitude,latitude,longitude,time\n");
    Cursor c = db.rawQuery(
      "SELECT gps_id, accuracy, altitude, latitude, longitude, time FROM "
        + SQLiteHelper.GPS_TABLE + " WHERE gps_id > " + lastId, null);
    int records = 0;
    long newLastId = lastId;
    while (c.moveToNext() && records < maxRecords) {
      newLastId = c.getLong(0);
      buf.append(c.getFloat(1));
      buf.append(",");
      buf.append(c.getDouble(2));
      buf.append(",");
      buf.append(c.getDouble(3));
      buf.append(",");
      buf.append(c.getDouble(4));
      buf.append(",");
      buf.append(c.getLong(5));
      buf.append("\n");
    Log.d("DataLayer", "recovered time" + c.getLong(5));
      records += 1;
    }
    Log.i("CSV", "records=" + records + "; count=" + c.getCount());
    return newLastId;
  }

  /**
   * The largest gps_id in the gps log table.
   * 
   * @return
   */
  public long getMaxGPSId() {
    Cursor c = db.rawQuery(
      "SELECT MAX(gps_id) FROM " + SQLiteHelper.GPS_TABLE, null);
    if (!c.moveToNext() || c.isNull(0))
      return 0;
    else
      return c.getLong(0);
  }
}
