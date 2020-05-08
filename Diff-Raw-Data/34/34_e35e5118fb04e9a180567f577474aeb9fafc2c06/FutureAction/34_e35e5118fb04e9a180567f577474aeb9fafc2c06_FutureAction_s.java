 package com.project.base;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 public abstract class FutureAction {
	private TimerTask task;
 	private Timer timer;
	private boolean waiting = false;
 
 	public FutureAction() {
 	}
 
 	public void startOrRestartCountdown(int activationTimeInMilliseconds) {
 		synchronized (this) {
			if (waiting) {
				task.cancel();
 				timer.cancel();
 			}
 
			task = new TimerTask() {
 				@Override
 				public boolean cancel() {
					waiting = false;
 					actionCancelled();
 					return super.cancel();
 				}
 
 				@Override
 				public void run() {
					if (task == this)
						performAction();
					waiting = false;
 				}
 			};
 
 			boolean isDaemon = true;
 			timer = new Timer(isDaemon);
 			timer.schedule(task, activationTimeInMilliseconds);
			waiting = true;
 		}
 	}
 
	// for the client to cancel any pending actions...
 	public void cancel() {
		timer.cancel();
		task.cancel();
		timer = null;
		task = null;
		waiting = false;
 	}
 
 	// will be called when client should act upon the scheduled action
 	public abstract void performAction();
 
 	// will be called to tell the client the scheduled action has been cancelled
 	public abstract void actionCancelled();
 }
