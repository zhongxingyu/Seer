 package com.zenika.dorm.core.dao.neo4j.util;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.json.JSONConfiguration;
 import com.zenika.dorm.core.dao.neo4j.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.core.MediaType;
 import java.lang.reflect.Type;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 public class Neo4jRequestExecutor implements RequestExecutor {
 
     private static Logger logger = LoggerFactory.getLogger(Neo4jRequestExecutor.class.getName());
 
     public static final String NODE_ENTRY_POINT_URI = "http://localhost:7474/db/data/node";
     public static final String DATA_ENTRY_POINT_URI = "http://localhost:7474/db/data";
     public static final String INDEX_ENTRY_POINT_URI = "http://localhost:7474/db/data/index";
     public static final String METADATA_RELATIONSHIP = "metadata_relationship";
     public static final String ORIGIN_RELATIONSHIP = "origin_relationship";
     public static final String BATCH_URI = "http://localhost:7474/db/data/batch";
 
     private WebResource resource;
 
     public Neo4jRequestExecutor() {
         ClientConfig config = new DefaultClientConfig();
         config.getClasses().addAll(getClasses());
         config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
         Client client = Client.create(config);
         resource = client.resource(DATA_ENTRY_POINT_URI);
     }
 
     @Override
     public <T extends Neo4jNode> void post(T node) {
         Neo4jResponse<T> response = resource.path("node").accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                 .entity(node).post(new GenericType<Neo4jResponse<T>>() {
                 });
         node.setResponse(response);
         logRequest("POST", resource);
     }
 
     @Override
     public void post(Neo4jRelationship relationship) throws URISyntaxException {
         resource.uri(new URI(relationship.getFrom())).accept(MediaType.APPLICATION_JSON)
                 .type(MediaType.APPLICATION_JSON).post(relationship);
         logRequest("POST", resource);
     }
 
     @Override
     public Neo4jIndex post(Neo4jIndex index) {
         logger.trace(Neo4jIndex.INDEX_PATH);
 
         try {
             index = resource.path(Neo4jIndex.INDEX_PATH).accept(MediaType.APPLICATION_JSON)
                     .type(MediaType.APPLICATION_JSON).post(Neo4jIndex.class, index);
         } catch (Exception e) {
             logger.info("Cannot establish connection to the neo4j server", e);
         }
 
         logRequest("POST", resource);
         return index;
     }
 
     @Override
     public void post(Neo4jNode node, URI indexUri) {
         resource.uri(indexUri).type(MediaType.APPLICATION_JSON).post("\"" + node.getResponse().getSelf() + "\"");
         logRequest("POST", resource);
     }
 
     @Override
     public List<Neo4jRelationship> post(URI uri, Neo4jTraverse traverse) {
         List<Neo4jRelationship> relationships = resource.uri(uri).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(traverse)
                 .post(new GenericType<List<Neo4jRelationship>>() {
                 });
         logRequest("POST", resource);
         return relationships;
     }
 
     @Override
     public <T> T get(URI uri, Class<T> type) {
         T object = resource.uri(uri).accept(MediaType.APPLICATION_JSON).get(type);
         logRequest("GET", resource);
         return object;
     }
 
     @Override
     public <T> T get(URI uri, Type type) {
         T object = resource.uri(uri).accept(MediaType.APPLICATION_JSON).get(new GenericType<T>(type));
         logRequest("GET", resource);
         return object;
     }
 
     @Override
     public <T extends Neo4jNode> T getNode(URI uri, Type type) {

         Neo4jResponse<T> response = resource.uri(uri).accept(MediaType.APPLICATION_JSON).get(new GenericType<Neo4jResponse<T>>(type));
         T node = response.getData();
         node.setResponse(response);
         node.setProperties();
         logRequest("GET", resource);
         return node;
     }
 
     @Override
     public <T extends Neo4jNode> T getNode(URI uri) {
         Neo4jResponse<T> response = resource
                 .uri(uri)
                 .accept(MediaType.APPLICATION_JSON)
                 .get(new GenericType<Neo4jResponse<T>>() {
                 });
         T node = response.getData();
         node.setResponse(response);
         node.setProperties();
         logRequest("GET", resource);
         return node;
     }
 
     public String test() {
         return "1";
     }
 
     public static void logRequest(String type, WebResource resource) {
         logger.info(type + " to " + resource.getURI());
     }
 
     private static Set<Class<?>> getClasses() {
         final Set<Class<?>> classes = new HashSet<Class<?>>();
 
         classes.add(ObjectMapperProvider.class);
         classes.add(Neo4jDependency.class);
         classes.add(Neo4jMetadata.class);
         classes.add(Neo4jMetadataExtension.class);
         classes.add(Neo4jResponse.class);
         classes.add(Neo4jIndex.class);
         return classes;
     }
 }
