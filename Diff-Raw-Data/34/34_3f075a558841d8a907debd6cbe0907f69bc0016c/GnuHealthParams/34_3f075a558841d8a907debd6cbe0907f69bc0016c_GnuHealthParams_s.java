 package eu.neq.mais.connector.gnuhealth;
 
 import eu.neq.mais.domain.gnuhealth.TimeGnu;
 import eu.neq.mais.domain.gnuhealth.TimeGnuShort;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static java.lang.Integer.parseInt;
 
 public abstract class GnuHealthParams {
 
 
     public static Object[] getAppointmentParams(int[] ids, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 ids,
                 new String[]{"appointment_date", "doctor", "appointment_type", "urgency", "comments", "speciality.rec_name", "patient.rec_name", "patient", "consultations.rec_name"
                 },
                 "REPLACE_CONTEXT"};
     }
 
     public static Object[] getPathologyParams(int[] ids, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 ids,
                 new String[]{"code", "name", "category.rec_name"
                 },
                 "REPLACE_CONTEXT"};
     }
 
     public static Object[] getProcedureParams(int[] ids, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 ids,
                 new String[]{"description", "name"
                 },
                 "REPLACE_CONTEXT"};
     }
 
 
     public static Object[] getDocumentDataParams(int[] ids, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 ids,
                 new String[]{"data"},
                 "REPLACE_CONTEXT"};
     }
 
 
     public static Object[] getDocumentListParams(int[] ids, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 //ids,
                 new Object[]{new String[]{"resource",
                         "=",
                         new String("gnuhealth.patient," + ids[0])}},
                 0,
                 1000,
                 null,
                 //new String[] {//"resource", "=", "gnuhealth.patient", ids.toString()   //or type since data gives all data
                 new GnuMetaData(ids)};
     }
 
     public static Object[] getDocumentReadParams(int[] user, int[] ids, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 ids,
                new String[]{"data", "link", "description", "type" //or type since data gives all data
                 },
                 new GnuMetaData(user)};
     }
 
 
     public static Object[] getLabTestTypeParams(int[] ids, String adminSession) {
         return new Object[]{1, adminSession, ids,
                 new String[]{"id", "code", "name"}, "REPLACE_CONTEXT"};
     }
 
     public static Object[] getLabTestsResultsParams(int[] ids, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 ids,
                 new String[]{"id", "date_analysis", "test", "patient", "name",
                         "test.rec_name", "patient.rec_name", "requestor"},
                 "REPLACE_CONTEXT"};
     }
 
     public static Object[] getLabTestRequestParams(int[] ids, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 ids,
                 new String[]{"date", "patient_id", "state",
                         "doctor_id.rec_name", "name.rec_name",},
                 "REPLACE_CONTEXT"};
     }
 
     public static Object[] getDiagnoseCreationParams(Map<Object, Object> paramMap, int[] diagnoseIds, String adminSession) {
 
         if (!((paramMap.get("date_start_treatment")).equals(false))) {
             paramMap.put("date_start_treatment", new TimeGnuShort(Long.parseLong((String) paramMap.get("date_start_treatment"))));
         }
         if (!((paramMap.get("healed_date")).equals(false))) {
             paramMap.put("healed_date", new TimeGnuShort(Long.parseLong((String) paramMap.get("healed_date"))));
         }
         if (!((paramMap.get("date_stop_treatment")).equals(false))) {
             paramMap.put("date_stop_treatment", new TimeGnuShort(Long.parseLong((String) paramMap.get("date_stop_treatment"))));
         }
         if (!((paramMap.get("diagnosed_date")).equals(false))) {
             paramMap.put("diagnosed_date", new TimeGnuShort(Long.parseLong((String) paramMap.get("diagnosed_date"))));
         }
 
         Object patientId = paramMap.get("patient_id");
 
         paramMap.remove("patient_id");
 
         Object[] createContainer = new Object[2];
         createContainer[0] = "create";
         createContainer[1] = paramMap;
 
         Map<Object, Object> addMap = new HashMap<Object, Object>();
 
 
         if (diagnoseIds.length != 0) {
             addMap.put("diseases", new Object[]{new Object[]{"add", diagnoseIds}, createContainer});
         } else {
             addMap.put("diseases", new Object[]{new Object[]{"add", new Object[]{}}, createContainer});
         }
 
         return new Object[]{1, adminSession, new Object[]{patientId}, addMap, "REPLACE_CONTEXT"};
 
     }
 
     public static Object[] getLabTestRequestCreationParams(String date,
                                                            String doctor_id, String name, String patient_id, String adminSession) {
         Map<Object, Object> paramMap = new HashMap<Object, Object>();
         paramMap.put("date", new TimeGnu(new Long(date)));
         paramMap.put("doctor_id", doctor_id);
         paramMap.put("name", name);
         paramMap.put("patient_id", patient_id);
         return new Object[]{1, adminSession, paramMap, "REPLACE_CONTEXT"};
     }
 
     public static Object[] getLabTestsDetailParams(int[] ids, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 ids,
                 new String[]{"date_analysis", "test", "patient", "name",
                         "test.rec_name", "patient.rec_name", "date_requested",
                         "requestor.rec_name", "results", "pathologist.rec_name", "critearea",
                         "diagnosis"}, "REPLACE_CONTEXT"};
     }
 
     public static Object[] getLabTestCriteriaParams(int[] ids, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 ids,
                 new String[]{"name", "result_text", "remarks", "upper_limit",
                         "lower_limit", "result", "excluded", "units",
                         "warning", "units.rec_name"}, "REPLACE_CONTEXT"};
     }
 
     public static Object[] getMedicationParams(String id, String adminSession) {
 
         return new Object[]{
                 1,
                 adminSession,
                 new int[]{parseInt(id)},
                 new String[]{"course_completed", "discontinued", "dose",
                         "route", "duration_period", "frequency_unit",
                         "dose_unit", "frequency", "indication", "notes",
                         "is_active", "admin_times", "common_dosage",
                         "discontinued_reason", "duration", "form.rec_name",
                         "doctor.rec_name", "route.rec_name",
                         "dose_unit.rec_name", "indication.rec_name",
                         "common_dosage.rec_name", "medicament.rec_name",
                         "start_treatment", "end_treatment"}, "REPLACE_CONTEXT"};
     }
 
     public static Object[] getMedicationsParams(String adminSession, int[] medicationIds, int typeOfParams) {
 
         if (typeOfParams == 0) {
             return new Object[]{
                     1,
                     adminSession,
                     medicationIds,
                     new String[]{"course_completed", "discontinued", "dose",
                             "route", "duration_period", "frequency_unit",
                             "dose_unit", "frequency", "indication", "notes",
                             "is_active", "admin_times", "common_dosage",
                             "discontinued_reason", "duration", "form.rec_name",
                             "doctor.rec_name", "route.rec_name",
                             "dose_unit.rec_name", "indication.rec_name",
                             "common_dosage.rec_name", "medicament.rec_name",
                             "start_treatment", "end_treatment"}, "REPLACE_CONTEXT"};
         } else if (typeOfParams == 1) {
             return new Object[]{
                     1,
                     adminSession,
                     medicationIds,
                     new String[]{"create_date"}, "REPLACE_CONTEXT"};
         } else {
             return new Object[]{
                     1,
                     adminSession,
                     medicationIds,
                     new String[]{""}, "REPLACE_CONTEXT"};
         }
 
 
     }
 
 
     public static Object[] getVaccinationParams(String id, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 new int[]{parseInt(id)},
                 new String[]{"dose", "vaccine.rec_name", "observations",
                         "vaccine_lot", "institution.rec_name", "date",
                         "next_dose_date"}, "REPLACE_CONTEXT"};
     }
 
     public static Object[] getVaccinationsParams(String adminSession,
                                                  int[] vaccinationIntIds, int typeOfParams) {
 
         if (typeOfParams == 0) {
             return new Object[]{
                     1,
                     adminSession,
                     vaccinationIntIds,
                     new String[]{"dose", "vaccine.rec_name", "observations",
                             "vaccine_lot", "institution.rec_name", "date",
                             "next_dose_date"}, "REPLACE_CONTEXT"};
         } else if (typeOfParams == 1) {
             return new Object[]{
                     1,
                     adminSession,
                     vaccinationIntIds,
                     new String[]{"create_date"}, "REPLACE_CONTEXT"};
         } else {
             return new Object[]{
                     1,
                     adminSession,
                     vaccinationIntIds,
                     new String[]{""}, "REPLACE_CONTEXT"};
         }
     }
 
     public static Object[] getReturnPatientsParams(String adminSession, int[] allPatientIds, int typeOfParams) {
 
         if (typeOfParams == 0) {
             return new Object[]{
                     1,
                     adminSession,
                     allPatientIds,
                     new String[]{"rec_name", "dob", "diseases", "sex",
                             "primary_care_doctor.name",
                             "primary_care_doctor.rec_name"}, "REPLACE_CONTEXT"};
         } else if (typeOfParams == 1) {
             return new Object[]{
                     1,
                     adminSession,
                     allPatientIds,
                     new String[]{"create_date"}, "REPLACE_CONTEXT"};
         } else {
             return new Object[]{
                     1,
                     adminSession,
                     allPatientIds,
                     new String[]{""}, "REPLACE_CONTEXT"};
         }
 
     }
 
     public static Object[] getReturnDiagnoseParams(int id, String adminSession) {
         return new Object[]{
                 1,
                 adminSession,
                 new int[]{id},
                 new String[]{"status", "pregnancy_warning", "is_active",
                         "short_comment", "diagnosed_date", "healed_date",
                         "pathology", "disease_severity", "is_infectious",
                         "is_allergy", "pathology.rec_name",
                         "date_start_treatment", "doctor", "age",
                         "weeks_of_pregnancy", "is_on_treatment",
                         "treatment_description", "extra_info",
                         "date_stop_treatment", "pcs_code", "allergy_type",
                         "doctor.rec_name", "pcs_code.rec_name"},
                 "REPLACE_CONTEXT"};
     }
 
     public static Object[] getReturnDiagnosesParams(String adminSession, int[] diagnoseIds, int typeOfParams) {
 
         if (typeOfParams == 0) {
             return new Object[]{
                     1,
                     adminSession,
                     diagnoseIds,
                     new String[]{"status", "pregnancy_warning", "is_active",
                             "short_comment", "diagnosed_date", "healed_date",
                             "pathology", "disease_severity", "is_infectious",
                             "is_allergy", "pathology.rec_name",
                             "date_start_treatment", "doctor", "age",
                             "weeks_of_pregnancy", "is_on_treatment",
                             "treatment_description", "extra_info",
                             "date_stop_treatment", "pcs_code", "allergy_type",
                             "doctor.rec_name", "pcs_code.rec_name"},
                     "REPLACE_CONTEXT"};
         } else if (typeOfParams == 1) {
             return new Object[]{
                     1,
                     adminSession,
                     diagnoseIds,
                     new String[]{"create_date"},
                     "REPLACE_CONTEXT"};
         } else {
             return new Object[]{
                     1,
                     adminSession,
                     diagnoseIds,
                     new String[]{""}, "REPLACE_CONTEXT"};
         }
 
     }
 
 
 }
