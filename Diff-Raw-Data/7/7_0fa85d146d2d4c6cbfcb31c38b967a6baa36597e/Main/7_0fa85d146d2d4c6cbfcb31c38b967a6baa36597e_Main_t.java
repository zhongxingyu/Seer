 package app;
 
 import java.io.Console;
 import java.util.Collection;
 import java.util.Scanner;
 
import data.contracts.IDataBaseService;
import data.contracts.repositories.RepositoryException;
 import data.db.DataBaseService;
 import data.db.IDBConnectionFactory;
 import data.db.impl.DBConnectionFactory;
 import data.db.impl.PGDataSource;
import data.orm.Group;
 import data.orm.ORMObjectException;
import data.orm.Student;
import data.orm.Subject;
 
 
 
 public class Main {
 
 	public static IDBConnectionFactory getDBConnectionFactory() {
 		PGDataSource source = new PGDataSource("localhost", "StudDB",
 				"postgres", "postgres");
 		IDBConnectionFactory factory = new DBConnectionFactory(
 				source.getDataSource());
 
 		return factory;
 	}
 
 	public static void main(String[] args) throws ORMObjectException {
 
 		try {
 			IDBConnectionFactory connectionFactory = getDBConnectionFactory();
 			IDataBaseService db = new DataBaseService(connectionFactory);
 			Scanner inp = new Scanner(System.in);
 
 			// Add new groups//
 /*
 			 * for (int i=0; i<23; i++) { //db.getStudents().remove(i);
 			 * //db.getGroups().remove(i); db.getSubjects().remove(i); }
 			 */
 			while (true) {
 				System.out.println("Enter cmd: ");
 				String cmd = inp.nextLine();
 				if (cmd.equals("add_groups") == true) {
 					System.out.print("Group Name:   ");
 					String gr = inp.nextLine();
 					Group group = new Group(gr);
 					db.getGroups().insert(group);
 					System.out.print("ok" + "\n");
 				}
 				if (cmd.equals("get_groups") == true) {
 					System.out.print("----" + "  " + "Groups" + "  " + "----"
 							+ "\n");
 					System.out
 							.print("id" + "  " + " |" + "  " + "Name " + "\n");
 					Collection<Group> groups = db.getGroups().getAll();
 					for (Group g : groups) {
 						System.out.print(g.getID() + "  " + " |" + "  "
 								+ g.getName() + "\n");
 					}
 					System.out.print("ok" + "\n");
 				}
 				if (cmd.equals("add_studs") == true) {
 					System.out.print("Student name:   ");
 					String stud_name = inp.nextLine();
 					System.out.print("Student surname:   ");
 					String stud_surname = inp.nextLine();
 					Student student = new Student(stud_name,stud_surname);
 					db.getStudents().insert(student);
 					System.out.print("ok" + "\n");
 				}
 				if (cmd.equals("get_studs") == true) {
 					System.out.print("--------" + "  " + "Students" + "  "
 							+ "--------" + "\n");
 					System.out.print("id" + "  " + " |" + "  " + "Name " + "  "
 							+ " |" + " Surname" + "\n");
 					Collection<Student> students = db.getStudents().getAll();
 					for (Student st : students) {
 						System.out.print(st.getID() + "  " + " |" + "  "
 								+ st.getName() + " |" + st.getSurname() + "\n");
 					}
 					System.out.print("ok" + "\n");
 				}
 				if (cmd.equals("add_subj") == true) {
 					System.out.print("Subject name:   ");
 					String subj = inp.nextLine();
 					Subject subject = new Subject(subj);
 					db.getSubjects().insert(subject);
 					System.out.print("ok" + "\n");
 				}
 				if (cmd.equals("get_subj") == true) {
 					System.out.print("----" + "  " + "Subjects" + "  " + "----"
 							+ "\n");
 					System.out
 							.print("id" + "  " + " |" + "  " + "Name " + "\n");
 					Collection<Subject> subjects = db.getSubjects().getAll();
 					for (Subject subj : subjects) {
 						System.out.print(subj.getID() + "  " + " |" + "  "
 								+ subj.getSubjName() + "\n");
 					}
 					System.out.print("ok" + "\n");
 				}
 				
 				if (cmd.equals("add_mark")==true)
 				{
 					System.out.print("Enter mark:   ");
 					int m = inp.nextInt();
 					//Mark mark = new Mark (m,)
 				}
 
 			}
 			/*
 			 * 
 			 * System.out.print(">cmd" + "  " + "'Add groups'"+ "\n");
 			 * System.out.print("ok" + "\n"); System.out.print(">cmd" + "  " +
 			 * "'Get group'" + "\n"); System.out.print("----" + "  " + "Groups"+
 			 * "  " + "----" + "\n"); System.out.print("id"+ "  "+ " |" + "  " +
 			 * "Name " + "\n"); db.getGroups().insert(group1);
 			 * db.getGroups().insert(group2); db.getGroups().insert(group3); //
 			 * Group gr = db.getGroups().getByName("IF-57A"); //
 			 * gr.setName("If59a"); Collection<Group> groups =
 			 * db.getGroups().getAll(); for (Group g : groups) {
 			 * //db.getGroups().remove(g.getID()); System.out.print(g.getID()+
 			 * "  "+ " |" + "  " + g.getName()+ "\n"); } System.out.print("ok" +
 			 * "\n");
 			 */
 
 			// Remove group//
 
 			/*
 			 * Subject subject1 = new Subject("English"); Subject subject2 = new
 			 * Subject("matan"); db.getSubjects().insert(subject1);
 			 * db.getSubjects().insert(subject2); //db.getSubjects().remove(4);
 			 * System.out.println(db.getSubjects().getByID(3));
 			 * System.out.println(db.getSubjects().getSubjectByName("matan"));
 			 * Collection<Subject> subjects = db.getSubjects().getAll(); for
 			 * (Subject sub : subjects) { System.out.println(sub.toString()); }
 			 * 
 			 * Student student1 = new Student ("Marianna", "Roshchenko");
 			 * Student student2 = new Student ("Rusya", "Grechka");
 			 * db.getStudents().insert(student1);
 			 * db.getStudents().insert(student2);
 			 * System.out.println(db.getStudents().getByID(2)); //Mark mark =
 			 * new Mark (subject1.getID(),student1.getID(),5);
 			 * //db.getStudents().AddMark(mark); // db.getStudents().remove(2);
 			 * Collection<Student> students = db.getStudents().getAll(); for
 			 * (Student st : students) { System.out.println(st.toString()); }
 			 * 
 			 * 
 			 * 
 			 * //db.getGroups().addStudent(group, student1); Group group1 =
 			 * db.getGroups().getByName("IF36d"); Student st =
 			 * db.getStudents().getByID(1); group1.addStudent(st);
 			 */
 
 		} catch (RepositoryException e) {
 
 			e.printStackTrace();
 		}
 
 		// System.out.println("Finished.");
 
 	}
 
 }
