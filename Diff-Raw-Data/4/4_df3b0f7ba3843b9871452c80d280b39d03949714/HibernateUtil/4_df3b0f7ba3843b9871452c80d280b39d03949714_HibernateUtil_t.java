 package com.axiomalaska.crks.util;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 
 public class HibernateUtil {
     private static final Configuration cfg = buildConfiguration();
     private static final SessionFactory sessionFactory = buildSessionFactory();
     private static final ThreadLocal<Session> threadSession = new ThreadLocal<Session>();
 
     private static Configuration buildConfiguration() {
         try {
             return new Configuration().configure();
         }
 
         catch (Throwable ex) {
             System.err.println("Initial Configuration creation failed." + ex);
             throw new ExceptionInInitializerError(ex);
         }
 
     }
 
     private static SessionFactory buildSessionFactory() {
         return cfg.buildSessionFactory();
     }
 
     public static SessionFactory getSessionFactory() {
         return sessionFactory;
     }
 
     public static Session currentSession() {
         Session s = threadSession.get();
         if (s == null || !s.isOpen() ) {
             s = sessionFactory.getCurrentSession();
             threadSession.set(s);
         }
 
         //starts a new transaction if needed
         if( s.getTransaction() == null || !s.getTransaction().isActive() ){
             s.beginTransaction();
         }
 
         return s;
     }
 
     public static void closeSession() {
         Session s = threadSession.get();
        if( s != null && s.getTransaction() != null && s.getTransaction().isActive() ){
        	rollbackTransaction();
        }
        
         threadSession.set(null);
 
         if (s != null ){
             if( s.isOpen() ){
                 s.close();
             }
         }
      }
 
     public static void commitTransaction(){
         final Session s = threadSession.get();
 
         if( s != null && s.isOpen() ){
             final Transaction tx = s.getTransaction();
 
             if( tx != null && tx.isActive() && !tx.wasCommitted() && !tx.wasRolledBack() ){
                 tx.commit();
             }
         }
     }
 
     public static void rollbackTransaction(){
         final Session s = threadSession.get();
         if( s != null && s.isOpen() ){
             final Transaction tx = s.getTransaction();
             if( tx != null && tx.isActive() && !tx.wasCommitted() && !tx.wasRolledBack() ){
                 System.out.println("Rolling back transaction");
                 tx.rollback();
             }
         }
     }
 }
