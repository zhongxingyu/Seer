 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
  *
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
 * $Id: Main.java,v 1.6 2008-06-25 05:44:12 qcheng Exp $
  *
  */
 
 package com.sun.identity.tools.bundles;
 
 import com.iplanet.am.util.SystemProperties;
 import com.sun.identity.setup.Bootstrap;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 public class Main implements SetupConstants{
     public static void main(String[] args) {
         ResourceBundle bundle = ResourceBundle.getBundle(System.getProperty(
             SETUP_PROPERTIES_FILE, DEFAULT_PROPERTIES_FILE));
 
         if ((System.getProperty(PRINT_HELP) != null) &&
             System.getProperty(PRINT_HELP).equals(YES)){
             SetupUtils.printUsage(bundle);
             System.exit(0);
         }
 
         if (System.getProperty(CHECK_VERSION) != null) {
             if (System.getProperty(CHECK_VERSION).equals(YES)) {
                 System.exit(VersionCheck.isValid());
             }
         }
 
         boolean loadConfig = (System.getProperty(CONFIG_LOAD) != null) &&
             System.getProperty(CONFIG_LOAD).equals(YES);
         String configPath = null;
         String currentOS = SetupUtils.determineOS();
         Properties configProp = null;
         
         if (loadConfig) {
             configPath = System.getProperty(AMCONFIG_PATH);
             try {
                 if ((configPath == null) || (configPath.length() == 0)) {
                     configPath = SetupUtils.getUserInput(bundle.getString(
                         currentOS + QUESTION));
                 }
                 
                 configProp = Bootstrap.load(configPath, false);
 
                 if (configProp == null) {
                     System.out.println(bundle.getString("message.error.dir"));
                     System.exit(1);
                 }
 
                 if (!configPath.endsWith(FILE_SEPARATOR)) {
                     configPath = configPath + FILE_SEPARATOR;
                 }
 
                 configProp.setProperty(USER_INPUT,
                     configPath.substring(0, configPath.length() - 1));
                 configProp.setProperty(CURRENT_PLATFORM, currentOS);
             } catch (Exception ex) {
                 System.out.println(bundle.getString("message.error.dir"));
                 System.exit(1);
             }
         } else {
             configProp = new Properties();
         }
         SetupUtils.evaluateBundleValues(bundle, configProp);
         try {
             SetupUtils.copyAndFilterScripts(bundle, configProp);
             if (loadConfig) {
                 System.out.println(bundle.getString(
                     "message.info.version.tools") + " " +
                     bundle.getString(TOOLS_VERSION));
                 System.out.println(
                     bundle.getString("message.info.version.am") +
                     " " + SystemProperties.get("com.iplanet.am.version"));
             }
         } catch (IOException ex) {
             System.out.println(bundle.getString("message.error.copy"));
             System.exit(1);
         }
     }
 }
 
