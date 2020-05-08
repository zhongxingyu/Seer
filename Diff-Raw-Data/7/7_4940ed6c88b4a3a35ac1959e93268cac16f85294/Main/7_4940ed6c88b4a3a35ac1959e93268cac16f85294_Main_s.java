 package org.joedog.pinochle;
 
 import org.joedog.pinochle.controller.*;
 import org.joedog.pinochle.model.*;
 import org.joedog.pinochle.game.*;
 import org.joedog.pinochle.player.*;
 import org.joedog.pinochle.view.*;
 import org.joedog.pinochle.view.actions.*;
 
 import javax.swing.JFrame;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import javax.swing.WindowConstants;
 
 import javax.swing.UIManager;
 
 /**
  * @author Jeffrey Fulmer
  */
 public class Main {
   private GameController controller;
   private GameActions    actions;
   private GameView       view;
   private GameModel      model;
   private Splash         splash;
   private PlayerFactory  factory;
 
   public Main() {
     this.splash     = new Splash();
     this.controller = new GameController();
     splash.setMessage("Game controller");
     this.view       = new GameView(controller);
     splash.setMessage("initializing data");
     this.model      = new GameModel();
     splash.setMessage("initializing panel");
     this.actions    = new GameActions(controller);
 
     controller.addView(view);
     controller.addModel(model);
 
     splash.close();
     view.display(); 
 
     for ( ;; ) {
       while (controller.alive){this.play();}
       view.close();
       System.exit(0);
     }
   }
 
   public synchronized void play () {
     int turn          = 0;
     int status        = GameController.DEAL;
     this.factory      = new PlayerFactoryImpl();
     Player [] players = {
       factory.getPlayer(this.controller, Player.COMPUTER),
       factory.getPlayer(this.controller, Player.COMPUTER),
       factory.getPlayer(this.controller, Player.HUMAN), 
       factory.getPlayer(this.controller, Player.COMPUTER)
     };
     for (int i = 0; i < players.length; i++) {
       String name = "default";
       switch (i) {
         case Pinochle.NORTH:
           name = controller.getProperty("PlayerNorthName");
           break;
         case Pinochle.EAST:
           name = controller.getProperty("PlayerEastName");
           break;
         case Pinochle.SOUTH:
           name = controller.getProperty("PlayerSouthName");
           break;
         case Pinochle.WEST:
           name = controller.getProperty("PlayerWestName");
           break;
       }
       players[i].setup(view.getSetting(i), i, name);
     }
 
     int x = 0;
     while (status != GameController.OVER) {
       status = controller.gameStatus();
       switch (status) {
         case GameController.DEAL:
           controller.newDeal(players);
           break;
         case GameController.BID:
           controller.getBids(players);
           /*for (int y = 0; y < 12; y++) {
             for (int i = 0; i < players.length; i++) {
               try {
                 Thread.sleep(1000);
               } catch (Exception e) {
                 e.printStackTrace();
               }
               players[i].takeTurn();
             }
           }*/
           //try {
           //  Thread.sleep(18000);
           //} catch (Exception e) {
           //  e.printStackTrace();
           //}
           break;
         case GameController.PASS:
           controller.passCards(players);
           break;
         case GameController.MELD:
           controller.getMeld(players);
           try {
             Thread.sleep(82000);
           } catch (Exception e) {
             e.printStackTrace();
           }
           System.exit(0);
           break;
         case GameController.PLAY:
           break;
         case GameController.SCORE:
           break;
         case GameController.OVER:
           break;
       }
       /* FOR REFERENCE
       if ((players[turn%2].getType()).equals("HUMAN")) {
         controller.setStatus("Your turn...");
       } else {
         controller.setStatus("My turn...");
         players[turn%2].setEngine(controller.getEngine());
       }
       players[turn%2].takeTurn(); 
       */
       turn++;
     }
     //deck = new Deck(); // Just using this for deck debugging
     //players[0].finish(status);
     //players[1].finish(status);
     return;
   }
 
   public static void main(String[] args) {
     try {
       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
     }
     catch (javax.swing.UnsupportedLookAndFeelException e) {
      System.err.println("Blurt requires java-1.6 or higher");
     }
     catch (ClassNotFoundException e) {
      System.err.println("Blurt requires java-1.6 or higher");
     }
     catch (InstantiationException e) {
     }
     catch (IllegalAccessException e) {
     }
     Main pinochle = new Main();
   }
 }
 
