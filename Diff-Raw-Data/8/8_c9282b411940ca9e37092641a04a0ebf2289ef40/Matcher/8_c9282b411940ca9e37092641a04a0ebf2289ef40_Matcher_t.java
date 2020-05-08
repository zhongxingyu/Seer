 package com.rmatcher.core.matcher;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ameen
  * Date: 4/18/13
  * Time: 8:19 PM
  */
 
 import org.apache.commons.math3.linear.RealMatrix;
 import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import java.util.*;
 
 public class Matcher {
 
     public static void main(String [] args) throws Exception {
 
         Connection connect = null;
         PearsonsCorrelation cor = new PearsonsCorrelation();
 
         try {
             Class.forName("com.mysql.jdbc.Driver");
             connect = DriverManager
                     .getConnection("jdbc:mysql://localhost/rmatcher?user=root&password=");
             connect.setAutoCommit(false);
 
            String user = "j8ocPEVty-EEYQk_Zeldyg";
 
             Map<String, Double> userRatedBusinesses = getListOfBusinessesFromUser(user, connect);
             Map<String, Map<String, Double>> commonReviewsByUser = getReviewsForBusinesses(userRatedBusinesses.keySet(), connect);
 
             for (String user_id : commonReviewsByUser.keySet()) {
                 Map<String, Double> reviews = commonReviewsByUser.get(user_id);
                 double[][] xyRatings = new double[reviews.size()][2];
                 int i = 0;
                 for(String biz : reviews.keySet()){
                     xyRatings[i][0] = reviews.get(biz);
                     xyRatings[i][1] = userRatedBusinesses.get(biz);
                     i++;
                 }
                 if(i > 1){
                     RealMatrix rm = cor.computeCorrelationMatrix(xyRatings);
 
                    System.out.println("Num of biz: " + i + " ,Correlation: " + rm.getEntry(0,1));
                 }
 
             }
 
         } catch (Exception e) {
             throw e;
         } finally {
             if (connect != null) {
                 connect.close();
             }
         }
     }
 
     public static Map<String, Double> getListOfBusinessesFromUser(String user_id, Connection connect)
             throws SQLException{
 
         Map<String, Double> userRatedBusinesses = new HashMap<>();
         ResultSet resultSet = null;
         PreparedStatement statement = connect
                 .prepareStatement("SELECT business_id, stars FROM rmatcher.review WHERE user_id = ?");
 
         try{
             statement.setString(1, user_id);
             statement.execute();
             resultSet = statement.getResultSet();
             while (resultSet.next()) {
                 userRatedBusinesses.put(resultSet.getString("business_id"), resultSet.getDouble("stars"));
             }
         }  catch (Exception e) {
             throw e;
         }
         finally {
             if (resultSet != null) {
                 resultSet.close();
             }
             statement.close();
         }
         return userRatedBusinesses;
     }
 
     public static Map<String, Map<String, Double>> getReviewsForBusinesses(Set<String> bizSet, Connection connect)
             throws SQLException{
 
         Map<String, Map<String, Double>> reviews = new HashMap<String, Map<String, Double>>();
 
         ResultSet resultSet = null;
         PreparedStatement statement = connect
                 .prepareStatement("SELECT user_id, stars FROM rmatcher.review WHERE business_id = ?");
 
         try{
             for (String biz : bizSet) {
                 statement.setString(1, biz);
                 statement.execute();
                 resultSet = statement.getResultSet();
                 while (resultSet.next()) {
                     String user_id = resultSet.getString("user_id");
                     Double stars = resultSet.getDouble("stars");
 
                    if(!reviews.containsKey(user_id)){
                         reviews.put(user_id, new HashMap<String, Double>());
                     }
 
                     reviews.get(user_id).put(biz, stars);
                 }
             }
         }  catch (Exception e) {
             throw e;
         }
         finally {
             if (resultSet != null) {
                 resultSet.close();
             }
             statement.close();
         }
 
         return reviews;
     }
 
 
 }
