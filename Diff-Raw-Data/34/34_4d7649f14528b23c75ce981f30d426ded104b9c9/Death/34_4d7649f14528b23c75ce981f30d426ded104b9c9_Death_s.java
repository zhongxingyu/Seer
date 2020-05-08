 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.etsai.kfsxtrackingserver.stats;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * Tallies the death count
  * @author etsai
  */
 public class Death {
     public static Death build(ResultSet rs) throws SQLException {
         Death death= new Death(rs.getInt("id"), rs.getString("name"));
         death.value.getAndSet(rs.getInt("count"));
         return death;
     }
     
     private final int id;
     private final String stat;
     private AtomicLong value;
 
     public Death(int id, String stat) {
         this.id= id;
         this.stat= stat;
         value= new AtomicLong();
     }
     
     public int getId() {
         return id;
     }
     public String getStat() {
         return stat;
     }
     
     public String getValue() {
         return value.toString();
     }
     
    public void addValue(int offset) {
         value.getAndAdd(offset);
     }
 }
