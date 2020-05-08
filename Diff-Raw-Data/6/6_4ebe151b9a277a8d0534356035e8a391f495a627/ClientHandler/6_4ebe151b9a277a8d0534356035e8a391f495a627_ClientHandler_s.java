 package com.lo54project.webservice.handler;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.lo54project.webservice.model.Client;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 public class ClientHandler {
         
         private static ObjectMapper mapper = new ObjectMapper(); 
         private static ClientConfig cConfig = new DefaultClientConfig();
         private static com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create(cConfig);
         private static WebResource service = client.resource(UriBuilder.fromUri("http://localhost:8080/LO54_Project").build());
         
         public static List<Client> parseClients() throws JsonProcessingException, UniformInterfaceException, ClientHandlerException, IOException, ParseException{
                 
                 List<Client> client = new ArrayList<Client>();
                 
                 JsonNode rootNode = mapper.readTree(service.path("rest").path("clients").accept(MediaType.APPLICATION_JSON).get(String.class));
                 for(JsonNode n : rootNode.path("client")) {
                         client.add(new Client(n.path("id").asInt(), n.path("lastname").asText(), n.path("firstname").asText(), n.path("address").asText(), n.path("phone").asText(), n.path("email").asText()));
                 }
                 
                 return client;
         }
         
         public static Client parseClientById(String id) throws JsonProcessingException, UniformInterfaceException, ClientHandlerException, IOException, ParseException{
             
             Client client = new Client();
             
             JsonNode rootNode = mapper.readTree(service.path("rest").path("clients").path(id).accept(MediaType.APPLICATION_JSON).get(String.class));
            for(JsonNode n : rootNode.path("client")) {
                    client = new Client(n.path("id").asInt(), n.path("lastname").asText(), n.path("firstname").asText(), n.path("address").asText(), n.path("phone").asText(), n.path("email").asText());
            }
             
             return client;
     }
 
 }
