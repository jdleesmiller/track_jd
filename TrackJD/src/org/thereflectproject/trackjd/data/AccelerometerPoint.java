package org.thereflectproject.trackjd.data;

import android.hardware.SensorEvent;

/**
 * A three-axis accelerometer measurement. The measurements are in the
 * coordinate system of the phone -- see
 * http://developer.android.com/reference/android/hardware/SensorEvent.html#values
 * for details.
 */
public class AccelerometerPoint extends AbstractSensorPoint {
  public AccelerometerPoint(SensorEvent event) {
    super(event);
  }

  @Override
  public String getTag() {
    return "accel_v1";
  }
  
  /**
   * Format is x, y, z.
   * 
   * @see org.thereflectproject.trackjd.data.AbstractPoint#printCsvData(java.lang.StringBuilder)
   */
  @Override
  public void printCsvData(StringBuilder sb) {
    sb.append(values[0]);
    sb.append(',');
    sb.append(values[1]);
    sb.append(',');
    sb.append(values[2]);
  }
}
