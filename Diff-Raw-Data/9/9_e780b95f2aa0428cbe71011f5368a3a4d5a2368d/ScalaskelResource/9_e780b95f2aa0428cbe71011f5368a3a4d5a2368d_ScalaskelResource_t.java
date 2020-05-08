 package org.diveintojee.codestory2013;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import static org.codehaus.jackson.map.SerializationConfig.Feature.WRITE_NULL_PROPERTIES;
 
 /**
  * @author louis.gueye@gmail.com
  */
 @Component
 @Path("/scalaskel")
 public class ScalaskelResource {
 
     @Autowired
     private ScalaskelService scalaskelService;
 
    @Path("/change/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @GET
     public Response getChange(@PathParam("id") String id) throws IOException {
         final Response.ResponseBuilder ok = Response.ok();
         final Integer amount = Integer.valueOf(id);
         Map<Integer, List<Map<String, Integer>>> result = scalaskelService.getChange(amount);
         ok.entity(result.get(amount).toArray());
         return ok.build();
     }
 }
