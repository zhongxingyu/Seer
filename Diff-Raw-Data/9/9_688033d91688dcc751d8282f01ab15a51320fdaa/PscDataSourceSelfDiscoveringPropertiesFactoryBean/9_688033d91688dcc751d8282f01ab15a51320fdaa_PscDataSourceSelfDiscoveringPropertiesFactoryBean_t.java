 package edu.northwestern.bioinformatics.studycalendar.database;
 
 import gov.nih.nci.cabig.ctms.tools.DataSourceSelfDiscoveringPropertiesFactoryBean;
 
 import java.io.File;
 import java.util.List;
 
 /**
  * Note: the dynamic computation of the various authentication properties in here is
  * temporary for 1.5.  In 2.0, we will add a separate configuration system (design
  * TBD) for configuring a pluggable authentication & SSO module.
  *
  * @author Rhett Sutphin
  */
 public class PscDataSourceSelfDiscoveringPropertiesFactoryBean extends DataSourceSelfDiscoveringPropertiesFactoryBean {
     private static final String DATASOURCE_SYSTEM_PROPERTY = "psc.config.datasource";
     private static final String PATH_SYSTEM_PROPERTY = "psc.config.path";
 
     private static final String DIALECT_PROPERTY_NAME = "datasource.dialect";
 
     private static final String CSM_URL_PROPERTY_NAME = "csm.datasource.url";
     private static final String CSM_DRIVER_PROPERTY_NAME = "csm.datasource.driver";
     private static final String CSM_USERNAME_PROPERTY_NAME = "csm.datasource.username";
     private static final String CSM_PASSWORD_PROPERTY_NAME = "csm.datasource.password";
     private static final String CSM_DIALECT_PROPERTY_NAME  = "csm.datasource.dialect";
     private static final String CSM_APPLICATION_CONTEXT_PROPERTY_NAME = "csm.application.context";
 
     private static final String AUTH_MODE_PROPERTY = "authenticationMode";
     private static final String WEBSSO_SERVER_URL_PROPERTY = "webSSO.server.baseUrl";
     private static final String WEBSSO_SERVER_CERT_PROPERTY = "webSSO.server.trustCert";
     private static final String WEBSSO_CAS_URL_PROPERTY = "webSSO.cas.acegi.security.url";
 
     private static final String CSM_APPLICATION_CONTEXT_VALUE = "study_calendar";
     //grid service related configuration
     private static final String GRID_REGISTRATION_CONSUMER_URL = "grid.registrationconsumer.url";
 
     private static final String GRID_REGISTRATION_CONSUMER_VALUE = "/wsrf-psc/services/cagrid/RegistrationConsumer";
 
     private static final String GRID_STUDY_CONSUMER_URL = "grid.studyconsumer.url";
     private static final String GRID_ROLLBACK_TIMEOUT = "grid.rollback.timeout";
 
     private static final String GRID_ROLLBACK_TIMEOUT_VALUE = "1";
 
     private static final String GRID_STUDY_CONSUMER_VALUE = "/wsrf-psc/services/cagrid/StudyConsumer";
 
     private static final String[] WEBSSO_PROPERTIES = {
             WEBSSO_CAS_URL_PROPERTY, WEBSSO_SERVER_URL_PROPERTY, WEBSSO_SERVER_CERT_PROPERTY
     };
 
     @Override
     public String getDatabaseConfigurationName() {
         String fromSys = System.getProperty(DATASOURCE_SYSTEM_PROPERTY);
         if (fromSys != null) {
             return fromSys;
         } else {
             return super.getDatabaseConfigurationName();
         }
     }
 
     @Override
     protected void computeProperties() {
         super.computeProperties();
         setPropertyDefault(AUTH_MODE_PROPERTY, "local");
         for (String webssoProperty : WEBSSO_PROPERTIES) {
             setPropertyDefault(webssoProperty, "If you are using CCTS-SSO, please set " + webssoProperty);
         }
 
         setPropertyDefault(GRID_STUDY_CONSUMER_URL, GRID_STUDY_CONSUMER_VALUE);
         setPropertyDefault(GRID_REGISTRATION_CONSUMER_URL, GRID_REGISTRATION_CONSUMER_VALUE);
         setPropertyDefault(GRID_ROLLBACK_TIMEOUT, GRID_ROLLBACK_TIMEOUT_VALUE);
 
         setPropertyDefault(CSM_URL_PROPERTY_NAME, getProperties().getProperty(URL_PROPERTY_NAME));
         setPropertyDefault(CSM_DRIVER_PROPERTY_NAME, getProperties().getProperty(DRIVER_PROPERTY_NAME));
         setPropertyDefault(CSM_USERNAME_PROPERTY_NAME, getProperties().getProperty(USERNAME_PROPERTY_NAME));
         setPropertyDefault(CSM_PASSWORD_PROPERTY_NAME, getProperties().getProperty(PASSWORD_PROPERTY_NAME));
         setPropertyDefault(CSM_DIALECT_PROPERTY_NAME, getProperties().getProperty(DIALECT_PROPERTY_NAME));
         setPropertyDefault(CSM_APPLICATION_CONTEXT_PROPERTY_NAME, CSM_APPLICATION_CONTEXT_VALUE);
     }
 
     @Override
     protected List<File> searchDirectories() {
         List<File> dirs = super.searchDirectories();
        String configuredPath = System.getProperty(PATH_SYSTEM_PROPERTY);
        if (configuredPath == null) {
            log.debug("{} not set -- will not search", PATH_SYSTEM_PROPERTY);
         } else {
            dirs.add(0, new File(configuredPath));
         }
         return dirs;
     }
 
     private void setPropertyDefault(String propertyName, String defaultValue) {
         if (getProperties().getProperty(propertyName) == null) {
             if (defaultValue == null) {
                 getProperties().remove(propertyName);
             } else {
                 getProperties().setProperty(propertyName, defaultValue);
             }
         }
     }
 }
