package com.zb.comp_b;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.zb.component.ComponentProcess;

@ComponentProcess(process = ":compb")
@Route(path="/compb/ui/main")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compb_activity_main);
    }
}
