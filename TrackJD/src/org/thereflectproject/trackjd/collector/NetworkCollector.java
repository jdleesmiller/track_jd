package org.thereflectproject.trackjd.collector;

import org.thereflectproject.trackjd.TrackJDService;
import org.thereflectproject.trackjd.data.NetworkPoint;

import android.location.Location;
import android.location.LocationManager;

/**
 * Poll the network location provider (typically cell tower or skyhook). See
 * NetworkPoint for the data format. Most of the work is taken care of by the
 * AbstractLocationSensorCollector.
 */
public class NetworkCollector extends AbstractLocationSensorCollector {
  
  public NetworkCollector(TrackJDService service) {
    super(service);
  }

  @Override
  public String getProvider() {
    return LocationManager.NETWORK_PROVIDER;
  }

  @Override
  protected void collectLocation(Location location) {
    logPoint(new NetworkPoint(location));
  }
}
