 package org.test.bugtracker.impl.bo;
 
 import static junit.framework.Assert.*;
 
 import org.testng.annotations.Test;
 import org.hibernate.exception.ConstraintViolationException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.test.bugtracker.bo.UserBO;
 import org.test.bugtracker.impl.model.UserImpl;
 import org.test.bugtracker.model.User;
 
 @ContextConfiguration(locations = { "classpath:spring/context.xml" })
 public class UserImplTest extends AbstractTestNGSpringContextTests {
     private static final String PASS = "pass";
     private static final long ID = 1L;
     private static final String LOGIN = "login";
     @Autowired
     private ApplicationContext context;
     @Autowired
     private UserBO userBO;
 
     @Test(priority = 0)
     public void createNewUserTest() {
         User user = new UserImpl();
         user.setLogin(LOGIN);
         user.setPass(PASS);
         userBO.save(user);
         assertTrue(user.getId() == ID);
     }
 
     @Test(priority = 1, dependsOnMethods = "createNewUserTest")
     public void findUserTestById() {
         User user = userBO.findById(1L);
         assertNotNull(user);
         assertEquals(LOGIN, user.getLogin());
         assertEquals(PASS, user.getPass());
     }
 
     @Test(priority = 1, dependsOnMethods = "createNewUserTest")
     public void findUserTestByLogin() {
         User user = userBO.findByLogin(LOGIN);
         assertNotNull(user);
         assertEquals(LOGIN, user.getLogin());
         assertEquals(PASS, user.getPass());
     }
 
     @Test(priority = 1, dependsOnMethods = "createNewUserTest", expectedExceptions = { ConstraintViolationException.class })
     public void createUserWithSomeIdTest() {
         User user = new UserImpl();
         user.setId(1L);
         user.setLogin(LOGIN);
         user.setPass(PASS);
         userBO.save(user);
     }
 
     @Test(priority = 1, dependsOnMethods = "createNewUserTest", expectedExceptions = { ConstraintViolationException.class })
     public void createUserWithSomeLoginTest() {
         User user = new UserImpl();
         user.setLogin(LOGIN);
         user.setPass(PASS);
         userBO.save(user);
     }
 
    @Test(priority = 1, dependsOnMethods = "createNewUserTest")
    public void updateUserLoginTest() {
         User user = userBO.findById(1L);
         user.setLogin(LOGIN + "2");
         user.setPass(PASS + "2");
         userBO.update(user);
         User user2 = userBO.findById(1L);
         assertNotNull(user2);
         assertEquals(LOGIN + "2", user2.getLogin());
         assertEquals(PASS + "2", user2.getPass());
     }
 
    @Test(priority = 2, dependsOnMethods = "createNewUserTest")
     public void deleteUserTest() {
         User user = userBO.findById(1L);
         userBO.delete(user);
         User user2 = userBO.findById(1L);
         assertNull(user2);
     }
 }
