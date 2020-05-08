 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.server;
 
 import capstone.game.GameSession;
 import capstone.player.GameRecord;
 import java.util.List;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class GameRecorder {
 
     private static Map<String, List<String>> gameIDs = new ConcurrentHashMap<String, List<String>>();  //PlayerName -> GameID
     private static Map<String, GameRecord> players = new ConcurrentHashMap<String, GameRecord>();      //GameID -> GameRecord
 
     public static void record(String gameID, String player, String coords) {
 
         if (!gameIDs.containsKey(player)) {
             gameIDs.put(player, new ArrayList<String>());
         }
         if (gameIDs.get(player).indexOf(gameID) == -1) {
             GameRecord game = new GameRecord();
             game.putCoords(coords);
            players.put(gameID, game);
             gameIDs.get(player).add(gameID);
         } 
         else {
             players.get(gameID).putCoords(coords);
         }
     }
 }
 
