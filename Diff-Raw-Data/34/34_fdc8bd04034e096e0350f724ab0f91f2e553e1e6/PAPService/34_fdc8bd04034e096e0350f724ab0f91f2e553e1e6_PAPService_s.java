 package org.glite.authz.pap.common;
 
 import java.util.Date;
 
 import javax.servlet.ServletContext;
 
 import org.glite.authz.pap.authz.AuthorizationEngine;
 import org.glite.authz.pap.common.exceptions.PAPConfigurationException;
 import org.glite.authz.pap.common.xacml.wizard.AttributeWizardTypeConfiguration;
 import org.glite.authz.pap.distribution.DistributionModule;
 import org.glite.authz.pap.distribution.PAPManager;
 import org.glite.authz.pap.monitoring.MonitoredProperties;
 import org.glite.authz.pap.repository.PAPContainer;
 import org.glite.authz.pap.repository.RepositoryManager;
 import org.opensaml.DefaultBootstrap;
 import org.opensaml.xml.ConfigurationException;
 import org.slf4j.Logger;
 
 public final class PAPService {
 
     static final Logger logger = org.slf4j.LoggerFactory.getLogger(PAPService.class);
 
     protected static void setStartupMonitoringProperties() {
 
         // TODO: find a more reliable naming scheme
 
         // Property: service startup time
         PAPConfiguration.instance().setMonitoringProperty(MonitoredProperties.SERVICE_STARTUP_TIME_PROP_NAME, new Date());
 
         PAPManager papManager = PAPManager.getInstance();
 
         // Property: number of local policies
         int numberOfLocalPolicies = papManager.getDefaultPAPContainer().getNumberOfPolicies();
         PAPConfiguration.instance().setMonitoringProperty(MonitoredProperties.NUM_OF_LOCAL_POLICIES_PROP_NAME,
                 numberOfLocalPolicies);
 
         // Property: number of remote policies
         int numOfRemotePolicies = 0;
         for (PAPContainer papContainer : PAPContainer.getContainers(papManager.getOrderedRemotePAPs())) {
             numOfRemotePolicies += papContainer.getNumberOfPolicies();
         }
         PAPConfiguration.instance().setMonitoringProperty(MonitoredProperties.NUM_OF_REMOTE_POLICIES_PROP_NAME,
                 numOfRemotePolicies);
 
         // Property: number of policies
         PAPConfiguration.instance().setMonitoringProperty(MonitoredProperties.NUM_OF_POLICIES_PROP_NAME,
                 numberOfLocalPolicies + numOfRemotePolicies);
 
         // Property: policy last modification time
         String policyLastModificationTimeString = papManager.getDefaultPAP().getPolicyLastModificationTimeString();
         PAPConfiguration.instance().setMonitoringProperty(MonitoredProperties.POLICY_LAST_MODIFICATION_TIME_PROP_NAME,
                 policyLastModificationTimeString);
     }
 
     public static void start(ServletContext context) {
 
         logger.info("Starting PAP service...");
 
         // Initialize configuaration
         PAPConfiguration conf = PAPConfiguration.initialize(context);
 
         // Start autorization service
         logger.info("Starting authorization engine...");
 
         AuthorizationEngine.initialize(conf.getPapAuthzConfigurationFileName());
 
         // Bootstrap opensaml
         try {
 
             logger.info("Bootstraping OpenSAML...");
             DefaultBootstrap.bootstrap();
 
         } catch (ConfigurationException e) {
 
             logger.error("Error configuring OpenSAML:" + e.getMessage());
             throw new PAPConfigurationException("Error configuring OpenSAML:" + e.getMessage(), e);
         }
         
         // Boostrap wizard attributes
         AttributeWizardTypeConfiguration.bootstrap();
 
         // Start repository manager
         logger.info("Starting repository manager...");
         RepositoryManager.bootstrap();
         
         PAPManager.initialize();
         
         RepositoryManager.setLocalPoliciesFromConfigurationFileIfSetIntoConfiguration();
         
         setStartupMonitoringProperties();
 
         logger.info("Starting pap distribution module...");
         DistributionModule.getInstance().startDistributionModule();
 
     }
 
     public static void stop() {
 
         logger.info("Shutting down PAP service...");
 
         logger.info("Shutting down distribution module...");
         DistributionModule.getInstance().stopDistributionModule();
 
         logger.info("Shutting down authorization module...");
        AuthorizationEngine.instance().shutdown();
 
     }
 
 }
