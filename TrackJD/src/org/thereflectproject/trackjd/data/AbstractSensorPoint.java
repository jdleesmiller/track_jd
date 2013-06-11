package org.thereflectproject.trackjd.data;

import android.hardware.SensorEvent;

/**
 * A data point from a SensorEvent. These events all store their data in an
 * array of floats and have a particular time stamp format, which we want
 * to map into UTC time stamps for consistency with other sensors.
 */
public abstract class AbstractSensorPoint extends AbstractPoint {
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
  protected static long eventTimestampToUTC(long timestamp) {
    return System.currentTimeMillis() + (timestamp - System.nanoTime())
        / 1000000L;
  }

  protected final float[] values;

  public AbstractSensorPoint(long eventTimestamp, float[] values) {
    super(eventTimestampToUTC(eventTimestamp));
    this.values = values;
  }

  public AbstractSensorPoint(SensorEvent event) {
    this(event.timestamp, event.values);
  }
}
