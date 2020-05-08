 package com.yuktix.test.jersey;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.glassfish.jersey.server.JSONP;
import com.yuktix.rest.exception.RestException;

 
 @Path("/calculator")
 public class Calculator{
 	
 	@GET
 	@Path("/echo")
 	@Consumes(MediaType.TEXT_PLAIN)
 	@Produces(MediaType.TEXT_PLAIN)
 	public String echoMethod(@QueryParam("x") String input) {
 		return input ;
 	}
 	
 	// curl -i -H "Content-Type: application/json" -X POST -d "firstName=james" 
 	// http://<service>/text?token=1234
 	
 	@POST
 	@Path("/text")
 	@Consumes(MediaType.TEXT_PLAIN)
 	@Produces(MediaType.TEXT_PLAIN)
 	public String textMethod(@QueryParam("token") String token, String json) {
 		// map json to json object 
 		return token + "::" + json + "\n";
 	}
 	
 	// curl -i -H "Content-Type: application/json" -X POST -d "{\"name\":\"james\"}" 
 	// http://<service>/json?token=1234
 	
 	@POST
 	@Path("/json")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.TEXT_PLAIN)
 	public String jsonMethod(@QueryParam("token") String token, com.yuktix.test.jersey.dto.Account account) {
 		// map json to json object 
 		return token + "::" + account.getName() + "::" + account.getLocation() + "\n";
 	}
 	
 	// curl -i -H "Content-Type: application/x-www-form-urlencoded" -X POST -d "name=james&location=london" 
 	// http://<service>/form?token=1234
 		
 	@POST
 	@Path("/form")
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	@Produces(MediaType.TEXT_PLAIN)
 	public String formMethod(@QueryParam("token") String token,
 			@FormParam("name") String name,
 			@FormParam("location") String location) {
 		// map json to json object 
 		return token + "::" + name + "::" + location + "\n";
 	}
 		
 	@GET
 	@Path("/add/{a}/{b}")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String addMethod(@PathParam("a") double a, @PathParam("b") double b) {
 		return (a + b) + "";
 	}
 	
 
 	@GET
 	@Path("/error")
 	@JSONP
 	@Produces({"application/javascript", MediaType.APPLICATION_JSON})
	public String errorMethod() {
		throw new RestException("json error in service") ;
 		
 	}
 	
 }
