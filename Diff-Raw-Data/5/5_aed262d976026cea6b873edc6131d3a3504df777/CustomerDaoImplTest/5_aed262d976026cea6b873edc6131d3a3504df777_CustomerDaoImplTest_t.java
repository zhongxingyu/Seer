 package cz.muni.fi.pa165.library;
 
 import cz.muni.fi.pa165.library.daos.CustomerDao;
 import cz.muni.fi.pa165.library.daos.CustomerDaoImpl;
 import cz.muni.fi.pa165.library.entities.Customer;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNotSame;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 import junit.framework.TestCase;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 /**
  *
  * @author vit.mica
  */
 public class CustomerDaoImplTest extends TestCase {
   /* private CustomerDao DAO;
     private EntityManagerFactory emf;
     private EntityManager em;
     
     @Before
     @Override
     public void setUp(){
         em = Persistence.createEntityManagerFactory("LibraryTestPU").createEntityManager();    
         DAO = new CustomerDaoImpl(em);
     }
 
     @After
     public void close() {
         em.close();
     }
     
     @Test
     public void testCreateCustomer(){
         Customer customer = newCustomer("Petr","Praha");
         em.getTransaction().begin();
         DAO.createCustomer(customer);
         em.getTransaction().commit();
         
         assertNotNull(customer.getId());
         assertNotNull(customer.getName());
         assertNotNull(customer.getAddress());
         
         Customer sameCustomer = DAO.findCustomerById(customer.getId());
         
         assertEquals(customer, sameCustomer);
     } 
     
     @Test
     public void testCreateCustomerWithNulls(){
         Customer customer = newCustomer("Petr",null);
         
         try {
             DAO.createCustomer(null);
             fail("Ex not thrown");
         } catch (IllegalArgumentException ex) {
             
         }
         
         try {
             DAO.createCustomer(customer);
             fail("Ex not thrown");
         } catch (IllegalArgumentException ex) {
             
         }
         
         customer = newCustomer("Petr","Praha");
         em.getTransaction().begin();
         DAO.createCustomer(customer);
         em.getTransaction().commit();
         
         assertNotNull(customer.getId());
         assertNotNull(customer.getName());
         assertNotNull(customer.getAddress());
     } 
     
     @Test
     public void testGetCustomer(){
         Customer customer1 = newCustomer("Petr","Praha");
         Customer customer2 = newCustomer("Jan","Brno");
         em.getTransaction().begin();
         DAO.createCustomer(customer1);
         DAO.createCustomer(customer2);
         em.getTransaction().commit();
         
         assertEquals(DAO.findCustomerById(customer1.getId()),customer1);
         assertEquals(DAO.findCustomerById(customer2.getId()),customer2);
     }
     
     @Test 
     public void testGetCustomerWithNullId() {
         try {
             DAO.findCustomerById(null);
             fail("Ex not thrown");
         } catch (IllegalArgumentException ex) {
             
         }
     }
     
     @Test 
     public void testDelete() {
         Customer customer = newCustomer("Petr","Praha");
         
         em.getTransaction().begin();
         DAO.createCustomer(customer);
         em.getTransaction().commit();
         
         assertNotNull(DAO.findCustomerById(customer.getId()));
         
         em.getTransaction().begin();
         DAO.deleteCustomer(customer);
         em.getTransaction().commit();
         
         try {
             DAO.deleteCustomer(customer);
             fail("Ex not thrown");
         } catch (IllegalArgumentException ex) {
             
         }
     }
     
     @Test 
     public void testDeleteWithNullId() {
         Customer customer = newCustomer("Petr","Praha");
         try {
             DAO.deleteCustomer(null);
             fail("Ex not thrown");
         } catch (IllegalArgumentException ex) {
             
         }
     }
     
     @Test 
     public void testUpdate() {
         Customer customer = new Customer();
         customer.setName("Petr");
         customer.setAddress("Prague");
         
         em.getTransaction().begin();
         DAO.createCustomer(customer);
         em.getTransaction().commit();
         
         Customer customer2 = DAO.findCustomerById(customer.getId());
         customer2.setName("Jan");
         em.getTransaction().begin();
         DAO.updateCustomer(customer2);
         em.getTransaction().commit();
         
         Customer customer3 = DAO.findCustomerById(customer.getId());
         
         assertEquals(customer2, customer3);
     
     }
     
     
     
     @Test
     public void testGetAllCustomers(){
         assertTrue(DAO.findAllCustomers().isEmpty());
         
         Customer customer1 = newCustomer("Petr","Praha");
         Customer customer2 = newCustomer("Jan","Brno");
         
         em.getTransaction().begin();
         DAO.createCustomer(customer1);
         DAO.createCustomer(customer2);
         em.getTransaction().commit();
         
         List<Customer> actual = DAO.findAllCustomers();
 
         assertFalse(DAO.findAllCustomers().isEmpty());
         assertEquals(DAO.findAllCustomers().size(),2);
                 
     } 
     
         private static Customer newCustomer(String name, String address) {
         Customer customer = new Customer();
         customer.setName(name);
         customer.setAddress(address);
         return customer;
    }*/
 }
