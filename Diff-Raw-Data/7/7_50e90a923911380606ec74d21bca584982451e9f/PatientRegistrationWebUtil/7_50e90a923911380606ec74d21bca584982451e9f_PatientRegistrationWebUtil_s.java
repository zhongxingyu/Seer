 package org.openmrs.module.patientregistration.util;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.ConceptDescription;
 import org.openmrs.ConceptMap;
 import org.openmrs.ConceptName;
 import org.openmrs.ConceptSource;
 import org.openmrs.ConceptWord;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.api.APIException;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.PersonService.ATTR_VIEW_TYPE;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.emr.EmrProperties;
 import org.openmrs.module.importpatientfromws.RemotePatient;
 import org.openmrs.module.paperrecord.UnableToPrintLabelException;
 import org.openmrs.module.patientregistration.PatientRegistrationConstants;
 import org.openmrs.module.patientregistration.PatientRegistrationGlobalProperties;
 import org.openmrs.module.patientregistration.PatientRegistrationUtil;
import org.openmrs.module.patientregistration.util.PrintErrorType;
 import org.openmrs.module.patientregistration.service.PatientRegistrationService;
 import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
 import org.openmrs.util.OpenmrsUtil;
 
 import javax.servlet.http.HttpSession;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import static org.openmrs.module.patientregistration.util.PrintErrorType.LABEL_PRINTER_ERROR;
 import static org.openmrs.module.patientregistration.util.PrintErrorType.LABEL_PRINTER_NOT_CONFIGURED;
 
 
 public class PatientRegistrationWebUtil {
 
 	protected static final Log log = LogFactory.getLog(PatientRegistrationWebUtil.class);
 	
 	/**
 	 * Resets the session variables used to store the specifics of patient data collected during a registration workflow
 	 * This should be called whenever the workflow is restarted
 	 */
 	public static void resetPatientRegistrationWorkflow(HttpSession session) {
 		// used to pass patient id, or list of patient ids between states in the workflow
 		session.removeAttribute("registration_patientId");
 		session.removeAttribute("registration_patientIds");
 		
 		// used to store transient patient data for the new patient to add
 		session.removeAttribute("registration_patientName");
 		session.removeAttribute("registration_patient");
 		session.removeAttribute("registration_age");
 		session.removeAttribute("registration_birthdate");
 	}
 
 	/**
 	 * Determines if the patient workflow session is "active"
 	 * Right now this simply means to confirm that the task and location have been specified
 	 */
 	public static Boolean confirmActivePatientRegistrationSession(HttpSession session) {
 		return (getRegistrationLocation(session) != null && getRegistrationTask(session) != null);
 	}
 	
 	/**
 	 * Given the session, returns the registration location associated with the session
 	 */
 	public static Location getRegistrationLocation(HttpSession session) {
         return new EmrContext(session).getSessionLocation();
 	}
 
     public static String createPrintErrorsQuery(List<PrintErrorType> printErrorTypes) {
         String query = "";
 
         for (PrintErrorType printErrorType : printErrorTypes) {
             query += "&printErrorsType=" + printErrorType.getCode();
         }
 
         return query;
     }
     public static List<PrintErrorType> printLabels(Patient patient, HttpSession session, Location location, int n) {
         List<PrintErrorType> printErrorTypes = new ArrayList<PrintErrorType>();
         try {
             Context.getService(PatientRegistrationService.class).printRegistrationLabel(patient, location , n);
         } catch (UnableToPrintLabelException e) {
            log.error("failed to print patient label", e);
             printErrorTypes.add(LABEL_PRINTER_ERROR);
             UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_DOSSIER_LABEL_PRINTING_FAILED);
         } catch (APIException ex){
            log.error("failed to print patient label", ex);
             printErrorTypes.add(LABEL_PRINTER_NOT_CONFIGURED);
             UserActivityLogger.logActivity(session, ex.getMessage());
         }
 
         return printErrorTypes;
     }
 
     /**
      * Sets the registration location (which just sets the underlying session location)
      *
      * @param session
      * @param location
      */
     public static void setRegistrationLocation(HttpSession session, Location location) {
         new EmrContext(session).setSessionLocation(location);
     }
 
     public static void saveToCache(List<RemotePatient> remotePatients, HttpSession session) {
         Map<String, RemotePatient> mpiSearchResults = (Map<String, RemotePatient>) session.getAttribute(PatientRegistrationConstants.MPI_SEARCH_RESULTS);
         if (mpiSearchResults == null) {
             mpiSearchResults = new HashMap<String, RemotePatient>();
             session.setAttribute(PatientRegistrationConstants.MPI_SEARCH_RESULTS, mpiSearchResults);
         }
         if(remotePatients!=null && remotePatients.size()>0){
             for(RemotePatient remotePatient : remotePatients){
                 mpiSearchResults.put(PatientRegistrationConstants.MPI_REMOTE_SERVER + ":" + remotePatient.getRemoteUuid(), remotePatient);
             }
         }
     }
 
     public static RemotePatient getFromCache(String uuid, HttpSession session){
 
         RemotePatient remotePatient = null;
         Map<String, RemotePatient> mpiSearchResults = (Map<String, RemotePatient>) session.getAttribute(PatientRegistrationConstants.MPI_SEARCH_RESULTS);
         if(mpiSearchResults!=null && mpiSearchResults.size()>0){
             remotePatient = mpiSearchResults.get(PatientRegistrationConstants.MPI_REMOTE_SERVER + ":" + uuid);
         }
         return remotePatient;
     }
 
     public static RemotePatient removeFromCache(String uuid, HttpSession session){
 
         RemotePatient remotePatient = null;
         Map<String, RemotePatient> mpiSearchResults = (Map<String, RemotePatient>) session.getAttribute(PatientRegistrationConstants.MPI_SEARCH_RESULTS);
         if(mpiSearchResults!=null && mpiSearchResults.size()>0){
             String key = PatientRegistrationConstants.MPI_REMOTE_SERVER + ":" + uuid;
             remotePatient = mpiSearchResults.get(key);
             if(remotePatient!=null){
                 mpiSearchResults.remove(key);
             }
         }
         return remotePatient;
     }
 
     public static Map<Integer, String> getEncounterEditURLs() {
 		Map<Integer, String> editURLs = new HashMap<Integer, String>();
 		EncounterType encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_VISIT_ENCOUNTER_TYPE();
 		if(encounterType!=null){
 			editURLs.put(encounterType.getEncounterTypeId(), "/module/patientregistration/workflow/primaryCareVisitEncounter.form");
 		}
 		encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PATIENT_REGISTRATION_ENCOUNTER_TYPE();
 		if(encounterType!=null){
 			editURLs.put(encounterType.getEncounterTypeId(), "/module/patientregistration/workflow/enterPatientDemo.form");
 		}
 		
 		encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_ENCOUNTER_TYPE();
 		if(encounterType!=null){
 			editURLs.put(encounterType.getEncounterTypeId(), "/module/patientregistration/workflow/primaryCareReceptionEncounter.form");
 		}
 		
 		return editURLs;
 	}
 	
 	public static Map<Integer, String> getEncounterTypeLocale() {
 		Map<Integer, String> encounterLocale = new HashMap<Integer, String>();
 		EncounterType encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_VISIT_ENCOUNTER_TYPE();
 		String encName =null;
 		if(encounterType!=null){
 			encName = Context.getMessageSourceService().getMessage("patientregistration.tasks.primaryCareVisit" , null, Context.getLocale());    		
 			encounterLocale.put(encounterType.getEncounterTypeId(), encName);
 		}
 		encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PATIENT_REGISTRATION_ENCOUNTER_TYPE();
 		if(encounterType!=null){
 			encName = Context.getMessageSourceService().getMessage("patientregistration.tasks.patientRegistration" , null, Context.getLocale());    
 			encounterLocale.put(encounterType.getEncounterTypeId(),encName);
 		}
 		
 		encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_ENCOUNTER_TYPE();
 		if(encounterType!=null){
 			encName = Context.getMessageSourceService().getMessage("patientregistration.tasks.primaryCareReception" , null, Context.getLocale());   
 			encounterLocale.put(encounterType.getEncounterTypeId(), encName);
 		}
 		encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_EPI_INFO_ENCOUNTER_TYPE();
 		if(encounterType!=null){
 			encName = Context.getMessageSourceService().getMessage("patientregistration.tasks.epiInfoVisit" , null, Context.getLocale());   
 			encounterLocale.put(encounterType.getEncounterTypeId(), encName);
 		}
 		
 		return encounterLocale;
 	}
 
 	/**
 	 * Given the session, returns the registration task associated with the session
 	 */
 	public static String getRegistrationTask(HttpSession session) {
 		return (String) session.getAttribute(PatientRegistrationConstants.SESSION_REGISTRATION_TASK);
 	}
 	
 	/**
 	 * Given the session, returns the progress of the retrospective task associated with the session
 	 */
 	public static TaskProgress getTaskProgress(HttpSession session) {
 		if(session.getAttribute(PatientRegistrationConstants.SESSION_TASK_PROGRESS)!=null){
 			return (TaskProgress) session.getAttribute(PatientRegistrationConstants.SESSION_TASK_PROGRESS);
 		}else{
 			return null;
 		}
 		
 	}
 	/**
 	 * Updates the progress of the retrospective task
 	 */
 	public static void setTaskProgress(HttpSession session, TaskProgress taskProgress){
 		if(taskProgress!=null){
 			session.setAttribute(PatientRegistrationConstants.SESSION_TASK_PROGRESS, taskProgress);
 		}
 	}
 	
 	/**
 	 * Updates the registration location in the session, if needed
 	 */
 	public static void setRegistrationTask(HttpSession session, String task) {
 		String currentTask = getRegistrationTask(session);
 		if (!OpenmrsUtil.nullSafeEquals(currentTask, task)) {
 			session.setAttribute(PatientRegistrationConstants.SESSION_REGISTRATION_TASK, task);
 			UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_REGISTRATION_TASK_CHANGED);
 		}
 	}
 	
 	public static List<POCObservation> getPOCObservation(List<Obs> obs, ConceptSource icd10){
 		List<POCObservation> obsDiagnosis = null;
 		if(obs!=null && obs.size()>0){
 			Concept notifyConcept = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_VISIT_NOTIFY_DIAGNOSIS_CONCEPT();
 			Map<String, String> notifyConceptMap = null;
 			if(notifyConcept==null){
 				log.error("Global property patientregistration.primaryCareVisitNotifyDiagnosisConcept is undefined or does not match an existing concept");
 			}else{
 				notifyConceptMap = PatientRegistrationUtil.getConvSetMap(notifyConcept, icd10);				
 			}
 			obsDiagnosis = new ArrayList<POCObservation>();
 			for(Obs ob : obs){				
 				POCObservation pocDiagnosis = new POCObservation();
 				pocDiagnosis.setObsId(ob.getId());
 				Concept codedDiagnosis= ob.getValueCoded();
 				if(codedDiagnosis!=null){
 					pocDiagnosis.setType(POCObservation.CODED);
 					pocDiagnosis.setId(codedDiagnosis.getId());
 					pocDiagnosis.setLabel(codedDiagnosis.getDisplayString().replace("\\", " "));
 					pocDiagnosis.setNotifiable(new Boolean(false)); 
 					if(notifyConceptMap!=null){
 						if(notifyConceptMap.containsValue(codedDiagnosis.getId().toString())){
 							pocDiagnosis.setNotifiable(new Boolean(true));
 						}
 					}
 					if(icd10!=null){
 						ConceptMap mapping = PatientRegistrationUtil.getConceptMapping(codedDiagnosis, icd10);						
 						if (mapping == null) {
 							pocDiagnosis.setLabel(codedDiagnosis.getDisplayString().replace("\\", " "));							
 						}
 						else {
 							pocDiagnosis.setLabel("(" + mapping.getSourceCode() + ") " + codedDiagnosis.getDisplayString().replace("\\", " "));							
 						}
 					}
 				}
 				else{
 					pocDiagnosis.setType(POCObservation.NONCODED);
 					pocDiagnosis.setId(new Integer(0));
 					pocDiagnosis.setLabel(ob.getValueText().replace("\\", " "));
 				}
 				obsDiagnosis.add(pocDiagnosis);
 			}			
 		}
 		return obsDiagnosis;
 	}
 	
 	public static List<Obs> getPatientDiagnosis(Patient patient, EncounterType encounterType, Encounter encounter, Location registrationLocation, Date encounterDate ){
 		
 		Concept codedConcept = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_VISIT_CODED_DIAGNOSIS_CONCEPT();
 		Concept uncodedConcept = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_VISIT_NON_CODED_DIAGNOSIS_CONCEPT();
 		List<Concept> questions = new ArrayList<Concept>();
 		questions.add(codedConcept);
 		questions.add(uncodedConcept);		
 		
 		return getPatientObs(patient, encounterType, encounter, questions, registrationLocation, encounterDate);
 	}
 	
 	public static List<Obs> getPatientPayment(Patient patient, EncounterType encounterType, Encounter encounter, Location registrationLocation, Date encounterDate ){
 		
 		List<Concept> questions = new ArrayList<Concept>();
 		questions.add(PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_VISIT_REASON_CONCEPT());
 		questions.add(PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_PAYMENT_AMOUNT_CONCEPT());
 		questions.add(PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_RECEIPT_NUMBER_CONCEPT());
 		
 		return getPatientObs(patient, encounterType, encounter, questions, registrationLocation, encounterDate);
 	}
     public static List<List<Obs>> getPatientGroupPayment(Patient patient, EncounterType encounterType, Encounter editEncounter, Location registrationLocation, Date encounterDate, EmrProperties emrProperties) {
         List<List<Obs>> paymentGroups = null;
         List<Concept> questions = new ArrayList<Concept>();
         questions.add(emrProperties.getPaymentConstructConcept());
 
         List<Obs> paymentGroupObs = getPatientObs(patient, encounterType, editEncounter, questions, registrationLocation, encounterDate);
         if(paymentGroupObs!=null && paymentGroupObs.size()>0){
             paymentGroups = new ArrayList<List<Obs>>();
             for(Obs groupObs : paymentGroupObs){
                 List<Obs> paymentGroup = new ArrayList<Obs>();
                 Set<Obs> groupMembers = groupObs.getGroupMembers();
                 paymentGroup.addAll(groupMembers);
                 paymentGroups.add(paymentGroup);
             }
         }
         return paymentGroups;
     }
 	
 	public static List<Obs> getPatientObs(
 			Patient patient, 
 			EncounterType encounterType, 
 			Encounter encounter, 
 			List<Concept> questions, 
 			Location registrationLocation, 
 			Date encounterDate ){
 		
 		List<Encounter> encounters =  new ArrayList<Encounter>();
 		if(encounterType!=null && encounter!=null){
 			//make sure this is the primary care visit encounter
 			if(encounterType.getId().compareTo(encounter.getEncounterType().getId()) ==0){
 				encounters.add(encounter);
 			}
 		}
 		
 		List<Obs> obs = Context.getService(PatientRegistrationService.class).getPatientObs(patient, encounterType, encounters, questions, registrationLocation, encounterDate);
 		
 		if(obs!=null && obs.size()>0){
 			return obs;
 		}else{
 			return null;
 		}
 		
 	}
 	
 	public static Concept getPrintingIDCardStatus(){
 		Concept concept = null;
 		List<ConceptWord> conceptWords = Context.getConceptService().getConceptWords("PrintingIDCardStatus", Locale.US);
 		if(conceptWords!=null && conceptWords.size()>0){
 			concept = conceptWords.get(0).getConcept();
 			Integer maxConceptId = new Integer(0);
 			for(ConceptWord conceptWord : conceptWords){
 				if (conceptWord.getConcept().getId().compareTo(maxConceptId) >0){
 					maxConceptId = conceptWord.getConcept().getId();
 					concept=conceptWord.getConcept();
 				}
 			}
 		}
 		
 		if(concept==null){
 			concept = new Concept();
 			concept.addName(new ConceptName("PrintingIDCardStatus", Locale.US));
 			concept.setConceptId(null);
 			concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Text"));
 			concept.setConceptClass(Context.getConceptService().getConceptClassByName("Misc"));	
 			ConceptDescription originalConceptDescription = new ConceptDescription();
 			originalConceptDescription.setLocale(Locale.ENGLISH);
 			originalConceptDescription.setDescription("PrintingIDCardStatus");
 			concept.addDescription(originalConceptDescription);
 			try{
 				concept = Context.getConceptService().saveConcept(concept);
 			}catch(Exception e){
 				log.error("failed to create PrintingIDCardStatus.", e);
 			}
 		}
 		return concept;
 	}
 	
 	public static Concept getPossiblePOCDuplicatesConcept(){
 		Concept concept = null;
 		List<ConceptWord> conceptWords = Context.getConceptService().getConceptWords("PossiblePOCDuplicates", Locale.US);
 		if(conceptWords!=null && conceptWords.size()>0){
 			concept = conceptWords.get(0).getConcept();
 			Integer maxConceptId = new Integer(0);
 			for(ConceptWord conceptWord : conceptWords){
 				if (conceptWord.getConcept().getId().compareTo(maxConceptId) >0){
 					maxConceptId = conceptWord.getConcept().getId();
 					concept=conceptWord.getConcept();
 				}
 			}
 		}
 		
 		if(concept==null){
 			concept = new Concept();
 			concept.addName(new ConceptName("PossiblePOCDuplicates", Locale.US));
 			concept.setConceptId(null);
 			concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Text"));
 			concept.setConceptClass(Context.getConceptService().getConceptClassByName("Misc"));	
 			ConceptDescription originalConceptDescription = new ConceptDescription();
 			originalConceptDescription.setLocale(Locale.ENGLISH);
 			originalConceptDescription.setDescription("PossiblePOCDuplicates");
 			concept.addDescription(originalConceptDescription);
 			try{
 				concept = Context.getConceptService().saveConcept(concept);
 			}catch(Exception e){
 				log.error("failed to create PossiblePOCDuplicates.", e);
 			}
 		}
 		return concept;
 	}
 	
 	public static List<Patient> getDistinctDuplicatePatients(HttpSession session){
 		
 		List<Patient> duplicatePatients = null;
 		Concept duplicateConcept= getPossiblePOCDuplicatesConcept();
 		Set<Integer> distinctDuplicateObs =  Context.getService(PatientRegistrationService.class).getDistinctDuplicateObs(duplicateConcept.getConceptId());
 		if(distinctDuplicateObs!=null && (distinctDuplicateObs.size()>0)){
 			Location location = PatientRegistrationWebUtil.getRegistrationLocation(session);
 			Set<Integer> counterIds = new HashSet<Integer>();
 			duplicatePatients = new ArrayList<Patient>();
 			for(Integer distinctId : distinctDuplicateObs){				
 				List<Patient> tempList =  new ArrayList<Patient>();
 				Patient patient = Context.getPatientService().getPatient(new Integer(distinctId));	
 				if(patient!=null && !patient.isVoided()){					
 					tempList.add(patient);
 				}				
 				List<Obs> obs = 
 					 Context.getObsService().getObservations(Collections.singletonList((Person)patient)
 								, null, Collections.singletonList((Concept)duplicateConcept)
 								, null, null, Collections.singletonList((Location)location)
 								, null, null, null, null, null, false);	 	
 				if(obs!=null && obs.size()>0){
 					for(Obs duplicateObs : obs){					
 						String duplicateUuid = duplicateObs.getValueText();
 			    		if(StringUtils.isNotBlank(duplicateUuid)){
 			    			Patient duplicatePatient =  Context.getPatientService().getPatientByUuid(duplicateUuid);
 			    			if(duplicatePatient!=null && !duplicatePatient.isVoided() && !counterIds.contains(duplicatePatient.getId())){			    				
 								if(!tempList.contains(duplicatePatient)){
 									tempList.add(duplicatePatient);			
 								}
 			    			}
 			    		}
 					}					
 				}
 				if(tempList.size()>1){
 					for(Patient temp: tempList){
 						if(!duplicatePatients.contains(temp)){
 							duplicatePatients.add(temp);
 						}
 					}
 				}
 			}
 		}
 		
 		return duplicatePatients;
 	}
 	
 	public static boolean addPocFalseDuplicatePatient(Integer patientId, Integer duplicateId, HttpSession session){
 		boolean success = false;
 		Concept falseDuplicateConcept= getFalsePOCDuplicatesConcept();
 		if(falseDuplicateConcept!=null){
 			success = addPocPatientObs(patientId, duplicateId, falseDuplicateConcept, session);
 		}		
 		return success;
 	}
 	
 	public static boolean addPocDuplicatePatient(Integer patientId, Integer duplicateId, HttpSession session){
 		boolean success = false;
 		Concept duplicateConcept= getPossiblePOCDuplicatesConcept();
 		if(duplicateConcept!=null){
 			success = addPocPatientObs(patientId, duplicateId, duplicateConcept, session);
 		}
 		
 		return success;
 	}
 	
 	public static boolean addPocPatientObs(Integer patientId, Integer duplicateId, Concept concept, HttpSession session){
 		boolean success = false;
 		Patient patient = Context.getPatientService().getPatient(new Integer(patientId));
 		Patient duplicatePatient = Context.getPatientService().getPatient(new Integer(duplicateId));
 		if(concept!=null && patient!=null && duplicatePatient!=null){
 			boolean foundDuplicate = false;
 			List<Obs> obs = 
 				 Context.getObsService().getObservations(Collections.singletonList((Person)patient)
 							, null, Collections.singletonList((Concept)concept)
 							, null, null, null
 							, null, null, null, null, null, false);	 	
 			if(obs!=null && obs.size()>0){
 				for(Obs duplicateObs : obs){
 					if(StringUtils.contains(duplicateObs.getValueText(), duplicatePatient.getUuid())){
 						foundDuplicate = true;
 						break;
 					}
 				}
 			}
 			if(!foundDuplicate){
 				Obs duplicateObs = new Obs();
 				duplicateObs.setPerson(patient);
 				duplicateObs.setConcept(concept);
 				duplicateObs.setObsDatetime(new Date());
 				Location location = PatientRegistrationWebUtil.getRegistrationLocation(session);
 				if(location!=null){
 					duplicateObs.setLocation(location);
 				}
 				duplicateObs.setValueText(duplicatePatient.getUuid());
 				try{
 					Context.getObsService().saveObs(duplicateObs, "POC duplicate patient");
 					success = true;
 				}catch(Exception e){
 					log.error("failed to save obs with duplicate poc patient info", e);
 				}
 			}
 		}
 		return success;
 	}
 	
 	public static Set<Integer> getPOCPatientDuplicateObs(Patient patient, Concept concept){		
 	    Set<Integer> pocDuplicateSet = null;	    
 	    if(patient!=null){	    	
 		    List<Obs> obs = 
 				 Context.getObsService().getObservations(Collections.singletonList((Person)patient)
 							, null, Collections.singletonList((Concept)concept)
 							, null, null, null
 							, null, null, null, null, null, false);	 	
 		    if(obs!=null && obs.size()>0){
 		    	pocDuplicateSet = new HashSet<Integer>();
 		    	for(Obs duplicateObs : obs){
 		    		String duplicateUuid = duplicateObs.getValueText();
 		    		if(StringUtils.isNotBlank(duplicateUuid)){
 		    			Patient duplicatePatient =  Context.getPatientService().getPatientByUuid(duplicateUuid);
 		    			if(duplicatePatient!=null){
 		    				pocDuplicateSet.add(duplicatePatient.getId());
 		    			}
 		    		}
 		    	}
 		    }
 	    }
 	    return pocDuplicateSet;
 	}
 	public static Set<Integer> getPOCFalsePatientDuplicates(Patient patient){		
 	    Set<Integer> pocFalseDuplicateSet = null;	    
 	    if(patient!=null){
 	    	Concept falseDuplicateConcept= getFalsePOCDuplicatesConcept();
 	    	if(falseDuplicateConcept!=null){
 	    		pocFalseDuplicateSet = getPOCPatientDuplicateObs(patient, falseDuplicateConcept);
 	    	}
 	    }
 	    return pocFalseDuplicateSet;
 	}
 	
 	public static Set<Integer> getPOCPatientDuplicates(Patient patient){		
 	    Set<Integer> pocDuplicateSet = null;	    
 	    if(patient!=null){
 	    	Concept duplicateConcept= getPossiblePOCDuplicatesConcept();
 	    	if(duplicateConcept!=null){
 	    		pocDuplicateSet = getPOCPatientDuplicateObs(patient, duplicateConcept);
 	    	}
 	    }
 	    return pocDuplicateSet;
 	}
 	public static Concept getFalsePOCDuplicatesConcept(){
 		Concept concept = null;
 		List<ConceptWord> conceptWords = Context.getConceptService().getConceptWords("FalsePOCDuplicates", Locale.US);
 		if(conceptWords!=null && conceptWords.size()>0){
 			concept = conceptWords.get(0).getConcept();
 			Integer maxConceptId = new Integer(0);
 			for(ConceptWord conceptWord : conceptWords){
 				if (conceptWord.getConcept().getId().compareTo(maxConceptId) >0){
 					maxConceptId = conceptWord.getConcept().getId();
 					concept=conceptWord.getConcept();
 				}
 			}
 		}
 		
 		if(concept==null){
 			concept = new Concept();
 			concept.addName(new ConceptName("FalsePOCDuplicates", Locale.US));
 			concept.setConceptId(null);
 			concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Text"));
 			concept.setConceptClass(Context.getConceptService().getConceptClassByName("Misc"));	
 			ConceptDescription originalConceptDescription = new ConceptDescription();
 			originalConceptDescription.setLocale(Locale.ENGLISH);
 			originalConceptDescription.setDescription("FalsePOCDuplicates");
 			concept.addDescription(originalConceptDescription);
 			try{
 				concept = Context.getConceptService().saveConcept(concept);
 			}catch(Exception e){
 				log.error("failed to create FalsePOCDuplicates.", e);
 			}
 		}
 		return concept;
 	}
 	
 	public static IDCardInfo updatePrintingCardStatus(Patient patient, EncounterType encounterType, Encounter encounter, Location location, Boolean status, Date obsDate){
 		
 		IDCardInfo cardInfo = new IDCardInfo();
 		Concept printingConcept = getPrintingIDCardStatus();		
 		List<Encounter> encounters = null;
 		if(encounter!=null && 
 				(encounterType==null || 
 				(encounterType!=null && encounterType.getId().compareTo(encounter.getEncounterType().getId())==0))){
 			encounters = Collections.singletonList((Encounter) encounter);
 		}
 		
 		Date startTime = null;
 		Date endTime = null;
 		if(obsDate!=null){
 			// clear the time component to get the start time to search (first millisecond of current day)
 			startTime = PatientRegistrationUtil.clearTimeComponent(obsDate);			
 			// create the end time to search (last millisecond of the current day)
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(startTime);
 			cal.add(Calendar.DAY_OF_MONTH, +1);
 			cal.add(Calendar.MILLISECOND, -1);
 			endTime = cal.getTime();			
 		}else{
 			obsDate = new Date();
 		}
 		List<Obs> obs = 
 			 Context.getObsService().getObservations(Collections.singletonList((Person)patient)
 						, null, Collections.singletonList((Concept)printingConcept)
 						, null, null, null
 						, null, null, null, startTime, endTime, false);	 			 		
 		if(obs!=null && obs.size()>0){
 			Obs tempObs = null;
 			Integer tempObsId=new Integer(0);
 			int counter = 0;
 			Date lastPrintingDate= null;
 			for(Obs ob : obs){
 				if(StringUtils.equals(ob.getValueText(), "true") ){
 					counter++;
 				}
 				if(ob.getObsId().compareTo(tempObsId) >0){
 					tempObsId = ob.getObsId();
 					tempObs=ob;
 					if(StringUtils.equals(tempObs.getValueText(), "true") ){
 						lastPrintingDate = tempObs.getObsDatetime();
 					}
 				}
 			}			
 			if((status==null) || 
 					(status!=null && (StringUtils.equals(tempObs.getValueText(), status.toString())))){
 				cardInfo.setPrintingObs(tempObs);
 				if(lastPrintingDate!=null){
 					cardInfo.setLastPrintingDate(lastPrintingDate);
 				}
 				cardInfo.setPrintingCounter(new Integer(counter));
 				return cardInfo;
 			}			
 				
 		}
 		Obs printingObs = null;
 		if(status!=null){
 			//create a new obs
 			Encounter registrationEncounter = null;
 			printingObs = new Obs();
 			printingObs.setConcept(printingConcept);
 			printingObs.setValueText(status.toString());		
 			if(encounter!=null){
 				registrationEncounter = encounter;				
 			}else if(encounterType!=null && patient!=null && location!=null){							
 				registrationEncounter =Context.getService(PatientRegistrationService.class).registerPatient(
 						  patient
 						, Context.getAuthenticatedUser().getPerson()
 						, encounterType
 						, location);
 			}
 			registrationEncounter.addObs(printingObs);
 			try{
 				registrationEncounter = Context.getService(EncounterService.class).saveEncounter(registrationEncounter);					
 			}catch(Exception e){
 				log.error("failed to create registration encounter", e);
 				printingObs = null;
 			}
 		}
 		if(printingObs!=null){
 			cardInfo.setPrintingObs(printingObs);
 			cardInfo.setLastPrintingDate(printingObs.getObsDatetime());
 			cardInfo.setPrintingCounter(new Integer(1));
 		}
 		return cardInfo;
 		
 	}
 	
 	/**
 	 * Resets the session timeout
 	 */
 	public static void setTimeout(HttpSession session) {
 		// make the timeout 1 hour
 		session.setMaxInactiveInterval(60 * 60);  // TODO: do we really want this long a timeout?
 	}
 	
 	/**
 	 * Updates the session variable "patientId" (if necessary) and loads the patient specified by that id
 	 */
 	public static Patient updatePatientIdSessionAttributeAndGetPatient(HttpSession session, Integer patientId) {
 		
 		// if a patient id has been passed as a parameter, update the session attribute
 		if (patientId != null) {
 			session.setAttribute("registration_patientId", patientId);
 		}
 		
 		// check to make sure the patient id session attribute has a value
 		if (session.getAttribute("registration_patientId") == null) {
 			return null;
 		}
 		
 		// fetch the patient
 		Patient patient = Context.getPatientService().getPatient((Integer) session.getAttribute("registration_patientId"));
 		if (patient == null) {
 			throw new APIException("No patient found with id " + patientId);
 		}
 			
 		return patient;
 	}
 	
 	public static Patient savePatient(Patient patient){		
 		Patient savedPatient = null;
 		if (patient!=null){
 			for (PersonAttributeType attr : Context.getPersonService().getPersonAttributeTypes(PERSON_TYPE.PATIENT, ATTR_VIEW_TYPE.VIEWING)) {
 				if (patient.getAttribute(attr) != null  && StringUtils.isBlank(patient.getAttribute(attr).getValue())) {
 					patient.removeAttribute(patient.getAttribute(attr));
 				}
 			}
 			Context.getPatientService().savePatient(patient);
 		}
 		return savedPatient;
 	}
 
 }
