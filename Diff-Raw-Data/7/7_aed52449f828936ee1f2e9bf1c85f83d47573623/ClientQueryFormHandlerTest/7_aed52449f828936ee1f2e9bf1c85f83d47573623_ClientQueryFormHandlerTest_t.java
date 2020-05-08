 package org.motechproject.ghana.national.handlers;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.bean.ClientQueryForm;
 import org.motechproject.ghana.national.domain.*;
 import org.motechproject.ghana.national.repository.AllObservations;
 import org.motechproject.ghana.national.repository.SMSGateway;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.mrs.model.*;
 import org.motechproject.util.DateUtil;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import java.text.DateFormat;
 import java.util.*;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.configuration.TextMessageTemplateVariables.*;
 
 public class ClientQueryFormHandlerTest {
     private ClientQueryFormHandler clientQueryFormHandler;
     @Mock
     private PatientService mockPatientService;
     @Mock
     private SMSGateway mockSmsGateway;
     @Mock
     private AllObservations mockAllObservations;
 
     @Before
     public void setUp() {
         initMocks(this);
         clientQueryFormHandler = new ClientQueryFormHandler();
         ReflectionTestUtils.setField(clientQueryFormHandler, "patientService", mockPatientService);
         ReflectionTestUtils.setField(clientQueryFormHandler, "allObservations", mockAllObservations);
         ReflectionTestUtils.setField(clientQueryFormHandler, "smsGateway", mockSmsGateway);
     }
 
     @Test
     public void shouldSendMessageWithEDDForPregnantClientForClientDetailsQuery() {
         String firstName = "firstName";
         String lastName = "lastName";
         Date dateOfBirth = DateUtil.now().minusYears(20).toDate();
         Integer age = 30;
         String gender = "Male";
         String phoneNumber = "phoneNumber";
         String responsePhoneNumber = "responsePhoneNumber";
         Date edd = DateUtil.now().toDate();
 
         String facilityId = "facilityId";
         String motechId = "motechId";
         String staffId = "staffId";
         Map<String, Object> params = createMotechEventWithForm(facilityId, motechId, staffId, responsePhoneNumber, ClientQueryType.CLIENT_DETAILS.toString());
 
         MRSPerson person = person(phoneNumber, dateOfBirth, age, gender, lastName, firstName);
         Patient patient = new Patient(new MRSPatient(motechId, person, new MRSFacility(facilityId)));
 
         when(mockPatientService.getPatientByMotechId(motechId)).thenReturn(patient);
         when(mockAllObservations.findObservation(motechId, Concept.PREGNANCY.getName())).thenReturn(mockPregnancyObservationWithEDD(edd));
 
         clientQueryFormHandler.handleFormEvent(new MotechEvent("form.validation.successful.NurseQuery.clientQuery", params));
 
         ArgumentCaptor<Map> templateValuesCaptor = ArgumentCaptor.forClass(Map.class);
         verify(mockSmsGateway).dispatchSMS(eq("PREGNANT_CLIENT_QUERY_RESPONSE_SMS_KEY"), templateValuesCaptor.capture(), eq(responsePhoneNumber));
 
         Map<String, String> messageParams = templateValuesCaptor.getValue();
         assertThat(messageParams.get(PHONE_NUMBER), is(phoneNumber));
         assertThat(messageParams.get(AGE), is(age.toString()));
         assertThat(messageParams.get(GENDER), is(gender));
         assertThat(messageParams.get(FIRST_NAME), is(firstName));
         assertThat(messageParams.get(LAST_NAME), is(lastName));
         assertThat(messageParams.get(DATE), is(DateFormat.getDateInstance().format(edd)));
         assertThat(messageParams.get(DOB), is(DateFormat.getDateInstance().format(dateOfBirth)));
 
 
     }
 
     @Test
     public void shouldSendMessageForFindClientIDQuery() {
         final String firstName = "firstName";
         final String lastName = "lastName";
         final Date dateOfBirth = DateUtil.now().minusYears(20).toDate();
         String dateString=DateFormat.getDateInstance().format(dateOfBirth);
         String phoneNumber = "phoneNumber";
         String responsePhoneNumber = "responsePhoneNumber";
         final String facilityId = "facilityId";
         final String motechId = "motechId";
         final MRSFacility mrsFacility = new MRSFacility(facilityId, "name", null, null, null, null);
 
         HashMap<String, Object> params = createClientQueryFormForFindClientId(firstName, lastName, dateOfBirth, phoneNumber, responsePhoneNumber, facilityId, null);
 
         ArrayList<MRSPatient> patients = new ArrayList<MRSPatient>() {{
             add(new MRSPatient(motechId, new MRSPerson().lastName(lastName).firstName(firstName).dateOfBirth(dateOfBirth).gender("F"), mrsFacility));
             add(new MRSPatient("45423", new MRSPerson().lastName(lastName).firstName("first").gender("M").dateOfBirth(new Date(1989, 5, 6)), mrsFacility));
         }};
         when(mockPatientService.getPatients(firstName, lastName, phoneNumber, dateOfBirth, null)).thenReturn(patients);
         when(mockSmsGateway.getSMSTemplate(ClientQueryFormHandler.FIND_CLIENT_RESPONSE_SMS_KEY)).thenReturn("MoTeCH ID=${motechId},${firstName},${lastName}, Sex=${gender}, DoB=${dob}, ${facility}");
 
         clientQueryFormHandler.handleFormEvent(new MotechEvent("form.validation.successful.NurseQuery.clientQuery", params));
 
         ArgumentCaptor<String> templateValuesCaptor = ArgumentCaptor.forClass(String.class);
         verify(mockSmsGateway).dispatchSMS(eq(responsePhoneNumber), templateValuesCaptor.capture());
 
        String actualMessage = templateValuesCaptor.getValue();
        assertTrue(actualMessage.contains(motechId));
        assertTrue(actualMessage.contains(firstName));
     }
 
     @Test
     public void shouldSendNoMatchingRecordsFoundIfNoRecordsFound() {
         final String firstName = "firstName";
         final String lastName = "lastName";
         final Date dateOfBirth = DateUtil.now().minusYears(20).toDate();
         String phoneNumber = "phoneNumber";
         String responsePhoneNumber = "responsePhoneNumber";
         final String facilityId = "facilityId";
 
         HashMap<String, Object> params = createClientQueryFormForFindClientId(firstName, lastName, dateOfBirth, phoneNumber, responsePhoneNumber, facilityId, null);
 
         when(mockPatientService.getPatients(firstName, lastName, phoneNumber, dateOfBirth, null)).thenReturn(new ArrayList<MRSPatient>());
         when(mockSmsGateway.getSMSTemplate(ClientQueryFormHandler.FIND_CLIENT_RESPONSE_SMS_KEY)).thenReturn("MoTeCH ID=${motechId}, FirstName=${firstName}, LastName=${lastName}, Sex=${gender}, DoB=${dob}, Facility=${facility}");
 
         clientQueryFormHandler.handleFormEvent(new MotechEvent("form.validation.successful.NurseQuery.clientQuery", params));
 
         ArgumentCaptor<String> templateValuesCaptor = ArgumentCaptor.forClass(String.class);
         verify(mockSmsGateway).dispatchSMS(eq(responsePhoneNumber), templateValuesCaptor.capture());
 
         assertEquals(Constants.NO_MATCHING_RECORDS_FOUND, templateValuesCaptor.getValue());
     }
 
     private HashMap<String, Object> createClientQueryFormForFindClientId(String firstName, String lastName, Date dateOfBirth, String phoneNumber, String responsePhoneNumber, String facilityId, String motechId) {
         final ClientQueryForm clientQueryForm = clientQueryForm(facilityId, motechId, "323", responsePhoneNumber, ClientQueryType.FIND_CLIENT_ID.toString());
         clientQueryForm.setFirstName(firstName);
         clientQueryForm.setLastName(lastName);
         clientQueryForm.setDateOfBirth(dateOfBirth);
         clientQueryForm.setPhoneNumber(phoneNumber);
         clientQueryForm.setNhis(null);
 
         return new HashMap<String, Object>() {{
             put(Constants.FORM_BEAN, clientQueryForm);
         }};
     }
 
     private MRSObservation mockPregnancyObservationWithEDD(Date edd) {
         MRSObservation<Concept> pregnancyObservation = new MRSObservation<Concept>(DateUtil.now().toDate(), Concept.PREGNANCY.getName(), null);
         MRSObservation eddObservation = new MRSObservation<Date>(DateUtil.now().toDate(), Concept.EDD.getName(), edd);
         pregnancyObservation.setDependantObservations(new HashSet<MRSObservation>(asList(eddObservation)));
         return pregnancyObservation;
     }
 
     private MRSPerson person(String phoneNumber, Date dateOfBirth, int age, String gender, String lastName, String firstName) {
         MRSPerson person = new MRSPerson();
         person.dateOfBirth(dateOfBirth);
         person.firstName(firstName);
         person.lastName(lastName);
         person.age(age);
         person.gender(gender);
         person.attributes(asList(new Attribute(PatientAttributes.PHONE_NUMBER.getAttribute(), phoneNumber)));
         return person;
     }
 
     private Map<String, Object> createMotechEventWithForm(String facilityId, String motechId, String staffId, String responsePhoneNumber, String clientQueryType) {
         final ClientQueryForm clientQueryForm = clientQueryForm(facilityId, motechId, staffId, responsePhoneNumber, clientQueryType);
         return new HashMap<String, Object>() {{
             put(Constants.FORM_BEAN, clientQueryForm);
         }};
     }
 
     private ClientQueryForm clientQueryForm(String facilityId, String motechId, String staffId, String responsePhoneNumber, String clientQueryType) {
         ClientQueryForm clientQueryForm = new ClientQueryForm();
         clientQueryForm.setFacilityId(facilityId);
         clientQueryForm.setMotechId(motechId);
         clientQueryForm.setStaffId(staffId);
         clientQueryForm.setSender(responsePhoneNumber);
         clientQueryForm.setQueryType(clientQueryType);
         return clientQueryForm;
     }
 }
