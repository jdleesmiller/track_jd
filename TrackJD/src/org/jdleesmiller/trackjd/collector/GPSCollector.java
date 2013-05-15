package org.jdleesmiller.trackjd.collector;

import org.jdleesmiller.trackjd.TrackJDService;
import org.jdleesmiller.trackjd.data.GPSPoint;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GPSCollector extends AbstractCollector {
  private static final int MAX_DATA = 100;

  private final CollectorBuffer<GPSPoint> buffer;
  private final LocationListener locationListener;
  private final LocationManager locationManager;

  public GPSCollector(TrackJDService context) {
    super(context);

    buffer = new CollectorBuffer<GPSPoint>(MAX_DATA);

    locationManager = (LocationManager) context
        .getSystemService(Context.LOCATION_SERVICE);

    locationListener = new LocationListener() {
      public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("DataCollector", "status changed: " + String.valueOf(status));
      }

      public void onProviderEnabled(String provider) {
        Log.i("DataCollector", "GPS provider enabled");
      }

      public void onProviderDisabled(String provider) {
        Log.i("DataCollector", "GPS provider disabled");
      }

      public void onLocationChanged(Location location) {
        buffer.store(new GPSPoint(location));
      }
    };
  }

  @Override
  public void start() {
    // set both minTime and minDistance to zero for maximum update rate;
    // this is 1Hz on the device I'm using, which is about right
    long minTime = 0;
    float minDistance = 0;

    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        minTime, minDistance, locationListener);
  }

  @Override
  public void stop() {
    locationManager.removeUpdates(locationListener);
  }

  @Override
  public AsyncHttpResponseHandler upload(RequestParams params,
      int maxDataToUpload) {
    final int dataUploaded = buffer
        .addCsvToPost("gps", params, maxDataToUpload);
    return new AsyncHttpResponseHandler() {
      @Override
      public void onSuccess(String arg0) {
        buffer.clear(dataUploaded);
      }
    };
  }
}
