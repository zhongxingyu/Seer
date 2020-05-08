 package pl.agh.enrollme.controller;
 
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.StreamedContent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import pl.agh.enrollme.model.*;
 import pl.agh.enrollme.repository.IConfigurationDAO;
 import pl.agh.enrollme.repository.IPersonDAO;
 import pl.agh.enrollme.repository.ITermDAO;
 import pl.agh.enrollme.service.StudentPointsService;
 
 import java.io.*;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Controller
 public class AntPreferencesFileController {
     private static final File prefFile = new File("/tmp/preferencesFile.txt");
     private static final Logger LOGGER = LoggerFactory.getLogger(AntPreferencesFileController.class.getName());
     private StreamedContent streamedPreferences;
 
     @Autowired
     private IPersonDAO personDAO;
 
     @Autowired
     private IConfigurationDAO configurationDAO;
 
     @Autowired
     private ITermDAO termDAO;
 
     @Autowired
     private StudentPointsService pointsService;
 
 
     public void generatePreferencesFile(Enroll enrollment) {
         try (BufferedWriter output = new BufferedWriter( new FileWriter(prefFile))) {
             output.write("\n" + generatePeoplePreferences(enrollment));
         } catch (IOException e) {
             LOGGER.error("Some IO Problems: ", e);
         }
 
         try {
             streamedPreferences = new DefaultStreamedContent( new FileInputStream(prefFile));
         } catch (FileNotFoundException e) {
             LOGGER.debug("there is no given file :(: " + prefFile.getName(), e);
         }
     }
 
     private String generatePeoplePreferences(Enroll enrollment) {
         EnrollConfiguration configuration = configurationDAO.getConfigurationByEnrollment(enrollment);
 
         //Simple validation (it shouldn't be here but no time for making configurationDAO/configuration view working
         //better unfortunetly:
         if (configuration.getPointsPerTerm() < 1) {
             throw new IllegalStateException("There is no proper configuration");
         }
 
         int coefficient;
         List<Person> people = personDAO.getPeopleWhoSavedPreferencesForCustomEnrollment(enrollment);
         StringBuilder preferences = new StringBuilder();
         for (Person person: people) {
             //append index to the file [291524]
            preferences.append("[ ").append(person.getIndeks()).append(" ]\n");
             List<Subject> savedSubjects = personDAO.getSavedSubjects(person);
             for (Subject subject: savedSubjects) {
                 //Start every subject line with subjectID and ":" e.g. - 13:
                 preferences.append(subject.getSubjectID()).append(":");
                List<Term> terms = termDAO.getTermsBySubject(subject);
                 for (Term term: terms) {
                     //Append for every term his ID and coefficient of preference: ID,coefficient
                     coefficient =
                             getCoefficient(configuration, pointsService.getPointsAssignedByUserToTheTerm(person, term));
                     if (coefficient != -1) {
                         //if term is signed as impossible by the user, skip it!
                         preferences.append(term.getTermPerSubjectID()).append(",").append(coefficient).
                             append(";");
                     }
                 }
                 preferences.append("\n");
             }
         }
         return preferences.toString();
     }
 
     public int getCoefficient(EnrollConfiguration configuration, Integer points) {
         if (points == 0) {
             return configuration.getPointsPerTerm() + configuration.getAdditionalPoints();
         } else if ( points > 0 ) {
             return configuration.getPointsPerTerm() - points;
         } else if ( points == -1 ) {
             return -1;
         }
         throw new IllegalStateException("Points are negative number less than -1: " + points);
     }
 
     public StreamedContent getStreamedPreferences() {
         return streamedPreferences;
     }
 }
