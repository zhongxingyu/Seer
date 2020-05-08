 package org.jclarity.training.chapter11;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class UserEntityTest extends AbstractTest {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(UserEntityTest.class);
 
     @Test
    public void testNewUser() {
 
         User user = new User();
        user.setName(Long.toString(new Date().getTime()));
         entityManager.persist(user);
         entityManager.getTransaction().commit();
 
        // see that the ID of the user was set by Hibernate
        LOGGER.info("user=" + user + ", user.id=" + user.getId());
         User foundUser = entityManager.find(User.class, user.getId());
 
        // note that foundUser is the same instance as user and is a concrete class (not a proxy)
        LOGGER.info("foundUser=" + foundUser);
         assertEquals(user.getName(), foundUser.getName());
     }
 }
