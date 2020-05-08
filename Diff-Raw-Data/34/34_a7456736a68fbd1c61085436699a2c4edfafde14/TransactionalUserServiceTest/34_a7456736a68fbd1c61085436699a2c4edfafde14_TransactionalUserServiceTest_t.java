 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.service.transactional;
 
 import static org.testng.AssertJUnit.assertNotNull;
 import static org.testng.AssertJUnit.assertNull;
 import static org.testng.AssertJUnit.assertEquals;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.jtalks.common.model.dao.UserDao;
 import org.jtalks.common.model.entity.User;
 import org.jtalks.poulpe.service.UserService;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import static org.mockito.Mockito.*;
 
 /**
  * Class for testing {@code TransactionalUserService} functionality.
  * 
  * @author Guram Savinov
  * 
  */
 public class TransactionalUserServiceTest {
 
     private UserDao userDao;
     private UserService userService;
 
     private static final String EMAIL = "username@mail.com";
     private static final String PASSWORD = "password";
     private static final String BAN_REASON = "ban reason";
     private static Collection<User> users;
 
     @BeforeMethod
     public void setUp() {
         userDao = mock(UserDao.class);
 
        userService = new TransactionalUserService(userDao);
 
         users = new ArrayList<User>();
         users.add(getUser("tony"));
         users.add(getUser("antony"));
         users.add(getUser("jack"));
     }
 
     @Test(expectedExceptions = NullPointerException.class)
     public void testSetPermanentBanStatusUsersNull() {
         userService.setPermanentBanStatus(null, true, BAN_REASON);
     }
 
     @Test(expectedExceptions = NullPointerException.class)
     public void testSetTemporaryBanStatusUsersNull() {
         userService.setTemporaryBanStatus(null, 1, BAN_REASON);
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void testSetTemporaryBanStatusIllegalArgument() {
         userService.setTemporaryBanStatus(users, -1, BAN_REASON);
     }
 
     @Test
     public void testSetPermanentBanStatus() {
         userService.setPermanentBanStatus(users, true, BAN_REASON);
 
         for (User user : users) {
             assertEquals(true, user.isPermanentBan());
             assertNull(user.getBanExpirationDate());
             assertEquals(user.getBanReason(), BAN_REASON);
             verify(userDao).saveOrUpdate(user);
         }
     }
 
     @Test
     public void testSetTemporaryBanStatus() {
         for (User user : users) {
             user.setPermanentBan(true);
         }
 
         userService.setTemporaryBanStatus(users, 5, BAN_REASON);
 
         for (User user : users) {
             assertEquals(false, user.isPermanentBan());
             assertNotNull(user.getBanExpirationDate());
             assertEquals(user.getBanReason(), BAN_REASON);
             verify(userDao).saveOrUpdate(user);
         }
     }
 
     /**
      * Creates and return the {@link User} entity with default username, email
      * and password, etc.
      * 
      * @param username
      *            username
      * @return the user entity
      */
     private User getUser(String username) {
         User user = new User(username, EMAIL, PASSWORD);
         return user;
     }
 
 }
