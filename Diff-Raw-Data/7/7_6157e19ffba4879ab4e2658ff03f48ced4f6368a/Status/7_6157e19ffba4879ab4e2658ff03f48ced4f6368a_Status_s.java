 package fivecardstud;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Status {
     static int handSize;
     static int numPlayers;
     List<Player> players;
     
     public Status() {
        handSize = 10;
         numPlayers = 2;
         players = new ArrayList<>();
        
        for (int i=0; i<numPlayers; i++) {
            players.add(new Player(i+1, "Player " + Integer.toString(i+1)));
        }
     }
 }
