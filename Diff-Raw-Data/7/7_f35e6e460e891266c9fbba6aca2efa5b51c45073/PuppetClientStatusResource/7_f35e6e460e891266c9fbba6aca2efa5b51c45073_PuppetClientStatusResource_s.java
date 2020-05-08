 package org.apache.ambari.agent.jersey;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 
 @Path("/production/run")
 public class PuppetClientStatusResource {
   private static final Logger LOG = Logger.getLogger(PuppetClientStatusResource.class);
   /**
    * 
    * @return
    */
   @GET
  @Path("{subResources")
  public String doGet(@Context UriInfo uriInfo) {
     LOG.info("Called on a get message " + uriInfo.getAbsolutePath());
     return "success";
   }
 
   /**
    *  
    * @return
    */
   @POST
   @Consumes("text/pson")
   public String doPost(@Context UriInfo uriInfo) {
     LOG.info("Puppet Kick Servlet called on " + uriInfo.getAbsolutePath());
     return "Success";
   }
 }
