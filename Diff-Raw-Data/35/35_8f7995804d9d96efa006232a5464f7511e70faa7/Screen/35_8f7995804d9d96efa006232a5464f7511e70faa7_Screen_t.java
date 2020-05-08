 package com.metagx.foundation.bettergl;
 
 import android.view.MotionEvent;
 
 public abstract class Screen {
    protected final GLGame game;
 
     protected final int glWidth, glHeight;
 
     protected int screenWidth = -1;
     protected int screenHeight = -1;
 
    public Screen(GLGame game, int glWidth, int glHeight) {
         this.game = game;
         this.glWidth = glWidth;
         this.glHeight = glHeight;
     }
 
     public int getScreenWidth() {
         return screenWidth;
     }
 
     public int getScreenHeight() {
         return screenHeight;
     }
 
     public void setScreenBounds(int width, int height) {
         this.screenWidth = width;
         this.screenHeight = height;
     }
 
 
     public abstract boolean onMotionEvent(MotionEvent e);
 
     public abstract void update(float deltaTime);
 
     public abstract void present(float deltaTime);
 
     public abstract void pause();
 
     public abstract void resume();
 
     public abstract void dispose();
 }
