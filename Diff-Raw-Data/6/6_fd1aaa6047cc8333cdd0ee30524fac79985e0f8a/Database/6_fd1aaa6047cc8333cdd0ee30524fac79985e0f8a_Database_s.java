 package net.krinsoft.petsuite.databases;
 
 import net.krinsoft.petsuite.PetCore;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * @author krinsdeath
  */
 public class Database {
 
     private enum Type {
         MySQL("com.mysql.jdbc.Driver"),
         SQLite("org.sqlite.JDBC");
         private String driver;
 
         Type(String driver) {
             this.driver = driver;
         }
 
         public String getDriver() {
             return this.driver;
         }
 
         public static Type forName(String name) throws TypeNotPresentException {
             for (Type type : values()) {
                 if (type.name().equalsIgnoreCase(name)) {
                     return type;
                 }
             }
             throw new TypeNotPresentException(name, new Throwable("Available types: " + Arrays.toString(values())));
         }
     }
 
     private PetCore plugin;
     private Type type;
 
     private Connection connection;
 
     private Map<String, PreparedStatement> prepared = new HashMap<String, PreparedStatement>();
 
     public Database(PetCore instance) {
         this.plugin     = instance;
 
         try {
             type = Type.forName(plugin.getConfig().getString("database.type", "SQLite"));
         } catch (TypeNotPresentException e) {
             e.printStackTrace();
             plugin.debug("Defaulting to SQLite...");
             type = Type.SQLite;
         }
         if (connect()) {
             buildDatabase();
         } else {
             plugin.debug("An error occurred while trying to connect to the database. Check your config.yml.");
         }
     }
 
     public void save() {
         try {
             if (connect()) {
                 connection.commit();
             }
         } catch (SQLException e) {
         }
     }
 
     public boolean connect() {
         try {
             if (connection != null && !connection.isClosed()) {
                 return true;
             }
             Class.forName(type.getDriver());
             if (!plugin.getDataFolder().exists()) {
                 plugin.getDataFolder().mkdirs();
             }
             Properties properties = new Properties();
             if (type.equals(Type.MySQL)) {
                 properties.put("user", plugin.getConfig().getString("database.user", "root"));
                 properties.put("password", plugin.getConfig().getString("database.password", "root"));
             }
             String connURL = "jdbc:" + type.name().toLowerCase() + ":" + getDatabasePath();
             connection = DriverManager.getConnection(connURL, properties);
             connection.setAutoCommit(false);
             return true;
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return false;
     }
 
     public String getDatabasePath() {
         String dbpath = "";
         switch (type) {
             case SQLite:
                 dbpath += plugin.getDataFolder().toString();
                 dbpath += "/petsuite.db";
                 break;
             case MySQL:
                 dbpath += "//" + plugin.getConfig().getString("database.hostname", "localhost");
                 dbpath += ":" + plugin.getConfig().getInt("database.port", 3306);
                 dbpath += "/" + plugin.getConfig().getString("database.name", "petsuite");
                 break;
             default:
                 dbpath = plugin.getDataFolder().toString();
                 dbpath += "/petsuite.db";
         }
         return dbpath;
     }
 
     private void buildDatabase() {
         try {
             String createBase = "CREATE TABLE IF NOT EXISTS petsuite_base (" +
                     "id INTEGER AUTO_INCREMENT, " +
                    "pet_uuid TEXT UNIQUE NOT NULL, " +
                    "owner TEXT, " +
                    "name TEXT, " +
                     "level INTEGER, " +
                     "kills INTEGER, " +
                     "PRIMARY KEY (id, owner)" +
                     ");";
             Statement state = connection.createStatement();
             state.executeUpdate(createBase);
             state.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public PreparedStatement prepare(String query) {
         if (connect()) {
             if (prepared.containsKey(query)) {
                 return prepared.get(query);
             } else {
                 try {
                     PreparedStatement statement = connection.prepareStatement(query);
                     prepared.put(query, statement);
                     return statement;
                 } catch (SQLException e) {
                 }
             }
         }
         return null;
     }
 
 }
