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
 * $Id: CreateTestXML.java,v 1.4 2007-08-17 20:06:20 sridharev Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.common.authentication;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 /**
  * <code>CreateTestXML</code> is a helper class to create the XML file for the
  * form based validation for each of the forms.
  * This is xml used by the <code>WebTest</code> to verify the test.
  */
 public class CreateTestXML {
     
     private static String newline = System.getProperty("line.separator");
     private static String fileseparator = System.getProperty("file.separator");
     private String fileName;
     
     /**
      * Default constructor
      */
     public CreateTestXML() {
     }
     
     /**
      * Creates the service based form Login XML
      * @param Map contains test related data
      * @param true if is is negative test
      * @return xml file name
      */
     public String createServiceXML(Map testMap, boolean testNegative)
     throws Exception {
         String users = (String)testMap.get("users");
         String passMsg = (String)testMap.get("passmessage");
         String failMsg = (String)testMap.get("failmessage");
         String testURL = (String)testMap.get("url");
         String baseDirectory = (String)testMap.get("baseDir");
         String loginService = (String)testMap.get("servicename");
         if (!testNegative) {
             fileName = baseDirectory + loginService + "-positive.xml";
         } else {
             fileName = baseDirectory + loginService + "-negative.xml";
         }
         PrintWriter out = new PrintWriter(new BufferedWriter
                 (new FileWriter(fileName)));
         out.write("<url href=\"" + testURL + "/UI/Login?service=" +
                 loginService);
         out.write("\">");
         out.write(newline);
         StringTokenizer testUsers = new StringTokenizer(users,"|");
         List<String> testUserList = new ArrayList<String>();
         int tokennumber = testUsers.countTokens();
         while (testUsers.hasMoreTokens()) {
             testUserList.add(testUsers.nextToken());
         }
         int totalforms = testUserList.size();
         int formcount = 0;
         for (String testUserName: testUserList) {
             formcount = formcount + 1;
             String tuser;
             String tpass;
             int uLength = testUserName.length();
             int uIndex = testUserName.indexOf(":");
             tuser= testUserName.substring(0,uIndex);
             tpass = testUserName.substring(uIndex+1,uLength);
             out.write("<form name=\"Login\" IDButton=\"\" >");
             out.write(newline);
             out.write("<input name=\"IDToken1\" value=\"" + tuser + "\" />");
             out.write(newline);
             out.write("<input name=\"IDToken2\" value=\"" + tpass + "\" />");
             out.write(newline);
             if (formcount == totalforms) {
                 if (!testNegative) {
                     out.write("<result text=\"" + passMsg + "\" />");
                 } else {
                     out.write("<result text=\"" + failMsg + "\" />");
                 }
                 out.write(newline);
                 out.write("</form>");
                 out.write(newline);
                 out.write("</url>");
                 out.write(newline);
             } else {
                 out.write("</form>");
                 out.write(newline);
             }
         }
         out.flush();
         out.close();
         
         return fileName;
     }
     
     /**
      * Creates the module based authentication form Login XML
      * @param Map contains test related data
      * @param true if it is negative test
      * @return xml file name
      */
     public String createModuleXML(Map testMap, boolean testNegative)
     throws Exception {
         String userName = (String)testMap.get("userName");
         String password = (String)testMap.get("password");
         String passmessage = (String)testMap.get("modulePassMsg");
         String failmessage = (String)testMap.get("moduleFailMsg");
         String redirectURL = (String)testMap.get("redirectURL");
         String baseDirectory = (String)testMap.get("baseDir");
         String strIdentifier = (String)testMap.get("uniqueIdentifier");
         if (!testNegative) {
             fileName = baseDirectory  + strIdentifier + "-module-positive.xml";
         } else {
             fileName = baseDirectory + strIdentifier + "-module-negative.xml";
             password = password + "tofail";
         }
         PrintWriter out = new PrintWriter(new BufferedWriter
                 (new FileWriter(fileName)));
         out.write("<url href=\"" + redirectURL);
         out.write("\">");
         out.write(newline);
         out.write("<form name=\"Login\" IDButton=\"\" >");
         out.write(newline);
         out.write("<input name=\"IDToken1\" value=\"" + userName + "\" />");
         out.write(newline);
         out.write("<input name=\"IDToken2\" value=\"" + password + "\" />");
         out.write(newline);
         if (!testNegative) {
             out.write("<result text=\"" + passmessage + "\" />");
         } else {
             out.write("<result text=\"" + failmessage + "\" />");
         }
         out.write(newline);
         out.write("</form>");
         out.write(newline);
         out.write("</url>");
         out.write(newline);
         out.flush();
         out.close();
         
         return fileName;
     }
     
     /**
      * Creates the Module based Goto or GotoOnFail URL form Login XML
      * @param Map contains test related data
      * @param true if is is negative test
      * @return xml file name
      */
     public String createModuleGotoXML(Map testMap, boolean testNegative)
     throws Exception {
         String userName = (String)testMap.get("userName");
         String password = (String)testMap.get("password");
         String passmessage = (String)testMap.get("modulePassMsg");
         String failmessage = (String)testMap.get("moduleFailMsg");
         String redirectURL = (String)testMap.get("redirectURL");
         String baseDirectory = (String)testMap.get("baseDir");
         String strIdentifier = (String)testMap.get("uniqueIdentifier");
         String gotoURL = (String)testMap.get("gotoURL");
         if (!testNegative) {
             fileName = baseDirectory + strIdentifier + "-goto.xml";
         } else {
             fileName = baseDirectory  + strIdentifier + "-gotofail.xml";
			password = password + "tofail";
         }
         PrintWriter out = new PrintWriter(new BufferedWriter
                 (new FileWriter(fileName)));
         out.write("<url href=\"" + gotoURL);
         out.write("\">");
         out.write(newline);
         out.write("<form name=\"Login\" IDButton=\"\" >");
         out.write(newline);
         out.write("<input name=\"IDToken1\" value=\"" + userName + "\" />");
         out.write(newline);
         out.write("<input name=\"IDToken2\" value=\"" + password + "\" />");
         out.write(newline);
         if (!testNegative) {
             out.write("<result text=\"" + passmessage + "\" />");
         } else {
             out.write("<result text=\"" + failmessage + "\" />");
         }
         out.write(newline);
         out.write("</form>");
         out.write(newline);
         out.write("</url>");
         out.write(newline);
         out.flush();
         out.close();
         
         return fileName;
     }
     
     /*
      * Create the required XML files for the Account Lockout/warning tests
      */
     public String createLockoutXML(Map testMap, boolean isWarning)
     throws Exception {
         String userName = (String)testMap.get("Loginuser");
         String password = (String)testMap.get("Loginpassword");
         password = password + "tofail";
         String attempts = (String)testMap.get("Loginattempts");
         String Passmsg = (String)testMap.get("Passmsg");
         String baseDirectory = (String)testMap.get("baseDir");
         String loginurl = (String)testMap.get("loginurl");
         int ilockattempts = Integer.parseInt(attempts);
         if (!isWarning) {
             fileName = baseDirectory  + "accountlock.xml";
         } else {
             fileName = baseDirectory +  "accountwarning.xml";
         }
         PrintWriter out = new PrintWriter(new BufferedWriter
                 (new FileWriter(fileName)));
         out.write("<url href=\"" + loginurl);
         out.write("\">");
         out.write(newline);
         int formcount = 0;
             for (int i=0; i < ilockattempts ; i ++) {
                 formcount = formcount + 1;
                 out.write("<form name=\"Login\" IDButton=\"\" >");
                 out.write(newline);
                 out.write("<input name=\"IDToken1\" value=\"" + userName + "\" />");
                 out.write(newline);
                 out.write("<input name=\"IDToken2\" value=\"" + password + "\" />");
                 out.write(newline);
                 if(formcount == ilockattempts){
                     out.write("<result text=\"" + Passmsg + "\" />");
                     out.write(newline);
                 }
                 out.write("</form>");
                 out.write(newline);
                 out.write(" <form anchorpattern=\"/UI/Login?\" />");
                 out.write(newline);
             }
         out.write("</url>");
         out.write(newline);
         out.flush();
         out.close();
         
         return fileName;
     }
     
     /**
      * Creates the XML file for the profile attribute tests
      */
     public String createProfileXML(Map testMap)
     throws Exception {
         String userName = (String)testMap.get("Loginuser");
         String password = (String)testMap.get("Loginpassword");
         String attempts = (String)testMap.get("Loginattempts");
         String Passmsg = (String)testMap.get("Passmsg");
         String baseDirectory = (String)testMap.get("baseDir");
         String loginurl = (String)testMap.get("loginurl");
         String profileattribute = (String)testMap.get("profileattr");
         fileName = baseDirectory  + profileattribute + "-test.xml";
         PrintWriter out = new PrintWriter(new BufferedWriter
                 (new FileWriter(fileName)));
         out.write("<url href=\"" + loginurl);
         out.write("\">");
         out.write(newline);
         out.write("<form name=\"Login\" IDButton=\"\" >");
         out.write(newline);
         out.write("<input name=\"IDToken1\" value=\"" + userName + "\" />");
         out.write(newline);
         out.write("<input name=\"IDToken2\" value=\"" + password + "\" />");
         out.write(newline);
         out.write("<result text=\"" + Passmsg + "\" />");
         out.write(newline);
         out.write("</form>");
         out.write(newline);
         out.write("</url>");
         out.write(newline);
         out.flush();
         out.close();
         
         return fileName;
     }
 }
