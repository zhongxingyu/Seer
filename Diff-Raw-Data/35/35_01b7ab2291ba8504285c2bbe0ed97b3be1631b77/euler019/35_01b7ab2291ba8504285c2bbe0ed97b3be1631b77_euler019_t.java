 package com.osiris.math.Projects;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import com.osiris.math.shared.MathException;
 
 public class euler019 {
 
 	/**
 	 * You are given the following information, but you may prefer to do some research for yourself.<br />
 	 * <br />
      * 1 Jan 1900 was a Monday.<br />
      * Thirty days has September,<br />
      * April, June and November.<br />
      * All the rest have thirty-one,<br />
      * Saving February alone,<br />
      * Which has twenty-eight, rain or shine.<br />
      * And on leap years, twenty-nine.<br />
      * <br />
      * A leap year occurs on any year evenly divisible by 4, but not on a century unless it is divisible by 400.<br />
      * <br />
 	 * How many Sundays fell on the first of the month during the twentieth century (1 Jan 1901 to 31 Dec 2000)?<br />
 	 * <br />
 	 * @return
 	 * @throws MathException
 	 */
 	
 	
 	
 	public static int execute() throws MathException{
 		
 		int[] daysInMonth = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; 
		int count = 2;
 		int sundays = 0;
 		
		for(int year = 1901; year < 2001; year++){
 			for(int month = 1; month < 13; month++){
 
 				if(count % 7 == 0){
 					sundays++;
 				}
 				
 				if(month == 2 && isLeap(year)){
 					count += 29;
 				} else {
 					count += daysInMonth[month - 1];
 				}
 				
 			}
 		}
 		return sundays;
 	}
 
 	/**
 	 * Returns an answer to the problem specified in execute
 	 * 
 	 * @return - the answer to the problem
 	 * @throws MathException
 	 */
 	public static String string() throws MathException{
 		
 		return "019.\tThe number of sundays on the first of the month in the 19th centure is: " + execute();
 	}
 	
 	public static boolean isLeap(int year){
 		if(year % 4 == 0 && year % 100 != 0){
 			return true;
 		} else if(year % 400 == 0){
 			return true;
 		}
 		return false;
 	}
 
 }
