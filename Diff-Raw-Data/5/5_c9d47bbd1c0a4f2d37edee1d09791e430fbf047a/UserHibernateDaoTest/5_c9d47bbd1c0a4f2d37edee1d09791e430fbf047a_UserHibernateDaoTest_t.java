 /**
  * Copyright (C) 2011  jtalks.org Team
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
  * Also add information on how to contact you by electronic and paper mail.
  * Creation date: Apr 12, 2011 / 8:05:19 PM
  * The jtalks.org Project
  */
 package org.jtalks.jcommune.model.dao.hibernate;
 
 import org.hibernate.SessionFactory;
 import org.jtalks.jcommune.model.entity.Persistent;
 import org.jtalks.jcommune.model.entity.User;
 import org.springframework.test.context.ContextConfiguration;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import javax.annotation.Resource;
 import java.util.List;
 
 /**
  * DAO tests for instance of {@link UserHibernateDao}
  *
  * @author Artem Mamchych
  */
 @ContextConfiguration(locations = {"classpath:/org/jtalks/jcommune/model/entity/applicationContext-dao.xml"})
 public class UserHibernateDaoTest extends BaseTest {
     private static final String USERNAME = "NickName";
     /**
      * Hibernate Session Factory instance.
      */
     @Resource(name = "sessionFactory")
     private SessionFactory sessionFactory;
     private UserHibernateDao dao;
     private User entity;
     private List<User> listAll;
 
     @BeforeMethod
     public void setUp() throws Exception {
         dao = new UserHibernateDao();
         dao.setSessionFactory(sessionFactory);
         Assert.assertNotNull(sessionFactory, SESSION_FACTORY_IS_NULL);
         entity = new User();
         entity.setFirstName("FirstName");
         entity.setLastName("LastName");
         entity.setUsername(USERNAME);
         entity.setEmail("mail@mail.com");
         entity.setPassword("password");
 
         clearDbTable(entity, sessionFactory);
     }
 
     @AfterMethod
     public void tearDown() throws Exception {
         entity = null;
     }
 
     @Test
     public void testEntityState() throws Exception {
         testSave();
         listAll = dao.getAll();
         Assert.assertTrue(entity.equals(listAll.get(0)), PERSISTENCE_ERROR);
         Assert.assertFalse(entity.equals(listAll.get(1)), PERSISTENCE_ERROR);
     }
 
     @Test
     public void testDBEmpty() throws Exception {
         int sizeBefore = dao.getAll().size();
         Assert.assertEquals(0, sizeBefore, DB_TABLE_NOT_EMPTY);
     }
 
     @Test
     public void testSave() throws Exception {
         //Add 2 users to DB
         dao.saveOrUpdate(entity);
         dao.saveOrUpdate(new User());
 
         int size = dao.getAll().size();
         Assert.assertEquals(2, size, ENTITIES_IS_NOT_INCREASED_BY_2);
     }
 
     @Test
     public void testDelete() throws Exception {
         testSave();
         listAll = dao.getAll();
         int size = listAll.size();
         Assert.assertEquals(2, size, DB_MUST_BE_NOT_EMPTY);
 
         for (User user : listAll) {
             dao.delete(user);
         }
         testDBEmpty();
     }
 
     @Test
     public void testDeleteById() throws Exception {
         testSave();
         listAll = dao.getAll();
         int size = listAll.size();
         Assert.assertEquals(2, size, DB_MUST_BE_NOT_EMPTY);
 
         for (Persistent p : listAll) {
             dao.delete(p.getId());
         }
         testDBEmpty();
     }
 
     @Test
     public void testGetById() throws Exception {
         testSave();
         listAll = dao.getAll();
         int size = listAll.size();
         Assert.assertEquals(2, size, DB_MUST_BE_NOT_EMPTY);
 
         for (Persistent p : listAll) {
             dao.delete(dao.get(p.getId()));
         }
         testDBEmpty();
     }
 
     @Test
     public void testGetAll() throws Exception {
         dao.saveOrUpdate(entity);
 
         int size = dao.getAll().size();
         Assert.assertEquals(1, size, ENTITIES_IS_NOT_INCREASED_BY_1);
     }
 
     @Test
     public void testUpdate() throws Exception {
         dao.saveOrUpdate(entity);
         dao.saveOrUpdate(entity);
         dao.saveOrUpdate(entity);
 
         int size = dao.getAll().size();
         Assert.assertEquals(1, size, ENTITIES_IS_NOT_INCREASED_BY_1);
     }
 
     @Test
     public void testGetByUsername() throws Exception {
         dao.saveOrUpdate(entity);
 
         User user = dao.getByUsername(USERNAME);
         Assert.assertEquals(USERNAME, user.getUsername(), "Username not match");
     }
 
     @Test
     public void testGetByUsernameNotExist() throws Exception {
         dao.saveOrUpdate(entity);
 
         User user = dao.getByUsername("Name");
         Assert.assertNull(user);
     }
 
     @Test
     public void testIsUserWithEmailExist() throws Exception {
         dao.saveOrUpdate(entity);
         User entity2 = new User();
         entity2.setEmail("email@dddd.co.uk");
         dao.saveOrUpdate(entity2);
 
         boolean result = dao.isUserWithEmailExist("mail@mail.com");
 
         Assert.assertTrue(result, "User not exist");
     }
 
     @Test
    public void testIsUserWithEmailNotExist() throws Exception {
         dao.saveOrUpdate(entity);
         User entity2 = new User();
         entity2.setEmail("email@dddd.co.uk");
         dao.saveOrUpdate(entity2);
 
         boolean result = dao.isUserWithEmailExist("dick@head.com");
 
         Assert.assertFalse(result, "User exist");
     }
 
     @Test
     public void testIsUserWithUsernameExist() throws Exception {
         dao.saveOrUpdate(entity);
         User entity2 = new User();
         entity2.setUsername("namename");
         dao.saveOrUpdate(entity2);
 
         boolean result = dao.isUserWithUsernameExist(USERNAME);
 
         Assert.assertTrue(result, "User not exist");
     }
 
     @Test
    public void testIsUserWithUsernameNotExist() throws Exception {
         dao.saveOrUpdate(entity);
         User entity2 = new User();
         entity2.setUsername("namename");
         dao.saveOrUpdate(entity2);
 
         boolean result = dao.isUserWithUsernameExist("Nonono");
 
         Assert.assertFalse(result, "User exist");
     }
 }
