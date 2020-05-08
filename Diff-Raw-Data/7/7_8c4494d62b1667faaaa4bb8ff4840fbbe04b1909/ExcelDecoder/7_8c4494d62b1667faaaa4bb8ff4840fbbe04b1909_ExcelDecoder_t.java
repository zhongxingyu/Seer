 /* ******************************************************** 
 ** Copyright (c) 2000-2001, Thomas Maxwell White, all rights reserved. 
 ** $Header$
 ******************************************************** */ 
 
 package org.dianexus.triceps;
 
 /** Microsoft Excel 97 systematically corrupts column data in a number of semi-defined ways.  This class fixes as much as possible.
  * (0) Let <i>stringize</i> be the process of surrounding data (e.g. from a column) with quote charcters (e.g. <i>hi there</i> becomes <i>"hi there"</i>)
  * (1) Frequently, but not always, Excel <i>stringizes</i> columns that contain whitespace (preceding, terminating, or internal).
  * (2) Frequently, when an internal quote character is detected, Excel <i>stringizes</i> the column, and replaces the internal quotes with paired quotes (" -> "")
  * (3) Excel has a buffer overflow bug when importing tab delimited data - no errors are reported, but the data is truncted, and subsequent rows are corrupted.
 **/
 class ExcelDecoder implements VersionIF {
 	/** Convert Excel-encoded string (whether <i>stringized</i> or not), to standard Java String format
 	**/
 	static String decode(String s) { 
		if (s.startsWith("\"") && s.endsWith("\"") && s.length() > 1) {
 			StringBuffer sb = new StringBuffer();
 
 			int start=1;
 			int stop=0;
 			while ((stop = s.indexOf("\"\"",start)) != -1) {
 				sb.append(s.substring(start,stop));
 				sb.append("\"");
 				start = stop+2;
 			}
			if (s.length() - 1 > start) {
				sb.append(s.substring(start,s.length()-1));
			}
 			return sb.toString();
 		}
 		else {
 			return s;
 		}
 	}
 }
