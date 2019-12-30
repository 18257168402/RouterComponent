package com.zb.comp_a;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.zb.common.IServiceCompC;
import com.zb.component.KRouter;

@Route(path="/compa/ui/main")
public class MainActivity extends AppCompatActivity {
    IServiceCompC compc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comp_activity_main);
        findViewById(R.id.compa_btn_nav_c).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compc = (IServiceCompC)KRouter.build("/compc/svr/iml").navigation();
                if(compc!=null){
                    compc.test();
                }else{
                    Log.e("RouteComponent","==IServiceCompC not found==");
                }
            }
        });
    }
}
