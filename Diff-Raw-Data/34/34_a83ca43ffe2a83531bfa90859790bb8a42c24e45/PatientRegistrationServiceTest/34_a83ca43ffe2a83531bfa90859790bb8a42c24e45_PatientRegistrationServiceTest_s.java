 package org.openmrs.module.patientregistration.service;
 
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.openmrs.Location;
 import org.openmrs.LocationAttribute;
 import org.openmrs.LocationAttributeType;
 import org.openmrs.Patient;
 import org.openmrs.module.emr.EmrProperties;
 import org.openmrs.module.emr.printer.Printer;
 import org.openmrs.module.emr.printer.PrinterService;
 import org.openmrs.module.emr.printer.UnableToPrintViaSocketException;
 import org.openmrs.module.patientregistration.PatientRegistrationUtil;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import java.util.UUID;
 
 import static org.mockito.Mockito.doNothing;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.powermock.api.mockito.PowerMockito.mockStatic;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(PatientRegistrationUtil.class)
 public class PatientRegistrationServiceTest {
 
     private PatientRegistrationServiceImpl patientRegistrationService;
 
     private PrinterService printerService;
 
     private EmrProperties emrProperties;
 
     @Before
     public void setup() {
         patientRegistrationService = spy(new PatientRegistrationServiceImpl());
         printerService = mock(PrinterService.class);
         patientRegistrationService.setPrinterService(printerService);
         emrProperties = mock(EmrProperties.class);
         patientRegistrationService.setEmrProperties(emrProperties);
     }
 
 
     @Test
     public void printIdCardLabel_shouldCallMethodToPrintIdCardLabel() throws UnableToPrintViaSocketException {
 
         Location location = new Location(1);
         Patient patient = new Patient(1);
         Printer printer = new Printer();
         printer.setId(1);
 
         when(printerService.getDefaultPrinter(location, Printer.Type.LABEL)).thenReturn(printer);
         doNothing().when(patientRegistrationService).printIdCardLabelUsingZPL(patient, printer);
 
         patientRegistrationService.printIDCardLabel(patient, location);
 
         verify(patientRegistrationService).printIdCardLabelUsingZPL(patient, printer);
     }
 
     @Test
     public void printIdCard_shouldCallMethodToPrintIdCard() throws UnableToPrintViaSocketException {
 
         Location location = new Location(1);
         Location medicalRecordLocation = new Location(2);
         Patient patient = new Patient(1);
         Printer printer = new Printer();
         printer.setId(1);
 
         mockStatic(PatientRegistrationUtil.class);
         when(PatientRegistrationUtil.getMedicalRecordLocationRecursivelyBasedOnTag(location)).thenReturn(medicalRecordLocation);
         when(printerService.getDefaultPrinter(location, Printer.Type.ID_CARD)).thenReturn(printer);
         doNothing().when(patientRegistrationService).printIdCardUsingEPCL(patient, printer, medicalRecordLocation);
 
         patientRegistrationService.printIDCard(patient, location);
 
         verify(patientRegistrationService).printIdCardUsingEPCL(patient, printer, medicalRecordLocation);
 
     }
 
     @Test
     public void printRegistrationLabel_shouldCallMethodToPrintRegistrationLabel() throws UnableToPrintViaSocketException {
 
         Location location = new Location(1);
        Location medicalRecordLocation = new Location(2);
         Patient patient = new Patient(1);
        Printer printer = new Printer();
        printer.setId(1);

        mockStatic(PatientRegistrationUtil.class);
        when(PatientRegistrationUtil.getMedicalRecordLocationRecursivelyBasedOnTag(location)).thenReturn(medicalRecordLocation);
        when(printerService.getDefaultPrinter(location, Printer.Type.LABEL)).thenReturn(printer);
        doNothing().when(patientRegistrationService).printRegistrationLabelUsingZPL(patient, printer, medicalRecordLocation, 2);
 
        patientRegistrationService.printRegistrationLabel(patient, location, 2);
 
        verify(patientRegistrationService).printRegistrationLabelUsingZPL(patient, printer, medicalRecordLocation, 2);
     }
 
     @Test
     public void getNameToPrintOnIdCardShouldReturnNameToPrintOnIdCard() {
 
         LocationAttributeType nameToPrintOnIdCardType = new LocationAttributeType();
         nameToPrintOnIdCardType.setId(1);
         nameToPrintOnIdCardType.setUuid(UUID.randomUUID().toString());
 
         Location location = new Location(1);
         location.setName("Regular name");
 
         LocationAttribute nameToPrintOnIdCard = new LocationAttribute();
         nameToPrintOnIdCard.setAttributeType(nameToPrintOnIdCardType);
         nameToPrintOnIdCard.setValue("Name to print");
         location.setAttribute(nameToPrintOnIdCard);
 
         when(emrProperties.getLocationAttributeTypeNameToPrintOnIdCard()).thenReturn(nameToPrintOnIdCardType);
 
         Assert.assertEquals("Name to print", patientRegistrationService.getNameToPrintOnIdCard(location));
     }
 
     @Test
     public void getNameToPrintOnIdCardShouldReturnDisplayNameIfNameToDisplayOnIdCardAttributeDefined() {
 
         LocationAttributeType nameToPrintOnIdCardType = new LocationAttributeType();
         nameToPrintOnIdCardType.setId(1);
         nameToPrintOnIdCardType.setUuid(UUID.randomUUID().toString());
 
         Location location = new Location(1);
         location.setName("Regular name");
 
         when(emrProperties.getLocationAttributeTypeNameToPrintOnIdCard()).thenReturn(nameToPrintOnIdCardType);
 
         Assert.assertEquals("Regular name", patientRegistrationService.getNameToPrintOnIdCard(location));
     }
 }
