 package com.laurinka.skga.server.repository;
 
 import com.laurinka.skga.server.model.Configuration;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
 import java.util.logging.Logger;
 
 @ApplicationScoped
 public class ConfigurationRepositoryBean implements ConfigurationRepository {
 
     @Inject
     private EntityManager em;
 
     @Inject
     Logger log;
 
     @Override
     public int getNumberOfNewSkgaNumbersToCheck() {
         Configuration result = findConfiguration(Keys.NEW_NUMBERS_OFFSET);
         return Integer.parseInt(result.getValue());
     }
 
     @PostConstruct
     public void init() {
         Configuration result = findConfiguration(Keys.NEW_NUMBERS_OFFSET);
         if (null != result) {
             return;
         }
 
         Configuration entity = new Configuration();
         entity.setName(Keys.NEW_NUMBERS_OFFSET.name());
         entity.setValue("500");
         em.persist(entity);
     }
 
     private Configuration findConfiguration(Keys key) {
         try {
            String s = "select m from Configuration m where m.key =:key";
             TypedQuery<Configuration> query = em.createQuery(s, Configuration.class);
            query.setParameter("key", key.name());
             return query.getSingleResult();
         } catch (NoResultException nre) {
             return null;
         }
     }
 }
