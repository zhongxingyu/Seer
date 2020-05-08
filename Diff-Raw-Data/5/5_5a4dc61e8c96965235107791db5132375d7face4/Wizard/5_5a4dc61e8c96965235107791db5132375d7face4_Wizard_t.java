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
 * $Id: Wizard.java,v 1.14 2008-05-02 05:52:28 hengming Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 package com.sun.identity.config.wizard;
 
 import com.sun.identity.config.pojos.LDAPStore;
 import com.sun.identity.config.util.AjaxPage;
 import com.sun.identity.setup.AMSetupServlet;
 import com.sun.identity.setup.ConfiguratorException;
 import com.sun.identity.setup.HttpServletRequestWrapper;
 import com.sun.identity.setup.HttpServletResponseWrapper;
 import com.sun.identity.setup.SetupProgress;
 import com.sun.identity.setup.SetupConstants;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.io.File;
 import javax.servlet.http.HttpServletRequest;
 import net.sf.click.control.ActionLink;
 import net.sf.click.Context;
 
 public class Wizard extends AjaxPage {
 
     public int startingTab = 1;
 
     public ActionLink createConfigLink = 
         new ActionLink("createConfig", this, "createConfig" );
     public ActionLink testUrlLink = 
         new ActionLink("testNewInstanceUrl", this, "testNewInstanceUrl" );
     public ActionLink pushConfigLink = 
         new ActionLink("pushConfig", this, "pushConfig" );
     
     private String cookieDomain = null;
     private String hostName = getHostName();
     private String dataStore = SetupConstants.SMS_EMBED_DATASTORE;
     
     public static String defaultUserName = "cn=Directory Manager";
     public static String defaultPassword = "";
     public static String defaultRootSuffix = "dc=opensso,dc=java,dc=net";
     public String defaultPort = Integer.toString(
         AMSetupServlet.getUnusedPort(hostName, 50389, 1000));
     
     /**
      * This is the 'execute' operation for the entire wizard.  This method 
      * aggregates all data submitted across the wizard pages here in one lump 
      * and hands it off to the back-end for processing.
      */
     public boolean createConfig() {
         HttpServletRequest req = getContext().getRequest();
         
         HttpServletRequestWrapper request = 
             new HttpServletRequestWrapper(getContext().getRequest());          
         HttpServletResponseWrapper response =                
             new HttpServletResponseWrapper(getContext().getResponse());        
         
         /* 
          * Get the admin password. use the same value for password and confirm
          * value because they were validated in the input screen
          */
         String adminPassword = (String)getContext().getSessionAttribute(
             SetupConstants.CONFIG_VAR_ADMIN_PWD);        
         request.addParameter(
             SetupConstants.CONFIG_VAR_ADMIN_PWD, adminPassword);
         request.addParameter(
             SetupConstants.CONFIG_VAR_CONFIRM_ADMIN_PWD, adminPassword);
             
         /*
          * Get the agent password. same value used for password and confirm
          * because they were validated in the input screen.
          */
         String agentPassword = (String)getContext().getSessionAttribute(
             SetupConstants.CONFIG_VAR_AMLDAPUSERPASSWD);
         request.addParameter(
             SetupConstants.CONFIG_VAR_AMLDAPUSERPASSWD, agentPassword);
         request.addParameter(
             SetupConstants.CONFIG_VAR_AMLDAPUSERPASSWD_CONFIRM, agentPassword);
         
         /* 
          * Set the data store information
          */
         String tmp = getAttribute(
             SetupConstants.CONFIG_VAR_DATA_STORE, 
             SetupConstants.SMS_EMBED_DATASTORE);
         request.addParameter(SetupConstants.CONFIG_VAR_DATA_STORE, tmp);
 
         boolean isEmbedded = false;
         if (tmp.equals(SetupConstants.SMS_EMBED_DATASTORE)) {
             tmp = getAttribute(SetupConstants.DS_EMB_REPL_FLAG, "false");
 
             /*
              * set the embedded replication information for local host port
              * and remote host port
              */
             isEmbedded = tmp.equals(SetupConstants.DS_EMP_REPL_FLAG_VAL);
             if (isEmbedded) {
                 request.addParameter(
                     SetupConstants.DS_EMB_REPL_FLAG,
                     SetupConstants.DS_EMP_REPL_FLAG_VAL);
                 
                 tmp = getAttribute("localRepPort", "");
                 request.addParameter(SetupConstants.DS_EMB_REPL_REPLPORT1, tmp);
 
                 tmp = getAttribute("existingHost", "");
                 request.addParameter(SetupConstants.DS_EMB_REPL_HOST2, tmp);
 
                 tmp = getAttribute("existingPort", "");
                 request.addParameter(SetupConstants.DS_EMB_REPL_PORT2, tmp);
 
                 tmp = getAttribute("existingRepPort", "");
                 request.addParameter(SetupConstants.DS_EMB_REPL_REPLPORT2, tmp);
             }
         }
 
         tmp = getAttribute("configStorePort", defaultPort);
         request.addParameter(
             SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_PORT, tmp); 
 
         tmp = getAttribute("rootSuffix", defaultRootSuffix);
         request.addParameter(SetupConstants.CONFIG_VAR_ROOT_SUFFIX, tmp);
        
         if (isEmbedded)
             tmp = getHostName();
         else
             tmp = getAttribute("configStoreHost", hostName);
         
         request.addParameter(
             SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_HOST, tmp);
 
         tmp = getAttribute("configStoreLoginId", defaultUserName);
         request.addParameter(
             SetupConstants.CONFIG_VAR_DS_MGR_DN, tmp);
 
         tmp = getAttribute("configStorePassword", "");
         request.addParameter(
             SetupConstants.CONFIG_VAR_DS_MGR_PWD, tmp);
         
         tmp = getAttribute(SetupConstants.CONFIG_VAR_DS_UM_SCHEMA,"");
         request.addParameter(
             SetupConstants.CONFIG_VAR_DS_UM_SCHEMA, tmp);
                 
         // user store repository
         tmp = (String)getContext().getSessionAttribute(
             SetupConstants.USER_STORE_HOST);        
         if (tmp != null) {                       
             Map store = new HashMap(12);  
             store.put(SetupConstants.USER_STORE_HOST, tmp);
 
             tmp = (String)getContext().getSessionAttribute(
                 SetupConstants.USER_STORE_PORT);
             store.put(SetupConstants.USER_STORE_PORT, tmp);
             tmp = (String)getContext().getSessionAttribute(
                 SetupConstants.USER_STORE_ROOT_SUFFIX);
             store.put(SetupConstants.USER_STORE_ROOT_SUFFIX, tmp);
             tmp = (String)getContext().getSessionAttribute(
                 SetupConstants.USER_STORE_LOGIN_ID);
             store.put(SetupConstants.USER_STORE_LOGIN_ID, tmp);      
             tmp = (String)getContext().getSessionAttribute(
                 SetupConstants.USER_STORE_LOGIN_PWD);
             store.put(SetupConstants.USER_STORE_LOGIN_PWD, tmp);      
             tmp = (String)getContext().getSessionAttribute(
                 SetupConstants.USER_STORE_TYPE);
             store.put(SetupConstants.USER_STORE_TYPE, tmp);                        
                                   
             request.addParameter("UserStore", store);
         }
         
         // site configuration is passed as a map of the site information 
         Map siteConfig = new HashMap(5);
         String loadBalancerHost = (String)getContext().getSessionAttribute( 
             SetupConstants.LB_SITE_NAME);
         String primaryURL = (String)getContext().getSessionAttribute(
             SetupConstants.LB_PRIMARY_URL);
         if (loadBalancerHost != null) {
             siteConfig.put(SetupConstants.LB_SITE_NAME, loadBalancerHost);
             siteConfig.put(SetupConstants.LB_PRIMARY_URL, primaryURL);
             request.addParameter(
                 SetupConstants.CONFIG_VAR_SITE_CONFIGURATION, siteConfig);
         }
 
         // server properties
         request.addParameter(
             SetupConstants.CONFIG_VAR_SERVER_HOST, getHostName());
         request.addParameter(
             SetupConstants.CONFIG_VAR_SERVER_PORT, 
             Integer.toString(req.getServerPort()));
         request.addParameter(
             SetupConstants.CONFIG_VAR_SERVER_URI, req.getRequestURI());
         request.addParameter(
             SetupConstants.CONFIG_VAR_SERVER_URL, 
            getAttribute("serverURL", req.getRequestURL().toString()));        
 
         tmp = (String)getContext().getSessionAttribute("encryptionKey");
         if (tmp == null) {
             tmp = AMSetupServlet.getRandomString();
         }
         request.addParameter(
             SetupConstants.CONFIG_VAR_ENCRYPTION_KEY, tmp);
 
         String cookie = 
             (String)getContext().getSessionAttribute("cookieDomain");
         if (cookie == null) {
             cookie = getCookieDomain();
         }
         request.addParameter(SetupConstants.CONFIG_VAR_COOKIE_DOMAIN, cookie);       
         
         String locale = 
             (String)getContext().getSessionAttribute("platformLocale");
         if (locale == null) {
             locale = SetupConstants.DEFAULT_PLATFORM_LOCALE;
         }
         request.addParameter(SetupConstants.CONFIG_VAR_PLATFORM_LOCALE, locale);
 
         String base = 
             (String)getContext().getSessionAttribute("configDirectory");
         if (base == null) {
             base = getBaseDir(getContext().getRequest());
         }
         request.addParameter(SetupConstants.CONFIG_VAR_BASE_DIR, base);
                    
         try {
             if (AMSetupServlet.processRequest(request, response)) {
                 writeToResponse("true");           
             } else {
                 writeToResponse(AMSetupServlet.getErrorMessage());
             }
         } catch (ConfiguratorException cfe) {
             writeToResponse(cfe.getMessage());
         } catch (Exception e) {
             writeToResponse("Error during configuration. Consult debug files for more information");
             debug.error("Wizard.createConfig() : error in processRequest: ", e);
         }
         
         setPath(null);
         return false;
     }
 
 
 }
