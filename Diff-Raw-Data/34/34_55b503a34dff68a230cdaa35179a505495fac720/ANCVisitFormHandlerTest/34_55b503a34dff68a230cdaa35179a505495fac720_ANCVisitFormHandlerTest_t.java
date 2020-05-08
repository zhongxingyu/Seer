 package org.motechproject.ghana.national.handlers;
 
import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.bean.ANCVisitForm;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.TTVaccineDosage;
import org.motechproject.ghana.national.service.FacilityService;
import org.motechproject.ghana.national.service.MotherVisitService;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.ghana.national.service.StaffService;
 import org.motechproject.ghana.national.vo.ANCVisit;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.mrs.model.MRSPerson;
 import org.motechproject.mrs.model.MRSUser;
import org.motechproject.util.DateUtil;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class ANCVisitFormHandlerTest {
 
     private ANCVisitFormHandler handler;
     @Mock
     private MotherVisitService mockMotherVisitService;
     @Mock
     private FacilityService mockFacilityService;
     @Mock
     private PatientService mockPatientService;
     @Mock
     private StaffService mockStaffService;
 
 
     @Before
     public void setUp() {
         initMocks(this);
         handler = new ANCVisitFormHandler();
         ReflectionTestUtils.setField(handler, "visitService", mockMotherVisitService);
         ReflectionTestUtils.setField(handler, "facilityService", mockFacilityService);
         ReflectionTestUtils.setField(handler, "patientService", mockPatientService);
         ReflectionTestUtils.setField(handler, "staffService", mockStaffService);
     }
 
     @Test
     public void shouldCreateANCVisitEncounterWithAllInfo() {
         String motechFacilityId = "232465";
         String staffId = "465";
         String motechId = "2321465";
 
         final ANCVisitForm ancVisitForm = new ANCVisitForm();
         ancVisitForm.setStaffId(staffId);
         ancVisitForm.setFacilityId(motechFacilityId);
         ancVisitForm.setMotechId(motechId);
         ancVisitForm.setDate(new Date());
         ancVisitForm.setSerialNumber("4ds65");
         ancVisitForm.setVisitNumber("4");
         ancVisitForm.setEstDeliveryDate(new Date(2012, 8, 8));
         ancVisitForm.setBpDiastolic(67);
         ancVisitForm.setBpSystolic(10);
         ancVisitForm.setWeight(65.67d);
         ancVisitForm.setComments("comments");
         ancVisitForm.setTtdose("4");
         ancVisitForm.setIptdose("56");
         ancVisitForm.setIptReactive("reactive");
         ancVisitForm.setItnUse("itn");
         ancVisitForm.setFht(4.3d);
         ancVisitForm.setFhr(4);
         ancVisitForm.setUrineTestGlucosePositive("glucose");
         ancVisitForm.setUrineTestProteinPositive("protein");
         ancVisitForm.setHemoglobin(13.8);
         ancVisitForm.setVdrlReactive("vdrl");
         ancVisitForm.setVdrlTreatment("treament");
         ancVisitForm.setDewormer("dewormer");
         ancVisitForm.setPmtct("pmctc");
         ancVisitForm.setPreTestCounseled("pre-test");
         ancVisitForm.setHivTestResult("hiv");
         ancVisitForm.setPostTestCounseled("post");
         ancVisitForm.setPmtctTreament("treatment");
         ancVisitForm.setLocation("location");
         ancVisitForm.setHouse("house");
         ancVisitForm.setCommunity("community");
         ancVisitForm.setReferred("referred");
         ancVisitForm.setMaleInvolved(false);
         ancVisitForm.setNextANCDate(new Date(2012, 2, 20));
 
 
         MotechEvent motechEvent = new MotechEvent("form.validation.successful.NurseDataEntry.ancVisit", new HashMap<String, Object>() {{
             put("formBean", ancVisitForm);
         }});
 
         String facilityId = "111";
         MRSFacility mrsFacility = new MRSFacility(facilityId);
         Patient patient = new Patient(new MRSPatient(motechId, new MRSPerson(), mrsFacility));
         MRSUser staff = new MRSUser();
         staff.id(staffId);
         Facility facility = new Facility(mrsFacility);
         facility.motechId(motechFacilityId);
 
         when(mockFacilityService.getFacilityByMotechId(motechFacilityId)).thenReturn(facility);
         when(mockPatientService.getPatientByMotechId(motechId)).thenReturn(patient);
         when(mockStaffService.getUserByEmailIdOrMotechId(staffId)).thenReturn(staff);
         
         handler.handleFormEvent(motechEvent);
 
        ArgumentCaptor<TTVaccineDosage> ttDosageCaptor = ArgumentCaptor.forClass(TTVaccineDosage.class);
        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        ArgumentCaptor<MRSUser> mrsUserCaptor = ArgumentCaptor.forClass(MRSUser.class);
        ArgumentCaptor<Facility> facilityCaptor = ArgumentCaptor.forClass(Facility.class);
        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(mockMotherVisitService).receivedTT(ttDosageCaptor.capture(), patientCaptor.capture(), mrsUserCaptor.capture(),
                facilityCaptor.capture(), dateCaptor.capture());
        assertThat(ttDosageCaptor.getValue().getDosage(), is(4));
        assertThat(patientCaptor.getValue().getMotechId(), is(motechId));
        assertThat(facilityCaptor.getValue().getMotechId(), is(motechFacilityId));
        assertThat(mrsUserCaptor.getValue().getId(), is(staffId));
        assertThat(dateCaptor.getValue(), is(DateUtil.today()));

         ArgumentCaptor<ANCVisit> captor = ArgumentCaptor.forClass(ANCVisit.class);
         verify(mockMotherVisitService).registerANCVisit(captor.capture());
         ANCVisit actualANCVisit = captor.getValue();
         assertEquals(ancVisitForm.getStaffId(), actualANCVisit.getStaff().getId());
         assertEquals(motechFacilityId, actualANCVisit.getFacility().getMotechId());
         assertEquals(ancVisitForm.getMotechId(), actualANCVisit.getPatient().getMotechId());
         assertEquals(ancVisitForm.getDate(), actualANCVisit.getDate());
         assertEquals(ancVisitForm.getSerialNumber(), actualANCVisit.getSerialNumber());
         assertEquals(ancVisitForm.getVisitNumber(), actualANCVisit.getVisitNumber());
         assertEquals(ancVisitForm.getEstDeliveryDate(), actualANCVisit.getEstDeliveryDate());
         assertEquals(ancVisitForm.getBpDiastolic(), actualANCVisit.getBpDiastolic());
         assertEquals(ancVisitForm.getBpSystolic(), actualANCVisit.getBpSystolic());
         assertEquals(ancVisitForm.getWeight(), actualANCVisit.getWeight());
         assertEquals(ancVisitForm.getComments(), actualANCVisit.getComments());
         assertEquals(ancVisitForm.getTtdose(), actualANCVisit.getTtdose());
         assertEquals(ancVisitForm.getIptdose(), actualANCVisit.getIptdose());
         assertEquals(ancVisitForm.getIptReactive(), actualANCVisit.getIptReactive());
         assertEquals(ancVisitForm.getItnUse(), actualANCVisit.getItnUse());
         assertEquals(ancVisitForm.getFht(), actualANCVisit.getFht());
         assertEquals(ancVisitForm.getFhr(), actualANCVisit.getFhr());
         assertEquals(ancVisitForm.getUrineTestGlucosePositive(), actualANCVisit.getUrineTestGlucosePositive());
         assertEquals(ancVisitForm.getUrineTestProteinPositive(), actualANCVisit.getUrineTestProteinPositive());
         assertEquals(ancVisitForm.getHemoglobin(), actualANCVisit.getHemoglobin());
         assertEquals(ancVisitForm.getVdrlReactive(), actualANCVisit.getVdrlReactive());
         assertEquals(ancVisitForm.getVdrlTreatment(), actualANCVisit.getVdrlTreatment());
         assertEquals(ancVisitForm.getDewormer(), actualANCVisit.getDewormer());
         assertEquals(ancVisitForm.getPmtct(), actualANCVisit.getPmtct());
         assertEquals(ancVisitForm.getPreTestCounseled(), actualANCVisit.getPreTestCounseled());
         assertEquals(ancVisitForm.getHivTestResult(), actualANCVisit.getHivTestResult());
         assertEquals(ancVisitForm.getPostTestCounseled(), actualANCVisit.getPostTestCounseled());
         assertEquals(ancVisitForm.getPmtctTreament(), actualANCVisit.getPmtctTreament());
         assertEquals(ancVisitForm.getLocation(), actualANCVisit.getLocation());
         assertEquals(ancVisitForm.getHouse(), actualANCVisit.getHouse());
         assertEquals(ancVisitForm.getCommunity(), actualANCVisit.getCommunity());
         assertEquals(ancVisitForm.getReferred(), actualANCVisit.getReferred());
         assertEquals(ancVisitForm.getMaleInvolved(), actualANCVisit.getMaleInvolved());
         assertEquals(ancVisitForm.getNextANCDate(), actualANCVisit.getNextANCDate());
     }
 }
