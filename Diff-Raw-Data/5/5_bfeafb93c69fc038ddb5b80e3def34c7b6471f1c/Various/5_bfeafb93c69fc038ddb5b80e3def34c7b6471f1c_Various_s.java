 //  Various.java
 
 /*
  * Copyright (c) 2008, Gennady & Michael Kushnir
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  * 	•	Redistributions of source code must retain the above copyright notice, this
  * 		list of conditions and the following disclaimer.
  * 	•	Redistributions in binary form must reproduce the above copyright notice,
  * 		this list of conditions and the following disclaimer in the documentation
  * 		and/or other materials provided with the distribution.
  * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
  * 		to endorse or promote products derived from this software without specific 
  * 		prior written permission.
  * 		
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.rujel.reusables;
 
 import com.webobjects.foundation.*;
 import com.webobjects.eoaccess.EOUtilities;
 import com.webobjects.eocontrol.*;
 
 import java.io.File;
 import java.util.Enumeration;
 
 public class Various {
 	
 	public static boolean boolForObject (Object value) {
 		if(value == null) return false;
 		if(value instanceof Boolean)
 			return ((Boolean)value).booleanValue();
 		if(value instanceof Number)
 			return (((Number)value).intValue() != 0);
 		if(value instanceof String) {
 			if(((String)value).length() == 0) return false;
 			if("false".equalsIgnoreCase((String)value)) return false;
 		}
 		return true;
 	}
 	
 	public static EOQualifier getEOInQualifier(String key, NSArray values) {
 		if(values == null || values.count() == 0)
 			return null;
 		if(values.count() == 1)
 			return new EOKeyValueQualifier(key,
 					EOQualifier.QualifierOperatorEqual,values.objectAtIndex(0));
 		NSMutableArray quals = new NSMutableArray();
 		Enumeration en = values.objectEnumerator();
 		while (en.hasMoreElements()) {
 			quals.addObject(new EOKeyValueQualifier(key,
 					EOQualifier.QualifierOperatorEqual,en.nextElement()));
 		}
 		return new EOOrQualifier(quals);
 	}
 	
 	public static String makeRoman(int value) {
 //		StringBuffer buf = new StringBuffer();
 		switch (value) {
 		case 1:
 			return "I";
 		case 2:
 			return "II";
 		case 3:
 			return "III";
 		case 4:
 			return "IV";
 		case 5:
 			return "V";
 		case 6:
 			return "VI";
 		case 7:
 			return "VII";
 		case 8:
 			return "VIII";
 		case 9:
 			return "IX";
 		case 10:
 			return "X";
 		case 11:
 			return "XI";
 		case 12:
 			return "XII";
 		default:
 			return Integer.toString(value);
 		}
 	}
 	
 	public static String cleanURL(String url) {
 		if(url.startsWith("http")) {
 			int pos = url.indexOf('/',8);
			url = url.substring(pos);
 		}
 		return url;
 	}
 	
 	public static String convertFilePath(String filePath) {
 		if(filePath == null)
 			return null;
 		if(filePath.startsWith("CONFIGDIR")) {
 			filePath = filePath.substring(9);
 			String prefix = System.getProperty("CONFIGDIR");
 			if(prefix == null)
 				prefix = NSPathUtilities.stringByAppendingPathComponent(
 						System.getProperty("WOLocalRootDirectory",""), 
 						"/Library/WebObjects/Configuration");
 			filePath = NSPathUtilities.stringByAppendingPathComponent(prefix,filePath);
 		}
 		if (filePath.startsWith("LOCALROOT")) {
 			filePath = filePath.substring(9);
 			filePath = NSPathUtilities.stringByAppendingPathComponent(
 					System.getProperty("WOLocalRootDirectory",""),filePath);
 		} else if (filePath.startsWith("WOROOT")) {
 			filePath = filePath.substring(6);
 			filePath = NSPathUtilities.stringByAppendingPathComponent(
 					System.getProperty("WORootDirectory","/System"),filePath);
 		} else if(filePath.charAt(0) == '~') {
 			filePath = filePath.substring(1);
 			filePath = NSPathUtilities.stringByAppendingPathComponent(
 					System.getProperty("WOUserDirectory",""),filePath);
 		} else if(filePath.charAt(0) != '.' && filePath.charAt(0) != '/') {
 			String prefix = System.getProperty("CONFIGDIR");
 			if(prefix == null)
 				prefix = NSPathUtilities.stringByAppendingPathComponent(
 						System.getProperty("WOLocalRootDirectory",""), 
 						"/Library/WebObjects/Configuration");
 			else
 				prefix = convertFilePath(prefix);
 			filePath = NSPathUtilities.stringByAppendingPathComponent(prefix, filePath);
 		}
 		if(File.separatorChar != '/')
 			filePath = filePath.replace('/', File.separatorChar);
 		return filePath;
 	}
 	
 	public static EOEnterpriseObject parseEO(String string, EOEditingContext ec) {
 		int idx = string.indexOf(':');
 		if(idx < 0)
 			throw new IllegalArgumentException("Wrong string format");
 		String entity = string.substring(0,idx);
 		string = string.substring(idx +1);
 		try {
 			Integer pk = new Integer(string);
 			return EOUtilities.objectWithPrimaryKeyValue(ec, entity, pk);
 		} catch (NumberFormatException e) {
 			NSDictionary pKey = NSPropertyListSerialization.dictionaryForString(string);
 			return EOUtilities.objectWithPrimaryKey(ec, entity, pKey);
 		}
 	}
 	
 	public static NSMutableArray argumentsFromString(String string, EOEditingContext ec) {
 		NSArray list = NSPropertyListSerialization.arrayForString(string);
 		NSMutableArray args = new NSMutableArray(list.count());
 		Enumeration enu = list.objectEnumerator();
 		while (enu.hasMoreElements()) {
 			Object obj = (Object) enu.nextElement();
 			if(obj instanceof String) {
 				try {
 					obj = new Integer((String)obj);
 				} catch (NumberFormatException e) {
 					try {
 						obj = parseEO((String)obj, ec);
 					} catch (Exception ex) {
 						; //string may not be parsed
 					}
 				}
 			}
 			args.addObject(obj);
 		}
 		return args;
 	}
 	
 	public static String stringFromArguments(NSArray arguments) {
 		NSMutableArray result = new NSMutableArray(arguments.count());
 		Enumeration enu = arguments.objectEnumerator();
 		while (enu.hasMoreElements()) {
 			Object obj = (Object) enu.nextElement();
 			if(obj instanceof EOEnterpriseObject) {
 				obj = WOLogFormatter.formatEO((EOEnterpriseObject)obj);
 			} else if(obj == NSKeyValueCoding.NullValue) {
 				obj = "nil";
 			}
 			result.addObject(obj);
 		}
 		return NSPropertyListSerialization.stringFromPropertyList(result);
 	}
 	
 	public static void formatQualifier(EOQualifier qual, StringBuilder buf, NSMutableArray args) {
 		if(qual instanceof EOKeyValueQualifier) {
 			EOKeyValueQualifier kq = (EOKeyValueQualifier)qual;
 			buf.append(kq.key()).append(' ');
 			buf.append(EOQualifier.stringForOperatorSelector(kq.selector()));
 			Object value = kq.value();
 			if(value == null || value == NSKeyValueCoding.NullValue) {
 				buf.append(" nil");
 			} else {
 				buf.append(' ').append('%');
 				if(value instanceof EOEnterpriseObject) {
 					value = WOLogFormatter.formatEO((EOEnterpriseObject)value);
 					buf.append('@');
 				} else if(value instanceof Integer) {
 					buf.append('d');
 				} else if(value instanceof Number) {
 					buf.append('f');
 				} else if(value instanceof String) {
 					buf.append('s');
 				} else {
 					buf.append('@');
 				}
 				args.addObject(value);
 			}
 		} else if(qual instanceof EOKeyComparisonQualifier) {
 			EOKeyComparisonQualifier cq = (EOKeyComparisonQualifier)qual;
 			buf.append(cq.leftKey()).append(' ');
 			buf.append(EOQualifier.stringForOperatorSelector(cq.selector()));
 			buf.append(' ').append(cq.rightKey());
 		} else if(qual instanceof EONotQualifier) {
 			buf.append("!(");
 			formatQualifier(((EONotQualifier)qual).qualifier(), buf, args);
 			buf.append(')');
 		} else if(qual instanceof EOAndQualifier || qual instanceof EOOrQualifier) {
 			NSArray list = null;
 			String operator = null;
 			if(qual instanceof EOAndQualifier) {
 				list = ((EOAndQualifier)qual).qualifiers();
 				operator = " AND ";
 			} else {
 				list = ((EOOrQualifier)qual).qualifiers();
 				operator = " OR ";
 			}
 			Enumeration enu = list.objectEnumerator();
 			while (enu.hasMoreElements()) {
 				EOQualifier qq = (EOQualifier) enu.nextElement();
 				boolean sub = (qq instanceof EOAndQualifier || qq instanceof EOOrQualifier);
 				if(sub)
 					buf.append('(');
 				formatQualifier(qq, buf, args);
 				if(sub)
 					buf.append(')');
 				if(enu.hasMoreElements())
 					buf.append(operator);
 			}
 		} else {
 			throw new IllegalArgumentException("Unsupported qualifier type");
 		}
 	}
 }
