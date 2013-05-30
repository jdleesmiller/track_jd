/**
 * 
 */
package org.jdleesmiller.trackjd;

import java.util.Arrays;
import java.util.List;

import org.jdleesmiller.trackjd.collector.AbstractCollector;
import org.jdleesmiller.trackjd.collector.AccelerometerCollector;
import org.jdleesmiller.trackjd.collector.BluetoothCollector;
import org.jdleesmiller.trackjd.collector.GPSCollector;
import org.jdleesmiller.trackjd.collector.OrientationCollector;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Background service for collecting data from a collection of sensors.
 */
public class TrackJDService extends Service {
  private boolean started;

  private GPSCollector gpsCollector;
  private AccelerometerCollector accelerometerCollector;
  private OrientationCollector orientationCollector;
  private BluetoothCollector bluetoothCollector;
  private List<AbstractCollector> collectors;
  private SQLiteHelper dbHelper;
  private SQLiteDatabase writableDb;
  private SQLiteDatabase readableDb;
  private DataLogger dataLogger;
  private DataUploader dataUploader;

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
   * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i("CollectorService", "onStartCommand");
    // make sure we only start the collectors once
    if (!started) {
      start();
    }
    started = true;

    return START_STICKY;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Service#onDestroy()
   */
  @Override
  public void onDestroy() {
    Log.i("CollectorService", "onDestroy");
    stop();
  }
  
  /**
   * Binder class used by activities to get data to/from this service.
   */
  public class LocalBinder extends Binder {
    public TrackJDService getService() {
      return TrackJDService.this;
    }
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
  
  /**
   * Call when writing data to the database. Do *not* close this; it is
   * closed only when the service stops.
   * 
   * @return null when service is stopping (or write access not available)
   */
  public SQLiteDatabase getWritableDb() {
    return writableDb;
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
    
    readableDb.close();
    readableDb = null;
    writableDb.close();
    writableDb = null;
  }
  
  /**
   * @return not null
   */
  public DataLogger getDataLogger() {
    return dataLogger;
  }

  /**
   * @return not null; not empty
   */
  public List<AbstractCollector> getCollectors() {
    return collectors;
  }
}
