package org.jdleesmiller.trackjd.collector;

import java.io.PrintStream;

import org.jdleesmiller.trackjd.CollectorService;
import org.jdleesmiller.trackjd.Constants;
import org.jdleesmiller.trackjd.data.AccelerometerPoint;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class AccelerometerCollector extends AbstractSensorCollector {

  private SensorEventListener sensorListener;
  
  private final CollectorBuffer<AccelerometerPoint> buffer;
  
  /**
   * Maximum number of data points to store in memory before flushing to disk.
   */
  private static final int MAX_DATA = 100;

  /**
   * In microseconds. Maximum update rate appear to be 50Hz, which is pretty
   * fast, so we default lower.
   */
  private static final int DEFAULT_INTERVAL = 250000;

  private static final String TABLE_NAME = "accel";

  public AccelerometerCollector(CollectorService context) {
    super(context);
    
    this.buffer = new CollectorBuffer<AccelerometerPoint>(MAX_DATA);

    sensorListener = new SensorEventListener() {
      public void onSensorChanged(SensorEvent event) {
        buffer.store(new AccelerometerPoint(event));
      }

      public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("AccelerometerCollector", "accelerometer accuracy changed: "
            + accuracy);
      }
    };
  }

  @Override
  public void start() {
    getSensorManager().registerListener(
        sensorListener,
        getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        getPreferences().getInt(Constants.PREF_ACCELEROMETER_INTERVAL,
            DEFAULT_INTERVAL));
  }

  @Override
  public void stop() {
    getSensorManager().unregisterListener(sensorListener);
  }

  @Override
  public void createTable(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + //
        "accel_id INTEGER PRIMARY KEY AUTOINCREMENT," + //
        "x FLOAT," + //
        "y FLOAT," + //
        "z FLOAT," + //
        "time INTEGER)");
  }

  @Override
  public void dropTable(SQLiteDatabase db) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
  }

  @Override
  public void printCsvHeader(PrintStream ps) {
    ps.print("x,y,z,time\n");
  }

  @Override
  public long printCsvData(PrintStream ps, long lastId, int maxRecords) {
    long newLastId = lastId;
    SQLiteDatabase db = getReadableDb();
    if (db != null) {
      Cursor c = db.rawQuery("SELECT accel_id, x, y, z, time FROM "
          + TABLE_NAME + " WHERE accel_id > " + lastId, null);
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
