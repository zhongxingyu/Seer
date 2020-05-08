 package com.lancktele.rest;
 
 import com.lancktele.rest.utils.Parser;
 import com.lancktele.rest.utils.PropertyLoader;
 import com.lanctele.rest.HttpRestApiPath;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import static org.junit.Assert.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: azee
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath*:applicationContext.xml")
 public class UsersTest {
 
     @Autowired
     private Parser parser;
 
     @Autowired
     private PropertyLoader propertyLoader;
 
     @Test
     public void getUserTest() throws Exception {
         HttpRestApiPath.Users.Uid call = new HttpRestApiPath.Users.Uid(propertyLoader.getEndpoint(), "ce853612f64d9668ccf8e04037e41514");
 
         //Getting response as string
         String response = call.getAsApplicationText(String.class);
         assertNotNull(response, "Can't get a response from Get User handler");
 
         //Getting user
         User user = parser.unmarshal(response, "user", User.class);
         assertNotNull("User is null", user);
 
         //Validating user's fields
         assertEquals(user.getBalance(), "1000.000000");
         assertEquals(user.getExternalId(), 63438328, 0);
         assertEquals(user.getExternalService(), "fotostrana");
         assertEquals(user.getGender(), "m");
         assertEquals(user.getNameFull(), "Anatoly Shuvalov");
         assertEquals(user.getPhoneNumber(), "79118360863");
         assertEquals(user.getPhoto(), "http://www.blogcdn.com/www.engadget.com/media/2008/04/johnny-videophone.jpg");
         assertTrue(user.isAcceptCalls());
     }
 }
