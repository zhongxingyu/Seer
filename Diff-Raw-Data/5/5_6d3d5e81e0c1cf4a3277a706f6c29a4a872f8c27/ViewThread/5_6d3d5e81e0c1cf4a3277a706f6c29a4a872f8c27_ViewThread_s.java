 package rpisdd.rpgme.gamelogic.dungeon.viewcontrol;
 
 import android.graphics.Canvas;
 import android.util.Log;
 import android.view.SurfaceHolder;
 
 public class ViewThread extends Thread {
 
 	private final SurfaceHolder surfaceHolder;
 	private final ThreadedSurfaceView gamePanel;
 	private boolean running;
 	private boolean paused;
 
 	public void setRunning(boolean running) {
 		this.running = running;
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public boolean isPaused() {
 		return paused;
 	}
 
 	public ViewThread(SurfaceHolder surfaceHolder, ThreadedSurfaceView gamePanel) {
 		super();
 		this.surfaceHolder = surfaceHolder;
 		this.gamePanel = gamePanel;
 	}
 
 	// desired fps
 	private final static int MAX_FPS = 60;
 	// maximum number of frames to be skipped
 	private final static int MAX_FRAME_SKIPS = 5;
 	// the frame period
 	private final static int FRAME_PERIOD = 1000 / MAX_FPS;
 
 	// Returns the time in seconds it took to complete the last frame.
 	public float deltaTime() {
 		float dt = (frameTime) / 1000f;
 		// System.out.format("Time: %f%n", dt);
 		return dt;
 	}
 
 	long beginTime; // the time when the cycle begun
 	long timeDiff; // the time it took for the cycle to execute
 	int sleepTime; // ms to sleep (<0 if we're behind)
 	int framesSkipped; // number of frames being skipped
 
 	long frameTime;
 
 	@Override
 	public void run() {
 		Canvas canvas;
 		// Log.d(TAG, "Starting game loop");
 
 		sleepTime = 0;
 
 		try {
 			// send the thread to sleep for a short period
 			// very useful for battery saving
 			Thread.sleep(400);
 		} catch (InterruptedException e) {
 			Log.d("ViewThread", "Interrupted.", e);
 		}
 
 		while (running) {
 
 			long startTime = System.currentTimeMillis();
 			canvas = null;
 			// try locking the canvas for exclusive pixel editing
 			// in the surface
 			try {
 
 				canvas = this.surfaceHolder.lockCanvas();
 
 				if (canvas == null) {
 					return;
 				}
 
 				synchronized (surfaceHolder) {
 					beginTime = System.currentTimeMillis();
 					framesSkipped = 0; // resetting the frames skipped
 					// update game state
 					this.gamePanel.update();
 					// render state to the screen
 					// draws the canvas on the panel
 					if (canvas != null) {
 						this.gamePanel.render(canvas);
 					}
 
 					// calculate how long did the cycle take
 					timeDiff = System.currentTimeMillis() - beginTime;
 					// calculate sleep time
 					sleepTime = (int) (FRAME_PERIOD - timeDiff);
 
 					if (sleepTime > 0) {
 						// if sleepTime > 0 we're OK
 						try {
 							// send the thread to sleep for a short period
 							// very useful for battery saving
 							Thread.sleep(sleepTime);
 						} catch (InterruptedException e) {
 							Log.d("ViewThread", "Interrupted.", e);
 						}
 					}
 
 					while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
 						// we need to catch up
 						// update without rendering
 						this.gamePanel.update();
 						// add frame period to check if in next frame
 						sleepTime += FRAME_PERIOD;
 						framesSkipped++;
 					}
 				}
 			} finally {
 				// in case of an exception the surface is not left in
 				// an inconsistent state
 				if (canvas != null) {
 					surfaceHolder.unlockCanvasAndPost(canvas);
 				}
 			} // end finally
 			frameTime = System.currentTimeMillis() - startTime;
 
 		}
 
 	}
 }
