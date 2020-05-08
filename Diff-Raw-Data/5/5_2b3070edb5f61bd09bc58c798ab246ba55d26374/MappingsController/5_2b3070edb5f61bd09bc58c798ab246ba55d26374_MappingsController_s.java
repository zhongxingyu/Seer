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
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Location;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.Program;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.iqchartimport.Constants;
 import org.openmrs.module.iqchartimport.MappingUtils;
 import org.openmrs.module.iqchartimport.Mappings;
 import org.openmrs.module.iqchartimport.Utils;
 import org.openmrs.web.WebConstants;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 /**
  * Controller for mappings page
  */
 @Controller("iqChartImportMappingsController")
 @RequestMapping("/module/iqchartimport/mappings")
 @SessionAttributes({"mappings"})
 public class MappingsController {
 
 	protected static final Log log = LogFactory.getLog(MappingsController.class);
 	
 	@SuppressWarnings("unchecked")
 	@RequestMapping(method = RequestMethod.GET)
 	public String showPage(HttpServletRequest request, ModelMap model) throws IOException {
 		Utils.checkSuperUser();
 		
 		List<PatientIdentifierType> identifierTypes = Context.getPatientService().getAllPatientIdentifierTypes();
 		List<Program> programs = Context.getProgramWorkflowService().getAllPrograms();
 		List<Location> locations = Context.getLocationService().getAllLocations();
 		
 		String provList = Context.getAdministrationService().getGlobalProperty(Constants.PROP_ADDRESS_ALL_PROVINCES);
 		String[] allProvinces = provList != null ? provList.split(",") : new String[]{};
 		
 		model.put("identifierTypes", identifierTypes);
 		model.put("allProvinces", allProvinces);
 		model.put("programs", programs);
 		model.put("locations", locations);
 		model.put("mappings", Mappings.getInstance());
 		model.put("encounterProvider", MappingUtils.getEncounterProvider());
 		
 		return "/module/iqchartimport/mappings";
 	}
 	
 	@RequestMapping(method = RequestMethod.POST)
	public String handleSubmit(HttpServletRequest request, @ModelAttribute("mappings") Mappings mappings, @RequestParam Boolean createProvider) {
 		Utils.checkSuperUser();
 		
		if (createProvider) {
 			MappingUtils.createEncounterProvider();
 			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Provider created");
 		}
 		else {
 			mappings.save();
 			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Mappings saved");
 		}
 		
 		return "redirect:mappings.form";
 	}
 }
