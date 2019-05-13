package com.widas.communicator.communicators;

import android.annotation.TargetApi;
import android.app.job.JobService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.widas.communicator.BuildConfig;
import com.widas.communicator.callbacks.BluetoothScanCallback;
import com.widas.communicator.callbacks.IConnectionListener;
import com.widas.communicator.callbacks.IDataCallBack;
import com.widas.communicator.callbacks.IScanResultListener;
import com.widas.communicator.callbacks.MainThreadHandler;
import com.widas.communicator.constants.CommunicatorConstants;
import com.widas.communicator.util.ResponseTimerTask;
import com.widas.communicator.util.UUIDUtils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Widas Manasa on 18-12-2017.
 * Bluetooth Low Energy Communicator to connect and send/receive data
 * Implements ICommunicator interface
 */
@TargetApi(18)
public class BleCommunicator extends BluetoothGattCallback implements ICommunicator, IScanResultListener {

    private static BleCommunicator INSTANCE = null;


    IConnectionListener mConnectionListener;

    Context mContext;

    @Inject
    BluetoothManager mBluetoothManager;

    @Inject
    BluetoothAdapter mBluetoothAdapter;

    private BluetoothGatt mBluetoothGatt;


    BluetoothScanCallback mBluetoothScanCallback;

    @Inject
    SparseArray<BluetoothDevice> mBluetoothDevices;

    private BluetoothDevice mBluetoothDevice;

    private boolean isScanning;
    private boolean isConnected;

    @Inject
    MainThreadHandler mainHandler;

    @Inject
    Message dataMessage;

    @Inject
    Bundle dataBundle;

    private Handler restartHandler = new Handler();

    private int mScanDuration;


    int expectedDataLength;
    ResponseTimerTask mResponseTimerTask;
    List<Byte> totalResponseBytes;

   // PriorityQueue<byte[]> requestQueue;



    byte currentSeqNo;


    @Inject
    public BleCommunicator(Context appContext, IConnectionListener iConnectionListener) {

        Timber.d("BleCommunicator constructor()");
        mContext = appContext;
        mConnectionListener = iConnectionListener;
        mBluetoothScanCallback = new BluetoothScanCallback(this);
        CommunicatorFactory.getCommunicatorComponent(appContext, iConnectionListener).inject(this);
       // requestQueue = new PriorityQueue<byte[]>();

    }


    @Override
    public void setUUIDs(String btServiceUUID) {

    }

    @Override
    public void setUUIDs(String serviceUUID, String notifyUUID, String readWriteUUID, String descriptorUUID) {
        Timber.d("BleCommunicator setBleUUIDs()");
        UUIDUtils.saveUUIDs(mContext, serviceUUID, notifyUUID, readWriteUUID, descriptorUUID);
    }


    @Override
    public boolean isHardwareSupported() {
        Timber.d("isHardwareSupported() ");
        /*checks if Bluetooth hardware is supported*/
        if (mBluetoothManager == null || mBluetoothAdapter == null) {
            Timber.d("BluetoothManager or BluetoothAdapter is null");
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Timber.d("BluetoothAdapter is not enabled");
            mConnectionListener.onRequestEnableBluetooth();
        }

        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

    }

    @Override
    public void searchForDevices(int scanDuration) {
        Timber.d("searchForDevices()");
        if (!isHardwareSupported()) {
            mConnectionListener.onBluetoothNotSupported();
            return;
        }

        mScanDuration = scanDuration;
        doScan(scanDuration);/* start scan for devices scan for a specific duration*/

    }

    @Override
    public void onDeviceSelected(int deviceId) {
        Timber.d("onDeviceSelected()");
       /* for (int i = 0; i < mBluetoothDevices.size(); i++) {
            BluetoothDevice device = mBluetoothDevices.get(mBluetoothDevices.keyAt(i));
            if (device.getAddress().equals(deviceAddress)) {
                mBluetoothDevice = device;
            }
        }*/

       mBluetoothDevice = mBluetoothDevices.get(deviceId);

        if (!isConnected) connect();
    }

    @Override
    public void connect() {
        Timber.d("connect()");
        if (mBluetoothGatt == null) {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, this);
        } else
            mBluetoothGatt.connect();

    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        Timber.d("onDeviceFound() " + device.getName());
        if (mScanDuration > 0) {
            int hash = device.hashCode();
            mBluetoothDevices.put(hash, device);
        } else if (mScanDuration == 0) {
            stopScan();
        }
    }

    @Override
    public void onDevicesNotFound() {
        mConnectionListener.onScanFailed("Could not find devices");
    }

    @Override
    public void onScanFailed() {
        mConnectionListener.onScanFailed("Failed to scan for devices");
    }

    @Override
    public void disconnect() {
        mBluetoothGatt.disconnect();
    }

    @Override
    public void reconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.connect();
        }
    }

    @Override
    public void sendData(final byte[] dataToSend) {
        Timber.d("sendData " + new String(dataToSend));
        // add to the queue and poll the head,
        // on receiving exp data length check for next and poll next
       //  doWrite(requestQueue.poll());

        doWrite(dataToSend);

    }
      /* getWriteObservable(dataToSend)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("onError() data not sent");
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("onComplete() data sent");
                    }
                });*/



    private void doWrite(byte[] bytes) {
        Timber.d("write initiated in main thread " + (Looper.myLooper() == Looper.getMainLooper()));
        if (mBluetoothGatt != null) { // having a non null gatt object means connection is on, in case of disconnection gatt object will be null
            try {
                BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUIDUtils.getServiceUUID())
                        .getCharacteristic(UUIDUtils.getRxUUID());
                characteristic.setValue(bytes);
                boolean writeStatus = mBluetoothGatt.writeCharacteristic(characteristic);
                Timber.d("write status " + writeStatus);
                if(!writeStatus){mainHandler.sendEmptyMessage(CommunicatorConstants.STATE_DATA_NOT_SENT);}
            } catch (NullPointerException e) {
                Timber.d("UUIDs are null");
            }
        } else {
            Timber.d("Bluetooth Gatt is null");

            mainHandler.sendEmptyMessage(CommunicatorConstants.STATE_DATA_NOT_SENT);

        }
    }

    /*this method was used to make send requests asynchronous using RxJava
    * but not using this anymore since bluetooth api is already asynchronous.*/
   /* private void doWrite1(byte[] bytes, IDataCallBack iDataCallBack){
        Timber.d("write initiated in main thread " + (Looper.myLooper() == Looper.getMainLooper()));
        if (mBluetoothGatt != null) { // having a non null gatt object means connection is on, in case of disconnection gatt object will be null
            try {

                BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUIDUtils.getServiceUUID())
                        .getCharacteristic(UUIDUtils.getRxUUID());
                characteristic.setValue(bytes);
                boolean writeStatus = mBluetoothGatt.writeCharacteristic(characteristic);
                Timber.d("write status " + writeStatus);
                if(writeStatus){
                    iDataCallBack.onSuccess();
                }else iDataCallBack.onFailure("Failed to write data");
            } catch (NullPointerException e) {
                Timber.d("UUIDs are null");
            }
        } else {
            Timber.d("Bluetooth Gatt is null");

           iDataCallBack.onFailure("No Bluetooth Connection");

        }
    }
*/
    /*RxJava implementation for making write requests asynchronous but presently not used*/
   /* private Observable<String> getWriteObservable(final byte[] bytesToWrite){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {
                doWrite1(bytesToWrite, new IDataCallBack() {
                    @Override
                    public void onSuccess() {
                        e.onComplete();
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        e.onError(new Throwable(errorMsg));
                    }
                });
            }
        });
    }*/


    @Override
    public void getData(byte[] commandToGetData, final int expectedDataLength) {
       /* try {
            requestQueue.add(commandToGetData);
        } catch (ClassCastException e) {
            Timber.d("ClassCastException " + commandToGetData.length);
        }*/

            this.expectedDataLength = expectedDataLength;
            this.totalResponseBytes = new ArrayList<Byte>();
            currentSeqNo = commandToGetData[commandToGetData.length - 1];
            sendData(commandToGetData);


       /* mResponseTimerTask = new ResponseTimerTask(new ResponseTimerTask.ResponseTimerTaskCallback() {
            @Override
            public void onResponseTimeElapsed() {
                Timber.d("onResponseTimeElapsed");
                if (totalResponseBytes == null || totalResponseBytes.size() != expectedDataLength) {
                    mainHandler.sendEmptyMessage(CommunicatorConstants.STATE_DATA_NOT_RECEIVED);
                }
            }
        });
        mResponseTimerTask.execute();*/

    }

    @Override
    public void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            mBluetoothManager = null;
            mBluetoothAdapter = null;
            mBluetoothDevices.clear();
        }
    }

    /*BluetoothGattCallback methods*/

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Timber.d("onConnectionStateChange()");

        switch (newState) {
            case BluetoothProfile.STATE_CONNECTING: {
                Timber.d("STATE_CONNECTING");
                mConnectionListener.onConnecting();
            }
            break;
            case BluetoothProfile.STATE_CONNECTED: {
                Timber.d("STATE_CONNECTED");
                isConnected = true;
                mBluetoothGatt.discoverServices();

            }
            break;
            case BluetoothProfile.STATE_DISCONNECTED: {
                Timber.d("STATE_DISCONNECTED");
                isConnected = false;
                mConnectionListener.onDisconnected();
                //reconnect();

            }
            break;
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Timber.d("onServicesDiscovered()");
        if (status == BluetoothGatt.GATT_SUCCESS) {

            try {
                setNotificationForCharacteristic(mBluetoothGatt.getService(UUIDUtils.getServiceUUID())
                        .getCharacteristic(UUIDUtils.getNotifyUUID()), true);
            } catch (NullPointerException e) {
                e.printStackTrace();
                mainHandler.sendEmptyMessage(CommunicatorConstants.STATE_NOT_CONNECTED);
            }

            mainHandler.sendEmptyMessage(CommunicatorConstants.STATE_CONNECTED);


        } else if (status == BluetoothGatt.GATT_FAILURE) {

            mainHandler.sendEmptyMessage(CommunicatorConstants.STATE_DISCONNECTED);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Timber.d("onCharacteristicRead()");

    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Timber.d("onCharacteristicWrite() " + characteristic.getValue().length);

    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

        Timber.d("onCharacteristicChanged() " + new String(characteristic.getValue()));
        byte[] responseBytes = characteristic.getValue();
        /* check for bytes size. if 1, then check for value if it is 1 or 0, then success or failure
        *  -> 1, keep adding next bytes to totalresponsebytes untill expected data length is reached
        *  -> 0, notify failure and clear totalresponsebytes, seq no and expecteddatalength
        * if it is seq no end of current req returnresponsebytes
        * */

        if(responseBytes.length>1){ // add to totalresponsebytes
            // copy the response bytes to totalBytes
            List<Byte> responseBytesList = getByteObjectsArrayList(responseBytes);
            totalResponseBytes.addAll(responseBytesList);
             //check if the totalBytes.length  is equal to expected data length
            //return totalBytes if yes.
            Timber.d("many bytes received totalResponseBytes now is "+totalResponseBytes.size());
            if (totalResponseBytes.size() >= expectedDataLength) {
                Timber.d("totalResponseBytes equal to expectedDataLength "+totalResponseBytes.size());

                returnResponseBytes(getByteArray(totalResponseBytes));
            }
        }else{ // 1 byte
            if(new Byte(responseBytes[0]).equals(new Byte(currentSeqNo))){ // end of response return it and reset everything

                returnResponseBytes(getByteArray(totalResponseBytes));
            }
            else if(responseBytes[0] == '1'){ // success
                Timber.d("command successful.. awaiting response bytes");
                totalResponseBytes.add(responseBytes[0]);
            }else if(responseBytes[0] == '0'){ // failure
                Timber.d("command failure..");

                mainHandler.sendEmptyMessage(CommunicatorConstants.STATE_DATA_NOT_RECEIVED);
            }else{ // other character

                Timber.d("invalid character received "+new String(responseBytes));
                mainHandler.sendEmptyMessage(CommunicatorConstants.STATE_DATA_NOT_RECEIVED);
            }
        }
    }
       // if (expectedDataLength > 0) {




       /* *//*if response length is equal to expected then copy and return totalresponse*//*
            if (responseBytes.length == expectedDataLength) {
                totalResponseBytes = Arrays.copyOf(responseBytes,responseBytes.length);
                Timber.d("response bytes equal to expectedDataLength  bytes = "+responseBytes.length);
                returnResponseBytes(totalResponseBytes);
            }
        *//*else copy whatever has come*//*
            else {
                Timber.d("responseBytes "+responseBytes.length+" totalResponseBytes "+totalResponseBytes.length);
                totalResponseBytes = Arrays.copyOf(responseBytes,responseBytes.length);
              //  System.arraycopy(responseBytes, 0, totalResponseBytes, totalResponseBytes.length, responseBytes.length);
                if (totalResponseBytes.length == expectedDataLength) {
                    Timber.d("totalResponseBytes received "+totalResponseBytes);
                    returnResponseBytes(totalResponseBytes);
                }
            }*/
      //  }






    private List<Byte> getByteObjectsArrayList(byte[] bytesArray) {
        List<Byte> byteObjectsArray = new ArrayList<Byte>();
        for (byte b : bytesArray) {
            byteObjectsArray.add(new Byte(b));
        }

        return byteObjectsArray;
    }

    private byte[] getByteArray(List<Byte> bytesArryaList) {
        byte[] bytesArray = new byte[bytesArryaList.size()];
        for (int i = 0; i < bytesArryaList.size(); i++) {
            byte primitiveByte = bytesArryaList.get(i).byteValue();
            bytesArray[i] = primitiveByte;
        }
        return bytesArray;
    }

    private void returnResponseBytes(byte[] responseBytes) {
       /* if(mResponseTimerTask!=null && !mResponseTimerTask.isCancelled()) {
            mResponseTimerTask.cancel(true);
            mResponseTimerTask = null;
        }*/
       Timber.d("returnResponseBytes()");
        Message dataMessage = new Message();
        dataMessage.what = CommunicatorConstants.STATE_DATA_RECEIVED;
        dataBundle.putByteArray(CommunicatorConstants.DATA_BUNDLE_KEY, responseBytes);
        dataMessage.setData(dataBundle);
        mainHandler.sendMessage(dataMessage);
        totalResponseBytes.clear();
        // check for next queued request and execute it

       /* if(requestQueue.size()>0){
            doWrite(requestQueue.poll());
            isCommInProgress = true;
        }*/
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Timber.d("onDescriptorRead()");
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Timber.d("onDescriptorWrite()");
    }


    private void doScan(int scanDuration) {
        Timber.d("doScan()");
        if (isScanning) return; // return if already scanning is in progress

        if (scanDuration == CommunicatorConstants.TILL_DEVICE_FOUND) {
            Timber.d("scan duratinon " + scanDuration);
            startScan();
        } else {
            new ScanningTask(scanDuration).execute();
        }

    }

    private void startScan() {
        // for devices running Lollipop and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<ScanFilter> scanFilters = new ArrayList<ScanFilter>(1);
            try {
                Timber.d("UUID " + UUIDUtils.getServiceUUID());
                ScanFilter filter = new ScanFilter.Builder().setServiceUuid(
                        UUIDUtils.getParcelServiceUUID()).build();
                scanFilters.add(filter);
            } catch (NullPointerException e) {   /*if setBleUUIDs has not been called, UUID in preferences may be null and in that case,
            * this throws an exception specifying that setBleUUIDs has to be called */
               /* throw new IllegalArgumentException("\"Communicator may not have been initialized with UUIDS.\n"
                        + "Call ICommunicator.setBleUUIDs(String serviceUUID,String notifyUUID,String readWriteUUID)");*/
                e.printStackTrace();
                Timber.d("UUIDs are null so scanning for any devices " + (mBluetoothAdapter == null));
                mBluetoothAdapter.getBluetoothLeScanner().startScan(mBluetoothScanCallback);
            }
            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(
                    ScanSettings.SCAN_MODE_BALANCED).build();
            try {
                mBluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters, scanSettings, mBluetoothScanCallback);
            } catch (NullPointerException e) {
                mConnectionListener.onRequestEnableBluetooth();
            }


        } else { // for devices running android lower than Lollipop
            try {
                mBluetoothAdapter.startLeScan(mBluetoothScanCallback);
            } catch (NullPointerException e) {
                Timber.d("BluetoothAdapter is null");
                mConnectionListener.onRequestEnableBluetooth();
            }
        }
        Timber.d("scan started");
        isScanning = true;
    }

    private void stopScan() {
        Timber.d("stopScan()");
        if (!isScanning) return;

        Timber.d("stopping scan");
        // for devices running Lollipop and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mBluetoothScanCallback);
            } catch (NullPointerException e) {
                Timber.d("BluetoothAdapter is null");
                mConnectionListener.onRequestEnableBluetooth();
            }
        } else // for devices running android lower than Lollipop
            try {
                mBluetoothAdapter.stopLeScan(mBluetoothScanCallback);
            } catch (NullPointerException e) {
                Timber.d("BluetoothAdapter is null");
                mConnectionListener.onRequestEnableBluetooth();
            }
        isScanning = false;

        Timber.d("scan stopped");
        if (!isScanning) {
            if (mBluetoothDevices.size() == 1) {
                Timber.d("looks like a device is found");
                int key = mBluetoothDevices.keyAt(0);
                mBluetoothDevice = mBluetoothDevices.get(key);
                // connect to the only device found
                if (!isConnected) connect();

            } else if (mBluetoothDevices.size() > 1) {
                Timber.d("More than one device were found " + mBluetoothDevices.size());
                mConnectionListener.showDeviceList(getDeviceList(mBluetoothDevices));
            } else {
                Timber.d("No devices found");
                mConnectionListener.onScanFailed("Could not find devices");
            }
        }


    }

    /*returns a Map<index,DeviceName>*/
    private Map<Integer, String> getDeviceList(SparseArray<BluetoothDevice> deviceList) {

        Map<Integer, String> devicesMap = new HashMap<>();
        for (int i = 0; i < deviceList.size(); i++) {
            int key = deviceList.keyAt(i);
            BluetoothDevice device = deviceList.get(key);
            devicesMap.put(i, device.getName());
            Timber.d("added device " + devicesMap.size());
        }

        return devicesMap;
    }

    private void setNotificationForCharacteristic(BluetoothGattCharacteristic characteristic, boolean enabled) throws NullPointerException {
        if (mBluetoothGatt == null) return;
        boolean setCharacteristicResult = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (!setCharacteristicResult) Timber.d("Failed to set gatt characteristic Notification");
        try {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUIDUtils.getDescriptorUUID());
            if (descriptor != null) {
                byte[] value = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                descriptor.setValue(value);
                boolean writtenDescriptor = mBluetoothGatt.writeDescriptor(descriptor);
                if (!writtenDescriptor)
                    Timber.d("Write descriptor failed. onCharacteristic Changed may not be called");
            } //else throw new NullPointerException("Descriptor UUID might have not been initialized");
        } catch (NullPointerException e) {
            Timber.d("Descriptor UUID might not have been initialized");
        }
    }

    public void restartBle() {
        if (null != mBluetoothAdapter) {
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();

                // TODO: display some kind of UI about restarting BLE
                restartHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mBluetoothAdapter.isEnabled()) {
                            mBluetoothAdapter.enable();
                        } else {
                            restartHandler.postDelayed(this, 2500);
                        }
                    }
                }, 2500);
            }
        }
    }

    /* starts scan in onPreExecute and updates main thread (shows progress)
    * waits for SCAN_DURATION in background
    * stops scan in onPostExecute and updates main thread (hides progress and connects)*/

    private class ScanningTask extends AsyncTask {
        private int scanDuration;

        public ScanningTask(int scanDuration) {
            this.scanDuration = scanDuration;
        }

        @Override
        protected void onPreExecute() {
            startScan();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (isScanning) {
                for (int i = this.scanDuration; i >= 0; i--) {
                    try {
                        Thread.sleep(1000);
                        Timber.d("Scanning duration " + i + " seconds more");
                    } catch (InterruptedException e) {
                        onPostExecute(null);
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

            if (mContext != null) stopScan();
        }
    }

    /*start a timer for 100 ms and keep concatenating bytes till expectedDataLength is met,
    * if not met, return error msg.*/


}
