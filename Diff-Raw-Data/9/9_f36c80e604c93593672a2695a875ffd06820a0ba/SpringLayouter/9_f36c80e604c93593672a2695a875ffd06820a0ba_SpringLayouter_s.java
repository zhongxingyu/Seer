 /*
  * SpringLayouter.java
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
  */
 package com.seanmadden.graphing.layouts;
 
 import java.util.*;
 
 import com.seanmadden.graphing.struct.*;
 
 public class SpringLayouter implements Layout, GraphListener {
 
         private Hashtable<Node, Double> xForces = new Hashtable<Node, Double>();
 
         private Hashtable<Node, Double> yForces = new Hashtable<Node, Double>();
 
         private double idealEdgeLength = 100;
 
         private double maxMove = 10;
 
         private boolean graphChanged = true;
 
         private Graph lastLayout = null;
 
         public void doLayout(Graph g) {
                 // if(!graphChanged && g == lastLayout) return;
                 // lastLayout = g;
 
                 Collection<Node> nodes = g.getAllNodes();
 
                 HashSet<Node> hit = new HashSet<Node>();
 
                 for (Node src : nodes) {
                         hit.add(src);
                         for (Node dst : nodes) {
                                 if (hit.contains(dst)) continue;
                                 repulsiveForce(src, dst);
                                 // System.out.println(src + "=>" + dst);
                                 // System.out.print("x: " + xForces.get(dst));
                                 // System.out.println(" y: " +
                                 // yForces.get(dst));
                         }
                 }
 
                 for (Edge ed : g.getAllEdges()) {
                         // attractiveForce(ed.getSource(), ed.getDestination());
                 }
                 double fx, fy;
                 for (Node nd : g.getAllNodes()) {
                         fx = xForces.get(nd);
                         fy = yForces.get(nd);
                         nd.getPoint().x += Math.abs(fx)
                                         / fx
                                         * ((Math.abs(fx) > maxMove) ? maxMove
                                                         : fx);
                         nd.getPoint().y += Math.abs(fy)
                                         / fy
                                         * ((Math.abs(fy) > maxMove) ? maxMove
                                                         : fy);
 
                         // System.out.println(nd.getPoint());
                 }
 
                 xForces.clear();
                 yForces.clear();
                 System.out.println("emptied.");
 
         }
 
         public boolean isLayoutable(Graph g) {
                 return false;
         }
 
         private double distance2(Node a, Node b) {
                 return (b.getPoint().x - a.getPoint().x)
                                 + +(b.getPoint().y - a.getPoint().y);
         }
 
         private double distance3(Node a, Node b) {
                 return distance2(a, b) * distance(a, b);
         }
 
         private double distance(Node a, Node b) {
                 return Math.sqrt(distance2(a, b));
         }
 
         private void repulsiveForce(Node a, Node b) {
                 double dx = a.getPoint().x - b.getPoint().x;
                 double dy = a.getPoint().y - b.getPoint().y;
                 dx /= distance2(a, b);
                 dy /= distance2(a, b);
 
                 System.out.println("dx: " + dx + " dy:" + dy);
 
                 xForceAdd(a, dx);
                 yForceAdd(a, dy);
                 xForceAdd(b, -dx);
                 yForceAdd(b, -dy);
 
         }
 
         private void attractiveForce(Node a, Node b) {
                 double dx = a.getPoint().x - b.getPoint().x;
                 double dy = a.getPoint().y - b.getPoint().y;
                 System.out.println();
 
                 final double ratio = (distance(a, b) - idealEdgeLength)
                                 / distance(a, b);
 
                 dx *= 2 * ratio;
                 dy *= 2 * ratio;
 
                 // System.out.println("dx: " + -dx + " dy: " + -dy);
                 // System.out.println(ratio);
                 //                
                 xForceAdd(a, dx);
                 yForceAdd(a, dy);
                 xForceAdd(b, -dx);
                 yForceAdd(b, -dy);
         }
 
         private double xForceAdd(Node a, double x) {
                 if (xForces.contains(a)) {
                         x += xForces.get(a);
                 }
                 xForces.put(a, x);
                 return x;
         }
 
         private double yForceAdd(Node a, double y) {
                 if (yForces.contains(a)) {
                         y += yForces.get(a);
                 }
                 yForces.put(a, y);
                 return y;
         }
 
        @Override
         public void triggerChange(Graph g, Change change) {
                 if (g == lastLayout) {
                         graphChanged = true;
                 } else {
                         lastLayout = null;
                         graphChanged = true;
                 }
         }
 
 }
