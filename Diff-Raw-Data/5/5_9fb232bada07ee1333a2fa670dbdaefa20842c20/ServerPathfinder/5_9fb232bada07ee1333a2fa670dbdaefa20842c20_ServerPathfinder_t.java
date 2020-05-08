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
 package de._13ducks.cor.game.server;
 
 import de._13ducks.cor.game.FloatingPointPosition;
 import de._13ducks.cor.game.SimplePosition;
 import de._13ducks.cor.game.server.movement.Edge;
 import de._13ducks.cor.game.server.movement.FreePolygon;
 import de._13ducks.cor.game.server.movement.MovementMap;
 import java.util.*;
 import org.apache.commons.collections.buffer.PriorityBuffer;
 import de._13ducks.cor.game.server.movement.Node;
 
 /**
  * Der Serverpathfinder.
  * Sucht Wege zwischen Knoten (Nodes)
  * Um einen echten Weg zu bekommen, muss der Weg danach noch überarbeitet werden, aber das ist nicht Aufgabe des
  * Pathfinders.
  * @author tfg
  */
 public final class ServerPathfinder {
 
     /**
      * Niemand kann einen Pathfinder erstellen, dies ist eine Utilityclass
      */
     private ServerPathfinder() {
     }
 
     public static synchronized List<Node> findPath(Node startNode, Node targetNode) {
 
 
 
         if (startNode == null || targetNode == null) {
             System.out.println("FixMe: SPathfinder, irregular call: " + startNode + "-->" + targetNode);
             return null;
         }
 
         if (startNode.equals(targetNode)) {
             return new ArrayList<Node>();
         }
 
         PriorityBuffer open = new PriorityBuffer();      // Liste für entdeckte Knoten
         LinkedHashSet<Node> containopen = new LinkedHashSet<Node>();  // Auch für entdeckte Knoten, hiermit kann viel schneller festgestellt werden, ob ein bestimmter Knoten schon enthalten ist.
         LinkedHashSet<Node> closed = new LinkedHashSet<Node>();    // Liste für fertig bearbeitete Knoten
 
         double cost_t = 0;    //Movement Kosten (gerade 5, diagonal 7, wird später festgelegt)
 
         startNode.setCost(0);   //Kosten für das Startfeld (von dem aus berechnet wird) sind natürlich 0
         open.add(startNode);  //Startfeld in die openlist
         containopen.add(startNode);
         targetNode.setParent(null);    //"Vorgängerfeld" vom Zielfeld noch nicht bekannt
 
         for (int j = 0; j < 40000; j++) {		//Anzahl der maximalen Durchläufe, bis Wegfindung aufgibt
 
             if (open.isEmpty()) {   //Abbruch, wenn openlist leer ist => es gibt keinen Weg
                 return null;
             }
 
             // Sortieren nicht mehr nötig, PriorityBuffer bewahrt die Felder in der Reihenfolge ihrer Priority - also dem F-Wert auf
             Node current = (Node) open.remove();		//der Eintrag aus der openlist mit dem niedrigesten F-Wert rausholen und gleich löschen
             containopen.remove(current);
             if (current.equals(targetNode)) {	//Abbruch, weil Weg von Start nach Ziel gefunden wurde
                 targetNode.setParent(current.getParent());   //"Vorgängerfeld" von Ziel bekannt
                 break;
             }
 
             // Aus der open wurde current bereits gelöscht, jetzt in die closed verschieben
             closed.add(current);
 
             List<Node> neighbors = current.getReachableNodes();
 
             for (Node node : neighbors) {
 
                 if (closed.contains(node)) {
                     continue;
                 }
 
                 // Kosten dort hin berechnen
                 cost_t = current.movementCostTo(node);
 
                 if (containopen.contains(node)) {         //Wenn sich der Knoten in der openlist befindet, muss berechnet werden, ob es einen kürzeren Weg gibt
 
                     if (current.getCost() + cost_t < node.getCost()) {		//kürzerer Weg gefunden?
 
                         node.setCost(current.getCost() + cost_t);         //-> Wegkosten neu berechnen
                         node.setValF(node.getCost() + node.getHeuristic());  //F-Wert, besteht aus Wegkosten vom Start + Luftlinie zum Ziel
                         node.setParent(current); //aktuelles Feld wird zum Vorgängerfeld
                     }
                 } else {
                     node.setCost(current.getCost() + cost_t);
                     node.setHeuristic((Math.abs((targetNode.getX() - node.getX())) + Math.abs((targetNode.getY() - node.getY()))) * 3);	// geschätzte Distanz zum Ziel
                     //Die Zahl am Ende der Berechnung ist der Aufwand der Wegsuche
                     //5 ist schnell, 4 normal, 3 dauert lange
 
                     node.setParent(current);						// Parent ist die RogPosition, von dem der aktuelle entdeckt wurde
                     node.setValF(node.getCost() + node.getHeuristic());  //F-Wert, besteht aus Wegkosten vom Start aus + Luftlinie zum Ziel
                     open.add(node);    // in openlist hinzufügen
                     containopen.add(node);
                 }
 
 
             }
         }
 
         if (targetNode.getParent() == null) {		//kein Weg gefunden
             return null;
         }
 
         ArrayList<Node> pathrev = new ArrayList();   //Pfad aus parents erstellen, von Ziel nach Start
         while (!targetNode.equals(startNode)) {
             pathrev.add(targetNode);
             targetNode = targetNode.getParent();
         }
         pathrev.add(startNode);
 
         ArrayList<Node> path = new ArrayList();	//Pfad umkehren, sodass er von Start nach Ziel ist
         for (int k = pathrev.size() - 1; k >= 0; k--) {
             path.add(pathrev.get(k));
         }
 
         return path;					//Pfad zurückgeben
     }
 
     public static List<SimplePosition> optimizePath(List<Node> path, SimplePosition startPos, SimplePosition endPos, MovementMap moveMap) {
         // Besseres, iteratives Vorgehen
         // Vorbereitungen: Der Weg muss Start und Ziel beinhalten
         path.add(0, startPos.toNode());
        // Ende muss seinen Polygon kennen:
        Node endNode = endPos.toNode();
        endNode.addPolygon(moveMap.containingPoly(endNode.x(), endNode.y()));
        path.add(endNode);
 
         FreePolygon startPolygon = moveMap.containingPoly(startPos.x(), startPos.y());
 
         boolean improved = true;
 
         while (improved) {
             improved = false;
 
             FreePolygon currentPoly = startPolygon;
 
             // Weg durchgehen
 
             for (int i = 1; i < path.size() - 1; i++) {
                 Node pre = path.get(i - 1);
                 Node cur = path.get(i);
                 Node nxt = path.get(i + 1);
 
                 // Testweise Kante zwischen pre und nxt erstellen
 
                 Edge edge = new Edge(pre, nxt);
 
                 // Im Folgenden wird untersucht, ob der neue Weg "edge" passierbar ist.
                 // Eventuell müssen für Polygonwechsel neue Nodes eingefügt werden
 
                 LinkedList<Node> extraNodes = new LinkedList<Node>();
                 // Damit wir beim Dreieckwechsel nicht wieder zurück gehen:
                 Node lastNode = null;
 
                 boolean routeAllowed = true;
 
                 // Jetzt so lange weiter laufen, bis wir im Ziel-Polygon sind
                 while (!nxt.getPolygons().contains(currentPoly)) {
                     // Untersuchen, ob es eine Seite des currentPolygons gibt, die sich mit der alternativRoute schneidet
                     List<Edge> edges = currentPoly.calcEdges();
                     Edge intersecting = null;
                     for (Edge testedge : edges) {
                         // Gibts da einen Schnitt?
                         SimplePosition intersection = edge.intersectionWithEdgeNotAllowed(testedge);
                         if (intersection != null && !intersection.equals(lastNode)) {
                             intersecting = testedge;
                             break;
                         }
                     }
                     // Kandidat für den nächsten Polygon
                     FreePolygon nextPoly = null;
                     // Kante gefunden
                     if (intersecting != null) {
                         // Von dieser Kante die Enden suchen
                         nextPoly = getOtherPoly(intersecting.getStart(), intersecting.getEnd(), currentPoly);
                     }
                     if (intersecting != null && nextPoly != null) {
                         // Wir haben einen Schnittpunkt und eine Kante gefunden, sind jetzt also in einem neuen Polygon
                         // Extra Node einfügen
                         Node extraNode = intersecting.intersectionWithEdgeNotAllowed(edge).toNode();
 
                         if (extraNode.equals(cur)) {
                             // Abbruch, das ist eine Gerade, hier kann man nicht abkürzen!
                             FreePolygon currentCand = commonSector(cur, nxt);
                             if (currentCand != null) {
                                 currentPoly = currentCand;
                             }
                             routeAllowed = false;
                             break;
                         }
 
                         extraNode.addPolygon(nextPoly);
                         extraNode.addPolygon(currentPoly);
                         extraNodes.add(extraNode);
                         lastNode = extraNode;
                         currentPoly = nextPoly;
                         // Der nächste Schleifendurchlauf wird den nächsten Polygon untersuchen
                     } else {
                         // Es gab leider keinen betretbaren Polygon hier.
                         // Das bedeutet, dass wir die Suche abbrechen können, der derzeit untersuchte Wegpunkt (cur)
                         // Ist also unverzichtbar.
                         // Es soll also der nächste Punkt untersucht werden, also die for einfach weiter laufen
                         // Eventuell muss aber der currentPoly geändert werden.
                         // CurrentPoly ändern, wenn in neuem Sektor:
                         FreePolygon currentCand = commonSector(cur, nxt);
                         if (currentCand != null) {
                             currentPoly = currentCand;
                         }
                         routeAllowed = false;
                         break;
                     }
 
                 }
 
                 // Wenn der neue Weg gültig war, einbauen. Sonst weiter mit dem nächsten Knoten
                 if (routeAllowed) {
                     // Den ursprünglichen Knoten löschen und die neuen Einbauen
                     path.remove(i);
                     path.addAll(i, extraNodes);
                     // Der Weg wurde geändert, die for muss neu starten
                     improved = true;
                     break;
                 }
 
                 // Wenn wir hier hinkommen, soll der nächste Knoten getestet werden.
                 extraNodes.clear();
             }
 
 
         }
 
         // Hier ist der Weg fertig optimiert
         // Start wieder löschen und zurückgeben
         path.remove(0);
 
         // Das Sektorsystem unterscheidet strikt zwischen SimplePosition und Node, deshalb die letzte durch eine Simple ersetzten
         Node last = path.remove(path.size() - 1);
 
         LinkedList<SimplePosition> retList = new LinkedList<SimplePosition>();
         for (Node n : path) {
             retList.add(n);
         }
 
         // Jetzt wieder einfügen
         retList.add(new FloatingPointPosition(last.x(), last.y()));
         return retList;
     }
 
     private static FreePolygon getOtherPoly(Node n1, Node n2, FreePolygon myself) {
         for (FreePolygon poly : n1.getPolygons()) {
             if (poly.equals(myself)) {
                 continue;
             }
             if (n2.getPolygons().contains(poly)) {
                 return poly;
             }
         }
         return null;
     }
 
     /**
      * Findet einen Sektor, den beide Knoten gemeinsam haben
      * @param n1 Knoten 1
      * @param n2 Knoten 2
      */
     private static FreePolygon commonSector(Node n1, Node n2) {
         for (FreePolygon poly : n1.getPolygons()) {
             if (n2.getPolygons().contains(poly)) {
                 return poly;
             }
         }
         return null;
     }
 }
