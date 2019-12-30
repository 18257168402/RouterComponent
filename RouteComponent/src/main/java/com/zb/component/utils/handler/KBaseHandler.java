package com.zb.component.utils.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2016/3/18.
 */
public class KBaseHandler<T> extends Handler {

    protected WeakReference<T> mRef;

    public KBaseHandler(T ref) {
        super();
        mRef = new WeakReference<>(ref);
    }

    public KBaseHandler(T ref, Looper looper) {
        super(looper);
        mRef = new WeakReference<>(ref);
    }

    protected KBaseHandler(T ref, Looper looper, Callback cb) {
        super(looper, cb);
        mRef = new WeakReference<>(ref);
    }

    public T ref() {
        return mRef.get();
    }

    protected static <T> boolean checkRef(WeakReference<T> mRef) {
        if (mRef == null) {
            return false;
        }
        if (mRef.get() == null) {
            return false;
        }
        T ref = mRef.get();
        if (ref instanceof KBaseHandlerRef) {
            if (!((KBaseHandlerRef) ref).isRefUseful()) {
                Log.e("Delay","====!isRefUseful== ");
                return false;
            }
        }
        if (mRef == null) {
            return false;
        }
        return true;
    }

    @Override
    final public void handleMessage(Message msg) {
        try {
            if (!checkRef(mRef)) {
                return;
            }
            super.handleMessage(msg);
            safeHandleMessage(ref(), msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    final public void dispatchMessage(Message msg) {
//        Log.e("Delay", "====dispatchMessage== ");
        try {
            if (!checkRef(mRef)) {
                Log.e("RemoteFileSearch","==dispatchMessage=!checkRef");
                return;
            }
            super.dispatchMessage(msg);
            safeDispatchMessage(ref(), msg);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Delay", "====dispatchMessageError== " + e.toString());
        }
    }

    public void safeHandleMessage(T ref, Message msg) {

    }

    public void safeDispatchMessage(T ref, Message msg) {

    }
}
