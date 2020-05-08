 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ch.heigvd.gamification.rest;
 
 import ch.heigvd.gamification.exceptions.EntityNotFoundException;
 import ch.heigvd.gamification.model.Event;
 import ch.heigvd.gamification.services.crud.EventsManagerLocal;
 import ch.heigvd.gamification.services.to.EventsTOServiceLocal;
 import ch.heigvd.gamification.to.PublicEventTO;
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
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
 
 /**
  *
  * @author Jean-Luc
  */
 @Stateless
 @Path("events")
 public class EventResource {
     @Context
     private UriInfo context;
 
     @EJB
     EventsManagerLocal eventsManager;
 
     @EJB
     EventsTOServiceLocal eventsTOService;
     
     public EventResource() {}
     
     @POST
     @Consumes({"application/json"})
     public Response createResource(PublicEventTO newEventTO) {
         Event newEvent = new Event();
         eventsTOService.updateEventEntity(newEvent, newEventTO);
        long newRuleId = eventsManager.create(newEvent);
        URI createdURI = context.getAbsolutePathBuilder().path(Long.toString(newRuleId)).build();
         return Response.created(createdURI).build();
     }
     
     @GET
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public List<PublicEventTO> getResourceList() {
         List<Event> events = eventsManager.findAll();
         List<PublicEventTO> result = new LinkedList<PublicEventTO>();
         for (Event event : events) {
             result.add(eventsTOService.buildPublicEventTo(event));
         }
         return result;
     }
     
     @GET
     @Path("{id}")
     @Produces({"application/json", "application/xml"})
     public PublicEventTO getResource(@PathParam("id") long id) throws EntityNotFoundException {
         Event event = eventsManager.findById(id);
         PublicEventTO eventTO = eventsTOService.buildPublicEventTo(event);
         return eventTO;
     }
 
     @PUT
     @Path("{id}")
     @Consumes({"application/json"})
     public Response Resource(PublicEventTO updatedEventTO, @PathParam("id") long id) throws EntityNotFoundException {
         Event eventToUpdate = eventsManager.findById(id);
         eventsTOService.updateEventEntity(eventToUpdate, updatedEventTO);
         eventsManager.update(eventToUpdate);
         return Response.ok().build();
     }
 
     @DELETE
     @Path("{id}")
     public Response deleteResource(@PathParam("id") long id) throws EntityNotFoundException {
         eventsManager.delete(id);
         return Response.ok().build();
     }
 }
