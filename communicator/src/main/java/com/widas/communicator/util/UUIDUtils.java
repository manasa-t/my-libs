package com.widas.communicator.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelUuid;

import java.util.UUID;


/**
 * Created by Widas Manasa on 20-12-2017.
 * UUID Utils is class to store the UUIDS required to communicate.\
 * Service UUID is unique identifier of the service offered by Bluetooth device, with which communication is established
 * Notify UUID
 * Rx Tx UUID is to read and write
 */

public class UUIDUtils {

    final private static String COMMUNICATOR_PREF_KEY = "communicator_preferences";
    final private static String  SERVICE_UUID_KEY = "service_uuid";
    final private static String NOTIFY_UUID_KEY = "service_notify_uuid";
    final private static String RX_UUID_KEY = "service_rx_uuid";
    final private static String DESCRIPTOR_UUID_KEY = "descriptor_uuid";
    final private static String BT_SERVICE_UUID_KEY = "bt_service_uuid";

    private static Context mContext;

    public static void saveUUIDs(Context pContext, String serviceUUID, String notifyUUID, String rxUUID, String descriptorUUID){
        mContext = pContext;
        if(serviceUUID != null && !serviceUUID.equals(""))saveServiceUUID(serviceUUID);
        if(notifyUUID != null && !notifyUUID.equals(""))saveNotifyUUID(notifyUUID);
        if(rxUUID != null && !rxUUID.equals(""))saveRXUUID(rxUUID);
        if(descriptorUUID != null && !descriptorUUID.equals(""))saveDescriptorUUID(descriptorUUID);
    }

    public static  void saveServiceUUID(String serviceUUID){
        SharedPreferences preferences = mContext.getSharedPreferences(COMMUNICATOR_PREF_KEY,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SERVICE_UUID_KEY,serviceUUID);
        editor.apply();
    }

    private static void saveNotifyUUID(String notifyUUID){
        SharedPreferences preferences = mContext.getSharedPreferences(COMMUNICATOR_PREF_KEY,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(NOTIFY_UUID_KEY,notifyUUID);
        editor.apply();
    }

    private static void saveRXUUID(String rxUUID){
        SharedPreferences preferences = mContext.getSharedPreferences(COMMUNICATOR_PREF_KEY,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(RX_UUID_KEY,rxUUID);
        editor.apply();
    }

    private static void saveDescriptorUUID(String descriptorUUID){
        SharedPreferences preferences = mContext.getSharedPreferences(COMMUNICATOR_PREF_KEY,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(DESCRIPTOR_UUID_KEY,descriptorUUID);
        editor.apply();
    }

    public static  void saveBtServiceUUID(Context context,String btServiceUUID){
        mContext = context;
        SharedPreferences preferences = mContext.getSharedPreferences(COMMUNICATOR_PREF_KEY,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(BT_SERVICE_UUID_KEY,btServiceUUID);
        editor.apply();
    }

    public static UUID getBtServiceUUID(){
        SharedPreferences preferences = mContext.getSharedPreferences(COMMUNICATOR_PREF_KEY,Context.MODE_PRIVATE);
        return UUID.fromString(preferences.getString(BT_SERVICE_UUID_KEY,null));
    }


    public static UUID getDescriptorUUID() throws NullPointerException{
        SharedPreferences preferences = mContext.getSharedPreferences(COMMUNICATOR_PREF_KEY,Context.MODE_PRIVATE);
        return UUID.fromString(preferences.getString(DESCRIPTOR_UUID_KEY,null));
    }


    public static UUID getServiceUUID() throws NullPointerException{
        SharedPreferences preferences = mContext.getSharedPreferences(COMMUNICATOR_PREF_KEY,Context.MODE_PRIVATE);
        return UUID.fromString(preferences.getString(SERVICE_UUID_KEY,null));
    }

    public static ParcelUuid getParcelServiceUUID() throws NullPointerException{
        SharedPreferences preferences = mContext.getSharedPreferences(COMMUNICATOR_PREF_KEY,Context.MODE_PRIVATE);
        return ParcelUuid.fromString(preferences.getString(SERVICE_UUID_KEY,null));
    }

    public static UUID getNotifyUUID() throws NullPointerException{
        SharedPreferences preferences = mContext.getSharedPreferences(COMMUNICATOR_PREF_KEY,Context.MODE_PRIVATE);
        return UUID.fromString(preferences.getString(NOTIFY_UUID_KEY,null));
    }

    public static UUID getRxUUID() throws NullPointerException{
        SharedPreferences preferences = mContext.getSharedPreferences(COMMUNICATOR_PREF_KEY,Context.MODE_PRIVATE);
        return UUID.fromString(preferences.getString(RX_UUID_KEY,null));
    }

}
