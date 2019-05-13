package com.widas.communicator.util;

import android.os.AsyncTask;
import android.os.Handler;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created by Widas Manasa on 09-03-2018.
 */

public class ResponseTimerTask extends AsyncTask {
    ResponseTimerTaskCallback mCallback;

    public ResponseTimerTask(ResponseTimerTaskCallback callback){
        this.mCallback = callback;
    }

    /*count till 500 ms*/

    long start;
    long elapsed;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        start = System.currentTimeMillis();
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        do{
            elapsed = System.currentTimeMillis();
        }while((elapsed-start)<500);
        return null;
    }

    /*on completing 100 ms, return saying time's up
    * in ideal cases, this should never be called
    * asynctask will be cancelled when expected data length is met*/
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Timber.d("onPostExecute");
        mCallback.onResponseTimeElapsed();
    }

    public interface ResponseTimerTaskCallback{
        public void onResponseTimeElapsed();
    }

}
