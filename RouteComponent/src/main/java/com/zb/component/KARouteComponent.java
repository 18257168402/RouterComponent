package com.zb.component;

import android.content.Context;
import android.util.Log;

import com.alibaba.android.arouter.base.UniqueKeyTreeMap;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.enums.RouteType;
import com.alibaba.android.arouter.facade.model.RouteMeta;
import com.alibaba.android.arouter.facade.template.IInterceptor;
import com.alibaba.android.arouter.facade.template.IInterceptorGroup;
import com.alibaba.android.arouter.facade.template.IProviderGroup;
import com.alibaba.android.arouter.facade.template.IRouteGroup;
import com.alibaba.android.arouter.facade.template.IRouteRoot;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.android.arouter.utils.ClassUtils;
import com.alibaba.android.arouter.utils.PackageUtils;
import com.zb.component.utils.KAppUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.alibaba.android.arouter.utils.Consts.AROUTER_SP_CACHE_KEY;
import static com.alibaba.android.arouter.utils.Consts.AROUTER_SP_KEY_MAP;
import static com.alibaba.android.arouter.utils.Consts.DOT;
import static com.alibaba.android.arouter.utils.Consts.ROUTE_ROOT_PAKCAGE;
import static com.alibaba.android.arouter.utils.Consts.SDK_NAME;
import static com.alibaba.android.arouter.utils.Consts.SEPARATOR;
import static com.alibaba.android.arouter.utils.Consts.SUFFIX_INTERCEPTORS;
import static com.alibaba.android.arouter.utils.Consts.SUFFIX_PROVIDERS;
import static com.alibaba.android.arouter.utils.Consts.SUFFIX_ROOT;

public class KARouteComponent {

    public  static class ProcessInfo{
        String process;
        RouteMeta meta;
        ProcessInfo(String process,RouteMeta meta){
            this.process = process;
            this.meta = meta;
        }
    }

    static Map<String, Class<? extends IRouteGroup>> groupsIndex = new HashMap<>();
    static Map<String, RouteMeta> providersIndex = new HashMap<>();
    static Map<Integer, Class<? extends IInterceptor>> interceptorsIndex = new UniqueKeyTreeMap<>("More than one interceptors use same priority [%s]");
    static Map<String, RouteMeta> routes = new HashMap<>();

    static Map<String,ProcessInfo> pathProcessMap = new HashMap<>();

    static ArrayList<Object> componentServices = new ArrayList<>();
    static ArrayList<Object> interceptServices = new ArrayList<>();
    static ArrayList<KComponentRoute> routeTable = new ArrayList<>();

    public static boolean isInited =false;
    public static void loadRoute(Context context){
        Set<String> routerMap=null;

        try {
            // It will rebuild router map every times when debuggable.
            if (true) {//ARouter.debuggable() || PackageUtils.isNewVersion(context)
                //Log.e("Aroute",">>>>loadRoute 1");
                //logger.info(TAG, "Run with debug mode or new install, rebuild router map.");
                // These class was generated by arouter-compiler.
                routerMap = ClassUtils.getFileNameByPackageName(context, ROUTE_ROOT_PAKCAGE);
                if (!routerMap.isEmpty()) {
                    context.getSharedPreferences(AROUTER_SP_CACHE_KEY, Context.MODE_PRIVATE).edit().putStringSet(AROUTER_SP_KEY_MAP, routerMap).apply();
                }

                PackageUtils.updateVersion(context);    // Save new version name when router map update finishes.
            } else {
                //Log.e("Aroute",">>>>loadRoute 2");
               // logger.info(TAG, "Load router map from cache.");
                routerMap = new HashSet<>(context.getSharedPreferences(AROUTER_SP_CACHE_KEY, Context.MODE_PRIVATE).getStringSet(AROUTER_SP_KEY_MAP, new HashSet<String>()));
            }
            groupsIndex.clear();
            interceptorsIndex.clear();
            providersIndex.clear();
            for (String className : routerMap) {
                //Log.e("ARouteInit",">>>>>>>>>>>>>>>>className:"+className);
                if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
                    // This one of root elements, load root.
                    ((IRouteRoot) (Class.forName(className).getConstructor().newInstance())).loadInto(groupsIndex);
                } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_INTERCEPTORS)) {
                    // Load interceptorMeta
                    ((IInterceptorGroup) (Class.forName(className).getConstructor().newInstance())).loadInto(interceptorsIndex);
                } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + DOT + SDK_NAME + SEPARATOR + SUFFIX_PROVIDERS)) {
                    // Load providerIndex
                    ((IProviderGroup) (Class.forName(className).getConstructor().newInstance())).loadInto(providersIndex);
                }
            }

            for(String group:groupsIndex.keySet()){
                Class<? extends IRouteGroup> groupMetaItem = groupsIndex.get(group);
                IRouteGroup iGroupInstance = groupMetaItem.getConstructor().newInstance();
                iGroupInstance.loadInto(routes);
            }
            for (String key:routes.keySet()){
                RouteMeta item = routes.get(key);

                KComponentRoute route = new KComponentRoute(key,item.getType().getId(),item.getDestination().getInterfaces());
                route.serviceAction = KRemoteComponentHandler.componentAction("");
                if(item.getType() == RouteType.PROVIDER ||
                        item.getType() == RouteType.ACTIVITY){
                    Class clazz = item.getDestination();//provider目标类
                    ComponentProcess processInfo = (ComponentProcess)clazz.getAnnotation(ComponentProcess.class);
                    if(processInfo!=null){
                        route.serviceAction =  KRemoteComponentHandler.componentAction(processInfo.process());
                        route.process = processInfo.process();
                        pathProcessMap.put(item.getPath(),new ProcessInfo(processInfo.process(),item));
                    }
                }
                routeTable.add(route);
            }
            //LogUtil.e("Routes",">>>>>>routeTable:"+ JSON.toJSONString(routeTable));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isLocalGroup(String group){
        for (String key:routes.keySet()){
            RouteMeta item = routes.get(key);
            if(item.getGroup().equals(group)){
                return true;
            }
        }
        return false;
    }
    public static boolean isSubProcessGroup(String group){
        for (String key:pathProcessMap.keySet()){
            ProcessInfo item = pathProcessMap.get(key);
            if(item.meta.getGroup().equals(group)&& !isSameProcess(item.process)){
                return true;
            }
        }
        return false;
    }


    public static boolean isFoundInLocal(Postcard postcard){
        return routes.get(postcard.getPath())!=null;
    }
//    public static boolean isRunInOtherProcess(Postcard postcard){
//        ProcessInfo processInfo = pathProcessMap.get(postcard.getPath());
//        if(processInfo==null){
//            return !KAppUtils.processName(mContext).equals(mContext.getPackageName());
//        }
//        return !KAppUtils.processName(mContext).equals(mContext.getPackageName()+processInfo.process);
//    }
    public static boolean isSameProcess(String process){
        return KAppUtils.processName(mContext).equals(mContext.getPackageName()+process);
    }
    public static boolean isRunInProcess(String path){//组件在当前进程内运行
        ProcessInfo processInfo = pathProcessMap.get(path);
        if(processInfo==null){//主进程
            //Log.e("ARouteInit","no process annotation!!");
            return KAppUtils.processName(mContext).equals(mContext.getPackageName());
        }
        //Log.e("ARouteInit","have process annotation!! "+processInfo.process);
        return KAppUtils.processName(mContext).equals(mContext.getPackageName()+processInfo.process);
    }
    public static Map<String,ProcessInfo> getProcessInfo(){
        return pathProcessMap;
    }

//    private static void loadProcessInfo(){
//        for(String key:routes.keySet()){
//            RouteMeta item = routes.get(key);
//            if(item.getType() == RouteType.PROVIDER ||
//                    item.getType() == RouteType.ACTIVITY){
//                Class clazz = item.getDestination();//provider目标类
//                ComponentProcess processInfo = (ComponentProcess)clazz.getAnnotation(ComponentProcess.class);
//                if(processInfo!=null){
//                    pathProcessMap.put(item.getPath(),new ProcessInfo(processInfo.process()));
//                }
//                for (int i=0;i<routeTable.size();i++){
//                    KComponentRoute route = routeTable.get(i);
//                    if(route.path.equals(item.getPath())){
//                        route.serviceAction =  KRemoteComponentHandler.componentAction();
//                    }
//                }
//            }
//        }
//    }


    public synchronized static ArrayList<KComponentRoute> getRouteTable(){
        return routeTable;
    }

    private static Context mContext;
    public synchronized static void init(Context context){
        if(isInited){
            return;
        }
        isInited = true;
        mContext = context;
        KRemoteComponentHandler.setContext(context);
        loadRoute(context);
        //loadProcessInfo();
        //Log.e("ARouteInit",">>>>>providersIndex "+providersIndex.toString());
        for (Map.Entry<String, RouteMeta> entry : providersIndex.entrySet()) {
            String key = entry.getKey();
            RouteMeta meta = entry.getValue();
            boolean isRunInProcess = isRunInProcess(meta.getPath());
            //Log.e("ARouteInit",">>>key:"+key+" meta:"+meta+" isRunInProcess:"+isRunInProcess);
            if(isRunInProcess){//初始化本进程组件服务以及服务拦截器
                if(IComponentService.class.isAssignableFrom(meta.getDestination())){
                    //Log.e("ARouteInit",">>>navigate:"+meta.getPath());
                    Object componentService = KRouter.build(meta.getPath()).navigation();
                    componentServices.add(componentService);
                }
                if(IServiceInterceptor.class.isAssignableFrom(meta.getDestination())){//拦截器不跨进程
                    Object interceptorService = KRouter.build(meta.getPath()).navigation();
                    interceptServices.add(interceptorService);
                }
            }
        }
    }

    public static String getComponentRoutePrefix(){
        String componentProvider_prefix = "com.zb.component";
        try {
            Class clazz = Class.forName("com.zb.component.PROVIDER_PREFIX");
            if(clazz!=null){
               Field field = clazz.getField("PROVIDER_HEADER");
               if(field!=null){
                   componentProvider_prefix = (String) field.get(clazz);
               }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return componentProvider_prefix;
    }
    public static String getRunningComponent(){
        String runningComponent = "";
        try {
            Class clazz = Class.forName("com.zb.component.PROVIDER_PREFIX");
            if(clazz!=null){
                Field field = clazz.getField("runComponent");
                if(field!=null){
                    runningComponent = (String) field.get(clazz);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return runningComponent;
    }
    public static String buildRoutetableProvider(Postcard postcard){
        return buildRoutetableProvider(postcard.getGroup());
    }
    public static String buildRoutetableProvider(String  group){
        return "content://"+getComponentRoutePrefix()+"."+group+".comp_provider/route_table";
    }
    public static String buildComponentServiceAction(Postcard postcard){
        return getComponentRoutePrefix()+"."+postcard.getGroup()+".comp_svr";
    }
}