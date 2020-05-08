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
 * $Id: ChainTestValidation.java,v 1.2 2007-05-04 22:05:34 sridharev Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.authentication;
 
 import com.gargoylesoftware.htmlunit.Page;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.WebResponse;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.webtest.DefaultTaskHandler;
 import java.util.Map;
 import org.testng.annotations.Test;
 
 /**
  * <code>ChainTestValidation</code> Validates the given
  * Chain by logging into the chain and checks the success
  * and failure by checking the appropiate goto URLs in each case
  * The validation is performed by <code>WebTest</code>
  *
  */
 public class ChainTestValidation extends TestCommon {
     
     private Map mapService;
     private WebClient webClient;
     private String  baseDir;
     private String testURL;
     private String testLogoutURL;
    
     /** 
      * Default constructor for ChainTestValidation
      * @param map contains data that is required validate the chain/service
      */
     public ChainTestValidation(Map testMap) throws Exception {
         super("ChainTestValidation");
         mapService = testMap;
         testURL = protocol + ":" + "//" + host + ":" + port + uri;
         testLogoutURL = testURL + "/UI/Logout";
         baseDir = getBaseDir();
         log(logLevel,"ChainTestValidation" , "BaseDir: " + baseDir);
         mapService.put("url",testURL);
         mapService.put("baseDir",baseDir); 
     }
     
    /**
     * Performs Positive Service based service Login
     */
     @Test(groups = {"client"}) 
     public void testServicebasedPositive(){
        try{
            boolean isNegative = false;
            CreateTestXML testXML = new CreateTestXML();
            String xmlFile = testXML.createServiceXML(mapService,isNegative);
            log(logLevel,"testServicebasedPositive " , xmlFile);
            DefaultTaskHandler task = new DefaultTaskHandler(xmlFile);
            webClient = new WebClient();
            Page page = task.execute(webClient);
            consoleLogout(webClient,testLogoutURL);
        }catch (Exception e){
            log(logLevel.SEVERE, "testServicebasedPositive", 
                    e.getMessage(), null);
            e.printStackTrace();
        }
    }
    
    /**
     * Performs Negative Service based service Login
     */
    @Test(groups = {"client"})
     public void testServicebasedNegative(){
         try{
             boolean isNegative = true;
             CreateTestXML testXML = new CreateTestXML();
             String xmlFile = testXML.createServiceXML(mapService,isNegative);
             log(logLevel,"testServicebasedNegative ", xmlFile);
             webClient = new WebClient();
             DefaultTaskHandler task = new DefaultTaskHandler(xmlFile);
             Page page = task.execute(webClient);
             WebResponse wresponse = page.getWebResponse();
             String resString = wresponse.getContentAsString();
             log(logLevel,"testServicebasedNegative",resString);
             consoleLogout(webClient,testLogoutURL);
         }catch (Exception e){
             log(logLevel.SEVERE, "testServicebasedNegative", 
                     e.getMessage(), null);
             e.printStackTrace();
         }
     }
 }
