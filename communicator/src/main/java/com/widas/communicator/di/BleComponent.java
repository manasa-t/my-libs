package com.widas.communicator.di;

import android.bluetooth.BluetoothManager;

import com.widas.communicator.communicators.BleCommunicator;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Widas Manasa on 02-01-2018.
 */
@Singleton
@Component(modules = BleModule.class)
public interface BleComponent {

   BluetoothManager bluetoothManager();

    void inject(BleCommunicator bleCommunicator);

    BleCommunicator bleCommunicator();

}
