 /*
  *  BlinzEngine - A library for large 2D world simultions and games.
  *  Copyright (C) 2009-2010  Blinz <gtalent2@gmail.com>
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
 
 import org.blinz.input.KeyEvent;
 import org.blinz.graphics.Graphics;
 import org.blinz.input.ClickEvent;
 import org.blinz.input.MouseEvent;
 import org.blinz.input.MouseWheelEvent;
 import org.blinz.util.Bounds;
 import org.blinz.util.Position3D;
 import org.blinz.util.Size;
 
 /**
  *
  * @author Blinz
  */
 public abstract class BaseSprite extends ZoneObject {
 
     /**
      * Marks the Sprite for removal from its Zone. After removal from its Zone
      * it will be unmarked for removal. If no other reference to it exists it
      * will be deleted by the garbage colletor.
      */
     public final void delete() {
         getData().spritesToDelete.add(this);
     }
 
     /**
      * Stub method called when this sprite is deleted.
      */
     protected void onDelete() {
     }
 
     /**
      *
      * @return a Size object representing the sprites size.
      */
     public abstract Size getSize();
 
     /**
      * @return Sprite's width.
      */
     public abstract int getWidth();
 
     /**
      * @return Sprite's height.
      */
     public abstract int getHeight();
 
     /**
      * @return a Position3D representing the position of this Sprite.
      */
     public abstract Position3D getPosition();
 
     /**
      *
      * @return Sprite's x coordinate.
      */
     public abstract int getX();
 
     /**
      *
      * @return Sprite's y coordinate.
      */
     public abstract int getY();
 
     /**
      *
      * @return Sprite's z coordinate.
      */
     public abstract float getLayer();
 
     /**
      * 
      * @return an instance of Bounds representing the location and size of this
      * sprite.
      */
     public abstract Bounds getBounds();
 
     /**
      * Sets the width of this sprite to the given value.
      * @param width
      */
     public final void setWidth(int width) {
         if (width < 1)
 	    width = 1;
 	else {
             if (width > getData().sectorWidth()) {
                 width = getData().sectorWidth();
             }
             if (width + getX() > getData().getZoneWidth()) {
                 width = getData().getZoneWidth() - getX();
             }
 	}
 
         Sector otr = getData().getSectorOf(getX() + getWidth(), getY());
         Sector ntr = getData().getSectorOf(getX() + width, getY());
 
         if (otr != ntr) {
             if (width > getWidth()) {//if new width is greater
                 ntr.addIntersectingSprite(this);
                 Sector nbr = getData().getSectorOf(getX() + width, getY() + getHeight());
                 if (ntr != nbr) {
                     nbr.addIntersectingSprite(this);
                 }
             } else {//if old width is greater
                 otr.removeIntersectingSprite(this);
                 Sector obr = getData().getSectorOf(getX() + getWidth(), getY() + getHeight());
                 if (otr != obr) {
                     obr.removeIntersectingSprite(this);
                 }
             }
         }
         updateWidth((short) width);
     }
 
     /**
      * Sets the height of this sprite to the given value.
      * @param height
      */
     public final void setHeight(int height) {
     	if (height < 1) 
 	    height = 1;
 	else {
             if (getY() + height > getData().getZoneHeight()) {
                 height = getData().getZoneHeight() - getY();
             }
             if (height > getData().sectorHeight()) {
                 height = getData().sectorHeight() - getY();
             }
 	}
 
        Sector nbl = getData().getSectorOf(getX(), getY() + height);
        Sector obl = getData().getSectorOf(getX(), getY() + getHeight());
 
         if (nbl != obl) {
             if (height > getHeight()) {
                 nbl.addIntersectingSprite(this);
                 Sector nbr = getData().getSectorOf(getX() + getWidth(), getY() + height);
                 if (nbr != nbl) {
                     nbr.addIntersectingSprite(this);
                 }
             } else {
                 obl.removeIntersectingSprite(this);
                 Sector obr = getData().getSectorOf(getX() + getWidth(), getY() + getHeight());
                 if (obl != obr) {
                     obr.removeIntersectingSprite(this);
                 }
             }
         }
         updateHeight((short) height);
     }
 
     /**
      * Sets the size of this sprite to the given values.
      * @param width
      * @param height
      */
     public final void setSize(int width, int height) {
         //Method excessively large because of frequency of call and need for efficiency
         if (width > getData().sectorWidth()) {
             width = getData().sectorWidth();
         }
         if (height > getData().sectorHeight()) {
             height = getData().sectorHeight();
         }
 
         if (getData() == null || (sector2().contains(getX() + getWidth(), getY() + height))) {
             updateWidth((short) width);
             updateHeight((short) height);
             return;
         }
 
         if (height < getHeight()) {
             final Sector goal = getData().getSectorOf(getX() + getWidth(), getY() + height);
             Sector s2 = sector2();
             while (s2 != goal) {
                 s2.removeSprite(this);
 
                 Sector s = s2.rightNeighbor;
                 Sector ts = getData().getSectorOf(getX(), s2.getY());
 
                 while (s != ts) {
                     s.removeSprite(this);
                     s = s.rightNeighbor;
                 }
 
                 s2 = s2.leftNeighbor;
             }
         } else {
             final Sector goal = getData().getSectorOf(getX() + getWidth(), getY() + height);
             Sector s2 = sector2();
             while (s2 != goal) {
                 s2.addIntersectingSprite(this);
 
                 Sector s = s2.bottomNeighbor;
                 Sector ts = getData().getSectorOf(getX(), s2.getY());
 
                 while (s != ts) {
                     s.addIntersectingSprite(this);
                     s = s.bottomNeighbor;
                 }
 
                 s2 = s2.rightNeighbor;
             }
         }
 
 
         if (width < getWidth()) {
             final Sector goal = getData().getSectorOf(getX() + width, getY() + height);
             Sector s2 = sector2();
             while (s2 != goal) {
                 s2.removeSprite(this);
 
                 Sector s = s2.topNeighbor;
                 Sector ts = getData().getSectorOf(s2.getX(), getY() + height);
 
                 while (s != ts) {
                     s.removeSprite(this);
                     s = s.topNeighbor;
                 }
 
                 s2 = s2.leftNeighbor;
             }
         } else if (width > getWidth()) {
             final Sector goal = getData().getSectorOf(getX() + width, getY() + height);
             Sector s2 = sector2();
             while (s2 != goal) {
                 s2.addIntersectingSprite(this);
                 Sector s = s2.topNeighbor;
                 Sector ts = getData().getSectorOf(s2.getX(), getY() + height);
 
                 while (s != ts) {
                     s.addIntersectingSprite(this);
                     s = s.topNeighbor;
                 }
 
                 s2 = s2.rightNeighbor;
             }
         }
         updateWidth((short) width);
         updateHeight((short) height);
     }
 
     /**
      * Sets the x location of this sprite to the given value.
      * @param x the new x coordinate of this sprite        //Method excessively large because of frequency of call and need for efficiency
     
      */
     public final void setX(int x) {
         //Method excessively large because of frequency of call and need for efficiency
         final ZoneData zoneData = getData();
 
         //ensure the new location is within bounds
         if (x < 0) {
             x = 0;
         } else if (x + getWidth() > zoneData.getZoneWidth()) {
             x = zoneData.getZoneWidth() - getWidth();
         }
 
 
         Sector otl = zoneData.getSectorOf(getX(), getY());
         Sector obr = zoneData.getSectorOf(getX() + getWidth(), getY() + getHeight());
 
         Sector ntl = zoneData.getSectorOf(x, getY());
         Sector nbr = zoneData.getSectorOf(x + getWidth(), getY() + getHeight());
 
         if (ntl != otl || nbr != obr) {//if Sector set has changed
             Sector otr = zoneData.getSectorOf(getX() + getWidth(), getY());
             Sector obl = zoneData.getSectorOf(getX(), getY() + getHeight());
             Sector ntr = zoneData.getSectorOf(x + getWidth(), getY());
             Sector nbl = zoneData.getSectorOf(x, getY() + getHeight());
 
             if (x > getX()) {//if moving right
                 if (ntl != nbl) {//if top does not equal bottom
                     if (ntl != otl) {//if left Sectors have changed
                         otl.removeSprite(this);
                         otl.removeIntersectingSprite(this);
                         obl.removeIntersectingSprite(this);
                         ntl.addSprite(this);
                         if (ntl != otr) {//if new left isn't old right
                             otr.removeIntersectingSprite(this);
                             obr.removeIntersectingSprite(this);
                             ntl.addIntersectingSprite(this);
                             nbl.addIntersectingSprite(this);
                         }
                         if (ntl != ntr) {//if new left Sectors aren't the new right Sectors
                             ntr.addIntersectingSprite(this);
                             nbr.addIntersectingSprite(this);
                         }
                     } else {//the left Sectors have not changed, thus right Sectors have changed
                         ntr.addIntersectingSprite(this);
                         nbr.addIntersectingSprite(this);
                     }
 
                 } else {//if top equals bottom
                     if (ntl != otl) {//if left Sector has changed
                         otl.removeSprite(this);
                         otl.removeIntersectingSprite(this);
                         ntl.addSprite(this);
                         if (ntl != otr) {//if new left isn't old right
                             otr.removeIntersectingSprite(this);
                             ntl.addIntersectingSprite(this);
                         }
                         if (ntl != ntr) {//if new left Sector isn't the new right Sector
                             ntr.addIntersectingSprite(this);
                         }
                     } else {//the left Sector has not changed, thus the right Sector has changed
                         ntr.addIntersectingSprite(this);
                     }
                 }
 
             } else {//moving left
                 if (ntl != nbl) {//if top does not equal bottom
                     if (ntr != otr) {//if right has changed
                         otr.removeIntersectingSprite(this);
                         obr.removeIntersectingSprite(this);
                         if (ntl != otl) {//if old left isn't new left
                             otl.removeSprite(this);
                             ntl.addSprite(this);
                             ntl.addIntersectingSprite(this);
                             nbl.addIntersectingSprite(this);
                             if (ntr != otl) {//if new right isn't old left
                                 otl.removeIntersectingSprite(this);
                                 obr.removeIntersectingSprite(this);
                                 if (ntr != ntl) {
                                     ntr.addIntersectingSprite(this);
                                     nbr.addIntersectingSprite(this);
                                 }
                             }
                         } else {//if old left is the new right
                             otl.removeSprite(this);
                             ntl.addSprite(this);
                             ntl.addIntersectingSprite(this);
                             nbr.addIntersectingSprite(this);
                         }
                     } else {//if right has not changed, left must have changed
                         otl.removeSprite(this);
                         ntl.addSprite(this);
                         ntl.addIntersectingSprite(this);
                         nbr.addIntersectingSprite(this);
                     }
                 } else {//if top equals bottom
                     if (ntr != otr) {//if right has changed
                         otr.removeIntersectingSprite(this);
                         if (ntl != otl) {//if old left isn't new left
                             otl.removeSprite(this);
                             ntl.addSprite(this);
                             ntl.addIntersectingSprite(this);
                             if (ntr != otl) {//if new right isn't old left
                                 otl.removeIntersectingSprite(this);
                                 if (ntr != ntl) {
                                     ntr.addIntersectingSprite(this);
                                 }
                             }
                         } else {//if old left is the new right
                             otl.removeSprite(this);
                             ntl.addSprite(this);
                             ntl.addIntersectingSprite(this);
                         }
                     } else {//if right has not changed, left must have changed
                         otl.removeSprite(this);
                         ntl.addSprite(this);
                         ntl.addIntersectingSprite(this);
                     }
                 }
             }
         }
 
         updateX(x);
     }
 
     /**
      * Sets the y location of this sprite to the given value.
      * @param y the new y coordinate of this sprite
      */
     public final void setY(int y) {
         //Method excessively large because of frequency of call and need for efficiency
         final ZoneData zoneData = getData();
 
         //ensure the new location is within bounds
         if (y < 0) {
             y = 0;
         } else if (y + getHeight() > zoneData.getZoneHeight()) {
             y = zoneData.getZoneHeight() - getHeight();
         }
 
         final Sector otl = getData().getSectorOf(getX(), getY());
         final Sector ntl = getData().getSectorOf(getX(), y);
         final Sector obr = getData().getSectorOf(getX() + getWidth(), getY() + getHeight());
         final Sector nbr = getData().getSectorOf(getX() + getWidth(), y + getHeight());
 
         if (otl != ntl || obr != nbr) {
             final Sector otr = getData().getSectorOf(getX() + getWidth(), getY());
             final Sector obl = getData().getSectorOf(getX(), getY() + getHeight());
             final Sector ntr = getData().getSectorOf(getX() + getWidth(), y);
             final Sector nbl = getData().getSectorOf(getX(), y + getHeight());
             if (y > getY()) {//if moving down
                 if (otl != otr) {//if left does not equal right
                     if (otl != ntl) {//if the top Sectors have changed
                         otl.removeSprite(this);
                         otl.removeIntersectingSprite(this);
                         otr.removeIntersectingSprite(this);
                         ntl.addSprite(this);
                         if (ntl != obl) {
                             ntl.addIntersectingSprite(this);
                             ntr.addIntersectingSprite(this);
                             obl.removeIntersectingSprite(this);
                             obr.removeIntersectingSprite(this);
                             if (ntl != nbl) {
                                 nbl.addIntersectingSprite(this);
                                 nbr.addIntersectingSprite(this);
                             }
                         } else {
                             nbl.addIntersectingSprite(this);
                             nbr.addIntersectingSprite(this);
                         }
                     } else {//if top Sectors haven't changed
                         //add self to new bottom Sectors
                         nbl.addIntersectingSprite(this);
                         nbr.addIntersectingSprite(this);
                     }
                 } else {//if left equals right
                     if (otl != ntl) {//if the top Sector has changed
                         otl.removeSprite(this);
                         otl.removeIntersectingSprite(this);
                         ntl.addSprite(this);
                         if (ntl != obl) {//if the new top isn't the old bottom
                             ntl.addIntersectingSprite(this);
                             obl.removeIntersectingSprite(this);
                             if (ntl != nbl) {
                                 nbl.addIntersectingSprite(this);
                             }
                         } else {
                             nbl.addIntersectingSprite(this);
                         }
                     } else {//if top Sector hasn't changed
                         //add self to the new bottom Sector
                         nbl.addIntersectingSprite(this);
                     }
                 }
             } else {//if moving up
                 if (ntl != ntr) {//if does not left equal right
                     if (otl != ntl) {//if the bottom Sector has changed
                         obr.removeIntersectingSprite(this);
                         obl.removeIntersectingSprite(this);
                         if (nbl != otl) {//new bottom isn't old top
                             otl.removeSprite(this);
                             ntl.addSprite(this);
                             otl.removeIntersectingSprite(this);
                             otr.removeIntersectingSprite(this);
                             ntl.addIntersectingSprite(this);
                             ntr.addIntersectingSprite(this);
                             if (ntl != nbl) {
                                 nbl.addIntersectingSprite(this);
                                 nbr.addIntersectingSprite(this);
                             }
                         } else {//if the new bottom is the old top
                             if (ntl != otl) {
                                 ntl.addIntersectingSprite(this);
                                 ntr.addIntersectingSprite(this);
                                 ntl.addSprite(this);
                                 otl.removeSprite(this);
                             }
                         }
                     } else {//bottom Sector has not changed
                         otl.removeSprite(this);
                         ntl.addIntersectingSprite(this);
                         ntr.addIntersectingSprite(this);
                         ntl.addSprite(this);
                     }
                 } else {//if left equals right
                     if (otl != ntl) {//if the bottom Sector has changed
 
                         obl.removeIntersectingSprite(this);
                         if (nbl != otl) {//new bottom isn't old top
                             otl.removeSprite(this);
                             ntl.addSprite(this);
                             otl.removeIntersectingSprite(this);
                             ntl.addIntersectingSprite(this);
                             if (ntl != nbl) {
                                 nbl.addIntersectingSprite(this);
                             }
                         } else {//if the new bottom is the old top
                             if (ntl != otl) {
                                 ntl.addIntersectingSprite(this);
                                 ntl.addSprite(this);
                                 otl.removeSprite(this);
                             }
                         }
                     } else {//bottom Sector has not changed
                         ntl.addSprite(this);
                         ntl.addIntersectingSprite(this);
                     }
                 }
             }
         }
 
         updateY(y);
     }
 
     /**
      * Sets the location of this sprite to the given coordinates.
      * @param x the new x coordinate of this sprite
      * @param y the new y coordinate of this sprite
      */
     public final void setPosition(final int x, final int y) {
         setX(x);
         setY(y);
     }
 
     /**
      * Sets the layer of this sprite to the given value.
      * The layer of this sprite determines the other sprites it can collide with
      * and the ord in which they will be drawn. The lower the number the deeper
      * into the Zone the layer is.
      * @param layer
      */
     public final void setLayer(float layer) {
         if (layer < 0) {
             layer = 0;
         } else if (layer > 49) {
             layer = 49;
         }
         updateLayer(layer);
     }
 
     /**
      *
      * @return the amount of time the Zone has been executing, not including the
      * time before it started executing for time it was paused.
      */
     protected final long zoneTime() {
         return getData().zoneTime;
     }
 
     /**
      * A stub method for listening to clicks. Implement as needed.
      * @param event contains data about the input
      */
     protected void buttonClicked(final ClickEvent event) {
     }
 
     /**
      * A stub method for listening to mouse button presses. Implement as needed.
      * @param event contains data about the input
      */
     protected void buttonPressed(final MouseEvent event) {
     }
 
     /**
      * A stub method for listening to mouse button releases. Implement as needed.
      * @param event contains data about the input
      */
     protected void buttonReleased(final MouseEvent event) {
     }
 
     /**
      * A stub method for listening to the mouse wheel. Implement as needed.
      * @param event contains data about the input
      */
     protected void mouseWheelScroll(final MouseWheelEvent event) {
     }
 
     /**
      * A stub method for listening to the keys pressed. Implement as needed.
      * @param event contains data about the input
      */
     protected void keyPressed(final KeyEvent event) {
     }
 
     /**
      * A stub method for listening to the keys released. Implement as needed.
      * @param event contains data about the input
      */
     protected void keyReleased(final KeyEvent event) {
     }
 
     /**
      * A stub method for listening to the key typed. Implement as needed.
      * @param event contains data about the input
      */
     protected void keyTyped(final KeyEvent event) {
     }
 
     /**
      * Adds the given sprite to this sprites zone.
      * @param sprite
      */
     protected final void addSpriteToZone(final BaseSprite sprite) {
         getData().addSprite(sprite);
     }
 
     /**
      *
      * @return the name assigned to this class as a String
      */
     protected String getName() {
         return "BaseSprite";
     }
 
     protected abstract void draw(final Graphics g, final Bounds bounds);
 
     @Override
     protected abstract void init();
 
     /**
      * Updates the width of this sprite to that given.
      * @param width
      */
     protected abstract void updateWidth(short width);
 
     /**
      * Updates the height of this sprite to that given.
      * @param height
      */
     protected abstract void updateHeight(short height);
 
     /**
      * Updates the x of the sprite to that given.
      * @param x
      */
     protected abstract void updateX(int x);
 
     /**
      * Updates the y of the sprite to that given.
      * @param y
      */
     protected abstract void updateY(int y);
 
     /**
      * Updates the layer of the sprite of that given.
      * @param layer
      */
     protected abstract void updateLayer(float layer);
 
     /**
      * Returns the Sector in which the lower right corner of this sprite resides.
      * @return Sector
      */
     private final Sector sector2() {
         return getData().sectors[(getX() + getWidth()) / getData().sectorWidth()][getY() + getHeight() / getData().sectorHeight()];
     }
 }
