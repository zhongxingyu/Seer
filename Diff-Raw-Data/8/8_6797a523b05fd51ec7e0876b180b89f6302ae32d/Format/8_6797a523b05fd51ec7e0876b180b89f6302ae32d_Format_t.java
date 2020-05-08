 package com.cffreedom.utils;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 
 /**
  * Original Class: com.cffreedom.utils.Format
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
  * 2013-04-27 	markjacobsen.net 	Added pad()
  * 2013-04-30 	markjacobsne.net 	Fixed repeatString()
  * 2013-06-12 	markjacobsen.net 	Consolidated date masks here
  * 2013-10-05 	markjacobsen.net 	Fixed formatBigDecimal()
  * 2014-09-16 	MarkJacobsen.net 	Changed format of MASK_FILE_TIMESTAMP
  * 2014-09-24 	MarkJacobsen.net 	stripNonNumeric() will return null if the input is null
  * 2014-10-13 	MarkJacobsen.net 	Added maxLenString()
  * 2015-01-05 	MarkJacobsen.net 	Added upperCaseFirstCharAllWords()
  */
 public class Format
 {
 	public final static String PHONE_10 = "PHONE_10";
 	public final static String PHONE_DASH = "PHONE_DASH";
 	public final static String PHONE_DOT = "PHONE_DOT";
 	public final static String PHONE_INT = "PHONE_INT";
 	
 	public static final String DATE_DEFAULT = "MM/dd/yyyy";
     public static final String DATE_HUMAN = "MM/dd/yyyy hh:mm a";
     public static final String DATE_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
     public static final String DATE_XML_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss";
     public static final String DATE_YYYYMM = "yyyyMM";
     public static final String DATE_YYYYMMDD = "yyyyMMdd";
     public static final String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
     public static final String DATE_TIME_12_HOUR = "h:mm a";
     public static final String DATE_TIME_24_HOUR = "H:mm";
     public static final String DATE_FILE = "yyyy-MM-dd";
     public static final String DATE_FILE_TIMESTAMP = "yyyy-MM-dd_HH-mm-ss";
     public static final String DATE_DB2_TIMESTAMP = DATE_TIMESTAMP;
     public static final String DATE_MMDDYY = "MMddyy";
 	
 	public static String date(String format, Date date)
 	{
 		DateFormat dateformat = new SimpleDateFormat(format);
 		return dateformat.format(date);
 	}
 	
 	public static String date(String format, Calendar date)
 	{
 		return Format.date(format, Convert.toDate(date));
 	}
 	
 	public static String phoneNumber(String format, String phoneNumber)
 	{
 		if (format.equalsIgnoreCase(PHONE_10) == true)
 		{
 			phoneNumber = stripNonNumeric(phoneNumber);
 			if (phoneNumber.length() > 10) {
 				phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
 			}
 		}
 		else if (format.equalsIgnoreCase(PHONE_DASH) == true)
 		{
 			phoneNumber = stripNonNumeric(phoneNumber);
 			if (phoneNumber.length() > 10) {
 				phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
 			}
 			phoneNumber = phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 6) + "-" + phoneNumber.substring(6, 10);
 		}
 		else if (format.equalsIgnoreCase(PHONE_DOT) == true)
 		{
 			phoneNumber = stripNonNumeric(phoneNumber);
 			if (phoneNumber.length() > 10) {
 				phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
 			}
 			phoneNumber = phoneNumber.substring(0, 3) + "." + phoneNumber.substring(3, 6) + "." + phoneNumber.substring(6, 10);
 		}
 		else if (format.equalsIgnoreCase(PHONE_INT) == true)
 		{
 			phoneNumber = stripNonNumeric(phoneNumber);
 			if (phoneNumber.length() == 10)
 			{
 				phoneNumber = "+1" + phoneNumber;
 			}
 			else if (phoneNumber.length() == 11)
 			{
 				phoneNumber = "+" + phoneNumber;
 			}
 		}
 
 		return phoneNumber;
 	}
 	
 	public static String currency(int amount, boolean includeDecimals)
 	{
 		int decimalPlaces = 2;
 		if (includeDecimals == false) { decimalPlaces = 0; }
 		return "$" + Format.number(amount, decimalPlaces);
 	}
 	
 	public static String currency(BigDecimal amount, boolean includeDecimals)
 	{
 		int decimalPlaces = 2;
 		if (includeDecimals == false) { decimalPlaces = 0; }
 		return "$" + Format.number(amount, decimalPlaces);
 	}
 	
 	public static String number(double number, int decimalPlaces)
 	{
 		BigDecimal val = new BigDecimal(number);
 		return Format.number(val, decimalPlaces, true);
 	}
 	
 	public static String number(int number, int decimalPlaces)
 	{
 		BigDecimal val = new BigDecimal(number);
 		return Format.number(val, decimalPlaces, true);
 	}
 	
 	public static String number(BigDecimal number, int decimalPlaces)
 	{
 		return Format.number(number, decimalPlaces, true);
 	}
 
 	public static String number(BigDecimal n, int decimalPlaces, boolean includeThousandsSeparator)
 	{
 		String format = null;
 		String decimalFormat = "";
 		
 		if (decimalPlaces > 0)
 		{
 			decimalFormat = "." + repeatString("0", decimalPlaces);
 		}
 		
 		if (includeThousandsSeparator == false)
 		{
 			format = "#0" + decimalFormat;
 		}
 		else
 		{
 			format = "#,##0" + decimalFormat;
 		}
 
 		NumberFormat formatter = new DecimalFormat(format);
 		return formatter.format(n);
 	}
 
 	public static String repeatString(String repeatThis, int repeatTimes)
 	{
 		StringBuffer buffer = new StringBuffer();
 		for (int x = 0; x < repeatTimes; x++)
 		{
 			buffer.append(repeatThis);
 		}
 		return buffer.toString();
 	}
 
 	public static String upperCaseFirstChar(String value)
 	{
 		if (Utils.hasLength(value) == false)
 		{
 			return value;
 		}
 		char[] titleCase = value.toCharArray();
 		titleCase[0] = ("" + titleCase[0]).toUpperCase().charAt(0);
 		return new String(titleCase);
 	}
 
 	public static String upperCaseFirstCharAllWords(String value)
 	{
 		if (Utils.hasLength(value) == false)
 		{
 			return value;
 		}
 		StringBuffer sb = new StringBuffer();
 		String[] words = value.split(" ");
 		for (int x = 0; x < words.length; x++) {
			char[] titleCase = words[x].toLowerCase().toCharArray();
 			if (titleCase.length >= 1) {
 				titleCase[0] = ("" + titleCase[0]).toUpperCase().charAt(0);
 				sb.append(new String(titleCase).trim());
 				sb.append(" ");
 			}
 		}
 		return sb.toString().trim();
 	}
 
 	public static String stripNonNumeric(String source)
 	{
 		if (source == null)
 		{
 			return source;
 		}
 		else
 		{
 			String ret = "";
 			for (int x = 0; x < source.length(); x++)
 			{
 				if (Character.isDigit(source.charAt(x)) == true)
 				{
 					ret += source.charAt(x);
 				}
 			}
 			return ret;
 		}
 	}
 
 	public static String stripCrLf(String source)
 	{
 		return Format.replace(replace(source, "\n", ""), "\r", "");
 	}
 	
 	public static String stripExtraSpaces(String source)
 	{
 		return source.replaceAll("\\s+", " ").trim();
 	}
 	
 	public static String maxLenString(String val, int maxLen)
 	{
 		if ((val != null) && (val.length() > maxLen)) {
 			val = val.substring(0, maxLen);
 		}
 		return val;
 	}
 
 	public static String replace(String source, String find, String replace)
 	{
 		return Format.replace(source, find, replace, false);
 	}
 
 	public static String replace(String source, String find, String replace, boolean caseSensative)
 	{
 		if (source != null)
 		{
 			final int len = find.length();
 			StringBuffer sb = new StringBuffer();
 			int found = -1;
 			int start = 0;
 
 			if (caseSensative == true)
 			{
 				found = source.indexOf(find, start);
 			}
 			else
 			{
 				found = source.toLowerCase().indexOf(find.toLowerCase(), start);
 			}
 
 			while (found != -1)
 			{
 				sb.append(source.substring(start, found));
 				sb.append(replace);
 				start = found + len;
 
 				if (caseSensative == true)
 				{
 					found = source.indexOf(find, start);
 				}
 				else
 				{
 					found = source.toLowerCase().indexOf(find.toLowerCase(), start);
 				}
 			}
 
 			sb.append(source.substring(start));
 
 			return sb.toString();
 		}
 		else
 		{
 			return "";
 		}
 	}
 
 	public static String replaceSpan(String source, String findStart, String findEnd, String replace)
 	{
 		return Format.replaceSpan(source, findStart, findEnd, replace, false);
 	}
 
 	/**
 	 * Replace a span of text with the replace value. Useful for stripping html.
 	 * 
 	 * @param source The string to strip from
 	 * @param findStart What you want to replace starts with
 	 * @param findEnd What you want to replace ends with
 	 * @param replace What to replace the span with
 	 * @param caseSensative True if we want to perform a case sensative search
 	 * @return String with all instances of the span stripped out
 	 */
 	public static String replaceSpan(String source, String findStart, String findEnd, String replace, boolean caseSensative)
 	{
 		if (source != null)
 		{
 			int findEndLen = findEnd.length();
 			StringBuffer sb = new StringBuffer();
 			int foundStart = -1;
 			int foundEnd = -1;
 			int start = 0;
 
 			if (caseSensative == true)
 			{
 				foundStart = source.indexOf(findStart, start);
 				foundEnd = source.indexOf(findEnd, start);
 			}
 			else
 			{
 				foundStart = source.toLowerCase().indexOf(findStart.toLowerCase(), start);
 				foundEnd = source.toLowerCase().indexOf(findEnd.toLowerCase(), start);
 			}
 
 			while ((foundStart != -1) && (foundEnd != -1))
 			{
 				sb.append(source.substring(start, foundStart));
 				sb.append(replace);
 				foundStart = foundEnd + findEndLen;
 				start = foundStart;
 
 				if (caseSensative == true)
 				{
 					foundStart = source.indexOf(findStart, start);
 					foundEnd = source.indexOf(findEnd, start);
 				}
 				else
 				{
 					foundStart = source.toLowerCase().indexOf(findStart.toLowerCase(), start);
 					foundEnd = source.toLowerCase().indexOf(findEnd.toLowerCase(), start);
 				}
 			}
 
 			sb.append(source.substring(start));
 
 			return sb.toString();
 		}
 		else
 		{
 			return "";
 		}
 	}
 	
 	public static String pad(String val, int totalChars) { return Format.pad(val, totalChars, " "); }
 	public static String pad(String val, int totalChars, String padChar) { return Format.pad(val, totalChars, padChar, true); }
 	public static String pad(String val, int totalChars, String padChar, boolean padRight)
 	{
 		int len = val.length();
 		if (len < totalChars)
 		{
 			String pad = repeatString(padChar, totalChars - len);
 			if (padRight == true) { val += pad; }
 			else { val = pad + val; }
 		}
 		return val;
 	}
 }
