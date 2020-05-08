 package org.yogocodes.bikewars.services.impl;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.yogocodes.bikewars.dao.UserDao;
 import org.yogocodes.bikewars.model.UserModel;
 
 @RunWith(MockitoJUnitRunner.class)
 public class UserServiceImplTest {
 	@Mock
 	private UserDao userDao;
 	@InjectMocks
 	private UserServiceImpl userServiceImpl;
 
 	@Test
 	public void testGetUser() {
 		final Long sessionUserId = System.currentTimeMillis();
 
 		final UserModel actualUser = userServiceImpl.getUser(sessionUserId);
 
		final UserModel userModel = userDao.getUser(sessionUserId);

 		Assert.assertNotNull("acutal user is null", actualUser);
 		Assert.assertEquals("actual user is altered", userModel, actualUser);
 
 	}
 
 }
