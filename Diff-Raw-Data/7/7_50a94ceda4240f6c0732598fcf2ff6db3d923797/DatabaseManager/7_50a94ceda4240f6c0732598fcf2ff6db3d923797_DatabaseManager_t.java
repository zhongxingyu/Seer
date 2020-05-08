 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of MineStarWarp.
  * 
  * MineStarWarp is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * MineStarWarp is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MineStarWarp.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.minestar.MineStarWarp.dataManager;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.TreeMap;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import com.minestar.MineStarWarp.Main;
 import com.minestar.MineStarWarp.Warp;
 
 public class DatabaseManager {
 
     // The connection to the SQLLite Database
     private final Connection con = ConnectionManager.getConnection();
 
     private final Server server;
 
     // PreparedStatements for the often used SQLLite Queries.
     private PreparedStatement addWarp = null;
     private PreparedStatement deleteWarp = null;
     private PreparedStatement changeGuestList = null;
     private PreparedStatement addHome = null;
     private PreparedStatement updateHome = null;
     private PreparedStatement convertToPublic = null;
     private PreparedStatement addSpawn = null;
     private PreparedStatement updateSpawn = null;
 
     /**
      * Uses for all database transactions
      * 
      * @param server
      */
     public DatabaseManager(Server server) {
         this.server = server;
         try {
             // create tables if not exists and compile the prepare Statements
             initiate();
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
         }
     }
 
     /**
      * Create the tables 'warps' and 'home' if not existing. Also compiles the
      * PreparedStatements.
      * 
      * @throws Exception
      */
     private void initiate() throws Exception {
         // check the database structure
         createTables();
         addWarp = con
                 .prepareStatement("INSERT INTO warps (name, creator, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?,?);");
         deleteWarp = con.prepareStatement("DELETE FROM warps WHERE name = ?;");
         changeGuestList = con
                 .prepareStatement("UPDATE warps SET permissions = ? WHERE name = ?;");
         addHome = con
                 .prepareStatement("INSERT INTO homes (player,world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?);");
         updateHome = con
                 .prepareStatement("UPDATE homes SET world = ? , x = ? , y = ? , z = ? , yaw = ? , pitch = ? WHERE player = ?;");
         convertToPublic = con
                 .prepareStatement("UPDATE warps SET permissions = null WHERE name = ?");
         addSpawn = con
                 .prepareStatement("INSERT INTO spawns (world,x,y,z,yaw,pitch) VALUES (?,?,?,?,?,?)");
         updateSpawn = con
                 .prepareStatement("UPDATE spawns SET  x = ? , y = ? , z = ? , yaw = ? , pitch = ? WHERE world = ?");
 
     }
 
     /**
      * Create the table 'warps' and 'home' when they are not exist.
      */
     private void createTables() throws Exception {
         // create the table for storing the warps
         con.createStatement().executeUpdate(
                 "CREATE TABLE IF NOT EXISTS `warps` ("
                         + "`id` INTEGER PRIMARY KEY,"
                         + "`name` varchar(32) NOT NULL DEFAULT 'warp',"
                         + "`creator` varchar(32) NOT NULL DEFAULT 'Player',"
                         + "`world` varchar(32) NOT NULL DEFAULT '0',"
                         + "`x` DOUBLE NOT NULL DEFAULT '0',"
                         + "`y` tinyint NOT NULL DEFAULT '0',"
                         + "`z` DOUBLE NOT NULL DEFAULT '0',"
                         + "`yaw` smallint NOT NULL DEFAULT '0',"
                         + "`pitch` smallint NOT NULL DEFAULT '0',"
                         + "`publicAll` boolean NOT NULL DEFAULT '1',"
                         + "`permissions` text DEFAULT null);");
         con.commit();
 
         // create the table for storing the homes
         con.createStatement().executeUpdate(
                 "CREATE TABLE IF NOT EXISTS `homes` ("
                         + "`player` varchar(32) PRIMARY KEY,"
                         + "`world` varchar(32) NOT NULL DEFAULT '0',"
                         + "`x` DOUBLE NOT NULL DEFAULT '0',"
                         + "`y` tinyint NOT NULL DEFAULT '0',"
                         + "`z` DOUBLE NOT NULL DEFAULT '0',"
                         + "`yaw` smallint NOT NULL DEFAULT '0',"
                         + "`pitch` smallint NOT NULL DEFAULT '0');");
         con.commit();
 
         // create the table for storing the homes
         con.createStatement().executeUpdate(
                 "CREATE TABLE IF NOT EXISTS `spawns` ("
                         + "`world` varchar(32) PRIMARY KEY,"
                         + "`x` DOUBLE NOT NULL DEFAULT '0',"
                         + "`y` tinyint NOT NULL DEFAULT '0',"
                         + "`z` DOUBLE NOT NULL DEFAULT '0',"
                         + "`yaw` smallint NOT NULL DEFAULT '0',"
                         + "`pitch` smallint NOT NULL DEFAULT '0');");
         con.commit();
 
     }
 
     /**
      * Load the Warps from the Database by and put them into a TreeMap. This
      * should only loaded onEnabled()
      * 
      * @return A TreeMap where the key the name of the warp is
      */
     public TreeMap<String, Warp> loadWarpsFromDatabase() {
 
         TreeMap<String, Warp> warps = new TreeMap<String, Warp>();
         try {
             ResultSet rs = con
                     .createStatement()
                     .executeQuery(
                             "SELECT name,creator,world,x,y,z,yaw,pitch,permissions FROM warps");
             while (rs.next()) {
 
                 String name = rs.getString(1);
                 String creator = rs.getString(2);
                 String world = rs.getString(3);
                 Location loc = new Location(server.getWorld(world),
                         rs.getDouble(4), rs.getInt(5), rs.getDouble(6),
                         rs.getInt(7), rs.getInt(8));
                 String guestsList = rs.getString(9);
                 Warp warp = new Warp(creator, loc,
                         this.convertsGuestsToList(guestsList));
                 warps.put(name, warp);
             }
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
         }
         Main.writeToLog("Loaded sucessfully " + warps.size() + " Warps");
         return warps;
     }
 
     /**
      * Load the homes from the database and put them into a TreeMap. This should
      * only loaded onEnabled()
      * 
      * @return A TreeMap where the key the name of the player is
      */
     public TreeMap<String, Location> loadHomesFromDatabase() {
 
         TreeMap<String, Location> homes = new TreeMap<String, Location>();
         try {
             ResultSet rs = con.createStatement().executeQuery(
                     "SELECT player,world,x,y,z,yaw,pitch FROM homes");
             while (rs.next()) {
 
                 String name = rs.getString(1);
                 String world = rs.getString(2);
                 Location loc = new Location(server.getWorld(world),
                         rs.getDouble(3), rs.getInt(4), rs.getDouble(5),
                         rs.getInt(6), rs.getInt(7));
                 homes.put(name, loc);
             }
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
         }
         Main.writeToLog("Loaded sucessfully " + homes.size() + " Homes");
         return homes;
     }
 
     /**
      * Load the spawn from the database and put them into a TreeMap. This should
      * only loaded onEnabled()
      * 
      * @return A TreeMap where the world name the key is
      */
     public TreeMap<String, Location> loadSpawnsFromDatabase() {
 
         TreeMap<String, Location> spawns = new TreeMap<String, Location>();
         try {
             ResultSet rs = con.createStatement().executeQuery(
                     "SELECT world,x,y,z,yaw,pitch FROM spawns");
             while (rs.next()) {
 
                String world = rs.getString(1);
                 Location loc = new Location(server.getWorld(world),
                        rs.getDouble(2), rs.getInt(3), rs.getDouble(4),
                        rs.getInt(5), rs.getInt(6));
                 spawns.put(world, loc);
             }
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
         }
         Main.writeToLog("Loaded sucessfully " + spawns.size() + " Spawns");
         return spawns;
 
     }
 
     /**
      * This saves the warp, which a player has created ingame, into the
      * database.
      * 
      * @param creator
      *            Player who is calling /warp create Name
      * @param name
      *            The name of the warp
      * @param warp
      *            The warp it selfs, just storing the
      * @return True when the warp is sucessfully added into the database
      */
     public boolean addWarp(Player creator, String name, Warp warp) {
         try {
             Location loc = warp.getLoc();
             // INSERT INTO warps (name, creator, world, x, y, z, yaw, pitch)
             // VALUES (?,?,?,?,?,?,?,?);
             addWarp.setString(1, name);
             addWarp.setString(2, creator.getName());
             addWarp.setString(3, creator.getWorld().getName());
             addWarp.setDouble(4, loc.getX());
             addWarp.setInt(5, loc.getBlockY());
             addWarp.setDouble(6, loc.getZ());
             addWarp.setInt(7, Math.round(loc.getYaw()) % 360);
             addWarp.setInt(8, Math.round(loc.getPitch()) % 360);
             addWarp.executeUpdate();
             con.commit();
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
             return false;
         }
         return true;
     }
 
     /**
      * When a player has not set a home yet, this method will called. It stores
      * a complete new entity to the home table, where the player name is the
      * primary key, because a player can set one home
      * 
      * @param creator
      *            The command caller
      * @return True when the home is sucessfully added into the database
      */
     public boolean setHome(Player creator) {
         try {
             Location loc = creator.getLocation();
             // INSERT INTO homes (player,world, x, y, z, yaw, pitch) VALUES
             // (?,?,?,?,?,?,?);
             addHome.setString(1, creator.getName());
             addHome.setString(2, creator.getWorld().getName());
             addHome.setDouble(3, loc.getX());
             addHome.setInt(4, loc.getBlockY());
             addHome.setDouble(5, loc.getZ());
             addHome.setInt(6, Math.round(loc.getYaw()) % 360);
             addHome.setInt(7, Math.round(loc.getPitch()) % 360);
             addHome.executeUpdate();
             con.commit();
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
             return false;
         }
         return true;
     }
 
     /**
      * When a spawn for a world isn't set yet, this method is called. It creates
      * a new entity in the table spawns and uses the world name as the primary
      * key to avoid double spawn positions in one world
      * 
      * @param loc
      *            Location of the spawn
      * @return True when the spawn is sucessfully added into the database
      */
     public boolean addSpawn(Location loc) {
         try {
             // INSERT INTO spawns (world,x,y,z,yaw,pitch) VALUES (?,?,?,?,?,?)
             addSpawn.setString(1, loc.getWorld().getName());
             addSpawn.setDouble(2, loc.getX());
             addSpawn.setInt(3, loc.getBlockY());
             addSpawn.setDouble(4, loc.getZ());
             addSpawn.setInt(5, Math.round(loc.getYaw()) % 360);
             addSpawn.setInt(6, Math.round(loc.getPitch()) % 360);
             addSpawn.executeUpdate();
             con.commit();
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
             return false;
         }
         return true;
     }
 
     /**
      * When a player has already set a home, this method will called. It just
      * updates the location and not create a new entity
      * 
      * @param player
      *            The command caller
      * @return True when the location of the player is sucessfully updated
      */
     public boolean updateHome(Player player) {
         try {
             Location loc = player.getLocation();
             // UPDATE homes SET world = ? , x = ? , y = ? , z = ? , yaw = ? ,
             // pitch = ? WHERE name = ?;
             updateHome.setString(1, player.getWorld().getName());
             updateHome.setDouble(2, loc.getX());
             updateHome.setInt(3, loc.getBlockY());
             updateHome.setDouble(4, loc.getZ());
             updateHome.setInt(5, Math.round(loc.getYaw()) % 360);
             updateHome.setInt(6, Math.round(loc.getPitch()) % 360);
             updateHome.setString(7, player.getName());
             updateHome.executeUpdate();
             con.commit();
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
             return false;
         }
         return true;
     }
 
     /**
      * Changes to position of a spawn, which already exists in a world.
      * 
      * @param loc
      *            The new location of the spawn
      * @return True when the position is sucessfully updated
      */
     public boolean updateSpawn(Location loc) {
         try {
             // UPDATE spawns SET x = ? , y = ? , z = ? , yaw = ? , pitch = ?
             // WHERE world = ?
             updateSpawn.setDouble(1, loc.getX());
             updateSpawn.setInt(2, loc.getBlockY());
             updateSpawn.setDouble(3, loc.getZ());
             updateSpawn.setInt(4, Math.round(loc.getYaw()) % 360);
             updateSpawn.setInt(5, Math.round(loc.getPitch()) % 360);
             updateSpawn.setString(6, loc.getWorld().getName());
             updateSpawn.executeUpdate();
             con.commit();
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
             return false;
         }
         return true;
     }
 
     /**
      * Removes a warp from the database by deleting the entity from the table
      * 'warps'
      * 
      * @param name
      *            The name of the warp to delete
      * @return True when the warp is sucessfully deleted
      */
     public boolean deleteWarp(String name) {
         try {
             // DELETE FROM warps WHERE name = ?;
             deleteWarp.setString(1, name);
             deleteWarp.executeUpdate();
             con.commit();
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
             return false;
         }
         return true;
     }
 
     /**
      * Updates the guest list by changing the value in 'warps.permissions'.
      * 
      * @param guestList
      *            The new guestlist in a format name,name,name...
      * @param name
      *            The name of the warp
      * @return True when the list is sucessfully changed
      */
     public boolean changeGuestList(String guestList, String name) {
         try {
             // UPDATE warps SET permissions = ? WHERE name = ?;
             changeGuestList.setString(1, guestList);
             changeGuestList.setString(2, name);
             changeGuestList.executeUpdate();
             con.commit();
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
             return false;
         }
         return true;
     }
 
     /**
      * Converts the guest list, which is saved as a string in the format
      * name,name,name..., to an ArrayList for Strings
      * 
      * @param guestList
      *            The loaded string from the database
      * @return null if the guest list was null(this is a public warp) <br>
      *         an empty list when the guestList is empty(this is a private warp
      *         with no guests) <br>
      *         Otherwise the name of player which can use the warp in an
      *         ArrayList
      */
     private ArrayList<String> convertsGuestsToList(String guestList) {
 
         if (guestList == null)
             return null;
         if (guestList.equals(""))
             return new ArrayList<String>();
         ArrayList<String> guests = new ArrayList<String>();
         String[] split = guestList.split(";");
         guests.addAll(Arrays.asList(split));
 
         return guests;
     }
 
     /**
      * Set the value for 'permissions' to null to remove the guest list
      * 
      * @param name
      *            The name of the warp
      * @return True when the value is sucessfully set to null
      */
     public boolean removeGuestsList(String name) {
         try {
             // UPDATE warps SET permissions = null WHERE name = ?
             convertToPublic.setString(1, name);
             convertToPublic.executeUpdate();
             con.commit();
         }
         catch (Exception e) {
             Main.writeToLog(e.getMessage());
             return false;
         }
         return true;
     }
 }
