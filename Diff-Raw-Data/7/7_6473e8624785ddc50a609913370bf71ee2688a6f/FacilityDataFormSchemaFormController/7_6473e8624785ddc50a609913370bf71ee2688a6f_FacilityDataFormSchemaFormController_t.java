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
 package org.openmrs.module.facilitydata.web.controller;
 
 import java.util.Date;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.facilitydata.model.FacilityDataFormSchema;
 import org.openmrs.module.facilitydata.model.enums.Frequency;
 import org.openmrs.module.facilitydata.propertyeditor.FacilityDataFormSchemaEditor;
 import org.openmrs.module.facilitydata.service.FacilityDataService;
 import org.openmrs.module.facilitydata.validator.FacilityDataFormSchemaValidator;
 import org.openmrs.web.WebConstants;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.ServletRequestBindingException;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 
 @Controller
 @RequestMapping("/module/facilitydata/schemaForm.form")
 public class FacilityDataFormSchemaFormController {
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         binder.registerCustomEditor(FacilityDataFormSchema.class, new FacilityDataFormSchemaEditor());
         binder.registerCustomEditor(Date.class, new CustomDateEditor(Context.getDateFormat(), true));
     }
    
    @ModelAttribute("frequencies")
    public Frequency[] getFrequencies() {
    	return Frequency.values();
    }
 
     @RequestMapping(method = RequestMethod.GET)
     public String viewForm(@RequestParam(required = false) Integer id, ModelMap map,
                            @ModelAttribute("schema") FacilityDataFormSchema schema) {
         FacilityDataService svc = Context.getService(FacilityDataService.class);
         if (id != null) {
             schema = svc.getFacilityDataFormSchema(id);
             map.addAttribute("schema", schema);
         }
         return "/module/facilitydata/schemaForm";
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public String saveSchema(@RequestParam(required = false) Integer id,
                              @ModelAttribute("schema") FacilityDataFormSchema schema, BindingResult result,
                              HttpServletRequest request, ModelMap map) throws ServletRequestBindingException {
         FacilityDataService svc = Context.getService(FacilityDataService.class);
         new FacilityDataFormSchemaValidator().validate(schema, result);
         if (result.hasErrors()) {
             return "/module/facilitydata/schemaForm";
         }
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "facilitydata.schema.saved");
         return String.format("redirect:schema.form?id=%s", svc.saveFacilityDataFormSchema(schema).getId());
     }
 }
