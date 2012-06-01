package org.jdleesmiller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles database creation.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
  
  private static final String DATABASE_NAME = "track_jd.db";
  
  /**
   * Increment this number when the schema changes. NB this drops any data
   * in the old database.
   */
  private static final int DATABASE_VERSION = 3;
  
  public static final String GPS_TABLE = "gps";
  
  public static final String ACCELEROMETER_TABLE = "accel";
  
  public static final String ORIENTATION_TABLE = "orient";
  
  public static final String BLUETOOTH_TABLE = "bluetooth";
  
  public SQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + GPS_TABLE + " (" +
      "gps_id INTEGER PRIMARY KEY AUTOINCREMENT," +
      "accuracy FLOAT," +
      "altitude DOUBLE," +
      "latitude DOUBLE," +
      "longitude DOUBLE," +
      "time INTEGER)");
    
    db.execSQL("CREATE TABLE " + ACCELEROMETER_TABLE + " (" +
      "accel_id INTEGER PRIMARY KEY AUTOINCREMENT," +
      "x FLOAT," +
      "y FLOAT," +
      "z FLOAT," +
      "time INTEGER)");
    
    db.execSQL("CREATE TABLE " + ORIENTATION_TABLE + " (" +
      "orient_id INTEGER PRIMARY KEY AUTOINCREMENT," +
      "azimuth FLOAT," +
      "pitch FLOAT," +
      "roll FLOAT," +
      "time INTEGER)");
    
    db.execSQL("CREATE TABLE " + BLUETOOTH_TABLE + " (" +
      "bluetooth_id INTEGER PRIMARY KEY AUTOINCREMENT," +
      "bdaddr CHAR(17)," +
      "rssi INTEGER," +
      "time INTEGER)");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // WARNING: upgrading destroys old data
    db.execSQL("DROP TABLE IF EXISTS " + GPS_TABLE);
    db.execSQL("DROP TABLE IF EXISTS " + ACCELEROMETER_TABLE);
    db.execSQL("DROP TABLE IF EXISTS " + ORIENTATION_TABLE);
    db.execSQL("DROP TABLE IF EXISTS " + BLUETOOTH_TABLE);
    onCreate(db);
  }
}


    /*
    // could save NMEA... not sure how useful that would be, and it looks like
    // the phone parses it anyway, so we might as well use the parsed version
    db.execSQL("CREATE TABLE " + NMEA_TABLE + " (" +
      "nmea_id INTEGER PRIMARY KEY AUTOINCREMENT," +
      "timestamp INT8," +
      "nmea TEXT)");
    db.execSQL("CREATE TABLE server (" +
      "server_id INTEGER PRIMARY KEY AUTOINCREMENT," +
      "name TEXT," +
      "uploaded_nmea_id INTEGER" +
      ")");
      //"last_gps_loc_id_uploaded INTEGER," +
      */
