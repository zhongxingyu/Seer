 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2.persistence;
 
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.AbstractCloudManagementPersistencyTest;
 import org.testng.annotations.*;
 
 /**
  * @author Itai Frenkel
  * Date: 25/07/13
  */
 public class Ec2PersistencyWithTwoManagersTwoFailuresTest extends AbstractCloudManagementPersistencyTest {
 
     @BeforeClass(alwaysRun = true)
     public void bootstrapAndInit() throws Exception{
         super.bootstrap();
         super.initManagementUrlsAndRestClient();
     }
    
    @AfterMethod
    public void afterTest() {
        restoreManagement();
    }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testManagementPersistencyTwoFailures() throws Exception {
         super.testManagementPersistencyTwoFailures();
     }
 
     @AfterClass(alwaysRun = true)
     public void teardown() throws Exception{
         super.teardown();
     }
 
     @Override
     protected String getCloudName() {
         return "ec2";
     }
 
     @Override
     protected boolean isReusableCloud() {
         return false;
     }
 }
