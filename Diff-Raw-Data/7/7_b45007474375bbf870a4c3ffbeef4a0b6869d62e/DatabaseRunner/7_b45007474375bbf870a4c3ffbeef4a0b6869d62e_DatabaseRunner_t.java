 package com.tyzoid.jailr.models;
 
 import com.tyzoid.jailr.JailrPlugin;
 import com.tyzoid.jailr.util.ExceptionPrinter;
 import com.tyzoid.jailr.util.Log;
 
 import java.sql.*;
 
 /**
  * @author Sushi
  */
 public class DatabaseRunner {
     ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
 
     Connection conn;
     DatabaseEngine engine;
     DatabaseMetaData meta;
     ResultSet rs;
     Statement st;
     String prefix;
 
     /* MySQL */
     String host;
     int port;
     String username;
     String password;
     String database;
 
     public DatabaseRunner() {
         this.engine = DatabaseCommon.getEngine();
 
         if (this.engine == DatabaseEngine.MYSQL) {
             this.host = JailrPlugin.getPlugin().getConfig().getString("database.host");
             this.port = JailrPlugin.getPlugin().getConfig().getInt("database.port");
             this.username = JailrPlugin.getPlugin().getConfig().getString("database.username");
             this.password = JailrPlugin.getPlugin().getConfig().getString("database.password");
             this.database = JailrPlugin.getPlugin().getConfig().getString("database.database");
             this.prefix = JailrPlugin.getPlugin().getConfig().getString("database.prefix");
         } else {
             this.prefix = "";
         }
     }
 
     public void initDb() {
         Log.info("[Database] Running database migrations...");
 
         switch (this.engine) {
             case H2:
                 /* * * * * * * *
                 * META RUNNER *
                 * * * * * * * */
                 try {
                     this.conn = DatabaseCommon.getConnection();
 
                     System.out.print(String.format("Looking for `%smeta` table... ", this.prefix));
                     this.meta = this.conn.getMetaData();
                     this.rs = this.meta.getTables(null, null, null, new String[]{this.prefix + "meta"});
 
                     if (this.rs.next()) {
                         System.out.println("Done!");
                     } else {
                         System.out.println("Not found.");
                         System.out.print(String.format("Attempting to create `%smeta` table... ", this.prefix));
 
                         this.st = this.conn.createStatement();
                         this.st.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.prefix + "meta (" +
                                 "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                                 "key VARCHAR(64) NOT NULL," +
                                 "value VARCHAR(128) NOT NULL" +
                                 ")");
 
                         System.out.println("Done!");
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (this.rs != null) {
                             this.rs.close();
                         }
 
                         if (this.st != null) {
                             this.st.close();
                         }
 
                         if (this.conn != null) {
                             this.conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
 
                 /* * * * * * * * * *
                  * PRISONER RUNNER *
                  * * * * * * * * * */
                 try {
                     this.conn = DatabaseCommon.getConnection();
 
                     System.out.print(String.format("Looking for `%sprisoners` table... ", this.prefix));
                     this.meta = this.conn.getMetaData();
                     this.rs = this.meta.getTables(null, null, null, new String[]{this.prefix + "prisoners"});
 
                     if (this.rs.next()) {
                         System.out.println("Done!");
                     } else {
                         System.out.println("Not found.");
                         System.out.print(String.format("Attempting to create `%sprisoners` table... ", this.prefix));
 
                         this.st = this.conn.createStatement();
                         this.st.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.prefix + "prisoners (" +
                                 "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                                 "player VARCHAR(20)," +
                                 "created_time INT," +
                                 "sentence_time INT," +
                                 "served_time INT," +
                                 "reason VARCHAR(45)," +
                                 "jailer VARCHAR(30)," +
                                "usergroup VARCHAR(30)," +
                                "inventory TEXT" +
                                 ")");
 
                         System.out.println("Done!");
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (this.rs != null) {
                             this.rs.close();
                         }
 
                         if (this.st != null) {
                             this.st.close();
                         }
 
                         if (this.conn != null) {
                             this.conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
                 break;
             case MYSQL:
                 /* * * * * * * *
                 * META RUNNER *
                 * * * * * * * */
                 try {
                     this.conn = DatabaseCommon.getConnection();
 
                     System.out.print(String.format("Looking for `%smeta` table... ", this.prefix));
                     this.meta = this.conn.getMetaData();
                     this.rs = this.meta.getTables(null, null, null, new String[]{this.prefix + "meta"});
 
                     if (this.rs.next()) {
                         System.out.println("Done!");
                     } else {
                         System.out.println("Not found.");
                         System.out.print(String.format("Attempting to create `%smeta` table... ", this.prefix));
 
                         this.st = this.conn.createStatement();
                         this.st.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.prefix + "meta (" +
                                 "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                                 "key VARCHAR(64) NOT NULL," +
                                 "value VARCHAR(128) NOT NULL" +
                                 ")");
 
                         System.out.println("Done!");
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (this.rs != null) {
                             this.rs.close();
                         }
 
                         if (this.st != null) {
                             this.st.close();
                         }
 
                         if (this.conn != null) {
                             this.conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
 
                 /* * * * * * * * * *
                  * PRISONER RUNNER *
                  * * * * * * * * * */
                 try {
                     this.conn = DatabaseCommon.getConnection();
 
                     System.out.print(String.format("Looking for `%sprisoners` table... ", this.prefix));
                     this.meta = this.conn.getMetaData();
                     this.rs = this.meta.getTables(null, null, null, new String[]{this.prefix + "prisoners"});
 
                     if (this.rs.next()) {
                         System.out.println("Done!");
                     } else {
                         System.out.println("Not found.");
                         System.out.print(String.format("Attempting to create `%sprisoners` table... ", this.prefix));
 
                         this.st = this.conn.createStatement();
                         this.st.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.prefix + "prisoners (" +
                                 "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                                 "player VARCHAR(20)," +
                                 "created_time INT," +
                                 "sentence_time INT," +
                                 "served_time INT," +
                                 "reason VARCHAR(45)," +
                                 "jailer VARCHAR(30)," +
                                 "usergroup VARCHAR(30)," +
                                 "inventory TEXT" +
                                 ")");
 
                         System.out.println("Done!");
                     }
                 } catch (SQLException ex) {
                     ExceptionPrinter.printFriendlyStackTrace(ex);
                 } finally {
                     try {
                         if (this.rs != null) {
                             this.rs.close();
                         }
 
                         if (this.st != null) {
                             this.st.close();
                         }
 
                         if (this.conn != null) {
                             this.conn.close();
                         }
                     } catch (SQLException ex) {
                         ExceptionPrinter.printFriendlyStackTrace(ex);
                     }
                 }
                 break;
         }
 
         Log.info("[Database] Done.");
     }
 }
