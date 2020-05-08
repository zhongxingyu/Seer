 package com.twu.biblioteca.controllers;
 
 import com.twu.biblioteca.classes.Persistence;
import com.twu.biblioteca.classes.Session;
 import com.twu.biblioteca.models.User;
 import junit.framework.Assert;
 import org.junit.Test;
 
 public class UserControllerTest {
 
     @Test
     public void autenticatesTheUser() {
         User u = new User("t", "t", "123456", "t");
 
        Assert.assertFalse(UserController.autenticate(u));
        Assert.assertTrue(Session.get(u) instanceof User);
     }
 
 }
