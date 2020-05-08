 package org.motechproject.ghana.national.web;
 
 import org.motechproject.ghana.national.domain.RegistrationType;
 import org.motechproject.ghana.national.exception.ParentNotFoundException;
 import org.motechproject.ghana.national.exception.PatientIdIncorrectFormatException;
 import org.motechproject.ghana.national.exception.PatientIdNotUniqueException;
 import org.motechproject.ghana.national.service.FacilityService;
 import org.motechproject.ghana.national.service.IdentifierGenerationService;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.ghana.national.web.form.CreatePatientForm;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 @Controller
 @RequestMapping(value = "/admin/patients")
 public class PatientController {
     public static final String NEW_PATIENT = "patients/new";
     public static final String CREATE_PATIENT_FORM = "createPatientForm";
     public static final String SUCCESS = "patients/success";
 
     private FacilityService facilityService;
     private PatientService patientService;
     private MessageSource messageSource;
     IdentifierGenerationService identifierGenerationService;
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
         dateFormat.setLenient(false);
         binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
     }
 
     public PatientController() {
     }
 
     @Autowired
     public PatientController(FacilityService facilityService, PatientService patientService, IdentifierGenerationService identifierGenerationService, MessageSource messageSource) {
         this.facilityService = facilityService;
         this.patientService = patientService;
         this.messageSource = messageSource;
         this.identifierGenerationService = identifierGenerationService;
     }
 
     @ApiSession
     @RequestMapping(value = "new", method = RequestMethod.GET)
     public String newPatientForm(ModelMap modelMap) {
         modelMap.put(CREATE_PATIENT_FORM, new CreatePatientForm());
         modelMap.mergeAttributes(facilityService.locationMap());
         return NEW_PATIENT;
     }
 
     @ApiSession
     @RequestMapping(value = "create", method = RequestMethod.POST)
     public String createPatient(CreatePatientForm createPatientForm, BindingResult result, ModelMap modelMap) {
         try {
             String patientID = "";
            if (createPatientForm.getRegistrationMode().equals(RegistrationType.AUTO_GENERATE_ID)) {
                 patientID = identifierGenerationService.newPatientId();
             }
             patientService.registerPatient(createPatientForm.getPatient(patientID), createPatientForm.getTypeOfPatient(), createPatientForm.getParentId());
         } catch (ParentNotFoundException e) {
             handleError(result, modelMap, messageSource.getMessage("patient_parent_not_found", null, Locale.getDefault()));
             return NEW_PATIENT;
         } catch (PatientIdNotUniqueException e) {
             handleError(result, modelMap, messageSource.getMessage("patient_id_duplicate", null, Locale.getDefault()));
             return NEW_PATIENT;
         } catch (PatientIdIncorrectFormatException e) {
             handleError(result, modelMap, messageSource.getMessage("patient_id_incorrect", null, Locale.getDefault()));
             return NEW_PATIENT;
         }
         return SUCCESS;
     }
 
     private void handleError(BindingResult bindingResult, ModelMap modelMap, String message) {
         modelMap.mergeAttributes(facilityService.locationMap());
         bindingResult.addError(new FieldError(CREATE_PATIENT_FORM, "parentId", message));
         modelMap.mergeAttributes(bindingResult.getModel());
     }
 }
