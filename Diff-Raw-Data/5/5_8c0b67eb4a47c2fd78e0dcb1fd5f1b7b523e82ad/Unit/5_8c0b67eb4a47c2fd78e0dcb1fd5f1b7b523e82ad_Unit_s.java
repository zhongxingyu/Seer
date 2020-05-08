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
 package de._13ducks.cor.game;
 
 import de._13ducks.cor.game.client.ClientCore;
 import java.io.*;
 import java.util.*;
 import de._13ducks.cor.game.client.ClientCore.InnerClient;
 import de._13ducks.cor.graphics.input.InteractableGameElement;
 
 /**
  * Superklasse für Einheiten
  *
  * Einheiten sind GO's, die sich bewegen können.
  * Dazu verwenden Einheiten statt normalen Positionen Fließkommazahlen,
  * um auch Positionen zwischen Feldern darstellen zu können.
  * Die von GameObject bekannten, feldbasierten Positionsoperationen funktionieren weiterhin,
  * sind jedoch nur eine (abgerundetete!) Darstellung der "echten Feldern"
  * Es existieren auch Getter für die Fließkommazahlen.
  * Im Gegensatz zu Gebäuden ermöglicht diese Implementierung keine flexiblen Größen
  * Einheiten dieser Implementierung spawnen sofort.
  * Unterklassen können Gebäude (Building) betreten (falls das Gebäude dies anbietet)
  */
 public abstract class Unit extends GameObject implements Serializable, Cloneable, Pauseable {
 
     /**
      * Die Geschwindigkeit der Einheit in Feldern pro Sekunde.
      */
     protected double speed;
     /**
      * Ist die Einheit gerade in einem Gebäude? Dann sind fast alle Behaviours (alle?) abgeschaltet.
      */
     private boolean isIntra = false;
 
     protected Unit(int newNetId, Position mainPos) {
         super(newNetId, mainPos);
         // Default-Werte *ugly*
         hitpoints = 100;
         maxhitpoints = 100;
         armorType = GameObject.ARMORTYPE_BUILDING;
         this.mainPosition = new FloatingPointPosition(this.mainPosition);
     }
 
     /**
      * Erzeugt eine Platzhalter-Einheit, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
      * Attribute und Fähigkeiten dient.
      */
     protected Unit(DescParamsUnit params) {
         super(params);
         applyUnitParams(params);
        this.mainPosition = new FloatingPointPosition(this.mainPosition);
     }
 
     /**
      * Erzeugt eine neue Einheit als eigenständige Kopie der Übergebenen.
      * Wichtige Parameter werden kopiert, Sachen die jede Einheit selber haben sollte nicht.
      * Wichtig: Die Position muss noch gesetzt werden, die ist Anfangs 0,0
      * @param newNetId Die netId der neuen Einheit
      * @param copyFrom Die Einheit, dessen Parameter kopiert werden sollen
      */
     protected Unit(int newNetId, Unit copyFrom) {
         super(newNetId, copyFrom);
         this.speed = copyFrom.speed;
        this.mainPosition = new FloatingPointPosition(this.mainPosition);
     }
 
     /**
      * Wendet die Parameterliste an (kopiert die Parameter rein)
      * @param par
      */
     private void applyUnitParams(DescParamsUnit par) {
         this.speed = par.getSpeed();
     }
     /**
      * @return the speed
      */
     public double getSpeed() {
         return speed;
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void unpause() {
     }
 
     @Override
     public String toString() {
         return ("Unit \"" + this.getName() + "\" ID: " + this.netID);
     }
 
     @Override
     public boolean renderInFullFog() {
         return false;
     }
 
     @Override
     public boolean renderInHalfFog() {
         //@TODO: Gebäude müssen sichtbar bleiben, wenn man sie einmal gesehen hat.
         return false;
     }
 
     @Override
     public boolean renderInNullFog() {
         return true;
     }
 
     /**
      * Ist die Einheit gerade in einem Gebäude? Dann sind fast alle Behaviours (alle?) abgeschaltet.
      * @return the isIntra
      */
     public boolean isIntra() {
         return isIntra;
     }
 
     @Override
     public boolean isSelectableByPlayer(int playerId) {
         return playerId == this.getPlayerId();
     }
 
     @Override
     public boolean isMultiSelectable() {
         return true;
     }
 
     @Override
     public boolean selectable() {
         return true;
     }
     
     @Override
     public int getColorId() {
         return getPlayerId();
     }
 
     @Override
     public void command(int button, Position target, List<InteractableGameElement> repeaters, boolean doubleKlick, InnerClient rgi) {
         // Befehl abschicken:
         rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, target.getX(), target.getY(), repeaters.get(0).getAbilityCaster().netID, repeaters.size() > 1 ? repeaters.get(1).getAbilityCaster().netID : 0));
         // Hier sind unter umständen mehrere Packete nötig:
         if (repeaters.size() == 2) {
             // Nein, abbrechen
             rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, 0, 0, 0, 0));
         } else if (repeaters.size() != 1) {
             // Jetzt den Rest abhandeln
             int[] ids = new int[4];
             for (int i = 0; i < 4; i++) {
                 ids[i] = 0;
             }
             int nextselindex = 2;
             int nextidindex = 0;
             // Solange noch was da ist:
             while (nextselindex < repeaters.size()) {
                 // Auffüllen
                 ids[nextidindex] = repeaters.get(nextselindex).getAbilityCaster().netID;
                 nextidindex++;
                 nextselindex++;
                 // Zu weit?
                 if (nextidindex == 4) {
                     // Einmal rausschicken & löschen
                     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, ids[0], ids[1], ids[2], ids[3]));
                     for (int i = 0; i < 4; i++) {
                         ids[i] = 0;
                     }
                     nextidindex = 0;
                 }
             }
             // Fertig, den Rest noch senden
             rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, ids[0], ids[1], ids[2], ids[3]));
         }
     }
 
     public FloatingPointPosition getPrecisePosition() {
         return (FloatingPointPosition) mainPosition;
     }
 
         /**
      * Stoppt die Einheit sofort - sofern genug Platz ist und die Einheit sich überhaupt bewegt.
      * Falls hier gerade kein Platz ist, wird die Einheit zur nächstmöglichen Position laufen.
      * Nur Client!
      */
     public void stopMovement(ClientCore.InnerClient rgi) {
         System.out.println("AddMe: Stop unit!");
     }
 
     /**
      * Findet heraus, ob die Einheit sich derzeit in einer Stoppbaren Bewegung befindet.
      * @return
      */
     public boolean moveStoppable() {
         return true;
     }
 }
