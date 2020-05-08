 package org.fao.fi.vme.msaccess.formatter;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
import java.util.Locale;
 
 import org.fao.fi.vme.VmeException;
 
 /**
  * Tony Thompson used a specific convention for noting the start to end dates. This logic reflects that convention and
  * parses the dates.
  * 
  * @author Erik van Ingen
  * 
  */
 public class MeetingDateParser {
 
 	private Date start;
 	private Date end;
 
 	/**
 	 * 17-21 Sep 2012
 	 * 
 	 * @param date
 	 */
 
 	public MeetingDateParser(String date) {
 
 		String values[] = determineValues(date);
 		String months[] = determineMonths(date);
 
 		String startDate = "";
 		String endDate = "";
 
 		if (date.length() >= 12 && date.length() <= 15) {
 			// same month
 			startDate = correctDayNumber(values[0]) + months[0] + values[2];
 			endDate = correctDayNumber(values[1]) + months[0] + values[2];
 
 		}
 		if (date.length() >= 19 && date.length() <= 21) {
 			// different months
 			startDate = correctDayNumber(values[0]) + months[0] + values[2];
 			endDate = correctDayNumber(values[1]) + months[1] + values[2];
 		}
 
		SimpleDateFormat sf = new SimpleDateFormat("ddMMMyyyy", Locale.ENGLISH);
 		try {
 			this.start = sf.parse(startDate);
 			this.end = sf.parse(endDate);
 		} catch (ParseException e) {
 			throw new VmeException(e);
 		}
 
 	}
 
 	private String correctDayNumber(String value) {
 		if (value.length() == 1) {
 			value = " " + value;
 		}
 		return value;
 	}
 
 	private String[] determineMonths(String date) {
 		String month = "";
 		String startMonth = "";
 		String endMonth = "";
 		for (int i = 0; i < date.length(); i++) {
 			String character = date.substring(i, i + 1);
 
 			if (character.matches("[a-zA-Z]")) {
 				month = month + character;
 				if (month.length() == 3 && startMonth.length() == 0) {
 					startMonth = month;
 					month = "";
 				}
 				if (startMonth.length() == 3 && month.length() == 3) {
 					endMonth = month;
 				}
 			}
 		}
 		if (endMonth.length() == 3) {
 			String[] values = { startMonth, endMonth };
 			return values;
 		} else {
 			String[] values = { startMonth };
 			return values;
 		}
 	}
 
 	String[] determineValues(String date) {
 		String[] values = new String[3];
 		int index = 0;
 		String number = "";
 		boolean foundNumber = false;
 		for (int i = 0; i < date.length(); i++) {
 			String character = date.substring(i, i + 1);
 			if (isNumber(character)) {
 				// number not complete
 				number = number + character;
 				foundNumber = true;
 			} else {
 				// number complete
 				if (foundNumber) {
 					values[index++] = number;
 					number = "";
 				}
 				foundNumber = false;
 			}
 
 		}
 		values[index++] = number;
 		return values;
 	}
 
 	public Date getStart() {
 		return start;
 	}
 
 	public Date getEnd() {
 		return end;
 	}
 
 	int convertString2Int(String value) {
 		value = value.trim();
 		return (new Integer(value)).intValue();
 	}
 
 	boolean isNumber(String string) {
 		boolean isNumber = true;
 		try {
 			new Integer(string).intValue();
 		} catch (Exception e) {
 			isNumber = false;
 		}
 		return isNumber;
 	}
 
 }
