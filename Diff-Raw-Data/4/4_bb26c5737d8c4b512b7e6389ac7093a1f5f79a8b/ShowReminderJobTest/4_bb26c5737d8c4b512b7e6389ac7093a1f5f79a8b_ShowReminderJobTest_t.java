 package de.ronnyfriedland.time.logic.jobs;
 
 import java.util.Date;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.ronnyfriedland.time.entity.Entry;
 import de.ronnyfriedland.time.entity.EntryState;
 import de.ronnyfriedland.time.entity.Project;
 import de.ronnyfriedland.time.logic.EntityController;
 
 public class ShowReminderJobTest {
 
     private EntityController entityController;
     private Project project;
     private Entry entry;
 
     class MockPopupJob extends ShowReminderJob {
         private boolean show = false;
 
         public boolean isShow() {
             return show;
         }
 
         @Override
         public void showPopup(boolean show) {
             this.show = true;
         }
     }
 
     @Before
     public void setUp() throws Exception {
         entityController = EntityController.getInstance();
         project = new Project();
         project.setDescription("test");
         project.setName("test");
         entityController.create(project);
 
         entry = new Entry();
         entry.setDate(new Date());
         entry.setDescription("test");
         entry.setDuration("1");
         entry.setProject(project);
         entry.setState(new EntryState(new Date()));
         entityController.create(entry);
     }
 
     @After
     public void tearDown() throws Exception {
         entityController.deleteDetached(entry);
         entityController.deleteDetached(project);
     }
 
     @Test
     public void testJobExecution() throws Exception {
         MockContext ctx = new MockContext();
 
         java.util.Calendar cal = java.util.Calendar.getInstance();
         cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
         ctx.setPreviousFireTime(cal.getTime());
 
         MockPopupJob job1 = new MockPopupJob();
         job1.execute(ctx);
 
         cal.add(java.util.Calendar.DAY_OF_MONTH, -2);
         ctx.setPreviousFireTime(cal.getTime());
 
         MockPopupJob job2 = new MockPopupJob();
         job2.execute(ctx);
 
        Assert.assertTrue(job1.isShow());
        Assert.assertFalse(job2.isShow());
     }
 }
