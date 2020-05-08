 package com.pineapple.valentine;
 
 import android.graphics.Canvas;
 import android.util.Log;
 import android.view.SurfaceHolder;
 
 import com.pineapple.valentine.GamePanel;
 
 public class MainThread extends Thread{
 
 	public final static String TAG = MainThread.class.getSimpleName();
 	private SurfaceHolder surfaceHolder;
 	private GamePanel gamePanel;
 	private MenuPanel menuPanel;
 	private boolean running;
 	private long startTime, endTime, overTime;
 	public final static int updateInterval = 40; //Milliseconds per frame
 	
 	public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel){
 		super();
 		this.surfaceHolder = surfaceHolder;
 		this.gamePanel = gamePanel;
 	}
 	
 	public MainThread(SurfaceHolder surfaceHolder, MenuPanel menuPanel){
 		super();
 		this.surfaceHolder = surfaceHolder;
 		this.menuPanel = menuPanel;
 	}
 	
 	public void setRunning(boolean flag){
 		this.running = flag;
 	}
 	
 	@Override
 	public void run(){
 		Canvas canvas;
 		Log.d(TAG, "Starting game loop");
 		if(!(gamePanel == null)){
 			while (running) {
 				startTime = System.currentTimeMillis();
 				gamePanel.update();
 				canvas = null;
 				try {
 					canvas = this.surfaceHolder.lockCanvas();
 					synchronized (surfaceHolder) {
 						this.gamePanel.render(canvas);
 					}
				} catch(NullPointerException e){}
				finally {
 					if (canvas != null) {
 						surfaceHolder.unlockCanvasAndPost(canvas);
 					}
 				} 
 				endTime = System.currentTimeMillis();
 				if(endTime - startTime <= updateInterval){
 					try {
 						Thread.sleep(updateInterval + startTime - endTime);
 					} catch(InterruptedException e){}
 				} else { //If the rendering took too long, we try to catch up by only updating the game state
 					//Log.d(TAG, "Overtime!");
 					overTime = endTime - startTime - updateInterval;
 					while(overTime >= 0){
 						startTime = System.currentTimeMillis();
 						gamePanel.update();
 						endTime = System.currentTimeMillis();
 						if(endTime - startTime <= updateInterval){
 							try {
 								Thread.sleep(updateInterval + startTime - endTime);
 							} catch(InterruptedException e){};
 						}
 						overTime -= updateInterval + startTime - endTime;
 					}
 				}
 			}
 		} else {
 			while (running) {
 				startTime = System.currentTimeMillis();
 				menuPanel.update();
 				canvas = null;
 				try {
 					canvas = this.surfaceHolder.lockCanvas();
 					
 					synchronized (surfaceHolder) {
 						this.menuPanel.render(canvas);
 					}
 					
				} catch(NullPointerException e){}
				finally {
 					if (canvas != null) {
 						surfaceHolder.unlockCanvasAndPost(canvas);
 					}
 				} 
 				endTime = System.currentTimeMillis();
 				try {
 					if(startTime - endTime > -updateInterval){
 						Thread.sleep(updateInterval + startTime - endTime);
 					}
 				} catch(InterruptedException e){}
 			}
 		}
 	}
 }
 
 
