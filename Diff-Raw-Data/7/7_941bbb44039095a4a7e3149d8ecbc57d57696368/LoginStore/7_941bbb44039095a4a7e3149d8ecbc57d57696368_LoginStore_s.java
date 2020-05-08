 package com.directi.train.tweetapp.services;
 
 import com.directi.train.tweetapp.model.UserItem;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Service;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: elricl
  * Date: 14/8/12
  * Time: 2:38 AM
  * To change this template use File | Settings | File Templates.
  */
 @Service
 public class LoginStore {
     @Autowired
    @Qualifier("simpleJdbcTemplate1")
    private SimpleJdbcTemplate db;
 
     public String registerUser(String email,String userName,String password) {
         List<UserItem> userData = db.query(String.format("select * from users where username='%s' or email='%s'",
                 userName, email), UserItem.rowMapper);
         UserItem userItem;
         try {
             userItem = userData.get(0);
             if(userItem.getEmail().equals(email) ){
                 return "1";
             }
             if(userItem.getUsername().equals(userName)){
                 return "2";
             }
         }
         catch (IndexOutOfBoundsException e) {
             password = PasswordStore.SHA(password);
             db.update(String.format("insert into users (email, username, password) values('%s', '%s', '%s')", email, userName, password));
         }
         return "0";
     }
 
     public UserItem checkLogin(String userName,String password) throws Exception{
         UserItem userData;
         try {
             userData = db.query(String.format("select * from users where username = '%s'", userName), UserItem.rowMapper).get(0);
             if (userData.getPassword().equals(PasswordStore.SHA(password))) {
                 userData.getId();
             } else {
                 throw new Exception("Invalid Password");
             }
         }
         catch (EmptyResultDataAccessException e) {
             throw new Exception("User does not exist.Please Register");
         }
         return userData;
     }
 
     public void changePassword(String password, String userName) {
         db.update(String.format("update users set password = '%s' where username = '%s'", PasswordStore.SHA(password), userName));
         String eMail = db.query(String.format("select email from users where username = '%s'", userName), new RowMapper<String>() {
             @Override
             public String mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getString("email");
             }
         }).get(0);
         PasswordStore.sendPassword(eMail, password);
     }
 
     public void forgotPassword(String userName) {
         String eMail = null;
         try {
             eMail = db.query(String.format("select email from users where username = '%s'", userName), new RowMapper<String>() {
                 @Override
                 public String mapRow(ResultSet resultSet, int i) throws SQLException {
                     return resultSet.getString("email");
                 }
             }).get(0);
         }  catch (Exception E) {
             E.printStackTrace();
         }
        String pwd = RandomStore.getPassword();
         db.update(String.format("update users set password = '%s' where email = '%s'", PasswordStore.SHA(pwd), eMail));
         PasswordStore.sendPassword(eMail, pwd);
     }}
