 package com.galineer.suzy.accountsim;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.chrono.CopticChronology;
 
 public class TestScheduledEvent {
 
   class EventTest implements AccountEvent {
     EventTest() {}
   }
 
   private AccountEvent event;
   private DateTime oct5at1am;
   private DateTime oct12at3pm;
   private DateTime nov5at1am;
   private DateTime nov12at3pm;
   private DateTime differentChronology;
   private Period daily;
   private Period weekly;
   private Period fortnightly;
   private Period monthly;
   private Period negativePeriod;
   private Period zeroPeriod;
 
   @Before
   public void setUp() {
     event = new EventTest();
     oct5at1am = new DateTime(2012, 10, 5, 1, 0);
     oct12at3pm = new DateTime(2012, 10, 12, 15, 0);
     nov5at1am = new DateTime(2012, 11, 5, 1, 0);
     nov12at3pm = new DateTime(2012, 11, 12, 15, 0);
     differentChronology = new DateTime(2012, 10, 12, 15, 0,
                                        CopticChronology.getInstance());
     daily = Period.days(1);
     weekly = Period.weeks(1);
     fortnightly = Period.weeks(2);
     monthly = Period.months(1);
     negativePeriod = Period.days(-1);
     zeroPeriod = Period.days(0);
   }
 
   @After
   public void tearDown() {
   }
 
   // Constructor specifying both start and end dates
 
   @Test
   public void testConstructorWithEndDateBasic() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, oct12at3pm, daily);
     assertEquals(event, s.getEvent());
     assertEquals(oct5at1am, s.getNextTime());
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithEndDateNullEvent() {
     ScheduledEvent s = new ScheduledEvent(null, oct5at1am, oct12at3pm, daily);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithEndDateNullStartDate() {
     ScheduledEvent s = new ScheduledEvent(event, null, oct12at3pm, daily);
   }
 
   @Test(expected=IllegalArgumentException.class)
  public void testConstructorWithEndDateStartDateDifferentChronology() {
    ScheduledEvent s = new ScheduledEvent(event, differentChronology,
                                          oct12at3pm, daily);
  }

  @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithEndDateNullEndDate() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, null, daily);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithEndDateBeforeStartDate() {
     ScheduledEvent s = new ScheduledEvent(event, oct12at3pm, oct5at1am, daily);
   }
 
   @Test(expected=IllegalArgumentException.class)
  public void testConstructorWithEndDateDifferentChronology() {
     ScheduledEvent s = new ScheduledEvent(event, oct12at3pm,
                                           differentChronology, daily);
   }
 
   @Test
   public void testConstructorWithEndDateNullPeriod() {
     // null period is fine, that just means the event doesn't repeat;
     // end date is redundant in this case
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, oct12at3pm, null);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithEndDateNegativePeriod() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, oct12at3pm,
                                          negativePeriod);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithEndDateZeroPeriod() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, oct12at3pm,
                                          zeroPeriod);
   }
 
   // Constructor specifying start date and number of repeats
 
   @Test
   public void testConstructorWithNumRepsBasic() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, weekly, 1);
     assertEquals(event, s.getEvent());
     assertEquals(oct5at1am, s.getNextTime());
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithNumRepsNullEvent() {
     ScheduledEvent s = new ScheduledEvent(null, oct5at1am, weekly, 1);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithNumRepsNullStartDate() {
     ScheduledEvent s = new ScheduledEvent(event, null, weekly, 1);
   }
 
   @Test(expected=IllegalArgumentException.class)
  public void testConstructorWithNumRepsStartDateDifferentChronology() {
    ScheduledEvent s = new ScheduledEvent(event, differentChronology,
                                          weekly, 1);
  }

  @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithNumRepsNegativePeriod() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, negativePeriod, 5);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithNumRepsZeroPeriod() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, zeroPeriod, 5);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithNumRepsZeroReps() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, daily, 0);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithNumRepsNegativeReps() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, daily, -1);
   }
 
   @Test
   public void testConstructorWithNumRepsNullPeriod() {
     // null period OK if event is non-repeating
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, null, 1);
   }
 
   @Test(expected=IllegalArgumentException.class)
   public void testConstructorWithNumRepsNullPeriodPositiveReps() {
     // null period not OK if event is to occur more than once
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, null, 2);
   }
 
   // ScheduledEvent comparsions
 
   @Test
   public void testComparisonDifferentDates() {
     ScheduledEvent s1 = new ScheduledEvent(event, oct5at1am, oct5at1am, null);
     ScheduledEvent s2 = new ScheduledEvent(event, oct12at3pm, oct12at3pm, null);
     assertEquals(-1, s1.compareTo(s2));
     assertEquals(1, s2.compareTo(s1));
   }
 
   @Test
   public void testComparisonOneNullDate() {
     // ScheduledEvent with null next date must come after one with non-null date
     ScheduledEvent s1 = new ScheduledEvent(event, oct5at1am, oct5at1am, null);
     ScheduledEvent s2 = new ScheduledEvent(event, oct12at3pm, oct12at3pm, null);
     s1.updateNextTime();
     assertNull(s1.getNextTime());
     assertEquals(1, s1.compareTo(s2));
     assertEquals(-1, s2.compareTo(s1));
   }
 
   @Test
   public void testComparisonBothNullDates() {
     // use random tiebreaker
     ScheduledEvent s1, s2;
     ScheduledEvent.initialiseGenerator(9043627L);
     s1 = new ScheduledEvent(event, oct5at1am, oct5at1am, null);
     s2 = new ScheduledEvent(event, oct12at3pm, oct12at3pm, null);
     s1.updateNextTime();
     s2.updateNextTime();
     assertNull(s1.getNextTime());
     assertNull(s2.getNextTime());
     assertEquals(1, s1.compareTo(s2));
 
     ScheduledEvent.initialiseGenerator(9043626L);
     s1 = new ScheduledEvent(event, oct5at1am, oct5at1am, null);
     s2 = new ScheduledEvent(event, oct12at3pm, oct12at3pm, null);
     s1.updateNextTime();
     s2.updateNextTime();
     assertNull(s1.getNextTime());
     assertNull(s2.getNextTime());
     assertEquals(-1, s1.compareTo(s2));
   }
 
   @Test
   public void testComparisonEqualDates() {
     // use random tiebreaker
     ScheduledEvent s1, s2;
     ScheduledEvent.initialiseGenerator(5312342L);
     s1 = new ScheduledEvent(event, oct5at1am, oct5at1am, null);
     s2 = new ScheduledEvent(event, oct5at1am, oct12at3pm, null);
     assertEquals(1, s1.compareTo(s2));
 
     ScheduledEvent.initialiseGenerator(5312343L);
     s1 = new ScheduledEvent(event, oct5at1am, oct5at1am, null);
     s2 = new ScheduledEvent(event, oct5at1am, oct12at3pm, null);
     assertEquals(-1, s1.compareTo(s2));
   }
 
   @Test
   public void testComparisonSelf() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, oct5at1am, null);
     assertEquals(0, s.compareTo(s));
   }
 
   @Test(expected=NullPointerException.class)
   public void testComparisonNull() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, oct5at1am, null);
     s.compareTo(null);
   }
 
   @Test(expected=ArithmeticException.class)
   public void testComparisonSameTiebreaker() {
     ScheduledEvent.initialiseGenerator(789325382L);
     ScheduledEvent s1 = new ScheduledEvent(event, oct5at1am, oct5at1am, null);
     ScheduledEvent.initialiseGenerator(789325382L);
     ScheduledEvent s2 = new ScheduledEvent(event, oct12at3pm, oct12at3pm, null);
     s1.compareTo(s2);
   }
 
   // Updating time of ScheduledEvent next occurrence
 
   @Test
   public void testUpdateTimeSameStartEndNullPeriod() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, oct5at1am, null);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
   }
 
   @Test
   public void testUpdateTimeRemainsNull() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, oct5at1am, null);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
   }
 
   @Test
   public void testUpdateTimeSameStartEndWithPeriod() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, oct5at1am, daily);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
   }
 
   @Test
   public void testUpdateTimeDifferentEndNullPeriod() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, oct12at3pm, null);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
   }
 
   @Test
   public void testUpdateTimeDifferentEndWithTooLongPeriod() {
     // event only occurs once because period is too long with respect to
     // start/end dates
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, oct12at3pm,
                                           monthly);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
   }
 
   @Test
   public void testUpdateTimeOneRepeatNullPeriod() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, null, 1);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
   }
 
   @Test
   public void testUpdateTimeOneRepeatWithPeriod() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, weekly, 1);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
   }
 
   @Test
   public void testUpdateTimeTwoEventsEndDateExact() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, nov5at1am, monthly);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertEquals(nov5at1am, s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
   }
 
   @Test
   public void testUpdateTimeTwoEventsEndDateLate() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, nov12at3pm,
                                           monthly);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertEquals(nov5at1am, s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
   }
 
   @Test
   public void testUpdateTimeTwoEventsNumReps() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, monthly, 2);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertEquals(nov5at1am, s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
   }
 
   @Test
   public void testUpdateTimeComparingWeeklyFortnightlyMonthly() {
     ScheduledEvent s = new ScheduledEvent(event, oct5at1am, weekly, 5);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertEquals(new DateTime(2012, 10, 12, 1, 0), s.getNextTime());
     s.updateNextTime();
     assertEquals(new DateTime(2012, 10, 19, 1, 0), s.getNextTime());
     s.updateNextTime();
     assertEquals(new DateTime(2012, 10, 26, 1, 0), s.getNextTime());
     s.updateNextTime();
     assertEquals(new DateTime(2012, 11, 2, 1, 0), s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
 
     s = new ScheduledEvent(event, oct5at1am, fortnightly, 3);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertEquals(new DateTime(2012, 10, 19, 1, 0), s.getNextTime());
     s.updateNextTime();
     assertEquals(new DateTime(2012, 11, 2, 1, 0), s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
 
     s = new ScheduledEvent(event, oct5at1am, monthly, 2);
     assertEquals(oct5at1am, s.getNextTime());
     s.updateNextTime();
     assertEquals(nov5at1am, s.getNextTime());
     s.updateNextTime();
     assertNull(s.getNextTime());
   }
 
   @Test
   public void testUpdateTimeTiebreaker() {
     // tiebreaker should reset each time ScheduledEvent's next time is updated
     ScheduledEvent.initialiseGenerator(3235238L);
     ScheduledEvent s1 = new ScheduledEvent(event, oct5at1am, daily, 3);
     ScheduledEvent s2 = new ScheduledEvent(event, oct5at1am, daily, 5);
     assertEquals(1, s1.compareTo(s2));
     s1.updateNextTime();
     s2.updateNextTime();
     assertEquals(new DateTime(2012, 10, 6, 1, 0), s1.getNextTime());
     assertEquals(new DateTime(2012, 10, 6, 1, 0), s2.getNextTime());
     assertEquals(-1, s1.compareTo(s2));
     s1.updateNextTime();
     s2.updateNextTime();
     assertEquals(new DateTime(2012, 10, 7, 1, 0), s1.getNextTime());
     assertEquals(new DateTime(2012, 10, 7, 1, 0), s2.getNextTime());
     assertEquals(1, s1.compareTo(s2));
 
     // this is also true when it is updated to null for the first time,
     // but not the second time (after next time is already null)
     ScheduledEvent.initialiseGenerator(3235238L);
     s1 = new ScheduledEvent(event, oct5at1am, daily, 1);
     s2 = new ScheduledEvent(event, oct5at1am, daily, 1);
     assertEquals(1, s1.compareTo(s2));
     s1.updateNextTime();
     s2.updateNextTime();
     assertNull(s1.getNextTime());
     assertNull(s1.getNextTime());
     assertEquals(-1, s1.compareTo(s2));
     s1.updateNextTime();
     s2.updateNextTime();
     assertNull(s1.getNextTime());
     assertNull(s1.getNextTime());
     assertEquals(-1, s1.compareTo(s2));
   }
 }
