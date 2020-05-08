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
 
 package io.github.alshain01.Flags.data;
 
 import io.github.alshain01.Flags.*;
 import io.github.alshain01.Flags.System;
 import io.github.alshain01.Flags.area.Area;
 import io.github.alshain01.Flags.area.Default;
 import io.github.alshain01.Flags.area.Subdivision;
 import io.github.alshain01.Flags.area.World;
 import io.github.alshain01.Flags.economy.EPurchaseType;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.sql.*;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 public class SQLDataStore implements DataStore {
     private Connection connection = null;
     protected String url, user, password;
 
     protected SQLDataStore() { }
 
     private String getSubID(Area area) {
         return (area instanceof Subdivision && ((Subdivision)area).isSubdivision()) ? "'" + ((Subdivision)area).getSystemSubID() + "'" : "null";
     }
 
 
     protected String areaBuilder(String query, Area area) {
         return query.replaceAll("%table%", area.getType().toString())
                 .replaceAll("%world%", area.getWorld().getName())
                 .replaceAll("%area%", area.getSystemID())
                 .replaceAll("%sub%", getSubID(area));
     }
 
     public SQLDataStore(String url, String user, String pw) {
         this.url = url;
         this.user = user;
         this.password = pw;
     }
 
     protected boolean connect(String url, String user, String password) {
         // Connect to the database.
         try {
             connection = DriverManager.getConnection(url, user, password);
             return true;
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
 
     @Override
     public boolean reload() {
         // Close the connection and reconnect.
         try {
             if(!(this.connection == null) && !this.connection.isClosed()) {
                 connection.close();
             }
         } catch (SQLException e) {
             SqlError(e.getMessage());
             return false;
         }
 
         return connect(url, user, password);
     }
 
     public boolean isConnected() {
         try {
             return !connection.isClosed();
         } catch (SQLException e) {
             SqlError(e.getMessage());
             return false;
         }
     }
 
     protected void SqlError(String error) {
         Flags.severe("[SQL DataStore Error] " + error);
     }
 
     protected void executeStatement(String statement) {
         Flags.debug("[SQL Statement] " + statement);
         try {
             Statement SQL = connection.createStatement();
             SQL.execute(statement);
         } catch (SQLException e) {
             SqlError(e.getMessage());
         }
     }
 
     protected ResultSet executeQuery(String query) {
         Flags.debug("[SQL Query] " + query);
         try {
             Statement SQL = connection.createStatement();
             return SQL.executeQuery(query);
         } catch (SQLException e) {
             SqlError(e.getMessage());
             return null;
         }
     }
 
     @Override
     public DBVersion readVersion() {
         ResultSet results = executeQuery("SELECT * FROM Version;");
         try {
             results.next();
             return new DBVersion(results.getInt("Major"), results.getInt("Minor"), results.getInt("Build"));
         } catch (SQLException ex) {
             SqlError(ex.getMessage());
         }
         return new DBVersion(0,0,0);
     }
 
     public void writeVersion(DBVersion version) {
         executeQuery("UPDATE Version SET Major=" + version.major + ", Minor=" + version.minor + ", Build=" + version.build + ";");
     }
 
     @Override
     public void update(JavaPlugin plugin) {
         // Nothing to update at this time
     }
 
     //TODO: Implementation Specific (BOOLEAN)
     @Override
     public boolean create(JavaPlugin plugin) {
         // STANDARD BOOLEAN
         // OVERRIDE for Implementation Specific
         if(!exists()) {
             executeStatement("CREATE TABLE IF NOT EXISTS Version (Major INT, Minor INT, Build INT);");
             executeStatement("INSERT INTO Version (Major, Minor, Build) VALUES (1,3,0);");
             executeStatement("CREATE TABLE IF NOT EXISTS Bundle (BundleName VARCHAR(25), FlagName VARCHAR(25), CONSTRAINT pk_BundleEntry PRIMARY KEY (BundleName, FlagName));");
             executeStatement("CREATE TABLE IF NOT EXISTS Price (FlagName VARCHAR(25), ProductType VARCHAR(25), Cost DOUBLE, CONSTRAINT pk_FlagType PRIMARY KEY (FlagName, ProductType));");
             executeStatement("CREATE TABLE IF NOT EXISTS WorldFlags (WorldName VARCHAR(50), FlagName VARCHAR(25), FlagValue BOOLEAN, FlagMessage VARCHAR(255), CONSTRAINT pk_WorldFlag PRIMARY KEY (WorldName, FlagName));");
             executeStatement("CREATE TABLE IF NOT EXISTS WorldTrust (WorldName VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), CONSTRAINT pk_WorldFlag PRIMARY KEY (WorldName, FlagName, Trustee));");
             executeStatement("CREATE TABLE IF NOT EXISTS DefaultFlags (WorldName VARCHAR(50), FlagName VARCHAR(25), FlagValue BOOLEAN, FlagMessage VARCHAR(255), CONSTRAINT pk_DefaultFlag PRIMARY KEY (WorldName, FlagName));");
             executeStatement("CREATE TABLE IF NOT EXISTS DefaultTrust (WorldName VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), CONSTRAINT pk_DefaultTrust PRIMARY KEY (WorldName, FlagName, Trustee));");
         }
         return true;
     }
 
     //TODO: Implementation Specific (BOOLEAN)
     protected void createSystemDB() {
         // STANDARD BOOLEAN
         // OVERRIDE for Implementation Specific
         executeStatement("CREATE TABLE IF NOT EXISTS " + System.getActive().toString()
                 + "Flags (WorldName VARCHAR(50), AreaID VARCHAR(50), AreaSubID VARCHAR(50), "
                 + "FlagName VARCHAR(25), FlagValue BOOLEAN, FlagMessage VARCHAR(255), "
                 + "CONSTRAINT pk_AreaFlag PRIMARY KEY (WorldName, AreaID, AreaSubID, FlagName));");
 
         executeStatement("CREATE TABLE IF NOT EXISTS " + System.getActive().toString()
                 + "Trust (WorldName VARCHAR(50), AreaID VARCHAR(50), "
                 + "AreaSubID VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), "
                 + "CONSTRAINT pk_WorldFlag PRIMARY KEY (WorldName, AreaID, AreaSubID, FlagName, Trustee));");
     }
 
     //TODO: Implementation Specific (ROW LIMITING)
     public boolean exists() {
         // We always need to create the system specific table
         // in case it changed since the database was created.
         // i.e. Grief Prevention was removed and WorldGuard was installed.
         if(System.getActive() != System.WORLD) {
             createSystemDB();
         }
 
         String[] connection = url.split("/");
 
         // STANDARD ROW LIMITING
         // OVERRIDE for Implementation Specific
         ResultSet results =
                 executeQuery("SELECT * FROM information_schema.tables "
                         + "WHERE table_schema = '%database%' AND table_name = 'Version' FETCH FIRST 1 ROWS ONLY;"
                         .replaceAll("%database%", connection[connection.length-1]));
 
         try {
             return results.next();
         } catch (SQLException e) {
             SqlError(e.getMessage());
         }
         return false;
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
 
     //TODO: Oracle doesn't support standard for inserting multiple rows
     @Override
     public void writeBundle(String bundleName, Set<Flag> flags) {
         if (flags == null || flags.size() == 0) {
             deleteBundle(bundleName);
             return;
         }
 
         StringBuilder values = new StringBuilder();
 
         // Clear out any existing version of this bundle.
         deleteBundle(bundleName);
 
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
     public double readPrice(Flag flag, EPurchaseType type) {
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
     public void writePrice(Flag flag, EPurchaseType type, double price) {
        String insertString = "INSERT INTO Price (FlagName, ProductType, Cost) VALUES ('%flag%', '%product%', %price%) ON DUPLICATE KEY UPDATE Cost=%price%;";
         executeStatement(insertString
                 .replaceAll("%flag%", flag.getName())
                 .replaceAll("%product%", type.toString())
                .replaceAll("%price%", String.valueOf(price)));
     }
 
     @Override
     public Boolean readFlag(Area area, Flag flag) {
         StringBuilder selectString = new StringBuilder("SELECT * FROM %table%Flags WHERE WorldName='%world%'");
         if(!(area instanceof Default || area instanceof World)) {
             selectString.append(" AND AreaID='%area%' AND AreaSubID=%sub%");
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
 
     //TODO: Implementation Specific (BOOLEAN)
     @Override
     public void writeFlag(Area area, Flag flag, Boolean value) {
         String insertString;
 
         if((area instanceof World) || (area instanceof Default)) {
             insertString = "INSERT INTO %table%Flags (WorldName, FlagName, FlagValue)"
                     + " VALUES ('%world%', '%flag%', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";
         } else {
             insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagValue)"
                     + " VALUES ('%world%', '%area%', %sub%, '%flag%', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";
         }
 
 
         executeStatement(areaBuilder(insertString, area)
                 .replaceAll("%flag%", flag.getName())
                 .replaceAll("%value%", String.valueOf(value)));
     }
 
     @Override
     public boolean readInheritance(Area area) {
         if(!(area instanceof Subdivision) || !((Subdivision)area).isSubdivision()) {
             Flags.debug("Cannot read inheritance, area is not a subdivision.");
             return false;
         }
 
         String selectString = "SELECT * FROM %table%Flags WHERE WorldName='%world%' AND AreaID='%area%' AND AreaSubID=%sub% AND FlagName='InheritParent';";
 
         ResultSet results = executeQuery(areaBuilder(selectString, area));
 
         try {
             if (!results.next()) {
                 Flags.debug("Inheritance flag not found in DataStore, assuming true.");
                 return true;
             }
             return results.getBoolean("FlagValue");
         } catch (SQLException ex){
             SqlError(ex.getMessage());
         }
         return true;
     }
 
     //TODO: Implementation Specific (BOOLEAN)
     @Override
     public void writeInheritance(Area area, boolean value) {
         if(!(area instanceof Subdivision) || !((Subdivision)area).isSubdivision()) {
             Flags.debug("Cannot write inheritance, area is not a subdivision.");
             return;
         }
 
         String insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagValue) "
                 + "VALUES ('%world%', '%area%', %sub%, 'InheritParent', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";
 
         executeStatement(areaBuilder(insertString, area)
                 .replaceAll("%value%", String.valueOf(value)));
     }
 
     @Override
     public String readMessage(Area area, Flag flag) {
         StringBuilder selectString = new StringBuilder("SELECT * FROM %table%Flags WHERE WorldName='%world%'");
         if(!(area instanceof Default || area instanceof World)) {
             selectString.append(" AND AreaID='%area%' AND AreaSubID=%sub%");
         }
         selectString.append(" AND FlagName='%flag%';");
 
         ResultSet results = executeQuery(areaBuilder(selectString.toString(), area)
                 .replaceAll("%flag%", flag.getName()));
 
         try {
             if(results.next()) {
                 return results.getString("FlagMessage");
             }
             Flags.debug("Found no SQL results for query");
             return null;
         } catch (SQLException ex){
             SqlError(ex.getMessage());
         }
         return null;
     }
 
     @Override
     public void writeMessage(Area area, Flag flag, String message) {
         Flags.debug("Writing Message to SQL DataStore");
         String insertString;
         if (message == null) {
             message = "null";
         } else {
             message = "'" + message + "'";
         }
         Flags.debug("Writing message: " + message);
 
         if(area instanceof Default || area instanceof World) {
             insertString = "INSERT INTO %table%Flags (WorldName, FlagName, FlagMessage) VALUES ('%world%', '%flag%', %message%) ON DUPLICATE KEY UPDATE FlagMessage=%message%;";
         } else {
             insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagMessage) VALUES ('%world%', '%area%', %sub%, '%flag%', %message%) ON DUPLICATE KEY UPDATE FlagMessage=%message%;";
         }
         executeStatement(areaBuilder(insertString, area)
                 .replaceAll("%flag%", flag.getName())
                 .replaceAll("%message%", message));
     }
 
     @Override
     public Set<String> readTrust(Area area, Flag flag) {
         StringBuilder selectString = new StringBuilder("SELECT * FROM %table%Trust WHERE WorldName='%world%'");
         if(!(area instanceof Default || area instanceof World)) {
             selectString.append(" AND AreaID='%area%' AND AreaSubID=%sub%");
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
     public void writeTrust(Area area, Flag flag, Set<String> players) {
         // Delete the old list to be replaced
         StringBuilder deleteString = new StringBuilder("DELETE FROM %table%Trust WHERE WorldName='%world%'");
         if(!(area instanceof Default || area instanceof World)) {
             deleteString.append(" AND AreaID='%area%' AND AreaSubID=%sub%");
         }
         deleteString.append(" AND FlagName='%flag%';");
 
         executeStatement(areaBuilder(deleteString.toString(), area).replaceAll("%flag%", flag.getName()));
 
         for(String p : players) {
             String insertString;
             if(area instanceof Default || area instanceof World) {
                 insertString = "INSERT INTO %table%Trust (WorldName, FlagName, Trustee) VALUES('%world%', '%flag%', '%player%');";
             } else {
                 insertString = "INSERT INTO %table%Trust (WorldName, AreaID, AreaSubID, FlagName, Trustee) VALUES('%world%', '%area%', %sub%, '%flag%', '%player%');";
             }
 
             executeStatement(areaBuilder(insertString, area)
                     .replaceAll("%flag%", flag.getName())
                     .replaceAll("%player%", p));
         }
     }
 
     @Override
     public void remove(Area area) {
         String deleteString = "DELETE FROM %table%%type% WHERE WorldName='%world%' AND AreaID='%area%' AND SubID=%sub%;";
         executeStatement(areaBuilder(deleteString, area)
                 .replaceAll("%type%", "Flags"));
 
         executeStatement(areaBuilder(deleteString, area)
                 .replaceAll("%type%", "Trust"));
     }
 
     @Override
     public DataStoreType getType() {
         return DataStoreType.POSTGRESQL;
     }
 }
