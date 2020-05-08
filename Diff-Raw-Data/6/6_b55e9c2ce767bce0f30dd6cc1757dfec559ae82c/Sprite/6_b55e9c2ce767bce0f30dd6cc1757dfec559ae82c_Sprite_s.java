 /*
  *  BlinzEngine - A library for large 2D world simultions and games.
  *  Copyright (C) 2009 - 2010  Blinz <gtalent2@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.blinz.world;
 
 import org.blinz.util.Bounds;
 import org.blinz.util.Position3D;
 import org.blinz.util.Size;
 
 /**
  *
  * @author Blinz
  */
 public abstract class Sprite extends BaseSprite {
 
     private int x = 0, y = 0;
     private float layer = 0;
     private short width = 1, height = 1;
 
     //PUBLIC METHODS------------------------------------------------------------
     /**
      *
      * @return Size object representing the Sprite's size.
      */
     @Override
     public final Size getSize() {
         return new Size(width, height);
     }
 
     /**
      * 
      * @return Width of Sprite.
      */
     @Override
     public final int getWidth() {
         return width;
     }
 
     /**
      * 
      * @return Height of Sprite
      */
     @Override
     public final int getHeight() {
         return height; 
     }
 
     /**
      * 
      * @return A Location object representing the Sprite's location.
      */
     @Override
     public final Position3D getPosition() {
        return new Position3D(x, y);
     }
 
     /**
      * 
      * @return The x coordinate of the Sprite.
      */
     @Override
     public final int getX() {
         return x;
     }
 
     /**
      * 
      * @return The y coordinate of the Sprite.
      */
     @Override
     public final int getY() {
         return y;
     }
 
     /**
      *
      * @return The z coordinate of the Sprite.
      */
     @Override
     public final float getLayer() {
         return layer;
     }
 
     @Override
     public Bounds getBounds() {
         return new Bounds(x, y, width, height);
     }
 
     public final void moveDown(final int distance) {
         setY(y + distance);
     }
 
     public final void moveUp(final int distance) {
         setY(y - distance);
     }
 
     public final void moveRight(final int distance) {
         setX(x + distance);
     }
 
     public final void moveLeft(final int distance) {
         setX(x - distance);
     }
 
    //END OF PUBLIC METHODS/////////////////////////////////////////////////////
    //PROTECTED METHODS---------------------------------------------------------

     @Override
     protected final void updateWidth(final int width) {
         this.width = (short)width;
     }
 
     @Override
     protected final void updateHeight(final int height) {
         this.height = (short)height;
     }
 
     @Override
     protected final void updateX(final int x) {
         this.x = x;
     }
 
     @Override
     protected final void updateY(final int y) {
         this.y = y;
     }
 
     @Override
     protected final void updateLayer(final float layer) {
         this.layer = layer;
     }
 }
