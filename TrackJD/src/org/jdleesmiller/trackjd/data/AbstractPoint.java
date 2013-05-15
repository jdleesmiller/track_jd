package org.jdleesmiller.trackjd.data;

import java.io.PrintStream;

/**
 * Base class for sensor measurements.
 */
public abstract class AbstractPoint {
  private final long time;

  public AbstractPoint(long time) {
    this.time = time;
  }
  
  long getUTCTime() {
    return time;
  }
  
  /**
   * Print the header row for data in comma-separated value format, without
   * a trailing newline.
   * 
   * @param ps
   */
  public void printCsvHeader(PrintStream ps) {
    ps.print("time");
  }
  
  /**
   * Print the point in comma-separated value format, without a trailing
   * newline.
   * 
   * @param ps
   */
  public void printCsvData(PrintStream ps) {
    ps.print(time);
  }
}
