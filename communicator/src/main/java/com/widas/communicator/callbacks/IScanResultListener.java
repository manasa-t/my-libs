package com.widas.communicator.callbacks;

import android.bluetooth.BluetoothDevice;
import android.util.SparseArray;

/**
 * Created by Widas Manasa on 19-12-2017.
 */

public interface IScanResultListener {

    public void onDeviceFound(BluetoothDevice device);

    public void onDevicesNotFound();

    public void onScanFailed();

}
