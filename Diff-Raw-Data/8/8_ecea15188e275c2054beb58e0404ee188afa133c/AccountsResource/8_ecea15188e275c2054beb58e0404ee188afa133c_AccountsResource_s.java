 /**
  * 
  */
 package fr.midipascher.web;
 
 import java.net.URI;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import fr.midipascher.domain.Account;
 import fr.midipascher.domain.business.Facade;
 
 /**
  * @author louis.gueye@gmail.com
  */
 @Component
 @Path(value = WebConstants.BACKEND_PATH + AccountsResource.ACCOUNT_COLLECTION_RESOURCE_PATH)
 public class AccountsResource {
 
     @Autowired
     private Facade facade;
 
     @Context
     private UriInfo uriInfo;
 
     // private static final Logger LOGGER =
     // LoggerFactory.getLogger(AccountsResource.class);
 
     public static final String ACCOUNT_COLLECTION_RESOURCE_PATH = "/accounts";
     public static final String ACCOUNT_SINGLE_RESOURCE_PATH = "/{accountId}";
 
     @POST
     @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
     public Response createAccount(final Account account) throws Throwable {

         final Long id = facade.createAccount(account);
         final URI uri = uriInfo.getAbsolutePathBuilder().path(AccountsResource.ACCOUNT_SINGLE_RESOURCE_PATH)
                 .build(String.valueOf(id));
 
         return Response.created(uri).build();
 
     }
 
     @DELETE
     @Path(ACCOUNT_SINGLE_RESOURCE_PATH)
     public Response deleteAccount(@PathParam(value = "accountId") final Long id) throws Throwable {
 
         facade.deleteAccount(id);
 
         return Response.ok().build();
 
     }
 
     @POST
     @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
     @Path(ACCOUNT_SINGLE_RESOURCE_PATH + "/lock")
     public Response lockAccount(@PathParam(value = "accountId") final Long id) throws Throwable {
 
         facade.lockAccount(id);
 
         return Response.ok().build();
 
     }
 
     @GET
     @Path(ACCOUNT_SINGLE_RESOURCE_PATH)
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public Response readAccount(@PathParam(value = "accountId") final Long id) throws Throwable {
 
         final Account account = facade.readAccount(id, true);
 
         return Response.ok(account).build();
 
     }
 
     @PUT
     @Path(ACCOUNT_SINGLE_RESOURCE_PATH)
     @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
     public Response updateAccount(@PathParam(value = "accountId") final Long id, final Account account)
             throws Throwable {
 
         facade.updateAccount(id, account);
 
         return Response.ok().build();
 
     }
 
 }
