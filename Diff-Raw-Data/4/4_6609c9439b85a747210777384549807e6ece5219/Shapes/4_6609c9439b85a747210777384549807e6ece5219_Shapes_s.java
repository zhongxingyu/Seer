 /*
  * Copyright (C) 2012 Gyver
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.gyver.matrixmover.generator;
 
 import com.gyver.matrixmover.core.Controller;
 import com.gyver.matrixmover.core.MatrixData;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  *
  * @author Gyver
  */
 public class Shapes extends ObjectsContainingGenerator {
 
     private int objectCount = 0;
     private int speed = 0;
     private int nextColor = 0;
     private int size = 0;
     private int alive = 0;
     private int fade = 0;
     private int expand = 0;
     private double updatesToNextDrop = 0;
     private double dropUpdatesDone = 0;
     private ObjectShape objectShape = null;
     private ObjectDirection objectDirection = null;
     private ArrayList<ShapeObjects> objectList = null;
 
     /**
      * @return the objectShape
      */
     public ObjectShape getObjectShape() {
         return objectShape;
     }
 
     /**
      * @return the objectDirection
      */
     public ObjectDirection getObjectDirection() {
         return objectDirection;
     }
 
     public enum ObjectShape {
 
         SQUARE_EMPTY(0),
         SQUARE_FILLED(1),
         CIRCLE_EMPTY(2),
         CIRCLE_FILLED(3);
         private int mode;
 
         private ObjectShape(int mode) {
             this.mode = mode;
         }
 
         public int getMode() {
             return mode;
         }
 
         public static ObjectShape getObjectShape(int nr) {
             for (ObjectShape s : ObjectShape.values()) {
                 if (s.getMode() == nr) {
                     return s;
                 }
             }
             return null;
         }
 
         @Override
         public String toString() {
             switch (this) {
                 case SQUARE_EMPTY:
                     return "Squares Empty";
                 case SQUARE_FILLED:
                     return "Squares Filled";
                 case CIRCLE_EMPTY:
                     return "Circles Empty";
                 case CIRCLE_FILLED:
                     return "Circles Filled";
                 default:
                     super.toString();
             }
             // if it has no string, return the enum-string
             return super.toString();
         }
     }
 
     public enum ObjectDirection {
 
         EXPLODE(0),
         IMPODE(1);
         private int mode;
 
         private ObjectDirection(int mode) {
             this.mode = mode;
         }
 
         public int getMode() {
             return mode;
         }
 
         public static ObjectDirection getObjectDirection(int nr) {
             for (ObjectDirection s : ObjectDirection.values()) {
                 if (s.getMode() == nr) {
                     return s;
                 }
             }
             return null;
         }
 
         @Override
         public String toString() {
             switch (this) {
                 case IMPODE:
                     return "Imploade";
                 case EXPLODE:
                     return "Explode";
                 default:
                     super.toString();
             }
             // if it has no string, return the enum-string
             return super.toString();
         }
     }
 
     public Shapes(MatrixData md) {
         super(GeneratorName.SHAPES, md, null);
         this.objectCount = 1;
         this.speed = 240;
         this.size = 2;
         this.alive = 15;
         this.fade = 15;
         this.expand = 2;
         this.objectShape = ObjectShape.SQUARE_EMPTY;
         this.objectDirection = ObjectDirection.EXPLODE;
         calculateUpdateRate();
         this.objectList = new ArrayList<ShapeObjects>();
     }
 
     @Override
     public void update() {
         //if its time, create a new ShapeObject
         dropUpdatesDone++;
         while (dropUpdatesDone >= updatesToNextDrop) {
             dropUpdatesDone = dropUpdatesDone - updatesToNextDrop;
             //paint 'objectCount' new drops
             for (int i = 0; i < objectCount; i++) {
                 int x = (int) Math.floor(Math.random() * internalBufferWidth);
                 int y = (int) Math.floor(Math.random() * internalBufferHeight);
                 nextColor = ((nextColor + 1) % colorMap.size());
                 int color = this.getColor(nextColor);
 
                 ShapeObjects obj = new ShapeObjects(x, y, color, size, alive, getExpand(), fade);
                 objectList.add(obj);
             }
         }
 
         Arrays.fill(this.internalBuffer, 0);
         ArrayList<ShapeObjects> objectsToRemove = new ArrayList<ShapeObjects>();
         for (ShapeObjects so : objectList) {
             so.update();
             if (so.isDead()) {
                 objectsToRemove.add(so);
             }
         }
 
         if (!objectsToRemove.isEmpty()) {
             objectList.removeAll(objectsToRemove);
         }
 
     }
 
     /**
      * @param objectShape the objectShape to set
      */
     public void setObjectShape(ObjectShape objectShape) {
         this.objectShape = objectShape;
     }
 
     /**
      * @param objectDirection the objectDirection to set
      */
     public void setObjectDirection(ObjectDirection objectDirection) {
         this.objectDirection = objectDirection;
     }
 
     /**
      * @return the objectCount
      */
     public int getObjectCount() {
         return objectCount;
     }
 
     /**
      * @param objectCount the oubjectCount to set
      */
     public void setObbjectCount(int objectCount) {
         this.objectCount = objectCount;
     }
 
     /**
      * @return the speed
      */
     public int getSpeed() {
         return speed;
     }
 
     /**
      * @param speed the speed to set
      */
     public void setSpeed(int speed) {
         this.speed = speed;
         calculateUpdateRate();
     }
 
     /**
      * @return the size
      */
     public int getSize() {
         return size + 1;
     }
 
     /**
      * @param size the size to set
      */
     public void setSize(int size) {
         this.size = size - 1;
     }
 
     /**
      * @return the alive
      */
     public int getAlive() {
         return alive;
     }
 
     /**
      * @param alive the alive to set
      */
     public void setAlive(int alive) {
         this.alive = alive;
     }
 
     /**
      * @return the fade
      */
     public int getFade() {
         return fade;
     }
 
     /**
      * @param fade the fade to set
      */
     public void setFade(int fade) {
         this.fade = fade;
     }
 
     /**
      * @return the expand
      */
     public int getExpand() {
         return expand;
     }
 
     /**
      * @param expand the expand to set
      */
     public void setExpand(int expand) {
         this.expand = expand;
     }
 
     private void calculateUpdateRate() {
         int fps = Controller.getControllerInstance().getFps();
         updatesToNextDrop = (fps / (speed / 60F));
     }
 
     private class ShapeObjects implements Serializable {
 
         private int x, y, color, size, alive, expand, fade;
         private boolean dead = false;
         private double expandIndex, expandHit;
         private int usedColor;
 
         /**
          * A ShapeObject represents one shape of the generator
          * @param x the x coordinate (center of shape)
          * @param y the y coordinate (center of shape)
          * @param color the color of the shape
          * @param size the initial size of the shape
          * @param alive the number of frames the shape is shown
          * @param expand the amount of pixels the shape expands in each direction over its livetime
          * @param fade the number of frames, the shape is fading out (last frames of lifetime)
          */
         private ShapeObjects(int x, int y, int color, int size, int alive, int expand, int fade) {
             this.x = x;
             this.y = y;
             this.color = color;
 
             switch (getObjectDirection()) {
                 case EXPLODE:
                     this.size = size;
                     break;
                 case IMPODE:
                     switch (getObjectShape()) {
                         case SQUARE_EMPTY:
                         case SQUARE_FILLED:
                             this.size = size + (2 * expand);
                             break;
                         case CIRCLE_EMPTY:
                         case CIRCLE_FILLED:
                             this.size = size + expand;
                             break;
                     }
                     break;
             }
 
 
             this.alive = alive;
             this.expand = expand;
             this.fade = fade;
             this.expandIndex = alive / (double) (expand + 1);
             this.expandHit = alive - expandIndex;
         }
 
         private void update() {
             alive--;
             if (alive <= 0) {
                 dead = true;
             }
 
             switch (getObjectDirection()) {
                 case EXPLODE:
                     while (alive < expandHit) {
                         switch (getObjectShape()) {
                             case SQUARE_EMPTY:
                             case SQUARE_FILLED:
                                 size = size + 2;
                                 break;
                             case CIRCLE_EMPTY:
                             case CIRCLE_FILLED:
                                 size++;
                                 break;
                         }
                         expandHit = expandHit - expandIndex;
                     }
                     break;
                 case IMPODE:
                     while (alive < expandHit) {
                         switch (getObjectShape()) {
                             case SQUARE_EMPTY:
                             case SQUARE_FILLED:
                                 size = size - 2;
                                 break;
                             case CIRCLE_EMPTY:
                             case CIRCLE_FILLED:
                                 size--;
                                 break;
                         }
                         expandHit = expandHit - expandIndex;
                     }
                     break;
             }
 
 
             //fade the color, if less time to live than to fade
             usedColor = color;
             if (alive < fade) {
                 short r = (short) Math.round(((color >> 16) & 255) * ((alive + 1) / (float) (fade + 1)));
                 short g = (short) Math.round(((color >> 8) & 255) * ((alive + 1) / (float) (fade + 1)));
                 short b = (short) Math.round((color & 255) * ((alive + 1) / (float) (fade + 1)));
                 usedColor = (r << 16) | (g << 8) | b;
             }
 
             switch (getObjectShape()) {
                 case SQUARE_EMPTY:
                     drawEmptySquare();
                     break;
                 case SQUARE_FILLED:
                     drawFilledSquare();
                     break;
                 case CIRCLE_EMPTY:
                     drawEmptyCircle();
                     break;
                 case CIRCLE_FILLED:
                     drawFilledCircle();
                     break;
             }
 
 
         }
 
         private boolean isDead() {
             return dead;
         }
 
         private void drawEmptySquare() {
             //calculate the coordinates
             int hs = size / 2;
             int rs = size % 2;
             int x_l = x - hs;
             int x_r = x + hs + rs;
             int y_t = y - hs;
             int y_b = y + hs + rs;
 
             //draw the shape
             //left line
             for (int i = y_t; i <= y_b; i++) {
                 if (x_l >= 0 && i >= 0 && i < internalBufferHeight) {
                     internalBuffer[x_l + (i * internalBufferWidth)] = usedColor;
                 }
             }
 
             //right line
             for (int i = y_t; i <= y_b; i++) {
                 if (x_r < internalBufferWidth && i >= 0 && i < internalBufferHeight) {
                     internalBuffer[x_r + (i * internalBufferWidth)] = usedColor;
                 }
             }
 
             //top line
             for (int i = x_l + 1; i < x_r; i++) {
                 if (y_t >= 0 && i >= 0 && i < internalBufferWidth) {
                     internalBuffer[i + (y_t * internalBufferWidth)] = usedColor;
                 }
             }
 
             //bottom line
             for (int i = x_l + 1; i < x_r; i++) {
                 if (y_b < internalBufferHeight && i >= 0 && i < internalBufferWidth) {
                     internalBuffer[i + (y_b * internalBufferWidth)] = usedColor;
                 }
             }
         }
 
         private void drawFilledSquare() {
             //calculate the coordinates
             int hs = size / 2;
             int rs = size % 2;
             int x_l = x - hs;
             int x_r = x + hs + rs;
             int y_t = y - hs;
             int y_b = y + hs + rs;
 
             //draw the shape
             //from left to right (linewise)
 
             for (int j = x_l; j <= x_r; j++) {
                 for (int i = y_t; i <= y_b; i++) {
                     setPixel(j, i, usedColor);
                     if (j >= 0 && j < internalBufferWidth && i >= 0 && i < internalBufferHeight) {
                         internalBuffer[j + (i * internalBufferWidth)] = usedColor;
                     }
                 }
             }
 
 
         }
 
         private void drawEmptyCircle() {
             double f = 1 - size;
             int ddFx = 1;
             double ddFy = -2 * size;
             int x_pos = 0;
             double y_pos = size;
 
             setPixel(x, (int) Math.round(y + size), usedColor);
             setPixel(x, (int) Math.round(y - size), usedColor);
             setPixel((int) Math.round(x + size), y, usedColor);
             setPixel((int) Math.round(x - size), y, usedColor);
 
             while (x_pos < y_pos) {
                 if (f >= 0) {
                     y_pos--;
                     ddFy += 2;
                     f += ddFy;
                 }
                 x_pos++;
                 ddFx += 2;
                 f += ddFx;
                 setPixel(x + x_pos, (int) Math.round(y + y_pos), usedColor);
                 setPixel(x - x_pos, (int) Math.round(y + y_pos), usedColor);
                 setPixel(x + x_pos, (int) Math.round(y - y_pos), usedColor);
                 setPixel(x - x_pos, (int) Math.round(y - y_pos), usedColor);
                 setPixel((int) Math.round(x + y_pos), y + x_pos, usedColor);
                 setPixel((int) Math.round(x - y_pos), y + x_pos, usedColor);
                 setPixel((int) Math.round(x + y_pos), y - x_pos, usedColor);
                 setPixel((int) Math.round(x - y_pos), y - x_pos, usedColor);
 
             }
         }
 
         private void drawFilledCircle() {
             for (double r = 0; r <= size; r = r + 0.1) {
                 double f = 1 - r;
                 int ddFx = 1;
                 double ddFy = -2 * r;
                 int x_pos = 0;
                 double y_pos = r;
 
                 setPixel(x, (int) Math.round(y + r), usedColor);
                 setPixel(x, (int) Math.round(y - r), usedColor);
                 setPixel((int) Math.round(x + r), y, usedColor);
                 setPixel((int) Math.round(x - r), y, usedColor);
 
                 while (x_pos < y_pos) {
                     if (f >= 0) {
                         y_pos--;
                         ddFy += 2;
                         f += ddFy;
                     }
                     x_pos++;
                     ddFx += 2;
                     f += ddFx;
                     setPixel(x + x_pos, (int) Math.round(y + y_pos), usedColor);
                     setPixel(x - x_pos, (int) Math.round(y + y_pos), usedColor);
                     setPixel(x + x_pos, (int) Math.round(y - y_pos), usedColor);
                     setPixel(x - x_pos, (int) Math.round(y - y_pos), usedColor);
                     setPixel((int) Math.round(x + y_pos), y + x_pos, usedColor);
                     setPixel((int) Math.round(x - y_pos), y + x_pos, usedColor);
                     setPixel((int) Math.round(x + y_pos), y - x_pos, usedColor);
                     setPixel((int) Math.round(x - y_pos), y - x_pos, usedColor);
 
                 }
             }
         }
 
         /**
          * Sets the pixel.
          *
          * @param x the x
          * @param y the y
          * @param col the col
          */
         private void setPixel(int x, int y, int col) {
             if (y >= 0 && y < internalBufferHeight && x >= 0 && x < internalBufferWidth) {
                 internalBuffer[y * internalBufferWidth + x] = col;
             }
         }
     }
 }
