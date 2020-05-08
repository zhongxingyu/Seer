 package coucou;
 
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
@Path("/coucou")
 public class Coucou {
 	
 	
 	@GET
 	@Produces(MediaType.TEXT_PLAIN)
 	public Response repondre() {
		return Response.ok("OK").build();
 	}
 	
 	
 }
 
