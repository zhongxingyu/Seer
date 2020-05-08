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
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import fr.midipascher.domain.Restaurant;
 import fr.midipascher.domain.business.Facade;
 
 /**
  * @author louis.gueye@gmail.com
  */
 @Component
 @Path(value = WebConstants.BACKEND_PATH + RestaurantsResource.RESTAURANT_COLLECTION_RESOURCE_PATH)
 public class RestaurantsResource {
 
     @Autowired
     private Facade facade;
 
     @Context
     private UriInfo uriInfo;
 
     // private static final Logger LOGGER =
    // LoggerFactory.getLogger(RestaurantsResource.class);
 
     public static final String RESTAURANT_COLLECTION_RESOURCE_PATH = AccountsResource.ACCOUNT_COLLECTION_RESOURCE_PATH
         + AccountsResource.ACCOUNT_SINGLE_RESOURCE_PATH + "/restaurants";
     public static final String RESTAURANT_SINGLE_RESOURCE_PATH = "/{restaurantId}";
 
     @POST
     @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
     public Response createRestaurant(@PathParam(value = "accountId") final Long accountId, final Restaurant restaurant)
             throws Throwable {
 
         final Long id = facade.createRestaurant(accountId, restaurant);
 
         final URI uri = uriInfo.getAbsolutePathBuilder().path(RESTAURANT_SINGLE_RESOURCE_PATH)
                 .build(String.valueOf(id));
 
         return Response.created(uri).build();
 
     }
 
     @DELETE
     @Path(RESTAURANT_SINGLE_RESOURCE_PATH)
     public Response deleteRestaurant(@PathParam(value = "accountId") final Long accountId,
             @PathParam(value = "restaurantId") final Long restaurantId) throws Throwable {
 
         facade.deleteRestaurant(accountId, restaurantId);
 
         return Response.ok().build();
 
     }
 
     @GET
     @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
     @Path(RESTAURANT_SINGLE_RESOURCE_PATH)
     public Response readRestaurant(@PathParam(value = "accountId") final Long accountId,
             @PathParam(value = "restaurantId") final Long restaurantId) throws Throwable {
 
         final Restaurant restaurant = facade.readRestaurant(accountId, restaurantId, true);
 
         return Response.ok(restaurant).build();
 
     }
 
     @PUT
     @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
     @Path(RESTAURANT_SINGLE_RESOURCE_PATH)
     public Response updateRestaurant(@PathParam(value = "accountId") final Long accountId,
             @PathParam(value = "restaurantId") final Long restaurantId, final Restaurant restaurant) throws Throwable {
 
         facade.updateRestaurant(accountId, restaurantId, restaurant);
 
         return Response.ok().build();
 
     }
 
 }
