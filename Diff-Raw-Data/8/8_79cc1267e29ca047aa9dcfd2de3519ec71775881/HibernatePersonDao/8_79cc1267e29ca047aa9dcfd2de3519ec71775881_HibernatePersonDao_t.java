 package no.steria.swhrs;
 
 import java.util.List;
 
import no.steria.swhrs.Person;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.cfg.Environment;
 import org.hibernate.context.ThreadLocalSessionContext;
 import org.joda.time.LocalDate;
 
 public class HibernatePersonDao implements PersonDao{
 
 	private SessionFactory sessionFactory;
 	private Person loggedInUser = Person.createDummyPerson("Ola Nordmann");
 	private List<HourRegistration> registrations;
 	
 	public HibernatePersonDao(String jndi) {
 		Configuration configuration = new Configuration();
 		configuration.setProperty(Environment.DATASOURCE, jndi);
 		configuration.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, ThreadLocalSessionContext.class.getName());
 		configuration.addAnnotatedClass(Person.class);
 		sessionFactory = configuration.buildSessionFactory();		
 	}
 	
 	@Override
 	public void beginTransaction() {
 		session().beginTransaction();
 	}
 
 	@Override
 	public void endTransaction(boolean b) {
 		session().getTransaction().commit();
 	}
 
 	@Override
 	public boolean saveHours(HourRegistration reg) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
	public HourRegistration getHours(Long person_id, LocalDate date) {
 		// TODO Auto-generated method stub
		return null;
 	}
 	
 	private Session session(){
 		return sessionFactory.getCurrentSession();
 	}
 	
 }
