 package edu.northwestern.bioinformatics.studycalendar.dao.auditing;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDaoTest;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Jalpa Patel
  */
 public class AuditDeleteEventListenerIntegratedTest extends AuditEventListenerTestCase {
     private PeriodDao periodDao;
 
     @Override
     protected String getTestDataFileName() {
         return String.format("../testdata/%s.xml",
             PeriodDaoTest.class.getSimpleName());
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         periodDao = (PeriodDao) getApplicationContext().getBean("periodDao");
     }
 
     public void testAuditEventAfterPeriodDelete() throws Exception {
         {
             Period p = periodDao.getById(-1111);
             periodDao.delete(p);
         }
 
         interruptSession();
 
         int objectId = -1111;
         List<Map> eventIds = getJdbcTemplate().queryForList("select id from audit_events where object_id = ? and class_name = ? and operation = 'DELETE'", new Object[]{objectId, Period.class.getName()});
        Number eventId = (Number)eventIds.get(0).get("id");
         assertNotNull("Audit event for DELETE is not created ", eventId);
         
        List<Map> rows = getAuditEventValuesForEvent(eventId.intValue());
         assertEquals("No of rows are different", 6, rows.size());
         Map durationQuantityRow = rows.get(1);
         Map durationUnitRow = rows.get(2);
         Map nameRow = rows.get(3);
         Map repetitionsRow = rows.get(4);
         Map startDayRow = rows.get(5);
 
         // Testing duration.quantity
         assertEquals("Duration.quantity previous value doesn't match", "6", durationQuantityRow.get(PREVIOUS_VALUE));
         assertEquals("Duration.quantity new value doesn't match", null, durationQuantityRow.get(NEW_VALUE));
 
         // Testing duration.Unit
         assertEquals("Duration.Unit previous value doesn't match", "week", durationUnitRow.get(PREVIOUS_VALUE));
         assertEquals("Duration.Unit new value doesn't match", null, durationUnitRow.get(NEW_VALUE));
 
         // Testing name
         assertEquals("Name previous value doesn't match", "Treatment", nameRow.get(PREVIOUS_VALUE));
         assertEquals("Name new value doesn't match", null, nameRow.get(NEW_VALUE));
 
         // Testing repetitions
         assertEquals("Repetitions previous value doesn't match", "3", repetitionsRow.get(PREVIOUS_VALUE));
         assertEquals("Repetitions new value doesn't match", null, repetitionsRow.get(NEW_VALUE));
 
         // Testing startDay
         assertEquals("StartDay previous value doesn't match", "8", startDayRow.get(PREVIOUS_VALUE));
         assertEquals("StartDay new value doesn't match", null, startDayRow.get(NEW_VALUE));
     }
 }
 
