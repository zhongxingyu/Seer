 package org.openmrs.module.simplelabentry.web.dwr;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.Vector;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.ConceptNumeric;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Order;
 import org.openmrs.OrderType;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.PersonAddress;
 import org.openmrs.PersonAttribute;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.PersonName;
 import org.openmrs.User;
 import org.openmrs.api.PatientIdentifierException;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.simplelabentry.SimpleLabEntryService;
 import org.openmrs.module.simplelabentry.util.SimpleLabEntryUtil;
 import org.openmrs.util.OpenmrsUtil;
 import org.openmrs.validator.PatientIdentifierValidator;
 import org.openmrs.web.dwr.DWRPatientService;
 import org.openmrs.web.dwr.PatientListItem;
 
 public class DWRSimpleLabEntryService {
 
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	// *******************
 	// Public methods
 	// *******************
 
 	/**
 	 * Retrieves a LabPatientListItem given the passed patientId
 	 * @param patientId  The patientId of the patient you wish to retrieve
 	 * @return LabPatientListItem
 	 */
 	public LabPatientListItem getPatient(Integer patientId) {
 		log.debug("Getting patient: " + patientId);
 		Patient p = Context.getPatientService().getPatient(patientId);
 		return new LabPatientListItem(p);
 	}
 	
 	/**
 	 * Retrieves a Collection of either LabPatientListItems and / or Strings containing informational messages
 	 * @param searchValue  
 	 * @return Collection<Object> containing LabPatientListItems and / or Strings containing informational messages
 	 */
 	public Collection<Object> findPatients(String searchValue) {
 		
 		Collection<Object> patientList = new Vector<Object>();
 		DWRPatientService dps = new DWRPatientService();
 		Collection<Object> pats = dps.findPatients(searchValue, false);
 		for (Object o : pats) {
 			if (o instanceof PatientListItem) {
 				PatientListItem pli = (PatientListItem) o;
 				LabPatientListItem item = getPatient(pli.getPatientId());
 				patientList.add(item);
 			}
 			else {
 				patientList.add(o);
 			}
 		}
 		return patientList;
 	}
 	
 	/**
 	 * Validates that a given identifier string is valid for a given identifier type
 	 * @param patientIdentifierTypeId - The id for the PatientIdentifierType to validate against
 	 * @param patientIdentifier - The identifier string to validate against  
 	 * @throws PatientIdentifierException if a validation error occurs
 	 */	
 	public void checkPatientIdentifier(Integer patientIdentifierTypeId, String patientIdentifier) throws PatientIdentifierException {
 		PatientIdentifierType type = Context.getPatientService().getPatientIdentifierType(patientIdentifierTypeId);
 		PatientIdentifierValidator.validateIdentifier(patientIdentifier, type);
 	}
 	
 	/**
 	 * Validates that a given identifier string is valid for a given identifier type
 	 * @param patientIdentifierTypeId - The id for the PatientIdentifierType to validate against
 	 * @param patientIdentifier - The identifier string to validate against  
 	 * @param List<String> - This collection of error messages is added to with details of any validation errors
 	 */	
 	private void checkPatientIdentifier(PatientIdentifierType type, String identifier, List<String> errors) {
 		try {
 			PatientIdentifierValidator.validateIdentifier(identifier, type);
 		}
 		catch (Exception e) {
 			errors.add(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Retrieves a LabPatientListItem for a patient that matches the given identifier / type combination
 	 * @param patientIdentifierTypeId - The id for the PatientIdentifierType to check
 	 * @param patientIdentifier - The identifier string to check
 	 * @return LabPatientListItem - The LabPatientListItem for the patient that matches the given identifier / type combination
 	 */	
 	public LabPatientListItem getPatientByIdentifier(Integer patientIdentifierTypeId, String patientIdentifier) {
 	    //TODO: add secondary identifier types to this module, same as primary care.
 		PatientService ps = Context.getPatientService();
 		PatientIdentifierType idType = ps.getPatientIdentifierType(patientIdentifierTypeId);
 		List<PatientIdentifierType> idTypeList = new java.util.ArrayList<PatientIdentifierType>();
 		String gpList = Context.getAdministrationService().getGlobalProperty("simplelabentry.patientIdentifierTypesToSearch");
 		for (StringTokenizer st = new StringTokenizer(gpList, ","); st.hasMoreTokens(); ) {
             String s = st.nextToken().trim();
             try {
                 idTypeList.add(Context.getPatientService().getPatientIdentifierType(Integer.valueOf(s)));
             } catch (Exception ex){
                 log.error("Could not load identifier type " + s + ".  Check the global property simplelabentry.patientIdentifierTypesToSearch");
             }
 		}
 		idTypeList.add(idType);
 		log.debug("Looking for patient with Identifier: " + idType.getName() + ": " + patientIdentifier);
 		List<Patient> patList = ps.getPatients(null, patientIdentifier, idTypeList, true);
 		log.debug("Found " + patList.size() + " patients: " + patList);
 		Patient p = (patList == null || patList.isEmpty() ? null : patList.get(0));
 		LabPatientListItem pli = new LabPatientListItem(p);
 		return pli;
 	}
 	
 	/**
 	 * Adds a new PatientIdentifier for the patient that matches the passed patientId
 	 * @param patientId - The patientId for the patient to add the identifier to
 	 * @param identifier - The identifier string to add
 	 * @param identifierTypeStr - The identifier type for the identifier to add
 	 * @param identifierLocationStr - The location for the identifier to add
 	 * @return LabPatientListItem - The LabPatientListItem for the patient that was changed
 	 */	
 	public LabPatientListItem addPatientIdentifier(String patientId, String identifier, String identifierTypeStr, String identifierLocationStr) {
 		
 		Patient patient = null;
 		PatientIdentifierType identifierType = null;
 		Location identifierLocation = null;
         
 		// Validate input
 		List<String> errors = new ArrayList<String>();
 		
 		if (patientId == null || "".equals(patientId)) {
 			errors.add("Patient ID is required");
 		}
 		else {
 			try {
 				patient = Context.getPatientService().getPatient(Integer.parseInt(patientId));
 				if (patient == null) {
 					errors.add("Invalid Patient ID specified.");
 				}
 			}
 			catch (Exception e) {
 				errors.add("Invalid Patient ID specified.");
 			}
 		}
 		
 		if (identifier == null || "".equals(identifier) || identifierTypeStr == null || "".equals(identifierTypeStr)) { 
 			errors.add("Identifier and Identifier Type are required. Passed values are [" + identifier + ","+ identifierTypeStr + "," + identifierLocationStr + "]");
 		}
 		else {
 		    //if patient already has this identifier but with another identiferType, don't recreate
 	        //just in case dojo patient search found patient through an identifer of another identifier type.
 	        //for example, if we find a patient through primaryCareId "I77", we don't want to 
 	        //create a local IMB ID of "I77"
 	        for (PatientIdentifier pi : patient.getActiveIdentifiers()){
 	            if (pi.getIdentifier().equals(identifier))
 	                return new LabPatientListItem(patient);
 	        }
 			try {
 				identifierType = Context.getPatientService().getPatientIdentifierType(Integer.valueOf(identifierTypeStr));
 				if (identifierType == null) {
 					errors.add("Invalid Patient Identifier Type.");
 				}
 				else {
 					checkPatientIdentifier(identifierType, identifier, errors);
 				}
 			}
 			catch (Exception e) {
 				errors.add("Invalid Patient Identifier Type.");
 			}
 			
 			try {
 				identifierLocation = Context.getLocationService().getLocation(Integer.valueOf(identifierLocationStr));
 			}
 			catch (Exception e) {}
 			if (identifierLocation == null) {
 				log.warn("No location specified for identifier, using default");
 				identifierLocation = Context.getLocationService().getLocation("Unknown Location");
 				if (identifierLocation == null) {
 					identifierLocation = Context.getLocationService().getLocation("Unknown");
 				}
 				if (identifierLocation == null) {
 					identifierLocation = Context.getLocationService().getLocation(1);
 				}
 			}
 		}
 		if (!errors.isEmpty()) {
 			String errorString = "Validation errors while trying to add patient identifier: " + errors;
 			log.error(errorString);
 			throw new RuntimeException(errorString);
 		}
 		
 		// Save identifier
 		PatientIdentifier patId = new PatientIdentifier(identifier, identifierType, identifierLocation);
 		patId.setPreferred(false);
 		patId.setPatient(patient);
 		patId.setCreator(Context.getAuthenticatedUser());
 		patId.setDateCreated(new Date());
 		patient.addIdentifier(patId);
 		patient = Context.getPatientService().savePatient(patient);
 		
 		return new LabPatientListItem(patient);
 	}
 	
 	/**
 	 * Creates a new Patient with the passed parameters
 	 * @param firstName - The patient's firstName
 	 * @param lastName - The patient's lastName
 	 * @param gender - The patient's gender
 	 * @param ageYStr - The patient's age in years
 	 * @param ageMStr - The patient's age in months
 	 * @param identifier - The patient identifier string to add
 	 * @param identifierTypeStr - The identifier type for the patient identifier to add
 	 * @param locationStr - The location for the patient identifier to add
 	 * @param province - The province for the patient address to add
 	 * @param countyDistrict - The countyDistrict for the patient address to add
 	 * @param cityVillage - The cityVillage for the patient address to add
 	 * @param neighborhoodCell - The neighborhoodCell for the patient address to add
 	 * @param address1 - The address1 for the patient address to add
 	 * @return LabPatientListItem - The LabPatientListItem for the patient that was created
 	 */	
 	public LabPatientListItem createPatient(String firstName, String lastName, String gender, String ageYStr, String ageMStr,
 								String identifier, String identifierTypeStr, String locationStr, 
 								String province, String countyDistrict, String cityVillage, String neighborhoodCell, String address1) {
 
 		PatientIdentifierType identifierType = null;
 		PersonAttributeType hcType = null;
 		Location location = null;
 		Integer ageY = null;
 		Integer ageM = null;
 		
 		List<String> errors = new ArrayList<String>();
 		
 		// Validate input
 		if (identifier == null || "".equals(identifier) || 
 			identifierTypeStr == null || "".equals(identifierTypeStr) ||
 			locationStr == null || "".equals(locationStr)) { 
 				errors.add("Identifier, Type, and Location are required. Passed values are [" + identifier + ","+ identifierTypeStr + "," + locationStr + "]");
 		}
 		else {
 			try {
 				identifierType = Context.getPatientService().getPatientIdentifierType(Integer.valueOf(identifierTypeStr));
 				if (identifierType == null) {
 					errors.add("Invalid Patient Identifier Type.");
 				}
 				else {
 					checkPatientIdentifier(identifierType, identifier, errors);
 				}
 			}
 			catch (Exception e) {
 				errors.add("Invalid Patient Identifier Type.");
 			}
 			
 			try {
 				location = Context.getLocationService().getLocation(Integer.valueOf(locationStr));
 				if (location == null) {
 					errors.add("Invalid Identifier Location.");
 				}
 			}
 			catch (Exception e) {
 				errors.add("Invalid Identifier Location.");
 			}
 			
 			try {
 				String attTypeProp = Context.getAdministrationService().getGlobalProperty("simplelabentry.patientHealthCenterAttributeType");
 				hcType = Context.getPersonService().getPersonAttributeType(Integer.valueOf(attTypeProp));
 				if (hcType == null) {
 					errors.add("Invalid Configuration of Patient Health Center Attribute Type");
 				}
 			}
 			catch (Exception e) {
 				errors.add("Invalid Configuration of Patient Health Center Attribute Type");
 			}
 		}
 
 		if (firstName == null || "".equals(firstName)) { errors.add("Given Name is required"); }
 		if (lastName == null || "".equals(lastName)) { errors.add("Family Name is required"); }
 		
 		if (gender == null || "".equals(gender)) {
 			errors.add("Gender is required"); 
 		}
 		else if (!"F".equals(gender) && !"M".equals(gender)) {
 			errors.add("Gender must be 'M' or 'F'"); 
 		}
 
 		try {
 			ageY = StringUtils.isBlank(ageYStr) ? 0 : Integer.valueOf(ageYStr);
 			ageM = StringUtils.isBlank(ageMStr) ? 0 : Integer.valueOf(ageMStr);
 			if (ageY == 0 && ageM == 0) {
 				errors.add("Age is required.");
 			}
 			else if (ageY > 120 || ageY < 0 || ageM < 0) {
 				errors.add("Age entered outside of allowed range");
 			}
 		}
 		catch (Exception e) {
 			errors.add("Invalid age entered. Age must be a whole number.");
 		}
 		
 		if (!errors.isEmpty()) {
 			String errorString = "Validation errors while trying to create a patient: " + errors;
 			log.error(errorString);
 			throw new RuntimeException(errorString);
 		}
 		
 		// Create new Patient
 		User user = Context.getAuthenticatedUser();
 		Date now = new Date();
 		Patient p = new Patient();
 		p.setPersonCreator(user);
 		p.setPersonDateCreated(now);
 		p.setPersonChangedBy(user);
 		p.setPersonDateChanged(new Date());
 
 		PersonName name = new PersonName(firstName, "", lastName);
 		name.setCreator(user);
 		name.setDateCreated(now);
 		name.setChangedBy(user);
 		name.setDateChanged(now);
 		p.addName(name);
 		
 		Calendar bdCal = Calendar.getInstance();
 		bdCal.set(Calendar.DATE, 1);
 		if (StringUtils.isBlank(ageMStr)) {
 			bdCal.set(Calendar.MONTH, Calendar.JANUARY);
 		}
 		else {
 			bdCal.add(Calendar.MONTH, (-1*ageM));
 		}
 		bdCal.add(Calendar.YEAR, (-1*ageY));
 		p.setBirthdate(bdCal.getTime());
 		p.setBirthdateEstimated(true);
 		
 		p.setGender(gender);
 		
 		PersonAttribute hcAttribute = new PersonAttribute(hcType, locationStr);
 		p.addAttribute(hcAttribute);
 		
 		PatientIdentifier patId = new PatientIdentifier(identifier, identifierType, location);
 		patId.setPreferred(true);
 		patId.setPatient(p);
 		patId.setCreator(user);
 		patId.setDateCreated(now);
 		p.addIdentifier(patId);
 		
 		PersonAddress address = new PersonAddress();
 		address.setPerson(p);
 		address.setAddress1(address1);
 		address.setStateProvince(province);
 		address.setCountyDistrict(countyDistrict);
 		address.setCityVillage(cityVillage);
 		address.setNeighborhoodCell(neighborhoodCell); 
 		address.setCreator(user);
 		address.setDateCreated(now);
 		p.addAddress(address);
 
 		Patient patient = Context.getPatientService().savePatient(p);
 		return new LabPatientListItem(patient);
 	}
 	
 	/**
 	 * Retrieves a LabOrderListItem given the passed orderId
 	 * @param orderId  The orderId of the order you wish to retrieve
 	 * @return LabOrderListItem
 	 */
 	public LabOrderListItem getOrder(Integer orderId) {
 		log.debug("Getting order: " + orderId);
 		Order o = Context.getOrderService().getOrder(orderId);
 		if (o == null) {
 			throw new RuntimeException("No order found with Order ID = " + orderId);
 		}
 		return new LabOrderListItem(o);
 	}
 	
 	/**
 	 * Saves a set of Lab Order given the passed parameters
 	 * @param orderId  The orderId of the order you wish to save
 	 * @param patientId  The patientId of the order you wish to save
 	 * @param orderConceptIds  Concept IDs for the Order
 	 * @param orderLocationStr  A String containing the Location ID for the Order
 	 * @param orderDateStr  A String containing the Order Start Date
 	 * @param accessionNumber  The accession number for the Order
 	 * @param discontinuedDateStr - A String containing the Order Discontinued Date
 	 * @param labResults - Map<String, LabResultListItem> containing a Map of concept string to LabResultListItem to save
 	 * @param resultFailureMap - Map<String, LabResultListItem> containing a Map of concept string to LabResultListItem to save
 	 * 
 	 * @return List<LabOrderListItem> - The saved LabOrders
 	 */
 	public List<LabOrderListItem> saveLabOrders(Integer orderId, Integer patientId, List<Integer> orderConceptIds, String orderLocationStr, String orderDateStr, String accessionNumber, String previousAccessionNumber, String discontinuedDateStr, Map<String, LabResultListItem> labResults, Map<String, LabResultListItem> resultFailureMap) {
 		
 		log.debug("Saving LabOrder with params: " + orderId + ", " + patientId + ", " + orderConceptIds + ", " + orderLocationStr + ", " + orderDateStr + ", " + accessionNumber + ", " + discontinuedDateStr + ", " + labResults);
 		Patient patient = null;
 		Map<Integer, Concept> orderConceptMap = null;
 		Location orderLocation = null;
 		Date orderDate = null;
 		EncounterType encounterType = null;
 		OrderType orderType = null;
 		Date discontinuedDate = null;
 		Date today = new Date();
 		Set<String> concepts = new TreeSet<String>();
         concepts.addAll(labResults.keySet());
         concepts.addAll(resultFailureMap.keySet());
 		List<String> errors = new ArrayList<String>();
 		
 		// Validate input
 		
 		if (orderConceptIds == null || orderConceptIds.isEmpty()) { 
 			errors.add("Order Type Concept is required");
 		}
 		else {
 			try {
 				orderConceptMap = new HashMap<Integer, Concept>(orderConceptIds.size());
 
 				for (Integer orderConceptId : orderConceptIds) {
 					Concept orderConcept = Context.getConceptService().getConcept(Integer.valueOf(orderConceptId));
 					if (orderConcept == null) {
 						errors.add("Order Type Concept was not found.");
 					}
 					else {
 						orderConceptMap.put(orderConceptId, orderConcept);
 					}
 				}
 			}
 			catch (Exception e) {
 				errors.add("Invalid Order Type Concept specified.");
 			}
 		}
 		
 		if (orderLocationStr == null || "".equals(orderLocationStr)) { 
 			errors.add("Order Location is required");
 		}
 		else {
 			try {
 				orderLocation = Context.getLocationService().getLocation(Integer.valueOf(orderLocationStr));
 				if (orderLocation == null) {
 					errors.add("Order Location was not found.");
 				}
 			}
 			catch (Exception e) {
 				errors.add("Invalid Order Location specified.");
 			}
 		}
 		
 		if (orderDateStr == null || "".equals(orderDateStr)) { 
 			errors.add("Order Date is required");
 		}
 		else {
 			try {
 				orderDate = Context.getDateFormat().parse(orderDateStr);
 				if (orderDate.after(today)) {
 					errors.add("Order Date cannot be in the future.");
 				}
 			}
 			catch (Exception e) {
 				errors.add("Invalid Order Date specified.");
 			}
 		}
 		
 		try {
 			String encounterTypeProp = Context.getAdministrationService().getGlobalProperty("simplelabentry.labTestEncounterType");
 			encounterType = Context.getEncounterService().getEncounterType(Integer.valueOf(encounterTypeProp));
 			if (encounterType == null) { 
 				errors.add("Encounter Type is required");
 			}
 		}
 		catch (Exception e) {
 			errors.add("Invalid Encounter Type configured.");
 		}
 		
 		try {
 			String orderTypeProp = Context.getAdministrationService().getGlobalProperty("simplelabentry.labOrderType");
 			orderType = Context.getOrderService().getOrderType(Integer.valueOf(orderTypeProp));
 			if (orderType == null) { 
 				errors.add("Order Type is required");
 			}
 		}
 		catch (Exception e) {
 			errors.add("Invalid Order Type configured.");
 		}
 		
 		if (discontinuedDateStr != null && !"".equals(discontinuedDateStr)) { 
 			try {
 				discontinuedDate = Context.getDateFormat().parse(discontinuedDateStr);
 				if (discontinuedDate.after(today)) {
 					errors.add("Result Date cannot be in the future.");
 				}
 			}
 			catch (Exception e) {
 				errors.add("Invalid Result Date specified.");
 			}
 		}
 		
 		// Patient
 		if (patientId == null) {
 			if (orderId == null) {
 				errors.add("Patient ID is required");
 			}
 		}
 		else {
 			try {
 				patient = Context.getPatientService().getPatient(Integer.valueOf(patientId));
 				if (patient == null) {
 					errors.add("Patient was not found.");
 				}
 				if (patient.isDead() && (patient.getDeathDate() == null || patient.getDeathDate().before(orderDate))) {
 					errors.add("You cannot enter an Order for a Patient who has died.");
 				}
 			}
 			catch (Exception e) {
 				errors.add("Invalid Patient ID specified.");
 			}
 		}
 		
 		// Lab Results
 		boolean hasLabResults = false;
 		for (String resultConcept : labResults.keySet()) {
 			LabResultListItem rli = labResults.get(resultConcept);
 			if (StringUtils.isNotBlank(rli.getResult())) {
 				hasLabResults = true;
 			}
 			Concept c = null;
 			try { 
 				c = Context.getConceptService().getConcept(Integer.parseInt(resultConcept)); 
 			} 
 			catch (Exception e) { }
 			if (c == null) {
 				errors.add("Lab Result has an invalid concept id = " + resultConcept);
 			}
 			else {
 				if (c.isNumeric()) {
 					ConceptNumeric cn = (ConceptNumeric) c;
 					if (StringUtils.isNotBlank(rli.getResult())) {
 						try {
 							Float result = Float.valueOf(rli.getResult());
 							if (!OpenmrsUtil.isValidNumericValue(result, cn)) {
 								errors.add("The value " + rli.getResult() + " entered for  " + cn.getName() + " is outside of it's absolute range of " + cn.getLowAbsolute() + " -> " + cn.getHiAbsolute());
 							}
 						}
 						catch (Exception e) {
 							errors.add("An invalid numeric value of " + rli.getResult() + " was entered for  " + c.getName());
 						}
 					}
 				}
 			}
 		}
 		//check for test failure
         if (!hasLabResults){
                 for (String resultConcept : resultFailureMap.keySet()){
                 LabResultListItem fli = resultFailureMap.get(resultConcept);
                 if (fli != null && fli.getResult().equals("1") || fli.getResult().equals("2") || fli.getResult().equals("3")){
                     hasLabResults = true;
                 }    
             }
         }
         
 		if (!hasLabResults && discontinuedDate != null) {
 			errors.add("You cannot enter a result date if no results have been entered.");
 		}
 		
 		// Ensure duplicate orders are not placed
 		SimpleLabEntryService ls = (SimpleLabEntryService) Context.getService(SimpleLabEntryService.class);
 		for (Concept orderConcept : orderConceptMap.values()) {
 			List<Order> existingOrders = ls.getLabOrders(orderConcept, orderLocation, orderDate, null, Arrays.asList(patient));
 			for (Order o : existingOrders) {
 				if (!o.getOrderId().equals(orderId) && StringUtils.equalsIgnoreCase(o.getAccessionNumber(), accessionNumber)) {
 					errors.add("You cannot enter an order that matches an existing order.");
 				}
 			}
 		}
 		
 		if (!errors.isEmpty()) {
 			StringBuffer errorString = new StringBuffer("Validation errors while trying to create order:\n\n");
 			for (String error : errors) {
 				errorString.append(" - " + error + "\n\n");
 			}
 			log.error(errorString);
 			throw new RuntimeException(errorString.toString());
 		}
 
 		// Validation Passed, now try to save objects
 		
 		User user = Context.getAuthenticatedUser();
 		Date now = new Date();
 		List<LabOrderListItem> labOrderListItems = new ArrayList<LabOrderListItem>();
 
 		// Use one Encoutner for all the newly created orders.
 		Encounter e = null;
 		for (Concept orderConcept : orderConceptMap.values()) {
 			// Create or Load existing Encounter and Order
 			Order o = null;
 			if (orderId == null) {
 				o = new Order();
 				o.setPatient(patient);
 				o.setOrderer(user);  // TODO: Is this what we want?
 				o.setCreator(user);
 				o.setDateCreated(now);
 				
 				if(e == null) {
 					e = new Encounter();
 					e.setEncounterType(encounterType);
 					e.setPatient(patient);
 					e.setDateCreated(now);
 					e.setCreator(user);
 					e.setProvider(user); // TODO: Is this what we want to do here?
 				}
 				
 				o.setEncounter(e);
 				e.addOrder(o);
 			}
 			else {
 				o = Context.getOrderService().getOrder(orderId);
 			}
 			
 			e = o.getEncounter();
 			e.setEncounterDatetime(orderDate);
 			e.setLocation(orderLocation);
 			
 			o.setOrderType(orderType);
 			o.setConcept(orderConcept);
 			o.setAccessionNumber(accessionNumber);
 			if (previousAccessionNumber != null && !previousAccessionNumber.equals(""))
 			o.setInstructions(previousAccessionNumber);
 			o.setStartDate(orderDate); // TODO: Confirm this
 			o.setDiscontinuedDate(discontinuedDate);
 			if (discontinuedDate != null) {
 				o.setDiscontinued(true);
 				o.setDiscontinuedBy(user);
 			}
 			else {
 				o.setDiscontinued(false);
 				o.setDiscontinuedBy(null);
 			}
 			
 			
 			
 			// Lab Results
 			
 			for (String resultConcept : concepts) {
 				LabResultListItem rli = labResults.get(resultConcept);
 				LabResultListItem fli = resultFailureMap.get(resultConcept);
 				boolean needToAdd = true;
 				for (Obs obs : e.getObs()) {
 					if (obs.getConcept().getConceptId().toString().equals(resultConcept)) {
 						String previousResult = LabResultListItem.getValueStringFromObs(obs);  //can return a null value
 						String previousFailureResult = LabResultListItem.getFailureCodeStringFromObs(obs);  //can't return a null value only 0,1,2,3
 						if (OpenmrsUtil.nullSafeEquals(previousResult, rli == null ? null : rli.getResult()) && OpenmrsUtil.nullSafeEquals(previousFailureResult, fli == null ? "0" : fli.getResult())) {
 							needToAdd = false;	
						} else {
 						    obs.setVoided(true);
 							obs.setVoidedBy(user);
 						}
 					}
 				}
 				if (needToAdd) {
 					Obs newObs = new Obs();
 					newObs.setConcept(Context.getConceptService().getConcept(Integer.parseInt(resultConcept)));
 					newObs.setEncounter(e);
 					newObs.setObsDatetime(e.getEncounterDatetime());
 					newObs.setOrder(o);
 					newObs.setPerson(e.getPatient());
 					LabResultListItem.setObsFromValueString(newObs, rli == null ? null : rli.getResult(), fli == null ?  null : fli.getResult());
 					newObs.setAccessionNumber(accessionNumber);
 					newObs.setCreator(user);
 					newObs.setDateCreated(now);
 					newObs.setUuid(UUID.randomUUID().toString());
 					//only add obs if either value is set, or failure recorded in comment:
 					if (newObs.getComment() != null || !newObs.getValueAsString(Context.getLocale()).equals("") || newObs.getValueModifier() != null)
 					    e.addObs(newObs);
 					log.debug("Added obs: " + newObs);
 				}
 			}
 	
 			labOrderListItems.add(new LabOrderListItem(o));
 		}
 		// MLH FIXME TODO HACK Make sure each Obs has a UUID before calling save 
 		// on the Encounter.  See: http://dev.openmrs.org/ticket/2357
 		for(Obs o : e.getAllObs()) {
 			if(o.getUuid() == null) {
 				o.setUuid(UUID.randomUUID().toString());
 			}
 		}
 		Context.getEncounterService().saveEncounter(e);
 		return labOrderListItems;
 	}
 	
 	/**
 	 * Voids a LabOrder and related Encounter if it has no orders given a 
 	 * passed Order ID and reason.
 	 * @param orderId  The orderId of the order you wish to void
 	 * @param reason  The reason why the order is being voided
 	 * @return LabOrderListItem
 	 */
 	public void deleteLabOrderAndEncounter(String orderId, String reason) {
 		Order o = Context.getOrderService().getOrder(Integer.valueOf(orderId));
 		boolean voidContext = o.getEncounter().getOrders().size() <= 1;
 		if (reason == null || reason.equals("")) {
 			reason = " ";
 		}
 		Context.getOrderService().voidOrder(o, reason);
 		if(voidContext)
 			Context.getEncounterService().voidEncounter(o.getEncounter(), reason);
 	}
 	
 	
 	public LabOrderListItem getPreviousOrders(Integer patientId){
 	    LabOrderListItem ret = new LabOrderListItem();
 	    ret.setPatientId(patientId);
 	    ret.setLabOrderIdsForPatient(SimpleLabEntryUtil.getLabOrderIDsByPatient(Context.getPatientService().getPatient(patientId), 6));
 	    return ret;
 	}
 }
