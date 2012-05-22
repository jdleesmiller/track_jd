package org.jdleesmiller;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class TrackJDActivity extends Activity {
  private DataLayer dataLayer;
  private DataUploader dataUploader;
  private LocationListener locationListener;
  private LocationManager locationManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    dataLayer = new DataLayer(this);
    
    dataUploader = new DataUploader(this, dataLayer);

    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    locationListener = new LocationListener() {
      public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(getApplicationContext(),
          "status changed: " + String.valueOf(status), Toast.LENGTH_SHORT)
          .show();
      }

      public void onProviderEnabled(String provider) {
        Toast.makeText(getApplicationContext(), "GPS enabled",
          Toast.LENGTH_SHORT).show();
      }

      public void onProviderDisabled(String provider) {
        Toast.makeText(getApplicationContext(), "GPS disabled",
          Toast.LENGTH_SHORT).show();
      }

      public void onLocationChanged(Location location) {
        dataLayer.logGPS(location);
      }
    };
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d("TEST", "ONSTART");

    dataLayer.open();
    dataUploader.start();

    // set both minTime and minDistance to zero for maximum update rate
    long minTime = 0;
    float minDistance = 0;
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
      minTime, minDistance, locationListener);
  }

  @Override
  protected void onStop() {
    Log.d("TEST", "ONSTOP");
    
    locationManager.removeUpdates(locationListener);
    dataUploader.stop();
    dataLayer.close();
    super.onStop();
  }

  public void clickStop(View view) {
    this.finish();
  }
}

// note: can get NMEA data directly (but still have to register the
// location listener as above)
// locationManager.addNmeaListener(new NmeaListener() {
// public void onNmeaReceived(long timestamp, String nmea) {
// Log.d("NMEA", nmea);
// }
// });