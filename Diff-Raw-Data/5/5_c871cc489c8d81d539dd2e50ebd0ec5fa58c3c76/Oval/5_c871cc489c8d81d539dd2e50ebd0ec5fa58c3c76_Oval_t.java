 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.model.drawing;
 
 import java.awt.Graphics2D;
 
 /**
  * An oval.
  */
 public class Oval extends Rectangle {
 
     /**
      * @param x
      * @param y
      */
     public Oval(int x, int y, int width, int height) {
         super(x, y, width, height);
     }
 
    protected void draw(Graphics2D g, int translateX, int translateY) {
         int minX = Math.min(startPoint.x, endPoint.x);
         int minY = Math.min(startPoint.y, endPoint.y);
         
         int width = Math.abs(startPoint.x - endPoint.x);
         int height = Math.abs(startPoint.y - endPoint.y);
         
         g.drawOval(minX, minY, width, height);
     }
     
    protected void drawBackground(Graphics2D g, int translateX, int translateY) {
         int minX = Math.min(startPoint.x, endPoint.x);
         int minY = Math.min(startPoint.y, endPoint.y);
         
         int width = Math.abs(startPoint.x - endPoint.x);
         int height = Math.abs(startPoint.y - endPoint.y);
         
         g.fillOval(minX, minY, width, height);
     }
 }
