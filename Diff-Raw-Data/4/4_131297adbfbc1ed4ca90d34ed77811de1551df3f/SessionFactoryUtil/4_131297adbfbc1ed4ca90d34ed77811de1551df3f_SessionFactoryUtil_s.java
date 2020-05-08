 package org.levi.engine.persistence.hibernate;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.tool.hbm2ddl.SchemaExport;
 import org.levi.engine.persistence.hibernate.process.hobj.*;
 import org.levi.engine.persistence.hibernate.user.hobj.GroupBean;
 import org.levi.engine.persistence.hibernate.user.hobj.UserBean;
 
 
 /**
  * Created by IntelliJ IDEA.
  * UserBean: eranda
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
    private static AnnotationConfiguration config = new AnnotationConfiguration();
     /**
      * Opens a session configured with the default settings.
      *
      * @return the session
      */
     public static void exportSchema(){
         AnnotationConfiguration config = new AnnotationConfiguration();
         config.addAnnotatedClass(UserBean.class);
         config.addAnnotatedClass(GroupBean.class);  //TODO need to transfer this to a default add
         config.addAnnotatedClass(DeploymentBean.class);
         config.addAnnotatedClass(ProcessInstanceBean.class);
         config.addAnnotatedClass(TaskBean.class);
         config.addAnnotatedClass(EngineDataBean.class);
         config.configure("persistance.xml");
         new SchemaExport(config).create(true, true);
     }
 
     public static Session getSession() {
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
