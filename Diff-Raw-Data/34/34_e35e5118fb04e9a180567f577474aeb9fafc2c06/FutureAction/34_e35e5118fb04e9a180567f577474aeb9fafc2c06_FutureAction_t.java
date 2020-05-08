 package com.project.base;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 public abstract class FutureAction {
 	private Timer timer;
 
 	public FutureAction() {
 	}
 
 	public void startOrRestartCountdown(int activationTimeInMilliseconds) {
 		synchronized (this) {
			if (timer != null) {
 				timer.cancel();
				timer = null;
 			}
 
			TimerTask task = new TimerTask() {
 				@Override
 				public boolean cancel() {
 					actionCancelled();
 					return super.cancel();
 				}
 
 				@Override
 				public void run() {
					performAction();
 				}
 			};
 
 			boolean isDaemon = true;
 			timer = new Timer(isDaemon);
 			timer.schedule(task, activationTimeInMilliseconds);
 		}
 	}
 
	// for the client to cancel any pending actions... (but does not try to cancel any tasks that may have already started)
 	public void cancel() {
		synchronized (this) {
			timer.cancel();
			timer = null;
		}
 	}
 
 	// will be called when client should act upon the scheduled action
 	public abstract void performAction();
 
 	// will be called to tell the client the scheduled action has been cancelled
 	public abstract void actionCancelled();
 }
