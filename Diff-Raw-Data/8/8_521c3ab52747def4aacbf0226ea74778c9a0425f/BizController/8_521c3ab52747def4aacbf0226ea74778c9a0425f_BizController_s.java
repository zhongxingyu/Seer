 package medoffice;
 
 import jsftoolkit.ejb.CrudService;
 import com.semanticprogrammer.lib.commons.Util;
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 import jsftoolkit.controller.AccessController;
 import jsftoolkit.util.FacesUtils;
 import medoffice.entity.Allergy;
 import medoffice.entity.Appointment;
 import medoffice.entity.Cvx;
 import medoffice.entity.Diagnosis;
 import medoffice.entity.Drug;
 import medoffice.entity.Guarantor;
 import medoffice.entity.Insurance;
 import medoffice.entity.MyOffice;
 import medoffice.entity.OfficeProvider;
 import medoffice.entity.OfficeSchedule;
 import medoffice.entity.Patient;
 import medoffice.entity.PatientAllergy;
 import medoffice.entity.PatientAuthorization;
 import medoffice.entity.PatientDiagnosis;
 import medoffice.entity.PatientEvent;
 import medoffice.entity.PatientFamilyHistory;
 import medoffice.entity.PatientHistoricData;
 import medoffice.entity.PatientHistoryExamTest;
 import medoffice.entity.PatientHistoryProblem;
 import medoffice.entity.PatientImmunization;
 import medoffice.entity.PatientInsurance;
 import medoffice.entity.PatientMedication;
 import medoffice.entity.PatientNote;
 import medoffice.entity.PatientPastMedicalHistory;
 import medoffice.entity.PatientRecordLog;
 import medoffice.entity.PatientSocialHistory;
 import medoffice.entity.PatientStatus;
 import medoffice.entity.PaymentMethod;
 import medoffice.entity.PaymentType;
 import medoffice.entity.Pharmacy;
 import medoffice.entity.Procedure;
 import medoffice.entity.ProcedureCptModifierSet;
 import medoffice.entity.Provider;
 import medoffice.entity.ProviderAppointmentCategory;
 import medoffice.entity.ProviderInsurance;
 import medoffice.entity.ProviderSchedule;
 import medoffice.entity.Referral;
 import medoffice.entity.ReferralFacility;
 import medoffice.entity.ReferralProcedure;
 import medoffice.entity.ReferralResult;
 import medoffice.entity.ReferringProvider;
 import medoffice.entity.ServicePaymentType;
 import medoffice.entity.Specialty;
 import medoffice.entity.Visit;
 import medoffice.entity.VisitCategory;
 import medoffice.entity.VisitCc;
 import medoffice.entity.VisitDiagnosis;
 import medoffice.entity.VisitDoc;
 import medoffice.entity.VisitExam;
 import medoffice.entity.VisitHpi;
 import medoffice.entity.VisitPayment;
 import medoffice.entity.VisitPfsh;
 import medoffice.entity.VisitProcedure;
 import medoffice.entity.VisitProgressNote;
 import medoffice.entity.VisitProgressNoteAddendum;
 import medoffice.entity.VisitRos;
 import medoffice.entity.VisitServiceLevel;
 import medoffice.entity.VisitTemplateState;
 import medoffice.entity.VisitVitals;
 import medoffice.entity.ZipCode;
 import medoffice.schedule.AppointmentTime;
 import medoffice.schedule.ScheduleUtil;
 import medoffice.schedule.TimePeriod;
 import medoffice.schedule.TimePeriodManager;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.LocalTime;
 
 @ManagedBean(name = "biz")
 @SessionScoped
 public class BizController {
 
    @EJB
    private CrudService crudService;
    private Map<Integer, List<VisitDoc>> visitDocListMap = null;
    private Long generatedId = 0L;
 
    public ReferringProvider referringProviderByOfficeProvider(OfficeProvider officeProvider) {
       if (officeProvider.getRole() != null && officeProvider.getRole().equals("self")) {
          Map params = new HashMap();
          params.put("npi", officeProvider.getAssociatedProvider().getNpi());
          List referringProviderList = crudService.findByNamedQuery("ReferringProvider.findByNpi", params, 1, "_MEDBASE");
          if (referringProviderList != null && !referringProviderList.isEmpty()) {
             return (ReferringProvider) referringProviderList.get(0);
          }
       }
       return null;
    }
 
    public Object findByCode(String entityName, String code) {
       Map params = new HashMap();
       params.put("code", code);
       List resultList = crudService.findByNamedQuery(entityName + ".findByCode", params);
       if (!resultList.isEmpty()) {
          return resultList.get(0);
       } else {
          return null;
       }
    }
 
    public MyOffice myOffice(String code) {
       return crudService.find(code, MyOffice.class);
    }
 
    public void selectAll(Map selectMap, List list) {
       for (Object obj : list) {
          selectMap.put(obj, true);
       }
    }
 
    public void shrinkMap(Map selectMap, List list) {
       if (selectMap == null || selectMap.isEmpty()) {
          return;
       }
       for (Object key : selectMap.keySet()) {
          if (list.contains(key)) {
          } else {
             selectMap.remove(key);
          }
       }
    }
 
    public List dummyList(int size) {
       List list = new ArrayList();
       if (size <= 0) {
          return list;
       }
       for (int i = 0; i < size; i++) {
          list.add(i);
       }
       return list;
    }
 
    public String removeFromPointers(String digit, String pointers) {
       if (pointers == null || digit == null || !digit.matches("\\d")) {
          return null;
       }
       return ("," + pointers + ",").replaceFirst("(?<=\\D)" + digit + "(?=\\D)", "").replaceAll("^,+|,+$", "").replaceAll(",+", ",");
    }
 
    public String addToPointers(String digit, String pointers) {
       if (pointers == null || pointers.equals("")) {
          return digit;
       }
       return pointers + "," + digit;
    }
 
    public List<Date> dateList(Date date, int count) {
       List<Date> dateList = new ArrayList();
       if (date == null) {
          return null;
       }
       Date next = new Date(date.getTime());
       for (int i = 0; i < count; i++) {
          dateList.add(next);
          next = new Date(next.getTime() + 24L * 3600L * 1000L);
       }
       return dateList;
    }
 
    public List appointmentListByDateProvider(int providerId, Date date) {
       Calendar startCurrent = new GregorianCalendar();
       startCurrent.setTime(date);
       startCurrent.set(Calendar.HOUR_OF_DAY, 0);
       startCurrent.set(Calendar.MINUTE, 0);
       startCurrent.set(Calendar.SECOND, 0);
       startCurrent.set(Calendar.MILLISECOND, 0);
       Calendar endCurrent = new GregorianCalendar();
       endCurrent.setTime(date);
       endCurrent.set(Calendar.HOUR_OF_DAY, 23);
       endCurrent.set(Calendar.MINUTE, 59);
       endCurrent.set(Calendar.SECOND, 59);
       endCurrent.set(Calendar.MILLISECOND, 0);
       Map params = new HashMap();
       params.put("providerId", providerId);
       params.put("startDate", startCurrent.getTime());
       params.put("endDate", endCurrent.getTime());
       List appointmentList = crudService.findByNamedQuery("Appointment.findByDateProvider", params);
       return appointmentList;
    }
 
    public List<PatientNote> filter(List<PatientNote> patientNoteList, boolean on) {
       if (!on || patientNoteList == null) {
          return patientNoteList;
       }
       List<PatientNote> result = new ArrayList();
       for (PatientNote patientNote : patientNoteList) {
          if (!patientNote.isArchive()) {
             result.add(patientNote);
          }
       }
       return result;
    }
 
    public Integer age(Patient patient) {
       if (patient == null || patient.getBirthDate() == null) {
          return null;
       }
       return (int) (((new Date()).getTime() - patient.getBirthDate().getTime()) / (365L * 24L * 3600L * 1000L));
    }
 
    public boolean compareWithMask(Short value, Short mask) {
       if (value == null || mask == null) {
          return false;
       }
       return (value & mask) == mask;
    }
 
    public Provider providerByNpi(String npi) {
       if (npi == null) {
          return null;
       }
       return crudService.find(npi, Provider.class);
    }
 
    public OfficeProvider officeProviderById(int id) {
       return crudService.find(id, OfficeProvider.class);
    }
 
    public OfficeProvider officeProviderByProvider(Provider provider) {
       Map params = new HashMap();
       params.put("provider", provider);
       List resultList = crudService.search("SELECT op FROM OfficeProvider op WHERE op.associatedProvider = :provider AND op.role = 'self'", params, 1);
       if (!resultList.isEmpty()) {
          return (OfficeProvider) resultList.get(0);
       }
       return null;
    }
 
    public String formattedDate(String strFormat, Date date) {
       if (strFormat == null || date == null) {
          return null;
       }
       SimpleDateFormat dateFormat = new SimpleDateFormat(strFormat);
       return dateFormat.format(date);
    }
 
    public Diagnosis diagnosis(String icd9) {
       return crudService.find(icd9, Diagnosis.class);
    }
 
    public void adaptVisit(Visit visit, Map procedureMap, Map commonDiagnosisMap, Map laboratoryDiagnosisMap, Map providerProcedureMap) {
       List visitIcdList = new ArrayList();
       if (visit.getDiagnosisList() != null) {
          for (Diagnosis d : visit.getDiagnosisList()) {
             visitIcdList.add(d.getCode());
          }
       }
       if (laboratoryDiagnosisMap != null) {
          for (Object key : laboratoryDiagnosisMap.keySet()) {
             Map diagnosisEntryMap = (Map) laboratoryDiagnosisMap.get(key);
             if (diagnosisEntryMap.get("_") != null && (Boolean) diagnosisEntryMap.get("_")) {
                String icd = ((String) key).replaceFirst(":\\d+$", "");
                if (!visitIcdList.contains(icd)) {
                   visitIcdList.add(icd);
                }
             }
          }
       }
       if (commonDiagnosisMap != null) {
          for (Object key : commonDiagnosisMap.keySet()) {
             Map diagnosisEntryMap = (Map) commonDiagnosisMap.get(key);
             if (diagnosisEntryMap.get("_") != null && (Boolean) diagnosisEntryMap.get("_")) {
                if (!visitIcdList.contains(key)) {
                   visitIcdList.add(key);
                }
             }
          }
       }
       Map<String, List<String>> visitMap = visitMap(visit);
       Map<String, List<String>> originalVisitMap = new LinkedHashMap(visitMap);
       for (Object cpt : procedureMap.keySet()) {
          Map diagnosisMap = (Map) procedureMap.get(cpt);
          if (diagnosisMap.get("_") != null && (Boolean) diagnosisMap.get("_")) {
             if (!visitMap.containsKey((String) cpt)) {
                visitMap.put((String) cpt, new ArrayList());
             }
             for (Object icd : diagnosisMap.keySet()) {
                Map diagnosisEntryMap = (Map) diagnosisMap.get(icd);
                if (diagnosisEntryMap.get("_") != null && (Boolean) diagnosisEntryMap.get("_")) {
                   if (!visitIcdList.contains(icd)) {
                      visitIcdList.add(icd);
                   }
                   if (!visitMap.get((String) cpt).contains((String) icd)) {
                      visitMap.get((String) cpt).add((String) icd);
                   }
                }
             }
          } else {
             for (Object icd : diagnosisMap.keySet()) {
                Map diagnosisEntryMap = (Map) diagnosisMap.get(icd);
                if (diagnosisEntryMap.get("_") != null && (Boolean) diagnosisEntryMap.get("_")) {
                   if (!visitIcdList.contains(icd)) {
                      visitIcdList.add(icd);
                   }
                }
             }
          }
       }
       if (providerProcedureMap != null) {
          for (Object cpt : providerProcedureMap.keySet()) {
             Map diagnosisMap = (Map) providerProcedureMap.get(cpt);
             if (diagnosisMap.get("_") != null && (Boolean) diagnosisMap.get("_")) {
                if (!visitMap.containsKey((String) cpt)) {
                   visitMap.put((String) cpt, new ArrayList());
                }
                for (Object icd : diagnosisMap.keySet()) {
                   Map diagnosisEntryMap = (Map) diagnosisMap.get(icd);
                   if (diagnosisEntryMap.get("_") != null && (Boolean) diagnosisEntryMap.get("_")) {
                      if (!visitIcdList.contains(icd)) {
                         visitIcdList.add(icd);
                      }
                      if (!visitMap.get((String) cpt).contains((String) icd)) {
                         visitMap.get((String) cpt).add((String) icd);
                      }
                   }
                }
             } else {
                for (Object icd : diagnosisMap.keySet()) {
                   Map diagnosisEntryMap = (Map) diagnosisMap.get(icd);
                   if (diagnosisEntryMap.get("_") != null && (Boolean) diagnosisEntryMap.get("_")) {
                      if (!visitIcdList.contains(icd)) {
                         visitIcdList.add(icd);
                      }
                   }
                }
             }
          }
       }
       List originalVisitMapKeyList = new ArrayList(originalVisitMap.keySet());
       for (String cpt : visitMap.keySet()) {
          if (originalVisitMap.keySet().contains(cpt)) {
             int index = originalVisitMapKeyList.indexOf(cpt);
             VisitProcedure visitProcedure = visit.getProcedureList().get(index);
             visitProcedure.setPointers(pointers(visitMap.get(cpt), visitIcdList));
          } else {
             VisitProcedure visitProcedure = new VisitProcedure();
             Procedure procedure = (Procedure) findByCode("Procedure", cpt);
             visitProcedure.setCode(cpt);
             visitProcedure.setProcedure(procedure);
             visitProcedure.setVisit(visit);
             visitProcedure.setPointers(pointers(visitMap.get(cpt), visitIcdList));
             visit.getProcedureList().add(visitProcedure);
          }
       }
       List<Diagnosis> diagnosisList = new ArrayList();
       for (Object icd : visitIcdList) {
          diagnosisList.add(diagnosis((String) icd));
       }
       visit.setDiagnosisList(diagnosisList);
       Collections.sort(visit.getProcedureList(), new Comparator() {
          @Override
          public int compare(Object o1, Object o2) {
             VisitProcedure s1 = (VisitProcedure) o1;
             VisitProcedure s2 = (VisitProcedure) o2;
             return s1.getCode().replaceAll("^(99)", "00$1").compareTo(s2.getCode().replaceAll("^(99)", "00$1"));
 
          }
       });
    }
 
    public String pointers(List procedureDiagnosisList, List visitDiagnosisList) {
       List indexList = new ArrayList();
       for (Object procedureDiagnosis : procedureDiagnosisList) {
          if (visitDiagnosisList.contains(procedureDiagnosis)) {
             indexList.add(Integer.toString(visitDiagnosisList.indexOf(procedureDiagnosis) + 1));
          }
       }
 //      Collections.sort(indexList);
       return join(indexList, ",");
    }
 
    public String join(List<String> list, String conjunction) {
       StringBuilder sb = new StringBuilder();
       boolean first = true;
       for (String item : list) {
          if (first) {
             first = false;
          } else {
             sb.append(conjunction);
          }
          sb.append(item);
       }
       return sb.toString();
    }
 
    public Map<String, List<String>> visitMap(Visit visit) {
       Map<String, List<String>> visitMap = new LinkedHashMap();
       if (visit.getProcedureList() != null) {
          for (VisitProcedure visitProcedure : visit.getProcedureList()) {
             visitMap.put(visitProcedure.getProcedure().getCode(), icdList(visitProcedure));
          }
       }
       return visitMap;
    }
 
    public List<String> icdList(VisitProcedure visitProcedure) {
       List icdList = new ArrayList();
       if (visitProcedure.getPointers() == null || visitProcedure.getPointers().trim().isEmpty()) {
          return icdList;
       }
       try {
          String[] pointers = visitProcedure.getPointers().split(",");
          for (String pointer : pointers) {
             int i = Integer.parseInt(pointer.trim()) - 1;
             if (i >= 0 && i < visitProcedure.getVisit().getDiagnosisList().size()) {
                icdList.add(visitProcedure.getVisit().getDiagnosisList().get(i).getCode());
             }
          }
       } catch (Exception ex) {
          Logger.getLogger(BizController.class.getName()).log(Level.SEVERE, null, ex);
       }
       return icdList;
    }
 
    public List<String> visitIcdList(Visit visit) {
       List visitIcdList = new ArrayList();
       if (visit.getDiagnosisList() != null) {
          for (Diagnosis d : visit.getDiagnosisList()) {
             visitIcdList.add(d.getCode());
          }
       }
       return visitIcdList;
    }
 
    public void fixupPointers(Visit visit) {
       if (visit.getProcedureList() == null) {
          return;
       }
       for (VisitProcedure visitProcedure : visit.getProcedureList()) {
          String pointers = visitProcedure.getPointers();
          if (pointers != null) {
             pointers = pointers.replaceAll("[^\\d,]", ",");
             pointers = pointers.replaceAll(",+", ",");
             pointers = pointers.replaceFirst("^,", "");
             pointers = pointers.replaceFirst(",$", "");
             visitProcedure.setPointers(pointers);
          }
       }
    }
 
    public void removeDiagnosis(Visit visit, Diagnosis diagnosis) {
       if (diagnosis == null || !visit.getDiagnosisList().contains(diagnosis)) {
          return;
       }
       List<String> visitIcdList = visitIcdList(visit);
       visitIcdList.remove(diagnosis.getCode());
       if (visit.getProcedureList() != null) {
          for (VisitProcedure visitProcedure : visit.getProcedureList()) {
             List<String> icdList = icdList(visitProcedure);
             if (icdList.contains(diagnosis.getCode())) {
                icdList.remove(diagnosis.getCode());
                visitProcedure.setPointers(pointers(icdList, visitIcdList));
             }
          }
       }
       visit.getDiagnosisList().remove(diagnosis);
    }
 
    public void addProcedure(Object obj, Procedure procedure) {
       if (obj instanceof Visit) {
          Visit visit = (Visit) obj;
          if (visit.getProcedureList() == null) {
             visit.setProcedureList(new ArrayList());
          }
          VisitProcedure visitProcedure = new VisitProcedure();
          visitProcedure.setCode(procedure.getCode());
          visitProcedure.setProcedure(procedure);
          visitProcedure.setVisit(visit);
          visit.getProcedureList().add(visitProcedure);
       }
    }
 
    public Specialty specialty(int id) {
       return crudService.find(id, Specialty.class);
    }
 
    public Double visitVitalsBmi(VisitVitals visitVitals) {
       if (visitVitals == null || visitVitals.getHeightBase() == null || visitVitals.getWeight() == null) {
          return null;
       }
       double inches = 0;
       if ("ft/in".equals(visitVitals.getHeightUnit())) {
          inches = visitVitals.getHeightBase() * 12 + (visitVitals.getHeightFraction() != null ? visitVitals.getHeightFraction() : 0);
       } else if ("m/cm".equals(visitVitals.getHeightUnit())) {
          inches = (visitVitals.getHeightBase() * 100 + (visitVitals.getHeightFraction() != null ? visitVitals.getHeightFraction() : 0)) / 2.54;
       }
       double pounds = 0;
       if ("lb".equals(visitVitals.getWeightUnit())) {
          pounds = visitVitals.getWeight();
       } else if ("kg".equals(visitVitals.getWeightUnit())) {
          pounds = visitVitals.getWeight() / 0.45359237;
       }
       if (inches <= 0 || pounds <= 0) {
          return null;
       }
       double bmi = (pounds * 703) / (Math.pow(inches, 2));
       return bmi;
    }
 
    public ReferringProvider referringProvider(String npi) {
       if (npi == null) {
          return null;
       }
       return crudService.find(npi, ReferringProvider.class);
    }
 
    public ProcedureCptModifierSet procedureCptModifierSet(String cpt) {
       if (cpt == null) {
          return null;
       }
       return crudService.find(cpt, ProcedureCptModifierSet.class);
    }
 
    public void removeVisit(Visit visit) {
       if (today(visit.getDate()) && visit.getAppointmentId() != null) {
          Appointment appointment = crudService.find(visit.getAppointmentId(), Appointment.class);
          appointment.setStatus("OPEN");
          crudService.update(appointment);
       }
       String notes = "patientId = " + visit.getPatient().getId() + "; officeProviderId = "
               + visit.getOfficeProvider().getId() + "; visitDate = " + formattedDate("MM/dd/yyyy", visit.getDate());
       patientRecordLog(visit.getPatient().getId(), "Visit", visit.getId(), "removed", notes);
       crudService.delete(visit);
    }
 
    public void addPatientVisitActivity(Visit visit, int userId) {
       if (visit == null || visit.getId() == null) {
          return;
       }
 //      HashMap<Integer, Object> params = new HashMap();
 //      params.put(1, visit.getId());
 //      params.put(2, visit.getPatient().getId());
 //      params.put(3, visit.getProvider().getId());
 //      params.put(4, visit.getPatient().getPrimaryInsuranceID());
 //      params.put(5, FacesUtils.getInitParameter("com.medenterprise.APPLICATION"));
 //      params.put(6, userId);
 //      crudService.executeNamedNativeQuery("Visit.AddPatientVisitActivity", params, "_MEDBASE");
    }
 
    public Referral newReferralFromPatient(Patient patient) {
       Referral referral = new Referral();
       referral.setPatientId(patient.getId());
       return referral;
    }
 
    public Referral newReferralFromVisit(Visit visit) {
       Referral referral = new Referral();
       referral.setPatientId(visit.getPatient().getId());
       referral.setOfficeProvider(visit.getOfficeProvider());
       Provider provider = null;
       if (referral.getOfficeProvider() != null) {
          if (referral.getOfficeProvider().getReferringProvider() != null) {
             provider = referral.getOfficeProvider().getReferringProvider();
          } else {
             provider = referral.getOfficeProvider().getAssociatedProvider();
          }
       }
       referral.setProvider(provider);
       return referral;
    }
 
    public PatientAuthorization newPatientAuthorizationFromPatient(Patient patient) {
       PatientAuthorization patientAuthorization = new PatientAuthorization();
       patientAuthorization.setPatientId(patient.getId());
       patientAuthorization.setPatient(patient);
       Insurance insurance = crudService.find(patient.getPrimaryInsuranceId(), Insurance.class);
       patientAuthorization.setInsurance(insurance);
       return patientAuthorization;
    }
 
    public Map patientAuthorizationValidationMap(PatientAuthorization patientAuthorization) {
       Map patientAuthorizationValidationMap = new HashMap();
       if (patientAuthorization.getStartDate() == null) {
          patientAuthorizationValidationMap.put("startDate", true);
       }
       if (patientAuthorization.getExpirationDate() == null) {
          patientAuthorizationValidationMap.put("expirationDate", true);
       }
       if (patientAuthorization.getNumber() == null || patientAuthorization.getNumber().trim().isEmpty()) {
          patientAuthorizationValidationMap.put("number", true);
       }
       return patientAuthorizationValidationMap;
    }
 
 //   public PatientAuthorization newPatientAuthorizationFromPatientAppointment(PatientAppointment patientAppointment) {
 //      PatientAuthorization patientAuthorization = new PatientAuthorization();
 //      patientAuthorization.setPatientId(patientAppointment.getPatientId());
 //      patientAuthorization.setInsurance(patientAppointment.getOfficePatient().getPrimaryInsurance());
 //      patientAuthorization.setProvider(patientAppointment.getOfficeProvider().getAssociatedProvider());
 //      patientAuthorization.setSpecialty(patientAppointment.getSpecialty());
 //      return patientAuthorization;
 //   }
    public ReferralResult newReferralResultFromPatient(Patient patient) {
       ReferralResult referralResult = new ReferralResult();
       referralResult.setPatientId(patient.getId());
       referralResult.setPatient(patient);
       return referralResult;
    }
 
    public ReferralResult newReferralResultFromReferral(Referral referral) {
       ReferralResult referralResult = new ReferralResult();
       referralResult.setPatientId(referral.getPatientId());
       referralResult.setPatient(referral.getPatient());
       referralResult.setProvider(referral.getProvider());
       referralResult.setOfficeProvider(referral.getOfficeProvider());
       referralResult.setFacility(referral.getFacility());
       if (referral.getProcedureList() != null && referral.getProcedureList().size() >= 1) {
          referralResult.setProcedure(referral.getProcedureList().get(0));
       }
       referralResult.setReferral(referral);
       return referralResult;
    }
 
    public ReferralResult newReferralResultFromVisit(Visit visit) {
       ReferralResult referralResult = new ReferralResult();
       referralResult.setPatientId(visit.getPatient().getId());
       referralResult.setOfficeProvider(visit.getOfficeProvider());
       return referralResult;
    }
 
    public Map visitListPaymentMap(List<Visit> visitList) {
       Map result = new HashMap();
       result.put("Cash", BigDecimal.ZERO);
       result.put("Check", BigDecimal.ZERO);
       result.put("Credit Card", BigDecimal.ZERO);
       result.put("Total", BigDecimal.ZERO);
       Map totalCategoryMap = new HashMap();
       result.put("totalCategoryMap", totalCategoryMap);
       List<VisitCategory> categoryList = new ArrayList();
       result.put("categoryList", categoryList);
       Map<String, Integer> totalVisitTypeMap = (HashMap) new HashMap();
       result.put("totalVisitTypeMap", totalVisitTypeMap);
       for (Visit visit : visitList) {
          if (visit.getPaymentList() != null) {
             Map visitMap = new HashMap();
             result.put(visit, visitMap);
             BigDecimal total = BigDecimal.ZERO;
             for (VisitPayment payment : visit.getPaymentList()) {
                String paymentMethod = payment.getPaymentMethod().getName();
                BigDecimal val = visitMap.containsKey(paymentMethod) ? (BigDecimal) visitMap.get(paymentMethod) : BigDecimal.ZERO;
                visitMap.put(paymentMethod, val.add(payment.getAmount()));
                total = total.add(payment.getAmount());
                if (result.get(paymentMethod) == null) {
                   result.put(paymentMethod, BigDecimal.ZERO);
                }
                result.put(paymentMethod, ((BigDecimal) result.get(paymentMethod)).add(payment.getAmount()));
                result.put("Total", ((BigDecimal) result.get("Total")).add(payment.getAmount()));
             }
             visitMap.put("Total", total);
             VisitCategory category = visit.getCategory();
             if (!categoryList.contains(category)) {
                categoryList.add(category);
                totalCategoryMap.put(category, new HashMap());
             }
             Map<String, Integer> categoryVisitTypeMap = (HashMap) totalCategoryMap.get(category);
             for (String type : visitTypeList(visit)) {
                if (!categoryVisitTypeMap.containsKey(type)) {
                   categoryVisitTypeMap.put(type, 0);
                }
                categoryVisitTypeMap.put(type, categoryVisitTypeMap.get(type) + 1);
                if (!totalVisitTypeMap.containsKey(type)) {
                   totalVisitTypeMap.put(type, 0);
                }
                totalVisitTypeMap.put(type, totalVisitTypeMap.get(type) + 1);
             }
          }
       }
       return result;
    }
 
    public String insuranceType(Patient patient) {
       if (patient.getInsuranceList() == null || patient.getInsuranceList().isEmpty()) {
          return "SELF";
       } else if (patient.getInsuranceList().size() == 1) {
          if (patient.getInsuranceList().get(0).getInsurance().getId() == 62 || patient.getInsuranceList().get(0).getInsurance().getId() == 503) {
             return "MA";
          } else if (patient.getInsuranceList().get(0).getInsurance().getId() == 63) {
             return "MC";
          } else {
             return "COMMERCIAL";
          }
       } else {
          boolean commercial = false;
          for (PatientInsurance pi : patient.getInsuranceList()) {
             if (pi.getInsurance().getId() != 62 && pi.getInsurance().getId() != 503 && pi.getInsurance().getId() != 63) {
                commercial = true;
                break;
             }
          }
          if (commercial) {
             return "COMMERCIAL";
          } else {
             return "MA+MC";
          }
       }
    }
 
    private List<String> visitTypeList(Visit visit) {
       List<String> typeList = new ArrayList();
       typeList.add("ALL");
       if (visit.getPatient().getAge() > 65) {
          typeList.add("GERIATRIC");
       }
       if (visit.getPatient().getInsuranceList() == null || visit.getPatient().getInsuranceList().isEmpty()) {
          typeList.add("SELF");
          return typeList;
       } else if (visit.getPatient().getInsuranceList().size() == 1) {
          if (visit.getPatient().getInsuranceList().get(0).getInsurance().getId() == 62 || visit.getPatient().getInsuranceList().get(0).getInsurance().getId() == 503) {
             typeList.add("MA");
          } else if (visit.getPatient().getInsuranceList().get(0).getInsurance().getId() == 63) {
             typeList.add("MC");
          } else {
             typeList.add("COMMERCIAL");
          }
       } else {
          boolean commercial = false;
          for (PatientInsurance pi : visit.getPatient().getInsuranceList()) {
             if (pi.getInsurance().getId() != 62 && pi.getInsurance().getId() != 503 && pi.getInsurance().getId() != 63) {
                commercial = true;
                break;
             }
          }
          if (commercial) {
             typeList.add("COMMERCIAL");
          } else {
             typeList.add("MA+MC");
          }
       }
       return typeList;
    }
 
    public List<Visit> sortedByPatientVisitList(List<Visit> visitList) {
       List<Visit> resultList = new ArrayList(visitList);
       Collections.sort(resultList, new Comparator<Visit>() {
          @Override
          public int compare(Visit v1, Visit v2) {
             int cmp = v1.getPatient().getLastName().compareTo(v2.getPatient().getLastName());
             if (cmp == 0) {
                cmp = v1.getPatient().getFirstName().compareTo(v2.getPatient().getFirstName());
             }
             if (cmp == 0) {
                cmp = v1.getDate().compareTo(v2.getDate());
             }
             if (cmp == 0) {
                cmp = v1.getTime().compareTo(v2.getTime());
             }
             return cmp;
          }
       });
       return resultList;
    }
 
    public OfficeProvider officeProviderByReferringProvider(Provider provider, String myOfficeCode) {
       if (provider == null) {
          return null;
       }
       Map params = new HashMap();
       params.put("npi", provider.getNpi());
       params.put("myOfficeCode", myOfficeCode);
       List resultList = crudService.findByNamedQuery("OfficeProvider.findByReferringNpi", params);
       if (resultList.size() >= 1) {
          return (OfficeProvider) resultList.get(0);
       }
       params.remove("myOfficeCode");
       resultList = crudService.findByNamedQuery("OfficeProvider.findSelfByNpi", params);
       if (resultList.size() >= 1) {
          return (OfficeProvider) resultList.get(0);
       }
       return null;
    }
 
    public ReferralFacility slqcFacility() {
       return crudService.find(90, ReferralFacility.class);
    }
 
    public void makePtReferral(Referral referral) {
       if (referral == null) {
          return;
       }
       referral.setFacility(slqcFacility());
       referral.setSpecialtyId(10);
       referral.setProcedureList(new ArrayList());
       ReferralProcedure ptProcedure = crudService.find(423, ReferralProcedure.class);
       referral.getProcedureList().add(ptProcedure);
    }
 
    public Date trimDate(Date date) {
       if (date == null) {
          return null;
       }
       Calendar calendar = new GregorianCalendar();
       calendar.setTime(date);
       calendar = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
       return calendar.getTime();
    }
 
    public List authorizableAppointmentList(Map params) {
       List appointmentList = crudService.findByNamedQuery("Appointment.authorizableAppointmentList", params, 0, "_MEDBASE");
       return appointmentList;
    }
 
    public List authorizableVisitList(Map params) {
       List visitList = crudService.findByNamedQuery("Visit.authorizableVisitList", params, 0, "_MEDBASE");
       return visitList;
    }
 
    public Object filterByPeriod(Object listObj, String period) {
       if (!(listObj instanceof List)) {
          return listObj;
       }
       List list = (List) listObj;
       if (list == null || period == null || period.equals("ALL")) {
          return list;
       }
       List resultList = new ArrayList();
       Calendar monthAgo = new GregorianCalendar();
       monthAgo.add(Calendar.MONTH, -1);
       Calendar yearAgo = new GregorianCalendar();
       yearAgo.add(Calendar.YEAR, -1);
       for (Object obj : list) {
          try {
             Date date = (Date) obj.getClass().getDeclaredMethod("getDate").invoke(obj);
             if (period.equals("MONTH")) {
                if (date.after(monthAgo.getTime())) {
                   resultList.add(obj);
                }
             } else if (period.equals("YEAR")) {
                if (date.after(yearAgo.getTime())) {
                   resultList.add(obj);
                }
             }
          } catch (Exception ex) {
             Logger.getLogger(BizController.class.getName()).log(Level.SEVERE, null, ex);
          }
       }
       return resultList;
    }
 
    public Object filterByActiveValue(Object listObj, String status) {
       if (!(listObj instanceof List)) {
          return listObj;
       }
       List list = (List) listObj;
       if (list == null || status == null || status.equals("ALL")) {
          return list;
       }
       List resultList = new ArrayList();
       for (Object obj : list) {
          try {
             boolean active = (boolean) obj.getClass().getDeclaredMethod("getActive").invoke(obj);
             if (status.equals("ACTIVE") && active) {
                resultList.add(obj);
             } else if (status.equals("INACTIVE") && !active) {
                resultList.add(obj);
             }
          } catch (Exception ex) {
             Logger.getLogger(BizController.class.getName()).log(Level.SEVERE, null, ex);
          }
       }
       return resultList;
    }
 
    public Object filterByStatus(Object listObj, String status) {
       if (!(listObj instanceof List)) {
          return listObj;
       }
       List list = (List) listObj;
       if (list == null || status == null || status.equals("ALL")) {
          return list;
       }
       List resultList = new ArrayList();
       for (Object obj : list) {
          try {
             String objStatus = (String) obj.getClass().getDeclaredMethod("getStatus").invoke(obj);
             if (status.equals(objStatus)) {
                resultList.add(obj);
             }
          } catch (Exception ex) {
             Logger.getLogger(BizController.class.getName()).log(Level.SEVERE, null, ex);
          }
       }
       return resultList;
    }
 
    public Object filterByType(Object listObj, String type) {
       if (!(listObj instanceof List)) {
          return listObj;
       }
       List list = (List) listObj;
       if (list == null || type == null || type.equals("ALL")) {
          return list;
       }
       List resultList = new ArrayList();
       for (Object obj : list) {
          try {
             String objStatus = (String) obj.getClass().getDeclaredMethod("getType").invoke(obj);
             if (type.equals(objStatus)) {
                resultList.add(obj);
             }
          } catch (Exception ex) {
             Logger.getLogger(BizController.class.getName()).log(Level.SEVERE, null, ex);
          }
       }
       return resultList;
    }
 
    public Patient refreshedPatient(Patient patient) {
       return crudService.find(patient.getId(), Patient.class);
    }
 
    public Visit refreshedVisit(Visit visit) {
       return crudService.find(visit.getId(), Visit.class);
    }
 
    public List providerList(Date appointmentDate) {
       Map params = new HashMap();
       if (appointmentDate != null) {
          GregorianCalendar calendar = new GregorianCalendar();
          calendar.setTime(appointmentDate);
          calendar = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
          params.put("startDate", calendar.getTime());
          calendar = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
          params.put("endDate", calendar.getTime());
       }
       return crudService.findByNamedQuery("Provider.findByAppointmentDate", params, 0, "_MEDBASE");
    }
 
    public List<Appointment> sortedByMrnLastTwoDigitsAppointmentList(List<Appointment> appointmentList) {
       List<Appointment> resultList = new ArrayList(appointmentList);
       Collections.sort(resultList, new Comparator<Appointment>() {
          @Override
          public int compare(Appointment a1, Appointment a2) {
             String mrn1 = "0" + (a1.getPatient() == null ? "0" : a1.getPatient().getId());
             mrn1 = mrn1.substring(mrn1.length() - 2);
             String mrn2 = "0" + (a2.getPatient() == null ? "0" : a2.getPatient().getId());
             mrn2 = mrn2.substring(mrn2.length() - 2);
             return mrn1.compareTo(mrn2);
          }
       });
       return resultList;
    }
 
    public Date startOfMonth(Date date, int yearOffset) {
       GregorianCalendar calendar = new GregorianCalendar();
       calendar.setTime(date);
       calendar.add(Calendar.YEAR, yearOffset);
       calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
       calendar = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
       return calendar.getTime();
    }
 
    public Date endOfMonth(Date date, int yearOffset) {
       GregorianCalendar calendar = new GregorianCalendar();
       calendar.setTime(date);
       calendar.add(Calendar.YEAR, yearOffset);
       calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
       calendar = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
       return calendar.getTime();
    }
 
    public Date secureDate(Date date) {
       if (date == null) {
          return null;
       }
       GregorianCalendar calendar = new GregorianCalendar();
       calendar.setTime(date);
       int year = calendar.get(Calendar.YEAR);
       if (year < 100) {
          year += 2000;
       }
       calendar.set(Calendar.YEAR, year);
       return calendar.getTime();
    }
 
    public Integer secureId(String strId) {
       if (strId != null) {
          strId = strId.trim().replaceAll("^(\\d+).*", "$1").replaceAll("\\D", "");
          Integer id = null;
          if (!strId.isEmpty()) {
             id = Integer.parseInt(strId);
             return id;
          }
       }
       return null;
    }
 
    public void removeSelectedFromList(Map selectMap, List list) {
       List toRemove = new ArrayList();
       for (Object obj : list) {
          if (selectMap.get(obj) != null && (Boolean) selectMap.get(obj) == true) {
             toRemove.add(obj);
          }
       }
       list.removeAll(toRemove);
       selectMap.clear();
    }
 
    public ServicePaymentType servicePaymentType(Patient patient) {
       if (patient.getInsuranceList() == null || patient.getInsuranceList().isEmpty()) {
          return null;
       }
       String primaryCode = null;
       String otherCode = null;
       for (PatientInsurance patientInsurance : patient.getInsuranceList()) {
          String insuranceTypeCode = patientInsurance.getInsurance().getInsuranceType().getCode();
          if (patientInsurance.getPatient().getPrimaryInsuranceId() != null && patientInsurance.getPatient().getPrimaryInsuranceId().equals(patientInsurance.getInsurance().getId())) {
             primaryCode = insuranceTypeCode;
          } else if (otherCode == null) {
             otherCode = insuranceTypeCode;
          } else {
             continue;
          }
       }
       if (primaryCode != null && otherCode != null && primaryCode.equals(otherCode)) {
          otherCode = null;
       }
       if ("MC".equals(primaryCode) && "MAM".equals(otherCode)) {
          otherCode = "C";
       }
       Map params = new HashMap();
       params.put("primaryInsuranceTypeCode", primaryCode);
       params.put("secondaryInsuranceTypeCode", otherCode);
       List resultList = crudService.findByNamedQuery("ServicePaymentType.findByCodes", params, 1);
       if (!resultList.isEmpty()) {
          return (ServicePaymentType) resultList.get(0);
       }
       return null;
    }
 
    public void reorderPatientInsuranceList(List<PatientInsurance> patientInsuranceList, PatientInsurance patientInsurance, int index, int userId) {
       if (patientInsuranceList == null || patientInsurance == null || index >= patientInsuranceList.size()) {
          return;
       }
       patientInsuranceList.remove(patientInsurance);
       if (index != -1) {
          patientInsuranceList.add(index, patientInsurance);
       }
       short accountIndex = 1;
       Patient patient = patientInsurance.getPatient();
       if (patientInsuranceList.isEmpty()) {
          patient.setPrimaryInsuranceId(null);
       }
       for (PatientInsurance pi : patientInsuranceList) {
          if (accountIndex == 1) {
             patient.setPrimaryInsuranceId(pi.getInsurance().getId());
          }
          pi.setAccountIndex(accountIndex++);
       }
       patientRecordLog(patient.getId(), "Patient", patient.getId(), "updated");
      crudService.update(patient, "_MEDBASE");
    }
 
    public void savePatientInsurance(PatientInsurance patientInsurance, int userId) {
       Patient patient = patientInsurance.getPatient();
       if (patientInsurance.getId() == null) {
          reorderPatientInsuranceList(patient.getInsuranceList(), patientInsurance, patient.getInsuranceList().indexOf(patientInsurance), userId);
          patientRecordLog(patient.getId(), "PatientInsurance", patientInsurance.getId(), "updated");
          return;
       }
       ServicePaymentType spt = servicePaymentType(patient);
       patient.setServicePaymentTypeId(spt == null ? null : spt.getId());
       patientRecordLog(patient.getId(), "Patient", patient.getId(), "updated");
      crudService.update(patient, "_MEDBASE");
    }
 
    public Integer toInt(Object obj) {
       if (obj == null) {
          return null;
       }
       Integer result = null;
       try {
          result = Integer.parseInt(obj.toString());
       } catch (Exception ex) {
       }
       return result;
    }
 
    public PatientInsurance addPatientInsurance(Patient patient) {
       PatientInsurance patientInsurance = new PatientInsurance();
       patientInsurance.setPatient(patient);
       patientInsurance.setRelationToGuarantor('S');
       if (patient.getInsuranceList() == null) {
          patient.setInsuranceList(new ArrayList());
       }
       patient.getInsuranceList().add(patientInsurance);
       return patientInsurance;
    }
 
    public Map patientInsuranceValidationMap(PatientInsurance patientInsurance) {
       Map patientInsuranceValidationMap = new HashMap();
       if (patientInsurance.getGuarantor() != null) {
          if (patientInsurance.getGuarantor().getLastName() == null || patientInsurance.getGuarantor().getLastName().trim().isEmpty()) {
             patientInsuranceValidationMap.put("guarantorLastName", true);
          }
          if (patientInsurance.getGuarantor().getFirstName() == null || patientInsurance.getGuarantor().getFirstName().trim().isEmpty()) {
             patientInsuranceValidationMap.put("guarantorFirstName", true);
          }
       }
       if (patientInsurance.getInsurance() == null) {
          patientInsuranceValidationMap.put("insurance", true);
       }
       return patientInsuranceValidationMap;
    }
 
    public void assignPcpNpi(PatientInsurance patientInsurance, String pcpStr) {
       if (pcpStr != null) {
          Pattern p = Pattern.compile("NPI:(\\d{10})", Pattern.CASE_INSENSITIVE);
          Matcher m = p.matcher(pcpStr);
          if (m.find()) {
             String npi = m.group(1);
             Map params = new HashMap();
             params.put("npi", npi);
             List result = crudService.search("SELECT r FROM ReferringProvider r WHERE r.npi LIKE :npi", params, 1, "_MEDBASE");
             if (!result.isEmpty()) {
                ReferringProvider referringProvider = (ReferringProvider) result.get(0);
                patientInsurance.setPcpNpi(referringProvider.getNpi());
                return;
             }
          }
       }
       patientInsurance.setPcpNpi(null);
    }
 
    public void appointmentToVisit(Appointment appointment) {
       Visit visit = new Visit(appointment);
       appointment.setStatus("CLOSED");
       crudService.create(visit);
       crudService.update(appointment);
    }
 
    public Visit visit(int id) {
       return crudService.find(id, Visit.class);
    }
 
    public void providerSignature() throws IOException {
       FacesContext facesContext = FacesContext.getCurrentInstance();
       ExternalContext externalContext = facesContext.getExternalContext();
       String npi = externalContext.getRequestParameterMap().get("npi");
       Provider provider = crudService.find(npi, Provider.class);
       if (provider != null && provider.getSignature() != null) {
          externalContext.setResponseContentType("image/png");
          externalContext.getResponseOutputStream().write(provider.getSignature());
       }
       facesContext.responseComplete();
    }
 
    public Patient newPatient(Map searchMap) {
       Patient patient = new Patient();
       if (searchMap.containsKey("lastName")) {
          patient.setLastName((String) searchMap.get("lastName"));
       }
       if (searchMap.containsKey("firstName")) {
          patient.setFirstName((String) searchMap.get("firstName"));
       }
       if (searchMap.containsKey("birthDate")) {
          patient.setBirthDate((Date) searchMap.get("birthDate"));
       }
       if (searchMap.containsKey("homePhone")) {
          patient.setHomePhone((String) searchMap.get("homePhone"));
       }
       patient.setSex('U');
       patient.setConditionId(0);
       return patient;
    }
 
    public Object create(Object object) {
       return crudService.create(object);
    }
 
    public List similarPatientList(Patient patient) {
       Map params = new HashMap();
       params.put("id", patient.getId());
       params.put("lastName", patient.getLastName());
       params.put("firstName", patient.getFirstName());
       params.put("birthDate", patient.getBirthDate());
       params.put("homePhone", patient.getHomePhone());
       return crudService.findByNamedQuery("Patient.similarPatients", params);
    }
 
    public Map patientValidationMap(Patient patient) {
       Map patientValidationMap = new HashMap();
       if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {
          patientValidationMap.put("lastName", true);
       }
       if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {
          patientValidationMap.put("firstName", true);
       }
       if (patient.getBirthDate() == null) {
          patientValidationMap.put("birthDate", true);
       }
       if (patient.getSex().equals('U')) {
          patientValidationMap.put("sex", true);
       }
       return patientValidationMap;
    }
 
    public List similarReferringProviderList(ReferringProvider referringProvider) {
       Map params = new HashMap();
       params.put("lastName", referringProvider.getLastName());
       params.put("firstName", referringProvider.getFirstName());
       params.put("npi", referringProvider.getNpi());
 //      params.put("phone", referringProvider.getPhone());
       return crudService.findByNamedQuery("ReferringProvider.similarReferringProviders", params, 0, "_MEDBASE");
    }
 
    public Map referringProviderValidationMap(ReferringProvider referringProvider) {
       Map referringProviderValidationMap = new HashMap();
       if (referringProvider.getLastName() == null || referringProvider.getLastName().trim().isEmpty()) {
          referringProviderValidationMap.put("lastName", true);
       }
       if (referringProvider.getFirstName() == null || referringProvider.getFirstName().trim().isEmpty()) {
          referringProviderValidationMap.put("firstName", true);
       }
       if (referringProvider.getNpi() == null || referringProvider.getNpi().isEmpty()) {
          referringProviderValidationMap.put("npi", true);
       }
       return referringProviderValidationMap;
    }
 
    public void refreshReferringProvider(ReferringProvider referringProvider, List<ReferringProvider> referringProviderList) {
       if (referringProvider.getNpi() != null) {
          ReferringProvider refreshedReferringProvider = crudService.find(referringProvider.getNpi(), ReferringProvider.class);
          referringProviderList.add(referringProviderList.indexOf(referringProvider), refreshedReferringProvider);
          referringProviderList.remove(referringProvider);
       }
    }
 
    public void saveReferringProvider(ReferringProvider referringProvider, List<ReferringProvider> referringProviderList) {
       if (referringProvider.getNpi() != null) {
          crudService.update(referringProvider);
       } else {
          referringProvider = crudService.create(referringProvider);
          referringProviderList.add(0, referringProvider);
       }
    }
 
    public List similarOfficeProviderList(OfficeProvider officeProvider) {
       if (officeProvider.getRole() != null && officeProvider.getRole().equals("self")) {
          Map params = new HashMap();
          params.put("id", officeProvider.getId());
          params.put("npi", officeProvider.getAssociatedProvider().getNpi());
          return crudService.findByNamedQuery("OfficeProvider.similarOfficeProviders", params, 0, "_HEALTHCARE");
       }
       return null;
    }
 
    public Map officeProviderValidationMap(OfficeProvider officeProvider) {
       Map officeProviderValidationMap = new HashMap();
       if (officeProvider.getSpecialtyId() == null) {
          officeProviderValidationMap.put("specialty", true);
       }
       if (officeProvider.getAlias() == null || officeProvider.getAlias().trim().isEmpty()) {
          officeProviderValidationMap.put("alias", true);
       }
       if (officeProvider.getRole() == null) {
          officeProviderValidationMap.put("role", true);
       } else if (officeProvider.getRole().equals("self")) {
          if (officeProvider.getAssociatedProvider().getLastName() == null || officeProvider.getAssociatedProvider().getLastName().trim().isEmpty()) {
             officeProviderValidationMap.put("lastName", true);
          }
          if (officeProvider.getAssociatedProvider().getFirstName() == null || officeProvider.getAssociatedProvider().getFirstName().trim().isEmpty()) {
             officeProviderValidationMap.put("firstName", true);
          }
          if (officeProvider.getAssociatedProvider().getNpi() == null || officeProvider.getAssociatedProvider().getNpi().isEmpty()) {
             officeProviderValidationMap.put("npi", true);
          }
       }
       return officeProviderValidationMap;
    }
 
    public void refreshOfficeProvider(OfficeProvider officeProvider, List<OfficeProvider> officeProviderList) {
       if (officeProvider.getId() != null) {
          OfficeProvider refreshedOfficeProvider = crudService.find(officeProvider.getId(), OfficeProvider.class);
          officeProviderList.add(officeProviderList.indexOf(officeProvider), refreshedOfficeProvider);
          officeProviderList.remove(officeProvider);
       }
    }
 
    public void saveOfficeProvider(OfficeProvider officeProvider, List<OfficeProvider> officeProviderList, Map<MyOffice, Boolean> myOfficeMap) {
       if (myOfficeMap != null) {
          List<MyOffice> myOfficeList = new ArrayList();
          for (Object myOfficeObj : myOfficeMap.keySet().toArray()) {
             if (myOfficeMap.get((MyOffice) myOfficeObj) != null && myOfficeMap.get((MyOffice) myOfficeObj)) {
                myOfficeList.add((MyOffice) myOfficeObj);
             }
          }
          officeProvider.setMyOfficeList(myOfficeList);
       }
       if (!"self".equals(officeProvider.getRole()) && officeProvider.getAssociatedProvider() != null) {
          OfficeProvider associatedOfficeProvider = officeProviderByProvider(officeProvider.getAssociatedProvider());
          officeProvider.setAssociatedOfficeProviderId(associatedOfficeProvider == null ? null : associatedOfficeProvider.getId());
       }
       officeProvider.setLastUpdated(new Date());
       if (officeProvider.getId() != null) {
          if ("self".equals(officeProvider.getRole()) && officeProvider.getAssociatedProvider() != null) {
             Provider associatedProvider = officeProvider.getAssociatedProvider();
             crudService.update(associatedProvider);
          }
          crudService.update(officeProvider);
       } else {
          if ("self".equals(officeProvider.getRole()) && officeProvider.getAssociatedProvider() != null) {
             Provider associatedProvider = officeProvider.getAssociatedProvider();
             associatedProvider = crudService.create(associatedProvider);
             officeProvider.setAssociatedProvider(associatedProvider);
          }
          officeProvider = crudService.create(officeProvider);
          officeProviderList.add(0, officeProvider);
       }
    }
 
    public void removeOfficeProvider(OfficeProvider officeProvider) {
       if ("self".equals(officeProvider.getRole()) && officeProvider.getAssociatedProvider() != null) {
          Provider associatedProvider = officeProvider.getAssociatedProvider();
          officeProvider.setAssociatedProvider(null);
          crudService.delete(associatedProvider);
       }
       crudService.delete(officeProvider);
    }
 
    public Map insuranceValidationMap(Insurance insurance) {
       Map insuranceValidationMap = new HashMap();
       if (insurance.getName() == null || insurance.getName().trim().isEmpty()) {
          insuranceValidationMap.put("name", true);
       }
       if (insurance.getTypeCode() == null) {
          insuranceValidationMap.put("type", true);
       }
       return insuranceValidationMap;
    }
 
    public void refreshInsurance(Insurance insurance, List<Insurance> insuranceList) {
       if (insurance.getId() != null) {
          Insurance refreshedInsurance = crudService.find(insurance.getId(), Insurance.class);
          insuranceList.add(insuranceList.indexOf(insurance), refreshedInsurance);
          insuranceList.remove(insurance);
       }
    }
 
    public void saveInsurance(Insurance insurance, List<Insurance> insuranceList) {
       if (insurance.getId() != null) {
          crudService.update(insurance);
       } else {
          insurance = crudService.create(insurance);
          insuranceList.add(0, insurance);
       }
    }
 
    public boolean sameNpi(ReferringProvider referringProvider, List<ReferringProvider> referringProviderList) {
       if (referringProvider == null || referringProviderList == null) {
          return false;
       }
       for (ReferringProvider rp : referringProviderList) {
          if (referringProvider.getNpi() != null && rp.getNpi() != null && referringProvider.getNpi().equals(rp.getNpi())) {
             return true;
          }
       }
       return false;
    }
 
    public List<Appointment> appointmentList(int providerId, Date startDate, Date endDate) {
       Map params = new HashMap();
       params.put("providerId", providerId);
       params.put("startDate", startDate);
       params.put("endDate", endDate);
       return castList(crudService.findByNamedQuery("Appointment.findByDateProvider", params, 0, "_MEDBASE"), Appointment.class);
    }
 
    public List<AppointmentTime> appointmentTimeList(int providerId, Date startDate, Date endDate) {
       Map params = new HashMap();
       params.put("providerId", providerId);
       params.put("startDate", startDate);
       params.put("endDate", endDate);
       return castList(crudService.findByNamedQuery("AppointmentTime.findByDateProvider", params, 0, "_MEDBASE"), AppointmentTime.class);
    }
 
    public List<ProviderSchedule> providerScheduleList(int providerId, Date startDate, Date endDate) {
       Map params = new HashMap();
       params.put("providerId", providerId);
       params.put("startDate", startDate);
       params.put("endDate", endDate);
       return castList(crudService.findByNamedQuery("ProviderSchedule.findByDatesProvider", params, 0, "_MEDBASE"), ProviderSchedule.class);
    }
 
    public ProviderSchedule newProviderSchedule(OfficeProvider officeProvider, String myOfficeCode) {
       ProviderSchedule providerSchedule = new ProviderSchedule();
       providerSchedule.setOfficeProvider(officeProvider);
       providerSchedule.setOfficeProviderId(officeProvider.getId());
       providerSchedule.setExceptOfficeHolidays(true);
       providerSchedule.setOfficeCode(myOfficeCode);
       providerSchedule.setPeriod1EveryValue(1);
       providerSchedule.setPeriod1Code('W');
       providerSchedule.setPeriod2Index(1);
       providerSchedule.setStartDate(new Date());
       providerSchedule.setStatusCode('A');
       return providerSchedule;
    }
 
    public Map providerScheduleValidationMap(ProviderSchedule providerSchedule) {
       Map providerScheduleValidationMap = new HashMap();
       if (providerSchedule.getStartDate() == null) {
          providerScheduleValidationMap.put("startDate", true);
       }
       if (providerSchedule.getEndDate() == null && providerSchedule.getPeriod1EveryValue() == 0) {
          providerScheduleValidationMap.put("endDate", true);
       }
       if (providerSchedule.getClassifierCode() == 'S' && (providerSchedule.getProviderScheduleTimeList() == null || providerSchedule.getProviderScheduleTimeList().isEmpty())) {
          providerScheduleValidationMap.put("emptyTimePeriod", true);
       }
       return providerScheduleValidationMap;
    }
 
    public void saveProviderSchedule(ProviderSchedule providerSchedule, int userId) {
       Date date = new Date();
       if (providerSchedule.getId() == null) {
          providerSchedule.setCreatedUserId(userId);
          providerSchedule.setCreated(date);
          crudService.create(providerSchedule, "_MEDBASE");
       }
       providerSchedule.setLastUpdated(date);
       providerSchedule.setLastUpdatedUserId(userId);
       crudService.update(providerSchedule, "_MEDBASE");
    }
 
    public OfficeSchedule newOfficeSchedule() {
       OfficeSchedule officeSchedule = new OfficeSchedule();
       officeSchedule.setClassifierCode('H');
       officeSchedule.setPeriod1EveryValue(1);
       officeSchedule.setPeriod1Code('W');
       officeSchedule.setPeriod2Index(1);
       officeSchedule.setStartDate(new Date());
       officeSchedule.setStatusCode('A');
       return officeSchedule;
    }
 
    public Map officeScheduleValidationMap(OfficeSchedule officeSchedule) {
       Map officeScheduleValidationMap = new HashMap();
 //      if (officeSchedule.getStartDate() == null) {
 //         officeScheduleValidationMap.put("startDate", true);
 //      }      
       return officeScheduleValidationMap;
    }
 
    public void saveOfficeSchedule(OfficeSchedule officeSchedule, int userId) {
 //      Date date = new Date();
       if (officeSchedule.getId() == null) {
 //         officeSchedule.setCreatedUserId(userId);
 //         officeSchedule.setCreated(date);
          crudService.create(officeSchedule, "_MEDBASE");
       }
 //      officeSchedule.setLastUpdated(date);
 //      officeSchedule.setLastUpdatedUserId(userId);
       crudService.update(officeSchedule, "_MEDBASE");
    }
 
    public List<ProviderAppointmentCategory> providerAppointmentCategoryList(int providerId) {
       Map params = new HashMap();
       params.put("providerId", providerId);
       return castList(crudService.findByNamedQuery("ProviderAppointmentCategory.findByProviderId", params, 0, "_MEDBASE"), ProviderAppointmentCategory.class);
    }
 
    public void addProviderAppointmentCategory(OfficeProvider officeProvider, VisitCategory visitCategory, Integer duration) {
       if (officeProvider == null || visitCategory == null || duration == null || duration == 0) {
          return;
       }
       ProviderAppointmentCategory providerAppointmentCategory = new ProviderAppointmentCategory();
       providerAppointmentCategory.setOfficeProviderId(officeProvider.getId());
       providerAppointmentCategory.setVisitCategory(visitCategory);
       providerAppointmentCategory.setDuration(duration.shortValue());
       crudService.create(providerAppointmentCategory);
    }
 
    public List<OfficeSchedule> officeScheduleList() {
       Map params = new HashMap();
       return castList(crudService.findByNamedQuery("OfficeSchedule.findActive", params, 0, "_MEDBASE"), OfficeSchedule.class);
    }
 
    private <T> List<T> castList(List list, Class<T> type) {
       List<T> resultList = new ArrayList();
       for (Object obj : list) {
          resultList.add((T) obj);
       }
       return resultList;
    }
 
    public List<TimePeriodManager> defineTimePeriodManagerList(Date startDate, int dayCount, int providerId, int duration) {
       Calendar calendar = new GregorianCalendar();
       calendar.setTime(startDate);
       calendar = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
       startDate = calendar.getTime();
       Date endDate = new DateTime(startDate).plusDays(dayCount - 1).toDate();
       List<AppointmentTime> appointmentTimeList = appointmentTimeList(providerId, startDate, endDate);
       List<ProviderSchedule> providerScheduleList = providerScheduleList(providerId, startDate, endDate);
       List<OfficeSchedule> officeScheduleList = officeScheduleList();
       List<TimePeriodManager> timePeriodManagerList = new ArrayList();
       for (int i = 0; i < dayCount; i++) {
          timePeriodManagerList.add(new TimePeriodManager());
       }
       ScheduleUtil.defineAvailableDatePeriods(appointmentTimeList, timePeriodManagerList, providerScheduleList,
               officeScheduleList, startDate, endDate);
       return timePeriodManagerList;
    }
 
    public Appointment newAppointment(Date date, Date time, Patient patient, int providerId, String categoryCode, int duration) {
       Appointment appointment = new Appointment();
       appointment.setDate(date);
       appointment.setTime(time);
       appointment.setPatient(patient);
       OfficeProvider provider = crudService.find(providerId, OfficeProvider.class);
       appointment.setOfficeProvider(provider);
       appointment.setCategoryCode(categoryCode);
       appointment.setDuration((short) duration);
       appointment.setSpecialtyId(provider.getSpecialtyId());
       appointment.setStatus("OPEN");
       return appointment;
    }
 
    public void rescheduleAppointment(Appointment appointment, Date date, Date time, int providerId, int userId) {
       OfficeProvider officeProvider = crudService.find(providerId, OfficeProvider.class);
       appointment.setDate(date);
       appointment.setTime(time);
       appointment.setOfficeProvider(officeProvider);
       appointment.setNotes("Rescheduled");
       appointment.setUserId(userId);
       appointment.setLastUpdated(new Date());
       crudService.update(appointment);
    }
 
    public ProviderAppointmentCategory preferredProviderAppointmentCategory(List<ProviderAppointmentCategory> providerAppointmentCategoryList, List<Patient> patientList, int specialtyId) {
       if (providerAppointmentCategoryList == null || providerAppointmentCategoryList.isEmpty()) {
          return null;
       }
       if (patientList == null || patientList.size() != 1) {
          return providerAppointmentCategoryList.get(0);
       }
       Patient patient = patientList.get(0);
       Map params = new HashMap();
       params.put("patientId", patient.getId());
       params.put("specialtyId", specialtyId);
       int visitCount = crudService.searchCount("SELECT v FROM Visit v WHERE v.specialtyId = :specialtyId AND v.patient.id = :patientId", params, 0, "_MEDBASE");
       ProviderAppointmentCategory preferred = null;
       if (visitCount == 0) {
          for (ProviderAppointmentCategory providerAppointmentCategory : providerAppointmentCategoryList) {
             if (providerAppointmentCategory.getVisitCategory().getCode().equals("NP")) {
                preferred = providerAppointmentCategory;
                break;
             }
          }
       } else {
          for (ProviderAppointmentCategory providerAppointmentCategory : providerAppointmentCategoryList) {
             if (!providerAppointmentCategory.getVisitCategory().getCode().equals("NP")) {
                preferred = providerAppointmentCategory;
                break;
             }
          }
       }
       if (preferred == null) {
          preferred = providerAppointmentCategoryList.get(0);
       }
       return preferred;
    }
 
    public String preferredProviderAppointmentCategoryCode(List<ProviderAppointmentCategory> providerAppointmentCategoryList, Patient patient, int specialtyId) {
       if (providerAppointmentCategoryList == null || providerAppointmentCategoryList.isEmpty()) {
          return null;
       }
       Map params = new HashMap();
       params.put("patientId", patient.getId());
       params.put("specialtyId", specialtyId);
       int visitCount = crudService.searchCount("SELECT v FROM Visit v WHERE v.specialtyId = :specialtyId AND v.patient.id = :patientId", params, 0, "_MEDBASE");
       ProviderAppointmentCategory preferred = null;
       if (visitCount == 0) {
          for (ProviderAppointmentCategory providerAppointmentCategory : providerAppointmentCategoryList) {
             if (providerAppointmentCategory.getVisitCategory().getCode().equals("NP")) {
                preferred = providerAppointmentCategory;
                break;
             }
          }
       } else {
          for (ProviderAppointmentCategory providerAppointmentCategory : providerAppointmentCategoryList) {
             if (!providerAppointmentCategory.getVisitCategory().getCode().equals("NP")) {
                preferred = providerAppointmentCategory;
                break;
             }
          }
       }
       if (preferred == null) {
          preferred = providerAppointmentCategoryList.get(0);
       }
       return preferred.getVisitCategory().getCode();
    }
 
    public int parseInt(String s) {
       return Integer.parseInt(s);
    }
 
    public Date parseTime(String s) {
       Date time = null;
       try {
          time = new SimpleDateFormat("h:mm a").parse(s);
       } catch (ParseException ex) {
          Logger.getLogger(BizController.class.getName()).log(Level.SEVERE, null, ex);
       }
       return time;
    }
 
    public Map<TimePeriodManager, List<String>> timePeriodManagerListValidationMap(List<TimePeriodManager> timePeriodManagerList, List<Patient> patientList, int specialtyId) {
       if (patientList == null || patientList.size() != 1) {
          return new HashMap();
       }
       Map<TimePeriodManager, List<String>> timePeriodManagerListValidationMap = new HashMap();
       Patient patient = patientList.get(0);
       Date lastVisitDate = lastVisitDate(patient);
       for (TimePeriodManager timePeriodManager : timePeriodManagerList) {
          timePeriodManagerListValidationMap.put(timePeriodManager, new ArrayList());
          boolean existsAppointmentForDate = existsAppointmentForDate(timePeriodManager.getActiveDate(), patient);
          boolean existsVisitForDate = lastVisitDate != null && timePeriodManager.getActiveDate().equals(lastVisitDate);
          if (existsAppointmentForDate || existsVisitForDate) {
             timePeriodManagerListValidationMap.get(timePeriodManager).add("The date already scheduled for this patient");
          }
       }
       return timePeriodManagerListValidationMap;
    }
 
    public String appointmentOnclick(List<String> errorList) {
       if (errorList == null || errorList.isEmpty()) {
          return null;
       }
       String onclick = "return confirm('";
       for (String error : errorList) {
          onclick += error + " ";
       }
       onclick += "Create appointment anyway?');";
       return onclick;
    }
 
    public Visit lastVisit(Patient patient, int specialtyId) {
       Visit visit = null;
       Map params = new HashMap();
       params.put("patientId", patient.getId());
       params.put("specialtyId", specialtyId);
       List result = crudService.search("SELECT v FROM Visit v WHERE v.specialtyId = :specialtyId AND v.patient.id = :patientId ORDER BY v.date DESC", params, 1, "_MEDBASE");
       if (!result.isEmpty()) {
          visit = (Visit) result.get(0);
       }
       return visit;
    }
 
    public Visit lastVisit(Patient patient) {
       Visit visit = null;
       Map params = new HashMap();
       params.put("patientId", patient.getId());
       List result = crudService.search("SELECT v FROM Visit v WHERE v.patient.id = :patientId ORDER BY v.date DESC", params, 1, "_MEDBASE");
       if (!result.isEmpty()) {
          visit = (Visit) result.get(0);
       }
       return visit;
    }
 
    public Date lastVisitDate(Patient patient) {
       Date date = null;
       Map params = new HashMap();
       params.put("patientId", patient.getId());
       List result = crudService.search("SELECT v.date FROM Visit v WHERE v.patient.id = :patientId ORDER BY v.date DESC", params, 1, "_MEDBASE");
       if (!result.isEmpty()) {
          date = (Date) result.get(0);
       }
       return date;
    }
 
    public boolean existsAppointmentForDate(Date date, Patient patient) {
       Map params = new HashMap();
       params.put("patientId", patient.getId());
       params.put("date", date);
       int count = crudService.searchCount("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.date = :date AND a.status = 'OPEN'", params, 1, "_MEDBASE");
       return count > 0;
    }
 
    public ReferringProvider referringProviderByLastVisit(Date date, Patient patient) {
       Visit lastVisit = lastVisit(patient, 1);
       if (lastVisit != null && date.equals(lastVisit.getDate())) {
          String npi = lastVisit.getOfficeProvider().getAssociatedNpi();
          if (npi != null) {
             ReferringProvider referringProvider = crudService.find(npi, ReferringProvider.class);
             return referringProvider;
          }
       }
       return null;
    }
 
    public String providerParticipateInsuranceWarning(String providerNpi, Integer insuranceId) {
       if (insuranceId == null || insuranceId == 0) {
          return "Patient doesn't have insurance";
       }
       Map params = new HashMap();
       params.put("providerNpi", providerNpi);
       params.put("insuranceId", insuranceId);
       int count = crudService.searchCount("SELECT pi FROM ProviderInsurance pi WHERE pi.insuranceId = :insuranceId AND pi.providerNpi = :providerNpi", params, 0, "_MEDBASE");
       if (count > 0) {
          return null;
       }
       return "Patient's insurance is not accepted by provider";
    }
 
    public List<Appointment> defineCyclical(Map<Integer, Boolean> weekDays, Integer qty, Date startDate, Date startTime, Date endTime,
            Patient patient, int providerId, String categoryCode, int duration, ReferringProvider referringProvider, boolean forceTime) {
       if (qty == null || startDate == null || startTime == null || endTime == null) {
          return null;
       }
       int counter = 0;
       DateTime startDay = new DateTime(startDate);
       DateTime day = startDay;
       List<Appointment> appointmentList = new ArrayList();
       while (counter < qty && day.minusDays(60).isBefore(startDay)) {
          if (weekDays.get(day.getDayOfWeek()) != null && weekDays.get(day.getDayOfWeek())) {
             TimePeriodManager timePeriodManager = defineTimePeriodManagerList(day.toDate(), 1, providerId, duration).get(0);
             if (timePeriodManager.active()) {
                Appointment appointment = null;
                List<TimePeriod> timePeriodList = forceTime ? timePeriodManager.getScheduleTimePeriodList() : timePeriodManager.getAvailableTimePeriodList();
                label1:
                for (TimePeriod timePeriod : timePeriodList) {
                   for (Date time : timePeriod.timeList(duration)) {
                      if ((localTime(time).isEqual(localTime(startTime)) || localTime(time).isAfter(localTime(startTime))) && (localTime(time).isEqual(localTime(endTime)) || localTime(time).isBefore(localTime(endTime)))) {
                         appointment = newAppointment(day.toDate(), time, patient, providerId, categoryCode, duration);
                         appointment.setReferringProviderNpi(referringProvider == null ? null : referringProvider.getNpi());
                         appointmentList.add(appointment);
                         break label1;
                      }
                   }
                }
                label2:
                if (appointment == null) {
                   for (TimePeriod timePeriod : timePeriodList) {
                      for (Date time : timePeriod.timeList(duration)) {
                         appointment = newAppointment(day.toDate(), time, patient, providerId, categoryCode, duration);
                         appointmentList.add(appointment);
                         appointment.setReferringProviderNpi(referringProvider == null ? null : referringProvider.getNpi());
                         break label2;
                      }
                   }
                } else {
                   counter++;
                }
             }
          }
          day = day.plusDays(1);
       }
       return appointmentList;
    }
 
    private LocalTime localTime(Date time) {
       return new DateTime(time, DateTimeZone.getDefault()).toLocalTime();
    }
 
    public String appointmentMarker(Appointment appointment) {
       if (appointment == null) {
          return null;
       }
       return "" + appointment.getDate() + ":" + appointment.getTime() + ":" + appointment.getOfficeProvider();
    }
 
    public void saveAppointmentList(List<Appointment> appointmentList, Map<String, Map<String, Boolean>> confirmationMap, int userId) {
       Date current = new Date();
       Integer cyclicalId = null;
       for (Appointment appointment : appointmentList) {
          if (confirmationMap.get(appointmentMarker(appointment)).get("_") != null && confirmationMap.get(appointmentMarker(appointment)).get("_")) {
             appointment.setCyclicalId(cyclicalId);
             appointment.setUserId(userId);
             appointment.setCreated(current);
             appointment.setLastUpdated(current);
             appointment = crudService.create(appointment);
             if (cyclicalId == null) {
                cyclicalId = appointment.getId();
             }
          }
       }
    }
 
    public ZipCode findZipCode(String code) {
       if (code == null || code.trim().isEmpty()) {
          return null;
       }
       return crudService.find(code, ZipCode.class);
    }
 
    public Boolean todayOrBefore(Date date) {
       if (date == null) {
          return null;
       }
       Date tomorrow = new DateTime(DateTimeZone.UTC).plusDays(1).toLocalDate().toDate();
       return date.before(tomorrow);
    }
 
    public Boolean today(Date date) {
       if (date == null) {
          return null;
       }
       return new DateTime(date).toLocalDate().equals(new DateTime().toLocalDate());
    }
 
    public Visit newVisit(Patient patient, OfficeProvider officeProvider, Date date, String categoryCode) {
       Visit visit = new Visit();
       visit.setCategoryCode(categoryCode);
       visit.setPatient(patient);
       visit.setOfficeProvider(officeProvider);
       visit.setSpecialtyId(officeProvider.getSpecialtyId());
       visit.setPrimaryInsuranceId(patient.getPrimaryInsuranceId());
       DateTime datetime = new DateTime(date);
       Date time = new Date();
       if (!today(datetime.toDate())) {
          visit.setTime(date);
       } else {
          visit.setTime(time);
       }
       visit.setDate(date);
       visit.setStatus("OPEN");
       visit.setCreated(time);
       return visit;
    }
 
    public Map<String, List<String>> visitValidationMap(Visit visit) {
       Map<String, List<String>> visitValidationMap = new HashMap();
       Visit lastVisit;
       lastVisit = lastVisit(visit.getPatient());
       if (lastVisit != null && visit.getDate().equals(lastVisit.getDate())) {
          if (!visitValidationMap.containsKey("warning")) {
             visitValidationMap.put("warning", new ArrayList());
          }
          visitValidationMap.get("warning").add("The date already scheduled for this patient");
       }
       return visitValidationMap;
    }
 
    public List newList(List list) {
       if (list == null) {
          return new ArrayList();
       }
       return new ArrayList(list);
    }
 
    public List appendToMinSize(List list, int size) {
       List result = new ArrayList(list);
       while (result.size() < size) {
          result.add(null);
       }
       return result;
    }
 
    public void autocompleteReferringProviderJson() throws IOException {
       FacesContext facesContext = FacesContext.getCurrentInstance();
       ExternalContext externalContext = facesContext.getExternalContext();
       String query = externalContext.getRequestParameterMap().get("query");
       externalContext.setResponseContentType("application/json");
       externalContext.setResponseCharacterEncoding("UTF-8");
       externalContext.getResponseOutputWriter().write(referringProviderJson(query));
       facesContext.responseComplete();
    }
 
    private String referringProviderJson(String query) {
       if (query == null || query.length() < 3) {
          return "";
       }
       Map params = new HashMap();
       params.put("query", "%" + query);
       List referringProviderList = crudService.search("SELECT r FROM ReferringProvider r WHERE r.lastName LIKE :query", params, 0, "_MEDBASE");
       String json = "{\n query: '" + query + "',\n suggestions:[";
       String data = "data:[";
       for (Object referringProviderObj : referringProviderList) {
          ReferringProvider referringProvider = (ReferringProvider) referringProviderObj;
          json += "'" + referringProvider.getLastName().replace("'", "\\'") + ", " + referringProvider.getFirstName().replace("'", "\\'") + " NPI:" + referringProvider.getNpi() + "',";
          data += "'" + referringProvider.getNpi() + "',";
       }
       json = json.replaceFirst(",$", "");
       data = data.replaceFirst(",$", "");
       json += "],\n";
       json += data + "]\n}";
       return json;
    }
 
    public String referringProviderStr(String npi) {
       if (npi == null) {
          return null;
       }
       ReferringProvider referringProvider = referringProvider(npi);
       if (referringProvider != null) {
          return referringProvider.getLastName().replace("'", "\\'") + ", " + referringProvider.getFirstName().replace("'", "\\'") + " NPI:" + referringProvider.getNpi();
       }
       return null;
    }
 
    public void addVisitPayment(Visit visit, PaymentType type, PaymentMethod method, BigDecimal amount, Integer checkNumber) {
       if (visit.getPaymentList() == null) {
          visit.setPaymentList(new ArrayList());
       }
       VisitPayment payment = new VisitPayment();
       payment.setVisit(visit);
       payment.setPaymentType(type);
       payment.setPaymentTypeId(type.getId());
       payment.setPaymentMethod(method);
       payment.setAmount(amount);
       if (checkNumber != null && checkNumber != 0) {
          payment.setCheckNumber(checkNumber);
       }
       visit.getPaymentList().add(payment);
 
    }
 
    public void addVisitDiagnosis(Visit visit, Diagnosis diagnosis) {
       if (visit == null || visit.getId() == null || diagnosis == null) {
          return;
       }
       if (visit.getDiagnosisList() != null && visit.getDiagnosisList().contains(diagnosis)) {
          return;
       }
       VisitDiagnosis visitDiagnosis = new VisitDiagnosis();
       visitDiagnosis.setDiagnosis(diagnosis);
       visitDiagnosis.setVisitId(visit.getId());
       visitDiagnosis.setPosition(visitDiagnosisPosition(visit.getVisitDiagnosisList()));
       crudService.create(visitDiagnosis);
    }
 
    public void addPrevVisitDiagnosisList(Visit visit, Visit prevVisit) {
       if (visit == null || prevVisit == null || prevVisit.getVisitDiagnosisList() == null) {
          return;
       }
       int position = visitDiagnosisPosition(visit.getVisitDiagnosisList());
       for (VisitDiagnosis visitDiagnosis : prevVisit.getVisitDiagnosisList()) {
          if (visit.getDiagnosisList() != null && visit.getDiagnosisList().contains(visitDiagnosis.getDiagnosis())) {
             continue;
          }
          visitDiagnosis.setVisitId(visit.getId());
          visitDiagnosis.setId(null);
          visitDiagnosis.setStatus(null);
          visitDiagnosis.setNotes(null);
          visitDiagnosis.setPosition(position++);
          crudService.create(visitDiagnosis);
       }
 
    }
 
    public int visitDiagnosisPosition(List<VisitDiagnosis> visitDiagnosisList) {
       int pos = 1;
       if (visitDiagnosisList == null) {
          return pos;
       }
       for (VisitDiagnosis visitDiagnosis : visitDiagnosisList) {
          if (visitDiagnosis.getPosition() >= pos) {
             pos = visitDiagnosis.getPosition() + 1;
          }
       }
       return pos;
    }
 
    public void addPatientDiagnosis(Patient patient, Diagnosis diagnosis) {
       if (patient == null || patient.getId() == null || diagnosis == null) {
          return;
       }
       if (patient.getDiagnosisList() != null && patient.getDiagnosisList().contains(diagnosis)) {
          return;
       }
       PatientDiagnosis patientDiagnosis = new PatientDiagnosis();
       patientDiagnosis.setDiagnosis(diagnosis);
       patientDiagnosis.setPatientId(patient.getId());
       patientDiagnosis.setActive(true);
       crudService.create(patientDiagnosis);
    }
 
    public void addVisitProcedure(Visit visit, Procedure procedure) {
       if (visit == null || visit.getId() == null || procedure == null) {
          return;
       }
       if (visit.getProcedureList() == null) {
          visit.setProcedureList(new ArrayList());
       }
       VisitProcedure visitProcedure = new VisitProcedure();
       visitProcedure.setCode(procedure.getCode());
       visitProcedure.setProcedure(procedure);
       visitProcedure.setVisit(visit);
       visit.getProcedureList().add(visitProcedure);
       crudService.update(visit);
    }
 
    public Map visitPaymentValidationMap(PaymentType type, PaymentMethod method, BigDecimal amount, Integer checkNumber) {
       Map visitPaymentValidationMap = new HashMap();
       if (type == null) {
          visitPaymentValidationMap.put("paymentType", true);
       }
       if (method == null) {
          visitPaymentValidationMap.put("paymentMethod", true);
       } else {
          if (method.getName().equals("Check") && (checkNumber == null || checkNumber == 0)) {
             visitPaymentValidationMap.put("checkNumber", true);
          }
       }
       if (amount == null || amount.equals(BigDecimal.ZERO)) {
          visitPaymentValidationMap.put("amount", true);
       }
       return visitPaymentValidationMap;
    }
 
    public PatientAllergy createPatientAllergy(int patientId, Allergy allergy, String reaction) {
       PatientAllergy patientAllergy = new PatientAllergy();
       patientAllergy.setPatientId(patientId);
       patientAllergy.setAllergy(allergy);
       patientAllergy.setReaction(reaction);
       patientAllergy.setActive(true);
       return patientAllergy;
    }
 
    public ProviderInsurance addProviderInsurance(OfficeProvider officeProvider) {
       ProviderInsurance providerInsurance = new ProviderInsurance();
       providerInsurance.setProviderNpi(officeProvider.getAssociatedProvider().getNpi());
       return providerInsurance;
    }
 
    public Map providerInsuranceValidationMap(ProviderInsurance providerInsurance) {
       Map providerInsuranceValidationMap = new HashMap();
       if (providerInsurance.getInsuranceId() == null) {
          providerInsuranceValidationMap.put("insurance", true);
       }
       return providerInsuranceValidationMap;
    }
 
    public List<Insurance> excludeDuplicates(List<Insurance> insuranceList, List<ProviderInsurance> providerInsuranceList, boolean ignore) {
       if (ignore) {
          return insuranceList;
       }
       List<Insurance> resultList = new ArrayList(insuranceList);
       List<Insurance> existsList = new ArrayList();
       for (ProviderInsurance providerInsurance : providerInsuranceList) {
          if (providerInsurance.getOfficeCode() == null) {
             existsList.add(providerInsurance.getInsurance());
          }
       }
       resultList.removeAll(existsList);
       return resultList;
    }
 
    public void saveProviderInsurance(ProviderInsurance providerInsurance, int userId) {
       if (providerInsurance.getId() == null) {
          providerInsurance.setUserId(userId);
          providerInsurance.setCreated(new Date());
          crudService.create(providerInsurance, "_HEALTHCARE");
 
       }
       crudService.update(providerInsurance, "_HEALTHCARE");
    }
 
    public List<Appointment> filterPastAppointments(List<Appointment> appointmentList) {
       List<Appointment> resultList = new ArrayList();
       Date current = new DateTime().withTime(0, 0, 0, 0).toDate();
       for (Appointment appointment : appointmentList) {
          if (appointment.getDate().after(current) || appointment.getDate().equals(current)) {
             resultList.add(appointment);
          }
       }
       return resultList;
    }
 
    public void mergePatient(int sourcePatientId, int destinationPatientId, int userId) {
       Map<Integer, Object> params = new HashMap();
       params.put(1, sourcePatientId);
       params.put(2, destinationPatientId);
       params.put(3, userId);
       crudService.executeNamedNativeQuery("Patient.merge", params, "_MEDBASE");
    }
 
    public VisitVitals visitVitals(Visit visit) {
       if (visit == null || visit.getId() == null) {
          return null;
       }
       VisitVitals visitVitals = crudService.find(visit.getId(), VisitVitals.class);
       if (visitVitals == null) {
          visitVitals = new VisitVitals();
          visitVitals.setVisitId(visit.getId());
          visitVitals = crudService.create(visitVitals);
       }
       return visitVitals;
    }
 
    public VisitCc visitCc(Visit visit) {
       if (visit == null || visit.getId() == null) {
          return null;
       }
       VisitCc visitCc = crudService.find(visit.getId(), VisitCc.class);
       if (visitCc == null) {
          visitCc = new VisitCc();
          visitCc.setVisitId(visit.getId());
       }
       return visitCc;
    }
 
    public VisitHpi visitHpi(Visit visit) {
       if (visit == null || visit.getId() == null) {
          return null;
       }
       VisitHpi visitHpi = crudService.find(visit.getId(), VisitHpi.class);
       if (visitHpi == null) {
          visitHpi = new VisitHpi();
          visitHpi.setVisitId(visit.getId());
       }
       return visitHpi;
    }
 
    public VisitRos visitRos(Visit visit) {
       if (visit == null || visit.getId() == null) {
          return null;
       }
       VisitRos visitRos = crudService.find(visit.getId(), VisitRos.class);
       if (visitRos == null) {
          visitRos = new VisitRos();
          visitRos.setVisitId(visit.getId());
       }
       return visitRos;
    }
 
    public VisitPfsh visitPfsh(Visit visit) {
       if (visit == null || visit.getId() == null) {
          return null;
       }
       VisitPfsh visitPfsh = crudService.find(visit.getId(), VisitPfsh.class);
       if (visitPfsh == null) {
          visitPfsh = new VisitPfsh();
          visitPfsh.setVisitId(visit.getId());
       }
       return visitPfsh;
    }
 
    public VisitExam visitExam(Visit visit) {
       if (visit == null || visit.getId() == null) {
          return null;
       }
       VisitExam visitExam = crudService.find(visit.getId(), VisitExam.class);
       if (visitExam == null) {
          visitExam = new VisitExam();
          visitExam.setVisitId(visit.getId());
       }
       return visitExam;
    }
 
    public VisitTemplateState visitTemplateState(Visit visit) {
       if (visit == null || visit.getId() == null) {
          return null;
       }
       VisitTemplateState visitTemplateState = crudService.find(visit.getId(), VisitTemplateState.class);
       if (visitTemplateState == null) {
          visitTemplateState = new VisitTemplateState();
          visitTemplateState.setVisitId(visit.getId());
       }
       return visitTemplateState;
    }
 
 //   public void testPath() {
 //      HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
 //      File script = new File(request.getServletContext().getRealPath("/script"));
 //      for (String f : script.list()) {
 //         System.out.println(f);
 //      }
 //   }
    public String sectionTemplate(String section, Visit visit) {
       HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
       File sectionDir = new File(request.getServletContext().getRealPath("/VisitProgressNote/section"));
       String commonTemplateName = section + ".html";
       String specialtyTemplateName = section + "." + specialty(visit.getSpecialtyId()).getName().replaceAll("\\s", "").toLowerCase() + ".html";
       File commonTemplate = null;
       File specialtyTemplate = null;
       File sectionTemplate = null;
       for (File f : sectionDir.listFiles()) {
          if (f.getName().equals(commonTemplateName)) {
             commonTemplate = f;
          }
          if (f.getName().equals(specialtyTemplateName)) {
             specialtyTemplate = f;
             break;
          }
       }
       if (specialtyTemplate != null) {
          sectionTemplate = specialtyTemplate;
       } else if (commonTemplate != null) {
          sectionTemplate = commonTemplate;
       }
       if (sectionTemplate != null) {
          return Util.getFileContent(sectionTemplate);
       }
       return null;
    }
 
    public String rosSectionFilter(String source, Visit visit) {
       if (visit == null || source == null || !source.contains("$rosSectionFilter")) {
          return source;
       }
       Map<String, Object> params = new HashMap();
       params.put("patientId", visit.getPatient().getId());
       List patientAllergyList = crudService.search("SELECT pa FROM PatientAllergy pa WHERE pa.patientId = :patientId AND pa.active = TRUE", params, 0, "_MEDBASE");
       Formatter fmt = new Formatter();
       if (patientAllergyList.isEmpty() && visit.getPatient().getStatus().getNkda() != null && visit.getPatient().getStatus().getNkda()) {
          fmt.format("Allergies: NKDA");
       } else if (!patientAllergyList.isEmpty()) {
          fmt.format("Allergies: ");
          for (Object patientAllergyObj : patientAllergyList) {
             PatientAllergy patientAllergy = (PatientAllergy) patientAllergyObj;
             String option = patientAllergy.getAllergy().getDescription();
             fmt.format("%1$s; ", option);
          }
       }
       return source.replaceFirst("\\$rosSectionFilter", fmt.toString().replaceFirst("; $", ""));
    }
 
    public List<PatientInsurance> registrationFormInsuranceList(Patient patient) {
       List<PatientInsurance> registrationFormInsuranceList = patient.getInsuranceList() == null ? new ArrayList() : new ArrayList(patient.getInsuranceList());
       while (registrationFormInsuranceList.size() < 2) {
          PatientInsurance pi = new PatientInsurance();
          pi.setAccountIndex((short) (registrationFormInsuranceList.size() + 1));
          pi.setGuarantor(new Guarantor());
          registrationFormInsuranceList.add(pi);
       }
       return registrationFormInsuranceList;
    }
 
    public void addVisitProgressNoteAddendum(VisitProgressNote progressNote, String text, int userId) {
       VisitProgressNoteAddendum visitProgressNoteAddendum = new VisitProgressNoteAddendum();
       visitProgressNoteAddendum.setProgressNote(progressNote);
       visitProgressNoteAddendum.setText(text);
       visitProgressNoteAddendum.setUserId(userId);
       visitProgressNoteAddendum.setDatetime(new Date());
       progressNote.getAddendumList().add(visitProgressNoteAddendum);
       crudService.update(progressNote, "_MEDBASE");
    }
 
    public void signVisitServiceLevel(VisitServiceLevel serviceLevel, int userId) {
       serviceLevel.setSignedUserId(userId);
       serviceLevel.setSigned(true);
       serviceLevel.setSignedDatetime(new Date());
       if (serviceLevel.getSubmittedCptCode() != null) {
          if (serviceLevel.visitCptCode() != null) {
             for (VisitProcedure visitProcedure : serviceLevel.getVisit().getProcedureList()) {
                if (visitProcedure.getCode().equals(serviceLevel.visitCptCode())) {
                   serviceLevel.getVisit().getProcedureList().remove(visitProcedure);
                   break;
                }
             }
          }
          Procedure procedure = crudService.find(serviceLevel.getSubmittedCptCode(), Procedure.class);
          VisitProcedure visitProcedure = new VisitProcedure();
          visitProcedure.setVisit(serviceLevel.getVisit());
          visitProcedure.setProcedure(procedure);
          visitProcedure.setCode(procedure.getCode());
          visitProcedure.setProcedure(procedure);
          visitProcedure.setVisit(serviceLevel.getVisit());
          serviceLevel.getVisit().getProcedureList().add(0, visitProcedure);
          crudService.update(serviceLevel.getVisit());
       }
       for (VisitDiagnosis visitDiagnosis : serviceLevel.getVisit().getVisitDiagnosisList()) {
          if (visitDiagnosis.getStatus() == null || visitDiagnosis.getStatus().getId() == 1 /*NULL OR MINOR*/) {
             continue;
          }
          boolean exists = false;
          for (PatientDiagnosis problem : serviceLevel.getVisit().getPatient().getActiveProblemList()) {
             if (problem.getDiagnosis().equals(visitDiagnosis.getDiagnosis())) {
                if (visitDiagnosis.getStatus().getId() == 4 /*RESOLVED*/) {
                   problem.setActive(false);
                   problem.setResolutionDate(visitDiagnosis.getResolutionDate());
                   crudService.update(problem);
                }
                exists = true;
                break;
             }
          }
          if (exists) {
             continue;
          }
          PatientDiagnosis patientDiagnosis = new PatientDiagnosis();
          patientDiagnosis.setDiagnosis(visitDiagnosis.getDiagnosis());
          patientDiagnosis.setPatientId(serviceLevel.getVisit().getPatient().getId());
          patientDiagnosis.setVisitId(serviceLevel.getVisit().getId());
          patientDiagnosis.setActive(visitDiagnosis.getStatus().getId() == 4 ? false : true);
          patientDiagnosis.setResolutionDate(visitDiagnosis.getResolutionDate());
          patientDiagnosis.setChronicity(visitDiagnosis.getChronicity());
          patientDiagnosis.setOnset(visitDiagnosis.getOnset());
          patientDiagnosis.setNote(visitDiagnosis.getNotes());
          crudService.create(patientDiagnosis);
       }
       FacesContext context = FacesContext.getCurrentInstance();
       ExternalContext ec = context.getExternalContext();
       HttpServletRequest req = (HttpServletRequest) ec.getRequest();
       String remoteAddress = req.getHeader("x-forwarded-for") != null ? req.getHeader("x-forwarded-for") : req.getRemoteAddr();
       serviceLevel.setSignedFromIp(remoteAddress);
       crudService.update(serviceLevel.getVisit().getProgressNote());
    }
 
    public List officeProcedureList(String patientType) {
       Map params = new HashMap();
       String[] codes;
       if ("New".equals(patientType)) {
          String[] temp = {"99201", "99202", "99203", "99204", "99205"};
          codes = temp;
       } else {
          String[] temp = {"99211", "99212", "99213", "99214", "99215"};
          codes = temp;
       }
       params.put("codes", Arrays.asList(codes));
       return crudService.search("SELECT p FROM Procedure p WHERE p.code IN :codes ORDER BY p.code", params, 0);
    }
 
    public List dupletList(List list) {
       if (list == null || list.isEmpty()) {
          return list;
       }
       int i = 0;
       List dupletList = new ArrayList();
       List duplet = null;
       for (Object obj : list) {
          if (i == 0) {
             duplet = new ArrayList();
             dupletList.add(duplet);
             i++;
          } else {
             i--;
          }
          duplet.add(obj);
       }
       if (duplet.size() < 2) {
          duplet.add(null);
       }
       return dupletList;
    }
 
    public PatientPastMedicalHistory createPatientPastMedicalHistory(Patient patient) {
       PatientPastMedicalHistory patientPastMedicalHistory = new PatientPastMedicalHistory();
       patientPastMedicalHistory.setPatientId(patient.getId());
       return patientPastMedicalHistory;
    }
 
    public String join(Map checkedMap, String separator) {
       String join = "";
       for (Object key : checkedMap.keySet()) {
          if (checkedMap.get(key) != null && (boolean) checkedMap.get(key)) {
             join += key + separator;
          }
       }
       if (!join.isEmpty() && join.length() >= separator.length()) {
          join = join.substring(0, join.length() - 1 - separator.length());
       }
       return join;
    }
 
    public void shrinkListCheckedMapOnsetMap(List list, Map checkedMap, Map onsetMap) {
       if (list == null || list.isEmpty()) {
          return;
       }
       List removeList = new ArrayList();
       for (Object obj : list) {
          if (checkedMap.containsKey(obj) && checkedMap.get(obj) != null && (boolean) checkedMap.get(obj)) {
          } else {
             removeList.add(obj);
          }
       }
       list.removeAll(removeList);
       for (Object obj : removeList) {
          checkedMap.remove(obj);
          onsetMap.remove(obj);
       }
    }
 
    public void addPatientProblemList(List list, Map checkedMap, Map onsetMap, Patient patient) {
       for (PatientDiagnosis problem : patient.getActiveProblemList()) {
          if (!list.contains(problem.getDiagnosis())) {
             list.add(problem.getDiagnosis());
             if (problem.getOnset() != null) {
                onsetMap.put(problem.getDiagnosis(), problem.getOnset());
             }
          }
       }
    }
 
    public void saveProblemList(String type, List<PatientHistoryProblem> list, Map checkedMap, Map onsetMap, String other, Patient patient, int userId) {
       if (list == null || list.isEmpty()) {
          return;
       }
       for (PatientHistoryProblem problem : list) {
          if (checkedMap.containsKey(problem.getName()) && checkedMap.get(problem.getName()) != null && (boolean) checkedMap.get(problem.getName())) {
             PatientPastMedicalHistory history = new PatientPastMedicalHistory();
             history.setPatientId(patient.getId());
             history.setType(type);
             history.setDescription(problem.getName());
             if (onsetMap.containsKey(problem.getName())) {
                history.setOnset((String) onsetMap.get(problem.getName()));
             }
             history.setUserId(userId);
             history.setCreated(new Date());
             crudService.create(history);
          }
       }
       if (checkedMap.containsKey("other") && checkedMap.get("other") != null && (boolean) checkedMap.get("other")) {
          PatientPastMedicalHistory history = new PatientPastMedicalHistory();
          history.setPatientId(patient.getId());
          history.setType(type);
          history.setDescription(other);
          if (onsetMap.containsKey("other")) {
             history.setOnset((String) onsetMap.get("other"));
          }
          history.setUserId(userId);
          history.setCreated(new Date());
          crudService.create(history);
       }
    }
 
    public void saveProblemList(List<PatientHistoryProblem> list, Map checkedMap, Map onsetMap, String other, Patient patient, int userId) {
       saveProblemList("problem", list, checkedMap, onsetMap, other, patient, userId);
    }
 
    public void saveSurgeryList(List<PatientHistoryProblem> list, Map checkedMap, Map onsetMap, String other, Patient patient, int userId) {
       saveProblemList("surgery", list, checkedMap, onsetMap, other, patient, userId);
    }
 
    public void saveTestList(List<PatientHistoryExamTest> list, Map checkedMap, Map onsetMap, Map abnormalMap, String other, Patient patient, int userId) {
       if (list == null || list.isEmpty()) {
          return;
       }
       for (PatientHistoryExamTest test : list) {
          if (checkedMap.containsKey(test.getName()) && checkedMap.get(test.getName()) != null && (boolean) checkedMap.get(test.getName())) {
             PatientPastMedicalHistory history = new PatientPastMedicalHistory();
             history.setPatientId(patient.getId());
             history.setType("test/exam");
             history.setDescription(test.getName());
             if (onsetMap.containsKey(test.getName())) {
                history.setOnset((String) onsetMap.get(test.getName()));
             }
             if (abnormalMap.containsKey(test.getName())) {
                String abnormalStr = (String) abnormalMap.get(test.getName());
                Boolean abnormal = null;
                if (abnormalStr != null) {
                   if (abnormalStr.equals("abnormal")) {
                      abnormal = true;
                   } else {
                      abnormal = false;
                   }
                }
                history.setAbnormal(abnormal);
             }
             history.setUserId(userId);
             history.setCreated(new Date());
             crudService.create(history);
          }
       }
       if (checkedMap.containsKey("other") && checkedMap.get("other") != null && (boolean) checkedMap.get("other")) {
          PatientPastMedicalHistory history = new PatientPastMedicalHistory();
          history.setPatientId(patient.getId());
          history.setType("test/exam");
          history.setDescription(other);
          if (onsetMap.containsKey("other")) {
             history.setOnset((String) onsetMap.get("other"));
          }
          if (abnormalMap.containsKey("other")) {
             String abnormalStr = (String) abnormalMap.get("other");
             Boolean abnormal = null;
             if (abnormalStr != null) {
                if (abnormalStr.equals("abnormal")) {
                   abnormal = true;
                } else {
                   abnormal = false;
                }
             }
             history.setAbnormal(abnormal);
          }
          history.setUserId(userId);
          history.setCreated(new Date());
          crudService.create(history);
       }
    }
 
    public void saveConditionList(String otherMember, Map<String, Map<String, Boolean>> condtitionChecked, Map<String, Map<String, String>> condtitionNote, Map<String, String> conditionOther, Patient patient, int userId) {
       for (String member : condtitionChecked.keySet()) {
          for (String condition : condtitionChecked.get(member).keySet()) {
             if (condtitionChecked.get(member).get(condition) != null && condtitionChecked.get(member).get(condition)) {
                if (member.equals("other") && (otherMember == null || otherMember.isEmpty())) {
                   continue;
                }
                PatientFamilyHistory history = new PatientFamilyHistory();
                history.setFamilyMember(member.equals("other") ? otherMember : member);
                history.setPatientId(patient.getId());
                history.setCondition(condition.equals("other") ? conditionOther.get(member) : condition);
                history.setNote(condtitionNote.get(member).get(condition));
                history.setUserId(userId);
                history.setCreated(new Date());
                crudService.create(history);
             }
          }
       }
    }
 
    public void saveDiagnosisProblemList(List<Diagnosis> list, Map checkedMap, Map onsetMap, Patient patient, int userId) {
       if (list == null || list.isEmpty()) {
          return;
       }
       for (Diagnosis diagnosis : list) {
          if (checkedMap.containsKey(diagnosis) && checkedMap.get(diagnosis) != null && (boolean) checkedMap.get(diagnosis)) {
             PatientPastMedicalHistory history = new PatientPastMedicalHistory();
             history.setPatientId(patient.getId());
             history.setType("problem");
             history.setCodingSchema("ICD-9");
             history.setCode(diagnosis.getCode());
             history.setDescription(diagnosis.getDescription());
             if (onsetMap.containsKey(diagnosis.getDescription())) {
                history.setOnset((String) onsetMap.get(diagnosis));
             }
             history.setUserId(userId);
             history.setCreated(new Date());
             crudService.create(history);
          }
       }
    }
 
    public void saveProcedureList(String type, List<Procedure> list, Map checkedMap, Map onsetMap, Map abnormalMap, Patient patient, int userId) {
       if (list == null || list.isEmpty()) {
          return;
       }
       for (Procedure procedure : list) {
          if (checkedMap.containsKey(procedure) && checkedMap.get(procedure) != null && (boolean) checkedMap.get(procedure)) {
             PatientPastMedicalHistory history = new PatientPastMedicalHistory();
             history.setPatientId(patient.getId());
             history.setType(type);
             history.setCodingSchema("CPT");
             history.setCode(procedure.getCode());
             history.setDescription(procedure.getDescription());
             if (onsetMap.containsKey(procedure)) {
                history.setOnset((String) onsetMap.get(procedure));
             }
             if (abnormalMap != null && abnormalMap.containsKey(procedure)) {
                String abnormalStr = (String) abnormalMap.get(procedure);
                Boolean abnormal = null;
                if (abnormalStr != null) {
                   if (abnormalStr.equals("abnormal")) {
                      abnormal = true;
                   } else {
                      abnormal = false;
                   }
                }
                history.setAbnormal(abnormal);
             }
             history.setUserId(userId);
             history.setCreated(new Date());
             crudService.create(history);
          }
       }
    }
 
    public void saveDiagnosisConditionList(String otherMember, Map<String, Map<Diagnosis, Boolean>> condtitionChecked, Map<String, Map<Diagnosis, String>> condtitionNote, Patient patient, int userId) {
       for (String member : condtitionChecked.keySet()) {
          for (Diagnosis diagnosis : condtitionChecked.get(member).keySet()) {
             if (condtitionChecked.get(member).get(diagnosis) != null && condtitionChecked.get(member).get(diagnosis)) {
                PatientFamilyHistory history = new PatientFamilyHistory();
                history.setFamilyMember(member.equals("other") ? otherMember : member);
                history.setPatientId(patient.getId());
                history.setCodingSchema("ICD-9");
                history.setCode(diagnosis.getCode());
                history.setCondition(diagnosis.getDescription());
                history.setNote(condtitionNote.get(member).get(diagnosis));
                history.setUserId(userId);
                history.setCreated(new Date());
                crudService.create(history);
             }
          }
       }
    }
 
    public void saveSocialHistoryList(String otherType, Map<String, String> status, Map<String, String> quitDate, Map<String, String> unit, Map<String, String> qty, Map<String, String> duration, Map<String, String> description, Patient patient, int userId) {
       for (String type : status.keySet()) {
          PatientSocialHistory history = new PatientSocialHistory();
          history.setType(type.equals("other") ? otherType : type);
          if (status.get(type) != null && status.get(type).equals("quit")) {
             history.setQuitDate(quitDate.get(type));
          }
          String descr = "";
          if (type.equals("smoking") && status.get(type) != null && status.get(type).equals("current")) {
             if (unit.containsKey(type) && qty.containsKey(type)) {
                descr += qty.get(type) + " " + unit.get(type) + " per day, ";
             }
             if (duration.containsKey(type)) {
                descr += duration.get(type) + ", ";
             }
          }
          if (description.containsKey(type)) {
             descr += description.get(type);
          }
          descr = descr.trim().replaceAll("[\\.,]$", "");
          history.setStatus(status.get(type));
          history.setDescription(descr.isEmpty() ? null : descr);
          history.setPatientId(patient.getId());
          history.setUserId(userId);
          history.setCreated(new Date());
          crudService.create(history);
       }
    }
 
    public void importHistory(VisitProgressNote progressNote) {
       VisitPfsh pfsh = progressNote.getPfsh();
       Map<String, String> types = new HashMap();
       types.put("problem", "");
       types.put("surgery", "");
       types.put("test/exam", "");
       boolean changed = false;
       for (PatientPastMedicalHistory history : progressNote.getVisit().getPatient().getPastMedicalHistoryList()) {
          String str = "";
          if (history.getDescription() != null) {
             str += history.getDescription();
          }
          if (history.getOnset() != null && !history.getOnset().isEmpty() && !history.getType().equals("problem")) {
             str += " (" + history.getOnset() + ")";
          }
          if (history.getAbnormal() != null) {
             str += (history.getAbnormal() ? " - abnormal" : " - normal");
          }
          if (!str.isEmpty()) {
             types.put(history.getType(), types.get(history.getType()) + str + ". ");
          }
       }
       String pastHistory = "";
       if (!types.get("problem").isEmpty()) {
          pastHistory += "Problems: " + types.get("problem");
       }
       if (!types.get("surgery").isEmpty()) {
          pastHistory += (pastHistory.isEmpty() ? "" : "\n") + "Surgeries: " + types.get("surgery");
       }
       if (!types.get("test/exam").isEmpty()) {
          pastHistory += (pastHistory.isEmpty() ? "" : "\n") + "Tests/Exams: " + types.get("test/exam");
       }
       if (!pastHistory.isEmpty()) {
          pfsh.setPastHistory(pastHistory.trim());
          changed = true;
       }
       Map<String, String> members = new HashMap();
       for (PatientFamilyHistory history : progressNote.getVisit().getPatient().getFamilyHistoryList()) {
          if (!members.containsKey(history.getFamilyMember())) {
             members.put(history.getFamilyMember(), history.getFamilyMember() + ": ");
          }
          members.put(history.getFamilyMember(), members.get(history.getFamilyMember()) + history.getCondition() + "; ");
       }
       String familyHistory = "";
       for (String key : members.keySet()) {
          familyHistory += members.get(key).replaceAll("; $", ". ");
       }
       if (!familyHistory.isEmpty()) {
          pfsh.setFamilyHistory(familyHistory.trim());
          changed = true;
       }
       String socialHistory = "";
       List<String> typeList = new ArrayList();
       for (PatientSocialHistory history : progressNote.getVisit().getPatient().getSocialHistoryList()) {
          if (typeList.contains(history.getType())) {
             continue;
          }
          typeList.add(history.getType());
          socialHistory += history.getType();
          if (history.getStatus() != null) {
             socialHistory += " (" + history.getStatus();
             if (history.getQuitDate() != null) {
                socialHistory += ":" + history.getQuitDate();
             }
             socialHistory += ")";
          }
          if (history.getDescription() != null) {
             socialHistory += ": " + history.getDescription() + "; ";
          } else {
             socialHistory += "; ";
          }
       }
       if (!socialHistory.isEmpty()) {
          pfsh.setSocialHistory(socialHistory.trim().replaceAll(";+$", "."));
          changed = true;
       }
       if (changed) {
          progressNote.cleaned();
          if (progressNote.isPersisted()) {
             crudService.update(progressNote);
          } else {
             crudService.create(progressNote);
          }
       }
    }
 
    public void savePatientHistoricData(Patient patient) {
       String[] fields = {"lastName", "firstName", "address", "city", "state", "zipCode", "homePhone", "cellPhone", "email"};
       for (String field : fields) {
          String newVal = patient.getField(field);
          String oldVal = patient.getLastHistoricData("patient", field) == null ? null : patient.getLastHistoricData("patient", field).getValue();
          if (newVal == null && oldVal == null) {
             continue;
          } else if (newVal != null && oldVal != null && newVal.equals(oldVal)) {
             continue;
          }
          PatientHistoricData newHistoricData = new PatientHistoricData();
          newHistoricData.setPatientId(patient.getId());
          newHistoricData.setUserId(patient.getUserId());
          newHistoricData.setDate(new Date());
          newHistoricData.setTable("patient");
          newHistoricData.setField(field);
          newHistoricData.setValue(newVal);
          crudService.create(newHistoricData);
       }
    }
 
    public PatientMedication addPatientMedication(Patient patient) {
       PatientMedication patientMedication = new PatientMedication();
       patientMedication.setPatient(patient);
       patientMedication.setPatientId(patient.getId());
       patientMedication.setActive(true);
       patientMedication.setStatus("NEW");
       return patientMedication;
    }
 
    public PatientMedication addVisitMedication(Visit visit) {
       PatientMedication patientMedication = new PatientMedication();
       patientMedication.setPatient(visit.getPatient());
       patientMedication.setPatientId(visit.getPatient().getId());
       patientMedication.setVisit(visit);
       if (visit.getOfficeProvider().getAssociatedNpi() != null) {
          Map params = new HashMap();
          params.put("npi", visit.getOfficeProvider().getAssociatedNpi());
          List result = crudService.search("SELECT r FROM ReferringProvider r WHERE r.npi LIKE :npi", params, 1, "_MEDBASE");
          if (!result.isEmpty()) {
             ReferringProvider referringProvider = (ReferringProvider) result.get(0);
             patientMedication.setPrescriber(referringProvider);
          }
       }
       patientMedication.setStatus("NEW");
       patientMedication.setActive(true);
       return patientMedication;
    }
 
    public Map patientMedicationValidationMap(PatientMedication medication) {
       Map patientInsuranceValidationMap = new HashMap();
       if (medication.getDrug() == null || medication.getDrug().trim().isEmpty()) {
          patientInsuranceValidationMap.put("drug", true);
       }
       for (PatientMedication patientMedication : medication.getPatient().getMedicationList()) {
          if (!patientMedication.equals(medication) && patientMedication.getActive() && patientMedication.getDrugCode() != null
                  && patientMedication.getDrugCode().equals(medication.getDrugCode())) {
             patientInsuranceValidationMap.put("duplicate", true);
             break;
          }
       }
       return patientInsuranceValidationMap;
    }
 
    public PatientAllergy addPatientAllergy(Patient patient) {
       PatientAllergy patientAllergy = new PatientAllergy();
       patientAllergy.setPatient(patient);
       patientAllergy.setPatientId(patient.getId());
       patientAllergy.setActive(true);
       return patientAllergy;
    }
 
    public Map patientAllergyValidationMap(PatientAllergy allergy) {
       Map patientInsuranceValidationMap = new HashMap();
       if (allergy.getAllergy() == null) {
          patientInsuranceValidationMap.put("allergy", true);
       }
       return patientInsuranceValidationMap;
    }
 
    public void assignAllergy(PatientAllergy patientAllergy, String allergyStr) {
       if (allergyStr != null) {
          Map params = new HashMap();
          params.put("_:description", allergyStr);
          List result = crudService.search("SELECT a FROM Allergy a WHERE a.description = :description", params, 1, "_MEDBASE");
          if (!result.isEmpty()) {
             Allergy allergy = (Allergy) result.get(0);
             patientAllergy.setAllergy(allergy);
             return;
          }
       }
       patientAllergy.setAllergy(null);
    }
 
    public void savePatientAllergy(PatientAllergy patientAllergy, int userId) {
       if (patientAllergy.getId() == null) {
          patientAllergy.setDatetime(new Date());
          patientAllergy.setUserId(userId);
          crudService.create(patientAllergy);
       } else {
          crudService.update(patientAllergy);
       }
    }
 
    public void assignPrescriber(PatientMedication patientMedication, String prescriberStr) {
       if (prescriberStr != null) {
          Pattern p = Pattern.compile("NPI:(\\d{10})", Pattern.CASE_INSENSITIVE);
          Matcher m = p.matcher(prescriberStr);
          if (m.find()) {
             String npi = m.group(1);
             Map params = new HashMap();
             params.put("npi", npi);
             List result = crudService.search("SELECT r FROM ReferringProvider r WHERE r.npi LIKE :npi", params, 1, "_MEDBASE");
             if (!result.isEmpty()) {
                ReferringProvider referringProvider = (ReferringProvider) result.get(0);
                patientMedication.setPrescriber(referringProvider);
                return;
             }
          }
       }
       patientMedication.setPrescriber(null);
    }
 
    public void assignDrug(Object obj, String drugStr) {
       if (obj != null && obj instanceof PatientMedication) {
          assignDrug((PatientMedication) obj, drugStr);
       }
    }
 
    private void assignDrug(PatientMedication patientMedication, String drugStr) {
       if (drugStr != null) {
          Map params = new HashMap();
          params.put("_:name", drugStr);
          List result = crudService.search("SELECT d FROM Drug d WHERE d.name = :name", params, 1, "_MEDBASE");
          if (!result.isEmpty()) {
             Drug drug = (Drug) result.get(0);
             patientMedication.setDrug(drug.getName());
             patientMedication.setDrugCode(drug.getCode());
             patientMedication.setTty(drug.getTty());
          } else {
             patientMedication.setDrug(drugStr);
             patientMedication.setDrugCode(null);
             patientMedication.setTty(null);
          }
          return;
       }
       patientMedication.setDrug(null);
       patientMedication.setDrugCode(null);
       patientMedication.setTty(null);
    }
 
    public void savePatientMedication(PatientMedication patientMedication, int userId) {
       if (patientMedication.getId() == null) {
          patientMedication.setCreated(new Date());
          patientMedication.setUserId(userId);
          crudService.create(patientMedication);
       } else {
          patientMedication.setLastUpdated(new Date());
          patientMedication.setLastUpdatedUserId(userId);
          crudService.update(patientMedication);
       }
    }
 
    public void autocompleteDrugJson() throws IOException {
       FacesContext facesContext = FacesContext.getCurrentInstance();
       ExternalContext externalContext = facesContext.getExternalContext();
       String query = externalContext.getRequestParameterMap().get("query");
       externalContext.setResponseContentType("application/json");
       externalContext.setResponseCharacterEncoding("UTF-8");
       externalContext.getResponseOutputWriter().write(drugJson(query));
       facesContext.responseComplete();
    }
 
    private String drugJson(String query) {
       if (query == null || query.length() < 3) {
          return "";
       }
       Map params = new HashMap();
       params.put("query", "%" + query);
       List drugList = crudService.search("SELECT d FROM Drug d WHERE d.name LIKE :query ORDER BY d.name", params, 0, "_MEDBASE");
       String json = "{\n query: '" + query + "',\n suggestions:[";
       String data = "data:[";
       for (Object drugObj : drugList) {
          Drug drug = (Drug) drugObj;
          if (drug.getTty().equals("SBD")) {
             json += "'<b>" + drug.getName().replaceAll("'", "\\\\'") + "</b>',";
          } else {
             json += "'" + drug.getName().replaceAll("'", "\\\\'") + "',";
          }
          data += "'" + drug.getCode() + "',";
       }
       json = json.replaceFirst(",$", "");
       data = data.replaceFirst(",$", "");
       json += "],\n";
       json += data + "]\n}";
       return json;
    }
 
    public void autocompletePharmacyJson() throws IOException {
       FacesContext facesContext = FacesContext.getCurrentInstance();
       ExternalContext externalContext = facesContext.getExternalContext();
       String query = externalContext.getRequestParameterMap().get("query");
       externalContext.setResponseContentType("application/json");
       externalContext.setResponseCharacterEncoding("UTF-8");
       externalContext.getResponseOutputWriter().write(pharmacyJson(query));
       facesContext.responseComplete();
    }
 
    private String pharmacyJson(String query) {
       if (query == null || query.length() < 3) {
          return "";
       }
       Map params = new HashMap();
       params.put("query", "%" + query);
       List pharmacyList = crudService.search("SELECT p FROM Pharmacy p WHERE p.name LIKE :query ORDER BY p.name", params, 0, "_MEDBASE");
       String json = "{\n query: '" + query + "',\n suggestions:[";
       String data = "data:[";
       for (Object pharmacyObj : pharmacyList) {
          Pharmacy pharmacy = (Pharmacy) pharmacyObj;
          json += "'" + pharmacy.getName().replaceAll("'", "\\\\'") + "',";
          data += "'" + pharmacy.getNcpdpProviderId() + "',";
       }
       json = json.replaceFirst(",$", "");
       data = data.replaceFirst(",$", "");
       json += "],\n";
       json += data + "]\n}";
       return json;
    }
 
    public PatientImmunization addPatientImmunization(Patient patient) {
       PatientImmunization patientImmunization = new PatientImmunization();
       patientImmunization.setPatient(patient);
       patientImmunization.setPatientId(patient.getId());
       return patientImmunization;
    }
 
    public Map patientImmunizationValidationMap(PatientImmunization patientMedication) {
       Map patientImmunizationValidationMap = new HashMap();
       if (patientMedication.getImmunizationType() == null || patientMedication.getImmunizationType().trim().isEmpty()) {
          patientImmunizationValidationMap.put("immunizationType", true);
       }
       return patientImmunizationValidationMap;
    }
 
    public void assignCvx(PatientImmunization patientImmunization, String cvxStr) {
       if (cvxStr != null) {
          Map params = new HashMap();
          params.put("_:description", cvxStr);
          List result = crudService.search("SELECT c FROM Cvx c WHERE c.description = :description ORDER BY c.description", params, 1, "_MEDBASE");
          if (!result.isEmpty()) {
             Cvx cvx = (Cvx) result.get(0);
             patientImmunization.setImmunizationType(cvx.getDescription());
             patientImmunization.setCvxCode(cvx.getCode());
          } else {
             patientImmunization.setImmunizationType(cvxStr);
             patientImmunization.setCvxCode(null);
          }
          return;
       }
       patientImmunization.setImmunizationType(null);
       patientImmunization.setCvxCode(null);
    }
 
    public void assignCvx(PatientImmunization patientImmunization) {
       if (patientImmunization.getCvxCode() != null) {
          Cvx cvx = crudService.find(patientImmunization.getCvxCode(), Cvx.class);
          patientImmunization.setImmunizationType(cvx.getShortDescription());
       } else {
          patientImmunization.setImmunizationType(null);
          patientImmunization.setCvxCode(null);
       }
    }
 
    public void savePatientImmunization(PatientImmunization patientImmunization, int userId) {
       if (patientImmunization.getId() == null) {
          patientImmunization.setCreated(new Date());
          patientImmunization.setUserId(userId);
          crudService.create(patientImmunization);
       } else {
          patientImmunization.setLastUpdated(new Date());
          patientImmunization.setLastUpdatedUserId(userId);
          crudService.update(patientImmunization);
       }
    }
 
    public void autocompleteCvxJson() throws IOException {
       FacesContext facesContext = FacesContext.getCurrentInstance();
       ExternalContext externalContext = facesContext.getExternalContext();
       String query = externalContext.getRequestParameterMap().get("query");
       externalContext.setResponseContentType("application/json");
       externalContext.setResponseCharacterEncoding("UTF-8");
       externalContext.getResponseOutputWriter().write(cvxJson(query));
       facesContext.responseComplete();
    }
 
    private String cvxJson(String query) {
       if (query == null || query.length() < 3) {
          return "";
       }
       Map params = new HashMap();
       params.put("query", "%" + query);
       List cvxList = crudService.search("SELECT c FROM Cvx c WHERE c.description LIKE :query", params, 0, "_MEDBASE");
       String json = "{\n query: '" + query + "',\n suggestions:[";
       String data = "data:[";
       for (Object cvxObj : cvxList) {
          Cvx cvx = (Cvx) cvxObj;
          json += "'" + cvx.getDescription() + "',";
          data += "'" + cvx.getCode() + "',";
       }
       json = json.replaceFirst(",$", "");
       data = data.replaceFirst(",$", "");
       json += "],\n";
       json += data + "]\n}";
       return json;
    }
 
    public void autocompleteAllergyJson() throws IOException {
       FacesContext facesContext = FacesContext.getCurrentInstance();
       ExternalContext externalContext = facesContext.getExternalContext();
       String query = externalContext.getRequestParameterMap().get("query");
       externalContext.setResponseContentType("application/json");
       externalContext.setResponseCharacterEncoding("UTF-8");
       externalContext.getResponseOutputWriter().write(allergyJson(query));
       facesContext.responseComplete();
    }
 
    private String allergyJson(String query) {
       if (query == null || query.length() < 1) {
          return "";
       }
       Map params = new HashMap();
       params.put("query", query);
       List cvxList = crudService.search("SELECT a FROM Allergy a WHERE a.description LIKE :query", params, 0, "_MEDBASE");
       String json = "{\n query: '" + query + "',\n suggestions:[";
       String data = "data:[";
       for (Object allergyObj : cvxList) {
          Allergy allergy = (Allergy) allergyObj;
          json += "'" + allergy.getDescription() + "',";
          data += "'" + allergy.getId() + "',";
       }
       json = json.replaceFirst(",$", "");
       data = data.replaceFirst(",$", "");
       json += "],\n";
       json += data + "]\n}";
       return json;
    }
 
    public void savePatientStatus(PatientStatus patientStatus, int userId) {
       patientStatus.setLastUpdated(new Date());
       patientStatus.setLastUpdatedUserId(userId);
       if (patientStatus.isPersisted()) {
          crudService.update(patientStatus);
       } else {
          crudService.create(patientStatus);
       }
       savePatientStatusHistoricData(patientStatus);
    }
 
    public void savePatientStatusHistoricData(PatientStatus patientStatus) {
       String[] fields = {"smoking", "nkda", "takesNoMedication", "exemptFromReporting"};
       for (String field : fields) {
          String newVal = patientStatus.getField(field);
          String oldVal = patientStatus.getPatient().getLastHistoricData("patient_status", field) == null ? null : patientStatus.getPatient().getLastHistoricData("patient_status", field).getValue();
          if (newVal == null && oldVal == null) {
             continue;
          } else if (newVal != null && oldVal != null && newVal.equals(oldVal)) {
             continue;
          }
          PatientHistoricData newHistoricData = new PatientHistoricData();
          newHistoricData.setPatientId(patientStatus.getPatientId());
          newHistoricData.setUserId(patientStatus.getLastUpdatedUserId());
          newHistoricData.setDate(patientStatus.getLastUpdated());
          newHistoricData.setTable("patient_status");
          newHistoricData.setField(field);
          newHistoricData.setValue(newVal);
          crudService.create(newHistoricData);
       }
    }
 
    public void changePatientMedications(Map form, int userId) {
       Visit visit = (Visit) ((Map) form.get("visit")).get("_");
       Date currentDate = new Date();
       Map medicationAction = (Map) form.get("medicationAction");
       Set medicationSet = medicationAction.keySet();
       for (Object medicationObj : medicationSet) {
          String status = (String) ((Map) medicationAction.get(medicationObj)).get("_");
          if (status.equals("NONE")) {
             continue;
          }
          PatientMedication medication = (PatientMedication) medicationObj;
          int prevId = medication.getId();
          if (status.equals("CONTINUED") || status.equals("DISCONTINUED")) {
             medication = crudService.find(prevId, PatientMedication.class);
          }
          medication.setId(null);
          medication.setPrevId(prevId);
          medication.setStatus(status);
          medication.setStatusDate(currentDate);
          medication.setVisit(visit);
          medication.setCreated(currentDate);
          medication.setUserId(userId);
          medication.setActive(status.equals("CONTINUED") ? true : false);
          medication.setLotNumber(null);
          medication.setExpirationDate(null);
          medication = crudService.create(medication);
          int nextId = medication.getId();
          medication = crudService.find(prevId, PatientMedication.class);
          medication.setActive(false);
          medication.setNextId(nextId);
          crudService.update(medication);
       }
    }
 
    public List favoriteMedicationList(Visit visit) {
       Map params = new HashMap();
       ReferringProvider prescriber = null;
       if (visit.getOfficeProvider().getAssociatedNpi() != null) {
          params.put("npi", visit.getOfficeProvider().getAssociatedNpi());
          List result = crudService.search("SELECT r FROM ReferringProvider r WHERE r.npi LIKE :npi", params, 1, "_MEDBASE");
          if (!result.isEmpty()) {
             prescriber = (ReferringProvider) result.get(0);
          }
       }
       if (prescriber == null) {
          return null;
       }
       params = new HashMap();
       params.put(":prescriber", prescriber);
       List result = crudService.search("SELECT pm FROM PatientMedication pm WHERE pm.favorite = true AND pm.prescriber = :prescriber ORDER BY pm.drug", params, 0, "_MEDBASE");
       for (Object paientMedicationObj : result) {
          PatientMedication medication = (PatientMedication) paientMedicationObj;
          medication.setPrevId(null);
          medication.setNextId(null);
          medication.setStatus("NEW");
          medication.setPrimaryDiagnosisCode(null);
          medication.setLastUpdated(null);
          medication.setLastUpdatedUserId(null);
          medication.setStatusDate(null);
       }
       return result;
    }
 
    public boolean activeFavoriteMedication(Patient patient, PatientMedication medication) {
       if (patient == null || medication == null) {
          return false;
       }
       for (PatientMedication patientMedication : patient.getMedicationList()) {
          if (patientMedication.getActive() && patientMedication.getDrugCode() != null && patientMedication.getDrugCode().equals(medication.getDrugCode())) {
             return true;
          }
       }
       return false;
    }
 
    public void favoritePatientMedications(Map form, int userId) {
       Visit visit = (Visit) ((Map) form.get("visit")).get("_");
       Date currentDate = new Date();
       Map medicationAction = (Map) form.get("medicationAction");
       Set medicationSet = medicationAction.keySet();
       for (Object medicationObj : medicationSet) {
          String status = (String) ((Map) medicationAction.get(medicationObj)).get("_");
          if (status.equals("NONE")) {
             continue;
          }
          PatientMedication medication = (PatientMedication) medicationObj;
          if (status.equals("UNFAVORITE")) {
             medication = crudService.find(medication.getId(), PatientMedication.class);
             medication.setFavorite(false);
             crudService.update(medication);
             continue;
          }
          if (status.equals("ADD")) {
             medication.setId(null);
             medication.setPrevId(null);
             medication.setStatus("NEW");
             medication.setStatusDate(currentDate);
             medication.setVisit(visit);
             medication.setStartDate(currentDate);
             medication.setCreated(currentDate);
             medication.setUserId(userId);
             medication.setPatientId(visit.getPatient().getId());
             medication.setPatient(visit.getPatient());
             medication.setActive(true);
             medication.setLotNumber(null);
             medication.setExpirationDate(null);
             medication.setFavorite(false);
             crudService.create(medication);
          }
       }
    }
 
    public void removePatientMedication(PatientMedication medication) {
       if (medication.getNextId() != null) {
          return;
       }
       if (medication.getPrevId() != null) {
          PatientMedication prevMedication = crudService.find(medication.getPrevId(), PatientMedication.class);
          if (prevMedication != null) {
             prevMedication.setNextId(null);
             prevMedication.setActive(true);
             crudService.update(prevMedication);
          }
       }
       crudService.delete(medication);
    }
 
    public Visit prevVisit(Visit visit) {
       Map params = new HashMap();
       params.put("patient", visit.getPatient());
       params.put("specialtyId", visit.getSpecialtyId());
       List result;
       if (visit.getId() != null) {
          params.put("id", visit.getId());
          result = crudService.search("SELECT v FROM Visit v WHERE v.patient = :patient AND v.specialtyId = :specialtyId AND v.id < :id ORDER BY v.date DESC", params, 1, "_MEDBASE");
       } else {
          result = crudService.search("SELECT v FROM Visit v WHERE v.patient = :patient AND v.specialtyId = :specialtyId ORDER BY v.date DESC", params, 1, "_MEDBASE");
       }
       if (!result.isEmpty()) {
          return (Visit) result.get(0);
       }
       return null;
    }
 
    public void addDiagnosisListToVisit(Visit visit, List<Diagnosis> diagnosisList) {
       if (diagnosisList == null) {
          return;
       }
       if (visit.getDiagnosisList() == null) {
          visit.setDiagnosisList(new ArrayList());
       }
       for (Diagnosis diagnosis : diagnosisList) {
          if (!visit.getDiagnosisList().contains(diagnosis)) {
             visit.getDiagnosisList().add(diagnosis);
          }
       }
    }
 
    public void importPrevVisit(Visit visit, Visit prevVisit, Map options) {
       if (visit == null || prevVisit == null || options.isEmpty()) {
          return;
       }
       if (((Map) options.get("diagnoses")).get("_") != null && (Boolean) ((Map) options.get("diagnoses")).get("_")) {
          if (visit.getDiagnosisList() == null) {
             visit.setDiagnosisList(new ArrayList());
          }
          for (Diagnosis diagnosis : prevVisit.getDiagnosisList()) {
             if (!visit.getDiagnosisList().contains(diagnosis)) {
                visit.getDiagnosisList().add(diagnosis);
             }
          }
       }
       if (((Map) options.get("procedures")).get("_") != null && (Boolean) ((Map) options.get("procedures")).get("_")) {
          if (visit.getProcedureList() == null) {
             visit.setProcedureList(new ArrayList());
          }
          for (VisitProcedure prevVisitProcedure : prevVisit.getProcedureList()) {
             VisitProcedure visitProcedure = new VisitProcedure();
             visitProcedure.setCode(prevVisitProcedure.getCode());
             visitProcedure.setProcedure(prevVisitProcedure.getProcedure());
             visitProcedure.setVisit(visit);
             visit.getProcedureList().add(visitProcedure);
          }
       }
       if (((Map) options.get("referringProvider")).get("_") != null && (Boolean) ((Map) options.get("referringProvider")).get("_")) {
          visit.setReferringProviderNpi(prevVisit.getReferringProviderNpi());
       }
    }
 
    public void patientRecordLog(int patientId, String source, Integer sourceRecordId, String event) {
       PatientRecordLog log = new PatientRecordLog();
       log.setApplication("medoffice");
       log.setPatientId(patientId);
       log.setSource(source);
       log.setSourceRecordId(sourceRecordId);
       log.setEvent(event);
       log.setDatetime(new Date());
       AccessController accessController = (AccessController) FacesUtils.getManagedObject("access");
       log.setUserId(accessController.getUser().getId());
       crudService.create(log);
    }
 
    public void patientRecordLog(int patientId, String source, int sourceRecordId, String event, String notes) {
       PatientRecordLog log = new PatientRecordLog();
       log.setApplication("medoffice");
       log.setPatientId(patientId);
       log.setSource(source);
       log.setSourceRecordId(sourceRecordId);
       log.setEvent(event);
       log.setDatetime(new Date());
       log.setNotes(notes);
       AccessController accessController = (AccessController) FacesUtils.getManagedObject("access");
       log.setUserId(accessController.getUser().getId());
       crudService.create(log);
    }
 
    public List<VisitDoc> visitDocList(int specialtyId) {
       if (visitDocListMap == null) {
          visitDocListMap = new LinkedHashMap();
       }
       if (!visitDocListMap.containsKey(specialtyId)) {
          Map params = new HashMap();
          params.put("specialtyId", specialtyId);
          List visitDocList = crudService.search("SELECT vd FROM VisitDoc vd WHERE vd.specialty.id = :specialtyId ORDER BY vd.pos", params, 0);
          visitDocListMap.put(specialtyId, castList(visitDocList, VisitDoc.class));
       }
       return visitDocListMap.get(specialtyId);
    }
 
    public void submitPatients(Map<PatientEvent, Boolean> patientEventMap) {
       for (PatientEvent patientEvent : patientEventMap.keySet()) {
          if (patientEventMap.get(patientEvent) != null && patientEventMap.get(patientEvent)) {
             try {
                submitPatient(patientEvent.getPatient());
                patientEventMap.remove(patientEvent);
             } catch (Exception ex) {
                patientRecordLog(patientEvent.getPatient().getId(), "Patient", patientEvent.getPatient().getId(), "submit_error");
             }
          }
       }
    }
 
    public void submitPatient(Patient patient) {
       patientRecordLog(patient.getId(), "Patient", patient.getId(), "submitted");
       System.out.println("Submitted " + patient.toString());
    }
 }
