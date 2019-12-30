package com.zb.component;

public class KInvokeResult {
    public Object result;
    public boolean isContinue;
    public boolean isReturn;
    private KInvokeResult(Object result,boolean isContinue,boolean isReturn){
        this.result = result;
        this.isContinue = isContinue;
        this.isReturn = isReturn;
    }
    public static KInvokeResult onReturn(Object object){//立即返回，并且返回值为object
        return new KInvokeResult(object,false,true);
    }
    public static KInvokeResult onContinue(){//继续调用流程
        return new KInvokeResult(null,true,false);
    }
}
