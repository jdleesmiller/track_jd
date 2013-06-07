package org.thereflectproject.trackjd.collector;

import org.thereflectproject.trackjd.Constants;
import org.thereflectproject.trackjd.TrackJDService;
import org.thereflectproject.trackjd.data.OrientationPoint;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class OrientationCollector extends AbstractSensorCollector {

  /**
   * In microseconds.
   */
  private static final int DEFAULT_INTERVAL = 500000;

  private final SensorEventListener sensorListener;

  public OrientationCollector(TrackJDService context) {
    super(context);

    sensorListener = new SensorEventListener() {
      private float[] rotationMatrix = new float[16];
      private float[] orientation = new float[3];

      public void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientation);
        logPoint(new OrientationPoint(event.timestamp, orientation));
      }

      public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("OrientationCollector", "orientation accuracy changed: "
            + accuracy);
      }
    };
  }

  @Override
  public void start() {
    getSensorManager().registerListener(
        sensorListener,
        getSensorManager().getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
        getPreferences().getInt(Constants.PREF_ORIENTATION_INTERVAL,
            DEFAULT_INTERVAL));
  }

  @Override
  public void stop() {
    getSensorManager().unregisterListener(sensorListener);
  }
}
