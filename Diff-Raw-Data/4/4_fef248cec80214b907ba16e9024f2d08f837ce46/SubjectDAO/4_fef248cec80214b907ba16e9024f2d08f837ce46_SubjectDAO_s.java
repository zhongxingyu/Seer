 package pl.agh.enrollme.repository;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.Subject;
 import pl.agh.enrollme.model.Teacher;
 import pl.agh.enrollme.utils.DayOfWeek;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Repository
 public class SubjectDAO extends GenericDAO<Subject> implements ISubjectDAO {
     private final static Logger LOGGER = LoggerFactory.getLogger(SubjectDAO.class.getName());
 
     @Autowired
     IEnrollmentDAO enrollmentDAO;
 
     public SubjectDAO() {
         super(Subject.class);
     }
 
     @PersistenceContext
     EntityManager em;
 
     @Override
     @Transactional
     public void fillCurrentUserSubjectList(Subject[] subjects) {
         //TODO: fill the subjects into databse under currentUser. (for subjects, user.addSubject())...
         LOGGER.debug(((UserDetails) SecurityContextHolder.getContext().getAuthentication().
                 getPrincipal()).getUsername());
     }
 
     @Override
     @Transactional
     public List<Subject> getSubjectsByEnrollment(Enroll enrollment) {
         enrollmentDAO.getByPK(enrollment.getEnrollID());
         return enrollment.getSubjects();
     }
 
     @Override
     @Transactional
     public Subject getSubject(Integer id) {
         return em.find(Subject.class, id);
     }
 
     @Override
     @Transactional
     public List<Subject> getSubjectsWithGroups(Enroll enroll) {
         Teacher teacher1 = new Teacher("dr", "Stanisław", "Sobieszko", "4.11");
         Teacher teacher2 = new Teacher("dr", "Stasio", "Mieszko", "4.11");
         Subject subject1 = new Subject(enroll, null, "Mikroprocki", 2, "#00ffff", "4.33", teacher1, DayOfWeek.MONDAY,
                 null, null);
         Subject subject2 = new Subject(enroll, null, "PSI 2", 4, "#ff0000", "4.11", teacher2, DayOfWeek.FRIDAY,
                 null, null);
         //subject1.setSubjectID(1);
         //subject2.setSubjectID(2);
         List<Subject> subjects = new ArrayList<Subject>(2);
         subjects.add(subject1);
         subjects.add(subject2);
         //em.merge(enroll);
         //em.persist(enroll);
         //em.merge(subject1);
         em.persist(subject1);
         //em.merge(subject2);
         em.persist(subject2);
         return subjects;
     }
 }
