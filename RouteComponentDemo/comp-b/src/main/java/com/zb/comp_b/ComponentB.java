package com.zb.comp_b;

import android.content.Context;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.zb.component.IComponentService;

@Route(path = "/compb/svr/component")
public class ComponentB implements IProvider,IComponentService {
    @Override
    public void init(Context context) {
        Log.e("RouteComponent","==ComponentB init==");
    }
}

