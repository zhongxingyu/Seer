 package com.inebriator.resources;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import com.inebriator.InebriatorWrapperListener;
 
 @Path("/inebriator/pour/straight/{name}")
 public class PourStraightResource {
 
	private static final Logger LOG = LoggerFactory.getLogger(PourStraightResource.class);
	
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	public Response pour(@PathParam("name") String name, @QueryParam("units") int units) {
 		if (units < 1) {
 			return Response.status(Status.BAD_REQUEST).entity("Number of units must be a positive integer").type(MediaType.TEXT_PLAIN).build();
 		}
 		try {
 			InebriatorWrapperListener.inebriator.pourStraight(name, units);
 		} catch (Exception e) {
			LOG.warn("PourStraight failed", e);
 			return Response.serverError().entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
 		}
 
 		return Response.ok().entity("Poured " + units + " unit" + (units > 1 ? "s" : "") + " of " + name + ".\n").type(MediaType.TEXT_PLAIN).build();
 	}
 }
