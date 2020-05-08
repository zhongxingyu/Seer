 package com.liferay.utility;
 
 import java.sql.*;
 import java.util.*;
 
 public class CustomFieldUtil {
     
   public static List<String> getAvailValues(String field, String prefix) throws SQLException {
       String connectionURL = "jdbc:mysql://localhost:3306/liferay?useUnicode=true&characterEncoding" +
               "=UTF-8&useFastDateParsing=false";
 
       try {
           Class.forName("com.mysql.jdbc.Driver").newInstance();
           Connection connection = DriverManager.getConnection(connectionURL, "lifedbuser",
                   "FCH9AWqDt2cpDvhQ");
           Statement statement = connection.createStatement();
          ResultSet rs = statement.executeQuery(String.format("select distinct data_ from expandovalue" +
                  " left join expandocolumn on expandocolumn.columnId = expandovalue.columnId where" +
                  " expandocolumn.name = '%s' and expandovalue.data_ like '%s%%';", field, prefix));
 
           List<String> result = new ArrayList<String>();
           while (rs.next()) {
               String rsn = rs.getString("data_");
               result.add(rsn);
           }
 
           rs.close();
           return result;
       } catch (ClassNotFoundException c) {
           System.out.println(c);
           return null;
       } catch (InstantiationException ie) {
           System.out.println(ie);
           return null;
       } catch (IllegalAccessException iae) {
           System.out.println(iae);
           return null;
       }
   }
     
 }
