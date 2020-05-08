 package com.scrumble.server.services;
 
 import com.scrumble.server.entities.Project;
 import com.scrumble.server.entities.Userstory;
 import com.scrumble.server.sessionbeans.UserstoryFacadeLocal;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 
 /**
  * REST Web Service
  *
  * @author ArNo
  */
 @Path("userstories")
 @Stateless
 public class UserStoriesResource {
 
     @Context
     private UriInfo context;
     
     @EJB
     private UserstoryFacadeLocal userStoryBean;
 
     /**
      * Creates a new instance of UserStoriesResource
      */
     public UserStoriesResource() {
     }
 
     /**
      * Retrieves representation of list of com.scrumble.server.entities.Userstory object
      * @return a JSON representation of the list of all userstories.
      */
     @GET
     @Path("all")
     @Produces("application/json")
     public List<Userstory> findAll() {
        return userStoryBean.findAllOrderByImportance();
     }
     
     
     /**
      * Retrieves representation of a single com.scrumble.server.entities.Userstory object
      * @param id the id of the Userstory object to retrieve
      * @return a JSON representation of the related Userstory object.
      */
     @GET
     @Path("search/{pattern}")
     @Produces("application/json")
     public List<Userstory> searchUserStoriesQuick(@PathParam("pattern") String pattern) {
         return userStoryBean.quickSearch(pattern);
     }
     
     
     /**
      * Retrieves representation of a single com.scrumble.server.entities.Userstory object
      * @param id the id of the Userstory object to retrieve
      * @return a JSON representation of the related Userstory object.
      */
     @GET
     @Path("{id}")
     @Produces("application/json")
     public Userstory getUserStory(@PathParam("id") String id) {
         return userStoryBean.find(Integer.parseInt(id));
     }
     
     
     /**
      * POST method for creating an instance of Userstory object
      * @param userstory JSON representation for the Userstory object
      * @return an HTTP response with content of the created resource.
      */
     @POST
     @Path("add")
     @Consumes("application/json")
     @Produces("application/json")
     public Response addUserStory(Userstory userstory) {
         userStoryBean.create(userstory);
         
         Response reponse=Response.status(200).build();
         return reponse;
     }
     
     
     /**
      * PUT method for updating an instance of Userstory object
      * @param userstory JSON representation for the Userstory object
      * @return an HTTP response with content of the created resource.
      */
     @PUT
     @Consumes("application/json")
     @Produces("application/json")
     public void updateUserStory(Userstory userstory) {
         userStoryBean.edit(userstory);
     }
     
     
     /**
      * Removes a single com.scrumble.server.entities.Userstory object
      * @param id the id of the Userstory object to remove
      * @return nothing.
      */
     @DELETE
     @Path("{id}")
     @Produces("application/json")
     public Response removeUserStory(@PathParam("id") String id) {
         if(userStoryBean.find(Integer.parseInt(id))!=null)
             userStoryBean.remove(userStoryBean.find(Integer.parseInt(id)));
         
         Response reponse=Response.status(200).build();
         return reponse;
     }
     
     
     /**
      * POST method for creating an instance of Userstory object
      * @param userstory JSON representation for the Userstory object
      * @return an HTTP response with content of the created resource.
      */
     @POST
     @Path("{id}/{position}")
     @Consumes("application/json")
     @Produces("application/json")
     public Response updatePriority(@PathParam("id") String id, @PathParam("position") String position) {
         userStoryBean.updateImportance(Integer.parseInt(id), Integer.parseInt(position));
         Response reponse=Response.status(200).build();
         return reponse;
     }
     
}
