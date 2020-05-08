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
 package org.openmrs.module.formfilter.web.controller;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Cohort;
 import org.openmrs.Privilege;
 import org.openmrs.Role;
import org.openmrs.api.UserService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.formfilter.FormFilterProperty;
 import org.openmrs.module.formfilter.FormFilterUtil;
 import org.openmrs.module.formfilter.api.FormFilterService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.context.request.WebRequest;
 
 /**
  * Controller handling requests to add or edit filter.
  */
 @Controller
 public class AddFormFilterPropertyController {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	/**
 	 * Adds property to web page to include Jquery or not.
 	 * 
 	 * @see FormFilterUtil#includeJquery()	 
 	 */
 	@ModelAttribute("includeJquery")
 	public boolean useJquery(WebRequest request){
 		return FormFilterUtil.includeJquery();
 	}
 	
 	/**
 	 * Returns all roles.
 	 * 
 	 * @param request
 	 * @return Roles, list of all roles
 	 */
 	@ModelAttribute("allRoles")
 	public List<Role> getRoles(WebRequest request) {
 		List<Role> roles = Context.getUserService().getAllRoles();
 		if (roles == null)
 			roles = new Vector<Role>();
 		
 		return roles;
 	}
 	
 	/**
 	 * Returns all privileges.
 	 * 
 	 * @param request
 	 * @return Privileges, list of all privileges
 	 */
 	@ModelAttribute("allPrivileges")
 	public List<Privilege> getPrivileges(WebRequest request) {
 		List<Privilege> privileges = Context.getUserService().getAllPrivileges();
 		if (privileges == null)
 			privileges = new Vector<Privilege>();
 		
 		return privileges;
 	}
 	
 	/**
 	 * Returns all cohorts.
 	 * 
 	 * @param request
 	 * @return cohorts, list of all cohorts
 	 */
 	@ModelAttribute("allCohorts")
 	public List<Cohort> getCohorts(WebRequest request) {
 		List<Cohort> cohorts = Context.getCohortService().getAllCohorts();
 		if (cohorts == null)
 			cohorts = new Vector<Cohort>();
 		
 		return cohorts;
 	}
 	
 	/**
 	 * Handles request related to adding Form filter.
 	 * 
 	 * @param model
 	 * @param formFilterId
 	 * @param formFilterPropertyId , if null adds new property or else returns respective property.
 	 * @should support add new filter functionality
 	 * @should support edit filter functionality
 	 */
 	@RequestMapping(value = "/module/formfilter/addformproperty", method = RequestMethod.GET)
 	public void addFormFilter(ModelMap model, @RequestParam("filterId") Integer formFilterId,
 	                          @RequestParam(value = "filterPropertyId", required = false) String formFilterPropertyId) {
 		
 		FormFilterService formFilterService = (FormFilterService) Context.getService(FormFilterService.class);
		UserService userService=Context.getUserService();
 		
 		FormFilterProperty formFilterProperty;
 		Map<String, String> map = new HashMap<String, String>();
 		
 		model.addAttribute("formFilter", formFilterService.getFormFilter(formFilterId));
 		
 		/*If formFilterPropertyId is not equal to null , then we will add formFilterProperty to show 
 		  page as edit filter option or else pass new object to add a new filter.*/
 
 		if (formFilterPropertyId != null) {
 			formFilterProperty = formFilterService.getFormFilterProperty(Integer.parseInt(formFilterPropertyId));
 			
 			for (String string : formFilterProperty.getProperties().split("&")) {
 				String str[] = string.split("=");
 				
 				if (formFilterProperty.getClassName().equalsIgnoreCase("org.openmrs.module.formfilter.impl.RoleFormFilter")) {
 					
 					List<Role> filterRoles = new Vector<Role>();
 					for (String roleString : str[1].split(",")) {						
						filterRoles.add(userService.getRole(roleString));
 					}
 					model.addAttribute("filterRoles", filterRoles);
 					map.put(str[0], str[0]);
 				} else if (formFilterProperty.getClassName().equalsIgnoreCase("org.openmrs.module.formfilter.impl.PrivilegeFormFilter")) {
 					
 					List<Privilege> filterPrivileges = new Vector<Privilege>();
 					for (String privilegeString : str[1].split(",")) {						
						filterPrivileges.add(userService.getPrivilege(privilegeString));
 					}
 					model.addAttribute("filterPrivileges", filterPrivileges);
 					map.put(str[0], str[0]);
 				} else {
 					map.put(str[0], str[1]);
 				}
 			}
 			
 		} else {
 			formFilterProperty = new FormFilterProperty();
 		}
 		
 		model.addAttribute("properties", map);
 		model.addAttribute("formFilterProperty", formFilterProperty);
 		
 	}
 	
 	/**
 	 * Handles request to save or update a filter to db.
 	 * 
 	 * @param formFilterProperty
 	 * @param request
 	 * @return to viewformfilter page to see list of all assigned filter's to a form.
 	 * @should add new FormFilterProperty
 	 * @should update FormFilterProperty
 	 */
 	@RequestMapping(value = "/module/formfilter/addformproperty", method = RequestMethod.POST)
 	public String onSubmit(@ModelAttribute("formfilterproperty") FormFilterProperty formFilterProperty,
 	                       BindingResult result, SessionStatus status, HttpServletRequest request) {
 		
 		String propertyType = request.getParameter("propertyType");
 		int formFilterId = Integer.parseInt(request.getParameter("formFilterId"));
 		FormFilterService formFilterService = (FormFilterService) Context.getService(FormFilterService.class);
 		
 		//Based on selected propertyType option in form , className and properties of formFilterPropertyType
 		//object are set manually.
 		if (propertyType.equalsIgnoreCase("AgeProperty")) {
 			formFilterProperty.setClassName("org.openmrs.module.formfilter.impl.AgeFormFilter");
 			formFilterProperty.setProperties("minimumAge=" + request.getParameter("minimumAge") + "&maximumAge="
 			        + request.getParameter("maximumAge"));
 		} else if (propertyType.equalsIgnoreCase("GenderProperty")) {
 			formFilterProperty.setClassName("org.openmrs.module.formfilter.impl.GenderFormFilter");
 			formFilterProperty.setProperties("gender=" + request.getParameter("gender"));
 		} else if (propertyType.equalsIgnoreCase("DateProperty")) {
 			formFilterProperty.setClassName("org.openmrs.module.formfilter.impl.DateFormFilter");
 			formFilterProperty.setProperties("date=" + request.getParameter("date") + "&show="
 			        + request.getParameter("show"));
 		} else if (propertyType.equalsIgnoreCase("RoleProperty")) {
 			formFilterProperty.setClassName("org.openmrs.module.formfilter.impl.RoleFormFilter");
 			String roleArr[] = request.getParameterValues("roles");
 			String properties = "";
 			for (String string : roleArr) {
 				properties += string + ",";
 			}
 			formFilterProperty.setProperties("roles=" + properties);
 		} else if (propertyType.equalsIgnoreCase("PrivilegeProperty")) {
 			formFilterProperty.setClassName("org.openmrs.module.formfilter.impl.PrivilegeFormFilter");
 			String privilegeArr[] = request.getParameterValues("privileges");
 			String properties = "";
 			for (String string : privilegeArr) {
 				properties += string + ",";
 			}
 			formFilterProperty.setProperties("privileges=" + properties);
 		} else if (propertyType.equalsIgnoreCase("CohortProperty")) {
 			formFilterProperty.setClassName("org.openmrs.module.formfilter.impl.CohortFormFilter");
 			formFilterProperty.setProperties("cohort=" + request.getParameter("cohort"));
 		}
 		
 		//if id of formFilterProperty object is 0 , add filter as new or else update it.
 		if (formFilterProperty.getFormFilterPropertyId() == 0) {
 			formFilterService.addFormFilterProperty(formFilterId, formFilterProperty);
 		} else {
 			formFilterService.saveFormFilterProperty(formFilterProperty);
 		}
 		
 		return "redirect:viewformfilter.form?formFilterId=" + formFilterId;
 		
 	}
 	
 }
