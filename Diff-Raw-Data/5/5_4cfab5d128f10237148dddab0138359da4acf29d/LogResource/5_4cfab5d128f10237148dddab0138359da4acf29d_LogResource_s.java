 package org.djd.fun.taiga.resource;
 
 import com.sun.jersey.api.JResponse;
 import com.sun.jersey.api.NotFoundException;
 import org.djd.fun.taiga.dao.DaoException;
 import org.djd.fun.taiga.dao.WayOfDao;
 import org.djd.fun.taiga.model.SomeData;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.UriBuilder;
 import java.util.List;
 import java.util.TimeZone;
 
 @Path("/logs")
 @Produces({MediaType.APPLICATION_JSON})
 public class LogResource {
 
   @POST
   public JResponse create() {
     return delete();
   }
 
   @DELETE
   public JResponse delete() {
     try {
       new WayOfDao().deleteAll();
       return JResponse.created(UriBuilder.fromPath("/").build()).build();
     } catch (DaoException e) {
       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
       throw new WebApplicationException(500);
     }
   }
 
   @GET
   public List<SomeData> get() {
     try {
       return new WayOfDao().getAll();
     } catch (DaoException e) {
       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
       throw new NotFoundException("Could not find any logs.");
     }
   }
 
   @POST
   @Path("/{somedata}")
   public JResponse add(@PathParam("somedata") String somedata) {
     try {
       int id = new WayOfDao().add(somedata);
      return JResponse.created(UriBuilder.fromPath("/" + id).build()).build();
     } catch (DaoException e) {
       e.printStackTrace();
       throw new WebApplicationException(500);
     }
   }
 }
 
