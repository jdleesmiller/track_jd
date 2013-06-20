package org.thereflectproject.trackjd.data;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

/**
 * A single Bluetooth device detection. We record the detected device's
 * Bluetooth MAC address, and also the Received Signal Strength Indicator
 * (RSSI) for the detection. We also abuse this class to record two special
 * bdaddrs for the start and end of the scan.
 */
public class BluetoothPoint extends AbstractPoint {
  private final String bdaddr;
  private final short rssi;
  
  private static final String START_SCAN_BDADDR = "_SCAN_START";
  
  private static final String END_SCAN_BDADDR = "_SCAN_END";
  
  /**
   * A dummy record that marks the start of a scan.
   * 
   * @return not null
   */
  public static BluetoothPoint newStartScanRecord() {
    return new BluetoothPoint(START_SCAN_BDADDR, (short)0);
  }
  
  /**
   * A dummy record that marks the end of a scan.
   * 
   * @return not null
   */
  public static BluetoothPoint newEndScanRecord() {
    return new BluetoothPoint(END_SCAN_BDADDR, (short)0);
  }
  
  /**
   * Constructor.
   * 
   * @param bdaddr
   * @param rssi
   */
  public BluetoothPoint(String bdaddr, short rssi) {
    super(System.currentTimeMillis());
    this.bdaddr = bdaddr;
    this.rssi = rssi;
  }

  /**
   * Construct from the intent that we get when we have a detection.
   * 
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

  /**
   * Format: MAC address, RSSI.
   * 
   * @see org.thereflectproject.trackjd.data.AbstractPoint#printCsvData(java.lang.StringBuilder)
   */
  @Override
  public void printCsvData(StringBuilder sb) {
    sb.append(bdaddr);
    sb.append(',');
    sb.append(rssi);
  }
}
