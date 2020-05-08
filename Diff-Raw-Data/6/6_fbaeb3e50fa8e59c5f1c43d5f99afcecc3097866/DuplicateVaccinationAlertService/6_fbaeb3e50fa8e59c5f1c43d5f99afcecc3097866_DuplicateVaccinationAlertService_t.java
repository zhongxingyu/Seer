 package org.motechproject.care.migration;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.log4j.Logger;
 import org.motechproject.care.domain.CareCaseTask;
 import org.motechproject.care.repository.AllCareCaseTasks;
 import org.motechproject.care.repository.AllChildren;
 import org.motechproject.care.repository.AllMothers;
 import org.motechproject.scheduletracking.api.domain.Enrollment;
 import org.motechproject.scheduletracking.api.repository.AllEnrollments;
 import org.motechproject.scheduletracking.api.service.impl.EnrollmentAlertService;
 
 import java.util.*;
 
 
 public class DuplicateVaccinationAlertService {
 
     Logger logger = Logger.getLogger(DuplicateVaccinationAlertService.class);
     private AllChildren allChildren;
     private AllMothers allMothers;
     private AllCareCaseTasks allCareCaseTasks;
     private AllEnrollments allEnrollments;
     private EnrollmentAlertService enrollmentAlertService;
     private HashMap<String, String> vaccinationScheduleName;
     private List<String> childVaccinations = new ArrayList();
     private List<String> motherVaccinations = new ArrayList();
 
 
     public DuplicateVaccinationAlertService(AllMothers allMothers, AllCareCaseTasks allCareCaseTasks, AllChildren allChildren, AllEnrollments allEnrollments, EnrollmentAlertService enrollmentAlertService) {
         this.allCareCaseTasks = allCareCaseTasks;
         this.allChildren = allChildren;
         this.allMothers = allMothers;
         this.allEnrollments = allEnrollments;
         this.enrollmentAlertService = enrollmentAlertService;
         populateVaccinationScheduleName();
         populateChildVaccinations();
         populateMotherVaccinations();
     }
 
     private void populateMotherVaccinations() {
         motherVaccinations.add("Anc 1");
         motherVaccinations.add("Anc 2");
         motherVaccinations.add("Anc 3");
         motherVaccinations.add("Anc 4");
         motherVaccinations.add("TT 1");
         motherVaccinations.add("TT 2");
         motherVaccinations.add("TT Booster");
 
     }
 
     private void populateChildVaccinations() {
         childVaccinations.add("Bcg");
         childVaccinations.add("DPT 1");
         childVaccinations.add("DPT 2");
         childVaccinations.add("DPT 3");
         childVaccinations.add("Hep 1");
         childVaccinations.add("Hep 2");
         childVaccinations.add("Hep 3");
         childVaccinations.add("Hep 0");
         childVaccinations.add("OPV 0");
         childVaccinations.add("OPV 1");
         childVaccinations.add("OPV 2");
         childVaccinations.add("OPV 3");
         childVaccinations.add("Vita");
         childVaccinations.add("Measles");
         childVaccinations.add("DPT Booster");
         childVaccinations.add("OPV Booster");
     }
 
     private void populateVaccinationScheduleName() {
         vaccinationScheduleName = new HashMap();
         vaccinationScheduleName.put("Hep 1", "Hepatitis Vaccination");
         vaccinationScheduleName.put("Hep 2", "Hepatitis Vaccination");
         vaccinationScheduleName.put("Hep 3", "Hepatitis Vaccination");
        vaccinationScheduleName.put("OPV 1", "OPV Vaccination");
        vaccinationScheduleName.put("OPV 2", "OPV Vaccination");
        vaccinationScheduleName.put("OPV 3", "OPV Vaccination");
         vaccinationScheduleName.put("DPT 1", "DPT Vaccination");
         vaccinationScheduleName.put("DPT 2", "DPT Vaccination");
         vaccinationScheduleName.put("DPT 3", "DPT Vaccination");
         vaccinationScheduleName.put("Anc 1", "Anc Visit");
         vaccinationScheduleName.put("Anc 2", "Anc Visit");
         vaccinationScheduleName.put("Anc 3", "Anc Visit");
         vaccinationScheduleName.put("Anc 4", "Anc4 Visit");
         vaccinationScheduleName.put("DPT Booster", "DPTBooster Vaccination");
         vaccinationScheduleName.put("Measles", "Measles Vaccination");
         vaccinationScheduleName.put("OPV Booster", "OPVBooster Vaccination");
         vaccinationScheduleName.put("Bcg", "Bcg Vaccination");
         vaccinationScheduleName.put("Vita", "Vita Vaccination");
         vaccinationScheduleName.put("TT 2", "TT Vaccination");
         vaccinationScheduleName.put("TT 1", "TT Vaccination");
         vaccinationScheduleName.put("OPV 0", "OPV0 Vaccination");
         vaccinationScheduleName.put("TT Booster", "TT Booster");
         vaccinationScheduleName.put("Hep 0", "Hepatitis0 Vaccination");
 
     }
 
 
     public void deleteCareTasksForGivenChildCase(String clientCaseID) {
         List<CareCaseTask> careCaseTasks;
 
         for (String vaccination : childVaccinations) {
             logger.info("deleting details for the child case Id for vaccination : " + vaccination);
             careCaseTasks = allCareCaseTasks.findTasksByClientCaseIdAndMilestoneName(clientCaseID, vaccination);
             careCaseTasks = sortCareCaseTasksBasedOnCurrentTime(careCaseTasks);
             boolean allTasksOpen = false;
             boolean deleteAllTasks = true;
             List<CareCaseTask> careCaseTasksForDelete = filterCareCaseTasksForDelete(careCaseTasks);
             if (careCaseTasks.size() == careCaseTasksForDelete.size()) {
                 allTasksOpen = true;
             }
             if (careCaseTasks.size() == 1 && careCaseTasksForDelete.size() == 1) {
                 deleteAllTasks = false;
             }
             deleteDuplicateCareCaseTasks(careCaseTasksForDelete, clientCaseID, vaccination, allTasksOpen, deleteAllTasks);
         }
     }
 
     public void deleteCareTasksForGivenMotherCase(String clientCaseID) {
         List<CareCaseTask> careCaseTasks;
         for (String vaccination : motherVaccinations) {
             logger.info("deleting details for the mother case Id for vaccination : " + vaccination);
             careCaseTasks = allCareCaseTasks.findTasksByClientCaseIdAndMilestoneName(clientCaseID, vaccination);
             careCaseTasks = sortCareCaseTasksBasedOnCurrentTime(careCaseTasks);
             boolean allTasksOpen = false;
             boolean deleteAllTasks = true;
             List<CareCaseTask> careCaseTasksForDelete = filterCareCaseTasksForDelete(careCaseTasks);
             if (careCaseTasks.size() == careCaseTasksForDelete.size()) {
                 allTasksOpen = true;
             }
             if (careCaseTasks.size() == 1 && careCaseTasksForDelete.size() == 1) {
                 deleteAllTasks = false;
             }
             deleteDuplicateCareCaseTasks(careCaseTasksForDelete, clientCaseID, vaccination, allTasksOpen, deleteAllTasks);
         }
     }
 
     private List<CareCaseTask> sortCareCaseTasksBasedOnCurrentTime(List<CareCaseTask> careCaseTasks) {
         Collections.sort(careCaseTasks, new Comparator<CareCaseTask>() {
             @Override
             public int compare(CareCaseTask careCaseTask1, CareCaseTask careCaseTask2) {
                 return careCaseTask1.getCurrentTime().compareTo(careCaseTask2.getCurrentTime());
             }
         });
         return careCaseTasks;
     }
 
     private List<CareCaseTask> filterCareCaseTasksForDelete(List<CareCaseTask> careCaseTasks) {
         List<CareCaseTask> duplicateTasksAllOpen = new ArrayList();
         List<CareCaseTask> careCaseTasksClosed = new ArrayList();
         for (CareCaseTask careCaseTask : careCaseTasks) {
             if (careCaseTask.getOpen() == true) {
                 duplicateTasksAllOpen.add(careCaseTask);
             } else {
                 careCaseTasksClosed.add(careCaseTask);
             }
         }
         if (careCaseTasks.size() == duplicateTasksAllOpen.size()) {
             return duplicateTasksAllOpen;
         } else {
             List<CareCaseTask> caseTasks = new ArrayList();
             Object[] tasks = CollectionUtils.subtract(careCaseTasks, careCaseTasksClosed).toArray();
 
             for (int i = 0; i < tasks.length; i++) {
                 caseTasks.add((CareCaseTask) tasks[i]);
             }
             return caseTasks;
         }
     }
 
 
     private void deleteDuplicateCareCaseTasks(List<CareCaseTask> careCaseTasks, String clientCaseID, String vaccination, boolean allTasksOpen, boolean deleteAllTasks) {
         List<CareCaseTask> careCaseTasksForDelete = new ArrayList<CareCaseTask>();
         if (careCaseTasks.size() >= 1 && deleteAllTasks) {
             int count = 0;
             count = allTasksOpen != true ? count : count + 1;
             for (int i = count; i < careCaseTasks.size(); i++) {
                 careCaseTasksForDelete.add(careCaseTasks.get(i));
             }
             logger.info("deleting duplicate care case tasks for the case Id");
             allCareCaseTasks.deleteDuplicateCareTasksIfOpen(careCaseTasksForDelete);
             unScheduleAlertTasks(clientCaseID, vaccination);
         }
     }
 
     private void unScheduleAlertTasks(String clientCaseID, String vaccination) {
         String scheduleName = vaccinationScheduleName.get(vaccination);
         Enrollment activeEnrollment = allEnrollments.getActiveEnrollment(clientCaseID, scheduleName);
         if (activeEnrollment != null) {
             logger.info("un schedule all alerts for the  Id  " + activeEnrollment.getId());
             enrollmentAlertService.unscheduleAllAlerts(activeEnrollment);
         }
     }
 }
