 /**
  * 
  */
 package org.acme;
 
 import static org.mockito.Mockito.*;
 
 import javax.inject.Inject;
 
 import org.acme.util.GuiceInject;
 import org.acme.util.ModuleAnnotations;
 import org.acme.util.Provide;
 import org.acme.util.TestUserProvider;
 import org.cotrix.test.ApplicationTest;
 import org.cotrix.web.client.presenter.UserBarPresenter;
 import org.cotrix.web.client.presenter.UserController;
 import org.cotrix.web.client.view.LoginDialog;
 import org.cotrix.web.client.view.RegisterDialog;
 import org.cotrix.web.client.view.UserBarView;
 import org.cotrix.web.common.client.widgets.AlertDialog;
 import org.cotrix.web.common.shared.exception.IllegalActionException;
 import org.cotrix.web.common.shared.exception.ServiceException;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class TestUserAuth extends ApplicationTest {
 	
 	@Mock @Provide
 	UserBarView userBarView;
 
 	@Mock @Provide
 	LoginDialog loginDialog;
 
 	@Mock @Provide
 	RegisterDialog registerDialog;
 	
 	@Mock @Provide
 	AlertDialog alertDialog;
 
 	@Inject
 	TestUserProvider testUserProvider; 
 	
 	@GuiceInject
 	UserBarPresenter presenter;
 	
 	@GuiceInject
 	UserController controller;
 
 	@Before
 	public void setup() {
 
 		MockitoAnnotations.initMocks(this);
 		
 		ModuleAnnotations.init(this);
 		
 		testUserProvider.createTestUser();
 	}
 	
 	@Test
 	public void testLogin() throws IllegalActionException, ServiceException {
 		
 		//User clicks login
 		presenter.onLoginClick();
 		
 		//Login dialog appears
 		verify(loginDialog).showCentered();
 
 		//User fills login form and he submits it
 		presenter.onLogin(testUserProvider.getUser().name(), testUserProvider.getPassword());
 		
 		//User is logged
 		verify(userBarView).setUsername(testUserProvider.getUser().name());
 	}
 	
 	@Test
 	public void testLoginFail() throws IllegalActionException, ServiceException {
 		
 		//User clicks login
 		presenter.onLoginClick();
 		
 		//Login dialog appears
 		verify(loginDialog).showCentered();
 
 		//User fills login form and he submits it
 		presenter.onLogin("unknow user", "unknown password");
 		
 		//Alert dialog is shown
 		verify(alertDialog).center(Mockito.anyString(), Mockito.anyString());
 	}
 	
 	@Test
 	public void testSignup() throws IllegalActionException, ServiceException {
 		
 		String username = "newUser";
 		
 		//User clicks sign-up
 		presenter.onRegister();
 		
 		//Register dialog appears
 		verify(registerDialog).showCentered();
 
 		//User fills register form and he submits it
 		presenter.onRegister(username, "newPassword", "email@test.te");
 		
 		//User is registered and logged
 		verify(userBarView).setUsername(username);
 	}
 	
 	@Test
 	public void testSignupFail() throws IllegalActionException, ServiceException {
 		
 		String username = testUserProvider.getUser().name();
 		
 		//User clicks sign-up
 		presenter.onRegister();
 		
 		//Register dialog appears
 		verify(registerDialog).showCentered();
 
 		//User fills register form and he submits it
 		presenter.onRegister(username, "newPassword", "email@test.te");
 		
 		//Alert dialog is shown
		verify(alertDialog).center(Mockito.anyString(), Mockito.anyString());
 	}
 
 }
