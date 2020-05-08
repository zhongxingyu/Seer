 package com.ctp.javaone.archiver.persistence;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.inject.Disposes;
 import javax.enterprise.inject.Produces;
import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
import org.slf4j.Logger;

 @ApplicationScoped
 public class EntityManagement {
    
    @Inject
    private Logger logger;
 
     private EntityManagerFactory entityManagerFactory;
 
     @Produces
     public EntityManager createEntityManager() {
        logger.debug("Create new EntityManager");
         return entityManagerFactory.createEntityManager();
     }
 
     public void close(@Disposes EntityManager entityManager) {
        logger.debug("Disposing EntityManager");
         entityManager.close();
     }
 
     @PostConstruct
     void init() {
        logger.debug("EntityManagerFactory starting up...");
         entityManagerFactory = Persistence.createEntityManagerFactory("archiverPU");
     }
 
     @PreDestroy
     void cleanup() {
         entityManagerFactory.close();
     }
 
 }
