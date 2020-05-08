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
 * $Id: SAMLv2ForceAuthNTests.java,v 1.1 2007-06-21 17:31:47 mrudulahg Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.samlv2;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.SAMLv2Common;
 import com.sun.identity.qatest.common.TestConstants;
 import com.sun.identity.qatest.common.webtest.DefaultTaskHandler;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * This class tests SP init SSO with ForceAuthn set to true & false.
  */
 public class SAMLv2ForceAuthNTests extends TestCommon {
     
     public WebClient webClient;
     private Map<String, String> configMap;
     private Map<String, String> usersMap;
     private String baseDir ;
     private String xmlfile;
     private DefaultTaskHandler task;
     private HtmlPage page;
     private FederationManager fmSP;
     private FederationManager fmIDP;
     private String spmetadata;
     private String idpmetadata;
     private String spurl;
     private String idpurl;
     ArrayList spuserlist = new ArrayList();
     ArrayList idpuserlist = new ArrayList();
     
     /**
      * This is constructor for this class.
      */
     public SAMLv2ForceAuthNTests() {
         super("SAMLv2ForceAuthNTests");
     }
     
     /**
      * This setup method creates required users.
      */
     @BeforeClass(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec",
     "ldapv3_sec"})
     public void setup() 
     throws Exception {
         URL url;
         HtmlPage page;
         ArrayList list;
         try {
             log(logLevel, "setup", "Entering");
             //Upload global properties file in configMap
             baseDir = getTestBase();
             configMap = new HashMap<String, String>();
             configMap = getMapFromResourceBundle("samlv2TestConfigData");
            configMap = getMapFromResourceBundle("samlv2TestData");
             log(logLevel, "setup", "ConfigMap is : " + configMap );
             
             // Create sp users
             spurl = configMap.get(TestConstants.KEY_SP_PROTOCOL) +
                     "://" + configMap.get(TestConstants.KEY_SP_HOST) + ":" +
                     configMap.get(TestConstants.KEY_SP_PORT) +
                     configMap.get(TestConstants.KEY_SP_DEPLOYMENT_URI);
             idpurl = configMap.get(TestConstants.KEY_IDP_PROTOCOL) +
                     "://" + configMap.get(TestConstants.KEY_IDP_HOST) + ":" +
                     configMap.get(TestConstants.KEY_IDP_PORT) +
                     configMap.get(TestConstants.KEY_IDP_DEPLOYMENT_URI);
             getWebClient();
             consoleLogin(webClient, spurl,
                     configMap.get(TestConstants.KEY_SP_AMADMIN_USER),
                     configMap.get(TestConstants.KEY_SP_AMADMIN_PASSWORD));
             fmSP = new FederationManager(spurl);
             consoleLogin(webClient, idpurl,
                     configMap.get(TestConstants.KEY_IDP_AMADMIN_USER),
                     configMap.get(TestConstants.KEY_IDP_AMADMIN_PASSWORD));
             fmIDP = new FederationManager(idpurl);
             list = new ArrayList();
             usersMap = new HashMap<String, String>();
             usersMap = getMapFromResourceBundle("samlv2ForceAuthNTests");
             Integer totalUsers = new Integer(
                     (String)usersMap.get("totalUsers"));
             for (int i = 1; i < totalUsers + 1; i++) {
                 //create sp user first
                 list.clear();
                 list.add("sn=" + usersMap.get(TestConstants.KEY_SP_USER + i));
                 list.add("cn=" + usersMap.get(TestConstants.KEY_SP_USER + i));
                 list.add("userpassword=" + usersMap.get(
                         TestConstants.KEY_SP_USER_PASSWORD + i));
                 list.add("inetuserstatus=Active");
                 log(logLevel, "setup", "SP user to be created is " + list);
                 fmSP.createIdentity(webClient, configMap.get(
                         TestConstants.KEY_SP_REALM),
                         usersMap.get(TestConstants.KEY_SP_USER + i), "User",
                         list);
                 spuserlist.add(usersMap.get(TestConstants.KEY_SP_USER + i));
                 
                 //create idp user
                 list.clear();
                 list.add("sn=" + usersMap.get(TestConstants.KEY_IDP_USER + i));
                 list.add("cn=" + usersMap.get(TestConstants.KEY_IDP_USER + i));
                 list.add("userpassword=" + usersMap.get(
                         TestConstants.KEY_IDP_USER_PASSWORD + i));
                 list.add("inetuserstatus=Active");
                 log(logLevel, "setup", "IDP user to be created is " + list);
                 fmIDP.createIdentity(webClient, configMap.get(
                         TestConstants.KEY_IDP_REALM),
                         usersMap.get(TestConstants.KEY_IDP_USER + i), "User",
                         list);
                 idpuserlist.add(usersMap.get(TestConstants.KEY_IDP_USER + i));
                 list.clear();
             }
         } catch (Exception e) {
             log(Level.SEVERE, "setup", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("setup");
     }
     
     /**
      * Create the webClient which will be used for the rest of the tests.
      */
     @BeforeClass(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec",
     "ldapv3_sec"})
     public void getWebClient() 
     throws Exception {
         try {
             webClient = new WebClient(BrowserVersion.MOZILLA_1_0);
         } catch (Exception e) {
             log(Level.SEVERE, "getWebClient", e.getMessage());
             e.printStackTrace();
             throw e;
         }
     }
     
     /**
      * @DocTest: SAML2|Perform SP initiated sso with ForceAuthn=true.
      */
     @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
     public void testforceAuthNtrue()
     throws Exception {
         entering("testforceAuthNtrue", null);
         try {
             configMap.put(TestConstants.KEY_SP_USER, 
                     usersMap.get(TestConstants.KEY_SP_USER + 1));
             configMap.put(TestConstants.KEY_SP_USER_PASSWORD, 
                     usersMap.get(TestConstants.KEY_SP_USER_PASSWORD + 1));
             configMap.put(TestConstants.KEY_IDP_USER, 
                     usersMap.get(TestConstants.KEY_IDP_USER + 1));
             configMap.put(TestConstants.KEY_IDP_USER_PASSWORD, 
                     usersMap.get(TestConstants.KEY_IDP_USER_PASSWORD + 1));
             log(logLevel, "testforceAuthNtrue", "Running: testforceAuthNtrue");
             getWebClient();
             consoleLogin(webClient, idpurl,
                     configMap.get(TestConstants.KEY_IDP_USER),
                     configMap.get(TestConstants.KEY_IDP_USER_PASSWORD));
             configMap.put("urlparams","ForceAuthn=true");
             String[] arrActions = {"forceauthntrue_sso",
                     "forceauthntrue_slo", 
                     "forceauthntrue_terminate"};
             String loginxmlfile = baseDir + arrActions[0] + ".xml";
             configMap.put("urlparams","ForceAuthn=true");
             SAMLv2Common.getxmlSPInitSSO(loginxmlfile, configMap, "artifact", 
                     false);
             configMap.remove("urlparams");
             String ssoxmlfile = baseDir + arrActions[1] + ".xml";
             SAMLv2Common.getxmlSPSLO(ssoxmlfile, configMap, "http");
             String sloxmlfile = baseDir + arrActions[2] + ".xml";
             SAMLv2Common.getxmlSPTerminate(sloxmlfile, configMap, "http");
             
             for (int i = 0; i < arrActions.length; i++) {
                 log(logLevel, "testforceAuthNtrue",
                         "Executing xml: " + arrActions[i]);
                 task = new DefaultTaskHandler(baseDir + arrActions[i] + ".xml");
                 page = task.execute(webClient);
             }
         } catch (Exception e) {
             log(Level.SEVERE, "testforceAuthNtrue", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("testforceAuthNtrue");
     }
     
     /**
      * @DocTest: SAML2|Perform SP init sso with ForceAuthn=true, with post profile
      */
     @Test(groups={"ff_sec", "ds_sec", "ldapv3_sec"})
     public void testforceAuthNtruePost()
     throws Exception {
         entering("testforceAuthNtruePost", null);
         try {
             log(logLevel, "testforceAuthNtruePost", "Running: " +
                     "testforceAuthNtruePost");
             configMap.put(TestConstants.KEY_SP_USER, 
                     usersMap.get(TestConstants.KEY_SP_USER + 2));
             configMap.put(TestConstants.KEY_SP_USER_PASSWORD, 
                     usersMap.get(TestConstants.KEY_SP_USER_PASSWORD + 2));
             configMap.put(TestConstants.KEY_IDP_USER, 
                     usersMap.get(TestConstants.KEY_IDP_USER + 2));
             configMap.put(TestConstants.KEY_IDP_USER_PASSWORD, 
                     usersMap.get(TestConstants.KEY_IDP_USER_PASSWORD + 2));
             getWebClient();
             consoleLogin(webClient, idpurl,
                     configMap.get(TestConstants.KEY_IDP_USER),
                     configMap.get(TestConstants.KEY_IDP_USER_PASSWORD));
             configMap.put("urlparams","ForceAuthn=true");
             String[] arrActions = {"forceauthntruepost_sso",
                     "forceauthntruepost_slo", 
                     "forceauthntruepost_terminate"};
             String loginxmlfile = baseDir + arrActions[0] + ".xml";
             configMap.put("urlparams","forceAuthN=true");
             SAMLv2Common.getxmlSPInitSSO(loginxmlfile, configMap, "post", false);
             configMap.remove("urlparams");
             String ssoxmlfile = baseDir + arrActions[1] + ".xml";
             SAMLv2Common.getxmlSPSLO(ssoxmlfile, configMap, "soap");
             String sloxmlfile = baseDir + arrActions[2] + ".xml";
             SAMLv2Common.getxmlSPTerminate(sloxmlfile, configMap, "soap");
             
             for (int i = 0; i < arrActions.length; i++) {
                 log(logLevel, "testforceAuthNtruePost",
                         "Executing xml: " + arrActions[i]);
                 task = new DefaultTaskHandler(baseDir + arrActions[i]
                         + ".xml");
                 page = task.execute(webClient);
             }
         } catch (Exception e) {
             log(Level.SEVERE, "testforceAuthNtruePost", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("testforceAuthNtruePost");
     }
     
     /**
      * @DocTest: SAML2|Perform SP initiated sso with ForceAuthn=false.
      */
     @Test(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
     public void testforceAuthNfalse()
     throws Exception {
         entering("testforceAuthNfalse", null);
         try {
             configMap.put(TestConstants.KEY_SP_USER, 
                     usersMap.get(TestConstants.KEY_SP_USER + 3));
             configMap.put(TestConstants.KEY_SP_USER_PASSWORD, 
                     usersMap.get(TestConstants.KEY_SP_USER_PASSWORD + 3));
             configMap.put(TestConstants.KEY_IDP_USER, 
                     usersMap.get(TestConstants.KEY_IDP_USER + 3));
             configMap.put(TestConstants.KEY_IDP_USER_PASSWORD, 
                     usersMap.get(TestConstants.KEY_IDP_USER_PASSWORD + 3));
             log(logLevel, "testforceAuthNfalse", "Running: testforceAuthNfalse");
             getWebClient();
             consoleLogin(webClient, idpurl,
                     configMap.get(TestConstants.KEY_IDP_USER),
                     configMap.get(TestConstants.KEY_IDP_USER_PASSWORD));
             configMap.put("urlparams","ForceAuthn=false");
             String[] arrActions = {"forceauthnfalse_sso",
                     "forceauthnfalse_slo", 
                     "forceauthnfalse_terminate"};
             String loginxmlfile = baseDir + arrActions[0] + ".xml";
             getxmlSPInitSSOSPOnly(loginxmlfile, configMap, "artifact");
             configMap.remove("urlparams");
             String ssoxmlfile = baseDir + arrActions[1] + ".xml";
             SAMLv2Common.getxmlSPSLO(ssoxmlfile, configMap, "http");
             String sloxmlfile = baseDir + arrActions[2] + ".xml";
             SAMLv2Common.getxmlSPTerminate(sloxmlfile, configMap, "http");
             
             for (int i = 0; i < arrActions.length; i++) {
                 log(logLevel, "testforceAuthNfalse",
                         "Executing xml: " + arrActions[i]);
                 task = new DefaultTaskHandler(baseDir + arrActions[i] + ".xml");
                 page = task.execute(webClient);
             }
         } catch (Exception e) {
             log(Level.SEVERE, "testforceAuthNfalse", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("testforceAuthNfalse");
     }
     
     /**
      * @DocTest: SAML2|Perform SP init sso with ForceAuthn=false, with post profile
      */
     @Test(groups={"ff_sec", "ds_sec", "ldapv3_sec"})
     public void testforceAuthNfalsePost()
     throws Exception {
         entering("testforceAuthNfalsePost", null);
         try {
             log(logLevel, "testforceAuthNfalsePost", "Running: " +
                     "testforceAuthNfalsePost");
             configMap.put(TestConstants.KEY_SP_USER, 
                     usersMap.get(TestConstants.KEY_SP_USER + 4));
             configMap.put(TestConstants.KEY_SP_USER_PASSWORD, 
                     usersMap.get(TestConstants.KEY_SP_USER_PASSWORD + 4));
             configMap.put(TestConstants.KEY_IDP_USER, 
                     usersMap.get(TestConstants.KEY_IDP_USER + 4));
             configMap.put(TestConstants.KEY_IDP_USER_PASSWORD, 
                     usersMap.get(TestConstants.KEY_IDP_USER_PASSWORD + 4));
             getWebClient();
             consoleLogin(webClient, idpurl,
                     configMap.get(TestConstants.KEY_IDP_USER),
                     configMap.get(TestConstants.KEY_IDP_USER_PASSWORD));
             configMap.put("urlparams","ForceAuthn=false");
             String[] arrActions = {"forceauthnfalsepost_sso",
                     "forceauthnfalsepost_slo", 
                     "forceauthnfalsepost_terminate"};
             String loginxmlfile = baseDir + arrActions[0] + ".xml";
             getxmlSPInitSSOSPOnly(loginxmlfile, configMap, "post");
             configMap.remove("urlparams");
             String ssoxmlfile = baseDir + arrActions[1] + ".xml";
             SAMLv2Common.getxmlSPSLO(ssoxmlfile, configMap, "soap");
             String sloxmlfile = baseDir + arrActions[2] + ".xml";
             SAMLv2Common.getxmlSPTerminate(sloxmlfile, configMap, "soap");
             
             for (int i = 0; i < arrActions.length; i++) {
                 log(logLevel, "testforceAuthNfalsePost",
                         "Executing xml: " + arrActions[i]);
                 task = new DefaultTaskHandler(baseDir + arrActions[i] + ".xml");
                 page = task.execute(webClient);
             }
         } catch (Exception e) {
             log(Level.SEVERE, "testforceAuthNfalsePost", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("testforceAuthNfalsePost");
     }
 
     /**
      * Cleanup methods deletes all the users which were created in setup
      */
     @AfterClass(groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
     public void cleanup()
     throws Exception {
         entering("cleanup", null);
         getWebClient();
         try {
             //get sp & idp extended metadata
             consoleLogin(webClient, spurl, configMap.get(
                     TestConstants.KEY_SP_AMADMIN_USER),
                     configMap.get(TestConstants.KEY_SP_AMADMIN_PASSWORD));
             fmSP.deleteIdentities(webClient, configMap.get(
                     TestConstants.KEY_SP_REALM), spuserlist, "User");
             
             consoleLogin(webClient, idpurl, configMap.get(
                     TestConstants.KEY_IDP_AMADMIN_USER),
                     configMap.get(TestConstants.KEY_IDP_AMADMIN_PASSWORD));
             fmIDP.deleteIdentities(webClient, configMap.get(
                     TestConstants.KEY_IDP_REALM),
                     idpuserlist, "User");
         } catch (Exception e) {
             log(Level.SEVERE, "cleanup", e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, spurl);
             consoleLogout(webClient, idpurl);
         }
         exiting("cleanup");
     }
     
     /**
      * Private function to get SP init SSO with only SP login.
      */
     private void getxmlSPInitSSOSPOnly(String xmlFileName, Map m,
             String bindingType)
     throws Exception {
         try {
             FileWriter fstream = new FileWriter(xmlFileName);
             BufferedWriter out = new BufferedWriter(fstream);
             String sp_proto = (String)m.get(TestConstants.KEY_SP_PROTOCOL);
             String sp_port = (String)m.get(TestConstants.KEY_SP_PORT);
             String sp_host = (String)m.get(TestConstants.KEY_SP_HOST);
             String sp_deployment_uri = (String)m.get(
                     TestConstants.KEY_SP_DEPLOYMENT_URI);
             String sp_alias = (String)m.get(TestConstants.KEY_SP_METAALIAS);
             String idp_entity_name = (String)m.get(
                     TestConstants.KEY_IDP_ENTITY_NAME);
             String sp_user = (String)m.get(TestConstants.KEY_SP_USER);
             String sp_userpw = (String)m.get(TestConstants.KEY_SP_USER_PASSWORD);
             String strResult = (String)m.get(TestConstants.KEY_SSO_INIT_RESULT);
 
             out.write("<url href=\"" + sp_proto +"://" + sp_host + ":"
                     + sp_port + sp_deployment_uri
                     + "/saml2/jsp/spSSOInit.jsp?metaAlias=/" + sp_alias
                     + "&amp;idpEntityID=" + idp_entity_name );
             if (bindingType == "post") {
                 out.write("&amp;binding=HTTP-POST");
             }
             if (m.get("urlparams") != null) {
                 out.write("&amp;" + m.get("urlparams"));
             }
             out.write("\">");
             out.write(newline);
             out.write("<form name=\"Login\" buttonName=\"\" >");
             out.write(newline);
             out.write("<input name=\"IDToken1\" value=\"" + sp_user + "\" />");
             out.write(newline);
             out.write("<input name=\"IDToken2\" value=\"" + sp_userpw + "\" />");
             out.write(newline);
             out.write("<result text=\"" + strResult + "\" />");
             out.write(newline);
             out.write("</form>");
             out.write(newline);
             out.write("</url>");
             out.write(newline);
             out.close();
         } catch (Exception e) {
             log(Level.SEVERE, "getxmlSPInitSSO", e.getMessage());
             e.printStackTrace();
             throw e;
         } 
     }    
 }
