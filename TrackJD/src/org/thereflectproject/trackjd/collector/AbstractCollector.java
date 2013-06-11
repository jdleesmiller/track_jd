package org.thereflectproject.trackjd.collector;

import org.thereflectproject.trackjd.Constants;
import org.thereflectproject.trackjd.TrackJDService;
import org.thereflectproject.trackjd.data.AbstractPoint;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A collector reads data from a particular sensor and sends it to the
 * DataLogger.
 */
public abstract class AbstractCollector {

  private final TrackJDService context;
  private final SharedPreferences prefs;

  public AbstractCollector(TrackJDService service) {
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
   * Called by subclasses when they have a new data point to record. 
   * 
   * @param point not null
   */
  protected void logPoint(AbstractPoint point) {
    this.context.getDataLogger().log(point);
  }

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
