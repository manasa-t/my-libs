package com.widas.communicator.callbacks;

import java.util.Map;

/**
 * Created by Widas Manasa on 18-12-2017.
 * Interface to communicate between Communicator and client that uses Communicator.
 * To be implemented by Interactor that uses ICommunicator
 * Includes callback methods to notify users with communication states and acknowledgements for data transfers
 */

public interface IConnectionListener {

    /*returns connection states as CONNECTED, NOT_CONNECTED, DISCONNECTED etc*/
    public void onConnected();
    public void onConnecting();
    public void onDisconnected();
    public void onNotConnected();

    /*callback to notify that scan for devices has failed*/
    public void onScanFailed(String errorMsg);

    /*callback to send a list of device for user to select which one to connect*/
    public void showDeviceList(Map<Integer,String> deviceList);

    /*callback to notify that bluetooth is not supported on the device*/
    public void onBluetoothNotSupported();

    /*request enable bluetooth*/
    public void onRequestEnableBluetooth();

    /*callback to notify that data has been sent to the device*/
    public void onDataSent();

    /*callback to notify that data has not been sent to the device*/
    public void onDataNotSent(String errorMsg);

    /*callback to notify that data has been received from the device*/
    public void onDataReceived(byte[] receivedData);

    /*callback to notify that data has not been received*/
    public void onDataNotReceived(String errorMsg);
}
