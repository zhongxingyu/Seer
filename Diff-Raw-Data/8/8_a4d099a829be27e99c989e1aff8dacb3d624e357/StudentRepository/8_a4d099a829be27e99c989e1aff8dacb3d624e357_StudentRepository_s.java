 package org.sukrupa.student;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import java.util.List;
 
 @Repository
 public class StudentRepository {
     private final SessionFactory sessionFactory;
 
     @Autowired
     public StudentRepository(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     public List<Student> findAll() {
         return sessionFactory.getCurrentSession().createCriteria(Student.class).list();
     }
 }
