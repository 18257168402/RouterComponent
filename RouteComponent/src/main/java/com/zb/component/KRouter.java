package com.zb.component;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.facade.enums.RouteType;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.android.arouter.launcher.ARouter;
import com.zb.component.utils.KContextWrap;
import com.zb.component.utils.KDispatcher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class KRouter {

    public static KPostcard build(String path){
        return new KPostcard(ARouter.getInstance().build(path));
    }
    public static KPostcard build(Uri url) {
        return new KPostcard(ARouter.getInstance().build(url));
    }

    public static <T> T navigation(Class<? extends T> service) {
        return ARouter.getInstance().navigation(service);
    }

    public static void inject(Object obj){
        ARouter.getInstance().inject(obj);
    }

    private static IEventBusFactory mEvFactory;
    public static void enableEventBus(IEventBusFactory eventBusFactory){
        mEvFactory = eventBusFactory;
    }

    public static void register(Object target){
        if(mEvFactory!=null){
            mEvFactory.eventBus().register(target);
        }
    }
    public static void unregister(Object target){
        if(mEvFactory!=null){
            mEvFactory.eventBus().unregister(target);
        }
    }
    public static void post(Object event){//只向本进程投递
        if(mEvFactory!=null){
            mEvFactory.eventBus().post(event);
        }
    }
    public static void postAllProcess(Object event){//向本进程和本应用其他组件进程投递
        if(mEvFactory!=null){
            mEvFactory.eventBus().post(event);
        }
        postToSubProcess("",event);//子进程
    }
    public static void postOtherProcess(Object event){//想本应用其他组件进程投递
        postToSubProcess("",event);//子进程
    }
    public static void postOtherConnectedApp(Object event){
        KRemoteStation.getInstance().eventPostToConnected(event,true);
    }
    public static void postLocal(Object event){//向本进程，以及已经连接上的进程投递
        if(mEvFactory!=null){
            mEvFactory.eventBus().post(event);
        }
        KRemoteStation.getInstance().eventPostToConnected(event,false);
    }
    public static void postGroup(String group,Object event){//向group所在进程投递
        if(!KARouteComponent.isLocalGroup(group)){
            postToRemote(group,event);
            return;
        }
        if(KARouteComponent.isSubProcessGroup(group)){
            postToSubProcess(group,event);
            return;
        }
        post(event);
    }
    public static boolean isRunningComponent(String compname){
        return KARouteComponent.getRunningComponent().equals(compname);
    }
    private static void postToRoutes(List<KComponentRoute> targetRoutes,String group,Object event){
        for (KComponentRoute item:targetRoutes){
            try {
                //Log.e("EventBus","postToRoutes "+item.process+" action:"+item.serviceAction);
                KRemoteStation.getInstance().remotePost(KContextWrap.obtainApplication(),item.serviceAction,event);
            }catch (Exception e){}
        }
    }
    private static List<KComponentRoute> targetRouteByGroup( List<KComponentRoute> routes,String group,boolean isSubProcess){
        //Log.e("EventBus",">>>routes:"+ JSON.toJSONString(routes));
        List<KComponentRoute> targetRoutes  = new ArrayList<>();
        for (KComponentRoute route:routes){//所有的路由
            KPostcard postcard = build(route.path);
            if(!TextUtils.isEmpty(group) && !postcard.getGroup().equals(group)){
                //Log.e("EventBus",">>>>not eql group:"+route.path);
                continue;
            }
            if(isSubProcess){//如果只向子进程发送，那么把主进程和相同进程过滤掉
//                if( TextUtils.isEmpty(route.process)){
//                  //  Log.e("EventBus",">>>>main process ret:"+route.path);
//                    continue;
//                }
                if(KARouteComponent.isSameProcess(route.process)){
                    //Log.e("EventBus",">>>>isSameProcess ret:"+route.path+" process: "+route.process+" action:"+route.serviceAction);
                    continue;
                }
            }
            boolean isFound = false;
            for (KComponentRoute item:targetRoutes){
                if(item.process.equals(route.process)){
                    isFound = true;
                    break;
                }
            }
            if(!isFound){
                //Log.e("EventBus",">>>>target route ret:"+route.path+" process: "+route.process+" action:"+route.serviceAction);
                targetRoutes.add(route);
            }
        }
        //Log.e("EventBus","target routes count:"+targetRoutes.size());
        return targetRoutes;
    }
    private static void postToRemote(String group,Object event){
        List<KComponentRoute> routes =  readRouteTable(KContextWrap.obtainApplication(),group);
        if(routes == null || routes.size() == 0){
            return;
        }

        postToRoutes(targetRouteByGroup(routes,group,false),group,event);
    }
    private static void postToSubProcess(String group,Object event){
        List<KComponentRoute> routes = KARouteComponent.getRouteTable();
        if(routes == null || routes.size() == 0){
            return;
        }
        postToRoutes(targetRouteByGroup(routes,group,true),group,event);
    }


    private static class KCbInfo{
        KPostcard postcard;
        KNavigationCallback callback;
    }
    private final static Object CALLBACKLCK =new Object();
    private static SparseArray<KCbInfo> callbackMap = new SparseArray<>();

    private static Handler uiHandler = new Handler(Looper.getMainLooper());
    private static void waitResponse(final int req,final KPostcard postcard,final KNavigationCallback callback){
        synchronized (CALLBACKLCK){
            KCbInfo info = new KCbInfo();
            info.postcard = postcard;
            info.callback = callback;
            callbackMap.append(req,info);
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronized (CALLBACKLCK){
                        callbackMap.remove(req);
                        if(callback!=null){
                            callback.onTimeout(postcard.mPostcard);
                        }
                    }
                }
            }, 5000);
        }
    }

    private static void result(int req,KNavResult result){
        if(req!=-1){
            KCbInfo info = callbackMap.get(req);
            if(info!=null && info.callback!=null){
                info.callback.onResult(info.postcard.mPostcard,result);
                callbackMap.remove(req);
            }
        }
    }
    public static void result(Postcard postcard,KNavResult result){
        synchronized (CALLBACKLCK){
            int req = postcard.getExtras().getInt("__seq",-1);
            result(req,result);
        }

    }
    public static void result(Intent intent, KNavResult result){
        synchronized (CALLBACKLCK){
           Bundle reqBundle = intent.getExtras();
           if(reqBundle!=null){
               int req = reqBundle.getInt("__seq",-1);
               result(req,result);
           }
        }
    }


    private static class ProviderInvocationHandler implements InvocationHandler{
        private IProvider mProvider;
        private KPostcard mPostcard;
        public ProviderInvocationHandler(IProvider provider,KPostcard postcard){
            mProvider=provider;
            mPostcard = postcard;
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {//provider调用代理
            //LogUtil.e("ProviderInvoke",">>>ProviderInvocationHandler invoke:"+method.getName());
            ArrayList<Object> interceptServices = KARouteComponent.interceptServices;

            for (int i=0;i<interceptServices.size();i++){
                IServiceInterceptor interceptor = (IServiceInterceptor)interceptServices.get(i);
                KInvokeResult result = interceptor.beforeInvoke(mPostcard,method.getName(),args);
                if(result.isReturn){
                    return result.result;
                }
            }
            Object realResult = null;
            try {
                realResult = method.invoke(mProvider,args);
            }catch (Throwable e){
                e.printStackTrace();
            }

            for (int i = 0; i < interceptServices.size(); i++) {
                IServiceInterceptor interceptor = (IServiceInterceptor) interceptServices.get(i);
                KInvokeResult result = interceptor.afterInvoke(mPostcard, method.getName(), realResult, args);
                if (result.isReturn) {
                    return result.result;
                }
            }
            return realResult;
        }
    }
    private static IProvider dynamicProvider(KPostcard postcard,IProvider provider){
        Class clazz = provider.getClass();
        Class[] interfaces = clazz.getInterfaces();
        ClassLoader loader = clazz.getClassLoader();
        IProvider proxy =(IProvider)Proxy.newProxyInstance(loader,interfaces,new ProviderInvocationHandler(provider,postcard));
        return proxy;
    }

    private static class RemoteProviderInvocationHandler implements InvocationHandler{
        private Context mContext;
        private KPostcard mPostcard;
        private KNavigationCallback mCallback;
        private String action;
        RemoteProviderInvocationHandler(Context context,String action,KPostcard postcard,KNavigationCallback callback){
            this.mContext = KContextWrap.wrap(context);
            this.mPostcard = postcard;
            this.mCallback = callback;
            this.action = action;
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)throws Exception {
            //Log.e("RouteTest",">>provider proxy invoke url:"+mPostcard.getPath()+" method:"+method.getName());
            return KRemoteStation.getInstance().providerCall(mContext,mPostcard.mPostcard,action,method,args,mCallback);
        }
    }
    private static Object proxyRemoteProvider(Context mContext,KPostcard postcard,KComponentRoute route,KNavigationCallback callback){
        Class[] interfaces = new Class[route.interfaceList.size()];
        int index = 0;
        for (int i=0;i<route.interfaceList.size();i++){
            try {
                interfaces[index++] = Class.forName(route.interfaceList.get(i));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        ClassLoader loader = postcard.getClass().getClassLoader();
        return Proxy.newProxyInstance(loader,interfaces,new RemoteProviderInvocationHandler(mContext,route.serviceAction,postcard,callback));
    }

    private static Object remoteProviderNavigation(Context mContext,KComponentRoute route, KPostcard postcard, int requestCode, KNavigationCallback callback){
        //LogUtil.e("RouteTest","remoteProviderNavigation!!!");
        boolean bConn = KComponentConnector.getInstance().connectRemoteComponent(
                mContext,route.serviceAction);
        if(bConn){
            return proxyRemoteProvider(mContext,postcard,route,callback);
        }
        return null;
    }
    private static Object remoteActivityNavigation(Context mContext,KComponentRoute route, KPostcard postcard, int requestCode,boolean isForResult, KNavigationCallback callback){
        try {
            KRemoteStation.getInstance().remoteNavigationActivity(KContextWrap.wrap(mContext),requestCode,isForResult,postcard.mPostcard,route.serviceAction,callback);
        }catch (KRemoteTimeoutException e){
            e.printStackTrace();
            if(callback!=null){
                callback.onLost(postcard.mPostcard);
            }
            return null;
        }catch (RemoteException e){
            e.printStackTrace();
            if(callback!=null){
                callback.onLost(postcard.mPostcard);
            }
            return null;
        }
        return null;
    }

    private static AtomicInteger mSeq = new AtomicInteger(1);
    private static Object remoteNavigationWithRoute(Context mContext,List<KComponentRoute> remoteRoutes, KPostcard postcard, int requestCode,boolean isForResult, KNavigationCallback callback){
        boolean foundInRoutetable = false;
        KComponentRoute remoteRoute = null;
        for (KComponentRoute route:remoteRoutes){
            if(route.path.equals(postcard.getPath())){
                foundInRoutetable = true;
                remoteRoute = route;
            }
        }
        if(!foundInRoutetable){
            if(callback!=null){
                callback.onLost(postcard.mPostcard);
            }
            return null;
        }
        //LogUtil.e("RouteTest","<<<<remoteNavigation route:"+remoteRoute);
        if(remoteRoute.type ==RouteType.ACTIVITY.getId()){
            return remoteActivityNavigation(mContext,remoteRoute,postcard,requestCode,isForResult,callback);
        }else if(remoteRoute.type == RouteType.PROVIDER.getId()){
            return remoteProviderNavigation(mContext,remoteRoute,postcard,requestCode,callback);
        }else{
            if(callback!=null){
                callback.onLost(postcard.mPostcard);
            }
            return null;
        }
    }

    private static List<KComponentRoute> readRouteTable(Context context,String group){
        String provider_uri = KARouteComponent.buildRoutetableProvider(group);
        return KComponentProvider.remoteRouteTable(KContextWrap.wrap(context),provider_uri);

    }

    private static Object remoteNavigation(Context mContext, final KPostcard postcard, int requestCode,final boolean isForResult, KNavigationCallback callback){
        List<KComponentRoute> remoteRoutes =readRouteTable(mContext,postcard.getGroup());
        if(remoteRoutes==null || remoteRoutes.size()==0){
            if(callback!=null){
                callback.onLost(postcard.mPostcard);
            }
            return null;
        }
        return remoteNavigationWithRoute(KContextWrap.wrap(mContext),remoteRoutes,postcard,requestCode,isForResult,callback);
    }
    private static Object processNavigation(Context mContext, KPostcard postcard, int requestCode,boolean isForResult, KNavigationCallback callback){
        List<KComponentRoute> remoteRoutes = KARouteComponent.getRouteTable();
        return remoteNavigationWithRoute(mContext,remoteRoutes,postcard,requestCode,isForResult,callback);
    }
    public static Object navigation(Context mContext, KPostcard postcard, int requestCode, KNavigationCallback callback) {
        return navigation(mContext,postcard,requestCode,false,callback);
    }
    public static Object navigation(Context mContext,final KPostcard postcard, int requestCode,final boolean isForResult,final KNavigationCallback callback) {
        if(!KARouteComponent.isFoundInLocal(postcard.mPostcard)){//非本地组件
            //LogUtil.e("RouteTest","remote navigation!!!");
            return remoteNavigation(KContextWrap.wrap(mContext),postcard,requestCode,isForResult,callback);
        }
        if(!KARouteComponent.isRunInProcess(postcard.mPostcard.getPath())){//当前app的其他进程组件
            return processNavigation(KContextWrap.wrap(mContext),postcard,requestCode,isForResult,callback);
        }
        return ARouter.getInstance().navigation(KContextWrap.wrap(mContext), postcard.mPostcard, requestCode, new NavigationCallback() {
            @Override
            public void onFound(final Postcard pc) {
                if(callback!=null){
                    callback.onFound(pc);
                }
                if(pc.getType() == RouteType.ACTIVITY && isForResult){
                    int seq = mSeq.incrementAndGet();
                    pc.withInt("__seq",seq);
                    waitResponse(seq,postcard,(KNavigationCallback)callback);
                }
                if(pc.getType() == RouteType.PROVIDER){
                    pc.setProvider(dynamicProvider(postcard,pc.getProvider()));
                }
            }

            @Override
            public void onLost(Postcard postcard) {
                if(callback!=null){
                    callback.onLost(postcard);
                }
            }

            @Override
            public void onArrival(Postcard postcard) {
                if(callback!=null){
                    callback.onArrival(postcard);
                }
            }

            @Override
            public void onInterrupt(Postcard postcard) {
                if(callback!=null){
                    callback.onInterrupt(postcard);
                }
            }
        });
    }

    public static void init(Application application) {
        KContextWrap.setApp(application);
        ARouter.init(application);
        KARouteComponent.init(application);
        KDispatcher.init(application);
        ARouter.setExecutor(KDispatcher.getExecutor());
    }

}
