package org.jdleesmiller.trackjd.data;

import android.hardware.SensorEvent;

/**
 * An accelerometer measurement.
 */
public class AccelerometerPoint extends AbstractSensorPoint {
  public AccelerometerPoint(SensorEvent event) {
    super(event);
  }

  @Override
  public String getTag() {
    return "accel_v1";
  }
  
  @Override
  public void printCsvHeader(StringBuilder sb) {
    sb.append("x,y,z");
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
