package org.jdleesmiller.trackjd;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.os.AsyncTask;
import android.util.Log;

public class DataUploadTask extends AsyncTask<Object, Object, Boolean> {

  private String url;
  private List<NameValuePair> postData;
  private int connectionTimeout;
  private int socketTimeout;

  public DataUploadTask(String url, List<NameValuePair> postData,
      int connectionTimeout, int socketTimeout) {
    this.url = url;
    this.postData = postData;
    this.connectionTimeout = connectionTimeout;
    this.socketTimeout = socketTimeout;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.os.AsyncTask#onCancelled(java.lang.Object)
   */
  @Override
  protected void onCancelled(Boolean result) {
    Log.w("DataUploadTask", "onCancelled called");
    onCancelled();
  }

  @Override
  protected Boolean doInBackground(Object... args) {
    try {
      HttpParams httpParams = new BasicHttpParams();
      HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
      HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
      HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
      HttpClient client = new DefaultHttpClient(httpParams);
      HttpPost post = new HttpPost(url);
      post.setEntity(new UrlEncodedFormEntity(postData));
      HttpResponse response = client.execute(post);
      Log.i("DataUploadTask", "STATUS: "
          + response.getStatusLine().getStatusCode());
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
        return true;
    } catch (UnsupportedEncodingException e) {
      Log.w("DataUploadTask", "failed with UnsupportedEncodingException");
      e.printStackTrace();
    } catch (ClientProtocolException e) {
      Log.w("DataUploadTask", "failed with ClientProtocolException");
    } catch (SocketTimeoutException e) {
      Log.w("DataUploadTask", "failed with SocketTimeoutException");
    } catch (SocketException e) {
      Log.w("DataUploadTask", "failed with SocketException");
    } catch (NoHttpResponseException e) {
      Log.w("DataUploadTask", "failed with NoHttpResponseException");
    } catch (IOException e) {
      Log.w("DataUploadTask", "failed with IOException");
      e.printStackTrace();
    } catch (RuntimeException e) {
      Log.w("DataUploadTask", "failed with generic Exception");
      e.printStackTrace();
    }
    return false;
  }
}
