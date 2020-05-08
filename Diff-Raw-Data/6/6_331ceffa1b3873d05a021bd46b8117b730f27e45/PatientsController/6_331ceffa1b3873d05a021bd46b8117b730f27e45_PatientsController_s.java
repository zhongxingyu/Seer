 package org.motechproject.ghana.national.web;
 
 import org.motechproject.ghana.national.service.FacilityService;
 import org.motechproject.ghana.national.web.form.CreatePatientForm;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping(value = "/admin/patients")
 public class PatientsController {
     public static final String NEW_PATIENT = "patients/new";
     public static final String CREATE_PATIENT_FORM = "createPatientForm";
 
     private FacilityService facilityService;
 
     public PatientsController() {
     }
 
     @Autowired
     public PatientsController(FacilityService facilityService) {
         this.facilityService = facilityService;
     }
 
     @ApiSession
     @RequestMapping(value = "new", method = RequestMethod.GET)
     public String newPatientForm(ModelMap modelMap) {
         modelMap.put(CREATE_PATIENT_FORM, new CreatePatientForm());
         modelMap.mergeAttributes(facilityService.locationMap());
         return NEW_PATIENT;
     }
 
     //TODO: Check if the patient type is mother or child and save accordingly.
     /*
    * if child, fetch mother using mother's motech id if available. & validate if it exists.
    * */
 }
