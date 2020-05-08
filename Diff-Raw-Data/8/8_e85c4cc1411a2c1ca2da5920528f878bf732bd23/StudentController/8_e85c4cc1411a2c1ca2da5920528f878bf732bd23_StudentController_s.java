 package no.niths.application.rest.interfaces;
 
 import java.util.List;
 
 import no.niths.domain.Course;
 import no.niths.domain.Student;
 
 /**
  * Controller for student
  *
  */
 public interface StudentController extends GenericRESTController<Student> {
 
 	/**
 	 * Returns a list of all students in a given course
 	 * @param course the course to get students from
 	 * @return list of students
 	 */
 	public List<Student> getStudentsWithNamedCourse(Course course);
 	
 	/**
 	 * Adds a student to a orientation group
 	 * 
 	 * @param studentId The id of the student
 	 * @param groupId The group id of the orientation group
 	 */
 	public void addStudentToOrientationGroup(long studentId,int groupId);
 	
 	/**
 	 * Return all students in an orientation group
 	 * 
 	 * @return list of all students in an orientation group
 	 */
 	public List<Student> getAllOrientationGroups();
 	/**
	 * Returns all stduents in a specific group
 	 * 
 	 * @param groupId id of the group
	 * @return list of all stduents
 	 */
 	public List<Student> getStudentsInOrientationGroup(int groupId);
 	
 	/**
	 * Removes a stduent from a group
 	 * 
 	 * @param studentId Id of the student
 	 * @param groupId id of the group
 	 */
 	public void removeStudentFromOrientationGroup(long studentId,int groupId);
 	
 	/**
 	 * Removes a student from all groups he/she is a member of
 	 * 
 	 * @param studentId id of the students to remove
 	 */
 	public void removeStudentFromAllOrientationGroups(long studentId);
 }
