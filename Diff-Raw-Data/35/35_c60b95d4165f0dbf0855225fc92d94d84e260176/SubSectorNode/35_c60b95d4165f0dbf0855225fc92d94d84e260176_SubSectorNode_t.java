 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de._13ducks.cor.game.server.movement;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class SubSectorNode implements Comparable<SubSectorNode> {
 
     /**
      * Koordinaten
      */
     private double x;
     /**
      * Koordinaten
      */
     private double y;
     /**
      * Auf wessen Laufkreis liegt dieses Hinderniss?
      */
     private ArrayList<SubSectorObstacle> myObstacle;
     /**
      * Kanten zu anderen Knoten
      */
     private ArrayList<SubSectorEdge> myEdges;
     /**
      * Die "Kosten" eines Weges. Siehe Pathfinder
      */
     private double cost;
     /**
      * Der "Vorgänger" dieses Knotens auf einem Weg. Siehe Pathfinder
      */
     private SubSectorNode parent;
 
     SubSectorNode(double x, double y, SubSectorObstacle... owner) {
         this.x = x;
         this.y = y;
         myObstacle = new ArrayList<SubSectorObstacle>();
         myObstacle.addAll(Arrays.asList(owner));
         myEdges = new ArrayList<SubSectorEdge>();
     }
 
     /**
      * Fügt die Kante zu diesem Knoten hinzu
      * Fall sie schon bekannt ist passiert nichts.
      * @param edge die neue Kante
      */
     void addEdge(SubSectorEdge edge) {
         if (!myEdges.contains(edge)) {
             myEdges.add(edge);
         }
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 37 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
         hash = 37 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
         return hash;
     }
 
     @Override
     public boolean equals(Object o) {
         if (o instanceof SubSectorNode) {
             SubSectorNode node = (SubSectorNode) o;
             if (Math.abs(x - node.x) < 0.001 && Math.abs(y - node.y) < 0.001) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Findet in einem Knoten mit 2 Hindernissen das andere heraus
      * (also nicht das, das übergeben wurde)
      * @param obst
      * @return 
      */
     SubSectorObstacle otherObstacle(SubSectorObstacle obst) {
         if (myObstacle.get(0).equals(obst)) {
             return myObstacle.get(1);
         } else {
             return myObstacle.get(0);
         }
     }
 
     /**
      * @return the x
      */
     double getX() {
         return x;
     }
 
     /**
      * @return the y
      */
     double getY() {
         return y;
     }
 
     /**
      * @return the myEdges
      */
     ArrayList<SubSectorEdge> getMyEdges() {
         return myEdges;
     }
 
     /**
      * @return the cost
      */
     double getCost() {
         return cost;
     }
 
     /**
      * @param cost the cost to set
      */
     void setCost(double cost) {
         this.cost = cost;
     }
 
     /**
      * @return the parent
      */
     SubSectorNode getParent() {
         return parent;
     }
 
     /**
      * @param parent the parent to set
      */
     void setParent(SubSectorNode parent) {
         this.parent = parent;
     }
 
     /**
      * Sucht die Kante zu angegeben Ziel raus und löscht sie.
      * @param next das Ziel
      */
     void removeEdgeTo(SubSectorNode next) {
         for (SubSectorEdge edge : myEdges) {
             if (edge.getOther(this).equals(next)) {
                 myEdges.remove(edge);
                 break;
             }
         }
     }
 
     @Override
     public int compareTo(SubSectorNode o) {
         if (o.cost > cost) {
             return -1;
         } else if (o.cost < cost) {
             return 1;
         } else {
             return 0;
         }
     }
 }
