package org.jdleesmiller.trackjd;

import com.loopj.android.http.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.jdleesmiller.trackjd.collector.AbstractCollector;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class DataUploader implements Runnable {

  /**
   * This is used for both the connection timeout (when trying to make the
   * connection, wait this long before giving up) and the socket timeout (once a
   * connection is made, wait this long for the upload to finish -- that is,
   * for us to receive the response).
   * The timeouts are important, because Internet connectivity will often be
   * intermittent, and we have to retry reasonably quickly.
   */
  private static final int TIMEOUT_MILLIS = 5000;

  /**
   * Wait this long before trying to upload.
   */
  private static final long UPLOAD_INTERVAL_MILLIS = 5000;

  /**
   * Max number of records to upload at once.
   */
  private static final int MAX_RECORDS_TO_UPLOAD = 100;

  private final CollectorService service;

  private final SharedPreferences prefs;

  private final Handler uploadHandler;

  public DataUploader(CollectorService service) {
    this.service = service;
    this.prefs = service.getSharedPreferences(Constants.PREFS_FILE_NAME,
        Context.MODE_PRIVATE);
    this.uploadHandler = new Handler();
  }

  public void start() {
    // reset the last uploaded record ids (don't upload stale data on restart)
    // for (AbstractCollector collector : this.service.getCollectors()) {
    // collector.
    //
    // }
    // lastGPSRecordId = dataLayer.getMaxGPSId();
    // lastAccelerometerRecordId = dataLayer.getMaxAccelerometerRecordId();
    // lastOrientationRecordId = dataLayer.getMaxOrientationRecordId();
    // lastBluetoothRecordId = dataLayer.getMaxBluetoothRecordId();

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
    client.cancelRequests(arg0, arg1)
    client.setTimeout(TIMEOUT_MILLIS);
    RequestParams params = new RequestParams();
    params.put("gps", "blah");
    client.post(getLogPath(), params, new AsyncHttpResponseHandler() {
      @Override
      public void onSuccess(String response) {
        // delete local data
      }
      
      @Override
      public void onFinish() {
        // poll again
      }
    });
    

    // List<NameValuePair> postData = new ArrayList<NameValuePair>();
    //
    // // fill in the post data
    // ByteArrayOutputStream buf = new ByteArrayOutputStream();
    // final long newLastGPSRecordId = dataLayer.getGPSAsCSV(buf,
    // lastGPSRecordId,
    // MAX_RECORDS_TO_UPLOAD);
    // if (newLastGPSRecordId != lastGPSRecordId)
    // postData.add(new BasicNameValuePair("gps", buf.toString()));
    //
    // buf.reset();
    // final long newLastAccelerometerRecordId =
    // dataLayer.getAccelerometerAsCSV(
    // buf, lastAccelerometerRecordId, MAX_RECORDS_TO_UPLOAD);
    // if (newLastAccelerometerRecordId != lastAccelerometerRecordId)
    // postData.add(new BasicNameValuePair("accel", buf.toString()));
    //
    // buf.reset();
    // final long newLastOrientationRecordId =
    // dataLayer.getOrientationAsCSV(buf,
    // lastOrientationRecordId, MAX_RECORDS_TO_UPLOAD);
    // if (newLastOrientationRecordId != lastOrientationRecordId)
    // postData.add(new BasicNameValuePair("orient", buf.toString()));
    //
    // buf.reset();
    // final long newLastBluetoothRecordId = dataLayer.getBluetoothAsCSV(buf,
    // lastBluetoothRecordId, MAX_RECORDS_TO_UPLOAD);
    // if (newLastBluetoothRecordId != lastBluetoothRecordId)
    // postData.add(new BasicNameValuePair("bt", buf.toString()));
    //
    // // post if there's anything new to report
    // if (postData.size() > 0) {
    // getDeviceIdentifiers(postData);
    //
    // UploadTask uploadTask = new UploadTask(getLogPath(), postData) {
    // @Override
    // protected void onPostExecute(Boolean result) {
    // // if we successfully uploaded the data, don't resend it
    // if (result) {
    // lastGPSRecordId = newLastGPSRecordId;
    // lastAccelerometerRecordId = newLastAccelerometerRecordId;
    // lastOrientationRecordId = newLastOrientationRecordId;
    // lastBluetoothRecordId = newLastBluetoothRecordId;
    // }
    // // always keep posting
    // uploadHandler.postDelayed(DataUploader.this, UPLOAD_INTERVAL_MILLIS);
    // }
    // };
    // uploadTask.execute();
    // } else {
    // // always keep posting
    // uploadHandler.postDelayed(DataUploader.this, UPLOAD_INTERVAL_MILLIS);
    // }
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
  public void getDeviceIdentifiers(List<NameValuePair> postData) {
    postData.add(new BasicNameValuePair("installation", Installation
        .id(service)));

    try {
      postData.add(new BasicNameValuePair("bdaddr", BluetoothAdapter
          .getDefaultAdapter().getAddress()));
    } catch (RuntimeException e) {
      Log.w("DataUploader", "failed to get Bluetooth MAC address");
    }

    try {
      WifiManager wifiManager = (WifiManager) service
          .getSystemService(Context.WIFI_SERVICE);
      postData.add(new BasicNameValuePair("macaddr", wifiManager
          .getConnectionInfo().getMacAddress()));
    } catch (RuntimeException e) {
      Log.w("DataUploader", "failed to get WiFi MAC address");
    }
  }
}
