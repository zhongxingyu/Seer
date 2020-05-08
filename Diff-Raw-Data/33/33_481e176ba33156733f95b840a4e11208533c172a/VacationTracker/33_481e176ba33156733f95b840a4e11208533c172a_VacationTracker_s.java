 package com.jbalboni.vacation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTimeConstants;
 import org.joda.time.Days;
 import org.joda.time.LocalDate;
 import org.joda.time.Months;
 import org.joda.time.Weeks;
 
 /*
  * Main logic class. Does the leave calculation based on used hours, accrued leave, and limit
  */
 public class VacationTracker {
 	private static final int WEEKS_IN_YEAR = 52;
 	private static final int MONTHS_IN_YEAR = 12;
 	private static final int DAYS_IN_YEAR = 365;
 
 	private LocalDate startDate;
 	private List<LeaveItem> history;
 	private float hoursPerYear;
 	private float initialHours;
 	private String leaveInterval;
 	private boolean accrualOn;
 	private LeaveCapType leaveCapType;
 	private float leaveCap;
 
 	public VacationTracker() {
 	}
 
 	public VacationTracker(LocalDate startDate, List<LeaveItem> history, float hoursPerYear, float initialHours,
 			String leaveInterval, boolean accrualOn, LeaveCapType leaveCapType, float leaveCap) {
 		this.startDate = startDate;
 		this.history = history;
 		this.hoursPerYear = hoursPerYear;
 		this.initialHours = initialHours;
 		this.leaveInterval = leaveInterval;
 		this.accrualOn = accrualOn;
 		this.leaveCap = leaveCap;
 		this.leaveCapType = leaveCapType;
 	}
 
 	public float calculateHours(LocalDate asOfDate) {
 		int interval = 0;
 		int historyIndex = 0;
 		//remove before and after leave items
 		List<LeaveItem> trimmedHist = trimLeave(asOfDate);
 
 		float vacationHours = 0;
 		vacationHours = initialHours;
 		
 		if (this.accrualOn == true) {
 			if (leaveInterval.equals("Daily")) {
 				LocalDate currentDate = startDate;
 				while (currentDate.isBefore(asOfDate)) {
 					float tempVacationHours;
 					LocalDate endOfYear = currentDate.plusDays(1).withMonthOfYear(DateTimeConstants.DECEMBER)
 							.withDayOfMonth(31);
 					if (endOfYear.isBefore(asOfDate)) {
 						Days days = Days.daysBetween(currentDate, endOfYear);
 						interval = days.getDays();
 						SumAndPos currentUsed = sumLeave(trimmedHist,historyIndex,endOfYear);
 						historyIndex = currentUsed.pos;
 						tempVacationHours = (interval * (hoursPerYear / DAYS_IN_YEAR)) - currentUsed.sum;
 						if (leaveCapType == LeaveCapType.CARRYOVER && tempVacationHours + vacationHours > leaveCap) {
 							vacationHours = leaveCap;
 						} else {
 							vacationHours += tempVacationHours;
 						}
 					} else {
 						Days days = Days.daysBetween(currentDate, asOfDate);
 						interval = days.getDays();
 						SumAndPos currentUsed = sumLeave(trimmedHist,historyIndex,asOfDate);
 						vacationHours += (interval * (hoursPerYear / DAYS_IN_YEAR)) - currentUsed.sum;
 						historyIndex = currentUsed.pos;
 					}
 					currentDate = endOfYear;
 				}
 			} else if (leaveInterval.equals("Weekly")) {
 				LocalDate currentDate = startDate;
 				while (currentDate.isBefore(asOfDate)) {
 					float tempVacationHours;
 					LocalDate endOfYear = currentDate.plusDays(1).withMonthOfYear(DateTimeConstants.DECEMBER)
 							.withDayOfMonth(31);
 					LocalDate newEndOfYear = endOfYear.withDayOfWeek(startDate.getDayOfWeek());
 					if (endOfYear.isAfter(newEndOfYear))
 						endOfYear = newEndOfYear.plusWeeks(1);
 					else
 						endOfYear = newEndOfYear;
 					if (endOfYear.isBefore(asOfDate)) {
 						Weeks weeks = Weeks.weeksBetween(currentDate, endOfYear);
 						interval = weeks.getWeeks();
 						SumAndPos currentUsed = sumLeave(trimmedHist,historyIndex,endOfYear);
 						historyIndex = currentUsed.pos;
 						tempVacationHours = (interval * (hoursPerYear / WEEKS_IN_YEAR)) - currentUsed.sum;
 						if (leaveCapType == LeaveCapType.CARRYOVER && tempVacationHours + vacationHours > leaveCap) {
 							vacationHours = leaveCap;
 						} else {
 							vacationHours += tempVacationHours;
 						}
 					} else {
 						Weeks weeks = Weeks.weeksBetween(currentDate, asOfDate);
 						interval = weeks.getWeeks();
						SumAndPos currentUsed = sumLeave(trimmedHist,historyIndex,endOfYear);
 						historyIndex = currentUsed.pos;
 						vacationHours += (interval * (hoursPerYear / WEEKS_IN_YEAR)) - currentUsed.sum;
 					}
 					currentDate = endOfYear;
 				}
 			} else if (leaveInterval.equals("Bi-Weekly")) {
 				LocalDate currentDate = startDate;
 				while (currentDate.isBefore(asOfDate)) {
 					float tempVacationHours;
 					LocalDate endOfYear = currentDate.plusDays(1).withMonthOfYear(DateTimeConstants.DECEMBER)
 							.withDayOfMonth(31);
 					LocalDate newEndOfYear = endOfYear.withDayOfWeek(startDate.getDayOfWeek());
 					if (endOfYear.isAfter(newEndOfYear))
 						endOfYear = newEndOfYear.plusWeeks(1);
 					else
 						endOfYear = newEndOfYear;
 					if (Weeks.weeksBetween(startDate, endOfYear).getWeeks() % 2 != 0)
 						endOfYear = endOfYear.plusWeeks(1);
 					if (endOfYear.isBefore(asOfDate)) {
 						Weeks weeks = Weeks.weeksBetween(currentDate, endOfYear);
 						interval = weeks.getWeeks();
 						SumAndPos currentUsed = sumLeave(trimmedHist,historyIndex,endOfYear);
 						historyIndex = currentUsed.pos;
 						tempVacationHours = (interval * (hoursPerYear / WEEKS_IN_YEAR)) - currentUsed.sum;
 						if (leaveCapType == LeaveCapType.CARRYOVER && tempVacationHours + vacationHours > leaveCap) {
 							vacationHours = leaveCap;
 						} else {
 							vacationHours += tempVacationHours;
 						}
 					} else {
 						Weeks weeks = Weeks.weeksBetween(currentDate, asOfDate);
 						interval = weeks.getWeeks() / 2;
						SumAndPos currentUsed = sumLeave(trimmedHist,historyIndex,endOfYear);
 						historyIndex = currentUsed.pos;
 						vacationHours += (interval * (hoursPerYear / (WEEKS_IN_YEAR / 2))) - currentUsed.sum;
 					}
 					currentDate = endOfYear;
 				}
 			} else if (leaveInterval.equals("Monthly")) {
 				LocalDate currentDate = startDate;
 				while (currentDate.isBefore(asOfDate)) {
 					float tempVacationHours;
 					LocalDate endOfYear = currentDate.plusDays(1).withMonthOfYear(DateTimeConstants.DECEMBER)
 							.withDayOfMonth(31);
 					LocalDate newEndOfYear = endOfYear.withDayOfMonth(startDate.getDayOfMonth());
 					if (endOfYear.isAfter(newEndOfYear))
 						endOfYear = newEndOfYear.plusMonths(1);
 					else
 						endOfYear = newEndOfYear;
 					if (endOfYear.isBefore(asOfDate)) {
 						Months months = Months.monthsBetween(currentDate, endOfYear);
 						interval = months.getMonths();
 						SumAndPos currentUsed = sumLeave(trimmedHist,historyIndex,endOfYear);
 						historyIndex = currentUsed.pos;
 						tempVacationHours = (interval * (hoursPerYear / MONTHS_IN_YEAR)) - currentUsed.sum;
 						if (leaveCapType == LeaveCapType.CARRYOVER && tempVacationHours + vacationHours > leaveCap) {
 							vacationHours = leaveCap;
 						} else {
 							vacationHours += tempVacationHours;
 						}
 					} else {
 						Months months = Months.monthsBetween(currentDate, asOfDate);
 						interval = months.getMonths();
						SumAndPos currentUsed = sumLeave(trimmedHist,historyIndex,endOfYear);
 						historyIndex = currentUsed.pos;
 						vacationHours += (interval * (hoursPerYear / MONTHS_IN_YEAR)) - currentUsed.sum;
 					}
 					currentDate = endOfYear;
 				}
 			}
 		} else {
 			vacationHours += hoursPerYear;
 		}
 		if (leaveCapType == LeaveCapType.MAX && vacationHours > leaveCap)
 			return leaveCap;
 		else
 			return vacationHours;
 	}
 	
 	/*
 	 * Stupid Java
 	 */
 	private static class SumAndPos {
 		public int pos;
 		public float sum;
 		public SumAndPos(int pos, float sum) {
 			this.pos = pos;
 			this.sum = sum;
 		}
 	}
 	
 	private SumAndPos sumLeave(List<LeaveItem> list, int start, LocalDate endDate) {
 		float leaveSum = 0;
 		int i = start;
		while (i < list.size() && !endDate.isAfter(list.get(i).getDate())) {
 			leaveSum += list.get(i).getHours();
 			i++;
 		}
 		return new SumAndPos(i,leaveSum);
 	}
 
 	/*
 	 * We don't need to count leave outside of the start date and as of date.
 	 */
 	private List<LeaveItem> trimLeave(LocalDate asOfDate) {
 		List<LeaveItem> hist = new ArrayList<LeaveItem>();
 		for (LeaveItem item : history) {
 			if (!(item.getDate().isBefore(startDate) || item.getDate().isAfter(asOfDate))) {
 				hist.add(item);
 			}
 		}
 		return hist;
 	}
 
 	public LeaveCapType getLeaveCapType() {
 		return leaveCapType;
 	}
 
 	public void setLeaveCapType(LeaveCapType leaveCapType) {
 		this.leaveCapType = leaveCapType;
 	}
 
 	public float getLeaveCap() {
 		return leaveCap;
 	}
 
 	public void setLeaveCap(float leaveCap) {
 		this.leaveCap = leaveCap;
 	}
 
 	public void setStartDate(LocalDate startDate) {
 		this.startDate = startDate;
 	}
 
 	public LocalDate getStartDate() {
 		return startDate;
 	}
 
 	public float getHoursPerYear() {
 		return hoursPerYear;
 	}
 
 	public void setHoursPerYear(float hoursPerYear) {
 		this.hoursPerYear = hoursPerYear;
 	}
 
 	public float getInitialHours() {
 		return initialHours;
 	}
 
 	public void setInitialHours(float initialHours) {
 		this.initialHours = initialHours;
 	}
 
 	public boolean isAccrualOn() {
 		return accrualOn;
 	}
 
 	public String getLeaveInterval() {
 		return leaveInterval;
 	}
 
 	public void setLeaveInterval(String leaveInterval) {
 		this.leaveInterval = leaveInterval;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (accrualOn ? 1231 : 1237);
 		result = prime * result + ((history == null) ? 0 : history.hashCode());
 		result = prime * result + Float.floatToIntBits(hoursPerYear);
 		result = prime * result + Float.floatToIntBits(initialHours);
 		result = prime * result + Float.floatToIntBits(leaveCap);
 		result = prime * result + ((leaveCapType == null) ? 0 : leaveCapType.hashCode());
 		result = prime * result + ((leaveInterval == null) ? 0 : leaveInterval.hashCode());
 		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof VacationTracker))
 			return false;
 		VacationTracker other = (VacationTracker) obj;
 		if (accrualOn != other.accrualOn)
 			return false;
 		if (history == null) {
 			if (other.history != null)
 				return false;
 		} else if (!history.equals(other.history))
 			return false;
 		if (Float.floatToIntBits(hoursPerYear) != Float.floatToIntBits(other.hoursPerYear))
 			return false;
 		if (Float.floatToIntBits(initialHours) != Float.floatToIntBits(other.initialHours))
 			return false;
 		if (Float.floatToIntBits(leaveCap) != Float.floatToIntBits(other.leaveCap))
 			return false;
 		if (leaveCapType != other.leaveCapType)
 			return false;
 		if (leaveInterval == null) {
 			if (other.leaveInterval != null)
 				return false;
 		} else if (!leaveInterval.equals(other.leaveInterval))
 			return false;
 		if (startDate == null) {
 			if (other.startDate != null)
 				return false;
 		} else if (!startDate.equals(other.startDate))
 			return false;
 		return true;
 	}
 
 	public void addHoursUsed(Float hoursUsed) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
