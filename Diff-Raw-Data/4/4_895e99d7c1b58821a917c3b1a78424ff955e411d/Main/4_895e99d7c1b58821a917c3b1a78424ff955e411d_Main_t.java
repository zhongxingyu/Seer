 package org.sbelei.hibernate;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.sbelei.hibernate.dto.UserDetails;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		UserDetails user = new UserDetails();
 		user.setUserId(1);
 		user.setUserName("John Doe");
 
 		SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
 		Session session = sessionFactory.openSession();
 		session.beginTransaction();
 		session.save(user);
 		session.getTransaction().commit();

 //		UserDetails anotherUser = new UserDetails();
 //		anotherUser.setUserId(1);
 //		System.out.println(anotherUser.getUserId());
 //		System.out.println(anotherUser.getUserName());
 
 	}
 
 }
