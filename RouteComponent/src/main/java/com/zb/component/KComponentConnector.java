package com.zb.component;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.zb.component.utils.KAppUtils;


public class KComponentConnector {
    private static KComponentConnector mInstance;
    public static KComponentConnector getInstance(){
        if(mInstance != null){
            return mInstance;
        }
        mInstance = new KComponentConnector();
        return mInstance;
    }



    public boolean connectRemoteComponent(Context context,String action){
        KRemoteStation station = KRemoteStation.getInstance();
        if(station.getRemoteComp(action)!=null){
            return true;
        }
        Intent intent = new Intent(action);
        //Log.e("RouteTest",">>>>>connectRemoteComponent "+action);
        boolean bBind = context.bindService(KAppUtils.createExplicitFromImplicitIntent(context,intent),
                new MyComponentConnection(context,action),
                Context.BIND_AUTO_CREATE);
        if(!bBind){
            //Log.e("RouteTest",">>>>>connt bindservice "+action);
            return false;
        }
        return true;
    }

    private IRemoteComponent mComponentBinder;

    public KComponentConnector(){
        mComponentBinder = new KRemoteComponentHandler();
    }

    private class MyComponentConnection implements ServiceConnection{
        private String action;
        private Context context;
        MyComponentConnection(Context context,String action){
            this.action = action;
            this.context = context;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Log.e("RouteTest",">>>>>onServiceConnected "+action);
            IRemoteComponent remote = IRemoteComponent.Stub.asInterface(service);
            KRemoteComponnetInfo componnetInfo = new KRemoteComponnetInfo(action,remote,mComponentBinder);

            if(KRemoteStation.getInstance().getRemoteComp(action)!=null){
                return;
            }
            KRemoteStation.getInstance().addRemoteComponent(componnetInfo);
            try {
                remote.report(KRemoteComponentHandler.createReportMsg(mComponentBinder));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            KRemoteStation.getInstance().removeRemoteComp(action);
        }
    }
}
