package com.zb.comp_c;

import android.content.Context;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.zb.common.IServiceCompC;

@Route(path = "/compc/svr/iml")
public class ServiceCompCImpl implements IServiceCompC {
    @Override
    public void test() {
        Log.e("RouteComponent","==ServiceCompCImpl test==");
    }

    @Override
    public void init(Context context) {

    }
}
