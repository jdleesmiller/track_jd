package org.jdleesmiller.trackjd.collector;

import org.jdleesmiller.trackjd.TrackJDService;
import org.jdleesmiller.trackjd.Constants;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A collector reads data from a particular sensor and stores it in a buffer.
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
   * Upload buffered data collected for this sensor.
   * 
   * @param params
   * @param maxDataToUpload
   *          maximum number of data points to upload
   */
  public abstract AsyncHttpResponseHandler upload(RequestParams params,
      int maxDataToUpload);

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
