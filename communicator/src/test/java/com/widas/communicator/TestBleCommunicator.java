package com.widas.communicator;

import android.content.Context;

import com.widas.communicator.callbacks.BluetoothScanCallback;
import com.widas.communicator.callbacks.IConnectionListener;
import com.widas.communicator.callbacks.IScanResultListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * Created by Widas Manasa on 08-01-2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestBleCommunicator {

    @Mock
    Context mContext;

    @Mock
    IConnectionListener mConnectionListener;

    @Mock
    IScanResultListener mScanResultListener;

    BluetoothScanCallback mBluetoothScanCallback;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        mBluetoothScanCallback =  new BluetoothScanCallback(mScanResultListener);

    }

    @Test
    public void isHardwareSupportedTest(){
        assert(true);
    }



}
