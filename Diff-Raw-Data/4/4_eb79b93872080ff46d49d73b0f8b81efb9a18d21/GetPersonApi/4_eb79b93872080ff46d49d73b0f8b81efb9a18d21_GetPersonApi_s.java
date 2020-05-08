 package edu.psu.iam.cpr.core.api;
 
 import java.text.ParseException;
 
 import javax.jms.JMSException;
 
 import org.json.JSONException;
 
 import edu.psu.iam.cpr.core.api.helper.ApiHelper;
 import edu.psu.iam.cpr.core.api.returns.PersonServiceReturn;
 import edu.psu.iam.cpr.core.database.Database;
 import edu.psu.iam.cpr.core.database.tables.AddressesTable;
 import edu.psu.iam.cpr.core.database.tables.DateOfBirthTable;
 import edu.psu.iam.cpr.core.database.tables.EmailAddressTable;
 import edu.psu.iam.cpr.core.database.tables.NamesTable;
 import edu.psu.iam.cpr.core.database.tables.PersonAffiliationTable;
 import edu.psu.iam.cpr.core.database.tables.PersonGenderTable;
 import edu.psu.iam.cpr.core.database.tables.PhonesTable;
 import edu.psu.iam.cpr.core.database.tables.PsuIdTable;
 import edu.psu.iam.cpr.core.database.tables.UseridTable;
 import edu.psu.iam.cpr.core.database.tables.validate.ValidatePerson;
 import edu.psu.iam.cpr.core.error.CprException;
 import edu.psu.iam.cpr.core.error.ReturnType;
 import edu.psu.iam.cpr.core.service.helper.ServiceCoreReturn;
 import edu.psu.iam.cpr.core.service.returns.AddressReturn;
 import edu.psu.iam.cpr.core.service.returns.AffiliationReturn;
 import edu.psu.iam.cpr.core.service.returns.DateOfBirthReturn;
 import edu.psu.iam.cpr.core.service.returns.EmailAddressReturn;
 import edu.psu.iam.cpr.core.service.returns.GenderReturn;
 import edu.psu.iam.cpr.core.service.returns.NameReturn;
 import edu.psu.iam.cpr.core.service.returns.PhoneReturn;
 import edu.psu.iam.cpr.core.service.returns.PsuIdReturn;
 import edu.psu.iam.cpr.core.service.returns.UseridReturn;
 
 /**
  * This class provides the implementation for the Get Person API.
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
  * @package edu.psu.iam.cpr.core.api
  * @author $Author$
  * @version $Rev$
  * @lastrevision $Date$
  */
 public class GetPersonApi extends ExtendedBaseApi {
 
 	/** Contains the index for the return history parameter */
 	private static final int RETURN_HISTORY = 0;
 
     /**
      * This method is used to execute the core logic for a service.
      * @param apiName contains the name of the api.
      * @param db contains a open database session.
      * @param personId contains the person identifier value.
      * @param updatedBy contains the userid requesting this information.
      * @param otherParameters contains an array of Java objects that are additional parameters for the service.
      * @return will return an object if successful.
      * @throws CprException will be thrown if there are any problems.
      * @throws JSONException will be thrown if there are any issues creating a JSON message.
      * @throws ParseException will be thrown if there are any issues related to parsing a data value.
      */	
 	@Override
 	public Object runApi(String apiName, Database db, ServiceCoreReturn serviceCoreReturn,
 			String updatedBy, Object[] otherParameters,
 			boolean checkAuthorization) throws CprException, JSONException,
 			ParseException, JMSException {
 		
 		final long personId = serviceCoreReturn.getPersonId();
 		
 		// Validate the return history parameter.
 		String returnHistory = (String) otherParameters[RETURN_HISTORY];
 		if (returnHistory == null) {
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, "Return history");
 		}
 		boolean returnHistoryFlag = (returnHistory.equals("Y")) ? true : false;
 		
 		// Validate the person parameters.
 		ValidatePerson.validatePersonParameters(db, personId, updatedBy);
 
 		PersonServiceReturn personServiceReturn = new PersonServiceReturn();
 
 		personServiceReturn.setStatusCode(ReturnType.SUCCESS.index());
 		personServiceReturn.setStatusMessage(ApiHelper.SUCCESS_MESSAGE);
 
 		// Get the DOB.
 		final DateOfBirthTable dateOfBirthTable = new DateOfBirthTable();
 		dateOfBirthTable.setReturnHistoryFlag(returnHistoryFlag);
 		final DateOfBirthReturn dateOfBirthReturn[] = dateOfBirthTable.getDateOfBirthForPersonId(db, 
 				personId);
 		personServiceReturn.setNumberOfDateOfBirthdays(dateOfBirthReturn.length);
 		personServiceReturn.setDateOfBirthRecord(dateOfBirthReturn);
 
 		// Get the Gender.
 		final PersonGenderTable perGenderRelTable = new PersonGenderTable();
 		perGenderRelTable.setReturnHistoryFlag(returnHistoryFlag);
 		final GenderReturn genderReturn[] = perGenderRelTable.getGenderForPersonId(db, 
 				personId);
 		personServiceReturn.setNumberOfGenders(genderReturn.length);
 		personServiceReturn.setGenderReturnRecord(genderReturn);
 
 		// Get the PSU ID.
 		final PsuIdTable psuIdTable = new PsuIdTable();
 		psuIdTable.setReturnHistoryFlag(returnHistoryFlag);
 		final PsuIdReturn psuIdReturn[] = psuIdTable.getPsuIdForPersonId(db, 
 				personId);
 		personServiceReturn.setNumberOfPsuIds(psuIdReturn.length);
 		personServiceReturn.setPsuIdReturnRecord(psuIdReturn);
 
 		// Get the names.
 		final NamesTable namesTable = new NamesTable();
 		namesTable.setReturnHistoryFlag(returnHistoryFlag);
 		final NameReturn nameReturn[] = namesTable.getNamesForPersonId(db, personId);
 		personServiceReturn.setNumberOfNames(nameReturn.length);
 		personServiceReturn.setNameReturnRecord(nameReturn);
 
 		// Get the addresses.
 		final AddressesTable addressesTable = new AddressesTable();
 		addressesTable.setReturnHistoryFlag(returnHistoryFlag);
 		final AddressReturn addressReturn[] = addressesTable.getAddress(db, personId);
 		personServiceReturn.setNumberOfAddresses(addressReturn.length);
 		personServiceReturn.setAddressReturnRecord(addressReturn);
 
 		// Get the phones.
 		final PhonesTable phonesTable = new PhonesTable();
 		phonesTable.setReturnHistoryFlag(returnHistoryFlag);
 		final PhoneReturn phoneReturn[] = phonesTable.getPhones(db, personId);
 		personServiceReturn.setNumberOfPhones(phoneReturn.length);
 		personServiceReturn.setPhoneReturnRecord(phoneReturn);
 
 		// Get the email addresses.
 		final EmailAddressTable emailAddressTable = new EmailAddressTable();
 		emailAddressTable.setReturnHistoryFlag(returnHistoryFlag);
 		final EmailAddressReturn emailAddressReturn[] = emailAddressTable.getEmailAddressForPersonId(db, personId);			
 		personServiceReturn.setNumberOfEmailAddresses(emailAddressReturn.length);
 		personServiceReturn.setEmailAddressReturnRecord(emailAddressReturn);
 
 		// Get the userids.
 		final UseridTable useridTable = new UseridTable();
 		useridTable.setReturnHistoryFlag(returnHistoryFlag);
 		final UseridReturn useridReturn[] = useridTable.getUseridsForPersonId(db, personId);
 		personServiceReturn.setNumberOfUserids(useridReturn.length);
 		personServiceReturn.setUseridReturnRecord(useridReturn);
 
 
 		// Get the affiliations.
 		final PersonAffiliationTable affiliationsTable = new PersonAffiliationTable();
 		affiliationsTable.setReturnHistoryFlag(returnHistoryFlag);
 		final AffiliationReturn affiliationReturn[] = affiliationsTable.getAllAffiliationsForPersonId(db, personId);
 		personServiceReturn.setNumberOfAffiliations(affiliationReturn.length);
 		personServiceReturn.setAffiliationReturnRecord(affiliationReturn);
 		
 		return (Object) personServiceReturn;
 
 	}
 
 }
