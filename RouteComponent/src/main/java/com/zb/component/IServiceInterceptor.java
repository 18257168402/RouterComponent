package com.zb.component;

public interface IServiceInterceptor {//服务拦截器不跨进程，只拦截本进程的服务
    KInvokeResult beforeInvoke(KPostcard postcard,String methodName,Object[] args);
    KInvokeResult afterInvoke(KPostcard postcard,String methodName,Object result,Object[] args);
}
