 package us.mapr.api.json;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import org.springframework.security.oauth2.consumer.OAuth2AccessTokenRequiredException;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
 
@Provider
 public class OAuth2AccessTokenRequiredExceptionJsonMapper implements ExceptionMapper<OAuth2AccessTokenRequiredException> {
     @Override
     public Response toResponse(OAuth2AccessTokenRequiredException e) {
         return Response
                 .status(Response.Status.FORBIDDEN)
                 .type("application/json")
                 .entity(new JSONObject().element("errors", new JSONArray().element(
                         new JSONObject()
                                 .element("message", e.getMessage())
                 )).toString())
                 .build();
 
     }
 }
