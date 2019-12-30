package com.zb.comp_b;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.zb.common.EventCompB1;
import com.zb.common.IRemoteCallback;
import com.zb.common.IRemoteSvrCompB;
import com.zb.component.ComponentProcess;
import com.zb.component.IRemoteListener;
import com.zb.component.KRouter;


@ComponentProcess(process = ":compb")
@Route(path = "/compb/svr/iml")
public class ServiceCompBImpl implements IRemoteSvrCompB{

    Handler handler;
    @Override
    public void testRemote() {
        Log.e("RouteComponent","==testRemote== process:"+android.os.Process.myPid());
    }

    @Override
    public void testRemoteCallback(IRemoteCallback listener) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess("hello world!");
            }
        },2000);
    }

    @Override
    public void testRemoteEventBus() {
        EventCompB1 ev = new EventCompB1();
        ev.msg="hello remote event bus";
        KRouter.postAllProcess(ev);
    }

    @Override
    public void init(Context context) {
        handler = new Handler(Looper.getMainLooper());
    }
}
