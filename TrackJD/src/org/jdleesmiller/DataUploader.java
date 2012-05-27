package org.jdleesmiller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class DataUploader implements Runnable {

  /**
   * How long to try to connect for before giving up. This timeout is important,
   * because Internet connectivity will often be intermittent, and we have to
   * retry reasonably quickly.
   */
  private static final int CONNECTION_TIMEOUT_MILLIS = 3000;

  /**
   * Once a connection is made, wait this long for the upload to finish. The
   * connection may drop out before we can send, or before the server confirms
   * receipt.
   */
  private static final int SOCKET_TIMEOUT_MILLIS = 3000;

  /**
   * Wait this long before trying to upload.
   */
  private static final long UPLOAD_INTERVAL_MILLIS = 5000;

  /**
   * Max number of records to upload at once.
   */
  private static final int MAX_RECORDS_TO_UPLOAD = 100;

  private final Context context;

  private final SharedPreferences prefs;

  private final Handler uploadHandler;

  private final DataLayer dataLayer;

  private long lastGPSRecordId;

  private long lastAccelerometerRecordId;

  private long lastOrientationRecordId;

  private long lastBluetoothRecordId;

  /**
   * Asynchronously post the logged data to the server.
   */
  private class UploadTask extends AsyncTask<Object, Object, Boolean> {
    private final String url;
    private final List<NameValuePair> postData;

    public UploadTask(String url, List<NameValuePair> postData) {
      this.url = url;
      this.postData = postData;
    }

    /**
     * Note: it's not clear from the docs what happens if we throw an exception
     * from this method. We rely on the onPostExecute handler being called in
     * order to continue polling; if it doesn't get called, the app is
     * effectively dead. So, I'm taking the conservative approach of eating
     * pretty much all errors here, including unexpected ones. This could be
     * improved.
     */
    @Override
    protected Boolean doInBackground(Object... args) {
      try {
        HttpParams httpParams = new BasicHttpParams();
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpConnectionParams.setConnectionTimeout(httpParams,
          CONNECTION_TIMEOUT_MILLIS);
        HttpConnectionParams.setSoTimeout(httpParams, SOCKET_TIMEOUT_MILLIS);
        HttpClient client = new DefaultHttpClient(httpParams);
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(postData));
        HttpResponse response = client.execute(post);
        Log.i("DataUploader", "STATUS: "
          + response.getStatusLine().getStatusCode());
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
          return true;
      } catch (UnsupportedEncodingException e) {
        Log.w("DataUploader", "failed with UnsupportedEncodingException");
        e.printStackTrace();
      } catch (ClientProtocolException e) {
        Log.w("DataUploader", "failed with ClientProtocolException");
        e.printStackTrace();
      } catch (SocketTimeoutException e) {
        Log.w("DataUploader", "failed with SocketTimeoutException");
      } catch (NoHttpResponseException e) {
        Log.w("DataUploader", "failed with NoHttpResponseException");
      } catch (IOException e) {
        Log.w("DataUploader", "failed with IOException");
        e.printStackTrace();
      } catch (RuntimeException e) {
        Log.w("DataUploader", "failed with generic Exception");
        e.printStackTrace();
      }
      return false;
    }
  }

  public DataUploader(Context context, DataLayer dataLayer) {
    this.context = context;
    this.prefs = context.getSharedPreferences(Constants.PREFS_FILE_NAME,
      Context.MODE_PRIVATE);
    this.uploadHandler = new Handler();
    this.dataLayer = dataLayer;
  }

  public void start() {
    uploadHandler.postDelayed(this, UPLOAD_INTERVAL_MILLIS);

    // reset the last uploaded record ids (don't upload stale data on restart)
    lastGPSRecordId = dataLayer.getMaxGPSId();
    lastAccelerometerRecordId = dataLayer.getMaxAccelerometerRecordId();
    lastOrientationRecordId = dataLayer.getMaxOrientationRecordId();
    lastBluetoothRecordId = dataLayer.getMaxBluetoothRecordId();
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
    List<NameValuePair> postData = new ArrayList<NameValuePair>();

    // fill in the post data
    StringBuilder buf = new StringBuilder();
    final long newLastGPSRecordId = dataLayer.getGPSAsCSV(buf, lastGPSRecordId,
      MAX_RECORDS_TO_UPLOAD);
    if (newLastGPSRecordId != lastGPSRecordId)
      postData.add(new BasicNameValuePair("gps", buf.toString()));

    buf.setLength(0);
    final long newLastAccelerometerRecordId = dataLayer.getAccelerometerAsCSV(
      buf, lastAccelerometerRecordId, MAX_RECORDS_TO_UPLOAD);
    if (newLastAccelerometerRecordId != lastAccelerometerRecordId)
      postData.add(new BasicNameValuePair("accel", buf.toString()));

    buf.setLength(0);
    final long newLastOrientationRecordId = dataLayer.getOrientationAsCSV(buf,
      lastOrientationRecordId, MAX_RECORDS_TO_UPLOAD);
    if (newLastOrientationRecordId != lastOrientationRecordId)
      postData.add(new BasicNameValuePair("orient", buf.toString()));

    buf.setLength(0);
    final long newLastBluetoothRecordId = dataLayer.getBluetoothAsCSV(buf,
      lastBluetoothRecordId, MAX_RECORDS_TO_UPLOAD);
    if (newLastBluetoothRecordId != lastBluetoothRecordId)
      postData.add(new BasicNameValuePair("bt", buf.toString()));

    // post if there's anything new to report
    if (postData.size() > 0) {
      getDeviceIdentifiers(postData);
      
      UploadTask uploadTask = new UploadTask(getLogPath(), postData) {
        @Override
        protected void onPostExecute(Boolean result) {
          // if we successfully uploaded the data, don't resend it
          if (result) {
            lastGPSRecordId = newLastGPSRecordId;
            lastAccelerometerRecordId = newLastAccelerometerRecordId;
            lastOrientationRecordId = newLastOrientationRecordId;
            lastBluetoothRecordId = newLastBluetoothRecordId;
          }
          // always keep posting
          uploadHandler.postDelayed(DataUploader.this, UPLOAD_INTERVAL_MILLIS);
        }
      };
      uploadTask.execute();
    } else {
      // always keep posting
      uploadHandler.postDelayed(DataUploader.this, UPLOAD_INTERVAL_MILLIS);
    }
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
      .id(context)));

    try {
      postData.add(new BasicNameValuePair("bdaddr", BluetoothAdapter
        .getDefaultAdapter().getAddress()));
    } catch (RuntimeException e) {
      Log.w("DataUploader", "failed to get Bluetooth MAC address");
    }

    try {
      WifiManager wifiManager = (WifiManager) context
        .getSystemService(Context.WIFI_SERVICE);
      postData.add(new BasicNameValuePair("macaddr", wifiManager
        .getConnectionInfo().getMacAddress()));
    } catch (RuntimeException e) {
      Log.w("DataUploader", "failed to get WiFi MAC address");
    }
  }
}
