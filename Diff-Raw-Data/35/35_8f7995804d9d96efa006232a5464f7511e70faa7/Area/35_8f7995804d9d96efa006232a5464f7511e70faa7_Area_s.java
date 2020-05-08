 package com.metagx.foundation.bettergl.model.area;
 
 import android.os.SystemClock;
 
 import com.metagx.foundation.bettergl.BindableVertices;
 import com.metagx.foundation.bettergl.GLGraphics;
 import com.metagx.foundation.bettergl.model.LineType;
 import com.metagx.foundation.bettergl.model.OpenGLLine;
 import com.metagx.foundation.bettergl.model.Triple;
 import com.metagx.foundation.math.Rectangle;
 import java.util.Random;
 
 import javax.microedition.khronos.opengles.GL10;
 
 /**
  * Created by Adam on 12/16/13.
  */
 public class Area {
 
     protected Rectangle areaBounds;
 
     protected BindableVertices bindableVertices;
 
     protected GLGraphics glGraphics;
 
     protected boolean isSealed = false;
 
 //    public static int static_i = 0;
 //    Triple<Float> color;
 //    public static final Triple<Float>[] static_colors = new Triple[7];
 
     private float alpha = 0.0f;
 
     private final float alpha_fade_delta = .05f;
     private final static float MAX_ALPHA = .4f;
 
     public Area(GLGraphics glGraphics, float centerX, float centerY, float width, float height) {
         areaBounds = new Rectangle(centerX-width/2, centerY-height/2, width, height);
 
         this.glGraphics = glGraphics;
 
         setBindableVertices();
 
 //        static_colors[0] = new Triple<Float>(1f,0f,0f);
 //        static_colors[1] = new Triple<Float>(0f,1f,0f);
 //        static_colors[2] = new Triple<Float>(0f,0f,1f);
 //        static_colors[3] = new Triple<Float>(1f,1f,0f);
 //        static_colors[4] = new Triple<Float>(0f,1f,1f);
 //        static_colors[5] = new Triple<Float>(1f,0f,1f);
 //        static_colors[6] = new Triple<Float>(0f,0f,0f);
 //
 //        color = static_colors[static_i%static_colors.length];
 //        static_i++;
     }
 
     public Rectangle getAreaBounds() {
         return areaBounds;
     }
 
     public boolean isSealed() {
         return isSealed;
     }
 
     public void setSealed(boolean isSealed) {
         this.isSealed = isSealed;
     }
 
     public SplitArea splitArea(OpenGLLine bisect) {
         if(bisect.getLineType() == LineType.HORIZONTAL) {
 
             //Horizontal
             float centerXTop = areaBounds.lowerLeft.x + areaBounds.width/2.0f;
            float centerYTop = (areaBounds.lowerLeft.y+areaBounds.height+bisect.getMotionModel().getPosition().y)/2.0f + 1;
 
             float centerXBottom = areaBounds.lowerLeft.x + areaBounds.width/2.0f;
             float centerYBottom = (areaBounds.lowerLeft.y+bisect.getMotionModel().getPosition().y)/2.0f;
 
             float topHeight = 2*(centerYTop-bisect.getMotionModel().getPosition().y);
             float bottomHeight = 2*(bisect.getMotionModel().getPosition().y - centerYBottom);
 
             final Area top = new Area(glGraphics, centerXTop, centerYTop, areaBounds.width, topHeight);
             final Area bottom = new Area(glGraphics, centerXBottom, centerYBottom, areaBounds.width, bottomHeight);
 
             return new SplitArea(top, bottom);
         } else {
             //Vertical
 
            float centerXRight = (areaBounds.lowerLeft.x+areaBounds.width+bisect.getMotionModel().getPosition().x)/2 + 1;
             float centerYRight = areaBounds.lowerLeft.y+areaBounds.height/2;
 
             float centerXLeft = (areaBounds.lowerLeft.x+bisect.getMotionModel().getPosition().x)/2;
             float centerYLeft = areaBounds.lowerLeft.y+areaBounds.height/2;
 
             float rightWidth = 2*(centerXRight-bisect.getMotionModel().getPosition().x);
             float leftWidth = 2*(bisect.getMotionModel().getPosition().x - centerXLeft);
 
             final Area right = new Area(glGraphics, centerXRight, centerYRight, rightWidth, areaBounds.height);
             final Area left = new Area(glGraphics, centerXLeft, centerYLeft, leftWidth, areaBounds.height);
 
             return new SplitArea(left, right);
         }
     }
 
     public void draw() {
         if(!isSealed) {
             return;
         }
 
         GL10 gl = glGraphics.getGL();
 
         gl.glDisable(GL10.GL_TEXTURE_2D);
         bindableVertices.bind();
 
         gl.glLoadIdentity();
 
         alpha = alpha<MAX_ALPHA?alpha+alpha_fade_delta:MAX_ALPHA;
 
         gl.glColor4f(1, 0, 0, alpha);
 
         gl.glTranslatef(areaBounds.lowerLeft.x + areaBounds.width / 2, areaBounds.lowerLeft.y + areaBounds.height / 2, 0);
         bindableVertices.draw(GL10.GL_TRIANGLES, 0, 6);
         gl.glColor4f(1, 1, 1, 1f);
         bindableVertices.unbind();
     }
 
     protected void setBindableVertices() {
         float width = areaBounds.width;
         float height = areaBounds.height;
 
         bindableVertices = new BindableVertices(glGraphics, 4, 6, false, false);
         bindableVertices.setVertices(new float[] {
                 -width/2, -height/2,
                 width/2, -height/2,
                 width/2, height/2,
                 -width/2, height/2}, 0, 8);
 
         bindableVertices.setIndices(new short[] {0, 1, 2, 2, 3, 0}, 0, 6);
     }
 }
