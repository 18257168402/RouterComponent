package com.zb.component;

import com.alibaba.android.arouter.facade.enums.RouteType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KComponentRoute implements Serializable{
    public String path;
    public int type;
    public String serviceAction;
    public String process;
    public int pid;
    public List<String> interfaceList;
    public KComponentRoute(String path,int type,Class[] interfaces){
        this.path = path;
        this.type = type;
        interfaceList = new ArrayList<>();
        process = "";
        if(type == RouteType.PROVIDER.getId()){
            for (Class item:interfaces){
                interfaceList.add(item.getName());
            }
        }
    }

    @Override
    public String toString() {
        return "KComponentRoute{" +
                "path='" + path + '\'' +
                ", type=" + type +
                ", serviceAction='" + serviceAction + '\'' +
                ", pid=" + pid +
                '}';
    }
}
