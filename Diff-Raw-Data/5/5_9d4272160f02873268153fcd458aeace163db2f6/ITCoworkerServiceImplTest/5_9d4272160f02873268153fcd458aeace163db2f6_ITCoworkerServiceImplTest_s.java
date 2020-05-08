 package org.xezz.timeregistration.service.impl;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.AnnotationConfigContextLoader;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
 import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
 import org.xezz.timeregistration.config.AppTestConfig;
 import org.xezz.timeregistration.config.DataTestConfig;
 import org.xezz.timeregistration.dao.CoworkerDAO;
 import org.xezz.timeregistration.services.CoworkerService;
 import org.xezz.timeregistration.services.impl.CoworkerServiceImpl;
 
 import javax.annotation.Resource;
 import javax.sql.DataSource;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.hamcrest.Matchers.*;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.junit.Assert.*;
 
 /**
  * User: bkoch
  * Date: 08.06.13
  * Time: 13:59
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {AppTestConfig.class, DataTestConfig.class, CoworkerServiceImpl.class})
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
         DirtiesContextTestExecutionListener.class,
         TransactionalTestExecutionListener.class,
         DbUnitTestExecutionListener.class})
 @ActiveProfiles("integration-test")
 @Configurable
 @DatabaseSetup("/META-INF/basicdata.xml")
 public class ITCoworkerServiceImplTest {
 
     // Default coworkerId that we know
     private final Long coworkerId = 1L;
     // Default coworkerId we know and we want to delete
     private final Long idOfCoworkerToDelete = 401L;
     // First name of the default Coworker
     private final String firstName = "Bastian";
     // Last name of the default Coworker
     private final String lastName = "Koch";
     // Required by DBUnit
     @Resource
     DataSource dataSource;
     @Autowired
     private CoworkerService coworkerService;
 
     @Before
     public void setUp() throws Exception {
 
     }
 
     @After
     public void tearDown() throws Exception {
 
     }
 
     @Test
     public void testCoworkersAll() throws Exception {
         assertNotNull("Service has not been injected", coworkerService);
         assertNotNull("service.getAll returned null", coworkerService.coworkersAll());
 
     }
 
     @Test
     public void testCoworkersByFirstName() throws Exception {
         final Iterable<CoworkerDAO> coworkerDAOs = coworkerService.coworkersByFirstName(firstName);
         assertNotNull("Received Iterable was null", coworkerDAOs);
         final Collection<CoworkerDAO> daoCollection = new ArrayList<CoworkerDAO>();
         for (CoworkerDAO c : coworkerDAOs) {
             daoCollection.add(c);
         }
         assertThat("Result does not have any entity", daoCollection.size(), is(greaterThan(0)));
         for (CoworkerDAO c : coworkerDAOs) {
             assertThat("First name was not " + firstName, firstName, is(c.getFirstName()));
         }
     }
 
     @Test
     public void testCoworkersByLastName() throws Exception {
         final Iterable<CoworkerDAO> daos = coworkerService.coworkersByLastName(lastName);
         assertNotNull("Received Iterable was null", daos);
         final Collection<CoworkerDAO> daoCollection = new ArrayList<CoworkerDAO>();
         for (CoworkerDAO c : daos) {
             daoCollection.add(c);
         }
         assertThat("Result does not have any entity", daoCollection.size(), is(greaterThan(0)));
         for (CoworkerDAO c : daos) {
             assertThat("Last name was not " + lastName, lastName, is(c.getLastName()));
         }
     }
 
     @Test
     public void testCoworkersByFirstAndLastName() throws Exception {
         final Iterable<CoworkerDAO> daos = coworkerService.coworkersByFirstAndLastName(firstName, lastName);
         assertNotNull("Received Iterable was null", daos);
         final Collection<CoworkerDAO> daoCollection = new ArrayList<CoworkerDAO>();
         for (CoworkerDAO c : daos) {
             daoCollection.add(c);
         }
         assertThat("Result does not have any entity", daoCollection.size(), is(greaterThan(0)));
         for (CoworkerDAO c : daos) {
             assertThat("First name was not " + firstName, firstName, is(c.getFirstName()));
             assertThat("Last name was not " + lastName, lastName, is(c.getLastName()));
         }
     }
 
     @Test
     public void testCoworkersByProject() throws Exception {
         // Todo: Test ProjectService first
        fail("Underlying service not tested yet, thus this test is not implemented");
     }
 
     @Test
     public void testCoworkerById() throws Exception {
         final CoworkerDAO coworker = coworkerService.coworkerById(coworkerId);
         assertNotNull("Received coworker was null", coworker);
         assertThat("ID did not match", coworkerId, is(coworker.getCoworkerId()));
     }
 
     @Test
     public void testCoworkerByTimeFrame() throws Exception {
         // ToDo: Test TimeFrameService first
        fail("Underlying service not tested yet, thus this test is not implemented");
     }
 
     @Test
     public void testAddNewCoworker() throws Exception {
         CoworkerDAO coworker = new CoworkerDAO();
         final String testFirstName = "Test first name";
         coworker.setFirstName(testFirstName);
         final String testLastName = "Test last name";
         coworker.setLastName(testLastName);
         final CoworkerDAO addedDao = coworkerService.addNewCoworker(coworker);
 
         assertThat("Expected first name to match", coworker.getFirstName(), is(addedDao.getFirstName()));
         assertThat("Expected last name to match", coworker.getLastName(), is(addedDao.getLastName()));
         assertThat("Expected a date which is not null", addedDao.getCreationDate(), is(not(nullValue())));
         assertThat("Expected a date which is not null", addedDao.getLastUpdatedDate(), is(not(nullValue())));
         assertThat("Expected creation and updated Date to be the same", addedDao.getCreationDate(), is(addedDao.getLastUpdatedDate()));
         assertThat("Expected an Id which is not null", addedDao.getCoworkerId(), is(notNullValue()));
     }
 
     @Test
     public void testUpdateCoworker() throws Exception {
         final CoworkerDAO dao = coworkerService.coworkerById(coworkerId);
         final Date lastUpdatedDate = dao.getLastUpdatedDate();
         final String updatedFirstname = "Updated name";
         dao.setFirstName(updatedFirstname);
         final CoworkerDAO updatedDao = coworkerService.updateCoworker(dao);
         assertNotNull("Expected coworker not being null after update", updatedDao);
         assertThat("Expected first name to be updated", updatedFirstname, is(updatedDao.getFirstName()));
         // TODO: Move this to its own test
         final CoworkerDAO queriedDao = coworkerService.coworkerById(coworkerId);
         assertThat("Expected last updated date to have changed and be later than the old one", lastUpdatedDate, is(lessThan(queriedDao.getLastUpdatedDate())));
     }
 
     @Test
     public void testDeleteCoworker() throws Exception {
         CoworkerDAO toDelete = coworkerService.coworkerById(idOfCoworkerToDelete);
         coworkerService.deleteCoworker(toDelete);
         assertNull("Expected to not receive any coworker", coworkerService.coworkerById(idOfCoworkerToDelete));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testUpdateCoworkerFailsWithUpdatedCreationDate() throws Exception {
         final CoworkerDAO coworkerDAO = coworkerService.coworkerById(coworkerId);
         assertNotNull("Received Coworker was null", coworkerDAO);
         coworkerDAO.setCreationDate(new Date(coworkerDAO.getCreationDate().getTime() + 12344));
         coworkerService.updateCoworker(coworkerDAO);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testUpdateCoworkerFailsWitChangedId() throws Exception {
         final CoworkerDAO coworkerDAO = coworkerService.coworkerById(coworkerId);
         assertNotNull("Received Coworker was null", coworkerDAO);
         coworkerDAO.setCoworkerId(coworkerDAO.getCoworkerId() + 2L);
         coworkerService.updateCoworker(coworkerDAO);
     }
 }
