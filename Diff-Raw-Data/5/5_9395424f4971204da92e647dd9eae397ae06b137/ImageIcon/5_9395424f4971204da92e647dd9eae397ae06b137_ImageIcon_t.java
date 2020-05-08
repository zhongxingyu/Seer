 //
 // $Id$
 //
 // BUI - a user interface library for the JME 3D engine
 // Copyright (C) 2005, Michael Bayne, All Rights Reserved
 //
 // This library is free software; you can redistribute it and/or modify it
 // under the terms of the GNU Lesser General Public License as published
 // by the Free Software Foundation; either version 2.1 of the License, or
 // (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
 package com.jmex.bui.icon;
 
 import com.jme.renderer.Renderer;
 
 import com.jmex.bui.BImage;
 
 /**
  * Displays an image as an icon.
  */
 public class ImageIcon extends BIcon
 {
     /**
      * Creates an icon from the supplied source image.
      */
     public ImageIcon (BImage image)
     {
         _image = image;
     }
 
     // documentation inherited
     public int getWidth ()
     {
         return _image.getWidth();
     }
 
     // documentation inherited
     public int getHeight ()
     {
         return _image.getHeight();
     }
 
     // documentation inherited
     public void wasAdded ()
     {
         super.wasAdded();
         _image.reference();
     }
 
     // documentation inherited
     public void wasRemoved ()
     {
         super.wasRemoved();
         _image.release();
     }
 
     // documentation inherited
     public void render (Renderer renderer, int x, int y, float alpha)
     {
         super.render(renderer, x, y, alpha);
        //_image.render(renderer, x, y, alpha);
        _image.render(renderer,
                0, 0, getWidth(), getHeight(),
                x, y, getWidth(), getHeight(), alpha);
     }
 
     protected BImage _image;
 }
