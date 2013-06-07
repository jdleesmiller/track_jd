package org.thereflectproject.trackjd.data;

/**
 * An orientation measurement.
 */
public class OrientationPoint extends AbstractSensorPoint {
  public OrientationPoint(long eventTimestamp, float [] orientation) {
    super(eventTimestamp, orientation);
  }

  @Override
  public String getTag() {
    return "orient_v1";
  }
  
  @Override
  public void printCsvHeader(StringBuilder sb) {
    sb.append("azimuth,pitch,roll");
  }
  
  @Override
  public void printCsvData(StringBuilder sb) {
    sb.append(values[0]);
    sb.append(',');
    sb.append(values[1]);
    sb.append(',');
    sb.append(values[2]);
  }
}
