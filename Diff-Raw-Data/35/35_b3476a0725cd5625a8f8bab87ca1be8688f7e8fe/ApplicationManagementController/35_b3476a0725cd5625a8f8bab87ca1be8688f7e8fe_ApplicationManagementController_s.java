 /**
  * Copyright (c) 2009 - 2013 By: CWS, Inc.
  * 
  * All rights reserved. These materials are confidential and
  * proprietary to CaspersBox Web Services N.A and no part of
  * these materials should be reproduced, published in any form
  * by any means, electronic or mechanical, including photocopy
  * or any information storage or retrieval system not should
  * the materials be disclosed to third parties without the
  * express written authorization of CaspersBox Web Services, N.A.
  */
 package com.cws.us.esolutions.controllers;
 
 import java.io.File;
 import java.util.Map;
 import java.util.List;
 import java.util.UUID;
 import java.util.Arrays;
 import org.slf4j.Logger;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.io.IOException;
 import java.util.Enumeration;
 import org.slf4j.LoggerFactory;
 import org.apache.commons.io.IOUtils;
 import javax.servlet.http.HttpSession;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import javax.servlet.http.HttpServletRequest;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.view.RedirectView;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 
 import com.cws.us.esolutions.Constants;
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.us.esolutions.ApplicationServiceBean;
 import com.cws.us.esolutions.dto.ApplicationRequest;
 import com.cws.esolutions.core.processors.dto.Server;
 import com.cws.esolutions.core.processors.dto.Project;
 import com.cws.esolutions.core.processors.dto.Platform;
 import com.cws.esolutions.core.processors.dto.Application;
 import com.cws.esolutions.core.processors.dto.SearchResult;
 import com.cws.esolutions.core.processors.dto.SearchRequest;
 import com.cws.us.esolutions.validators.DeploymentValidator;
 import com.cws.esolutions.core.processors.dto.SearchResponse;
 import com.cws.us.esolutions.validators.ApplicationValidator;
 import com.cws.esolutions.security.audit.dto.RequestHostInfo;
 import com.cws.us.esolutions.validators.SearchRequestValidator;
 import com.cws.esolutions.core.processors.enums.CoreServicesStatus;
 import com.cws.esolutions.core.processors.dto.ProjectManagementRequest;
 import com.cws.esolutions.core.processors.dto.ProjectManagementResponse;
 import com.cws.esolutions.core.processors.dto.PlatformManagementRequest;
 import com.cws.esolutions.core.processors.dto.PlatformManagementResponse;
 import com.cws.esolutions.core.processors.dto.ApplicationManagementRequest;
 import com.cws.esolutions.core.processors.exception.SearchRequestException;
 import com.cws.esolutions.core.processors.dto.ApplicationManagementResponse;
 import com.cws.esolutions.core.processors.impl.ProjectManagementProcessorImpl;
 import com.cws.esolutions.core.processors.exception.ProjectManagementException;
 import com.cws.esolutions.core.processors.impl.PlatformManagementProcessorImpl;
 import com.cws.esolutions.core.processors.exception.PlatformManagementException;
 import com.cws.esolutions.core.processors.interfaces.IProjectManagementProcessor;
 import com.cws.esolutions.core.processors.impl.ApplicationManagementProcessorImpl;
 import com.cws.esolutions.core.processors.interfaces.IPlatformManagementProcessor;
 import com.cws.esolutions.core.processors.exception.ApplicationManagementException;
 import com.cws.esolutions.core.processors.interfaces.IApplicationManagementProcessor;
 /**
  * eSolutions_java_source
  * com.cws.us.esolutions.controllers
  * ApplicationManagementController.java
  *
  * $Id$
  * $Author$
  * $Date$
  * $Revision$
  * @author kh05451
  * @version 1.0
  *
  * History
  * ----------------------------------------------------------------------------
  * kh05451 @ Jan 16, 2013 11:53:26 AM
  *     Created.
  */
 @Controller
 @RequestMapping("/application-management")
 public class ApplicationManagementController
 {
     private String applMgmt = null;
     private String addAppPage = null;
     private String projectMgmt = null;
     private String serviceName = null;
     private String defaultPage = null;
     private String viewAppPage = null;
     private String viewFilePage = null;
     private String platformMgmt = null;
     private String deployAppPage = null;
     private String messageNoFileData = null;
     private String retrieveFilesPage = null;
     private String addProjectRedirect = null;
     private String addPlatformRedirect = null;
     private String messageFileUploaded = null;
     private String viewApplicationsPage = null;
     private String addApplicationRedirect = null;
     private String messageNoBinaryProvided = null;
     private String messageApplicationAdded = null;
     private ApplicationServiceBean appConfig = null;
     private String messageNoPlatformAssigned = null;
     private String messageApplicationRetired = null;
     private String messageNoApplicationsFound = null;
     private String messageNoAppVersionProvided = null;
     private SearchRequestValidator searchValidator = null;
     private DeploymentValidator deploymentValidator = null;
     private ApplicationValidator applicationValidator = null;
 
     private static final String CNAME = ApplicationManagementController.class.getName();
 
     private static final Logger DEBUGGER = LoggerFactory.getLogger(Constants.DEBUGGER);
     private static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     private static final Logger ERROR_RECORDER = LoggerFactory.getLogger(Constants.ERROR_LOGGER + CNAME);
 
     public final void setApplicationValidator(final ApplicationValidator value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setValidator(final ApplicationValidator value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.applicationValidator = value;
     }
 
     public final void setDeploymentValidator(final DeploymentValidator value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setValidator(final DeploymentValidator value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.deploymentValidator = value;
     }
 
     public final void setSearchValidator(final SearchRequestValidator value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setSearchValidator(final ServerValidator value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.searchValidator = value;
     }
 
     public final void setAppConfig(final ApplicationServiceBean value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setAppConfig(final CoreServiceBean value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.appConfig = value;
     }
 
     public final void setAddAppPage(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setAddAppPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.addAppPage = value;
     }
 
     public final void setServiceName(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setServiceName(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.serviceName = value;
     }
 
     public final void setDefaultPage(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setDefaultPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.defaultPage = value;
     }
 
     public final void setViewAppPage(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setViewAppPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.viewAppPage = value;
     }
 
     public final void setViewFilePage(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setViewFilePage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.viewFilePage = value;
     }
 
     public final void setDeployAppPage(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setDeployAppPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.deployAppPage = value;
     }
 
     public final void setRetrieveFilesPage(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setRetrieveFilesPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.retrieveFilesPage = value;
     }
 
     public final void setApplMgmt(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setApplMgmt(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.applMgmt = value;
     }
 
     public final void setProjectMgmt(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setProjectMgmt(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.projectMgmt = value;
     }
 
     public final void setPlatformMgmt(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setPlatformMgmt(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.platformMgmt = value;
     }
 
     public final void setAddProjectRedirect(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setAddProjectRedirect(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.addProjectRedirect = value;
     }
 
     public final void setAddPlatformRedirect(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setAddPlatformRedirect(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.addPlatformRedirect = value;
     }
 
     public final void setViewApplicationsPage(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setViewApplicationsPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.viewApplicationsPage = value;
     }
 
     public final void setMessageNoApplicationsFound(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setMessageNoApplicationsFound(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageNoApplicationsFound = value;
     }
 
     public final void setMessageNoFileData(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setMessageNoFileData(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageNoFileData = value;
     }
 
     public final void setMessageNoPlatformAssigned(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setMessageNoPlatformAssigned(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageNoPlatformAssigned = value;
     }
 
     public final void setMessageApplicationAdded(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setMessageApplicationAdded(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageApplicationAdded = value;
     }
 
     public final void setMessageFileUploaded(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setMessageFileUploaded(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageFileUploaded = value;
     }
 
     public final void setMessageNoBinaryProvided(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setMessageNoBinaryProvided(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageNoBinaryProvided = value;
     }
 
     public final void setMessageNoAppVersionProvided(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setMessageNoAppVersionProvided(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageNoAppVersionProvided = value;
     }
 
     public final void setMessageApplicationRetired(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setMessageApplicationRetired(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageApplicationRetired = value;
     }
 
     public final void setAddApplicationRedirect(final String value)
     {
         final String methodName = ApplicationManagementController.CNAME + "#setAddApplicationRedirect(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.addApplicationRedirect = value;
     }
 
     @RequestMapping(value = "/default", method = RequestMethod.GET)
     public final ModelAndView showDefaultPage()
     {
         final String methodName = ApplicationManagementController.CNAME + "#showDefaultPage()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             mView.addObject("command", new SearchRequest());
             mView.setViewName(this.defaultPage);
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/list-applications", method = RequestMethod.GET)
     public final ModelAndView doListApplications()
     {
         final String methodName = ApplicationManagementController.CNAME + "#doListApplications()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IApplicationManagementProcessor appMgr = new ApplicationManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 ApplicationManagementRequest appRequest = new ApplicationManagementRequest();
                 appRequest.setRequestInfo(reqInfo);
                 appRequest.setServiceId(this.applMgmt);
                 appRequest.setUserAccount(userAccount);
                 appRequest.setApplicationId(this.appConfig.getApplicationId());
                 appRequest.setApplicationName(this.appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementRequest: {}", appRequest);
                 }
 
                 ApplicationManagementResponse appResponse = appMgr.listApplications(appRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementResponse: {}", appResponse);
                 }
 
                 if (appResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     List<Application> applicationList = appResponse.getApplicationList();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("List<Application>: {}", applicationList);
                     }
 
                     if ((applicationList != null) && (applicationList.size() != 0))
                     {
                         mView.addObject("applicationList", applicationList);
                         mView.setViewName(this.viewApplicationsPage);
                     }
                     else
                     {
                         mView.addObject(Constants.ERROR_MESSAGE, this.messageNoApplicationsFound);
                         mView.setViewName(this.defaultPage);
                     }
                 }
                 else if (appResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView = new ModelAndView(new RedirectView());
                     mView.addObject(Constants.ERROR_RESPONSE, appResponse.getResponse());
                     mView.setViewName(this.addApplicationRedirect);
                 }
             }
             catch (ApplicationManagementException amx)
             {
                 ERROR_RECORDER.error(amx.getMessage(), amx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/application/{request}", method = RequestMethod.GET)
     public final ModelAndView showApplication(@PathVariable("request") final String request)
     {
         final String methodName = ApplicationManagementController.CNAME + "#showApplication(@PathVariable(\"request\") final String request)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("request: {}", request);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IApplicationManagementProcessor appMgr = new ApplicationManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Application reqApplication = new Application();
                 reqApplication.setApplicationGuid(request);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Application: {}", reqApplication);
                 }
 
                 // get a list of available servers
                 ApplicationManagementRequest appRequest = new ApplicationManagementRequest();
                 appRequest.setRequestInfo(reqInfo);
                 appRequest.setUserAccount(userAccount);
                 appRequest.setServiceId(this.applMgmt);
                 appRequest.setApplication(reqApplication);
                 appRequest.setApplicationId(this.appConfig.getApplicationId());
                 appRequest.setApplicationName(this.appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementRequest: {}", appRequest);
                 }
 
                 ApplicationManagementResponse appResponse = appMgr.getApplicationData(appRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementResponse: {}", appResponse);
                 }
 
                 if (appResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     Application resApplication = appResponse.getApplication();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Application: {}", resApplication);
                     }
 
                     mView.addObject("application", resApplication);
                     mView.setViewName(this.viewAppPage);
                 }
                 else if (appResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_RESPONSE, appResponse.getResponse());
                     mView.setViewName(this.defaultPage);
                 }
             }
             catch (ApplicationManagementException amx)
             {
                 ERROR_RECORDER.error(amx.getMessage(), amx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/add-application", method = RequestMethod.GET)
     public final ModelAndView showAddApplication()
     {
         final String methodName = ApplicationManagementController.CNAME + "#showAddApplication()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IProjectManagementProcessor projectMgr = new ProjectManagementProcessorImpl();
         final IPlatformManagementProcessor platformMgr = new PlatformManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             RequestHostInfo reqInfo = new RequestHostInfo();
             reqInfo.setHostName(hRequest.getRemoteHost());
             reqInfo.setHostAddress(hRequest.getRemoteAddr());
             reqInfo.setSessionId(hSession.getId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             }
 
             ProjectManagementRequest projectReq = new ProjectManagementRequest();
             projectReq.setRequestInfo(reqInfo);
             projectReq.setUserAccount(userAccount);
             projectReq.setServiceId(this.projectMgmt);
             projectReq.setApplicationId(this.appConfig.getApplicationId());
             projectReq.setApplicationName(this.appConfig.getApplicationName());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("ProjectManagementRequest: {}", projectReq);
             }
 
             try
             {
                 ProjectManagementResponse projectResponse = projectMgr.listProjects(projectReq);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ProjectManagementResponse: {}", projectResponse);
                 }
 
                 if (projectResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     List<Project> projects = projectResponse.getProjectList();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("projects: {}", projects);
                     }
 
                     if ((projects != null) && (projects.size() != 0))
                     {
                         Map<String, String> projectListing = new HashMap<>();
 
                         for (Project project : projects)
                         {
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("Project: {}", project);
                             }
 
                             projectListing.put(project.getProjectGuid(), project.getProjectCode());
                         }
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("projectListing: {}", projectListing);
                         }
 
                         mView.addObject("projectListing", projectListing);
                     }
                 }
                 else if (projectResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
 
                     return mView;
                 }
                 else
                 {
                     mView = new ModelAndView(new RedirectView());
                     mView.setViewName(this.addProjectRedirect);
 
                     return mView;
                 }
             }
             catch (ProjectManagementException pmx)
             {
                 mView = new ModelAndView(new RedirectView());
                 mView.setViewName(this.addProjectRedirect);
 
                 return mView;
             }
 
             PlatformManagementRequest platformReq = new PlatformManagementRequest();
             platformReq.setRequestInfo(reqInfo);
             platformReq.setServiceId(this.platformMgmt);
             platformReq.setUserAccount(userAccount);
             platformReq.setApplicationId(this.appConfig.getApplicationId());
             platformReq.setApplicationName(this.appConfig.getApplicationName());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("PlatformManagementRequest: {}", platformReq);
             }
 
             try
             {
                 PlatformManagementResponse platformResponse = platformMgr.listPlatforms(platformReq);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("PlatformManagementResponse: {}", platformResponse);
                 }
 
                 if (platformResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     List<Platform> platformList = platformResponse.getPlatformList();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("platformList: {}", platformList);
                     }
 
                     if ((platformList != null) && (platformList.size() != 0))
                     {
                         Map<String, String> platformListing = new HashMap<>();
 
                         for (Platform platform : platformList)
                         {
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("Platform: {}", platform);
                             }
 
                             platformListing.put(platform.getPlatformGuid(), platform.getPlatformName());
                         }
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("platformListing: {}", platformListing);
                         }
 
                         mView.addObject("platformListing", platformListing);
                     }
                 }
                 else if (platformResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
 
                     return mView;
                 }
                 else
                 {
                     mView = new ModelAndView(new RedirectView());
                     mView.setViewName(this.addPlatformRedirect);
 
                     return mView;
                 }
             }
             catch (PlatformManagementException pmx)
             {
                 mView = new ModelAndView(new RedirectView());
                 mView.setViewName(this.addPlatformRedirect);
 
                 return mView;
             }
 
             mView.addObject("command", new ApplicationRequest());
             mView.setViewName(this.addAppPage);
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/retire-application/application/{application}", method = RequestMethod.GET)
     public final ModelAndView showRetireApplication(@PathVariable("application") final String application)
     {
         final String methodName = ApplicationManagementController.CNAME + "#showRetireApplication(@PathVariable(\"application\") final String application)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", application);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IApplicationManagementProcessor appMgr = new ApplicationManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Application reqApplication = new Application();
                 reqApplication.setApplicationGuid(application);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Application: {}", reqApplication);
                 }
 
                 // get a list of available servers
                 ApplicationManagementRequest appRequest = new ApplicationManagementRequest();
                 appRequest.setRequestInfo(reqInfo);
                 appRequest.setUserAccount(userAccount);
                 appRequest.setServiceId(this.applMgmt);
                 appRequest.setApplication(reqApplication);
                 appRequest.setApplicationId(this.appConfig.getApplicationId());
                 appRequest.setApplicationName(this.appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementRequest: {}", appRequest);
                 }
 
                 ApplicationManagementResponse appRes = appMgr.deleteApplicationData(appRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementResponse: {}", appRes);
                 }
 
                 if (appRes.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     mView.addObject(Constants.RESPONSE_MESSAGE, this.messageApplicationRetired);
                     mView.setViewName(this.defaultPage);
                 }
                 else if (appRes.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_RESPONSE, appRes.getResponse());
                     mView.setViewName(this.defaultPage);
                 }
             }
             catch (ApplicationManagementException amx)
             {
                 ERROR_RECORDER.error(amx.getMessage(), amx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/retrieve-files/application/{application}", method = RequestMethod.GET)
     public final ModelAndView showRetrieveFilesPage(@PathVariable("application") final String application)
     {
         final String methodName = ApplicationManagementController.CNAME + "#showRetrieveFilesPage(@PathVariable(\"application\") final String application)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", application);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IApplicationManagementProcessor appMgr = new ApplicationManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             // validate the user has access
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Application appl = new Application();
                 appl.setApplicationGuid(application);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Application: {}", appl);
                 }
 
                 ApplicationManagementRequest appRequest = new ApplicationManagementRequest();
                 appRequest.setApplication(appl);
                 appRequest.setRequestInfo(reqInfo);
                 appRequest.setUserAccount(userAccount);
                 appRequest.setServiceId(this.applMgmt);
                 appRequest.setApplicationId(this.appConfig.getApplicationId());
                 appRequest.setApplicationName(this.appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementRequest: {}", appRequest);
                 }
 
                 ApplicationManagementResponse appResponse = appMgr.getApplicationData(appRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementResponse: {}", appResponse);
                 }
 
                 if (appResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     Application app = appResponse.getApplication();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Application: {}", app);
                     }
 
                     if (app != null)
                     {
                         List<Platform> platformList = app.getApplicationPlatforms();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("platformList: {}", platformList);
                         }
 
                         if ((platformList != null) && (platformList.size() != 0))
                         {
                             mView.addObject("platformList", platformList);
                             mView.addObject("application", app);
                             mView.setViewName(this.retrieveFilesPage);
                         }
                         else
                         {
                             mView.addObject(Constants.ERROR_MESSAGE, this.messageNoPlatformAssigned);
                             mView.setViewName(this.defaultPage);
                         }
                     }
                     else
                     {
                         mView.addObject(Constants.ERROR_MESSAGE, this.messageNoApplicationsFound);
                         mView.setViewName(this.defaultPage);
                     }
                 }
                 else if (appResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView.addObject(Constants.RESPONSE_MESSAGE, appResponse.getResponse());
                     mView.setViewName(this.defaultPage);
                 }
             }
             catch (ApplicationManagementException amx)
             {
                 ERROR_RECORDER.error(amx.getMessage(), amx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/retrieve-files/application/{application}/platform/{platform}", method = RequestMethod.GET)
     public final ModelAndView showRetrieveFilesPage(@PathVariable("application") final String application, @PathVariable("platform") final String platform)
     {
         final String methodName = ApplicationManagementController.CNAME + "#showRetrieveFilesPage(@PathVariable(\"application\") final String application, @PathVariable(\"platform\") final String platform)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", platform);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IPlatformManagementProcessor platformMgr = new PlatformManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             // validate the user has access
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Application app = new Application();
                 app.setApplicationGuid(application);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Application: {}", app);
                 }
 
                 Platform reqPlatform = new Platform();
                 reqPlatform.setPlatformGuid(platform);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Platform: {}", reqPlatform);
                 }
 
                 PlatformManagementRequest platformRequest = new PlatformManagementRequest();
                 platformRequest.setUserAccount(userAccount);
                 platformRequest.setRequestInfo(reqInfo);
                 platformRequest.setServiceId(this.platformMgmt);
                 platformRequest.setPlatform(reqPlatform);
                 platformRequest.setApplicationId(this.appConfig.getApplicationId());
                 platformRequest.setApplicationName(this.appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("PlatformManagementRequest: {}", platformRequest);
                 }
 
                 PlatformManagementResponse platformResponse = platformMgr.getPlatformData(platformRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("PlatformManagementResponse: {}", platformResponse);
                 }
 
                 if (platformResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     Platform resPlatform = platformResponse.getPlatformData();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Platform: {}", resPlatform);
                     }
 
                     if (resPlatform != null)
                     {
                         List<Server> webServerList = resPlatform.getWebServers();
                         List<Server> appServerList = resPlatform.getAppServers();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("webServerList: {}", webServerList);
                             DEBUGGER.debug("appServerList: {}", appServerList);
                         }
 
                         mView.addObject("platform", resPlatform);
                         mView.addObject("webServerList", webServerList);
                         mView.addObject("appServerList", appServerList);
                         mView.addObject("application", app);
                         mView.setViewName(this.retrieveFilesPage);
                     }
                     else
                     {
                         mView.addObject(Constants.ERROR_MESSAGE, this.messageNoPlatformAssigned);
                         mView.setViewName(this.defaultPage);
                     }
                 }
                 else if (platformResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView.addObject(Constants.RESPONSE_MESSAGE, platformResponse.getResponse());
                     mView.setViewName(this.defaultPage);
                 }
             }
             catch (PlatformManagementException pmx)
             {
                 ERROR_RECORDER.error(pmx.getMessage(), pmx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/retrieve-files/application/{application}/platform/{platform}/server/{server}", method = RequestMethod.GET)
     public final ModelAndView showRetrieveFilesPage(@PathVariable("application") final String application, @PathVariable("platform") final String platform, @PathVariable("server") final String server)
     {
         final String methodName = ApplicationManagementController.CNAME + "#showRetrieveFilesPage(@PathVariable(\"application\") final String application, @PathVariable(\"server\") final String server)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", application);
             DEBUGGER.debug("Value: {}", server);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IApplicationManagementProcessor appMgr = new ApplicationManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             // validate the user has access
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Platform reqPlatform = new Platform();
                 reqPlatform.setPlatformGuid(platform);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Platform: {}", reqPlatform);
                 }
 
                 Application appl = new Application();
                 appl.setApplicationGuid(application);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Application: {}", appl);
                 }
 
                 Server targetServer = new Server();
                 targetServer.setServerGuid(server);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Server: {}", targetServer);
                 }
 
                 ApplicationManagementRequest appRequest = new ApplicationManagementRequest();
                 appRequest.setApplication(appl);
                 appRequest.setRequestInfo(reqInfo);
                 appRequest.setUserAccount(userAccount);
                 appRequest.setServiceId(this.applMgmt);
                 appRequest.setServer(targetServer);
                 appRequest.setApplicationId(this.appConfig.getApplicationId());
                 appRequest.setApplicationName(this.appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementRequest: {}", appRequest);
                 }
 
                 ApplicationManagementResponse appResponse = appMgr.applicationFileRequest(appRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementResponse: {}", appResponse);
                 }
 
                 if (appResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     List<String> fileList = appResponse.getFileList();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("List<String>: {}", fileList);
                     }
 
                     if ((fileList != null) && (fileList.size() != 0))
                     {
                         mView.addObject("fileList", fileList);
                         mView.addObject("application", appl);
                         mView.addObject("server", targetServer);
                         mView.addObject("platform", reqPlatform);
                         mView.addObject("currentPath", appResponse.getApplication().getBasePath());
                         mView.setViewName(this.retrieveFilesPage);
                     }
                 }
                 else if (appResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_RESPONSE, appResponse.getResponse());
                     mView.addObject("application", appResponse.getApplication());
                     mView.setViewName(this.viewAppPage);
                 }
             }
             catch (ApplicationManagementException amx)
             {
                 ERROR_RECORDER.error(amx.getMessage(), amx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/list-files/application/{application}/platform/{platform}/server/{server}", method = RequestMethod.GET)
     public final ModelAndView showListFiles(@PathVariable("application") final String application, @PathVariable("platform") final String platform, @PathVariable("server") final String server, @RequestParam(value = "vpath", required = true) final String vpath)
     {
         final String methodName = ApplicationManagementController.CNAME + "#showListFiles(@PathVariable(\"application\") final String application, @PathVariable(\"platform\") final String platform, @PathVariable(\"server\") final String server, @RequestParam(value = \"vpath\", required = true) final String vpath)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", application);
             DEBUGGER.debug("Value: {}", server);
             DEBUGGER.debug("Value: {}", vpath);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IApplicationManagementProcessor appMgr = new ApplicationManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             // validate the user has access
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Platform reqPlatform = new Platform();
                 reqPlatform.setPlatformGuid(platform);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Platform: {}", reqPlatform);
                 }
 
                 Server targetServer = new Server();
                 targetServer.setServerGuid(server);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Server: {}", targetServer);
                 }
 
                 Application targetApp = new Application();
                 targetApp.setApplicationGuid(application);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Application: {}", targetApp);
                 }
 
                 ApplicationManagementRequest appRequest = new ApplicationManagementRequest();
                 appRequest.setApplication(targetApp);
                 appRequest.setRequestInfo(reqInfo);
                 appRequest.setUserAccount(userAccount);
                 appRequest.setServiceId(this.applMgmt);
                 appRequest.setServer(targetServer);
                 appRequest.setRequestFile(vpath);
                 appRequest.setApplicationId(this.appConfig.getApplicationId());
                 appRequest.setApplicationName(this.appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementRequest: {}", appRequest);
                 }
 
                 ApplicationManagementResponse appResponse = appMgr.applicationFileRequest(appRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementResponse: {}", appResponse);
                 }
 
                 if (appResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     // we're going to go out, get a list of files/directories
                     // in the base of the application, and return that back here
                     // then list them out as links on the display
                     // need a way to ensure the existing selected path is here
                     // as well as the newly selected path too
                     if (appResponse.getFileData() != null)
                     {
                         byte[] fileData = appResponse.getFileData();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("fileData: {}", fileData);
                         }
 
                         if ((fileData != null) && (fileData.length != 0))
                         {
                             String dataString = IOUtils.toString(fileData, this.appConfig.getFileEncoding());
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("dataString: {}", dataString);
                             }
 
                             if (StringUtils.isNotEmpty(dataString))
                             {
                                 dataString = StringUtils.replace(dataString, "<", "&lt;");
                                 dataString = StringUtils.replace(dataString, ">", "&gt;");
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("dataString: {}", dataString);
                                 }
 
                                 if (StringUtils.isNotEmpty(dataString))
                                 {
                                     mView.addObject("server", targetServer);
                                     mView.addObject("platform", reqPlatform);
                                     mView.addObject("fileData", dataString);
                                     mView.addObject("application", appResponse.getApplication());
                                     mView.addObject("currentPath", vpath);
                                     mView.setViewName(this.viewFilePage);
                                 }
                                 else
                                 {
                                     mView.addObject(Constants.ERROR_MESSAGE, this.messageNoFileData);
                                     mView.addObject("platform", reqPlatform);
                                     mView.addObject("server", targetServer);
                                     mView.addObject("application", appResponse.getApplication());
                                     mView.addObject("currentPath", vpath);
                                     mView.setViewName(this.viewFilePage);
                                 }
                             }
                             else
                             {
                                 mView.addObject(Constants.ERROR_MESSAGE, this.messageNoFileData);
                                 mView.addObject("platform", reqPlatform);
                                 mView.addObject("server", targetServer);
                                 mView.addObject("application", appResponse.getApplication());
                                 mView.addObject("currentPath", vpath);
                                 mView.setViewName(this.viewFilePage);
                             }
                         }
                         else
                         {
                             mView.addObject(Constants.ERROR_MESSAGE, this.messageNoFileData);
                             mView.addObject("platform", reqPlatform);
                             mView.addObject("server", targetServer);
                             mView.addObject("application", appResponse.getApplication());
                             mView.addObject("currentPath", vpath);
                             mView.setViewName(this.viewFilePage);
                         }
                     }
                     else
                     {
                         List<String> fileList = appResponse.getFileList();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("List<String>: {}", fileList);
                         }
 
                         mView.addObject("platform", reqPlatform);
                         mView.addObject("application", appResponse.getApplication());
                         mView.addObject("currentPath", vpath);
                         mView.addObject("server", targetServer);
                         mView.addObject("fileList", fileList);
                         mView.setViewName(this.retrieveFilesPage);
                     }
                 }
                 else if (appResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_RESPONSE, appResponse.getResponse());
                     mView.addObject("application", application);
                     mView.setViewName(this.viewAppPage);
                 }
             }
             catch (ApplicationManagementException amx)
             {
                 ERROR_RECORDER.error(amx.getMessage(), amx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
             catch (IOException iox)
             {
                 ERROR_RECORDER.error(iox.getMessage(), iox);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/deploy-application/application/{application}", method = RequestMethod.GET)
     public final ModelAndView showDeployApplication(@PathVariable("application") final String application)
     {
         final String methodName = ApplicationManagementController.CNAME + "#showDeployApplication(@PathVariable(\"application\") final String application)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", application);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IApplicationManagementProcessor appMgr = new ApplicationManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             // validate the user has access
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Application appl = new Application();
                 appl.setApplicationGuid(application);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Application: {}", appl);
                 }
 
                 ApplicationManagementRequest appRequest = new ApplicationManagementRequest();
                 appRequest.setApplication(appl);
                 appRequest.setRequestInfo(reqInfo);
                 appRequest.setUserAccount(userAccount);
                 appRequest.setServiceId(this.applMgmt);
                 appRequest.setApplicationId(this.appConfig.getApplicationId());
                 appRequest.setApplicationName(this.appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementRequest: {}", appRequest);
                 }
 
                 ApplicationManagementResponse appResponse = appMgr.getApplicationData(appRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementResponse: {}", appResponse);
                 }
 
                 if (appResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     Application app = appResponse.getApplication();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Application: {}", app);
                     }
 
                     if (app != null)
                     {
                         List<Platform> appPlatforms = app.getApplicationPlatforms();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("Platform: {}", appPlatforms);
                         }
 
                         if ((appPlatforms != null) && (appPlatforms.size() != 0))
                         {
                             // add the server listing to the page
                             mView.addObject("application", app);
                             mView.addObject("platformList", appPlatforms);
                             mView.setViewName(this.deployAppPage);
                         }
                         else
                         {
                             mView.addObject(Constants.ERROR_MESSAGE, this.messageNoPlatformAssigned);
                             mView.setViewName(this.defaultPage);
                         }
                     }
                     else
                     {
                         mView.addObject(Constants.ERROR_MESSAGE, this.messageNoApplicationsFound);
                         mView.setViewName(this.defaultPage);
                     }
                 }
                 else if (appResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_RESPONSE, appResponse.getResponse());
                     mView.setViewName(this.appConfig.getErrorResponsePage());
                 }
             }
             catch (ApplicationManagementException amx)
             {
                 ERROR_RECORDER.error(amx.getMessage(), amx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/deploy-application/application/{application}/platform/{platform}", method = RequestMethod.GET)
     public final ModelAndView showDeployApplication(@PathVariable("application") final String application, @PathVariable("platform") final String platform)
     {
         final String methodName = ApplicationManagementController.CNAME + "#showDeployApplication(@PathVariable(\"application\") final String application, @PathVariable(\"platform\") final String platform)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", application);
             DEBUGGER.debug("Value: {}", platform);
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", application);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IPlatformManagementProcessor platformMgr = new PlatformManagementProcessorImpl();
         final IApplicationManagementProcessor appMgr = new ApplicationManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             // validate the user has access
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Application appl = new Application();
                 appl.setApplicationGuid(application);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Application: {}", appl);
                 }
 
                 Platform reqPlatform = new Platform();
                 reqPlatform.setPlatformGuid(platform);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Platform: {}", reqPlatform);
                 }
 
                 PlatformManagementRequest platformRequest = new PlatformManagementRequest();
                 platformRequest.setUserAccount(userAccount);
                 platformRequest.setRequestInfo(reqInfo);
                 platformRequest.setServiceId(this.platformMgmt);
                 platformRequest.setPlatform(reqPlatform);
                 platformRequest.setApplicationId(this.appConfig.getApplicationId());
                 platformRequest.setApplicationName(this.appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("PlatformManagementRequest: {}", platformRequest);
                 }
 
                 PlatformManagementResponse platformResponse = platformMgr.getPlatformData(platformRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("PlatformManagementResponse: {}", platformResponse);
                 }
 
                 if (platformResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     Platform resPlatform = platformResponse.getPlatformData();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Platform: {}", resPlatform);
                     }
 
                     if (resPlatform != null)
                     {
                         // get the app info
                         ApplicationManagementRequest appRequest = new ApplicationManagementRequest();
                         appRequest.setApplication(appl);
                         appRequest.setRequestInfo(reqInfo);
                         appRequest.setUserAccount(userAccount);
                         appRequest.setServiceId(this.applMgmt);
                         appRequest.setApplicationId(this.appConfig.getApplicationId());
                         appRequest.setApplicationName(this.appConfig.getApplicationName());
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ApplicationManagementRequest: {}", appRequest);
                         }
 
                         ApplicationManagementResponse appResponse = appMgr.getApplicationData(appRequest);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ApplicationManagementResponse: {}", appResponse);
                         }
 
                         if (appResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                         {
                             Application app = appResponse.getApplication();
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("Application: {}", app);
                             }
 
                             if (app != null)
                             {
                                 // add the server listing to the page
                                 mView.addObject("command", new ApplicationRequest());
                                 mView.addObject("application", app);
                                 mView.addObject("platform", resPlatform);
                                 mView.setViewName(this.deployAppPage);
                             }
                             else
                             {
                                 mView.addObject(Constants.ERROR_MESSAGE, this.messageNoApplicationsFound);
                                 mView.setViewName(this.defaultPage);
                             }
                         }
                         else if (appResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                         {
                             mView.setViewName(this.appConfig.getUnauthorizedPage());
                         }
                         else
                         {
                             mView.addObject(Constants.ERROR_RESPONSE, appResponse.getResponse());
                             mView.setViewName(this.appConfig.getErrorResponsePage());
                         }
                     }
                     else
                     {
                         mView.addObject(Constants.ERROR_MESSAGE, this.messageNoPlatformAssigned);
                         mView.setViewName(this.defaultPage);
                     }
                 }
                 else if (platformResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_MESSAGE, platformResponse.getResponse());
                     mView.setViewName(this.defaultPage);
                 }
             }
             catch (PlatformManagementException pmx)
             {
                 ERROR_RECORDER.error(pmx.getMessage(), pmx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
             catch (ApplicationManagementException amx)
             {
                 ERROR_RECORDER.error(amx.getMessage(), amx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/search", method = RequestMethod.POST)
     public final ModelAndView submitApplicationSearch(@ModelAttribute("request") final SearchRequest request, final BindingResult bindResult)
     {
         final String methodName = ApplicationManagementController.CNAME + "#submitApplicationSearch(@ModelAttribute(\"request\") final SearchRequest request, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("SearchRequest: {}", request);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final ISearchProcessor searchProcessor = new SearchProcessorImpl();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             this.searchValidator.validate(request, bindResult);
 
             if (bindResult.hasErrors())
             {
                 // validation failed
                 ERROR_RECORDER.error("Errors: {}", bindResult.getAllErrors());
 
                 mView.addObject(Constants.ERROR_MESSAGE, this.appConfig.getMessageValidationFailed());
                 mView.addObject("command", new SearchRequest());
                 mView.setViewName(this.defaultPage);
 
                 return mView;
             }
 
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 SearchResponse searchRes = searchProcessor.doApplicationSearch(request);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("SearchResponse: {}", searchRes);
                 }
 
                 if (searchRes.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     mView.addObject(Constants.SEARCH_RESULTS, searchRes.getResults());
                     mView.addObject("command", new SearchRequest());
                     mView.setViewName(this.defaultPage);
                 }
                 else if (searchRes.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_RESPONSE, searchRes.getResponse());
                     mView.addObject("command", new SearchRequest());
                     mView.setViewName(this.defaultPage);
                 }
             }
             catch (SearchRequestException srx)
             {
                 ERROR_RECORDER.error(srx.getMessage(), srx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/add-application", method = RequestMethod.POST)
     public final ModelAndView doAddApplication(@ModelAttribute("request") final ApplicationRequest request, final BindingResult bindResult)
     {
         final String methodName = ApplicationManagementController.CNAME + "#doAddApplication(@ModelAttribute(\"request\") final ApplicationRequest request, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", request);
             DEBUGGER.debug("Value: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IApplicationManagementProcessor processor = new ApplicationManagementProcessorImpl();
         final IPlatformManagementProcessor platformMgr = new PlatformManagementProcessorImpl();
         final IProjectManagementProcessor projectMgr = new ProjectManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             this.applicationValidator.validate(request, bindResult);
 
             if (bindResult.hasErrors())
             {
                 // validation failed
                 ERROR_RECORDER.error("Errors: {}", bindResult.getAllErrors());
 
                 mView.addObject(Constants.ERROR_MESSAGE, this.appConfig.getMessageValidationFailed());
                 mView.addObject("command", new ApplicationRequest());
                 mView.setViewName(this.addAppPage);
 
                 return mView;
             }
 
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Platform newPlatform = new Platform();
                 newPlatform.setPlatformGuid(request.getPlatform());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Platform: {}", newPlatform);
                 }
 
                 Project newProject = new Project();
                 newProject.setProjectGuid(request.getProject());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Project: {}", newProject);
                 }
 
                 Application newApp = new Application();
                 newApp.setApplicationGuid(UUID.randomUUID().toString());
                 newApp.setApplicationName(request.getApplicationName());
                 newApp.setApplicationPlatforms(new ArrayList<>(Arrays.asList(newPlatform)));
                 newApp.setApplicationVersion(request.getVersion());
                 newApp.setApplicationCluster(request.getClusterName());
                 newApp.setApplicationLogsPath(request.getLogsPath());
                 newApp.setApplicationProject(newProject);
                 newApp.setApplicationInstallPath(request.getInstallPath());
                 newApp.setPidDirectory(request.getPidDirectory());
                 newApp.setScmPath(request.getScmPath());
                 newApp.setJvmName(request.getJvmName());
                 newApp.setBasePath(request.getBasePath());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Application: {}", newApp);
                 }
 
                 ApplicationManagementRequest appRequest = new ApplicationManagementRequest();
                 appRequest.setApplication(newApp);
                 appRequest.setServiceId(this.applMgmt);
                 appRequest.setRequestInfo(reqInfo);
                 appRequest.setUserAccount(userAccount);
                 appRequest.setApplicationId(this.appConfig.getApplicationId());
                 appRequest.setApplicationName(this.appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementRequest: {}", request);
                 }
 
                 ApplicationManagementResponse response = processor.addNewApplication(appRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementResponse: {}", response);
                 }
 
                 if (response.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     // app added
                     ProjectManagementRequest projectReq = new ProjectManagementRequest();
                     projectReq.setRequestInfo(reqInfo);
                     projectReq.setUserAccount(userAccount);
                     projectReq.setServiceId(this.projectMgmt);
                     projectReq.setApplicationId(this.appConfig.getApplicationId());
                     projectReq.setApplicationName(this.appConfig.getApplicationName());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ProjectManagementRequest: {}", projectReq);
                     }
 
                     ProjectManagementResponse projectResponse = projectMgr.listProjects(projectReq);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ProjectManagementResponse: {}", projectResponse);
                     }
 
                     if (projectResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                     {
                         List<Project> projects = projectResponse.getProjectList();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("projects: {}", projects);
                         }
 
                         if ((projects != null) && (projects.size() != 0))
                         {
                             Map<String, String> projectListing = new HashMap<>();
 
                             for (Project project : projects)
                             {
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("Project: {}", project);
                                 }
 
                                 projectListing.put(project.getProjectGuid(), project.getProjectCode());
                             }
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("projectListing: {}", projectListing);
                             }
 
                             mView.addObject("projectListing", projectListing);
                         }
                     }
                     else if (projectResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                     {
                         mView.setViewName(this.appConfig.getUnauthorizedPage());
 
                         return mView;
                     }
 
                     PlatformManagementRequest platformReq = new PlatformManagementRequest();
                     platformReq.setRequestInfo(reqInfo);
                     platformReq.setServiceId(this.platformMgmt);
                     platformReq.setUserAccount(userAccount);
                     platformReq.setApplicationId(this.appConfig.getApplicationId());
                     platformReq.setApplicationName(this.appConfig.getApplicationName());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("PlatformManagementRequest: {}", platformReq);
                     }
 
                     PlatformManagementResponse platformResponse = platformMgr.listPlatforms(platformReq);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("PlatformManagementResponse: {}", platformResponse);
                     }
 
                     if (platformResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                     {
                         List<Platform> platformList = platformResponse.getPlatformList();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("platformList: {}", platformList);
                         }
 
                         if ((platformList != null) && (platformList.size() != 0))
                         {
                             Map<String, String> platformListing = new HashMap<>();
 
                             for (Platform platform : platformList)
                             {
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("Platform: {}", platform);
                                 }
 
                                 platformListing.put(platform.getPlatformGuid(), platform.getPlatformName());
                             }
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("platformListing: {}", platformListing);
                             }
 
                             mView.addObject("platformListing", platformListing);
                         }
                     }
                     else if (platformResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                     {
                         mView.setViewName(this.appConfig.getUnauthorizedPage());
 
                         return mView;
                     }
 
                     mView.addObject(Constants.RESPONSE_MESSAGE, this.messageApplicationAdded);
                     mView.addObject("command", new ApplicationRequest());
                     mView.setViewName(this.addAppPage);
                 }
                 else if (response.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView.addObject("command", request);
                     mView.addObject(Constants.ERROR_RESPONSE, response.getResponse());
                     mView.setViewName(this.addAppPage);
                 }
             }
             catch (ApplicationManagementException amx)
             {
                 ERROR_RECORDER.error(amx.getMessage(), amx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
             catch (ProjectManagementException pmx)
             {
                 ERROR_RECORDER.error(pmx.getMessage(), pmx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
             catch (PlatformManagementException pmx)
             {
                 ERROR_RECORDER.error(pmx.getMessage(), pmx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/deploy-application", method = RequestMethod.POST)
     public final ModelAndView doDeployApplication(@ModelAttribute("request") final ApplicationRequest request, final BindingResult bindResult)
     {
         final String methodName = ApplicationManagementController.CNAME + "#doDeployApplication(@ModelAttribute(\"request\") final ApplicationRequest request, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", request);
             DEBUGGER.debug("Value: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IApplicationManagementProcessor appMgr = new ApplicationManagementProcessorImpl();
         final IPlatformManagementProcessor platformMgr = new PlatformManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (this.appConfig.getServices().get(this.serviceName))
         {
             this.deploymentValidator.validate(request, bindResult);
 
             if (bindResult.hasErrors())
             {
                 // validation failed
                 mView = new ModelAndView(new RedirectView());
                 mView.setViewName(this.viewAppPage + "/application/" + request.getApplicationGuid());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ModelAndView: {}", mView);
                 }
 
                 return mView;
             }
 
             // validate the user has access
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
                 reqInfo.setSessionId(hSession.getId());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Application reqApplication = new Application();
                 reqApplication.setApplicationGuid(request.getApplicationGuid());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Application: {}", reqApplication);
                 }
 
                 // get a list of available servers
                 ApplicationManagementRequest appReq = new ApplicationManagementRequest();
                 appReq.setRequestInfo(reqInfo);
                 appReq.setUserAccount(userAccount);
                 appReq.setServiceId(this.applMgmt);
                 appReq.setApplication(reqApplication);
                 appReq.setApplicationId(this.appConfig.getApplicationId());
                 appReq.setApplicationName(this.appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementRequest: {}", appReq);
                 }
 
                 ApplicationManagementResponse appRes = appMgr.getApplicationData(appReq);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ApplicationManagementResponse: {}", appRes);
                 }
 
                 if (appRes.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     Application resApplication = appRes.getApplication();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Application: {}", resApplication);
                     }
 
                     // have the application, get the platform
                     Platform reqPlatform = new Platform();
                     reqPlatform.setPlatformGuid(request.getPlatform());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Platform: {}", reqPlatform);
                     }
 
                     PlatformManagementRequest platformRequest = new PlatformManagementRequest();
                     platformRequest.setUserAccount(userAccount);
                     platformRequest.setRequestInfo(reqInfo);
                     platformRequest.setServiceId(this.platformMgmt);
                     platformRequest.setPlatform(reqPlatform);
                     platformRequest.setApplicationId(this.appConfig.getApplicationId());
                     platformRequest.setApplicationName(this.appConfig.getApplicationName());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("PlatformManagementRequest: {}", platformRequest);
                     }
 
                     PlatformManagementResponse platformResponse = platformMgr.getPlatformData(platformRequest);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("PlatformManagementResponse: {}", platformResponse);
                     }
 
                     if (platformResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                     {
                         Platform resPlatform = platformResponse.getPlatformData();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("Platform: {}", resPlatform);
                         }
 
                         if (resPlatform != null)
                         {
                             // excellent
                             if (StringUtils.isNotEmpty(resApplication.getScmPath()))
                             {
                                 // this is an scm build. make sure the version number was populated
                                 if ((StringUtils.isNotEmpty(request.getVersion())) && (!(StringUtils.equals(resApplication.getApplicationVersion(), request.getVersion()))) && (!(StringUtils.equals(request.getVersion(), "0.0"))))
                                 {
                                     // good. at this point we're going to download the files and then
                                     // well, do something.. not really sure how this is gonna work out
                                     // quite yet.
                                     mView.addObject(Constants.RESPONSE_MESSAGE, this.messageFileUploaded);
                                     mView.addObject("application", resApplication);
                                     mView.setViewName(this.viewAppPage);
                                 }
                                 else
                                 {
                                     // whoops.
                                     mView.addObject(Constants.ERROR_MESSAGE, this.messageNoAppVersionProvided);
                                     mView.addObject("command", new ApplicationRequest());
                                     mView.addObject("application", resApplication);
                                     mView.addObject("platform", resPlatform);
                                     mView.setViewName(this.deployAppPage);
                                 }
                             }
                             else
                             {
                                 // we should have a binary file attached to the request
                                 if (request.getApplicationBinary() != null)
                                 {
                                     // excellent
                                     MultipartFile binary = request.getApplicationBinary();
                                     File repository = FileUtils.getFile(this.appConfig.getUploadDirectory());
 
                                     if (DEBUG)
                                     {
                                         DEBUGGER.debug("MultipartFile: {}", binary);
                                     }
 
                                     if (binary != null)
                                     {
                                         FileUtils.writeByteArrayToFile(FileUtils.getFile(repository, binary.getOriginalFilename()), binary.getBytes());
 
                                         // all set here
                                         mView.addObject(Constants.RESPONSE_MESSAGE, this.messageFileUploaded);
                                         mView.addObject("application", resApplication);
                                         mView.setViewName(this.viewAppPage);
                                     }
                                     else
                                     {
                                         // no files
                                         // add the server listing to the page
                                         mView.addObject(Constants.ERROR_MESSAGE, this.messageNoBinaryProvided);
                                         mView.addObject("command", new ApplicationRequest());
                                         mView.addObject("application", resApplication);
                                         mView.addObject("platform", resPlatform);
                                         mView.setViewName(this.deployAppPage);
                                     }
                                 }
                                 else
                                 {
                                     mView.addObject(Constants.ERROR_MESSAGE, this.messageNoBinaryProvided);
                                     mView.addObject("command", new ApplicationRequest());
                                     mView.addObject("application", resApplication);
                                     mView.addObject("platform", resPlatform);
                                     mView.setViewName(this.deployAppPage);
                                 }
                             }
                         }
                         else
                         {
                             // no platform
                             mView.addObject(Constants.ERROR_MESSAGE, this.messageNoPlatformAssigned);
                             mView.setViewName(this.appConfig.getErrorResponsePage());
                         }
                     }
                     else if (platformResponse.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                     {
                         mView.setViewName(this.appConfig.getUnauthorizedPage());
                     }
                     else
                     {
                         // error
                         mView.addObject(Constants.ERROR_RESPONSE, platformResponse.getResponse());
                         mView.setViewName(this.appConfig.getErrorResponsePage());
                     }
                 }
                 else if (appRes.getRequestStatus() == CoreServicesStatus.UNAUTHORIZED)
                 {
                     mView.setViewName(this.appConfig.getUnauthorizedPage());
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_RESPONSE, appRes.getResponse());
                     mView.setViewName(this.defaultPage);
                 }
             }
             catch (ApplicationManagementException amx)
             {
                 ERROR_RECORDER.error(amx.getMessage(), amx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
             catch (PlatformManagementException pmx)
             {
                 ERROR_RECORDER.error(pmx.getMessage(), pmx);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
             catch (IOException iox)
             {
                 ERROR_RECORDER.error(iox.getMessage(), iox);
 
                 mView.setViewName(this.appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(this.appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 }
