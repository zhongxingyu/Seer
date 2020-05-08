 package com.galineer.suzy.accountsim.framework;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.chrono.CopticChronology;
 
 public class TestSchedule {
 
   class EventTest implements AccountEvent {
     EventTest() {}
     public void run() {}
   }
 
   class EventWithID implements AccountEvent {
     private int eventID;
     private ArrayList<Integer> trace;
 
     EventWithID(int eventID, ArrayList<Integer> trace) {
       this.eventID = eventID;
       this.trace = trace;
     }
 
     public void run() {
       this.trace.add(new Integer(this.eventID));
     }
   }
 
   private Schedule schedule;
   private AccountEvent event1;
   private AccountEvent event2;
   private AccountEvent event3;
   private DateTime oct5at1am;
   private DateTime oct12at3pm;
   private DateTime nov5at1am;
   private DateTime nov12at3pm;
   private DateTime differentChronology;
   private Period daily;
   private Period weekly;
   private Period fortnightly;
   private Period negativePeriod;
   private Period zeroPeriod;
 
   @Before
   public void setUp() {
     schedule = new Schedule();
     event1 = new EventTest();
     event2 = new EventTest();
     event3 = new EventTest();
     oct5at1am = new DateTime(2012, 10, 5, 1, 0);
     oct12at3pm = new DateTime(2012, 10, 12, 15, 0);
     nov5at1am = new DateTime(2012, 11, 5, 1, 0);
     nov12at3pm = new DateTime(2012, 11, 12, 15, 0);
     differentChronology = new DateTime(2012, 10, 12, 15, 0,
                                        CopticChronology.getInstance());
     daily = Period.days(1);
     weekly = Period.weeks(1);
     fortnightly = Period.weeks(2);
     negativePeriod = Period.days(-1);
     zeroPeriod = Period.days(0);
   }
 
   @After
   public void tearDown() {
   }
 
 
 
   // Construction and empty schedule properties
 
   @Test
   public void testScheduleConstructor() {
     assertFalse(schedule.hasMoreEvents());
     assertNull(schedule.getEarliestEventTime());
   }
 
 
 
   // Add event specifying end date
 
   @Test
   public void testAddEventWithEndDateBasic() {
     schedule.addEvent(event1, oct5at1am, oct12at3pm, daily);
     assertTrue(schedule.hasMoreEvents());
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithEndDateNullEvent() {
     schedule.addEvent(null, oct5at1am, oct12at3pm, daily);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithEndDateNullStartDate() {
     schedule.addEvent(event1, null, oct12at3pm, daily);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithEndDateStartDateDifferentChronology() {
     schedule.addEvent(event1, differentChronology, oct12at3pm, daily);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithEndDateNullEndDate() {
     schedule.addEvent(event1, oct5at1am, null, daily);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithEndDateBeforeStartDate() {
     schedule.addEvent(event1, oct12at3pm, oct5at1am, daily);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithEndDateDifferentChronology() {
     schedule.addEvent(event1, oct12at3pm, differentChronology, daily);
   }
 
   @Test
   public void testAddEventWithEndDateNullPeriod() {
     // null period is fine, that just means the event doesn't repeat;
     // end date is redundant in this case
     schedule.addEvent(event1, oct5at1am, oct12at3pm, null);
     assertTrue(schedule.hasMoreEvents());
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithEndDateNegativePeriod() {
     schedule.addEvent(event1, oct5at1am, oct12at3pm, negativePeriod);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithEndDateZeroPeriod() {
     schedule.addEvent(event1, oct5at1am, oct12at3pm, zeroPeriod);
   }
 
 
 
   // Add event, non-recurring
 
   @Test
   public void testAddEventNonRecurringBasic() {
     schedule.addEvent(event1, oct5at1am);
     assertTrue(schedule.hasMoreEvents());
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventNonRecurringNullEvent() {
    schedule.addEvent(null, oct5at1am);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventNonRecurringNullStartDate() {
    schedule.addEvent(event1, null);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventNonRecurringStartDateDifferentChronology() {
    schedule.addEvent(event1, differentChronology);
   }
 
 
 
   // Add event specifying number of repeats
 
   @Test
   public void testAddEventWithNumRepsBasic() {
     schedule.addEvent(event1, oct5at1am, daily, 8);
     assertTrue(schedule.hasMoreEvents());
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithNumRepsNullEvent() {
     schedule.addEvent(null, oct5at1am, weekly, 1);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithNumRepsNullStartDate() {
     schedule.addEvent(event1, null, weekly, 1);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithNumRepsStartDateDifferentChronology() {
     schedule.addEvent(event1, differentChronology, weekly, 1);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithNumRepsNegativePeriod() {
     schedule.addEvent(event1, oct5at1am, negativePeriod, 5);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithNumRepsZeroPeriod() {
     schedule.addEvent(event1, oct5at1am, zeroPeriod, 5);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithNumRepsZeroReps() {
     schedule.addEvent(event1, oct5at1am, daily, 0);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithNumRepsNegativeReps() {
     schedule.addEvent(event1, oct5at1am, daily, -1);
   }
 
   @Test
   public void testAddEventWithNumRepsNullPeriod() {
     // null period OK if event is non-repeating
     schedule.addEvent(event1, oct5at1am, null, 1);
     assertTrue(schedule.hasMoreEvents());
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testAddEventWithNumRepsNullPeriodPositiveReps() {
     // null period not OK if event is to occur more than once
     schedule.addEvent(event1, oct5at1am, null, 2);
   }
 
 
 
 
   // Popping next event
 
   @Test(expected=NoSuchElementException.class)
   public void testPopOnEmptySchedule() {
     assertFalse(schedule.hasMoreEvents());
     AccountEvent e = schedule.popNextEvent();
   }
 
   @Test
   public void testPopNonRecurringEvent() {
     schedule.addEvent(event1, oct5at1am);
     assertTrue(schedule.hasMoreEvents());
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
 
     AccountEvent e = schedule.popNextEvent();
     assertEquals(event1, e);
     assertFalse(schedule.hasMoreEvents());
     assertNull(schedule.getEarliestEventTime());
   }
 
   @Test
   public void testPopMultipleNonRecurringEvents() {
     schedule.addEvent(event1, oct12at3pm);
     assertEquals(oct12at3pm, schedule.getEarliestEventTime());
     schedule.addEvent(event2, oct5at1am);
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
     schedule.addEvent(event3, nov5at1am);
 
     assertTrue(schedule.hasMoreEvents());
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
     AccountEvent e = schedule.popNextEvent();
     assertEquals(event2, e);
 
     assertTrue(schedule.hasMoreEvents());
     assertEquals(oct12at3pm, schedule.getEarliestEventTime());
     e = schedule.popNextEvent();
     assertEquals(event1, e);
 
     assertTrue(schedule.hasMoreEvents());
     assertEquals(nov5at1am, schedule.getEarliestEventTime());
     e = schedule.popNextEvent();
     assertEquals(event3, e);
 
     assertFalse(schedule.hasMoreEvents());
     assertNull(schedule.getEarliestEventTime());
   }
 
   @Test
   public void testPopRecurringEvent() {
     schedule.addEvent(event1, oct5at1am, daily, 3);
     assertTrue(schedule.hasMoreEvents());
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
     AccountEvent e = schedule.popNextEvent();
     assertEquals(event1, e);
 
     assertTrue(schedule.hasMoreEvents());
     assertEquals(new DateTime(2012, 10, 6, 1, 0),
                  schedule.getEarliestEventTime());
     e = schedule.popNextEvent();
     assertEquals(event1, e);
     
     assertTrue(schedule.hasMoreEvents());
     assertEquals(new DateTime(2012, 10, 7, 1, 0),
                  schedule.getEarliestEventTime());
     e = schedule.popNextEvent();
     assertEquals(event1, e);
 
     assertFalse(schedule.hasMoreEvents());
     assertNull(schedule.getEarliestEventTime());
   }
 
   @Test
   public void testAddPopMixedEvents() {
     schedule.addEvent(event1, nov5at1am, nov12at3pm, weekly);
     assertEquals(nov5at1am, schedule.getEarliestEventTime());
     schedule.addEvent(event2, oct5at1am, fortnightly, 4);
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
     schedule.addEvent(event3, oct12at3pm);
 
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
     AccountEvent e = schedule.popNextEvent();
     assertEquals(event2, e);
 
     assertEquals(oct12at3pm, schedule.getEarliestEventTime());
     e = schedule.popNextEvent();
     assertEquals(event3, e);
 
     assertEquals(new DateTime(2012, 10, 19, 1, 0),
                  schedule.getEarliestEventTime());
     e = schedule.popNextEvent();
     assertEquals(event2, e);
 
     assertEquals(new DateTime(2012, 11, 2, 1, 0),
                  schedule.getEarliestEventTime());
     e = schedule.popNextEvent();
     assertEquals(event2, e);
 
     assertEquals(nov5at1am, schedule.getEarliestEventTime());
     e = schedule.popNextEvent();
     assertEquals(event1, e);
 
     assertEquals(new DateTime(2012, 11, 12, 1, 0),
                  schedule.getEarliestEventTime());
     e = schedule.popNextEvent();
     assertEquals(event1, e);
 
     assertEquals(new DateTime(2012, 11, 16, 1, 0),
                  schedule.getEarliestEventTime());
     e = schedule.popNextEvent();
     assertEquals(event2, e);
 
     assertFalse(schedule.hasMoreEvents());
     assertNull(schedule.getEarliestEventTime());
   }
 
 
 
   // Run events up to given date
 
   @Test
   public void testRunUntil() throws ScheduledEventExecutionException {
     ArrayList<Integer> trace = new ArrayList<Integer>();
       // trace will contain IDs of events that have run, in order
 
     AccountEvent e1 = new EventWithID(1, trace);
     AccountEvent e2 = new EventWithID(2, trace);
     AccountEvent e3 = new EventWithID(3, trace);
     schedule.addEvent(e1, nov5at1am, nov12at3pm, weekly);
     schedule.addEvent(e2, oct5at1am, fortnightly, 4);
     schedule.addEvent(e3, oct12at3pm);
 
     assertEquals(0, trace.size());
     assertEquals(oct5at1am, schedule.getEarliestEventTime());
     schedule.runEventsUntil(oct5at1am);
     assertEquals(0, trace.size());
 
     schedule.runEventsUntil(nov5at1am);
     assertEquals(4, trace.size());
     assertEquals(2, trace.get(0).intValue());
     assertEquals(3, trace.get(1).intValue());
     assertEquals(2, trace.get(2).intValue());
     assertEquals(2, trace.get(3).intValue());
     trace.clear();
 
     schedule.runEventsUntil(new DateTime(2012, 12, 1, 0, 0));
     assertEquals(3, trace.size());
     assertEquals(1, trace.get(0).intValue());
     assertEquals(1, trace.get(1).intValue());
     assertEquals(2, trace.get(2).intValue());
     trace.clear();
 
     assertFalse(schedule.hasMoreEvents());
     schedule.runEventsUntil(new DateTime(2012, 12, 31, 0, 0));
     assertEquals(0, trace.size());
   }
 }
