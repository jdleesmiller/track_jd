package org.jdleesmiller.trackjd;

import com.loopj.android.http.*;

import java.util.ArrayList;
import java.util.List;

import org.jdleesmiller.trackjd.collector.AbstractCollector;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

public class DataUploader implements Runnable {

  /**
   * This is used for both the connection timeout (when trying to make the
   * connection, wait this long before giving up) and the socket timeout (once a
   * connection is made, wait this long for the upload to finish -- that is, for
   * us to receive the response). The timeouts are important, because Internet
   * connectivity will often be intermittent, and we have to retry reasonably
   * quickly.
   */
  private static final int TIMEOUT_MILLIS = 5000;

  /**
   * Wait this long before trying to upload.
   */
  private static final long UPLOAD_INTERVAL_MILLIS = 5000;

  /**
   * Max number of records to upload at once for any single sensor.
   */
  private static final int MAX_RECORDS_TO_UPLOAD = 100;

  private final TrackJDService service;

  private final SharedPreferences prefs;

  private final Handler uploadHandler;

  public DataUploader(TrackJDService service) {
    this.service = service;
    this.prefs = service.getSharedPreferences(Constants.PREFS_FILE_NAME,
        Context.MODE_PRIVATE);
    this.uploadHandler = new Handler();
  }

  public void start() {
    uploadHandler.postDelayed(this, UPLOAD_INTERVAL_MILLIS);
  }

  public void stop() {
    uploadHandler.removeCallbacks(this);
  }

  /**
   * @return fully qualified URI to post data to
   */
  public String getLogPath() {
    String serverName = prefs.getString(Constants.PREF_SERVER_NAME, "");
    return "http://" + serverName + "/log";
  }

  public void run() {
    AsyncHttpClient client = new AsyncHttpClient();
    client.setTimeout(TIMEOUT_MILLIS);
    RequestParams params = new RequestParams();
    final List<AsyncHttpResponseHandler> callbacks = new ArrayList<AsyncHttpResponseHandler>();
    for (AbstractCollector collector : service.getCollectors()) {
      callbacks.add(collector.upload(params, MAX_RECORDS_TO_UPLOAD));
    }
    getDeviceIdentifiers(params);
    client.post(service, getLogPath(), params, new AsyncHttpResponseHandler() {
      @Override
      public void onSuccess(String response) {
        // let collectors delete the data that we just uploaded 
        for (AsyncHttpResponseHandler callback : callbacks) {
          callback.onSuccess(response);
        }
      }

      @Override
      public void onFinish() {
        // always keep polling
        uploadHandler.postDelayed(DataUploader.this, UPLOAD_INTERVAL_MILLIS);
      }
    });
  }

  /**
   * Try to get as many unique device identifiers as we can. I assume that these
   * are cheap to get, so it isn't too bad to compute them on every post. They
   * should not change, but they can become known and unknown -- e.g. if
   * Bluetooth is not turned on, the bdaddr comes back as "<UNKNOWN>". If we
   * can't get one, try to continue running -- we have several device
   * identifiers that we can use.
   * 
   * @param postData
   */
  public void getDeviceIdentifiers(RequestParams params) {
    params.put("installation", Installation.id(service));

    try {
      params.put("bdaddr", BluetoothAdapter.getDefaultAdapter().getAddress());
    } catch (RuntimeException e) {
      Log.w("DataUploader", "failed to get Bluetooth MAC address");
    }

    try {
      WifiManager wifiManager = (WifiManager) service
          .getSystemService(Context.WIFI_SERVICE);
      params.put("macaddr", wifiManager.getConnectionInfo().getMacAddress());
    } catch (RuntimeException e) {
      Log.w("DataUploader", "failed to get WiFi MAC address");
    }
  }
}
