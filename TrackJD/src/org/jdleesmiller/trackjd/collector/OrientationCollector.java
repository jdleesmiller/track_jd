package org.jdleesmiller.trackjd.collector;

import java.io.PrintStream;

import org.jdleesmiller.trackjd.CollectorService;
import org.jdleesmiller.trackjd.Constants;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class OrientationCollector extends AbstractSensorCollector {

  /**
   * In microseconds.
   */
  private static final int DEFAULT_INTERVAL = 500000;

  public static final String TABLE_NAME = "orient";

  private SensorEventListener sensorListener;

  public OrientationCollector(CollectorService context) {
    super(context);

    sensorListener = new SensorEventListener() {
      private float[] rotationMatrix = new float[16];
      private float[] orientation = new float[3];

      public void onSensorChanged(SensorEvent event) {
        SQLiteDatabase db = getWritableDb();
        if (db != null) {
          try {
            SensorManager.getRotationMatrixFromVector(rotationMatrix,
                event.values);
            SensorManager.getOrientation(rotationMatrix, orientation);
            ContentValues values = new ContentValues(4);
            values.put("azimuth", orientation[0]);
            values.put("pitch", orientation[1]);
            values.put("roll", orientation[2]);
            values.put("time", eventTimestampToUTC(event.timestamp));
            db.insert(TABLE_NAME, null, values);
          } catch (RuntimeException e) {
            // this was crashing for a while, but I don't think it should now
            Log.e("OrientationCollector", "failed to get orientation");
            e.printStackTrace();
          }
        }
      }

      public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("OrientationCollector", "orientation accuracy changed: "
            + accuracy);
      }
    };
  }

  @Override
  public void start() {
    getSensorManager().registerListener(
        sensorListener,
        getSensorManager().getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
        getPreferences().getInt(Constants.PREF_ORIENTATION_INTERVAL,
            DEFAULT_INTERVAL));
  }

  @Override
  public void stop() {
    getSensorManager().unregisterListener(sensorListener);
  }

  @Override
  public void createTable(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + //
        "orient_id INTEGER PRIMARY KEY AUTOINCREMENT," + //
        "azimuth FLOAT," + //
        "pitch FLOAT," + //
        "roll FLOAT," + //
        "time INTEGER)");
  }

  @Override
  public void dropTable(SQLiteDatabase db) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
  }

  @Override
  public void printCsvHeader(PrintStream ps) {
    ps.print("azimuth,pitch,roll,time\n");
  }

  @Override
  public long printCsvData(PrintStream ps, long lastId, int maxRecords) {
    long newLastId = lastId;
    SQLiteDatabase db = getReadableDb();
    if (db != null) {
      Cursor c = db.rawQuery(
          "SELECT orient_id, azimuth, pitch, roll, time FROM " + TABLE_NAME
              + " WHERE orient_id > " + lastId, null);
      int records = 0;
      while (c.moveToNext() && records < maxRecords) {
        newLastId = c.getLong(0);
        ps.print(c.getFloat(1));
        ps.print(",");
        ps.print(c.getFloat(2));
        ps.print(",");
        ps.print(c.getFloat(3));
        ps.print(",");
        ps.print(c.getLong(4));
        ps.print("\n");
        records += 1;
      }
    }
    return newLastId;
  }
}
