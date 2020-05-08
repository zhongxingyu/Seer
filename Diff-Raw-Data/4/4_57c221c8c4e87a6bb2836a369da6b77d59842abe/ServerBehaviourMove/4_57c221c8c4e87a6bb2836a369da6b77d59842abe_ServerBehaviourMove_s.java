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
 import de._13ducks.cor.game.GameObject;
 import de._13ducks.cor.game.Moveable;
 import de._13ducks.cor.game.SimplePosition;
 import de._13ducks.cor.game.server.Server;
 import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
 import de._13ducks.cor.game.server.ServerCore;
 import de._13ducks.cor.map.fastfindgrid.Traceable;
 import java.util.List;
 import org.newdawn.slick.geom.Circle;
 import org.newdawn.slick.geom.Polygon;
 
 /**
  * Lowlevel-Movemanagement
  * 
  * Verwaltet die reine Bewegung einer einzelnen Einheit.
  * Kümmert sich nicht weiter um Formationen/Kampf oder ähnliches.
  * Erhält vom übergeordneten MidLevelManager einen Richtungsvektor und eine Zielkoordinate.
  * Läuft dann dort hin. Tut sonst nichts.
  * Hat exklusive Kontrolle über die Einheitenposition.
  * Weigert sich, sich schneller als die maximale Einheitengeschwindigkeit zu bewegen.
  * Dadurch werden Sprünge verhindert.
  *
  * Wenn eine Kollision festgestellt wird, wird der überliegende GroupManager gefragt was zu tun ist.
  * Der GroupManager entscheidet dann über die Ausweichroute oder lässt uns warten.
  */
 public class ServerBehaviourMove extends ServerBehaviour {
 
     private Moveable caster2;
     private Traceable caster3;
     private SimplePosition target;
     private double speed;
     private boolean stopUnit = false;
     private long lastTick;
     private SimplePosition clientTarget;
     private MovementMap moveMap;
     private GroupMember pathManager;
     /**
      * Die Systemzeit zu dem Zeitpunkt, an dem mit dem Warten begonnen wurde
      */
     private long waitStartTime;
     /**
      * Gibt an, ob gerade gewartet wird
      * (z.B. wenn etwas im Weg steht und man wartet bis es den Weg freimacht)
      */
     private boolean wait;
     /**
      * Die Zeit, die gewartet wird (in Nanosekunden) (eine milliarde ist eine sekunde)
      */
     private static final long waitTime = 3000000000l;
     /**
      * Eine minimale Distanz, die Einheiten beim Aufstellen wegen einer Kollision berücksichtigen. 
      * Damit wird verhindert, dass aufgrund von Rundungsfehlern Kolision auf ursprünlich als frei
      * eingestuften Flächen berechnet wird.
      */
     public static final double MIN_DISTANCE = 0.1;
     /**
      * Ein simples Flag, das nach dem Kollisionstest gesetzt ist.
      */
     private boolean colliding = false;
     /**
      * Zeigt nach dem Kollisionstest auf das letzte (kollidierende) Objekt.
      */
     private Moveable lastObstacle;
 
     public ServerBehaviourMove(ServerCore.InnerServer newinner, GameObject caster1, Moveable caster2, Traceable caster3, MovementMap moveMap) {
         super(newinner, caster1, 1, 20, true);
         this.caster2 = caster2;
         this.caster3 = caster3;
         this.moveMap = moveMap;
 
     }
 
     @Override
     public void activate() {
         active = true;
         trigger();
     }
 
     @Override
     public void deactivate() {
         active = false;
     }
 
     @Override
     public synchronized void execute() {
         // Auto-Ende:
         if (target == null || speed <= 0) {
             deactivate();
             return;
         }
 
         // Wir laufen also.
         // Aktuelle Position berechnen:
         FloatingPointPosition oldPos = caster2.getPrecisePosition();
         Vector vec = target.toFPP().subtract(oldPos).toVector();
         vec.normalizeMe();
         long ticktime = System.nanoTime();
         vec.multiplyMe((ticktime - lastTick) / 1000000000.0 * speed);
         FloatingPointPosition newpos = vec.toFPP().add(oldPos);
 
         // Ob die Kollision später noch geprüft werden muss. Kann durch ein Weiterlaufen nach warten überschrieben werden.
         boolean checkCollision = true;
 
         // Wir sind im Warten-Modus. Jetzt also testen, ob wir zur nächsten Position können
         if (wait) {
             // Testen, ob wir schon weiterlaufen können:
             // Echtzeitkollision:
             newpos = checkAndMaxMove(oldPos, newpos);
             if (colliding) {
                 // Immer noch Kollision
                 if (System.nanoTime() - waitStartTime < waitTime) {
                     // Das ist ok, einfach weiter warten
                     lastTick = System.nanoTime();
                     return;
                 } else {
                     // Wartezeit abgelaufen
                     wait = false;
                     // Wir stehen schon, der Client auch --> nichts weiter zu tun.
                     target = null;
                     deactivate();
                     System.out.println("STOP waiting: " + caster2);
                     return;
                 }
             } else {
                 // Nichtmehr weiter warten - Bewegung wieder starten
                 System.out.println("GO! Weiter mit " + caster2 + " " + newpos + " nach " + target);
                 wait = false;
                 checkCollision = false;
             }
         }
 
         if (!target.equals(clientTarget) && !stopUnit) {
             // An Client senden
             rgi.netctrl.broadcastMoveVec(caster2.getNetID(), target.toFPP(), speed);
             clientTarget = target.toFPP();
         }
 
         if (checkCollision) {
             // Zu laufenden Weg auf Kollision prüfen
             newpos = checkAndMaxMove(oldPos, newpos);
 
             if (!stopUnit && colliding) {
                 // Kollision. Gruppenmanager muss entscheiden, ob wir warten oder ne Alternativroute suchen.
                 wait = this.caster2.getMidLevelManager().collisionDetected(this.caster2, lastObstacle);
 
                 if (wait) {
                     waitStartTime = System.nanoTime();
                     // Spezielle Stopfunktion: (hält den Client in einem Pseudozustand)
                     // Der Client muss das auch mitbekommen
                     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 24, caster2.getNetID(), 0, Float.floatToIntBits((float) newpos.getfX()), Float.floatToIntBits((float) newpos.getfY())));
                     setMoveable(oldPos, newpos);
                     clientTarget = null;
                     System.out.println("WAIT-COLLISION " + caster2 + " with " + lastObstacle + " stop at " + newpos);
                     return; // Nicht weiter ausführen!
                 } else {
                     // Bricht die Bewegung vollständig ab.
                     System.out.println("STOP-COLLISION " + caster2 + " with " + lastObstacle);
                     stopUnit = true;
                 }
             }
         }
         // Ziel schon erreicht?
         Vector nextVec = target.toFPP().subtract(newpos).toVector();
         if ((vec.isOpposite(nextVec) || newpos.equals(target)) && !stopUnit) {
             // Zielvektor erreicht
             // Wir sind warscheinlich drüber - egal einfach auf dem Ziel halten.
             setMoveable(oldPos, target.toFPP());
            caster3.setCell(Server.getInnerServer().netmap.getFastFindGrid().getNewCell(caster3));
             // Neuen Wegpunkt anfordern:
             if (!pathManager.reachedTarget(caster2)) {
                 // Wenn das false gibt, gibts keine weiteren, dann hier halten.
                 target = null;
                 stopUnit = false; // Es ist wohl besser auf dem Ziel zu stoppen als kurz dahinter!
                 deactivate();
             }
 
         } else {
             // Sofort stoppen?
             if (stopUnit) {
                 // Der Client muss das auch mitbekommen
                 rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 24, caster2.getNetID(), 0, Float.floatToIntBits((float) newpos.getfX()), Float.floatToIntBits((float) newpos.getfY())));
                 System.out.println("MANUSTOP: " + caster2 + " at " + newpos);
                 setMoveable(oldPos, newpos);
                 target = null;
                 stopUnit = false;
                 deactivate();
             } else {
                 // Weiterlaufen
                 setMoveable(oldPos, newpos);
                 lastTick = System.nanoTime();
             }
         }
 
     }
 
     private void setMoveable(FloatingPointPosition from, FloatingPointPosition to) {
         caster2.setMainPosition(to);
         // Neuer Sektor?
         while (pathManager.nextSectorBorder() != null && pathManager.nextSectorBorder().sidesDiffer(from, to)) {
             // Ja, alten löschen und neuen setzen!
             SectorChangingEdge edge = pathManager.borderCrossed();
             caster2.setMyPoly(edge.getNext(caster2.getMyPoly()));
         }
     }
 
     @Override
     public void gotSignal(byte[] packet) {
     }
 
     @Override
     public void pause() {
         caster2.pause();
     }
 
     @Override
     public void unpause() {
         caster2.unpause();
     }
 
     /**
      * Setzt den Zielvektor für diese Einheit.
      * Es wird nicht untersucht, ob das Ziel in irgendeiner Weise ok ist, die Einheit beginnt sofort loszulaufen.
      * In der Regel sollte noch eine Geschwindigkeit angegeben werden.
      * Wehrt sich gegen nicht existente Ziele.
      * @param pos die Zielposition, wird auf direktem Weg angesteuert.
      */
     public synchronized void setTargetVector(SimplePosition pos) {
         if (pos == null) {
             throw new IllegalArgumentException("Cannot send " + caster2 + " to null");
         }
         if (!pos.toVector().isValid()) {
             throw new IllegalArgumentException("Cannot send " + caster2 + " to invalid position");
         }
         target = pos;
         lastTick = System.nanoTime();
         clientTarget = Vector.ZERO;
         activate();
     }
 
     /**
      * Setzt den Zielvektor und die Geschwindigkeit und startet die Bewegung sofort.
      * @param pos die Zielposition
      * @param speed die Geschwindigkeit
      */
     public synchronized void setTargetVector(SimplePosition pos, double speed) {
         changeSpeed(speed);
         setTargetVector(pos);
     }
 
     /**
      * Ändert die Geschwindigkeit während des Laufens.
      * Speed wird verkleinert, wenn der Wert über dem Einheiten-Maximum liegen würde
      * @param speed Die Einheitengeschwindigkeit
      */
     public synchronized void changeSpeed(double speed) {
         if (speed > 0 && speed <= caster2.getSpeed()) {
             this.speed = speed;
         }
         trigger();
     }
 
     public boolean isMoving() {
         return target != null;
     }
 
     /**
      * Stoppt die Einheit innerhalb eines Ticks.
      */
     public synchronized void stopImmediately() {
         stopUnit = true;
         trigger();
     }
 
     /**
      * Findet einen Sektor, den beide Knoten gemeinsam haben
      * @param n1 Knoten 1
      * @param n2 Knoten 2
      */
     private FreePolygon commonSector(Node n1, Node n2) {
         for (FreePolygon poly : n1.getPolygons()) {
             if (n2.getPolygons().contains(poly)) {
                 return poly;
             }
         }
         return null;
     }
 
     /**
      * Berechnet den Winkel zwischen zwei Vektoren
      * @param vector_1
      * @param vector_2
      * @return
      */
     public double getAngle(FloatingPointPosition vector_1, FloatingPointPosition vector_2) {
         double scalar = ((vector_1.getfX() * vector_2.getfX()) + (vector_1.getfY() * vector_2.getfY()));
 
         double vector_1_lenght = Math.sqrt((vector_1.getfX() * vector_1.getfX()) + vector_2.getfY() * vector_1.getfY());
         double vector_2_lenght = Math.sqrt((vector_2.getfX() * vector_2.getfX()) + vector_2.getfY() * vector_2.getfY());
 
         double lenght = vector_1_lenght * vector_2_lenght;
 
         double angle = Math.acos((scalar / lenght));
 
         return angle;
     }
 
     /**
      * Untersucht die gegebene Route auf Echtzeitkollision.
      * Sollte alles frei sein, wird to zurückgegeben.
      * Ansonsten gibts eine neue Position, bis zu der problemlos gelaufen werden kann.
      * Im schlimmsten Fall ist dies die from-Position, die frei sein MUSS!
      * @param from von wo wir uns losbewegen
      * @param to wohin wir laufen
      * @return FloatingPointPosition bis wohin man laufen kann.
      */
     private FloatingPointPosition checkAndMaxMove(FloatingPointPosition from, FloatingPointPosition to) {
         // Zuallererst: Wir dürfen nicht über das Ziel hinaus laufen:
         Vector oldtargetVec = new Vector(target.x() - from.x(), target.y() - from.y());
         Vector newtargetVec = new Vector(target.x() - to.x(), target.y() - to.y());
         if (oldtargetVec.isOpposite(newtargetVec)) {
             // Achtung, zu weit!
             to = target.toFPP();
         }
         // Zurücksetzen
         lastObstacle = null;
         colliding = false;
         List<Moveable> possibleCollisions = moveMap.moversAroundPoint(caster2.getPrecisePosition(), caster2.getRadius() + 10, caster2.getMyPoly());
         // Uns selber ignorieren
         possibleCollisions.remove(caster2);
         // Freies Gebiet markieren:
         Vector ortho = new Vector(to.getfY() - from.getfY(), from.getfX() - to.getfX()); // 90 Grad verdreht (y, -x)
         ortho.normalizeMe();
         ortho.multiplyMe(caster2.getRadius());
         Vector fromv = from.toVector();
         Vector tov = to.toVector();
         Polygon poly = new Polygon();
         poly.addPoint((float) fromv.add(ortho).x(), (float) fromv.add(ortho).y());
         poly.addPoint((float) fromv.add(ortho.getInverted()).x(), (float) fromv.add(ortho.getInverted()).y());
         poly.addPoint((float) tov.add(ortho.getInverted()).x(), (float) tov.add(ortho.getInverted()).y());
         poly.addPoint((float) tov.add(ortho).x(), (float) tov.add(ortho).y());
         poly.addPoint((float) fromv.add(ortho).x(), (float) fromv.add(ortho).y());
 
         for (Moveable t : possibleCollisions) {
             float radius = (float) (t.getRadius() + MIN_DISTANCE);
             Circle c = new Circle((float) t.getPrecisePosition().x(), (float) t.getPrecisePosition().y(), radius); //Das getUnit ist ugly!
             // Die drei Kollisionsbedingungen: Schnitt mit Begrenzungslinien, liegt innerhalb des Testpolygons, liegt zu nah am Ziel
             if (poly.intersects(c) || poly.includes(c.getCenterX(), c.getCenterY()) || to.getDistance(t.getPrecisePosition()) < caster2.getRadius() + radius) {
                 // Kollision!
                 // Jetzt muss poly verkleinert werden.
                 // Dazu muss die Zielposition to auf der Strecke von from nach to so weit wie notwendig nach hinten verschoben werden.
                 // Notwendiger Abstand zur gefundenen Kollision t
                 float distanceToObstacle = (float) (this.caster2.getRadius() + radius + MIN_DISTANCE);
                 // Vector, der vom start zum Ziel der Bewegung zeigt.
                 Vector dirVec = new Vector(to.getfX() - from.getfX(), to.getfY() - from.getfY());
                 // 90 Grad dazu
                 Vector origin = new Vector(-dirVec.getY(), dirVec.getX());
                 // Strecke vom Hinderniss in 90-Grad-Richtung
                 Edge edge = new Edge(t.getPrecisePosition().toNode(), t.getPrecisePosition().add(origin.toFPP()).toNode());
                 // Strecke zwischen start und ziel
                 Edge edge2 = new Edge(caster2.getPrecisePosition().toNode(), caster2.getPrecisePosition().add(dirVec.toFPP()).toNode());
                 // Schnittpunkt
                 SimplePosition p = edge.endlessIntersection(edge2);
                 if (p == null) {
                     System.out.println("ERROR: " + caster2 + " " + edge + " " + edge2 + " " + t.getPrecisePosition() + " " + origin + " " + dirVec + " " + distanceToObstacle);
                 }
                 // Abstand vom Hinderniss zur Strecke edge2
                 double distance = t.getPrecisePosition().getDistance(p.toFPP());
                 // Abstand vom Punkt auf edge2 zu freigegebenem Punkt
                 double b = Math.sqrt((distanceToObstacle * distanceToObstacle) - (distance * distance));
                 // Auf der Strecke weit genug zurück gehen:
                 FloatingPointPosition nextnewpos = p.toVector().add(dirVec.getInverted().normalize().multiply(b)).toFPP();
 
                 // Zurückgegangenes Stück analysieren
                 if (new Vector(nextnewpos.getfX() - fromv.x(), nextnewpos.getfY() - fromv.y()).isOpposite(dirVec)) {
                     // Ganz zurück setzen. Dann muss man nicht weiter suchen, die ganze Route ist hoffnungslos blockiert
                     colliding = true;
                     lastObstacle = t;
                     return from;
                 }
 
                 // Hier gibt es keine Kollision mehr.
                 // poly neu bauen:
                 to = nextnewpos;
                 tov = nextnewpos.toVector();
                 poly = new Polygon();
                 poly.addPoint((float) fromv.add(ortho).x(), (float) fromv.add(ortho).y());
                 poly.addPoint((float) fromv.add(ortho.getInverted()).x(), (float) fromv.add(ortho.getInverted()).y());
                 poly.addPoint((float) tov.add(ortho.getInverted()).x(), (float) tov.add(ortho.getInverted()).y());
                 poly.addPoint((float) tov.add(ortho).x(), (float) tov.add(ortho).y());
                 poly.addPoint((float) fromv.add(ortho).x(), (float) fromv.add(ortho).y());
 
                 colliding = true;
                 lastObstacle = t;
 
                 // Falls wir so weit zurück mussten, dass es gar netmehr weiter geht:
                 if (from.equals(to)) {
                     return from;
                 }
             }
         }
 
         // Jetzt wurde alles oft genug verschoben um definitiv keine Kollision mehr zu haben.
         // Bis hierhin die Route also freigeben:
         return to;
     }
 
     /**
      * Hiermit lässt sich herausfinden, ob dieser mover gerade wartet, weil jemand im Weg steht.
      * @return 
      */
     boolean isWaiting() {
         return wait;
     }
 
     /**
      * @return the pathManager
      */
     GroupMember getPathManager() {
         return pathManager;
     }
 
     /**
      * @param pathManager the pathManager to set
      */
     void setPathManager(GroupMember pathManager) {
         this.pathManager = pathManager;
     }
 }
