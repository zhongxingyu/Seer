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
 
 import de._13ducks.cor.game.FloatingPointPosition;
 import de._13ducks.cor.game.SimplePosition;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Ein Knoten. Zwischen diesen werden Vielecke aufgespannt (FreePolygon)
  * Ein Knoten gehört dabei zu mehreren Polygonen.
  * Mit Hilfe dieser Knoten sucht ein A* den besten Weg.
  * Deshalb hat dieses Objekt auch diverse Wegfindungs-Relevante Variablen
  */
 public class Node implements Comparable<Node>, SimplePosition {
 
     /**
      * Diese Listen enthält alle Polygone, auf deren Kanten dieser Polygon liegt oder deren Ecken er markiert.
      */
     private List<FreePolygon> myPolys;
     /**
      * Die absolute x-Koordinate dieses Polygons
      */
     private final double x;
     /**
      * Die absolute y-Koordinate dieses Polygons
      */
     private final double y;
     /**
      * Die "Kosten" eines Weges. Siehe Pathfinder
      */
     private double cost;
     /**
      * Der "Vorgänger" dieses Knotens auf einem Weg. Siehe Pathfinder
      */
     private Node parent;
     /**
      * Der magische "F-Wert" dieses Knotens. Siehe Pathfinder
      */
     private double valF;
     /**
      * Der heuristik-Suchwert. Siehe Pathfinder
      */
     private double heuristic;
 
     /**
      * Erzeugt einen neuen Knoten mit den angegebenen Koordinaten.
      * Die Koordinaten dürfen nicht negativ sein, sonst gibts ne Exception!
      * @param x Die X-Koordinate
      * @param y Die Y-Koordinate
      */
     public Node(double x, double y) {
         this.x = x;
         this.y = y;
         myPolys = new ArrayList<FreePolygon>();
     }
 
     /**
      * Registriert einen Polygon bei diesem Knoten.
      * Der Knoten weiß dann in Zunkunft, dass er auf einer der Kanten dieses Polygons liegt (oder diese als Ecke aufspannt)
      * Polygon darf nicht null sein. (IllegalArgumentException)
      * Wenn dieser Node den Polygon bereits kennt passiert nichts.
      * @param poly der neue Polygon
      */
     public void addPolygon(FreePolygon poly) {
         if (poly == null) {
             throw new IllegalArgumentException("Poly must not be null!");
         }
         if (!myPolys.contains(poly)) {
             myPolys.add(poly);
         }
     }
 
     /**
      * Löscht einen registrierten Polygon wieder.
      * Sollte dieser Knoten den Polygon gar nicht kennen, passiert nichts.
      * @param poly der alte Polygon
      */
     public void removePoly(FreePolygon poly) {
         myPolys.remove(poly);
     }
 
     /**
      * Die absolute x-Koordinate dieses Polygons
      * @return the x
      */
     public double getX() {
         return x;
     }
 
     /**
      * Die absolute y-Koordinate dieses Polygons
      * @return the y
      */
     public double getY() {
         return y;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 53 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
         hash = 53 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
         return hash;
     }
 
     @Override
     public boolean equals(Object o) {
         if (o instanceof SimplePosition) {
             SimplePosition n = (SimplePosition) o;
             // Bei Fließkomma-Vergleichen immer eine Toleranz zulassen, wegen den Rundungsfehlern.
             if (Math.abs(n.x() - this.x) < 0.01 && Math.abs(n.y() - this.y) < 0.01) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public String toString() {
         return x + "/" + y;
     }
     
     /**
      * Liefert eine Vektor-Representation dieses Knotens.
      * Der Vektor enthält nur die derzeitgen Positionsdaten.
      * Alle anderen Daten wie z.B. benachbarte Polygone gehen verloren.
      * @return einen Vektor-Representation dieses Knotens.
      */
     public Vector toVector() {
         return new Vector(x, y);
     }
 
     /**
      * @return the cost
      */
     public double getCost() {
         return cost;
     }
 
     /**
      * @param cost the cost to set
      */
     public void setCost(double cost) {
         this.cost = cost;
     }
 
     /**
      * @return the parent
      */
     public Node getParent() {
         return parent;
     }
 
     /**
      * @param parent the parent to set
      */
     public void setParent(Node parent) {
         this.parent = parent;
     }
     
     
     public int compareTo(Node f) {
         if (f.getValF() > this.getValF()) {
             return -1;
         } else if (f.getValF() < getValF()) {
             return 1;
         } else {
             return 0;
         }
     }
 
     /**
      * @return the valF
      */
     public double getValF() {
         return valF;
     }
 
     /**
      * @param valF the valF to set
      */
     public void setValF(double valF) {
         this.valF = valF;
     }
 
     public List<Node> getReachableNodes() {
        LinkedList<Node> nodes = new LinkedList();
         for (FreePolygon poly : myPolys) {
             List<Node> polynodes = poly.getNodes();
             for (Node n : polynodes) {
                 if (!nodes.contains(n)) {
                     nodes.add(n);
                 }
             }
         }
         // Uns selber raus nehmen, falls drin
         nodes.remove(this);
         return nodes;
     }
 
     /**
      * Liefert die Kosten (Wegfindung, siehe dort) von diesem Knoten zu einem anderen.
      * @param node der andere
      * @return die Kosten von diesem Knoten zu einem anderen.
      */
     public double movementCostTo(Node node) {
         return Math.sqrt((x - node.x) * (x - node.x) + (y - node.y) * (y - node.y));
     }
 
     /**
      * @return the heuristic
      */
     public double getHeuristic() {
         return heuristic;
     }
 
     /**
      * @param heuristic the heuristic to set
      */
     public void setHeuristic(double heuristic) {
         this.heuristic = heuristic;
     }
 
     /**
      * Wandelt einen Node in eine FloatingPointPosition um.
      * @return eine FloatingPointPosition mit den Koordinaten dieses Knotens
      */
     public FloatingPointPosition toFPP() {
         return new FloatingPointPosition(x, y);
     }
 
     public double x() {
         return x;
     }
 
     public double y() {
         return y;
     }
 
     public Node toNode() {
         return new Node(x, y);
     }
 
     public List<FreePolygon> getPolygons() {
         return Collections.unmodifiableList(myPolys);
     }
 }
