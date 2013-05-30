package org.jdleesmiller.trackjd;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TrackJDApplication {
  private Context context;
  
  private static TrackJDApplication instance;
  
  public static void startIfNotRunning(Context context) {
    if (instance == null) {
      instance = new TrackJDApplication(context);
      instance.start();
    }
  }
  
  public static void stopIfRunning() {
    if (instance != null) {
      instance.stop();
      instance = null;
    }
  }
  
  private TrackJDApplication(Context context) {
    this.context = context;
  }
  
  private void start() {
    Log.d("TrackJDApplication", "start");
    context.startService(new Intent(context, TrackJDService.class));
  }

  public void stop() {
    Log.d("TrackJDApplication", "stop");
    context.stopService(new Intent(context, TrackJDService.class));
  }
}
