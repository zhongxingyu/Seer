 package no.niths.services;
 
 import java.util.List;
 
 import no.niths.domain.Course;
 import no.niths.domain.Topic;
 import no.niths.infrastructure.interfaces.CoursesRepository;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class CourseService {
 
     @Autowired
     private CoursesRepository repo;
 
     public void createCourse(Course course) {
         repo.create(course);
     }
 
     public Course getCourseById(long id) {
     	Course c = repo.getById(id);
     	if(c != null){
     		c.getTopics().size();
     	}
         return c;
    }
     
     public Course getCourse(String name, int grade, String term){
     	Course c = repo.getCourse(name, grade, term);
    	c.getTopics().size();
     	return c;
     }
     
     public List<Course> getAllCourses(Course c) {
     	List<Course> results = repo.getAll(c);
     	for (Course cor : results){
     		cor.getTopics().size();
     	}
         return results;
     }
 
     public void updateCourse(Course course) {
         repo.update(course);
     }
 
     public boolean deleteCourse(long id) {
         return repo.delete(id);
     }
 }
