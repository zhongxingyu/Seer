 package org.sukrupa.event;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.Sets;
 import org.hibernate.Criteria;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Disjunction;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.sukrupa.student.Student;
 
 import java.util.List;
 import java.util.Set;
 
 @Repository
 public class EventRepository {
 
 	private static final String STUDENT_ID = "studentId";
 	static final String ATTENDEES_SEPARATOR = ",";
 
 	private SessionFactory sessionFactory;
 
     @Autowired
     public EventRepository(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     public List<Event> getAll() {
         return sessionFactory.getCurrentSession().createCriteria(Event.class).list();
     }
 
     public void save(EventRecord eventRecord) {
 	    Set<Student> attendees = retrieveStudent(eventRecord.getAttendees());
 	    sessionFactory.getCurrentSession().save(Event.createFrom(eventRecord, attendees));
     }
 
     private Set<Student> retrieveStudent(String studentIds) {
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Student.class);
        Criterion attendee = Restrictions.in(STUDENT_ID, createStudentIds(studentIds));
        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(attendee);
        criteria.add(disjunction);
         return Sets.newHashSet(criteria.list());
     }
 
 	private Set<String> createStudentIds(String studentIds) {
 		return Sets.newLinkedHashSet(Splitter.on(ATTENDEES_SEPARATOR).omitEmptyStrings().trimResults().split(studentIds));
 	}
 
 }
