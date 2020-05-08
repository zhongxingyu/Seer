 package org.vcs.medmanage;
 
 import android.content.Context;
 
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import db.DatabaseHelper;
 import entities.Log;
 import entities.Medication;
 import entities.Resident;
 import entities.ResidentUtils;
 
 public class ResidentService {
 
     private DatabaseHelper databaseHelper;
     private Context context;
 
     public ResidentService(Context context) {
         this.databaseHelper = new DatabaseHelper(context);
         this.context = context;
     }
 
     public List<Resident> getResidentsForCorridor(String corridor) {
         RuntimeExceptionDao<Resident, Integer> residentDao = this.databaseHelper.getRuntimeExceptionDao(Resident.class);
 
         return ResidentUtils.getResidentsInNeighborhood(residentDao, corridor);
     }
 
     public List<Resident> getResidentsForAlphabetRange(String alphabetRange) {
         RuntimeExceptionDao<Resident, Integer> residentDao = this.databaseHelper.getRuntimeExceptionDao(Resident.class);
 
         return ResidentUtils.getResidentsInNameRange(residentDao, alphabetRange);
     }
 
     /*
      * Determines the status of a resident.
      *
      *  Within the last 24 hours,
      *      Red - if a patient had to take medications and hasn't, window has expired.
      *      Yellow - if a patient has to take medications and hasn't taken them. window hasn't expired.
      *      Green - Medication is up to date.
      */
     public List<Resident> getResidentsForStatus(String residentStatus) {
         Date now = new Date(); Date startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000);
         List<Resident> residents = getResidentsWithAppointmentsAndStatus(startTime, now, true);
 
         List<Resident> matchingResidents = new ArrayList<Resident>();
         for(Resident resident : residents) {
             if (resident.getStatus().toString().equalsIgnoreCase(residentStatus)) matchingResidents.add(resident);
         }
         return matchingResidents;
     }
 
     /*
      * Sets the status for all residents with appointments and returns the ones that have
      * an appointment. Yes, the function is named and it sets. malpractice alert!
      */
     public List<Resident> getResidentsWithAppointmentsAndStatus(Date startTime, Date endTime, boolean allResidents) {
         RuntimeExceptionDao<Resident, Integer> residentDao = this.databaseHelper.getRuntimeExceptionDao(Resident.class);
         List<Resident> residents = ResidentUtils.getAllResidents(residentDao);
 
         List<Resident> returnResidents = new ArrayList<Resident>();
         for (Resident resident : residents) {
             List<MedicationAppointment> medicationAppointments =
                     new CalendarService(context).getResidentMedications(resident, startTime, endTime);
 
             if (!allResidents && (medicationAppointments == null || medicationAppointments.size() == 0)) continue;
             returnResidents.add(resident);
 
             List<MedicationAppointment> unfulfilledAppointments = new ArrayList<MedicationAppointment>();
             for(MedicationAppointment medicationAppointment : medicationAppointments) {
                 if (!medicationAppointment.isComplete()) unfulfilledAppointments.add(medicationAppointment);
             }
 
             if (unfulfilledAppointments.isEmpty()) resident.setStatus(ResidentStatus.GREEN);
             else {
                 resident.setStatus(ResidentStatus.YELLOW);
                 for (MedicationAppointment medicationAppointment : unfulfilledAppointments) {
                     if (endTime.getTime() > medicationAppointment.getMedicationTime().getTime() + medicationAppointment.getMedicationWindowInMillis())
                         resident.setStatus(ResidentStatus.RED);
                 }
             }
         }
 
         return returnResidents;
     }
 
     public List<MedicationAppointment> getAppointmentsForAllResidents(Date startTime, Date endTime) {
         RuntimeExceptionDao<Resident, Integer> residentDao = this.databaseHelper.getRuntimeExceptionDao(Resident.class);
         List<Resident> residents = ResidentUtils.getAllResidents(residentDao);
 
         List<MedicationAppointment> medicationAppointments = new ArrayList<MedicationAppointment>();
         for(Resident resident : residents) {
             List<MedicationAppointment> residentAppointments = new CalendarService(context).getResidentMedications(resident, startTime, endTime);
             medicationAppointments.addAll(residentAppointments);
         }
 
         Collections.sort(medicationAppointments);
 
         return medicationAppointments;
     }
 
     public List<String> getResidentsToAlert() {
         RuntimeExceptionDao<Resident, Integer> residentDao = this.databaseHelper.getRuntimeExceptionDao(Resident.class);
         List<Resident> residents = ResidentUtils.getAllResidents(residentDao);
 
        Date now = new Date(); Date startTime = new Date(now.getTime() - 2 * 60 * 60 * 1000);
 
         List<String> alerts = new ArrayList<String>();
         for (Resident resident : residents) {
             List<MedicationAppointment> medicationAppointments =
                     new CalendarService(context).getResidentMedications(resident, startTime, now);
 
             for (MedicationAppointment medicationAppointment : medicationAppointments) {
                 if (!medicationAppointment.isComplete()) {
                    if (-now.getTime() +
                             (medicationAppointment.getMedicationTime().getTime() +
                              medicationAppointment.getMedicationWindowInMillis()) < (10 * 60 * 1000)) {
                         alerts.add(resident.getName() + " " + resident.getRoomNumber() + " " + medicationAppointment.getMedication().getName());
                     }
                 }
             }
         }
 
         return alerts;
     }
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
