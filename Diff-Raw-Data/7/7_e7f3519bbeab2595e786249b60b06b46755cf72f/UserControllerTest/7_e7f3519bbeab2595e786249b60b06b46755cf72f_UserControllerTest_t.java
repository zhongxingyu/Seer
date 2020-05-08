 package com.twu.biblioteca.controllers;
 
 import com.twu.biblioteca.classes.Persistence;
 import com.twu.biblioteca.models.User;
 import junit.framework.Assert;
 import org.junit.Test;
 
 public class UserControllerTest {
 
     @Test
     public void autenticatesTheUser() {
         User u = new User("t", "t", "123456", "t");
        Persistence.addUser(u);
 
        Assert.assertTrue(UserController.autenticate(u));
     }
 
 }
