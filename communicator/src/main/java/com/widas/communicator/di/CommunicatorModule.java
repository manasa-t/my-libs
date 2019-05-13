package com.widas.communicator.di;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.SparseArray;

import com.widas.communicator.callbacks.BluetoothScanCallback;
import com.widas.communicator.callbacks.IConnectionListener;
import com.widas.communicator.callbacks.IScanResultListener;
import com.widas.communicator.callbacks.MainThreadHandler;
import com.widas.communicator.communicators.BTCommunicator;
import com.widas.communicator.communicators.BleCommunicator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Widas Manasa on 28-12-2017.
 */

@Module
public class CommunicatorModule {

    private Context context;

    private IConnectionListener connectionListener;

    public CommunicatorModule(Context context, IConnectionListener connectionListener){
        this.context = context;
        this.connectionListener = connectionListener;

    }

    @Provides
    @Singleton
    public BleCommunicator provideBleCommunicator(Context context,IConnectionListener iConnectionListener){
        return new BleCommunicator(context,iConnectionListener);
    }

    @Provides
    @Singleton
    public BTCommunicator provideBtCommunicator(Context context, IConnectionListener iConnectionListener){
        return new BTCommunicator(context,iConnectionListener);
    }

    @Provides
    public Context provideContext(){
        return this.context;
    }

    @Provides
    public IConnectionListener provideConnectionListener(){
        return connectionListener;
    }



    @TargetApi(18)
    @Provides
    @Singleton
    BluetoothManager provideBluetoothManager(){
        return (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    @TargetApi(18)
    @Provides
    @Singleton
    BluetoothAdapter provideBluetoothAdapter(BluetoothManager bluetoothManager){
        return (BluetoothAdapter)bluetoothManager.getAdapter();
    }

    @Provides
    @Singleton
    public MainThreadHandler provideMainThreadHandler(){
        return new MainThreadHandler(this.context,connectionListener);
    }

    @Provides
    public SparseArray<BluetoothDevice> provideBluetoothDeviceArray(){
        return new SparseArray<BluetoothDevice>();
    }

   @Provides
   public Message provideMessage(){
        return new Message();
   }

   @Provides
   public Bundle provideBundle(){
       return new Bundle();
   }

}
