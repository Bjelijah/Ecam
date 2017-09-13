package com.howell.utils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;			

/**
 * Created by Administrator on 2017/7/14.
 */

public class ThreadUtil {
    private static final int POOL_MAX = 5;
    public static ExecutorService sCachedThreadPool=null;
    public static ExecutorService sFixedThreadPool=null;
    public static ScheduledExecutorService sScheduledThread=null;
    public static ScheduledExecutorService sScheduledSingleThread=null;
    public static ScheduledExecutorService sScheduledSingleThreadForDaemon=null;
    public static ExecutorService sSingleThread = null;
    /**
     * 大量短期小任务
     * @param r
     */
    public static void cachedThreadStart(Runnable r){
        if (sCachedThreadPool==null){
            sCachedThreadPool = Executors.newCachedThreadPool();
        }
        sCachedThreadPool.execute(r);
    }

    public static void cachedThreadShutDown(){
        if (sCachedThreadPool==null)return;
        sCachedThreadPool.shutdown();
        try {
            if (!sCachedThreadPool.awaitTermination(60, TimeUnit.SECONDS)){
                sCachedThreadPool.shutdownNow();
                if (!sCachedThreadPool.awaitTermination(60, TimeUnit.SECONDS)){
                    System.err.println("cachedThreadShutDown error!!");
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sCachedThreadPool = null;
    }

    public static void fixThreadStart(Runnable r,  Integer nThreads){
        if (sFixedThreadPool==null){
            sFixedThreadPool = Executors.newFixedThreadPool(nThreads==null?POOL_MAX:nThreads);
        }
        sFixedThreadPool.execute(r);
    }

    public static void fixThreadShutDown(){
        if (sFixedThreadPool==null)return;
        sFixedThreadPool.shutdown();
        try {
            if (!sFixedThreadPool.awaitTermination(60, TimeUnit.SECONDS)){
                sFixedThreadPool.shutdownNow();
                if (!sFixedThreadPool.awaitTermination(60, TimeUnit.SECONDS)){
                    System.err.println("cachedThreadShutDown error!!");
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sFixedThreadPool = null;
    }

    public static ScheduledExecutorService createNewScheduledThread(){
        return Executors.newScheduledThreadPool(POOL_MAX);
    }

    public static void scheduledThreadStart(ScheduledExecutorService s,Runnable r,long delay,long period,TimeUnit t){
        s.scheduleAtFixedRate(r,delay,period,t);
    }

    public static void scheduledThreadStart(Runnable r, long delay, long period, TimeUnit t){
        if (sScheduledThread==null){
            sScheduledThread = Executors.newScheduledThreadPool(POOL_MAX);
        }
        sScheduledThread.scheduleAtFixedRate(r,delay,period,t);
    }
    public static void scheduledThreadStart(Runnable r, long delay, TimeUnit t){
        if (sScheduledThread==null){
            sScheduledThread = Executors.newScheduledThreadPool(POOL_MAX);
        }
        sScheduledThread.schedule(r,delay,t);
    }
    
    public static void scheduledThreadForDaemonStart(Runnable r, long delay, TimeUnit t){
        if (sScheduledSingleThreadForDaemon==null){
        	sScheduledSingleThreadForDaemon = Executors.newSingleThreadScheduledExecutor();
        
        	sScheduledSingleThreadForDaemon.scheduleAtFixedRate(r, delay, delay, t);
        }
        
    }
    
    public static void scheduledThreadForDaemonShutDown(){
        if (sScheduledSingleThreadForDaemon==null)return;
        sScheduledSingleThreadForDaemon.shutdown();
        if(!sScheduledSingleThreadForDaemon.isShutdown()){
        	sScheduledSingleThreadForDaemon.shutdownNow();
        }
        sScheduledSingleThreadForDaemon = null;
    }
    
    
    public static void scheduledThreadShutDown(){
        if (sScheduledThread==null)return;
        sScheduledThread.shutdown();
        sScheduledThread = null;
    }

    public static void scheduledSingleThreadStart(Runnable r,long delay,long period,TimeUnit t){
        if (sScheduledSingleThread == null){
            sScheduledSingleThread = Executors.newSingleThreadScheduledExecutor();
        }
        sScheduledSingleThread.scheduleAtFixedRate(r,delay,period,t);
    }

    public static void scheduledSingleThreadShutDown(){
        if (sScheduledSingleThread==null)return;
        sScheduledSingleThread.shutdown();
        if (!sScheduledSingleThread.isShutdown()) {
			sScheduledSingleThread.shutdownNow();
		}
        sScheduledSingleThread=null;
    }

    public static void scheduledSingleThreadStart(Runnable r,long delay,TimeUnit t){
        if (sScheduledSingleThread == null){
            sScheduledSingleThread =  Executors.newSingleThreadScheduledExecutor();
        }
        sScheduledSingleThread.schedule(r,delay,t);
    }
    
    public static void singleThreadStart(Runnable r){
        if (sSingleThread==null){
            sSingleThread = Executors.newSingleThreadExecutor();
        }
        sSingleThread.execute(r);
    }
    public static void singleTHreadShutDown(){
        if (sSingleThread==null)return;
        sSingleThread.shutdown();
        sSingleThread = null;
    }
}
