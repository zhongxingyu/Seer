 package com.steamedpears.comp3004.views;
 
 import com.google.gson.*;
 import com.steamedpears.comp3004.models.*;
 import org.apache.log4j.*;
 
 import static org.mockito.Mockito.*;
 
 import javax.swing.*;
 import java.io.*;
 import java.util.*;
 
 public class PlayedCardsViewTest extends JFrame {
     public PlayedCardsViewTest() throws Exception {
         // initialization
        setBounds(0,0,960,560);
 
         // build played cards
         JsonArray cards = (new JsonParser())
                 .parse(new FileReader("src/main/resources/data/cards.json"))
                 .getAsJsonObject()
                 .get("cards")
                 .getAsJsonArray();
         ArrayList<Card> playedCards = new ArrayList<Card>();
         for(int i = 0; i < 17; ++i) {
             playedCards.add(new Card(cards.get(i).getAsJsonObject()));
         }
 
         // build wonders
         JsonArray wonders = (new JsonParser())
                 .parse(new FileReader("src/main/resources/data/wonderlist.json"))
                 .getAsJsonObject()
                 .get("wonders")
                 .getAsJsonArray();
 
         // mock player
         Player player = mock(Player.class);
         when(player.getPlayedCards()).thenReturn(playedCards);
         when(player.getWonder()).thenReturn(new Wonder(wonders.get(0).getAsJsonObject()));
 
         // add played cards view
         add(new PlayedCardsView(player));
 
         // display
         setVisible(true);
     }
 
     public static void main(String[] args) throws Exception {
         BasicConfigurator.configure();
         new PlayedCardsViewTest();
     }
 }
