 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Scanner;
 
 
 public class Semester implements PrimaryKeyManager, Serializable, Comparable<Semester>{
 	private static final long serialVersionUID = 1L;
 	// Attributes
 	public static int pk = 1;
 	private int id;
 	private String name;
 	private HashMap<Integer, Course> courses;
 
 	// Constructors
 	public Semester(String name) {
 		this.id = pk;
 		autoIncrement(this.id);
 		this.name = name;
 		this.courses = new HashMap<Integer, Course>();
 	}
 	// Getters and Setter
 	public int getID() { return id; }
 	public String getName() { return name; }
 	public HashMap<Integer, Course> getCourses() { return courses; }
 	
 	public void setID(int id) { this.id = id; }
 	public void setName(String name) { this.name = name; }
 	public void setCourses(HashMap<Integer, Course> courses) { this.courses = courses; }
 	
 	// Specific methods.
 	public Course getCourse(int id) throws KeyErrorException {
 		if (courses.containsKey(id)) return courses.get(id);
 		else throw new KeyErrorException(String.format("Course ID %d does not exists in current semester", id));
 	}
 	public void addCourse(Course course) throws DuplicateKeyException {
 		if (courses.containsKey(course.getID()))
 			throw new DuplicateKeyException(String.format("Course ID %d has already been added.", course.getID()));
 		else {
 			Course copyCourse = new Course(course);
 			copyCourse.setSemester(this);
 			courses.put(copyCourse.getID(), copyCourse);
 		}
 	}
 	public void rmCourse(Course course) throws KeyErrorException {
 		if (courses.containsKey(course.getID())) {
 			courses.remove(course.getID());
 		}
 		else throw new KeyErrorException(String.format("Course ID %d has not yet been added.", course.getID()));
 		
 	}
 	public void endSemester(School school, SaveData data) throws EndSemesterException {
 		// Check if all grades have been input.
 		// At the same time calculate all GPA and CGPA
 		Grade grade = null;
 		Course newCourse = null;
 		for (Course course : getCourses().values()) {
 			for (Student student : course.getGroups().getStudents()) {
 				try {
 					grade = student.getGrade(course);
 					grade.calcGPA();
 					student.calcCGPA();
 				} catch (KeyErrorException e){
 					throw new EndSemesterException(
 							String.format(
 									"Student %d, %s have no grades in Course %d, %s", 
 									student.getID(), student.getName(),
 									course.getID(), course.getName()));
 				}
 			}
 			// Shallow copy all courses in semester for archiving.
 			// By replacing courses in school with new ones.
 			newCourse = new Course(course);
 			try {
 				school.rmCourse(course);
 				school.addCourse(newCourse);
 			} catch (Exception e) {
 				//Pass
 			}
 			
 			// Clear all registered students in school's courses.
 			newCourse.setGroups(new CourseGroup(course, course.getGroups().getCapacity()));
 		}
 		data.setCurrentSemester(0);
 	}
 	
 	@Override
 	public void autoIncrement(int id) {
 		if (id < pk) return;
 		pk = ++id;
 		
 	}
 
 	@Override
 	public void readObject(ObjectInputStream stream) throws IOException,
 			ClassNotFoundException {
 		stream.defaultReadObject();
 		autoIncrement(this.id);
 	}
 	
 	@Override
 	public int compareTo(Semester sem) {
 		// Reverse sorting
 		return sem.getID() - this.getID();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Semester)) return false;
 		Semester temp = (Semester) obj;
 		if (this.id == temp.getID()) return true;
 		if (this.getName().equals(temp.getName())) return true;
 		return false;
 	}
 	
 	public static void main(School school, SaveData data, Scanner scan) {
 		// Declarations
 		int choice = 0;
 		boolean done = false;
 		String stringInput = null;
 		Semester sem = null;
 		
 		if (data.getCurrentSemester() != null) {
 			done = true;
 			Semester.main(data.getCurrentSemester(), school, data, scan);
 		}
 		while(!done) {
 			System.out.println("Semester Manager.");
 			System.out.println("\t1. Start a new semester.");
 			System.out.println("\t0. Go back to previous menu.");
 			System.out.print("Choice: ");
 			choice = scan.nextInt(); scan.nextLine();
 			
 			switch (choice) {
 				case 0:
 					done = true;
 					break;
 				case 1:
 					System.out.print("Please input semester name: ");
 					stringInput = scan.nextLine();
 					try {
 						sem = new Semester(stringInput);
 						data.addSemester(sem);
 						data.setCurrentSemester(sem.getID());
 						done = true;
 						Semester.main(sem, school, data, scan);
 					} catch (DuplicateKeyException e) {
 						System.out.println(e.getMessage());
 					}
 					break;
 				default:
 					System.out.print("Error: Invalid input.");
 					break;
 			}
 		}
 	}
 	
 	public static void main(Semester sem, School school, SaveData data, Scanner scan) {
 		int choice = 0;
 		int studentID = 0;
 		int courseID = 0;
 		boolean done = false;
 		
 		while(!done) {
 			System.out.println("Semester Registration Manager.");
 			System.out.println("\t1. Add a course to current semester.");
 			System.out.println("\t2. Add all courses to current semester.");
 			System.out.println("\t3. Remove a course from current semester.");
 			System.out.println("\t4. Remove all courses from current semester.");
 			System.out.println("\t5. Register a student to a course.");
 			System.out.println("\t6. Edit student's group in a course.");
 			System.out.println("\t7. Manage grades for a student of a course.");
 			System.out.println("\t8. Unregister a student from a course.");
 			System.out.println("\t9. Unregister all students from a course.");
 			System.out.println("\t10. End current semester.");
 			System.out.println("\t0. Go back to previous menu.");
 			System.out.print("Choice: ");
 			choice = scan.nextInt(); scan.nextLine();
 			
 			switch (choice) {
 				case 0:
 					done = true;
 					break;
 				case 1:
 					System.out.print("Please input course ID: ");
 					courseID = scan.nextInt(); scan.nextLine();
 					try {
 						sem.addCourse(school.getCourse(courseID));
						school.getCourse(courseID.setSemester(sem));
 						System.out.printf("Course ID %d added successfully.\n", courseID);
 					} catch (KeyErrorException e) {
 						System.out.println(e.getMessage());
 					} catch (DuplicateKeyException f) {
 						System.out.println(f.getMessage());
 					}
 					break;
 				case 2:
 					sem.setCourses(school.getCourses());
 					break;
 				case 3:
 					System.out.print("Please input course ID: ");
 					courseID = scan.nextInt(); scan.nextLine();
 					try {
						sem.getCourse(courseID).setSemester((Semester)null);
 						sem.rmCourse(sem.getCourse(courseID));
 						System.out.printf("Course ID %d removed successfully.\n", courseID);
 					} catch (KeyErrorException e) {
 						System.out.println(e.getMessage());
 					}
 					break;
 				case 4:
					for (Course course: school.getCourses().values()) {
						course.setSemester((Semester)null);
					}
 					sem.setCourses(new HashMap<Integer, Course>());
 					break;
 				case 5:
 				case 6:
 					System.out.print("Please input student ID: ");
 					studentID = scan.nextInt(); scan.nextLine();
 					System.out.print("Please input Course ID: ");
 					courseID = scan.nextInt(); scan.nextLine();
 					HashMap<CourseType, Integer> oriGroupIDs = null;
 					CourseGroup groups = null;
 					try {
 						Student student = school.getStudent(studentID);
 						Course course = sem.getCourse(courseID);
 						groups = course.getGroups();
 						HashMap<CourseType, Integer> groupIDs = new HashMap<CourseType, Integer>();
 						if (choice == 6) {
 							oriGroupIDs = groups.getGroups(student);
 							groups.rmStudent(student);
 						}
 						System.out.println(groups.print(""));
 						if (groups.getLecture() != null) {
 							System.out.print("Please input Lecture group: ");
 							groupIDs.put(CourseType.LECTURE, scan.nextInt());
 						}
 						if (groups.getTutorials() != null) {
 							System.out.print("Please input Tutorial group: ");
 							groupIDs.put(CourseType.TUTORIAL, scan.nextInt());
 						}
 						if (groups.getLabs() != null) {
 							System.out.print("Please input Lab group: ");
 							groupIDs.put(CourseType.LAB, scan.nextInt());
 						}
 						groups.addStudent(student, groupIDs);
 						student.addCourse(course);
 						System.out.printf("Course ID %d removed successfully.\n", courseID);
 					} catch (KeyErrorException e) {
 						System.out.println(e.getMessage());
 					} catch (MaxCapacityException f) {
 						System.out.println(f.getMessage());
 					} catch (DuplicateKeyException g) {
 						System.out.println(g.getMessage());
 					} finally {
 						if (choice == 6 && oriGroupIDs != null) {
 							try {
 								groups.addStudent(school.getStudent(studentID), oriGroupIDs);
 							} catch (Exception e) {
 								// pass
 							}
 						}
 					}
 					break;
 				case 7:
 					System.out.print("Please input student ID: ");
 					studentID = scan.nextInt(); scan.nextLine();
 					System.out.print("Please input Course ID: ");
 					courseID = scan.nextInt(); scan.nextLine();
 					try {
 						Grade.main(school.getStudent(studentID),
 								   sem.getCourse(courseID),
 								   scan);
 					} catch (KeyErrorException e) {
 						System.out.println(e.getMessage());
 					}
 					break;
 				case 8:
 					System.out.print("Please input student ID: ");
 					studentID = scan.nextInt(); scan.nextLine();
 					System.out.print("Please input Course ID: ");
 					courseID = scan.nextInt(); scan.nextLine();
 					try {
 						// Remove student from course
 						sem.getCourse(courseID)
 						   .getGroups()
 						   .rmStudent(school.getStudent(studentID));
 						// Remove course from student
 						school.getStudent(studentID).rmCourse(sem.getCourse(courseID));
 					} catch (KeyErrorException e) {
 						System.out.println(e.getMessage());
 					}
 					break;
 				case 9:
 					System.out.print("Are you sure? (Y/N): ");
 					choice = scan.nextLine().toUpperCase().charAt(0);
 					if (choice == 'Y') {
 						System.out.print("Please input course ID: ");
 						choice = scan.nextInt(); scan.nextLine();
 						HashSet<Student> students = null;
 						CourseGroup courseGroup = null;
 						try {
 							courseGroup = sem.getCourse(courseID).getGroups();
 							students = courseGroup.getStudents();
 							for (Student stud : students) {
 								courseGroup.rmStudent(stud);
 							}
 						} catch (KeyErrorException e) {
 							System.out.println(e.getMessage());
 						}
 					}
 					break;
 				case 10:
 					try {
 						sem.endSemester(school, data);
 						done = true;
 					} catch (EndSemesterException e) {
 						System.out.println(e.getMessage());
 					}
 					break;
 				default:
 					System.out.println("Error: Invalid choice.");
 					break;
 			}
 		}
 	}
 }
