package org.thereflectproject.trackjd.collector;

import org.thereflectproject.trackjd.Constants;
import org.thereflectproject.trackjd.TrackJDService;
import org.thereflectproject.trackjd.data.BluetoothPoint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

/**
 * Repeatedly scan for Bluetooth devices.
 */
public class BluetoothCollector extends AbstractCollector {

  private BluetoothAdapter bluetoothAdapter;
  private BroadcastReceiver bluetoothReceiver;
  private BroadcastReceiver bluetoothDiscoveryFinishedReceiver;

  private final Handler handler;

  /**
   * In milliseconds.
   */
  private static final int DEFAULT_INTERVAL = 10000;

  public BluetoothCollector(TrackJDService context) {
    super(context);

    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    // this is called when we have a detection
    bluetoothReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
          logPoint(new BluetoothPoint(intent));
        }
      }
    };

    // this is called when a scan finishes
    bluetoothDiscoveryFinishedReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent
            .getAction())) {
          logPoint(BluetoothPoint.newEndScanRecord());
          startScanAfterDelay();
        }
      }
    };

    // used to insert a configurable delay between scans
    handler = new Handler();
  }

  @Override
  public void start() {
    getContext().registerReceiver(bluetoothReceiver,
        new IntentFilter(BluetoothDevice.ACTION_FOUND));
    getContext().registerReceiver(bluetoothDiscoveryFinishedReceiver,
        new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    startScanAfterDelay();
  };

  @Override
  public void stop() {
    getContext().unregisterReceiver(bluetoothReceiver);
    getContext().unregisterReceiver(bluetoothDiscoveryFinishedReceiver);
  };

  /**
   * Start a new scan after the interval has elapsed. Note that if Bluetooth
   * is disabled, this method calls itself to start a new scan.
   */
  private void startScanAfterDelay() {
    handler.postDelayed(
        new Runnable() {
          public void run() {
            if (bluetoothAdapter.isEnabled()) {
              bluetoothAdapter.startDiscovery();
              logPoint(BluetoothPoint.newStartScanRecord());
            } else {
              startScanAfterDelay();
            }
          }
        },
        getPreferences().getInt(Constants.PREF_BLUETOOTH_INTERVAL,
            DEFAULT_INTERVAL));
  }
}
