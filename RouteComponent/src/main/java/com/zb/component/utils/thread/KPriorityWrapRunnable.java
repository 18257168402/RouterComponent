package com.zb.component.utils.thread;

public class KPriorityWrapRunnable extends KPriorityRunnable {

    private Runnable mTask;
    public KPriorityWrapRunnable(Runnable runnable, KTaskPriority priority) {
        super(priority);
        mTask = runnable;
    }

    @Override
    public void run() {
        if(mTask!=null){
            mTask.run();
        }
    }
}
