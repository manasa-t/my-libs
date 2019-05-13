package com.widas.communicator.communicators;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.reflect.Method;

import timber.log.Timber;

/**
 * Created by Widas Manasa on 13-02-2018.
 */

public class PairingHelper {

    public static void registerReciever(Context context){
        context.registerReceiver(mPairReceiver,new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    public static void unregisterReceiver(Context context){
        context.unregisterReceiver(mPairReceiver);
    }

    public static  void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static  void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state 		= intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                   // showToast("Paired");
                    Timber.d("Paired");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                   // showToast("Unpaired");
                    Timber.d("Unpaired");
                }

               // mAdapter.notifyDataSetChanged();
            }
        }
    };
}
