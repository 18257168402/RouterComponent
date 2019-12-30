package com.zb.comp_a;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.zb.component.IComponentService;
import com.zb.component.KRouter;
import com.zb.component.plugins.EventBusFactory;


@Route(path = "/compa/svr/component")
public class ComponentA implements IProvider,IComponentService{
    @Override
    public void init(Context context) {
        Log.e("RouteComponent","==ComponentA init==");

    }
}
