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
 * $Id: UnconfigureSAMLv2.java,v 1.2 2007-05-30 19:24:24 mrudulahg Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.samlv2;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.TestConstants;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import org.testng.annotations.AfterTest;
 
 /**
  * This class removes the configuration on SP & IDP 
  * It removes SP & IDP entities on both sides, and also removes the COT. 
  */
 public class UnconfigureSAMLv2 extends TestCommon {
     private WebClient webClient;
     private Map<String, String> configMap;
     private String spurl;
     private String idpurl;
     
     /** Creates a new instance of UnconfigureSAMLv2 */
     public UnconfigureSAMLv2() {
         super ("UnconfigureSAMLv2");
     }
     
     /**
      * Create the webClient which should be run before each test.
      */
    private void getWebClient() 
    throws Exception {
         try {
             webClient = new WebClient(BrowserVersion.MOZILLA_1_0);
         } catch (Exception e) {
             log (Level.SEVERE, "getWebClient", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         }
     }
     
     /**
      * Configure sp & idp
      * @DocTest: SAML2|Unconfigure SP & IDP by deleting entities & COT's 
      */
     @AfterTest (groups={"ff", "ds", "ldapv3", "ff_sec", "ds_sec", "ldapv3_sec"})
     public void UnconfigureSAMLv2()
     throws Exception {
         entering ("UnconfigureSAMLv2", null);
         String spurl;
         String idpurl;
         try {
             //Upload global properties file in configMap
             configMap = new HashMap<String, String>();
             getWebClient();
             
             configMap = getMapFromResourceBundle("samlv2TestConfigData");
             log (logLevel, "UnconfigureSAMLv2", "Map:" + configMap);
             
             spurl = configMap.get(TestConstants.KEY_SP_PROTOCOL) + "://" + 
                     configMap.get(TestConstants.KEY_SP_HOST) + ":"
                     + configMap.get(TestConstants.KEY_SP_PORT)
                     + configMap.get(TestConstants.KEY_SP_DEPLOYMENT_URI);
             idpurl = configMap.get(TestConstants.KEY_IDP_PROTOCOL) + "://" + 
                     configMap.get(TestConstants.KEY_IDP_HOST) + ":"
                     + configMap.get(TestConstants.KEY_IDP_PORT)
                     + configMap.get(TestConstants.KEY_IDP_DEPLOYMENT_URI);
             
             consoleLogin (webClient, spurl,
                     (String)configMap.get(TestConstants.KEY_SP_AMADMIN_USER),
                     (String)configMap.get(
                     TestConstants.KEY_SP_AMADMIN_PASSWORD));
             consoleLogin (webClient, idpurl,
                     (String)configMap.get(TestConstants.KEY_IDP_AMADMIN_USER),
                     (String)configMap.get(
                     TestConstants.KEY_IDP_AMADMIN_PASSWORD));
         } catch (Exception e) {
             log (Level.SEVERE, "UnconfigureSAMLv2", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         }
         
         try{
             FederationManager spfm = new FederationManager(spurl);
             FederationManager idpfm = new FederationManager(idpurl);
             
             HtmlPage idpEntityPage = idpfm.listEntities(webClient,
                     configMap.get (TestConstants.KEY_IDP_REALM), "saml2");
             //Delete IDP & SP entities on IDP
             if (idpEntityPage.getWebResponse().getContentAsString().
                     contains(configMap.get(TestConstants.KEY_IDP_ENTITY_NAME))) {
                 log (logLevel, "UnconfigureSAMLv2", "idp entity exists at sp. ",
                         null);
                 HtmlPage idpDeleteEntityPage = idpfm.deleteEntity (webClient,
                         configMap.get (TestConstants.KEY_IDP_ENTITY_NAME),
                         configMap.get (TestConstants.KEY_IDP_REALM), false,
                         "saml2");
                 if (idpDeleteEntityPage.getWebResponse().getContentAsString().
                         contains("Descriptor is deleted for entity, " +
                         configMap.get(TestConstants.KEY_IDP_ENTITY_NAME))) {
                     log (logLevel, "UnconfigureSAMLv2", "Deleted IDP entity " +
                             "on IDP side", null);
                 } else {
                     log (logLevel, "UnconfigureSAMLv2", "Couldnt delete sp " +
                             "entity on IDP side", null);
                     assert false;
                 }
             }
            
             if (idpEntityPage.getWebResponse().getContentAsString().
                     contains(configMap.get(TestConstants.KEY_SP_ENTITY_NAME))) {
                 log (logLevel, "UnconfigureSAMLv2", "sp entity exists at idp. ",
                         null);
                 HtmlPage spDeleteEntityPage = idpfm.deleteEntity (webClient,
                         configMap.get(TestConstants.KEY_SP_ENTITY_NAME),
                         configMap.get(TestConstants.KEY_IDP_REALM), false,
                         "saml2");
                 if (spDeleteEntityPage.getWebResponse().getContentAsString().
                         contains("Descriptor is deleted for entity, " +
                         configMap.get(TestConstants.KEY_SP_ENTITY_NAME))) {
                     log (logLevel, "UnconfigureSAMLv2", "Deleted sp entity " +
                             "on IDP side", null);
                 } else {
                     log (logLevel, "UnconfigureSAMLv2", "Couldnt delete sp " +
                             "entity on IDP side", null);
                     assert false;
                 }
             }
             
             //Delete COT on IDP side.
             HtmlPage idpcotPage = idpfm.listCircleOfTrusts(webClient,
                     configMap.get (TestConstants.KEY_IDP_REALM));
             if (idpcotPage.getWebResponse().getContentAsString().
                     contains (configMap.get (TestConstants.KEY_IDP_COT))) {
                 log (logLevel, "UnconfigureSAMLv2", "COT exists at IDP side",
                         null);
                 idpcotPage = idpfm.deleteCircleOfTrust(webClient,
                         configMap.get(TestConstants.KEY_IDP_COT),
                         configMap.get(TestConstants.KEY_IDP_REALM));
                 if (!idpcotPage.getWebResponse().getContentAsString().
                         contains("Circle of trust, " +
                         configMap.get(TestConstants.KEY_IDP_COT)
                         + " is deleted.")) {
                     log (logLevel, "UnconfigureSAMLv2", "Couldn't delete " +
                             "COT at IDP side" +
                             idpcotPage.getWebResponse().getContentAsString(), null);
                 } else {
                     log (logLevel, "UnconfigureSAMLv2", "Deleted COT " +
                             "at IDP side", null);                    
                 }
             }
             
             HtmlPage spEntityPage = spfm.listEntities (webClient,
                     configMap.get (TestConstants.KEY_SP_REALM), "saml2");
             //Delete SP & IDP entities on sp
             if (spEntityPage.getWebResponse().getContentAsString().
                     contains (configMap.get (TestConstants.KEY_SP_ENTITY_NAME))) {
                 log (logLevel, "UnconfigureSAMLv2", "sp entity exists at sp. ",
                         null);
                 HtmlPage spDeleteEntityPage = spfm.deleteEntity (webClient,
                         configMap.get (TestConstants.KEY_SP_ENTITY_NAME),
                         configMap.get (TestConstants.KEY_SP_REALM), false,
                         "saml2");
                 if (spDeleteEntityPage.getWebResponse().getContentAsString().
                         contains ("Descriptor is deleted for entity, " +
                         configMap.get (TestConstants.KEY_SP_ENTITY_NAME))) {
                     log (logLevel, "UnconfigureSAMLv2", "Deleted sp entity on " +
                             "SP side", null);
                 } else {
                     log (logLevel, "UnconfigureSAMLv2", "Couldnt delete idp " +
                             "entity on SP side", null);
                     assert false;
                 }
             }
             if (spEntityPage.getWebResponse().getContentAsString().
                     contains (configMap.get (TestConstants.KEY_IDP_ENTITY_NAME))) {
                 log (logLevel, "UnconfigureSAMLv2", "idp entity exists at sp. ", 
                         null);
                 HtmlPage idpDeleteEntityPage = spfm.deleteEntity (webClient,
                         configMap.get (TestConstants.KEY_IDP_ENTITY_NAME),
                         configMap.get (TestConstants.KEY_SP_REALM), false,
                         "saml2");
                 if (idpDeleteEntityPage.getWebResponse().getContentAsString().
                         contains ("Descriptor is deleted for entity, " +
                         configMap.get (TestConstants.KEY_IDP_ENTITY_NAME))) {
                     log (logLevel, "UnconfigureSAMLv2", "Deleted idp entity on " +
                             "SP side", null);
                 } else {
                     log (logLevel, "UnconfigureSAMLv2", "Couldnt delete idp " +
                             "entity on SP side", null);
                     assert false;
                 }
             }
             
             //Delete COT on sp side.
             HtmlPage spcotPage = spfm.listCircleOfTrusts (webClient,
                     configMap.get (TestConstants.KEY_SP_REALM));
             if (spcotPage.getWebResponse().getContentAsString().
                     contains (configMap.get (TestConstants.KEY_SP_COT))) {
                 spcotPage = spfm.deleteCircleOfTrust (webClient,
                         configMap.get (TestConstants.KEY_SP_COT),
                         configMap.get (TestConstants.KEY_SP_REALM));
                 if (!spcotPage.getWebResponse().getContentAsString().
                         contains("Circle of trust, "
                         + configMap.get (TestConstants.KEY_SP_COT)
                         + " is deleted.")) {
                     log (logLevel, "UnconfigureSAMLv2", "Couldn't delete " +
                             "COT at SP side" +
                             spcotPage.getWebResponse().getContentAsString(), null);
                     assert false;
                 } else {
                     log (logLevel, "UnconfigureSAMLv2", "Deleted COT " +
                             "at SP side", null);                    
                 }
             }
         } catch (Exception e) {
             log (Level.SEVERE, "UnconfigureSAMLv2", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout (webClient, spurl);
             consoleLogout (webClient, idpurl);
         }
         exiting ("UnconfigureSAMLv2");
     }
 }
