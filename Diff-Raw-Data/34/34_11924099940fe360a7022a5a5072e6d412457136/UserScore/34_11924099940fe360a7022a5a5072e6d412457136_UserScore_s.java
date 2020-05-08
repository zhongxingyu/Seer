 // $Id$
 
 package me.kukkii.janken.log;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import me.kukkii.janken.Player;
 import me.kukkii.janken.Result;
 
 public class UserScore implements Serializable {
 
   // private Map<Player, int[]> scoreMap;
   private Map<String, int[]> scoreMap;
 
   public UserScore() {
     // scoreMap = new HashMap<Player, int[]>();
     scoreMap = new HashMap<String, int[]>();
   }
 
   public int[] getScore(Player player) {
     return getScore(player.getName());
   }
 
   public int[] getScore(String name) {
     int[] scores = scoreMap.get(name);
     return (scores == null) ? (new int[3]) : scores;
   }
 
   public void setScore(Player player, int[] scores) {
     setScore(player.getName(), scores);
   }
 
   public void setScore(String name, int[] scores) {
     scoreMap.put(name, scores);
   }
 
   public void addScore(Player player, Result result) {
     addScore(player.getName(), result);
   }
 
   public void addScore(String name, Result result) {
     int[] scores = getScore(name);
     if (scores == null) {
       scores = new int[3];
       // setScore(name, scores);
     }
     addScore(scores, result);
 
     scores = getScore((String)null);
     if (scores == null) {
       scores = new int[3];
      setScore((String)null, scores);
     }
     addScore(scores, result);
    setScore(name, scores);
   }
 
   private void addScore(int[] scores, Result result) {
     switch (result) {
     case WIN:
       scores[0] += 1;
       break;
     case LOSE:
       scores[1] += 1;
       break;
     case DRAW:
     default:
       scores[2] += 1;
       break;
     }
   }
 
 }
