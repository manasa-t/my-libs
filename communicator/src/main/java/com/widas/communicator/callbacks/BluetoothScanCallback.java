package com.widas.communicator.callbacks;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;

import timber.log.Timber;

/**
 * Created by Widas Manasa on 19-12-2017.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothScanCallback extends ScanCallback implements BluetoothAdapter.LeScanCallback{

    private IScanResultListener mScanResultListener;

    public BluetoothScanCallback(IScanResultListener scanResultListener){
        this.mScanResultListener = scanResultListener;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        if(result !=null){
            Timber.d("onScanResult not null");
            if(result.getDevice() != null){mScanResultListener.onDeviceFound(result.getDevice());}
            else mScanResultListener.onDevicesNotFound();
        } else mScanResultListener.onDevicesNotFound();
    }

    @Override
    public void onScanFailed(int errorCode) {
        mScanResultListener.onScanFailed();
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Timber.d("onLeScan() device found "+device.getName());
        if(device != null)mScanResultListener.onDeviceFound(device);
        else mScanResultListener.onDevicesNotFound();
    }
}
