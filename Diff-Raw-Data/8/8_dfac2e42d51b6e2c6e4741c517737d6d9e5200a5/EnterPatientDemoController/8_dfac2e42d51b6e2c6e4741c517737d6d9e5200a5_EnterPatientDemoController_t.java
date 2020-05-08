 package org.openmrs.module.patientregistration.controller.workflow;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.PersonAddress;
 import org.openmrs.PersonAttribute;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.PersonName;
 import org.openmrs.api.APIException;
 import org.openmrs.api.PersonService.ATTR_VIEW_TYPE;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.emr.adt.AdtService;
 import org.openmrs.module.emr.paperrecord.UnableToPrintLabelException;
 import org.openmrs.module.emr.printer.UnableToPrintViaSocketException;
 import org.openmrs.module.patientregistration.Age;
 import org.openmrs.module.patientregistration.Birthdate;
 import org.openmrs.module.patientregistration.PatientRegistrationConstants;
 import org.openmrs.module.patientregistration.PatientRegistrationGlobalProperties;
 import org.openmrs.module.patientregistration.PatientRegistrationUtil;
 import org.openmrs.module.patientregistration.controller.AbstractPatientDetailsController;
 import org.openmrs.module.patientregistration.service.PatientRegistrationService;
 import org.openmrs.module.patientregistration.util.PatientRegistrationWebUtil;
 import org.openmrs.module.patientregistration.util.PrintErrorType;
 import org.openmrs.module.patientregistration.util.TaskProgress;
 import org.openmrs.module.patientregistration.util.UserActivityLogger;
 import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpSession;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.openmrs.module.patientregistration.PatientRegistrationUtil.getMedicalRecordLocationRecursivelyBasedOnTag;
 import static org.openmrs.module.patientregistration.util.PatientRegistrationWebUtil.getRegistrationLocation;
 import static org.openmrs.module.patientregistration.util.PrintErrorType.CARD_PRINTER_ERROR;
 import static org.openmrs.module.patientregistration.util.PrintErrorType.CARD_PRINTER_NOT_CONFIGURED;
 import static org.openmrs.module.patientregistration.util.PrintErrorType.LABEL_PRINTER_ERROR;
 import static org.openmrs.module.patientregistration.util.PrintErrorType.LABEL_PRINTER_NOT_CONFIGURED;
 
 @Controller
 @RequestMapping(value = "/module/patientregistration/workflow/enterPatientDemo.form")
 public class EnterPatientDemoController  extends AbstractPatientDetailsController{
 
     @ModelAttribute("patient")
     public Patient getPatient(HttpSession session
             , @RequestParam(value= "patientId", required = false) String patientId){
         Patient patient = (Patient)session.getAttribute(PatientRegistrationConstants.REGISTRATION_PATIENT);
         if(patient==null && StringUtils.isNotBlank(patientId)){
             try{
                 patient = Context.getPatientService().getPatient(new Integer(patientId));
             }catch(Exception e){
                log.info("patient not found", e);
             }
         }
         PersonName personName=null;
         // if a patient is associated with the session, put it in the model map
         if (patient== null) {
             patient = new Patient();
         }else{
             personName = patient.getPersonName();
         }
         if(personName==null){
             personName = new PersonName();
         }
         patient.addName(personName);
         patient.getPersonName().setPreferred(true);
 
         // if all the standard attributes haven't been configured, configure them, so that we have something to bind to
         for (PersonAttributeType attr : Context.getPersonService().getPersonAttributeTypes(PERSON_TYPE.PATIENT, ATTR_VIEW_TYPE.VIEWING)) {
             if (patient.getAttribute(attr) == null) {
                 patient.addAttribute(new PersonAttribute(attr, null));
             }
         }
 
         return patient;
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView showSelectPatient(
             @ModelAttribute("patient") Patient patient
             , @RequestParam(value= "editDivId", required = false) String editDivId
             ,@RequestParam(value= "hiddenPrintIdCard", required = false) String hiddenPrintIdCard
             ,@RequestParam(value= "nextTask", required = false) String nextTask
             ,@RequestParam(value= "subTask", required = false) String subTask
             ,@RequestParam(value= "patientIdentifier", required = false) String patientIdentifier
             , HttpSession session
             , ModelMap model) {
         // confirm that we have an active session
         if (!PatientRegistrationWebUtil.confirmActivePatientRegistrationSession(session)) {
             return new ModelAndView(PatientRegistrationConstants.WORKFLOW_FIRST_PAGE);
         }
 
         UserActivityLogger.startActivityGroup(session);
 
         if (patient.getPatientId() == null) {
             UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_REGISTRATION_CREATE_STARTED);
         }
         else {
             UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_REGISTRATION_EDIT_STARTED, "Patient: " + patient.getUuid());
         }
 
         if (patient!=null && (patient.getId()!=null)){
             patient = Context.getPatientService().getPatient(patient.getId());
         }
         Birthdate birthdate = new Birthdate();
         if(patient!=null){
             Date patientBirthdate= patient.getBirthdate();
             if(patientBirthdate!=null){
                 birthdate = new Birthdate(patientBirthdate);
             }
         }
         model.addAttribute("birthdate", birthdate);
         model.addAttribute("patientIdentifierMap", getPatientIdentifierMap(patient));
         if(StringUtils.isNotBlank(editDivId)){
             model.addAttribute("editDivId", editDivId);
         }
         if(StringUtils.isNotBlank(patientIdentifier)){
             model.addAttribute("patientIdentifier", patientIdentifier);
         }
         PatientIdentifier preferredIdentifier = PatientRegistrationUtil.getPreferredIdentifier(patient);
         if(preferredIdentifier!=null){
             model.addAttribute("patientPreferredIdentifier", preferredIdentifier.toString());
         }
         if(StringUtils.equals(hiddenPrintIdCard, "yes")){
             Location registrationLocation = PatientRegistrationWebUtil.getRegistrationLocation(session);
             EncounterType encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PATIENT_REGISTRATION_ENCOUNTER_TYPE();
             Encounter encounter = Context.getService(PatientRegistrationService.class).registerPatient(
                     patient
                     , Context.getAuthenticatedUser().getPerson()
                     , encounterType
                     , registrationLocation);
             try {
                 Context.getService(PatientRegistrationService.class).printIDCard(patient, new EmrContext(session).getSessionLocation());
                 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_ID_CARD_PRINTING_SUCCESSFUL);
                 PatientRegistrationWebUtil.updatePrintingCardStatus(patient, encounterType, encounter, registrationLocation, new Boolean(true), new Date());
             }
             catch (Exception e) {
                 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_ID_CARD_PRINTING_FAILED);
                 PatientRegistrationWebUtil.updatePrintingCardStatus(patient, encounterType, encounter, registrationLocation, new Boolean(false), new Date());
             }
 
             return new ModelAndView("redirect:/module/patientregistration/workflow/enterPatientDemo.form?editDivId=scanIdCardDiv&patientId="+ patient.getId());
         }
         if(StringUtils.isNotBlank(nextTask)){
             model.addAttribute("nextTask", nextTask);
         }
         if(StringUtils.isNotBlank(subTask)){
             model.addAttribute("subTask", subTask);
         }
         return new ModelAndView("/module/patientregistration/workflow/enterPatientDemo");
     }
 
     @RequestMapping(params="clear", method = RequestMethod.POST)
     public ModelAndView clearPatientName(
             @ModelAttribute("patient") Patient patient, BindingResult result
             ,HttpSession session){
 
         if(patient!=null){
             PersonName personName= patient.getPersonName();
             if(personName==null){
                 personName = new PersonName();
             }
             personName.setGivenName(null);
             patient.addName(personName);
             patient.getPersonName().setPreferred(true);
             session.setAttribute(PatientRegistrationConstants.REGISTRATION_PATIENT, patient);
         }
         return new ModelAndView("/module/patientregistration/workflow/enterPatientDemo");
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public ModelAndView processSelectPatient(
             @ModelAttribute("patient") Patient patient, BindingResult result
             , @ModelAttribute("birthdate") Birthdate birthdate, BindingResult birthdateResult
             , @ModelAttribute("age") Age age, BindingResult ageResult
             , @RequestParam(value = "hiddenPatientIdentifier", required=false) String patientIdentifier
             ,@RequestParam("hiddenConfirmFirstName") String patientInputName
             ,@RequestParam("hiddenConfirmLastName") String patientLastName
             ,@RequestParam("hiddenConfirmGender") String patientGender
             ,@RequestParam("hiddenPatientAddress") String patientAddress
             ,@RequestParam("hiddenConfirmPhoneNumber") String phoneNumber
             ,@RequestParam("hiddenNextTask") String nextTask
             ,@RequestParam(value= "hiddenPrintIdCard", required = false) String hiddenPrintIdCard
             ,@RequestParam(value= "subTask", required = false) String subTask
             ,HttpSession session
             , ModelMap model) {
 
         boolean printIdCard=false;
         UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_REGISTRATION_SUBMITTED);
         if (patient!=null && (patient.getId()!=null)){
             patient = Context.getPatientService().getPatient((Integer) patient.getId());
         }
         if(StringUtils.isNotBlank(patientInputName)){
             if(patient!=null){
                 PersonName personName= patient.getPersonName();
                 if(personName==null){
                     personName = new PersonName();
                 }
                 personName.setGivenName(patientInputName);
                 if(StringUtils.isNotBlank(patientLastName)){
                     personName.setFamilyName(patientLastName);
                 }
                 patient.addName(personName);
                 patient.getPersonName().setPreferred(true);
 
             }
         }
         if(StringUtils.isNotBlank(patientGender)){
             patient.setGender(patientGender);
         }
         boolean unknownPatient = PatientRegistrationUtil.isUnknownPatient(patient);
         //do not do birthdate validation for John Doe patients
         if(!StringUtils.equals(subTask, PatientRegistrationConstants.REGISTER_JOHN_DOE_TASK) &&
                 !unknownPatient){
             // make sure user specified either a birth date or year
             if (!birthdate.hasValue() && !age.hasValue()) {
                 birthdateResult.reject("Person.birthdate.required");
             }
             // validate the appropriate set of fields
             if (birthdate.hasValue()) {
                 birthdateValidator.validate(birthdate, birthdateResult);
             }else{
                 ageValidator.validate(age, ageResult);
             }
             if (birthdateResult.hasErrors() || ageResult.hasErrors()) {
                 model.addAttribute("birthdateErrors", birthdateResult);
                 model.addAttribute("ageErrors", ageResult);
                 return new ModelAndView("/module/patientregistration/workflow/enterPatientDemo", model);
             }
         }
         if (birthdate.hasValue()){
             patient.setBirthdate(birthdate.asDateObject());
             if (!birthdate.isExact()) {
                 patient.setBirthdateEstimated(true);
             }else{
                 patient.setBirthdateEstimated(false);
             }
         }else if (age.hasValue()){
             patient.setBirthdate(PatientRegistrationUtil.calculateBirthdateFromAge(age, null));
             patient.setBirthdateEstimated(true);
         }
 
         Location medicalRecordLocation = getMedicalRecordLocationRecursivelyBasedOnTag(getRegistrationLocation(session));
         Location encounterLocation = getRegistrationLocation(session) ;
 
         // if a fixed patient identifier location has been set, get it
 
         PatientIdentifierType zlIdentifierType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE();
         if(zlIdentifierType!=null){
             PatientIdentifier patientPreferredIdentifier = PatientRegistrationUtil.getPreferredIdentifier(patient);
             if(patientPreferredIdentifier==null ||
                     (patientPreferredIdentifier!=null && (patientPreferredIdentifier.getIdentifierType().getId().compareTo(zlIdentifierType.getId())!=0))){
                 //if the existing preferred Identifier is not ZL EMR ID create a new one
                 PatientIdentifier identifier = new PatientIdentifier(null, zlIdentifierType, medicalRecordLocation);
                 if(StringUtils.isNotBlank(patientIdentifier)){
                     identifier.setIdentifier(patientIdentifier);
                 }else{
                     identifier.setIdentifier(PatientRegistrationUtil.assignIdentifier(zlIdentifierType)) ;
                 }
                log.info("Created new identifier" + identifier.getIdentifier());
                 identifier.setPreferred(true);
                 patient.addIdentifier(identifier);
                 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_REGISTRATION_NEW_ZL_ID, "Identifier: " + identifier);
             }
             printIdCard=true;
         }else{
            log.warn("no preferred identifier has been set");
             model.addAttribute("identifierErrors", "please set preferred identifier");
             return new ModelAndView("/module/patientregistration/workflow/enterPatientDemo", model);
         }
 
 
         if(StringUtils.isNotBlank(patientAddress)){
             if(patient!=null){
                 PersonAddress personAddress = patient.getPersonAddress();
                 if(personAddress==null){
                     personAddress = new PersonAddress();
                 }
                 List<String> levels = PatientRegistrationUtil.getAddressHierarchyLevels();
                 if(levels!=null && levels.size()>0){
                     Collections.reverse(levels);
                 }
 
                 int i = 0;
                 // iterate through all the names in the search string to form the PersonAddress object
                 for (String name : patientAddress.split("\\|")) {
                     if (name!=null) {
                         if (levels.size() <= i-1) {  // make sure we haven't reached the bottom level, because this would make no sense
                             log.error("Address hierarchy levels have not been properly defined.");
                         }
                         else {
                             PatientRegistrationUtil.setAddressFieldValue(personAddress, levels.get(i), name);
                         }
                     }
                     i++;
                 }
                 if (personAddress!=null)
                     personAddress.setPreferred(true);
                 patient.addAddress(personAddress);
             }
         }
 
         if(patient.getPersonAddress()!=null){
             // remove the address if it is blank
             if (PatientRegistrationUtil.isBlank(patient.getPersonAddress())) {
                 patient.removeAddress(patient.getPersonAddress());
             }
         }
 
         PersonAttributeType phoneType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_ID_CARD_PERSON_ATTRIBUTE_TYPE();
 
         // remove any attributes that are blank
         for (PersonAttributeType attr : Context.getPersonService().getPersonAttributeTypes(PERSON_TYPE.PATIENT, ATTR_VIEW_TYPE.VIEWING)) {
             PersonAttribute att = patient.getAttribute(attr);
             if (att != null) {
                 if (StringUtils.isBlank(att.getValue())) {
                     patient.removeAttribute(att); // If somehow the patient has any blank attribute saved, remove it
                 }
                 else if (StringUtils.isBlank(phoneNumber) && attr.equals(phoneType)) {
                     patient.removeAttribute(att); // If the user blanked out the existing phone number attribute, remove it
                 }
             }
         }
 
         // now print the patient attribute type that has specified in the idCardPersonAttributeType global property
 
         if (phoneType != null && StringUtils.isNotBlank(phoneNumber)) {
             PersonAttribute personAttribute = new PersonAttribute(phoneType, phoneNumber);
             patient.addAttribute(personAttribute);
         }
 
         PersonAttributeType unknownPatientAttributeType = PatientRegistrationGlobalProperties.UNKNOWN_PATIENT_PERSON_ATTRIBUTE_TYPE();
         if (StringUtils.equals(subTask, PatientRegistrationConstants.REGISTER_JOHN_DOE_TASK)){
             if(unknownPatientAttributeType!=null){
                 PersonAttribute personAttribute = new PersonAttribute(unknownPatientAttributeType, "true");
                 patient.addAttribute(personAttribute);
             }
         }
 
         TaskProgress taskProgress = PatientRegistrationWebUtil.getTaskProgress(session);
         if(taskProgress!=null){
             taskProgress.setPatientId(patient.getId());
             taskProgress.setProgressBarImage(PatientRegistrationConstants.RETROSPECTIVE_PROGRESS_2_IMG);
             Map<String, Integer> completedTasks = new HashMap<String, Integer>();
             completedTasks.put("registrationTask", new Integer(1));
             taskProgress.setCompletedTasks(completedTasks);
             PatientRegistrationWebUtil.setTaskProgress(session, taskProgress);
         }
         Context.getPatientService().savePatient(patient);
 
 
         //add Patient Registration encounter
         EncounterType encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PATIENT_REGISTRATION_ENCOUNTER_TYPE();
         Encounter encounter = Context.getService(PatientRegistrationService.class).registerPatient(
                 patient
                 , Context.getAuthenticatedUser().getPerson()
                 , encounterType
                 , encounterLocation);
 
         List<PrintErrorType> printErrorTypes = new ArrayList<PrintErrorType>();
         // if this is a J. Doe unconscious arrival, then we check them in automatically for a visit
         if (StringUtils.equals(subTask, PatientRegistrationConstants.REGISTER_JOHN_DOE_TASK)) {
             Context.getService(AdtService.class).checkInPatient(patient, getRegistrationLocation(session) , null, null, null, false);
             try {
                 Context.getService(PatientRegistrationService.class).printRegistrationLabel(patient, getRegistrationLocation(session) , 2);
 
             } catch (UnableToPrintLabelException e) {
                 log.error("failed to print patient label", e);
                 printErrorTypes.add(LABEL_PRINTER_ERROR);
                 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_DOSSIER_LABEL_PRINTING_FAILED);
             } catch (APIException ex){
                 log.error("failed to print patient label", ex);
                 printErrorTypes.add(LABEL_PRINTER_NOT_CONFIGURED);
                 UserActivityLogger.logActivity(session, ex.getMessage());
             }
 
             try{
                 Context.getService(PatientRegistrationService.class).printIDCardLabel(patient, new EmrContext(session).getSessionLocation());
             }
             catch (UnableToPrintLabelException e) {
                 log.error("failed to print patient label", e);
                 printErrorTypes.add(CARD_PRINTER_ERROR);
                 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_ID_CARD_LABEL_PRINTING_FAILED);
             } catch (APIException ex){
                 log.error("failed to print patient label", ex);
                 printErrorTypes.add(CARD_PRINTER_NOT_CONFIGURED);
                 UserActivityLogger.logActivity(session, ex.getMessage());
             }
         }
 
         String nextPage =null;
         if(StringUtils.equals(hiddenPrintIdCard, "no")){
             printIdCard=false;
         }else if(printIdCard){
 
             //print an ID card only if a new ZL EMR ID has been created
             try {
                 Context.getService(PatientRegistrationService.class).printIDCard(patient, new EmrContext(session).getSessionLocation());
                 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_ID_CARD_PRINTING_SUCCESSFUL);
                 PatientRegistrationWebUtil.updatePrintingCardStatus(patient, encounterType, encounter, encounterLocation, new Boolean(true), new Date());
             }
             catch (Exception e) {
                 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_ID_CARD_PRINTING_FAILED);
                 PatientRegistrationWebUtil.updatePrintingCardStatus(patient, encounterType, encounter, encounterLocation, new Boolean(false), new Date());
             }
 
             if(StringUtils.isNotBlank(nextTask)){
                 return new ModelAndView("redirect:/module/patientregistration/workflow/" + nextTask + "?patientId=" + patient.getPatientId(), model);
             }
             nextPage = "redirect:/module/patientregistration/workflow/enterPatientDemo.form?editDivId=scanIdCardDiv&patientId="+ patient.getId();
             return new ModelAndView(nextPage);
         }
         // since this is a new patient, set the flag that lets us know we want to automatically bring a registration label
         session.setAttribute("registration_printRegistrationLabel", true);
         PatientRegistrationWebUtil.resetPatientRegistrationWorkflow(session);
         String message = encounter == null ? null : "Created encounter: " + encounter.getUuid();
         UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_REGISTRATION_COMPLETED, message);
         UserActivityLogger.endActivityGroup(session);
 
         if(StringUtils.isNotBlank(nextTask) && !StringUtils.equals(subTask, PatientRegistrationConstants.REGISTER_JOHN_DOE_TASK)){
             return new ModelAndView("redirect:/module/patientregistration/workflow/" + nextTask + "?patientId=" + patient.getPatientId(), model);
         }
         String printErrorsQuery = createPrintErrorsQuery(printErrorTypes);
         nextPage = "redirect:/module/patientregistration/workflow/patientDashboard.form?patientId="+ patient.getId() + printErrorsQuery;
         return new ModelAndView(nextPage);
     }
 
     private String createPrintErrorsQuery(List<PrintErrorType> printErrorTypes) {
         String query = "";
 
         for (PrintErrorType printErrorType : printErrorTypes) {
             query += "&printErrorsType=" + printErrorType.getCode();
         }
 
         return query;
     }
 }
