 /*
  *  Copyright 2012 Lolmewn <lolmewn@centrility.nl>.
  */
 
 package nl.lolmewn.sortal;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  *
  * @author Lolmewn <lolmewn@centrility.nl>
  */
 public class UserInfo {
     
     private String username;
     
     private HashMap<String, Integer> warpUses = new HashMap<String, Integer>();
     private HashMap<Location, Integer> locUses = new HashMap<Location, Integer>();
 
     public UserInfo(String user) {
         this.username = user;
     }
     
     public boolean hasUsedWarp(String warp){
         return this.warpUses.containsKey(warp);
     }
     
     public int getUsedWarp(String warp){
         if(!this.warpUses.containsKey(warp)){
             return 0;
         }
         return this.warpUses.get(warp);
     }
     
     public void addtoUsedWarp(String warp, int add){
         if(!this.hasUsedWarp(warp)){
             this.warpUses.put(warp, add);
             return;
         }
         this.warpUses.put(warp, this.warpUses.get(warp)+add);
     }
     
     public boolean hasUsedLocation(Location loc){
         return this.locUses.containsKey(loc);
     }
     
     public int getUsedLocation(Location loc){
         if(!this.locUses.containsKey(loc)){
             return 0;
         }
         return this.locUses.get(loc);
     }
     
     public void addtoUsedLocation(Location loc, int add){
         if(!this.hasUsedLocation(loc)){
             this.locUses.put(loc, 1);
             return;
         }
         this.locUses.put(loc, this.locUses.get(loc)+add);
     }
     
     public void save(MySQL m, String table){
         for(String warp : this.warpUses.keySet()){
             ResultSet set = m.executeQuery("SELECT * FROM " + table + " WHERE warp='" + warp + "' AND player='" + username + "'");
             if(set == null){
                 //thats weird..
                 continue;
             }
             try {
                 boolean found = false;
                 while(set.next()){
                    //is already in the table
                     m.executeStatement("UPDATE " + table + " SET used=" + this.getUsedWarp(warp) + " WHERE warp='" + warp + "' AND player='" + this.username + "'");
                     found = true;
                     break;
                 }
                 if(!found){
                     m.executeStatement("INSERT INTO " + table + "(player, warp, used) VALUES ('" + this.username + "', '" + warp + "', " + this.getUsedWarp(warp) + ")");  
                 }                
             } catch (SQLException ex) {
                 Bukkit.getLogger().log(Level.SEVERE, null, ex);
             }
         }
         for(Location loc : this.locUses.keySet()){
             ResultSet set = m.executeQuery("SELECT * FROM " + table + " WHERE "
                     + "x=" + loc.getBlockX() + " AND "
                     + "y=" + loc.getBlockY() + " AND "
                     + "z=" + loc.getBlockZ() + " AND "
                     + "world='" + loc.getWorld().getName() + "'");
             if(set == null){
                 //thats weird..
                 continue;
             }
             try {
                 boolean found = false;
                 while(set.next()){
                    //is already in the table
                     m.executeStatement("UPDATE " + table + " SET used=" + this.locUses.get(loc) + " WHERE "
                     + "x=" + loc.getBlockX() + " AND "
                     + "y=" + loc.getBlockY() + " AND "
                     + "z=" + loc.getBlockZ() + " AND "
                     + "world='" + loc.getWorld().getName() + "'");
                     found = true;
                     break;
                 }
                 if(!found){
                     m.executeStatement("INSERT INTO " + table + "(player, x, y, z, world, used) VALUES ('" + 
                             this.username + "', " + 
                             loc.getBlockX() + ", " + 
                             loc.getBlockY() + ", " + 
                             loc.getBlockZ() + ", '" + 
                             loc.getWorld().getName() + "',"
                             + this.getUsedLocation(loc) + ")");  
                 }                
             } catch (SQLException ex) {
                 Bukkit.getLogger().log(Level.SEVERE, null, ex);
             }
         }
         
     }
     
     public void save(File f){
         if(!f.exists()){
             try {
                 f.createNewFile();
             } catch (IOException ex) {
                 Bukkit.getLogger().log(Level.SEVERE, null, ex);
             }
         }
         YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
         for(String warp : this.warpUses.keySet()){
            c.set(this.username + "." + warp, this.warpUses.get(warp));
         }
         for(Location loc : this.locUses.keySet()){
            c.set(this.username + "." + loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), this.locUses.get(loc));
         }
         try {
             c.save(f);
         } catch (IOException ex) {
             Bukkit.getLogger().log(Level.SEVERE, null, ex);
         }
     }
 
 }
