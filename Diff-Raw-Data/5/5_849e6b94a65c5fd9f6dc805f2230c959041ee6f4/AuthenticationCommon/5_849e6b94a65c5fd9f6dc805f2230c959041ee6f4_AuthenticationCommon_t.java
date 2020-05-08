 /* The contents of this file are subject to the terms
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
 * $Id: AuthenticationCommon.java,v 1.9 2008-09-15 18:19:53 cmwesley Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.common.authentication;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.sun.identity.qatest.common.SMSCommon;
 import com.sun.identity.qatest.common.SMSConstants;
 import com.sun.identity.qatest.common.TestCommon;
 import java.net.URL;
 import java.util.logging.Level;
 import com.iplanet.sso.SSOToken;
 
 /**
  * This class contains helper method related to Authentication.
  */
 public class AuthenticationCommon extends TestCommon {
     
     SSOToken ssoToken;
     SMSCommon smsCommon;
     
     public AuthenticationCommon() {
         super("AuthenticationCommon");
         try{
             ssoToken = getToken(adminUser, adminPassword, basedn);
             smsCommon = new SMSCommon(ssoToken);
         } catch (Exception e) {
             e.getStackTrace();
         }
     }
     
     /**
      * Tests zero page login for given mode. This is a positive test. If the
      * login is  unsuccessfull, an error is thrown.
      */
     public void testZeroPageLoginPositive(WebClient wc, String user, 
             String password, String mode,  String modeValue, String passMsg)
     throws Exception {
         Object[] params = {user, password, mode, modeValue, passMsg};
         entering("testZeroPageLoginPositive", params);
         String strTest = null;
         try {
             strTest = protocol + ":" + "//" + host + ":" + port + uri +
                     "/UI/Login?" + mode + "=" + modeValue + "&IDToken1=" +
                     user + "&IDToken2=" + password;
             log(Level.FINEST, "testZeroPageLoginPositive", strTest);
             URL url = new URL(strTest);
             HtmlPage page = (HtmlPage)wc.getPage( url );
             
             log(Level.FINEST, "testZeroPageLoginPositive", page.getTitleText());
             // Tests for everything if mode is not set to "role" or the 
             // configured plugin is of type amsdk. 
             if (!mode.equalsIgnoreCase("role") || 
                     smsCommon.isPluginConfigured(ssoToken,
                     SMSConstants.UM_DATASTORE_SCHEMA_TYPE_AMSDK, realm )) {             
                 assert page.getTitleText().equals(passMsg);
             } else {
                 log(Level.FINEST, "testZeroPageLoginPositive", 
                         "Role based test is skipped for non amsdk plugin ...");
             }   
         } catch (Exception e) {
             log(Level.SEVERE, "testZeroPageLoginPositive", e.getMessage(),
                     params);
             e.printStackTrace();
             throw e;
         }
         exiting("testZeroPageLoginPositive");
     }
     
     /**
      * Tests zero page login for a module. This is a negative test. If the
      * login is  successfull, an error is thrown.
      */
     public void testZeroPageLoginNegative(WebClient wc, String user, 
             String password, String mode, String modeValue, String failMsg)
     throws Exception {
         Object[] params = {user, password, mode, modeValue, failMsg};
         entering("testZeroPageLoginNegative", params);
         String strTest = null;
         try {
             strTest = protocol + ":"  + "//" + host + ":" + port + uri +
                     "/UI/Login?" + mode + "=" + modeValue + "&IDToken1=" +
                    user + "&IDToken2=not" + password;
             log(Level.FINEST, "testZeroPageLoginNegative", strTest);
             URL url = new URL(strTest);
             HtmlPage page = (HtmlPage)wc.getPage( url );
             log(Level.FINEST, "testZeroPageLoginNegative", page.getTitleText());
             
             // Tests for everything if mode is not set to "role" or the 
             // configured plugin is of type amsdk. 
             if (!mode.equalsIgnoreCase("role") || 
                     smsCommon.isPluginConfigured(ssoToken,
                     SMSConstants.UM_DATASTORE_SCHEMA_TYPE_AMSDK, realm )) {             
                 assert page.getTitleText().equals(failMsg);
             } else {
                 log(Level.FINEST, "testZeroPageLoginNegative", 
                         "Role based test is skipped for non amsdk plugin ...");
             } 
         } catch (Exception e) {
             log(Level.SEVERE, "testZeroPageLoginNegative", e.getMessage(),
                     params);
             e.printStackTrace();
             throw e;
         }
         exiting("testZeroPageLoginNegative");
     }
     
     /**
      * Tests zero page login for given mode. This is a test for a valid user
      * but the user session has expired.
      */
     public void testZeroPageLoginFailure(WebClient wc, String user, 
             String password, String mode, String modeValue, String passMsg)
     throws Exception {
         Object[] params = {user, password, mode, modeValue, passMsg};
         entering("testZeroPageLoginFailure", params);
         try {
             String strTest = protocol + ":" + "//" + host + ":" + port + uri +
                     "/UI/Login?" + mode + "=" + modeValue + "&IDToken1=" +
                     user + "&IDToken2=" + password;
             log(Level.FINEST, "testZeroPageLoginFailure", strTest);
             URL url = new URL(strTest);
             HtmlPage page = (HtmlPage)wc.getPage( url );
             log(Level.FINEST, "testZeroPageLoginFailure", page.getTitleText());
             
             // Tests for everything if mode is not set to "role" or the 
             // configured plugin is of type amsdk. 
             if (!mode.equalsIgnoreCase("role") || 
                     smsCommon.isPluginConfigured(ssoToken,
                     SMSConstants.UM_DATASTORE_SCHEMA_TYPE_AMSDK, realm )) {             
                 assert page.getTitleText().equals(passMsg);
             } else {
                 log(Level.FINEST, "testZeroPageLoginFailure", 
                         "Role based test is skipped for non amsdk plugin ...");
             } 
         } catch (Exception e) {
             log(Level.SEVERE, "testZeroPageLoginFailure", e.getMessage(),
                     params);
             e.printStackTrace();
             throw e;
         }
         exiting("testZeroPageLoginFailure");
     }
     
     /**
      * Tests zero page login for a anonymous user. This is a poitive test. If
      * the login is unsuccessfull, an error is thrown.
      */
     public void testZeroPageLoginAnonymousPositive(WebClient wc, String user, 
             String password,String mode, String modeValue, String passMsg)
     throws Exception {
         Object[] params = {user, password, mode, modeValue, passMsg};
         entering("testZeroPageLoginAnonymousPositive", params);
         String strTest = null;
         try {
             strTest = protocol + ":"  + "//" + host + ":" + port + uri +
                     "/UI/Login?" + mode + "=" + modeValue + "&IDToken1=" +
                     user;
             log(Level.FINEST, "testZeroPageLoginAnonymousPositive", strTest);
             URL url = new URL(strTest);
             HtmlPage page = (HtmlPage)wc.getPage( url );
             log(Level.FINEST, "testZeroPageLoginAnonymousPositive",
                     page.getTitleText());
             
             // Tests for everything if mode is not set to "role" or the 
             // configured plugin is of type amsdk. 
             if (!mode.equalsIgnoreCase("role") || 
                     smsCommon.isPluginConfigured(ssoToken,
                     SMSConstants.UM_DATASTORE_SCHEMA_TYPE_AMSDK, realm )) {             
                 assert page.getTitleText().equals(passMsg);
             } else {
                 log(Level.FINEST, "testZeroPageLoginAnonymousPositive", 
                         "Role based test is skipped for non amsdk plugin ...");
             } 
         } catch (Exception e) {
             log(Level.SEVERE, "testZeroPageLoginAnonymousPositive",
                     e.getMessage(),
                     params);
             e.printStackTrace();
             throw e;
         }
         exiting("testZeroPageLoginAnonymousPositive");
     }
     
     /**
      * Tests zero page login for a anonymous user. This is a negative test. If
      * the login is successfull, an error is thrown.
      */
     public void testZeroPageLoginAnonymousNegative(WebClient wc, String user, 
             String password, String mode, String modeValue, String failMsg)
     throws Exception {
         Object[] params = {user, password, mode, modeValue, failMsg};
         entering("testZeroPageLoginAnonymousNegative", params);
         String strTest = null;
         try {
             strTest = protocol + ":"  + "//" + host + ":" + port + uri +
                     "/UI/Login?" + mode + "=" + modeValue + "&IDToken1=" +
                     user + "negative";
             log(Level.FINEST, "testZeroPageLoginAnonymousNegative", strTest);
             URL url = new URL(strTest);
             HtmlPage page = (HtmlPage)wc.getPage( url );
             log(Level.FINEST, "testZeroPageLoginAnonymousNegative",
                     page.getTitleText());
             
             // Tests for everything if mode is not set to "role" or the 
             // configured plugin is of type amsdk. 
             if (!mode.equalsIgnoreCase("role") || 
                     smsCommon.isPluginConfigured(ssoToken,
                     SMSConstants.UM_DATASTORE_SCHEMA_TYPE_AMSDK, realm )) {             
                 assert page.getTitleText().equals(failMsg);
             } else {
                 log(Level.FINEST, "testZeroPageLoginAnonymousPositive", 
                         "Role based test is skipped for non amsdk plugin ...");
             } 
         } catch (Exception e) {
             log(Level.SEVERE, "testZeroPageLoginAnonymousNegative",
                     e.getMessage(),
                     params);
             e.printStackTrace();
             throw e;
         }
         exiting("testZeroPageLoginAnonymousNegative");
     }
     
     /**
      * Retrieve the SMSCommon instance.
      * @return <code>SMSCommon</code>
      */
     public SMSCommon getSMSCommon() { return smsCommon; }
     
     
 }
