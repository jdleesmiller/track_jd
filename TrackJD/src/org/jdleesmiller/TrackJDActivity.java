package org.jdleesmiller;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class TrackJDActivity extends Activity {
  private DataLayer dataLayer;
  private DataCollector dataCollector;
  private DataUploader dataUploader;
  
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
    Log.d("TrackJDActivity", "ONCREATE");

    super.onCreate(savedInstanceState);

    //
    // interface
    //
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

    //
    // tracking
    //
    dataLayer = new DataLayer(this);

    dataCollector = new DataCollector(this, dataLayer);

    dataUploader = new DataUploader(this, dataLayer);

    dataLayer.open();
    dataCollector.start();
    dataUploader.start();
  }

  @Override
  protected void onDestroy() {
    Log.d("TrackJDActivity", "ONDESTROY");

    dataCollector.stop();
    dataUploader.stop();
    dataLayer.close();

    super.onDestroy();
  }

  public void clickStop(View view) {
    this.finish();
  }
}
