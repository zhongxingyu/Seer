 /*
  * BufferedLayouter.java
  * 
  * Copyright (C) 2008 Sean P Madden
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * If you would like to license this code under the GNU LGPL, please see
  * http://www.seanmadden.net/licensing for details.
  * 
  */
 package com.seanmadden.graphing.layouts;
 
 import java.util.Vector;
 
 import com.seanmadden.graphing.struct.Graph;
 
 /**
  * This class acts as a iterated layouter in that it does nothing else but run
  * other layouts in sequence.
  *
  * @author Sean P Madden
  */
 public class BufferedLayouter implements Layout {
     /**
      * 
      */
     private Vector<Layout> layouts = new Vector<Layout>();
 
     /**
     * [Place method description here]
     * 
     * @param l the layout to add
     */
     public final void addLayout(final Layout l) {
         if (!layouts.contains(l)) {
             layouts.add(l);
         }
     }
 
     /**
     * [Place method description here]
     * 
     * @param l the layout to remove
     * @return whether we can remove the layout
     */
     public final boolean removeLayout(final Layout l) {
         return layouts.remove(l);
     }
 
     /**
      * [Place method description here]
      *
      * @param g the graph we're layouting
      */
     public final void doLayout(final Graph g) {
         for (Layout l : layouts) {
             l.doLayout(g);
         }
     }
 
     public boolean isLayoutable(Graph g) {
         for (Layout l : layouts) {
             if (!l.isLayoutable(g)) { return false; }
         }
         return true;
     }
 
 }
