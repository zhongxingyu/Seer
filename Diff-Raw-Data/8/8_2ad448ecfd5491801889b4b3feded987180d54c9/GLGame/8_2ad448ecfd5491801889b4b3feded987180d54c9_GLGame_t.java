 package org.racenet.framework;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.app.Activity;
 import android.content.Context;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLSurfaceView.Renderer;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.view.Window;
 import android.view.WindowManager;
 
 /**
  * Android Activity which represents an openGL ES game
  * 
  * @author soh#zolex
  *
  */
 public abstract class GLGame extends Activity implements Renderer {
 	
 	/**
 	 * Internal states of the game
 	 */
     public enum GLGameState {
         Initialized,
         Running,
         Paused,
         Finished,
         Idle
     }
     
     public GLSurfaceView glView;
     Screen screen;
     GLGameState state = GLGameState.Initialized;
     Object stateChanged = new Object();
     long startTime = System.nanoTime();
     WakeLock wakeLock;
     float deltaRest = 0;
     
     @Override
     /**
      * When the activity is being created
      * create instances of all openGL ES dependencies
      */
     public void onCreate(Bundle savedInstanceState) {
     	
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         glView = new GLSurfaceView(this);
         glView.setRenderer(this);
         setContentView(glView);
         
         FileIO.setupInstance(getAssets());
         Audio.setupInstance(this);
         
         PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "racesow");
         wakeLock.acquire();
     }
     
     /**
      * Acquire wakelock on resume
      */
     public void onResume() {
     	
         super.onResume();
         glView.onResume();
         wakeLock.acquire();
     }
     
     /**
      * Get the startScreen for the game when
      * the openGL surface is being created
      * 
      * @param GL10 unused
      * @param EGLConfig config
      */
     public void onSurfaceCreated(GL10 unused, EGLConfig config) {   
     	
         synchronized (stateChanged) {
         	
             if(state == GLGameState.Initialized) {
             	
                 screen = getStartScreen();
             }
             
             state = GLGameState.Running;
             screen.resume();
             startTime = System.nanoTime();
         }        
     }
     
     /**
      * Must be implemented by a derivative game
      * 
      * @return Screen
      */
     public abstract Screen getStartScreen();
     
     /**
      * Nothing to do on surfaceChanged
      * 
      * @param GL10 unused
      * @param int width
      * @param int height
      */
     public void onSurfaceChanged(GL10 unused, int width, int height) {   
     	
     }
     
     /**
      * onDrawframe is being called by openGL and updates
      * the world and draws it accoring to the internal gamestate
      * 
      * @param GL10 unused
      */
     public void onDrawFrame(GL10 unused) {     
     	
         GLGameState state = null;
         
         // listen for a state change
         synchronized (stateChanged) {
         	
             state = this.state;
         }
         
         // when the game is running update the world and draw it
         if (state == GLGameState.Running) {
         	
             float deltaTime = (System.nanoTime()-startTime) / 1000000000.0f;
             startTime = System.nanoTime();
           	screen.update(deltaTime);
             screen.present(deltaTime);
         }
         
         // when the game was paused, set the new state to idle
         // ie. when the home button is pressed and the game is minimized
         if (state == GLGameState.Paused) {
         	
             screen.pause();            
             synchronized (stateChanged) {
             	
                 this.state = GLGameState.Idle;
                 stateChanged.notifyAll();
             }
         }
         
         // When the game is closed get rid of all loaded content
         if (state == GLGameState.Finished) {
             screen.pause();
             screen.dispose();
             synchronized(stateChanged) {
                 this.state = GLGameState.Idle;
                 stateChanged.notifyAll();
             }            
         }
     }   
     
     @Override 
     /**
      * Called by opengl when the game is minimized
      */
     public void onPause() {
     	
         synchronized(stateChanged) {
         	
             if (isFinishing()) {
             	
             	state = GLGameState.Finished;
             	
             } else {
             	
             	state = GLGameState.Paused;
             }
             
             while(true) {
             	
                 try {
                 	
                     stateChanged.wait();
                     break;
                     
                 } catch(InterruptedException e) {}
             }
         }
         
         wakeLock.release();
         glView.onPause();  
         super.onPause();
     }    
 
     /**
      * Set the current game screen
      * 
      * @param Screen screen
      */
     public void setScreen(Screen screen) {
     	
         if (screen == null) {
         	
         	throw new IllegalArgumentException("Screen must not be null");
         }
 
         this.screen.pause();
         this.screen.dispose();
         this.screen = screen;
     }
 
     /**
      * Get the current screen
      * 
      * @return Screen
      */
     public Screen getCurrentScreen() {
     	
         return screen;
     }
     
     /**
      * Get the screen width
      * 
      * @return float
      */
     public int getScreenWidth() {
     	
     	return getWindowManager().getDefaultDisplay().getWidth();
     }
     
     /**
      * Get the screen height
      * 
      * @return float
      */
     public int getScreenHeight() {
     	
     	return getWindowManager().getDefaultDisplay().getHeight();
     }
 }
