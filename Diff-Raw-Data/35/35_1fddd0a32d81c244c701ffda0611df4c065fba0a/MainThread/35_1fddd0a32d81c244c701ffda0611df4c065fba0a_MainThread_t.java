 package com.relurori.breakout;
 
 import android.app.AlertDialog;
 import android.graphics.Canvas;
 import android.util.Log;
 import android.view.SurfaceHolder;
 
 public class MainThread extends Thread {
 
 	private static final String TAG = MainThread.class.getSimpleName();
 	/** running state */
 	private boolean running;
 	
 	/** surface holder for the main game panel is needed because we
 	 * need to lock the surface when we draw*/
 	private SurfaceHolder surfaceHolder;
 	
 	/** main game panel */
 	private MainPanel panel;
 	
 	private final static int MAX_FPS = 40;
 	private final static int MAX_FRAME_SKIPS = 5;
 	private final static int FRAME_PERIOD = 1000 / MAX_FPS;
 	
 	final int STATE_INPROGRESS = 0;
 	final int STATE_EXIT = 1;
 	final int STATE_RETRY = 2;
 	
 	public MainThread(SurfaceHolder surfaceHolder, MainPanel panel) {
 		super();
 		this.surfaceHolder= surfaceHolder;  
 		this.panel = panel;
 	}
 	
 	/** change running state */
 	public void setRunning(boolean running) {
 		this.running = running;
 	}
 	
 	public SurfaceHolder getSurfaceHolder() {
 		return surfaceHolder;
 	}
 	
 	/** game loop */
 	@Override
 	public void run() {
 		Canvas c = null;
 		long ticks = 0L;
 		long startTime;
 		long elapsedTime;
 		/** if < 0, we're too slow */
 		int sleepTime;
 		/** number of frame renderings skipped if we fall behind */
 		int framesSkipped;
 		
 		sleepTime = 0;
 		
 		Log.d(TAG, "Starting game loop");
 		while (running) {
 			try {
 				c = surfaceHolder.lockCanvas();
				
				if (c == null) {
					Log.d(TAG,"Breaking run loop");
					break;
				}
				
 				synchronized(surfaceHolder) {
 					
 					startTime = System.currentTimeMillis();
 					framesSkipped = 0;
 					
 					panel.updatePhysics();
 					panel.draw(c);
 					
 					if (panel.getPauseStatus() == true) {
 						while (panel.getRunState() == STATE_INPROGRESS) {}
 						switch(panel.getRunState()) {
 						case STATE_EXIT:
 							running = false;
 							break;
 						case STATE_RETRY:
 							running = true;
 							panel.setRunState(STATE_INPROGRESS);
 							break;
 						}
 					}
 					
 					elapsedTime = System.currentTimeMillis() - startTime;
 					sleepTime = (int)(FRAME_PERIOD - elapsedTime);
 					
 					if (sleepTime > 0) {
 						try {
 							/** we completed too fast; wait */
 							Thread.sleep(sleepTime);
 						} catch (InterruptedException e) {}
 					}
 					
 					while (elapsedTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
 						/** too slow; let's update only physics. don't render */
 						panel.updatePhysics();
 						elapsedTime += FRAME_PERIOD;
 						framesSkipped++;
 					}
 
 				}
 			} finally {
 				if (c != null) {
 					surfaceHolder.unlockCanvasAndPost(c);
 				}
 			}
 			ticks++;
 		}
 		
 		Log.d(TAG, "Game loop ran " + ticks + "times");
 	}
 
 }
