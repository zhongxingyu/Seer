 /*
  * Copyright (c) 2012 GuntherDW
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package com.guntherdw.bukkit.TweakWarp.DataSource;
 
 import com.guntherdw.bukkit.TweakWarp.TweakWarp;
 import com.guntherdw.bukkit.TweakWarp.Warp;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author GuntherDW
  */
 public class MySQL {
 
     private TweakWarp plugin;
     private String db, user, pass, dbhost;
 
     public MySQL(TweakWarp instance) {
         this.plugin = instance;
         this.loadDriver();
         this.setupConnection();
     }
 
     public void initConfig()
     {
         try{
             plugin.getConfig().set("database", "databasename");
             plugin.getConfig().set("username", "database-username");
             plugin.getConfig().set("password", "database-password");
         } catch (Throwable e)
         {
             plugin.getLogger().severe("[TweakWarp] There was an exception while we were saving the config, be sure to doublecheck!");
         }
     }
 
     private void loadDriver() {
         final String driverName = "com.mysql.jdbc.Driver";
         try {
             Class.forName(driverName);
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     private Connection getConnection()
     {
         try {
             String url = "jdbc:mysql://"+dbhost+":3306/" + db;
             return DriverManager.getConnection(url + "?autoReconnect=true&user=" + user + "&password=" + pass);
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         }
     }
 
    public void setupConnection() {
         this.dbhost = plugin.getConfig().getString("dbhost");
         this.db =  plugin.getConfig().getString("database");
         this.user = plugin.getConfig().getString("username");
         this.pass = plugin.getConfig().getString("password");
     }
 
     public List<Warp> getAllWarps() {
         Connection conn = getConnection();
         PreparedStatement st = null;
         ResultSet rs = null;
         List<Warp> warps = null;
         Warp temp = null;
         try {
             int count = 0;
             st = conn.prepareStatement("SELECT id,name,x,y,z,pitch,yaw,world,warpgroup,accessgroup FROM `warps`");
             rs = st.executeQuery();
             while(rs.next()) {
                 temp = new Warp(rs.getDouble("x"),
                         rs.getDouble("y"),
                         rs.getDouble("z"),
                         rs.getFloat("pitch"),
                         rs.getFloat("yaw"),
                         rs.getString("name"),
                         rs.getString("world"),
                         rs.getString("warpgroup"),
                         rs.getString("accessgroup")
                 );
                 temp.setId(rs.getInt("id"));
 
                 if(warps==null)
                     warps = new ArrayList<Warp>();
                 warps.add(temp);
 
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 if(conn!=null) conn.close();
                 if(st!=null) st.close();
                 if(rs!=null) rs.close();
             } catch(Exception ex) { ; }
         }
         return warps;
     }
 
     public Warp getWarp(int id) {
         Connection conn = getConnection();
         PreparedStatement st = null;
         ResultSet rs = null;
         Warp temp = null;
         try {
             st = conn.prepareStatement("SELECT id,name,x,y,z,pitch,yaw,world,warpgroup,accessgroup FROM `warps` WHERE `id` = ?");
             st.setInt(1, id);
             rs = st.executeQuery();
             if(rs.next()) {
                 temp = new Warp(rs.getDouble("x"),
                         rs.getDouble("y"),
                         rs.getDouble("z"),
                         rs.getFloat("pitch"),
                         rs.getFloat("yaw"),
                         rs.getString("name"),
                         rs.getString("world"),
                         rs.getString("warpgroup"),
                         rs.getString("accessgroup")
                 );
                 temp.setId(rs.getInt("id"));
             }
 
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 if(conn!=null) conn.close();
                 if(st!=null) st.close();
                 if(rs!=null) rs.close();
             } catch(Exception ex) { ; }
         }
         return temp;
     }
 
     public Warp getWarp(String name, String warpgroup) {
         Connection conn = getConnection();
         PreparedStatement st = null;
         ResultSet rs = null;
         Warp temp = null;
         try {
             st = conn.prepareStatement("SELECT id,name,x,y,z,pitch,yaw,world,warpgroup,accessgroup FROM `warps` WHERE `name` = ? AND `warpgroup` = ?");
             st.setString(1, name);
             st.setString(2, warpgroup);
             rs = st.executeQuery();
             if(rs.next()) {
                 temp = new Warp(rs.getDouble("x"),
                         rs.getDouble("y"),
                         rs.getDouble("z"),
                         rs.getFloat("pitch"),
                         rs.getFloat("yaw"),
                         rs.getString("name"),
                         rs.getString("world"),
                         rs.getString("warpgroup"),
                         rs.getString("accessgroup")
                 );
                 temp.setId(rs.getInt("id"));
             }
 
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 if(conn!=null) conn.close();
                 if(st!=null) st.close();
                 if(rs!=null) rs.close();
             } catch(Exception ex) { ; }
         }
         return temp;
     }
 
     public int addWarp(Warp warp) { // Returns the id for the newly generated warp
         if(warp==null) return -1;
         //boolean isUpdate = warp.getId()!=-1?true:false;
 
         Connection conn = getConnection();
         PreparedStatement st = null;
         ResultSet rs = null;
         try {
             // Delete any warp that was there beforehand
             this.deleteWarp(warp.getName(), warp.getWarpgroup());
 
             st = conn.prepareStatement("INSERT INTO `warps` (name,x,y,z,pitch,yaw,world,warpgroup,accessgroup) VALUES (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
             st.setString(1, warp.getName());
             st.setDouble(2, warp.getX());
             st.setDouble(3, warp.getY());
             st.setDouble(4, warp.getZ());
             st.setFloat (5, warp.getPitch());
             st.setFloat (6, warp.getYaw());
             st.setString(7, warp.getWorld());
             st.setString(8, warp.getWarpgroup());
             st.setString(9, warp.getAccessgroup());
 
             // st.executeQuery();
             st.executeUpdate();
             rs = st.getGeneratedKeys();
             if(rs.next())
                 return rs.getInt(1);
 
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 if(conn!=null) conn.close();
                 if(st!=null) st.close();
                 if(rs!=null) rs.close();
             } catch(Exception ex) { ; }
         }
         return -1;
     }
 
     public void deleteWarp(int id) {
         Connection conn = getConnection();
         PreparedStatement st = null;
         try{
             st = conn.prepareStatement("DELETE FROM `warps` WHERE `id` = ?");
             st.setInt(1, id);
             st.executeUpdate();
            return;
 
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 if(conn!=null) conn.close();
                 if(st!=null) st.close();
                 // if(rs!=null) rs.close();
             } catch(Exception ex) { ; }
         }
     }
 
     public int updateGroup(int id, String newGrp) {
         Connection conn = getConnection();
         PreparedStatement st = null;
         try{
             st = conn.prepareStatement("UPDATE `warps` SET `warpgroup` = ? WHERE `id` = ?");
             st.setString(1, newGrp);
             st.setInt   (2, id);
             return st.executeUpdate();
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 if(conn!=null) conn.close();
                 if(st!=null) st.close();
             } catch(Exception ex) { ; }
         }
         return 0;
     }
 
     public void deleteWarp(String warpname, String warpgroup) {
         if(warpname==null||warpname.trim().equals("")) return;
         Connection conn = getConnection();
         PreparedStatement st = null;
         try{
             st = conn.prepareStatement("DELETE FROM `warps` WHERE `name` = ? AND `warpgroup` = ?");
             st.setString(1, warpname);
             st.setString(2, warpgroup);
             st.executeUpdate();
            return;
 
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try{
                 if(conn!=null) conn.close();
                 if(st!=null) st.close();
             } catch(Exception ex) { ; }
         }
     }
 
 }
