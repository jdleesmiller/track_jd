package org.thereflectproject.trackjd.collector;

import org.thereflectproject.trackjd.TrackJDService;

import android.content.Context;
import android.hardware.SensorManager;

/**
 * Common features for collecting from sensors that use a SensorManager.
 * 
 * These sensors register a SensorEventListener that requests updates at a given
 * rate. Note that the given rate between collections is just a hint -- I've
 * seen sensors deliver points much faster and much slower than the configured
 * rate for short periods, and we don't currently make any effort to rate limit.
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
