 package fivecardstud;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Status {
     static int handSize;
     static int numPlayers;
     List<Player> players;
     
     public Status() {
        handSize = 5;
         numPlayers = 2;
         players = new ArrayList<>();
     }
 }
