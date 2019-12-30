package com.zb.routecomponentdemo;

import android.content.Context;
import android.util.Log;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.zb.component.IServiceInterceptor;
import com.zb.component.KInvokeResult;
import com.zb.component.KPostcard;

public class SvrInterceptor implements IProvider,IServiceInterceptor {
    @Override
    public void init(Context context) {

    }

    @Override
    public KInvokeResult beforeInvoke(KPostcard kPostcard, String methodName, Object[] objects) {
        Log.e("RouteComponent","==beforeInvoke:"+kPostcard.getPath()+" method:"+methodName+"==");
        return KInvokeResult.onContinue();
    }

    @Override
    public KInvokeResult afterInvoke(KPostcard kPostcard, String methodName, Object o, Object[] objects) {
        Log.e("RouteComponent","==afterInvoke:"+kPostcard.getPath()+" method:"+methodName+"==");
        return KInvokeResult.onContinue();
    }
}
