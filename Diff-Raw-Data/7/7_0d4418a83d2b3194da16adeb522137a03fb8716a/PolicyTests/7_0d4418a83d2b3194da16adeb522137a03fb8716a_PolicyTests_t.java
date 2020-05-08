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
 * $Id: PolicyTests.java,v 1.4 2007-08-29 06:56:49 arunav Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.policy;
 
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.PolicyCommon;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 import org.testng.Reporter;
 import java.util.Map;
 
 /**
  * This class has the methods to create and evaluate policies using client
  * policy evaluation API
  */
 
 public class PolicyTests extends TestCommon {
     
     private int polIdx;
     private int evalIdx;
     private String strSetup;
     private String strCleanup;
     private String strPeAtOrg;
     private String strDynamic;
     private String strDynamicRefValue;
     private PolicyCommon mpc;
     private ResourceBundle rbg;
     private ResourceBundle rbp;
     private ResourceBundle rbr;
     private String strReferringOrg;
     private String strLocRB = "PolicyTests";
     private String strGblRB = "PolicyGlobal";
     private String strRefRB = "PolicyReferral";
    
     /**
      * Class constructor. No arguments
      */  
     public PolicyTests() 
     throws Exception {
         super("PolicyTests");
         mpc = new PolicyCommon();
         rbg = ResourceBundle.getBundle(strGblRB);
         rbp = ResourceBundle.getBundle(strLocRB);
         rbr = ResourceBundle.getBundle(strRefRB);
     }
     
    /**
     * This method sets up all the required identities, generates the xmls and
     * creates the policies in the server
     */
     @Parameters({"policyIdx","evaluationIdx","setup","cleanup", "peAtOrg", 
     "dynamic"})
     @BeforeClass(groups={"ds_ds","ds_ds_sec","ff_ds","ff_ds_sec"})
     public void setup(String policyIdx, String evaluationIdx, String setup
             , String cleanup, String peAtOrg, String dynamic)
     throws Exception {
         Object[] params = {policyIdx, evaluationIdx, setup, cleanup, dynamic,
         peAtOrg};
         entering("setup", params);
         try {
             polIdx = new Integer(policyIdx).intValue();
             evalIdx = new Integer(evaluationIdx).intValue();
             strSetup = setup;
             strCleanup = cleanup;
             strPeAtOrg = peAtOrg;
             strDynamic = dynamic;           
             if (strSetup.equals("true")) {
                 if (strPeAtOrg.equals(realm)) {
                     mpc.createIdentities(strLocRB, polIdx,  strPeAtOrg );
                     mpc.createPolicyXML(strGblRB, strLocRB, polIdx, strLocRB +
                         ".xml", strPeAtOrg);
                     mpc.createPolicy(strLocRB + ".xml", strPeAtOrg);
                    Thread.sleep(80000);
                 } else { 
                     mpc.createRealm("/" + strPeAtOrg);
                     mpc.createIdentities(strLocRB, polIdx, strPeAtOrg);
                     
                     if (strDynamic.equals("false")) {
                         mpc.createReferralPolicyXML(strGblRB, strRefRB, 
                                 strLocRB, polIdx, strRefRB +  ".xml");
                         strReferringOrg = rbr.getString(strLocRB + polIdx + 
                              ".referringOrg");
                         mpc.createPolicy(strRefRB + ".xml", strReferringOrg );
                        Thread.sleep(30000);
                     } else {
                         strDynamicRefValue = "true";
                         mpc.setDynamicReferral(strDynamicRefValue);
                         mpc.createDynamicReferral(strGblRB, strRefRB, strLocRB,  
                                 polIdx, strPeAtOrg);
                     }   
                     mpc.createPolicyXML(strGblRB, strLocRB, polIdx, 
                         strLocRB + ".xml", strPeAtOrg);
                     mpc.createPolicy(strLocRB + ".xml", strPeAtOrg);
                     Thread.sleep(75000);                                      
                 } 
             }
         } catch (Exception e) {
             log(Level.SEVERE, "setup", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("setup");
     }
          
    /**
     * This method evaluates the policies using the client policy evaluation API
     * Policy_sub, Policy_ldapFilter, Policy_sub_exclude, Policy_Wildcard, Subre
     * alm, Combination policies, policy response attributes
     */
     @Parameters({ "peAtOrg"}) 
     @Test(groups={"ds_ds","ds_ds_sec","ff_ds","ff_ds_sec"})      
     public void evaluatePolicyAPI(String peAtOrg)
     throws Exception {
         Object[] params = {peAtOrg};
         entering("evaluatePolicyAPI", params);
         String strEvalIdx = strLocRB + polIdx + ".evaluation" + evalIdx;
         String resource = rbg.getString(rbp.getString(strEvalIdx +
                 ".resource"));
         String usernameIdx = rbp.getString(strEvalIdx + ".username");
         String username = rbp.getString(usernameIdx);
         String passwordIdx = rbp.getString(strEvalIdx + ".password");
         String password = rbp.getString(passwordIdx);
         String action = rbp.getString(strEvalIdx + ".action");
         String expResult = rbp.getString(strEvalIdx + ".expectedResult");
         String description = rbp.getString(strEvalIdx + ".description");
         int idIdx = mpc.getIdentityIndex(usernameIdx);
         Map map = mpc.getPolicyEnvParamMap(strLocRB, polIdx, evalIdx);
         
         Reporter.log("Test description: " + description);
         Reporter.log("Resource: " + resource);
         Reporter.log("Username: " + username);
         Reporter.log("Password: " + password);
         Reporter.log("Action: " + action);
         Reporter.log("Env Param: " + map);
         Reporter.log("Expected Result: " + expResult);
         
         SSOToken userToken = getToken(username, password, peAtOrg);
         mpc.setProperty(strLocRB, userToken, polIdx, idIdx);
         mpc.evaluatePolicyThroughAPI(resource, userToken, action, map,
                 expResult, idIdx);
         exiting("evaluatePolicyAPI");
     }
     
    /**
     * This method cleans all the identities and policies  that were setup
     */
     @Parameters({"peAtOrg"})
     @AfterClass(groups={"ds_ds","ds_ds_sec","ff_ds","ff_ds_sec"})    
     public void cleanup(String peAtOrg)
     throws Exception {
         Object[] params = {peAtOrg};
         entering("cleanup", params);
         try {
             if (strCleanup.equals("true")) {
                 if (peAtOrg.equals(realm)) {
                     mpc.deleteIdentities(strLocRB, polIdx, peAtOrg);
                     mpc.deletePolicies(strLocRB, polIdx, peAtOrg);
                 } else {
                     mpc.deleteReferralPolicies(strLocRB, strRefRB, polIdx);
                     mpc.deleteIdentities(strLocRB, polIdx, peAtOrg); 
 		    mpc.deleteRealm(peAtOrg); 
                 }
             }
         } catch (Exception e) {
             log(Level.SEVERE, "cleanup", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("cleanup");
     }
 }
