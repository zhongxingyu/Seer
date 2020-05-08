 package com.jbalboni.vacation.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.LocalDate;
 
 import android.test.AndroidTestCase;
 
 import com.jbalboni.vacation.LeaveCapType;
 import com.jbalboni.vacation.VacationTracker;
 import com.jbalboni.vacation.LeaveItem;
 
 public class VacationTrackerTest extends AndroidTestCase {
 	public VacationTrackerTest() {
 		super();
 	}
 
 	public void testInitial() {
 		float initialHours = 6;
 		VacationTracker tracker = new VacationTracker(new LocalDate(), new ArrayList<LeaveItem>(), 80.0f, initialHours, "Daily", true, LeaveCapType.NONE,0);
 		assertEquals(tracker.calculateHours(new LocalDate()), initialHours);
 	}
 
 	public void testAccrualOff() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 80;
 		String leaveInterval = "Daily";
 		boolean accrualOn = false;
 		LocalDate startDate = new LocalDate();
 		float leaveCap = 0;
 		LeaveCapType leaveCapType = LeaveCapType.NONE;
 		
 		startDate = startDate.minusMonths(1);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours(new LocalDate()), hoursPerYear);
 	}
 
 	public void testAccrualOn() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 120;
 		String leaveInterval = "Daily";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate();
 		float leaveCap = 0;
 		LeaveCapType leaveCapType = LeaveCapType.NONE;
 
 		startDate = startDate.minusYears(1);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours((new LocalDate()).minusDays(1)), hoursPerYear);
 	}
 
 	public void testHoursUsed() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		hoursUsed.add(new LeaveItem((new LocalDate()).minusDays(2),17));
 		float hoursPerYear = 120;
 		String leaveInterval = "Daily";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate();
 		float leaveCap = 0;
 		LeaveCapType leaveCapType = LeaveCapType.NONE;
 
 		startDate = startDate.minusYears(1);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(hoursPerYear - (hoursUsed.get(0)).getHours(),tracker.calculateHours((new LocalDate()).minusDays(1)));
 	}
 
 	public void testWeekly() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 52;
 		String leaveInterval = "Weekly";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate();
 		float leaveCap = 0;
 		LeaveCapType leaveCapType = LeaveCapType.NONE;
 
 		startDate = startDate.minusWeeks(1).plusDays(1);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours(new LocalDate()), initialHours);
 
 		tracker.setStartDate(startDate.minusDays(1));
 		assertEquals(tracker.calculateHours(new LocalDate()), hoursPerYear / 52.0f);
 	}
 
 	public void testMonthly() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 12;
 		String leaveInterval = "Monthly";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate();
 		float leaveCap = 0;
 		LeaveCapType leaveCapType = LeaveCapType.NONE;
 
 		startDate = startDate.minusMonths(1).plusDays(1);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours(new LocalDate()), initialHours);
 
 		tracker.setStartDate(startDate.minusDays(1));
 		assertEquals(tracker.calculateHours(new LocalDate()), hoursPerYear / 12.0f);
 	}
 
 	public void testBiWeekly() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 26;
 		String leaveInterval = "Bi-Weekly";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate();
 		float leaveCap = 0;
 		LeaveCapType leaveCapType = LeaveCapType.NONE;
 
 		startDate = startDate.minusWeeks(2).plusDays(1);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours(new LocalDate()), initialHours);
 
 		tracker.setStartDate(startDate.minusDays(1));
 		assertEquals(tracker.calculateHours(new LocalDate()), hoursPerYear / 26.0f);
 	}
 	
 	public void testMaxLeaveCap() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 120;
 		String leaveInterval = "Daily";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate();
 		float leaveCap = 100;
 		LeaveCapType leaveCapType = LeaveCapType.MAX;
 
 		startDate = startDate.minusYears(1);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours(new LocalDate()), leaveCap);
 	}
 	public void testCarryoverInitial() {
 		float initialHours = 60;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 120;
 		String leaveInterval = "Monthly";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate(2011,12,2);
 		float leaveCap = 40;
 		LeaveCapType leaveCapType = LeaveCapType.CARRYOVER;
 
 		LocalDate endDate = new LocalDate(2012,2,2);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours(endDate), 50f);
 	}
 	public void testCarryover() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 120;
 		String leaveInterval = "Monthly";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate(2011,12,2);
 		float leaveCap = 5;
 		LeaveCapType leaveCapType = LeaveCapType.CARRYOVER;
 
 		LocalDate endDate = new LocalDate(2012,2,2);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours(endDate), 15f);
 	}
 	public void testCarryoverDaily() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 365;
 		String leaveInterval = "Daily";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate(2011,12,2);
 		float leaveCap = 5;
 		LeaveCapType leaveCapType = LeaveCapType.CARRYOVER;
 
 		LocalDate endDate = new LocalDate(2012,2,2);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours(endDate), 38f);
 	}
 	public void testCarryoverWeekly() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 52;
 		String leaveInterval = "Weekly";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate(2011,12,2);
 		float leaveCap = 4;
 		LeaveCapType leaveCapType = LeaveCapType.CARRYOVER;
 
 		LocalDate endDate = new LocalDate(2012,2,3);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours(endDate), 8f);
 	}
 	public void testCarryoverBiWeekly() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 52;
 		String leaveInterval = "Bi-Weekly";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate(2011,12,2);
 		float leaveCap = 2;
 		LeaveCapType leaveCapType = LeaveCapType.CARRYOVER;
 
 		LocalDate endDate = new LocalDate(2012,2,3);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours(endDate), 4f);
 	}
 	public void testCarryoverTwoYear() {
 		float initialHours = 0;
 		List<LeaveItem> hoursUsed = new ArrayList<LeaveItem>();
 		float hoursPerYear = 120;
 		String leaveInterval = "Monthly";
 		boolean accrualOn = true;
 		LocalDate startDate = new LocalDate(2010,1,2);
 		float leaveCap = 15;
 		LeaveCapType leaveCapType = LeaveCapType.CARRYOVER;
 
 		LocalDate endDate = new LocalDate(2012,3,2);
 
 		VacationTracker tracker = new VacationTracker(startDate, hoursUsed, hoursPerYear, initialHours, leaveInterval,
 				accrualOn, leaveCapType, leaveCap);
 		assertEquals(tracker.calculateHours(endDate), 35f);
 	}
 }
