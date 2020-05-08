 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Scanner;
 
 public class Student extends Person implements PrimaryKeyManager {
 	private static final long serialVersionUID = 1L;
 	// Attributes
 	public static int pk = 1; // For PrimaryKeyManager (unique IDs)
 	private int sid;
 	private String smail;
 	private School school;
 	private Program program;
 	private StudentType type;
 	private double CGPA;
 	private HashMap<Integer, Course> courses; // Courses taken
 	private HashMap<Integer, Group> classes; // Classes enrolled
 	private HashMap<Course, Grade> grades; // Grades for Course.
 	
 	// Constructors
 	public Student(String name, Gender gender, School school) {
 		super(name, gender);
 		this.sid = pk;
 		autoIncrement(this.sid);
 		this.smail = null;
 		this.school = school;
 		this.program = null;
 		this.type = null;
 		this.CGPA = 0.0;
 		courses = new HashMap<Integer, Course>();
 		classes = new HashMap<Integer, Group>();
 		grades = new HashMap<Course, Grade>(); //
 	}
 	public Student(int id, String name, Gender gender, School school) {
 		super(name, gender);
 		this.sid = id;
 		autoIncrement(this.sid);
 		this.smail = null;
 		this.school = school;
 		this.program = null;
 		this.type = null;
 		this.CGPA = 0.0;
 		courses = new HashMap<Integer, Course>();
 		classes = new HashMap<Integer, Group>();
 		grades = new HashMap<Course, Grade>();
 	}
 	
 	// Getters and Setters
 	@Override
 	public int getID() { return sid; }
 	public String getSmail() { return smail; }
 	public School getSchool() { return school; }
 	public Program getProgram() { return program; }
 	public String getType() { return type.getDescription(); }
 	public double getCGPA() { return CGPA; }
 	public HashMap<Integer, Course> getCourses() { return courses; }
 	public HashMap<Integer, Group> getClasses() { return classes; }
 	public HashMap<Course, Grade> getGrades() { return grades; }
 	
 	@Override
 	public void setID(int id) { this.sid = id; }
 	public void setSmail(String email) { this.smail = email; }
 	public void setSchool(School school) { this.school = school; }
 	public void setProgram(Program program) { this.program = program; }
 	public void setType(StudentType type) { this.type = type; }
 	public void setCGPA(double cgpa) { this.CGPA = cgpa; }
 	public void setCourses(HashMap<Integer, Course> courses) { this.courses = courses; }
 	public void setClasses(HashMap<Integer, Group> groups) { this.classes = groups; }
 	public void setGrades(HashMap<Course, Grade> grades) { this.grades = grades; }
 	
 	// Specific methods
 	public Course getCourse(int id) throws KeyErrorException {
 		if (courses.containsKey(id)) return courses.get(id);
 		else throw new KeyErrorException(String.format("Student %d, %s is not registered in Course ID %d",getID(),getName(),id));
 	}
 	public Group getClass(int id) throws KeyErrorException {
 		if (classes.containsKey(id)) return classes.get(id);
 		else throw new KeyErrorException(String.format("Student %d, %s is not enrolled in Group ID %d",getID(),getName(),id));
 	}
 	public Grade getGrade(Course course) throws KeyErrorException {
 		if (grades.containsKey(course)) return grades.get(course);
 		else throw new KeyErrorException(String.format("Student %d, %s have no grades in course ID %d",getID(),getName(),course.getID()));
 	}
 	public void addCourse(Course course) throws DuplicateKeyException {
 		if (courses.containsKey(course.getID())) throw new DuplicateKeyException();
 		else courses.put(course.getID(), course);
 	}
 	public void addClass(Group group) throws DuplicateKeyException {
 		if (classes.containsKey(group.getID())) throw new DuplicateKeyException();
 		else classes.put(group.getID(), group);
 	}
 	public void addGrade(Grade grade) throws DuplicateKeyException {
 		if (grades.containsKey(grade.getCourse().getID())) throw new DuplicateKeyException();
 		else grades.put(grade.getCourse(), grade);
 	}
 	public void rmCourse(Course course) throws KeyErrorException {
 		if (courses.containsKey(course.getID())) courses.remove(course.getID());
 		else throw new KeyErrorException();
 	}
 	public void rmClass(Group group) throws KeyErrorException {
 		if (classes.containsKey(group.getID())) classes.remove(group.getID());
 		else throw new KeyErrorException();
 	}
 	public void rmGrade(Grade grade) throws KeyErrorException {
 		if (grades.containsKey(grade.getCourse())) grades.remove(grade.getCourse().getID());
 		else throw new KeyErrorException();
 	}
 	public void calcCGPA() {
 		double cgp = 0.0;
 		int totalAU = 0;
 		for (Grade grade : getGrades().values()) {
 			cgp += grade.getGPA();
 			totalAU += grade.getCourse().getAU();
 		}
 		setCGPA(cgp/totalAU);
 	}
 	public HashMap<Semester, ArrayList<Course>> getCoursesBySemester() {
 		HashMap<Semester, ArrayList<Course>> courses = new HashMap<Semester, ArrayList<Course>>();
 		for (Course course : this.grades.keySet()) {
 			if (!courses.containsKey(course.getSemester()))
 				courses.put(course.getSemester(), new ArrayList<Course>());
 			courses.get(course.getSemester()).add(course);
 		}
 		for (ArrayList<Course> courseList : courses.values()) {
 			Collections.sort(courseList);
 		}
 		return courses;
 	}
 	public String printTranscript(String tabs) {
 		String msg = new String();
 		HashMap<Semester, ArrayList<Course>> courses = getCoursesBySemester();
 		ArrayList<Semester> semList = new ArrayList<Semester>(courses.keySet());
 		Collections.sort(semList);
 		msg += String.format("%sStudent: %d, %s\n",tabs, getID(), getName());
 		msg += String.format("%sCGPA: %.2f\n",tabs, this.CGPA);
 		msg += String.format("%sCourses taken: \n",tabs);
 		for (Semester sem : semList) {
 			msg += String.format("%sSemester: %s\n", tabs, sem.getName());
 			for (Course course : courses.get(sem)) {
 				msg += String.format("%s\tCourse: %d, %s\n", tabs, course.getID(), course.getName());
 				msg += this.grades.get(course).printMarks(tabs+"\t\t");
 			}
 		}
 		return msg;
 	}
 	
 	@Override
 	public void autoIncrement(int id) {
 		if (id < pk) return;
 		pk = ++id;
 	}
 	@Override
 	public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
 		stream.defaultReadObject();
 		autoIncrement(this.getID());
 	}
 	
 	public void printParticulars() {
 		System.out.println(printParticulars(""));
 	}
 	public String printParticulars(String tabs) {
 		String msg = new String();
 		msg += String.format("%sGeneral Particulars: \n", tabs);
 		msg += printPersonParticulars(tabs+"\t");
 		msg += String.format("%sStudent Particulars: \n", tabs);
 		msg += printStudentParticulars(tabs+"\t");
 		return msg;
 	}
 	public String printStudentParticulars(String tabs) {
 		String msg = new String();
 		msg += String.format("%sStudent Mail: %s\n", tabs, smail);
 		msg += String.format("%sSchool: %s\n", tabs, school.getName());
 		msg += String.format("%sProgram: %s\n", tabs, program);
 		msg += String.format("%sStudent Type: %s\n", tabs, type);
 		msg += String.format("%sCGPA: %s\n", tabs, CGPA);
 		return msg;
 	}
 	
 	public static void main(School school, Scanner scan) {
 		// Declarations
 		int choice = 0;
 		int studentID = 0;
 		boolean done = false;
 		char gender;
 		String stringInput = null;
 		Student student = null;
		ArrayList<Student> studentList = null;
 				
 		while(!done) {
 			System.out.println("Welcome to Student Manager.");
 			System.out.println("\t1. Print students list by ID.");
 			System.out.println("\t2. Print students list by Name.");
 			System.out.println("\t3. Print student transcript.");
 			System.out.println("\t4. Create a new student.");
 			System.out.println("\t5. Edit a student.");
 			System.out.println("\t6. Delete a student.");
 			System.out.println("\t0. Go back to previous menu.");
 			System.out.print("Choice: ");
 			choice = scan.nextInt(); scan.nextLine();
 			
 			switch (choice) {
 				case 0:
 					done = true;
 					break;
 				case 1:
 				case 2:
					studentList = new ArrayList<Student>(school.getStudents().values());
 					if (choice == 1) Collections.sort(studentList);
 					else Collections.sort(studentList, new SortByNameComparator());
 					System.out.println("Students List:");
 					System.out.printf("%-5s | %-10s | %-60s\n", "NO", "STUDENT ID", "NAME");
 					System.out.println(new String(new char[81]).replace('\0', '-'));
 					for (Student std : studentList) {
 						System.out.printf("%-5d | %-10d | %-60s\n", studentList.indexOf(std)+1, std.getID(), std.getName());
 					}
 					break;
 				case 3:
 					student = null;
 					System.out.print("Please input student ID: ");
 					try {
 						studentID = scan.nextInt(); scan.nextLine();
 						System.out.println(school.getStudent(studentID).printTranscript("\t"));
 					} catch (KeyErrorException f) {
 						System.out.println(f.getMessage());
 					}
 					break;
 				case 4:
 					student = null;
 					System.out.print("Please input student name: ");
 					stringInput = scan.nextLine();
 					System.out.print("Please choose gender (M/F): ");
 					gender = scan.nextLine().charAt(0);
 					switch (gender) {
 						case 'M':
 						case 'm':
 							student = new Student(stringInput, Gender.MALE, school);
 							break;
 						case 'F':
 						case 'f':
 							student = new Student(stringInput, Gender.FEMALE, school);
 							break;
 						default:
 							System.out.println("Error: Invalid selection.");
 							break;
 					}
 					if (student != null) {
 						try {
 							school.addStudent(student);
							studentList = new ArrayList<Student>(school.getStudents().values());
 							System.out.printf("Student %d, %s created successfully.\n", student.getID(), student.getName());
 							System.out.println();
 							System.out.println("Students List:");
 							System.out.printf("%-5s | %-10s | %-60s\n", "NO", "STUDENT ID", "NAME");
 							System.out.println(new String(new char[81]).replace('\0', '-'));
 							for (Student std : studentList) {
 								System.out.printf("%-5d | %-10d | %-60s\n", studentList.indexOf(std)+1, std.getID(), std.getName());
 							}
 						} catch (DuplicateKeyException e) {
 							System.out.println(e.getMessage());
 						}
 					} else System.out.println("Error: Student not created.");
 					break;
 				case 5:
 				case 6:
 					student = null;
 					System.out.print("Please input student ID: ");
 					try {
 						studentID = scan.nextInt(); scan.nextLine();
 						if (choice == 5) Student.main(school.getStudent(studentID), scan);
 						else {
 							student = school.getStudent(studentID);
 							school.rmStudent(student);
 							System.out.printf("Student %d, %s removed.\n", student.getID(), student.getName());
 						}
 					} catch (KeyErrorException e) {
 						System.out.println(e.getMessage());
 					}
 					break;
 				default:
 					System.out.println("Error: Invalid choice.");
 					break;
 			}
 		}
 	}
 	public static void main(Student student, Scanner scan) {
 		int choice = 0;
 		boolean done = false;
 		String stringInput = null;
 		ArrayList<School> schoolList = null;
 		Program[] programs = null;
 		StudentType[] studentTypes = null;
 		
 		while (!done) {
 			System.out.printf("Student: %d, %s\n", student.getID(), student.getName());
 			System.out.println(student.printParticulars("\t"));
 			System.out.println("\t1. Edit General Particulars.");
 			System.out.println("\t2. Edit Student Particulars.");
 			System.out.println("\t0. Go back to previous menu.");
 			System.out.print("Please choose an option: ");
 			choice = scan.nextInt(); scan.nextLine();
 			switch (choice) {
 				case 0:
 					done = true;
 					break;
 				case 1:
 					Person.main(student, scan);
 					break;
 				case 2:
 					while (!done) {
 						System.out.printf("Student: %d, %s\n", student.getID(), student.getName());
 						System.out.println(student.printStudentParticulars("\t"));
 						System.out.println("\t1. Edit Student eMail.");
 						System.out.println("\t2. Edit School.");
 						System.out.println("\t3. Edit Program.");
 						System.out.println("\t4. Edit Student Type.");
 						System.out.println("\t0. Go back to previous menu.");
 						System.out.print("Please choose an option: ");
 						choice = scan.nextInt(); scan.nextLine();
 						
 						switch (choice) {
 							case 0:
 								done = true;
 								break;
 							case 1:
 								System.out.print("Please input student eMail: ");
 								stringInput = scan.nextLine();
 								student.setSmail(stringInput);
 								break;
 							case 2:
 								System.out.println("Please choose the following schools: ");
 								schoolList = new ArrayList<School>(SCRAME.data.getSchools());
 								for (School sch : schoolList) {
 									System.out.printf("\t%d. %s\n", (schoolList.indexOf(sch))+1, sch.getName());
 								}
 								System.out.print("Choice: ");
 								choice = scan.nextInt(); scan.nextLine();
 								try {
 									schoolList.get(choice-1).addStudent(student);
 									student.getSchool().rmStudent(student);
 									student.setSchool(schoolList.get(choice-1));
 								} catch (IndexOutOfBoundsException e) {
 									System.out.println("Error: Invalid choice.");
 								} catch (DuplicateKeyException f) {
 									System.out.println(f.getMessage());
 								} catch (KeyErrorException g) {
 									System.out.println(g.getMessage());
 								}
 								break;
 							case 3:
 								System.out.println("Please choose the following programs: ");
 								programs = Program.values();
 								for (int i=0; i<programs.length; i++) {
 									System.out.printf("\t%d. %s\n", i+1, programs[i].getName());
 								}
 								System.out.print("Choice: ");
 								choice = scan.nextInt(); scan.nextLine();
 								try {
 									student.setProgram(programs[choice-1]);
 								} catch (IndexOutOfBoundsException e) {
 									System.out.println("Error: Invalid choice.");
 								}
 								break;
 							case 4:
 								System.out.println("Please choose the following student types: ");
 								studentTypes = StudentType.values();
 								for (int i=0; i<studentTypes.length; i++) {
 									System.out.printf("\t%d. %s\n", i+1, studentTypes[i].getDescription());
 								}
 								System.out.print("Choice: ");
 								choice = scan.nextInt(); scan.nextLine();
 								try {
 									student.setType(studentTypes[choice-1]);
 								} catch (IndexOutOfBoundsException e) {
 									System.out.println("Error: Invalid choice.");
 								}
 								break;
 							default:
 								System.out.println("Error: Invalid choice.");
 								break;
 						}
 					}
 					done = false;
 					break;
 				default:
 					System.out.println("Error: Invalid choice.");
 					break;
 			}
 		}
 	}
 }
