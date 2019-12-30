package com.zb.component.utils.thread;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class KNetExecutorService extends ThreadPoolExecutor implements IExecutorService,OnNetworkConnectivityChangeListener{
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_SECONDS = 30;
    private ConcurrentHashMap<Integer,SerialExecutor> mSerialPools = new ConcurrentHashMap<>();
    private Context mContext;
    private boolean mAdjustFollowNetwork = true;

    private static final int DEFAULT_CAPACITY = 20;

    private KNetworkBroadcastReceiver mNetworkReceiver;
    public KNetExecutorService(Context context){
        /*
         * corePoolSize 核心线程数，在队列未满的情况下的工作线程数
         * maximumPoolSize 最大线程数，在队列满了之后，会新建最多到这么多线程来延缓迟滞
         * keepAliveTime 当线程数大于corePoolSize,如果没有任务需要处理，那么线程最多等待keepAliveTime自行销毁
         * */
        super(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(DEFAULT_CAPACITY), new ZBThreadFactory());
        //PriorityBlockingQueue优先级队列，每次出队返回优先级最高的
        if(!(mContext instanceof Application)){
            mContext = context.getApplicationContext();
        }else{
            mContext = context;
        }
        mNetworkReceiver = new KNetworkBroadcastReceiver(this);
        mNetworkReceiver.register(mContext);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        mNetworkReceiver.unregister(mContext);

    }

    public void setAdjustFollowNetworkEnable(boolean enable){
        mAdjustFollowNetwork = enable;
        if(!mAdjustFollowNetwork){
            mNetworkReceiver.unregister(mContext);
        }else{
            if(!mNetworkReceiver.isRegister()){
                mNetworkReceiver.register(mContext);
            }
        }
    }


    @Override
    public void onNetworkConnectivityChange(NetworkInfo info) {
        if(mAdjustFollowNetwork){
            adjustThreadCount(info);
        }
    }

    void adjustThreadCount(NetworkInfo info) {//根据网络调整线程数量
        if (info == null || !info.isConnectedOrConnecting()) {
            return;
        }
        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_ETHERNET:
                setCorePoolSize(4);
                setMaximumPoolSize(5);
                break;
            case ConnectivityManager.TYPE_MOBILE:
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:  // 4G
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        setCorePoolSize(3);
                        setMaximumPoolSize(3);
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS: // 3G
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        setCorePoolSize(2);
                        setMaximumPoolSize(2);
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS: // 2G
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        setCorePoolSize(1);
                        setMaximumPoolSize(1);
                        break;
                    default:
                        setCorePoolSize(CORE_POOL_SIZE);
                        setMaximumPoolSize(MAXIMUM_POOL_SIZE);
                }
                break;
            default:
                setCorePoolSize(CORE_POOL_SIZE);
                setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        }
    }

    @Override
    public synchronized void execute(Runnable command) {
        super.execute(new ZBPriorityTask(command));
    }

    @Override
    public void execute(Runnable command, KTaskPriority priority) {
        execute(new KPriorityWrapRunnable(command,priority));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(task);
    }

    /**
     * 有时候会有某些任务应该按照放入顺序串行执行保证先后顺序
     * 比如设备开启预览，停止预览，这样的请求是应该串行执行的,如果并行，极可能出现异步问题
     *
     */
    public synchronized void executeSerial(int type,Runnable command){
        SerialExecutor serialExecutor = mSerialPools.get(type);
        if(serialExecutor == null){
            serialExecutor = new SerialExecutor(this);
            mSerialPools.put(type,serialExecutor);
        }
        serialExecutor.execute(command);
    }

    @Override
    public void executeSerial(int type, Runnable command, KTaskPriority priority) {
        executeSerial(type,new KPriorityWrapRunnable(command,priority));
    }


    private static class KPriorityTaskLinkerRunnable extends KPriorityRunnable implements KTaskLinker {
        private Runnable mTask;
        private static final int STATE_PADDING = 1;
        private static final int STATE_RUNNING = 2;
        private static final int STATE_FINISHED= 3;
        private static final int STATE_CANCEL= 4;

        KPriorityTaskLinkerRunnable(Runnable command){
            super( command instanceof KPriorityRunnable ? ((KPriorityRunnable)command).getPriority(): KTaskPriority.NORMAL);
            mTask = command;
            mState.set(STATE_PADDING);
        }
        private AtomicInteger mState = new AtomicInteger();
        @Override
        public void run() {
            if(mState.compareAndSet(STATE_PADDING,STATE_RUNNING)){
                mTask.run();
            }
            mState.compareAndSet(STATE_RUNNING,STATE_FINISHED);
        }

        @Override
        public boolean cancelIfNoExec() {
            return mState.compareAndSet(STATE_PADDING,STATE_CANCEL) || mState.get() == STATE_CANCEL;
        }
    }

    /**
     * 如果任务仍在队列中没有执行，需要取消任务，用以下两个接口
     * 返回的ZBTaskLinker可以用于取消任务
     */
    public synchronized KTaskLinker executeWithLinker(Runnable command){
        KPriorityTaskLinkerRunnable task = new KPriorityTaskLinkerRunnable(command);
        execute(task);
        return task;
    }

    @Override
    public KTaskLinker executeWithLinker(Runnable command, KTaskPriority priority) {
        return executeWithLinker(new KPriorityWrapRunnable(command,priority));
    }

    public synchronized KTaskLinker executeSerialWithLinker(int type, Runnable command){
        KPriorityTaskLinkerRunnable task = new KPriorityTaskLinkerRunnable(command);
        executeSerial(type,task);
        return task;
    }

    @Override
    public KTaskLinker executeSerialWithLinker(int type, Runnable command, KTaskPriority priority) {
        return executeSerialWithLinker(type,new KPriorityWrapRunnable(command,priority));
    }

    private static class SerialExecutor implements Executor {
        private Executor mInnerExcutor;
        private SerialExecutor(Executor innerExcutor){
            mInnerExcutor = innerExcutor;
        }
        final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
        Runnable mActive;

        public synchronized void execute(final Runnable r) {
            mTasks.offer(new Runnable() {
                public void run() {
                    try {
                        r.run();
                    } finally {
                        scheduleNext();
                    }
                }
            });
            if (mActive == null) {
                scheduleNext();
            }
        }

        private synchronized void scheduleNext() {
            if ((mActive = mTasks.poll()) != null) {
                mInnerExcutor.execute(mActive);
            }
        }
    }



    private static class ZBPriorityTask implements Runnable,Comparable<ZBPriorityTask>{
        private static final AtomicInteger TASK_SEQ = new AtomicInteger();
        private Runnable mTask;
        private int mSeq;
        public ZBPriorityTask(Runnable task){
            mTask = task;
            mSeq = TASK_SEQ.incrementAndGet();
        }
        @Override
        public void run() {
            mTask.run();
        }
        @Override
        public int compareTo(ZBPriorityTask other) {

            KTaskPriority mPriority = mTask instanceof KPriorityRunnable ? ((KPriorityRunnable)mTask).getPriority(): KTaskPriority.NORMAL;
            KTaskPriority oPriority = other.mTask instanceof KPriorityRunnable ? ((KPriorityRunnable)other.mTask).getPriority(): KTaskPriority.NORMAL;
            int compare = mPriority.value() == oPriority.value()?mSeq - other.mSeq :(oPriority.value() - mPriority.value());
            //默认后入队的优先级低,PriorityBlockingQueue值越小，优先级越高
//            LogUtil.e("TestPriority",">>>>>>compareTo "+compare+" M "+mPriority.value()+" O "+oPriority.value()
//                    +" mSeq:"+mSeq+" oSEQ:"+ other.mSeq);
            return compare;
        }
    }

    private static class ZBThreadFactory implements ThreadFactory{
        @Override
        public Thread newThread(Runnable r) {
            return new ZBThread(r);
        }
    }
    private static class ZBThread extends Thread {
        private static final AtomicInteger THREAD_COUNT = new AtomicInteger();
        public ZBThread(Runnable r) {
            super(r);
            setName("ZBThread-"+THREAD_COUNT.incrementAndGet());
        }

        @Override public void run() {
            //Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
            super.run();
            THREAD_COUNT.decrementAndGet();
        }
    }
}
