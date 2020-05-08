 package com.imie.morpion;
 
 import com.imie.morpion.controller.NetworkClient;
 import com.imie.morpion.controller.NetworkController;
 import com.imie.morpion.controller.NetworkServer;
 import com.imie.morpion.model.Game;
import com.imie.morpion.model.SquareState;
 import com.imie.morpion.view.Window;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.UUID;
 
 /**
  * @author Marc-Antoine Perennou<Marc-Antoine@Perennou.com>
  */
 public class App {
 
    public static void main(String[] args) throws Exception {
       boolean server = (args.length == 1 && args[0].compareTo("--server") == 0);
       String id = UUID.randomUUID().toString();
 
       Window window = new Window((server) ? "server" : "client");
       window.setVisible(true);
 
      Game game = new Game((server) ? SquareState.P1 : SquareState.P2);
       game.addGameListener(window);
       game.onStateUpdate();
 
       final NetworkController nc = (server) ? new NetworkServer(game, id) : new NetworkClient(game, id);
       nc.start();
       nc.joinGame();
 
       window.addBoardListener(nc);
       window.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent winEvt) {
             nc.quit();
          }
       });
    }
 }
