 package dk.statsbiblioteket.doms.common;
 
 import java.net.URL;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import dk.statsbiblioteket.doms.central.CentralWebservice;
 import dk.statsbiblioteket.doms.central.CentralWebserviceService;
 
 /** Create the object instances for doms ingester. */
 public abstract class IngesterFactory {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private static final String DEFAULT_FEDORA_WEBSERVICE_URL 
         = "http://localhost:7880/centralWebservice-service/central/?wsdl";
     private CentralWebservice centralWebservice;
     private static final String FEDORA_USERNAME_PROPERTY 
         = "dk.statsbiblioteket.doms.fedorausername";
     private static final String FEDORA_PASSWORD_PROPERTY 
         = "dk.statsbiblioteket.doms.fedorapassword";
     private static final String FEDORA_WEBSERVICE_URL_PROPERTY 
         = "dk.statsbiblioteket.doms.fedorawebserviceurl";
     private static final String DEFAULT_FEDORA_USERNAME = "fedoraAdmin";
     private static final String DEFAULT_FEDORA_PASSWORD = "fedoraAdminPass";
     protected final Properties config;
 
     /**
      * Initialise factory with given configuration.
      *
      * @param config The configuration.
      */
     public IngesterFactory(Properties config) {
         if (config == null) {
             this.config = new Properties(System.getProperties());
         } else {
             this.config = config;
         }
     }
 
     /**
      * Get ingester singleton.
      *
      * @return A doms ingester.
      */
     public abstract Ingester getIngester();
 
     /**
      * Get doms webservice singleton.
      *
      * @return A client to the DOMS Central webservice.
      */
     protected synchronized CentralWebservice getWebservice(IngestContext context) {
         String username, password;
         
         if(context.getUsername() != null) {
             username = context.getUsername();
             password = context.getPassword();
         } else {
             username =  config.getProperty(FEDORA_USERNAME_PROPERTY, DEFAULT_FEDORA_USERNAME);
             password = config.getProperty(FEDORA_PASSWORD_PROPERTY, DEFAULT_FEDORA_PASSWORD);
         }
         String address = context.getWSDLlocation();
         if (address == null){
            address = config.getProperty(FEDORA_WEBSERVICE_URL_PROPERTY,
                                            DEFAULT_FEDORA_WEBSERVICE_URL);
         }
             
         try {
             if (centralWebservice == null) {
                 CentralWebservice webservice = new CentralWebserviceService(
                         new URL(address),
                                new QName("http://central.doms.statsbiblioteket.dk/", "CentralWebserviceService"))
                 .getCentralWebservicePort();
                 Map<String, Object> domsAPILogin = ((BindingProvider) webservice).getRequestContext();
                 domsAPILogin.put(BindingProvider.USERNAME_PROPERTY, username);
                 domsAPILogin.put(BindingProvider.PASSWORD_PROPERTY, password);
                 centralWebservice = webservice;
             }
             return centralWebservice;
         } catch (Exception e) {
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 }
