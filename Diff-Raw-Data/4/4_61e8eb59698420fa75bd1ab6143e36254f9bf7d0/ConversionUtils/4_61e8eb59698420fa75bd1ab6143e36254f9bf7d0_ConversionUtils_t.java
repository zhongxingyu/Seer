 package com.cffreedom.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.apache.commons.codec.binary.Base64;
 
 /**
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
  * 2013-04-24 	markjacobsen.net 	Added toString(InputStream)
  * 2013-04-30 	markjacobsen.net 	Added toArrayListOfStrings()
  * 2013-05-08 	markjacobsen.net 	Added toDate(long)
  * 2013-05-29 	markjacobsen.net 	Handling string dates in the form yyyy-MM-dd better in toDate(val, mask)
  */
 public class ConversionUtils
 {
 	public static String toBase64(String val)
 	{
 		return new String(Base64.encodeBase64(val.getBytes()));
 	}
 
 	public static String toMd5(String val) throws NoSuchAlgorithmException
 	{
 		MessageDigest md = MessageDigest.getInstance("MD5");
 		md.update(val.getBytes());
 
 		byte byteData[] = md.digest();
 
 		// convert the byte to hex format method 1
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < byteData.length; i++) {
 			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
 		}
 
 		System.out.println("Digest(in hex format):: " + sb.toString());
 
 		// convert the byte to hex format method 2
 		StringBuffer hexString = new StringBuffer();
 		for (int i = 0; i < byteData.length; i++)
 		{
 			String hex = Integer.toHexString(0xff & byteData[i]);
 			if (hex.length() == 1)
 				hexString.append('0');
 			hexString.append(hex);
 		}
 
 		return hexString.toString();
 	}
 
 	public static String toString(Long val)
 	{
 		return String.valueOf(val.longValue());
 	}
 	
 	public static String toString(int val)
     {
         return (new Integer(val)).toString();
     }
 	
 	public static String toString(byte val)
 	{
 		return Byte.toString(val);
 	}
 	
 	public static String toString(byte[] val)
 	{
 		return new String(val);
 	}
 	
 	public static String toString(char val)
 	{
 		return Character.toString(val);
 	}
 	
 	public static String toString(char[] val)
 	{
 		return new String(val);
 	}
 	
 	public static String toString(InputStream val) throws IOException
 	{
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 		BufferedReader br = new BufferedReader(new InputStreamReader(val));
 		while ((line = br.readLine()) != null) {
 			sb.append(line);
 		}
 		return sb.toString();
 	}
 	
 	public static ArrayList<String> toArrayListOfStrings(String[] vals)
 	{
 		ArrayList<String> ret = new ArrayList<String>();
 		for (int x = 0; x < vals.length; x++)
 		{
 			ret.add(vals[x]);
 		}
 		return ret;
 	}
 	
 	//------------------------------------------------------------------
 	// Int methods
 	public static int toInt(String val)
 	{
 		return (new Integer(val)).intValue();	
 	}
 	
 	public static int toInt(long val)
 	{
 		return (new Long(val)).intValue();	
 	}
 	
 	public static int toInt(double val)
 	{
 		return (new Double(val)).intValue();	
 	}
 	
 	public static int toInt(boolean val)
 	{
 	    if (val == true)
 	    {
 	        return 1;
 	    }else{
 	        return 0;
 	    }
 	}
 	
 	public static int[] toIntArray(String[] vals)
 	{
 		int[] l_nArray = new int[vals.length];
 		for (int x = 0; x < vals.length; x++)
 		{
 			l_nArray[x] = toInt(vals[x]);
 		}
 		return l_nArray;	
 	}
 
     //------------------------------------------------------------------
     // Calendar methods
     public static Calendar toCalendar(String val, String mask) throws ParseException
     {
     	Calendar cal = Calendar.getInstance();
         cal.setTime(toDate(val, mask));
         return cal;
     }
    
     public static Calendar toCalendar(java.util.Date val)
     {
     	Calendar cal = Calendar.getInstance();
         cal.setTime(val);
         return cal;
     }
    
     //------------------------------------------------------------------
     // Date/Time methods           
     public static java.util.Date toDate(Calendar val)
     {
         try {
             return val.getTime();
         } catch (Exception e) { return null; }
     }
    
     public static java.util.Date toDate(java.sql.Date val)
     {
         try {
             return (java.util.Date)val;
         } catch (Exception e) { return null; }
     }
    
     public static java.util.Date toDate(String val)
     {
     	return toDate(val, DateTimeUtils.MASK_DEFAULT_DATE);
     }
     
     public static java.util.Date toDate(long val)
     {
     	return new Date(val);
     }
    
     public static java.util.Date toDate(String val, String mask)
     {
         String retVal = val;
        
         try
         {
         	if (mask.compareTo(DateTimeUtils.MASK_DEFAULT_DATE) == 0)
             {
         		String[] parts = val.split("/"); // split(a_sVal, "/");
                 if ( (parts.length == 3) && (parts[2].length() != 4) )
                 {
                 	String year = toString(DateTimeUtils.year(new java.util.Date()));
                     year = year.substring(0, 4 - parts[2].length());
                     year = year + parts[2];
                     retVal = parts[0] + "/" + parts[1] + "/" + year;
                 }
             }
         	else if (mask.compareTo(DateTimeUtils.MASK_FILE_DATESTAMP) == 0)
         	{
         		String tmp = val.substring(5, val.length()) + "-" + val.substring(0, 4);
         		retVal = tmp.replace('-', '/');
        		mask = DateTimeUtils.MASK_DEFAULT_DATE; // Have to reset it to parse correctly
         	}
                    
             DateFormat df = new SimpleDateFormat(mask);
             return df.parse(retVal);
         }
        catch (Exception e) { e.printStackTrace(); return null; }
     }
     
     public static java.util.Date toDateNoTime(java.util.Date val)
     {
     	return toDate(DateTimeUtils.dateFormat(val));
     }
    
     public static java.util.Date[] toDateArray(String[] vals) throws ParseException
     {
         return toDateArray(vals, DateTimeUtils.MASK_DEFAULT_DATE);
     }
                
     public static java.util.Date[] toDateArray(String[] vals, String mask) throws ParseException
     {
         java.util.Date[] dateArray = new java.util.Date[vals.length];
         for (int x = 0; x < vals.length; x++)
         {
         	dateArray[x] = toDate(vals[x], mask);
         }
         return dateArray;         
     }
    
     @SuppressWarnings("deprecation")
 	public static java.sql.Date toSqlDate(String val)
     {
         try {
             return new java.sql.Date(java.sql.Date.parse(val));
         } catch (Exception e) { return null; }
     }
    
     public static java.sql.Date toSqlDate(java.util.Date val)
     {
         try {
             return (java.sql.Date)val;
         } catch (Exception e) { return null; }
     }
    
     public static java.util.Date toTime(String val) throws Exception
     {
         try {
         	DateFormat df = new SimpleDateFormat(DateTimeUtils.MASK_TIME_12_HOUR);
             return df.parse(val);
         } catch (Exception e) { return null; }
     }
    
     /***
     * This function converts a standard java.util.Date to a
      * DB2 Formated date.
      * @param inDate java.util.Date to convert to DB2 date string
     * @return DB2 date string
     */
     public static String toDB2DateString(java.util.Date val){
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         return df.format(val);
     }
    
     /***
     * This function converts a standard java.sql.Date to a
      * DB2 Formated date.
      * @param inDate java.sql.Date to convert to DB2 date string
     * @return DB2 date string
     */       
     public static String toDB2DateString(java.sql.Date val){
        
         return val.toString();
     }          
     /***
     * This function converts a standard java.util.Date to a
      * DB2 Formated date.
      * @param inDate java.util.Date to convert to DB2 date string
     * @return DB2 date string
     * @throws ParseException
     */
     public static java.sql.Date toDB2Date(String val) throws ParseException{
         DateFormat df;
         if (val.trim().charAt(4)=='-'){
             df = new SimpleDateFormat("yyyy-MM-dd");
         } else {
             df = new SimpleDateFormat("MM/dd/yyyy");
         }
        
         java.util.Date tempdate = df.parse(val);
         java.sql.Date db2date = (java.sql.Date)tempdate;
 
         return db2date;
     }
    
     /**
     * This function converts a standard java.util.Date to a
     * java.sql.Timestamp suitable for a db TIMESTAMP or DATETIME
     * @param a_dVal java.util.Date to convert to a Timestamp object
     * @return java.sql.Timestamp object
     */
     public static java.sql.Timestamp toTimestamp(java.util.Date val)
     {
         return new java.sql.Timestamp(val.getTime());
     }
 
 }
