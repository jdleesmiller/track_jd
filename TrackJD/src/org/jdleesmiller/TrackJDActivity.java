package org.jdleesmiller;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * Known bug: changing orientation after starting the app causes problems --
 * we really need to register the listeners in a service, not in an activity.
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
    
    // creating the singleton starts the background process -- we should
    // probably use a service instead of this hack
    TrackJDApplication.startIfNotRunning(this);
  }

  public void clickStop(View view) {
    TrackJDApplication.stopIfRunning();
    this.finish();
  }
}
