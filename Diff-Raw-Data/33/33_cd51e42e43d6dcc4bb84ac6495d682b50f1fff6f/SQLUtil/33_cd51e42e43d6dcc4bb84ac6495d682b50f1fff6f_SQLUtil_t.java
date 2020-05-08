 /*
  * @(#)SQLUtil.java $Date: Dec 16, 2011 6:28:55 PM $
  * 
  * Copyright  2011 FortMoon Consulting, Inc. All Rights Reserved.
  * 
  * This software is the confidential and proprietary information of FortMoon
  * Consulting, Inc. ("Confidential Information"). You shall not disclose such
  * Confidential Information and shall use it only in accordance with the terms
  * of the license agreement you entered into with FortMoon Consulting.
  * 
  * FORTMOON MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
  * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
  * NON-INFRINGEMENT. FORTMOON SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
  * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
  * DERIVATIVES.
  * 
  */
 package com.fortmoon.utils;
 
 import java.sql.Date;
 
 /**
  * @author Christopher Steel - FortMoon Consulting, Inc.
  *
  * @since Dec 16, 2011 6:28:55 PM
  */
 public class SQLUtil {
 	//Logger log = Logger.getLogger(this.getClass());
     
 	/**
 	 * @param val
 	 * @return
 	 */
 	private static boolean isDate(String val) {
 		try {
 			Date.valueOf(val);
 			return true;
 		}
 		catch(IllegalArgumentException ie) {
 		}
 		return false;
 	}
 	
 	private static boolean isBool(String val) {
 		if(val != null && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")))
 			return true;
 		return false;
 	}
 	
 	private static boolean isChar(String val) {
 		if(val != null && val.length() == 1)
 			return true;
 		return false;
 	}
 	
 	private static boolean isTime(String val) {
 		try {
 		if(null != java.sql.Time.valueOf(val))
 			return true;
 		}
 		catch(IllegalArgumentException iae) {
 			
 		}
 		return false;
 	}
 
 	private static boolean isDateTime(String val) {
 		if(val != null) {
 			String str[] = val.split("\\s");
 			if(str.length == 2 && isDate(str[0]) && isTime(str[1]))
 				return true;
 		}
 		return false;
 	}
 	
 	private static boolean isNull(String val) {
 		if(val == null || val.isEmpty())
 			return true;
 		return false;
 	}
 	
 	/**
 	 * @param string
 	 * @return
 	 */
 	private static boolean isInteger(String string) {
 		if (string != null && !(string.length() > 1 && string.startsWith("0"))) {
 			try {
 				Integer.parseInt(string);
 				return true;
 			} catch (IllegalArgumentException iae) {
 
 			}
 		}
 		return false;
 	}
 	
 	private static boolean isFloat(String string) {
 		if (string != null && string.contains(".")) {
 			try {
				Float value = Float.parseFloat(string);
				if(Float.NEGATIVE_INFINITY < value && value < Float.MAX_VALUE) {
 					return true;
 				}
 			} catch (IllegalArgumentException iae) {
 
 			}
 		}
 		return false;
 	}
 
 	private static boolean isDouble(String string) {
 		if (string != null  && string.contains(".")) {
 			try {
				Double value = Double.parseDouble(string);
				if(Double.NEGATIVE_INFINITY < value && value < Double.MAX_VALUE) {
					return true;
				}
 			} catch (IllegalArgumentException iae) {
 
 			}
 		}
 		return false;
 	}
 	/**
 	 * @param string
 	 * @return
 	 */
 	private static boolean isBigInt(String string) {
 		if (string != null && !(string.length() > 1 && string.startsWith("0"))) {
 			try {
				Long value = Long.parseLong(string);
				if(Long.MIN_VALUE < value && value < Long.MAX_VALUE) {
					return true;
				}
 			} catch (IllegalArgumentException iae) {
 
 			}
 		}
 		return false;
 	}
 	
 	private static boolean isVarChar(String string) {
 		if (string != null && string.length() < 255) {
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * @param string
 	 * @return
 	 */
 	public static SQLTYPE getType(String string) {
 		if(isNull(string))
 			return SQLTYPE.NULL;
 		if(isDateTime(string))
 			return SQLTYPE.DATETIME;
 		if(isDate(string))
 			return SQLTYPE.DATE;
 		if(isTime(string))
 			return SQLTYPE.TIME;
 		if(isBool(string))
 			return SQLTYPE.BOOL;
 		if(isInteger(string))
 			return SQLTYPE.INTEGER;
 		if(isBigInt(string))
 			return SQLTYPE.BIGINT;
 		if(isBigInt(string))
 			return SQLTYPE.BIGINT;
 		if(isFloat(string))
 			return SQLTYPE.FLOAT;
 		if(isDouble(string))
 			return SQLTYPE.DOUBLE;		
 		if(isChar(string))
 			return SQLTYPE.CHAR;
 		if(isVarChar(string))
 			return SQLTYPE.VARCHAR;
 		if(isBlob(string))
 			return SQLTYPE.BLOB;
 		return SQLTYPE.LONGBLOB;
 	}
 
 
 	/**
 	 * @param string
 	 * @return
 	 */
 	private static boolean isBlob(String string) {
 		if(null != string && string.length() < 65535)
 			return true;
 		return false;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String blob = new String(new char[65534]);
 		String longBlob = new String(new char[65536]);
		
 		System.out.println("Type of <null>:			" + getType(null));
 		System.out.println("Type of '':			" + getType(""));
 		System.out.println("Type of 2011-08-23:		" + getType("2011-08-23"));
 		System.out.println("Type of 12:02:23:		" + getType("12:02:23"));
 		System.out.println("Type of 2011-08-23 12:02:23:	" + getType("2011-08-23 12:02:23"));
 		System.out.println("Type of 12:			" + getType("12"));
 		System.out.println("Type of 12.23:			" + getType("12.23"));
 		System.out.println("Type of 0:			" + getType("0"));
 		System.out.println("Type of 016:			" + getType("016"));
 		System.out.println("Type of 16:			" + getType("16"));
 		System.out.println("Type of 22000000000:		" + getType("22000000000"));
 		System.out.println("Type of 22000000000.1:		" + getType("22000000000.1"));
 		System.out.println("Type of 9223372036854775808:	" + getType("9223372036854775808"));
 		System.out.println("Type of 9223372036854775808.1:	" + getType("9223372036854775808.1"));
 		System.out.println("Type of 4.4028235E38:		" + getType("4.4028235E38"));
 		System.out.println("Type of x:			" + getType("x"));
 		System.out.println("Type of true:			" + getType("true"));
 		System.out.println("Type of FALSE:			" + getType("FALSE"));
 		System.out.println("Type of x12.23:			" + getType("x12.23"));
 		System.out.println("Type of 12.x:			" + getType("12.x"));
 		System.out.println("Type of abc:			" + getType("abc"));
 		System.out.println("Type of [String > 255]:		" + getType(blob));
 		System.out.println("Type of [String > 65535]:	" + getType(longBlob));
 		System.out.println("Float max:			" + Float.MAX_VALUE);
 		System.out.println("Int max:			" + Integer.MAX_VALUE);
 		System.out.println("Long max:			" + Long.MAX_VALUE);
 		System.out.println("Double max:			" + Double.MAX_VALUE);
 		System.out.println("TYPE.FLOAT compareTo TYPE.DOUBLE:			" + (SQLTYPE.FLOAT.compareTo(SQLTYPE.DOUBLE)));
 		System.out.println("TYPE.DOUBLE compareTo TYPE.FLOAT:			" + (SQLTYPE.DOUBLE.compareTo(SQLTYPE.FLOAT)));
 	}
 
 
 }
