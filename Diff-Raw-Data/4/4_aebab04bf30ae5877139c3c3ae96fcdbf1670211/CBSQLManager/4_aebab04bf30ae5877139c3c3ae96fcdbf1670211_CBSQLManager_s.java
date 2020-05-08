 package com.minecraftserver.castlebreach;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import lib.PatPeter.SQLibrary.*;
 
 public class CBSQLManager {
     MySQL        db;
     CastleBreach plugin;
 
     public CBSQLManager(CastleBreach parent) {
         plugin = parent;
        db = new MySQL(plugin.getLogger(), "CB", "CastleBreach", "root", "LoLxD");
         openDB();
         firstRun();
     }
 
     private boolean openDB() {
         return db.open();
     }
 
     private boolean closeDB() {
         return db.close();
     }
 
     private void firstRun() {
         if (!db.isConnected()) return;
         Statement statement;
         // Creating Tables
         if (!db.isTable("PlayerPoints")) {
             try {
                 db.query(db
                         .prepare("CREATE TABLE PlayerPoints(P_Id int NOT NULL AUTO_INCREMENT,Playername varchar(255),Points int)"));
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private ResultSet executeQuery(String query) {
         try {
             PreparedStatement ps = db.prepare(query);
             ResultSet rs = db.query(ps);
             return rs;
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public void addPoints(String playername, int newpoints) {
         int points = getPoints(playername);
         if (points != 0)
             executeQuery("insert into PlayerPoints (Playername, Points)Values (" + playername + ","
                     + newpoints + ");");
         else executeQuery("update PlayerPoints set (Points=" + (points + newpoints)
                 + ") where Playername='" + playername + "';");
 
     }
 
     public int getPoints(String playername) {
         try {
             ResultSet rs = executeQuery("Select Points From PlayerPoints where Playername='"
                     + playername + "';");
             if (rs != null) {
                 rs.next();
                 return rs.getInt(1);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return 0;
     }
 
     public void stop() {
         closeDB();
     }
 
 }
