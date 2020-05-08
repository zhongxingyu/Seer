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
 import java.util.Vector;
 
 /**
  *
  * @author Blinz
  */
 final class Sector extends ZoneObject {
 
     Sector topNeighbor, bottomNeighbor, rightNeighbor, leftNeighbor;
     private final UnorderedList<UpdatingSprite> updatingSprites = new UnorderedList<UpdatingSprite>();
     private final Vector<UpdatingSprite> updatingSpritesToAdd = new Vector<UpdatingSprite>();
     private final Vector<UpdatingSprite> updatingSpritesToRemove = new Vector<UpdatingSprite>();
     /**
      * Sprites that intersect this Sector can be seen by observers viewing this
      * Sector from above.
      */
     private final UnorderedList<BaseSprite> intersectingSprites = new UnorderedList<BaseSprite>();
     private final UnorderedList<CollidibleSprite> collidibleSprites = new UnorderedList<CollidibleSprite>();
     private final Vector<Camera> cameras = new Vector<Camera>();
     private final Vector<Camera> camerasToAdd = new Vector<Camera>(), camerasToRemove = new Vector<Camera>();
     private final Bounds bounds = new Bounds();
 
     /**
      * Sector constructer.
      * @param x x coordinate of this Sectoer
      * @param y y coordinate of this Sectoer
      */
     Sector(int x, int y) {
         bounds.setPosition(x, y);
     }
 
     @Override
     void init() {
         findNeighbors();
     }
 
     /**
      * Updates the sprites in this Sector.
      */
     final void update() {
         for (int i = 0; i < updatingSprites.size(); i++) {
             updatingSprites.get(i).update();
         }
     }
 
     /**
      * Takes care of modifications made during the update.
      */
     final void postUpdate() {
         manageUpdatingSprites();
         manageCameras();
     }
 
     /**
      * Returns the x position of this Sector in the array.
      * @return
      */
     final int getXIndex() {
         return bounds.getX() / getData().sectorSize.width;
     }
 
     /**
      * Returns the y position of this Sector in the array.
      * @return
      */
     final int getYIndex() {
         return bounds.getY() / getData().sectorSize.height;
     }
 
     /**
      * Returns the x location of this Sector.
      * @return x
      */
     final int getX() {
         return bounds.getX();
     }
 
     /**
      * Returns the y location of this Sector.
      * @return y
      */
     final int getY() {
         return bounds.getY();
     }
 
     /**
      * Returns the width of Sectors in this Zone.
      * @return width of this Sector
      */
     final int getWidth() {
         return getData().sectorSize.width;
     }
 
     /**
      * Returns the height of Sectors in this Zone.
      * @return height of this Sector
      */
     final int getHeight() {
         return getData().sectorSize.height;
     }
 
     /**
      * Adds given Camera to this Sector to recieve current and incoming sprites.
      * @param sprite
      */
     final void addCamera(Camera camera) {
         camerasToAdd.add(camera);
     }
 
     /**
      * Removes the given Camera from this Sector.
      * @param camera
      */
     final void removeCamera(Camera camera) {
         camerasToRemove.add(camera);
     }
 
     /**
      * Adds given sprite to this Sector.
      * @param sprite
      */
     protected final void addSprite(BaseSprite sprite) {
         if (sprite instanceof UpdatingSprite) {
             updatingSpritesToAdd.add((UpdatingSprite) sprite);
         }
     }
 
     /**
      * Removes the given sprite from this sector.
      * @param sprite
      */
     final void removeSprite(BaseSprite sprite) {
         if (sprite instanceof UpdatingSprite) {
             updatingSpritesToRemove.add((UpdatingSprite) sprite);
         }
     }
 
     /**
      * Adds given sprite to the list of sprites intersecting this Sector.
      * @param sprite
      */
     final void addIntersectingSprite(BaseSprite sprite) {
         intersectingSprites.add(sprite);
         if (sprite instanceof CollidibleSprite) {
             synchronized (collidibleSprites) {
                 collidibleSprites.add((CollidibleSprite) sprite);
             }
         }
         for (Camera camera : cameras) {
             camera.addSprite(sprite);
         }
     }
 
     /**
      * Removes the given sprite from the list of sprites intersecting this Sector.
      * @param sprite
      */
     final void removeIntersectingSprite(BaseSprite sprite) {
         intersectingSprites.remove(sprite);
         if (sprite instanceof CollidibleSprite) {
             synchronized (collidibleSprites) {
                 collidibleSprites.add((CollidibleSprite) sprite);
             }
         }
         for (Camera camera : cameras) {
             camera.decrementSpriteUsage(sprite);
         }
     }
 
     final void checkCollisions(CollidibleSprite sprite) {
         synchronized (collidibleSprites) {
             for (int i = 0; i < collidibleSprites.size(); i++) {
                 BaseSprite s1 = (BaseSprite) sprite, s2 = (BaseSprite) collidibleSprites.get(i);
                if (Bounds.intersects(s1.getX(), s1.getY(), s1.getWidth(), s1.getHeight(),
                         s2.getX(), s2.getY(), s2.getWidth(), s2.getHeight())) {
                     if (s1 != s2) {
                        ((CollidibleSprite)s1).collide(s1);
                        ((CollidibleSprite)s2).collide(s2);
                     }
                 }
             }
         }
     }
 
     /**
      * Removes the given sprite from the list of sprites intersecting this Sector.
      * @param sprite
      */
     synchronized final void deleteIntersectingSprite(BaseSprite sprite) {
         removeIntersectingSprite(sprite);
     }
 
     /**
      * Removes the given UpdatingSprite now. For use only by the main update process
      * in Zone.
      * @param sprite
      */
     synchronized final void deleteUpdatingSprite(UpdatingSprite sprite) {
         if (updatingSprites.remove(sprite)) {
             updatingSpritesToAdd.remove(sprite);
         }
     }
 
     /**
      *
      * @param x
      * @param y
      * @return true if given point is within the bounds of this Sector, false otherwise
      */
     final boolean contains(int x, int y) {
         bounds.setSize(getData().sectorSize);
         return bounds.contains(x, y);
     }
 
     /**
      * Returns true if the specified bounds intersects with the bounds of this Sector,
      * false if it does not.
      * @param bounds
      * @return true if the specified coordinates intersect this Sector
      */
     final boolean intersects(Bounds bounds) {
         this.bounds.setSize(getData().sectorSize);
         return this.bounds.intersects(bounds);
     }
 
     /**
      * Returns true if the specified bounds intersects with the bounds of this Sector,
      * false if it does not.
      * @param x
      * @param y
      * @param width
      * @param height
      * @return true if the specified coordinates intersect this Sector
      */
     final boolean intersects(int x, int y, int width, int height) {
         this.bounds.setSize(getData().sectorSize);
         return this.bounds.intersects(x, y, width, height);
     }
 
     final void findNeighbors() {
         int ix = bounds.getX() / getData().sectorSize.width;
         int iy = bounds.getY() / getData().sectorSize.height;
 
         if (ix > 0) {
             leftNeighbor = getData().sectors[ix - 1][iy];
         }
         if (iy > 0) {
             topNeighbor = getData().sectors[ix][iy - 1];
         }
         if (ix < getData().sectors.length - 1) {
             rightNeighbor = getData().sectors[ix + 1][iy];
         }
         if (iy < getData().sectors[ix].length - 1) {
             bottomNeighbor = getData().sectors[ix][iy + 1];
         }
     }
 
     /**
      * Trims the size of lists.
      */
     final void trimLists() {
         updatingSpritesToAdd.trimToSize();
         updatingSpritesToRemove.trimToSize();
         intersectingSprites.trimToSize();
         cameras.trimToSize();
     }
 
     /**
      * Manages sprites on the updating sprites list.
      */
     private final void manageUpdatingSprites() {
         for (int i = updatingSpritesToRemove.size() - 1; i > -1; i--) {
             updatingSprites.remove(updatingSpritesToRemove.remove(i));
         }
         for (int i = updatingSpritesToAdd.size() - 1; i > -1; i--) {
             updatingSprites.add(updatingSpritesToAdd.remove(i));
         }
     }
 
     /**
      * Manages cameras on the Cameras list.
      */
     private final void manageCameras() {
         //remove old Cameras
         for (int i = camerasToRemove.size() - 1; i > -1; i--) {
             Camera camera = camerasToRemove.remove(i);
             cameras.remove(camera);
             for (int n = 0; n < intersectingSprites.size(); n++) {
                 camera.decrementSpriteUsage(intersectingSprites.get(n));
             }
         }
         //add new Cameras
         for (int i = camerasToAdd.size() - 1; i > -1; i--) {
             Camera camera = camerasToAdd.remove(i);
             cameras.add(camera);
             for (int n = 0; n < intersectingSprites.size(); n++) {
                 camera.addSprite(intersectingSprites.get(n));
             }
         }
     }
 }
