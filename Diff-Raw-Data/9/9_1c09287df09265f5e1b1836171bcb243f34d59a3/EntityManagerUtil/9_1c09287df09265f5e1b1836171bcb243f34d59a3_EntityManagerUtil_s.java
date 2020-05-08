 /*
  * EntityManagerUtil.java
  *
  * Created on September 11, 2010, 8:33 AM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.persistence;
 
 import com.rameses.sql.SqlContext;
 import com.rameses.sql.SqlExecutor;
 import java.util.Map;
 import java.util.Queue;
 
 /**
  *
  * @author elmo
  */
 public final class EntityManagerUtil {
     
     public static void executeQueue(Queue queue, SqlContext sqlContext, Map vars, boolean transactionOpen, boolean debug) throws Exception{
         try {
             if(!transactionOpen) sqlContext.openConnection();
             while(!queue.isEmpty()) {
                 SqlExecutor se= (SqlExecutor)queue.remove();
                 if(debug) {
                     System.out.println(se.getStatement());
                     int i = 0;
                     for(Object s: se.getParameterNames()) {
                         System.out.println("param->"+s+ "="+ se.getParameterValues().get(i++) );
                     }
                 }
                if( vars!=null ) se.setParameters(vars);
                 se.execute();
             }
         } catch(Exception e) {
             throw e;
             
         } finally {
             if(!transactionOpen) sqlContext.closeConnection();
         }
     }
     
     
     
 }
