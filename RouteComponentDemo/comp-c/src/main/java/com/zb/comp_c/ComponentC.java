package com.zb.comp_c;

import android.content.Context;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.zb.component.IComponentService;
@Route(path = "/compc/svr/component")
public class ComponentC implements IProvider,IComponentService {
    @Override
    public void init(Context context) {
        Log.e("RouteComponent","==ComponentC init==");
    }
}
