 package com.steamedpears.comp3004.views;
 
 import com.steamedpears.comp3004.SevenWonders;
 import com.steamedpears.comp3004.models.players.Player;
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Map;
 
 public class ResultsDialog extends GameDialog {
     public ResultsDialog(JFrame f, final SevenWonders controller) {
         super(f,"End of Game Results",controller);
         setLayout(new MigLayout("wrap 2"));
         add(new JLabel("Player"));
         add(new JLabel("Score"));
         Map<Player,Integer> results = (controller.getGame()).tabulateResults();
         for(Player player : results.keySet()) {
             String title = "" + player.getPlayerId();
             Player thisPlayer = controller.getLocalPlayer();
             if(player.equals(thisPlayer)) {
                 title = "You";
             } else if(player.equals(thisPlayer.getPlayerLeft())) {
                 title = "Left";
             } else if(player.equals(thisPlayer.getPlayerRight())) {
                 title = "Right";
             }
             add(new JLabel(title));
             add(new JLabel("" + results.get(player)));
         }
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent windowEvent) {
                 // Since we can't get the program to restart cleanly...
                 // just kill it and force the user to restart!
                controller.openNewGameDialog();
             }
         });
         pack();
     }
 }
