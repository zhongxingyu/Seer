 /**
  *
  */
 package org.jrecruiter.dao;
 
 import java.util.Date;
 
 import junit.framework.Assert;
 
 import org.jrecruiter.model.Role;
 import org.jrecruiter.model.User;
 import org.jrecruiter.model.UserToRole;
 import org.jrecruiter.test.BaseTest;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  * @author Gunnar Hillert
  * @version $Id$
  */
 public class UserToRoleDaoTest extends BaseTest {
 
	private @Autowired UserDao       userDao;
	private @Autowired UserToRoleDao userToRoleDao;
	private @Autowired RoleDao       roleDao;
 
     @Test
     public void testSaveUserToRole() {
 
         final Role role = new Role(null, "TEST");
         Role savedRole = roleDao.save(role);
         entityManager.flush();
 
        User user = userDao.get(-1L);
 
         UserToRole userToRole = new UserToRole(null, savedRole, user);
 
         UserToRole savedUserToRole = userToRoleDao.save(userToRole);
         entityManager.flush();
 
         Assert.assertNotNull(savedUserToRole.getId());
     }
 
     @Test
     public void testSaveUserToRole2() {
 
         final Role role = new Role(null, "TEST");
         Role savedRole = roleDao.save(role);
 
         User user = getUser();
 
         UserToRole userToRole = new UserToRole(null, savedRole, user);
 
         Assert.assertTrue(user.getUserToRoles().size() == 0);
 
         user.getUserToRoles().add(userToRole);
 
         Assert.assertTrue(user.getUserToRoles().size() == 1);
 
         User savedUser = userDao.save(user);
         entityManager.flush();
 
         Assert.assertTrue(savedUser.getUserToRoles().size() == 1);
     }
 
 
     private User getUser() {
 
         final User user = new User();
         user.setUsername("demo44");
         user.setEmail("demo@demo.com");
         user.setFirstName("Demo First Name");
         user.setLastName("Demo Last Name");
         user.setPassword("demo");
         user.setPhone("123456");
         user.setRegistrationDate(new Date());
 
         return user;
     }
 }
