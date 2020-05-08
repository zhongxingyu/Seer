 package persistance;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import model.Course;
 
 public class StaticCourseRepository implements CourseRepository{
 
 	List<Course> courses;
 	
 	public StaticCourseRepository(){
 		courses = initializeCourseList();
 	}
 	@Override
 	public List<Course> getAvailableCourses() {
 		List<Course> output = new ArrayList<Course>();
 
 		//Find the courses with openings and add them to the output.
 		for (Course course: courses)
 			if (course.getCurrentEnrollment() < course.getEnrollmentLimit())
 				output.add(course);
 		
 		//Return immutable list of courses.
 		return Collections.unmodifiableList(output);
 	}
 
 	@Override
 	public Course getCourse(String courseId) {
 
 		//Search the collection for course
 		for (Course course: courses)
 			if (course.getCourseId().equals(courseId))
 				return course;
 		//If no course matches found return null
 		return null;
 	}
 
 	@Override
 	public boolean saveNewCourse(Course course) {
 		
 		if (course == null)
 			return false;
 
 		if (courses.contains(course))
 			return false;//If already present return false
 		else
 			courses.add(course);//Otherwise add the course and return true
 		
 		return true;
 	}
 
 	@Override
 	public boolean updateCourse(Course updatedCourse) {
 		//Protect against nulls
 		if (updatedCourse == null)
 			return false;
 		
 		for (Course oldCourse: courses)
 			//Find course in our course list
 			if (oldCourse.getCourseId().equalsIgnoreCase(updatedCourse.getCourseId())){
 				//Loop until course in our course list has same enrollment
 				while (oldCourse.getCurrentEnrollment() != updatedCourse.getCurrentEnrollment()){
 					if (oldCourse.getCurrentEnrollment() < updatedCourse.getCurrentEnrollment())
 						oldCourse.incrementEnrollment();
 					else
 						oldCourse.decrementEnrollment();
 				}
 				//Update successful
 				return true;
 			}
 		
 		//Course not present so return false	
 		return false;
 	}
 	/**
 	 * Used to instantiate a group of courses programmatically.
 	 * @return List of courses
 	 */
 	private List<Course> initializeCourseList(){
 		//Create an ArrayList of type Courses to store list of courses
 		List<Course> courseList = new ArrayList<Course>();
 
 		Calendar juneFirst2013 = new GregorianCalendar(2013, Calendar.JUNE, 01);
 		Calendar juneFifteenth2013 = new GregorianCalendar(2013, Calendar.JUNE, 15);
 		Calendar augustThirtyFirst2013 = new GregorianCalendar(2013, Calendar.AUGUST, 31);
 		Calendar septemberFifteenth2013 = new GregorianCalendar(2013, Calendar.SEPTEMBER, 15);
 
 		//create 10 course Objects to store in an arraylist
 		Course course1 = new Course("CS460", juneFirst2013, augustThirtyFirst2013,"Java Programming I", 
 				"Java is an excellent choice for those new to programming wishing to enhance their current skillset or change their career. " +
 				"The aim of this course is to provide students with the knowledge and competencies to be able to write and design sophisticated " +
 				"professional programs using Java through exercises.",10,1);
 		Course course2 = new Course("CS461", juneFirst2013, augustThirtyFirst2013,"Java Programming II", 
 				"Expand your knowledge of Java and learn about several of the advanced features available in the Java programming environment. " +
 				"This course focuses on the development of advanced graphical user interfaces (GUIs) using Swing, multithreading and concurrency.",10,2);
 		Course course3 = new Course("MGMT497", juneFirst2013, augustThirtyFirst2013,"Concise Writing", 
 				"Strong writing skills are necessary for effective communication in today's complex business world, " +
 				"especially with the advent of new technologies. This class will take an audience centered approach for " +
 				"preparing and writing messages considering the objective of the message and the organizational context for maximum impact.",5,3);
 		Course course4 = new Course("CSCI425", juneFirst2013, augustThirtyFirst2013,"Data Modeling", 
 				"In this course you will learn how to develop data models which are used to publish the information " +
 				"that organizations rely on for their day-to-day decision making processes. Basic data modeling techniques " +
 				"will be explored including capturing data requirements, analyzing data elements, identifying entities and " +
 				"attributes, and determining relationships between entities.",10,4);
		Course course5 = new Course("MGMT481", juneFirst2013, augustThirtyFirst2013,"Fundamentals of " +
 				"Business Analysis", "Whether you are new to the field of business analysis, or a supervisor of business " +
 				"analysts, this core class provides a basic understanding of the functions and business impact of " +
 				"the business analyst role. This course focuses on business analysis functions as they relate to " +
 				"the development of enterprise-wide solutions and the business analysis project life cycle.",7,7);
 		Course course6 = new Course("MGMT481", juneFifteenth2013, septemberFifteenth2013,"Medical Product Manufacturing", 
 				"Learn about the essential manufacturing principles for medical device and pharmaceutical products, and the " +
 				"regulations governing the medical product manufacturing process. Gain valuable knowledge in understanding the " +
 				"key principles, challenges and issues involved in good manufacturing practices (GMPs) of medical products.",5,3);
 		Course course7 = new Course("EECS44", juneFifteenth2013, septemberFifteenth2013,"System Validation and Verification", 
 				"Expand your knowledge of test and evaluation, analysis, demonstration, and examination as methods of inspection for " +
 				"proving design capabilities compliance with requirements. A focus is placed on tools and techniques utilized to manage " +
 				"the complete verification process.",8,8);
 		Course course8 = new Course("MGMT410", juneFifteenth2013, septemberFifteenth2013, "Introduction to Business Process " +
 				"Optimization", "This course will provide participants with an understanding of the components of the Business Process " +
 				"Optimization framework and an awareness of the methodologies, tools and models that serve as its core elements.",9,0);
 		Course course9 = new Course("EECS495", juneFifteenth2013, septemberFifteenth2013, "C Programming for DSP", 
 				"C programming is a preferred high-level programming language for digital signal processing (DSP) applications. " +
 				"Increase your knowledge on efficient DSP programming techniques and how to write DSP code in C.",4,2);
		Course course10 = new Course("CS460", juneFifteenth2013, septemberFifteenth2013, "Human Resources and the Law", 
 				"This course covers the primary basic federal and state laws which come to bear on the practice of human resources management, " +
 				"and which govern employer-employee relations.",6,5);
 		
 		//Add each of the 10 course objects to the arraylist.
 		courseList.add(course1);
 		courseList.add(course2);
 		courseList.add(course3);
 		courseList.add(course4);
 		courseList.add(course5);
 		courseList.add(course6);
 		courseList.add(course7);
 		courseList.add(course8);
 		courseList.add(course9);
 		courseList.add(course10);
 		
 		//Sort courses.
 		Collections.sort(courseList);
 		
 		return courseList;
 	}
 }
