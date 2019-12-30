package com.zb.component.utils.thread;

public interface KTaskLinker {
    /**
     * 取消任务，如果任务已经在执行中，返回false，如果已经执行完毕或者已经取消返回true
     */
    boolean cancelIfNoExec();
}
