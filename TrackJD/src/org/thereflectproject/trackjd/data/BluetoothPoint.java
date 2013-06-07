package org.thereflectproject.trackjd.data;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

/**
 * A single Bluetooth device detection.
 */
public class BluetoothPoint extends AbstractPoint {

  private final String bdaddr;
  private final short rssi;

  /**
   * @param intent
   *          a BluetoothDevice.ACTION_FOUND intent
   */
  public BluetoothPoint(Intent intent) {
    super(System.currentTimeMillis());

    BluetoothDevice device = intent
        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

    this.bdaddr = device.getAddress();
    this.rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,
        Short.MIN_VALUE);
  }

  @Override
  public String getTag() {
    return "bt_v1";
  }

  @Override
  public void printCsvHeader(StringBuilder sb) {
    sb.append("bdaddr,rssi");
  }

  @Override
  public void printCsvData(StringBuilder sb) {
    sb.append(bdaddr);
    sb.append(',');
    sb.append(rssi);
  }
}