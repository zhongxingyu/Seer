 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.applications.rainbow.lib;
 
 import java.util.Hashtable;
 import java.util.Map;
 
 public class FormatManager {
 
 	Map<String, String> pairs;
 	
 	public void load (String p_sPath) {
 		//TODO: Load format manager data from external file
 		
 		pairs = new Hashtable<String, String>();
 		pairs.put(".xlf", "okf_xliff");
 		pairs.put(".xml", "okf_xml");
 		pairs.put(".html", "okf_html");
 		pairs.put(".htm", "okf_html");
 		pairs.put(".properties", "okf_properties");
 		pairs.put(".tmx", "okf_tmx");
 		pairs.put(".mif", "okf_mif");
 		pairs.put(".rtf", "okf_rtf");
 		pairs.put(".idml", "okf_idml");
 		pairs.put(".po", "okf_po");
 		pairs.put(".pot", "okf_po");
 		pairs.put(".docx", "okf_openxml");
 		pairs.put(".docm", "okf_openxml");
 		pairs.put(".dotm", "okf_openxml");
 		pairs.put(".xlsx", "okf_openxml");
 		pairs.put(".pptx", "okf_openxml");
 		pairs.put(".odt", "okf_openoffice");
 		pairs.put(".ott", "okf_openoffice");
 		pairs.put(".sxw", "okf_openoffice");
 		pairs.put(".stw", "okf_openoffice");
 		pairs.put(".odp", "okf_openoffice");
 		pairs.put(".otp", "okf_openoffice");
 		pairs.put(".sxi", "okf_openoffice");
 		pairs.put(".sti", "okf_openoffice");
 		pairs.put(".ods", "okf_openoffice");
 		pairs.put(".ots", "okf_openoffice");
 		pairs.put(".sxc", "okf_openoffice");
 		pairs.put(".stc", "okf_openoffice");
 		pairs.put(".odg", "okf_openoffice");
 		pairs.put(".otg", "okf_openoffice");
 		pairs.put(".sxd", "okf_openoffice");
 		pairs.put(".std", "okf_openoffice");
 		pairs.put(".sdlxliff", "okf_xliff");
 		pairs.put(".dtd", "okf_dtd");
 		pairs.put(".ts", "okf_ts");
 	}
 	
 	/**
 	 * Tries to guess the format and the encoding of a give document.
 	 * @param p_sPath Full path of the document to process.
 	 * @return An array of string: 0=guessed encoding or null,
 	 * 1=guessed filter settings or null,
 	 */
 	public String[] guessFormat (String p_sPath) {
 		String[] aRes = new String[2];
 		aRes[0] = Utils.detectEncoding(p_sPath);
 		String sExt = Utils.getExtension(p_sPath).toLowerCase();
 		aRes[1] = pairs.get(sExt);
 		return aRes;
 	}
 
 }
