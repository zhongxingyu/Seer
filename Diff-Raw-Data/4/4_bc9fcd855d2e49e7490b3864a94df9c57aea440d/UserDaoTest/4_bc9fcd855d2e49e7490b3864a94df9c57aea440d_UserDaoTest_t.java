 package com.thoughtworks.twu.persistence;
 
 
 import junit.framework.Assert;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import com.thoughtworks.twu.domain.User;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:applicationContext.xml"})
 @TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
 public class UserDaoTest {
 
     @Autowired
     private UserDao userDao;
 
    @Test @Ignore
     public void shouldGetUserName(){
         User user = new User();
         user.setName("rtessier");
         user.setEmail("rtessier@thoughtworks.com");
         userDao.saveUser(user);
 
         User userFromDatabase = userDao.getUserByName("rtessier");
 
         Assert.assertEquals("rtessier@thoughtworks.com", userFromDatabase.getEmail());
     }
 }
