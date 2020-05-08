 package com.steamedpears.comp3004.views;
 import com.steamedpears.comp3004.models.*;
 
 import static com.steamedpears.comp3004.models.PlayerCommand.*;
 import net.miginfocom.swing.MigLayout;
 import org.apache.log4j.*;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 import java.util.Timer;
 
 public class PlayerView extends JPanel {
     private static final int SELECTED_MULTIPLIER = 4;
 
     static Logger logger = Logger.getLogger(PlayerView.class);
 
     private Player player;
     private CardView selectedCardView;
     private Map<String, String> persistentMessages;
     private int timer = 0;
     private Timer scheduledUpdateTimer;
 
     private JLabel messageLabel;
     private JLabel persistentMessageLabel;
     private JButton discardButton;
     private JButton playButton;
     private JButton buildButton;
 
     public PlayerView(Player player) {
         logger.info("Created with player[" + player + "]");
         setLayout(new MigLayout());
         this.player = player;
         persistentMessages = new HashMap<String, String>();
         update();
         scheduledUpdateTimer = new Timer();
         scheduledUpdateTimer.scheduleAtFixedRate(new TimerTask() {
             @Override
             public void run() {
                 updateTimer();
             }
         },new Date(),1000);
     }
 
     /**
      * Set the ephemeral message on this view.
      * @param message The ephemeral message to be displayed.
      */
     public void setMessage(String message) {
         if(messageLabel == null) return;
         messageLabel.setText(message);
     }
 
     /**
      * Add a persistent message to this view.
      * @param key The key to which to assign this message (for easy removal).
      * @param message The persistent message to be displayed.
      */
     public void addMessage(String key, String message) {
         persistentMessages.put(key,message);
         updatePersistentMessages();
     }
 
     private void updatePersistentMessages() {
         StringBuffer message = new StringBuffer();
         for(String s : persistentMessages.values()) {
             message.append(s);
             message.append("\n");
         }
         persistentMessageLabel.setText(message.toString());
     }
 
     /**
      * Update the view.
      */
     public void update() {
         removeAll();
 
         // cards in hand
         List<Card> hand = player.getHand();
         for(Card c : hand) {
             CardView cv = new CardView(c);
             cv.setSelectionListener(new CardSelectionListener(){
                 @Override
                 public void handleSelection(Card card) {
                     logger.info("Selecting " + card);
                     if(selectedCardView != null) {
                         selectedCardView.setCard(card);
                     }
                 }
             });
             add(cv, "aligny top");
         }
 
         // selected card
         if(hand.size() > 0) {
             selectedCardView = new CardView(hand.get(0), CardView.DEFAULT_WIDTH * SELECTED_MULTIPLIER);
             add(selectedCardView, "newline, span " + SELECTED_MULTIPLIER);
         }
 
         // group action buttons together
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(new MigLayout());
 
         // discard button
         discardButton = new JButton("Discard");
         discardButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 doneMove();
                 PlayerCommand move = new PlayerCommand();
                 move.action = PlayerCardAction.DISCARD;
                 move.card = selectedCardView.getCard().getId();
                 player.setCurrentCommand(move);
                 player.wake();
             }
         });
         buttonPanel.add(discardButton,"span");
 
         // play button
         playButton = new JButton("Play");
         playButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 doneMove();
                 PlayerCommand move = new PlayerCommand();
                 move.action = PlayerCardAction.PLAY;
                 move.card = selectedCardView.getCard().getId();
                 player.setCurrentCommand(move);
                 player.wake();
             }
         });
         // TODO: disable if can't actually play
         buttonPanel.add(playButton,"span");
 
         // build wonder button
         buildButton = new JButton("Build Wonder");
         buildButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 doneMove();
                 PlayerCommand move = new PlayerCommand();
                 move.action = PlayerCardAction.BUILD;
                 move.card = selectedCardView.getCard().getId();
                 player.setCurrentCommand(move);
                 player.wake();
             }
         });
         if(player.hasFinishedWonder()) {
             buildButton.setEnabled(false);
         }
         // TODO: disable if can't actually build
         buttonPanel.add(buildButton,"span");
 
         add(buttonPanel,"span 2");
 
         // messages
         JPanel messagePanel = new JPanel();
         messagePanel.setLayout(new MigLayout());
 
         messageLabel = new JLabel("The game has begun!");
         messagePanel.add(messageLabel, "span");
 
         persistentMessageLabel = new JLabel("");
         messagePanel.add(persistentMessageLabel,"span");
 
         add(messagePanel,"span");
 
         // assets
         add(new AssetView(this.player),"gapleft 25, gaptop 1, span");
 
        // TODO: add some way to switch to viewing another player
        // TODO: add some way to switch to high level view

         newMove();
     }
 
     public void newMove() {
         buildButton.setEnabled(true);
         playButton.setEnabled(true);
         discardButton.setEnabled(true);
         timer = SevenWondersGame.TURN_LENGTH / 1000;
         updateTimer();
     }
 
     public void doneMove() {
         buildButton.setEnabled(false);
         playButton.setEnabled(false);
         discardButton.setEnabled(false);
         timer = 0;
         updateTimer();
     }
 
     private void updateTimer() {
         if(timer <= 0) {
             setMessage("Move chosen.");
         } else {
             int minutes = timer / 60;
             int seconds = timer % 60;
            setMessage(minutes + ":" + seconds + " left to choose a move.");
             --timer;
         }
     }
 }
