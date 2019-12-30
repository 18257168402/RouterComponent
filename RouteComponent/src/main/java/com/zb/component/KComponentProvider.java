package com.zb.component;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

public class KComponentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
       // Log.e("ProcessTest",">>>KComponentProvider onCreate");
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String[] columns = {"path","type","serviceAction","pid","interfaces","process"};


        ArrayList<KComponentRoute> routes = KARouteComponent.getRouteTable();
        MatrixCursor cursor = new MatrixCursor(columns,routes.size());
        for (KComponentRoute route:routes){
            cursor.addRow(new Object[]{route.path,route.type,route.serviceAction, Process.myPid(),JSON.toJSONString(route.interfaceList),route.process});
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        //Log.e("ProcessTest",">>>KComponentProvider insert");
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
    public static List<KComponentRoute> remoteRouteTable(Context context, String url){
        List<KComponentRoute> routes = new ArrayList<>();
        try {
            Uri uri = Uri.parse(url);
            ContentValues values = new ContentValues();
            ContentResolver resolver =  context.getContentResolver();
            Cursor cursor = resolver.query(uri,null,null,null,null);
            if(cursor!=null){
                while (cursor.moveToNext()){
                    List<String> interfaces = JSON.parseArray(cursor.getString(4),String.class);
                    List<Class> validInterfaces = new ArrayList<>();
                    for (String item:interfaces){
                        try {
                            Class clazz = Class.forName(item);
                            if(clazz!=null){
                                validInterfaces.add(clazz);
                            }
                        }catch (Exception e){}
                    }
                    Class[] interfaceArr = new Class[validInterfaces.size()];
                    for (int i=0;i<validInterfaces.size();i++){
                        interfaceArr[i] = validInterfaces.get(i);
                    }

                    KComponentRoute route = new KComponentRoute(cursor.getString(0),cursor.getInt(1),interfaceArr);
                    route.serviceAction = cursor.getString(2);
                    route.pid = cursor.getInt(3);
                    route.process = cursor.getString(5);
                    routes.add(route);
                }
                cursor.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //Log.e("RouteTest",">>>remoteRouteTable:"+url+" routeTable:"+ JSON.toJSONString(routes));
        return routes;
    }
}
