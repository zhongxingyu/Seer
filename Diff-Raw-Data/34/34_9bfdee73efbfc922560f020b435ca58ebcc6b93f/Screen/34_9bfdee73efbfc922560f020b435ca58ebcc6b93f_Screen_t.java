 package com.voracious.dragons.client.graphics;
 
 import java.awt.Graphics2D;
 
 public abstract class Screen implements Drawable{
     protected int width, height;
     private int offsetx, offsety;
 
     public Screen(int width, int height){
         this.width = width;
         this.height = height;
         this.offsetx = this.offsety = 0;
     }
     
     public void draw(Graphics2D g){
         g.translate(-offsetx, -offsety);
         render(g);
     }
     
     /**
      * Called periodically, draw things here
      */
     public abstract void render(Graphics2D g);
     
     /**
      * Called periodically, do calculations updates here
      */
     public abstract void tick();
    
    /**
     * Translates the screen so that when it is drawn the entire thing will be translated
     * 
     * @param dx amount to translate in the x direction
     * @param dy amount to translate in the y direction
     */
    public void translate(int dx, int dy){
        offsetx += dx;
        offsety += dy;
    }
 
     /**
      * @return the width
      */
     public int getWidth() {
         return width;
     }
     
     /**
      * @return the height
      */
     public int getHeight() {
         return height;
     }
 
     /**
      * @return the offsetx
      */
     protected int getOffsetx() {
         return offsetx;
     }
 
     /**
      * @param offsetx the offsetx to set
      */
     protected void setOffsetx(int offsetx) {
         this.offsetx = offsetx;
     }
 
     /**
      * @return the offsety
      */
     protected int getOffsety() {
         return offsety;
     }
 
     /**
      * @param offsety the offsety to set
      */
     protected void setOffsety(int offsety) {
         this.offsety = offsety;
     }
 }
