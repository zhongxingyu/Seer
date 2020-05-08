 package com.candou.ic.navigation.wxdh.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.List;
 
 import com.candou.db.Database;
 import com.candou.ic.navigation.wxdh.vo.App;
 import com.candou.util.DateTimeUtil;
 
 public class AppDao {
     private static String table_Name = "wxdh_app";
 
     public static void addBatchApps(List<App> apps) {
         try {
             Connection connection = Database.getConnection();
             PreparedStatement ps = connection
                 .prepareStatement("insert ignore into "+table_Name+" (id, name, intro, url, f, oc, wsu, detail, dts, cid, cname, icon, icon_name, imc,imc_name, sc,direct_number,created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 
             for (App app : apps) {
                 String now = DateTimeUtil.nowDateTime();
 
                 ps.setInt(1, app.getId());
                 ps.setString(2, app.getName());
                 ps.setString(3, app.getIntro());
                 ps.setString(4, app.getUrl());
                 ps.setInt(5, app.getF());
                 ps.setString(6, app.getOc());
                 ps.setString(7,app.getWsu());
                 ps.setString(8, app.getDetail());
                ps.setInt(9, app.getDts());
                ps.setInt(10, app.getCid());
                 ps.setString(11, app.getCname());
                 ps.setString(12, app.getIcon());
                 ps.setString(13, app.getIcon_name());
                 ps.setString(14, app.getImc());
                 ps.setString(15, app.getImc_name());
                 ps.setString(16, app.getSc());
                 ps.setString(17, app.getDirect_number());
                 ps.setString(18, now);
                 ps.setString(19, now);
 
                 ps.executeUpdate();
             }
 
             ps.close();
             connection.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
 
 
 
 }
