 package net.incredibles.brtaquiz.controller;
 
 import com.google.inject.Inject;
 import net.incredibles.brtaquiz.dao.UserDao;
 import net.incredibles.brtaquiz.domain.User;
 import net.incredibles.brtaquiz.service.Session;
 import net.incredibles.brtaquiz.testhelpers.DbTableTruncater;
 import net.incredibles.brtaquiz.testhelpers.InjectedTestRunner;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import java.sql.SQLException;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author sharafat
  * @Created 2/18/12 8:29 PM
  */
 @RunWith(InjectedTestRunner.class)
 public class LoginControllerTest {
     private static final String SAMPLE_REG_NO = "123";
     private static final String SAMPLE_PIN_NO = "456";
 
     @Inject
     private LoginController loginController;
     private UserDao userDao;
     private Session session;
 
     @Before
     public void setup() {
         userDao = loginController.getUserDao();
         session = loginController.getSession();
     }
 
     @Test
     public void testLogin_ForExistingUser() throws SQLException {
        User expected = new User(SAMPLE_REG_NO, SAMPLE_PIN_NO);
        userDao.save(expected);
 
         boolean success = loginController.login(SAMPLE_REG_NO, SAMPLE_PIN_NO);
         assertThat(success, is(false));
 
         User actual = session.getLoggedInUser();
         assertThat(actual.toString(), equalTo(expected.toString()));
     }
 
     @Test
     public void testLogin_ForNewUser() throws SQLException {
         deleteAllUsers();
 
         User expected = userDao.getByRegistrationAndPinNo(SAMPLE_REG_NO, SAMPLE_PIN_NO);
         assertThat(expected, nullValue());
 
         boolean success = loginController.login(SAMPLE_REG_NO, SAMPLE_PIN_NO);
        assertThat(success, is(false));
 
         expected = userDao.getByRegistrationAndPinNo(SAMPLE_REG_NO, SAMPLE_PIN_NO);
         assertThat(expected, notNullValue());
         User actual = session.getLoggedInUser();
         assertThat(actual.getId(), not(0));
         assertThat(actual.toString(), equalTo(expected.toString()));
     }
 
     private void deleteAllUsers() {
         DbTableTruncater.truncate(User.class);
     }
 
 }
