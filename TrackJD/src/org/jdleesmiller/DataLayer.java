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
 * 
 * Note: it is not necessary to close the database, because we want to keep
 * logging as long as the process is alive, and the OS will close the database
 * when the process dies.
 */
public class DataLayer {
  private SQLiteHelper dbHelper;
  private SQLiteDatabase db;

  public DataLayer(Context context) {
    dbHelper = new SQLiteHelper(context);
  }

  public void open() throws SQLException {
    Log.d("DataLayer", "open");
    db = dbHelper.getWritableDatabase();
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
    return db.insert(SQLiteHelper.GPS_TABLE, null, values);
  }

  /**
   * @param x
   * @param y
   * @param z
   * @param time
   * @return id of inserted row
   */
  public long logAccelerometer(float x, float y, float z, long time) {
    ContentValues values = new ContentValues(4);
    values.put("x", x);
    values.put("y", y);
    values.put("z", z);
    values.put("time", time);
    return db.insert(SQLiteHelper.ACCELEROMETER_TABLE, null, values);
  }

  /**
   * @param azimuth
   * @param pitch
   * @param roll
   * @param time
   * @return id of inserted row
   */
  public long logOrientation(float azimuth, float pitch, float roll, long time) {
    ContentValues values = new ContentValues(4);
    values.put("azimuth", azimuth);
    values.put("pitch", pitch);
    values.put("roll", roll);
    values.put("time", time);
    return db.insert(SQLiteHelper.ORIENTATION_TABLE, null, values);
  }

  /**
   * A UTC timestamp from System.currentTimeMillis is stored with the record.
   * 
   * @param bdaddr
   * @param rssi
   * @return id of inserted row
   */
  public long logBluetooth(String bdaddr, short rssi) {
    ContentValues values = new ContentValues(3);
    values.put("bdaddr", bdaddr);
    values.put("rssi", rssi);
    values.put("time", System.currentTimeMillis());
    return db.insert(SQLiteHelper.BLUETOOTH_TABLE, null, values);
  }

  /**
   * @return number of GPS records currently in the database
   */
  public long countGPS() {
    return DatabaseUtils.queryNumEntries(db, SQLiteHelper.GPS_TABLE);
  }

  /**
   * @return number of accelerometer records currently in the database
   */
  public long countAccelerometer() {
    return DatabaseUtils.queryNumEntries(db, SQLiteHelper.ACCELEROMETER_TABLE);
  }

  /**
   * Dump rows recorded with logGPS to CSV.
   * 
   * @param buf
   * @param lastId
   * @param maxRecords
   * @return id of last record in the returned CSV, or lastId if no records
   */
  public long getGPSAsCSV(StringBuilder buf, long lastId, int maxRecords) {
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
      records += 1;
    }
    return newLastId;
  }

  /**
   * Dump rows recorded with logAccelerometer to CSV.
   * 
   * @param buf
   * @param lastId
   * @param maxRecords
   * @return id of last record in the returned CSV, or lastId if no records
   */
  public long getAccelerometerAsCSV(StringBuilder buf, long lastId,
    int maxRecords) {
    buf.append("x,y,z,time\n");
    Cursor c = db.rawQuery("SELECT accel_id, x, y, z, time FROM "
      + SQLiteHelper.ACCELEROMETER_TABLE + " WHERE accel_id > " + lastId, null);
    int records = 0;
    long newLastId = lastId;
    while (c.moveToNext() && records < maxRecords) {
      newLastId = c.getLong(0);
      buf.append(c.getFloat(1));
      buf.append(",");
      buf.append(c.getFloat(2));
      buf.append(",");
      buf.append(c.getFloat(3));
      buf.append(",");
      buf.append(c.getLong(4));
      buf.append("\n");
      records += 1;
    }
    return newLastId;
  }

  /**
   * Dump rows recorded with logOrientation to CSV.
   * 
   * @param buf
   * @param lastId
   * @param maxRecords
   * @return id of last record in the returned CSV, or lastId if no records
   */
  public long getOrientationAsCSV(StringBuilder buf, long lastId,
    int maxRecords) {
    buf.append("azimuth,pitch,roll,time\n");
    Cursor c = db.rawQuery("SELECT orient_id, azimuth, pitch, roll, time FROM "
      + SQLiteHelper.ORIENTATION_TABLE + " WHERE orient_id > " + lastId, null);
    int records = 0;
    long newLastId = lastId;
    while (c.moveToNext() && records < maxRecords) {
      newLastId = c.getLong(0);
      buf.append(c.getFloat(1));
      buf.append(",");
      buf.append(c.getFloat(2));
      buf.append(",");
      buf.append(c.getFloat(3));
      buf.append(",");
      buf.append(c.getLong(4));
      buf.append("\n");
      records += 1;
    }
    return newLastId;
  }

  /**
   * Dump rows recorded with logBluetooth to CSV.
   * 
   * @param buf
   * @param lastId
   * @param maxRecords
   * @return id of last record in the returned CSV, or lastId if no records
   */
  public long getBluetoothAsCSV(StringBuilder buf, long lastId,
    int maxRecords) {
    buf.append("bdaddr,rssi,time\n");
    Cursor c = db.rawQuery("SELECT bluetooth_id, bdaddr, rssi, time FROM "
      + SQLiteHelper.BLUETOOTH_TABLE + " WHERE bluetooth_id > " + lastId, null);
    int records = 0;
    long newLastId = lastId;
    while (c.moveToNext() && records < maxRecords) {
      newLastId = c.getLong(0);
      buf.append(c.getString(1));
      buf.append(",");
      buf.append(c.getInt(2));
      buf.append(",");
      buf.append(c.getLong(3));
      buf.append("\n");
      records += 1;
    }
    return newLastId;
  }

  /**
   * @return largest id in the GPS log table.
   */
  public long getMaxGPSId() {
    return getMaxIdFromTable(SQLiteHelper.GPS_TABLE, "gps_id");
  }

  /**
   * @return largest id in the accelerometer log table.
   */
  public long getMaxAccelerometerRecordId() {
    return getMaxIdFromTable(SQLiteHelper.ACCELEROMETER_TABLE, "accel_id");
  }

  /**
   * @return largest id in the orientation log table.
   */
  public long getMaxOrientationRecordId() {
    return getMaxIdFromTable(SQLiteHelper.ORIENTATION_TABLE, "orient_id");
  }

  /**
   * @return largest id in the bluetooth log table.
   */
  public long getMaxBluetoothRecordId() {
    return getMaxIdFromTable(SQLiteHelper.BLUETOOTH_TABLE, "bluetooth_id");
  }

  /**
   * @param table
   *          trusted text only
   * @param idColumn
   *          trusted text only
   * @return largest id in the given table
   */
  private long getMaxIdFromTable(String table, String idColumn) {
    Cursor c = db.rawQuery("SELECT MAX(" + idColumn + ") FROM " + table, null);
    if (!c.moveToNext() || c.isNull(0))
      return 0;
    else
      return c.getLong(0);
  }
}
