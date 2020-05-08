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
 * $Id: DefaultSummary.java,v 1.5 2008-01-18 22:15:34 jonnelson Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 package com.sun.identity.config;
 
 import com.sun.identity.setup.AMSetupServlet;
 import com.sun.identity.config.util.TemplatedPage;
 import net.sf.click.control.ActionLink;
 import net.sf.click.control.Form;
 import net.sf.click.control.FieldSet;
 import net.sf.click.control.HiddenField;
 import net.sf.click.control.Label;
 import net.sf.click.control.PasswordField;
 import net.sf.click.Context;
 import net.sf.click.control.Submit;
 
 import com.sun.identity.setup.SetupConstants;
 import com.sun.identity.config.util.AjaxPage;
 import java.io.File;
 
 import java.util.Iterator;
 import java.util.Enumeration;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * 
  */
 public class DefaultSummary extends AjaxPage {
 
     public Form defaultForm = new Form("defaultForm");
 
     private String cookieDomain = null;
     private String hostName = null;
     
     public void onInit() {
         HttpServletRequest request = getContext().getRequest();
         
         defaultForm.setColumns(2);
         FieldSet fieldSet = new FieldSet(
             "fieldSet", "Configuration Default Values");
         defaultForm.add(fieldSet);
         fieldSet.setShowBorder(true);              
         
         // User Name and Password Fields
         fieldSet.add(new Label("name", "Administrator:"));
         fieldSet.add(new Label("nameValue", "amAdmin"));
         defaultForm.add(new HiddenField("username", "amAdmin" ) );        
         fieldSet.add(new PasswordField(
             SetupConstants.CONFIG_VAR_ADMIN_PWD, 
             "Administrator Password", true), 2);
         fieldSet.add(new PasswordField(
             SetupConstants.CONFIG_VAR_CONFIRM_ADMIN_PWD, 
             "Retype Administrator Password", true ), 2);
         fieldSet.add(new PasswordField(
             SetupConstants.CONFIG_VAR_AMLDAPUSERPASSWD, 
             "Default Agent Password", true ), 2);
         fieldSet.add(new PasswordField(
             SetupConstants.CONFIG_VAR_AMLDAPUSERPASSWD_CONFIRM, 
             "Retype Default Agent Password", true ), 2);
                 
         defaultForm.add(new HiddenField(
             SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_HOST, getHostName()));
         defaultForm.add(new HiddenField(
             SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_PORT, 
             "" + AMSetupServlet.getUnusedPort(getHostName(),50389, 1000)));
         
         defaultForm.add(new HiddenField(
             SetupConstants.CONFIG_VAR_SERVER_HOST, getHostName()));
         defaultForm.add(new HiddenField(
             SetupConstants.CONFIG_VAR_SERVER_PORT, ""+request.getServerPort()));
         defaultForm.add(new HiddenField(
             SetupConstants.CONFIG_VAR_SERVER_URI, request.getRequestURI()));
         defaultForm.add(new HiddenField(
             SetupConstants.CONFIG_VAR_SERVER_URL, request.getRequestURL().toString()));
         
         defaultForm.add(new HiddenField(
             SetupConstants.CONFIG_VAR_BASE_DIR, getBaseDir()));
 
         defaultForm.add(new HiddenField(
             SetupConstants.CONFIG_VAR_ENCRYPTION_KEY, 
             AMSetupServlet.getRandomString()));
         
         defaultForm.add(new HiddenField(
             SetupConstants.CONFIG_VAR_COOKIE_DOMAIN, getCookieDomain()));
         
         
         defaultForm.add(new HiddenField(
             SetupConstants.CONFIG_VAR_DS_MGR_PWD, ""));
         
         defaultForm.add(new HiddenField(SetupConstants.CONFIG_VAR_DATA_STORE,
             SetupConstants.SMS_EMBED_DATASTORE));                       
                 
         defaultForm.add(new HiddenField(SetupConstants.CONFIG_VAR_PLATFORM_LOCALE, 
             SetupConstants.DEFAULT_PLATFORM_LOCALE));
         
         Submit submit = new Submit("save", getMessage("save"), this, "onSubmit" );
         submit.setAttribute( "onclick", "submitDefaultSummaryForm(); return false;");
         
         defaultForm.add(submit);
     }
 
     public boolean onSubmit() {            
         HttpServletResponse response = getContext().getResponse();
         HttpServletRequest request = getContext().getRequest();
         
         if (defaultForm.isValid() ) {
             String password = defaultForm.getField(
                 SetupConstants.CONFIG_VAR_ADMIN_PWD).getValue();
             String passwordConfirm = defaultForm.getField(
                     SetupConstants.CONFIG_VAR_CONFIRM_ADMIN_PWD).getValue();
                         
             if (!password.equals(passwordConfirm) ) {
                 defaultForm.setError(getLocalizedString("newPassword.error" ) );
                 response.setHeader("formError", "true" );
                 return false;
             } else {                 
                 String agentPassword = defaultForm.getField(
                     SetupConstants.CONFIG_VAR_AMLDAPUSERPASSWD).getValue();
                 String agentConfirm = defaultForm.getField(
                     SetupConstants.CONFIG_VAR_AMLDAPUSERPASSWD_CONFIRM).getValue();
                 
                 if (agentPassword.equals(agentConfirm)) {
                     if (!agentPassword.equals(password)) {
                         if (!AMSetupServlet.processRequest(request, response)) {
                             // configuration failed.
                             // tbd: get the reason for the failure.
                             defaultForm.setError(getLocalizedString(
                                 "configuration.failed.error"));
                             response.setHeader("formError","true");
                             return false;
                         }
                     } else {
                         // agent and admin password are not different
                         defaultForm.setError(getLocalizedString(
                             "configurator.urlaccessagent.passwd.match.amadmin.pwd"));
                         response.setHeader("formError", "true");
                         return false;
                     }
                 } else {
                     // agent passwords did not match
                     defaultForm.setError(getLocalizedString(
                         "configurator.urlaccessagent.passwd.nomatch"));
                     response.setHeader("formError", "true");
                     return false;
                 }                
             }
         } else {
             defaultForm.setError("Processing error in page.");
             response.setHeader("formError", "true" );
             return false;
         }
         
         // configuration went ok...
         return true;
     }
 }
