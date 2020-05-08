 package com.madalla.webapp.email;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.madalla.bo.security.UserData;
 
 public class EmailFormatter {
 
 	public static String getUserEmailBody(UserData user, String message) {
 		StringBuffer sb = new StringBuffer("Dear" +
				(StringUtils.isNotEmpty(" "+user.getFirstName())?user.getFirstName():""+
				(StringUtils.isNotEmpty(" "+user.getLastName())?user.getLastName():""))+",");
 		sb.append(System.getProperty("line.separator"));
 		sb.append(message);
 		return sb.toString();
 	}
 
 }
