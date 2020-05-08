 package com.jsoft.test;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Controller;
 
 import com.jsoft.domain.entities.User;
 import com.jsoft.services.UserService;
 
 
 /**
  * Made by aurbrsz / 12/26/11 - 17:41
  */
 public class UserTest {
 
   @Autowired private UserService userService;
 
   public static void main(String[] args) {
     new UserTest().runTest();
   }
 
   private void runTest() {
     ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext.xml");
     testRegisterUser();
     testLoadUser();
     testUpdateUser();
     testDeleteUser();
   }
 
   // We are registering a user : we put him in the db
   public void testRegisterUser() {
     System.out.println(userService);
     userService.saveUser("pseudo1", "Some email", "Some password");
   }
 
   // The user deletes his account
   public void testDeleteUser() {
 
   }
 
   // We want to update the user info
   public void testUpdateUser() {
 
   }
 
   // We create a user, load it in the db and delete it
   public void testLoadUser() {
     User loadedUser;
     loadedUser = userService.findUserByPseudo("pseudo0");
     System.out.println(loadedUser);
 
     loadedUser = userService.findUserByPseudo("pseudo1");
     System.out.println(loadedUser);
 
     Iterable<User> allUsers = userService.findAllUsers();
     for (User user : allUsers) {
       System.out.println(user.toString());
     }
 
   }
 //todo1: eviter dup
  //close db cleanly
  //faire marcher search
  //neo4j : browser
  //autowired userservice
 }
