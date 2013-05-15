package org.jdleesmiller.trackjd.collector;

import java.io.PrintStream;

import org.jdleesmiller.trackjd.CollectorService;
import org.jdleesmiller.trackjd.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

/**
 * A collector reads data from a particular sensor and stores it in a buffer.
 */
public abstract class AbstractCollector {

  private final CollectorService context;
  private final SharedPreferences prefs;
  
  public AbstractCollector(CollectorService service) {
    this.context = service;

    prefs = service.getSharedPreferences(Constants.PREFS_FILE_NAME,
        Context.MODE_PRIVATE);
  }

  protected Context getContext() {
    return this.context;
  }

  protected SharedPreferences getPreferences() {
    return this.prefs;
  }
  
  /**
   * Call when reading data to the database. Do *not* close this; it is
   * closed only when the service stops.
   * 
   * @return may be null (e.g. disk full or program stopping)
   */
  protected SQLiteDatabase getReadableDb() {
    return context.getReadableDb();
  }
  
  /**
   * Call when writing data to the database. Do *not* close this; it is
   * closed only when the service stops.
   * 
   * @return may be null (e.g. disk full or program stopping)
   */
  protected SQLiteDatabase getWritableDb() {
    return context.getWritableDb();
  }

  /**
   * Create the database table that we use to record the output from this
   * collector (if it doesn't already exist).
   */
  public abstract void createTable(SQLiteDatabase db);

  /**
   * Drop the database table that we use to record the output from this
   * collector (if it exists).
   */
  public abstract void dropTable(SQLiteDatabase db);
  
  /**
   * Print CSV header with trailing newline.
   * 
   * @param os
   */
  public abstract void printCsvHeader(PrintStream ps);
  
  /**
   * Print data in CSV format.
   * 
   * @param os
   * @param lastId
   * @param maxRecords
   * @return id of the last record in the returned CSV, or lastId if no records
   */
  public abstract long printCsvData(PrintStream ps, long lastId, int maxRecords);

  /**
   * Called by the CollectorService exactly once over the lifetime of the
   * service.
   */
  public void start() {
    // do nothing
  }

  /**
   * Called by the CollectorService exactly once over the lifetime of the
   * service.
   */
  public void stop() {
    // do nothing
  }
}
