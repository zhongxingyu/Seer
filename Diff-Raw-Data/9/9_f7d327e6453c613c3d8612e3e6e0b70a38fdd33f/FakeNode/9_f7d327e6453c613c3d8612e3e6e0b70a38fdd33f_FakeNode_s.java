 /*
  *  Copyright 2008, 2009, 2010, 2011:
  *   Tobias Fleig (tfg[AT]online[DOT]de),
  *   Michael Haas (mekhar[AT]gmx[DOT]de),
  *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
  *
  *  - All rights reserved -
  *
  *
  *  This file is part of Centuries of Rage.
  *
  *  Centuries of Rage is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Centuries of Rage is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package de._13ducks.cor.game.server.movement;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Eine spezielle Klasse, die sich wie ein Node verhält.
  * Wird temporär in den Pathfinder-Pfadsuchbaum eingebaut damit der Pathfinder den Start- und Zielknoten richtig berücksichtigen kann.
  */
 public class FakeNode extends Node {
 
     private List<Node> fakeNeighbors;
     private FreePolygon myPoly;
 
     /**
      * Erzeugt einen neuen Fakenode mit den gegebenen Koordinaten
      * @param x
      * @param y 
      */
     public FakeNode(double x, double y, FreePolygon poly) {
         super(x, y);
         this.myPoly = poly;
         fakeNeighbors = new LinkedList<Node>();
     }
 
     @Override
     public List<Node> getReachableNodes() {
         return fakeNeighbors;
     }
     
     public void setReachableNodes(Node... nodes) {
        fakeNeighbors = Arrays.asList(nodes);
     }
 
     @Override
     public List<FreePolygon> getPolygons() {
         LinkedList<FreePolygon> list = new LinkedList<FreePolygon>();
         list.add(myPoly);
         return list;
     }
 }
