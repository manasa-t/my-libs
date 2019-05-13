package com.widas.communicator.callbacks;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.widas.communicator.constants.CommunicatorConstants;

/**
 * Created by Widas Manasa on 28-12-2017.
 * BluetoothGattCallbacks get called in background thread and hence throws exception if ConnectionListener is used there.
 * hence this class to communicate the connection callbacks to application's main thread.
 */

public class MainThreadHandler extends Handler {


    private IConnectionListener mConnectionListener;

    public MainThreadHandler(Context pContext, IConnectionListener pConnectionListener){
        super(pContext.getMainLooper());
        this.mConnectionListener = pConnectionListener;
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what){
            case CommunicatorConstants.STATE_CONNECTED:
                mConnectionListener.onConnected();
                break;
            case CommunicatorConstants.STATE_CONNECTING:
                mConnectionListener.onConnecting();
                break;
            case CommunicatorConstants.STATE_DISCONNECTED:
                mConnectionListener.onDisconnected();
                break;
            case CommunicatorConstants.STATE_NOT_CONNECTED:
                mConnectionListener.onNotConnected();
                break;
            case CommunicatorConstants.STATE_DATA_SENT:
                mConnectionListener.onDataSent();
                break;
            case CommunicatorConstants.STATE_DATA_NOT_SENT:
                mConnectionListener.onDataNotSent("Could not send data to Specto");
                break;
            case CommunicatorConstants.STATE_DATA_RECEIVED:
                mConnectionListener.onDataReceived((byte[])msg.getData().get(CommunicatorConstants.DATA_BUNDLE_KEY));
                break;
            case CommunicatorConstants.STATE_DATA_NOT_RECEIVED:
                mConnectionListener.onDataNotReceived("Could not get data from Specto");
                break;

        }
    }
}
