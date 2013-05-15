package org.jdleesmiller.trackjd.data;

import java.io.PrintStream;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

/**
 * A single Bluetooth device detection.
 */
public class BluetoothDatum extends AbstractPoint {

  private final String bdaddr;
  private final short rssi;

  /**
   * @param intent
   *          a BluetoothDevice.ACTION_FOUND intent
   */
  public BluetoothDatum(Intent intent) {
    super(System.currentTimeMillis());

    BluetoothDevice device = intent
        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

    this.bdaddr = device.getAddress();
    this.rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,
        Short.MIN_VALUE);
  }

  @Override
  public void printCsvHeader(PrintStream ps) {
    super.printCsvHeader(ps);
    ps.print(",bdaddr,rssi");
  }

  @Override
  public void printCsvData(PrintStream ps) {
    super.printCsvData(ps);
    ps.print(',');
    ps.print(bdaddr);
    ps.print(',');
    ps.print(rssi);
  }
}
