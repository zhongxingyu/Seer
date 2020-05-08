 package com.gh.jordner.jpa.filesystem;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 import jordner.jpa.preferences.JPADefaults;
 import jordner.jpa.preferences.PreferenceConstants;
 
 import org.eclipse.e4.core.di.annotations.Creatable;
 import org.eclipse.e4.core.di.extensions.Preference;
 import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
 import org.eclipse.gemini.ext.di.GeminiPersistenceProperty;
 import org.eclipse.persistence.config.PersistenceUnitProperties;
 
 @Creatable
 public class VerzeichnisDAO {
 
     @Inject
     @GeminiPersistenceContext(unitName = JPADefaults.JPA_UNIT, properties = {
            @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_USER, valuePref = @Preference("jpa.preferences."
                    + PreferenceConstants.P_JDBC_USERNAME)),
             @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_PASSWORD, valuePref = @Preference(PreferenceConstants.P_JDBC_PASSWORD)),
             @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_DRIVER, valuePref = @Preference(PreferenceConstants.P_JDBC_DRIVER)),
             @GeminiPersistenceProperty(name = PersistenceUnitProperties.JDBC_URL, valuePref = @Preference(PreferenceConstants.P_JDBC_URL)),
             @GeminiPersistenceProperty(name = PersistenceUnitProperties.LOGGING_LEVEL, value = "FINE")
     // @GeminiPersistenceProperty(name = PersistenceUnitProperties.WEAVING,
     // value = "false"),
     // @GeminiPersistenceProperty(name =
     // PersistenceUnitProperties.WEAVING_INTERNAL, value = "false"),
     // @GeminiPersistenceProperty(name =
     // PersistenceUnitProperties.DDL_GENERATION, value =
     // PersistenceUnitProperties.DROP_AND_CREATE),
     // @GeminiPersistenceProperty(name =
     // PersistenceUnitProperties.DDL_GENERATION_MODE, value =
     // PersistenceUnitProperties.DDL_DATABASE_GENERATION)
     })
     private EntityManager em;
 
     public void insert(Verzeichnis dataObj) throws SQLException {
         checkConnection();
         EntityTransaction trx = em.getTransaction();
         trx.begin();
         em.persist(dataObj);
         trx.commit();
     }
 
     public List<Verzeichnis> listAllVerzeichnisse() {
         // HINT:
         // http://www.adam-bien.com/roller/abien/entry/selecting_all_jpa_entities_as
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<Verzeichnis> cq = cb.createQuery(Verzeichnis.class);
         Root<Verzeichnis> rootEntry = cq.from(Verzeichnis.class);
         CriteriaQuery<Verzeichnis> all = cq.select(rootEntry);
         TypedQuery<Verzeichnis> allQuery = em.createQuery(all);
         return allQuery.getResultList();
     }
 
     public void remove(final Verzeichnis verzeichnis) throws SQLException {
         checkConnection();
         EntityTransaction trx = em.getTransaction();
         trx.begin();
         em.remove(verzeichnis);
         trx.commit();
     }
 
     @PreDestroy
     public void destroy() {
         if (em != null && em.isOpen()) {
             em.close();
         }
     }
 
     private void checkConnection() throws SQLException {
         if (em == null) {
             throw new SQLException(
                     "EntityManager is null. Not connected to database!");
         }
         System.out.println(em.getProperties().toString());
     }
 }
