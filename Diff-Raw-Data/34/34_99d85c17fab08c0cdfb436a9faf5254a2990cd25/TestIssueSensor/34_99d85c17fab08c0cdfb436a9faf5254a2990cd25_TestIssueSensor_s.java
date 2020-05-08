 package org.hackystat.sensor.ant.issue;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import java.util.Arrays;
 import java.util.List;
 import org.apache.tools.ant.BuildException;
 import org.hackystat.sensor.ant.test.AntSensorTestHelper;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.client.SensorBaseClientException;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
 import org.hackystat.sensorshell.usermap.SensorShellMapException;
 import org.junit.Test;
 
 /**
  * Test cases for Issue Sensor.
  * @author Shaoxuan
  *
  */
 public class TestIssueSensor extends AntSensorTestHelper {
 
   private static final String NEW = "New";
   private static final String STARTED = "Started";
   private static final String ACCEPTED = "Accepted";
   private static final String FIXED = "Fixed";
   private static final String DEFECT = "Defect";
   private static final String TASK = "Task";
   private static final String ENHANCEMENT = "Enhancement";
   private static final String MEDIUM = "Medium";
   private static final String HIGH = "High";
   private static final String CRITICAL = "Critical";
   
   /**
    * Test the issue sensor with public Google Project Hosting service.
    * It will get data from project http://code.google.com/feeds/p/hackystat-sensor-ant.
    * @throws SensorBaseClientException if test fail.
    * @throws SensorShellMapException if error when loading sensorshell map
    */
   //@Ignore("Broken on 2/12/2009. Also renamed method to prevent Ant-based invocation")
   @Test
   public void testIssueSensorWithGoogleProjectHosting() 
     throws SensorBaseClientException, SensorShellMapException {
     IssueSensor sensor = new IssueSensor();
     sensor.setProjectName("hackystat-sensor-ant");
     // As there is no shell map for mapping account, all data will be sensor under the name
     // of TestAntSensors@hackystat.org.
     sensor.setDataOwnerHackystatAccount(user);
     sensor.setDataOwnerHackystatPassword(user);
     sensor.setDefaultHackystatSensorbase(host);
     sensor.setVerbose(true);
 
     System.out.println("Testing " + user + " in " + host);
     
     try {
       SensorBaseClient client = new SensorBaseClient(host, user, user);
       client.deleteSensorData(user);
       sensor.execute();
       int issueSize = client.getSensorDataIndex(user, IssueSensor.ISSUE_SENSOR_DATA_TYPE)
                             .getSensorDataRef().size();
       assertTrue("Should be at least 40 issues there.", issueSize > 40);
       assertEquals("All issue should be found new.", issueSize, sensor.updatedIssues.size());
       
       sensor.execute();
       assertEquals("Number of issue extract should be the same.", issueSize, 
           client.getSensorDataIndex(user, IssueSensor.ISSUE_SENSOR_DATA_TYPE)
           .getSensorDataRef().size());
       assertEquals("Now none issue should be found new.", 0, sensor.updatedIssues.size());
       System.out.println("Testing Issue Sensor.");
     }
     catch (BuildException e) {
       System.out.println(e.getMessage());
       if (e.getMessage().contains("IO error")) {
         System.out.println("Test not run because of IO exceptions.");
       }
       fail();
       //e.printStackTrace();
       return;
     }
   }
   
   /**
    * Test inner logic with makeup test data, in form of String arrays.
    * @throws SensorShellMapException if error when loading sensorshell map
    */
   @Test
   public void testIssueSensorWithMakeupTestData() throws SensorShellMapException {
     IssueSensor sensor = new IssueSensor();
     sensor.setProjectName("hackystat-sensor-ant");
     // As there is no shell map for mapping account, all data will be sensor under the name
     // of TestAntSensors@hackystat.org.
     sensor.setDataOwnerHackystatAccount(user);
     sensor.setDataOwnerHackystatPassword(user);
     sensor.setDefaultHackystatSensorbase(host);
     sensor.setVerbose(true);
 
     SensorBaseClient client = new SensorBaseClient(host, user, user);
     try {
       client.deleteSensorData(user);
     }
     catch (SensorBaseClientException e) {
       e.printStackTrace();
     }
     
     System.out.println("Testing " + user + " in " + host);
     
 
     String tester1 = "issueSensorTester1@hackystat.org";
     String tester2 = "issueSensorTester2@hackystat.org";
 
     String ms1 = "8.3";
     String ms2 = "8.4";
     
     //"MMM dd, yyyy kk:mm:ss"
     List<String[]> testIssueTableContents1 = Arrays.asList(
         new String[]{"21", DEFECT, ACCEPTED, MEDIUM, ms1, tester1, "Sep 07, 2008 11:00:00"}, 
         new String[]{"23", ENHANCEMENT, ACCEPTED, MEDIUM, "", tester1, "Jul 20, 2009 00:24:06"}, 
         new String[]{"14", ENHANCEMENT, NEW, HIGH, "", tester2, "Jun 20, 2009 14:35:32"});
     List<String[]> testIssueTableContents2 = Arrays.asList(
         new String[]{"21", ENHANCEMENT, FIXED, MEDIUM, ms2, tester1, "Sep 07, 2008 T11:00:00"}, 
         new String[]{"23", ENHANCEMENT, ACCEPTED, HIGH, "", tester2, "Jul 20, 2009 00:24:06"}, 
         new String[]{"14", ENHANCEMENT, NEW, HIGH, "", tester2, "Jun 20, 2009 14:35:32"}, 
         new String[]{"25", DEFECT, STARTED, CRITICAL, ms2, tester1, "Jul 21, 2009 20:00:00"}, 
         new String[]{"18", TASK, FIXED, HIGH, "", tester2, "Jul 21, 2009 18:00:00"});
     
     sensor.processGoogleIssueCsvData(testIssueTableContents1);
     try {
       List<SensorDataRef> refs = client.getSensorDataIndex(user, IssueSensor.ISSUE_SENSOR_DATA_TYPE)
         .getSensorDataRef();
       assertEquals("Should be 3 sensordata.", 3, refs.size());
       for (SensorDataRef ref : refs) {
         SensorData sensorData = client.getSensorData(ref);
         int id = IssueEntry.getIssueId(sensorData);
         switch (id) {
           case 21: 
             assertIssueSensorData(testIssueTableContents1.get(0), sensorData);
             break;
           case 23: 
             assertIssueSensorData(testIssueTableContents1.get(1), sensorData);
             break;
           case 14: 
             assertIssueSensorData(testIssueTableContents1.get(2), sensorData);
             break;
           default: fail("Unexpected issue found with id = " + id);
         }
         assertEquals("All 3 issues should be found new.", 3, sensor.updatedIssues.size());
       }
     }
     catch (SensorBaseClientException e) {
       fail("SensorBaseClientException when checking sensordata. " + e.getMessage());
       e.printStackTrace();
     }
     
     sensor.processGoogleIssueCsvData(testIssueTableContents2);
     try {
       List<SensorDataRef> refs = client.getSensorDataIndex(user, IssueSensor.ISSUE_SENSOR_DATA_TYPE)
         .getSensorDataRef();
       assertEquals("Should be 5 sensordata.", 5, refs.size());
       for (SensorDataRef ref : refs) {
         SensorData sensorData = client.getSensorData(ref);
         int id = IssueEntry.getIssueId(sensorData);
         switch (id) {
           case 21: 
             assertIssueSensorData(testIssueTableContents2.get(0), sensorData);
             break;
           case 23: 
             assertIssueSensorData(testIssueTableContents2.get(1), sensorData);
             break;
           case 14: 
             assertIssueSensorData(testIssueTableContents2.get(2), sensorData);
             break;
           case 25: 
             assertIssueSensorData(testIssueTableContents2.get(3), sensorData);
             break;
           case 18: 
             assertIssueSensorData(testIssueTableContents2.get(4), sensorData);
             break;
           default: fail("Unexpected issue found with id = " + id);
         }
         assertEquals("4 of 5 issues should be found new.", 4, sensor.updatedIssues.size());
       }
     }
     catch (SensorBaseClientException e) {
       fail("SensorBaseClientException when checking sensordata. " + e.getMessage());
       e.printStackTrace();
     }
   }
 
   private void assertIssueSensorData(String[] strings, SensorData sensorData) {
     try {
       assertEquals("Checking type.", strings[1], 
           IssueEntry.getLatestValueWithKey(sensorData, IssueEntry.TYPE_PROPERTY_KEY));
      assertEquals("Checking type.", strings[2], 
           IssueEntry.getLatestValueWithKey(sensorData, IssueEntry.STATUS_PROPERTY_KEY));
      assertEquals("Checking type.", strings[3], 
           IssueEntry.getLatestValueWithKey(sensorData, IssueEntry.PRIORITY_PROPERTY_KEY));
      assertEquals("Checking type.", strings[4], 
           IssueEntry.getLatestValueWithKey(sensorData, IssueEntry.MILESTONE_PROPERTY_KEY));
      assertEquals("Checking type.", strings[5], 
           IssueEntry.getLatestValueWithKey(sensorData, IssueEntry.OWNER_PROPERTY_KEY));
     }
     catch (Exception e) {
       fail("Fail to get latest value. " + e.getMessage());
       e.printStackTrace();
     }
   }
   
 }
