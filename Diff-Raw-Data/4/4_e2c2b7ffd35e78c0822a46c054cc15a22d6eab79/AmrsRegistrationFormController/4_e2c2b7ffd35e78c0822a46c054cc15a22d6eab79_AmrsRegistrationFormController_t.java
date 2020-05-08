 package org.openmrs.module.amrsregistration.web.controller;
 
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.Person;
 import org.openmrs.PersonAddress;
 import org.openmrs.PersonAttribute;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.PersonName;
 import org.openmrs.Relationship;
 import org.openmrs.RelationshipType;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.PatientIdentifierException;
 import org.openmrs.api.PersonService;
 import org.openmrs.api.PersonService.ATTR_VIEW_TYPE;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.amrsregistration.AmrsRegistration;
 import org.openmrs.module.amrsregistration.AmrsSearchManager;
 import org.openmrs.propertyeditor.ConceptEditor;
 import org.openmrs.propertyeditor.LocationEditor;
 import org.openmrs.propertyeditor.PatientIdentifierTypeEditor;
 import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
 import org.openmrs.validator.PatientIdentifierValidator;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.beans.propertyeditors.CustomNumberEditor;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.ModelAndViewDefiningException;
 import org.springframework.web.servlet.mvc.AbstractWizardFormController;
 import org.springframework.web.servlet.view.RedirectView;
 
 // Note: The wizard controller workflow for form submission:
 // - Get current page
 // - Process cancel
 // - Process finish
 // - Validate command object
 // - Post processing
 // - Get target page
 // - Redirect to current page when there's error
 // - Redirect to target page when there's no error
 // There are several alternatives to perform validation on things that are not in the command object:
 // First Way
 //  - Process inside onBindAndValidate
 // 
 
 // TODO: externalize string to the constant class
 public class AmrsRegistrationFormController extends AbstractWizardFormController {
 	
     private Log log = LogFactory.getLog(AmrsRegistrationFormController.class);
     private PatientIdentifierValidator validator = new PatientIdentifierValidator() {
         public void validate(Object obj, Errors errors) {
             PatientIdentifier pi = (PatientIdentifier) obj;
             try {
                 validateRequiredFields(pi);
                 validateIdentifier(pi);
             }
             catch (Exception e) {
                 log.error(e.getMessage());
                 errors.reject(e.getMessage());
             }
         }
         public void validateRequiredFields(PatientIdentifier pi) throws PatientIdentifierException {
             if (pi.getIdentifierType() == null || pi.getIdentifierType().getPatientIdentifierTypeId() == null ||
                     Context.getPatientService().getPatientIdentifierType(pi.getIdentifierType().getPatientIdentifierTypeId()) == null) {
                 throw new PatientIdentifierException("amrsregistration.page.edit.invalidIdentifierType");
             }
             if (!AmrsRegistrationConstants.AMRS_TARGET_ID.equals(pi.getIdentifierType().getName()) &&
                     (pi.getLocation() == null || pi.getLocation().getLocationId() == null ||
                     Context.getLocationService().getLocation(pi.getLocation().getLocationId()) == null)) {
                     throw new PatientIdentifierException("amrsregistration.page.edit.invalidIdentifierLocation");
             }
         }
     };
 	
 	public AmrsRegistrationFormController() {
 	}
 	
 	protected Object formBackingObject(HttpServletRequest request) throws ModelAndViewDefiningException {
 		AmrsRegistration amrsRegistration = new AmrsRegistration();
 		amrsRegistration.setPatient(getNewPatient());
 		return amrsRegistration;
 	}
 	
 	/**
 	 * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors, int)
 	 */
 	@SuppressWarnings("unchecked")
     @Override
 	protected Map<String, Object> referenceData(HttpServletRequest request, Object command, Errors errors, int page) throws Exception {
 		HashMap<String, Object> localHashMap = new HashMap<String, Object>();
 		
 		// on first page, show error when the registration identifier type is not defined
 		if (page != AmrsRegistrationConstants.START_PAGE) {
 			// send the target identifier to all pages that need it
 			localHashMap.put("amrsIdType", AmrsRegistrationConstants.AMRS_TARGET_ID);
 			if (page == AmrsRegistrationConstants.EDIT_PAGE || page == AmrsRegistrationConstants.ASSIGN_ID_PAGE) {
 				localHashMap.put("maxReturned", AmrsSearchManager.MAX_RETURNED_PATIENTS);
 				// initial binding for the template for identifier, name, and address
 				localHashMap.put("emptyIdentifier", new PatientIdentifier());
 				localHashMap.put("emptyName", new PersonName());
 				localHashMap.put("emptyAddress", new PersonAddress());
 				// add new template for relationship
 				localHashMap.put("emptyRelationship", new Relationship());
 				
 				// used to flag down whether to show the patient's attributes or not
 				List<PersonAttributeType> attributeTypes = Context.getPersonService().getPersonAttributeTypes(PERSON_TYPE.PATIENT, ATTR_VIEW_TYPE.LISTING);
 				localHashMap.put("displayAttributes", attributeTypes.size() > 0);
 				
 				// on edit page, we also need to show the potential matches
 				// this is used when you are coming back from other page
 				// such as:
 				// - submitting the form but there are errors on the form
 				// - coming back from assign id or review page
 				String idCard = ServletRequestUtils.getStringParameter(request, "idCardInput", null);
 				String amrsId = ServletRequestUtils.getStringParameter(request, "amrsIdentifier", null);
 				if (idCard == null && amrsId == null) {
 					
 					AmrsRegistration amrsRegistration = (AmrsRegistration) command;
 					Patient patient = amrsRegistration.getPatient();
 					
 					AmrsSearchManager searchManager = new AmrsSearchManager();
 					
 					Set<Patient> matches = new HashSet<Patient>();
 					Set<Patient> currentPatient = (Set<Patient>) request.getAttribute("potentialMatches");
 					if (currentPatient != null && currentPatient.size() > 0)
 						matches.addAll(currentPatient);
 					
 					List<Patient> patients = searchManager.getPatients(patient.getPersonName(),
 						patient.getPersonAddress(), patient.getPatientIdentifier(), patient.getAttributes(), patient.getGender(),
 						patient.getBirthdate(), patient.getAge(), 10);
 					
 					for (Patient p : patients)
 		                matches.add(p);
 					
 					request.setAttribute("potentialMatches", matches);
 					request.setAttribute("selectionOnly", Boolean.FALSE);
 				}
 			}
 		} else {
 			// if you're on the start page, then show this errors when the target identifier type is not defined
 			PatientIdentifierType type = Context.getPatientService().getPatientIdentifierTypeByName(AmrsRegistrationConstants.AMRS_TARGET_ID);
 			if (type == null) {
 				errors.reject("amrsregistration.page.start.undefinedTarget",
 					new Object[]{AmrsRegistrationConstants.AMRS_TARGET_ID},
 					"Target identifier is not specified");
 				errors.reject("amrsregistration.page.start.targetSpecify",
 					new Object[]{AmrsRegistrationConstants.AMRS_TARGET_ID_KEY},
 					"Please specify target in the global properties");
 			}
 		}
 		
 		return localHashMap;
 	}
 	
 	/**
      * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#postProcessPage(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors, int)
      */
     @Override
     protected void postProcessPage(HttpServletRequest request, Object command, Errors errors, int page) throws Exception {
     	AmrsRegistration amrsRegistration = (AmrsRegistration) command;
     	Patient patient = amrsRegistration.getPatient();
     	
     	// post process each page that needs processing (have a form that is in the spring's command object).
     	if (page == AmrsRegistrationConstants.EDIT_PAGE) {
 
     		// don't process form from page two if you select a patient from the potential matches pop-up
 			String idCard = ServletRequestUtils.getStringParameter(request, "patientIdInput", null);
     		if (idCard == null) {
         		// get a list of person attribute that belong to the patient
         		List<PersonAttributeType> attributeTypes = Context.getPersonService().getPersonAttributeTypes(PERSON_TYPE.PATIENT, ATTR_VIEW_TYPE.LISTING);
         		
         		// remove the 
         		for (PersonAttribute attribute : patient.getAttributes()) {
         			Integer id = attribute.getAttributeType().getPersonAttributeTypeId();
                     String value = ServletRequestUtils.getStringParameter(request, String.valueOf(id), null);
                     if (value != null) {
                     	attribute.setValue(value);
                     	attributeTypes.remove(attribute.getAttributeType());
                     }
                 }
         		
         		// iterate over what is left in the list to see whether the user add a new element or not
         		for (PersonAttributeType personAttributeType : attributeTypes) {
         			Integer id = personAttributeType.getPersonAttributeTypeId();
                     String value = ServletRequestUtils.getStringParameter(request, String.valueOf(id), StringUtils.EMPTY);
                     if (value != null && value.length() > 0) {
                         PersonAttribute attribute = new PersonAttribute();
                         attribute.setAttributeType(personAttributeType);
                         attribute.setValue(value);
                         patient.addAttribute(attribute);
                     }
                 }
 
         		// Preferred Handling (see #1663) --> The following docs also valid for identifier and address
         		// Since the preferred handling is still buggy on trunk, so I wrote my own way of handling preferred
         		// So here it goes:
         		
         		// Get the preferred value of the preferred from the list
         		// The value will be 1 or 2 or 3 ... or n, where n is the position of the name in the set
         		// The names implementation is using TreeSet, so names in a person object always will have the same order
         		String namePreferred = ServletRequestUtils.getStringParameter(request, "namePreferred", StringUtils.EMPTY);
         		int selectedName = NumberUtils.toInt(namePreferred);
         		
         		// flag whether the preferred name is one of the name in the person object or a new PersonName object
         		boolean preferredNameCreated = true;
         		if (selectedName < patient.getNames().size()) {
         			// Preferred is one of the name in the person, iterate the name and move the preferred correct position.
         			// This is a blind assignment of preferred without checking whether the preferred is moved from one name
         			// to another or not
         			preferredNameCreated = false;
         			int counter = 0;
         			for (PersonName name : patient.getNames()) {
         				if (counter == selectedName)
         					name.setPreferred(Boolean.TRUE);
         				else
         					name.setPreferred(Boolean.FALSE);
         				counter ++;
                     }
         		} else
         			// Preferred is not one of the name in the person object, so it's going to be on one of
         			// person name array. If you have multiple text input in the form with the same name, then
         			// you will get it as an array inside the request object.
         			selectedName = selectedName - patient.getNames().size();
         		
         		String identifierPreferred = ServletRequestUtils.getStringParameter(request, "identifierPreferred", StringUtils.EMPTY);
         		int selectedIdentifier = NumberUtils.toInt(identifierPreferred);
 
         		boolean preferredIdentifierCreated = true;
         		if (selectedIdentifier < patient.getIdentifiers().size()) {
         			// preferred is one of the name in the patient
         			// iterate the name and move the preferred to other place
         			preferredIdentifierCreated = false;
         			int counter = 0;
         			for (PatientIdentifier identifier: patient.getIdentifiers()) {
         				if (counter == selectedIdentifier)
         					identifier.setPreferred(Boolean.TRUE);
         				else
         					identifier.setPreferred(Boolean.FALSE);
         				counter ++;
                     }
         		} else
         			selectedIdentifier = selectedIdentifier - patient.getIdentifiers().size();
         		
         		String addressPreferred = ServletRequestUtils.getStringParameter(request, "addressPreferred", StringUtils.EMPTY);
         		int selectedAddress = NumberUtils.toInt(addressPreferred);
         		boolean preferredAddressCreated = true;
         		if (selectedAddress < patient.getAddresses().size()) {
         			// preferred is one of the name in the patient
         			// iterate the name and move the preferred to other place
         			preferredAddressCreated = false;
         			int counter = 0;
         			for (PersonAddress address: patient.getAddresses()) {
         				if (counter == selectedAddress)
         					address.setPreferred(Boolean.TRUE);
         				else
         					address.setPreferred(Boolean.FALSE);
         				counter ++;
                     }
         		} else
         			selectedAddress = selectedAddress - patient.getAddresses().size();
         		// End of preferred processing
         		
         		// Start processing part of the form that is created by user (using "add new" button)
         		
         		// arrays from the identifiers section
         		String[] ids = ServletRequestUtils.getStringParameters(request, "identifier");
         		String[] idTypes = ServletRequestUtils.getStringParameters(request, "identifierType");
         		String[] locations = ServletRequestUtils.getStringParameters(request, "location");
         		
         		// only process when the user add a new identifier
         		if (ids != null && idTypes != null && locations != null) {
         			int maxIds = ids.length;
         			
         			LocationService locationService = Context.getLocationService();
         			
         			// only add identifier when the identifier is not blank.
         			// ignore the blank identifiers ...
         			for (int j = 0; j < maxIds; j++) {
 							PatientIdentifier identifier = new PatientIdentifier();
 							identifier.setIdentifier(ids[j]);
 							
 							Integer idType = NumberUtils.createInteger(idTypes[j]);
 							identifier.setIdentifierType(Context.getPatientService().getPatientIdentifierType(idType));
 							
 							Integer location = NumberUtils.createInteger(locations[j]);
 							identifier.setLocation(locationService.getLocation(location));
 							// Assign preferred for selected identifier. The array will be indexed the same way
 							// with how it is rendered on the page. So, we can tell which one is preferred or not
 							// based on this selectedIdentifier value (refer to the preferred processing above)
 							if (preferredIdentifierCreated && selectedIdentifier == j)
 								identifier.setPreferred(Boolean.TRUE);
 							patient.addIdentifier(identifier);
                     }
         		}
 
         		// arrays from the names section
         		String[] givenNames = ServletRequestUtils.getStringParameters(request, "givenName");
         		String[] middleNames = ServletRequestUtils.getStringParameters(request, "middleName");
         		String[] familyNames = ServletRequestUtils.getStringParameters(request, "familyName");
         		
         		// Since the requirement doesn't strict which field must be filled in, we assume that we will
         		// allow a name to have either given, middle or family name. So, create a new PersonName object
         		// when you have given name or middle name or family name
         		if (givenNames != null || middleNames != null || familyNames != null) {
         			
         			int maxNames = 0;
         			if (givenNames != null && givenNames.length > maxNames)
         				maxNames = givenNames.length;
         			if (middleNames != null && middleNames.length > maxNames)
         				maxNames = middleNames.length;
         			if (familyNames != null && familyNames.length > maxNames)
         				maxNames = familyNames.length;
         			
         			for (int j = 0; j < maxNames; j++) {
         				PersonName name = new PersonName();
         				name.setGivenName(givenNames[j]);
         				name.setMiddleName(middleNames[j]);
         				name.setFamilyName(familyNames[j]);
         				if (preferredNameCreated && selectedName == j)
         					name.setPreferred(Boolean.TRUE);
         				patient.addName(name);
                     }
         		}
         		
         		// arrays from the address section
         		String[] address1s = ServletRequestUtils.getStringParameters(request, "address1");
         		String[] address2s = ServletRequestUtils.getStringParameters(request, "address2");
         		String[] cells = ServletRequestUtils.getStringParameters(request, "neighborhoodCell");
         		String[] cities = ServletRequestUtils.getStringParameters(request, "cityVillage");
         		String[] townships = ServletRequestUtils.getStringParameters(request, "townshipDivision");
         		String[] counties = ServletRequestUtils.getStringParameters(request, "countyDistrict");
         		String[] states = ServletRequestUtils.getStringParameters(request, "stateProvince");
         		String[] regions = ServletRequestUtils.getStringParameters(request, "region");
         		String[] subregions = ServletRequestUtils.getStringParameters(request, "subregion");
         		String[] countries = ServletRequestUtils.getStringParameters(request, "country");
         		String[] postalCodes = ServletRequestUtils.getStringParameters(request, "postalCode");
         		
         		// The logic is similar with name above, create a new PersonAddress if you find any non blank
         		// value on the form.
         		if (address1s != null || address1s != null ||
         				cells != null || cities != null ||
         				townships != null || counties != null ||
         				states != null || regions != null ||
         				subregions != null || countries != null ||
         				postalCodes != null) {
         			int maxAddress = 0;
         			if(address1s != null && address1s.length > maxAddress)
         				maxAddress = address1s.length;
         			if(address1s != null && address1s.length > maxAddress)
         				maxAddress = address1s.length;
         			if(cells != null && cells.length > maxAddress)
         				maxAddress = cells.length;
         			if(cities != null && cities.length > maxAddress)
         				maxAddress = cities.length;
         			if(townships != null && townships.length > maxAddress)
         				maxAddress = townships.length;
         			if(counties != null && counties.length > maxAddress)
         				maxAddress = counties.length;
         			if(states != null && states.length > maxAddress)
         				maxAddress = states.length;
         			if(regions != null && regions.length > maxAddress)
         				maxAddress = regions.length;
         			if(subregions != null && subregions.length > maxAddress)
         				maxAddress = subregions.length;
         			if(countries != null && countries.length > maxAddress)
         				maxAddress = countries.length;
         			if(postalCodes != null && postalCodes.length > maxAddress)
         				maxAddress = postalCodes.length;
         			
         			for (int j = 0; j < maxAddress; j++) {
         				PersonAddress pa = new PersonAddress();
         				pa.setAddress1(address1s[j]);
         				pa.setAddress2(address2s[j]);
         				pa.setNeighborhoodCell(cells[j]);
         				pa.setCityVillage(cities[j]);
         				pa.setTownshipDivision(townships[j]);
         				pa.setCountyDistrict(counties[j]);
         				pa.setStateProvince(states[j]);
         				pa.setRegion(regions[j]);
         				pa.setSubregion(subregions[j]);
         				pa.setCountry(countries[j]);
         				pa.setPostalCode(postalCodes[j]);
         				pa.setCountyDistrict(counties[j]);
                         if (preferredAddressCreated && selectedAddress == j)
                         	pa.setPreferred(Boolean.TRUE);
         				patient.addAddress(pa);
         			}
         		}
         		
         		// process relationship removal
         		String[] commandRelationships = ServletRequestUtils.getStringParameters(request, "commandRelationship");
     			// user remove one or more relationship
     			String separator = "|";
     			List<Relationship> relationships = new ArrayList<Relationship>();
     			if (amrsRegistration.getRelationships() != null) {
         			for (Relationship relationship : amrsRegistration.getRelationships()) {
         				String relationshipId = "";
         				if (relationship.getRelationshipId() != null)
         					relationshipId = relationship.getRelationshipId().toString();
         				
         				StringBuffer buffer = new StringBuffer();
         				buffer.append(relationshipId).append(separator);
         				buffer.append(relationship.getRelationshipType()).append(separator);
         				buffer.append(relationship.getPersonA().getPersonName()).append(separator);
         				buffer.append(relationship.getPersonB().getPersonName());
         				if (commandRelationships != null) {
             				for (String string : commandRelationships) {
                                 if (buffer.toString().equals(string))
                                 	relationships.add(relationship);
                             }
         				}
                     }
     			}
     			
     			amrsRegistration.setRelationships(relationships);
         		
         		// process newly created relationship
 
         		String[] personAs = ServletRequestUtils.getStringParameters(request, "personA");
         		@SuppressWarnings("unused")
                 String[] personBs = ServletRequestUtils.getStringParameters(request, "personB");
         		String[] relationshipTypeIds = ServletRequestUtils.getStringParameters(request, "relationshipTypeId");
         		String[] relationshipPersonIds = ServletRequestUtils.getStringParameters(request, "relationshipPersonId");
         		String[] relationshipGivenNames = ServletRequestUtils.getStringParameters(request, "relationshipGivenName");
         		String[] relationshipMiddleNames = ServletRequestUtils.getStringParameters(request, "relationshipMiddleName");
         		String[] relationshipFamilyNames = ServletRequestUtils.getStringParameters(request, "relationshipFamilyName");
         		String[] relationshipGenders = ServletRequestUtils.getStringParameters(request, "relationshipGender");
         		String[] relationshipAges = ServletRequestUtils.getStringParameters(request, "relationshipAge");
         		String[] relationshipBirthdates = ServletRequestUtils.getStringParameters(request, "relationshipBirthdate");
         		// all above arrays will have same length
         		int counter = relationshipTypeIds.length;
         		
         		PersonService personService = Context.getPersonService();
         		
         		for (int i = 0; i < counter; i++) {
 	                Relationship relationship = new Relationship();
 	                
 	                RelationshipType relationshipType = personService.getRelationshipType(NumberUtils.toInt(relationshipTypeIds[i]));
 	                relationship.setRelationshipType(relationshipType);
 	                
 	                // the other person in the relationship
 	                Person otherPerson = null;
 	                if ("N/A".equals(relationshipPersonIds[i])) {
 	                	// don't create relationship for invalid data
 	                	if ("N/A".equals(relationshipGenders[i]))
 	                		continue;
 	                	// create a new person using given data
 	                	otherPerson = new Person();
 	                	
 	                	PersonName name = new PersonName();
 	                	name.setGivenName(relationshipGivenNames[i]);
 	                	name.setMiddleName(relationshipMiddleNames[i]);
 	                	name.setFamilyName(relationshipFamilyNames[i]);
 	                	otherPerson.addName(name);
 	                	
 	                	otherPerson.setGender(relationshipGenders[i]);
 	                	updateBirthdate(otherPerson, relationshipBirthdates[i].replaceAll("N/A", ""), relationshipAges[i].replaceAll("N/A", ""));
 	                } else {
 	                	otherPerson = personService.getPerson(NumberUtils.toInt(relationshipPersonIds[i]));
 	                }
 	                
 	                if ("N/A".equals(personAs[i])) {
 	                	// personA is the patient
 	                	relationship.setPersonA(patient);
 	                	relationship.setPersonB(otherPerson);
 	                } else {
 	                	relationship.setPersonA(otherPerson);
 	                	relationship.setPersonB(patient);
 	                }
 	                
 	                amrsRegistration.addRelationship(relationship);
                 }
     		}
     	}
     	
     	int targetPage = getTargetPage(request, page);
     	
     	// Post processing for the assign id page
     	if (targetPage == AmrsRegistrationConstants.REVIEW_PAGE) {
     		
     		// variable that will be sent from jsp for the amrs id
 			String amrsId = ServletRequestUtils.getStringParameter(request, "amrsIdentifier", null);
 			if (amrsId != null) {
 				// prepare the amrs target id
 				PatientIdentifierType type = Context.getPatientService().getPatientIdentifierTypeByName(AmrsRegistrationConstants.AMRS_TARGET_ID);
             	boolean foundAmrsId = false;
 				try {
 					// search if the patient already have the targeted identifier or not
 					// if yes then do update
 	                validator.validateIdentifier(amrsId, type);
 	                for (PatientIdentifier identifier : patient.getIdentifiers()) {
                         if (patient.getIdentifiers().size() == 1 &&
                                 (identifier == null || identifier.getIdentifier() == null || identifier.getIdentifier() == "")) {
                             patient.setIdentifiers(null);
                             break;
                         }
                         if (identifier.getIdentifierType() != null && identifier.getIdentifierType().equals(type)) {
 	                    	foundAmrsId = true;
 	                    	identifier.setIdentifier(amrsId);
                             validator.validateIdentifier(identifier);
 	                    }
 	                }
 	                // if not then create a new identifier
 	                if (!foundAmrsId) {
 	                	PatientIdentifier identifier = new PatientIdentifier();
 	                	identifier.setIdentifier(amrsId);
 	                	identifier.setIdentifierType(type);
 	                	identifier.setLocation(Context.getLocationService().getDefaultLocation());
                         validator.validateIdentifier(identifier);
                         patient.addIdentifier(identifier);
 	                }
                 }
                 catch (Exception e) {
                 	// the identifier is not valid, this exception will contains message explaining why the validation fail
         			errors.reject(e.getMessage());
                 }
 			}
     	}
     }
 
 	/**
      * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#onBindAndValidate(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.BindException, int)
      */
     @SuppressWarnings("unchecked")
     @Override
     protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors, int page)
                                                                                                                 throws Exception {
     	AmrsRegistration amrsRegistration = (AmrsRegistration) command;
     	Patient patient = amrsRegistration.getPatient();
 		
 		if (page == AmrsRegistrationConstants.EDIT_PAGE) {
 			// update the patient age and birthdate
 			String date = ServletRequestUtils.getStringParameter(request, "birthdateInput", "");
 			String age = ServletRequestUtils.getStringParameter(request, "ageInput", null);
 			updateBirthdate(patient, date, age);
 			
 			// This section to delete address and name and identifier that already attached to the patient object.
 			// Address (and name and identifier) that already attached to the patient object can't be removed automatically.
 			// We need to check whether they are still being sent from the jsp or not. If not, then remove it from the patient
 			// object. Address, name and identifier that already attached to the patient object will have the following
 			// pattern:
 			// - Address 	--> Addresses[x].<property-name>
 			// - Name 		--> Names[x].<property-name>
 			// - Identifier --> Identifier[x].<property-name>
 			// Address, name and identifier that is not attached to the patient object will only have the following pattern:
 			// - Address (or Name or Identifier) --> <property-name>
 			// This is what causing the bug in the preferred handling :)
 			boolean[] addresses = new boolean[patient.getAddresses().size()];
 			boolean[] names = new boolean[patient.getNames().size()];
 			boolean[] ids = new boolean[patient.getIdentifiers().size()];
             /*
             String[] address1s = ServletRequestUtils.getStringParameters(request, "address1");
             String[] address2s = ServletRequestUtils.getStringParameters(request, "address2");
             String[] cells = ServletRequestUtils.getStringParameters(request, "neighborhoodCell");
             String[] cities = ServletRequestUtils.getStringParameters(request, "cityVillage");
             String[] townships = ServletRequestUtils.getStringParameters(request, "townshipDivision");
             String[] counties = ServletRequestUtils.getStringParameters(request, "countyDistrict");
             String[] states = ServletRequestUtils.getStringParameters(request, "stateProvince");
             String[] regions = ServletRequestUtils.getStringParameters(request, "region");
             String[] subregions = ServletRequestUtils.getStringParameters(request, "subregion");
             String[] countries = ServletRequestUtils.getStringParameters(request, "country");
             String[] postalCodes = ServletRequestUtils.getStringParameters(request, "postalCode");
             log.error("regions.length: " + regions.length);
             log.error("subregions.length: " + subregions.length);
             for(int i=0; i<regions.length-1; i++) {
                 log.error("region[" + i + "]: " + regions[i]);
             }
             for(int i=0; i<regions.length-1; i++) {
                 log.error("subregion[" + i + "]: " + subregions[i]);
             }
             */
 			int counter = NumberUtils.max(addresses.length, names.length, ids.length);
 			for (int i = 0; i < counter; i++) {
 	            if (i < addresses.length)
 	            	addresses[i] = false;
 	            if (i < names.length)
 	            	names[i] = false;
 	            if (i < ids.length)
 	            	ids[i] = false;
             }
 
 			boolean updateNeeded = false;
 			Enumeration paramNames = request.getParameterNames();
 			while (paramNames.hasMoreElements()) {
 				String paramName = (String) paramNames.nextElement();
 				for (int i = 0; i < counter; i++) {
 					if (paramName.startsWith("identifiers[" + i + "]")) {
 						ids[i] = true;
 						updateNeeded = true;
 					}
 					if (paramName.startsWith("addresses[" + i + "]")) {
 						addresses[i] = true;
 						updateNeeded = true;
 					}
 					if (paramName.startsWith("names[" + i + "]")) {
 						names[i] = true;
 						updateNeeded = true;
 					}
                 }
 			}
 			
 			if (updateNeeded) {
 				
 				int deleteCounter = 0;
 				for (PersonName name : patient.getNames()) {
 		            if (!names[deleteCounter]) {
 		            	if (name.getPersonNameId() != null)
 		            		errors.reject("amrsregistration.page.edit.namesWithId");
 		            	else
 		            		patient.removeName(name);
 		            }
 		            deleteCounter ++;
 				}
 				
 				deleteCounter = 0;
 				for (PersonAddress address : patient.getAddresses()) {
 		            if (!addresses[deleteCounter]) {
 		            	if (address.getPersonAddressId() != null)
 		            		errors.reject("amrsregistration.page.edit.addressWithId");
 		            	else
 		            		patient.removeAddress(address);
 		            }
 		            deleteCounter ++;
 				}
 				
 				// handle the target identifier
 				deleteCounter = 0;
 				for (PatientIdentifier identifier : patient.getIdentifiers())
 		            if (!ids[deleteCounter]) {
 		            	if (StringUtils.isBlank(identifier.getIdentifier()))
 		            		patient.removeIdentifier(identifier);
 		            deleteCounter ++;
 				}
 			}
 		}
     	
 	    super.onBindAndValidate(request, command, errors, page);
     }
 
 	protected int getTargetPage(HttpServletRequest request, Object command, Errors errors, int page) {		
 		
 		int targetPage = super.getTargetPage(request, command, errors, page);
 		
 		AmrsRegistration amrsRegistration = (AmrsRegistration) command;
 		Patient patient = amrsRegistration.getPatient();
 		
 		PatientService patientService = Context.getPatientService();
 		PersonService personService = Context.getPersonService();
 		
 		// if current page is the start page, then try to search the identifier
 		if (page == AmrsRegistrationConstants.START_PAGE) {
 			// get the identifier
 			String idCard = ServletRequestUtils.getStringParameter(request, "idCardInput", null);
 			if (idCard != null) {
 				// search for all patient with the specified identifier
 				List<Patient> patients = patientService.getPatients(StringUtils.EMPTY, idCard, null, true);
 				if (patients != null && patients.size() > 0) {
 					// default to found patient, but no required id type
 					targetPage = AmrsRegistrationConstants.ASSIGN_ID_PAGE;
 					if (patients.size() > 1) {
 						// more than one patient found
 						// show patient selection and force the user to select one of the patient (or start over again)
 						Set<Patient> matches = new HashSet<Patient>();
 						for (Patient p : patients)
 							matches.add(p);
 						request.setAttribute("potentialMatches", matches);
 						// property to force the user to select one of the patient
 						request.setAttribute("selectionOnly", Boolean.TRUE);
 					}
 					else {
 						// only one patient found
 						// test whether the patient already have the targeted identifier
 						Patient matchedPatient = patients.get(0);
 						copyPatient(patient, matchedPatient);
 						patient.getAttributeMap();
 						List<Relationship> relationships = personService.getRelationshipsByPerson(patient);
 						for (Relationship relationship : relationships) {
 	                        amrsRegistration.initRelationship(relationship);
                         }
 						
 						if (targetIdentifierExists(patient))
 							// send user to review page if the target identifier is found
 							targetPage = AmrsRegistrationConstants.REVIEW_PAGE;
 					}
 				} else
 					// show error when no patient is found
 					errors.reject("amrsregistration.page.start.error", new Object[] {idCard}, "No patient found in the system");
 			}
 		} else if (page == AmrsRegistrationConstants.EDIT_PAGE) {
 			// in the edit patient page, you can select patient from the search
 			String patientId = ServletRequestUtils.getStringParameter(request, "patientIdInput", null);
 			if (patientId != null) {
 				// get the patient object based on the patient id
 				Patient matchedPatient = patientService.getPatient(NumberUtils.toInt(patientId));
 				if (matchedPatient != null) {
 					copyPatient(patient, matchedPatient);
 					patient.getAttributeMap();
 					List<Relationship> relationships = personService.getRelationshipsByPerson(patient);
 					for (Relationship relationship : relationships) {
                         amrsRegistration.initRelationship(relationship);
                     }
 				}
 			}
 			
 			// set the default page after the edit page to assign id page
 			targetPage = AmrsRegistrationConstants.ASSIGN_ID_PAGE;
 			if (targetIdentifierExists(patient))
 				// jump to review if the selected patient already have the targeted amrs identifier
 				targetPage = AmrsRegistrationConstants.REVIEW_PAGE;
 			
 			// validate the page before going to the next page
 			validate(command, errors, false);
 			
 		} else if (page == AmrsRegistrationConstants.ASSIGN_ID_PAGE) {
 			// from the assign page, you are also allowed to select patient from the search result
 			String patientId = ServletRequestUtils.getStringParameter(request, "patientIdInput", null);
 			if (patientId != null) {
 				// get the patient object
 				Patient matchedPatient = patientService.getPatient(NumberUtils.toInt(patientId));
 				if (matchedPatient != null) {
 					copyPatient(patient, matchedPatient);
 					patient.getAttributeMap();
 					List<Relationship> relationships = personService.getRelationshipsByPerson(patient);
 					for (Relationship relationship : relationships) {
                         amrsRegistration.initRelationship(relationship);
                     }
 					
 					// set the default to return to assign id page
 					targetPage = AmrsRegistrationConstants.ASSIGN_ID_PAGE;
 					if (targetIdentifierExists(patient))
 						// jump to review when the patient already have the targeted amrs identifier
 						targetPage = AmrsRegistrationConstants.REVIEW_PAGE;
 				}
 			}
 		}
 		
 		return targetPage;
 	}
 	
 	private boolean targetIdentifierExists(Patient patient) {
 		boolean exist = false;
 		for (PatientIdentifier identifier : patient.getIdentifiers()) {
 			PatientIdentifierType identifierType = identifier.getIdentifierType();
 			if (identifierType != null &&
 					!identifier.isVoided() &&
 					AmrsRegistrationConstants.AMRS_TARGET_ID.equals(identifierType.getName())) {
 				// found the required id type, go to confirmation page
 				exist = true;
 				break;
 			}
         }
 		return exist;
 	}
 	
 	
 
 	@Override
     protected void validatePage(Object command, Errors errors, int page, boolean finish) {
 		// only validate when process finish is being called
 		if (finish && page == AmrsRegistrationConstants.EDIT_PAGE) {
 		//if (page == AmrsRegistrationConstants.REVIEW_PAGE || page == AmrsRegistrationConstants.EDIT_PAGE) {
 			validate(command, errors, finish);
 		}
 		
 		if (finish && page == AmrsRegistrationConstants.ASSIGN_ID_PAGE) {
 			AmrsRegistration amrsRegistration = (AmrsRegistration) command;
 			Patient patient = amrsRegistration.getPatient();
 			for (PatientIdentifier identifier: patient.getIdentifiers()) {
 				PatientIdentifierType identifierType = identifier.getIdentifierType();
 				if (!StringUtils.isBlank(identifier.getIdentifier()))
 					validator.validate(identifier, errors);
 			}
 		}
 	}
 	
 	private void validate(Object command, Errors errors, boolean finish) {
 		// finish flag will determine whether we need to check for the validity of the patient identifier
 		
 		AmrsRegistration amrsRegistration = (AmrsRegistration) command;
 		Patient patient = amrsRegistration.getPatient();
 		
 		boolean foundInvalid = false;
 		for (PersonName name: patient.getNames()) {
 			
 			// if all fields for the name is empty then check if the name already has an id
 			// if yes, then that name can only be voided
 			// if no, then we can remove the name
     		if (StringUtils.isBlank(name.getFamilyName()) &&
     				StringUtils.isBlank(name.getGivenName()) &&
     				StringUtils.isBlank(name.getMiddleName())) {
     			if (name.getPersonNameId() != null)
     				foundInvalid = true;
     			else {
     				if (patient.getNames().size() > 1)
     					patient.removeName(name);
     				else
     					foundInvalid = true;
     			}
     		}
         }
 		
 		// show message for empty name with id
 		if (foundInvalid) {
 			errors.reject("amrsregistration.page.edit.invalidName");
 		}
 		
 		if (patient.getBirthdate() == null)
 			errors.rejectValue("patient.birthdate", "amrsregistration.page.edit.invalidDate");
 		else {
 			if (patient.getBirthdate().after(new Date()))
     			errors.rejectValue("patient.birthdate", "amrsregistration.page.edit.futureDate");
 		}
 		
 		if (StringUtils.isEmpty(patient.getGender()))
 			errors.rejectValue("patient.gender", "amrsregistration.page.edit.invalidGender");
 
 		for (PatientIdentifier identifier: patient.getIdentifiers()) {
 			if (!StringUtils.isBlank(identifier.getIdentifier())) {
 				validator.validate(identifier, errors);
             }
 	    }
 
 		foundInvalid = false;
 		for (PersonAddress address: patient.getAddresses()) {
             if (address.isBlank()) {
                 patient.removeAddress(address);
                 continue;
             }
     		if (StringUtils.isBlank(address.getSubregion()) ||
     				StringUtils.isBlank(address.getRegion()))
     			
     			// TODO: probably need to do validation like above (name)
     			foundInvalid = true;
         }
 		
 		if (foundInvalid)
 			errors.reject("amrsregistration.page.edit.invalidAddress");
 	}
 
 	/**
 	 * Allows for other Objects to be used as values in input tags. Normally, only strings and lists
 	 * are expected
 	 * 
 	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
 	 *      org.springframework.web.bind.ServletRequestDataBinder)
 	 */
 	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
 		super.initBinder(request, binder);
 		
 		NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
 		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, nf, true));
 		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(Context.getDateFormat(), true, 10));
 		binder.registerCustomEditor(PatientIdentifierType.class, new PatientIdentifierTypeEditor());
 		binder.registerCustomEditor(Location.class, new LocationEditor());
 		binder.registerCustomEditor(Concept.class, "civilStatus", new ConceptEditor());
 		binder.registerCustomEditor(Concept.class, "causeOfDeath", new ConceptEditor());
 	}
 
     protected ModelAndView processCancel(
             HttpServletRequest request,
             HttpServletResponse response, Object command,
             BindException bindException) throws Exception {
         return new ModelAndView(new RedirectView(request
                 .getContextPath()
                 + "/module/amrsregistration/registration.form"));
     }
 
     protected ModelAndView processFinish(
             HttpServletRequest request,
             HttpServletResponse response, Object command,
             BindException bindException) throws Exception {
         AmrsRegistration amrsRegistration = (AmrsRegistration) command;
     	Patient patient = amrsRegistration.getPatient();
     	
         List<PatientIdentifier> identifiers = new ArrayList<PatientIdentifier>();
 		for (PatientIdentifier identifier : patient.getIdentifiers()) {
             if(StringUtils.isNotBlank(identifier.getIdentifier()))
             	identifiers.add(identifier);
         }
 		Set<PatientIdentifier> patientIdentifiers = new TreeSet<PatientIdentifier>();
 		patientIdentifiers.addAll(identifiers);
 		patient.setIdentifiers(patientIdentifiers);
 		
         for (PatientIdentifier identifier : patient.getIdentifiers()) {
             if (!AmrsRegistrationConstants.AMRS_TARGET_ID.equals(identifier.getIdentifierType().getName())) {
                 identifier.setPreferred(false);
             } else {
             	identifier.setPreferred(true);
             }
         }
 		
         if (patient != null) {
             Patient savedPatient = Context.getPatientService().savePatient(patient);
             for (Relationship relationship : amrsRegistration.getRelationships()) {
             	if (isSimilar(savedPatient, relationship.getPersonA())) {
 	                relationship.setPersonA(savedPatient);
 	                Person otherPerson = relationship.getPersonB();
 	                if (otherPerson.getPersonId() == null) {
 	                	Person personB = Context.getPersonService().savePerson(otherPerson);
 	                	relationship.setPersonB(personB);
 	                }
                 } else {
                 	relationship.setPersonB(savedPatient);
                 	Person otherPerson = relationship.getPersonA();
                 	if (otherPerson.getPersonId() == null) {
                 		Person personA = Context.getPersonService().savePerson(otherPerson);
                 		relationship.setPersonA(personA);
                 	}
                 }
     	        Context.getPersonService().saveRelationship(relationship);
             }
         }
         return new ModelAndView(new RedirectView(request
                 .getContextPath()
                 + "/module/amrsregistration/registration.form"));
     }
     
     private boolean isSimilar(Person a, Person b) {
     	if (a != null && b != null) {
     		return a.getGender().equals(b.getGender()) && 
     				a.getBirthdate().equals(b.getBirthdate()) &&
     				a.getPersonName().equals(b.getPersonName());
     	}
     	return false;
     }
 
     private Patient getNewPatient() {
     	Patient patient = null;
     	
         Set<PersonName> names = new TreeSet<PersonName>();
         names.add(new PersonName());
         
         Set<PersonAddress> addresses = new TreeSet<PersonAddress>();
         addresses.add(new PersonAddress());
         
         Person localPerson = new Person();
         localPerson.setNames(names);
         localPerson.setAddresses(addresses);
         
         PatientIdentifier identifier = new PatientIdentifier();
         identifier.setIdentifier("");
         Set<PatientIdentifier> identifiers = new TreeSet<PatientIdentifier>();
         identifiers.add(identifier);
         
         patient = new Patient(localPerson);
         patient.setIdentifiers(identifiers);
         return patient;
         
     }
     
     private void copyPatient(Patient to, Patient from) {
     	to.setPersonId(from.getPersonId());
 		
     	to.setAddresses(from.getAddresses());
     	to.setNames(from.getNames());
     	to.setAttributes(from.getAttributes());
 		
     	to.setGender(from.getGender());
     	to.setBirthdate(from.getBirthdate());
     	to.setBirthdateEstimated(from.getBirthdateEstimated());
     	to.setDead(from.isDead());
     	to.setDeathDate(from.getDeathDate());
     	to.setCauseOfDeath(from.getCauseOfDeath());

        to.setCreator(from.getCreator());
        to.setDateCreated(from.getDateCreated());
     	to.setPersonCreator(from.getPersonCreator());
     	to.setPersonDateCreated(from.getPersonDateCreated());
     	to.setPersonChangedBy(from.getPersonChangedBy());
     	to.setPersonDateChanged(from.getPersonDateChanged());
     	to.setPersonVoided(from.isPersonVoided());
     	to.setPersonVoidedBy(from.getPersonVoidedBy());
     	to.setPersonDateVoided(from.getPersonDateVoided());
     	to.setPersonVoidReason(from.getPersonVoidReason());
 		to.setPatientId(from.getPatientId());
 		to.setIdentifiers(from.getIdentifiers());
     }
     
     private void updateBirthdate(Person person, String date, String age) {
     	Date birthdate = null;
 		boolean birthdateEstimated = false;
 		if (date != null && !date.equals("")) {
 			try {
 				// only a year was passed as parameter
 				if (date.length() < 5) {
 					Calendar c = Calendar.getInstance();
 					c.set(Calendar.YEAR, Integer.valueOf(date));
 					c.set(Calendar.MONTH, 0);
 					c.set(Calendar.DATE, 1);
 					birthdate = c.getTime();
 					birthdateEstimated = true;
 				}
 				// a full birthdate was passed as a parameter
 				else {
 					birthdate = Context.getDateFormat().parse(date);
 					birthdateEstimated = false;
 				}
 			}
 			catch (ParseException e) {
 				log.debug("Error getting date from birthdate", e);
 			}
 		} else if (age != null && !age.equals("")) {
 			Calendar c = Calendar.getInstance();
 			c.setTime(new Date());
 			Integer d = c.get(Calendar.YEAR);
 			d = d - Integer.parseInt(age);
 			try {
 				birthdate = DateFormat.getDateInstance(DateFormat.SHORT).parse("01/01/" + d);
 				birthdateEstimated = true;
 			}
 			catch (ParseException e) {
 				log.debug("Error getting date from age", e);
 			}
 		}
 		if (birthdate != null) {
 			person.setBirthdate(birthdate);
 			person.setBirthdateEstimated(birthdateEstimated);
 		}
     }
 }
