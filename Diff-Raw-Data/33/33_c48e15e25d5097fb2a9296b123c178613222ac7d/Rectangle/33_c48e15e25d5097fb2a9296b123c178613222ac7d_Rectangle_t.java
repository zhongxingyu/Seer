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
 import java.awt.Point;
 import java.awt.RenderingHints;
 
 /**
  * An rectangle
  */
 public class Rectangle extends AbstractDrawing {
     protected Point startPoint;
     protected Point endPoint;
     private transient java.awt.Rectangle bounds;
     
     public Rectangle(int startX, int startY, int endX, int endY) {
 
         startPoint = new Point(startX, startY);
         endPoint = new Point(endX, endY);
     }
     
     /* (non-Javadoc)
 	 * @see net.rptools.maptool.model.drawing.Drawable#getBounds()
 	 */
 	public java.awt.Rectangle getBounds() {
 
 		if (bounds == null) {
 	        int x = Math.min(startPoint.x, endPoint.x);
 	        int y = Math.min(startPoint.y, endPoint.y);
 	        int width = Math.abs(endPoint.x - startPoint.x);
 	        int height = Math.abs(endPoint.y - startPoint.y);
 	        
 	        bounds = new java.awt.Rectangle(x, y, width, height);
 		}
         
 		return bounds;
 	}
     
     public Point getStartPoint() {
         return startPoint;
     }
     
     public Point getEndPoint() {
         return endPoint;
     }
 
     protected void draw(Graphics2D g) {
     	
         int minX = Math.min(startPoint.x, endPoint.x);
         int minY = Math.min(startPoint.y, endPoint.y);
         
         int width = Math.abs(startPoint.x - endPoint.x);
         int height = Math.abs(startPoint.y - endPoint.y);
         
     	Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
     	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
         g.drawRect(minX, minY, width-1, height-1);
     	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
     }
 
     protected void drawBackground(Graphics2D g) {
         int minX = Math.min(startPoint.x, endPoint.x);
         int minY = Math.min(startPoint.y, endPoint.y);
         
         int width = Math.abs(startPoint.x - endPoint.x);
         int height = Math.abs(startPoint.y - endPoint.y);
         
     	Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
     	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.drawRect(minX, minY, width-1, height-1);
         g.fillRect(minX, minY, width-1, height-1);
     	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
     }
 }
