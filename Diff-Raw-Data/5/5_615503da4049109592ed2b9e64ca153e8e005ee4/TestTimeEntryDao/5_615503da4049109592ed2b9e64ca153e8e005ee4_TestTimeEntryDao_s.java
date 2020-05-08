 package ru.silvestrov.timetracker.data;
 
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Created by Silvestrov Ilya
  * Date: 1/19/12
  * Time: 10:47 PM
  */
 public class TestTimeEntryDao {
     private TimeEntryDao timeEntryDao;
 
     @Before
     public void setUp() {
        DataConfiguration dataConfiguration = new DataConfiguration("./testDB", "test-db.xml", "context.xml");
        timeEntryDao = dataConfiguration.getTimeEntryDao();
     }
 
     @Test
     public void testLatestTimeEntry() {
         TimeEntry t = timeEntryDao.getLastTimeEntry(2);
         Assert.assertNotNull(t);
         Assert.assertEquals(200, t.getTimeStart());
     }
 
     @Test
     public void testLatestTimeEntryForEmptyActivity() {
         TimeEntry t = timeEntryDao.getLastTimeEntry(1);
         Assert.assertNull(t);
     }
 }
