 package org.mule.galaxy.spring.config;
 
 
 import org.mule.galaxy.test.AbstractConfigLookupTest;
 
 public class RegistryIndexesLookupTest extends AbstractConfigLookupTest
 {
     public void testLookupByBeanId() throws Exception
     {
         doLookup("select artifact where spring.bean = 'TestObject1'", 1);
     }
 
     public void testLookupByBeanName() throws Exception
     {
         doLookup("select artifact where spring.bean = 'TestObject2'", 1);
     }
 
 
     public void testLookupByDescription() throws Exception
     {
        doLookup("select artifact where spring.description = 'Test Spring Application Context'", 1);
     }
 }
