 package server.persistence;
 
import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import server.entities.Day;
 import server.entities.EmailAddress;
 import server.entities.Form;
 import server.entities.Lesson;
 import server.entities.Login;
 import server.entities.Replacement;
 import server.entities.Room;
 import server.entities.Subject;
 import server.entities.Teacher;
 import server.entities.Timetable;
 import server.entities.TimetableLesson;
 import server.operations.PasswordEncryptor;
 
 public class PersistEntityTest {
 
 	private EntityManager em;
 
 	@Before
 	public void init() {
 		this.em = HibernateUtil.getEntityManager();
 	}
 
 	@After
 	public void cleanup() {
 		this.em.close();
 	}
 	
 	@Test
 	public void entityManagerTest() {
 		
 		this.em.getTransaction().begin();
 		
 		Teacher teacher1 = new Teacher();
 		teacher1.setFirstname("Hans");
 		teacher1.setName("Wurst");
 		teacher1.setShortName("WUHA");
 		em.persist(teacher1);
 		
 		final Form form1 = new Form();
 		form1.setDescription("ita");
 		form1.setTeacher(teacher1);
 		this.em.persist(form1);
 		
 		Teacher teacher2 = new Teacher();
 		teacher2.setFirstname("Gesinde");
 		teacher2.setName("Hammel");
 		teacher2.setShortName("HaGe");
 		em.persist(teacher2);
 		
 		final Form form2 = new Form();
 		form2.setTeacher(teacher2);
 		form2.setDescription("itb");
 		this.em.persist(form2);
 		
 		Room room1 = new Room();
 		room1.setDescription("raum53");
 		em.persist(room1);
 		
 		Room room2 = new Room();
 		room2.setDescription("raum53");
 		em.persist(room2);
 		
 		Lesson lesson1 = new Lesson();
 		lesson1.setTimeFrom(Time.valueOf("10:00:00"));
 		lesson1.setTimeTo(Time.valueOf("10:45:00"));
 		em.persist(lesson1);
 		
 		Lesson lesson2 = new Lesson();
 		lesson2.setTimeFrom(Time.valueOf("10:45:00"));
 		lesson2.setTimeTo(Time.valueOf("11:30:00"));
 		em.persist(lesson2);
 		
 		Subject subject1 = new Subject();
 		subject1.setDescription("Biologie");
 		subject1.setShortName("Bio");
 		em.persist(subject1);
 		
 		Subject subject2 = new Subject();
 		subject2.setDescription("System Gammelierung");
 		subject2.setShortName("SYSGAM");
 		em.persist(subject2);
 		
 		Day day1 = new Day();
 		day1.setDescription("Montag");
 		em.persist(day1);
 		
 		Day day2 = new Day();
 		day2.setDescription("Dienstag");
 		em.persist(day2);
 		
 		TimetableLesson timetableLesson1 = new TimetableLesson();
 		timetableLesson1.setForm(form1);
 		timetableLesson1.setTeacher(teacher1);
 		timetableLesson1.setRoom(room1);
 		timetableLesson1.setLesson(lesson1);
 		timetableLesson1.setDay(day1);
 		timetableLesson1.setSubject(subject1);
 		em.persist(timetableLesson1);
 		
 		TimetableLesson timetableLesson2 = new TimetableLesson();
 		timetableLesson2.setForm(form1);
 		timetableLesson2.setTeacher(teacher2);
 		timetableLesson2.setRoom(room2);
 		timetableLesson2.setLesson(lesson2);
 		timetableLesson2.setDay(day1);
 		timetableLesson2.setSubject(subject2);
 		em.persist(timetableLesson2);
 		
 		List<TimetableLesson> timetableLessons = new ArrayList<TimetableLesson>();
 		timetableLessons.add(timetableLesson1);
 		timetableLessons.add(timetableLesson2);
 				
 		Timetable timetable1 = new Timetable();
 		timetable1.setLessons(timetableLessons);
 		em.persist(timetable1);
 		
 		EmailAddress email = new EmailAddress();
 		email.setEMailAddress("hans@wurst.de");
 		em.persist(email);
 			
 		Login login1 = new Login();
 		login1.setEmail(email);
 		login1.setPassword(new PasswordEncryptor().encryptPassword("test"));		
 		login1.setUser("Hans");
 		em.persist(login1);
 		
 		Replacement replacement = new Replacement();
 		replacement.setDate(Calendar.getInstance().getTime());
 		replacement.setRoom(room1);
 		replacement.setTeacher(teacher1);
 		replacement.setForm(form1);
 		em.persist(replacement);
 		
 		this.em.getTransaction().commit();
 
 		@SuppressWarnings("unchecked")
 		final List<Form> list = this.em.createNativeQuery(
 				"select * from Klasse", Form.class).getResultList();
		assertTrue(list.size() > 2);
 		for (final Form current : list) {
 			final String firstName = current.getDescription();
 			assertTrue(firstName.equals("ita") || firstName.equals("itb"));
 		}
 	}
 }
