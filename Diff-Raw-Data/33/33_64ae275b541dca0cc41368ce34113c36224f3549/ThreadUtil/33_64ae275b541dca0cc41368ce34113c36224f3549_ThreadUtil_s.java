 package com.secondhand.view.scene;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 
import com.secondhand.controller.MainActivity;

 public final class ThreadUtil {
 	
 	private ThreadUtil() {}
 	
	public static void runOnUpdateThread(final Method method) {
		final Engine engine = MainActivity.engine;
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
