package com.zb.component.utils.thread;

/**
 * 任务优先级，默认只有低中高，也可以自行定义
 */
public class KTaskPriority {

    public static final KTaskPriority LOW = new KTaskPriority(1000);
    public static final KTaskPriority NORMAL = new KTaskPriority(2000);
    public static final KTaskPriority HIGHT = new KTaskPriority(3000);
    private int mValue;
    KTaskPriority(int value){
        this.mValue = value;
    }
    public int value(){
        return mValue;
    }
}
