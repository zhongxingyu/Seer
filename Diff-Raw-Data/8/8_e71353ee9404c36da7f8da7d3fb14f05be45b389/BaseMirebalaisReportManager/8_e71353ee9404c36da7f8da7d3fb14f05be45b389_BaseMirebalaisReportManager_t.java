 package org.openmrs.module.mirebalaisreports.definitions;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Location;
 import org.openmrs.OpenmrsObject;
 import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.DispositionService;
 import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
 import org.openmrs.module.reporting.evaluation.parameter.Parameter;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.Date;
 
 /**
  * Includes helpful methods for dealing with Mirebalais Metadata (this class exists so that someday we might consider
  * moving BaseReportManager into a shared refapp module)
  */
 public abstract class BaseMirebalaisReportManager extends BaseReportManager {
 
     public static final String SQL_DIR = "org/openmrs/module/mirebalaisreports/sql/";
 
     protected final Log log = LogFactory.getLog(getClass());
 
     @Autowired
 	MirebalaisReportsProperties mirebalaisReportsProperties;
 
     @Autowired
    DispositionService dispositionService;

    @Autowired
     EmrApiProperties emrApiProperties;
 
     public abstract String getUuid();
 
     public void setMirebalaisReportsProperties(MirebalaisReportsProperties mirebalaisReportsProperties) {
         this.mirebalaisReportsProperties = mirebalaisReportsProperties;
     }
 
     public Parameter getStartDateParameter() {
         return new Parameter("startDate", "mirebalaisreports.parameter.startDate", Date.class);
     }
 
     public Parameter getEndDateParameter() {
         return new Parameter("endDate", "mirebalaisreports.parameter.endDate", Date.class);
     }
 
     public Parameter getLocationParameter() {
         return new Parameter("location", "mirebalaisreports.parameter.location", Location.class);
     }
 
     protected String applyMetadataReplacements(String sql) {
         log.debug("Replacing metadata references");
         MirebalaisReportsProperties mrp = mirebalaisReportsProperties;
 
         sql = replace(sql, "zlId", mrp.getZlEmrIdentifierType());
         sql = replace(sql, "dosId", mrp.getDossierNumberIdentifierType());
         sql = replace(sql, "hivId", mrp.getHivEmrIdentifierType());
 
         sql = replace(sql, "testPt", mrp.getTestPatientPersonAttributeType());
         sql = replace(sql, "phoneType", mrp.getTelephoneNumberPersonAttributeType());
 
         sql = replace(sql, "regEnc", mrp.getRegistrationEncounterType());
         sql = replace(sql, "chkEnc", mrp.getCheckInEncounterType());
         sql = replace(sql, "vitEnc", mrp.getVitalsEncounterType());
         sql = replace(sql, "consEnc", mrp.getConsultEncounterType());
         sql = replace(sql, "radEnc", mrp.getRadiologyOrderEncounterType());
         sql = replace(sql, "paid", mrp.getAmountPaidConcept());
         sql = replace(sql, "wt", mrp.getWeightConcept());
         sql = replace(sql, "ht", mrp.getHeightConcept());
         sql = replace(sql, "muac", mrp.getMuacConcept());
         sql = replace(sql, "temp", mrp.getTemperatureConcept());
         sql = replace(sql, "hr", mrp.getPulseConcept());
         sql = replace(sql, "rr", mrp.getRespiratoryRateConcept());
         sql = replace(sql, "sbp", mrp.getSystolicBpConcept());
         sql = replace(sql, "dbp", mrp.getDiastolicBpConcept());
         sql = replace(sql, "o2", mrp.getBloodOxygenSaturationConcept());
         sql = replace(sql, "coded", mrp.getCodedDiagnosisConcept());
         sql = replace(sql, "noncoded", mrp.getNonCodedDiagnosisConcept());
         sql = replace(sql, "comment", mrp.getClinicalImpressionsConcept());
         sql = replace(sql, "notifiable", mrp.getSetOfWeeklyNotifiableDiseases());
         sql = replace(sql, "urgent", mrp.getSetOfUrgentDiseases());
         sql = replace(sql, "santeFamn", mrp.getSetOfWomensHealthDiagnoses());
         sql = replace(sql, "psycho", mrp.getSetOfPsychologicalDiagnoses());
         sql = replace(sql, "peds", mrp.getSetOfPediatricDiagnoses());
         sql = replace(sql, "outpatient", mrp.getSetOfOutpatientDiagnoses());
         sql = replace(sql, "ncd", mrp.getSetOfNcdDiagnoses());
         sql = replace(sql, "notDx", mrp.getSetOfNonDiagnoses());
         sql = replace(sql, "ed", mrp.getSetOfEmergencyDiagnoses());
         sql = replace(sql, "ageRst", mrp.getSetOfAgeRestrictedDiagnoses());
        sql = replace(sql, "dispo", dispositionService.getDispositionDescriptor().getDispositionConcept());
 
         log.debug("Replacing metadata references complete.");
         return sql;
     }
 
     protected String replace(String sql, String oldValue, OpenmrsObject newValue) {
         String s = sql.replace(":" + oldValue, newValue.getId().toString());
         return s;
     }
 
 }
