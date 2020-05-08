 package spockdataaccess.ejb;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.PostConstruct;
 import javax.ejb.Stateful;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import spockdataaccess.ejb.requestsupport.*;
 
 /**
  *
  * Responsible for managing high and low level requests to and from the database.
  * 
  * @author Loren Chorley
  */
 @Stateful
 public class RequestBean {
     private static final Logger logger = Logger.getLogger("spockdataaccess.ejb.RequestBean");
     
     @PersistenceContext
     private EntityManager em;
     
     private ConfigurationFunctions configurationFns;
     private NetworkFunctions networkFns;
     private ExperimentFunctions experimentFns;
     private EnvironmentFunctions environmentFns;
     
     public ConfigurationFunctions getConfigurationFns() {
         return configurationFns;
     }
     
     public NetworkFunctions getNetworkFns() {
         return networkFns;
     }
     
     public ExperimentFunctions getExperimentFns() {
         return experimentFns;
     }
     
     public EnvironmentFunctions getEnvironmentFns() {
         return environmentFns;
     }
     
     @PostConstruct
     public void RequestBeanConstruction() {
         configurationFns = new ConfigurationFunctions(em);
         networkFns = new NetworkFunctions(em);
         experimentFns = new ExperimentFunctions(em);
        environmentFns = new EnvironmentFunctions(em);
         
         logger.log(Level.INFO,
                        "RequestBean instantiate!!!",
                        new Object[] {  });
         
     }
     
 }
