package com.zb.component.utils;

import android.os.Parcelable;
import android.os.RemoteException;

import com.zb.component.IRemoteCallback;
import com.zb.component.IRemoteListener;
import com.zb.component.KCallArgWrap;
import com.zb.component.KRemoteComponentHandler;
import com.zb.component.KRemoteMessage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class KConvertUtil {
    public static Class[] uppackTypes(String[] argTypeStrs){
        Class[] argTypes = null;
        if(argTypeStrs!=null) {
            argTypes = new Class[argTypeStrs.length];
            for (int i = 0; i < argTypeStrs.length; i++) {
                try{
                    if(argTypeStrs[i].equals("int")){
                        argTypes[i] = int.class;
                    }else if(argTypeStrs[i].equals("short")){
                        argTypes[i] = short.class;
                    }else if(argTypeStrs[i].equals("byte")){
                        argTypes[i] = byte.class;
                    }else if(argTypeStrs[i].equals("long")){
                        argTypes[i] = long.class;
                    }else if(argTypeStrs[i].equals("char")){
                        argTypes[i] = char.class;
                    }else if(argTypeStrs[i].equals("float")){
                        argTypes[i] = float.class;
                    }else if(argTypeStrs[i].equals("double")){
                        argTypes[i] = double.class;
                    }else if(argTypeStrs[i].equals("boolean")){
                        argTypes[i] = boolean.class;
                    }else{
                        argTypes[i] = Class.forName(argTypeStrs[i]);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return argTypes;
    }

    public static String[] packArgTypes(Class[] argTypes){
        String[] argTypeStrs = null;
        if(argTypes!=null){
            argTypeStrs = new String[argTypes.length];
            for (int i=0;i<argTypes.length;i++){
                argTypeStrs[i] = argTypes[i].getName();
            }
        }
        return argTypeStrs;
    }


    public static Object[]   unWrapCallArgs(KCallArgWrap[] args){
        Object[] realArgs = null;
        if(args!=null){
            realArgs = new Object[args.length];
            for (int i=0;i<args.length;i++){
                KCallArgWrap item = args[i];
                if(item.type == KCallArgWrap.TYPE_CALLBACK){
                    realArgs[i] = createProxyProviderCbArg(item.cb,item.interfaceClazz);
                }else{
                    realArgs[i] = item.obj;
                }
            }
        }
        return realArgs;
    }

    public static <T> void unpackArgWrap(Parcelable[] args,T[] outT){
        if(args!=null && outT!=null){
            for (int i=0;i<args.length;i++){
                outT[i] = (T)args[i];
            }
        }
    }

    public static Object defaultReturn(Method method){
        if(     int.class.isAssignableFrom(method.getReturnType())||
                short.class.isAssignableFrom(method.getReturnType())||
                char.class.isAssignableFrom(method.getReturnType())||
                long.class.isAssignableFrom(method.getReturnType())||
                byte.class.isAssignableFrom(method.getReturnType())||
                float.class.isAssignableFrom(method.getReturnType())||
                double.class.isAssignableFrom(method.getReturnType())){
            return -1;
        }else if(boolean.class.isAssignableFrom(method.getReturnType())){
            return false;
        }
        return null;
    }


    /**
     * 远端，一个可回调调用端的函数参数，给实现函数的是一个代理
     * 然后收到实现函数的回调的时候，回调给调用端
     */
    private static Object createProxyProviderCbArg(final IRemoteCallback callback, Class clazz){
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                KRemoteMessage msg = new KRemoteMessage(KRemoteComponentHandler.componentAction());
                msg.data.putString("method",method.getName());
                Class[] argTypes = method.getParameterTypes();
                String[] argTypeStrs =packArgTypes(argTypes);
                msg.data.putStringArray("argTypes",argTypeStrs);
                KCallArgWrap[] wrapArgs = KConvertUtil.wrapCallArgs(method.getParameterTypes(),args,method);
                msg.data.putParcelableArray("args",wrapArgs);
                KRemoteMessage reply =new KRemoteMessage();
                callback.onCallback(msg,reply);
                KCallArgWrap wrap = reply.data.getParcelable("result");
                return wrap == null ? defaultReturn(method) : wrap.obj;
            }
        });
    };
    /**
     * 调用端，可回调的函数参数，进行代理
     * 远端会通过IRemoteCallback这个Binder来回调
     */
    private static IRemoteCallback createProxyCallback(final Object arg,final Class argType){
        return new IRemoteCallback.Stub() {
            @Override
            public void onCallback(KRemoteMessage msg,KRemoteMessage reply) throws RemoteException {
                String methodName = msg.data.getString("method");
                String[] methodArgsClasss = msg.data.getStringArray("argTypes");
                Class[] methodArgTypes = uppackTypes(methodArgsClasss);

                Parcelable[] parcelables = msg.data.getParcelableArray("args");
                KCallArgWrap[] wrapArgs = parcelables==null?null:new KCallArgWrap[parcelables.length];
                KConvertUtil.unpackArgWrap(parcelables,wrapArgs);
                Object[] realArgs = KConvertUtil.unWrapCallArgs(wrapArgs);
                try {
                    Method method = argType.getMethod(methodName,methodArgTypes);
                    Object result = method.invoke(arg,realArgs);
                    paddResultMessage(method.getReturnType(),result,reply);//返回参数
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
    }
    public static void paddResultMessage(Class argType,Object arg,KRemoteMessage msg){
        if(msg==null){
            return;
        }
        msg.data.putParcelable("result",new KCallArgWrap(argType,arg));
    }
    public static KCallArgWrap[] wrapCallArgs( Class[] argTypes,Object[] args,Method method){
        KCallArgWrap[] wrapArgs = null;
        if(args!=null){
            wrapArgs = new KCallArgWrap[args.length];
            for (int i=0;i<args.length;i++){
                Object arg = args[i];
                Class argType = method.getParameterTypes()[i];
                if(arg!=null && arg instanceof IRemoteListener && argType.isInterface()){//要支持回调的参数
                    IRemoteCallback cb = createProxyCallback(arg,argType);
                    KCallArgWrap proxyArg = new KCallArgWrap(cb,argType);
                    wrapArgs[i] = proxyArg;
                }else{
                    wrapArgs[i] = new KCallArgWrap(argTypes[i],arg);
                }
            }
        }
        return wrapArgs;
    }
}
