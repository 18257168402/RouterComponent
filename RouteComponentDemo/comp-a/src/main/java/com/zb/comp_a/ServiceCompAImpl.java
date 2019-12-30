package com.zb.comp_a;

import android.content.Context;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.zb.common.IServiceCompA;


@Route(path = "/compa/svr/iml")
public class ServiceCompAImpl implements IServiceCompA{
    @Override
    public void init(Context context) {

    }

    @Override
    public void test() {
        Log.e("RouteComponent","==ServiceCompAImpl test==");
    }
}
