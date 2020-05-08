 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 
 package org.bedework.webdav.servlet.common;
 
 import java.util.Collection;
 import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
 
 /** Place for utility methods
  *
  *   @author Mike Douglass   douglm  rpi.edu
  */
 public class WebdavUtils {
   /** Get the prefix from the request
    *
    * @param req
    * @return String prefix
    */
   public static String getUrlPrefix(final HttpServletRequest req) {
     try {
       String url = req.getRequestURL().toString();
 
       String contextPath = req.getContextPath();
       if ((contextPath == null) || (contextPath.equals("."))) {
         contextPath = "/";
       }
 
       String sp = req.getServletPath();
       if ((sp == null) || (sp.equals("."))) {
         sp = "/";
       }
 
       String prefix = Util.buildPath(false, contextPath, "/", sp);
       if (prefix.equals("/")) {
         prefix = "";
       }
 
       final int pos = url.indexOf(prefix);
 
       if (pos > 0) {
         url = url.substring(0, pos);
       }
 
       return url + prefix;
     } catch (final Throwable t) {
       Logger.getLogger(WebdavUtils.class).warn(
           "Unable to get url from " + req);
       return "BogusURL.this.is.probably.a.portal";
     }
   }
 
   /**
    * @param c
    * @return boolean true for empty or null Collection
    */
   public static boolean emptyCollection(Collection c) {
     return (c == null) || (c.size() == 0);
   }
 }
 
