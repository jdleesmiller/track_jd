package org.jdleesmiller;

import android.content.Context;
import android.util.Log;

public class TrackJDApplication {
  private final DataCollector dataCollector;
  private final DataUploader dataUploader;
  
  private static DataLayer dataLayer;
  
  private static TrackJDApplication instance;
  
  public static DataLayer dataLayer(Context context) {
    // only start the data layer once; we never have to stop it
    if (dataLayer == null) {
      dataLayer = new DataLayer(context);
      dataLayer.open();
    }
    return dataLayer;
  }
  
  public static void startIfNotRunning(Context context) {
    if (instance == null) {
      dataLayer(context);
      instance = new TrackJDApplication(context);
      instance.start();
    }
  }
  
  public static void stopIfRunning() {
    // NB: must never close the dataLayer -- the OS handles this
    if (instance != null) {
      instance.stop();
      instance = null;
    }
  }
  
  private TrackJDApplication(Context context) {
    dataCollector = new DataCollector(context, dataLayer);

    dataUploader = new DataUploader(context, dataLayer);
  }
  
  private void start() {
    Log.d("TrackJDApplication", "start");
    dataCollector.start();
    dataUploader.start();
  }

  public void stop() {
    Log.d("TrackJDApplication", "stop");
    dataCollector.stop();
    dataUploader.stop();
  }
}
