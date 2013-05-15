package org.jdleesmiller.trackjd.data;

import android.hardware.SensorEvent;

/**
 * A data point from a SensorEvent. 
 */
public abstract class AbstractSensorPoint extends AbstractPoint {
  protected final float [] values;
  
  public AbstractSensorPoint(SensorEvent event) {
    super(eventTimestampToUTC(event.timestamp));
    this.values = event.values;
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
  protected static long eventTimestampToUTC(long timestamp) {
    return System.currentTimeMillis() + (timestamp - System.nanoTime())
      / 1000000L;
  }
}
