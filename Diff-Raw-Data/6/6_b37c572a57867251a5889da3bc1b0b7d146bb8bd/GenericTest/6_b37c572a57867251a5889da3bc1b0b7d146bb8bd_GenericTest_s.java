 package uk.ac.cam.sup;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.cfg.DefaultComponentSafeNamingStrategy;
 import org.hibernate.service.ServiceRegistry;
 import org.hibernate.service.ServiceRegistryBuilder;
 import org.junit.After;
 import org.junit.Before;
 
 public abstract class GenericTest {
	Session session;
 
 	@Before
 	public void setUp() throws Exception {
		session = HibernateUtil.getTransaction();
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 		session.close();
 	}
 	
 	
 }
