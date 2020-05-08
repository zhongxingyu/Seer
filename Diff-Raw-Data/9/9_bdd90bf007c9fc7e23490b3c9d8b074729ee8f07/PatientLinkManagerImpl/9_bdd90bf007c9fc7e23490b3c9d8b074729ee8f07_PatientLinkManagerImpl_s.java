 package org.patientview.radar.service.impl;
 
 import org.patientview.model.Patient;
 import org.patientview.model.PatientLink;
 import org.patientview.radar.dao.DemographicsDao;
 import org.patientview.radar.dao.PatientLinkDao;
 import org.patientview.radar.exception.PatientLinkException;
 import org.patientview.radar.service.PatientLinkManager;
 import org.patientview.radar.util.RadarUtility;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.StringUtils;
 
 /**
  * User: james@solidstategroup.com
  * Date: 14/11/13
  * Time: 17:04
  */
 public class PatientLinkManagerImpl implements PatientLinkManager {
 
     private final static Logger LOGGER = LoggerFactory.getLogger(PatientLinkManagerImpl.class);
 
     private DemographicsDao demographicsDao;
 
     private PatientLinkDao patientLinkDao;
 
     public PatientLink createLink(PatientLink patientLink) {
         return patientLinkDao.createLink(patientLink);
     }
 
     public PatientLink getPatientLink(String nhsNo, String unitCode) {
         return patientLinkDao.getPatientLink(nhsNo, unitCode);
     }
 
     public void setPatientLinkDao(PatientLinkDao patientLinkDao) {
         this.patientLinkDao = patientLinkDao;
     }
 
     // create the new patient record and link entity
     public Patient linkPatientRecord(Patient patient) throws PatientLinkException {
        try {
             PatientLink patientLink =  new PatientLink();
             patientLink.setSourceNhsNO(patient.getNhsno());
 
             String unitCode = patient.getUnitcode();
             if (StringUtils.isEmpty(unitCode)) {
                 if (patient.getRenalUnit() != null) {
                     unitCode = patient.getRenalUnit().getUnitCode();
                 }
             }
 
             patientLink.setSourceUnit(unitCode);
             patientLink.setDestinationNhsNo(patient.getNhsno());
             patientLink.setDestinationUnit(patient.getDiseaseGroup().getId());
 
             patientLinkDao.createLink(patientLink);
 
        } catch (Exception e) {
            LOGGER.error("Error creating link record for patient", e);
            throw new PatientLinkException("There has been an error creating the link record for the patient", e);
        }
 
        Patient newPatient = new Patient();
        newPatient.setNhsno(patient.getNhsno());
        if (patient.getDiseaseGroup() != null) {
             newPatient.setUnitcode(patient.getDiseaseGroup().getId());
        }
 
        return RadarUtility.mergePatientRecords(patient, newPatient);
     }
 
     public Patient getMergePatient(Patient sourcePatient) throws Exception {
         PatientLink patientLink = this.getPatientLink(sourcePatient.getNhsno(), sourcePatient.getUnitcode());
         Patient radarPatient = demographicsDao.getDemographicsByNhsNoAndUnitCode(patientLink.getDestinationNhsNo(),
                 patientLink.getDestinationUnit());
 
         return RadarUtility.mergePatientRecords(sourcePatient, radarPatient);
     }
 
 
     // Create bare patient record for Radar - reset the Id and remove fields that shouldn't be used
     public Patient createLinkRecord(Patient patient) {
 
         patient.setId(0L);
         RadarUtility.mergePatientRecords(patient, new Patient());
         patient.setUnitcode(patient.getDiseaseGroup().getId());
         return patient;
     }
 
     public void setDemographicsDao(DemographicsDao demographicsDao) {
         this.demographicsDao = demographicsDao;
     }
 
 }
