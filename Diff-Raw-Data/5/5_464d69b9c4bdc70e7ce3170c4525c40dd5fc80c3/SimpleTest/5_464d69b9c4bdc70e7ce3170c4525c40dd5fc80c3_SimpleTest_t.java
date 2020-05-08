 package com.datastax.hectorjpa.store;
 
 import static org.junit.Assert.*;
 
 import javax.persistence.EntityManager;
 
 import org.junit.Test;
 
 import com.datastax.hectorjpa.ManagedEntityTestBase;
 import com.datastax.hectorjpa.bean.Customer;
 import com.datastax.hectorjpa.bean.Phone;
 import com.datastax.hectorjpa.bean.PrimitiveTypes;
 import com.datastax.hectorjpa.bean.SimpleTestBean;
 import com.datastax.hectorjpa.bean.Store;
 import com.datastax.hectorjpa.bean.Phone.PhoneType;
 import com.datastax.hectorjpa.bean.PrimitiveTypes.TestEnum;
 import com.datastax.hectorjpa.bean.tree.Role;
 import com.datastax.hectorjpa.bean.tree.Techie;
 
 public class SimpleTest extends ManagedEntityTestBase {
   
   @Test
   public void testBuildEntityManagerFactory() {
     
     EntityManager em = entityManagerFactory.createEntityManager();
     em.getTransaction().begin();
     em.persist(new SimpleTestBean(1, "foo"));
     em.getTransaction().commit();
     em.close();
     
     //em.getTransaction().begin();
     
     EntityManager em2 = entityManagerFactory.createEntityManager();
     SimpleTestBean stb = em2.find(SimpleTestBean.class, 1);
     //em.getTransaction().commit();
     assertEquals("foo",stb.getName());
     
 
     em2.getTransaction().begin();
     em2.remove(stb);
     em2.getTransaction().commit();
     em2.close();
   }  
   
   @Test
   public void testPrimitives() {
     PrimitiveTypes saved = new PrimitiveTypes();
     saved.setBoolValue(true);
     saved.setCharValue('\uffff');
     saved.setShortVal((short) 256);
     saved.setIntVal(10000);
     saved.setDoubleVal(.00023);
     saved.setLongVal(200000000l);
     saved.setFloatVal(22.0029292f);
     saved.setTestEnum(TestEnum.TWO);
         
     
     EntityManager em = entityManagerFactory.createEntityManager();
     em.getTransaction().begin();
     em.persist(saved);
     em.getTransaction().commit();
     em.close();
     
     //em.getTransaction().begin();
     
     EntityManager em2 = entityManagerFactory.createEntityManager();
     
     PrimitiveTypes returned = em2.find(PrimitiveTypes.class, saved.getId());
    
     //em.getTransaction().commit();
     assertEquals(saved,returned);
     assertEquals(saved.isBoolValue(), returned.isBoolValue());
     assertEquals(saved.getCharValue(), returned.getCharValue());
     assertEquals(saved.getShortVal(), returned.getShortVal());
     assertEquals(saved.getIntVal(), returned.getIntVal());
     assertEquals(saved.getDoubleVal(), returned.getDoubleVal(), 0);
     assertEquals(saved.getLongVal(), returned.getLongVal());
     assertEquals(saved.getFloatVal(), returned.getFloatVal(), 0);
     assertEquals(saved.getTestEnum(), returned.getTestEnum());
     
 
     em2.close();
   }  
 
   
   /**
    * Test that after saving an entity with embedded object flush is correctly called if only
    * the embedded entity is updated.  Causing bugs in non transactional read then a transactional write.
    */
   @Test
   public void embeddedFieldOnlyDirty() {
 
     EntityManager em = entityManagerFactory.createEntityManager();
     em.getTransaction().begin();
 
     Customer james = new Customer();
     james.setEmail("james@test.com");
     james.setName("James");
     james.setPhoneNumber(new Phone("+641112223333", PhoneType.MOBILE));
 
   
     em.persist(james);
     em.getTransaction().commit();
     em.close();
 
     EntityManager em2 = entityManagerFactory.createEntityManager();
   
     em2.getTransaction().begin();
     
     Customer returned = em2.find(Customer.class, james.getId());
  
    em2.getTransaction().commit();
     /**
      * Make sure the stores are equal and everything is in sorted order
      */
     assertEquals(james, returned);
 
     
     //test embedded objects
     assertEquals(james.getPhoneNumber(), returned.getPhoneNumber());
     assertEquals(james.getPhoneNumber().getPhoneNumber(), returned.getPhoneNumber().getPhoneNumber());
     assertEquals(james.getPhoneNumber().getType(), returned.getPhoneNumber().getType());
     

     
     //now update the values
     returned.getPhoneNumber().setPhoneNumber("+6411122255555");
     
     //now start a transaction and flush, this value should be retained and persisted
     em2.getTransaction().begin();
     
     em2.persist(returned);
     
     em2.getTransaction().commit();
     
     em2.close();
     
     
     EntityManager em3 = entityManagerFactory.createEntityManager();
 
     Customer returned2 = em3.find(Customer.class, james.getId());
     
     assertEquals(returned, returned2);
     
     assertEquals(returned.getPhoneNumber(), returned2.getPhoneNumber());
     assertEquals(returned.getPhoneNumber().getPhoneNumber(), returned2.getPhoneNumber().getPhoneNumber());
     assertEquals(returned.getPhoneNumber().getType(), returned2.getPhoneNumber().getType());
     
     
     
 
   }
   
 
   /**
    * Test simple instance with no collections to ensure we persist properly
    * without indexing
    */
   @Test
   public void basicEmbeddedCollectionPersistence() {
 
     EntityManager em = entityManagerFactory.createEntityManager();
     em.getTransaction().begin();
 
     Store store = new Store();
     store.setName("Manhattan");
 
     Customer james = new Customer();
     james.setEmail("james@test.com");
     james.setName("James");
     james.setPhoneNumber(new Phone("+641112223333", PhoneType.MOBILE));
    
     Phone other1 = new Phone("+641112223334", PhoneType.HOME);
     Phone other2 = new Phone("+641112223335", PhoneType.HOME);
     
     james.addOtherPhone(other1);
     james.addOtherPhone(other2);
 
     store.addCustomer(james);
     
     em.persist(store);
     em.getTransaction().commit();
     em.close();
 
     EntityManager em2 = entityManagerFactory.createEntityManager();
 
     
     Store returnedStore = em2.find(Store.class, store.getId());
 
     /**
      * Make sure the stores are equal and everything is in sorted order
      */
     assertEquals(store, returnedStore);
 
     assertEquals(james, returnedStore.getCustomers().get(0));
     
     Customer returnedCust = returnedStore.getCustomers().get(0);
     
     //test embedded objects
     assertEquals(james.getPhoneNumber(), returnedCust.getPhoneNumber());
     
     assertEquals(other1, returnedCust.getOtherPhones().get(0));
     
     assertEquals(other1.getPhoneNumber(), returnedCust.getOtherPhones().get(0).getPhoneNumber());
     assertEquals(other1.getType(), returnedCust.getOtherPhones().get(0).getType());
     
     assertEquals(other2, returnedCust.getOtherPhones().get(1));
     
     assertEquals(other2.getPhoneNumber(), returnedCust.getOtherPhones().get(1).getPhoneNumber());
     assertEquals(other2.getType(), returnedCust.getOtherPhones().get(1).getType());
 
 
   }
   
   /**
    * Tests an enumeration collection is saved properly when in a collection
    */
   @Test
   public void enumCollection(){
     Techie tech = new Techie();
     
     tech.addRole(Role.ADMIN);
     tech.addRole(Role.POWERUSER);
     
     EntityManager em = entityManagerFactory.createEntityManager();
     em.getTransaction().begin();
 
     em.persist(tech);
     
     em.getTransaction().commit();
     em.close();
     
     
     EntityManager em2 = entityManagerFactory.createEntityManager();
     
     Techie returned = em2.find(Techie.class, tech.getId());
     
     assertEquals(tech, returned);
     
     assertTrue(returned.getRoles().contains(Role.ADMIN));
     assertTrue(returned.getRoles().contains(Role.POWERUSER));
     
     
     
   }
 
 }
