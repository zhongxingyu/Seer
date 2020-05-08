 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class CalendarUtil {
 
 	private static java.util.Calendar cal = java.util.Calendar.getInstance();
 	private static CalendarUtil util;
 	
 	private CalendarUtil(){
 		cal.set(cal.DATE, 1);
 	}
 	
 	/**
 	 * Months are written in <code>int</code>'s from 0 to 11. <br>
 	 *	TODO: form 1 to 12 please!
 	 * Example: June would be <code> 5. </code>
 	 * Wrong input is not handled.
 	 * 
 	 * @param month
 	 */
 	public static CalendarUtil getInstanceToday(){
 		util = new CalendarUtil();
 		return util;	
 	}
 	
 	/**
 	 * Returns the current month as <code> int</code>, starting <br>
 	 * with <code>0</code> (January) and ending with <code> 11</code> (December).<br>
 	 * 
 	 * @return month
 	 */
 	public int getMonth(){
 		int month = cal.get(cal.MONTH) + 1;
 		return month;
 	}
 	
 	public int getYear(){
 		return cal.get(cal.YEAR);
 	}
 	/**
 	 * Sets month within the same year.<br>
 	 */
 	public void setMonth(int month){
 		cal.set(cal.MONTH, (month-1));
 	}
 	/**
 	 * Sets the default time stamp one month ahead.
 	 */
 	public void nextMonth(){
 		cal.add(cal.MONTH, 1);
 	}
 	
 	/**
 	 * Sets the default time stamp one month back.
 	 */
 	public void previousMonth(){
 		cal.add(cal.MONTH, -1);
 	}
 	
 	public int getNumberOfDaysOfMonth(){
 		return cal.getActualMaximum(cal.DATE);
 	}
 	
 	/**
 	 * Returns the first weekday of this month as <code> int</code>. <br>
 	 * Monday is the 1. and so on till Sunday as the 7. day.<br>
 	 * For example, if Tuesday is the first day in a month, then <code> 3 </code> is returned.
 	 * 
 	 * @return weekday
 	 */
 	public int getFirstDayInMonth(){
		int temp = cal.DAY_OF_WEEK;
 		temp = (temp + 6)%7;
 		return temp;
			
 	}
 
 	public List<Integer> getLastMonthDates() {
 		cal.set(cal.MONTH, -1);
 		List<Integer> lastMonthTemp = new ArrayList<Integer>();
 		lastMonthTemp = getThisMonthDates();		
 		cal.set(cal.MONTH, 1);
 		
 		List<Integer> lastMonth = new ArrayList<Integer>();
 		int firstDayOfCurrentMonth = getFirstDayInMonth();
 		for(int i = firstDayOfCurrentMonth; i>0; i--){
 			lastMonth.add(lastMonthTemp.get(i-1));
 		}
 		
 		return lastMonth;
 	}
 
 	public List<Integer> getThisMonthDates() {
 		List<Integer> currentMonth = new ArrayList<Integer>();
 		int noOfDays = getNumberOfDaysOfMonth();
 		for(int i = 1; i<= noOfDays; ++i){
 			currentMonth.add(i);
 		}
 		return currentMonth;
 	}
 
 	public List<Integer> getNextMonthDates() {
 		
 		cal.set(cal.MONTH, 1);
 		List<Integer> nextMonthTemp = new ArrayList<Integer>();
 		nextMonthTemp = getThisMonthDates();
 		int lastDayNextMonth = getFirstDayInMonth() - 1;
 		cal.set(cal.MONTH, -1);
 		
 		List<Integer> nextMonth = new ArrayList<Integer>();
 		for(int i = 0; i<lastDayNextMonth; i++){
 			nextMonth.add(nextMonthTemp.get(i));
 		}
 				
 		return nextMonth;
 	}
 	
 	public String getTitle(){
 		Date date = cal.getTime();
 		String title = String.format("%tB %tY", date, date);
 		return title;
 	}
	//for testing only
	public void setAtDate(int year, int month, int date){
		cal.set(year, month-1, date);
	}
 }	
