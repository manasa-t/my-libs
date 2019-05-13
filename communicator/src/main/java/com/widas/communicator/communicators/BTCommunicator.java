package com.widas.communicator.communicators;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.widas.communicator.callbacks.IConnectionListener;
import com.widas.communicator.util.UUIDUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Widas Manasa on 18-12-2017.
 * Classic Bluetooth Communicator to connect and send/receive data
 * Implements ICommunicator interface
 */

public class BTCommunicator implements ICommunicator {

    private static BTCommunicator INSTANCE = null;
    private final String DEVICE_NAME = "BTP";
    private static final int BUFFER_LENGTH = 1024;

    private Context mContext;

    private IConnectionListener mConnectionListener;

    @Inject
    BluetoothAdapter mBluetoothAdapter;

    @Inject
    SparseArray<BluetoothDevice> mBluetoothDevices;


    private BluetoothDevice mBluetoothDevice;
    private FindDevicesReceiver mfindDevicesReceiver;

    private BluetoothSocket mBluetoothSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;



    @Inject
    public BTCommunicator(Context context,IConnectionListener listener){
        this.mContext = context;
        this.mConnectionListener = listener;
        mfindDevicesReceiver = new FindDevicesReceiver();
        CommunicatorFactory.getCommunicatorComponent(context,listener).inject(this);
    }


    @Override
    public void setUUIDs(String btServiceUUID) {
        UUIDUtils.saveBtServiceUUID(mContext,btServiceUUID);
    }

    @Override
    public void setUUIDs(String serviceUUID, String notifyUUID, String readWriteUUID, String descriptorUUID) {

    }



    @Override
    public boolean isHardwareSupported() {
        if(mBluetoothAdapter == null)
            return false;
        if(!mBluetoothAdapter.isEnabled()){
            mConnectionListener.onRequestEnableBluetooth();
        }

        return true;
    }

    @Override
    public void searchForDevices(int scanDuration) {
        Timber.d("searchForDevices()");
        if(!isHardwareSupported()) mConnectionListener.onBluetoothNotSupported();
        else new scanningTask().execute();
    }

    @Override
    public void onDeviceSelected(int deviceId) {
        /*for (int i = 0; i < mBluetoothDevices.size(); i++) {
            BluetoothDevice device = mBluetoothDevices.get(mBluetoothDevices.keyAt(i));
            if (device.getAddress().equals(deviceAddress)) {
                mBluetoothDevice = device;
            }
        }*/
        mBluetoothDevice = mBluetoothDevices.get(deviceId);

        connect();
    }


    @Override
    public void connect() {
        Timber.d("connect()");
        if(mBluetoothDevice!=null) {

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUIDUtils.getBtServiceUUID());
                mBluetoothSocket.connect();
                mInStream = mBluetoothSocket.getInputStream();
                mOutStream = mBluetoothSocket.getOutputStream();
                mConnectionListener.onConnected();
            } catch (IOException e) {
                mConnectionListener.onNotConnected();
            }
        }else{
            mConnectionListener.onScanFailed("Failed to find devices");
        }
    }

    private void bondWithDevice(BluetoothDevice device){
        if(device.getBondState()!= BluetoothDevice.BOND_BONDED){
            Timber.d("Pairing...");
            PairingHelper.pairDevice(device);
        }else{
            PairingHelper.unpairDevice(device);
            PairingHelper.pairDevice(device);
        }
        PairingHelper.registerReciever(mContext);
    }

    @Override
    public void disconnect() {
        PairingHelper.unregisterReceiver(mContext);
        if(mBluetoothSocket != null){
            try{
                mBluetoothSocket.close();
            }catch(IOException e){

            }
        }
    }

    @Override
    public void reconnect() {

    }

    @Override
    public void sendData(byte[] dataToSend) {
        Timber.d("sendData() "+dataToSend.length);
        try {
            mOutStream.write(dataToSend);
            mConnectionListener.onDataSent();
        }catch(IOException e){
            e.printStackTrace();
            mConnectionListener.onDataNotSent("Could not send data ");
        }
    }

    @Override
    public void getData(byte[] commandToGetData, int expectedDataLength) {
        Timber.d("getData() "+commandToGetData.length);
        byte[] readBuffer = new byte[BUFFER_LENGTH];
        try{
            int bytes = mInStream.read(readBuffer);
            mConnectionListener.onDataReceived(readBuffer);
        }catch(IOException e){
            e.printStackTrace();
            mConnectionListener.onDataNotReceived("Could not get data");
        }
    }

    @Override
    public void close() {
        try{
            PairingHelper.unregisterReceiver(mContext);
            mInStream.close();
            mOutStream.close();
            mBluetoothSocket.close();
            mContext.unregisterReceiver(mfindDevicesReceiver);
        }catch(IOException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }finally{
            mInStream = null;
            mOutStream = null;
        }


    }

    private void startScan(){
        Timber.d("startScan()");
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mfindDevicesReceiver,intentFilter);
        mBluetoothAdapter.startDiscovery();
    }

    private void stopScan(){
        Timber.d("stopScan()");
        mBluetoothAdapter.cancelDiscovery();


        if(mBluetoothDevices.size()< 1) {
            Timber.d("No devices found");
            mConnectionListener.onScanFailed("Could not find devices");
        }
        else if(mBluetoothDevices.size() == 1){
            Timber.d("device found");
            mBluetoothDevice = mBluetoothDevices.get(mBluetoothDevices.keyAt(0));
            connect();
        }
        else if(mBluetoothDevices.size()>1){
            Timber.d("More than one devices found");
            mConnectionListener.showDeviceList(getDeviceList(mBluetoothDevices));
        }
    }
    /*returns a Map<index,DeviceName>*/
    private Map<Integer, String> getDeviceList(SparseArray<BluetoothDevice> deviceList) {

        Map<Integer, String> devicesMap = new HashMap<>();
        for (int i = 0; i < deviceList.size(); i++) {
            int key = deviceList.keyAt(i);
            BluetoothDevice device = deviceList.get(key);
            devicesMap.put(i, device.getName());
            Timber.d("added device "+devicesMap.size());
        }

        return devicesMap;
    }



    private class scanningTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            startScan();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            for(int i=SCAN_DURATION;i>=0;i--){
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            stopScan();
        }
    }

    private class FindDevicesReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == BluetoothDevice.ACTION_FOUND){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device != null && device.getName()!=null && device.getName().contains(DEVICE_NAME)) {
                    mBluetoothDevices.put(device.hashCode(), device);
                }
            }
        }
    }
}
