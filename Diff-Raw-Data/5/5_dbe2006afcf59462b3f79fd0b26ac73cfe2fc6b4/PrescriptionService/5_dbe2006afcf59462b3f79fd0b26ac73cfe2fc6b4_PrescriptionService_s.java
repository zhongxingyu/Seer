 package ch.bfh.bti7081.s2013.blue.service;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 
 import ch.bfh.bti7081.s2013.blue.entities.Patient;
 import ch.bfh.bti7081.s2013.blue.entities.PrescriptionItem;
 
 public class PrescriptionService {
     
     private static PrescriptionService prescriptionService = null;
     private PrescriptionService() {
     }
     
     public static PrescriptionService getInstance() {
         if (prescriptionService == null) {
             prescriptionService = new PrescriptionService();
         }
         return prescriptionService;
     }
     
     /**
      * returns a list with DailyPrescirptions of the next 7 days
      * @param patient
      * @return dailyPrescriptions
      */
     public List<DailyPrescription> getDailyPrescriptions(Patient patient) {
         EntityManager em = PatientService.getInstance().getEntityManager();
         
         TypedQuery<PrescriptionItem> query = em.createQuery("SELECT item FROM PrescriptionItem item " +
                        "WHERE item.prescription.patient=:patient", PrescriptionItem.class);
         query.setParameter("patient", patient);
         List<PrescriptionItem> items = query.getResultList();
         
         
         List<DailyPrescription> dailyPrescriptions = new ArrayList<DailyPrescription>();
         
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         Calendar endOfDay = (Calendar) calendar.clone();
         endOfDay.add(Calendar.DAY_OF_MONTH, 1);
         
         for (int i = 0; i < 7; i++) {
             DailyPrescription dailyPrescription = new DailyPrescription();
             dailyPrescription.setDate(calendar.getTime());
             calendar.add(Calendar.DAY_OF_MONTH, 1);
             endOfDay.add(Calendar.DAY_OF_MONTH, 1);
             
             for (PrescriptionItem item : items) {
                 
                if (item.getStartDate().getTime() < endOfDay.getTime().getTime() && item.getEndDate().getTime() > calendar.getTime().getTime()) {
                     if (item.getMorning() > 0) {
                         dailyPrescription.getMorningDrugs().put(item.getMedicalDrug(), item.getMorning());
                     }
                     if (item.getNoon() > 0) {
                         dailyPrescription.getNoonDrugs().put(item.getMedicalDrug(), item.getNoon());
                     }
                     if (item.getEvening() > 0) {
                         dailyPrescription.getEveningDrugs().put(item.getMedicalDrug(), item.getEvening());
                     }
                     if (item.getNight() > 0) {
                         dailyPrescription.getNightDrugs().put(item.getMedicalDrug(), item.getNight());
                     }
                }
             }
             dailyPrescriptions.add(dailyPrescription);
         }
         return dailyPrescriptions;
     }
 
     /**
      * Returns a list of all prescriptions
      * 
      * @param patient
      * @return prescriptions
      */
     public List<PrescriptionItem> getPrescriptions(Patient patient) {
         EntityManager em = PatientService.getInstance().getEntityManager();
         
         List<PrescriptionItem> prescriptions = new ArrayList<PrescriptionItem>();
 
         TypedQuery<PrescriptionItem> query = em.createQuery("SELECT item FROM PrescriptionItem item " +
                    "WHERE item.prescription.patient=:patient", PrescriptionItem.class);
         query.setParameter("patient", patient);
         prescriptions = query.getResultList();
         
         return prescriptions;    
     }
 }
