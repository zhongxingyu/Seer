 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.etsai.kfsxtrackingserver.stats;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Stores a collection of stats for a player ID and a category group
  * @author etsai
  */
 public class Player {
     private final int id;
    private final String steamid;
     private String category;
     private Map<String, Integer> stats;
     
     public static Player build(ResultSet rs) throws SQLException {
        Player player= new Player(rs.getInt("id"), rs.getString("steamid"));
         
        player.category= rs.getString("category");
         String[] stats= rs.getString("stats").split(",");
         for(String stat: stats) {
             String[] split= stat.split("=");
             player.stats.put(split[0], Integer.valueOf(split[1]));
         }
         return player;
     }
     
    public Player(int id, String steamid) {
         this.id= id;
        this.steamid= steamid;
         stats= new HashMap<>();
     }
     
     public int getId() {
         return id;
     }
    public String getSteamId() {
        return steamid;
     }
     public String getCategory() {
         return category;
     }
     public synchronized Set<String> getStatKeys() {
         return Collections.unmodifiableSet(stats.keySet());
     }
     public synchronized Integer getStat(String stat) {
         return stats.get(stat);
     }
     public synchronized Map<String, Integer> getStats() {
         return Collections.unmodifiableMap(stats);
     }
     public synchronized void accumulate(String stat, int offset) {
         Integer value;
         
         if (!stats.containsKey(stat)) {
             stats.put(stat, 0);
         }
         value= stats.get(stat);
         value+= offset;
         stats.put(stat, value);
     }
 }
