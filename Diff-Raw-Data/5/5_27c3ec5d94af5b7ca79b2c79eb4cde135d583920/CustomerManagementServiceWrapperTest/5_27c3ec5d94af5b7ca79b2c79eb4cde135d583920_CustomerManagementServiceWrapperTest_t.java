 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package at.ac.tuwien.infosys.aic.it;
 
 import java.util.List;
 import at.ac.tuwien.infosys.aic.model.Address;
 import org.junit.Before;
 import at.ac.tuwien.infosys.aic.model.Customer;
 import at.ac.tuwien.infosys.aic.soap.CustomerManagementServiceWrapper;
 import at.ac.tuwien.infosys.aic.store.DataStore;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import javax.xml.ws.soap.SOAPFaultException;
 import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 import static at.ac.tuwien.infosys.aic.Constants.*;
 
 public class CustomerManagementServiceWrapperTest extends BaseIntegrationTest {
 
     private DataStore ds = DataStore.getInstance();
     Logger log = Logger.getLogger("CustomerManagerServiceWrapperTes");
 
     @Before
     public void init() {
         ds.init();
     }
 
     @Test
     public void testGetExsistingCustomer() throws Exception {
 
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(CustomerManagementServiceWrapper.class);
         factory.setAddress(CUSTOMERMANAGEMENTWRAPPER);
         CustomerManagementServiceWrapper customerManager = (CustomerManagementServiceWrapper) factory.create();
 
         Customer c1 = customerManager.get(CUSTOMER1);
         assertNotNull(c1);
         assertThat(c1, is(ds.getCustomer(CUSTOMER1)));
 
     }
 
     @Test //(expected=SOAPFaultException.class)
     public void testGetNonExistingCustomer() throws Exception {
 
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(CustomerManagementServiceWrapper.class);
         factory.setAddress(CUSTOMERMANAGEMENTWRAPPER);
         CustomerManagementServiceWrapper customerManager = (CustomerManagementServiceWrapper) factory.create();
 
         try {
             Customer c1 = customerManager.get("WRONGID");
         } catch (SOAPFaultException e) {
             log.info("caught UnknownCustomerFault");
             assertThat(e.getMessage(), is("unknown costumer fault"));
         }
     }
 
     @Test
     public void testDeleteNonExistingCustomer() throws Exception {
 
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(CustomerManagementServiceWrapper.class);
         factory.setAddress(CUSTOMERMANAGEMENTWRAPPER);
         CustomerManagementServiceWrapper customerManager = (CustomerManagementServiceWrapper) factory.create();
 
         try {
             customerManager.delete("WrongCustomer");
         } catch (SOAPFaultException e) {
             log.info("caught UnknownCustomerFault");
             assertThat(e.getMessage(), is("unknown costumer fault"));
         }
 
     }
 
     @Test
     public void testDeleteExistingCustomer() throws Exception {
 
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(CustomerManagementServiceWrapper.class);
         factory.setAddress(CUSTOMERMANAGEMENTWRAPPER);
         CustomerManagementServiceWrapper customerManager = (CustomerManagementServiceWrapper) factory.create();
 
         customerManager.delete(CUSTOMER1);
 
         try {
             Customer c1 = ds.getCustomer(CUSTOMER1);
         } catch (SOAPFaultException e) {
             log.info("caught UnknownCustomerFault");
             assertThat(e.getMessage(), is("unknown costumer fault"));
             
         }
     }
 
     @Test
     public void testPutNonExistingCustomer() throws Exception {
 
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(CustomerManagementServiceWrapper.class);
         factory.setAddress(CUSTOMERMANAGEMENTWRAPPER);
         CustomerManagementServiceWrapper customerManager = (CustomerManagementServiceWrapper) factory.create();
 
         Customer c = new Customer();
         c.setId("newCustomer");
         c.setName("New Customer");
         c.setOpenBalance(BigDecimal.ZERO);
         List<Address> addresses = new ArrayList<Address>();
         c.setAdresses(addresses);
 
         customerManager.put(c);
        assertThat(customerManager.get("newCustomer"), is(c));
     }
 
     @Test
     public void testPutExistingCustomer() throws Exception {
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(CustomerManagementServiceWrapper.class);
         factory.setAddress(CUSTOMERMANAGEMENTWRAPPER);
         CustomerManagementServiceWrapper customerManager = (CustomerManagementServiceWrapper) factory.create();
 
         Customer c = ds.getCustomer(CUSTOMER1);
         c.setName("New Name");
         customerManager.put(c);
         assertThat(ds.getCustomer(CUSTOMER1).getName(), is("New Name"));
     }
 
     @Test
     public void testPostNonExistingCustomer() throws Exception {
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(CustomerManagementServiceWrapper.class);
         factory.setAddress(CUSTOMERMANAGEMENTWRAPPER);
         CustomerManagementServiceWrapper customerManager = (CustomerManagementServiceWrapper) factory.create();
 
         Customer c = new Customer();
         c.setId("newCustomer");
         c.setName("New Customer");
         c.setOpenBalance(BigDecimal.ZERO);
         List<Address> addresses = new ArrayList<Address>();
         c.setAdresses(addresses);
 
         try {
             customerManager.post(c);
             fail("exception expected");
         } catch (SOAPFaultException e) {
             log.info("caught UnknownCustomerFault");
             assertThat(e.getMessage(), is("unknown costumer fault"));
         }
 
     }
 
     @Test
     public void testPostExistingCustomer() throws Exception {
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(CustomerManagementServiceWrapper.class);
         factory.setAddress(CUSTOMERMANAGEMENTWRAPPER);
         CustomerManagementServiceWrapper customerManager = (CustomerManagementServiceWrapper) factory.create();
 
         Customer c = ds.getCustomer(CUSTOMER1);
         c.setName("New Name");
         customerManager.post(c);
         assertThat(ds.getCustomer(CUSTOMER1).getName(), is("New Name"));
 
     }
 
     @Test
     public void testUpdateAccountExistingCustomer() throws Exception {
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(CustomerManagementServiceWrapper.class);
         factory.setAddress(CUSTOMERMANAGEMENTWRAPPER);
         CustomerManagementServiceWrapper customerManager = (CustomerManagementServiceWrapper) factory.create();
         assertNotNull(ds.getCustomer(CUSTOMER1).getOpenBalance());
         customerManager.update_account(CUSTOMER1, new BigDecimal(8.0));
        assertThat(customerManager.get(CUSTOMER1).getOpenBalance(), is(new BigDecimal(18.0)));
 
     }
 
     @Test
     public void testNotifyExistingCustomer() throws Exception {
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(CustomerManagementServiceWrapper.class);
         factory.setAddress(CUSTOMERMANAGEMENTWRAPPER);
         CustomerManagementServiceWrapper customerManager = (CustomerManagementServiceWrapper) factory.create();
 
         customerManager.notify(CUSTOMER1, "Good Morning Vienna");
         assertNotNull(ds.getMessages(CUSTOMER1).getLast());
         log.info("Customer1 got new Message: " + ds.getMessages(CUSTOMER1).getLast());
         assertThat(ds.getMessages(CUSTOMER1).getLast(), is("Good Morning Vienna"));
      
     }
 
     @Test
     public void testNotifyNonExistingCustomer() throws Exception {
         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
         factory.setServiceClass(CustomerManagementServiceWrapper.class);
         factory.setAddress(CUSTOMERMANAGEMENTWRAPPER);
         CustomerManagementServiceWrapper customerManager = (CustomerManagementServiceWrapper) factory.create();
 
         try {
               customerManager.notify("NoCustomer", "Good Morning Vienna");
         } catch (SOAPFaultException e) {
             log.info("caught UnknownCustomerFault");
             assertThat(e.getMessage(), is("unknown costumer fault"));
         }
     }
 }
