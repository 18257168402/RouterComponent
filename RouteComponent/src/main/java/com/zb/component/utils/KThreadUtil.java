package com.zb.component.utils;

import android.os.Looper;

/**
 * Created by lss on 16-11-23.
 */

public class KThreadUtil {

    public static void setThreadCurName(String name){
        Thread.currentThread().setName(name);
    }
    public static boolean isUiThread(){
        if(Thread.currentThread()==Looper.getMainLooper().getThread()){
            return true;
        }
        return false;
    }
}
