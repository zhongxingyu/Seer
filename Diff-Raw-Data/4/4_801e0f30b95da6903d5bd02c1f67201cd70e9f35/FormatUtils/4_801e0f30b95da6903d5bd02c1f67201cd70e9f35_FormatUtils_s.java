 package com.cffreedom.utils;
 
 import java.util.Date;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 
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
  * 2013-04-27 	markjacobsen.net 	Added pad()
  */
 public class FormatUtils
 {
 	public final static String PHONE_10 = "PHONE_10";
 	public final static String PHONE_DASH = "PHONE_DASH";
 	public final static String PHONE_DOT = "PHONE_DOT";
 	public final static String PHONE_INT = "PHONE_INT";
 	
 	public static final String DATE_ONLY = "MM/dd/yyyy";
     public static final String DATE_FULL_DATE_TIME = "MM/dd/yyyy hh:mm a";
     public static final String DATE_YYYYMMDD = "yyyyMMdd";
     public static final String DATE_DB2_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
     public static final String DATE_TIME_12_HOUR = "h:mm a";
     public static final String DATE_TIME_24_HOUR = "H:mm";
 	
 	public static String formatDate(String format, Date date)
 	{
 		DateFormat l_oFormat = new SimpleDateFormat(format);
 		return l_oFormat.format(date);
 	}
 	
 	public static String formatPhoneNumber(String format, String phoneNumber)
 	{
 		if (format.equalsIgnoreCase(PHONE_10) == true)
 		{
 			phoneNumber = stripNonNumeric(phoneNumber);
 		}
 		else if (format.equalsIgnoreCase(PHONE_DASH) == true)
 		{
 			phoneNumber = stripNonNumeric(phoneNumber);
 			phoneNumber = phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 6) + "-" + phoneNumber.substring(6, 10);
 		}
 		else if (format.equalsIgnoreCase(PHONE_DOT) == true)
 		{
 			phoneNumber = stripNonNumeric(phoneNumber);
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
 
 	public static String formatBigDecimal(BigDecimal n, int decimalPlaces)
 	{
 		return formatBigDecimal(n, decimalPlaces, true);
 	}
 
 	public static String formatBigDecimal(BigDecimal n, int decimalPlaces, boolean includeThousandsSeparator)
 	{
 		String format = null;
 
 		if (includeThousandsSeparator == false)
 		{
 			format = "#0." + repeatString("0", decimalPlaces - 1);
 		}
 		else
 		{
 			format = "#,##0." + repeatString("0", decimalPlaces - 1);
 		}
 
 		NumberFormat formatter = new DecimalFormat(format);
 		return formatter.format(n);
 	}
 
 	public static String repeatString(String repeatThis, int repeatTimes)
 	{
		StringBuffer buffer = new StringBuffer(repeatThis);
 		for (int x = 0; x < repeatTimes; x++)
 		{
 			buffer.append(repeatThis);
 		}
 		return buffer.toString();
 	}
 
 	public static String upperCaseFirstChar(String value)
 	{
 		if (value.trim().length() == 0)
 		{
 			return value;
 		}
 		char[] titleCase = value.toCharArray();
 		titleCase[0] = ("" + titleCase[0]).toUpperCase().charAt(0);
 		return new String(titleCase);
 	}
 
 	public static String stripNonNumeric(String source)
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
 
 	public static String stripCrLf(String source)
 	{
 		return replace(replace(source, "\n", ""), "\r", "");
 	}
 	
 	public static String stripExtraSpaces(String source)
 	{
 		return source.replaceAll("\\s+", " ").trim();
 	}
 
 	public static String replace(String source, String find, String replace)
 	{
 		return replace(source, find, replace, false);
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
 					found = source.toLowerCase()
 							.indexOf(find.toLowerCase(), start);
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
 		return replaceSpan(source, findStart, findEnd, replace, false);
 	}
 
 	/**
 	 * Replace a span of text with the replace value. Useful for stripping html.
 	 * 
 	 * @param source
 	 *            The string to strip from
 	 * @param findStart
 	 *            What you want to replace starts with
 	 * @param findEnd
 	 *            What you want to replace ends with
 	 * @param replace
 	 *            What to replace the span with
 	 * @param caseSensative
 	 *            True if we want to perform a case sensative search
 	 * @return String with all instances of the span stripped out
 	 */
 	public static String replaceSpan(String source, String findStart, String findEnd, String replace, boolean caseSensative)
 	{
 		if (source != null)
 		{
 			int l_iFindEndLen = findEnd.length();
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
 				foundStart = source.toLowerCase()
 						.indexOf(findStart.toLowerCase(), start);
 				foundEnd = source.toLowerCase()
 						.indexOf(findEnd.toLowerCase(), start);
 			}
 
 			while ((foundStart != -1) && (foundEnd != -1))
 			{
 				sb.append(source.substring(start, foundStart));
 				sb.append(replace);
 				foundStart = foundEnd + l_iFindEndLen;
 				start = foundStart;
 
 				if (caseSensative == true)
 				{
 					foundStart = source.indexOf(findStart, start);
 					foundEnd = source.indexOf(findEnd, start);
 				}
 				else
 				{
 					foundStart = source.toLowerCase()
 							.indexOf(findStart.toLowerCase(), start);
 					foundEnd = source.toLowerCase()
 							.indexOf(findEnd.toLowerCase(), start);
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
 	
 	public static String pad(String val, int totalChars) { return pad(val, totalChars, true); }
 	public static String pad(String val, int totalChars, boolean padRight) { return pad(val, totalChars, padRight, " "); }
 	public static String pad(String val, int totalChars, boolean padRight, String padChar)
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
