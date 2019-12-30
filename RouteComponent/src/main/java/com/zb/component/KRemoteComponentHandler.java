package com.zb.component;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.fastjson.JSON;
import com.zb.component.utils.KConvertUtil;
import com.zb.component.utils.KContextWrap;

import java.lang.reflect.Method;

public class KRemoteComponentHandler extends IRemoteComponent.Stub {


    private void linkRemoteComponentToDeath(final KRemoteComponnetInfo component){
        try {
            component.remote.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    //Log.e("ProcessTest","binder death action:"+component.action);
                    KRemoteStation.getInstance().removeRemoteComp(component.action);
                }
            },0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void report(KRemoteMessage msg) throws RemoteException {
        //Log.e("RemoteTest",">>>>>report  "+msg.action);
        KBinderParcelWrap binderWrap = msg.data.getParcelable("component");
        //Log.e("RemoteTest",">>>binderWrap "+binderWrap);
        IRemoteComponent remote = IRemoteComponent.Stub.asInterface(binderWrap.binder);
        //Log.e("RemoteTest",">>>remote "+remote);

        if(KRemoteStation.getInstance().getRemoteComp(msg.action)==null){
            KRemoteComponnetInfo remoteComp = new KRemoteComponnetInfo(msg.action,remote,this);
            linkRemoteComponentToDeath(remoteComp);
            KRemoteStation.getInstance().addRemoteComponent(remoteComp);
        }
        try {
            remote.onReport(new KRemoteMessage(componentAction()));
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    @Override
    public void post(KRemoteMessage msg)throws RemoteException {
        KCallArgWrap data =  msg.data.getParcelable("data");
        KRouter.post(data.obj);
    }
    private KRemoteMessage createNavCallback(String method,Bundle result){
        KRemoteMessage msg = new KRemoteMessage(componentAction());
        msg.data = new Bundle();
        msg.data.putString("method",method);
        if(result!=null){
            msg.data.putBundle("result",result);
        }
        return msg;
    }

    @Override
    public void providerCall(KRemoteMessage msg, KRemoteMessage reply) {
        String url = msg.data.getString("url");
        String methodName = msg.data.getString("method");
        //Log.e("RouteTest",">>>>>on ProviderCall url:"+url+"  method:"+methodName+" process:"+AppUtils.processName(mContext));
        Object provider =  KRouter.build(url).navigation();
        if(provider == null){
            return;
        }
        String[] argTypeStrs = msg.data.getStringArray("argTypes");
        Class[] argTypes = KConvertUtil.uppackTypes(argTypeStrs);
        try {
           Method method = provider.getClass().getMethod(methodName,argTypes);

           Parcelable[] parcelables = msg.data.getParcelableArray("args");
           KCallArgWrap[] args = parcelables==null?null:new KCallArgWrap[parcelables.length];
           KConvertUtil.unpackArgWrap(parcelables,args);

           Object[] realArgs = KConvertUtil.unWrapCallArgs(args);
           // Log.e("RouteTest",">>provider call before! "+JSON.toJSONString(realArgs));
           Object result = method.invoke(provider,realArgs);
           // Log.e("RouteTest",">>provider call after!");
           KConvertUtil.paddResultMessage(method.getReturnType(),result,reply);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void navigationActivity(KRemoteMessage msg) {
        Bundle data = msg.data;
        Bundle navExtra = data.getBundle("param");
        String path = data.getString("path");
        int reqCode = data.getInt("reqCode");
        boolean isForResult = data.getBoolean("isForResult");
        KBinderParcelWrap binderWrap = data.getParcelable("cb");
        final IRemoteCallback cb = IRemoteCallback.Stub.asInterface(binderWrap.binder);

        KNavigationCallback navCb = new KNavigationCallback() {
            @Override
            public void onResult(Postcard postcard, KNavResult bundle) {
                try {
                    cb.onCallback(createNavCallback("onResult",bundle.mBundle),new KRemoteMessage());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onTimeout(Postcard postcard) {
            }
            @Override
            public void onFound(Postcard postcard) {
                try {
                    cb.onCallback(createNavCallback("onFound",null),new KRemoteMessage());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onLost(Postcard postcard) {
                try {
                    cb.onCallback(createNavCallback("onLost",null),new KRemoteMessage());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onArrival(Postcard postcard) {
                try {
                    cb.onCallback(createNavCallback("onArrival",null),new KRemoteMessage());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onInterrupt(Postcard postcard) {
                try {
                    cb.onCallback(createNavCallback("onInterrupt",null),new KRemoteMessage());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        if(isForResult){
            KRouter.build(path).with(navExtra).navigationForResult(null, navCb);
        }else{
            KRouter.build(path).with(navExtra).navigation(null,navCb);
        }
    }

    @Override
    public void onReport(KRemoteMessage msg) throws RemoteException {
        //Log.e("RemoteTest",">>>>>onReport  "+msg.action);
    }

    private static Context mContext;
    public static void setContext(Context context){
        mContext = KContextWrap.app(context);
    }


    /**
     * 一个组件app，如果有多个进程用于运行服务，那么每个进程应该提供一个service用于通讯，如果一个app有多个组件的服务需要提供，
     * 那么每个组件的服务应该分在不同的group下。每个group需要提供一个provider给外部用来查询路由表
     * @return
     */
    public static String componentAction(String process){
        String pkgName = mContext.getPackageName();
        //String curProcessName = AppUtils.processName(mContext);
        String curProcessTail = "";
        if(!TextUtils.isEmpty(process)){
            curProcessTail = process;
        }
        return pkgName+".comp_svr"+curProcessTail;
    }
    public static String componentAction(){
        return componentAction("");
    }
    public static KRemoteMessage createReportMsg(IRemoteComponent handler){
        KRemoteMessage msg = new KRemoteMessage(componentAction());
        msg.data = new Bundle();
        msg.data.putParcelable("component",new KBinderParcelWrap(handler.asBinder()));
        return msg;
    }

    public static KRemoteMessage createRemoteActivityNavigationMessage(Postcard postcard,int requestCode,boolean isForResult,IRemoteCallback cb){
        KRemoteMessage msg = new KRemoteMessage(componentAction());
        msg.data = new Bundle();
        msg.data.putString("path",postcard.getPath());
        msg.data.putString("group",postcard.getGroup());
        msg.data.putBundle("param",postcard.getExtras());
        msg.data.putInt("reqCode",requestCode);
        msg.data.putBoolean("isForResult",isForResult);
        msg.data.putParcelable("cb",new KBinderParcelWrap(cb.asBinder()));
        return msg;
    }

    public static KRemoteMessage createPostMessage(Object object){
        KRemoteMessage msg = new KRemoteMessage(componentAction());
        msg.data.putParcelable("data",new KCallArgWrap(object.getClass(),object));
        return msg;
    }

    public static KRemoteMessage createRemoteProviderCallMessage(Postcard postcard, Method method,KCallArgWrap[] args,IRemoteCallback cb){
        KRemoteMessage msg = new KRemoteMessage(componentAction());
        msg.data = new Bundle();
        msg.data.putString("url",postcard.getPath());
        msg.data.putString("method",method.getName());
        Class[] argTypeClazzs = method.getParameterTypes();
        String[] argTypeStrs = KConvertUtil.packArgTypes(argTypeClazzs);
        //LogUtil.e("RouteTest","call provider argTypeStrs:"+ JSON.toJSONString(argTypeStrs));

        msg.data.putStringArray("argTypes",argTypeStrs);
        msg.data.putString("returnType",method.getReturnType().getName());
        msg.data.putParcelableArray("args",args);
        msg.data.putParcelable("cb",new KBinderParcelWrap(cb.asBinder()));
        return msg;
    }









}
