package com.zb.component.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;


import com.zb.component.utils.handler.KBaseHandlerRef;
import com.zb.component.utils.handler.KBaseThreadHandler;
import com.zb.component.utils.thread.KNetExecutorService;
import com.zb.component.utils.thread.KTaskLinker;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class KDispatcher {

    private static KNetExecutorService mExecutor;
    private static Handler mMainthreadHandler;
    private static Handler mThreadHandler;
    private static Object threadRef =new KBaseHandlerRef(){
        @Override
        public boolean isRefUseful() {
            return true;
        }
    };
    public static void init(Context context){
        mExecutor = new KNetExecutorService(context);
        mMainthreadHandler = new Handler(Looper.getMainLooper());
        mThreadHandler = KBaseThreadHandler.buildup(threadRef);
    }

    public static void shutDown(){
        if(mExecutor!=null){
            mExecutor.shutdown();
        }
        if(mThreadHandler!=null){
            mThreadHandler.getLooper().quit();
        }
    }

    public static KNetExecutorService getExecutor(){
        return mExecutor;
    }
    public static void post(Runnable cmd){
        mExecutor.execute(cmd);
    }
    public static void postDelay(final Runnable cmd,long delayms){
//        mThreadHandler.postDelayed(()->{
//            mExecutor.execute(cmd);
//        },delayms);
        mThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mExecutor.execute(cmd);
            }
        },delayms);
    }

    public static KTaskLinker postWithLinker(Runnable cmd){
        return mExecutor.executeWithLinker(cmd);
    }

    public static void serial(int type,Runnable cmd){
        mExecutor.executeSerial(type,cmd);
    }
    public static KTaskLinker serialWithLinker(int type, Runnable cmd){
        return mExecutor.executeSerialWithLinker(type,cmd);
    }
    public static <T> T blockSubmitSerial(int type,Callable<T> cmd){
        FutureTask<T> task = new FutureTask<>(cmd);
        serial(type,task);
        T result=null;
        try {
            result =  task.get();
        }catch (Exception e){

        }
        return result;
    }

    public static void postToUI(Runnable cmd){
        mMainthreadHandler.post(cmd);
    }
    public static void blockSumbmitOnUI(Runnable cmd){
        FutureTask<Void> task = new FutureTask<Void>(cmd,null);
        mMainthreadHandler.post(task);
        try {
            task.get();
        }catch (Exception e){

        }
    }
    public static <T> T blockSubmitOnUI(Callable<T> cmd){
        FutureTask<T> task = new FutureTask<>(cmd);
        mMainthreadHandler.post(task);
        T result=null;
        try {
            result =  task.get();
        }catch (Exception e){

        }
        return result;
    }
}
