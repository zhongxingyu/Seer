 package pl.agh.enrollme.webflow.services;
 
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
 import org.springframework.stereotype.Service;
 import pl.agh.enrollme.webflow.model.EnrollConfiguration;
 
 import javax.persistence.PersistenceContext;
 import java.util.Map;
 
 /**
  * @author Michal Partyka
  */
 @Service("configurationDAO")
 public class ConfigurationDAO implements IConfigurationDAO {
 
     @PersistenceContext
     EntityManager em;
 
     public void addConfiguration(EnrollConfiguration configuration) {
         //TODO - em.merge, this is for a while only (to check if persist doesnt work when same PK is provided
         em.persist(configuration);
     }
 
     public Map<String, Integer> getConfigurationByID(Integer id) {
 //        TODO: @down
         throw new UnsupportedOperationException("Not supported yet");
     }
 }
