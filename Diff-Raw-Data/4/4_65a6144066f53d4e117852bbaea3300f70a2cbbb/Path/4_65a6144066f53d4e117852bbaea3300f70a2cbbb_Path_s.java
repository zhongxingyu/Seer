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
 package thirteenducks.cor.game;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import thirteenducks.cor.game.client.ClientCore;
 import thirteenducks.cor.game.server.ServerCore.InnerServer;
 import thirteenducks.cor.map.CoRMapElement.collision;
 
 /**
  * Ein Weg ist eine Folge von Feldern, die eine Einheit entlang laufen kann.
  * Enthält Variablen und Mechanismen, um die aktuelle Position der Einheit auf dem Weg zu bestimmen/speichern
  */
 public class Path implements Pauseable, Serializable {
 
     /**
      * Die Position auf der die Bewegung begonnen hat.
      */
     private Position startPos;
     /**
      * Die Position auf der die Bewegung enden soll.
      */
     private Position targetPos;
     /**
      * Der Zeitpunkt, zu dem die Einheit losgelaufen ist.
      */
     private long moveStartTime;
     /**
      * Die Weglänge - Echte Einheiten.
      */
     private double length;
     /**
      * Wurde die Bewegung pausiert? (Pause/Speichermodus)
      */
     private boolean movePaused;
     /**
      * Der Zeitpunkt des Pausierens
      */
     private long movePauseTime;
     /**
      * Wurde die Weglänge bereits berechnet?
      * Wenn true steht auch path zur Verfügung
      */
     private boolean pathComputed;
     /**
      * Die Einheit soll am Ende des Weges in ein Gebäude springen
      */
     private int jumpTo = 0;
     /**
      * Wurde die Sprunganweisung eben erst gesetzt?
      * Die Sprunganweisung überlebt genau einen Bewegungsbefehl, dann wird sie gelöscht.
      */
     private boolean jumpJustSet = false;
     /**
      * Der Index des zuletzt erreichten Wegpunktes
      */
     private int lastWayPoint;
     /**
      * Die Strecke bis zum nächsten Wegpunkt
      */
     private double nextWayPointDist;
     /**
      * Der eigentliche Weg.
      * Muss erst berechnet werden, steht zur Verfügung, wenn pathLengthCalced = true
      */
     private List<PathElement> path;
     /**
      * True, während sich die Einheit bewegt, also das Behaviour laufen soll.
      */
     private boolean moving;
     /**
      * Die Laufgeschwindigkeit in Feldern/Sekunde.
      */
     private double speed;
     /**
      * Grafiksystem - Deltaberechnung.
      */
     private double gNextPointDist;
     /**
      * Grafiksystem - Deltaberechnung.
      */
     private int gLastPointIdx;
 
     /**
      * Erzeugt einen neuen Pfad mit der gegebenen Geschwindigkeit.
      * Sollte sich diese einmal ändern, muss der Path darüber benachrichtigt werden.
      * @param speed die Geschwindigkeite in Feldern/Sekunde
      */
     public Path(double speed) {
         this.speed = speed;
     }
 
     /**
      * Berechnet die Länge des Weges dieser Einheit.
      * Die berechnete Weglänge wird in length gespeichert.
      */
     private synchronized void computePath(List<Position> newPath) {
         pathComputed = false;
         length = 0;
         path = new ArrayList<PathElement>();
         path.add(new PathElement(newPath.get(0)));
         for (int i = 1; i < newPath.size(); i++) {
             Position pos = newPath.get(i);
             Position old = newPath.get(i - 1);
             // Richtung berechnen
             int vec = pos.subtract(old).transformToIntVector();
             // Strecke berechnen, mit Pytagoras
             double abschnitt = Math.sqrt(Math.pow(old.getX() - pos.getX(), 2) + Math.pow(old.getY() - pos.getY(), 2));
             length = length + abschnitt;
             path.add(new PathElement(pos, length, vec));
         }
         pathComputed = true;
         moving = true;
     }
 
     /**
      * @return the startPos
      */
     public Position getStartPos() {
         return startPos;
     }
 
     /**
      * @return the targetPos
      */
     public Position getTargetPos() {
         return targetPos;
     }
 
     /**
      * Überschreibt den Pfad und lässt die Einheit vom Beginn loslaufen
      * @param newPath der neue Weg
      */
     public synchronized void overwritePath(List<Position> newPath) {
         startPos = newPath.get(0);
         targetPos = newPath.get(newPath.size() - 1);
         lastWayPoint = 0;
         gLastPointIdx = 0;
         computePath(newPath);
         this.nextWayPointDist = path.get(1).getDistance();
         gNextPointDist = nextWayPointDist;
         moveStartTime = System.currentTimeMillis();
     }
 
     /**
      * Versucht, den Weg "seamless" zu ändern.
      * Da diese Änderung während der Bewegung erfolgt, kann es sein, dass die Einheit noch bis zum nächsten Feld weiter läuft
      * @param newPath
      */
     public synchronized void switchPath(ArrayList<Position> newPath) {
         targetPos = newPath.get(newPath.size() - 1);
         computePath(newPath);
         try {
             if (lastWayPoint + 1 > path.size() - 1) {
                 // Automatisch zurückstellen, falls Überschreitung festgestellt.
                 lastWayPoint = path.size() - 2;
                 gLastPointIdx = lastWayPoint;
                 System.out.println("WARNING: Setting unit back on Path-Switch, probably causes Pfusch");
             }
         } catch (Exception ex) {
             System.out.println("ERROR: Problems switching Path:");
             ex.printStackTrace();
         }
         nextWayPointDist = path.get(lastWayPoint + 1).getDistance();
         gNextPointDist = nextWayPointDist;
     }
 
     /**
      * Verwaltet die Bewegung der Unit auf dem Path.
      * Muss regelmäßig aufgerufen werden
      * @param caster2 Die Einheit, deren Pfad verwaltet wird.
      */
     public synchronized void clientManagePath(ClientCore.InnerClient rgi, Unit caster2) { // ugly
         if (isMoving()) {
             long passedTime = 0;
             if (movePaused) {
                 passedTime = movePauseTime - moveStartTime;
             } else {
                 passedTime = System.currentTimeMillis() - moveStartTime;
             }
             double passedWay = passedTime * caster2.speed / 1000;
             // Schon fertig?
             if (passedWay >= length) {
                 // Fertig, Bewegung stoppen
                 caster2.setMainPosition(targetPos);
                 targetPos = null;
                 path = null;
                 moving = false;
                 return;
             }
             // Zuletzt erreichten Wegpunkt finden
             if (passedWay >= nextWayPointDist) {
                 // Sind wir einen weiter oder mehrere
                 int weiter = 1;
                 while (passedWay > path.get(lastWayPoint + 1 + weiter).getDistance()) {
                     weiter++;
                 }
                 lastWayPoint += weiter;
                 nextWayPointDist = path.get(lastWayPoint + 1).getDistance();
                 caster2.setMainPosition(path.get(lastWayPoint).getPos());
             }
         }
     }
 
     @Override
     public void pause() {
         movePaused = true;
         movePauseTime = System.currentTimeMillis();
     }
 
     @Override
     public void unpause() {
         movePaused = false;
         moveStartTime = System.currentTimeMillis() - (movePauseTime - moveStartTime);
     }
 
     /**
      * Verwaltet den Pfad Serverseitig (Server-Behaviour)
      * Tut nichts, wenn sich die Einheit gerade nicht bewegt.
      * @param rgi
      * @param caster2
      */
     public void serverManagePath(InnerServer rgi, Unit caster2) {
         if (isMoving()) {
             boolean gotError = false;
             int trys = 0;
 
             do {
                 trys++;
                 gotError = false;
                 try {
                     long passedTime = 0;
                     if (movePaused) {
                         passedTime = movePauseTime - moveStartTime;
                     } else {
                         passedTime = System.currentTimeMillis() - moveStartTime;
                     }
                     double passedWay = passedTime * caster2.speed / 1000;
                     // Schon fertig?
                     if (passedWay >= length) {
                         // Fertig, Bewegung stoppen
                         caster2.setMainPosition(targetPos);
                         targetPos = null;
                         path = null;
                         rgi.netmap.setCollision(caster2.getMainPosition(), collision.occupied);
                         rgi.netmap.setUnitRef(caster2.getMainPosition(), caster2, caster2.getPlayerId());
                        caster2.attackManager.moveStopped();
                         moving = false;
                         return;
                     }
                     // Zuletzt erreichten Wegpunkt finden
                     if (passedWay >= nextWayPointDist) {
                         // Sind wir einen weiter oder mehrere
                         int weiter = 1;
                         while (passedWay > path.get(lastWayPoint + 1 + weiter).getDistance()) {
                             weiter++;
                         }
                         lastWayPoint += weiter;
                         nextWayPointDist = path.get(lastWayPoint + 1).getDistance();
                         caster2.setMainPosition(path.get(lastWayPoint).getPos());
 
                         // Ziel in Reichweite? (für Anhalten)
                         // Neuer Wegpunkt! - Berechnungen durchführen:
 
                         // Ziel nichtmehr frei?
 
                         /*          if (rgi.netmap.isGroundColliding(caster2.movingtarget) || (!reservedTarget && rgi.netmap.checkFieldReservation(caster2.movingtarget))) {
                         if (caster2.jumpTo == 0) { // Nicht bei jumps
                         // Weg zu neuem Ziel berechnen
                         RogPosition pos = caster2.movingtarget.aroundMe(1, rgi);
                         // Reservierung nur löschen, wenn wir die selber schon eingetragen haben
                         if (reservedTarget) {
                         rgi.netmap.deleteFieldReservation(caster2.movingtarget);
                         reservedTarget = false;
                         }
                         boolean ret = false;
                         if (caster2.position.getDistance(pos) < 100) {
                         ret = caster2.moveToPosition(pos, rgi, true);
                         } else {
                         ret = caster2.moveToPosition(pos, rgi, false);
                         }
                         if (!ret) {
                         // Das Ausweichen ging nicht, die Einheit ist ziemlich "gelockt".
                         // Versuche die Einheit da wo sie derzeit ist anzuhalten
                         System.out.println("FixMe: Units target blocked, but can't change path! Trying to stop...");
                         boolean ret2 = caster2.moveToPosition(caster2.position.aroundMe(0, rgi), rgi, true);
                         if (!ret2) {
                         // Da hilft nix mehr...
                         System.out.println("FixMe: Unit can't move anywhere, this may result in 2 units on one field - SRY!");
                         }
                         }
                         }
                         } */
 
                         // Einheit noch nicht am Ziel angekommen - übernächstes Feld frei?
                         // Fürt zu sinnlosem rumgerenne - son mist
                         // (Nächstes braucht man nicht zu prüfen, da rennen wir ja eh schon hin...
 
                         /*if (caster2.lastwaypoint < caster2.path.size() - 3) {
                         // Feld prüfen
                         if (rgi.netmap.isGroundColliding(caster2.path.get(caster2.lastwaypoint + 2))) {
                         // Blockiert, Weg neu suchen
                         //System.out.println(System.currentTimeMillis() + ": Way to " + caster2.movingtarget + " is blocked, re-calcing...");
                         //caster2.moveToPosition(caster2.movingtarget, rgi);
                         }
                         } */
 
                     }
                 } catch (Exception ex) {
                     // Fehler, mit Ziemlicher Sicherheit wurde was von nem anderen Thread geändert, während dieser durchlief.
                     // Darum einfach noch mal versuchen
                     if (trys < 2) {
                         gotError = true;
                         System.out.println("Error in SMoveBehaviour, trying again...");
                         rgi.logger(ex);
                     } else {
                         gotError = false;
                         System.out.println("Critical: 2nd try in SMoveBehaviour didn't help.");
                     }
                 }
             } while (gotError);
         }
     }
 
     /**
      * True, während sich die Einheit bewegt, also das Behaviour laufen soll.
      * @return the moving
      */
     private boolean isMoving() {
         return moving;
     }
 
     /**
      * Berechnet die exakte Position der Einheit und berechnet die Zeichenkoordinaten.
      * Dazu werden x und y - die Zeichenkoordinaten des Zuordnungsfeldes benötigt.
      * Diese Methode liefert die gegebenen Werte zurück, falls gerade keine Bewegung läuft.
      * @param x Koordinate des letzten Zuordnungsfeldes
      * @param y Koordinate des letzten Zuordnungsfeldes
      * @return x und y die korrekten Zeichenkoordinaten.
      */
     public synchronized int[] calcExcactPosition(int x, int y) {
         if (isMoving()) {
             // Berechnung notwendig:
             // Letze Zuordnung holen:
             Position zPos = path.get(lastWayPoint).getPos();
             // Default-Berechnung:
             try {
                 long passedTime = 0;
                 if (movePaused) {
                     passedTime = movePauseTime - moveStartTime;
                 } else {
                     passedTime = System.currentTimeMillis() - moveStartTime;
                 }
                 double passedWay = passedTime * speed / 1000;
                 // Schon fertig?
                 if (passedWay < length) {
                     // Zuletzt erreichten Wegpunkt finden
                     if (passedWay >= this.gNextPointDist) {
 
                         // Sind wir einen weiter oder mehrere
                         int weiter = 1;
                         while (passedWay > path.get(gLastPointIdx + 1 + weiter).getDistance()) {
                             weiter++;
                         }
                         gLastPointIdx += weiter;
 
                         gNextPointDist = path.get(gLastPointIdx + 1).getDistance();
                     }
                     // In ganz seltenen Fällen ist hier lastwaypoint zu hoch (vermutlich (tfg) ein multithreading-bug)
                     // Daher erst checken und ggf. reduzieren:
                     if (gLastPointIdx >= path.size() - 1) {
                         System.out.println("Client: Lastwaypoint-Error, setting back. May causes jumps!?");
                         gLastPointIdx = path.size() - 2;
                     }
                     double diffLength = passedWay - path.get(gLastPointIdx).getDistance();
                     // Wir haben jetzt den letzten Punkt der Route, der bereits erreicht wurde, und die Strecke, die danach noch gefahren wurde...
                     // Jetzt noch die Richtung
                     int diffX = path.get(gLastPointIdx + 1).getPos().getX() - path.get(gLastPointIdx).getPos().getX();
                     int diffY = path.get(gLastPointIdx + 1).getPos().getY() - path.get(gLastPointIdx).getPos().getY();
                     // Prozentanteil der Stecke, die zurückgelegt wurde
                     double potPathWay = Math.sqrt(Math.pow(Math.abs(diffX), 2) + Math.pow(Math.abs(diffY), 2));
                     double faktor = diffLength / potPathWay * 100;
                     double lDiffX = diffX * faktor / 10; //    / 100 * 10
                     double lDiffY = diffY * faktor / 100 * 7.5;
                     // Eventuell ist die gegebene x und y Zuordungsposition schlecht - prüfen
                     Position pdiff = zPos.subtract(path.get(gLastPointIdx).getPos());
                     // Aktuelle Koordinaten reinrechnen:
                     x += lDiffX - (pdiff.getX() * 10);
                     y += lDiffY - (pdiff.getY() * 7.5);
                 }
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
 
         // Egal ob was reingerechnet wurde, oder nicht:
         return new int[]{x, y};
     }
 
     /**
      * Mini-Klasse, um mehrere Werte zu Speichern.
      * Speichert den Wegpunkt, die Strecke bis dort hin und die Richtung bis dort hin.
      */
     class PathElement {
 
         /**
          * Das eigentliche "Feld" dieses Wegelements
          */
         private Position pos;
         /**
          * Die Distanz vom Anfang des Weges bis hier her.
          */
         private double distance;
         /**
          * Die Richtung vom letzten Feld zu diesem.
          * Im 8-ter System der Grafikengine angegeben.
          */
         private int direction;
 
         /**
          * Erstellt eine Wegposition ohne Richtung und Entfernung.
          * Für den Anfang von Wegen
          * @param get
          */
         private PathElement(Position get) {
             pos = get;
         }
 
         /**
          * Erstellt eine reguläre Wegposition mit den angegebenen Parametern.
          * @param pos Position dieses Feldes
          * @param length die Strecke bis zu diesem Feld
          * @param vec die Richtung, aus der man kommt
          */
         private PathElement(Position pos, double length, int vec) {
             this.pos = pos;
             distance = length;
             direction = vec;
         }
 
         /**
          * @return the pos
          */
         public Position getPos() {
             return pos;
         }
 
         /**
          * @return the distance
          */
         public double getDistance() {
             return distance;
         }
 
         /**
          * @return the direction
          */
         public int getDirection() {
             return direction;
         }
     }
 }
