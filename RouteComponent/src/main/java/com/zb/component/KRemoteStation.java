package com.zb.component;
import	java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.alibaba.android.arouter.facade.Postcard;
import com.zb.component.utils.KConvertUtil;
import com.zb.component.utils.KContextWrap;
import com.zb.component.utils.KThreadUtil;
import com.zb.component.utils.KDispatcher;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KRemoteStation {
    private static KRemoteStation mInstance;
    public static KRemoteStation getInstance(){
        if(mInstance != null){
            return mInstance;
        }
        mInstance = new KRemoteStation();
        return mInstance;
    }


    private final static Object RemoteCompLck = new Object();
    private List<KRemoteComponnetInfo> mRemoteCompInfos = new ArrayList<>();
    private final static Object RemoteTaskLck = new Object();
    private HashMap<String,List<Runnable>> mTaskLists = new HashMap<>();

    public void addActionTask(String action,Runnable task){
        synchronized (RemoteTaskLck){
            List<Runnable> tasks = mTaskLists.get(action);
            if(tasks==null){
                tasks = new ArrayList<>();
                mTaskLists.put(action,tasks);
            }
            tasks.add(task);
        }

    }
    public void cancelActionTask(String action,Runnable task){
        synchronized (RemoteTaskLck){
            List<Runnable> tasks = mTaskLists.get(action);
            if(tasks!=null){
                tasks.remove(task);
            }
        }
    }
    public void execTasks(String action){
        synchronized (RemoteTaskLck){
            List<Runnable> tasks = mTaskLists.get(action);
            if(tasks!=null){
                for (Runnable item:tasks){
                    item.run();
                }
                tasks.clear();
            }
        }
    }

    public void addRemoteComponent(KRemoteComponnetInfo info){
        synchronized (RemoteCompLck){
            if(getRemoteComp(info.action)!=null){
                return;
            }
            mRemoteCompInfos.add(info);
            RemoteCompLck.notifyAll();
        }
        execTasks(info.action);
    }
    public KRemoteComponnetInfo getRemoteComp(String action){
        synchronized (RemoteCompLck){
            for (int i=0;i<mRemoteCompInfos.size();i++){
                KRemoteComponnetInfo item = mRemoteCompInfos.get(i);
                if(item.action.equals(action)){
                    return item;
                }
            }
        }
        return null;
    }
    public void removeRemoteComp(String action){
        synchronized (RemoteCompLck){
            for (int i=0;i<mRemoteCompInfos.size();i++){
                KRemoteComponnetInfo item = mRemoteCompInfos.get(i);
                if(item.action.equals(action) ){
                    mRemoteCompInfos.remove(item);
                    break;
                }
            }
        }
    }

//    private Handler uiHandler = new Handler(Looper.getMainLooper());
//    private void brokeLoopDelay(long ms){
//        uiHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                brokeLoop();
//            }
//        },ms);
//    }
//    private void brokeLoop(){
//        throw new NullPointerException("broke inner loop!");
//    }
//    private KRemoteComponnetInfo waitRemoteComponentInfo(String action,Postcard postcard,KNavigationCallback callback){
//        KRemoteComponnetInfo componnetInfo = null;
//        synchronized (RemoteCompLck){
//            componnetInfo = getRemoteComp(action);
//            if(componnetInfo ==null){
//                if(KThreadUtil.isUiThread()){
//                    brokeLoopDelay(5000);
//                    try {
//                        Looper.loop();
//                    }catch (Exception e){
//                    }
//                }else{
//                    try {
//                        RemoteCompLck.wait(5000);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//            componnetInfo = getRemoteComp(action);
//        }
//        return componnetInfo;
//    }

    private KRemoteComponnetInfo runTaskOrReturnComponent(final String action,final Runnable task,final Postcard postcard,final KNavigationCallback callback)throws KRemoteTimeoutException{
        KRemoteComponnetInfo componnetInfo = null;

        synchronized (RemoteCompLck){
            componnetInfo = getRemoteComp(action);
            if(componnetInfo == null){
                if(KThreadUtil.isUiThread()){//主线程只能直接返回，等待service连接后再执行
                    addActionTask(action,task);
                    KDispatcher.postDelay(new Runnable() {
                        @Override
                        public void run() {
                            cancelActionTask(action,task);
                            if(callback!=null){
                                callback.onTimeout(postcard);
                            }
                        }
                    }, 5000);
                }else{
                    try {//线程调用的话就等待主线程连接就行了
                        RemoteCompLck.wait(5000);
                    }catch (InterruptedException e){
                        throw new KRemoteTimeoutException();
                    }
                }
                componnetInfo = getRemoteComp(action);
            }
        }
        return componnetInfo;
    }

    public void remotePost(final Context context,final String action,final Object event)throws Exception{
        boolean bConn = KComponentConnector.getInstance().connectRemoteComponent(
                context,action);
        if(!bConn){
            return;
        }
        KRemoteComponnetInfo componnetInfo = null;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    remotePost(context,action,event);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        if((componnetInfo=runTaskOrReturnComponent(action,task,null,null))==null){
            return;
        }
        componnetInfo.remote.post(KRemoteComponentHandler.createPostMessage(event));
    }
    public void remoteNavigationActivity(final Context context,final int requestCode,final boolean isForResult,final Postcard postcard,final String action,final KNavigationCallback callback) throws KRemoteTimeoutException,RemoteException{
        boolean bConn = KComponentConnector.getInstance().connectRemoteComponent(
                context,action);
        //LogUtil.e("ProcessTest","<<<<remoteActivityNavigation bConn:"+bConn);

        if(!bConn){
            if(callback!=null){
                callback.onLost(postcard);
            }
            return;
        }

        KRemoteComponnetInfo componnetInfo = null;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    remoteNavigationActivity(context,requestCode,isForResult,postcard,action,callback);
                }catch (Exception e){
                    e.printStackTrace();
                    if(callback!=null){
                        callback.onTimeout(postcard);
                    }
                }
            }
        };
        if((componnetInfo=runTaskOrReturnComponent(action,task,postcard,callback))==null){
            return;
        }

        KRemoteMessage msg = KRemoteComponentHandler.createRemoteActivityNavigationMessage(postcard,requestCode,isForResult, new IRemoteCallback.Stub() {
            @Override
            public void onCallback(KRemoteMessage msg,KRemoteMessage reply) throws RemoteException {
                String method = msg.data.getString("method");
                if(callback!=null && method!=null && method.equals("onFound")){
                    callback.onFound(postcard);
                }
                if(callback!=null && method!=null && method.equals("onLost")){
                    callback.onLost(postcard);
                }
                if(callback!=null && method!=null && method.equals("onArrival")){
                    callback.onArrival(postcard);
                }
                if(callback!=null && method!=null && method.equals("onInterrupt")){
                    callback.onInterrupt(postcard);
                }
                if(callback!=null && method!=null && method.equals("onResult")){
                    Bundle bundle = msg.data.getBundle("result");
                    callback.onResult(postcard,new KNavResult(bundle));
                }
            }
        });
        componnetInfo.remote.navigationActivity(msg);
    }

    public void eventPostToConnected(Object object,boolean justOtherApp){
        List<KRemoteComponnetInfo> allConnects;
        synchronized (RemoteCompLck){
            mRemoteCompInfos = new ArrayList<>(mRemoteCompInfos);
        }
        //Log.e("EventBus",">>>eventPostToConnected:"+mRemoteCompInfos.size());
        for (KRemoteComponnetInfo conn:mRemoteCompInfos){
            try {
                //Log.e("EventBus",">>>>connect action:"+conn.action);
                if(justOtherApp && conn.action.startsWith(KContextWrap.obtainApplication().getPackageName())){
                    continue;
                }
                conn.remote.post(KRemoteComponentHandler.createPostMessage(object));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public Object providerCall(final Context context,final Postcard postcard,final String action,final Method method,final Object[] args,final KNavigationCallback callback) throws Exception{

        boolean bConnected =  KComponentConnector.getInstance().connectRemoteComponent(context,action);
        if(!bConnected){
            throw new KRemoteCallException();
        }
        KRemoteComponnetInfo componnetInfo = null;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    providerCall(context,postcard,action,method,args,callback);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        if((componnetInfo=runTaskOrReturnComponent(action,task,postcard,callback))==null){
            return KConvertUtil.defaultReturn(method);
        }
        Log.e("RouteTest",">>providerCall "+postcard.getPath()+" method:"+method.getName()+" action:"+action);
        KCallArgWrap[] wrapArgs = KConvertUtil.wrapCallArgs(method.getParameterTypes(),args,method);

        Log.e("RouteTest",">>createRemoteProviderCallMessage");
        KRemoteMessage msg = KRemoteComponentHandler.createRemoteProviderCallMessage(postcard, method, wrapArgs, new IRemoteCallback.Stub() {
            @Override
            public void onCallback(KRemoteMessage msg,KRemoteMessage reply) throws RemoteException {

            }
        });
        KRemoteMessage outMsg = new KRemoteMessage();
        Log.e("RouteTest",">>>>>remote.providerCall before! "+componnetInfo.remote);
        componnetInfo.remote.providerCall(msg,outMsg);
        Log.e("RouteTest",">>>>>remote.providerCall after!");
        KCallArgWrap wrap = outMsg.data.getParcelable("result");//返回值无效
        return wrap==null? KConvertUtil.defaultReturn(method):wrap.obj;
    }
}
