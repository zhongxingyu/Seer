 package service;
 
 import models.contracts.JobOrder;
 import models.school.SoeExam;
 import models.school.SoeExamState;
 import models.school.YearCourse;
 import models.user.User;
 
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class SoeExamService {
 
     public List<SoeExamState> getStatesList() {
         return Arrays.asList(SoeExamState.values());
     }
 
     public void update(SoeExam soe, List<User> users) {
 
         soe.examinators = users;
 
         if ((soe.state == SoeExamState.PLANNIFIED && soe.plannifiedDuration == 0) ||
                 (soe.state == SoeExamState.COMPLETED && soe.realDuration == 0)) {
             throw new RuntimeException();
         }
 
         soe.save();
     }
 
     public SoeExam create(SoeExam soe, YearCourse course, List<User> users) {
 
         soe.examinators = users;
         soe.course = course;
 
         if (soe.plannifiedDuration == 0) {
             soe.state = SoeExamState.WAITING;
         } else if (soe.realDuration == 0) {
             soe.state = SoeExamState.PLANNIFIED;
         } else {
             soe.state = SoeExamState.COMPLETED;
         }
 
         soe.save();
 
         return soe;
     }
 
     public List<YearCourse> getNotOrderedSoesForUser(User user) {
 
         Query query = SoeExam.em().createQuery("select s from SoeExam s, in(s.examinators) u " +
                 "where s.id not in (select jos.id from JobOrder jo join jo.soeExams jos where jo.user = :user) " +
                 "and u = :user and s.state != :state");
 
         query.setParameter("user", user);
         query.setParameter("state", SoeExamState.WAITING);
 
         return query.getResultList();
     }
 
     public SoeExam getNotOrderedSoeForUser(long id, User user) {
 
         Query query = JobOrder.em().createQuery("select se from SoeExam se, in(se.examinators) u " +
                 "where se.id not in (select jos.id from JobOrder jo join jo.soeExams jos where jo.user = :user) " +
                 "and se.id = :id and u = :user and se.state != :state");
 
         query.setParameter("id", id);
         query.setParameter("user", user);
         query.setParameter("state", SoeExamState.WAITING);
 
         try {
             return (SoeExam) query.getSingleResult();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     public List<SoeExam> getSoeExamsByExaminatorAndYear(User examinator, int year) {
 
         Query query = SoeExam.em().createQuery("select se from SoeExam se " +
                 "join se.course yc join yc.course c " +
                "where :examinator member of se.examinators and yc.year = :year");
 
         query.setParameter("examinator", examinator)
                 .setParameter("year", year);
 
         return query.getResultList();
     }
 }
