 package org.bahmni.feed.openerp.event;
 
 import org.apache.log4j.Logger;
 import org.bahmni.feed.openerp.ObjectMapperRepository;
 import org.bahmni.feed.openerp.OpenMRSPatientMapper;
 import org.bahmni.feed.openerp.domain.OpenMRSPatient;
 import org.bahmni.openerp.web.client.OpenERPClient;
 import org.bahmni.openerp.web.request.OpenERPRequest;
 import org.bahmni.openerp.web.request.builder.Parameter;
 import org.bahmni.webclients.WebClient;
 import org.ict4h.atomfeed.client.domain.Event;
 import org.ict4h.atomfeed.client.service.EventWorker;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class OpenERPCustomerServiceEventWorker implements EventWorker {
     OpenERPClient openERPClient;
     private String feedUrl;
     private WebClient webClient;
     private String urlPrefix;
 
     private static Logger logger = Logger.getLogger(OpenERPCustomerServiceEventWorker.class);
 
     public OpenERPCustomerServiceEventWorker(String feedUrl, OpenERPClient openERPClient, WebClient webClient, String urlPrefix) {
         this.feedUrl = feedUrl;
         this.openERPClient = openERPClient;
         this.webClient = webClient;
         this.urlPrefix = urlPrefix;
     }
 
     @Override
     public void process(Event event) {
         try {
             openERPClient.execute(mapRequest(event));
         } catch (Exception e) {
             logger.error(e);
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void cleanUp(Event event) {
     }
 
     public void processFailedEvents(Event event) {
         try {
             openERPClient.execute(mapFailedEventRequest(event));
         } catch (Exception e) {
             logger.error(e);
             throw new RuntimeException(e);
         }
     }
 
     private OpenERPRequest mapFailedEventRequest(Event event) throws IOException {
         List<Parameter> parameterList = getParameters(event);
         parameterList.add(createParameter("is_failed_event", "True", "boolean"));
         return new OpenERPRequest("atom.event.worker", "process_event", parameterList);
     }
 
     private OpenERPRequest mapRequest(Event event) throws IOException {
         return new OpenERPRequest("atom.event.worker", "process_event", getParameters(event));
     }
 
     private List<Parameter> getParameters(Event event) throws IOException {
         String content = event.getContent();
         String patientJSON = webClient.get(URI.create(urlPrefix + content), new HashMap<String, String>(0));
 
         OpenMRSPatientMapper openMRSPatientMapper = new OpenMRSPatientMapper(ObjectMapperRepository.objectMapper);
         OpenMRSPatient openMRSPatient = openMRSPatientMapper.map(patientJSON);
 
         return mapParameters(openMRSPatient, event.getId(), event.getFeedUri());
     }
 
     private List<Parameter> mapParameters(OpenMRSPatient openMRSPatient, String eventId, String feedUri) {
         List<Parameter> parameters = new ArrayList<Parameter>();
         parameters.add(createParameter("name", openMRSPatient.getName(), "string"));
         parameters.add(createParameter("ref", openMRSPatient.getIdentifiers().get(0).getIdentifier(), "string"));
         parameters.add(createParameter("village", openMRSPatient.getPerson().getPreferredAddress().getCityVillage(), "string"));
 
         parameters.add(createParameter("category", "create.customer", "string"));
        if((feedUrl != null && feedUrl.contains("$param.value")) || (feedUri != null && feedUri.contains("$param.value")))
             throw new RuntimeException("Junk values in the feedUrl:$param.value");
         parameters.add(createParameter("feed_uri", feedUrl, "string"));
         parameters.add(createParameter("last_read_entry_id", eventId, "string"));
         parameters.add(createParameter("feed_uri_for_last_read_entry", feedUri, "string"));
         return parameters;
     }
 
 
     private Parameter createParameter(String name, String value, String type) {
         return new Parameter(name, value, type);
     }
 
 }
