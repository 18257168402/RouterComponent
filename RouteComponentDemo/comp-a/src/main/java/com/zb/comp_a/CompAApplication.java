package com.zb.comp_a;

import android.app.Application;
import android.util.Log;

import com.zb.component.KRouter;
import com.zb.component.plugins.EventBusFactory;

public class CompAApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if(KRouter.isRunningComponent("comp-a")){
            Log.e("RouteComponent",">>>>KRouteInit");
            KRouter.init(this);
            KRouter.enableEventBus(new EventBusFactory());
        }
    }
}
