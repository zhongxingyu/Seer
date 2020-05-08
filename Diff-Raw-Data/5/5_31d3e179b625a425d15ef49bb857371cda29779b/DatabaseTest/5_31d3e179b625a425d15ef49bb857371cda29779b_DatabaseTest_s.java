 package spitapp.core;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.criterion.Restrictions;
 
 import com.lowagie.text.DocumentException;
 
 import spitapp.core.model.CareLevel;
 import spitapp.core.model.Document;
 import spitapp.core.model.Patient;
 import spitapp.core.model.ExpensesEntry;
 import spitapp.core.model.Task;
 import spitapp.core.model.Appointment;
 import spitapp.core.service.PdfService;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 
 /**
  * Unit test for simple App.
  */
 public class DatabaseTest 
 {
 
     /**
      * Rigourous Test :-)
      * @throws IOException 
      * @throws DocumentException 
      */
     @BeforeClass
     public static void initDemoData() throws DocumentException, IOException
     {
     	SessionFactory sessionFactory = new AnnotationConfiguration()
 		.configure().buildSessionFactory();
 		Session session = sessionFactory.getCurrentSession();
     	Transaction tx = session.beginTransaction();
 		
     	// Delete oldTesttermin
    /**	List<Appointment> appointmentList = session.createCriteria(Appointment.class).list();
 		for(Appointment appointment: appointmentList){
 			if(StringUtils.equals(appointment.getAppointmentDescription(), "testermin")){
 				session.delete(appointment);
 				session.delete(appointment.getPatient());
 			}
 		}
		*/
 		// Create a new Testtermin
 		Appointment termin = new Appointment();
 		termin.setAppointmentDescription("testermin");
 		termin.setFromDate(new Date());
 		termin.setToDate(new Date());
 
 		Patient patient = new Patient();
 		patient.setAge(18);
 		patient.setCareLevel(CareLevel.A1);
 		patient.setHobbies("Kong-Fu fighting");
 		patient.setFirstName("Swen");
 		patient.setLastName("Lanthemann");
 
 		Document dok = new Document();
 		PdfService pdfService = new PdfService();
 		String fileName = "test";
 		dok.setFileName(fileName);
 		dok.setFile(pdfService.createPdf(fileName));
 		List<Document> docList = new ArrayList<Document>();
 		docList.add(dok);
 
 		Task task = new Task();
 		task.setDescription("test2");
 		List<Task> tasks = new ArrayList<Task>();
 		tasks.add(task);
 
 		ExpensesEntry spesen = new ExpensesEntry();
 		spesen.setExpensesDescription("test3");
 		List<ExpensesEntry> expensesList = new ArrayList<ExpensesEntry>();
 		expensesList.add(spesen);
 
 		patient.setTasks(tasks);
 		patient.setDocuments(docList);
 		patient.setExpenses(expensesList);
 
 		termin.setPatient(patient);
 		session.saveOrUpdate(termin);
 
 		tx.commit();
    }
     
     @Test
     public void testIsDbReadable()
     {
     	
 		SessionFactory sessionFactory = new AnnotationConfiguration()
 		.configure().buildSessionFactory();
 		Session session = sessionFactory.getCurrentSession();
 
 		Transaction tx = session.beginTransaction();
 		List<Patient> patienten = session.createCriteria(Patient.class).add( Restrictions.like("firstName", "S%"))
 			    .setMaxResults(50)
 			    .list();
 		
 		assertTrue((patienten.size()>0));
 		tx.commit();
     }
 }
