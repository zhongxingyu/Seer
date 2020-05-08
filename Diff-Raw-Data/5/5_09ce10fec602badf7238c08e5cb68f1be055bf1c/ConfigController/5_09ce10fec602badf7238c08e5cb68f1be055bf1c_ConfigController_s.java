 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.logmanager.web.controller;
 
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.helpers.LogLog;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.openmrs.module.Module;
 import org.openmrs.module.ModuleFactory;
 import org.openmrs.module.logmanager.Constants;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.ParameterizableViewController;
 
 /**
  * Controller for reset page
  */
 public class ConfigController extends ParameterizableViewController {
 	
 	/* (non-Javadoc)
 	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected ModelAndView handleRequestInternal(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		
 		// Reset log4j configuration
 		if (request.getParameter("clear") != null)		
 			clearConfiguration();
 		else if (request.getParameter("reload") != null)		
 			reloadConfiguration();
 		else if (request.getParameter("startSQL") != null)		
 			setHibernateSQLLogging(true);
 		else if (request.getParameter("stopSQL") != null)		
 			setHibernateSQLLogging(false);
 		
 		Logger sqlLogger = LogManager.exists(Constants.LOGGER_HIBERNATE_SQL);
		Level sqlLoggerLevel = (sqlLogger != null) ? sqlLogger.getLevel() : null;
 		
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("sqlLoggerName", Constants.LOGGER_HIBERNATE_SQL);
		model.put("sqlLoggerStarted", sqlLoggerLevel.toInt() <= Level.DEBUG.toInt());
 		
 		return new ModelAndView(getViewName(), model);
 	}
 	
 	/**
 	 * Clears the log4j configuration
 	 */
 	private void clearConfiguration() {
 		BasicConfigurator.resetConfiguration();
 	}
 	
 	/**
 	 * Reloads the log4j configuration
 	 */
 	private void reloadConfiguration() {
 		try {				
 			// Load main OpenMRS log4j.xml
 			URL url = ConfigController.class.getResource("/log4j.xml");
 			DOMConfigurator.configure(url);
 			
 			// Load log4j config files from each module if they exist
 			Collection<Module> modules = ModuleFactory.getStartedModules();
 			for (Module module : modules) {
 				if (module.getLog4j() != null)
 					DOMConfigurator.configure(module.getLog4j().getDocumentElement());
 			}
 			
 		} catch (Exception e) {
 		  LogLog.error(e.getMessage());
 		}
 	}
 	
 	private void setHibernateSQLLogging(boolean on) {
 		LogManager.getLogger("org.hibernate.SQL").setLevel(on ? Level.DEBUG : Level.OFF);
 	}
 }
