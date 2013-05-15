package org.jdleesmiller.trackjd.collector;

import org.jdleesmiller.trackjd.TrackJDService;
import org.jdleesmiller.trackjd.Constants;
import org.jdleesmiller.trackjd.data.OrientationDatum;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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

  private static final int MAX_DATA = 100;

  private final CollectorBuffer<OrientationDatum> buffer;

  private final SensorEventListener sensorListener;

  public OrientationCollector(TrackJDService context) {
    super(context);

    buffer = new CollectorBuffer<OrientationDatum>(MAX_DATA);

    sensorListener = new SensorEventListener() {
      private float[] rotationMatrix = new float[16];
      private float[] orientation = new float[3];

      public void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientation);
        buffer.store(new OrientationDatum(event.timestamp, orientation));
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

  @Override
  public AsyncHttpResponseHandler upload(RequestParams params,
      int maxDataToUpload) {
    final int dataUploaded = buffer.addCsvToPost("orient", params,
        maxDataToUpload);
    return new AsyncHttpResponseHandler() {
      @Override
      public void onSuccess(String arg0) {
        buffer.clear(dataUploaded);
      }
    };
  }
}
