package org.thereflectproject.trackjd.data;

import android.location.Location;

/**
 * A fix from a coarse network location sensor.
 * 
 * The docs suggest that the time of the fix, which is available from
 * location.getTime(), can be very different from the time at which the sensor
 * delivers the fix -- presumably it can give you old fixes. So, to see whether
 * this is significant, we record both the fix time and the time at which the
 * point was recorded. It may be that this isn't actually important at all, in
 * which case we can just record one of the timestamps.
 */
public class NetworkPoint extends AbstractPoint {
  private final long eventTime;
  private final float accuracy;
  private final double latitude;
  private final double longitude;

  public NetworkPoint(Location location) {
    super(location.getTime());

    this.eventTime = System.currentTimeMillis();
    this.latitude = location.getLatitude();
    this.longitude = location.getLongitude();

    if (location.hasAccuracy()) {
      this.accuracy = location.getAccuracy();
    } else {
      this.accuracy = Float.NaN;
    }
  }

  @Override
  public String getTag() {
    return "network_v1";
  }

  @Override
  public void printCsvData(StringBuilder sb) {
    sb.append(eventTime);
    sb.append(',');
    sb.append(latitude);
    sb.append(',');
    sb.append(longitude);
    sb.append(',');
    sb.append(accuracy);
  }
}
