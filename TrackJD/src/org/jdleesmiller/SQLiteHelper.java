package org.jdleesmiller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles database creation.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
  
  private static final String DATABASE_NAME = "track_jd.db";
  private static final int DATABASE_VERSION = 2;
  
  public static final String GPS_TABLE = "gps";
  
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
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // WARNING: upgrading destroys old data
    db.execSQL("DROP TABLE IF EXISTS " + GPS_TABLE);
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
