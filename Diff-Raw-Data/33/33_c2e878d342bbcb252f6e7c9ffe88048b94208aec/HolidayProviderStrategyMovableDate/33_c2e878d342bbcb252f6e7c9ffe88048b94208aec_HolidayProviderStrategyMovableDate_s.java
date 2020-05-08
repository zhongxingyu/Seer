 package de.z35.commons.collections.exam.freedays;
 
 import java.util.Calendar;
 
 /**
  * Many thanks to 'http://michaelthompson.org/technikos/holidays.php' 
  * for his inspirations
  * 
  * @author ufuchs
  *
  */
 public class HolidayProviderStrategyMovableDate implements HolidayProviderStrategy {
 
 	static int MONTH = 0;
 	static int OCCURRENCE_IN_MONTH = 1;
 	static int DAY_OF_WEEK = 2;
 	
 	private int year;
 	
 	// params from 'dateTemplate'
 	
 	private int month;
 	
 	/**
 	 * the number of the occurrence of the target day
 	 */
 	private int occurenceInMonth;
 	
 	private int dayOfWeek;
 	
 	/**
 	 * 
 	 * @param dateTemplate like 'M2.3.1'
 	 * @return
 	 */
 	public void setParams(int year, String dateTemplate) {
 		
 		this.year = year;
 		
 		// drops the leading character 'M'
 		String template = dateTemplate.substring(1);
 
 		// splits into single figures, e.g '1.2.0' to {"1","2","0"}
 		String[] parts = template.split("\\.");
 
 		// the first digit represents the month
 		this.month = Integer.parseInt(parts[MONTH]) - 1;
 		
 		// the second digit represents the occurrence of the target day
 		this.occurenceInMonth = Integer.parseInt(parts[OCCURRENCE_IN_MONTH]);
 		
 		// the last digit represents the day of the week.
 		// It starts with Sunday = 1 and ends with Saturday = 7
 		this.dayOfWeek = Integer.parseInt(parts[DAY_OF_WEEK]);
 		
 	}
 		
 	/**
 	 *
 	 */
 	@Override
 	public String transformTemplate() {
 
 		Calendar cal = Calendar.getInstance();
 
 		cal.set(Calendar.YEAR, this.year);
 		cal.set(Calendar.MONTH, this.month);
 		cal.set(Calendar.DAY_OF_MONTH, 1);
 		
		int earliestDay = getEarliestDay(); 
 		
		int offs = calculateOffset(cal.get(Calendar.DAY_OF_WEEK), this.dayOfWeek);
 		
 		int dayOfMonth = earliestDay + offs;
 		
 	    if (this.occurenceInMonth > 4) {
 	    	dayOfMonth += getOffsetToLastOccurrence(cal, dayOfMonth);
 	    }
 	    
 	    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
 
 		return DateTimeUtils.calToString(cal);
 
 	}
 
 	/**
 	 * 
 	 * @param cal
 	 * @param earliestDayInMonth
 	 * @return
 	 */
 	private int getOffsetToLastOccurrence(Calendar cal, int earliestDayInMonth) {
 		
 		int offset = 0;
 		
 	    int daysOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
 	    
 	    if ((earliestDayInMonth + 7) <= daysOfMonth) {
 	    	offset = 7;
 	    }
 		
 		return offset;
 		
 	}
 
 	/**
 	 * Related to the day when month starts with the same day of week.
 	 * To get the real day 6:1 cases there is an offset needed.
 	 * @return
 	 */
	private int getEarliestDay() {
 		
 		int subtrahend = 1;
 		
 		if (this.occurenceInMonth > 4) {
 			subtrahend = 2;
 		}
  
		int earliestDay = (this.dayOfWeek - 1) 
			+ 7 * (this.occurenceInMonth - subtrahend);
 		
 	    return earliestDay;
 		
 	}
 
 	/**
 	 * 
 	 * [Citation by Michael Thompson ]
 	 * "Find the offset between the target weekday and weekday of the earliest 
 	 *  possible date in the given year"
 	 * 
 	 * @param cal
 	 * @see http://michaelthompson.org/technikos/holidays.php
 	 * @return
 	 */
 	private int calculateOffset(int monthStartsOnDay, int dayOfWeek) {
	    
	    // 'java.util.Calendar' related stuff
	    monthStartsOnDay -= 1;
	    dayOfWeek = dayOfWeek - 1;
 
 	    int offset = 0;
 	    
 		if (dayOfWeek != monthStartsOnDay) {
 			
 			offset = dayOfWeek - monthStartsOnDay + 7;
 			
 			if (dayOfWeek < monthStartsOnDay) {
 				// offset = dayOfWeek + -7 + monthStartsOnDay ;
 			} else {
 				offset = ((dayOfWeek + (7 - monthStartsOnDay)) - 7);
 				offset = dayOfWeek - monthStartsOnDay + 7 - 7;
 			}
 			
 		}
 		
 		return offset;
 		
 	}
 
 }
