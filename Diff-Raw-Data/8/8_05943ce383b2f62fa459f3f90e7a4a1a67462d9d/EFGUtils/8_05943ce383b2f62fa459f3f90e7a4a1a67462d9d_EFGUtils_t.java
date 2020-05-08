 /**
  * $Id: EFGUtils.java,v 1.1.1.1 2007/08/01 19:11:27 kasiedu Exp $
  *
  * Copyright (c) 2003  University of Massachusetts Boston
  *
  * Authors: Jacob K Asiedu, Kimmy Lin
  *
  * This file is part of the UMB Electronic Field Guide.
  * UMB Electronic Field Guide is free software; you can redistribute it
  * and/or modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2, or
  * (at your option) any later version.
  *
  * UMB Electronic Field Guide is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the UMB Electronic Field Guide; see the file COPYING.
  * If not, write to:
  * Free Software Foundation, Inc.
  * 59 Temple Place, Suite 330
  * Boston, MA 02111-1307
  * USA
  */
 
 package project.efg.util.utils;
 
 import java.beans.Introspector;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.util.Date;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import project.efg.util.interfaces.EFGImportConstants;
 
 import com.Ostermiller.util.ExcelCSVParser;
 import com.Ostermiller.util.LabeledCSVParser;
 
 /**
  * This file contains the class EFGUtils, which has many static fields and
  * methods available to the EFG. Usually, items are added here when it's noticed
  * that similar functionality is present in many other classes. It would
  * probably be a good idea to periodically go through this file and make sure
  * that the fields and methods present shouldn't be in a particular class.
  */
 public class EFGUtils {
 	
 	private static String currentTableName = null;
 	private static String[] metadataTableHeaders = null;
 
 	static Logger log = null;
 	static {
 		try {
 			log = Logger.getLogger(EFGUtils.class);
 		} catch (Exception ee) {
 		}
 	}
 	public synchronized static String constructCacheKey(String ds, String bool){
 		StringBuffer key = new StringBuffer(ds);
 		key.append(bool);
 		return key.toString().toLowerCase();
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public synchronized static String getCurrentTableName() {
 		if(currentTableName == null || "".equals(currentTableName.trim())) {
 			currentTableName =EFGImportConstants.EFG_RDB_TABLES;
 		}
 		return currentTableName;
 	}
 	/**
 	 * 
 	 * @param tableName
 	 */
 	public synchronized static void setTableName(String tableName) {
 		currentTableName = tableName;
 	}
 	/**
 	 * @return a string with a maximum of 240 characters
 	 * @param string -
 	 *            The string to parse
 	 */
 	public synchronized static String parse240(String string) {
 		if (string == null) {
 			return "";
 		}
 		string = string.trim();
 		if (string.length() > 240) {
 			string = string.substring(0, 240);
 		}
 		return string;
 	}
 
 	/**
 	 * @return a string form of a uri where uri separators are replaced with EFg
 	 *         separators
 	 * @param string -
 	 *            The uri form of a string to parse
 	 */
 	public synchronized static String parseEFGSEP(String string) {
 		string = string.replaceAll(project.efg.util.interfaces.RegularExpresionConstants.FORWARD_SLASH,
 				project.efg.util.interfaces.EFGImportConstants.EFG_SEP);
 		return string.replaceAll(project.efg.util.interfaces.RegularExpresionConstants.COLONSEP,
 				project.efg.util.interfaces.EFGImportConstants.EFG_COLON);
 	}
 	/**
 	 * @return a string with EFG separators replaced with uri separators
 	 * @param string -
 	 *            The string to parse
 	 */
 	public synchronized static String reverseParseEFGSEP(String string) {
 		string = string.replaceAll(project.efg.util.interfaces.EFGImportConstants.EFG_SEP,
 				project.efg.util.interfaces.RegularExpresionConstants.FORWARD_SLASH);
 		return string.replaceAll(project.efg.util.interfaces.EFGImportConstants.EFG_COLON,
 				project.efg.util.interfaces.RegularExpresionConstants.COLONSEP);
 	}
 	/**
 	 * Return an identifier that can be used to create a Table name
 	 * this implementation removes all non-english alphabets in the String
 	 * but if the string length after that is 0 it supplies some generic string
 	 * Note that some random numbers are appended to the table names before 
 	 * they are created.
 	 * @param originalName
 	 * @return
 	 */
 	public synchronized static String getDataBaseTableName(URI fullFileName) {
 		
 		if(fullFileName == null) {
 			return null;
 		}
 		String name = fullFileName.toString();
 		try {
 			
 			File f = new File(name);
 			name = f.getName();
 			int index = name.lastIndexOf(".");
 			if (index > -1) {
 				name = name.substring(0, index);
 			}
 		} catch (Exception ee) {
 
 		}
 		name = name.replaceAll("[^A-Za-z]", "");
 		if(name.trim().equals("")) {
 			name = "EFG_SUPPLIED_TABLE_NAME";
 		}
 		return name.toLowerCase();
 	}
 	   
 	/**
 	 * @param fullName -
 	 *            The full path to a file
 	 * @return the name of a file without the extension
 	 */
 	public synchronized static String getName(URI fullFileName) {
 		String name = fullFileName.toString();
 		try {
 			
 			File f = new File(name);
 			name = f.getName();
 			int index = name.lastIndexOf(".");
 			if (index > -1) {
 				return name.substring(0, index);
 			}
 		} catch (Exception ee) {
 
 		}
 		return name;
 	}
 	/**
 	 * 
 	 * @return an Array of static metadata headers
 	 */
 	public static String[] getMetadataTableHeaders() {
 		if (metadataTableHeaders == null) {
 			log.debug("About to read metadata files for the first time");
 			metadataTableHeaders = readMetadataTableHeaders();
 			log.debug("Read metadata files for the first time");
 			if (metadataTableHeaders == null) {
 				log.debug("MetadataTable file Could not be read");
 			}
 		}
 		return metadataTableHeaders;
 	}
 	/**
 	 * 
 	 * @return the environment variables 
 	 */
 	public static Properties getEnvVars() {
 		Process p = null;
 		Properties envVars = new Properties();
 		try {
 			Runtime r = Runtime.getRuntime();
 			String OS = System.getProperty("os.name").toLowerCase();
 			if (OS.indexOf("windows 9") > -1) {
 				p = r.exec("command.com /c set");
 			} else if ((OS.indexOf("nt") > -1)
 					|| (OS.indexOf("windows 20") > -1)
 					|| (OS.indexOf("windows xp") > -1)) {
 				p = r.exec("cmd.exe /c set");
 			} else {
 				// our last hope, we assume Unix (thanks to H. Ware for the fix)
 				p = r.exec("env");
 			}
 			if (p != null) {
 				BufferedReader br = new BufferedReader(new InputStreamReader(p
 						.getInputStream()));
 				String line;
 				while ((line = br.readLine()) != null) {
 					int idx = line.indexOf('=');
 					if (idx > 0) {
 						String key = line.substring(0, idx);
 						String value = line.substring(idx + 1);
 						envVars.setProperty(key, value);
 					}
 				}
 			}
 			
 		} catch (Exception ee) {
 			String message = ee.getMessage();
 			log.error(message);
 		}
 		return envVars;
 	}
 
 	/**
 	 * 
 	 * @return get the full directory path to Tomcat
 	 */
 	public static String getServerHome() {
 		return EFGImportConstants.EFGProperties.getProperty("efg.serverlocations.current");
 	}
 
 	/**
 	 * Writes msg.toString() to System.err and then flushes the stream.
 	 * 
 	 * @param msg
 	 *            msg object to write
 	 */
 	public static void log(Object msg) {
 		if (msg == null) {
 			System.err.println("");
 		} else {
 			System.err.println(new Date().toString() + "-------- "
 					+ msg.toString());
 		}
 		System.err.flush();
 	}
 	
 	/**
 	 * This method takes a String and 
 	 * returns a returns a string that can be used as a field
 	 * name in a relational database.
 	 * 
 	 * @param origString
 	 *            the pre-encoded string
 	 * @return a string that can be used as
 	 * a relational database field name
 	 */
 	public static String encodeToFieldName(String origString) {
 		String str = origString;
 		while(EFGImportConstants.SQL_KEY_WORDS.contains(str.toLowerCase())){
 			str = str + "_";
 		}
 		return Introspector.decapitalize(toRDBFieldName(str));
 	}
 
 	/**
 	 * 
 	 * Convert a string to a relational database field name
 	 * 
 	 */
 	private static String toRDBFieldName(String origString) {
 		StringBuffer sb = new StringBuffer(origString);
 		int strLength = sb.length();
 		
 		//move the character to the end of the string		
 		if (strLength > 0 && !Character.isJavaIdentifierStart(sb.charAt(0))){
 			
 			//make a new field name and prepend a constant 'E' to it
 			long legalName = EFGUniqueID.getID();
 			//sb.setCharAt(0, '_');
 			sb = new StringBuffer();
 			sb.append(EFGImportConstants.FIELDS_BEGIN_CHAR + legalName);
 			return sb.toString();
 		}
		for (int i = 1; i < strLength; i++){
			if (!Character.isJavaIdentifierPart(sb.charAt(i))){
				sb.setCharAt(i, '_');
			}
		}
 		return sb.toString();
 	}
 
 	private static String[] readMetadataTableHeaders() {
 		try {
 			InputStream stream = project.efg.util.utils.EFGUtils.class
 					.getResourceAsStream(project.efg.util.interfaces.EFGImportConstants.CSV_METADATA_HEADERS);
 	
 			LabeledCSVParser lcsvp = new LabeledCSVParser(new ExcelCSVParser(
 					stream));
 			if (lcsvp != null) {
 				return lcsvp.getLabels();
 			}
 		} catch (Exception ee) {
 			log.error(" An error occured during importation of : "
 					+ project.efg.util.interfaces.EFGImportConstants.CSV_METADATA_HEADERS + "\n");
 			log.error(ee.getMessage());
 		}
 		return null;
 	}
 
 }
 
 // $Log: EFGUtils.java,v $
 // Revision 1.1.1.1  2007/08/01 19:11:27  kasiedu
 // efg2.1.1.0 version of efg2
 //
 // Revision 1.5  2007/01/21 02:07:18  kasiedu
 // no message
 //
 // Revision 1.4  2007/01/14 17:29:41  kasiedu
 // no message
 //
 // Revision 1.3  2007/01/14 15:54:57  kasiedu
 // no message
 //
 // Revision 1.2  2006/12/08 03:50:58  kasiedu
 // no message
 //
 // Revision 1.1.2.5  2006/11/16 19:15:48  kasiedu
 // no message
 //
 // Revision 1.1.2.4  2006/08/26 22:12:24  kasiedu
 // Updates to xsl files
 //
 // Revision 1.1.2.3  2006/08/09 18:55:24  kasiedu
 // latest code confimrs to what exists on Panda
 //
 // Revision 1.1.2.2  2006/07/11 21:46:12  kasiedu
 // "Added more configuration info"
 //
 // Revision 1.1.2.1  2006/06/08 13:27:40  kasiedu
 // New files
 //
 // Revision 1.2 2006/02/25 13:16:42 kasiedu
 // no message
 //
 // Revision 1.1.1.1 2006/01/25 21:03:48 kasiedu
 // Release for Costa rica
 //
 // Revision 1.1.1.1 2003/10/17 17:03:09 kimmylin
 // no message
 //
 // Revision 1.3 2003/08/20 18:45:42 kimmylin
 // no message
 //
