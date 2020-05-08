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
 
 import de._13ducks.cor.game.SimplePosition;
 import java.util.*;
 import org.apache.commons.collections.buffer.PriorityBuffer;
 
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
 
     public static synchronized List<Node> findPath(SimplePosition start, SimplePosition target, FreePolygon startSector, MovementMap moveMap) {
 
         if (start == null || target == null) {
             System.out.println("FixMe: SPathfinder, irregular call: " + start + "-->" + target);
             return null;
         }
 
         FreePolygon targetSector = moveMap.containingPoly(target.x(), target.y());
 
         if (targetSector == null) {
             // Ziel ungültig abbrechen
             System.out.println("Irregular target. Aborting");
             return null;
         }
         FakeNode startNode = new FakeNode(start.x(), start.y(), startSector);
         Node targetNode = new FakeNode(target.x(), target.y(), targetSector);
         targetNode.addPolygon(targetSector);
 
         // Der Startknoten muss die Member seines Polys kennen
         startNode.setReachableNodes(computeDirectReachable(startNode, startSector));
         // Der Zielknoten muss den Membern seines Polys bekannt sein
         // Die Movement-Map darf aber nicht verändert werden. Des halb müssen einige Aufrufe intern abgefangen werden und das reingedoktert werden.
         List<Node> preTargetNodes = Arrays.asList(computeDirectReachable(targetNode, targetSector));
 
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
 
             List<Node> neighbors = computeNeighbors(current, targetNode, preTargetNodes);
 
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
                     node.setHeuristic(Math.sqrt(Math.pow(Math.abs((targetNode.getX() - node.getX())), 2) + Math.pow(Math.abs((targetNode.getY() - node.getY())), 2)));	// geschätzte Distanz zum Ziel
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
 
         ArrayList<Node> pathrev = new ArrayList<Node>();   //Pfad aus parents erstellen, von Ziel nach Start
         while (!targetNode.equals(startNode)) {
             pathrev.add(targetNode);
             targetNode = targetNode.getParent();
         }
         pathrev.add(startNode);
 
         ArrayList<Node> path = new ArrayList<Node>();	//Pfad umkehren, sodass er von Start nach Ziel ist
         for (int k = pathrev.size() - 1; k >= 0; k--) {
             path.add(pathrev.get(k));
         }
 
         /**
          * An dieser Stelle muss der Weg nocheinmal überarbeitet werden.
          * Es kann nämlich durch neue Tweaks sein, dass dies die Knoten nicht direkt
          * verbunden sind (also keinen gemeinsamen Polygon haben)
          * Das tritt z.B. bei der Start- und Zieleinsprungpunkt-Variierung auf.
          */
         for (int i = 0; i < path.size() - 1; i++) {
             Node n1 = path.get(i);
             Node n2 = path.get(i + 1);
             FreePolygon commonSector = commonSector(n1, n2);
            if (commonSector != null) {
                 // Das hier ist der interessante Fall, die beiden Knoten sind nicht direkt verbunden, es muss ein Zwischenknoten eingefügt werden:
                 // Dessen Punkt suchen
                 Edge direct = new Edge(n1, n2);
                 Node newNode = null;
                 // Die Polygone von n1 durchprobieren
                 for (FreePolygon currentPoly : n1.getPolygons()) {
                     List<Edge> edges = currentPoly.calcEdges();
                     for (Edge testedge : edges) {
                         // Gibts da einen Schnitt?
                         SimplePosition intersection = direct.intersectionWithEndsNotAllowed(testedge);
                         if (intersection != null) {
                             // Kandidat für den nächsten Polygon
                             FreePolygon nextPoly = null;
                             // Kante gefunden
                             // Von dieser Kante die Enden suchen
                             nextPoly = getOtherPoly(testedge.getStart(), testedge.getEnd(), currentPoly);
 
                             newNode = intersection.toNode();
                             newNode.addPolygon(currentPoly);
                             newNode.addPolygon(nextPoly);
                             break;
                         }
                     }
                     if (newNode != null) {
                         break;
                     }
                 }
 
                 if (newNode == null) {
                     // Das dürfte nicht passieren, der Weg ist legal gefunden worden, muss also eigentlich existieren
                     System.out.println("[Pathfinder][ERROR]: Cannot insert Nodes into route, aborting!");
                     return null;
                 } else {
                     path.add(i + 1, newNode);
                 }
             }
         }
 
 
 
         return path;					//Pfad zurückgeben
     }
 
     /**
      * Der Start und Zielknoten sind von weit mehr als nur den Knoten ihres Polygons erreichbar.
      * Dies muss bereits während der Basic-Berechnung beachtet werden, das kann die Pfadoptimierung nachträglich nichtmehr leisten.
      * Also alle Nodes suchen, die ohne Hinderniss direkt erreichbar sind
      * @param from alle vollständig im Mesh liegenden Kanten von hier zu Nachbarknoten suchen
      * @param basicPolygon Der Polygon, in dem der Node drinliegt.
      * @return alle direkt erreichbaren Knoten (natürlich sind die des basicPolygons auch dabei)
      */
     private static Node[] computeDirectReachable(Node from, FreePolygon basicPolygon) {
         // Das ist eine modifizierte Breitensuche:
         LinkedList<FreePolygon> open = new LinkedList<FreePolygon>(); // Queue für zu untersuchende Polygone
         LinkedHashSet<FreePolygon> openContains = new LinkedHashSet<FreePolygon>(); // Welche Elemente die open enthält (schnellerer Test)
         LinkedHashSet<FreePolygon> closed = new LinkedHashSet<FreePolygon>();
         LinkedHashSet<Node> testedNodes = new LinkedHashSet<Node>();
         LinkedList<Node> result = new LinkedList<Node>();
         open.offer(basicPolygon); // Start-Polygon
         openContains.add(basicPolygon);
 
         while (!open.isEmpty()) {
             // Diesen hier bearbeiten wir jetzt
             FreePolygon poly = open.poll();
             openContains.remove(poly);
             closed.add(poly);
 
             boolean containsreachableNodes = false;
             // Alle Nodes dieses Knotens untersuchen
             for (Node node : poly.getNodes()) {
                 // Schon bekannt?
                 if (result.contains(node)) {
                     // Bekannt und ok
                     containsreachableNodes = true;
                 } else {
                     if (testedNodes.contains(node)) {
                         // Der geht nicht
                     } else {
                         // Testen!
                         FreePolygon currentPoly = basicPolygon;
                         // Testweise Kante zwischen from und node erstellen
                         Edge edge = new Edge(from, node);
                         // Im Folgenden wird untersucht, ob der neue Weg "edge" passierbar ist.
                         // Damit wir beim Dreieckwechsel nicht wieder zurück gehen:
                         Node lastNode = null;
 
                         boolean routeAllowed = true;
 
                         // Jetzt so lange weiter laufen, bis wir im Ziel-Polygon sind
                         while (!node.getPolygons().contains(currentPoly)) {
                             // Untersuchen, ob es eine Seite des currentPolygons gibt, die sich mit der alternativRoute schneidet
                             List<Edge> edges = currentPoly.calcEdges();
                             Edge intersecting = null;
                             for (Edge testedge : edges) {
                                 // Gibts da einen Schnitt?
                                 SimplePosition intersection = edge.intersectionWithEndsNotAllowed(testedge);
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
                                 // Extra Node benötigt
                                 Node extraNode = intersecting.intersectionWithEndsNotAllowed(edge).toNode();
 
                                 extraNode.addPolygon(nextPoly);
                                 extraNode.addPolygon(currentPoly);
                                 lastNode = extraNode;
                                 currentPoly = nextPoly;
                                 // Der nächste Schleifendurchlauf wird den nächsten Polygon untersuchen
                             } else {
                                 // Es gab leider keinen betretbaren Polygon hier.
                                 // Das bedeutet, dass wir die Suche abbrechen können, es gibt hier keinen direkten Weg
                                 routeAllowed = false;
                                 break;
                             }
 
                         }
 
                         // Wenn der neue Weg gültig war, einbauen. Sonst weiter mit dem nächsten Knoten
                         if (routeAllowed) {
                             // In die erlaubt-Liste:
                             result.add(node);
                             testedNodes.add(node);
                             containsreachableNodes = true;
                         } else {
                             testedNodes.add(node);
                         }
                     }
                 }
             }
 
             // Nur weiter in die Tiefe gehen, wenn mindestens einer erreichbar war
             if (containsreachableNodes) {
                 // Alle Nachbarn untersuchen:
                 for (FreePolygon n : poly.getNeighbors()) {
                     // Schon bekannt/bearbeitet?
                     if (!openContains.contains(n) && !closed.contains(n)) {
                         // Nein, also auch zur Bearbeitung vorsehen
                         open.add(n);
                         openContains.add(n);
                     }
                 }
             }
         }
         return result.toArray(new Node[0]);
     }
 
     private static List<Node> computeNeighbors(Node current, Node target, List<Node> preTargetNodes) {
         List<Node> originalNodes = current.getReachableNodes();
         if (preTargetNodes.contains(current)) {
             originalNodes.add(target);
         }
         return originalNodes;
     }
 
     public static List<SimplePosition> optimizePath(List<Node> path, SimplePosition startPos, SimplePosition endPos, MovementMap moveMap) {
         // Besseres, iteratives Vorgehen
 
         FreePolygon startPolygon = moveMap.containingPoly(startPos.x(), startPos.y());
         if (startPolygon == null) {
             System.out.println("ERROR! Target unreachable (no poly found)");
             return null;
         }
 
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
                         SimplePosition intersection = edge.intersectionWithEndsNotAllowed(testedge);
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
                         Node extraNode = intersecting.intersectionWithEndsNotAllowed(edge).toNode();
 
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
 
         LinkedList<SimplePosition> retList = new LinkedList<SimplePosition>();
         for (Node n : path) {
             retList.add(n);
         }
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
