 /*
  *	File: @(#)StringUtils.java 	Package: com.pace.base.utility 	Project: Paf Base Libraries
  *	Created: Sep 7, 2005  		By: JWatkins
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *	This software is the confidential and proprietary information of Palladium Group, Inc.
  *	("Confidential Information"). You shall not disclose such Confidential Information and 
  * 	should use it only in accordance with the terms of the license agreement you entered into
  *	with Palladium Group, Inc.
  *
  *
  *
 	Date			Author			Version			Changes
 	xx/xx/xx		xxxxxxxx		x.xx			..............
  * 
  */
 package com.pace.base.utility;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import com.Ostermiller.util.StringTokenizer;
 
 /**
  * String utilities
  *
  * @version	x.xx
  * @author JWatkins
  *
  */
 public class StringUtils {
 
 	/**
 	 *	Convert single integer array to string list of elements
 	 *
      * @param array Array of objects containing alpha-numeric characters
      * @param stringOpenChars Character that will be placed at the beginning of the return string
      * @param stringCloseChars Character that will be placed at the end of the return string
      * @param elementOpenChars Character that will be placed at the beginning of each array element
      * @param elementCloseChars Character that will be placed at the end of each array element
      * @param elementDelim Character to use as delimiter between array elements
 	 * @return String
 	 */
 	public static String arrayToString (int[] array, String stringOpenChars, String stringCloseChars, 
 			String elementOpenChars, String elementCloseChars, String elementDelim) {
 		
 		String arrayString = stringOpenChars;
 
 		// Convert index to string value
 		for (int i = 0; i < array.length; i++) {
 			if (i < array.length - 1) {
 				arrayString = arrayString + elementOpenChars + array[i] + elementCloseChars + elementDelim;
 			} else {
 				arrayString = arrayString + elementOpenChars + array[i] + elementCloseChars + stringCloseChars;					
 			}
 		}
 		return arrayString;
 	}
 
     /**
      *  convenience wrapper providing some default options.
      *
      * @param array
      * @return String
      */
     public static String arrayToString(int[] array) {
         return arrayToString(array, "{", "}", "[", "]", ", ");
     }
     
     /**
      *	Convert single string array to string list of elements
      *
      * @param array Array of objects containing alpha-numeric characters
      * @param stringOpenChars Character that will be placed at the beginning of the return string
      * @param stringCloseChars Character that will be placed at the end of the return string
      * @param elementOpenChars Character that will be placed at the beginning of each array element
      * @param elementCloseChars Character that will be placed at the end of each array element
      * @param elementDelim Character to use as delimiter between array elements
      * @return String
      */
     public static String arrayToString (String[] array, String stringOpenChars, String stringCloseChars, 
     		String elementOpenChars, String elementCloseChars, String elementDelim)  {
     	
     	final int AVG_TEXT_LENGTH = 15;
     	StringBuffer arrayString = new StringBuffer(array.length * AVG_TEXT_LENGTH);
     	arrayString.append(stringOpenChars);
     
     	// Convert index to string value
     	for (int i = 0; i < array.length; i++) {
     		if (i < array.length - 1) {
     			arrayString.append(elementOpenChars + array[i] + elementCloseChars + elementDelim);
     		} else {
     			arrayString.append(elementOpenChars + array[i] + elementCloseChars + stringCloseChars);					
     		}
     	}
     	return arrayString.toString();
     }
     
     /**
      *	convenience wrapper providing some default options.
      *
      * @param array
      * @return String
      */
     public static String arrayToString(String[] array) {
         return arrayToString(array, "{", "}", "[", "]", ", ");
     }
 
     /**
      * 
      * Converts an array of strings to a string delimited by separator
      *
      * @param a array to convert
      * @param separator separator to use
      * @return
      */
     public static String arrayToString(String[] a, String separator) {
         if (a == null || separator == null) {
             return null;
         }
         StringBuilder result = new StringBuilder();
         if (a.length > 0) {
             result.append(a[0]);
             for (int i=1; i < a.length; i++) {
                 result.append(" " + separator + " ");
                 result.append(a[i]);
             }
         }
         return result.toString();
       }
 
     
     /**
      *  Convert single object array to string list of elements via the toString of the object.
      *
      * @param array Array of objects containing alpha-numeric characters
      * @param stringOpenChars Character that will be placed at the beginning of the return string
      * @param stringCloseChars Character that will be placed at the end of the return string
      * @param elementOpenChars Character that will be placed at the beginning of each array element
      * @param elementCloseChars Character that will be placed at the end of each array element
      * @param elementDelim Character to use as delimiter between array elements
      * @return String
      */
     public static String arrayToString (Object[] array, String stringOpenChars, String stringCloseChars, 
             String elementOpenChars, String elementCloseChars, String elementDelim)  {
         
         StringBuffer arrayString = new StringBuffer(stringOpenChars);
     
         // Convert index to string value
         for (int i = 0; i < array.length; i++) {
             if (i < array.length - 1) {
                 arrayString.append(elementOpenChars + array[i].toString() + elementCloseChars + elementDelim);
             } else {
                 arrayString.append(elementOpenChars + array[i].toString() + elementCloseChars + stringCloseChars);                 
             }
         }
         return arrayString.toString();
     }    
 
     /**
      *  Convert single object array to string list of elements via the toString of the object.
      *
      * @param array Array of objects containing alpha-numeric characters
      * @param stringOpenChars Character that will be placed at the beginning of the return string
      * @param stringCloseChars Character that will be placed at the end of the return string
      * @param elementOpenChars Character that will be placed at the beginning of each array element
      * @param elementCloseChars Character that will be placed at the end of each array element
      * @param elementDelim Character to use as delimiter between array elements
      * @param elementsPerLine Number of elements to display on a line
      * @return String
      */
     public static String arrayToString (Object[] array, String stringOpenChars, String stringCloseChars, 
             String elementOpenChars, String elementCloseChars, String elementDelim, int elementsPerLine)  {
         
         StringBuffer arrayString = new StringBuffer(stringOpenChars);
     
         // Convert index to string value
         for (int i = 0; i < array.length; i++) {
             if (i < array.length - 1) {
                 arrayString.append(elementOpenChars + array[i].toString() + elementCloseChars + elementDelim);
                 if (i%elementsPerLine ==0) arrayString.append("\n");
             } else {
                 arrayString.append(elementOpenChars + array[i].toString() + elementCloseChars + stringCloseChars);                 
             }
         }
         return arrayString.toString();
     }       
     
     
     /**
      *  convenience wrapper providing some default options.
      *
      * @param array Array of objects containing alpha-numeric characters
      * @return String
      */
     public static String arrayToString(Object[] array) {
         return arrayToString(array, "{", "}", "[", "]", ", ");
     }
     
     /**
      *  convenience wrapper providing some default options.
      *
      * @param array Array of objects containing alpha-numeric characters
      * @param elementsPerLine Number of elements to display on a line
      * @return String
      */
     public static String arrayToString(Object[] array, int elementsPerLine) {
         return arrayToString(array, "{", "}", "[", "]", ", ", elementsPerLine);
     }
 
     
 	/**
 	 * 	Format the integer as a string with a comma seperator
 	 * 	between each set of three digts
 	 * 
 	 * @param number Number to format
 	 * @return
 	 */
 	public static String commaFormat(Number number) {
 		
 		String defaultPattern = "#,###,###,###,###";
 		return decimalFormat(number, defaultPattern);
 	}
 
 	/**
      * 	Format a number using the supplied decimal format pattern
      * 
      * @param number Number to format
      * @param pattern Decimal format pattern
      * 
      * @return Formatted number
      */
     public static String decimalFormat(Number number, String pattern) {
     	
 		NumberFormat formatter = new DecimalFormat(pattern);
 		String formattedNumber = formatter.format(number);
 		
 		return formattedNumber;
 	
     }
     
     
     /**
      *  convenience wrapper providing some default options.
      *
      * @param list List of objects containing alpha-numeric characters
      * @return String
      */
     public static String arrayListToString(List<String> list) {
     	Collections.sort(list);
     	String[] array = list.toArray(new String[0]);
         return arrayToString(array);
     }
     
     /**
      *  convenience wrapper providing some default options.
      *
      * @param set Set of objects containing alpha-numeric characters
      * @return String
      */
     public static String setToString(Set<String> set) {
     	return arrayListToString(new ArrayList<String>(set));
     }
     
 	/**
 	 *	Put double quotes ("") around supplied text string.
 	 *
 	 * @param text Text string to put double quotes around
 	 * @return Updated string
 	 */
 	public static String doubleQuotes(String text) {
 		return "\"" + text + "\"";
 	}       
 
 	/**
 	 *	Put single quotes ('') around supplied text string.
 	 *
 	 * @param text Text string to put double quotes around
 	 * @return Updated string
 	 */
 	public static String quotes(String text) {
 		return "'" + text + "'";
 	}
 
 	/**
 	 *  Replace all occurrences of target value with replacement value, ignoring case
 	 *
 	 * @param searchString String to search
 	 * @param targetValue Value being replaced
 	 * @param replacementValue Replacement value
 	 * 
 	 * @return Updated search string
 	 */
 	public static String replaceAllIgnoreCase(String searchString, String targetValue, String replacementValue) {
 		
 		String updatedString = searchString;
 		
 		// Search for target
 		int pos = updatedString.toUpperCase().indexOf(targetValue.toUpperCase());
 		
 		// If target is found
 		while (pos >= 0) {
 			// Replace token value
 			updatedString = updatedString.substring(0, pos) + replacementValue + updatedString.substring(pos + targetValue.length());
 			
 			// Search again
 			pos = updatedString.toUpperCase().indexOf(targetValue.toUpperCase());
 		}		
 		
 		// Return updated string
 		return updatedString;
 	}       
 	
 
 	/**
 	 *  Read specified text file into a string variable
 	 *
 	 * @param fullPathFilename Full path to file
 	 * 
 	 * @return String
 	 * @throws IOException
 	 */
 	public static String textFileToString(String fullPathFilename) throws IOException {
 		
 		StringBuffer sb = new StringBuffer(1024);
 		BufferedReader reader = new BufferedReader(new FileReader(fullPathFilename));
 				
 		char[] chars = new char[1024];
 		@SuppressWarnings("unused")
 		int numRead = 0;
 		while( (numRead = reader.read(chars)) > -1){
 			sb.append(String.valueOf(chars));	
 		}
 
 		reader.close();
 
 		return sb.toString();
 	}
 
 	/**
 	 *  Parse formatted sql text file
 	 *
 	 * @param fullPathFilename Full path to file
 	 * 
 	 * @return ArrayList<String>
 	 */
 	public static ArrayList<String> parseSqlFile(String fullPathFilename) {
 		
 		ArrayList<String> sqlStatements = new ArrayList<String>();
 		final String SQL_DELIM = ";";
 		
 		// Open sql file
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(fullPathFilename));
 
 			// Read file
 			boolean eof = false;
 			StringBuffer newSqlQuery = new StringBuffer(256); 
 			while (!eof) {
 				
 				// Read next line
 				String line = reader.readLine();
 
 				// Exit loop upon EOF
 				if (line == null) {
 					eof = true;
 					continue;
 				} 
 				
 				// Ignore comments and blank lines
 				line = line.trim();
 				if (line.startsWith("--") || line.equals("")) {
 					continue;
 				}
 				
 				// Convert tabs to blanks
 				line.replace("\\t", "    ");
 				
 				// Look for sql delimiter
 				int pos = line.indexOf(SQL_DELIM);
 				
 				// Look for complete sql queries
 				while (pos >= 0) {
 					
 					// Parse out sql query
 					newSqlQuery.append(line.substring(0, pos));
 					sqlStatements.add(newSqlQuery.toString());
 					newSqlQuery = new StringBuffer(256);
 					line = line.substring(pos + 1).trim();
 						
 					// Look for delimiter
 					pos = line.indexOf(SQL_DELIM);
 					
 				}
 				
 				// Append rest of line
 				newSqlQuery.append(line);
 			
 			}
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			// Close file
 			try {
 				reader.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		return sqlStatements;
 	}
 	
 	/**
 	 * 
 	 * Splits a string into a list based on a delimiter
 	 *
 	 * @param stringToSplit string to split
 	 * @param delim delimiter to split on
 	 * @return list of strings split on the delimiter
 	 */
 	public static List<String> stringToList(String stringToSplit, String delim) {
 		
 		List<String> arrayList = new ArrayList<String>();
 		
 		if ( stringToSplit != null && delim != null ) {
 		
 			StringTokenizer st = new StringTokenizer(stringToSplit, delim);
 			
 			String[] stringAr = st.toArray();
 			
 			if ( stringAr != null ) {
 				
 				for (int i = 0; i < stringAr.length; i++ 	) {
 					
 					arrayList.add(stringAr[i].trim());
 				}
 				
 			}
 		
 		}
 		
 		return arrayList;
 	}
 		
    /**
      * 
      * Converts an list of strings to a string delimited by separator
      *
      * @param a array to convert
      * @param separator separator to use
      * @return
      */
     public static String arrayListToString(List<String> list, String separator) {
     	Collections.sort(list);
     	String[] array = list.toArray(new String[0]);
     	return arrayToString(array, separator);
       }
 
     /**
      *  convenience wrapper providing some default options.
      *
      * @param set Set of objects containing alpha-numeric characters
      * @return String
      */
     public static String setToString(Set<String> set, String separator) {
     	return arrayListToString(new ArrayList<String>(set), separator);
     }
 }
