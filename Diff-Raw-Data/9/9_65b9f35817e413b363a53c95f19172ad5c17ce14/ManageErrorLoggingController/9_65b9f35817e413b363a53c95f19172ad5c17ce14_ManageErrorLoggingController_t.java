 /**
  * The contents of this file are subject to the OpenMRS Public License Version
  * 1.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.
  *
  * Copyright (C) OpenMRS, LLC. All Rights Reserved.
  */
 package org.openmrs.module.errorlogging.web.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.GlobalProperty;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.errorlogging.ErrorLoggingConstants;
 import org.openmrs.web.WebConstants;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * The main controller.
  */
 @Controller
 public class ManageErrorLoggingController {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	/**
 	 * Displays the form to manage the errorlogging module
 	 * 
 	 * @param model 
 	 */
 	@RequestMapping(value = "/module/errorlogging/manage.form", method = RequestMethod.GET)
 	public void showForm(ModelMap model) {
 		String ignoredExceptions = getIgnoredExceptions();
 		if (ignoredExceptions != null) {
 			model.addAttribute("ignoredExceptions", ignoredExceptions);
 		} else {
 			model.addAttribute("ignoredExceptions", "");
 		}
 	}
 	
 	@RequestMapping(value = "module/errorlogging/manage.form", method = RequestMethod.POST)
 	public ModelAndView processSubmit(@RequestParam(value = "exceptions", required = true) String ignoredExceprions,
 	                                  ModelMap model, HttpServletRequest request) {
 		boolean successSave = saveIgnoredExceprions(ignoredExceprions);
 		HttpSession httpSession = request.getSession();
 		if (successSave) {
 			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "errorlogging.ignredExceptions.successSaveMessage");
 		} else {
 			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "errorlogging.ignredExceptions.errorSaveMessage");
 		}
 		return new ModelAndView("redirect:./manage.form");
 	}
 	
 	/**
 	 * Save errors that have to be ignored
 	 *
 	 * @param errors that have to be ignored
 	 * @return true in the case of a successful saving, otherwise - false
 	 */
 	private boolean saveIgnoredExceprions(String errors) {
		if (errors == null) {
			return false;
		}
 		GlobalProperty glProp = Context.getAdministrationService().getGlobalPropertyObject(
 		    ErrorLoggingConstants.ERRROR_LOGGING_GP_IGNORED_EXCEPTION);
 		if (glProp != null) {
 			glProp.setPropertyValue(errors);
 			GlobalProperty saved = Context.getAdministrationService().saveGlobalProperty(glProp);
			if (saved != null) {
 				return true;
 			}
		}		
 		return false;
 	}
 	
 	/**
 	 * Get ignored exceptions
 	 *
 	 * @return string which include ignored exceptions
 	 */
 	private String getIgnoredExceptions() {
 		GlobalProperty glProp = Context.getAdministrationService().getGlobalPropertyObject(
 		    ErrorLoggingConstants.ERRROR_LOGGING_GP_IGNORED_EXCEPTION);
 		if (glProp != null) {
 			return glProp.getPropertyValue();
 		}
 		return null;
 	}
 }
