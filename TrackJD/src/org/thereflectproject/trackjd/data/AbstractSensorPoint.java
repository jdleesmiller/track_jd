package org.thereflectproject.trackjd.data;

import java.util.Arrays;

import android.hardware.SensorEvent;

/**
 * A data point from a SensorEvent. These events all store their data in an
 * array of floats and have a particular time stamp format, which we want to map
 * into UTC time stamps for consistency with other sensors.
 * 
 * A handy reference:
 * http://developer.android.com/reference/android/hardware/SensorEvent.html
 * 
 * I have never seen it documented, but I have circumstantial evidence that the
 * values array on a SensorEvent may be a reference to an internal buffer. That
 * means that if we just keep a reference to the values array, we sometimes get
 * data that looks like it's been overwritten with something else. It is
 * therefore recommended to store a copy of the values array, rather than a
 * reference to the values array provided with the SensorEvent object. The
 * constructor that takes a SensorEvent enforces this practice, but the other
 * constructor does not.
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

  /**
   * Constructor.
   * 
   * @param eventTimestamp
   * @param values
   *          this is NOT copied -- see notes at top of class
   */
  public AbstractSensorPoint(long eventTimestamp, float[] values) {
    super(eventTimestampToUTC(eventTimestamp));
    this.values = values;
  }

  /**
   * Constructor. This copies the given event's values array to our own values
   * array; see notes at top of class.
   * 
   * @param event
   */
  public AbstractSensorPoint(SensorEvent event) {
    this(event.timestamp, Arrays.copyOf(event.values, event.values.length));
  }
}
