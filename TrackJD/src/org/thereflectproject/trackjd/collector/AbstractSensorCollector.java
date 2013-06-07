package org.thereflectproject.trackjd.collector;

import org.thereflectproject.trackjd.TrackJDService;

import android.content.Context;
import android.hardware.SensorManager;

/**
 * Common features for sensors that use a SensorManager.
 */
public abstract class AbstractSensorCollector extends AbstractCollector {

  private SensorManager sensorManager;

  public AbstractSensorCollector(TrackJDService context) {
    super(context);
    
    sensorManager = (SensorManager) context
        .getSystemService(Context.SENSOR_SERVICE);
  }

  /**
   * @return the sensorManager
   */
  public SensorManager getSensorManager() {
    return sensorManager;
  }
}
