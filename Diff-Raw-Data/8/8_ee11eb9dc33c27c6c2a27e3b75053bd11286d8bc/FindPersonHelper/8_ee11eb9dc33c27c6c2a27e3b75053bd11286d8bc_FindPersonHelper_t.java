 package edu.psu.iam.cpr.core.api.helper;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import edu.psu.iam.cpr.core.api.returns.FindPersonServiceReturn;
 import edu.psu.iam.cpr.core.database.DBTypes;
 import edu.psu.iam.cpr.core.database.Database;
 import edu.psu.iam.cpr.core.database.beans.IdentifierType;
 import edu.psu.iam.cpr.core.database.tables.DateOfBirthTable;
 import edu.psu.iam.cpr.core.database.tables.UseridTable;
 import edu.psu.iam.cpr.core.database.tables.validate.ValidateDateOfBirth;
 import edu.psu.iam.cpr.core.database.types.CprPropertyName;
 import edu.psu.iam.cpr.core.database.types.MatchType;
 import edu.psu.iam.cpr.core.error.CprException;
 import edu.psu.iam.cpr.core.error.ReturnType;
 import edu.psu.iam.cpr.core.service.helper.ServiceCoreReturn;
 import edu.psu.iam.cpr.core.service.returns.DateOfBirthReturn;
 import edu.psu.iam.cpr.core.service.returns.UseridReturn;
 import edu.psu.iam.cpr.core.util.CprProperties;
 import edu.psu.iam.cpr.core.util.ValidateSSN;
 
 /**
  * This is a helper class for the FindPerson() service.
  * It contains several methods used by the FindPerson service.
  * 
  * After instantiating this class, the user should call
  * getMatchCodes() to populate the match codes using the GI
  * service. Then the matchNameMatchCodeFromCPR(),
  * matchAddressMatchCodeFromCPR(), and matchCityMatchCodeFromCPR()
  * methods may be used.
  * 
  * Copyright 2012 The Pennsylvania State University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * @package edu.psu.iam.cpr.core.api.helper
  * @author $Author$
  * @version $Rev$
  * @lastrevision $Date$
  * 
  */
 public class FindPersonHelper {
 	/** the person id */
 	private long personId = -1L;
 	
 	/** Handle to the CPR database. */
 	private Database db;
 	
 	private Long matchSet;
 	/** Contains the match set */
 	
 	/** Instance of logger */
 	private static final Logger LOG4J_LOGGER = Logger.getLogger(FindPersonHelper.class);
 
 	private static final int MMDD_FORMATTED = 5;
 	private static final int MMDDYYYY_FORMATTED = 10;
 	private static final int MMDDYYYY = 8;
 		
 	/**
 	 * Constructor.
 	 * 
 	 * @param db An open handle to the CPR database.
 	 * 
 	 * @throws GeneralDatabaseException if db is null or closed.
 	 */
 	public FindPersonHelper(final Database db) {
 		
 		super();		
 		this.db = db;
 
 	}
 	
 	/**
 	 * Gets the personID.
 	 *
 	 * @return the person id, or -1 if one was not cached.
 	 */
 	public long getPersonId() {
 		return personId;
 	}
 
 	/**
 	 * Sets the personID. This method should only be used for testing!
 	 *
 	 * param the person id
 	 */
 	public void setPersonId(long id) {
 		personId = id;
 	}
 	
 	/**
 	 * Do a match using an open source algorithm.
 	 * @param serviceCoreReturn contains the service core return object.
 	 * @param requestedBy contains the user who requested the search.
 	 * @param psuId penn state id.
 	 * @param userId userid.
 	 * @param ssn contains the social security number.
 	 * @param firstName contains the first name.
 	 * @param lastName contains the last name.
  	 * @param middleName contains the middle name(s).
 	 * @param address1 contains the address line #1.
 	 * @param address2 contains the address line #2.
 	 * @param address3 contains the address line #3.
 	 * @param city contains the city.
 	 * @param state contains the state.
 	 * @param postalCode contains the postal code.
 	 * @param plus4 contains the plus4 postal code.
 	 * @param country contains the country.
 	 * @param dateOfBirth contains the dob.
 	 * @param gender contains the gender.
 	 * @param rankCutOff contains the rank cutoff - penn state only.
 	 * @return will a return a find person service return object if successful.
 	 * @throws GeneralDatabaseException will be thrown if there is a general database problem.
 	 * @throws CprException will be thrown if there is a cpr specific problem.
 	 * @throws ParseException 
 	 */
 	@SuppressWarnings("unchecked")
 	public FindPersonServiceReturn doSearchForPersonOS(ServiceCoreReturn serviceCoreReturn,
 			String requestedBy,
 			String psuId, 
 			String userId, 
 			String ssn, 
 			String firstName,
 			String lastName, 
 			String middleName,
 			String address1,
 			String address2, 
 			String address3,
 			String city, 
 			String state, 
 			String postalCode,
 			String plus4,
 			String country, 
 			String dateOfBirth,
 			String gender, 
 			String rankCutOff) throws CprException, ParseException {
 		
 		String localSSN = (ssn != null) ? ssn.trim() : null;
 		String localUserid = (userId != null) ? userId.trim() : null;
 		String localFirstName = (firstName != null) ? firstName.trim() : null;
 		String localLastName = (lastName != null) ? lastName.trim() : null;
 		String localDateOfBirth = (dateOfBirth != null) ? dateOfBirth.trim() : null;
 		String localCity = (city != null) ? city.trim() : null;
 		
 		if (localSSN != null && localSSN.length() > 0) {
 			LOG4J_LOGGER.debug("SearchForPerson: found ssn parm = " + localSSN);
 			if (ValidateSSN.validateSSN(localSSN)) {
 				localSSN = ValidateSSN.extractNumericSSN(localSSN);
 				LOG4J_LOGGER.debug("SearchForPerson: found valid ssn = " + localSSN);
 			} 
 			else {
 				LOG4J_LOGGER.debug("SearchForPerson: found invalid ssn = " + localSSN);
 				return new FindPersonServiceReturn(ReturnType.INVALID_PARAMETERS_EXCEPTION.index(), "Invalid value was specified for the SSN.");
 			}
 		}
 	
 		if (localFirstName == null || localFirstName.length() == 0) {
 			LOG4J_LOGGER.debug("SearchForPerson: found invalid first name = " + localFirstName);
 			return new FindPersonServiceReturn(ReturnType.INVALID_PARAMETERS_EXCEPTION.index(), "First name must contain a value.");
 		} 
 		
 		if (localLastName == null || localLastName.length() == 0) {
 			LOG4J_LOGGER.debug("SearchForPerson: found invalid last name = " + localLastName);
 			return new FindPersonServiceReturn(ReturnType.INVALID_PARAMETERS_EXCEPTION.index(), "Last name must contain a value.");
 		} 
 		
 		if (localDateOfBirth == null || localDateOfBirth.length() == 0) {
 			LOG4J_LOGGER.debug("SearchForPerson: no date of birth entered");
 			return new FindPersonServiceReturn(ReturnType.INVALID_PARAMETERS_EXCEPTION.index(), "A date of birth must be entered.");
 		}
 		
 		if (localCity == null || localCity.length() == 0) {
 			LOG4J_LOGGER.debug("SearchForPerson: no city entered");
 			return new FindPersonServiceReturn(ReturnType.INVALID_PARAMETERS_EXCEPTION.index(), "A city must be entered.");			
 		}
 		
 		else {
 			String numericDateOfBirth = localDateOfBirth;			
 			// Take care of MM/DD and MM/DD/YYYY cases.
 			if (localDateOfBirth.length() == MMDD_FORMATTED || localDateOfBirth.length() == MMDDYYYY_FORMATTED) {				
 				// If the DOB has a dash in it, replace it with a slash.
 				localDateOfBirth = localDateOfBirth.replace('-', '/');
 				// Attempt to validate the DOB.
 				if (! ValidateDateOfBirth.isDateOfBirthValid(localDateOfBirth)) {
 					LOG4J_LOGGER.debug("SearchForPerson: found invalid date of birth = " + localDateOfBirth);
 					return new FindPersonServiceReturn(ReturnType.INVALID_PARAMETERS_EXCEPTION.index(), "An invalid date of birth was entered.");						
 				}
 				
 				// Extract a character representation of the date of birth.
 				numericDateOfBirth = DateOfBirthTable.toDobChar(localDateOfBirth);
 				LOG4J_LOGGER.debug("SearchForPerson: converting date of birth to a numeric string " + numericDateOfBirth);
 			}
 			// Verify that the resultant DOB is exactly 8 characters.
 			LOG4J_LOGGER.debug("SearchForPerson: Verify that DOB is exactly 8 characters.");	
 			if (numericDateOfBirth.length() != MMDDYYYY) {
 				LOG4J_LOGGER.debug("SearchForPerson: found invalid date of birth = " + localDateOfBirth);
 				return new FindPersonServiceReturn(ReturnType.INVALID_PARAMETERS_EXCEPTION.index(), "An invalid date of birth was entered.");											
 			}
 			else {
 				LOG4J_LOGGER.debug("SearchForPerson: found valid date of birth = " + localDateOfBirth);
 			}
 		}
 		
 		// Get the list of matching person ids for the name.  We are doing a simple exact match.
 		List<Long> nameMatch = new ArrayList<Long>();
 		if (localLastName != null) {
 			
 			// We are doing a query for names with history.
 			Session session = db.getSession();
 			String sqlQuery = "select distinct personId from Names where upper(lastName)=:last_name AND upper(firstName)=:first_name";
 			Query query = session.createQuery(sqlQuery);
 			query.setParameter("last_name", localLastName.toUpperCase());
 			query.setParameter("first_name", localFirstName.toUpperCase());
 			for (Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 				Long personIdValue = (Long) it.next();
 				nameMatch.add(personIdValue);
 			}
 		}
 		
 		// Get the list of matches for the SSN.
 		List<Long> ssnMatch = new ArrayList<Long>();
 		if (localSSN != null) {
 			
 			// Need to find the key that is associated with the SSN person identifier.
 			Map<String,Object> map = DBTypes.INSTANCE.getTypeMaps(DBTypes.IDENTIFIER_TYPE);
 			IdentifierType identifierType = null;
 			for (Map.Entry<String,Object> entry : map.entrySet()) {
 				identifierType = (IdentifierType) entry.getValue();
 				if (identifierType.getTypeName().equals(Database.SSN_IDENTIFIER)) {
 					break;
 				}
 			}
 			
 			// Do a query for the active person identifier.
 			Session session = db.getSession();
 			String sqlQuery = "select distinct personId from PersonIdentifier where typeKey = :type_key AND identifierValue = :identifier_value AND endDate is null";
 			Query query = session.createQuery(sqlQuery);
 			query.setParameter("type_key", identifierType.getTypeKey());
 			query.setParameter("identifier_value", localSSN);
 			for (Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 				Long personIdValue = (Long) it.next();
 				ssnMatch.add(personIdValue);
 			}
 		}
 		
 		// Get the list of matches for the userid.
 		List<Long> useridMatch = new ArrayList<Long>();
 		if (localUserid != null) {
 			Session session = db.getSession();
 			String sqlQuery = "select distinct personId from Userid where userid = :userid";
 			Query query = session.createQuery(sqlQuery);
 			query.setParameter("userid", localUserid);
 			for (Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 				Long personIdValue = (Long) it.next();
 				useridMatch.add(personIdValue);
 			}		
 		}
 		
 		// Get the list of matches for the city.
 		List<Long> cityMatch = new ArrayList<Long>(); 
 		if (localCity != null) {
 			Session session = db.getSession();
 			String sqlQuery = "select distinct personId from Addresses where upper(city) = :city";
 			Query query = session.createQuery(sqlQuery);
 			query.setParameter("city", localCity.toUpperCase());
 			for (Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 				Long personIdValue = (Long) it.next();
 				cityMatch.add(personIdValue);
 			}
 		}
 			
 		// Check name, SSN, and DOB.
 		if (localSSN != null) {
 			List<Long> finalMatch = new ArrayList<Long>();
 			finalMatch = (List<Long>) CollectionUtils.intersection(nameMatch, ssnMatch);
 			
 			if (finalMatch.size() == 1) {
 				
 				setPersonId(finalMatch.get(0));
 
 				// Verify that the DOB matches.
 				if (matchDOBInCPR(localDateOfBirth)) {
					LOG4J_LOGGER.info("SearchForPerson: match by ssn successful");								
 					FindPersonServiceReturn findPersonReturn = new FindPersonServiceReturn(ReturnType.SUCCESS.index(), "SUCCESS");
 					findPersonReturn.setMatchingMethod(MatchType.SSN.toString());
 					findPersonReturn.setPersonID(getPersonId());
 					findPersonReturn.setPsuID(null);
 					findPersonReturn.setUserId(getPrimaryUserIdFromCPR());
 					return findPersonReturn;
 				}
 			}
 		}
 		
 		// Check name, userid and DOB.
 		if (localUserid != null) {
 			List<Long> finalMatch = new ArrayList<Long>();
 			finalMatch = (List<Long>) CollectionUtils.intersection(nameMatch, useridMatch);
 			if (finalMatch.size() == 1) {
 
 				setPersonId(finalMatch.get(0));
 
 				// Verify that the DOB matches.
 				if (matchDOBInCPR(localDateOfBirth)) {
 					// Log a success!
					LOG4J_LOGGER.info("SearchForPerson: match by user id successful");					        
 					FindPersonServiceReturn findPersonReturn = new FindPersonServiceReturn(ReturnType.SUCCESS.index(), "SUCCESS");
 					findPersonReturn.setMatchingMethod(MatchType.USERID.toString());
 					findPersonReturn.setPersonID(getPersonId());				
 					findPersonReturn.setPsuID(null);
 					findPersonReturn.setUserId(getPrimaryUserIdFromCPR());
 
 					return findPersonReturn;
 				}
 			}
 		}
 		
 		// Check name, city and DOB.
 		if (localCity != null) {
 			List<Long> finalMatch = new ArrayList<Long>();
 			finalMatch = (List<Long>) CollectionUtils.intersection(nameMatch, cityMatch);
 			if (finalMatch.size() == 1) {
 				setPersonId(finalMatch.get(0));
 
 				// Verify that the DOB matches.
 				if (matchDOBInCPR(localDateOfBirth)) {
 					// Log a success!
					LOG4J_LOGGER.info("SearchForPerson: match by city successful");					        
 					FindPersonServiceReturn findPersonReturn = new FindPersonServiceReturn(ReturnType.SUCCESS.index(), "SUCCESS");
 					findPersonReturn.setMatchingMethod(MatchType.CITY.toString());
 					findPersonReturn.setPersonID(getPersonId());				
 					findPersonReturn.setPsuID(null);
 					findPersonReturn.setUserId(getPrimaryUserIdFromCPR());
 
 					return findPersonReturn;
 				}
 				
 			}
 		}
 		
 		// No match was found.
 		FindPersonServiceReturn findPersonReturn = new FindPersonServiceReturn(ReturnType.PERSON_NOT_FOUND_EXCEPTION.index(), "FAILURE");
         findPersonReturn.setMatchingMethod(MatchType.NO_MATCH.toString());        
         return findPersonReturn;
 	}
 
 	/**
 	 * Match a user's (partial) date of birth against the value stored in the CPR.
 	 * @param dob The (partial) DOB to be matched.
 	 * 
 	 * @return true If the record in the CPR matches the input parameters; otherwise, false.
 	 * @throws GeneralDatabaseException 
 	 * @throws CprException 
 	 */
 	public boolean matchDOBInCPR(final String dob) throws CprException {
 		LOG4J_LOGGER.debug("FPH: checking date of birth; personId = " + personId + ", dob = " + dob);
 		
 		if (personId < 0) {
 			return false;
 		}
 		
 		if (dob == null || "".equals(dob.trim())) {
 			return false;
 		}
 		
 		Date d;
 		try {
 			LOG4J_LOGGER.debug("FPH: ready for conversion of dob = " + dob + " to Date object");			
 			d = parseDateString(dob, true);
 		} catch (ParseException ex) {
 			LOG4J_LOGGER.debug("FPH: parse exception = " + ex.getMessage());		
 			return false;
 		}
 		
 		Calendar inputCal = Calendar.getInstance(Locale.US);
 		inputCal.setTime(d);
 	
 		DateOfBirthTable dobTable = new DateOfBirthTable();
 		
 		LOG4J_LOGGER.debug("FPH: ready for conversion to retrieve date from DOB table");	
 		dobTable.setReturnHistoryFlag(false);
 		DateOfBirthReturn dobReturn[] = dobTable.getDateOfBirthForPersonId(db, personId);
 		if (dobReturn.length == 0 || dobReturn[0].getDob() == null) {
 			LOG4J_LOGGER.debug("FPH: no dob found");	
 			return false;
 		}
 
 		String cprDOB = dobReturn[0].getDob();
 		
 		LOG4J_LOGGER.debug("FPH: date of birth found " + cprDOB );
 		
     	try {
     		final Date cprDate = parseDateString(cprDOB, true);
     		final Calendar cprCal = convertDateToCalendar(cprDate, true);
     		
     		// if the exact dates match, return true
     		if (dob.equals(cprDate)) {
     			return true;
     		}
     		
     		// check for a partial match
     		int inputMonth = inputCal.get(Calendar.MONTH);
     		int cprMonth = cprCal.get(Calendar.MONTH);
     		int inputDay = inputCal.get(Calendar.DAY_OF_MONTH);
     		int cprDay = cprCal.get(Calendar.DAY_OF_MONTH);
     		int inputYear = inputCal.get(Calendar.YEAR);
     		int cprYear = inputCal.get(Calendar.YEAR);
     		    		
     		if ((inputMonth == cprMonth && (inputDay == cprDay || inputYear == cprYear)) || (inputDay == cprDay && inputYear == cprYear)) { 
     			return true;
     		}
     		
     		return false;
     	}
     	catch (Exception e) {
     		return false;
     	}
 	}
 	
 	/**
 	 * Get the primary user id (access id) from the CPR.
 	 * 
 	 * @return the primary user ID or an empty string on error
 	 * @throws CprException 
 	 */
 	public String getPrimaryUserIdFromCPR() throws CprException {
 	
 		LOG4J_LOGGER.debug("FPH: now getting primary userid for person id = " + personId);
 		
 		if (personId < 0) {
 			return "";
 		}
 		
 		final UseridTable u = new UseridTable();
 		u.setReturnHistoryFlag(false);
 		final UseridReturn[] queryResults = u.getUseridsForPersonId(db, personId);
 		
 		if (queryResults == null || queryResults.length == 0) {
 			return "";
 		}
 		
 		for (UseridReturn uid : queryResults) {
 			if (uid.getPrimary().equals("Y")) {
 				return uid.getUserid();
 			}
 		}
 		
 		return "";
 		
 	}
 	
 	public String getPrimaryUserIdFromCPRFor(Long persId) throws CprException {
 		
 		LOG4J_LOGGER.debug("FPH: now getting primary userid for person id = " + persId);
 		
 		if (persId < 0) {
 			return "";
 		}
 		
 		final UseridTable u = new UseridTable();
 		u.setReturnHistoryFlag(false);
 		final UseridReturn[] queryResults = u.getUseridsForPersonId(db, persId);
 		
 		LOG4J_LOGGER.debug("FPH: now looking up user id = " + persId);
 		
 		if (queryResults == null || queryResults.length == 0) {
 			return "";
 		}
 		
 		LOG4J_LOGGER.debug("FPH: number of userids found = " + queryResults.length);
 		
 		for (UseridReturn uid : queryResults) {
 			if (uid.getPrimary().equals("Y")) {
 				return uid.getUserid();
 			}
 		}
 		
 		return "";
 		
 	}
 		
 	/**
 	 * Parse a date string and return a Calendar object.
 	 * 
 	 * @param dateString The date to parse (in MM-DD-YYYY or MM/DD/YYYY format)
 	 * @param lenient Should parsing be lenient
 	 * 
 	 * @return a Calendar object
 	 * @throws ParseException if dateString is malformed
 	 */
 	public static final Date parseDateString(final String dateString, boolean lenient) throws ParseException {
 		LOG4J_LOGGER.debug("FPH: now = parseDateString for string = " + dateString);
 		
 		if (dateString == null) {
 			throw new ParseException("Date string is null", 0);
 		}
 		
 		SimpleDateFormat sdf = new SimpleDateFormat(CprProperties.INSTANCE.getProperties().getProperty(
 				CprPropertyName.CPR_FORMAT_PARTIAL_DATE.toString()));
 		sdf.setLenient(lenient);
 		Date d = sdf.parse(dateString.replace('-', '/').trim());
 		
 		return d;
 	}
 	
 	/**
 	 * Convert a Date object into a Calendar object.
 	 * 
 	 * @param d The date to convert
 	 * @param lenient Should parsing be lenient
 	 * 
 	 * @return a Calendar object
 	 */
 	public static final Calendar convertDateToCalendar(final Date d, boolean lenient) {
 
 		Calendar cal = Calendar.getInstance(Locale.US);
 		cal.setLenient(lenient);
 		cal.setTime(d);	
 	
 		return cal;
 	}
 
 	/**
 	 * @param matchSet the matchSet to set
 	 */
 	public void setMatchSet(Long matchSet) {
 		this.matchSet = matchSet;
 	}
 
 	/**
 	 * @return the matchSet
 	 */
 	public Long getMatchSet() {
 		return matchSet;
 	}
 }
