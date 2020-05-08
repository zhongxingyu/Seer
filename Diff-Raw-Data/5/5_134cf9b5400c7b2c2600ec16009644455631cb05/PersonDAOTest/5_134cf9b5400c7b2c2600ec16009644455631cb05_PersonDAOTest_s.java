 package net.tirasa.test.addressbook.tests;
 
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import net.tirasa.test.addressbook.dao.PersonDAO;
 import net.tirasa.test.addressbook.data.Person;
 import net.tirasa.test.addressbook.exceptions.DatabaseException;
 import org.junit.Assert;
 import java.util.Iterator;
 import java.util.List;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 

 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:testEnv.xml"})
 @Transactional
 public class PersonDAOTest {
 
     @Autowired
     private PersonDAO personDAO;
 
     private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(PersonDAOTest.class);
 
     @Before
     public void populateDb() throws DatabaseException {
         personDAO.save(new Person("Pippo", "pippo@p.it", "2222222"));
         personDAO.save(new Person("Pluto", "pluto@p.it", "1111111"));
         personDAO.save(new Person("Foo", "foo@p.it", "2222222"));
     }
 
     @Test
     public void testSave() throws DatabaseException {
         LOG.info("TESTING SAVE OPERATION...");
         Assert.assertNotNull(personDAO.save(new Person("Bar", "bar@b.it", "12345789")));
         List<Person> list = personDAO.list();
         Assert.assertEquals(4, list.size());
     }
 
     @Test
     public void testList() throws DatabaseException {
         LOG.info("TESTING LIST OPERATION...");
         List<Person> list = personDAO.list();
         Assert.assertNotNull(list);
         Assert.assertEquals(3, list.size());
     }
 
     @Test
     public void testDelete() throws DatabaseException {
         Assert.assertFalse(personDAO.list().isEmpty());
         for (Iterator<Person> it = personDAO.list().iterator(); it.hasNext();) {
             Person temp = it.next();
             personDAO.delete(temp.getId());
         }
         Assert.assertTrue(personDAO.list().isEmpty());
     }
 
     @Test
     public void testFind() throws DatabaseException {
         int i = 0;
         String names[] = {"Pippo", "Pluto", "Foo"};
         for (Iterator<Person> it = personDAO.list().iterator(); it.hasNext();) {
             Assert.assertEquals(personDAO.find(it.next().getId()).getName(), names[i]);
             i++;
         }
     }
 }
