package org.jdleesmiller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class DataCollector {
  private final Context context;
  private final SharedPreferences prefs;
  private final LocationManager locationManager;
  private final LocationListener locationListener;
  private final SensorManager sensorManager;
  private final SensorEventListener accelerometerListener;
  private final SensorEventListener orientationListener;
  private final BluetoothAdapter bluetoothAdapter;
  private final BroadcastReceiver bluetoothReceiver;
  private BroadcastReceiver bluetoothDiscoveryFinishedReceiver;

  /**
   * In microseconds. Maximum update rate appear to be 50Hz, which is pretty
   * fast, so we default lower.
   */
  private static final int DEFAULT_ACCELEROMETER_INTERVAL = 250000;

  /**
   * In microseconds.
   */
  private static final int DEFAULT_ORIENTATION_INTERVAL = 500000;

  public DataCollector(Context context, final DataLayer dataLayer) {
    this.context = context;

    prefs = context.getSharedPreferences(Constants.PREFS_FILE_NAME,
      Context.MODE_PRIVATE);

    locationManager = (LocationManager) context
      .getSystemService(Context.LOCATION_SERVICE);

    locationListener = new LocationListener() {
      public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("DataCollector", "status changed: " + String.valueOf(status));
      }

      public void onProviderEnabled(String provider) {
        Log.i("DataCollector", "GPS provider enabled");
      }

      public void onProviderDisabled(String provider) {
        Log.i("DataCollector", "GPS provider disabled");
      }

      public void onLocationChanged(Location location) {
        dataLayer.logGPS(location);
      }
    };

    sensorManager = (SensorManager) context
      .getSystemService(Context.SENSOR_SERVICE);

    accelerometerListener = new SensorEventListener() {
      public void onSensorChanged(SensorEvent event) {
        dataLayer.logAccelerometer(event.values[0], event.values[1],
          event.values[2], eventTimestampToUTC(event.timestamp));
      }

      public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("DataCollector", "accelerometer accuracy changed" + accuracy);
      }
    };

    orientationListener = new SensorEventListener() {
      private float[] rotationMatrix = new float[16];
      private float[] orientation = new float[3];

      public void onSensorChanged(SensorEvent event) {
        // this seems to crash sometimes, but I haven't yet figured out why
        try {
          SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
          SensorManager.getOrientation(rotationMatrix, orientation);
          dataLayer.logOrientation(orientation[0], orientation[1],
            orientation[2], eventTimestampToUTC(event.timestamp));
        } catch(RuntimeException e) {
          Log.e("DataCollector", "failed to get orientation");
          e.printStackTrace();
          return;
        }
      }

      public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("DataCollector", "orientation accuracy changed: " + accuracy);
      }
    };

    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    bluetoothReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
          BluetoothDevice device = intent
            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
          short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,
            Short.MIN_VALUE);
          dataLayer.logBluetooth(device.getAddress(), rssi);
        }
      }
    };

    // when a scan finishes, start a new one
    bluetoothDiscoveryFinishedReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent
          .getAction())) {
          bluetoothAdapter.startDiscovery();
        }
      }
    };
  }

  public void start() {
    // set both minTime and minDistance to zero for maximum update rate;
    // this is 1Hz on the device I'm using, which is about right
    long minTime = 0;
    float minDistance = 0;
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
      minTime, minDistance, locationListener);

    sensorManager.registerListener(accelerometerListener, sensorManager
      .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), prefs.getInt(
      Constants.PREF_ACCELEROMETER_INTERVAL, DEFAULT_ACCELEROMETER_INTERVAL));

    sensorManager.registerListener(orientationListener, sensorManager
      .getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), prefs.getInt(
      Constants.PREF_ORIENTATION_INTERVAL, DEFAULT_ORIENTATION_INTERVAL));

    if (bluetoothAdapter.isEnabled()) {
      context.registerReceiver(bluetoothReceiver, new IntentFilter(
        BluetoothDevice.ACTION_FOUND));
      context.registerReceiver(bluetoothDiscoveryFinishedReceiver,
        new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
      bluetoothAdapter.startDiscovery();
    }
  }

  public void stop() {
    locationManager.removeUpdates(locationListener);
    sensorManager.unregisterListener(accelerometerListener);
    sensorManager.unregisterListener(orientationListener);
    context.unregisterReceiver(bluetoothReceiver);
    context.unregisterReceiver(bluetoothDiscoveryFinishedReceiver);
  }

  /**
   * The sensor events give us a timestamp with nanosecond precision, but the
   * nanoseconds are referenced to an arbitrary origin (some threads on the
   * Internet say that it is the system uptime). Convert it to something more
   * useful. The approach here is based on
   * http://stackoverflow.com/questions/5500765
   * /accelerometer-sensorevent-timestamp
   * 
   * @param timestamp
   * @return
   */
  private long eventTimestampToUTC(long timestamp) {
    return System.currentTimeMillis() + (timestamp - System.nanoTime())
      / 1000000L;
  }
}

// note: can get NMEA data directly (but still have to register the
// location listener as above)
// locationManager.addNmeaListener(new NmeaListener() {
// public void onNmeaReceived(long timestamp, String nmea) {
// Log.d("NMEA", nmea);
// }
// });