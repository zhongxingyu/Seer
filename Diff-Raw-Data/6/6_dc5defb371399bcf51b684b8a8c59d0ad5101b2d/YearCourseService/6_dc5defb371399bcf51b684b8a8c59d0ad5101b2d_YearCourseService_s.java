 package service;
 
 import exceptions.CoreException;
 import helpers.YearCourseHelper;
 import models.contracts.Contract;
 import models.school.Course;
 import models.school.YearCourse;
 import models.user.User;
 import org.joda.time.LocalDate;
 
 import javax.inject.Inject;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class YearCourseService extends AbstractService<YearCourse> {
 
     public List<YearCourse> getUserCoursesForYear(User user, int year) {
 
         Query query = YearCourse.em().createQuery("select yc from YearCourse yc " +
                 "where yc.professor = :user " +
                 "and yc.year = :year");
 
         query.setParameter("user", user)
                 .setParameter("year", year);
 
         return query.getResultList();
     }
 
     public List<YearCourse> getAvailableCoursesForUser(User user) {
 
//        if (user.skills.isEmpty()) {
//            return new ArrayList<YearCourse>();
//        }
 
         Query query = YearCourse.em().createQuery("select yc from YearCourse yc " +
                 "join fetch yc.course c " +
                 "where c in (:skills) " +
                 "and (yc.professor is null OR yc.professor = :user) " +
                 "and yc.year = :year");
 
         query.setParameter("skills", user.skills)
                 .setParameter("user", user)
                 .setParameter("year", YearCourseHelper.getCurrentYear());
 
         return query.getResultList();
     }
 
     public List<YearCourse> getAllCoursesForYear(int year) {
 
         Query query = YearCourse.em().createQuery("select yc from YearCourse yc " +
                 "where yc.year = :year");
 
         query.setParameter("year", year);
 
         return query.getResultList();
     }
 
     public void candidate(YearCourse course, User user) {
         course.candidates.add(user);
         course.save();
     }
 
     public void validateCandidature(YearCourse course, User user) {
         course.candidates.remove(user);
         course.professor = user;
         course.save();
     }
 
     public void cancelCandidature(YearCourse course, User user) {
         course.candidates.remove(user);
         course.save();
     }
 
     public List<YearCourse> getNotOrderedCoursesForUser(User user) {
 
         Query query = YearCourse.em().createQuery("select yc from YearCourse yc " +
                 "where yc.id not in (select joc.id from JobOrder jo join jo.courses joc where jo.user = :user) " +
                 "and yc.professor = :user and yc.duration != null and yc.startDate != null and yc.endDate != null");
 
         query.setParameter("user", user);
 
         return query.getResultList();
     }
 
     public YearCourse getNotOrderedCourseForUser(long id, User user) {
 
         Query query = YearCourse.em().createQuery("select yc from YearCourse yc " +
                 "where yc.id not in (select c.id from JobOrder jo join jo.courses c where jo.user = :user) " +
                 "and yc.id = :id and yc.professor = :user");
 
         query.setParameter("id", id);
         query.setParameter("user", user);
 
         try {
             return (YearCourse) query.getSingleResult();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     public void update(YearCourse yearCourse) throws CoreException {
 
         YearCourse fromDb = YearCourse.findById(yearCourse.id);
 
         if (null == fromDb) {
             throw new CoreException().type(CoreException.Type.NULL);
         }
 
         if (!isEditable(fromDb)) {
             throw new CoreException().type(CoreException.Type.REJECTED);
         }
 
         fromDb.duration = yearCourse.duration;
         fromDb.startDate = yearCourse.startDate;
         fromDb.endDate = yearCourse.endDate;
         fromDb.save();
     }
 
     public boolean isEditable(YearCourse yearCourse) {
 
         if (!yearCourse.orders.isEmpty() || yearCourse.year < YearCourseHelper.getCurrentYear()) {
             return false;
         }
 
         return true;
     }
 }
