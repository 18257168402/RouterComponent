package com.zb.component.utils;

import android.app.Application;
import android.content.Context;


public class KContextWrap {
    public static Context obtainApplication(){
        if(mAppContext!=null){
            return mAppContext;
        }
       return null;
    }

    private static Context mAppContext;
    public static void setApp(Context context){
        mAppContext = context;
    }
    public static Context wrap(Context context){
        if(context==null){
            return obtainApplication();
        }
        return context;
    }
    public static Application app(Context context){
        if(context==null){
            context = obtainApplication();
        }
        if(context==null){
            return null;
        }
        if(context instanceof Application){
            return (Application) context;
        }
        return (Application)context.getApplicationContext();
    }
}
