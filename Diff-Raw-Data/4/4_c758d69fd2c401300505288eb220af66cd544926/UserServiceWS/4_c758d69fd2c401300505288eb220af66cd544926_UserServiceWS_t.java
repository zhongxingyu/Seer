 package com.pq.trends.webservices;
 
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.xml.bind.JAXBElement;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.pq.trends.domain.User;
 import com.pq.trends.services.UserService;
 
 
 @Component
 @Path("/user")
 public class UserServiceWS {
 
 	@Autowired
 	UserService userservice;
 	
 	@GET
 	@Path("msg")
 	public Response getMessage() {
 		return Response.status(200).entity("Welcome to RestFul Web Services").build();
 	}
 	
 	@GET
 	@Path("/finduser/{id}")
	@Produces({MediaType.APPLICATION_JSON})
 	public Response getUser(@PathParam("id") String id) {
 		System.out.println("@GET Find User Method ::: " + id);
 		User user = userservice.findUser(id, "weclome");	 
 		System.out.println("@GET Find User Method :user:: " + user);
		return Response.status(200).entity(user).build();
 	}
 	
 	
 	@GET
 	@Path("/userlist")
 	@Produces({MediaType.APPLICATION_JSON})
 	public List<User> getAllUserList() {
 		 
 		return userservice.getAllUsers();
 		 
 	}
 	
 	//to test thru HTML form
 	@POST
 	@Path("/adduser")	
 	public Response addUser(@FormParam("id") Integer id,
 			@FormParam("firstName") String firstName,
 			@FormParam("lastName") String lastName,
 			@FormParam("email") String email,
 			@FormParam("password") String password) {
 			 		 
 		User user = new User(id, firstName, lastName, email,password);
 		
 		System.out.println("Post Method ::: "+ user);
 		System.out.println("@userservice.save(user) ...."+ userservice.save(user));
 		
 		return Response.status(200).entity("Welcome to RestFul Web Services - Post Method - completed").build();
 	}
 	
 	@POST
 	@Path("/saveuser")
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response saveUser(User user) {
 		
 		 
 		System.out.println("Post Method ::: "+ user);
 		
 		System.out.println("@userservice.save(user) ...."+ userservice.save(user));
 		
 		return Response.status(200).entity("Welcome to RestFul Web Services - Post Method - completed").build();
 	}
 
 	@PUT
 	@Path("/updateuser")
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response putUser(User user) {
 		
 		 
 		System.out.println("Put Method ::: "+ user);
 		
 		System.out.println("@userservice.save(user) ...."+ userservice.save(user));
 		
 		return Response.status(200).entity("Welcome to RestFul Web Services - PUT Method - completed").build();
 	}
 	
 	
 	@DELETE
 	@Path("deleteuser/{id}")	 
 	public void deleteUser(@PathParam("id") String id) {
 			 		 		 
 		System.out.println("Delete Method ::: " + id);
 		userservice.delete(id);	 
 	}
 	
 }
