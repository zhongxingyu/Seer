 package org.jclarity.training.chapter11;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class UserEntityTest extends AbstractTest {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(UserEntityTest.class);
 
     @Test
    public void persistedUserHasId() {
        User user = new User();
        user.setName("Ben");
        entityManager.persist(user);
        assertNotNull(user.getId());
    }

    @Test
    public void committedUserHasId() {
        User user = new User();
        user.setName("Ben");
        entityManager.persist(user);
        entityManager.getTransaction().commit();
        assertNotNull(user.getId());
    }

    @Test
    public void findNewUser() {
 
         User user = new User();
        user.setName("Ben");
         entityManager.persist(user);
         entityManager.getTransaction().commit();
 
         User foundUser = entityManager.find(User.class, user.getId());
 
        // Note that foundUser is the same instance as user and is a concrete class (not a proxy)
         assertEquals(user.getName(), foundUser.getName());
     }
 }
