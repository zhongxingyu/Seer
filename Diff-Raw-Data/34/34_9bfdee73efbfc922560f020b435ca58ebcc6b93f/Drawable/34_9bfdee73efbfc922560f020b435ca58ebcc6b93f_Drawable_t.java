 package com.voracious.dragons.client.graphics;
 
 import java.awt.Graphics2D;
 
 public interface Drawable {
    /**
     * Draws this object onto the provided graphics object
     * 
     * @param g object to draw onto
     */
     public void draw(Graphics2D g);
 }
