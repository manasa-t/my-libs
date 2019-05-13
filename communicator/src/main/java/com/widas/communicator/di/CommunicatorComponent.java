package com.widas.communicator.di;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;

import com.widas.communicator.communicators.BTCommunicator;
import com.widas.communicator.communicators.BleCommunicator;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Widas Manasa on 02-01-2018.
 */
@Singleton
@Component(modules = CommunicatorModule.class)
public interface CommunicatorComponent {

   BluetoothManager bluetoothManager();

   BluetoothAdapter bluetoothAdapter();

    void inject(BleCommunicator bleCommunicator);

    BleCommunicator bleCommunicator();

    BTCommunicator btCommunicator();

    void inject(BTCommunicator btCommunicator);

}
