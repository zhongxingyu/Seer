 package database;
 
 import java.io.* ;
import java.util.*;
import entities.*;
 
 
 enum TypeSwitch {
 	Teacher, Student
 }
 
 public class DatabaseReader {
 	
 	//fieslds
 	private ArrayList<String> strings ;
 	private LinkedList<Human> people ;
 	private BufferedReader reader;
 	
 	public DatabaseReader(String filename)throws IOException {
 		final FileInputStream fis = new FileInputStream(filename);
 		strings = new ArrayList<String>() ;
 		people = new LinkedList<Human>();
 		reader = new BufferedReader(new InputStreamReader(fis));
 	}
 
 	@SuppressWarnings("static-access")
 	public LinkedList<Human> populate() {
 		
 		String noteType;
 		TypeSwitch typeSwitcher = null;
 		String line = System.getProperty("user.dir");
 		// System.out.println(line);
 		try {
 
 			while ((line = reader.readLine()) != null) { // Читаем до конца
 															// файла
 				while (!line.equals("-")) // Читаем до маркера разделителя
 				{
 					strings.add(line);
 					line = reader.readLine();
 				}
 
 				noteType = strings.get(0);
 				// Блок сравнения
 				if (noteType.equals("Teacher"))
 					typeSwitcher = typeSwitcher.Teacher;
 				if (noteType.equals("Student"))
 					typeSwitcher = typeSwitcher.Student;
 				switch (typeSwitcher) {
 				
 				case Teacher:
 					people.add(new Teacher(strings));
 					break;
 
 				case Student:
 					people.add(new Student(strings));
 					break;
 
 				default:
 					throw new IllegalStateException();
 				}
 
 				strings.clear(); // Чистим временный массив для построения
 									// единичного объекта.
 			}
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return people;
 
 	}
 
 }
