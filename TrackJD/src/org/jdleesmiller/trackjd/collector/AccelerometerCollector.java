package org.jdleesmiller.trackjd.collector;

import org.jdleesmiller.trackjd.TrackJDService;
import org.jdleesmiller.trackjd.Constants;
import org.jdleesmiller.trackjd.data.AccelerometerPoint;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class AccelerometerCollector extends AbstractSensorCollector {

  private SensorEventListener sensorListener;
  
  /**
   * In microseconds. Maximum update rate appear to be 50Hz, which is pretty
   * fast, so we default lower.
   */
  private static final int DEFAULT_INTERVAL = 250000;

  public AccelerometerCollector(TrackJDService context) {
    super(context);

    sensorListener = new SensorEventListener() {
      public void onSensorChanged(SensorEvent event) {
        logPoint(new AccelerometerPoint(event));
      }

      public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("AccelerometerCollector", "accelerometer accuracy changed: "
            + accuracy);
      }
    };
  }

  @Override
  public void start() {
    getSensorManager().registerListener(
        sensorListener,
        getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        getPreferences().getInt(Constants.PREF_ACCELEROMETER_INTERVAL,
            DEFAULT_INTERVAL));
  }

  @Override
  public void stop() {
    getSensorManager().unregisterListener(sensorListener);
  }
}
