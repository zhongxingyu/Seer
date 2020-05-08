 package com.tjh.swivel.controller;
 
 import com.tjh.swivel.model.ShuntRequestHandler;
 import org.apache.log4j.Logger;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Map;
 
@Path("config/shunt")
 public class ConfigureShuntResource {
     public static final String REMOTE_URL_KEY = "remoteURL";
 
     protected static Logger LOGGER = Logger.getLogger(ConfigureShuntResource.class);
 
     protected RequestRouter router;
     protected ConfigurationResource configurationResource;
 
     @PUT
    @Path("shunt/{localPath: .*}")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public Map<String, Map<String, Object>> putShunt(@PathParam("localPath") URI localPath, Map<String, String> json)
             throws URISyntaxException {
         try {
             String remoteURL = json.get(REMOTE_URL_KEY);
             LOGGER.debug(String.format("Configuring shunt: proxying %1$s to %2$s", localPath, remoteURL));
 
             router.setShunt(localPath, new ShuntRequestHandler(new URL(remoteURL)));
             return configurationResource.getConfiguration();
         } catch (IllegalArgumentException iae) {
             throw new WebApplicationException(iae, Response.Status.CONFLICT);
         } catch (ClassCastException cce) {
             throw new WebApplicationException(cce, Response.Status.CONFLICT);
         } catch (MalformedURLException mue) {
             throw new WebApplicationException(mue, Response.Status.BAD_REQUEST);
         }
     }
 
     @DELETE
    @Path("shunt/{localPath: .*}")
     @Produces(MediaType.APPLICATION_JSON)
     public Map<String, Map<String, Object>> deleteShunt(@PathParam("localPath") URI localPath) {
         LOGGER.debug(String.format("Removing shunt for %1$s", localPath));
 
         router.deleteShunt(localPath);
 
         return configurationResource.getConfiguration();
     }
 
     public void setConfigurationResource(ConfigurationResource configurationResource) {
         this.configurationResource = configurationResource;
     }
 
     public void setRouter(RequestRouter router) { this.router = router; }
 }
