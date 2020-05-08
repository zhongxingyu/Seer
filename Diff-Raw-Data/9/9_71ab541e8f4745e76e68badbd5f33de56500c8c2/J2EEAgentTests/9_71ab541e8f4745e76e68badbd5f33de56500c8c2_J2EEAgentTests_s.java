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
 * $Id: J2EEAgentTests.java,v 1.2 2008-04-18 19:28:51 nithyas Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.agents;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.qatest.common.PolicyCommon;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.IDMCommon;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 import org.testng.Reporter;
 
 
 /**
  * This class evaluates general policy tests though the
  * for different kinds of policy rules, subjects, conditions
  * and response providers.
  */
 public class J2EEAgentTests extends TestCommon {
     
     private int polIdx;
     private int evalIdx;
     private int iIdx;
     private String strSetup;
     private String strCleanup;
     private PolicyCommon mpc;
     private boolean executeAgainstOpenSSO;
     private ResourceBundle rbg;
     private ResourceBundle rbp;
     private String strLocRB = "J2EEAgentTests";
     private String strGblRB = "agentsGlobal";
     private WebClient webClient;
     private HtmlPage page;
     private String logoutURL;
     private IDMCommon idmc;
     private int pollingTime;
     private SSOToken admintoken;
     private String strAgentType;    
     
     /**
      * Class constructor. Instantiates the ResourceBundles and other
      * common class objects needed by the tests.
      */
     public J2EEAgentTests() 
     throws Exception {
         super("J2EEAgentTests");
         mpc = new PolicyCommon();
         rbg = ResourceBundle.getBundle(strGblRB);
         rbp = ResourceBundle.getBundle(strLocRB);
         executeAgainstOpenSSO = new Boolean(rbg.getString(strGblRB +
                 ".executeAgainstOpenSSO")).booleanValue();
         strAgentType = rbg.getString(strGblRB + ".agentType");
         idmc = new IDMCommon();
         pollingTime = new Integer(rbg.getString(strGblRB +
                 ".pollingInterval")).intValue();
         admintoken = getToken(adminUser, adminPassword, basedn);
     }
     
     /**
      * Creates the policies/identities on the server.
      */
     @Parameters({"policyIdx", "evaluationIdx", "setup", "cleanup"})
     @BeforeClass(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void setup(String policyIdx, String evaluationIdx, String setup
             , String cleanup)
     throws Exception {
         Object[] params = {policyIdx, evaluationIdx, setup, cleanup};
         entering("setup", params);
         if (!strAgentType.contains("J2EE")) {
         Reporter.log ("Agent being tested is of type " + 
                 strAgentType + ".<br>These tests are for J2EE Agents " + 
                 "only. Skipping TCs");
         assert(false);
         }
         if (executeAgainstOpenSSO) {
             try {
                 polIdx = new Integer(policyIdx).intValue();
                 evalIdx = new Integer(evaluationIdx).intValue();
                 strSetup = setup;
                 strCleanup = cleanup;
                 logoutURL = protocol + ":" + "//" + host + ":" + port + uri +
                     "/UI/Logout";
                 String testIdType = rbp.getString(strLocRB + polIdx + 
                         ".identity1.type");
                 if (strSetup.equals("true")) {
                     if (!idmc.isIdTypeSupported(admintoken, "/", testIdType)) {
                        log(Level.SEVERE, "setup ", "IDType " + 
                                testIdType + " is not supported in " + 
                                "this deployment.");
                        assert(false);
                     } else {
                         log(Level.FINE, "setup ", "IDType " 
                                 + testIdType + " is supported in " + 
                                 "this deployment.");
                         mpc.createIdentities(strLocRB, polIdx, "/");
                         mpc.createPolicyXML(strGblRB, strLocRB, polIdx, 
                             strLocRB + ".xml", "/");
                        mpc.createPolicy(strLocRB + ".xml", "/");
                     }
                 }
             } catch (Exception e) {
                 log(Level.SEVERE, "setup", e.getMessage());
                 e.printStackTrace();
                 throw e;
             }
         } else
             log(Level.FINE, "setup", "Either executing against a " + 
                     "non OpenSSO install OR Web Agents");
         exiting("setup");
     }
 
     /**
      * Evaluates policy through the J2EE Agent.
      */
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void evaluatePolicy()
     throws Exception {
         entering("evaluatePolicy", null);
             try {
                 webClient = new WebClient();
                 webClient.setCookiesEnabled(true);
                 String strEvalIdx = strLocRB + polIdx + ".evaluation" + evalIdx;
                 String resource = rbg.getString(rbp.getString(strEvalIdx + 
                         ".resource"));
                 String usernameIdx = rbp.getString(strEvalIdx + ".username");
                 String username = rbp.getString(usernameIdx);
                 String passwordIdx = rbp.getString(strEvalIdx + ".password");
                 String password = rbp.getString(passwordIdx);
                 String expResult= rbp.getString(strEvalIdx + 
                         ".expectedResult");
                 String expResultAdditionalCheck= rbp.getString(strEvalIdx + 
                         ".expectedResultAdditionalCheck");
                 String description = rbp.getString(strEvalIdx + ".description");
                 Reporter.log("Test description: " + description);   
                 Reporter.log("Resource: " + resource);   
                 Reporter.log("Username: " + username);   
                 Reporter.log("Password: " + password);   
                 Reporter.log("Expected Result: " + expResult);   
                 if (!expResultAdditionalCheck.equals("")) {
                     Reporter.log("Expected Result (additional check): " + 
                             expResultAdditionalCheck);   
                 }
                 page = consoleLogin(webClient, resource, username, password);
                 page = (HtmlPage)webClient.getPage(resource);
                 iIdx = -1;
                 iIdx = getHtmlPageStringIndex(page, expResult);
                 assert (iIdx != -1);
                 if (!expResultAdditionalCheck.equals("")) {
                     iIdx = -1;
                     iIdx = getHtmlPageStringIndex(page, 
                             expResultAdditionalCheck);
                     assert (iIdx != -1);
                 }
             } catch (Exception e) {
                 log(Level.SEVERE, "evaluatePolicy", e.getMessage());
                 e.printStackTrace();
                 throw e;
             } finally {
                 consoleLogout(webClient, logoutURL);
             }
         exiting("evaluatePolicy");
     }
 
     /**
      * Deletes policies and users.
      */
     @AfterClass(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void cleanup()
     throws Exception {
         entering("cleanup", null);
         log(Level.SEVERE, "cleanup", "executeAgainstOpenSSO && strAgentType" + 
                 executeAgainstOpenSSO + "," + strAgentType);                            
         if (executeAgainstOpenSSO && strAgentType.contains("J2EE")) {
             try {
                 if (strCleanup.equals("true")) {
                     log(Level.SEVERE, "cleanup", "strCleanup=" + strCleanup);                    
                     mpc.deleteIdentities(strLocRB, polIdx, "/");
                     mpc.deletePolicies(strLocRB, polIdx, "/");
                 }
             } catch (Exception e) {
                 log(Level.SEVERE, "cleanup", e.getMessage());
                 e.printStackTrace();
                 throw e;
             }
         } else
             log(Level.FINE, "cleanup", "Either Executing against " + 
                     "non OpenSSO Install OR Web Agents");
         exiting("cleanup");
     }
 }
