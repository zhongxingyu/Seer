 package com.alvarosantisteban.berlincurator;
 
 public class Utils {
 
 	/**
 	 * Normalizes a date from the I Heart Berlin and Berlin Art Parasites format: "May 13 2013"  
 	 * or the White Trash format: "22 May 2013" to the app's format "13/05/2013"
 	 * 
 	 * @param inputDate the date in the I Heart Berlin, Berlin Art Parasites or White Trash format
 	 * @return a String with the date normalized
 	 */
 	public static String formatDate(String inputDate){
 		String monthNumber;
 		String monthLetter = "";
 		String day = "";
 		String[] monthDayYear = inputDate.split(" ");
 		for (int i=0;i<2;i++){ // Just the first two
 			// If there is a letter, we have the month
 			if (Character.isLetter(monthDayYear[i].charAt(0))){
 				monthLetter = monthDayYear[i];
 			}else{ // If not, we have the day
				day = monthDayYear[i];
 			}
 		}
 		if (monthLetter.equals("January"))
 			monthNumber = "01";
 		else if (monthLetter.equals("February"))
             monthNumber = "02";
 		else if (monthLetter.equals("March"))
             monthNumber = "03";
         else if (monthLetter.equals("April"))
             monthNumber = "04";
         else if (monthLetter.equals("May"))
             monthNumber = "05";
         else if (monthLetter.equals("June"))
             monthNumber = "06";
         else if (monthLetter.equals("July"))
             monthNumber = "07";
         else if (monthLetter.equals("August"))
             monthNumber = "08";
         else if (monthLetter.equals("September"))
             monthNumber = "09";
         else if (monthLetter.equals("October"))
             monthNumber = "10";
         else if (monthLetter.equals("November"))
             monthNumber = "11";
         else if (monthLetter.equals("December"))
             monthNumber = "12";
         else
             monthNumber = "00";
 		String total = day+"/"+monthNumber+"/"+monthDayYear[2];
 		return total.trim(); 
 	}
 }
