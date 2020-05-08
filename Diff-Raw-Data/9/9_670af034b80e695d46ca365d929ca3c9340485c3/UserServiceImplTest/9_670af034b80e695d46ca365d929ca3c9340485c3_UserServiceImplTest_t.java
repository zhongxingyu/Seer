 package org.yogocodes.bikewars.services.impl;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.yogocodes.bikewars.dao.UserDao;
 import org.yogocodes.bikewars.model.UserModel;
import org.yogocodes.bikewars.util.UserModelUtil;
 
 @RunWith(MockitoJUnitRunner.class)
 public class UserServiceImplTest {
 	@Mock
 	private UserDao userDao;
 	@InjectMocks
 	private UserServiceImpl userServiceImpl;
 
 	@Test
 	public void testGetUser() {
 		final Long sessionUserId = System.currentTimeMillis();
 
		final UserModel userModel = UserModelUtil.createUser(sessionUserId);
		Mockito.when(userDao.getUser(sessionUserId)).thenReturn(userModel);
 		final UserModel actualUser = userServiceImpl.getUser(sessionUserId);
 
 		Assert.assertNotNull("acutal user is null", actualUser);
 		Assert.assertEquals("actual user is altered", userModel, actualUser);
 
 	}
 
 }
