 package org.bahmni.feed.openerp;
 
 
 import com.sun.syndication.io.FeedException;
 import org.apache.log4j.Logger;
 import org.bahmni.feed.openerp.event.EventWorkerFactory;
 import org.bahmni.openerp.web.client.OpenERPClient;
 import org.bahmni.webclients.openmrs.OpenMRSAuthenticationResponse;
 import org.bahmni.webclients.openmrs.OpenMRSAuthenticator;
 import org.ict4h.atomfeed.client.factory.AtomFeedProperties;
 import org.ict4h.atomfeed.client.repository.AllFailedEvents;
 import org.ict4h.atomfeed.client.repository.AllFeeds;
 import org.ict4h.atomfeed.client.repository.AllMarkers;
 import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
 import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
 import org.ict4h.atomfeed.client.service.AtomFeedClient;
 import org.ict4h.atomfeed.client.service.EventWorker;
 import org.ict4h.atomfeed.client.service.FeedClient;
 import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.HashMap;
 
 @Controller
 public class OpenERPCustomerFeedClientService {
     private static Logger logger = Logger.getLogger(OpenERPCustomerFeedClientService.class);
     public static final String JSESSION_ID_KEY = "JSESSIONID";
 
     private AtomFeedClient atomFeedClient;
     private TaskMonitor taskMonitor;
     private OpenERPAtomFeedProperties atomFeedProperties;
     private JdbcConnectionProvider jdbcConnectionProvider;
     private EventWorkerFactory workerFactory;
     private OpenERPClient openERPClient;
     private String feedName;
     private AllFeeds allFeeds;
     private AllMarkers allMarkers;
     private AllFailedEvents allFailedEvents;
     private OpenMRSAuthenticator openMRSAuthenticator;
 
     public OpenERPCustomerFeedClientService() {}
 
     @Autowired
     public OpenERPCustomerFeedClientService(OpenERPAtomFeedProperties atomFeedProperties, OpenERPClient openERPClient,
                                            String feedName, JdbcConnectionProvider threadLocalJdbcConnectionProvider,
                                             org.bahmni.feed.openerp.TaskMonitor customerFeedClientMonitor) throws FeedException {
        this(atomFeedProperties,threadLocalJdbcConnectionProvider, new EventWorkerFactory(), openERPClient, feedName,
                getAllFeeds(atomFeedProperties), new AllMarkersJdbcImpl(threadLocalJdbcConnectionProvider),
                new AllFailedEventsJdbcImpl(threadLocalJdbcConnectionProvider), customerFeedClientMonitor,
                 new OpenMRSAuthenticator(atomFeedProperties.getAuthenticationURI(), atomFeedProperties.getConnectionTimeoutInMilliseconds(), atomFeedProperties.getReplyTimeoutInMilliseconds()));
     }
 
     OpenERPCustomerFeedClientService(OpenERPAtomFeedProperties atomFeedProperties, JdbcConnectionProvider jdbcConnectionProvider,
                                      EventWorkerFactory workerFactory, OpenERPClient openERPClient, String feedName,
                                      AllFeeds allFeeds, AllMarkers allMarkers, AllFailedEvents allFailedEvents,
                                      TaskMonitor taskMonitor, OpenMRSAuthenticator openMRSAuthenticator) throws FeedException {
         this.atomFeedProperties = atomFeedProperties;
         this.jdbcConnectionProvider = jdbcConnectionProvider;
         this.workerFactory = workerFactory;
         this.openERPClient = openERPClient;
         this.feedName = feedName;
         this.allFeeds = allFeeds;
         this.allMarkers = allMarkers;
         this.allFailedEvents = allFailedEvents;
         this.openMRSAuthenticator = openMRSAuthenticator;
         this.taskMonitor = taskMonitor;
     }
 
     public void processFeed()  {
         try {
             taskMonitor.startTask();
             logger.info("Processing Customer Feed "+ DateTime.now());
 
             getAtomFeedClient().processEvents();
         } catch (Exception e) {
             logger.error("failed customer feed execution " + e);
         } finally {
             taskMonitor.endTask();
         }
     }
 
     public void processFailedEvents()  {
         try {
             taskMonitor.startTask();
             logger.info("Processing failed events for Customer Feed "+ DateTime.now());
 
             getAtomFeedClient().processFailedEvents();
         } catch (Exception e) {
             logger.error("failed customer feed execution " + e);
         } finally {
             taskMonitor.endTask();
         }
     }
 
     private FeedClient getAtomFeedClient() throws FeedException {
         if (atomFeedClient == null) {
             atomFeedClient = getFeedClient(atomFeedProperties,jdbcConnectionProvider, feedName, openERPClient, workerFactory, allFeeds, allMarkers, allFailedEvents);
         }
         return atomFeedClient;
     }
 
     private static AllFeeds getAllFeeds(OpenERPAtomFeedProperties atomFeedProperties) {
         AtomFeedProperties feedProperties = new org.ict4h.atomfeed.client.factory.AtomFeedProperties();
         feedProperties.setConnectTimeout(atomFeedProperties.getConnectionTimeoutInMilliseconds());
         feedProperties.setReadTimeout(atomFeedProperties.getReplyTimeoutInMilliseconds());
         feedProperties.setMaxFailedEvents(atomFeedProperties.getMaxFailedEvents());
         return new AllFeeds(feedProperties, new HashMap<String, String>());
     }
 
     private AtomFeedClient getFeedClient(OpenERPAtomFeedProperties atomFeedProperties,JdbcConnectionProvider jdbcConnectionProvider,
                                                 String feedName, OpenERPClient openERPClient, EventWorkerFactory eventWorkerFactory,
                                                 AllFeeds allFeeds, AllMarkers allMarkers, AllFailedEvents allFailedEvents) throws FeedException {
         String feedUri = atomFeedProperties.getFeedUri(feedName);
         try {
             String urlPrefix = getURLPrefix(atomFeedProperties);
 
             OpenMRSAuthenticationResponse authenticationResponse = openMRSAuthenticator.authenticate(atomFeedProperties.getOpenMRSUser(),
                     atomFeedProperties.getOpenMRSPassword(), ObjectMapperRepository.objectMapper);
             if (!authenticationResponse.isAuthenticated()) throw new FeedException("Failed to authenticate with OpenMRS");
             String sessionIdValue = authenticationResponse.getSessionId();
 
             EventWorker eventWorker = eventWorkerFactory.getWorker("openerp.customer.service", atomFeedProperties.getFeedUri(feedName),openERPClient,
                     atomFeedProperties.getConnectionTimeoutInMilliseconds(), atomFeedProperties.getReplyTimeoutInMilliseconds(), JSESSION_ID_KEY, sessionIdValue, urlPrefix);
 
             return new AtomFeedClient(allFeeds, allMarkers, allFailedEvents, atomFeedProperties(), jdbcConnectionProvider, new URI(feedUri), eventWorker) ;
         } catch (URISyntaxException e) {
             logger.error(e);
             throw new RuntimeException("error for uri:" + feedUri);
         }
     }
 
     private static AtomFeedProperties atomFeedProperties() {
         AtomFeedProperties atomFeedProperties = new AtomFeedProperties();
         atomFeedProperties.setControlsEventProcessing(true);
         return atomFeedProperties;
     }
 
     private static String getURLPrefix(OpenERPAtomFeedProperties atomFeedProperties) {
         String authenticationURI = atomFeedProperties.getAuthenticationURI();
         try {
             URL openMRSAuthURL = new URL(authenticationURI);
             return String.format("%s://%s", openMRSAuthURL.getProtocol(), openMRSAuthURL.getAuthority());
         } catch (MalformedURLException e) {
             throw new RuntimeException("Is not a valid URI - " + authenticationURI);
         }
     }
 }
