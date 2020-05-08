 package com.heavyplayer.util;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 public class ElasticThreadPoolExecutor extends ThreadPoolExecutor {
 	private int mStartCorePoolSize;
 	private Object elasticityLock = new Object(); // Used to avoid decrementing too much
 
 	public ElasticThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
 			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
 		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
 		mStartCorePoolSize = corePoolSize;
 	}
 	public ElasticThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
 			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
 		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
 		mStartCorePoolSize = corePoolSize;
 	}
 	public ElasticThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
 			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
 		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
 				threadFactory);
 		mStartCorePoolSize = corePoolSize;
 	}
 	public ElasticThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
 			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
 			RejectedExecutionHandler handler) {
 		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
 				threadFactory, handler);
 		mStartCorePoolSize = corePoolSize;
 	}
 
 	@Override
 	public void execute(Runnable command) {
 		synchronized(elasticityLock) {
			int count = getActiveCount() + getQueue().size();
			if(count >= getCorePoolSize() && getCorePoolSize() < getMaximumPoolSize()) {        
 				setCorePoolSize(getCorePoolSize() + 1);
 			}
 		}
 		super.execute(command);
 	}
 
 	@Override
 	protected void afterExecute(Runnable r, Throwable t) {
 		synchronized(elasticityLock) {
 			if(getQueue().isEmpty() && getCorePoolSize() > mStartCorePoolSize) {
 				setCorePoolSize(getCorePoolSize() - 1);
 			}
 		}
 	};
 }
