 package com.nityankhanna.androidutils;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 /**
  * Created by Nityan Khanna on Jan 03 2014.
  */
 public class DateUtils {
 
 	private DateUtils() {
 	}
 
 	public static Date convertToLocalTime(Date date, DateTimeFormat dateTimeFormat) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimeFormat.getValue());
 		long when;
 
 		try {
 			when = dateFormat.parse(dateToString(date, dateTimeFormat)).getTime();
 
 			return new Date(when + TimeZone.getDefault().getRawOffset() + (TimeZone.getDefault().inDaylightTime(new Date())
 					? TimeZone.getDefault().getDSTSavings() : 0));
 		} catch (ParseException e) {
 			e.printStackTrace();
			throw new IllegalArgumentException("");
 		}
 	}
 
 	public static Date convertToNewFormat(String dateToFormat, String currentFormat, String targetFormat) {
 		SimpleDateFormat originalFormat = new SimpleDateFormat(currentFormat);
 		SimpleDateFormat newFormat = new SimpleDateFormat(targetFormat);
 
 		try {
 			Date date = originalFormat.parse(dateToFormat);
 			String formattedDateAsString = newFormat.format(date);
 			return newFormat.parse(formattedDateAsString);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException("");
 		}
 	}
 
 	public static String dateToString(Date date) {
 		return dateToString(date, DateTimeFormat.MONTH_DAY_YEAR);
 	}
 
 	public static String dateToString(Date date, DateTimeFormat dateTimeFormat) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimeFormat.getValue());
 		return dateFormat.format(date);
 	}
 
 	public static Date stringToDate(String date, DateTimeFormat dateTimeFormat) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimeFormat.getValue());
 
 		try {
 			return dateFormat.parse(date);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException("The date: " + date + " format does not match the format " + dateTimeFormat.getValue() + ", unable to parse");
 		}
 	}
 }
