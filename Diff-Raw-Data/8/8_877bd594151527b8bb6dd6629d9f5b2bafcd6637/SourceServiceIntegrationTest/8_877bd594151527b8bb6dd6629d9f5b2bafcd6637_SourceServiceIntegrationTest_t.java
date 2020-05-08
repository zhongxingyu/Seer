 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
 
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * @author Saurabh Agrawal
  */
 public class SourceServiceIntegrationTest extends DaoTestCase {
     private static final String TARGET_SOURCE = "targetSource";
     private static final String SOURCE = "source";
 
     private SourceService sourceService = (SourceService) getApplicationContext().getBean("sourceService");
 
     private Source targetSource;
     private Source source;
     private ActivityDao activityDao = (ActivityDao) getApplicationContext().getBean("activityDao");
     private ActivityTypeDao activityTypeDao = (ActivityTypeDao) getApplicationContext().getBean("activityTypeDao");
     List<Activity> activitiesToAddAndRemove;
 
     private Activity activity, anotherActivity, activityToUpdate, activityToDelete;
     private ActivityType activityType;
     protected void setUp() throws Exception {
         super.setUp();
 
         source = Fixtures.createSource(SOURCE);
         targetSource = Fixtures.createSource(TARGET_SOURCE);
 
         activitiesToAddAndRemove = new ArrayList<Activity>();
 
         activityType = Fixtures.createActivityType("LAB_TEST");
         activityTypeDao.save(activityType);
 
        activity = Fixtures.createActivity("activity1", "code", null, activityType);
        anotherActivity = Fixtures.createActivity("anotherActivity", "code2", null, activityType);
        activityToUpdate = Fixtures.createActivity("activityToUpdate", "CS", null, activityType);
        activityToDelete = Fixtures.createActivity("activityToDelete", "CS", null, activityType);
 
         activitiesToAddAndRemove.add(activity);
         activitiesToAddAndRemove.add(anotherActivity);
     }
 
 
     public void testUpdateSourceDeleteExistingActivitiesAndAddNewActivity() {
 
         source = sourceService.getByName("ICD9");
         assertNotNull("source must not be null", source);
         assertEquals("source must have 3 activities", Integer.valueOf(4), Integer.valueOf(source.getActivities().size()));
         sourceService.updateSource(source, activitiesToAddAndRemove);
 
         interruptSession();
         source = sourceService.getByName("ICD9");
         assertEquals("source must have only 2 activities now", Integer.valueOf(2), Integer.valueOf(activityDao.getBySourceId(source.getId()).size()));
 
 
     }
 
 
     public void testUpdateSourceForUpdateAndDeleteActivity() {
 
         activitiesToAddAndRemove.add(activityToUpdate);
         source = sourceService.getByName("ICD9");
         assertNotNull("source must not be null", source);
         assertEquals("source must have 3 activities", Integer.valueOf(4), Integer.valueOf(source.getActivities().size()));
         sourceService.updateSource(source, activitiesToAddAndRemove);
 
         interruptSession();
         source = sourceService.getByName("ICD9");
         assertEquals("source must have only 3 activities ", Integer.valueOf(3), Integer.valueOf(source.getActivities().size()));
     }
 }
