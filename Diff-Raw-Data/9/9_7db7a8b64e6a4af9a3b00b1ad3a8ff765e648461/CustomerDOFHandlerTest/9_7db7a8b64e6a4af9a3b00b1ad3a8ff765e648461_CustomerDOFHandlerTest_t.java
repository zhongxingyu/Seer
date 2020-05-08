 package org.doframework.sample.jdbc_app.test.dof.test;
 
import java.io.InputStream;

 import junit.framework.TestCase;
 
 import org.doframework.sample.jdbc_app.entity.Customer;
 import org.doframework.sample.jdbc_app.test.dof.CustomerDOFHandler;
 
 /**
  * User: gordonju Date: Jan 13, 2008 Time: 4:17:22 PM
  */
 public class CustomerDOFHandlerTest extends TestCase
 {
 
     /**
      * This will test that we have our files set up so they are accessible. In this example, we are
      * using the method InputStream is = ClassLoader.getSystemResourceAsStream(xmlDescriptionFile);
      * Thus, we need to add the test_data directory to the classpath. In IntelliJ, this is done by
      * adding the directory as a "project library"
      */
     public void testParsing()
     {
         String testFile = "customer.25.xml";
        InputStream is = ClassLoader.getSystemResourceAsStream(testFile);
        
         CustomerDOFHandler mxf = new CustomerDOFHandler();
        Customer customer = mxf.createCustomer(is);
        
         assertEquals(25, customer.getId());
         assertEquals("John Smith", customer.getName());
     }
 
 
 }
