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
 
 package org.openmrs.module.iqchartimport.web.controller;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.module.iqchartimport.Utils;
 import org.openmrs.module.iqchartimport.task.TaskEngine;
 import org.openmrs.module.iqchartimport.task.ImportTask;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Import status AJAX controller
  */
 @Controller("iqChartImportStatusController")
 @RequestMapping("/module/iqchartimport/status")
 public class StatusController {
 
 	protected static final Log log = LogFactory.getLog(StatusController.class);
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public void getProgress(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		Utils.checkSuperUser();
 		
 		ImportTask task = TaskEngine.getCurrentTask();
 		String json = null;
 		
 		if (task != null) {
 			String completed = task.isCompleted() ? "true" : "false";
			String exception = (task.getException() != null) ? ("'" + task.getException().getClass().getName() + "'") : "null";
			String exceptionMessage = (task.getException() != null && task.getException().getMessage() != null) ? ("'" + task.getException().getMessage() + "'") : "null";
			
 			json = 
 				"{ " +
 				"  task: { " +
 				"    completed: " + completed + ", " +
 				"    exception: " + exception + ", " +
				"    exceptionMessage: " + exceptionMessage + ", " +
 				"    progress: " + task.getProgress() + ", " +
 				"    importedPatients: " + task.getPatientsImported() + ", " +
 				"    importedEncounters: " + task.getEncountersImported() +
 				"  } " +
 				"}";
 		}
 		else
 			json = "{ task: null }";
 		
 		response.setContentType("application/json");			
 		response.getWriter().write(json);
 	}
 }
