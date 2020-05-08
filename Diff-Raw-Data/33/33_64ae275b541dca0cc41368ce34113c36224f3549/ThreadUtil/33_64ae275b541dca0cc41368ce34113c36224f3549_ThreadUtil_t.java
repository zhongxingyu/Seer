 package com.secondhand.view.scene;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 
 public final class ThreadUtil {
	private static ThreadUtil instance = new ThreadUtil();
	
	private Engine engine;
 	
 	private ThreadUtil() {}
 	
	public static ThreadUtil getInstance() {
		return ThreadUtil.instance;
	}
	
	public void  initialize(final Engine engine) {
		this.engine = engine;
	}
	
	public void runOnUpdateThread(final Method method) {
 		engine.registerUpdateHandler(new IUpdateHandler() {
 			@Override
 			public void onUpdate(final float pSecondsElapsed) {
 				engine.unregisterUpdateHandler(this);
 				engine.runOnUpdateThread(new Runnable() {
 					@Override
 					public void run() {
 						
 						method.method();
 					}
 				});
 			}
 
 			@Override
 			public void reset() {
 			}
 		});
 	}
 
 	public static abstract class Method{
 		public abstract void method();
 	}
 }
