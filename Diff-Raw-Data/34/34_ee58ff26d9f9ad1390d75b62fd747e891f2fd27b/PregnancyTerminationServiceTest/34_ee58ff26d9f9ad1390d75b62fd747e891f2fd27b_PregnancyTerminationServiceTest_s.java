 package org.motechproject.ghana.national.service;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.service.request.PregnancyTerminationRequest;
 import org.motechproject.mrs.model.MRSObservation;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.util.DateUtil;
 import org.springframework.test.util.ReflectionTestUtils;
 import org.unitils.reflectionassert.ReflectionComparatorMode;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.domain.Constants.*;
 import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
 
 public class PregnancyTerminationServiceTest {
     private PregnancyTerminationService pregnancyTerminationService;
     @Mock
     private PatientService mockPatientService;
     @Mock
     private EncounterService mockEncounterService;
 
     @Mock
     private FacilityService mockFacilityService;
 
     @Before
     public void setUp() {
         initMocks(this);
         pregnancyTerminationService = new PregnancyTerminationService();
         ReflectionTestUtils.setField(pregnancyTerminationService, "patientService", mockPatientService);
         ReflectionTestUtils.setField(pregnancyTerminationService, "encounterService", mockEncounterService);
         ReflectionTestUtils.setField(pregnancyTerminationService, "facilityService", mockFacilityService);
     }
 
     @Test
     public void shouldDeceasePatientIfDeadDuringPregnancyTermination() {
         String motechId = "motechId";
         String staffId = "staffId";
         String facilityId = "facilityId";
         String mrsFacilityId = "mrsFacilityId";
         PregnancyTerminationRequest request = pregnancyTermination(motechId, staffId, facilityId);
         request.setDead(Boolean.TRUE);
 
         Patient mockPatient = mock(Patient.class);
         MRSPatient mockMRSPatient = mock(MRSPatient.class);
         when(mockPatientService.getPatientByMotechId(request.getMotechId())).thenReturn(mockPatient);
         when(mockPatient.getMrsPatient()).thenReturn(mockMRSPatient);
 
         Facility mockFacility=mock(Facility.class);
         when(mockFacilityService.getFacilityByMotechId(facilityId)).thenReturn(mockFacility);
         when(mockFacility.getMrsFacilityId()).thenReturn(mrsFacilityId);
 
         pregnancyTerminationService.terminatePregnancy(request);
 
         verify(mockPatientService).deceasePatient(request.getMotechId(), request.getTerminationDate(), OTHER_CAUSE_OF_DEATH, PREGNANCY_TERMINATION);
     }
 
     @Test
     public void shouldCreateEncounterOnPregnancyTermination() {
         String staffId = "staffId";
         String facilityId = "facilityId";
         String motechId = "motechId";
         String mrsFacilityId = "mrsFacilityId";
         final Date date = DateUtil.newDate(2000, 12, 12).toDate();
         final PregnancyTerminationRequest request = pregnancyTermination(motechId, staffId, facilityId);
         request.setTerminationDate(date);
 
         Patient mockPatient = mock(Patient.class);
         MRSPatient mockMRSPatient = mock(MRSPatient.class);
         when(mockPatientService.getPatientByMotechId(request.getMotechId())).thenReturn(mockPatient);
         when(mockPatient.getMrsPatient()).thenReturn(mockMRSPatient);
 
         Facility mockFacility=mock(Facility.class);
         when(mockFacilityService.getFacilityByMotechId(facilityId)).thenReturn(mockFacility);
         when(mockFacility.getMrsFacilityId()).thenReturn(mrsFacilityId);
 
         pregnancyTerminationService.terminatePregnancy(request);
         ArgumentCaptor<Set> captor = ArgumentCaptor.forClass(Set.class);
 
         verify(mockEncounterService).persistEncounter(eq(mockMRSPatient), eq(staffId), eq(mrsFacilityId), eq(ENCOUNTER_PREGTERMVISIT), eq(date), captor.capture());
 
         Set observations = captor.getValue();
 
         Set<MRSObservation> expectedObservations = new HashSet<MRSObservation>() {{
             add(new MRSObservation<Boolean>(date, CONCEPT_PREGNANCY_STATUS, false));
            add(new MRSObservation<String>(date, CONCEPT_TERMINATION_TYPE, "2"));
            add(new MRSObservation<String>(date, CONCEPT_TERMINATION_PROCEDURE, "1"));
            add(new MRSObservation<String>(date, CONCEPT_TERMINATION_COMPLICATION, "Bleeding"));
            add(new MRSObservation<String>(date, CONCEPT_TERMINATION_COMPLICATION, "Sepsis/Infection"));
             add(new MRSObservation<Boolean>(date, CONCEPT_MATERNAL_DEATH, false));
             add(new MRSObservation<Boolean>(date, CONCEPT_REFERRED, false));
             add(new MRSObservation<String>(date, CONCEPT_COMMENTS, "Patient lost lot of blood"));
             add(new MRSObservation<Boolean>(date, CONCEPT_POST_ABORTION_FP_COUNSELING, Boolean.FALSE));
             add(new MRSObservation<Boolean>(date, CONCEPT_POST_ABORTION_FP_ACCEPTED, Boolean.TRUE));
         }};
 
         assertReflectionEquals(expectedObservations, observations, ReflectionComparatorMode.LENIENT_ORDER);
     }
 
     private PregnancyTerminationRequest pregnancyTermination(String motechId, String staffId, String facilityId) {
         PregnancyTerminationRequest request = new PregnancyTerminationRequest();
         request.setFacilityId(facilityId);
         request.setMotechId(motechId);
         request.setStaffId(staffId);
         request.setDead(Boolean.FALSE);
         request.setReferred(Boolean.FALSE);
         request.setTerminationProcedure("1");
         request.setTerminationType("2");
         request.setComments("Patient lost lot of blood");
        request.addComplication("Bleeding");
        request.addComplication("Sepsis/Infection");
         request.setPostAbortionFPAccepted(Boolean.TRUE);
         request.setPostAbortionFPCounselling(Boolean.FALSE);
         return request;
     }
 
 }
