 package com.epam.kharkiv.cdp.oleshchuk.cinema;
 
 import com.epam.kharkiv.cdp.oleshchuk.cinema.service.TestUserService;
 import junit.framework.Assert;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
 @ContextConfiguration("file:src/main/resources/spring-config.xml")
 public class PropagationStrategiesTests {
 
     @Autowired
     private TestUserService userService;
 
 
     @Before
     public void cleanDBBefore() {
         userService.cleanTable();
     }
 
     @Test
     public void inserUserWithReadOnlyAndSupportsPropagation() {
         userService.insertUserReadOnlyPropagationSupports("testUser");
         checkResultOfTransaction(1);
     }
 
    @Test(expected = Exception.class)
     public void inserUserWithReadOnlyAndRequiredPropagation() {
         userService.insertUserReadOnlyPropagationRequired("testUser2");
        checkResultOfTransaction(0);
     }
 
     @Test(expected = Exception.class)
     public void insertUserOuterRequiresNewInnerRequiresNewAndOuterThrowsException() throws Exception {
         userService.insertUserPropagationRequiresNew("outerUser");
         checkResultOfTransaction(2);
     }
 
 
     @Test(expected = Exception.class)
     public void insertUserOuterRequiredInnerRequiresNewAndInnerThrowsRuntimeException() throws Exception {
         userService.testRequired("requiresNewUser");
         checkResultOfTransaction(0);
     }
 
     private void checkResultOfTransaction(int expectedRawCounts) {
         int realCount = userService.getAllCount();
         Assert.assertEquals(expectedRawCounts, realCount);
     }
 
     @After
     public void cleanDB() {
         userService.cleanTable();
     }
 }
