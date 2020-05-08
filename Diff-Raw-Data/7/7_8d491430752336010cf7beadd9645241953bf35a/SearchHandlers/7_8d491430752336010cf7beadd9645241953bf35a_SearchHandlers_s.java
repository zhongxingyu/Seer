 package com.example.xpSearchLiang.web;
 
 import com.britesnow.snow.web.handler.annotation.WebModelHandler;
 import com.britesnow.snow.web.param.annotation.WebModel;
 import com.britesnow.snow.web.param.annotation.WebParam;
 import com.example.xpSearchLiang.DBManager;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Singleton
 public class SearchHandlers {
 
    public static final String sql = "select title,ts_headline(body, plainto_tsquery('%s'))   body, " +
            "                ts_rank(to_tsvector(body||tags||title), plainto_tsquery('%s')) rank " +
            "                from xpsearchliang_schema.post order by rank";
 
     @Inject
     DBManager dbManager;
 
 
     @WebModelHandler(startsWith="/search")
     public void search(@WebModel Map m, @WebParam("q")String q){
         Connection conn = dbManager.getConnection();
         List ls = new ArrayList();
         try {
             PreparedStatement ps = conn.prepareStatement(String.format(sql, q, q));
             ResultSet rs = ps.executeQuery();
             while(rs.next()){
                 Map map = new HashMap();
                 map.put("title", rs.getString("title"));
                 map.put("body", rs.getString("body"));
                 ls.add(map);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }finally {
             try {
                 conn.close();
             } catch (SQLException e) {
                 //
             }
         }
         m.put("results", ls);
         m.put("q", q);
     }
 }
