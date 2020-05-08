 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Holiday;
 import edu.northwestern.bioinformatics.studycalendar.domain.RelativeRecurringHoliday;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
 
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * @author Rhett Sutphin
  */
 public class SiteDaoTest extends DaoTestCase {
     private SiteDao siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
 
     public void testGetById() throws Exception {
         Site actual = siteDao.getById(-4);
         assertNotNull("Study not found", actual);
         assertEquals("Wrong id", -4, (int) actual.getId());
         assertEquals("Wrong name", "default", actual.getName());
     }
 
     public void testGetByAssignedIdentifier() throws Exception {
         Site actual = siteDao.getByAssignedIdentifier("assignedId");
         assertNotNull("site not found", actual);
         assertEquals("Wrong assignedIdentifier", "assignedId", actual.getAssignedIdentifier());
         //check for name also
         actual = siteDao.getByAssignedIdentifier("default");
         assertNull("site must not exists because identifier is not null", actual);
 
         actual = siteDao.getByAssignedIdentifier("anotherSite");
         assertNotNull("site not found", actual);
         assertEquals("Wrong assignedIdentifier", "anotherSite", actual.getAssignedIdentifier());
 
         actual = siteDao.getByAssignedIdentifier("Mayo Clinic");
         assertEquals("Wrong assigned identifier", "Mayo Clinic", actual.getAssignedIdentifier());
         assertEquals("Wrong name", "Mayo Clinic", actual.getName());
     }
 
     public void testDeleteHoliday() throws Exception {
         Site actual = siteDao.getById(-4);
         List<Holiday> list = actual.getHolidaysAndWeekends();
         actual.getHolidaysAndWeekends().remove(1);
         siteDao.save(actual);
 
 
         interruptSession();
 
         Site reloaded = siteDao.getById(-4);
         assertEquals("Holiday not removed", 1, reloaded.getHolidaysAndWeekends().size());
         assertEquals("Wrong holiday removed", -2,
                 (int) reloaded.getHolidaysAndWeekends().get(0).getId());
     }
 
     public void testAddHoliday() throws Exception {
         Site actual = siteDao.getById(-4);
 
         RelativeRecurringHoliday holidayToAdd = new RelativeRecurringHoliday();
         holidayToAdd.setWeekNumber(1);
         holidayToAdd.setDayOfTheWeek("Monday");
         holidayToAdd.setMonth(Calendar.SEPTEMBER);
         holidayToAdd.setId(-3);
         holidayToAdd.setDescription("Closed");
 
         List<Holiday> list = actual.getHolidaysAndWeekends();
         int size = list.size();
         list.add(holidayToAdd);
         actual.setHolidaysAndWeekends(list);
         siteDao.save(actual);
 
 
         interruptSession();
 
         Site reloaded = siteDao.getById(-4);
         assertEquals("Holiday is not added", size + 1, reloaded.getHolidaysAndWeekends().size());
         assertEquals("Wrong holiday added", holidayToAdd,
                 reloaded.getHolidaysAndWeekends().get(2));
     }
 
     public void testCount() throws Exception {
        assertEquals("Should be two sites, to start", 3, siteDao.getCount());
 
         Site newSite = new Site();
         newSite.setName("Hampshire");
         siteDao.save(newSite);
         interruptSession();
        assertEquals("Should be three sites after saving", 4, siteDao.getCount());
 
         getJdbcTemplate().update("DELETE FROM sites");
         assertEquals("And now there should be none", 0, siteDao.getCount());
     }
 }
