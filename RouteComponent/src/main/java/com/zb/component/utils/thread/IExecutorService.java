package com.zb.component.utils.thread;

import java.util.concurrent.ExecutorService;

public interface IExecutorService extends ExecutorService {
     void setAdjustFollowNetworkEnable(boolean enable);
     void execute(Runnable command);
     void execute(Runnable command, KTaskPriority priority);
     void executeSerial(int type, Runnable command);
     void executeSerial(int type, Runnable command, KTaskPriority priority);
     KTaskLinker executeWithLinker(Runnable command);
     KTaskLinker executeWithLinker(Runnable command, KTaskPriority priority);
     KTaskLinker executeSerialWithLinker(int type, Runnable command);
     KTaskLinker executeSerialWithLinker(int type, Runnable command, KTaskPriority priority);
}
