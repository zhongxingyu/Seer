 package pl.agh.enrollme.service;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import pl.agh.enrollme.controller.termmanagement.AdminScheduleController;
 import pl.agh.enrollme.model.*;
 import pl.agh.enrollme.repository.ISubjectDAO;
 import pl.agh.enrollme.repository.ITeacherDAO;
 import pl.agh.enrollme.repository.ITermDAO;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Author: Piotr Turek
  */
 @Service
 public class TermManagementService implements ITermManagementService {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(TermManagementService.class);
 
     @Autowired
     private ISubjectDAO subjectDAO;
 
     @Autowired
     private ITermDAO termDAO;
 
     @Autowired
     private ITeacherDAO teacherDAO;
 
     @Override
     public AdminScheduleController createScheduleController(Enroll enroll) {
         //Enroll configuration of the current enroll
         final EnrollConfiguration enrollConfiguration = enroll.getEnrollConfiguration();
 
         final List<Subject> subjects = subjectDAO.getSubjectsByEnrollment(enroll);
         LOGGER.debug("Subjects of " + enroll + " enrollment retrieved: " + subjects);
 
         final List<Teacher> teachers = teacherDAO.getList();
         LOGGER.debug("Teachers retrieved: " + teachers);
 
         //list of terms to display
         final List<Term> terms = new ArrayList<>();
 
         for (Subject s : subjects) {
             final List<Term> termsBySubject = termDAO.getTermsBySubject(s);
             if (termsBySubject.size() > 0) {
                 terms.addAll(termsBySubject);
             }
         }
         LOGGER.debug("Terms retrieved (" + terms.size() + " of them): " + terms);
 
         //creating schedule controller
         final AdminScheduleController scheduleController = new AdminScheduleController(enrollConfiguration, subjects,
                 terms, teachers);
         LOGGER.debug("ScheduleController created: " + scheduleController);
 
         return scheduleController;
     }
 
 
     public void saveScheduleState(AdminScheduleController scheduleController) {
         final List<Subject> subjects = scheduleController.getSubjects();
         clearSubjectTerms(subjects);    //delete all terms belonging to subjects from current enrollment
         LOGGER.debug("Terms cleared");
 
         final List<Term> terms = scheduleController.getTerms();
         LOGGER.debug(terms.size() + " terms retrieved from schedule");
         for (Term t : terms) {
             LOGGER.debug("Term retrieved: " + t);
         }
 
         final Map<Integer, Integer> termCounters = new HashMap<>();         //map containing term counters; subjectID is the key
 
         //Initialize map counters
         for (Subject subject : subjects) {
             termCounters.put(subject.getSubjectID(), 1);
             LOGGER.debug("Subject counter init, ID=" + subject.getSubjectID());
         }
         LOGGER.debug("Counters initialized");
 
         //Setting TermPerSubjectID of terms
         for (Term term : terms) {
             final Subject termSubject = term.getSubject();
            final Integer id = termCounters.get(termSubject.getSubjectID());
            final TermPK termPK = term.getTermId();
            termPK.setTermPerSubjectID(id);
             termCounters.put(termSubject.getSubjectID(), id+1);
             LOGGER.debug("Term: " + term + " set termpersubjectid to " + id);
         }
         LOGGER.debug("IDs set");
 
         for (Term term : terms) {
             termDAO.add(term);
             LOGGER.debug("Term: " + term + " has been persisted");
         }
         LOGGER.debug("State persisted");
 
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Data saved", terms.size() + " terms were saved.");
         addMessage(message);
     }
 
     /**
      * Deletes from database all terms belonging to subjects from subjects list
      * @param subjects
      */
     private void clearSubjectTerms(List<Subject> subjects) {
         for (Subject s : subjects) {
             final List<Term> termsBySubject = termDAO.getTermsBySubject(s);
             for (Term t : termsBySubject) {
                 LOGGER.debug("Removing term: " + t);
                 termDAO.remove(t);
             }
         }
     }
 
     private void addMessage(FacesMessage message) {
         FacesContext.getCurrentInstance().addMessage(null, message);
     }
 }
