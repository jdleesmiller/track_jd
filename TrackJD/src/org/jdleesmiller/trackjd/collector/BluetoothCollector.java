package org.jdleesmiller.trackjd.collector;

import org.jdleesmiller.trackjd.TrackJDService;
import org.jdleesmiller.trackjd.data.BluetoothDatum;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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
  
  private static final int MAX_DATA = 100;

  private final CollectorBuffer<BluetoothDatum> buffer;

  private boolean registered;
  private BluetoothAdapter bluetoothAdapter;
  private BroadcastReceiver bluetoothReceiver;
  private BroadcastReceiver bluetoothDiscoveryFinishedReceiver;

  public BluetoothCollector(TrackJDService context) {
    super(context);
    
    this.buffer = new CollectorBuffer<BluetoothDatum>(MAX_DATA);
        
    registered = false;

    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    bluetoothReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
          buffer.store(new BluetoothDatum(intent));
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

  @Override
  public AsyncHttpResponseHandler upload(RequestParams params, int maxDataToUpload) {
    final int dataUploaded = buffer.addCsvToPost("bt", params, maxDataToUpload);
    return new AsyncHttpResponseHandler() {
      @Override
      public void onSuccess(String arg0) {
        buffer.clear(dataUploaded);
      }
    };
  }
}
