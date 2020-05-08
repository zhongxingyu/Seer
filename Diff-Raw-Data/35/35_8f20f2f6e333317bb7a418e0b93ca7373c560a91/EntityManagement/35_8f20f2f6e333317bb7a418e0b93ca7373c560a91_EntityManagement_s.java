 package com.ctp.javaone.archiver.persistence;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.inject.Disposes;
 import javax.enterprise.inject.Produces;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 @ApplicationScoped
 public class EntityManagement {
 
     private EntityManagerFactory entityManagerFactory;
 
     @Produces
     public EntityManager createEntityManager() {
         return entityManagerFactory.createEntityManager();
     }
 
     public void close(@Disposes EntityManager entityManager) {
         entityManager.close();
     }
 
     @PostConstruct
     void init() {
         entityManagerFactory = Persistence.createEntityManagerFactory("archiverPU");
     }
 
     @PreDestroy
     void cleanup() {
         entityManagerFactory.close();
     }
 
 }
