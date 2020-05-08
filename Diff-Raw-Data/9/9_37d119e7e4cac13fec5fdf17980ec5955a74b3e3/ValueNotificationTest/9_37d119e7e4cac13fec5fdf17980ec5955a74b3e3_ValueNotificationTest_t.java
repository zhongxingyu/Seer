 package es.upm.dit.gsi.shanks.notification;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.LogManager;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import es.upm.dit.gsi.shanks.notification.test.TestAgent;
 import es.upm.dit.gsi.shanks.notification.test.TestDefinitions;
 
 public class ValueNotificationTest {
 
     /**
      * @throws Exception
      */
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         LogManager lm = LogManager.getLogManager();
         File configFile = new File("src/test/resources/logging.properties");
         lm.readConfiguration(new FileInputStream(configFile));
 
     }
 
     /**
      * @throws Exception
      */
     @Before
     public void setUp() throws Exception {
     }
 
     /**
      * @throws Exception
      */
     @After
     public void tearDown() throws Exception {
     }
 
     /**
      * Create a single notifications a check its fields. 
      */
     @Test
     public void createNotification() {
 
         ValueNotification iNotification = new ValueNotification(
                 TestDefinitions.NOTIFICATION_ID, TestDefinitions.NOTIFICATION_WHEN,
                 TestDefinitions.NOTIFICATION_SOURCE, TestDefinitions.VN_ELEMENT_ID,
                 TestDefinitions.VN_VALUE);
 
         Assert.assertTrue(iNotification.getId().equals(TestDefinitions.NOTIFICATION_ID));
         Assert.assertTrue(iNotification.getWhen() == TestDefinitions.NOTIFICATION_WHEN);
         Assert.assertTrue(iNotification.getSource().equals(
                 TestDefinitions.NOTIFICATION_SOURCE));
         Assert.assertTrue(iNotification.getElementID().equals(
                 TestDefinitions.VN_ELEMENT_ID));
         Assert.assertTrue(iNotification.getValue().equals(
                 TestDefinitions.VN_VALUE));
     }
 
     /**
      * create a number of notifications and add them to a NotificationManager.  
      */
     @Test
     public void createAndAddNotificationsToNotificationManager() {
         
         ArrayList<Notification> ln = new ArrayList<Notification>();
         
         for (int i = 0; i < TestDefinitions.NOTIFICATIONS_SIZE; i++) {
             ln.add(new ValueNotification(TestDefinitions.NOTIFICATION_ID+i, 
                     TestDefinitions.NOTIFICATION_WHEN, TestDefinitions.NOTIFICATION_SOURCE, 
                     TestDefinitions.VN_ELEMENT_ID, TestDefinitions.VN_VALUE));
         }
         try {
             NotificationManager nm = new NotificationManager(ln, null, TestDefinitions.getSimulation(0));
             Assert.assertEquals(ln, (nm.getByType(ValueNotification.class)));
             ln.add(new ValueNotification(null, 0, nm, null, null));
             Assert.assertNotSame(ln, (nm.getByType(ValueNotification.class)));
         } catch (Exception e) {
             e.printStackTrace();
             Assert.fail(""+e);
         }
     }
     
     /**
      * Create a list of notifications, add them to a NotificationManager and them 
      * make ID queries to the NotificationManager. 
      */
     @Test
     public void checkIDQueries() {
         
         ArrayList<Notification> ln = new ArrayList<Notification>();
         
         for (int i = 0; i < TestDefinitions.NOTIFICATIONS_SIZE; i++) {
             ln.add(new ValueNotification(TestDefinitions.NOTIFICATION_ID+i, 
                     TestDefinitions.NOTIFICATION_WHEN, TestDefinitions.NOTIFICATION_SOURCE, 
                     TestDefinitions.VN_ELEMENT_ID, TestDefinitions.VN_VALUE));
         }
         
         ArrayList<Notification> obtained_ln = new ArrayList<Notification>();
         NotificationManager nm = null;
         try {
             nm = new NotificationManager(ln, null, TestDefinitions.getSimulation(0));
             obtained_ln = nm.getByType(ValueNotification.class);
             Assert.assertEquals(ln, obtained_ln);
             for (Notification expected: ln){
                 Assert.assertEquals(expected, nm.getByID(expected.getId()));
             }
         } catch (Exception e) {
             e.printStackTrace();
             Assert.fail(""+e);
         }
     }
     
     /**
      * Create a list of notifications, add them to a NotificationManager and them 
      * make step queries to the NotificationManager. 
      */
     @Test
     public void checkStepQueries() {
         
         ArrayList<Notification> ln = new ArrayList<Notification>();
         HashMap<String, Integer> stepCount = new HashMap<String, Integer>();
         
         for (int i = 0; i < TestDefinitions.NOTIFICATIONS_SIZE; i++) {
             Long step = Math.round(TestDefinitions.NOTIFICATION_WHEN*Math.random());
             if(stepCount.containsKey(step.toString())){
                 stepCount.put(step.toString(), stepCount.get(step.toString())+1);
             } else {
                 stepCount.put(step.toString(), 1);
             }
             ln.add(new ValueNotification(TestDefinitions.NOTIFICATION_ID+i, step, 
                     TestDefinitions.NOTIFICATION_SOURCE, TestDefinitions.VN_ELEMENT_ID, 
                     TestDefinitions.VN_VALUE));
         }
         
         NotificationManager nm = null;
         try {
             nm = new NotificationManager(ln, null, TestDefinitions.getSimulation(0));
             Assert.assertEquals(ln, nm.getByType(ValueNotification.class));
             
             for(String step:stepCount.keySet()){
                 List<Notification> obtained = nm.getByStep(Integer.parseInt(step));
                 Assert.assertTrue(obtained.size()==stepCount.get(step).intValue());
                 for (Notification n: obtained){
                     Assert.assertTrue(ln.contains(n));
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             Assert.fail(""+e);
         }
     }
     
     /**
      * Create a list of notifications, add them to a NotificationManager and them 
      * make step queries to the NotificationManager. 
      */
     @Test
     public void checkSourceQueries() {
         
         ArrayList<Notification> ln = new ArrayList<Notification>();
         HashMap<Object, Integer> sourceCount = new HashMap<Object, Integer>();
         ArrayList<Object> allSources = new ArrayList<Object>();
         
         for(int i=0; i<TestDefinitions.SOURCES_SIZE+1; i++)
             allSources.add(new TestAgent(TestDefinitions.AGENT_ID+i));
         
         for (int i=0; i < TestDefinitions.NOTIFICATIONS_SIZE; i++) {
             Object source = null;
             switch((int)(Math.round(TestDefinitions.SOURCES_SIZE*Math.random()))) {
             case 0:
                 source = allSources.get(0);
                 break;
             case 1:
                 source = allSources.get(1);
                 break;    
             case 2:
                 source = allSources.get(2);
                 break;
             case 3:
                 source = allSources.get(3);
                 break;
             case 4:
                 source = allSources.get(4);
                 break;
             case 5:
                 source = allSources.get(5);
                 break;
             default:
                 break;
             }
             if(sourceCount.containsKey(source)){
                 sourceCount.put(source, sourceCount.get(source)+1);
             } else {
                 sourceCount.put(source, 1);
             }
             ln.add(new ValueNotification(TestDefinitions.NOTIFICATION_ID+i,
                     TestDefinitions.NOTIFICATION_WHEN, source, TestDefinitions.VN_ELEMENT_ID, 
                     TestDefinitions.VN_VALUE));
         }
         NotificationManager nm = null;
         try {
             nm = new NotificationManager(ln, null, TestDefinitions.getSimulation(0));
             Assert.assertEquals(ln, nm.getByType(ValueNotification.class));
             
             for(Object source:sourceCount.keySet()){
                 List<Notification> obtained = nm.getBySource(source);
                 Assert.assertTrue(obtained.size()==sourceCount.get(source).intValue());
                 for (Notification n: obtained){
                     Assert.assertTrue(ln.contains(n));
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             Assert.fail(""+e);
         }
     }
     
     /**
      * Create a list of notifications, add them to a NotificationManager and them 
      * make step queries to the NotificationManager. 
      */
     @Test
     public void checkInteractionQueries() {
         
         ArrayList<Notification> ln = new ArrayList<Notification>();
         for (int i = 0; i < TestDefinitions.NOTIFICATIONS_SIZE; i++) {
             ln.add(new ValueNotification(TestDefinitions.NOTIFICATION_ID+i, 
                     TestDefinitions.NOTIFICATION_WHEN, TestDefinitions.NOTIFICATION_SOURCE, 
                     TestDefinitions.VN_ELEMENT_ID, TestDefinitions.VN_VALUE));
         }
         NotificationManager nm = null;
         try {
             nm = new NotificationManager(ln, null, TestDefinitions.getSimulation(0));
             Assert.assertEquals(ln, nm.getByType(ValueNotification.class));
             @SuppressWarnings("unused")
             List<Notification> obtained = nm.getByInteraction(TestDefinitions.IN_INTERACTION);
             Assert.fail();
         } catch (Exception e) {
         }
     }
     
     /**
      * Create a list of notifications, add them to a NotificationManager and them 
      * make queries by target to the NotificationManager. 
      */
     @Test
     public void checkTargetQueries() {
         ArrayList<Notification> ln = new ArrayList<Notification>();
         for (int i = 0; i < TestDefinitions.NOTIFICATIONS_SIZE; i++) {
             ln.add(new ValueNotification(TestDefinitions.NOTIFICATION_ID+i, 
                     TestDefinitions.NOTIFICATION_WHEN, TestDefinitions.NOTIFICATION_SOURCE, 
                     TestDefinitions.VN_ELEMENT_ID, TestDefinitions.VN_VALUE));
         }
         NotificationManager nm = null;
         try {
             nm = new NotificationManager(ln, null, TestDefinitions.getSimulation(0));
             Assert.assertEquals(ln, nm.getByType(ValueNotification.class));
             @SuppressWarnings("unused")
             List<Notification> obtained = nm.getByTarget(TestDefinitions.IN_INTERACTION);
             Assert.fail();
         } catch (Exception e) {
         }
         
     }
     
     /**
      * Create a list of notifications, add them to a NotificationManager and them 
      * make queries by ElementId to the NotificationManager. 
      */
     @Test
     public void checkElementIDQueries() {
         ArrayList<Notification> ln = new ArrayList<Notification>();
         HashMap<String, Integer> elementIDCount = new HashMap<String, Integer>();
         ArrayList<String> allElementIDs = new ArrayList<String>();
         
         for(int i=0; i<=TestDefinitions.ELEMENT_ID_SIZE; i++)
             allElementIDs.add(TestDefinitions.VN_ELEMENT_ID+i);
         
         for (int i=0; i<TestDefinitions.NOTIFICATIONS_SIZE; i++) {
             String elementID = allElementIDs.get((int)
                     (Math.round((TestDefinitions.ELEMENT_ID_SIZE)*Math.random())));
             if(elementIDCount.containsKey(elementID)){
                 elementIDCount.put(elementID, elementIDCount.get(elementID)+1);
             } else {
                 elementIDCount.put(elementID, 1);
             }
             ln.add(new ValueNotification(TestDefinitions.NOTIFICATION_ID+i, 
                     TestDefinitions.NOTIFICATION_WHEN, TestDefinitions.NOTIFICATION_SOURCE, 
                     elementID, TestDefinitions.VN_VALUE));
         }
         try {
             NotificationManager nm = new NotificationManager(ln, null, TestDefinitions.getSimulation(0));
             Assert.assertEquals(ln, (nm.getByType(ValueNotification.class)));
             for(String elementID:elementIDCount.keySet()){
                 List<Notification> obtained = nm.getByElementID(elementID);
                 Assert.assertTrue(obtained.size()==elementIDCount.get(elementID).intValue());
                 for (Notification n: obtained){
                     Assert.assertTrue(ln.contains(n));
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             Assert.fail(""+e);
         }
     }
     
     /**
      * Create a list of notifications making different events get launched. 
      */
     @Test
    public void createCustomUserNotifications() {
 
 //        Assert.assertTrue(catched);
     }
 }
