package com.zb.component;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class KRemoteComponentService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
       // Log.e("RemoteTest",">>>>>KRemoteComponentService onCreate  ");
    }

    private IRemoteComponent mBinder = new KRemoteComponentHandler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder.asBinder();
    }
}
