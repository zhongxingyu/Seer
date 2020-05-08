 /* The MIT License
  * 
  * Copyright (c) 2008 Jay Gorrell
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
 package net.rptools.maptool.client.ui.token;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.Shape;
import java.awt.geom.Path2D;
 import java.awt.geom.Rectangle2D;
 
 import net.rptools.maptool.model.Token;
 
 /**
  * Paint a square so that it doesn't overlay any other states being displayed in the same grid.
  * 
  * @author Jay
  */
 public class FlowTriangleTokenOverlay extends FlowColorDotTokenOverlay {
 
 
     /**
      * Default constructor needed for XML encoding/decoding
      */
     public FlowTriangleTokenOverlay() {
       this(TokenOverlay.DEFAULT_STATE_NAME, Color.RED, -1);
     }
 
     /**
      * Create a new dot token overlay
      * 
      * @param aName Name of the token overlay
      * @param aColor Color of the dot
      * @param aGrid Size of the overlay grid for this state. All states with the 
      * same grid size share the same overlay.
      */
     public FlowTriangleTokenOverlay(String aName, Color aColor, int aGrid) {
       super(aName, aColor, aGrid);
     }
 
     /**
      * @see net.rptools.maptool.client.ui.token.TokenOverlay#clone()
      */
     @Override
     public Object clone() {
         TokenOverlay overlay = new FlowTriangleTokenOverlay(getName(), getColor(), getGrid());
         overlay.setOrder(getOrder());
         return overlay;
     }
     
     /**
      * @see net.rptools.maptool.client.ui.token.FlowColorDotTokenOverlay#getShape(java.awt.Rectangle, net.rptools.maptool.model.Token)
      */
     @Override
     protected Shape getShape(Rectangle bounds, Token token) {
         Rectangle2D r = getFlow().getStateBounds2D(bounds, token, getName());
        Path2D p = new Path2D.Double();
         p.moveTo(r.getCenterX(), r.getY());
         p.lineTo(r.getX(), r.getMaxY());
         p.lineTo(r.getMaxX(), r.getMaxY());
         p.lineTo(r.getCenterX(), r.getY());
         p.closePath();
         return p;
     }
 }
