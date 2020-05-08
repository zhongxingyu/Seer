 package edu.mines.Schedule;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * Operates an interface for scheduling {@link Student}s and {@link Instructor}s
  * to various {@link Course}s
  */
 public class Scheduler {
 	ArrayList<Student> studentList;
 	ArrayList<Instructor> instructorList;
 	ArrayList<CourseMeeting> courseMeetingList;
 	CourseMeetingManager meetingManager;
 	EnrollmentManager enrollmentManager;
 	TeachingSessionManager teachingSessionManager;
 
 	public Scheduler() {
 		studentList = new ArrayList<Student>();
 		instructorList = new ArrayList<Instructor>();
 		courseMeetingList = new ArrayList<CourseMeeting>();
 
 		meetingManager = CourseMeetingManager.getInstance();
 		enrollmentManager = EnrollmentManager.getInstance();
 		teachingSessionManager = TeachingSessionManager.getInstance();
 	}
 
 	private void setupInstructors() {
 		Instructor instructor1 = new Instructor("Lance Thode", "43123456", Department.CS);
 		Instructor instructor2 = new Instructor("Kurt Melody", "42671231", Department.MATH);
 		Instructor instructor3 = new Instructor("Cody Gladding", "67345252", Department.MNG);
 		Instructor instructor4 = new Instructor("Tyrone Kilmon", "78567336", Department.CS);
 		instructorList.add(instructor1);
 		instructorList.add(instructor2);
 		instructorList.add(instructor3);
 		instructorList.add(instructor4);
 	}
 
 	private void setupStudents() {
 		ArrayList<Major> major = new ArrayList<Major>();
 		ArrayList<Course> preReqs = new ArrayList<Course>();
 		major.add(Major.CompSci);
 		Student student1 = new Student("Hugh Mann", "44886266", major, preReqs);
 		Student student2 = new Student("Justin Case", "54863295", major, preReqs);
 		Student student3 = new Student("Jim Marcus", "77775554", major, preReqs);
 		Student student4 = new Student("Rico Salvo", "87895641", major, preReqs);
 		Student student5 = new Student("Marco System", "13467985", major, preReqs);
 		studentList.add(student1);
 		studentList.add(student2);
 		studentList.add(student3);
 		studentList.add(student4);
 		studentList.add(student5);
 	}
 
 	private void setupCourseMeetings() {
 		ArrayList<Course> preReqs = new ArrayList<Course>();
 		ArrayList<String> books1 = new ArrayList<String>();
 		books1.add("Refactoring to patterns");
 		books1.add("Effective Java");
 		Course course1 = new Course("443", "Advanced Java", Department.CS, 3, books1, preReqs);
 		long jan1_2011_11AM = new Long("1293904800000");
 		Date date1 = new Date(jan1_2011_11AM);
 		CourseMeeting meeting1 = new CourseMeeting(course1, Classroom.CTLM102, date1, 50);
 		courseMeetingList.add(meeting1);
 
 		Course course2 = new Course("101", "Intro to Computer Science", Department.CS, 3, null, preReqs);
 		CourseMeeting meeting2 = new CourseMeeting(course2, Classroom.GC249, date1, 50);
 		courseMeetingList.add(meeting2);
 
 		long jan1_2011_12PM = new Long("1293908400000");
 		Date date2 = new Date(jan1_2011_12PM);
 		Course course3 = new Course("261", "Programming Concepts", Department.CS, 3, null, preReqs);
 		CourseMeeting meeting3 = new CourseMeeting(course3, Classroom.CO209, date2, 50);
 		courseMeetingList.add(meeting3);
 
 		Course course4 = new Course("200", "Physics I : Introductory Mechanics", Department.PHYS, 4.5,
 				null, preReqs);
 		CourseMeeting meeting4 = new CourseMeeting(course4, Classroom.CTLM102, date2, 50);
 		courseMeetingList.add(meeting4);
 
 	}
 
 	private void printMenu() {
 		System.out.println("This interface allows you to do the following:");
 		System.out.println("[1] Print this menu");
 		System.out.println("[2] List Students");
 		System.out.println("[3] List Instructors");
 		System.out.println("[4] List Courses");
 		System.out.println("[5] Add student to course");
 		System.out.println("[6] Assign instructor to course");
 		System.out.println("[7] Quit");
 	}
 
 	/**
 	 * Ask the user for an instructor number and a course number
 	 * 
 	 * @throws Exception
 	 */
 	private void addInstructor() throws Exception {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		System.out.println("Enter the number of an instructor:");
 		int x = 0;
 		for (Instructor instructor : instructorList) {
 			System.out.println(x + " " + instructor.toString());
 			x += 1;
 		}
 		String choice = br.readLine().trim();
 		Instructor instructor;
 		try {
 			instructor = instructorList.get(Integer.parseInt(choice));
 		} catch (IndexOutOfBoundsException e) {
 			System.out.println("Invalid instructor number");
 			printMenu();
 			return;
 		}
 
 		x = 0;
 		for (CourseMeeting meeting : courseMeetingList) {
 			System.out.println(x + " " + meeting.toString());
 			x += 1;
 		}
 		choice = br.readLine().trim();
 		CourseMeeting meeting;
 		try {
 			meeting = courseMeetingList.get(Integer.parseInt(choice));
 		} catch (IndexOutOfBoundsException e) {
 			System.out.println("Invalid course number");
 			printMenu();
 			return;
 		}
 
 		TeachingSession session = new TeachingSession(instructor, meeting);
 		teachingSessionManager.addSession(session);
 		System.out.println("Added instructor to course");
 	}
 
 	private void addStudent() throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter the number of a student:");
 		int x = 0;
 		for (Student student : studentList) {
 			System.out.println(x + " " + student.toString());
 			x += 1;
 		}
 		String choice = br.readLine().trim();
 		Student student;
 		try {
 			student = studentList.get(Integer.parseInt(choice));
 		} catch (IndexOutOfBoundsException e) {
 			System.out.println("Invalid student number");
 			printMenu();
 			return;
 		}
 
 		x = 0;
 		for (CourseMeeting meeting : courseMeetingList) {
 			System.out.println(x + " " + meeting.toString());
 			x += 1;
 		}
 		choice = br.readLine().trim();
 		CourseMeeting meeting;
 		try {
 			meeting = courseMeetingList.get(Integer.parseInt(choice));
 		} catch (IndexOutOfBoundsException e) {
 			System.out.println("Invalid course number");
 			printMenu();
 			return;
 		}
 
 		Enrollment enrollment = new Enrollment(student, meeting);
 		enrollmentManager.addEnrollment(enrollment);
 		System.out.println("Added student to course");
 
 	}
 
 	private void listCourses() {
 		;
 		for (CourseMeeting meeting : courseMeetingList) {
 			System.out.println(meeting);
 		}
 	}
 
 	private void listInstructors() {
 		// We got a unique list of instructors, so lets print them
 		for (Instructor instructor : instructorList) {
 			System.out.println(instructor);
 		}
 	}
 
 	private void listStudents() {
 		// We got a unique list of students, so lets print them
 		for (Student student : studentList) {
 			System.out.println(student);
 		}
 	}
 
 	private void handleChoice(int choice) throws Exception {
 		System.out.println(choice);
 		switch (choice) {
 		case 1:
 		default:
 			printMenu();
 			break;
 
 		case 2:
 			listStudents();
 			break;
 
 		case 3:
 			listInstructors();
 			break;
 
 		case 4:
 			listCourses();
 			break;
 
 		case 5:
 			addStudent();
 			break;
 
 		case 6:
 			addInstructor();
 			break;
 
 		case 7:
 			System.exit(0);
 			break;
 		}
 	}
 
 	public static void main(String[] args) throws NumberFormatException, Exception {
 		Scheduler scheduler = new Scheduler();
 		scheduler.setupStudents();
 		scheduler.setupInstructors();
 		scheduler.setupCourseMeetings();
 
 		System.out.println("Welcome to the simple scheduling system.");
 		scheduler.printMenu();
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		while (true) {
 			String choice = br.readLine();
 			scheduler.handleChoice(Integer.parseInt(choice.trim()));
 		}
 
 	}
 
 }
