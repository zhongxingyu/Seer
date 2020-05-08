 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.socraticgrid.mdwslib;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import junit.framework.TestCase;
 import static junit.framework.TestCase.assertEquals;
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 //import org.springframework.core.
 
 @RunWith(SpringJUnit4ClassRunner.class)
 // ApplicationContext will be loaded from "/applicationContext.xml" and "/applicationContext-test.xml"
 // in the root of the classpath
 @ContextConfiguration(locations =
 {
     "classpath:TestSpringXMLConfig.xml"
 })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
 /**
  *
  * @author Jerry Goodnough
  */
 public class GetNhinDataSourceTest extends TestCase
 {
 
     @Autowired
     private ApplicationContext ctx;
 
     public GetNhinDataSourceTest()
     {
     }
 
     @Override
     protected void setUp() throws Exception
     {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception
     {
         super.tearDown();
     }
 
     /**
      * Test of getMaximumConnectionLifeTime method, of class GetNhinDataSource.
      * In Specific the default lifetime
      */
     @Test
     public void testGetMaximumConnectionLifeTime()
     {
         System.out.println("getMaximumConnectionLifeTime");
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         long expResult = 300000L;
         long result = instance.getMaximumConnectionLifeTime();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setMaximumConnectionLifeTime method, of class GetNhinDataSource.
      */
     @Test
     public void testSetMaximumConnectionLifeTime()
     {
         System.out.println("setMaximumConnectionLifeTime");
         long maximumConnectionLifeTime = 1000 * 60 * 2L;
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         instance.setMaximumConnectionLifeTime(maximumConnectionLifeTime);
         long result = instance.getMaximumConnectionLifeTime();
         assertEquals(maximumConnectionLifeTime, result);
 
     }
 
     /**
      * Test of getDomainRemap method, of class GetNhinDataSource.
      */
     @Test
     public void testGetDomainRemap()
     {
         System.out.println("getDomainRemap");
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         Map result = instance.getDomainRemap();
         assertTrue(result.size() > 2);
     }
 
     /**
      * Test of setDomainRemap method, of class GetNhinDataSource.
      */
     @Test
     public void testSetDomainRemap()
     {
         System.out.println("setDomainRemap");
         Map<String, String> domainRemap = new HashMap<String, String>();
         domainRemap.put("Rx", "meds");
         domainRemap.put("meds", "meds");
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
 
         instance.setDomainRemap(domainRemap);
     }
 
     /**
      * Test of isUseMappedDomains method, of class GetNhinDataSource.
      */
     @Test
     public void testIsUseMappedDomains()
     {
         System.out.println("isUseMappedDomains");
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         boolean expResult = true;
         boolean result = instance.isUseMappedDomains();
         assertEquals(expResult, result);
 
     }
 
     /**
      * Test of setUseMappedDomainsOnly method, of class GetNhinDataSource.
      */
     @Test
     public void testSetUseMappedDomainsOnly()
     {
         System.out.println("setUseMappedDomainsOnly");
         boolean useMappedDomainsOnly = true;
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         instance.setUseMappedDomainsOnly(useMappedDomainsOnly);
         boolean expResult = true;
         boolean result = instance.isUseMappedDomains();
         assertEquals(expResult, result);
 
     }
 
     /**
      * Test of getPassword method, of class GetNhinDataSource.
      */
     @Test
     public void testGetPassword()
     {
         System.out.println("getPassword");
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         String expResult = "programmer1";
         String result = instance.getPassword();
         assertEquals(expResult, result);
 
     }
 
     /**
      * Test of setPassword method, of class GetNhinDataSource.
      */
     @Test
     public void testSetPassword()
     {
         System.out.println("setPassword");
         String password = "programmer1";
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         instance.setPassword(password);
     }
 
     /**
      * Test of getUserId method, of class GetNhinDataSource.
      */
     @Test
     public void testGetUserId()
     {
         System.out.println("getUserId");
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         String expResult = "1programmer";
         String result = instance.getUserId();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setUserId method, of class GetNhinDataSource.
      */
     @Test
     public void testSetUserId()
     {
         System.out.println("setUserId");
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         String userId = "1programmer";
         instance.setUserId(userId);
     }
 
     /**
      * Test of getMdwsEndpoint method, of class GetNhinDataSource.
      */
     @Test
     public void testGetMdwsEndpoint()
     {
         System.out.println("getMdwsEndpoint");
         String expResult = "http://172.31.5.104/mdws2/EmrSvc.asmx";
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         String result = instance.getMdwsEndpoint();
         assertEquals(expResult, result);
    
     }
 
     /**
      * Test of setMdwsEndpoint method, of class GetNhinDataSource.
      */
     @Test
     public void testSetMdwsEndpoint()
     {
         System.out.println("setMdwsEndpoint");
         String mdwsEndpoint = "http://172.31.5.104/mdws2/EmrSvc.asmx";
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         instance.setMdwsEndpoint(mdwsEndpoint);
     }
 
     /**
      * Test of getSiteId method, of class GetNhinDataSource.
      */
     @Test
     public void testGetSiteId()
     {
         System.out.println("getSiteId");
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         String expResult = "102";
         String result = instance.getSiteId();
         assertEquals(expResult, result);
 
     }
 
     /**
      * Test of setSiteId method, of class GetNhinDataSource.
      */
     @Test
     public void testSetSiteId()
     {
         System.out.println("setSiteId");
         String siteId = "102";
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         instance.setSiteId(siteId);
    
     }
 
     /**
      * Test of isDomainSupported method, of class GetNhinDataSource.
      */
     @Test
     public void testIsDomainSupported()
     {
         System.out.println("isDomainSupported");
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         boolean result = instance.isDomainSupported("george");
         assertFalse(result);
         result = instance.isDomainSupported("meds");
         assertTrue(result);
     }
 
     /**
      * Test of getData method, of class GetNhinDataSource.
      */
     @Test
     public void testGetData()
     {
         System.out.println("getData");
         String domain = "meds";
         String id = "100013";
         Properties props = null;
 
         GetNhinDataSource instance = (GetNhinDataSource) ctx.getBean("MDWSTest");
         InputStream result = instance.getData(domain, id, props);
         assertNotNull(result);
         
         try
         {
 
             IOUtils.copy(result, System.out);
         }
         catch (Exception e)
         {
             //
             fail("Exception streaming result");
         }
         
     }
 }
