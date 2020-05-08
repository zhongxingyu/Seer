 package gov.nih.nci.security.ri.dao;
 
 import java.net.URL;
 
 import net.sf.hibernate.HibernateException;
 import net.sf.hibernate.Session;
 import net.sf.hibernate.SessionFactory;
 import net.sf.hibernate.Transaction;
 import net.sf.hibernate.cfg.Configuration;
 
 import org.apache.log4j.Logger;
 
 /**
  * Base class for DAO Persistence and Retreival
  * 
  * @author Brian Husted
  *  
  */
 public class SecurityRIDAO {
 
 	private static SessionFactory sessionFactory = null;
 
 	static final Logger log = Logger.getLogger(SecurityRIDAO.class.getName());
 
 	protected static void saveObject(Object o) throws HibernateException {
 		Session s = null;
 		try {
 			s = getSessionFactory().openSession();
 			Transaction t = s.beginTransaction();
 
 			s.save(o);
 			t.commit();
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex) {
 			}
 		}
 	}
 
 	protected static void updateObject(Object o) throws HibernateException {
 		Session s = null;
 		try {
 			s = getSessionFactory().openSession();
 			Transaction t = s.beginTransaction();
 
 			s.update(o);
 			t.commit();
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex) {
 			}
 		}
 	}
 
 	protected static void deleteObject(Object o) throws HibernateException {
 		Session s = null;
 		try {
 			s = getSessionFactory().openSession();
 			s.delete(o);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex) {
 			}
 		}
 	}
 
 	/**
 	 * Returns a value object based on the class type and primary key.
 	 * 
 	 * @param c
 	 * @param primaryKey
 	 * @return
 	 * @throws HibernateException
 	 */
 	public static Object searchObjectByPrimaryKey(Class c, Long primaryKey)
 			throws HibernateException {
 
 		Session s = null;
 
 		try {
 
 			s = getSessionFactory().openSession();
 			return s.load(c, primaryKey);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex) {
 			}
 		}
 
 	}
 
 	/**
 	 * Returns the SessionFactory.  This object returned is a
 	 * singleton.
 	 * @return Returns the sessionFactory.
 	 */
 	protected static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
 
 			synchronized (SecurityRIDAO.class) {
				if (sessionFactory == null) {
 
 					try {
 						URL url = SecurityRIDAO.class.getClassLoader()
 								.getResource("hibernate.cfg.xml");
 						log.debug("The url to the config file is: "
 								+ url.toString());
 						sessionFactory = new Configuration().configure(url)
 								.buildSessionFactory();
 					} catch (Exception ex) {
 						log.fatal("Unable to create SessionFactory", ex);
 						throw new RuntimeException(
 								"Unable to create SessionFactory", ex);
 					}
 				}
 			}
 		}
 		return sessionFactory;
 	}
 
 }
