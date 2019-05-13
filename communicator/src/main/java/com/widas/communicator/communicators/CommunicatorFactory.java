package com.widas.communicator.communicators;

import android.content.Context;

import com.widas.communicator.BuildConfig;
import com.widas.communicator.callbacks.IConnectionListener;
import com.widas.communicator.constants.CommunicatorConstants;
import com.widas.communicator.di.CommunicatorComponent;
import com.widas.communicator.di.CommunicatorModule;
import com.widas.communicator.di.DaggerCommunicatorComponent;

import timber.log.Timber;

/**
 * Created by Widas Manasa on 18-12-2017.
 */

public class CommunicatorFactory {

    private static CommunicatorComponent mCommunicatorComponent;

    public static ICommunicator getCommunicator(int type, Context context, IConnectionListener listener) {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        switch (type) {
            case CommunicatorConstants.BLUETOOTH_TYPE_BLE:
                return getCommunicatorComponent(context, listener).bleCommunicator();

            case CommunicatorConstants.BLUETOOTH_TYPE_BT:
                return getCommunicatorComponent(context,listener).btCommunicator();


            default:
                return getCommunicatorComponent(context, listener).bleCommunicator();
        }


    }

    public static CommunicatorComponent getCommunicatorComponent(Context context, IConnectionListener iConnectionListener) {
        if(mCommunicatorComponent == null) {
              mCommunicatorComponent = DaggerCommunicatorComponent.builder()
                      .communicatorModule(new CommunicatorModule(context,iConnectionListener))
                      .build();
        }
        return mCommunicatorComponent;
    }
}
