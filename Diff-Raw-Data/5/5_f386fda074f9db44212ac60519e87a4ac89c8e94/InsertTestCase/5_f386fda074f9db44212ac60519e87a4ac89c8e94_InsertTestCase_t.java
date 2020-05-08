 package com.rmatcher.core.json;
 
 import com.google.common.base.Joiner;
 
 import java.sql.*;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Joon-Sub
  * Date: 5/4/13
  * Time: 2:30PM
  */
 
 public class InsertTestCase {
 
     public static void main(String [] args) throws Exception{
 
         Connection connect = null;
 
         try {
             Class.forName("com.mysql.jdbc.Driver");
             connect = DriverManager
                     .getConnection("jdbc:mysql://localhost/rmatcher?"
                             //+ "user=root&password=123456");
                             + "user=root&password=");
             connect.setAutoCommit(false);
 
             List<TestCase> userTestlist = getUserTestCase(connect);
             Iterator<TestCase> userTestIterator = userTestlist.iterator();
            insertTestCase(connect, userTestIterator);
 
         } catch (Exception e) {
             throw e;
         } finally {
             if (connect != null) {
                 connect.close();
             }
         }
     }
 
     private static void insertTestCase(Connection connect, Iterator<TestCase> userTestlist) throws SQLException {
         PreparedStatement statement = connect
                 .prepareStatement("INSERT INTO rmatcher.viewTestCase values (?, ?, ?)");
 
         int count = 0;
         int currentUserCnt = 0;
         int thirtyPercent = 0;
         String userID = "";
 
         while(userTestlist.hasNext()){
             TestCase user = userTestlist.next();
 
             if (!userID.equals(user.getUser_id())){
                 userID = user.getUser_id();                               // current user's ID
                 System.out.println(userID);
                 currentUserCnt = 0;
                 thirtyPercent = (int)Math.ceil(user.getTotalCount()*.3);  // find 30%
             }
 
             currentUserCnt++;                                         // keep track of user cnt;
 
             if (currentUserCnt <= thirtyPercent){
                 statement.setString(1, user.getUser_id());
                 statement.setString(2, user.getReview_id());
                 statement.setInt(3, thirtyPercent);
                 statement.addBatch();
                 ++count;
             }
             if (count % 1000 == 0) {
                 statement.executeBatch();
                 count = 0;
             }
         }
         statement.executeBatch();
         connect.commit();
         statement.close();
     }
 
     public static List<TestCase> getUserTestCase(Connection connect) throws SQLException{
 
         List<TestCase> userList = new ArrayList<TestCase>();
         ResultSet resultSet = null;
         PreparedStatement statement = connect
                 .prepareStatement("SELECT * FROM rmatcher.viewFullTestCase");
 
         try{
             statement.execute();
             resultSet = statement.getResultSet();
             while (resultSet.next()) {
                 TestCase sb = new TestCase(resultSet.getString("user_id"), resultSet.getString("review_id"), resultSet.getInt("userTotalReview"));
                 userList.add(sb);
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
         return userList;
     }
 }
