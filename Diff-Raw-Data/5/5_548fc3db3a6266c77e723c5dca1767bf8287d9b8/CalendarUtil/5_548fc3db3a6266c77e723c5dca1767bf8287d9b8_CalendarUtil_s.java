 package models;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 public class CalendarUtil {
 
	private java.util.Calendar cal = java.util.Calendar.getInstance();
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
 		cal.set(cal.DAY_OF_MONTH, 1);
 		System.out.println("Set to the first of month: " + cal.getTime().toString());
 		int temp = cal.get(cal.DAY_OF_WEEK);
 		temp = ((temp + 5)%7)+1;
 		System.out.println("Get the weekday of the first: " + temp);
 		return temp;
 			
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
 		
 		int lastDay = cal.getActualMaximum(cal.DATE);
 		System.out.println("Last day in this month: "+lastDay);
 		cal.set(cal.DAY_OF_MONTH, lastDay);
 		int lastDayOfWeek = (((cal.get(cal.DAY_OF_WEEK))+5)%7)+1;
 		cal.set(cal.DAY_OF_MONTH, 1);	//setting default
 		int remainingDays = 7-lastDayOfWeek;
 		
 		List<Integer> nextMonth = new ArrayList<Integer>();
 		for(int i = 1; i<= remainingDays; i++){
 			nextMonth.add((Integer)i);
 		}	
 		return nextMonth;
 	}
 	
 
 	public List<Integer> getLastMonthDates() {
 		
 		int firstDay = getFirstDayInMonth();
 		cal.set(cal.DAY_OF_MONTH, 1);
 		cal.add(cal.DAY_OF_MONTH, -1);
 
 		int lastDayOflastMonth = cal.get(cal.DAY_OF_MONTH);	
 		cal.add(cal.DAY_OF_WEEK, 1);
 		
 		List<Integer> lastMonth = new ArrayList<Integer>();
 		for(int i = 1; i < firstDay; i++){
 			lastMonth.add((Integer)lastDayOflastMonth);
 			lastDayOflastMonth--;
 		}
 		Collections.reverse(lastMonth);
 		return lastMonth;
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
 	
 	public boolean isToday(int dayOfMonth, int month, int year){
 		java.util.Calendar temp = java.util.Calendar.getInstance();
 		int currentDay = temp.get(temp.DAY_OF_MONTH);
		int currentMonth = temp.get(temp.MONTH);
 		int currentYear = temp.get(temp.YEAR);
 		return (currentDay == dayOfMonth) && (currentMonth == month) && (currentYear == year);
 	}
 	
 	private int startMonday(int startSunday){
 		return ((startSunday+5)%7)+1;
 	}
 	
 
 }	
