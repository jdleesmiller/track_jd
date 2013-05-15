package org.jdleesmiller.trackjd;

import java.util.List;

import org.jdleesmiller.trackjd.collector.AbstractCollector;

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
  private static final int DATABASE_VERSION = 4;

  private List<AbstractCollector> collectors;
  
  public SQLiteHelper(Context context, List<AbstractCollector> collectors) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.collectors = collectors;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    for (AbstractCollector collector : collectors) {
      collector.createTable(db);
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // WARNING: upgrading destroys old data
    for (AbstractCollector collector : collectors) {
      collector.dropTable(db);
      collector.createTable(db);
    }
  }
}
