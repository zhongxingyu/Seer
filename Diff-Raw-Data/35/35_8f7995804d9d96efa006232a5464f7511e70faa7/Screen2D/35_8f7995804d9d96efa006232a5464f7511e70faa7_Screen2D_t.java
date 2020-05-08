 package com.metagx.foundation.bettergl;
 
 import javax.microedition.khronos.opengles.GL10;
 
 /**
  * Created by Adam on 12/9/13.
  */
 public abstract class Screen2D extends Screen {
 
    public Screen2D(GLGame game, int glWidth, int glHeight) {
         super(game, glWidth, glHeight);
     }
 
     @Override
     public void resume() {
         GL10 gl = game.getGLGraphics().getGL();
         gl.glClearColor(0,1,0,0);
         gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
         gl.glMatrixMode(GL10.GL_PROJECTION);
         gl.glLoadIdentity();
         gl.glOrthof(0, glWidth, 0, glHeight, 1, -1);
 
         gl.glEnable(GL10.GL_TEXTURE_2D);
 
         gl.glMatrixMode(GL10.GL_MODELVIEW);
     }
 }
