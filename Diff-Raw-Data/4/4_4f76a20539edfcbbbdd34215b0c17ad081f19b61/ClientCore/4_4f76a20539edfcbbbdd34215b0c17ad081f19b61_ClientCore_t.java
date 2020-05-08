 /*
  *  Copyright 2008, 2009, 2010, 2011:
  *   Tobias Fleig (tfg[AT]online[DOT]de),
  *   Michael Hase (mekhar[AT]gmx[DOT]de),
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
 package de._13ducks.cor.game.client;
 
 import de._13ducks.cor.networks.client.ClientNetController;
 import de._13ducks.cor.graphics.impl.TeamSelector;
 import java.io.*;
 import java.util.*;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import de._13ducks.cor.graphics.ClientChat;
 import de._13ducks.cor.game.Core;
 import de._13ducks.cor.graphics.CoreGraphics;
 import de._13ducks.cor.mainmenu.LobbyScreen;
 import de._13ducks.cor.networks.lobby.Lobby;
 import de._13ducks.cor.sound.SoundModule;
 import java.net.InetAddress;
 
 /**
  * Der Client-Core
  * 
  * Startet CoR im Client-Modus
  * 
  * Erzeugt ein eigenes Logfile (client_log.txt)
  *
  * @author tfg
  */
 public class ClientCore extends Core {
 
     public ClientCore.InnerClient rgi;
     public CoreGraphics rGraphics;
     /*RogSound rSound;
     RogGameLogic rGameLogic;
     NetClientMapModule rMap;
     Pathfinder rPathfinder;
     RogMainMenu rMainMenu; */
     SoundModule soundM;
     ClientNetController netController;
     ClientGameController gamectrl;
     ClientMapModule mapMod;
     ClientChat cchat;
     String playername;
     /**
      * Referenz auf die Lobby
      */
     LobbyScreen lobby;
     ClientStatistics cs;
 
     public ClientCore(HashMap<String, String> cfg) {
         cfgvalues = cfg;
         // Debug?
         debugmode = "true".equals(cfgvalues.get("debug"));
         System.out.println("Debug is: " + (debugmode ? "ON" : "OFF"));
         System.out.println("Loading graphics...");
         try {
             rGraphics = new CoreGraphics(cfgvalues, this);
         } catch (Exception ex) {
             System.out.println("ERROR! Details:");
             ex.printStackTrace();
         }
 
     }
 
     /*public ClientCore(boolean debug, InetAddress connectTo, int port, String playername_t, DisplayMode mode, boolean fullScreen, HashMap newcfg) throws SlickException {
     
     playername = playername_t;
     lobby = new Lobby();
     cfgvalues = newcfg;
     
     
     // Hier beginnt der Code richtig zu laufen
     // Einstellungen aus Startoptionen übernehmen
     debugmode = debug;
     
     rgi = new ClientCore.InnerClient(playername, isAIClient);
     
     gamectrl = new ClientGameController(rgi);
     
     
     //gamectrl = new ClientGameController(rgi);
     
     
     // Neues Logfile anlegen (altes Löschen)
     initLogger();
     rgi.logger("[Core] Init RoG-Core");
     if (debugmode) {
     rgi.logger("[Core] Debug-Mode active");
     } else {
     rgi.logger("[Core] Debug-Mode off");
     }
     
     //Module Laden
     
     rgi.logger("[CoreInit] Loading Modules & SubModules");
     
     rgi.logger("[CoreInit] Loading clientgamecontroller...");
     
     
     // gamecrtl wird weiter oben initialisiert
     //gamectrl = new ClientGameController(rgi);
     
     rgi.logger("[CoreInit] Loading graphicsengine");
     
     // Grafik initialisieren(und KI):
     rGraphics = new thirteenducks.cor.graphics.CoreGraphics(rgi, new Dimension(mode.getWidth(), mode.getHeight()), fullScreen);
     
     
     
     rgi.logger("[CoreInit]: Loading lobby...");
     // Lobby initialisieren und anzeigen:
     
     lobby.setVisible(true);
     
     rgi.logger("[CoreInit]: Loading client-netcontroller");
     
     netController = new ClientNetController(rgi, lobby);
     
     
     mapMod = new ClientMapModule(rgi);
     
     cchat = new ClientChat(rgi);
     rGraphics.content.overlays.add(cchat);
     
     cs = new ClientStatistics(rgi);
     
     
     
     
     rgi.initInner();
     
     /*
     
     mapMod.initModule();
     
     rGraphics.initModule();
     rGraphics.initSubs();
     
      */
 
     /*   rgi.logger("[Core]: Connecting to server...");
     
     if (netController.connectTo(connectTo, port)) {
     lobby.initlobby(netController, rgi);
     Thread t = new Thread(new Runnable() {
     
     @Override
     public void run() {
     soundM = new SoundModule();
     rgi.rogSound = soundM;
     //soundM.loopSound("wolfe.ogg");
     lobby.jButton2.setEnabled(true);
     }
     });
     t.start();
     
     } else {
     // Das geht so nicht, das ging nicht
     // Alles wieder abschalten
     lobby.dispose();
     rGraphics.destroy();
     throw new java.lang.RuntimeException("IP invalid/unreachable or server not running");
     }
     
     
     } */
     /**
      * Wird vom Hauptmenu aufgerufen, wenn zu einem Server connected werden soll.
      */
     public boolean joinServer(String playername, InetAddress adr, int port) {
         System.out.println("Try joining server: " + adr + " port " + port);
         this.playername = playername;
         lobby = this.rGraphics.getMainmenu().getLobby();
         rgi = new InnerClient(playername);
         gamectrl = new ClientGameController(rgi);
         initLogger();
         netController = new ClientNetController(rgi, lobby);
         mapMod = new ClientMapModule(rgi);
         cchat = new ClientChat(rgi);
         cs = new ClientStatistics(rgi);
         rgi.initInner();
         rGraphics.setInner(rgi);
         System.out.println("Pre-setup complete, connecting...");
         if (netController.connectTo(adr, port)) {
             // Namenswunsch an den Serve senden:
             lobby.send('N' + this.playername);
            this.rGraphics.getMainmenu().showlobby();
             return true;
         } else {
             //lobby.dispose();
             return false;
         }
     }
 
     @Override
     public void initLogger() {
         if (!Core.logOFF) {
             // Erstellt ein neues Logfile
             try {
                 FileWriter logcreator = new FileWriter("client_log.txt");
                 logcreator.close();
             } catch (IOException ex) {
                 // Warscheinlich darf man das nicht, den Adminmodus emfehlen
                 JOptionPane.showMessageDialog(new JFrame(), "Cannot write to logfile. Please start CoR as Administrator", "admin required", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
                 rgi.shutdown(2);
             }
         }
     }
 
     public static void main(String[] args) {
         System.out.println("Starting up...");
         // Settings einlesen
         System.out.println("Reading settings...");
         HashMap<String, String> cfg = readCfg();
         new ClientCore(cfg);
     }
 
     private static HashMap readCfg() {
         HashMap<String, String> cfg = new HashMap<String, String>();
         File cfgFile = null;
         try {
             cfgFile = new File("client_cfg.txt");
             FileReader cfgReader = new FileReader(cfgFile);
             BufferedReader reader = new BufferedReader(cfgReader);
             String zeile = null;
             int i = 0; // Anzahl der Durchläufe zählen
             while ((zeile = reader.readLine()) != null) {
                 if (i == 0) {
                     // Die erste Zeile überspringen
                     //   continue;
                 }
                 // Liest Zeile fuer Zeile, jetzt auswerten und in Variablen
                 // schreiben
                 int indexgleich = zeile.indexOf('='); // Istgleich suchen
                 if (indexgleich == -1) {
                 } else {
                     String v1 = zeile.substring(0, indexgleich); // Vor dem =
                     // rauschneiden
                     String v2 = zeile.substring(indexgleich + 1); // Nach dem
                     // =
                     // rausschneiden
                     System.out.println(v1 + " = " + v2);
                     cfg.put(v1, v2);
 
                 }
             }
             reader.close();
         } catch (FileNotFoundException e1) {
             // cfg-Datei nicht gefunden -  egal, wird automatisch neu angelegt
             System.out.println("client_cfg.txt not found, creating new one...");
             try {
                 cfgFile.createNewFile();
             } catch (IOException ex) {
                 System.out.println("[ERROR] Failed to create client_cfg.txt .");
             }
         } catch (IOException e2) {
             // Inakzeptabel
             e2.printStackTrace();
             System.out.println("[ERROR] Critical I/O ERROR!");
             System.exit(1);
         }
         return cfg;
     }
 
     public class InnerClient extends Core.CoreInner {
 
         public CoreGraphics rogGraphics;
         public ClientMapModule mapModule;
         public ClientNetController netctrl;
         public ClientGameController game;
         public ClientChat chat;
         public ClientStatistics clientstats;
         public String playername;
         //RogPathfinder rogPathfinder;
         //RogGameLogic rogGameLogic;
         public SoundModule rogSound;
         public String lastlog = "";
         public TeamSelector teamSel;
 
         public InnerClient() {
         }
 
         private InnerClient(String playername_t) {
             Client.setInnerClient(this);
             playername = playername_t;
         }
 
         @Override
         public void initInner() {
             super.initInner();
             rogGraphics = rGraphics;
             mapModule = mapMod;
             netctrl = netController;
             game = gamectrl;
             chat = cchat;
             clientstats = cs;
             teamSel = new TeamSelector(this);
             //rogGameLogic = rGameLogic;
             //rogSound = rSound; */
         }
 
         @Override
         public void logger(String x) {
             if (!logOFF) {
                 if (!lastlog.equals(x)) { // Nachrichten nicht mehrfach speichern
                     lastlog = x;
                     // Schreibt den Inhalt des Strings zusammen mit dem Zeitpunkt in die
                     // log-Datei
                     try {
                         FileWriter logwriter = new FileWriter("client_log.txt", true);
                         String temp = String.format("%tc", new Date()) + " - " + x + "\n";
                         logwriter.append(temp);
                         logwriter.flush();
                         logwriter.close();
 
 
                     } catch (IOException ex) {
                         ex.printStackTrace();
                         shutdown(2);
                     }
                 }
             }
         }
 
         @Override
         public void logger(Throwable t) {
             if (!logOFF) {
                 // Nimmt Exceptions an und schreibt den Stacktrace ins
                 // logfile
                 try {
                     if (debugmode) {
                         System.out.println("ERROR!!!! More info in logfile...");
                     }
                     FileWriter logwriter = new FileWriter("client_log.txt", true);
                     logwriter.append('\n' + String.format("%tc", new Date()) + " - ");
                     logwriter.append("[JavaError]:   " + t.toString() + '\n');
                     StackTraceElement[] errorArray;
                     errorArray = t.getStackTrace();
                     for (int i = 0; i < errorArray.length; i++) {
                         logwriter.append("            " + errorArray[i].toString() + '\n');
                     }
                     logwriter.flush();
                     logwriter.close();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                     shutdown(2);
                 }
             }
         }
     }
 }
