package com.zb.comp_c;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.zb.component.KNavResult;
import com.zb.component.KRouter;

@Route(path="/compc/ui/main")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compc_activity_main);

        KRouter.result(getIntent(),new KNavResult().withString("data","hello route comp") );
    }
}
