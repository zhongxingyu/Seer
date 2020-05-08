 package com.cffreedom.utils;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cffreedom.exceptions.InfrastructureException;
 import com.cffreedom.utils.db.ConnectionManager;
 import com.cffreedom.utils.file.FileUtils;
 
 /**
  * Original Class: com.cffreedom.utils.Utils
  * @author markjacobsen.net (http://mjg2.net/code)
  * Copyright: Communication Freedom, LLC - http://www.communicationfreedom.com
  * 
  * Free to use, modify, redistribute.  Must keep full class header including 
  * copyright and note your modifications.
  * 
  * If this helped you out or saved you time, please consider...
  * 1) Donating: http://www.communicationfreedom.com/go/donate/
  * 2) Shoutout on twitter: @MarkJacobsen or @cffreedom
  * 3) Linking to: http://visit.markjacobsen.net
  * 
  * Changes:
  * 2013-04-08 	markjacobsen.net 	Added JavaDoc comments
  * 2013-04-30 	markjacobsen.net 	Added longestString()
  * 2013-05-21 	markjacobsen.net 	Added appendToStringArray() and appendToIntArray()
  * 2013-07-19	markjacobsen.net 	Added hasLength()
  * 2013-09-15 	markjacobsen.net 	Added getRandomString()
  * 2014-10-22 	MarkJacobsen.net 	Added getProperties()
  */
 public class Utils
 {
 	private static final Logger logger = LoggerFactory.getLogger("com.cffreedom.utils.Utils");
 	private static final String ALPHA_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
 	private static Random random = new Random();
 	
 	/**
      * @param val The number to evaluate
      * @return True if we can convert the value to an integer, otherwise false
      */
 	public static boolean isInt(String val)
 	{
 		try
 		{
 			Convert.toInt(val);
 			return true;
 		}
 		catch (Exception e)
 		{
 			return false;
 		}
 	}
 	
 	/**
      * @param val The number to evaluate
      * @return True if all characters in the string are digits, otherwise false
      */
     public static boolean isNumeric(String val)
     {
        if (val.length() == 0)
        {
             return false;
         }
        
         for (int x = 0; x < val.length(); x++)
         {
             if (Character.isDigit(val.charAt(x)) == false)
             {
                 return false;
             }
      }
         return true;
     }
    
     /**
      * @param val The date to evaluate
      * @return True if we can convert it to a date, otherwise false
      */
     public static boolean isDate(String val)
     {
         try
         {
             Convert.toDate(val);
             return true;
         }catch (Exception e){
             return false;
         }
     }
     
     /**
      * Determine if a string has length (and account for nulls)
      * @param val The value to evaluate
      * @return True if it is not null and has length, false otherwise
      */
     public static boolean hasLength(String val)
     {
     	try
     	{
     		if ((val != null) && (val.length() > 0)){
     			return true;
     		}else{
     			return false;
     		}
     	}catch (NullPointerException e){
     		return false;
     	}
     }
     
     /**
      * This is really just a wrapper to System.out.println and System.out.print - just shorter Utils.output("something")
      * @param val What to output
      * @param newline Include a newline at the end of the output
      */
     public static void output(String val, boolean newline)
     {
     	if (newline == true) {
     		System.out.println(val);
     	}else{
     		System.out.print(val);
     	}
     }
     public static void output(String val) { output(val, true); }
     
     /**
      * Convenience method for prompting for input without any prompt shown on screen.
      * Useful for continuation of input until a special character is read.
      * @return The value the user types in
      */
     public static String promptBare() { return prompt(null, null, false); } 
     
     /**
      * Convenience method for prompting for a password
      * @param prompt What to prompt the user with (ex: "Password:")
      * @return The value the user types in
      */
     public static String promptPassword(String prompt) { return prompt(prompt, null, true); }
     public static String promptPassword() { return promptPassword("Password"); }
     
     /**
      * @param prompt What to prompt the user with (ex: "Username:")
      * @param defaultVal The value to use if the user just presses enter (ex: "Username [jdoe]:")
      * @param isPassword If true, don't show the characters the user types
      * @return The value the user types in
      */
     public static String prompt(String prompt, String defaultVal, boolean isPassword)
 	{
     	String enteredVal = null;
     	
     	if (prompt == null)
     	{
     		prompt = "";
     	}
     	else
     	{
 	    	if (defaultVal != null) { prompt += " [" + defaultVal + "]"; }
 			prompt += ": ";
     	}
     	
 		// When running inside of Eclipse you won't be able to get access to the Console object
 		if (System.console() != null)
 		{
 			logger.trace("Got a console object");
 			if (isPassword == true)
 			{
 				enteredVal = Convert.toString(System.console().readPassword(prompt));
 			}
 			else
 			{
 				enteredVal = System.console().readLine(prompt);
 			}
 		}
 		else
 		{
 			System.out.print(prompt);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
 
             try
             {
             	enteredVal = bufferedReader.readLine();
             }
             catch (Exception e) { enteredVal = null; }
 		}
 		
 		if ((enteredVal == null) || (enteredVal.trim().length() == 0))
 		{
 			enteredVal = defaultVal;
 		}
 		
 		return enteredVal;
 	}
     public static String prompt(String prompt) { return prompt(prompt, null); }
     public static String prompt(String prompt, String defaultVal) { return prompt(prompt, defaultVal, false); }
     
     /**
      * @param val String to get the last character from.
      * @return The last character (as a String) in the passed in string.
      */
     public static String lastChar(String val)
     {
     	if (val.length() > 0)
     	{
     		return Convert.toString(val.charAt(val.length() - 1));
     	}
     	else
     	{
     		return "";
     	}
     }
     
     public static int longestString(List<String> vals)
     {
     	int longest = 0;
     	for (String val : vals)
     	{
     		if (val.length() > longest) { longest = val.length(); }
     	}
     	return longest;
     }
 
 	public static int longestString(String[] vals)
 	{
 		return longestString(Convert.toListOfStrings(vals));
 	}
 	
 	public static String[] appendToStringArray(String[] array, String val)
 	{
 		String[] newArray = null;
 		
 		if (array == null)
 		{
 			newArray = new String[1];
 			newArray[0] = val;
 		}
 		else
 		{
 			newArray = Arrays.copyOf(array, array.length + 1);
 			newArray[newArray.length - 1] = val;
 		}
 		
 		return newArray;
 	}
 	
 	public static int[] appendToIntArray(int[] array, int val)
 	{
 		int[] newArray = null;
 		
 		if (array == null)
 		{
 			newArray = new int[1];
 			newArray[0] = val;
 		}
 		else
 		{
 			newArray = Arrays.copyOf(array, array.length + 1);
 			newArray[newArray.length - 1] = val;
 		}
 		
 		return newArray;
 	}
 	
 	public static String getRandomString(int len)
 	{
 		String ret = "";
 		for (int x = 0; x < len; x++)
 		{
 			ret += ALPHA_NUM.charAt(random.nextInt(ALPHA_NUM.length()));
 		}
 		return ret;
 	}
 	
 	/**
 	 * Allows you to get properties from a file on the file system or in the classpath. 
 	 * Will use file system first. 
 	 * @param file Full path to a properties file or the name of a properties file to pull off the classpath
 	 * @return The properties from the file
 	 * @throws InfrastructureException 
 	 */
 	public static Properties getProperties(String file) throws InfrastructureException
 	{
 		InputStream inputStream = null;
 		Properties props = new Properties();
 		
 		try
 		{
 			if (FileUtils.fileExists(file) == true)
 			{
 				logger.info("Loading from passed in file: {}", file);
 				inputStream = new FileInputStream(file);
 			}
 			else
 			{
 				logger.info("Attempting to find file on classpath: {}", file);
 				inputStream = Utils.class.getClassLoader().getResourceAsStream(file);
 			}
 			
 			if (inputStream == null)
 			{
 				throw new InfrastructureException("Properties file not found: "+file);
 			}
 			else
 			{
 				logger.debug("Loading property file");
 				
 				props.load(inputStream);
 				inputStream.close();
 			}
 		}
 		catch (IOException e)
 		{
 			throw new InfrastructureException("Error getting properties from: "+file, e);
 		}
 		
 		return props;
 	}
 }
