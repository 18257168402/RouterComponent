package com.zb.routecomponentdemo;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.zb.component.KRouter;
import com.zb.component.plugins.EventBusFactory;

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ARouter.openDebug();
        ARouter.openLog();
        if(KRouter.isRunningComponent("comp-app")){
            KRouter.init(this);
            KRouter.enableEventBus(new EventBusFactory());
        }

    }
}
