 package com.steamedpears.comp3004.views;
 
 import com.steamedpears.comp3004.models.Card;
 import com.steamedpears.comp3004.models.Player;
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.*;
 import java.util.List;
 
 public class PlayedCardsView extends JPanel {
     private static final int SELECTED_MULTIPLIER = 4;
 
     private Player player;
     private CardView selectedCardView;
 
     public PlayedCardsView(Player player) {
         this.player = player;
         setLayout(new MigLayout("wrap 9"));
         update();
     }
 
     /**
      * Update the view.
      */
     public void update() {
         removeAll();
         selectedCardView = null;
 
         List<Card> playedCards = player.getPlayedCards();
         if(playedCards.size() == 0) return;
 
         selectedCardView = new CardView(playedCards.get(0),CardView.DEFAULT_WIDTH * SELECTED_MULTIPLIER);
         add(selectedCardView,"span " + SELECTED_MULTIPLIER + " " + SELECTED_MULTIPLIER);
 
         for(Card c : playedCards) {
             CardView cv = new CardView(c);
             cv.setSelectionListener(new CardSelectionListener() {
                 @Override
                 public void handleSelection(Card card) {
                     selectedCardView.setCard(card);
                 }
             });
             add(cv);
         }
 
         WonderView wonderView = new WonderView(player.getWonder());
        wonderView.setWonderWidth(650);
         add(wonderView,"cell 0 4, span");
     }
 }
