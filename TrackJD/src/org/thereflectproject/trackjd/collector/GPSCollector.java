package org.thereflectproject.trackjd.collector;

import org.thereflectproject.trackjd.TrackJDService;
import org.thereflectproject.trackjd.data.GPSPoint;

import android.location.Location;
import android.location.LocationManager;

/**
 * Poll the GPS. See GPSPoint for the data format. Most of the work is taken
 * care of by the AbstractLocationSensorCollector.
 */
public class GPSCollector extends AbstractLocationSensorCollector {

  public GPSCollector(TrackJDService service) {
    super(service);
  }

  @Override
  public String getProvider() {
    return LocationManager.GPS_PROVIDER;
  }

  @Override
  protected void collectLocation(Location location) {
    logPoint(new GPSPoint(location));
  }
}
