 package com.intuit.qbo.plugin;
 
 import com.intuit.ipp.data.CompanyInfo;
 import com.intuit.ipp.exception.FMSException;
 import oauth.signpost.OAuthConsumer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 @Path("")
 public class IntuitQBOPlugin
 {
   @Context
   HttpServletRequest request;
   
   @Path("NewoAuth")
   @GET
   @Produces("application/json")
   public Response getoAuthTokens() {
     OAuthConsumer consumer = InternalIPPManager.retrieveoAuthToken(request);
     
     request.getSession().setAttribute("oAuthConsumer", consumer);
     
     return Response.ok().entity("Tokens retrieved").build();      
   }
   
   @Path("oAuthStatus")
   @GET
   @Produces("application/json")
   public Response getoAuthStatus() throws FMSException {
     OAuthConsumer consumer = (OAuthConsumer) request.getSession().getAttribute("oAuthConsumer");
     
     if (consumer == null || consumer.getToken() == null || consumer.getTokenSecret() == null) {
       throw new FMSException("No oAuth token present");
     }
     
     CompanyInfo company = InternalIPPManager.getCompanyInfoWithoAuth(consumer, InternalIPPManager.getRealmId(request));
     
     if (company == null) {
       throw new FMSException("Invalid oAuth token - cannot get company info");
     }
     
     return Response.ok().entity("true").build();      
   }
 
   @Path("customerCount")
   @GET
   @Produces("application/json")
   public Response getCustomerCount() throws FMSException {
     OAuthConsumer consumer = (OAuthConsumer) request.getSession().getAttribute("oAuthConsumer");
     int count = InternalIPPManager.getCustomerCountWithoAuth(consumer, InternalIPPManager.getRealmId(request));
     
     return Response.ok().entity(String.valueOf(count)).build();
   }
 }
