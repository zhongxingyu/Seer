 package org.levi.engine.persistence.hibernate;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.levi.engine.persistence.hibernate.user.hobj.GroupDaoImpl;
import org.levi.engine.persistence.hibernate.user.hobj.UserDaoImpl;
 
 
 /**
  * Created by IntelliJ IDEA.
  * UserDaoImpl: eranda
  * Date: May 3, 2011
  * Time: 11:53:32 AM
  * To change this template use File | Settings | File Templates.
  */
 public class SessionFactoryUtil {
 
     /**
      * The single instance of hibernate SessionFactory
      */
     //private static SessionFactory sessionFactory;
 
     /**
      * disable constructor to guaranty a single instance
      */
     private SessionFactoryUtil() {
     }
 


    private static SessionFactory sessionFactory;
    /**
      * Opens a session configured with the default settings. 
      *
      * @return the session
      */
     public static Session getSession() {
         AnnotationConfiguration config = new AnnotationConfiguration();
         config.addAnnotatedClass(UserDaoImpl.class);
         config.addAnnotatedClass(GroupDaoImpl.class);  //TODO need to transfer this to a default add
     	config.configure("persistence.xml");
         new SchemaExport(config).create(true, true);  //TODO active and deactive this option as master reset
         sessionFactory = config.buildSessionFactory();
         return sessionFactory.openSession();
     }
 
     /**
      * Opens a session and will not bind it to a session context
      *
      * @return the session
      */
     public Session openSession() {
         return sessionFactory.openSession();
     }
 
     public Session getCurrentSession() {
         return sessionFactory.getCurrentSession();
     }
 
     /**
      * closes the session factory
      */
     public static void close() {
         if (sessionFactory != null) {
             sessionFactory.close();
         }
         sessionFactory = null;
     }
 }
