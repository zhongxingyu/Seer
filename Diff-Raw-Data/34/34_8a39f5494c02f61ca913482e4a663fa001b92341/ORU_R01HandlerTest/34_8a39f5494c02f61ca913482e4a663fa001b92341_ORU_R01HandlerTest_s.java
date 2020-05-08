 package org.openmrs.module.pacsintegration.handler;
 
 import ca.uhn.hl7v2.HL7Exception;
 import ca.uhn.hl7v2.app.ApplicationException;
 import ca.uhn.hl7v2.model.Message;
 import ca.uhn.hl7v2.model.v23.message.ACK;
 import ca.uhn.hl7v2.parser.Parser;
 import ca.uhn.hl7v2.parser.PipeParser;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentMatcher;
 import org.openmrs.Concept;
 import org.openmrs.ConceptSource;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.Provider;
 import org.openmrs.User;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.ProviderService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emrapi.EmrApiProperties;
 import org.openmrs.module.pacsintegration.PacsIntegrationConstants;
 import org.openmrs.module.pacsintegration.PacsIntegrationProperties;
 import org.openmrs.module.radiologyapp.RadiologyOrder;
 import org.openmrs.module.radiologyapp.RadiologyReport;
 import org.openmrs.module.radiologyapp.RadiologyService;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.lessThan;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.powermock.api.mockito.PowerMockito.mockStatic;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(Context.class)
 public class ORU_R01HandlerTest {
 
     private ORU_R01Handler handler;
 
     private AdministrationService adminService;
 
     private PatientService patientService;
 
     private ConceptService conceptService;
 
     private RadiologyService radiologyService;
 
     private ProviderService providerService;
 
     private LocationService locationService;
 
     private EmrApiProperties emrApiProperties;
 
     private PacsIntegrationProperties pacsIntegrationProperties;
 
     private PatientIdentifierType primaryIdentifierType = new PatientIdentifierType();
 
     private Provider principalResultsInterpreter = new Provider();
 
     private Concept reportTypeFinal = new Concept();
 
     @Before
     public void setup() {
 
         adminService = mock(AdministrationService.class);
         when(adminService.getGlobalProperty(PacsIntegrationConstants.GP_SENDING_FACILITY)).thenReturn("openmrs_mirebalais");
 
         patientService = mock(PatientService.class);
         radiologyService = mock(RadiologyService.class);
         conceptService = mock(ConceptService.class);
         providerService = mock(ProviderService.class);
         locationService = mock(LocationService.class);
 
         emrApiProperties = mock(EmrApiProperties.class);
         when(emrApiProperties.getPrimaryIdentifierType()).thenReturn(primaryIdentifierType);
 
         pacsIntegrationProperties = mock(PacsIntegrationProperties.class);
         ConceptSource loinc = new ConceptSource();
         loinc.setName("LOINC");
         when(pacsIntegrationProperties.getProcedureCodesConceptSource()).thenReturn(loinc);
         when(pacsIntegrationProperties.getReportTypeFinalConcept()).thenReturn(reportTypeFinal);
 
         User authenticatedUser = new User();
         mockStatic(Context.class);
         when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
 
         handler = new ORU_R01Handler();
         handler.setAdminService(adminService);
         handler.setPatientService(patientService);
         handler.setConceptService(conceptService);
         handler.setRadiologyService(radiologyService);
         handler.setProviderService(providerService);
         handler.setLocationService(locationService);
         handler.setEmrApiProperties(emrApiProperties);
         handler.setPacsIntegrationProperties(pacsIntegrationProperties);
     }
 
     @Test
     public void shouldReturnErrorACKIfNoPatientIdentifierInResponse() throws HL7Exception, ApplicationException {
 
         String message = "MSH|^~\\&|HMI||RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                 "PID|1||||Patient^Test^||19770222|M||||||||||\r" +
                 "PV1|1||||||||||||||||||\r" +
                 "OBR|1||0000001297|36554-4^CHEST|||20130228170556||||||||||||MBL^CR||||||F|||||||&Goodrich&Mark&&&&^M123||||20130228170556\r" +
                 "OBX|1|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|2|TX|36554-4&BODY^CHEST||Clinical Indication: ||||||F\r" +
                 "OBX|3|TX|36554-4&BODY^CHEST||test x-ray.||||||F\r" +
                 "OBX|4|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|5|TX|36554-4&BODY^CHEST||A test final report!!||||||F\r" +
                 "OBX|6|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|7|TX|36554-4&BODY^CHEST||Findings:  Posteroanterior and lateral chest radiographs were obtained.  The ||||||F\r" +
                 "OBX|8|TX|36554-4&BODY^CHEST||lungs are well inflated.  No infiltrate, pneumonia, or pulmonary edema is ||||||F\r" +
                 "OBX|9|TX|36554-4&BODY^CHEST||present.  The cardiac and mediastinal structures appear normal.  The pleural ||||||F\r" +
                 "OBX|10|TX|36554-4&BODY^CHEST||spaces and bony structures are normal.||||||F\r" +
                "OBX|11|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|12|TX|36554-4&BODY^CHEST||Summary:  Normal chest radiographs.||||||F\r";
 
         ACK ack = (ACK) handler.processMessage(parseMessage(message));
 
         assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AR"));
         assertThat(ack.getMSA().getTextMessage().getValue(), is("Cannot import message. No patient identifier specified."));
     }
 
     @Test
     public void shouldReturnErrorACKIfNoPatientWithPatientIdentifier() throws HL7Exception, ApplicationException {
 
         when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                 .thenReturn(new ArrayList<Patient>());
 
 
         String message = "MSH|^~\\&|HMI||RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                 "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                 "PV1|1||||||||||||||||||\r" +
                 "OBR|1||0000001297|36554-4^CHEST|||20130228170556||||||||||||MBL^CR||||||F|||||||&Goodrich&Mark&&&&^M123||||20130228170556\r" +
                 "OBX|1|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|2|TX|36554-4&BODY^CHEST||Clinical Indication: ||||||F\r" +
                 "OBX|3|TX|36554-4&BODY^CHEST||test x-ray.||||||F\r" +
                 "OBX|4|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|5|TX|36554-4&BODY^CHEST||A test final report!!||||||F\r" +
                 "OBX|6|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|7|TX|36554-4&BODY^CHEST||Findings:  Posteroanterior and lateral chest radiographs were obtained.  The ||||||F\r" +
                 "OBX|8|TX|36554-4&BODY^CHEST||lungs are well inflated.  No infiltrate, pneumonia, or pulmonary edema is ||||||F\r" +
                 "OBX|9|TX|36554-4&BODY^CHEST||present.  The cardiac and mediastinal structures appear normal.  The pleural ||||||F\r" +
                 "OBX|10|TX|36554-4&BODY^CHEST||spaces and bony structures are normal.||||||F\r" +
                "OBX|11|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|12|TX|36554-4&BODY^CHEST||Summary:  Normal chest radiographs.||||||F\r";
 
         ACK ack = (ACK) handler.processMessage(parseMessage(message));
 
         assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AR"));
         assertThat(ack.getMSA().getTextMessage().getValue(), is("Cannot import message. No patient with identifier GG2F98"));
     }
 
     @Test
     public void shouldReturnErrorACKIfPatientIdentifierAndAccessionNumberDontMatchSamePatient() throws HL7Exception, ApplicationException {
 
         Patient patient = new Patient(1);
         Patient anotherPatient = new Patient(2);
 
         RadiologyOrder radiologyOrder = new RadiologyOrder();
         radiologyOrder.setPatient(anotherPatient);
 
         when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                 .thenReturn(Collections.singletonList(patient));
 
         when(radiologyService.getRadiologyOrderByAccessionNumber("0000001297")).thenReturn(radiologyOrder);
 
         String message = "MSH|^~\\&|HMI||RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                 "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                 "PV1|1||||||||||||||||||\r" +
                 "OBR|1||0000001297|36554-4^CHEST|||20130228170556||||||||||||MBL^CR||||||F|||||||M123&Goodrich&Mark&&&&||||20130228170556\r" +
                 "OBX|1|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|2|TX|36554-4&BODY^CHEST||Clinical Indication: ||||||F\r" +
                 "OBX|3|TX|36554-4&BODY^CHEST||test x-ray.||||||F\r" +
                 "OBX|4|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|5|TX|36554-4&BODY^CHEST||A test final report!!||||||F\r" +
                 "OBX|6|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|7|TX|36554-4&BODY^CHEST||Findings:  Posteroanterior and lateral chest radiographs were obtained.  The ||||||F\r" +
                 "OBX|8|TX|36554-4&BODY^CHEST||lungs are well inflated.  No infiltrate, pneumonia, or pulmonary edema is ||||||F\r" +
                 "OBX|9|TX|36554-4&BODY^CHEST||present.  The cardiac and mediastinal structures appear normal.  The pleural ||||||F\r" +
                 "OBX|10|TX|36554-4&BODY^CHEST||spaces and bony structures are normal.||||||F\r" +
                "OBX|11|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|12|TX|36554-4&BODY^CHEST||Summary:  Normal chest radiographs.||||||F\r";
 
         ACK ack = (ACK) handler.processMessage(parseMessage(message));
 
         assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AR"));
         assertThat(ack.getMSA().getTextMessage().getValue(), is("Cannot import message. Patient referenced in message different from patient attached to existing order."));
     }
 
     @Test
     public void shouldReturnErrorACKIfProcedureNotFound() throws HL7Exception, ApplicationException {
 
         Patient patient = new Patient(1);
         RadiologyOrder radiologyOrder = new RadiologyOrder();
         radiologyOrder.setPatient(patient);
 
         when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                 .thenReturn(Collections.singletonList(patient));
         when(radiologyService.getRadiologyOrderByAccessionNumber("0000001297")).thenReturn(radiologyOrder);
         when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(null);
 
         String message = "MSH|^~\\&|HMI||RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                 "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                 "PV1|1||||||||||||||||||\r" +
                 "OBR|1||0000001297|36554-4^CHEST|||20130228170556||||||||||||MBL^CR||||||F|||||||M123&Goodrich&Mark&&&&||||20130228170556\r" +
                 "OBX|1|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|2|TX|36554-4&BODY^CHEST||Clinical Indication: ||||||F\r" +
                 "OBX|3|TX|36554-4&BODY^CHEST||test x-ray.||||||F\r" +
                 "OBX|4|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|5|TX|36554-4&BODY^CHEST||A test final report!!||||||F\r" +
                 "OBX|6|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|7|TX|36554-4&BODY^CHEST||Findings:  Posteroanterior and lateral chest radiographs were obtained.  The ||||||F\r" +
                 "OBX|8|TX|36554-4&BODY^CHEST||lungs are well inflated.  No infiltrate, pneumonia, or pulmonary edema is ||||||F\r" +
                 "OBX|9|TX|36554-4&BODY^CHEST||present.  The cardiac and mediastinal structures appear normal.  The pleural ||||||F\r" +
                 "OBX|10|TX|36554-4&BODY^CHEST||spaces and bony structures are normal.||||||F\r" +
                "OBX|11|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|12|TX|36554-4&BODY^CHEST||Summary:  Normal chest radiographs.||||||F\r";
 
         ACK ack = (ACK) handler.processMessage(parseMessage(message));
 
         assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AR"));
         assertThat(ack.getMSA().getTextMessage().getValue(), is("Cannot import message. Procedure code not recognized."));
     }
 
     @Test
     public void shouldSaveReportEncounterAndSendACK() throws HL7Exception, ApplicationException {
 
         Patient patient = new Patient(1);
         RadiologyOrder radiologyOrder = new RadiologyOrder();
         radiologyOrder.setPatient(patient);
         Concept procedure = new Concept();
         Location reportLocation = new Location();
 
         when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                 .thenReturn(Collections.singletonList(patient));
         when(radiologyService.getRadiologyOrderByAccessionNumber("0000001297")).thenReturn(radiologyOrder);
         when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);
         when(providerService.getProviderByIdentifier("M123")).thenReturn(principalResultsInterpreter);
         when(locationService.getLocation("Mirebalais Hospital")).thenReturn(reportLocation);
 
         String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                 "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                 "PV1|1||||||||||||||||||\r" +
                 "OBR|1||0000001297|36554-4^CHEST|||20130228170556||||||||||||MBL^CR||||||F|||||||M123&Goodrich&Mark&&&&||||20130228170556\r" +
                 "OBX|1|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|2|TX|36554-4&BODY^CHEST||Clinical Indication: ||||||F\r" +
                 "OBX|3|TX|36554-4&BODY^CHEST||test x-ray.||||||F\r" +
                 "OBX|4|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|5|TX|36554-4&BODY^CHEST||A test final report!!||||||F\r" +
                 "OBX|6|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|7|TX|36554-4&BODY^CHEST||Findings:  Posteroanterior and lateral chest radiographs were obtained.  The ||||||F\r" +
                 "OBX|8|TX|36554-4&BODY^CHEST||lungs are well inflated.  No infiltrate, pneumonia, or pulmonary edema is ||||||F\r" +
                 "OBX|9|TX|36554-4&BODY^CHEST||present.  The cardiac and mediastinal structures appear normal.  The pleural ||||||F\r" +
                 "OBX|10|TX|36554-4&BODY^CHEST||spaces and bony structures are normal.||||||F\r" +
                "OBX|11|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|12|TX|36554-4&BODY^CHEST||Summary:  Normal chest radiographs.||||||F\r";
 
         ACK ack = (ACK) handler.processMessage(parseMessage(message));
 
         assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));
 
         RadiologyReport expectedReport = new RadiologyReport();
         expectedReport.setPatient(patient);
         expectedReport.setAccessionNumber("0000001297");
         expectedReport.setAssociatedRadiologyOrder(radiologyOrder);
         expectedReport.setPrincipalResultsInterpreter(principalResultsInterpreter);
         expectedReport.setProcedure(procedure);
         expectedReport.setReportType(reportTypeFinal);
         expectedReport.setReportLocation(reportLocation);
         expectedReport.setReportBody(buildExpectedReportBody());
 
         Calendar cal = Calendar.getInstance();
         cal.set(2013,1,28);
         cal.set(Calendar.HOUR_OF_DAY, 17);
         cal.set(Calendar.MINUTE, 05);
         cal.set(Calendar.SECOND, 56);
         cal.set(Calendar.MILLISECOND, 00);
         expectedReport.setReportDate(cal.getTime()) ;
 
         verify(radiologyService).saveRadiologyReport(argThat(new IsExpectedRadiologyReport(expectedReport)));
     }
 
     @Test
     public void shouldNotFailIfProviderOrLocationOrOrderIsNotFound() throws HL7Exception, ApplicationException {
 
         Patient patient = new Patient(1);
         RadiologyOrder radiologyOrder = new RadiologyOrder();
         radiologyOrder.setPatient(patient);
         Concept procedure = new Concept();
         Location reportLocation = new Location();
 
         when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                 .thenReturn(Collections.singletonList(patient));
         when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);
 
         // mimick not finding a matching provider, location or order
         when(radiologyService.getRadiologyOrderByAccessionNumber("0000001297")).thenReturn(null);
         when(providerService.getProviderByIdentifier("M123")).thenReturn(null);
         when(locationService.getLocation("Mirebalais Hospital")).thenReturn(null);
 
         String message = "MSH|^~\\&|HMI||RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                 "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                 "PV1|1||||||||||||||||||\r" +
                 "OBR|1||0000001297|36554-4^CHEST|||20130228170556||||||||||||MBL^CR||||||F|||||||||||20130228170556\r" +
                 "OBX|1|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|2|TX|36554-4&BODY^CHEST||Clinical Indication: ||||||F\r" +
                 "OBX|3|TX|36554-4&BODY^CHEST||test x-ray.||||||F\r" +
                 "OBX|4|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|5|TX|36554-4&BODY^CHEST||A test final report!!||||||F\r" +
                 "OBX|6|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|7|TX|36554-4&BODY^CHEST||Findings:  Posteroanterior and lateral chest radiographs were obtained.  The ||||||F\r" +
                 "OBX|8|TX|36554-4&BODY^CHEST||lungs are well inflated.  No infiltrate, pneumonia, or pulmonary edema is ||||||F\r" +
                 "OBX|9|TX|36554-4&BODY^CHEST||present.  The cardiac and mediastinal structures appear normal.  The pleural ||||||F\r" +
                 "OBX|10|TX|36554-4&BODY^CHEST||spaces and bony structures are normal.||||||F\r" +
                "OBX|11|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|12|TX|36554-4&BODY^CHEST||Summary:  Normal chest radiographs.||||||F\r";
 
         ACK ack = (ACK) handler.processMessage(parseMessage(message));
 
         assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));
 
         RadiologyReport expectedReport = new RadiologyReport();
         expectedReport.setPatient(patient);
         expectedReport.setAccessionNumber("0000001297");
         expectedReport.setAssociatedRadiologyOrder(null);
         expectedReport.setPrincipalResultsInterpreter(null);
         expectedReport.setProcedure(procedure);
         expectedReport.setReportType(reportTypeFinal);
         expectedReport.setReportLocation(null);
         expectedReport.setReportBody(buildExpectedReportBody());
 
         Calendar cal = Calendar.getInstance();
         cal.set(2013,1,28);
         cal.set(Calendar.HOUR_OF_DAY, 17);
         cal.set(Calendar.MINUTE, 05);
         cal.set(Calendar.SECOND, 56);
         cal.set(Calendar.MILLISECOND, 00);
         expectedReport.setReportDate(cal.getTime()) ;
 
         verify(radiologyService).saveRadiologyReport(argThat(new IsExpectedRadiologyReport(expectedReport))) ;
     }
 
 
     @Test
     // to handle time synchronization issues that may exist between PACS and OpenMRS
     public void shouldNotFailIfDatetimeInFutureByLessThanFiveMinutes() throws HL7Exception, ApplicationException {
 
         Patient patient = new Patient(1);
         RadiologyOrder radiologyOrder = new RadiologyOrder();
         radiologyOrder.setPatient(patient);
         Concept procedure = new Concept();
         Location reportLocation = new Location();
 
         when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                 .thenReturn(Collections.singletonList(patient));
         when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);
 
         // mimick not finding a matching provider, location or order
         when(radiologyService.getRadiologyOrderByAccessionNumber("0000001297")).thenReturn(null);
         when(providerService.getProviderByIdentifier("M123")).thenReturn(null);
         when(locationService.getLocation("Mirebalais Hospital")).thenReturn(null);
 
         // create a report time that is 4 minutes in the future
         DateTime date = new DateTime();
         DateTime futureTime = date.plusMinutes(4);
         String futureTimeString = DateTimeFormat.forPattern("yyyyMMddHHmmss").print(futureTime);
 
         String message = "MSH|^~\\&|HMI||RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                 "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                 "PV1|1||||||||||||||||||\r" +
                 "OBR|1||0000001297|36554-4^CHEST|||" + futureTimeString + "||||||||||||MBL^CR||||||F|||||||||||" + futureTimeString +"\r" +
                 "OBX|1|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|2|TX|36554-4&BODY^CHEST||Clinical Indication: ||||||F\r" +
                 "OBX|3|TX|36554-4&BODY^CHEST||test x-ray.||||||F\r" +
                 "OBX|4|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|5|TX|36554-4&BODY^CHEST||A test final report!!||||||F\r" +
                 "OBX|6|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|7|TX|36554-4&BODY^CHEST||Findings:  Posteroanterior and lateral chest radiographs were obtained.  The ||||||F\r" +
                 "OBX|8|TX|36554-4&BODY^CHEST||lungs are well inflated.  No infiltrate, pneumonia, or pulmonary edema is ||||||F\r" +
                 "OBX|9|TX|36554-4&BODY^CHEST||present.  The cardiac and mediastinal structures appear normal.  The pleural ||||||F\r" +
                 "OBX|10|TX|36554-4&BODY^CHEST||spaces and bony structures are normal.||||||F\r" +
                "OBX|11|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|12|TX|36554-4&BODY^CHEST||Summary:  Normal chest radiographs.||||||F\r";
 
         ACK ack = (ACK) handler.processMessage(parseMessage(message));
 
         assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));
 
         verify(radiologyService).saveRadiologyReport(argThat(new HasReportDateBetween(date.toDate(), new Date()))) ;
     }
 
 
     @Test
     public void shouldReturnErrorACKIfReportDateMoreThanFiveMinutesInFuture() throws HL7Exception, ApplicationException {
 
         Patient patient = new Patient(1);
         RadiologyOrder radiologyOrder = new RadiologyOrder();
         radiologyOrder.setPatient(patient);
 
         when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                 .thenReturn(Collections.singletonList(patient));
         when(radiologyService.getRadiologyOrderByAccessionNumber("0000001297")).thenReturn(radiologyOrder);
         when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(null);
 
         String message = "MSH|^~\\&|HMI||RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                 "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                 "PV1|1||||||||||||||||||\r" +
                 "OBR|1||0000001297|36554-4^CHEST|||30000228170556||||||||||||MBL^CR||||||F|||||||M123&Goodrich&Mark&&&&||||30000228170556\r" +
                 "OBX|1|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|2|TX|36554-4&BODY^CHEST||Clinical Indication: ||||||F\r" +
                 "OBX|3|TX|36554-4&BODY^CHEST||test x-ray.||||||F\r" +
                 "OBX|4|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|5|TX|36554-4&BODY^CHEST||A test final report!!||||||F\r" +
                 "OBX|6|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|7|TX|36554-4&BODY^CHEST||Findings:  Posteroanterior and lateral chest radiographs were obtained.  The ||||||F\r" +
                 "OBX|8|TX|36554-4&BODY^CHEST||lungs are well inflated.  No infiltrate, pneumonia, or pulmonary edema is ||||||F\r" +
                 "OBX|9|TX|36554-4&BODY^CHEST||present.  The cardiac and mediastinal structures appear normal.  The pleural ||||||F\r" +
                 "OBX|10|TX|36554-4&BODY^CHEST||spaces and bony structures are normal.||||||F\r" +
                "OBX|11|TX|36554-4&BODY^CHEST||||||||F\r" +
                 "OBX|12|TX|36554-4&BODY^CHEST||Summary:  Normal chest radiographs.||||||F\r";
 
         ACK ack = (ACK) handler.processMessage(parseMessage(message));
 
         assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AR"));
         assertThat(ack.getMSA().getTextMessage().getValue(), is("Date cannot be more than 5 minutes in the future."));
     }
 
 
 
     private Message parseMessage(String message) throws HL7Exception {
         Parser parser = new PipeParser();
         return parser.parse(message);
     }
 
     private String buildExpectedReportBody() {
                 return
                 "\r\n" +
                 "Clinical Indication: \r\n" +
                 "test x-ray.\r\n" +
                 "\r\n" +
                 "A test final report!!\r\n" +
                 "\r\n" +
                 "Findings:  Posteroanterior and lateral chest radiographs were obtained.  The \r\n" +
                 "lungs are well inflated.  No infiltrate, pneumonia, or pulmonary edema is \r\n" +
                 "present.  The cardiac and mediastinal structures appear normal.  The pleural \r\n" +
                 "spaces and bony structures are normal.\r\n" +
                 "\r\n" +
                "Summary:  Normal chest radiographs.\r\n";
     }
 
     public class IsExpectedRadiologyReport extends ArgumentMatcher<RadiologyReport> {
 
         private RadiologyReport expectedReport;
 
         public IsExpectedRadiologyReport(RadiologyReport radiologyReport){
             this.expectedReport = radiologyReport;
         }
 
         @Override
         public boolean matches(Object o) {
             RadiologyReport report = (RadiologyReport) o;
 
             assertThat(report.getPatient(), is(expectedReport.getPatient()));
             assertThat(report.getAccessionNumber(), is(expectedReport.getAccessionNumber()));
             assertThat(report.getAssociatedRadiologyOrder(), is(expectedReport.getAssociatedRadiologyOrder()));
             assertThat(report.getProcedure(), is(expectedReport.getProcedure()));
             assertThat(report.getReportDate(), is(expectedReport.getReportDate()));
             assertThat(report.getReportType(), is(expectedReport.getReportType()));
             assertThat(report.getReportLocation(), is(expectedReport.getReportLocation()));
             assertThat(report.getReportBody(), is(expectedReport.getReportBody()));
 
             return true;
         }
     }
 
     public class HasReportDateBetween extends ArgumentMatcher<RadiologyReport> {
 
         private Date lowerRange;
 
         private Date upperRange;
 
         public HasReportDateBetween(Date lowerRange, Date upperRange) {
             this.lowerRange = lowerRange;
             this.upperRange = upperRange;
         }
 
         @Override
         public boolean matches(Object o) {
             RadiologyReport report = (RadiologyReport) o;
 
             assertThat(report.getReportDate(), greaterThan(lowerRange));
             assertThat(report.getReportDate(), lessThan(upperRange));
 
             return true;
         }
 
     }
 
 }
