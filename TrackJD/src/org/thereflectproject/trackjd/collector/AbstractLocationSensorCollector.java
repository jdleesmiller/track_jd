package org.thereflectproject.trackjd.collector;

import org.thereflectproject.trackjd.TrackJDService;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public abstract class AbstractLocationSensorCollector extends AbstractCollector {

  private final LocationListener locationListener;
  private final LocationManager locationManager;

  public AbstractLocationSensorCollector(TrackJDService service) {
    super(service);

    locationManager = (LocationManager) service
        .getSystemService(Context.LOCATION_SERVICE);

    locationListener = new LocationListener() {
      public void onLocationChanged(Location location) {
        Log.d("LocationSensorCollector", getProvider() + " point");
        collectLocation(location);
      }

      public void onProviderDisabled(String provider) {
        Log.i("LocationSensorCollector", provider + " disabled");
      }

      public void onProviderEnabled(String provider) {
        Log.i("LocationSensorCollector", provider + " enabled");
      }

      public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("LocationSensorCollector", provider + " status changed: "
            + String.valueOf(status));
      }
    };
  }

  /**
   * Requested minimum distance between updates; see the requestLocationUpdates
   * API. Zero means the maximum rate.
   * 
   * @return in meters
   */
  public float getMinDistance() {
    return 0.0f;
  }

  /**
   * Requested minimum time between updates; see the requestLocationUpdates API.
   * Zero means the maximum rate.
   * 
   * @return in milliseconds
   */
  public long getMinTime() {
    return 0;
  }

  /**
   * The name of the location provider to request updates from; see the
   * requestLocationUpdates API.
   * 
   * @return e.g. LocationManager.GPS_PROVIDER
   */
  public abstract String getProvider();

  @Override
  public void start() {
    Log.d("LocationSensorCollector", getProvider() + " enabled: "
        + locationManager.isProviderEnabled(getProvider()));
    locationManager.requestLocationUpdates(getProvider(), getMinTime(),
        getMinDistance(), locationListener);
  }

  @Override
  public void stop() {
    locationManager.removeUpdates(locationListener);
  }

  protected abstract void collectLocation(Location location);
}
