 package edu.northwestern.bioinformatics.studycalendar.dao.delta;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import static edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase.*;
 
 import java.util.List;
 import java.util.Calendar;
 
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 
 public class AmendmentDaoTest extends DaoTestCase {
    private AmendmentDao amendmentDao = (AmendmentDao) getApplicationContext().getBean("amendmentDao");
 
     public void testGetById() throws Exception {
         Amendment actual = amendmentDao.getById(-100);
         assertNotNull("Amendment not found", actual);
         assertEquals("Wrong name", "abc", actual.getName());
         assertDayOfDate("Wrong date", 2006, Calendar.FEBRUARY, 1, actual.getDate());
         assertEquals("Wrong number of deltas", 1, actual.getDeltas().size());
         Delta delta = actual.getDeltas().get(0);
         assertEquals("Wrong delta", -102, (int) delta.getId());
         assertEquals("Wrong node for delta", -22, (int) delta.getNode().getId());
         assertSame("Reverse relationship not loaded", actual, delta.getRevision());
     }
 
     public void testLoadMandatoryFlag() throws Exception {
         assertFalse(amendmentDao.getById(-200).isMandatory());
     }
 
     public void testSave() throws Exception {
         int initialCount;
         {
             Amendment amendment = new Amendment();
             amendment.setName("new name");
             amendment.setDate(DateTools.createDate(2008, Calendar.FEBRUARY, 3));
             initialCount = amendmentDao.getAll().size();
             amendmentDao.save(amendment);
         }
 
         interruptSession();
 
         List<Amendment> listAfterAdding = amendmentDao.getAll();
         assertEquals("Amendment wasn't added ", initialCount + 1 , listAfterAdding.size());
     }
 
     public void testGetAll() throws Exception {
         List<Amendment> listBeforeAdding = amendmentDao.getAll();
         assertEquals("Amendment wasn't added ", 2 , listBeforeAdding.size());
     }
 
 }
