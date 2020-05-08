 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.util;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.apache.commons.logging.Log;

 public class HttpUtil {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
 	
     public static String getCookieValue(String name, HttpServletRequest request) {
         Cookie[] cookies = request.getCookies();
         if (cookies != null) {
             for (Cookie cookie : cookies) {
                 if (name.equals(cookie.getName())) {
                     return cookie.getValue();
                 }
             }
         }
         return null;
     }
 
     public static void setCookie(String name, String value, int maxAge, HttpServletResponse response) {
         Cookie cookie = new Cookie(name, value);
         cookie.setMaxAge(maxAge);
         cookie.setPath("/orangeleap");
         response.addCookie(cookie);
     }
 
     public static void removeCookie(String name, HttpServletResponse response) {
         Cookie cookie = new Cookie(name, "");
         cookie.setMaxAge(0);
         cookie.setPath("/orangeleap");
         response.addCookie(cookie);
     }
 
     public static String getUserIpAddress(HttpServletRequest request) {
         return request.getRemoteAddr();
     }
 
     public static String jsEscape(String s) {
     	if (s != null) {
			s = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'").replace("\r","").replace("\n","\\n");
     	}
     	return s;
     }
 }
