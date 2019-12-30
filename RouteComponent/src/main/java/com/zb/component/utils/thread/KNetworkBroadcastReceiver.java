package com.zb.component.utils.thread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class KNetworkBroadcastReceiver extends BroadcastReceiver{
    private OnNetworkConnectivityChangeListener mListener;
    private boolean isRegister = false;
    public KNetworkBroadcastReceiver(OnNetworkConnectivityChangeListener listener){
        mListener = listener;
    }

    public boolean isRegister() {
        return isRegister;
    }

    public synchronized void  register(Context context) {
        if(isRegister){
            return;
        }
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(CONNECTIVITY_ACTION);
            context.registerReceiver(this, filter);
        }catch (Exception e){}
        isRegister = true;
    }

    public synchronized void unregister(Context context) {
        if(!isRegister){
            return;
        }
        try {
            context.unregisterReceiver(this);
        }catch (Exception e){}
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        final String action = intent.getAction();
       if (CONNECTIVITY_ACTION.equals(action)) {
           ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
           if(connectivityManager!=null )
               mListener.onNetworkConnectivityChange(connectivityManager.getActiveNetworkInfo());
        }
    }
}
