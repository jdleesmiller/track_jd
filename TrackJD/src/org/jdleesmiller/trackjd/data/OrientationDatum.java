package org.jdleesmiller.trackjd.data;

import java.io.PrintStream;

/**
 * An orientation measurement.
 */
public class OrientationDatum extends AbstractSensorPoint {
  public OrientationDatum(long eventTimestamp, float [] orientation) {
    super(eventTimestamp, orientation);
  }
  
  @Override
  public void printCsvHeader(PrintStream ps) {
    super.printCsvHeader(ps);
    ps.print(",azimuth,pitch,roll");
  }
  
  @Override
  public void printCsvData(PrintStream ps) {
    super.printCsvData(ps);
    ps.print(',');
    ps.print(values[0]);
    ps.print(',');
    ps.print(values[1]);
    ps.print(',');
    ps.print(values[2]);
  }
}
