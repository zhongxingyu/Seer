 package com.icbat.game.tradesong.screens.listeners;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 
 /***/
 public class ZoomOnScrollListener extends InputListener {
 
     public static final float ZOOM_SPEED = 0.2f;
     private final OrthographicCamera camera;
     private static final float MAX_ZOOM = 3f;
     private static final float MIN_ZOOM = 0.1f;
 
     /**
      * Changes the camera zoom by the given amount. Checks to make sure within both bounds.
      * */
     private void offsetZoom(float amount) {
         float proposedZoom = camera.zoom + amount;
         if (proposedZoom >= MIN_ZOOM && proposedZoom <= MAX_ZOOM) {
             camera.zoom = proposedZoom;
         }
     }
 
     public ZoomOnScrollListener(OrthographicCamera camera) {
         this.camera = camera;
     }
 
     @Override
     public boolean scrolled(InputEvent event, float x, float y, int amount) {
         // amount: -1 is scrolling up, 1 is scrolling down
         offsetZoom(amount * ZOOM_SPEED);
 
         return true;
     }
 }
