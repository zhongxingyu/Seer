 package com.dynamobi.ws.util;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 /**
  * Convert resultSet to XML
  * @author Ray Zhang
  * @since Feb 05,2010
  *
  */
 public class JDBCUtil
 {
 
     private JDBCUtil()
     {
 
     }
 
     public static String resultSetToXML(ResultSet rs) throws SQLException
     {
 
         ResultSetMetaData rsmd = rs.getMetaData();
         int colCount = rsmd.getColumnCount();
         StringBuffer ret = new StringBuffer();       
         
         while (rs.next()) {
             ret.append("<Table>");  
             for (int i = 1; i <= colCount; i++) {
                 String columnName = rsmd.getColumnName(i);
                 String value = rs.getString(i);
                 ret.append("<"+columnName+">"+ value);
                 ret.append("</"+columnName+">");
             }
             ret.append("</Table>");  
         }
         return ret.toString();
         
     }
 
 }
