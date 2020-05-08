 package com.mobileApps.server.ws.resources;
 
 import java.net.URI;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import com.mobileApps.server.ws.domain.Event;
 import com.mobileApps.server.ws.service.EventService;
 
 @Path("/events")
 public class EventResource {
 
 
 	private EventService eventService;
 	
 	@POST
 	@Consumes({MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
 	public Response syncEvents(@Context UriInfo ui, 
 		    					 @Context HttpServletResponse response,  
 								 List<Event> eventList){
 		
 		List<Event> processedList = EventService.createOrUpdateEventList(eventList);
 		
 
 		return Response.ok(processedList).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	@POST
 	@Path("/event")
 	@Consumes({MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
 	public Response registerEvent(@Context UriInfo ui, 
 			@Context HttpServletResponse response,  
 			Event event){
 		
 		event = EventService.registerEvent(event);
 		
 		if(event.getId() == null)
 			throw new WebApplicationException(
 					Response
 					.status(Status.PRECONDITION_FAILED)
 					.entity("Event alreay exists .. testing!!!")
 					.build()
 					);
 		
 		return Response.created(URI.create("/" + event.getId())).entity(event).header("Access-Control-Allow-Origin", "*").build();
 	}
 	
 	@PUT
 	@Consumes({MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
 	public Response updateEvent(@Context UriInfo ui, 
 		    					 @Context HttpServletResponse response,  
 								 Event event){
 		
 		event = EventService.updateEvent(event);
 		
 	
 		return Response.created(URI.create("/" + event.getId())).entity(event).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	@PUT
 	@Path("/{eventId}")
 	@Consumes({MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
 	public Response updateEvent(@Context UriInfo ui, 
 								@Context HttpServletResponse response,  
								@PathParam("eventId") Long eventId){
 		
		EventService.logicalRemoveById(eventId);
 		
 		
		return Response.ok().header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	
 	
 	@GET
 	@Produces({ MediaType.APPLICATION_JSON })
 	public Response getAllEvents(){
 		
 		return Response.ok(EventService.getAllEvents()).header("Access-Control-Allow-Origin", "*")
 				.build();
 	}
 
 	@GET
 	@Path("/{eventId}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	@SuppressWarnings("static-access")
 	public Response getEventsById(@PathParam("eventId") Long id){
 		
 		return Response.ok(eventService.getEventsById(id)).header("Access-Control-Allow-Origin", "*")
 				.build();
 	}
 
 	
 	@DELETE
 	@Path("/{eventId}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	@SuppressWarnings("static-access")
 	public Response removeEventsById(@PathParam("eventId") Long id){
 		EventService.removeEventsById(id);
 		return Response.ok().header("Access-Control-Allow-Origin", "*")
 				.build();
 	}
 
 	@GET
 	@Path("/user/{userId}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	@SuppressWarnings("static-access")
 	public Response getEventsByUserId(@PathParam("userId") Long id){
 		
 		return Response.ok(EventService.getEventsByUserId(id)).header("Access-Control-Allow-Origin", "*")
 				.build();
 	}
 	
 	
 
 	@GET
 	@Path("/date")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public Response getEventsForToday( @QueryParam("searchDate") Date date){		
 		
 		return Response.ok(EventService.getEventsForSpecificDate(date)).header("Access-Control-Allow-Origin", "*")
 				.build();
 		//.header("Content-Type", "application/json")
 	}
 
 	
 	
 	@GET
 	@Path("/location/country/{country}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	@SuppressWarnings("static-access")
 	public Response getEventsByLocationCountry(@PathParam("country") String country, 	
 											   @DefaultValue("title") @QueryParam("sortBy") String sortBy,
 											   @DefaultValue("asc") @QueryParam("sortMode") String sortMode){
 		
 		return Response.ok(eventService.getEventsByLocationSimple(country, sortBy, sortMode)).header("Access-Control-Allow-Origin", "*")
 				.build();
 	}
 	
 
 	@GET
 	@Path("/location/country/{country}/city/{city}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	@SuppressWarnings("static-access")
 	public Response getEventsByLocation( @PathParam("country") String country,
 										@PathParam("city") String city//,
 //										 @QueryParam("sortBy") String sortBy
 										 ){
 		
 		return Response.ok(eventService.getEventsByLocation(city)).header("Access-Control-Allow-Origin", "*")
 				.build();
 	}
 	
 
 //	@GET
 //	@Path("/location/{city}/date/{date}")
 //	@Produces({ MediaType.APPLICATION_JSON })
 //	@SuppressWarnings("static-access")
 //	public Response getEventsByLocation(@PathParam("city") String id, @PathParam("date") String date){
 //		try {
 //		      final Date date = ISO_BASIC.parseDateTime(dateAsString);
 //		      return date + " is on a " + date.dayOfWeek().getAsText() + ".";
 //		    } catch (IllegalArgumentException e) {
 //		      throw new WebApplicationException(
 //		        Response
 //		          .status(Status.BAD_REQUEST)
 //		          .entity("Couldn't parse date: " + dateAsString + " (" + e.getMessage() + ")")
 //		          .build()
 //		      );
 //		    }
 //		return Response.ok(eventService.getEventsByLocation(id)).header("Access-Control-Allow-Origin", "*")
 //				.build();
 //	}
 
 	
 }
