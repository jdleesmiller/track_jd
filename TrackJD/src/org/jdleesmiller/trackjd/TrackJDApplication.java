package org.jdleesmiller.trackjd;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TrackJDApplication {
  //private final DataCollector dataCollector;
  //private final DataUploader dataUploader;

  private Context context;
  
  private static TrackJDApplication instance;
  
  public static void startIfNotRunning(Context context) {
    if (instance == null) {
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
    this.context = context;
    
    //dataCollector = new DataCollector(context, dataLayer);
    //dataUploader = new DataUploader(context, dataLayer);
  }
  
  private void start() {
    Log.d("TrackJDApplication", "start");
    context.startService(new Intent(context, CollectorService.class));
    //dataCollector.start();
    //dataUploader.start();
  }

  public void stop() {
    Log.d("TrackJDApplication", "stop");
    context.stopService(new Intent(context, CollectorService.class));
    //dataCollector.stop();
    //dataUploader.stop();
  }
}
