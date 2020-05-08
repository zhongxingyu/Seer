 package ru.tsystems.karpova.dao;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.PersistenceContext;
 
 @Stateless
 public class BasicDAO {
 
    protected static final EntityManagerFactory emf = Persistence
             .createEntityManagerFactory("myapp");
 
     protected EntityManager em;
 
     public BasicDAO() {
         em = emf.createEntityManager();
     }
 }
