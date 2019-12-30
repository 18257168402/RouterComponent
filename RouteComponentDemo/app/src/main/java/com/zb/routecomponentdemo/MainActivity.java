package com.zb.routecomponentdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.zb.common.BuildConfig;
import com.zb.common.EventCompB1;
import com.zb.common.IRemoteCallback;
import com.zb.common.IRemoteSvrCompB;
import com.zb.common.IServiceCompA;
import com.zb.component.KBaseNavCallback;
import com.zb.component.KNavResult;
import com.zb.component.KRouter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@Route(path="/app/ui/main")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_tocompa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KRouter.build("/compa/ui/main")
                        .navigation();
            }
        });
        findViewById(R.id.btn_tocompa_svr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IServiceCompA compa =  (IServiceCompA)KRouter.build("/compa/svr/iml").navigation();
                if(compa!=null){
                    compa.test();
                }else{
                    Log.e("RouteComponent","==IServiceCompA not found==");
                }
            }
        });
        findViewById(R.id.btn_tocompb_ui).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KRouter.build("/compb/ui/main")
                        .navigation();
            }
        });
        findViewById(R.id.btn_tocompb_svr1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IRemoteSvrCompB compb =  (IRemoteSvrCompB)KRouter.build("/compb/svr/iml").navigation();
                if(compb!=null){
                    Log.e("RouteComponent","===compb svr1 mainprocess:"+android.os.Process.myPid());
                    compb.testRemote();
                }else{
                    Log.e("RouteComponent","==IRemoteSvrCompB not found==");
                }
            }
        });
        findViewById(R.id.btn_tocompb_svr2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IRemoteSvrCompB compb =  (IRemoteSvrCompB)KRouter.build("/compb/svr/iml").navigation();
                if(compb!=null){

                    compb.testRemoteCallback(new IRemoteCallback() {
                        @Override
                        public void onSuccess(String str) {
                            Log.e("RouteComponent","===compb testRemoteCallback onSuccess:"+str);
                        }

                        @Override
                        public void onError(int code) {

                        }
                    });
                }else{
                    Log.e("RouteComponent","==IRemoteSvrCompB not found==");
                }
            }
        });

        findViewById(R.id.btn_tocompb_svr3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IRemoteSvrCompB compb =  (IRemoteSvrCompB)KRouter.build("/compb/svr/iml").navigation();
                if(compb!=null){

                    compb.testRemoteEventBus();
                }else{
                    Log.e("RouteComponent","==IRemoteSvrCompB not found==");
                }
            }
        });
        findViewById(R.id.btn_tocompc_ui).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KRouter.build("/compc/ui/main")
                        .navigationForResult(MainActivity.this,new KBaseNavCallback(){
                            @Override
                            public void onResult(Postcard postcard, KNavResult result) {
                                super.onResult(postcard, result);
                                Log.e("RouteComponent","onResult "+result.getString("data"));
                            }
                        });
            }
        });
        KRouter.register(this);

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventFromOtherProcess(EventCompB1 event){
        Log.e("RouteComponent","event from compb:"+event.msg);
    }


}
