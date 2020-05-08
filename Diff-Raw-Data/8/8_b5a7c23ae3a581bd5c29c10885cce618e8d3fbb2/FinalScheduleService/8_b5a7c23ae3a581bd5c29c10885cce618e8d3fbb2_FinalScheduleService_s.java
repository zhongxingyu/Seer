 package pl.agh.enrollme.service;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.controller.finalschedule.FinalScheduleController;
 import pl.agh.enrollme.controller.preferencesmanagement.ProgressRingController;
 import pl.agh.enrollme.controller.preferencesmanagement.ScheduleController;
 import pl.agh.enrollme.model.*;
 import pl.agh.enrollme.repository.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Author: Piotr Turek
  */
 @Service
 public class FinalScheduleService implements IFinalScheduleService {
 
     private final static Logger LOGGER = LoggerFactory.getLogger(FinalScheduleService.class);
 
     @Autowired
     private IPersonDAO personDAO;
 
     @Autowired
     private ISubjectDAO subjectDAO;
 
     @Autowired
     private ITermDAO termDAO;
 
     @Autowired
     private IStudentPointsPerTermDAO pointsDAO;
 
     @Autowired
     private IEnrollmentDAO enrollDAO;
 
     @Autowired
     private PersonService personService;
 
     @Override
     @Transactional
     public FinalScheduleController createScheduleController(Enroll enroll) {
         final Person person = personService.getCurrentUser();
         LOGGER.debug("Person: " + person + " retrieved from security context");
 
         //Enroll configuration of the current enroll
         final EnrollConfiguration enrollConfiguration = enroll.getEnrollConfiguration();
         LOGGER.debug("EnrollConfiguration: " + enrollConfiguration + " retrieved from enroll: " + enroll);
 
         final List<Subject> subjectsByEnrollment = subjectDAO.getSubjectsByEnrollment(enroll);
         LOGGER.debug("Subjects of " + enroll + " enrollment retrieved: " + subjectsByEnrollment);
 
         final List<Subject> personSubjects = subjectDAO.getSubjectsByPerson(person);
         LOGGER.debug("Subjects of " + person + " person retrieved: " + personSubjects);
 
         //list of subjects belonging to the currentEnrollment, choosen by person
         final List<Subject> subjects = new ArrayList<>();
 
         for (Subject subject : personSubjects) {
             if (subjectsByEnrollment.contains(subject)) {
                 subjects.add(subject);
             }
         }
         LOGGER.debug("Intersection found: " + subjects);
 
         //list of terms to display
         final List<Term> terms = new ArrayList<>();
 
         //list of intermediate terms
         final List<Term> interTerms = new ArrayList<>();
 
         for (Subject s : subjects) {
             final List<Term> termsBySubject = termDAO.getTermsBySubject(s);
             if (termsBySubject.size() > 0) {
                 interTerms.addAll(termsBySubject);
             }
         }
         LOGGER.debug("Terms retrieved (" + interTerms.size() + " of them): " + interTerms);
 
         //list of currently assigned points
         final List<StudentPointsPerTerm> points = new ArrayList<>();
 
         for (Term t : interTerms) {
             final StudentPointsPerTerm byPersonAndTerm = pointsDAO.getByPersonAndTerm(person, t);
            if (byPersonAndTerm != null && byPersonAndTerm.getAssigned()) {
                points.add(byPersonAndTerm);
                 terms.add(t);
             }
         }
         LOGGER.debug("Current preferences retrieved (" + points.size() + " of them): " + points);
 
         //creating schedule controller
         final FinalScheduleController scheduleController = new FinalScheduleController(enrollConfiguration, subjects, terms);
         LOGGER.debug("ScheduleController created: " + scheduleController);
 
         return scheduleController;
     }
 
 }
