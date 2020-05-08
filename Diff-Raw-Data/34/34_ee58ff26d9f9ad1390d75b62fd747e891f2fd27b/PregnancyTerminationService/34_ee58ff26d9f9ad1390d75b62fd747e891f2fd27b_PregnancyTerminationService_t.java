 package org.motechproject.ghana.national.service;
 
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.service.request.PregnancyTerminationRequest;
 import org.motechproject.mrs.model.MRSObservation;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.motechproject.openmrs.advice.LoginAsAdmin;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import static org.motechproject.ghana.national.domain.Constants.*;
import static org.motechproject.ghana.national.tools.Utility.safePareInteger;
 
 @Service
 public class PregnancyTerminationService {
 
     public static final String PREGNANCY_TERMINATION = "Pregnancy Termination";
     public static final String OTHER_CAUSE_OF_DEATH = "OTHER";
     @Autowired
     PatientService patientService;
 
     @Autowired
     EncounterService encounterService;
 
     @Autowired
     FacilityService facilityService;
 
     @LoginAsAdmin
     @ApiSession
     public void terminatePregnancy(PregnancyTerminationRequest request) {
         MRSPatient mrsPatient = patientService.getPatientByMotechId(request.getMotechId()).getMrsPatient();
         Facility facility = facilityService.getFacilityByMotechId(request.getFacilityId());
         encounterService.persistEncounter(mrsPatient, request.getStaffId(), facility.getMrsFacilityId(), Constants.ENCOUNTER_PREGTERMVISIT, request.getTerminationDate(), prepareObservations(request));
         if (request.isDead())
             patientService.deceasePatient(request.getMotechId(), request.getTerminationDate(), OTHER_CAUSE_OF_DEATH, PREGNANCY_TERMINATION);
     }
 
     private Set<MRSObservation> prepareObservations(PregnancyTerminationRequest request) {
         Set<MRSObservation> mrsObservations = new HashSet<MRSObservation>();

        for (String complication : request.getComplications()) {
            addObservation(request.getTerminationDate(), mrsObservations, CONCEPT_TERMINATION_COMPLICATION, safePareInteger(complication));
        }
         addObservation(request.getTerminationDate(), mrsObservations, CONCEPT_PREGNANCY_STATUS, false);
        addObservation(request.getTerminationDate(), mrsObservations, CONCEPT_TERMINATION_TYPE, safePareInteger(request.getTerminationType()));
        addObservation(request.getTerminationDate(), mrsObservations, CONCEPT_TERMINATION_PROCEDURE, safePareInteger(request.getTerminationProcedure()));
         addObservation(request.getTerminationDate(), mrsObservations, CONCEPT_MATERNAL_DEATH, request.isDead());
         addObservation(request.getTerminationDate(), mrsObservations, CONCEPT_REFERRED, request.isReferred());
         addObservation(request.getTerminationDate(), mrsObservations, CONCEPT_COMMENTS, request.getComments());
         addObservation(request.getTerminationDate(), mrsObservations, CONCEPT_POST_ABORTION_FP_ACCEPTED, request.getPostAbortionFPAccepted());
         addObservation(request.getTerminationDate(), mrsObservations, CONCEPT_POST_ABORTION_FP_COUNSELING, request.getPostAbortionFPCounselling());
         return mrsObservations;
     }
 
     private <T> void addObservation(Date observationDate, Set<MRSObservation> mrsObservations, String observationType, T observationValue) {
         if (observationValue != null)
             mrsObservations.add(new MRSObservation<T>(observationDate, observationType, observationValue));
     }
 }
