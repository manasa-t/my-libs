package com.widas.communicator.communicators;

import android.content.Context;

/**
 * Created by Widas Manasa on 18-12-2017.
 * This interface offers methods to use a Bluetooth or any communicator
 * like connect, disconnect, sendData etc
 * To be implemented by Communicator services like Bluetooth/Wifi etc
 */

public interface ICommunicator {
    public static final int SCAN_DURATION = 10;

    /*initializes Communicator with only service uuid*/
    public void setUUIDs(String btServiceUUID);

    /*initializes Communicator with device uuids to search for*/
    public void setUUIDs(String serviceUUID, String notifyUUID, String readWriteUUID, String descriptorUUID);

    /*checks if Hardware Supported for ex: Whether Device has Bluetooth
    * Includes checking for different Android versions and Bluetooth Versions*/
    public boolean isHardwareSupported();

    /*scans for bluetooth devices nearby*/
    public void searchForDevices(int scanDuration);

    /*used as callback from user input on which device is selected for connection
     out of the list of devices displayed to user*/
    public void onDeviceSelected(int deviceId);

    /*calls isHardwareSupported and then scans for devices
    * connects to device that is found and pairs if necessary*/
    public void connect();

    /*disconnects from device that is connected*/
    public void disconnect();

    /*disconnects and connects again to reconnect/refresh connection*/
    public void reconnect();

    /*sends data as byte array to the connected device */
    public void sendData(byte[] dataToSend);

    /*gets data as byte array from connected device*/
    public void getData(byte[] commandToGetData, int expectedDataLength);

    /*close the connection*/
    public void close();
}


