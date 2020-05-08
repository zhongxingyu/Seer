 package org.CommunityService.Services;
 
 import java.util.List;
 
 import hibernate.HibernateUtil;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 
 public class DBConnection {
 
	public static Object query(String query, List<String> parameters)
 			throws HibernateException {
 		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
 		Session session = sessionFactory.openSession();
 		session.beginTransaction();
 		Query q = null;
 		if (parameters == null || parameters.size() == 0) {
 			q = session.createQuery(query);
 		} else {
 			for (int i = 0; i < parameters.size(); i++) {
 				q = session.createQuery(query).setParameter(i,
 						parameters.get(i));
 			}
 		}
 		if (q != null) {
 			Object v = q.list();
 			return v;
 		}
 		session.getTransaction().commit();
 		session.close();
 		return null;
 
 	}
 
 	public static Object persist(Object object) throws HibernateException {
 		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
 		Session session = sessionFactory.openSession();
 		session.beginTransaction();
 		if (object != null) {
 			session.persist(object);
 		}
 		session.getTransaction().commit();
 		session.close();
 		return null;
 
 	}
 }
