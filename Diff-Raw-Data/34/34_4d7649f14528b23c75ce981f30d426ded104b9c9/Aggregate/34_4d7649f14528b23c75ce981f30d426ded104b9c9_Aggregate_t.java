 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.etsai.kfsxtrackingserver.stats;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Stores the collective sum of the player stats
  * @author etsai
  */
 public class Aggregate extends Death {
     public static Aggregate build(ResultSet rs) throws SQLException {
         Aggregate aggregate= new Aggregate(rs.getInt("id"), rs.getString("stat"), 
                 rs.getString("category"));
        aggregate.addValue(Long.valueOf(rs.getString("value")));
         return aggregate;
     }
     private final String category;
     
     public Aggregate(int id, String stat, String category) {
         super(id, stat);
         this.category= category;
     }
     
     public String getCategory() {
         return category;
     }
     
 }
