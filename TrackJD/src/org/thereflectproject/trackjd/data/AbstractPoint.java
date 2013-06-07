package org.thereflectproject.trackjd.data;

/**
 * Base class for sensor measurements.
 * 
 * Data points are immutable -- once created, their measurements are not
 * modifiable.
 */
public abstract class AbstractPoint {
  private final long time;

  public AbstractPoint(long time) {
    this.time = time;
  }
  
  /**
   * A tag representing the type of measurement and the version of the schema
   * for this measurement type. For example, "gps_v1" is a measurement from
   * GPS; if we later add or remove measurements, we should refer to those
   * as "gps_v2", etc., to avoid confusion between different data formats. 
   * 
   * @return not null
   */
  public abstract String getTag();
  
  /**
   * The UTC time stamp associated with this measurement. We use UTC time stamps
   * for all measurements, because they're generally precise enough for us,
   * and it makes it easy to compare measurements from different sensors.
   * 
   * @return 
   */
  public long getUTCTime() {
    return time;
  }
  
  /**
   * Print the header row for data in comma-separated value format, without
   * the time and without a trailing newline.
   * 
   * @param ps not null
   */
  public abstract void printCsvHeader(StringBuilder sb);
  
  /**
   * Print the point in comma-separated value format, without the time
   * and without a trailing newline.
   * 
   * @param ps not null
   */
  public abstract void printCsvData(StringBuilder sb);
}
