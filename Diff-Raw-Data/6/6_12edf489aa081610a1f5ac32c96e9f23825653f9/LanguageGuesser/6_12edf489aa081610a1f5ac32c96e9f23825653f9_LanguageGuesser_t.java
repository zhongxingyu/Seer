 package com.gentics.cr.analytics.language;
 
 import java.io.IOException;
 import java.io.InputStream;
 
import org.apache.nutch.analysis.lang.custom.LanguageIdentifier;
 /**
  * 
  * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
  * @version $Revision: 180 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class LanguageGuesser {
 	private static LanguageIdentifier langID = null;
 	
 	/**
 	 * Detect language from a String
 	 * @param text
 	 * @return detected language
 	 */
 	public static String detectLanguage(String text)
 	{
 		LanguageIdentifier li = getLIInstance();
 		
 		return li.identify(text);
 	}
 	
 	/**
 	 * Detect language from an input stream
 	 * @param is
 	 * @return detected language
 	 * @throws IOException
 	 */
 	public static String detectLanguage(InputStream is) throws IOException
 	{
 		LanguageIdentifier li = getLIInstance();
 		return li.identify(is);
 	}
 	
 	/**
 	 * Detect language from an input stream
 	 * @param is
 	 * @param charset
 	 * @return detected language
 	 * @throws IOException
 	 */
 	public static String detectLanguage(InputStream is, String charset) throws IOException
 	{
 		LanguageIdentifier li = getLIInstance();
 		return li.identify(is,charset);
 	}
 	
 	private static LanguageIdentifier getLIInstance()
 	{
 		if(langID==null)
 		{
			langID = new LanguageIdentifier();
 		}
 		return(langID);
 	}
 }
