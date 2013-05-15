package org.jdleesmiller.trackjd.collector;

import java.io.PrintStream;

import org.jdleesmiller.trackjd.CollectorService;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GPSCollector extends AbstractCollector {
  private LocationListener locationListener;
  private LocationManager locationManager;

  public static final String TABLE_NAME = "gps";

  public GPSCollector(CollectorService context) {
    super(context);

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
        SQLiteDatabase db = getWritableDb();
        if (db != null) {
          ContentValues values = new ContentValues(5);
          if (location.hasAccuracy())
            values.put("accuracy", location.getAccuracy());
          if (location.hasAltitude())
            values.put("altitude", location.getAltitude());
          values.put("latitude", location.getLatitude());
          values.put("longitude", location.getLongitude());
          values.put("time", location.getTime());
          db.insert(TABLE_NAME, null, values);
        }
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
  public void createTable(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + //
        "gps_id INTEGER PRIMARY KEY AUTOINCREMENT," + //
        "accuracy FLOAT," + //
        "altitude DOUBLE," + //
        "latitude DOUBLE," + //
        "longitude DOUBLE," + //
        "time INTEGER)");
  }

  @Override
  public void dropTable(SQLiteDatabase db) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
  }

  @Override
  public void printCsvHeader(PrintStream ps) {
    ps.print("accuracy,altitude,latitude,longitude,time\n");
  }

  @Override
  public long printCsvData(PrintStream ps, long lastId, int maxRecords) {
    long newLastId = lastId;
    SQLiteDatabase db = getReadableDb();
    if (db != null) {
      Cursor c = db.rawQuery(
          "SELECT gps_id, accuracy, altitude, latitude, longitude, time FROM "
              + TABLE_NAME + " WHERE gps_id > " + lastId, null);
      int records = 0;
      while (c.moveToNext() && records < maxRecords) {
        newLastId = c.getLong(0);
        ps.print(c.getFloat(1));
        ps.print(",");
        ps.print(c.getDouble(2));
        ps.print(",");
        ps.print(c.getDouble(3));
        ps.print(",");
        ps.print(c.getDouble(4));
        ps.print(",");
        ps.print(c.getLong(5));
        ps.print("\n");
        records += 1;
      }
    }
    return newLastId;
  }
}
