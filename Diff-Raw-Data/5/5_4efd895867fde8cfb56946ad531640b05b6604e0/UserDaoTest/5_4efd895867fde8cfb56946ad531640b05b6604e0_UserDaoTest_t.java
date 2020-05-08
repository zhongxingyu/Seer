 package com.ec.deploy.persist.dao.auth;
 
 import javax.annotation.Resource;
 
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.ec.deploy.model.auth.User;
 import com.ec.deploy.model.tenancy.Tenant;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(
     locations = {
         "classpath:/spring/persist-context.xml"
     })
 @Transactional
 @TransactionConfiguration
 public class UserDaoTest
 {
 
     @PersistenceContext
     private EntityManager entityManager;
 
     @Resource(name = "UserDao")
     private UserDao userDao;
 
     private User user;
     private Tenant tenant;
     private static final String defaultLastName;
     private static final String defaultFirstName;
    private static final String defaultUsername;
 
     static {
         defaultFirstName = "Josiah";
         defaultLastName = "Haswell";
        defaultUsername = "josiahhaswell";
     }
 
     @Before
     public void setUp() {
         createTenant();
         createUser();
     }
 
     private void createUser()
     {
         user = new User();
         user.setTenant(tenant);
         user.setLastName(defaultLastName);
         user.setFirstName(defaultFirstName);
        user.setUsername(defaultUsername);
     }
 
     private void createTenant()
     {
         tenant = new Tenant();
         tenant.setName("Pepsi");
         tenant.setDescription("Just a tenant");
         entityManager.persist(tenant);
     }
     
     @Test
     public void ensureDaoCanPersistValidUser() {
         assertTrue(userDao.save(user));
     }
     @Test
     public void ensureDaoReturnsFalseWhenAttemptingToPersistNull()
     {
         assertFalse(userDao.save(null));
     }
     
     @Test
     public void ensureDaoReturnsFalseWhenAttemptingToPersistInvalidUser() {
         user.setLastName(null);
         assertFalse(userDao.save(user));
     }
 
     @Test
     public void ensureSaveIncrementsCount() {
         assertEquals(userDao.count(), 0l);
         userDao.save(user);
         assertEquals(userDao.count(), 1l);
     }
 
     @Test
     public void ensureListOnEmptyRepositoryReturnsNoElements() {
         assertTrue(userDao.list().isEmpty());
     }
 
     @Test
     public void ensureListOnRepositoryWithSingleUserReturnsUser() {
         userDao.save(user);
         assertEquals(userDao.list().size(), 1);
         assertEquals(userDao.list().get(0).getFirstName(), user.getFirstName());
     }
     
     @Test
     public void ensure() {
 
     }
 }
