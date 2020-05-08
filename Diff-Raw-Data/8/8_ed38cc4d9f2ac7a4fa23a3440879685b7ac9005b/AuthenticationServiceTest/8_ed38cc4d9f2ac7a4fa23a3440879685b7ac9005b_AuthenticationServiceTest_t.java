 package wad.tukki.services;
 
 import org.junit.After;
import static org.junit.Assert.*;
 import org.junit.Test;
 import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import wad.tukki.models.User;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/spring-context.xml",
                                    "file:src/main/webapp/WEB-INF/spring-database.xml",
                                    "file:src/main/webapp/WEB-INF/spring-security.xml"})
 public class AuthenticationServiceTest {
 
     @Autowired
     AuthenticationService authenticationService;
     
     @After
     public void tearDown() {
         authenticationService.invalidate();
     }
     
     @Test
     public void canAuthenticateWithExistingUser() {
         
         User user = new User();
         user.setUsername("kasper");
         user.setPassword("repsakkasper");
         
         authenticationService.authenticate(user);
         
         assertTrue(authenticationService.isAuthenticated());
     }
     
     @Test(expected = BadCredentialsException.class)
     public void badCredentialsCaught() {
         
         User user = new User();
         user.setUsername("kasper");
         user.setPassword("kasper");
         
         authenticationService.authenticate(user);
     }
     
     @Test
     public void afterInvalidationNotAuthenticated() {
         
         User user = new User();
         user.setUsername("kasper");
         user.setPassword("repsakkasper");
         
         authenticationService.authenticate(user);
         authenticationService.invalidate();
         
         assertFalse(authenticationService.isAuthenticated());
     }
     
     @Test
     public void getUsernameOfUnauthenticatedUserReturnsNull() {
         assertEquals(null, authenticationService.getUsername());
     }
     
     @Test
     public void correctUsernameForAuthenticatedUser() {
         
         User user = new User();
         user.setUsername("kasper");
         user.setPassword("repsakkasper");
         
         authenticationService.authenticate(user);
         
         assertEquals("kasper", authenticationService.getUsername());
     }
 }
