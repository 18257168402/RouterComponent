package com.zb.component.utils.handler;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2016/4/13.
 */
public class KBaseThreadHandler<T> extends KBaseHandler<T> {

    public interface SafeCallback<T>{
        boolean  safeHandleMessage(T ref, Message msg);
    }

    protected KBaseThreadHandler(T ref, Looper looper){
        super(ref,looper);
    }
    protected KBaseThreadHandler(T ref, Looper looper, Handler.Callback cb){
        super(ref,looper,cb);
        mRef= new WeakReference<>(ref);
    }

   public static<T> KBaseThreadHandler<T> buildup(T ref){
       HandlerThread handleThread=new MyThreadHandler();
       handleThread.start();
       KBaseThreadHandler<T> handler=new KBaseThreadHandler<T>(ref, handleThread.getLooper());
       return handler;
   }
    public static <T> KBaseThreadHandler<T> buildup(T ref, SafeCallback<T> innerHandler){
        //LogUtils.e("HandlerBuild","----buildup 22-------");
        HandlerThread handleThread=new MyThreadHandler();
        handleThread.start();
        KBaseThreadHandler<T> handler=new KBaseThreadHandler<T>(ref, handleThread.getLooper(),new InnerCallback<T>(ref,innerHandler));
        return handler;
    }

    private static class InnerCallback<T> implements Handler.Callback {
        protected WeakReference<T> mRef;
        protected WeakReference<SafeCallback<T>> mCbRef;
        InnerCallback(T ref,SafeCallback<T> mCallback){
            mRef=new WeakReference<T>(ref);
            mCbRef=new WeakReference<SafeCallback<T>>(mCallback);
        }
        @Override
        public boolean handleMessage(Message msg) {
            //Log.e("TestCtrl","==handleMessage==　"+msg.what);
            boolean bres=false;
            try {
                if(!checkRef(mRef)){return false;}
                if(!checkRef(mCbRef)){return false;}
                bres=mCbRef.get().safeHandleMessage(mRef.get(),msg);
            }catch (Exception e){
                //e.printStackTrace();
            }
            return bres;

        }
    }
    private static class  MyThreadHandler extends HandlerThread {
        public MyThreadHandler(){super("MyThreadHandler");}
        @Override
        public void run() {
            //LogUtils.e("MyThreadHandler","---buildup--");
            super.run();
            //LogUtils.e("MyThreadHandler","---quit--");
        }
    }
}
