 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.flags;
 
 import io.github.alshain01.flags.area.*;
 import io.github.alshain01.flags.economy.EconomyPurchaseType;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.sql.*;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * Class for handling SQL Database Storage
  */
 final class DataStoreMySQL implements DataStore {
     private Connection connection = null;
     private final String url, user, password;
     private final Logger logger = Bukkit.getPluginManager().getPlugin("Flags").getLogger();
 
     DataStoreMySQL(String url, String user, String pw) {
         this.url = url;
         this.user = user;
         this.password = pw;
 
         connect(url, user, password);
     }
 
     /*
      * Public
      */
     @SuppressWarnings("unused") // API
     public boolean isConnected() {
         try {
             return !connection.isClosed();
         } catch (SQLException e) {
             SqlError(e.getMessage());
             return false;
         }
     }
 
     public void close() {
         try {
             connection.close();
         } catch (SQLException e) {
             SqlError(e.getMessage());
         }
     }
 
     /*
      * Interface Methods
      */
     @Override
     public void create(JavaPlugin plugin) {
         // STANDARD BOOLEAN
         // OVERRIDE for Implementation Specific
         if(notExists()) {
             executeStatement("CREATE TABLE IF NOT EXISTS Version (Major INT, Minor INT, Build INT);");
             executeStatement("INSERT INTO Version (Major, Minor, Build) VALUES (1,3,0);");
             executeStatement("CREATE TABLE IF NOT EXISTS Bundle (BundleName VARCHAR(25), FlagName VARCHAR(25), CONSTRAINT pk_BundleEntry PRIMARY KEY (BundleName, FlagName));");
             executeStatement("CREATE TABLE IF NOT EXISTS Price (FlagName VARCHAR(25), ProductType VARCHAR(25), Cost DOUBLE, CONSTRAINT pk_FlagType PRIMARY KEY (FlagName, ProductType));");
             executeStatement("CREATE TABLE IF NOT EXISTS WorldFlags (WorldName VARCHAR(50), FlagName VARCHAR(25), FlagValue BOOLEAN, FlagMessage VARCHAR(255), CONSTRAINT pk_WorldFlag PRIMARY KEY (WorldName, FlagName));");
             executeStatement("CREATE TABLE IF NOT EXISTS WorldTrust (WorldName VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), CONSTRAINT pk_WorldFlag PRIMARY KEY (WorldName, FlagName, Trustee));");
             executeStatement("CREATE TABLE IF NOT EXISTS DefaultFlags (WorldName VARCHAR(50), FlagName VARCHAR(25), FlagValue BOOLEAN, FlagMessage VARCHAR(255), CONSTRAINT pk_DefaultFlag PRIMARY KEY (WorldName, FlagName));");
             executeStatement("CREATE TABLE IF NOT EXISTS DefaultTrust (WorldName VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), CONSTRAINT pk_DefaultTrust PRIMARY KEY (WorldName, FlagName, Trustee));");
         }
     }
 
     @Override
     public void reload() {
         // Close the connection and reconnect.
         try {
             if(!(this.connection == null) && !this.connection.isClosed()) {
                 connection.close();
             }
         } catch (SQLException e) {
             SqlError(e.getMessage());
             return;
         }
 
         connect(url, user, password);
     }
 
     @Override
     public DataStoreVersion readVersion() {
         ResultSet results = executeQuery("SELECT * FROM Version;");
         try {
             results.next();
             return new DataStoreVersion(results.getInt("Major"), results.getInt("Minor"), results.getInt("Build"));
         } catch (SQLException ex) {
             SqlError(ex.getMessage());
         }
         return new DataStoreVersion(0,0,0);
     }
 
     @Override
     public DataStoreType getType() {
         return DataStoreType.MYSQL;
     }
 
     @Override
     public void update(JavaPlugin plugin) {
         // Nothing to update at this time
     }
 
     @Override
     public Set<String> readBundles() {
         final ResultSet results = executeQuery("SELECT DISTINCT BundleName FROM Bundle;");
         Set<String> bundles = new HashSet<String>();
 
         try {
             while(results.next()) {
                 bundles.add(results.getString("BundleName"));
             }
         } catch (SQLException ex){
             SqlError(ex.getMessage());
             return new HashSet<String>();
         }
         return bundles;
     }
 
     @Override
     public Set<Flag> readBundle(String name) {
         final ResultSet results = executeQuery("SELECT * FROM Bundle WHERE BundleName='" + name + "';");
         HashSet<Flag> flags = new HashSet<Flag>();
 
         try {
             while(results.next()) {
                 String flagName = results.getString("FlagName");
                 if(Flags.getRegistrar().getFlag(flagName) != null) {
                     flags.add(Flags.getRegistrar().getFlag(flagName));
                 }
             }
         } catch (SQLException ex){
             SqlError(ex.getMessage());
             return new HashSet<Flag>();
         }
         return flags;
     }
 
     private void deleteBundle(String name) {
         executeStatement("DELETE FROM Bundle WHERE BundleName='" + name + "';");
     }
 
     @Override
     public void writeBundle(String bundleName, Set<Flag> flags) {
         StringBuilder values = new StringBuilder();
 
         // Clear out any existing version of this bundle.
         // If no flags are provided, assume we are deleting it.
         deleteBundle(bundleName);
         if (flags == null || flags.size() == 0) {
             return;
         }
 
         Iterator<Flag> iterator = flags.iterator();
         while(iterator.hasNext()) {
             Flag flag = iterator.next();
             values.append("('").append(bundleName).append("','").append(flag.getName()).append("')");
             if(iterator.hasNext()) {
                 values.append(",");
             }
         }
 
         executeStatement("INSERT INTO Bundle (BundleName, FlagName) VALUES " + values + ";");
     }
 
     @Override
     public Boolean readFlag(Area area, Flag flag) {
         StringBuilder selectString = new StringBuilder("SELECT * FROM %table%Flags WHERE WorldName='%world%'");
         if(!(area instanceof Default || area instanceof Wilderness)) {
             selectString.append(" AND AreaID='%area%' AND AreaSubID='%sub%'");
         }
         selectString.append(" AND FlagName='%flag%';");
 
         ResultSet results = executeQuery(areaBuilder(selectString.toString(), area)
                 .replaceAll("%flag%", flag.getName()));
 
         try {
             if(results.next()) {
                 boolean value = results.getBoolean("FlagValue");
                 if (results.wasNull()) { return null; }
                 return value;
             }
             return null;
         } catch (SQLException ex){
             SqlError(ex.getMessage());
         }
         return null;
     }
 
     @Override
     public void writeFlag(Area area, Flag flag, Boolean value) {
         String insertString;
 
         if((area instanceof Wilderness) || (area instanceof Default)) {
             insertString = "INSERT INTO %table%Flags (WorldName, FlagName, FlagValue)"
                     + " VALUES ('%world%', '%flag%', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";
         } else {
             insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagValue)"
                     + " VALUES ('%world%', '%area%', '%sub%', '%flag%', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";
         }
 
 
         executeStatement(areaBuilder(insertString, area)
                 .replaceAll("%flag%", flag.getName())
                 .replaceAll("%value%", String.valueOf(value)));
     }
 
     //For SQL Importer
     void writeAreaFlag(Boolean value, String flagName, String areaType, String worldName, String systemID, String systemSubID) {
         String insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagValue)"
                 + " VALUES ('%world%', '%area%', '%sub%', '%flag%', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";
 
         executeStatement(areaBuilder(insertString, areaType, worldName, systemID, systemSubID)
                 .replaceAll("%flag%", flagName)
                 .replaceAll("%value%", String.valueOf(value)));
     }
 
     @Override
     public String readMessage(Area area, Flag flag) {
         StringBuilder selectString = new StringBuilder("SELECT * FROM %table%Flags WHERE WorldName='%world%'");
         if(!(area instanceof Default || area instanceof Wilderness)) {
             selectString.append(" AND AreaID='%area%' AND AreaSubID='%sub%'");
         }
         selectString.append(" AND FlagName='%flag%';");
 
         ResultSet results = executeQuery(areaBuilder(selectString.toString(), area)
                 .replaceAll("%flag%", flag.getName()));
 
         try {
             if(results.next()) {
                return results.getString("FlagMessage").replaceAll("''", "'");
             }
             return null;
         } catch (SQLException ex){
             SqlError(ex.getMessage());
         }
         return null;
     }
 
     @Override
     public void writeMessage(Area area, Flag flag, String message) {
         String insertString;
         if (message == null) {
             message = "null";
         } else {
             message = "'" + message.replaceAll("'", "''") + "'";
         }
 
         if(area instanceof Default || area instanceof Wilderness) {
             insertString = "INSERT INTO %table%Flags (WorldName, FlagName, FlagMessage) VALUES ('%world%', '%flag%', %message%) ON DUPLICATE KEY UPDATE FlagMessage=%message%;";
         } else {
             insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagMessage) VALUES ('%world%', '%area%', '%sub%', '%flag%', %message%) ON DUPLICATE KEY UPDATE FlagMessage=%message%;";
         }
         executeStatement(areaBuilder(insertString, area)
                 .replaceAll("%flag%", flag.getName())
                 .replaceAll("%message%", message));
     }
 
     // For SQL Importer
     private void writeAreaMessage(String message, String flagName, String areaType, String worldName, String systemID, String systemSubID) {
        String insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagMessage) VALUES ('%world%', '%area%', '%sub%', '%flag%', %message%) ON DUPLICATE KEY UPDATE FlagMessage=%message%;";
 
        executeStatement(areaBuilder(insertString, areaType, worldName, systemID, systemSubID)
                 .replaceAll("%flag%", flagName)
                 .replaceAll("%message%", message));
     }
 
     @Override
     public double readPrice(Flag flag, EconomyPurchaseType type) {
         String selectString = "SELECT * FROM Price WHERE FlagName='%flag%' AND ProductType='%type%';";
         ResultSet results = executeQuery(selectString
                 .replaceAll("%flag%", flag.getName())
                 .replaceAll("%type%", type.toString()));
 
         try {
             if(results.next()) {
                 return results.getDouble("Cost");
             }
             return 0;
         } catch (SQLException ex){
             SqlError(ex.getMessage());
         }
         return 0;
     }
 
     @Override
     public void writePrice(Flag flag, EconomyPurchaseType type, double price) {
         String insertString = "INSERT INTO Price (FlagName, ProductType, Cost) VALUES ('%flag%', '%product%', %price%) ON DUPLICATE KEY UPDATE Cost=%price%;";
         executeStatement(insertString
                 .replaceAll("%flag%", flag.getName())
                 .replaceAll("%product%", type.toString())
                 .replaceAll("%price%", String.valueOf(price)));
     }
 
     @Override
     public Set<String> readTrust(Area area, Flag flag) {
         StringBuilder selectString = new StringBuilder("SELECT * FROM %table%Trust WHERE WorldName='%world%'");
         if(!(area instanceof Default || area instanceof Wilderness)) {
             selectString.append(" AND AreaID='%area%' AND AreaSubID='%sub%'");
         }
         selectString.append(" AND FlagName='%flag%';");
 
         ResultSet results = executeQuery(areaBuilder(selectString.toString(), area)
                 .replaceAll("%flag%", flag.getName()));
 
         try {
             Set<String> trustList = new HashSet<String>();
             while(results.next()) {
                 trustList.add(results.getString("Trustee"));
             }
             return trustList;
         } catch (SQLException ex){
             SqlError(ex.getMessage());
         }
         return new HashSet<String>();
     }
 
     @Override
     public Set<String> readPlayerTrust(Area area, Flag flag) {
         StringBuilder selectString = new StringBuilder("SELECT * FROM %table%Trust WHERE WorldName='%world%'");
         if(!(area instanceof Default || area instanceof Wilderness)) {
             selectString.append(" AND AreaID='%area%' AND AreaSubID='%sub%'");
         }
         selectString.append(" AND FlagName='%flag%';");
 
         ResultSet results = executeQuery(areaBuilder(selectString.toString(), area)
                 .replaceAll("%flag%", flag.getName()));
 
         try {
             Set<String> trustList = new HashSet<String>();
             while(results.next()) {
                 if(!results.getString("Trustee").contains(".")) {
                     trustList.add(results.getString("Trustee"));
                 }
             }
             return trustList;
         } catch (SQLException ex){
             SqlError(ex.getMessage());
         }
         return new HashSet<String>();
     }
 
     @Override
     public Set<String> readPermissionTrust(Area area, Flag flag) {
         StringBuilder selectString = new StringBuilder("SELECT * FROM %table%Trust WHERE WorldName='%world%'");
         if(!(area instanceof Default || area instanceof Wilderness)) {
             selectString.append(" AND AreaID='%area%' AND AreaSubID='%sub%'");
         }
         selectString.append(" AND FlagName='%flag%';");
 
         ResultSet results = executeQuery(areaBuilder(selectString.toString(), area)
                 .replaceAll("%flag%", flag.getName()));
 
         try {
             Set<String> trustList = new HashSet<String>();
             while(results.next()) {
                 if(results.getString("Trustee").contains(".")) {
                     trustList.add(results.getString("Trustee"));
                 }
             }
             return trustList;
         } catch (SQLException ex){
             SqlError(ex.getMessage());
         }
         return new HashSet<String>();
     }
 
     @Override
     public void writeTrust(Area area, Flag flag, Set<String> players) {
         // Delete the old list to be replaced
         StringBuilder deleteString = new StringBuilder("DELETE FROM %table%Trust WHERE WorldName='%world%'");
         if(!(area instanceof Default || area instanceof Wilderness)) {
             deleteString.append(" AND AreaID='%area%' AND AreaSubID='%sub%'");
         }
         deleteString.append(" AND FlagName='%flag%';");
 
         executeStatement(areaBuilder(deleteString.toString(), area).replaceAll("%flag%", flag.getName()));
 
         String insertString;
         if(area instanceof Default || area instanceof Wilderness) {
             insertString = "INSERT INTO %table%Trust (WorldName, FlagName, Trustee) VALUES('%world%', '%flag%', '%player%');";
         } else {
             insertString = "INSERT INTO %table%Trust (WorldName, AreaID, AreaSubID, FlagName, Trustee) VALUES('%world%', '%area%', '%sub%', '%flag%', '%player%');";
         }
 
         for(String p : players) {
             executeStatement(areaBuilder(insertString, area)
                     .replaceAll("%flag%", flag.getName())
                     .replaceAll("%player%", p));
         }
     }
 
     // For SQL Importer
     private void writeAreaTrust(Set<String> players, String flagName, String areaType, String worldName, String systemID, String systemSubID) {
         String insertString = "INSERT INTO %table%Trust (WorldName, AreaID, AreaSubID, FlagName, Trustee) VALUES('%world%', '%area%', '%sub%', '%flag%', '%player%');";
         for(String p : players) {
             executeStatement(areaBuilder(insertString, areaType, worldName, systemID, systemSubID)
                     .replaceAll("%flag%", flagName)
                     .replaceAll("%player%", p));
         }
     }
 
     @Override
     public boolean readInheritance(Area area) {
         if(!(area instanceof Subdivision) || !((Subdivision)area).isSubdivision()) {
             return false;
         }
 
         String selectString = "SELECT * FROM %table%Flags WHERE WorldName='%world%' AND AreaID='%area%' AND AreaSubID='%sub%' AND FlagName='InheritParent';";
 
         ResultSet results = executeQuery(areaBuilder(selectString, area));
 
         try {
             return !results.next() || results.getBoolean("FlagValue");
         } catch (SQLException ex){
             SqlError(ex.getMessage());
         }
         return true;
     }
 
     @Override
     public void writeInheritance(Area area, boolean value) {
         if(!(area instanceof Subdivision) || !((Subdivision)area).isSubdivision()) {
             return;
         }
 
         writeInheritance(value, area.getCuboidType().getCuboidName(), area.getWorld().getName(),
                 area.getSystemID(), ((Subdivision) area).getSystemSubID());
     }
 
     void writeInheritance(boolean value, String areaType, String worldName, String systemID, String systemSubID) {
         String insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagValue) "
                 + "VALUES ('%world%', '%area%', '%sub%', 'InheritParent', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";
 
         executeStatement(areaBuilder(insertString, areaType, worldName, systemID, systemSubID)
                 .replaceAll("%value%", String.valueOf(value)));
     }
 
     @Override
     public void remove(Area area) {
         String deleteString = "DELETE FROM %table%%type% WHERE WorldName='%world%' AND AreaID='%area%' AND SubID='%sub%';";
         executeStatement(areaBuilder(deleteString, area)
                 .replaceAll("%type%", "Flags"));
 
         executeStatement(areaBuilder(deleteString, area)
                 .replaceAll("%type%", "Trust"));
     }
 
     /*
      * Database Import/Export
      */
     public void importDB() {
         logger.info("Importing YAML Database to " + getType().getName());
         DataStore yaml = new DataStoreYaml((Flags)Bukkit.getPluginManager().getPlugin("Flags"));
 
         convertGenericData(yaml, this);
 
         // Import the system data
         Set<String> keys = ((DataStoreYaml)yaml).readKeys();
         for(String key : keys) {
             String[] keyNodes = key.split("\\.");
             if(!keyNodes[0].equalsIgnoreCase(CuboidType.getActive().toString())) { continue; }
 
             // Parent id's & 'InheritParent' are 5, Subdivisions are 6, all others are incomplete.
             if(keyNodes.length < 5 || keyNodes.length > 6) { continue; }
 
             String world = keyNodes[1];
             String id = keyNodes[2];
             String subID = "null";
             String flag = keyNodes[3];
 
             if(keyNodes.length == 6 || key.contains("InheritParent")) { // Subdivision or InheritParent
                 subID = keyNodes[3];
                 flag = keyNodes[4];
             }
 
             if(key.contains("InheritParent")) {
                 writeInheritance(((DataStoreYaml) yaml).getBoolean(key), CuboidType.getActive().toString(), world, id, subID);
                 continue;
             }
 
             if(key.contains("Value")) {
                 writeAreaFlag(((DataStoreYaml) yaml).getBoolean(key), flag, CuboidType.getActive().toString(), world, id, subID);
                 continue;
             }
 
             if(key.contains("Message")) {
                 writeAreaMessage("'" + ((DataStoreYaml) yaml).getString(key) + "'", flag, CuboidType.getActive().toString(), world, id, subID);
                 continue;
             }
 
             if(key.contains("Trust")) {
                 List<?> rawPlayers = ((DataStoreYaml) yaml).getList(key);
                 Set<String> players = new HashSet<String>();
                 for(Object o : rawPlayers) {
                     players.add((String)o);
                 }
 
                 writeAreaTrust(players, flag, CuboidType.getActive().toString(), world, id, subID);
             }
         }
 
         logger.info("Import Complete");
     }
 
     @SuppressWarnings("unused") // Future enhancement
     public void exportDB() {
         logger.info("Exporting " + getType().getName() + " Database to YAML");
         DataStore yaml = new DataStoreYaml((Flags)Bukkit.getPluginManager().getPlugin("Flags"));
 
         convertGenericData(this, yaml);
 
 
 
         logger.info("Export Complete");
     }
 
     /*
      * Protected
      */
     String areaBuilder(String query, Area area) {
         return query.replaceAll("%table%", area.getCuboidType().toString())
                 .replaceAll("%world%", area.getWorld().getName())
                 .replaceAll("%area%", area.getSystemID())
                 .replaceAll("%sub%", getSubID(area));
     }
 
     String areaBuilder(String query, String systemName, String worldName, String systemID, String systemSubID) {
         return query.replaceAll("%table%", systemName)
                 .replaceAll("%world%", worldName)
                 .replaceAll("%area%", systemID)
                 .replaceAll("%sub%", systemSubID);
     }
 
     void connect(String url, String user, String password) {
         // Connect to the database.
         try {
             connection = DriverManager.getConnection(url, user, password);
         } catch (SQLException e) {
             SqlError(e.getMessage());
         }
     }
 
     void SqlError(String error) {
         logger.severe("[SQL DataStore Error] " + error);
     }
 
     void executeStatement(String statement) {
         try {
             Statement SQL = connection.createStatement();
             Flags.debug(statement);
             SQL.execute(statement);
         } catch (SQLException e) {
             SqlError(e.getMessage());
         }
     }
 
     ResultSet executeQuery(String query) {
         try {
             Statement SQL = connection.createStatement();
             Flags.debug(query);
             return SQL.executeQuery(query);
         } catch (SQLException e) {
             SqlError(e.getMessage());
             return null;
         }
     }
 
     void createSystemDB() {
         // STANDARD BOOLEAN
         // OVERRIDE for Implementation Specific
         executeStatement("CREATE TABLE IF NOT EXISTS " + CuboidType.getActive().toString()
                 + "Flags (WorldName VARCHAR(50), AreaID VARCHAR(50), AreaSubID VARCHAR(50), "
                 + "FlagName VARCHAR(25), FlagValue BOOLEAN, FlagMessage VARCHAR(255), "
                 + "CONSTRAINT pk_AreaFlag PRIMARY KEY (WorldName, AreaID, AreaSubID, FlagName));");
 
         executeStatement("CREATE TABLE IF NOT EXISTS " + CuboidType.getActive().toString()
                 + "Trust (WorldName VARCHAR(50), AreaID VARCHAR(50), "
                 + "AreaSubID VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), "
                 + "CONSTRAINT pk_WorldFlag PRIMARY KEY (WorldName, AreaID, AreaSubID, FlagName, Trustee));");
     }
 
     private boolean notExists() {
         // We always need to create the system specific table
         // in case it changed since the database was created.
         // i.e. Grief Prevention was removed and WorldGuard was installed.
         if(CuboidType.getActive() != CuboidType.WILDERNESS) {
             createSystemDB();
         }
 
         String[] connection = url.split("/");
 
         // Result Limiting, requires MYSQL exclusively
         ResultSet results =
                 executeQuery("SELECT * FROM information_schema.tables "
                         + "WHERE table_schema = '%database%' AND table_name = 'Version' LIMIT 1;"
                         .replaceAll("%database%", connection[connection.length-1]));
 
         try {
             return !results.next();
         } catch (SQLException e) {
             SqlError(e.getMessage());
         }
         return true;
     }
 
     /*
      * Private
      */
     private String getSubID(Area area) {
         return (area instanceof Subdivision && ((Subdivision)area).isSubdivision()) ? ((Subdivision)area).getSystemSubID() : "null";
     }
 
     @SuppressWarnings("unused") // Future enhancement
     private void writeVersion(DataStoreVersion version) {
         executeQuery("UPDATE Version SET Major=" + version.getMajor() + ", Minor=" + version.getMinor() + ", Build=" + version.getBuild() + ";");
     }
 
     private static void convertGenericData(DataStore convertFrom, DataStore convertTo) {
         //Convert the bundles
         for(String b : convertFrom.readBundles()) {
             convertTo.writeBundle(b, convertFrom.readBundle(b));
         }
 
         //Convert the prices
         for(Flag f : Flags.getRegistrar().getFlags()) {
             double price = convertFrom.readPrice(f, EconomyPurchaseType.Flag);
             if(price > (double)0) {
                 convertTo.writePrice(f, EconomyPurchaseType.Flag, price);
             }
 
             price = convertFrom.readPrice(f, EconomyPurchaseType.Message);
             if(price > (double)0) {
                 convertTo.writePrice(f, EconomyPurchaseType.Message, price);
             }
         }
 
         //Convert world & default data
         for(org.bukkit.World w : Bukkit.getWorlds()) {
             for(Flag f : Flags.getRegistrar().getFlags()) {
                 Wilderness world = new Wilderness(w);
                 Default def = new Default(w);
 
                 //Flags
                 Boolean value = convertFrom.readFlag(world, f);
                 if(value != null) {
                     convertTo.writeFlag(world, f, value);
                 }
 
                 value = convertFrom.readFlag(def, f);
                 if(value != null) {
                     convertTo.writeFlag(def, f, convertFrom.readFlag(def, f));
                 }
 
                 //Messages
                 String message = convertFrom.readMessage(world, f);
                 if(message != null) {
                     convertTo.writeMessage(world, f, message);
                 }
 
                 message = convertFrom.readMessage(def, f);
                 if(message != null) {
                     convertTo.writeMessage(def, f, message);
                 }
 
                 //Trust Lists
                 Set<String> trust = convertFrom.readTrust(world, f);
                 if(!trust.isEmpty()) {
                     convertTo.writeTrust(world, f, trust);
                 }
 
                 trust = convertFrom.readTrust(def, f);
                 if(!trust.isEmpty()) {
                     convertTo.writeTrust(def, f, trust);
                 }
                 
             }
         }
     }
 
 }
