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
 
 package de.minestar.MineStarWarp.database;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.TreeMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import de.minestar.MineStarWarp.Core;
 import de.minestar.MineStarWarp.Warp;
 import de.minestar.minestarlibrary.database.AbstractDatabaseHandler;
 import de.minestar.minestarlibrary.database.DatabaseConnection;
 import de.minestar.minestarlibrary.database.DatabaseType;
 import de.minestar.minestarlibrary.database.DatabaseUtils;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class DatabaseManager extends AbstractDatabaseHandler {
 
     // Prepared Statements
     private PreparedStatement addWarp;
     private PreparedStatement deleteWarp;
     private PreparedStatement changeGuestList;
     private PreparedStatement updateWarp;
     private PreparedStatement renameWarp;
     private PreparedStatement addHome;
     private PreparedStatement updateHome;
     private PreparedStatement convertToPublic;
     private PreparedStatement setBank;
     private PreparedStatement updateBank;
     // /Prepared Statements
 
     public DatabaseManager(String pluginName, File dataFolder) {
         super(pluginName, dataFolder);
     }
 
     @Override
     protected DatabaseConnection createConnection(String pluginName, File dataFolder) throws Exception {
         File configFile = new File(dataFolder, "sqlconfig.yml");
         YamlConfiguration config = new YamlConfiguration();
 
         if (!configFile.exists()) {
             DatabaseUtils.createDatabaseConfig(DatabaseType.SQLLite, configFile, pluginName);
             return null;
         }
 
         config.load(configFile);
         return new DatabaseConnection(pluginName, DatabaseType.SQLLite, config);
     }
 
     @Override
     protected void createStructure(String pluginName, Connection con) throws Exception {
         DatabaseUtils.createStructure(getClass().getResourceAsStream("/structure.sql"), con, pluginName);
     }
 
     @Override
     protected void createStatements(String pluginName, Connection con) throws Exception {
 
         this.addWarp = con.prepareStatement("INSERT INTO warps (name, creator, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?,?)");
 
         this.deleteWarp = con.prepareStatement("DELETE FROM warps WHERE name = ?");
 
         this.changeGuestList = con.prepareStatement("UPDATE warps SET permissions = ? WHERE name = ?");
 
         this.updateWarp = con.prepareStatement("UPDATE warps SET world = ? , x = ? , y = ? , z = ? , yaw = ? , pitch = ? WHERE name = ?");
 
         this.renameWarp = con.prepareStatement("UPDATE warps SET name = ? WHERE name = ?");
 
         this.addHome = con.prepareStatement("INSERT INTO homes (player,world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?)");
 
         this.updateHome = con.prepareStatement("UPDATE homes SET world = ? , x = ? , y = ? , z = ? , yaw = ? , pitch = ? WHERE player = ?");
 
         this.convertToPublic = con.prepareStatement("UPDATE warps SET permissions = null WHERE name = ?");
 
         this.setBank = con.prepareStatement("INSERT INTO banks (player,world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?)");
 
         this.updateBank = con.prepareStatement("UPDATE banks SET world = ? , x = ? , y = ? , z = ? , yaw = ? , pitch = ? WHERE player = ?");
 
     }
 
     @Override
     public void updateDatabase(String pluginName, Connection con, File dataFolder) throws Exception {
         File update = new File(dataFolder, "update.sql");
         if (update.exists()) {
             ConsoleUtils.printInfo(pluginName, "Start to import sql changes");
             DatabaseUtils.createStructure(update, con, pluginName);
             // rename file to prevent double import
             update.renameTo(new File(dataFolder, "update_done.sql"));
         }
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
             ResultSet rs = dbConnection.getConnection().createStatement().executeQuery("SELECT name,creator,world,x,y,z,yaw,pitch,permissions FROM warps");
             while (rs.next()) {
 
                 String name = rs.getString(1);
                 String creator = rs.getString(2);
                 String world = rs.getString(3);
                 Location loc = new Location(Bukkit.getWorld(world), rs.getDouble(4), rs.getDouble(5), rs.getDouble(6), rs.getInt(7), rs.getInt(8));
                 String guestsList = rs.getString(9);
                 Warp warp = new Warp(creator, loc, this.guestListAsSet(guestsList));
                 warps.put(name, warp);
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't loading all warps from database!");
         }
         ConsoleUtils.printInfo(Core.NAME, "Warps loaded: " + warps.size());
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
             ResultSet rs = dbConnection.getConnection().createStatement().executeQuery("SELECT player,world,x,y,z,yaw,pitch FROM homes");
             while (rs.next()) {
 
                 String name = rs.getString(1).toLowerCase();
                 String world = rs.getString(2);
                 Location loc = new Location(Bukkit.getWorld(world), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getInt(6), rs.getInt(7));
                 homes.put(name, loc);
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't loading all homes from database!");
         }
         ConsoleUtils.printInfo(Core.NAME, "Homes loaded: " + homes.size());
         return homes;
     }
 
     /**
      * Load the homes from the database and put them into a TreeMap. This should
      * only loaded onEnabled()
      * 
      * @return A TreeMap where the key the name of the player is
      */
     public HashMap<String, Location> loadBanksFromDatabase() {
 
         HashMap<String, Location> banks = new HashMap<String, Location>();
         try {
             ResultSet rs = dbConnection.getConnection().createStatement().executeQuery("SELECT player,world,x,y,z,yaw,pitch FROM banks");
             while (rs.next()) {
 
                 String name = rs.getString(1).toLowerCase();
                 String world = rs.getString(2);
                 Location loc = new Location(Bukkit.getWorld(world), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getInt(6), rs.getInt(7));
                 banks.put(name, loc);
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't loading all banks from database!");
         }
         ConsoleUtils.printInfo(Core.NAME, "Banks loaded: " + banks.size());
         return banks;
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
             // VALUES (?,?,?,?,?,?,?,?)
             addWarp.setString(1, name);
             addWarp.setString(2, creator.getName());
             addWarp.setString(3, creator.getWorld().getName());
             addWarp.setDouble(4, loc.getX());
             addWarp.setDouble(5, loc.getY());
             addWarp.setDouble(6, loc.getZ());
             addWarp.setInt(7, Math.round(loc.getYaw()) % 360);
             addWarp.setInt(8, Math.round(loc.getPitch()) % 360);
             addWarp.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't store a new warp in database! Name=" + name + ",Player=" + creator.getName() + " ,Loc=" + creator.getLocation().toString());
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
             // (?,?,?,?,?,?,?)
             addHome.setString(1, creator.getName().toLowerCase());
             addHome.setString(2, creator.getWorld().getName());
             addHome.setDouble(3, loc.getX());
             addHome.setDouble(4, loc.getY());
             addHome.setDouble(5, loc.getZ());
             addHome.setInt(6, Math.round(loc.getYaw()) % 360);
             addHome.setInt(7, Math.round(loc.getPitch()) % 360);
             addHome.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't set home in database! Player=" + creator.getName() + " ,Loc=" + creator.getLocation());
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
             // pitch = ? WHERE name = ?
             updateHome.setString(1, player.getWorld().getName());
             updateHome.setDouble(2, loc.getX());
             updateHome.setDouble(3, loc.getY());
             updateHome.setDouble(4, loc.getZ());
             updateHome.setInt(5, Math.round(loc.getYaw()) % 360);
             updateHome.setInt(6, Math.round(loc.getPitch()) % 360);
             updateHome.setString(7, player.getName().toLowerCase());
             updateHome.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update home in database! Player=" + player.getName() + " ,Loc=" + player.getLocation());
             return false;
         }
         return true;
     }
 
     /**
      * When a bank isn't set for a player, this method will called. It stores a
      * complete new entity to the bank table, where the player name is the
      * primary key, because a player can have only one bank
      * 
      * @param playerName
      *            The owner of the bank (not the caller!)
      * @param bankLocation
      *            Position of the command caller
      * 
      * @return True when the bank is sucessfully added into the database
      */
     public boolean setBank(String playerName, Location bankLocation) {
         try {
             // INSERT INTO banks (player,world, x, y, z, yaw, pitch) VALUES
             // (?,?,?,?,?,?,?)
             setBank.setString(1, playerName.toLowerCase());
             setBank.setString(2, bankLocation.getWorld().getName());
             setBank.setDouble(3, bankLocation.getX());
             setBank.setDouble(4, bankLocation.getY());
             setBank.setDouble(5, bankLocation.getZ());
             setBank.setInt(6, Math.round(bankLocation.getYaw()) % 360);
             setBank.setInt(7, Math.round(bankLocation.getPitch()) % 360);
             setBank.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't set new bank location in database! Player=" + playerName + " ,Loc=" + bankLocation);
             return false;
         }
         return true;
     }
 
     /**
      * When a bank is already set for a player, this method will called. It just
      * updates the location and not create a new entity
      * 
      * @param player
      *            The command caller
      * @return True when the location of the bank is sucessfully updated
      */
     public boolean updateBank(String playerName, Location bankLocation) {
         try {
             // UPDATE banks SET world = ? , x = ? , y = ? , z = ? , yaw = ? ,
             // pitch = ? WHERE name = ?
             updateBank.setString(1, bankLocation.getWorld().getName());
             updateBank.setDouble(2, bankLocation.getX());
             updateBank.setDouble(3, bankLocation.getY());
             updateBank.setDouble(4, bankLocation.getZ());
             updateBank.setInt(5, Math.round(bankLocation.getYaw()) % 360);
             updateBank.setInt(6, Math.round(bankLocation.getPitch()) % 360);
             updateBank.setString(7, playerName.toLowerCase());
             updateBank.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update bank location in database! Player=" + playerName + ",Loc=" + bankLocation);
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
             // DELETE FROM warps WHERE name = ?
             deleteWarp.setString(1, name);
             deleteWarp.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't delete warp from database! Warp=" + name);
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
             // UPDATE warps SET permissions = ? WHERE name = ?
             changeGuestList.setString(1, guestList);
             changeGuestList.setString(2, name);
             changeGuestList.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't change guest list in database! Warp=" + name + " ,GuestList=" + guestList);
             return false;
         }
         return true;
     }
     /**
      * Updates the location of the warp.
      * 
      * @param warpName
      *            The name of the warp
      * @param loc
      *            The new location of the warp.
      * @return True when the location sucessfully changed.
      */
     public boolean updateWarp(String warpName, Location loc) {
         try {
             // UPDATE warps SET world = ? , x = ? , y = ? , z = ? , yaw = ? ,
             // pitch = ?, WHERE name = ?
             updateWarp.setString(1, loc.getWorld().getName());
             updateWarp.setDouble(2, loc.getX());
             updateWarp.setDouble(3, loc.getY());
             updateWarp.setDouble(4, loc.getZ());
             updateWarp.setInt(5, Math.round(loc.getYaw() % 360));
             updateWarp.setInt(6, Math.round(loc.getPitch() % 360));
             updateWarp.setString(7, warpName);
             updateWarp.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update warp location in database! Warp=" + warpName + " ,Loc=" + loc);
             return false;
         }
         return true;
     }
 
     /**
      * Change the name of a warp.
      * 
      * @param oldname
      *            The oldname of the warp.
      * @param newname
      *            The newname of the warp.
      * @return True when the warpname sucessfully changed.
      */
     public boolean renameWarp(String oldname, String newname) {
         try {
             // UPDATE warps SET name = ? WHERE name = ?
             renameWarp.setString(1, newname);
             renameWarp.setString(2, oldname);
             renameWarp.executeUpdate();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't rename warp in database! OldName=" + oldname + " ,NewName=" + newname);
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
     private HashSet<String> guestListAsSet(String guestList) {
 
         if (guestList == null)
             return null;
         if (guestList.equals(""))
             return new HashSet<String>();
         HashSet<String> guests = new HashSet<String>();
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
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't delete guest list in database! Warp=" + name);
             return false;
         }
         return true;
     }
 }
