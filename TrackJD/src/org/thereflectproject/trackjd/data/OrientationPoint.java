package org.thereflectproject.trackjd.data;

/**
 * An orientation measurement. The orientation is measured in terms of azimuth,
 * pitch and roll in the coordinate system defined for this Android API method:
 * http://developer.android.com/reference/android/hardware/SensorManager.html#getOrientation(float[],%20float[])
 */
public class OrientationPoint extends AbstractSensorPoint {
  public OrientationPoint(long eventTimestamp, float [] orientation) {
    super(eventTimestamp, orientation);
  }

  @Override
  public String getTag() {
    return "orient_v1";
  }
   
  /**
   * Format: azimuth, pitch, roll.
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
