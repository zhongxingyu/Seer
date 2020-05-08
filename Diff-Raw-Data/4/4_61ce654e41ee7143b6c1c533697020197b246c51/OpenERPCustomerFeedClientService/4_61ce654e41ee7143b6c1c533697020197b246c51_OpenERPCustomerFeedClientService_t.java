 package org.bahmni.feed.openerp;
 
 
 import org.apache.log4j.Logger;
 import org.bahmni.feed.openerp.event.EventWorkerFactory;
 import org.bahmni.openerp.web.client.OpenERPClient;
 import org.ict4h.atomfeed.client.factory.AtomFeedProperties;
 import org.ict4h.atomfeed.client.repository.AllFailedEvents;
 import org.ict4h.atomfeed.client.repository.AllFeeds;
 import org.ict4h.atomfeed.client.repository.AllMarkers;
 import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
 import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
 import org.ict4h.atomfeed.client.service.AtomFeedClient;
 import org.ict4h.atomfeed.client.service.EventWorker;
 import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 
 import java.net.URI;
 import java.net.URISyntaxException;
import java.util.HashMap;
 
 @Controller
 public class OpenERPCustomerFeedClientService {
     private AtomFeedClient atomFeedClient;
 
     private static Logger logger = Logger.getLogger(OpenERPCustomerFeedClientService.class);
     private TaskMonitor taskMonitor;
 
     public OpenERPCustomerFeedClientService() {
     }
 
     @Autowired
     public OpenERPCustomerFeedClientService(OpenERPAtomFeedProperties atomFeedProperties, OpenERPClient openERPClient,
                                             String feedName, JdbcConnectionProvider jdbcConnectionProvider,
                                             org.bahmni.feed.openerp.TaskMonitor customerFeedClientMonitor) {
         this(atomFeedProperties,jdbcConnectionProvider, new EventWorkerFactory(), openERPClient, feedName,
                 getAllFeeds(atomFeedProperties), new AllMarkersJdbcImpl(jdbcConnectionProvider),
                 new AllFailedEventsJdbcImpl(jdbcConnectionProvider), customerFeedClientMonitor);
     }
 
     OpenERPCustomerFeedClientService(OpenERPAtomFeedProperties atomFeedProperties, JdbcConnectionProvider jdbcConnectionProvider, EventWorkerFactory workerFactory,
                                      OpenERPClient openERPClient, String feedName,
                                      AllFeeds allFeeds, AllMarkers allMarkers, AllFailedEvents allFailedEvents, TaskMonitor taskMonitor) {
         this.taskMonitor = taskMonitor;
         this.atomFeedClient = getFeedClient(atomFeedProperties,jdbcConnectionProvider, feedName, openERPClient, workerFactory, allFeeds, allMarkers, allFailedEvents);
     }
 
     public void processFeed()  {
         try {
             taskMonitor.startTask();
 
             logger.info("Processing Customer Feed "+ DateTime.now());
             atomFeedClient.processEvents();
         } catch (Exception e) {
             logger.error("failed customer feed execution " + e);
         } finally {
             taskMonitor.endTask();
         }
     }
 
     private static AllFeeds getAllFeeds(OpenERPAtomFeedProperties atomFeedProperties) {
         AtomFeedProperties feedProperties = new org.ict4h.atomfeed.client.factory.AtomFeedProperties();
         feedProperties.setConnectTimeout(atomFeedProperties.getConnectionTimeoutInMilliseconds());
         feedProperties.setReadTimeout(atomFeedProperties.getReplyTimeoutInMilliseconds());
         feedProperties.setMaxFailedEvents(atomFeedProperties.getMaxFailedEvents());
        return new AllFeeds(feedProperties, new HashMap<String, String>());
     }
 
     private static AtomFeedClient getFeedClient(OpenERPAtomFeedProperties atomFeedProperties,JdbcConnectionProvider jdbcConnectionProvider,
                                                 String feedName,
                                                 OpenERPClient openERPClient, EventWorkerFactory eventWorkerFactory,
                                                 AllFeeds allFeeds, AllMarkers allMarkers, AllFailedEvents allFailedEvents) {
         String feedUri = atomFeedProperties.getFeedUri(feedName);
         try {
             EventWorker eventWorker = eventWorkerFactory.getWorker("openerp.customer.service", atomFeedProperties.getFeedUri(feedName),openERPClient);
             return new AtomFeedClient(allFeeds, allMarkers, allFailedEvents,atomFeedProperties(),jdbcConnectionProvider, new URI(feedUri), eventWorker) ;
         } catch (URISyntaxException e) {
             logger.error(e);
             throw new RuntimeException("error for uri:" + feedUri);
         }
     }
 
     private static AtomFeedProperties atomFeedProperties() {
         AtomFeedProperties atomFeedProperties = new AtomFeedProperties();
         atomFeedProperties.setControlsEventProcessing(true);
         return atomFeedProperties;  //To change body of created methods use File | Settings | File Templates.
     }
 
 }
