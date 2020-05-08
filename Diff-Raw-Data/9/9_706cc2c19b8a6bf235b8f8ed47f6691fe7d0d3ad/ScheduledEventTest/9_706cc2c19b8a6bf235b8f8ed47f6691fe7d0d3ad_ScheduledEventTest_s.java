 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import edu.nwu.bioinformatics.commons.DateUtils;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Conditional;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.NotApplicable;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author Rhett Sutphin
  */
 public class ScheduledEventTest extends StudyCalendarTestCase {
     private ScheduledEvent scheduledEvent = new ScheduledEvent();
 
     public void testGetActualDateCurrentDate() throws Exception {
         Date expected = DateUtils.createDate(2006, Calendar.AUGUST, 3);
         scheduledEvent.setIdealDate(DateUtils.createDate(2006, Calendar.AUGUST, 1));
         scheduledEvent.changeState(new Scheduled(null, expected));
         assertEquals(expected, scheduledEvent.getActualDate());
     }
 
     public void testGetActualDateIsPreviousDate() throws Exception {
         Date expected = DateUtils.createDate(2006, Calendar.AUGUST, 3);
         scheduledEvent.setIdealDate(DateUtils.createDate(2006, Calendar.AUGUST, 1));
         scheduledEvent.changeState(new Scheduled(null, DateUtils.createDate(2006, Calendar.AUGUST, 4)));
         scheduledEvent.changeState(new Scheduled(null, expected));
         scheduledEvent.changeState(new Canceled());
         assertEquals(expected, scheduledEvent.getActualDate());
     }
 
     public void testGetActualDateIsIdealDate() throws Exception {
         Date expected = DateUtils.createDate(2006, Calendar.AUGUST, 3);
         scheduledEvent.setIdealDate(expected);
         assertEquals(expected, scheduledEvent.getActualDate());
     }
 
     public void testChangeStateWithNoEvents() throws Exception {
         scheduledEvent.changeState(new Canceled("A"));
         assertEquals("Wrong number of states in history", 1, scheduledEvent.getAllStates().size());
         assertTrue("Wrong curent state", scheduledEvent.getCurrentState() instanceof Canceled);
         assertEquals("Wrong curent state", "A", scheduledEvent.getCurrentState().getReason());
         assertEquals(0, scheduledEvent.getPreviousStates().size());
     }
 
     public void testChangeStateWithCurrentEventOnly() throws Exception {
         scheduledEvent.changeState(new Canceled("A"));
         scheduledEvent.changeState(new Canceled("B"));
         assertEquals("Wrong number of states in history", 2, scheduledEvent.getAllStates().size());
         assertEquals("Wrong current state", "B", scheduledEvent.getCurrentState().getReason());
         assertEquals("Wrong number of previous states", 1, scheduledEvent.getPreviousStates().size());
         assertEquals("Wrong previous state", "A", scheduledEvent.getPreviousStates().get(0).getReason());
     }
     
     public void testChangeStateWithExistingHistory() throws Exception {
         scheduledEvent.changeState(new Canceled("A"));
         scheduledEvent.changeState(new Canceled("B"));
         scheduledEvent.changeState(new Canceled("C"));
         assertEquals("Wrong number of states in history", 3, scheduledEvent.getAllStates().size());
         assertEquals("Wrong current state", "C", scheduledEvent.getCurrentState().getReason());
         assertEquals("Wrong number of previous states", 2, scheduledEvent.getPreviousStates().size());
         assertEquals("Wrong previous state 0", "A", scheduledEvent.getPreviousStates().get(0).getReason());
         assertEquals("Wrong previous state 1", "B", scheduledEvent.getPreviousStates().get(1).getReason());
     }
 
     public void testGetAllWithNone() throws Exception {
         assertEquals(0, scheduledEvent.getAllStates().size());
     }
 
     public void testGetAllWithCurrentOnly() throws Exception {
         scheduledEvent.changeState(new Canceled("A"));
         List<ScheduledEventState> all = scheduledEvent.getAllStates();
         assertEquals(1, all.size());
         assertEquals("A", all.get(0).getReason());
     }
     
     public void testGetAllWithHistory() throws Exception {
         scheduledEvent.changeState(new Canceled("A"));
         scheduledEvent.changeState(new Canceled("B"));
         scheduledEvent.changeState(new Canceled("C"));
 
         List<ScheduledEventState> all = scheduledEvent.getAllStates();
         assertEquals(3, all.size());
         assertEquals("A", all.get(0).getReason());
         assertEquals("B", all.get(1).getReason());
         assertEquals("C", all.get(2).getReason());
     }
 
     public void testChangeStateCanceledToOccurredWithOffStudy() throws Exception {
         scheduledEvent.changeState(new Scheduled("New", DateUtils.createDate(2007, Calendar.SEPTEMBER, 2)));
         ScheduledCalendar calendar = new ScheduledCalendar();
         StudyParticipantAssignment assignment = new StudyParticipantAssignment();
         calendar.setAssignment(assignment);
         calendar.addArm(new ScheduledArm());
         calendar.getScheduledArms().get(0).addEvent(scheduledEvent);
         scheduledEvent.changeState(new Canceled());
         assignment.setEndDateEpoch(DateUtils.createDate(2007, Calendar.SEPTEMBER, 1));
         
         scheduledEvent.changeState(new Occurred());
         assertEquals("Wrong states size", 2, scheduledEvent.getAllStates().size());
         assertEquals("Wrong event state", ScheduledEventMode.CANCELED, scheduledEvent.getCurrentState().getMode());
     }
 
     public void testScheduleConditional() throws Exception {
         scheduledEvent.changeState(new Conditional("New", DateUtils.createDate(2007, Calendar.SEPTEMBER, 2)));
         scheduledEvent.changeState(new Scheduled("Scheduled", DateUtils.createDate(2007, Calendar.SEPTEMBER, 2)));
         scheduledEvent.changeState(new Occurred("Occurred", DateUtils.createDate(2007, Calendar.SEPTEMBER, 2)));
 
         assertEquals("Conditional flag not set", true, scheduledEvent.isConditionalEvent());
         assertEquals("Wrong previous state size", 2, scheduledEvent.getPreviousStates().size());
     }
 
     public void testIsConditionalPos() throws Exception {
         Date date = DateUtils.createDate(2006, Calendar.AUGUST, 3);
         scheduledEvent.changeState(new Conditional("Conditional", date));
         scheduledEvent.changeState(new Scheduled("Conditional", date));
         assertTrue("Event should be conditional", scheduledEvent.isConditionalEvent());
     }
 
     public void testIsConditionalNeg() throws Exception {
         Date date = DateUtils.createDate(2006, Calendar.AUGUST, 3);
         scheduledEvent.changeState(new Scheduled("Conditional", date));
         assertFalse("Event should not be conditional", scheduledEvent.isConditionalEvent());
     }
 
     public void testIsValidStateChangePos() throws Exception {
         Date date = DateUtils.createDate(2006, Calendar.AUGUST, 3);
         scheduledEvent.changeState(new Conditional("Conditional", date));
         assertTrue("Should be valid new state", scheduledEvent.isValidNewState(Scheduled.class));
     }
 
     public void testIsValidStateChangeNeg() throws Exception {
         Date date = DateUtils.createDate(2006, Calendar.AUGUST, 3);
         scheduledEvent.changeState(new Conditional("Conditional", date));
         assertFalse("Should not be valid new state", scheduledEvent.isValidNewState(Canceled.class));
     }
 
     public void testUnscheduleScheduledEvent() throws Exception {
         scheduledEvent.changeState(new Scheduled());
         assertEquals("Test setup failure", 1, scheduledEvent.getAllStates().size());
 
         scheduledEvent.unscheduleIfOutstanding("Testing");
         assertEquals("State not changed", 2, scheduledEvent.getAllStates().size());
         assertEquals("State not changed", ScheduledEventMode.CANCELED,
             scheduledEvent.getCurrentState().getMode());
         assertEquals("New state has wrong reason", "Testing",
             scheduledEvent.getCurrentState().getReason());
     }
 
     public void testUnscheduleConditionalEvent() throws Exception {
         scheduledEvent.changeState(new Conditional());
         assertEquals("Test setup failure", 1, scheduledEvent.getAllStates().size());
 
         scheduledEvent.unscheduleIfOutstanding("Testing");
         assertEquals("State not changed", 2, scheduledEvent.getAllStates().size());
         assertEquals("State not changed", ScheduledEventMode.NOT_APPLICABLE,
             scheduledEvent.getCurrentState().getMode());
         assertEquals("New state has wrong reason", "Testing",
             scheduledEvent.getCurrentState().getReason());
     }
 
     public void testUnscheduleCanceledEvent() throws Exception {
         scheduledEvent.changeState(new Canceled());
         assertEquals("Test setup failure", 1, scheduledEvent.getAllStates().size());
 
         scheduledEvent.unscheduleIfOutstanding("Testing");
         assertEquals("State changed", 1, scheduledEvent.getAllStates().size());
         assertEquals("State changed", ScheduledEventMode.CANCELED,
             scheduledEvent.getCurrentState().getMode());
     }
 
     public void testUnscheduleOccurredEvent() throws Exception {
         scheduledEvent.changeState(new Occurred());
         assertEquals("Test setup failure", 1, scheduledEvent.getAllStates().size());
 
         scheduledEvent.unscheduleIfOutstanding("Testing");
         assertEquals("State changed", 1, scheduledEvent.getAllStates().size());
         assertEquals("State changed", ScheduledEventMode.OCCURRED,
             scheduledEvent.getCurrentState().getMode());
     }
 
     public void testUnscheduleNotApplicableEvent() throws Exception {
         scheduledEvent.changeState(new NotApplicable());
         assertEquals("Test setup failure", 1, scheduledEvent.getAllStates().size());
 
         scheduledEvent.unscheduleIfOutstanding("Testing");
         assertEquals("State changed", 1, scheduledEvent.getAllStates().size());
         assertEquals("State changed", ScheduledEventMode.NOT_APPLICABLE,
             scheduledEvent.getCurrentState().getMode());
     }
 }
