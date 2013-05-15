package org.jdleesmiller.trackjd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jdleesmiller.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Known bug: changing orientation after starting the app causes errors (but
 * does not seem to break the app) -- we really need to register the listeners
 * in a service, not in an activity.
 */
public class TrackJDActivity extends Activity {
  /**
   * On a scale of 0 to 1. The intention is to save some battery life.
   */
  private static final float DIMMED_SCREEN_BRIGHTNESS = 0.1f;

  /**
   * Assume we're on a LAN.
   */
  private static final String DEFAULT_SERVER_NAME = "192.168.x.x:3666";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);

    final SharedPreferences prefs = getSharedPreferences(
      Constants.PREFS_FILE_NAME, MODE_PRIVATE);

    final EditText serverName = (EditText) findViewById(R.id.server_name);
    serverName.setOnKeyListener(new View.OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        // set the server name when the user presses enter
        if ((event.getAction() == KeyEvent.ACTION_DOWN)
          && (keyCode == KeyEvent.KEYCODE_ENTER)) {
          SharedPreferences.Editor prefsEditor = prefs.edit();
          prefsEditor.putString(Constants.PREF_SERVER_NAME, serverName
            .getText().toString());
          prefsEditor.commit();
          return true;
        }
        return false;
      }
    });
    serverName.setText(prefs.getString(Constants.PREF_SERVER_NAME,
      DEFAULT_SERVER_NAME));

    final CheckBox dimScreen = (CheckBox) findViewById(R.id.dim_screen);
    dimScreen
      .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
          boolean isChecked) {
          WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
          if (isChecked) {
            layoutParams.screenBrightness = DIMMED_SCREEN_BRIGHTNESS;
          } else {
            layoutParams.screenBrightness = 1.0f;
          }
          getWindow().setAttributes(layoutParams);
          SharedPreferences.Editor prefsEditor = prefs.edit();
          prefsEditor.putBoolean(Constants.PREF_DIM_SCREEN, isChecked);
          prefsEditor.commit();
        }
      });
    dimScreen.setChecked(prefs.getBoolean(Constants.PREF_DIM_SCREEN, false));
    
    final CheckBox enableLogging = (CheckBox) findViewById(R.id.enable_logging);
    enableLogging.setChecked(true);
    enableLogging
      .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
          boolean isChecked) {
          if (isChecked) {
            TrackJDApplication.startIfNotRunning(TrackJDActivity.this);
          } else {
            TrackJDApplication.stopIfRunning();
          }
        }
      });

    // creating the singleton starts the background process -- we should
    // probably use a service instead of this hack
    TrackJDApplication.startIfNotRunning(this);
  }

  //
  // This should work according to the docs, but the timeout actually remains
  // at two minutes, at least on my HTC Sensation Z710e.
  // http://developer.android.com/guide/topics/wireless/bluetooth.html
  //
  // There seem to be some long-standing bugs with this part of the API:
  // http://stackoverflow.com/questions/10906737/extend-android-bluetooth-discoverability
  //
  // Fortunately, setting it in the Bluetooth settings screen seems to work, so
  // I am removing this.
  //
//  public void clickMakeDiscoverable(View view) {
//    Intent discoverableIntent = new Intent(
//      BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//    discoverableIntent.putExtra(
//      BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
//    startActivity(discoverableIntent);
//  }

  public void clickExportCSVs(View view) throws IOException {
//    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//    String timestamp = timestampFormat.format(new Date());
//    String fileStem = "track_jd";
//
//    FileOutputStream accelerometerOutputStream = null;
//    FileOutputStream bluetoothOutputStream = null;
//    FileOutputStream gpsOutputStream = null;
//    FileOutputStream orientationOutputStream = null;
//
//    try {
//      accelerometerOutputStream = new FileOutputStream(new File(
//        Environment.getExternalStorageDirectory(), fileStem + "_accelerometer_"
//          + timestamp + ".csv"));
//      TrackJDApplication.dataLayer(this).getAccelerometerAsCSV(
//        accelerometerOutputStream, 0, Integer.MAX_VALUE);
//
//      bluetoothOutputStream = new FileOutputStream(new File(
//        Environment.getExternalStorageDirectory(), fileStem + "_bluetooth_"
//          + timestamp + ".csv"));
//      TrackJDApplication.dataLayer(this).getBluetoothAsCSV(
//        bluetoothOutputStream, 0, Integer.MAX_VALUE);
//
//      gpsOutputStream = new FileOutputStream(new File(
//        Environment.getExternalStorageDirectory(), fileStem + "_gps_"
//          + timestamp + ".csv"));
//      TrackJDApplication.dataLayer(this).getGPSAsCSV(gpsOutputStream, 0,
//        Integer.MAX_VALUE);
//
//      orientationOutputStream = new FileOutputStream(new File(
//        Environment.getExternalStorageDirectory(), fileStem + "_orientation_"
//          + timestamp + ".csv"));
//      TrackJDApplication.dataLayer(this).getOrientationAsCSV(
//        orientationOutputStream, 0, Integer.MAX_VALUE);
//
//      Toast.makeText(this,
//        "wrote data to " + Environment.getExternalStorageDirectory(),
//        Toast.LENGTH_SHORT).show();
//    } catch (FileNotFoundException e) {
//      Toast
//        .makeText(
//          this,
//          "FAILED: "
//            + e.getLocalizedMessage()
//            + "\nMake sure your SD card is not already mounted on your computer.",
//          Toast.LENGTH_LONG).show();
//    } finally {
//      if (accelerometerOutputStream != null)
//        accelerometerOutputStream.close();
//      if (bluetoothOutputStream != null)
//        bluetoothOutputStream.close();
//      if (gpsOutputStream != null)
//        gpsOutputStream.close();
//      if (orientationOutputStream != null)
//        orientationOutputStream.close();
//    }
  }

  public void clickStop(View view) {
    TrackJDApplication.stopIfRunning();
    this.finish();
  }
}
