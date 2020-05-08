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
 
 package org.openmrs.module.iqchartimport;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.DrugOrder;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.PatientProgram;
 import org.openmrs.PersonAddress;
 import org.openmrs.PersonName;
 import org.openmrs.Program;
 import org.openmrs.api.APIException;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.iqchartimport.iq.IQChartSession;
 import org.openmrs.module.iqchartimport.iq.IQPatient;
 import org.openmrs.module.iqchartimport.iq.code.ExitCode;
 import org.openmrs.module.iqchartimport.iq.code.HIVStatusPartCode;
 import org.openmrs.module.iqchartimport.iq.code.MaritalCode;
 import org.openmrs.module.iqchartimport.iq.code.ModeCode;
 import org.openmrs.module.iqchartimport.iq.code.SexCode;
 import org.openmrs.module.iqchartimport.iq.code.TBScreenCode;
 import org.openmrs.module.iqchartimport.iq.code.TransferCode;
 import org.openmrs.module.iqchartimport.iq.code.WHOStageCode;
 import org.openmrs.module.iqchartimport.iq.model.Pregnancy;
 import org.openmrs.module.iqchartimport.iq.model.Regimen;
 import org.openmrs.module.iqchartimport.iq.model.TBMedication;
 import org.openmrs.module.iqchartimport.iq.model.TBTreatment;
 import org.openmrs.module.iqchartimport.iq.obs.BaseIQObs;
 import org.openmrs.module.iqchartimport.iq.obs.CD4Obs;
 import org.openmrs.module.iqchartimport.iq.obs.HeightObs;
 import org.openmrs.module.iqchartimport.iq.obs.TBScreenObs;
 import org.openmrs.module.iqchartimport.iq.obs.WHOStageObs;
 import org.openmrs.module.iqchartimport.iq.obs.WeightObs;
 
 /**
  * Builder which creates OpenMRS entities from IQChart objects equivalents
  */
 public class EntityBuilder {
 
 	protected static final Log log = LogFactory.getLog(EntityBuilder.class);
 	
 	private IQChartSession session;
 	private EntityCache cache = new EntityCache();
 	
 	/**
 	 * Creates a new entity builder
 	 * @param session the IQChart session to load data from
 	 */
 	public EntityBuilder(IQChartSession session) {
 		this.session = session;
 	}
 	
 	/**
 	 * Gets the TRACnetID identifier type to use for imported patients. Depending on module
 	 * options this will load existing identifier type or create a new one.
 	 * @return the identifier type
 	 * @throws IncompleteMappingException if mappings are not configured properly
 	 */
 	public PatientIdentifierType getTRACnetIDType() {
 		int tracnetIDTypeId = Mappings.getInstance().getTracnetIDTypeId();
 		if (tracnetIDTypeId > 0)
 			return Context.getPatientService().getPatientIdentifierType(tracnetIDTypeId);	
 		else
 			throw new IncompleteMappingException();
 	}
 	
 	/**
 	 * Gets the HIV program to use for imported patients
 	 * @return the HIV program
 	 * @throws IncompleteMappingException if mappings are not configured properly
 	 */
 	public Program getHIVProgram() {
 		int hivProgramId = Mappings.getInstance().getHIVProgramId();
 		if (hivProgramId > 0)
 			return Context.getProgramWorkflowService().getProgram(hivProgramId);	
 		else
 			throw new IncompleteMappingException();
 	}
 	
 	/**
 	 * Gets the TB program to use for imported patients
 	 * @return the TB program
 	 * @throws IncompleteMappingException if mappings are not configured properly
 	 */
 	public Program getTBProgram() {
 		int tbProgramId = Mappings.getInstance().getTBProgramId();
 		if (tbProgramId > 0)
 			return Context.getProgramWorkflowService().getProgram(tbProgramId);	
 		else
 			throw new IncompleteMappingException();
 	}
 	
 	/**
 	 * Gets the site location for imported encounters, identifiers etc
 	 * @return the location
 	 * @throws IncompleteMappingException if mappings are not configured properly
 	 */
 	public Location getSiteLocation() {
 		int siteLocationId = Mappings.getInstance().getSiteLocationId();
 		if (siteLocationId > 0)
 			return cache.getLocation(siteLocationId);	
 		else
 			throw new IncompleteMappingException();
 	}
 	
 	/**
 	 * Gets a patient by TRACnet ID
 	 * @param tracnetID the TRACnet ID
 	 * @return the patient
 	 */
 	public Patient getPatient(int tracnetID) {
 		IQPatient iqPatient = session.getPatient(tracnetID);
 		return iqPatient != null ? convertPatient(iqPatient) : null;
 	}
 	
 	/**
 	 * Gets OpenMRS patient objects for each patient in IQChart 
 	 * @return the patients
 	 * @throws APIException if module is not configured properly
 	 */
 	public List<Patient> getPatients() {
 		List<Patient> patients = new ArrayList<Patient>();
 		
 		for (IQPatient iqPatient : session.getPatients()) {
 			patients.add(convertPatient(iqPatient));
 		}
 		
 		return patients;
 	}
 	
 	/**
 	 * Gets the patient's programs
 	 * @param patient the patient
 	 * @param tracnetID the patient TRACnet ID
 	 * @return the patient programs
 	 */
 	public List<PatientProgram> getPatientPrograms(Patient patient, int tracnetID) {		
 		List<PatientProgram> patientPrograms = new ArrayList<PatientProgram>();
 		IQPatient iqPatient = session.getPatient(tracnetID);
 		
 		if (iqPatient.getEnrollDate() != null) {
 			// Get HIV program to use
 			Program hivProgram = getHIVProgram();
 			
 			PatientProgram hivPatientProgram = new PatientProgram();
 			hivPatientProgram.setPatient(patient);
 			hivPatientProgram.setProgram(hivProgram);
 			hivPatientProgram.setDateEnrolled(iqPatient.getEnrollDate());
 			hivPatientProgram.setDateCompleted(iqPatient.getExitDate());
 			patientPrograms.add(hivPatientProgram);
 		}
 		
 		List<TBTreatment> tbTreatments = session.getPatientTBTreatments(iqPatient);
 		if (tbTreatments.size() > 0) {
 			// Get TB program
 			Program tbProgram = getTBProgram();
 			
 			for (TBTreatment tbTreatment : tbTreatments) {
 				PatientProgram tbPatientProgram = new PatientProgram();
 				tbPatientProgram.setPatient(patient);
 				tbPatientProgram.setProgram(tbProgram);
 				tbPatientProgram.setDateEnrolled(tbTreatment.getStartDate());
 				tbPatientProgram.setDateCompleted(tbTreatment.getEndDate());
 				patientPrograms.add(tbPatientProgram);
 			}
 		}
 		
 		return patientPrograms;
 	}
 	
 	/**
 	 * Gets the patient's encounters
 	 * @param patient the patient
 	 * @param tracnetID the patient's TRACnet ID
 	 * @return the encounters
 	 */
 	public List<Encounter> getPatientEncounters(Patient patient, int tracnetID) {
 		IQPatient iqPatient = session.getPatient(tracnetID);
 		Map<Date, Encounter> encounters = new TreeMap<Date, Encounter>();
 		
 		makePatientInitialEncounter(patient, tracnetID, encounters);
 		makePatientObsEncounters(patient, tracnetID, encounters);
 		
 		if (iqPatient.getExitDate() != null)
 			makePatientExitEncounter(patient, tracnetID, encounters);
 		
 		addPregnancyObsToEncounters(patient, tracnetID, encounters);
 		
 		// Create sorted list
 		List<Encounter> sorted = new ArrayList<Encounter>();
 		for (Date date : encounters.keySet())
 			sorted.add(encounters.get(date));
 				
 		return sorted;
 	}
 	
 	/**
 	 * Gets the patient's drug orders
 	 * @param patient the patient
 	 * @param tracnetID the patient's TRACnet ID
 	 * @return the drug orders
 	 */
 	public List<DrugOrder> getPatientDrugOrders(Patient patient, int tracnetID) {
 		List<DrugOrder> drugOrders = new ArrayList<DrugOrder>();
 		IQPatient iqPatient = session.getPatient(tracnetID);
 		
 		// Get ARV regimens from IQChart and convert to OpenMRS drug orders
 		List<Regimen> iqRegimens = session.getPatientRegimens(iqPatient);
 		for (Regimen regimen : iqRegimens) {
 			
 			// Map regimen components to OpenMRS drug concepts
 			Integer[] drugConceptIds = DrugMapping.getRegimenConceptIds(regimen.getRegimen());
 			
 			Concept conceptDiscontinued = null;
 			if (regimen.getChangeCode() != null)
 				conceptDiscontinued = cache.getConcept(regimen.getChangeCode().mappedConcept);
 			
 			// Create order for each drug
 			for (Integer drugConceptId : drugConceptIds) {
 				Concept drugConcept = cache.getConcept(drugConceptId);
 				DrugOrder order = new DrugOrder();
 				order.setOrderType(cache.getOrderType(1));
 				order.setPatient(patient);
 				order.setDrug(null);
 				order.setConcept(drugConcept);
 				order.setStartDate(regimen.getStartDate());
 				order.setDiscontinued(regimen.getEndDate() != null);
 				order.setDiscontinuedDate(regimen.getEndDate());
 				order.setDiscontinuedReason(conceptDiscontinued);
 				order.setDiscontinuedReasonNonCoded(regimen.getOtherDetails());
 				order.setVoided(false);
 				
 				drugOrders.add(order);
 			}
 		}
 		
 		// Get TB medications from IQChart and convert to OpenMRS drug orders (using concepts rather than actual drugs objects)
 		List<TBMedication> iqTBMedications = session.getPatientTBMedications(iqPatient);
 		for (TBMedication tbMedication : iqTBMedications) {
 			
 			Integer drugConceptId = DrugMapping.getDrugConceptId(tbMedication.getDrug());
 			if (drugConceptId == null)
 				throw new IncompleteMappingException("Missing TB drug: " + tbMedication.getDrug());
 		
 			DrugOrder order = new DrugOrder();
 			order.setOrderType(cache.getOrderType(1));
 			order.setPatient(patient);
 			order.setConcept(cache.getConcept(drugConceptId));
 			order.setStartDate(tbMedication.getStartDate());
 			order.setDiscontinued(tbMedication.getEndDate() != null);
 			order.setDiscontinuedDate(tbMedication.getEndDate());
 			order.setVoided(false);
 			
 			drugOrders.add(order);
 		}
 		
 		return drugOrders;
 	}
 
 	/**
 	 * Makes the initial encounter for a patient
 	 * @param patient the patient
 	 * @param tracnetID the patient TRACnet ID
 	 * @param encounters the existing encounters
 	 */
 	protected void makePatientInitialEncounter(Patient patient, int tracnetID, Map<Date, Encounter> encounters) {	
 		IQPatient iqPatient = session.getPatient(tracnetID);
 		Encounter encounter = encounterForDate(patient, iqPatient.getEnrollDate(), encounters);
 		
 		// Add 'civil status' obs
 		if (iqPatient.getMaritalStatusCode() != null) {
 			Concept conceptCivil = cache.getConcept(MaritalCode.mappedQuestion);
 			Concept conceptAns = cache.getConcept(iqPatient.getMaritalStatusCode().mappedAnswer);		
 			Obs obs = makeObs(patient, iqPatient.getEnrollDate(), conceptCivil);
 			obs.setValueCoded(conceptAns);
 			encounter.addObs(obs);
 		}
 		
 		// Add 'mode of admission' obs
 		if (iqPatient.getModeCode() != null) {		
 			Concept conceptEnroll = cache.getConcept(ModeCode.mappedQuestion);
 			Concept conceptMode = cache.getConcept(iqPatient.getModeCode().mappedAnswer);		
 			Obs obs = makeObs(patient, iqPatient.getEnrollDate(), conceptEnroll);
 			obs.setValueCoded(conceptMode);
 			encounter.addObs(obs);
 		}
 		
 		// Add 'transfer in' obs
 		if (iqPatient.getTransferCode() != null) {
 			Concept conceptTransferIn = cache.getConcept(TransferCode.mappedQuestion);
 			Concept conceptAns = cache.getConcept(iqPatient.getTransferCode().mappedAnswer);
 			Obs obs = makeObs(patient, iqPatient.getEnrollDate(), conceptTransferIn);
 			obs.setValueCoded(conceptAns);
 			encounter.addObs(obs);
 		}
 		
 		// Add 'partner HIV status' obs
 		if (iqPatient.getHIVStatusPartCode() != null) {
 			Concept conceptPartStatus = cache.getConcept(HIVStatusPartCode.mappedQuestion);
 			Concept conceptAns = cache.getConcept(iqPatient.getTransferCode().mappedAnswer);
 			Obs obs = makeObs(patient, iqPatient.getEnrollDate(), conceptPartStatus);
 			obs.setValueCoded(conceptAns);
 			encounter.addObs(obs);
 		}
 	}
 	
 	/**
 	 * Makes the obs encounters for a patient
 	 * @param patient the patient
 	 * @param tracnetID the patient TRACnet ID
 	 * @param encounters the existing encounters
 	 */
 	protected void makePatientObsEncounters(Patient patient, int tracnetID, Map<Date, Encounter> encounters) {
 		IQPatient iqPatient = session.getPatient(tracnetID);
 		List<BaseIQObs> iqObss = session.getPatientObs(iqPatient);
 		
 		for (BaseIQObs iqObs : iqObss) {
 			Encounter encounter = encounterForDate(patient, iqObs.getDate(), encounters);		
 			Obs obs = makeObs(patient, iqObs.getDate(), null);
 			
 			if (iqObs instanceof HeightObs) {
 				obs.setConcept(cache.getConcept("@concept.height"));
 				obs.setValueNumeric((double)((HeightObs)iqObs).getHeight());		
 			}
 			else if (iqObs instanceof WeightObs) {
 				obs.setConcept(cache.getConcept("@concept.weight"));
 				obs.setValueNumeric((double)((WeightObs)iqObs).getWeight());		
 			}
 			else if (iqObs instanceof CD4Obs) {
 				obs.setConcept(cache.getConcept("@concept.cd4_count"));
 				obs.setValueNumeric((double)((CD4Obs)iqObs).getCd4Count());		
 			}
 			else if (iqObs instanceof TBScreenObs) {
 				Concept conceptAns = cache.getConcept(((TBScreenObs)iqObs).getCode().mappedAnswer);
 				if (conceptAns != null) {
 					obs.setConcept(cache.getConcept(TBScreenCode.mappedQuestion));
 					obs.setValueCoded(conceptAns);
 				}
 			}
 			else if (iqObs instanceof WHOStageObs) {
 				String[] ageDepStages = (String[]) ((WHOStageObs)iqObs).getStage().mappedAnswer;
 				int age = patient.getAge(iqObs.getDate());
 				boolean isPediatric = (age < Constants.ADULT_START_AGE);
 				String stage = isPediatric ? ageDepStages[0] : ageDepStages[1];
 				
 				obs.setConcept(cache.getConcept(WHOStageCode.mappedQuestion));
 				obs.setValueCoded(cache.getConcept(stage));
 			}
 
 			if (obs.getConcept() != null)
 				encounter.addObs(obs);
 		}
 	}
 	
 	/**
 	 * Makes the exit encounter for a patient
 	 * @param patient the patient
 	 * @param tracnetID the patient TRACnet ID
 	 * @param encounters the existing encounters
 	 */
 	protected void makePatientExitEncounter(Patient patient, int tracnetID, Map<Date, Encounter> encounters) {
 		IQPatient iqPatient = session.getPatient(tracnetID);
 		Encounter encounter = encounterForDate(patient, iqPatient.getExitDate(), encounters);
 		
 		// Add 'exit reason' obs
 		if (iqPatient.getExitCode() != null) {		
 			Concept conceptExited = cache.getConcept(ExitCode.mappedQuestion);
 			Concept conceptReason = cache.getConcept(iqPatient.getExitCode().mappedAnswer);
 			
 			Obs obs = makeObs(patient, iqPatient.getExitDate(), conceptExited);
 			obs.setValueCoded(conceptReason);
 			encounter.addObs(obs);
 		}
 	}
 	
 	/**
 	 * Adds pregnancy obs to all encounters
 	 * @param patient the patient
 	 * @param tracnetID the patient's TRACnet ID
 	 * @param encounters the patient's encounters
 	 */
 	protected void addPregnancyObsToEncounters(Patient patient, int tracnetID, Map<Date, Encounter> encounters) {
 		IQPatient iqPatient = session.getPatient(tracnetID);
 		List<Pregnancy> pregnancies = session.getPatientPregnancies(iqPatient);
 		Concept conceptPregnancy = cache.getConcept("PATIENT PREGNANCY STATUS");
 		Concept conceptYes = cache.getConcept(Dictionary.YES);
 		Concept conceptNo = cache.getConcept(Dictionary.NO);
 		Concept conceptNA = cache.getConcept(Dictionary.NOT_APPLICABLE);
 		
 		for (Date date : encounters.keySet()) {
 			Obs obs = makeObs(patient, date, conceptPregnancy);
 			
 			// Pregnancy = not applicable for all male patients
 			if (iqPatient.getSexCode() == SexCode.MALE)
 				obs.setValueCoded(conceptNA);
 			else {
 				// Does date fall inside the range of a incomplete pregnancy record (one without an end date)
 				for (Pregnancy pregnancy : pregnancies) {
 					if (pregnancy.getDateEnd() == null && date.after(pregnancy.getDateStart()) && date.before(pregnancy.getEstDelivery())) {
 						// Don't add any obs... because we just don't know what happened
						continue;
 					}
 				}
 				
 				// Check each of patient's pregnancies 
 				boolean pregOnDate = false;
 				for (Pregnancy pregnancy : pregnancies) {
 					if (date.after(pregnancy.getDateStart()) && date.before(pregnancy.getDateEnd())) {
 						pregOnDate = true;
 						break;
 					}
 				}
 				
 				obs.setValueCoded(pregOnDate ? conceptYes : conceptNo);
 			}
 			encounters.get(date).addObs(obs);
 		}
 	}
 	
 	/**
 	 * Converts an IQChart patient to an OpenMRS patient
 	 * @param iqPatient the IQChart patient
 	 * @return the OpenMRS patient
 	 * @throws IncompleteMappingException if mapping is not configured properly
 	 */
 	protected Patient convertPatient(IQPatient iqPatient) {
 		// Get TRACnet ID identifier type
 		PatientIdentifierType tracnetIDType = getTRACnetIDType();
 		
 		Patient patient = new Patient();
 		
 		// Create TRACnet identifier
 		PatientIdentifier tracnetID = new PatientIdentifier();
 		tracnetID.setIdentifier("" + iqPatient.getTracnetID());
 		tracnetID.setIdentifierType(tracnetIDType);
 		tracnetID.setLocation(getSiteLocation());
 		tracnetID.setPreferred(true);
 		patient.addIdentifier(tracnetID);
 		
 		// Create name object
 		PersonName name = new PersonName();
 		name.setGivenName(iqPatient.getFirstName());
 		name.setFamilyName(iqPatient.getLastName());
 		patient.addName(name);
 		
 		// Create address object using AddressHierarchRwanda mappings
 		String province = Mappings.getInstance().getAddressProvince();
 		String country = Context.getAdministrationService().getGlobalProperty(Constants.PROP_ADDRESS_COUNTRY);
 		PersonAddress address = new PersonAddress();
 		address.setNeighborhoodCell(iqPatient.getCellule());
 		address.setCityVillage(iqPatient.getSector());
 		address.setCountyDistrict(iqPatient.getDistrict());
 		address.setStateProvince(province);
 		address.setCountry(country);
 		patient.addAddress(address);
 		
 		// Set patient gender
 		if (iqPatient.getSexCode() != null)
 			patient.setGender((String)iqPatient.getSexCode().mappedValue);
 		
 		// Set patient birth date
 		patient.setBirthdate(iqPatient.getDob());
 		patient.setBirthdateEstimated(iqPatient.isDobEstimated());
 		
 		// Set living/dead
 		if (iqPatient.getExitCode() != null && iqPatient.getExitCode() == ExitCode.DECEASED)
 			patient.setDead(true);
 		
 		return patient;
 	}
 	
 	/**
 	 * Gets an encounter for the given day - if one exists it is returned
 	 * @param patient the patient
 	 * @param date the day
 	 * @param encounters the existing encounters
 	 * @return the encounter
 	 */
 	protected Encounter encounterForDate(Patient patient, Date date, Map<Date, Encounter> encounters) {
 		// Look for existing encounter on that date
 		if (encounters.containsKey(date)) 
 			return encounters.get(date);
 		
 		// If no existing, then this will be an initial encounter
 		boolean isInitial = encounters.isEmpty();
 		
 		// Create new one
 		Encounter encounter = new Encounter();
 		encounter.setEncounterType(getEncounterType(patient, date, isInitial));
 		encounter.setLocation(getSiteLocation());
 		encounter.setProvider(MappingUtils.getEncounterProvider());
 		encounter.setEncounterDatetime(date);
 		encounter.setPatient(patient);
 		
 		// Store in encounter map and return
 		encounters.put(date, encounter);
 		return encounter;
 	}
 	
 	/**
 	 * Gets the encounter type to use for imported obs
 	 * @param patient the patient
 	 * @param date the encounter date
 	 * @param initial whether this is an initial encounter
 	 * @return the encounter type
 	 * @throws IncompleteMappingException if mappings are not configured properly
 	 */
 	protected EncounterType getEncounterType(Patient patient, Date date, boolean isInitial) {
 		// Calc patient age at time of encounter
 		int age = patient.getAge(date);
 		boolean isPediatric = (age < Constants.ADULT_START_AGE);
 		
 		if (isInitial && isPediatric)
 			return cache.getEncounterType("PEDSINITIAL");
 		else if (isInitial && !isPediatric)
 			return cache.getEncounterType("ADULTINITIAL");
 		else if (!isInitial && isPediatric)
 			return cache.getEncounterType("PEDSRETURN");
 		else
 			return cache.getEncounterType("ADULTRETURN");
 	}
 	
 	/**
 	 * Helper method to create a new obs
 	 * @param patient the patient
 	 * @param date the date
 	 * @param concept the concept
 	 * @return the obs
 	 */
 	protected Obs makeObs(Patient patient, Date date, Concept concept) {
 		Obs obs = new Obs();
 		obs.setPerson(patient);
 		obs.setLocation(getSiteLocation());
 		obs.setObsDatetime(date);
 		obs.setConcept(concept);
 		return obs;
 	}
 }
