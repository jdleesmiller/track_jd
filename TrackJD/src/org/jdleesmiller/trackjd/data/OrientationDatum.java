package org.jdleesmiller.trackjd.data;

import java.io.PrintStream;

import android.hardware.SensorEvent;

public class OrientationDatum extends AbstractSensorPoint {
  public OrientationDatum(SensorEvent event) {
    super(event);
  }
  
  @Override
  void printCsvHeader(PrintStream ps) {
    super.printCsvHeader(ps);
    ps.print(",x,y,z");
  }
  
  @Override
  void printCsvData(PrintStream ps) {
    super.printCsvData(ps);
    ps.print(',');
    ps.print(values[0]);
    ps.print(',');
    ps.print(values[1]);
    ps.print(',');
    ps.print(values[2]);
  }

}
