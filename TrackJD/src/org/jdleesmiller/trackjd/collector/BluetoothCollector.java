package org.jdleesmiller.trackjd.collector;

import org.jdleesmiller.trackjd.TrackJDService;
import org.jdleesmiller.trackjd.data.BluetoothDatum;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Repeatedly scan for Bluetooth devices.
 */
public class BluetoothCollector extends AbstractCollector {
  
  private boolean registered;
  private BluetoothAdapter bluetoothAdapter;
  private BroadcastReceiver bluetoothReceiver;
  private BroadcastReceiver bluetoothDiscoveryFinishedReceiver;

  public BluetoothCollector(TrackJDService context) {
    super(context);
    
    registered = false;

    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    bluetoothReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
          logPoint(new BluetoothDatum(intent));
        }
      }
    };

    // when a scan finishes, start a new one
    bluetoothDiscoveryFinishedReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent
            .getAction())) {
          bluetoothAdapter.startDiscovery();
        }
      }
    };
  }

  @Override
  public void start() {
    if (bluetoothAdapter.isEnabled()) {
      registered = true;
      getContext().registerReceiver(bluetoothReceiver,
          new IntentFilter(BluetoothDevice.ACTION_FOUND));
      getContext().registerReceiver(bluetoothDiscoveryFinishedReceiver,
          new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
      bluetoothAdapter.startDiscovery();
    }
  };

  @Override
  public void stop() {
    if (registered) {
      getContext().unregisterReceiver(bluetoothReceiver);
      getContext().unregisterReceiver(bluetoothDiscoveryFinishedReceiver);
    }
  };
}
