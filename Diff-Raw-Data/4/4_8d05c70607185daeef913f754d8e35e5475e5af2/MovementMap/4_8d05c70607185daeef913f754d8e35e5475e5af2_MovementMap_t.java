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
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.CoordinateSequence;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LinearRing;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
 import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
 import de._13ducks.cor.game.Building;
 import de._13ducks.cor.game.FloatingPointPosition;
 import de._13ducks.cor.game.Moveable;
 import de._13ducks.cor.game.Position;
 import de._13ducks.cor.map.CoRMap;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import org.newdawn.slick.geom.Circle;
 
 /**
  * Stellt die Map als (ungerichteten) Graph von konvexen Vielecken (Polygonen) dar.
  */
 public class MovementMap {
 
     private List<FreePolygon> polys;
     private List<Node> nodes;
     private List<Moveable> managedMovers;
 
     /**
      * Privater Konstruktor. CreateMovementMap verwenden!
      */
     private MovementMap(CoRMap map, List<Building> blocked) {
         polys = new ArrayList<FreePolygon>();
         nodes = new ArrayList<Node>();
         managedMovers = new ArrayList<Moveable>();
 
 
         try {
 
             long time = System.currentTimeMillis();
 
             GeometryFactory fact = new GeometryFactory();
 
             // Aussen-Shape machen
             Coordinate[] outer = new Coordinate[]{new Coordinate(0, 0), new Coordinate(map.getMapSizeX(), 0), new Coordinate(0, map.getMapSizeY()), new Coordinate(map.getMapSizeX(), map.getMapSizeY()), new Coordinate(0, 0)};
             CoordinateSequence outerSeq = new CoordinateArraySequence(outer);
 
             ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
 
             for (Building building : blocked) {
                 Position[] vis = building.getVisisbilityPositions();
                 // Achtung: Bei Oben, Unten und Rechts wird was hinzugezählt, weil ja nicht der Anfangspunkt des Feldes (mitte links) gemeint ist, sondern z.B. das Ende
                 Coordinate[] loch = new Coordinate[]{new Coordinate(vis[0].getX(), vis[0].getY()), new Coordinate(vis[1].getX() + 1, vis[1].getY() - 1), new Coordinate(vis[2].getX() + 1, vis[2].getY() + 1), new Coordinate(vis[3].getX() + 2, vis[3].getY()), new Coordinate(vis[0].getX(), vis[0].getY())};
                 CoordinateSequence seq = new CoordinateArraySequence(loch);
                 holes.add(new LinearRing(seq, fact));
             }
 
             DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
 
             Polygon poly = fact.createPolygon(new LinearRing(outerSeq, fact), holes.toArray(new LinearRing[0]));
 
             builder.setSites(poly);
 
             GeometryCollection col = (GeometryCollection) builder.getTriangles(fact);
 
             for (int i = 0; i < col.getNumGeometries(); i++) {
                 Polygon cPoly = (Polygon) col.getGeometryN(i);
                 Coordinate[] coords = cPoly.getCoordinates();
                 // Schauen, ob die Nodes schon exisiteren, sonst neue nehmen
                 FreePolygon myPolygon = new FreePolygon(true, getKnownOrNew(coords[0].x, coords[0].y), getKnownOrNew(coords[1].x, coords[1].y), getKnownOrNew(coords[2].x, coords[2].y));
                 addPoly(myPolygon, holes);
             }
 
             System.out.println("Movemap calculation took: " + (System.currentTimeMillis() - time) + " ms");
 
             // Optimize Mesh - Benachbarte Dreiecke kombinieren, solange sie konvex bleiben
 
             time = System.currentTimeMillis();
 
             combineOptimize();
 
             System.out.println("Movemap optimization took: " + (System.currentTimeMillis() - time) + " ms");
 
 
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      * Erstellt eine neue MovementMap.
      * Erzeugt die interne Graphenstruktur aus konvexen Polygonen
      * @param map die CoRMap, um die es geht.
      * @param blocked alle Positionen, die nicht verwendet werden können
      * @return die fertige MovementMap
      */
     public static MovementMap createMovementMap(CoRMap map, List<Building> blocked) {
         MovementMap moveMap = new MovementMap(map, blocked);
         return moveMap;
     }
 
     /**
      * Verarbeitet die Polygone der aktuellen Movemap und versucht möglichst viele zu verbinden, solange die Verbundenen noch konvex sind.
      */
     private void combineOptimize() {
         int counter = 0;
         do {
             counter = 0;
             for (int i = 0; i < polys.size(); i++) {
                 // Für jeden Polygon alle Nachbarn durchgehen
                 FreePolygon wpoly = polys.get(i);
                 List<FreePolygon> neighbors = wpoly.getNeighbors();
                 for (int n = 0; n < neighbors.size(); n++) {
                     // Versuche Vereinigung und schaue, ob dann noch konvex
                     FreePolygon newPoly = FreePolygon.getMergedCopy(wpoly, neighbors.get(n));
                     if (newPoly.isConvex()) {
                         computeMerge(wpoly, neighbors.get(n), newPoly);
                         counter++;
                         break; //Wpoly ist anders
                     }
                 }
             }
             System.out.println("Removed " + counter + " polygons");
         } while (counter != 0);
     }
 
     /**
      * Verrechnet einen Merge.
      * Sorgt dafür, dass old1 und old2 richtig aus der moveMap rausgeworden werden und dass alle Node und neighbor-
      * Referenzen jetzt auf den neuen Zeigen
      * @param old1 Der erste alte Polygon
      * @param old2 Der zweite alte Polygon
      * @param newP Der neue Polygon
      */
     private void computeMerge(FreePolygon old1, FreePolygon old2, FreePolygon newP) {
         // Nachbarschaften umbiegen:
         for (FreePolygon poly : old1.getNeighbors()) {
             // Alten löschen, neuen Registrieren, aber nicht bei old2!
             if (poly.equals(old2)) {
                 continue;
             }
             // Ansonsten weiter:
             poly.removeNeighbor(old1);
             poly.registerNeighbor(newP);
             newP.registerNeighbor(poly);
         }
         for (FreePolygon poly : old2.getNeighbors()) {
             // Alten löschen, neuen Registrieren, aber nicht bei old2!
             if (poly.equals(old1)) {
                 continue;
             }
             // Ansonsten weiter:
             poly.removeNeighbor(old2);
             poly.registerNeighbor(newP);
             newP.registerNeighbor(poly);
         }
         // Nodes ändern
         for (Node node : old1.getNodes()) {
             node.removePoly(old1);
             node.addPolygon(newP);
         }
         for (Node node : old2.getNodes()) {
             node.removePoly(old2);
             node.addPolygon(newP);
         }
         polys.remove(old1);
         polys.remove(old2);
         polys.add(newP);
     }
 
     /**
      * Checkt, ob ein Polygon zur Liste hinzugefügt werden darf, verwaltet Nachbarschaftsbeziehungen und fügt ihn schließlich hinzu
      * @param poly ein neuer Polygon
      */
     private void addPoly(FreePolygon poly, List<LinearRing> blocked) {
         // Alle Blockierten Bereiche durchgehen, wenn der neue Polygon drei (oder mehr) Polygone mit einem gemeinsam ist ist es ein Loch
         List<Node> pnodes = poly.getNodes();
         for (LinearRing ring : blocked) {
             int shared = 0;
             Coordinate[] coords = ring.getCoordinates();
             for (int i = 0; i < coords.length - 1; i++) { // Nur bis length - 1, der Startpunkt ist zweimal drin
                 Coordinate coord = coords[i];
                 if (pnodes.contains(new Node(coord.x, coord.y))) {
                     shared++;
                 }
             }
             if (shared >= 3) {
                // Verweis auf diesen Polygon aus den Nodes löschen
                for (Node node : poly.getNodes()) {
                    node.removePoly(poly);
                }
                 return; // Geht nicht, Loch
             }
 
         }
         // Nachbarn suchen
         for (FreePolygon freePoly : polys) {
             // Als Nachbar eintragen?
             if (freePoly.isNeighbor(poly)) {
                 freePoly.registerNeighbor(poly);
                 poly.registerNeighbor(freePoly);
             }
         }
         // Jetzt adden
         polys.add(poly);
     }
 
     /**
      * Schaut nach, ob der Knoten bereits bekannt ist. Wenn nicht, wird ein neuer angelegt.
      * @param x Die X-Koordinate des zu suchenden Knoten
      * @param y Die Y-Koordinate des zu suchenden Knoten
      * @return Der neue Knoten, entweder aus der Datenbank oder ein ganz neuer
      */
     private Node getKnownOrNew(double x, double y) {
         int index = nodes.indexOf(new Node(x, y));
         Node newnode = null;
         if (index != -1) {
             newnode = nodes.get(index);
         } else {
             newnode = new Node(x, y);
         }
         nodes.add(newnode);
         return newnode;
     }
 
     public List<FreePolygon> getPolysForDebug() {
         return Collections.unmodifiableList(polys);
     }
 
     /**
      * Ordnet der Einheit (einmalig) einen Sektor zu.
      * Wenn die Einheit schon bekannt ist, passiert gar nichts
      * @param unit die Einheit
      */
     public void registerMoveable(Moveable moveable) {
         if (!managedMovers.contains(moveable)) {
             FloatingPointPosition upos = moveable.getPrecisePosition();
             FreePolygon poly = containingPoly(upos.getfX(), upos.getfY());
             if (poly != null) {
                 poly.addMoveable(moveable);
                 System.out.println(poly + " contains " + moveable);
                 managedMovers.add(moveable);
                 moveable.setMyPoly(poly);
             }
         }
     }
 
     /**
      * Sucht den Polygon, der diesen Punkt enthält.
      * Vorsicht: Das Polygonnetz deckt nicht alle Punkte ab!
      * @param x
      * @param y
      * @return der gefundene Polygon oder null
      */
     public FreePolygon containingPoly(double x, double y) {
         for (FreePolygon poly : polys) {
             if (poly.contains(x, y)) {
                 return poly;
             }
         }
         System.out.println("Noone contains " + x + " " + y);
         return null;
     }
 
     /**
      * Sucht alle Einheiten im Umkreis um diese hier heraus.
      * Sucht über Sektorgrenzen hinweg.
      * @param mover der Mover um den gesucht wird.
      * @param radius der radius
      * @return eine List mit allen gefundenen Einheiten
      */
     public List<Moveable> moversAround(Moveable mover, double radius) {
         LinkedList<Moveable> movers = new LinkedList<Moveable>();
         // Alle relevanten Sektoren herausfinden:
         List<FreePolygon> relPolys = polysAround(mover.getPrecisePosition().getfX(), mover.getPrecisePosition().getfY(), radius);
         for (FreePolygon poly : relPolys) {
             // Alle Einheiten dieses Sektors analysieren
             List<Moveable> moversInSec = poly.getResidents();
             for (Moveable moverS : moversInSec) {
                 if (moverS.getPrecisePosition().getDistance(mover.getPrecisePosition()) <= radius) {
                     movers.add(moverS);
                 }
             }
         }
         return movers;
     }
 
     /**
      * Findet alle Polygone, die einen Teil des Kreises mit Radius radius um x,y bilden.
      * @param x x-punkt
      * @param y y-punkt
      * @param radius der suchradius
      * @return true, or false;
      */
     private List<FreePolygon> polysAround(double x, double y, double radius) {
         LinkedList<FreePolygon> aPolys = new LinkedList<FreePolygon>();
         FreePolygon owner = containingPoly(x, y);
         aPolys.add(owner);
 
         Circle circle = new Circle((float) x, (float) y, (float) radius);
 
         for (FreePolygon poly : polys) {
             if (poly.equals(owner)) {
                 continue;
             }
             if (circle.intersects(poly.toSlickPoly())) {
                 aPolys.add(poly);
             }
         }
 
         return aPolys;
     }
 
     /**
      * Sucht den nächstgelegenen Knoten zu einem Moveable in seinem Sektor.
      * @param mover das Moveable
      * @return Der nächste Node
      */
     public Node nearestSectorNode(Moveable mover) {
         return nearestNode(mover.getMyPoly(), mover.getPrecisePosition());
     }
 
     /**
      * Sucht den nächstgelegenen Knoten zu einer gegebenen Position.
      * @param target die Position
      * @return Der nächste Node
      */
     public Node nearestSectorNode(FloatingPointPosition target) {
         // Erst den Knoten suchen
         return nearestNode(containingPoly(target.getfX(), target.getfY()), target);
     }
     
     /**
      * Fragt den gegebenen Sektor nach dem nächsten Knoten.
      * @param poly
      * @param pos
      * @return 
      */
     private Node nearestNode(FreePolygon poly, FloatingPointPosition pos) {
         return poly.closestNode(pos);
     }
 }
