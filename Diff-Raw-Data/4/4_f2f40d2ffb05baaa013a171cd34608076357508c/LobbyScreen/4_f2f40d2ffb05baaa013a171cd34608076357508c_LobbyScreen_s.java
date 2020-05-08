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
 package de._13ducks.cor.mainmenu;
 
import de._13ducks.cor.mainmenu.components.ChatWindow;
 import java.util.ArrayList;
 import de._13ducks.cor.mainmenu.components.Container;
 import de._13ducks.cor.mainmenu.components.Frame;
 import de._13ducks.cor.mainmenu.components.ImageButton;
 import de._13ducks.cor.mainmenu.components.LobbyChat;
 import de._13ducks.cor.mainmenu.components.Player;
 import de._13ducks.cor.mainmenu.components.TiledImage;
 
 /**
  * Die Lobby
  * hier werde nTeams, Servereinstellungen, Map etc festgelegt und das Spiel gestartet
  * @author michael
  */
 public class LobbyScreen extends Container {
 
     /**
      * die cmd-id für lobby-kommunikation
      */
     static byte lobbycomid = 14;
     /**
      * Die Spielerliste
      */
     ArrayList<Player> players;
     /**
      * Der Name des lokalen Spielers
      */
     String playerName;
     /**
      * Der "Bereit"-Button
      */
     private ImageButton readyButton;
     /**
      * Ist der lokale Spieler bereit?
      */
     private boolean ready;
     /**
      * Das Chatfenster
      */
     private LobbyChat lobbyChat;
 
     /**
      * Konstruktor
      * @param m - Hauptmenü-Referenz
      */
     public LobbyScreen(MainMenu m) {
         super(m, 1, 1, 100, 100);
 
         ready = false;
 
         players = new ArrayList<Player>();
 
         // Map-Preview
         // @TODO: Die startposition in diesem Preview auswählen
         super.addComponent(new TiledImage(m, 5, 5, 90, 90, "img/mainmenu/rost.png"));
 
         // Spielerliste:
         super.addComponent(new Frame(m, 10, 10, 45, 60));
 
         // Map-preview:
         super.addComponent(new Frame(m, 60, 10, 30, 30));
 
         // Serveroptionen:
         super.addComponent(new Frame(m, 60, 50, 20, 15));
 
         // Chat:
        super.addComponent(lobbyChat = new LobbyChat(m,10,75));
         super.addComponent(lobbyChat);
         // Ready-Button:
         readyButton = new ImageButton(m, 60, 85, 12, 6, "img/mainmenu/buttonnew.png", "READY") {
 
             @Override
             public void mouseClicked(int button, int x, int y, int clickCount) {
                 if (ready == true) {
                     send('4' + playerName);
                     System.out.println("Player is ready: " + ready);
                 } else {
                     send('3' + playerName);
                     System.out.println("Player is ready: " + ready);
                 }
             }
         };
         super.addComponent(readyButton);
 
 
     }
 
     /**
      * Sendet eine Nachricht an den Server
      * @param s - Die zu sendende Nachricht
      */
     public void send(String s) {
         getMainMenu().getClientCore().rgi.netctrl.broadcastString((s + '\0'), lobbycomid);
     }
 
     /**
      * Gibt den Spieler mit dem angegebenen Namen zurück, wenn es einen gibt.
      * @param name - der Name des gesuchten Spielers
      * @return - das Player-Objekt
      */
     private Player getPlayer(String name) {
         for (Player p : players) {
             if (p.getPlayerName().equals(name)) {
                 return p;
             }
         }
         System.out.print("Player" + name + " not found!");
         return null;
     }
 
     /**
      * Fügt einne neuen Spieler hinzu
      * @param name - der Name des neuen Spielers
      */
     public void addPlayer(String name) {
         Player p = new Player(this.getMainMenu(), this, 11, 11 + ((Player.playerSlotHeight + 2) * players.size()), name);
 
         players.add(p);
 
         super.addComponent(p);
     }
 
     /**
      * Entfernt einen Spieler aus der Lobby (z.B. kick, leave oder timeout)
      * @param name_t - der Name der zu entfernenden Spielers
      */
     public void removePlayer(String name) {
         super.removeComponent(getPlayer(name));
     }
 
     /**
      * Ändert die Farbe eines Spielers
      * @param name_t - der Name des Spielers
      * @param color - Die neue Farbe
      */
     public void changePlayerColor(String name_t, String color) {
     }
 
     /**
      * Ändert das Team eines Spielers
      * @param name_t - der Name des Spielers
      * @param team - das neue Team des Spielers
      */
     public void changePlayerTeam(String name_t, int team) {
     }
 
     /**
      * Ändert die Rasse eines Spielers
      * @deprecated
      * @param name_t - der Naem des Spielers
      * @param race - die neue Rasse
      */
     public void changePlayerRace(String name_t, int race) {
     }
 
     /**
      * Ändert den Status des Spielers (Bereit oder nicht bereit)
      * @param name  - der Name des Spielers
      * @param ready - der neue Status
      */
     public void changePlayerStatus(String name, boolean ready) {
         getPlayer(name).setReady(ready);
         if (name.equals(playerName)) {
             if (ready == true) {
                 this.ready = true;
                 readyButton.setText("READY");
             } else {
                 this.ready = false;
                 readyButton.setText("NOT READY");
             }
 
         }
     }
 
     /**
      * empfängt eine neue Chat-Nachricht
      * @param message - die chatnachricht
      */
     public void chatMessage(String message) {
         lobbyChat.chatMessage(message);
     }
 
     /**
      * Macht einen Spieler zum Host
      * @param name_t - der Name des Spielers
      */
     public void setHostPlayer(String name_t) {
     }
 
     /**
      * getter für Playername
      * @return - der Name des lokalen Spielers
      */
     public String getPlayername() {
         return playerName;
     }
 
     /**
      * Setter für playerName
      * @param name - der neue Spielername
      */
     public void setPlayerName(String name) {
         playerName = name;
     }
 }
