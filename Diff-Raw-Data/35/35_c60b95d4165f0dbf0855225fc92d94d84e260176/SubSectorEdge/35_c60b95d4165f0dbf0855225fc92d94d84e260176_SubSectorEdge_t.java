 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de._13ducks.cor.game.server.movement;
 
 public class SubSectorEdge {
 
     /**
      * Die beiden Knoten.
      */
     private SubSectorNode n1, n2;
     /**
      * Die Länge dieser Kante ("das Kantengewicht")
      */
     private double length;
 
     SubSectorEdge(SubSectorNode n1, SubSectorNode n2, double lenght) {
         this.n1 = n1;
         this.n2 = n2;
         this.length = lenght;
     }
 
     /**
      * Findet die Gegenseite der Linie. Zum Beispiel, wenn man entlanglaufen will,
      * ruft man getOther(meinKnoten) auf, und bekommt das Ziel, wo man von
      * meinKnoten mit dieser Kante hinkommt.
      * ACHTUNG: Der übergebene Knoten muss Teil dieser Kante sein!!!
      * @param thisOne
      * @return
      */
     SubSectorNode getOther(SubSectorNode thisOne) {
         if (n1.equals(thisOne)) {
             return n2;
         } else {
             return n1;
         }
     }

    /**
     * Liefert die Länge dieser Kante
     * @return die Länge dieser Kante
     */
    double getLength() {
        return length;
    }
 }
