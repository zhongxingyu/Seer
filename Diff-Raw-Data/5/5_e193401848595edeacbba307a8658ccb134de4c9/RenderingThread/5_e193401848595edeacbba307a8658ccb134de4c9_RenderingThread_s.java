 package com.infectosaurus;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import com.infectosaurus.map.Level;
 import com.infectosaurus.map.TileType;
 import com.infectosaurus.tools.FixedSizeArray;
 
 import android.opengl.GLU;
 import android.util.Log;
 import android.view.Display;
 import android.view.WindowManager;
 
 
 public class RenderingThread implements Panel.Renderer {
     
     private ObjectHandler drawQueue;
 	private Object drawLock;
 	private boolean drawQueueChanged;
     
  
     public RenderingThread() {
     	Log.d(Main.TAG,"In RThread");
     	drawLock = new Object();
     }
 
 	public void onDrawFrame(GL10 gl) { 
 		OpenGLSystem.gl = gl;
 		//Avoid drawing same scene twice
 		synchronized(drawLock) {
             if (!drawQueueChanged) {
                 while (!drawQueueChanged) {
                     try {
                         drawLock.wait();
                     } catch (InterruptedException e) {
                         // No big deal if this wait is interrupted.
                     }
                 }
             }
             drawQueueChanged = false;
         }
 		
 		synchronized (this) {
 			int mWidth = BaseObject.gamePointers.panel.getWidth();
 			int mHeight= BaseObject.gamePointers.panel.getHeight();
 			
 			//Get current camera state, to avoid different 
 			//camera throughout drawing
 			//TODO We pretty much copy the whole camera.
 			// Different solution? Not static?
 			int cameraX = Camera.pos.x;
 			int cameraY = Camera.pos.y;
 			int cameraHeight = Camera.screenHeight;
 			int cameraWidth = Camera.screenWidth;
 			float scale = Camera.scale;
 			
 			
 			DrawableBitmap.beginDrawing(gl, mWidth, mHeight);
 			
 			//Draw tiles
 			Level level = BaseObject.gamePointers.level;
 			TileType[][] bgTiles = level.renderQueue;
 			
 			if(bgTiles != null && bgTiles.length > 0){		
 				//TODO Do way more efficient!
 				for (int i = 0; i < bgTiles.length; i++) {
 					for (int j = 0; j < bgTiles[i].length; j++) {
 						
 						int x = Level.TILE_SIZE*i;
 						int y = Level.TILE_SIZE*j;
 						//Check if element is outside the screen view
 				        if(x + Level.TILE_SIZE < cameraX) continue;
				    	if(x > cameraX + cameraWidth) continue;
 				        if(y + Level.TILE_SIZE < cameraY) continue;
				    	if(y > cameraY + cameraHeight) continue;
 				    	
 						bgTiles[i][j].draw(gl, x - cameraX
 								, y - cameraY, scale, scale);
 					}
 				}
 			}
 			
 			
 			if (drawQueue != null && drawQueue.getObjects().getCount() > 0 ){
 				FixedSizeArray<RenderElement> objects = drawQueue.getObjects();
 				final int count = objects.getCount();
 				Object[] elems = objects.getArray();
 				objects.sort(true);
 
 				for (int i = 0; i < count; i++){	
 					RenderElement elem = (RenderElement)elems[i];
 					
 					if(elems[i] == null){ 
 						Log.d(Main.TAG, "elem in drawBGQueue is " + elem + 
 								"Last count was " + count + " Now it is "+ objects.getCount());
 						continue;
 					}
 					elem.drawable.draw(gl, 
 									   elem.x - cameraX, 
 									   elem.y - cameraY, 
 									   scale, 
 									   scale);
 				}
 			}
 			DrawableBitmap.endDrawing(gl);
 		}
 	}
 
 
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		gl.glViewport(0, 0, width, height);
         
         /*
          * Set our projection matrix. This doesn't have to be done each time we
          * draw, but usually a new projection needs to be set when the viewport
          * is resized.
          */
         gl.glMatrixMode(GL10.GL_PROJECTION);
         gl.glLoadIdentity();
         float ratio = (float)width/height;
         gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
 	}
 
 
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		
 		 /*
          * Some one-time OpenGL initialization can be made here probably based
          * on features of this particular context
          */
         gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
 
         gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
         gl.glShadeModel(GL10.GL_FLAT);
         gl.glDisable(GL10.GL_DEPTH_TEST);
         gl.glEnable(GL10.GL_TEXTURE_2D);
         /*
          * By default, OpenGL enables features that improve quality but reduce
          * performance. One might want to tweak that especially on software
          * renderer.
          */
         gl.glDisable(GL10.GL_DITHER);
         gl.glDisable(GL10.GL_LIGHTING);
 
         gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
 
         gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 	}
 
 	public synchronized void setDrawQueue(ObjectHandler drawQueue) {
 		this.drawQueue = drawQueue;
 		synchronized(drawLock) {
             drawQueueChanged = true;
             drawLock.notify();
 		}
 	}
 	
 	/**
      * This function blocks while drawFrame() is in progress, and may be used by other threads to
      * determine when drawing is occurring.
      */
 
 	public synchronized void waitDrawingComplete() {
 		
 	}
 }
