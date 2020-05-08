 /* SVN FILE: $Id: SoapClientIP.java 5992 2013-01-09 18:37:24Z slk24 $ */
 package edu.psu.iam.cpr.ip.ui.soap;
 
 /**
  * SoapClientIP client Soap Interface 
  * 
  * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 United States License. To 
  * view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/us/ or send a letter to Creative 
  * Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  *
  * @package edu.psu.iam.cpr.ip.ui.soap 
  * @author $Author: slk24 $
  * @version $Rev: 5992 $
  * @lastrevision $Date: 2013-01-09 13:37:24 -0500 (Wed, 09 Jan 2013) $
  */
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import edu.psu.iam.cpr.core.database.types.MatchType;
 import edu.psu.iam.cpr.core.service.returns.MatchReturn;
 import edu.psu.iam.cpr.core.service.returns.PersonReturn;
 import edu.psu.iam.cpr.core.service.returns.PsuIdReturn;
 import edu.psu.iam.cpr.core.service.returns.UseridReturn;
 import edu.psu.iam.cpr.ip.ui.common.MagicNumber;
 import edu.psu.iam.cpr.ip.ui.common.UIConstants;
 import edu.psu.iam.cpr.ip.ui.validation.FieldUtility;
 import edu.psu.iam.cpr.core.api.returns.FindPersonServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.PersonServiceReturn;
 import edu.psu.iam.cpr.core.api.returns.ServiceReturn;
 /**
  *
  * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 United States License. To
  * view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/us/ or send a letter to Creative
  * Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  *
  * @package edu.psu.iam.cpr.ip.ui.soap
  * @author $Author: slk24 $
  * @version $Rev: 5992 $
  * @lastrevision $Date: 2013-01-09 13:37:24 -0500 (Wed, 09 Jan 2013) $
  */
 
 /**
  *
  * This class contains the Identity Provisioning interactions with the soap services
  */
 public final class SoapClientIP {
 	
 	private static final String THE_RESULTS_CODE_IS = " The results code is";
 	private static final String THE_RESULTS_MESSAGE_IS = "The results message is ";
 	private static final String STATUS_MSG = "statusMsg";
 	private static final String STATUS_CODE = "statusCode";
 	private static final String PERSON_ID = "PERSON_ID";
 	private static final String SRV_PERSON_ID = "srv.personId";
 	private static final String SRV_PSU_ID = "srv.psuId";
 	private static final String SRV_USER_ID = "srv.userId";
 	private static final String USER_ID = "userId";
 	private static final String PLUS4 = "plus4";
 	private static final String PSU_ID = "psuId";
 	/** Instance of logger */                                                     
 	private static final Logger LOG = Logger.getLogger(SoapClientIP.class);
 	
 	/**
 	 * Prevent or make it difficult for someone to instantiate this utility class
 	 */
 	private SoapClientIP()  { }
 
 	/**
 	 * This method uses the data entered during the identity provisioning process to call the find person soap service. The service
 	 * is first called with the entered legal name and current (permanent) address. If no matches are found and a former name is entered, 
 	 * the service is called with the former name and current address. If no matches are found and a previous address is entered, the 
 	 * service is called first with the former name and previous address, then with the legal name and previous address.
 	 * @param sessionData contains the key-value pairs of the identity provisioning session data
 	 * @param uniqueId is a String parameter with either the session id, or a simple unique id.  Used to prefix log entries for a session
 	 * @return The HashMap will contain a statusCode (0 = success, 1 = warning, 2 = Data entry error, 3 = fatal) and statusMsg
 	 * @return If a match is found, the ids (person, user, psu) that exist for the record are returned in the map
 	 */
 	public static Map<String, String> findPerson(Map<String, Object> sessionData, String uniqueId) {
 
 		// create the hashMap to send to find person using the current name and address
 		HashMap<String, String> findPerson = new HashMap<String, String>();
 
 		findPerson.put(UIConstants.PRINCIPAL_ID, (String)sessionData.get(UIConstants.RAC_PRINCIPAL_ID));
 		findPerson.put(UIConstants.PASSWORD, (String)sessionData.get(UIConstants.RAC_PASSWORD));
 		findPerson.put(UIConstants.REQUESTED_BY, (String)sessionData.get(UIConstants.RAC_REQUESTED_BY));
 		if (sessionData.containsKey(UIConstants.IDI_PENN_STATE_ID))
 		{
 			// make sure psuId is formatted correctly (only numbers)
 			findPerson.put(PSU_ID, removeNonDigits((String)sessionData.get(UIConstants.IDI_PENN_STATE_ID)));	
 		}
 		findPerson.put(USER_ID, (String)sessionData.get(UIConstants.IDI_USER_ID));
 		if (sessionData.containsKey(UIConstants.IDI_SOCIAL_SECURITY_NUMBER))
 		{
 			// make sure ssn is formatted correctly (only numbers or null)
 			findPerson.put(UIConstants.SSN, removeNonDigits((String)sessionData.get(UIConstants.IDI_SOCIAL_SECURITY_NUMBER)));	
 		}
 		findPerson.put(UIConstants.FIRST_NAME, (String)sessionData.get(UIConstants.LNA_FIRST_NAME));
 		findPerson.put(UIConstants.LAST_NAME, (String)sessionData.get(UIConstants.LNA_LAST_NAME));
 		findPerson.put(UIConstants.MIDDLE_NAMES, (String)sessionData.get(UIConstants.LNA_MIDDLE_NAMES));
 		findPerson.put(UIConstants.ADDRESS1, (String)sessionData.get(UIConstants.CRA_ADDRESS_LINE1));
 		findPerson.put(UIConstants.ADDRESS2, (String)sessionData.get(UIConstants.CRA_ADDRESS_LINE2));
 		findPerson.put(UIConstants.ADDRESS3, (String)sessionData.get(UIConstants.CRA_ADDRESS_LINE3));
 		findPerson.put(UIConstants.CITY, (String)sessionData.get(UIConstants.CRA_CITY));
 		if (!mapValueIsEmpty((String)sessionData.get(UIConstants.CRA_STATE))) 
 		{
 			findPerson.put(UIConstants.STATE_OR_PROV, (String)sessionData.get(UIConstants.CRA_STATE));	
 		}
 		else if (!mapValueIsEmpty((String)sessionData.get(UIConstants.CRA_PROVINCE))) 
 		{
 			findPerson.put(UIConstants.STATE_OR_PROV, (String)sessionData.get(UIConstants.CRA_PROVINCE));	
 		}
 		if (sessionData.containsKey(UIConstants.CRA_POSTAL_CODE)) 
 		{
 			// make sure postal code is formatted correctly (only numbers or null)
 			findPerson.put(UIConstants.POSTAL_CODE, removeNonDigits((String)sessionData.get(UIConstants.CRA_POSTAL_CODE)));		
 		}
 		findPerson.put(PLUS4, (String)sessionData.get("cra.plus4"));
 		findPerson.put(UIConstants.COUNTRY, (String)sessionData.get(UIConstants.CRA_COUNTRY));
 		if (!(mapValueIsEmpty((String)sessionData.get(UIConstants.PER_BIRTH_MONTH)) || mapValueIsEmpty((String)sessionData.get(UIConstants.PER_BIRTH_DAY)))) 
 		{
 			String dob = sessionData.get(UIConstants.PER_BIRTH_MONTH) + "/" + sessionData.get(UIConstants.PER_BIRTH_DAY);
 			if (!mapValueIsEmpty((String)sessionData.get(UIConstants.PER_BIRTH_YEAR))) {
 				dob += "/" + sessionData.get(UIConstants.PER_BIRTH_YEAR);
 			}
 			findPerson.put(UIConstants.DOB, dob);
 		}
 		findPerson.put(UIConstants.GENDER, (String)sessionData.get(UIConstants.PER_GENDER));
 
 		LOG.info(uniqueId +" " +"Sending to find person client " + findPerson.get(UIConstants.PRINCIPAL_ID) + findPerson.get(UIConstants.REQUESTED_BY) + 
 				findPerson.get(PSU_ID) + findPerson.get(USER_ID) + findPerson.get(UIConstants.SSN) + findPerson.get(UIConstants.FIRST_NAME) + findPerson.get(UIConstants.LAST_NAME) +  
 				findPerson.get(UIConstants.MIDDLE_NAMES) + findPerson.get(UIConstants.ADDRESS1) +  findPerson.get(UIConstants.ADDRESS2) +  findPerson.get(UIConstants.ADDRESS3) +  findPerson.get(UIConstants.CITY) + 
 				findPerson.get(UIConstants.STATE_OR_PROV) +  findPerson.get(UIConstants.POSTAL_CODE) + findPerson.get(PLUS4) +  findPerson.get(UIConstants.COUNTRY) +  findPerson.get(UIConstants.DOB) + 
 				findPerson.get(UIConstants.GENDER) + findPerson.get("rankCutOff"));
 		
 		FindPersonServiceReturn personServiceReturn = null;
 		Map<String, String> results = new HashMap<String, String>();
 		
 		try {
 			//call the service
 			personServiceReturn = SoapClient.callFindPersonService(findPerson, uniqueId);
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			// Print the stack trace in case someone needs it for debugging
 			LOG.info(String.format("%s exception calling FindPersonService [%s]", uniqueId, e.getMessage()));
 			showTraceOutput(e);
 			results.put(STATUS_CODE, "3");
 			results.put(STATUS_MSG, e.getMessage());
 			return results;
 		}
 	
 		LOG.info(String.format("%s %s %s", uniqueId, THE_RESULTS_CODE_IS, personServiceReturn.getStatusCode()));
 		LOG.info(String.format("%s %s %s", uniqueId, THE_RESULTS_MESSAGE_IS, personServiceReturn.getStatusMessage()));
 		results = evaluateResults(personServiceReturn, uniqueId);
 		
 		if (results.isEmpty()) {
 
 			boolean formerFirstNameEntered = false;
 			boolean formerLastNameEntered = false;
 
 			// Check for former first name
 			String formerFirstName = (String)sessionData.get(UIConstants.FMR_FIRST_NAME);
 			if (!mapValueIsEmpty(formerFirstName)) {
 				formerFirstNameEntered = true;
 				findPerson.put(UIConstants.FIRST_NAME, formerFirstName);
 			}
 			// Check for former last name
 			String formerLastName = (String)sessionData.get(UIConstants.FMR_LAST_NAME);
 			if (!mapValueIsEmpty(formerLastName)) {
 				formerLastNameEntered = true;
 				findPerson.put(UIConstants.LAST_NAME, formerLastName);
 			}
 
 			// Person was not found, try search again with former name, if it exists
 			if (formerLastNameEntered || formerFirstNameEntered) {
				LOG.info(String.format("%s Sending to find person with former name %s", uniqueId, findPerson.toString()));
 
 				try {
 					//call the service
 					personServiceReturn = SoapClient.callFindPersonService(findPerson, uniqueId);
 				} catch (Exception e) {
 					LOG.info(String.format("%s exception - [%s]", uniqueId, e.getMessage()));
 					showTraceOutput(e);
 					results.put(STATUS_CODE, "3");
 					results.put(STATUS_MSG, e.getMessage());
 					return results;
 				}
 
 				LOG.info(String.format("%s %s %s", uniqueId, THE_RESULTS_CODE_IS, personServiceReturn.getStatusCode()));
 				LOG.info(String.format("%s %s %s", uniqueId, THE_RESULTS_MESSAGE_IS, personServiceReturn.getStatusMessage()));
 				results = evaluateResults(personServiceReturn, uniqueId);
 				if (results.isEmpty()) {
 
 					// Person was not found, try search again with former name and former address, if a former address was entered
 					String previousAddress1 = (String)sessionData.get(UIConstants.PRA_ADDRESS_LINE1);
 					String previousPostalCode = (String)sessionData.get(UIConstants.PRA_POSTAL_CODE);
 					String previousCountry = (String)sessionData.get(UIConstants.PRA_COUNTRY);
 					if (!(mapValueIsEmpty(previousAddress1) || mapValueIsEmpty(previousPostalCode) || mapValueIsEmpty(previousCountry))) {
 
						LOG.info(String.format("%s Sending to find person with former name and former address %s", uniqueId, findPerson.toString()));
 
 						findPerson.put(UIConstants.ADDRESS1, previousAddress1);
 						findPerson.put(UIConstants.ADDRESS2, (String)sessionData.get(UIConstants.PRA_ADDRESS_LINE2));
 						findPerson.put(UIConstants.ADDRESS3, (String)sessionData.get(UIConstants.PRA_ADDRESS_LINE3));
 						findPerson.put(UIConstants.POSTAL_CODE, removeNonDigits(previousPostalCode));
 						findPerson.put(PLUS4, (String)sessionData.get("pra.plus4"));
 						findPerson.put(UIConstants.COUNTRY, previousCountry);
 						if (!mapValueIsEmpty((String)sessionData.get(UIConstants.PRA_STATE))) {
 							findPerson.put(UIConstants.STATE_OR_PROV, (String)sessionData.get(UIConstants.PRA_STATE));	
 						}
 						else if (!mapValueIsEmpty((String)sessionData.get(UIConstants.PRA_PROVINCE)))
 						{
 							findPerson.put(UIConstants.STATE_OR_PROV, (String)sessionData.get(UIConstants.PRA_PROVINCE));	
 						}
 
 						try {
 							//call the service
 							personServiceReturn = SoapClient.callFindPersonService(findPerson, uniqueId);
 						} catch (Exception e) {
 							LOG.info(String.format("%s exception - [%s]", uniqueId, e.getMessage()));
 							showTraceOutput(e);
 							results.put(STATUS_CODE, "3");
 							results.put(STATUS_MSG, e.getMessage());
 							return results;
 						}
 
 						LOG.info(String.format("%s %s %s", uniqueId, THE_RESULTS_CODE_IS, personServiceReturn.getStatusCode()));
 						LOG.info(String.format("%s %s %s", uniqueId, THE_RESULTS_MESSAGE_IS, personServiceReturn.getStatusMessage()));
 						results = evaluateResults(personServiceReturn, uniqueId);
 						if (results.isEmpty()) {
 							// Person was not found, try with current name and former address
 
 							// reset name values to legal instead of former
 							findPerson.put(UIConstants.FIRST_NAME, (String)sessionData.get(UIConstants.LNA_FIRST_NAME));
 							findPerson.put(UIConstants.LAST_NAME, (String)sessionData.get(UIConstants.LNA_LAST_NAME));
 							findPerson.put(UIConstants.MIDDLE_NAMES, (String)sessionData.get(UIConstants.LNA_MIDDLE_NAMES));
 
 							LOG.info("Sending to find person with current name and former address " + sessionData.toString());
 
 							try {
 								//call the service
 								personServiceReturn = SoapClient.callFindPersonService(findPerson, uniqueId);
 							} catch (Exception e) {
 								LOG.info(String.format("%s exception - [%s]", uniqueId, e.getMessage()));
 								showTraceOutput(e);
 								results.put(STATUS_CODE, "3");
 								results.put(STATUS_MSG, e.getMessage());
 								return results;
 							}
 
 							LOG.info(String.format("%s %s %s", uniqueId, THE_RESULTS_CODE_IS, personServiceReturn.getStatusCode()));
 							LOG.info(String.format("%s %s %s", uniqueId, THE_RESULTS_MESSAGE_IS, personServiceReturn.getStatusMessage()));
 							results = evaluateResults(personServiceReturn, uniqueId);
 
 						}
 
 					}
 				}
 			}
 		}
 		// return the statusCode, statusMsg and if found, person identity keys
 		return results;
 	}
 
 	/**
 	 * Provide a listing of the stack trace when necessary
 	 * @param e - The exception object
 	 */
 	public static void  showTraceOutput(Exception e)
 	{
 		StackTraceElement[] trace = e.getStackTrace();
 		for(int i = 0; i < trace.length; i++)
 		{
 			LOG.debug(trace[i]);
 		}
 	}
 
 	/**
 	 * This method evaluates the find person return, if a match was found, the results (person identifiers) are returned 
 	 * @param personServiceReturn
 	 * @return statusCode (0 = success, 1 = warning, 2 = data entry error, 3 = fatal) and statusMessage and person identifiers if match found
 	 */
 	private static HashMap<String, String> evaluateResults(FindPersonServiceReturn personServiceReturn,  String uniqueId) {
 
 		HashMap<String, String> results = new HashMap<String, String>();
 
 		if (personServiceReturn.getStatusCode() == 0) {
 			// Match found
 			results.put(STATUS_CODE, "0");
 			results.put(STATUS_MSG, "Match found");
 			LOG.info(uniqueId + " " +"The match method is " + personServiceReturn.getMatchingMethod());
 			if (personServiceReturn.getMatchingMethod().equals(MatchType.NEAR_MATCH.toString())) {
 				// If it's a near match, send back the highest score (should be the first entry in the array)
 				MatchReturn highestReturn = personServiceReturn.getNearMatchArray()[0];
 				results.put(SRV_PERSON_ID, highestReturn.getPersonId().toString());
 				if (highestReturn.getPsuId() != null) {
 					results.put(SRV_PSU_ID, highestReturn.getPsuId());
 				}
 				if (highestReturn.getUserId() != null) {
 					results.put(SRV_USER_ID, highestReturn.getUserId());
 				}
 
 			} else {
 				results.put(SRV_PERSON_ID, Long.toString(personServiceReturn.getPersonID()));
 				if (personServiceReturn.getPsuID() != null) {
 					results.put(SRV_PSU_ID, personServiceReturn.getPsuID());
 				}
 				if (personServiceReturn.getUserId() != null) {
 					results.put(SRV_USER_ID, personServiceReturn.getUserId());
 				}
 				LOG.info(uniqueId +" " +"The person id is " + personServiceReturn.getPersonID());
 				LOG.info(uniqueId +" " +"The psu id is " + personServiceReturn.getPsuID());
 				LOG.info(uniqueId +" " +"The user id is " + personServiceReturn.getUserId());
 			}
 		} else if (personServiceReturn.getStatusCode() == MagicNumber.I3 || personServiceReturn.getStatusCode() == MagicNumber.INT_16) {
 			LOG.info(uniqueId +" " +"Call to find person did not find a match. The return msg is " + personServiceReturn.getStatusMessage());
 			results.put(STATUS_CODE, "1");
 			results.put(STATUS_MSG, "No Match found");
 		} else if (personServiceReturn.getStatusCode() == MagicNumber.INT_7) {
 			LOG.info(uniqueId +" " +"Call to find person had a data entry error. The return msg is " + personServiceReturn.getStatusMessage());
 			results.put(STATUS_CODE, "2");
 			results.put(STATUS_MSG, personServiceReturn.getStatusMessage());
 		} else {
 			// Error
 			LOG.info(uniqueId +" " +"Error occurred during find person: " + personServiceReturn.getStatusMessage() + " code: " + personServiceReturn.getStatusCode());
 			results.put(STATUS_CODE, "3");
 			results.put(STATUS_MSG, personServiceReturn.getStatusMessage());
 		}
 		return results;
 	}
 
 
 	/**
 	 * This method uses the data entered during the identity provisioning process to call the add person soap service.
 	 * @param sessionData contains the key-value pairs of the identity provisioning session data
 	 * @return The results will contain a statusCode (0 = success, 1 = warning, 3 = fatal) and statusMsg
 	 * @return If the add was successful, the ids (person, user, psu) that were created for the record are returned in the map
 	 */
 	public static Map<String, String> addPerson(HashMap<String, String> sessionData, String uniqueId) {
 
 		// create the hashMap from the session data to send to add person
 		Map<String, String> addPerson = formatSessionData(sessionData);
 
 		addPerson.putAll(formatAddressData(sessionData));
 		addPerson.putAll(formatPhoneData(sessionData));
 
 		PersonServiceReturn personServiceReturn = null;
 		Map<String, String> results = new HashMap<String, String>();
 		
 		try {
 			//call the service
 			personServiceReturn = SoapClient.callAddPersonService(addPerson, uniqueId);
 		} catch (Exception e) {
 			results.put(STATUS_CODE, "3");
 			results.put(STATUS_MSG, e.getMessage());
 			return results;
 		}
 
 		if (personServiceReturn.getStatusCode() == 0) {
 			results.put(STATUS_CODE, "0");
 			results.put(STATUS_MSG, "Add successful");
 			// determine results to send back to session
 			PersonReturn returnedPerson = personServiceReturn.getPersonReturn();
 			results.put(SRV_PERSON_ID, Long.toString(returnedPerson.getPersonId()));
 			// if there is one psuId associated with this person, set the value 
 			if (personServiceReturn.getNumberOfPsuIds() == 1) {
 				results.put(SRV_PSU_ID, personServiceReturn.getPsuIdReturnRecord()[0].getPsuId());
 			}
 
 			if (personServiceReturn.getNumberOfUserids() > 0) {
 				for (int i = 0; i < personServiceReturn.getNumberOfUserids(); ++i) {
 					UseridReturn currentUserId = personServiceReturn.getUseridReturnRecord()[i];
 					if (currentUserId.getPrimary().equals("Y")) {
 						results.put(SRV_USER_ID, currentUserId.getUserid());
 					}
 				}
 			}
 			
 			LOG.info(uniqueId +" " +"The person id is " + results.get(SRV_PERSON_ID));
 			LOG.info(uniqueId +" " +"The psu id is " + results.get(SRV_PSU_ID));
 			LOG.info(uniqueId +" " +"The user id is " + results.get(SRV_USER_ID));
 		}
 		else {
 			results.put(STATUS_CODE, "3");
 			results.put(STATUS_MSG, personServiceReturn.getStatusMessage());
 		}
 		return results;
 	}
 
 	/**
 	 * This method uses the data entered during the identity provisioning process to call the update person soap service. The address and phone data
 	 * is not updated, but added so that no data is overwritten
 	 * @param sessionData contains the key-value pairs of the identity provisioning session data
 	 * @return The results will contain a statusCode (0 = success, 1 = warning, 3 = fatal) and statusMsg
 	 */
 	public static HashMap<String, String> updatePerson(HashMap<String, String> sessionData, String uniqueId) {
 
 		// create the hashMap from the session data to send to add person
 		HashMap<String, String> updatePerson = formatSessionData(sessionData);
 		
 		// Remove the request to assign person_id and PSU_id 
 		updatePerson.put(UIConstants.ASSIGN_PSU_ID,  "N");
 		updatePerson.put(UIConstants.ASSIGN_USER_ID, "N");
 
 		updatePerson.put(UIConstants.IDENTIFIER_TYPE, PERSON_ID);
 		updatePerson.put("identifier", sessionData.get(SRV_PERSON_ID));
 
 		PersonServiceReturn personServiceReturn = null;
 		HashMap<String, String> results = new HashMap<String, String>();
 		try {
 			//call the service to update the person data
 			personServiceReturn = SoapClient.callUpdatePersonService(updatePerson);
 			LOG.info(uniqueId +" " +"Return status code is " + personServiceReturn.getStatusCode());
 			LOG.info(uniqueId +" " +"Status Msg is " + personServiceReturn.getStatusMessage());
 			LOG.info(uniqueId +" " +"Person id is " + personServiceReturn.getPersonReturn().getPersonId());
 		} catch (Exception e) {
 			LOG.info(uniqueId +" " +"Error occurred during update person: " + e.getMessage());
 			results.put(STATUS_CODE, "1");
 			results.put(STATUS_MSG, e.getMessage());
 			return results;
 		}
 		
 		if (personServiceReturn.getStatusCode() == 0) {
 			results.put(STATUS_CODE, "0");
 			results.put(STATUS_MSG, "Update successful");
 			// determine results to send back to session
 			PersonReturn returnedPerson = personServiceReturn.getPersonReturn();
 			results.put(SRV_PERSON_ID, Long.toString(returnedPerson.getPersonId()));
 			// if there is one psuId associated with this person, set the value 
 			if (personServiceReturn.getNumberOfPsuIds() > 0) {
 				for (int i = 0; i < personServiceReturn.getNumberOfPsuIds(); ++i) {
 					PsuIdReturn currentPsuId = personServiceReturn.getPsuIdReturnRecord()[i];
 					if (currentPsuId.getEndDate() == null) {
 						results.put(SRV_USER_ID, currentPsuId.getPsuId());
 					}
 				}
 			}
 			if (personServiceReturn.getNumberOfUserids() > 0) {
 				for (int i = 0; i < personServiceReturn.getNumberOfUserids(); ++i) {
 					UseridReturn currentUserId = personServiceReturn.getUseridReturnRecord()[i];
 					if (currentUserId.getPrimary().equals("Y")) {
 						results.put(SRV_USER_ID, currentUserId.getUserid());
 					}
 				}
 			}
 			LOG.info("The person id is " + results.get(SRV_PERSON_ID));
 			LOG.info("The psu id is " + results.get(SRV_PSU_ID));
 			LOG.info("The user id is " + results.get(SRV_USER_ID));
 	
 			// Add the entered current address as a new permanent address
 			if (!addAddress(sessionData)) {
 				results.put(STATUS_CODE, "1");
 				results.put(STATUS_MSG, "Address could not be added.");
 			}
 			// Add the entered phone number as a new permanent phone number
 			if (!addPhone(sessionData)) {
 				results.put(STATUS_CODE, "1");
 				results.put(STATUS_MSG, "Phone could not be added.");
 			}
 		}
 		else {
 			results.put(STATUS_CODE, "1");
 			results.put(STATUS_MSG, personServiceReturn.getStatusMessage());
 		}
 		return results;
 	}
 
 	/**
 	 * This method uses the data entered during the identity provisioning process to call the add address soap service.
 	 * @param sessionData contains the key-value pairs of the identity provisioning session data
 	 * @return true if the call was successful, false if not
 	 */
 	public static boolean addAddress(HashMap<String, String> sessionData) {
 
 		HashMap<String, String> addAddress = new HashMap<String, String>();
 		
 		addAddress.put(UIConstants.PRINCIPAL_ID, sessionData.get(UIConstants.RAC_PRINCIPAL_ID));
 		addAddress.put(UIConstants.PASSWORD, sessionData.get(UIConstants.RAC_PASSWORD));
 
 		// Add the entered current address as a new permanent address
 		addAddress.put(UIConstants.REQUESTED_BY, sessionData.get(UIConstants.RAC_REQUESTED_BY));
 
 		addAddress.put(UIConstants.IDENTIFIER_TYPE, PERSON_ID);
 		addAddress.put("identifier", sessionData.get(SRV_PERSON_ID));
 
 		HashMap<String, String> formattedAddressData = formatAddressData(sessionData);
  
 		// Only call add address, if the required information is available
 		if (!formattedAddressData.isEmpty()) {
 			
 			addAddress.putAll(formattedAddressData);
 	
 			ServiceReturn addressServiceReturn = null;
 			
 			try {
 				//call the service
 				addressServiceReturn = SoapClient.callAddAddressService(addAddress);
 			} catch (Exception e) {
 				LOG.info("Error occurred during add address: " + e.getMessage());
 				return false;
 			}
 			
 			// if the add was successful or the address already exists
 			if (addressServiceReturn.getStatusCode() == 0 || addressServiceReturn.getStatusCode() == MagicNumber.INT_201) {
 		        // return success
 				return true;
 			}
 			LOG.info("Error occurred during add address: " + addressServiceReturn.getStatusMessage() + " code: " + addressServiceReturn.getStatusCode());
 		}
 		return false;
 	}
 
 	/**
 	 * This method uses the data entered during the identity provisioning process to call the add phone soap service.
 	 * @param sessionData contains the key-value pairs of the identity provisioning session data
 	 * @return true if the call was successful, false if not
 	 */
 	public static boolean addPhone(HashMap<String, String> sessionData) {
 
 		HashMap<String, String> addPhone = new HashMap<String, String>();
 
 		addPhone.put(UIConstants.PRINCIPAL_ID, sessionData.get(UIConstants.RAC_PRINCIPAL_ID));
 		addPhone.put(UIConstants.PASSWORD, sessionData.get(UIConstants.RAC_PASSWORD));
 		
 		// Add the entered current address as a new permanent address
 		addPhone.put(UIConstants.REQUESTED_BY, sessionData.get(UIConstants.RAC_REQUESTED_BY));
 
 		addPhone.put(UIConstants.IDENTIFIER_TYPE, PERSON_ID);
 		addPhone.put("identifier", sessionData.get(SRV_PERSON_ID));
 
 		HashMap<String, String> formattedPhoneData = formatPhoneData(sessionData);
 		 
 		// Only call add phone, if the required information is available
 		if (!formattedPhoneData.isEmpty()) {
 			
 			addPhone.putAll(formattedPhoneData);
 	
 			ServiceReturn phoneServiceReturn = null;
 			
 			try {
 				//call the service
 				phoneServiceReturn = SoapClient.callAddPhoneService(addPhone);
 			} catch (Exception e) {
 				LOG.info("Error occurred during add phone: " + e.getMessage());
 				return false;
 			}
 	
 			// if the add was successful or the phone number already exists
 			if (phoneServiceReturn.getStatusCode() == 0 || phoneServiceReturn.getStatusCode() == MagicNumber.INT_201) {
 		        // return success
 				return true;
 			}
 			LOG.info("Error occurred during add phone: " + phoneServiceReturn.getStatusMessage() + " code: " + phoneServiceReturn.getStatusCode());
 		}
 		return false;
 	}
 
 	/**
 	 * This method removes all non-digits from the given string
 	 * @param stringWithNonDigits
 	 * @return string of only digits
 	 */
 	private static String removeNonDigits(String stringWithNonDigits) {
 		return formatEmptyString(stringWithNonDigits.replaceAll("[^0-9]", ""));
 	}
 
 	private static String formatEmptyString(String nonFormattedString) {
 		// if the string is empty, set the value to null
 		if (mapValueIsEmpty(nonFormattedString)) {
 			nonFormattedString = null;
 		}
 		return nonFormattedString;
 	}
 
 	/**
 	 * This method checks to see if a map value is empty
 	 * @param mapValue to check
 	 * @return true is the value is null or empty, false otherwise
 	 */
 	private static boolean mapValueIsEmpty(String mapValue) {
 		if (mapValue == null || mapValue.trim().equals("") || mapValue.equals("null"))
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * This method formats the session data commonly used for calling add and update person
 	 * @param sessionData 
 	 * @return key-value hash of data reformatted for calling add or update person
 	 */
 	public static HashMap<String, String> formatSessionData(HashMap<String, String> sessionData) {
 		HashMap<String, String> formattedPersonData = new HashMap<String, String>();
 
 		formattedPersonData.put(UIConstants.PRINCIPAL_ID, sessionData.get(UIConstants.RAC_PRINCIPAL_ID));
 		formattedPersonData.put(UIConstants.PASSWORD, sessionData.get(UIConstants.RAC_PASSWORD));
 		formattedPersonData.put(UIConstants.REQUESTED_BY, sessionData.get(UIConstants.RAC_REQUESTED_BY));
 		formattedPersonData.put(UIConstants.IDENTIFIER_TYPE, PERSON_ID);
 		formattedPersonData.put("identifier", sessionData.get(SRV_PERSON_ID));
 
 		formattedPersonData.put(UIConstants.ASSIGN_PSU_ID, sessionData.get(UIConstants.RAC_ASSIGN_PSU_ID));
 		formattedPersonData.put(UIConstants.ASSIGN_USER_ID, sessionData.get(UIConstants.RAC_ASSIGN_USER_ID));
 
 		formattedPersonData.put(UIConstants.GENDER, sessionData.get(UIConstants.PER_GENDER));
 		formattedPersonData.put(UIConstants.NAME_TYPE, sessionData.get("lna.nameType"));
 		formattedPersonData.put(UIConstants.FIRST_NAME, sessionData.get(UIConstants.LNA_FIRST_NAME));
 		formattedPersonData.put(UIConstants.MIDDLE_NAMES, sessionData.get(UIConstants.LNA_MIDDLE_NAMES));
 		formattedPersonData.put(UIConstants.LAST_NAME, sessionData.get(UIConstants.LNA_LAST_NAME));
 		formattedPersonData.put(UIConstants.SUFFIX, sessionData.get(UIConstants.LNA_SUFFIX));
 
 		if (sessionData.containsKey(UIConstants.IDI_SOCIAL_SECURITY_NUMBER))
 		{
 			// make sure ssn is formatted correctly (only numbers or null)
 			formattedPersonData.put(UIConstants.SSN, removeNonDigits(sessionData.get(UIConstants.IDI_SOCIAL_SECURITY_NUMBER)));	
 		}
 
 		if (!(mapValueIsEmpty(sessionData.get(UIConstants.PER_BIRTH_MONTH)) || mapValueIsEmpty(sessionData.get(UIConstants.PER_BIRTH_DAY))))
 		{
 			String dob = sessionData.get(UIConstants.PER_BIRTH_MONTH) + "/" + sessionData.get(UIConstants.PER_BIRTH_DAY);
 			if (!mapValueIsEmpty(sessionData.get(UIConstants.PER_BIRTH_YEAR))) {
 				dob += "/" + sessionData.get(UIConstants.PER_BIRTH_YEAR);
 			}
 			formattedPersonData.put(UIConstants.DOB, dob);
 		}
 
 		formattedPersonData.put(UIConstants.EMAIL_TYPE, sessionData.get(UIConstants.CON_EMAIL_TYPE));
 		formattedPersonData.put(UIConstants.EMAIL, sessionData.get(UIConstants.CON_EMAIL));
 		// Will depend on RA. Is there a mapping of affiliations to RA?
 		formattedPersonData.put(UIConstants.AFFILIATION, sessionData.get("??????"));
 		
 		return formattedPersonData;
 	}
 
 	/**
 	 * This method formats the address data for calling the add address service
 	 * @param sessionData
 	 * @return if the required information is available, a key-value hash for add address, otherwise an empty hash
 	 */
 	public static HashMap<String, String> formatAddressData(HashMap<String, String> sessionData) {
 		HashMap<String, String> formattedAddressData = new HashMap<String, String>();
 
 		// If the required elements are missing, return an empty map
 		if (!(mapValueIsEmpty(sessionData.get(UIConstants.CRA_ADDRESS_TYPE)) || mapValueIsEmpty(sessionData.get(UIConstants.CRA_ADDRESS_LINE1)) ||
 				mapValueIsEmpty(sessionData.get(UIConstants.CRA_COUNTRY)) || mapValueIsEmpty(sessionData.get(UIConstants.CRA_POSTAL_CODE)))) {
 
 			formattedAddressData.put(UIConstants.ADDRESS_TYPE, sessionData.get(UIConstants.CRA_ADDRESS_TYPE));
 			formattedAddressData.put(UIConstants.ADDRESS_DOCUMENT_TYPE, sessionData.get(UIConstants.CRA_ADDRESS_DOCUMENT_TYPE));
 			formattedAddressData.put(UIConstants.ADDRESS_GROUP_ID, sessionData.get(UIConstants.CRA_ADDRESS_GROUP_ID));
 			formattedAddressData.put(UIConstants.ADDRESS1, sessionData.get(UIConstants.CRA_ADDRESS_LINE1));
 			formattedAddressData.put(UIConstants.ADDRESS2, sessionData.get(UIConstants.CRA_ADDRESS_LINE2));
 			formattedAddressData.put(UIConstants.ADDRESS3, sessionData.get(UIConstants.CRA_ADDRESS_LINE3));
 			formattedAddressData.put(UIConstants.CITY, sessionData.get(UIConstants.CRA_CITY));
 			formattedAddressData.put(UIConstants.COUNTRY, sessionData.get(UIConstants.CRA_COUNTRY));
 	
 			if (!mapValueIsEmpty(sessionData.get(UIConstants.CRA_STATE))) 
 			{
 				formattedAddressData.put(UIConstants.STATE_OR_PROV, sessionData.get(UIConstants.CRA_STATE));	
 			}
 			else if (!mapValueIsEmpty(sessionData.get(UIConstants.CRA_PROVINCE))) 
 			{
 				formattedAddressData.put(UIConstants.STATE_OR_PROV, sessionData.get(UIConstants.CRA_PROVINCE));	
 			}
 	
 			if (sessionData.containsKey(UIConstants.CRA_POSTAL_CODE)) 
 			{
 				// make sure postal code is formatted correctly (only numbers or null)
 				formattedAddressData.put(UIConstants.POSTAL_CODE, removeNonDigits(sessionData.get(UIConstants.CRA_POSTAL_CODE)));		
 			}
 		}
 		
 		return formattedAddressData;
 	}
 	
 
 	/**
 	 * This method formats the phone data for calling the add phone service
 	 * @param sessionData
 	 * @return if the required information is available, a key-value hash for add phone, otherwise an empty hash
 	 */
 	public static HashMap<String, String> formatPhoneData(HashMap<String, String> sessionData) {
 		HashMap<String, String> formattedPhoneData = new HashMap<String, String>();
 
 		// If the required elements are missing, return an empty map
 		if (!(mapValueIsEmpty(sessionData.get(UIConstants.CON_PHONE_TYPE)) || mapValueIsEmpty(sessionData.get(UIConstants.CON_PHONE_NUMBER)) ||
 				mapValueIsEmpty(sessionData.get(UIConstants.CON_INTERNATIONAL_NUMBER)))) {
 			
 			formattedPhoneData.put(UIConstants.PHONE_TYPE, sessionData.get(UIConstants.CON_PHONE_TYPE));
 			formattedPhoneData.put(UIConstants.PHONE_GROUP_ID, sessionData.get(UIConstants.CON_PHONE_GROUP_ID));
 			formattedPhoneData.put(UIConstants.PHONE_NUMBER, sessionData.get(UIConstants.CON_PHONE_NUMBER));
 			formattedPhoneData.put(UIConstants.EXTENSION, sessionData.get(UIConstants.CON_EXTENSION));
 			formattedPhoneData.put(UIConstants.INTERNATIONAL_NUMBER, sessionData.get(UIConstants.CON_INTERNATIONAL_NUMBER));
 		}
 		if(FieldUtility.fieldIsNotPresent(formattedPhoneData.get(UIConstants.INTERNATIONAL_NUMBER)) || 
 		   formattedPhoneData.get(UIConstants.INTERNATIONAL_NUMBER).equalsIgnoreCase("false"))
 		{
 			formattedPhoneData.put(UIConstants.INTERNATIONAL_NUMBER, "N");
 		}
 		else
 		if(formattedPhoneData.get(UIConstants.INTERNATIONAL_NUMBER).equalsIgnoreCase("true"))
 		{
 			formattedPhoneData.put(UIConstants.INTERNATIONAL_NUMBER, "Y");
 		}
 		
 		return formattedPhoneData;
 	}
 
 }
 
 
