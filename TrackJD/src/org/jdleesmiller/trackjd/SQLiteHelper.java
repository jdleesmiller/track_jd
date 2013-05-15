package org.jdleesmiller.trackjd;

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
  private static final int DATABASE_VERSION = 5;

  public SQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    // TODO
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // TODO
  }
}
