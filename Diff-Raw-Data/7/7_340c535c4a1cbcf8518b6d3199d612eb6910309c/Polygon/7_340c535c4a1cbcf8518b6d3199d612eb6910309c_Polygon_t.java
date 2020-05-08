 /*
  *  BlinzCore - core library of audio, video, and other essential classes.
  *  Copyright (C) 2009  BlinzProject <gtalent2@gmail.com>
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
 package org.blinz.graphics;
 
 import java.util.Vector;
 import org.blinz.util.Position;
 
 /**
  * Polygon stores a series of points, each being connected to the previous with
  * the exception of the last one which is connected to the first.
  * @author Blinz Project
  */
 public final class Polygon {
 
     /**
      * List storing points on the polygon.
      */
     private Vector<Position> points = new Vector<Position>();
 
     /**
     * Adds a copy of the specified point to the last spot on the list.
      * @param loc
      */
     public final void addPoint(Position loc) {
         points.add(new Position(loc));
     }
 
     /**
      * Adds the specified point to the last spot on the list.
      * @param x
      * @param y
      */
     public final void addPoint(int x, int y) {
         points.add(new Position(x, y));
     }
 
     /**
     * Adds the specified point to the last spot on the list by reference.
      * @param loc
      */
     public final void addPointByReference(Position loc) {
         points.add(loc);
     }
 
     /**
      * Gests the point at the specified index.
      * @param index
      * @return the point at index
      */
     public final Position get(int index) {
         return points.get(index);
     }
 
     /**
      * Returns the number of points in the Polygon.
      * @return int - number of points in the Polygon.
      */
     public final int size() {
         return points.size();
     }
 }
