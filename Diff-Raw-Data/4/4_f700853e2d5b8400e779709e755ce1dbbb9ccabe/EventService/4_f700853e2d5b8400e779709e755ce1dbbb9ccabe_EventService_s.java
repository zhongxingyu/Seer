 package org.sukrupa.event;
 
 import com.google.common.collect.Sets;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 import org.sukrupa.platform.DoNotRemove;
 import org.sukrupa.student.Student;
 import org.sukrupa.student.StudentRepository;
 
 import java.util.List;
 import java.util.Set;
 
 import static java.lang.String.format;
 
 @Component
 public class EventService {
 
     private EventRepository eventRepository;
     private StudentRepository studentRepository;
 
 	private static final Logger LOG = Logger.getLogger(EventService.class);
 
     @DoNotRemove
     EventService() {
     }
 
     @Autowired
     public EventService(EventRepository eventRepository, StudentRepository studentRepository) {
         this.eventRepository = eventRepository;
         this.studentRepository = studentRepository;
     }
 
     @Transactional
     public void save(Event event, String... studentIdsOfAttendees) {
         event.addAttendees(studentRepository.findByStudentIds(studentIdsOfAttendees));
         eventRepository.save(event);
     }
 
     public Event getEvent(int eventId) {
         return eventRepository.load(eventId);
     }
 
     public List<Event> list() {
         return eventRepository.list();
     }
 
 	public Set<String> validateStudentIdsOfAttendees(Set<String> studentIdsOfAttendees) {
		if (studentIdsOfAttendees.isEmpty()) {
			LOG.debug(format("empty student ids"));
			return Sets.newHashSet("No student ids specified");
		}
 		Set<Student> students = studentRepository.findByStudentIds(studentIdsOfAttendees.toArray(new String[]{}));
 		Set<String> loadedStudentsIds = Sets.newHashSet();
 
 		for (Student student : students) {
 			loadedStudentsIds.add(student.getStudentId());
 		}
 		return Sets.difference(studentIdsOfAttendees, loadedStudentsIds);
 	}
 	
 }
