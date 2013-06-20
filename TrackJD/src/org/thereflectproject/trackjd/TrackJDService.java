package org.thereflectproject.trackjd;

import java.util.Arrays;
import java.util.List;

import org.thereflectproject.trackjd.collector.AbstractCollector;
import org.thereflectproject.trackjd.collector.AccelerometerCollector;
import org.thereflectproject.trackjd.collector.BluetoothCollector;
import org.thereflectproject.trackjd.collector.GPSCollector;
import org.thereflectproject.trackjd.collector.OrientationCollector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * The main background service. This coordinates the DataLogger, DataUploader
 * and the Collectors for the sensors. The 
 */
public class TrackJDService extends Service {
  /**
   * Binder class used by activities to get data to/from this service.
   */
  public class LocalBinder extends Binder {
    public TrackJDService getService() {
      return TrackJDService.this;
    }
  }
  
  /**
   * Start the background service. This can be called multiple times, even
   * if the service is already running -- subsequent starts will have no
   * effect.
   * 
   * @param context not null
   */
  public static void startBackgroundService(Context context) {
    Log.d("TrackJDService", "startBackgroundService");
    context.startService(new Intent(context, TrackJDService.class)); 
  }
  
  /**
   * Stop the background service.
   * 
   * @param context not null
   */
  public static void stopBackgroundService(Context context) {
    Log.d("TrackJDService", "stopBackgroundService");
    context.stopService(new Intent(context, TrackJDService.class));
 }

  private AccelerometerCollector accelerometerCollector;
  private BluetoothCollector bluetoothCollector;
  private List<AbstractCollector> collectors;
  private DataLogger dataLogger;
  private DataUploader dataUploader;
  private SQLiteHelper dbHelper;
  private GPSCollector gpsCollector;
  private OrientationCollector orientationCollector;
  private SQLiteDatabase readableDb;
  private boolean started;

  private SQLiteDatabase writableDb;
  
  /**
   * @return not null; not empty
   */
  public List<AbstractCollector> getCollectors() {
    return collectors;
  }

  /**
   * @return not null
   */
  public DataLogger getDataLogger() {
    return dataLogger;
  }
  
  /**
   * Call when reading data from the database. Do *not* close this; it is
   * closed only when the service stops.
   * 
   * @return null when service is stopping
   */
  public SQLiteDatabase getReadableDb() {
    return readableDb;
  }

  /**
   * Call when writing data to the database. Do *not* close this; it is
   * closed only when the service stops.
   * 
   * @return null when service is stopping (or write access not available)
   */
  public SQLiteDatabase getWritableDb() {
    return writableDb;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see android.app.Service#onBind(android.content.Intent)
   */
  @Override
  public IBinder onBind(Intent intent) {
    return new LocalBinder();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Service#onCreate()
   */
  @Override
  public void onCreate() {
    started = false;

    dbHelper = new SQLiteHelper(this);
    
    dataLogger = new DataLogger(this);
    dataUploader = new DataUploader(this);

    gpsCollector = new GPSCollector(this);
    accelerometerCollector = new AccelerometerCollector(this);
    orientationCollector = new OrientationCollector(this);
    bluetoothCollector = new BluetoothCollector(this);

    collectors = Arrays.asList(gpsCollector, accelerometerCollector,
        orientationCollector, bluetoothCollector);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Service#onDestroy()
   */
  @Override
  public void onDestroy() {
    stop();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // make sure we only start the collectors once
    if (!started) {
      start();
    }
    started = true;

    return START_STICKY;
  }
  
  private void start() {
    writableDb = dbHelper.getWritableDatabase();
    readableDb = dbHelper.getWritableDatabase();
    
    for (AbstractCollector collector : collectors) {
      collector.start();
    }
    dataUploader.start();
  }

  private void stop() {
    for (AbstractCollector collector : collectors) {
      collector.stop();
    }
    dataUploader.stop();
    dataLogger.stop();
    
    readableDb.close();
    readableDb = null;
    writableDb.close();
    writableDb = null;
  }
}
