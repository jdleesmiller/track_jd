package org.jdleesmiller.trackjd.collector;

import org.jdleesmiller.trackjd.TrackJDService;

import android.content.Context;
import android.hardware.SensorManager;

/**
 * Common features for reading from sensors.
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
