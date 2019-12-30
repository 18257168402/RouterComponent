package com.zb.component.utils.thread;


/**
 * 投入线程池的任务可以继承ZBPriorityRunnable来提供一个优先级
 *
 * 有些任务比较紧急，需要提高优先级
 * 有些任务则不那么紧急，则需要降低优先级，比如后台定时轮询一下设备列表的在线状态
 *
 */
public abstract class KPriorityRunnable implements Runnable{
    private KTaskPriority mPriority;
    public KPriorityRunnable(KTaskPriority priority){
        mPriority = priority;
    }
    public KTaskPriority getPriority(){
        return mPriority;
    }
}
