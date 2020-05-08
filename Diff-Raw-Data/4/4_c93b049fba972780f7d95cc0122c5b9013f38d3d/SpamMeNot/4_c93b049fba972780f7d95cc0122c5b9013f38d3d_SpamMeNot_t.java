 package com.aquent.viewtools;
 
 import org.apache.velocity.tools.view.tools.ViewTool;
 
 import com.dotmarketing.util.Logger;
 
 import java.util.Random;
 import java.lang.StringBuffer;
 
 /**
  * 
  * @author cfalzone
  * 
  * A Viewtool Implementation of the Spam-me-not Java version by Bjrn Bergenheim
  * Original Version can be found at:  http://paula.edmiston.org/nb/spamx/SpamMeNot.java
  *
  */
 public class SpamMeNot implements ViewTool {
 
 	@Override
 	public void init(Object initData) {
 		Logger.info(this, "Spam Me Not Viewtool Initialized");
 	}
 	
 	/**
 	 * Encode email addresses to make it harder for spammers to harvest them.
 	 * 
 	 * @param emailAddress    the address to encode
 	 * @param name            the name to be displayed in statusbar
 	 * @return                a complete email link
 	 */
 	public String encodeEmail(String emailAddress, String name) {
 		if( name == null) name = "";
 		String originalString = "mailto:" + emailAddress;
 
		String encodedMailto = encodeString(originalString);
		String encodedName = encodeString(name);
 
 		//if no name is supplied, use email in statusbar
 		if( name.length() == 0 ) 
 			return "<a href=\"" + encodedMailto + "\">" + encodedMailto + "</a>";
 		else 
 			return "<a href=\"" + encodedMailto + "\">" + encodedName + "</a>";
 	}
 	
 	/**
 	 * Encode a string 
 	 * 
 	 * @param s		The String to Encode
 	 * @return		The encoded String
 	 */
 	public String encodeString(String s) {
 		long seed = System.currentTimeMillis();
 		Random random = new Random(seed);
 		
 		if(s == null) s = "";
 		
 		StringBuffer encodedString = new StringBuffer();
 		int originalLenght = s.length();
 		
 		int i;
 		for (i = 0; i < originalLenght; i++) {
 			switch (random.nextInt(3)) {
 				case 0 : //Decimal code
 					encodedString.append("&#" + (int)s.charAt(i) + ";");
 					break;
 				case 1 : //Hexadecial code
 					encodedString.append("&#x" + Integer.toHexString(s.charAt(i)) + ";");
 					break;
 				case 2 : //No action code
 					encodedString.append(s.charAt(i));
 					break;
 			}
 		}
 		
 		return encodedString.toString();
 	}
 
 }
