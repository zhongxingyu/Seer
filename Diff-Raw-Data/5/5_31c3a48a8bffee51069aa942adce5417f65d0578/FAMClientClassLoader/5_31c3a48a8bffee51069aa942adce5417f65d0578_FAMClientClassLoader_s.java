 /*
  * The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: FAMClientClassLoader.java,v 1.2 2007-09-04 21:55:41 mrudul_uchil Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.wssagents.common;
 
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 public class FAMClientClassLoader {
     
     public static URLClassLoader urlCls;
     
     /** Constant for file separator */
     public static final String FILE_SEPARATOR = "/";
     
     /**
      * Returns a classloader to load fmclientsdk.jar
      */
     public static URLClassLoader getFAMClientClassLoader() {
         if (urlCls == null) {
             try {
                 String FILE_BEGIN = "file:";
                 String osName = System.getProperty("os.name");
                 if((osName != null) && 
                     (osName.toLowerCase().startsWith("windows"))) {
                     FILE_BEGIN = "file:/";
                 }
                 URL urls[] = new URL[3];
                 String installRoot = System.getProperty(
                     "com.sun.aas.installRoot");
                 String instanceRoot = System.getProperty(
                     "com.sun.aas.instanceRoot");
                 String clientSDKPath = FILE_BEGIN + installRoot +
                     FILE_SEPARATOR + "addons" + FILE_SEPARATOR +
                    "accessmanager" + FILE_SEPARATOR + "famclientsdk.jar";
                 String configPath = FILE_BEGIN + instanceRoot +
                     FILE_SEPARATOR + "config" + FILE_SEPARATOR;
                 String xmlsecPath = FILE_BEGIN + installRoot +
                     FILE_SEPARATOR + "lib" + FILE_SEPARATOR +
                     "webservices-rt.jar";
                 
                 if (_logger != null) {
                     _logger.log(Level.FINE, "FAMHttpAuthModule.initialize:"+
                         " clientSDKPath : " + clientSDKPath +
                         " configPath : " + configPath +
                         " xmlsecPath : " + xmlsecPath);
                 }
                 urls[0] = new URL(clientSDKPath);
                 urls[1] = new URL(configPath);
                 urls[2] = new URL(xmlsecPath);
                 urlCls = new FAMURLClassLoader(urls,
                     Thread.currentThread().getContextClassLoader().
                                                getParent());
             } catch (Exception ex) {
                 if(_logger != null) {
                     _logger.log(Level.SEVERE,
                         "FAMClientClassLoader.getAMClientClassLoader failed");
                 }
                 ex.printStackTrace();
             }
         }
         return (urlCls);
     }
     
     private static Logger _logger = null;
     static {
         LogManager logManager = LogManager.getLogManager();
         _logger = logManager.getLogger(
             "javax.enterprise.system.core.security");
     }
 }
