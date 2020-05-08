 package org.racenet.framework;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.opengl.GLES10;
 
 /**
  * A camera which represents the two dimensional
  * view area of an openGL ES application
  * 
  * @author soh#zolex
  *
  */
 public class Camera2 implements Drawable {
 	
     public final Vector2 position;
     public float zoom;
     public final float frustumWidth;
     public final float frustumHeight;
     protected List<HudItem> hudItems = new ArrayList<HudItem>();
     
     /**
      * Constructor
      * 
      * @param GLGraphics glGraphics
      * @param float frustumWidth
      * @param float frustumHeight
      */
     public Camera2(float frustumWidth, float frustumHeight) {
     	
         this.frustumWidth = frustumWidth;
         this.frustumHeight = frustumHeight;
         this.position = new Vector2(frustumWidth / 2, frustumHeight / 2);
         this.zoom = 1.0f;
     }
 
     /**
      * Set the camera to a new position and
      * care for all HUD items to move with the camera
      * 
      * @param float x
      * @param float y
      */
 	public void setPosition(float x, float y) {
 		
 		this.position.x = x;
 		this.position.y = y;
 		
 		for (int i = 0; i < hudItems.size(); i++) {
 			
 			HudItem item = hudItems.get(i);
			if (item != null) {
			
				item.vertices[0].x = x + item.cameraX;
				item.vertices[0].y = y + item.cameraY;
			}
 		}
 	}
     
 	/**
 	 * Zoom the camera view and care for all
 	 * HUD items to be realigned
 	 * 
 	 * @param float factor
 	 */
 	public void setZoom(float factor) {
 		
 		this.zoom = factor;
 		
 		for (int i = 0; i < hudItems.size(); i++) {
 			
 			HudItem item = hudItems.get(i);
 			item.vertices[0].x = item.cameraX * factor;
 			item.vertices[0].y = item.cameraY * factor;
 		}
 	}
 	
 	/**
 	 * Add a HUD item to the camrea
 	 * 
 	 * @param HudItem item
 	 */
 	public void addHud(HudItem item) {
 		
 		this.hudItems.add(item);
 	}
 	
 	/**
 	 * Remove a HudItem from the camera
 	 * 
 	 * @param HudItem item
 	 */
     public void removeHud(HudItem item) {
     	
     	if (this.hudItems.contains(item)) {
     		
     		this.hudItems.remove(item);
     	}
     }
     
     /**
      * Setup openGL ES internals
      */
     public void setViewportAndMatrices(int width, int height) {
     	
         GLES10.glViewport(0, 0, width, height);
         GLES10.glMatrixMode(GLES10.GL_PROJECTION);
         GLES10.glLoadIdentity();
         GLES10.glOrthof(
         	position.x - frustumWidth * zoom / 2, 
 	        position.x + frustumWidth * zoom / 2, 
 	        position.y - frustumHeight * zoom / 2, 
 	        position.y + frustumHeight * zoom / 2, 
 	        1, -1);
         GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
         GLES10.glLoadIdentity();
         GLES10.glHint(GLES10.GL_PERSPECTIVE_CORRECTION_HINT, GLES10.GL_FASTEST);
     }
     
     /**
      * Draw all HUD items attached to the camera
      */
     public void draw() {
     	
 		for (int i = 0; i < hudItems.size(); i++) {
 			
 			hudItems.get(i).draw();
 		}
     }
     
     /**
      * Reload the HUD textures
      */
     public void reloadTexture() {
     	
     	for (int i = 0; i < hudItems.size(); i++) {
     		
     		this.hudItems.get(i).reloadTexture();
     	}
     }
     
     /**
      * Get rid of the HUD textures
      */
     public void dispose() {
     	
     	for (int i = 0; i < hudItems.size(); i++) {
     		
     		this.hudItems.get(i).dispose();
     	}
     }
 }
