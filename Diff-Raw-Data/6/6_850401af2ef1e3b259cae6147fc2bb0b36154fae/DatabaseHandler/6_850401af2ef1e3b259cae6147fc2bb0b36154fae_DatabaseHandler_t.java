 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.database;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
import java.sql.Timestamp;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.bukkit.Location;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.Bank;
 import de.minestar.FifthElement.data.Home;
 import de.minestar.FifthElement.data.Warp;
 import de.minestar.minestarlibrary.config.MinestarConfig;
 import de.minestar.minestarlibrary.database.AbstractDatabaseHandler;
 import de.minestar.minestarlibrary.database.DatabaseConnection;
 import de.minestar.minestarlibrary.database.DatabaseType;
 import de.minestar.minestarlibrary.database.DatabaseUtils;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class DatabaseHandler extends AbstractDatabaseHandler {
 
     public DatabaseHandler(File dataFolder) {
         super(Core.NAME, dataFolder);
     }
 
     @Override
     protected DatabaseConnection createConnection(String pluginName, File dataFolder) throws Exception {
         File configFile = new File(dataFolder, "sqlconfig.yml");
         if (!configFile.exists())
             DatabaseUtils.createDatabaseConfig(DatabaseType.MySQL, configFile, pluginName);
         else
             return new DatabaseConnection(pluginName, DatabaseType.MySQL, new MinestarConfig(configFile));
 
         return null;
     }
 
     @Override
     protected void createStructure(String pluginName, Connection con) throws Exception {
         DatabaseUtils.createStructure(getClass().getResourceAsStream("/structure.sql"), con, pluginName);
     }
 
     @Override
     protected void createStatements(String pluginName, Connection con) throws Exception {
 
         addWarp = con.prepareStatement("INSERT INTO warp (name, owner, world, x, y, z, yaw, pitch, isPublic, guests, useMode, creationDate) VALUES (? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
 
         deleteWarp = con.prepareStatement("DELETE FROM warp WHERE id = ?");
 
         updateWarpLocation = con.prepareStatement("UPDATE warp SET world = ? , x = ? , y = ? , z = ? , yaw = ? , pitch = ? WHERE id = ?");
 
         updateWarpName = con.prepareStatement("UPDATE warp SET name = ? WHERE id = ?");
 
         updateGuestList = con.prepareStatement("UPDATE warp SET guests = ? WHERE id = ?");
 
         updateAccess = con.prepareStatement("UPDATE warp SET isPublic = ? WHERE id = ?");
 
         addHome = con.prepareStatement("INSERT INTO home (player, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
 
         updateHomeLocation = con.prepareStatement("UPDATE home SET world = ? , x = ? , y = ? , z = ? , yaw = ? , pitch = ? WHERE id = ?");
 
         addBank = con.prepareStatement("INSERT INTO bank (player, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
 
         updateBankLocation = con.prepareStatement("UPDATE bank SET world = ? , x = ? , y = ? , z = ? , yaw = ? , pitch = ? WHERE id = ?");
 
         updateUseMode = con.prepareStatement("UPDATE warp SET useMode = ? WHERE id = ?");
     }
 
     // *****************
     // ***** WARPS *****
     // *****************
 
     /* STATEMENTS */
     private PreparedStatement addWarp;
     private PreparedStatement deleteWarp;
     private PreparedStatement updateWarpLocation;
     private PreparedStatement updateWarpName;
     private PreparedStatement updateGuestList;
     private PreparedStatement updateAccess;
     private PreparedStatement updateUseMode;
 
     public TreeMap<String, Warp> loadWarps() {
         TreeMap<String, Warp> warpMap = new TreeMap<String, Warp>();
         try {
             Statement stat = dbConnection.getConnection().createStatement();
             ResultSet rs = stat.executeQuery("SELECT id,name, owner, world, x, y, z, yaw, pitch, isPublic, guests, useMode, creationDate FROM warp");
             // TEMP VARIABLEN
             int id;
             String name;
             String owner;
             String worldName;
             double x;
             double y;
             double z;
             float yaw;
             float pitch;
             boolean isPublic;
             String guests;
             byte useMode;
             Date date;
 
             // CREATE WARPS
             while (rs.next()) {
                 // LOAD VARIABLEN
                 id = rs.getInt(1);
                 name = rs.getString(2);
                 owner = rs.getString(3);
                 worldName = rs.getString(4);
                 x = rs.getDouble(5);
                 y = rs.getDouble(6);
                 z = rs.getDouble(7);
                 yaw = rs.getFloat(8);
                 pitch = rs.getFloat(9);
                 isPublic = rs.getBoolean(10);
                 guests = rs.getString(11);
                 useMode = rs.getByte(12);
                 date = rs.getDate(13);
 
                 // CREATE WARP AND PUT IT TO MAP
                 warpMap.put(name.toLowerCase(), new Warp(id, name, isPublic, owner, guests, worldName, x, y, z, yaw, pitch, useMode, date));
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't load warps from table!");
             // RETURN AN EMPTY MAP WHEN THERE IS AN ERROR
             warpMap.clear();
         }
         return warpMap;
     }
 
     public boolean addWarp(Warp warp) {
 
         try {
             // INSERT WARP INTO TABLE
             addWarp.setString(1, warp.getName());
             addWarp.setString(2, warp.getOwner());
             addWarp.setString(3, warp.getLocation().getWorld().getName().toLowerCase());
             addWarp.setDouble(4, warp.getLocation().getX());
             addWarp.setDouble(5, warp.getLocation().getY());
             addWarp.setDouble(6, warp.getLocation().getZ());
             addWarp.setFloat(7, warp.getLocation().getYaw());
             addWarp.setFloat(8, warp.getLocation().getPitch());
             addWarp.setBoolean(9, warp.isPublic());
             addWarp.setString(10, "");
             addWarp.setByte(11, warp.getUseMode());
            addWarp.setTimestamp(12, new Timestamp(warp.getCreationDate().getTime()));
 
             addWarp.executeUpdate();
 
             // GET THE GENERATED ID
             ResultSet rs = addWarp.getGeneratedKeys();
             int id = 0;
             if (rs.next()) {
                 id = rs.getInt(1);
                 warp.setId(id);
                 return true;
             } else {
                 ConsoleUtils.printError(Core.NAME, "Can't get the id for the warp = " + warp);
                 return false;
             }
 
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't insert the warp = " + warp);
             return false;
         }
     }
 
     public boolean deleteWarp(Warp warp) {
 
         try {
             // DELETE WARP FROM TABLE
             deleteWarp.setInt(1, warp.getId());
             return deleteWarp.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't delete the warp = " + warp);
             return false;
         }
     }
 
     public boolean updateWarpLocation(Warp warp) {
 
         try {
             // UPDATE THE WARP LOCATION
             Location loc = warp.getLocation();
             updateWarpLocation.setString(1, loc.getWorld().getName().toLowerCase());
             updateWarpLocation.setDouble(2, loc.getX());
             updateWarpLocation.setDouble(3, loc.getY());
             updateWarpLocation.setDouble(4, loc.getZ());
             updateWarpLocation.setFloat(5, loc.getYaw());
             updateWarpLocation.setFloat(6, loc.getPitch());
             updateWarpLocation.setInt(7, warp.getId());
             return updateWarpLocation.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update warp location of warp = " + warp);
             return false;
         }
     }
 
     public boolean updateWarpName(Warp warp) {
 
         try {
             // UPDATE THE WARP NAME
             updateWarpName.setString(1, warp.getName());
             updateWarpName.setInt(2, warp.getId());
             return updateWarpName.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update warp name of warp = " + warp);
             return false;
         }
     }
 
     public boolean updateGuests(Warp warp) {
 
         try {
             // UPDATE THE GUEST LIST
             updateGuestList.setString(1, warp.getGuestList());
             updateGuestList.setInt(2, warp.getId());
             return updateGuestList.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update the guest list of warp = " + warp);
             return false;
         }
     }
 
     public boolean updateAccess(Warp warp) {
 
         try {
             // UPDATE THE ACCESS MODIFIER
             updateAccess.setBoolean(1, warp.isPublic());
             updateAccess.setInt(2, warp.getId());
             return updateAccess.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update the access modifier of warp = " + warp);
             return false;
         }
     }
 
     // *****************
     // ***** HOMES *****
     // *****************
 
     /* STATEMENTS */
     private PreparedStatement addHome;
     private PreparedStatement updateHomeLocation;
 
     public Map<String, Home> loadHomes() {
 
         Map<String, Home> homeMap = new HashMap<String, Home>();
         try {
             Statement stat = dbConnection.getConnection().createStatement();
             ResultSet rs = stat.executeQuery("SELECT id, player, world, x, y, z, yaw, pitch FROM home");
             // TEMP VARIABLEN
             int id;
             String player;
             String worldName;
             double x;
             double y;
             double z;
             float yaw;
             float pitch;
 
             // CREATE WARPS
             while (rs.next()) {
                 // LOAD VARIABLEN
                 id = rs.getInt(1);
                 player = rs.getString(2);
                 worldName = rs.getString(3);
                 x = rs.getDouble(4);
                 y = rs.getDouble(5);
                 z = rs.getDouble(6);
                 yaw = rs.getFloat(7);
                 pitch = rs.getFloat(8);
 
                 // CREATE WARP AND PUT IT TO MAP
                 homeMap.put(player.toLowerCase(), new Home(id, player, x, y, z, yaw, pitch, worldName));
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't load homes from table!");
             // RETURN AN EMPTY MAP WHEN THERE IS AN ERROR
             homeMap.clear();
         }
         return homeMap;
     }
 
     public boolean addHome(Home home) {
         try {
             // INSERT NEW HOME
             addHome.setString(1, home.getOwner());
             addHome.setString(2, home.getLocation().getWorld().getName());
             addHome.setDouble(3, home.getLocation().getX());
             addHome.setDouble(4, home.getLocation().getY());
             addHome.setDouble(5, home.getLocation().getZ());
             addHome.setFloat(6, home.getLocation().getYaw());
             addHome.setFloat(7, home.getLocation().getPitch());
 
             addHome.executeUpdate();
 
             // GET THE GENERATED ID
             ResultSet rs = addHome.getGeneratedKeys();
             int id = 0;
             if (rs.next()) {
                 id = rs.getInt(1);
                 home.setId(id);
                 return true;
             } else {
                 ConsoleUtils.printError(Core.NAME, "Can't get the id for the home = " + home);
                 return false;
             }
 
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't add home to database! Home = " + home);
             return false;
         }
     }
 
     public boolean updateHomeLocation(Home home) {
 
         try {
             // SET NEW LOCATION VALUES
             updateHomeLocation.setString(1, home.getLocation().getWorld().getName());
             updateHomeLocation.setDouble(2, home.getLocation().getX());
             updateHomeLocation.setDouble(3, home.getLocation().getY());
             updateHomeLocation.setDouble(4, home.getLocation().getZ());
             updateHomeLocation.setFloat(5, home.getLocation().getYaw());
             updateHomeLocation.setFloat(6, home.getLocation().getPitch());
             updateHomeLocation.setInt(7, home.getId());
 
             // EXECUTE
             return updateHomeLocation.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't store location change of home to database! Home = " + home);
             return false;
         }
 
     }
 
     // *****************
     // ***** BANKS *****
     // *****************
 
     /* STATEMENTS */
     private PreparedStatement addBank;
     private PreparedStatement updateBankLocation;
 
     public Map<String, Bank> loadBanks() {
 
         Map<String, Bank> bankMap = new HashMap<String, Bank>();
         try {
             Statement stat = dbConnection.getConnection().createStatement();
             ResultSet rs = stat.executeQuery("SELECT id, player, world, x, y, z, yaw, pitch FROM bank");
             // TEMP VARIABLEN
             int id;
             String player;
             String worldName;
             double x;
             double y;
             double z;
             float yaw;
             float pitch;
 
             // CREATE WARPS
             while (rs.next()) {
                 // LOAD VARIABLEN
                 id = rs.getInt(1);
                 player = rs.getString(2);
                 worldName = rs.getString(3);
                 x = rs.getDouble(4);
                 y = rs.getDouble(5);
                 z = rs.getDouble(6);
                 yaw = rs.getFloat(7);
                 pitch = rs.getFloat(8);
 
                 // CREATE WARP AND PUT IT TO MAP
                 bankMap.put(player.toLowerCase(), new Bank(id, player, x, y, z, yaw, pitch, worldName));
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't load bank from table!");
             // RETURN AN EMPTY MAP WHEN THERE IS AN ERROR
             bankMap.clear();
         }
         return bankMap;
     }
 
     public boolean addBank(Bank bank) {
         try {
             // INSERT NEW HOME
             addBank.setString(1, bank.getOwner());
             addBank.setString(2, bank.getLocation().getWorld().getName());
             addBank.setDouble(3, bank.getLocation().getX());
             addBank.setDouble(4, bank.getLocation().getY());
             addBank.setDouble(5, bank.getLocation().getZ());
             addBank.setFloat(6, bank.getLocation().getYaw());
             addBank.setFloat(7, bank.getLocation().getPitch());
 
             addBank.executeUpdate();
 
             // GET THE GENERATED ID
             ResultSet rs = addBank.getGeneratedKeys();
             int id = 0;
             if (rs.next()) {
                 id = rs.getInt(1);
                 bank.setId(id);
                 return true;
             } else {
                 ConsoleUtils.printError(Core.NAME, "Can't get the id for the bank = " + bank);
                 return false;
             }
 
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't add home to database! Bank = " + bank);
             return false;
         }
     }
 
     public boolean updateBankLocation(Bank bank) {
 
         try {
             // SET NEW LOCATION VALUES
             updateBankLocation.setString(1, bank.getLocation().getWorld().getName());
             updateBankLocation.setDouble(2, bank.getLocation().getX());
             updateBankLocation.setDouble(3, bank.getLocation().getY());
             updateBankLocation.setDouble(4, bank.getLocation().getZ());
             updateBankLocation.setFloat(5, bank.getLocation().getYaw());
             updateBankLocation.setFloat(6, bank.getLocation().getPitch());
             updateBankLocation.setInt(7, bank.getId());
 
             // EXECUTE
             return updateBankLocation.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't store location change of Bank to database! Bank = " + bank);
             return false;
         }
 
     }
 
     public boolean updateUseMode(Warp warp) {
 
         try {
             // SET NEW USEMODE
             updateUseMode.setByte(1, warp.getUseMode());
             updateUseMode.setInt(2, warp.getId());
 
             // EXECUTE
             return updateUseMode.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't save useMode update for the warp = " + warp);
             return false;
         }
     }
 }
