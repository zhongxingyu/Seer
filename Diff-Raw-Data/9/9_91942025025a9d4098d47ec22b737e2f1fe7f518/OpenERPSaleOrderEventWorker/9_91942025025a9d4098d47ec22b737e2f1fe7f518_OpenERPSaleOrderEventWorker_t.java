 package org.bahmni.feed.openerp.event;
 
 import org.apache.log4j.Logger;
 import org.bahmni.feed.openerp.ObjectMapperRepository;
 import org.bahmni.feed.openerp.OpenMRSEncounterMapper;
 import org.bahmni.feed.openerp.domain.encounter.OpenERPOrder;
 import org.bahmni.feed.openerp.domain.encounter.OpenERPOrders;
 import org.bahmni.feed.openerp.domain.encounter.OpenMRSEncounter;
 import org.bahmni.feed.openerp.domain.encounter.OpenMRSOrder;
 import org.bahmni.openerp.web.client.OpenERPClient;
 import org.bahmni.openerp.web.request.OpenERPRequest;
 import org.bahmni.openerp.web.request.builder.Parameter;
 import org.bahmni.webclients.WebClient;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.ict4h.atomfeed.client.domain.Event;
 import org.ict4h.atomfeed.client.service.EventWorker;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class OpenERPSaleOrderEventWorker implements EventWorker {
     OpenERPClient openERPClient;
     private String feedUrl;
     private WebClient webClient;
     private String urlPrefix;
     public static ObjectMapper objectMapper = new ObjectMapper();
 
 
     private static Logger logger = Logger.getLogger(OpenERPSaleOrderEventWorker.class);
 
     public OpenERPSaleOrderEventWorker(String feedUrl, OpenERPClient openERPClient, WebClient webClient, String urlPrefix) {
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
 
         OpenMRSEncounterMapper openMRSEncounterMapper = new OpenMRSEncounterMapper(ObjectMapperRepository.objectMapper);
         OpenMRSEncounter openMRSEncounter = openMRSEncounterMapper.map(patientJSON);
 
         return mapParameters(openMRSEncounter, event.getId(), event.getFeedUri());
     }
 
     private List<Parameter> mapParameters(OpenMRSEncounter openMRSEncounter, String eventId, String feedUri) throws IOException {
         List<Parameter> parameters = new ArrayList<Parameter>();
         String patientDisplay = openMRSEncounter.getPatient().getDisplay();
         String patientId = patientDisplay.split(" ")[0];
 
         parameters.add(createParameter("category", "create.sale.order", "string"));
         parameters.add(createParameter("customer_id", patientId, "string"));
        if((feedUrl != null && feedUrl.contains("$param.value")) || (feedUri != null && feedUri.contains("$param.value")))
             throw new RuntimeException("Junk values in the feedUrl:$param.value");
         parameters.add(createParameter("feed_uri", feedUrl, "string"));
         parameters.add(createParameter("last_read_entry_id", eventId, "string"));
         parameters.add(createParameter("feed_uri_for_last_read_entry", feedUri, "string"));
 
         OpenERPOrders orders = new OpenERPOrders();
         orders.setId(openMRSEncounter.getUuid());
 
         if(openMRSEncounter.getOrders().size() > 0){
             for(OpenMRSOrder order : openMRSEncounter.getOrders())    {
                 OpenERPOrder openERPOrder = new OpenERPOrder();
                 openERPOrder.setId(order.getUuid());
                 List<String> productIds = new ArrayList<String>();
                 productIds.add(order.getConcept().getUuid());
                 openERPOrder.setProductIds(productIds);
                 orders.getOpenERPOrders().add(openERPOrder);
             }
             String ordersJson = objectMapper.writeValueAsString(orders);
 
             parameters.add(createParameter("orders", ordersJson, "string"));
         }
         return parameters;
     }
 
 
     private Parameter createParameter(String name, String value, String type) {
         return new Parameter(name, value, type);
     }
 
 }
