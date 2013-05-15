package org.jdleesmiller.trackjd.collector;

import java.io.PrintStream;

import org.jdleesmiller.trackjd.CollectorService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BluetoothCollector extends AbstractCollector {

  public static final String TABLE_NAME = "bluetooth";

  private boolean registered;
  private BluetoothAdapter bluetoothAdapter;
  private BroadcastReceiver bluetoothReceiver;
  private BroadcastReceiver bluetoothDiscoveryFinishedReceiver;

  public BluetoothCollector(CollectorService context) {
    super(context);
    registered = false;

    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    bluetoothReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

          SQLiteDatabase db = getWritableDb();
          if (db != null) {
            BluetoothDevice device = intent
                .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            ContentValues values = new ContentValues(3);
            values.put("bdaddr", device.getAddress());
            values.put("rssi", intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,
                Short.MIN_VALUE));
            values.put("time", System.currentTimeMillis());
            db.insert(TABLE_NAME, null, values);
          }
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
  public void createTable(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + //
        "bluetooth_id INTEGER PRIMARY KEY AUTOINCREMENT," + //
        "bdaddr CHAR(17)," + //
        "rssi INTEGER," + //
        "time INTEGER)");
  }

  @Override
  public void dropTable(SQLiteDatabase db) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
  }

  @Override
  public void printCsvHeader(PrintStream ps) {
    ps.print("bdaddr,rssi,time\n");
  }

  @Override
  public long printCsvData(PrintStream ps, long lastId, int maxRecords) {
    long newLastId = lastId;
    SQLiteDatabase db = getReadableDb();
    if (db != null) {
      Cursor c = db.rawQuery("SELECT bluetooth_id, bdaddr, rssi, time FROM "
          + TABLE_NAME + " WHERE bluetooth_id > " + lastId, null);
      int records = 0;
      while (c.moveToNext() && records < maxRecords) {
        newLastId = c.getLong(0);
        ps.print(c.getString(1));
        ps.print(",");
        ps.print(c.getInt(2));
        ps.print(",");
        ps.print(c.getLong(3));
        ps.print("\n");
        records += 1;
      }
    }
    return newLastId;
  }
}
