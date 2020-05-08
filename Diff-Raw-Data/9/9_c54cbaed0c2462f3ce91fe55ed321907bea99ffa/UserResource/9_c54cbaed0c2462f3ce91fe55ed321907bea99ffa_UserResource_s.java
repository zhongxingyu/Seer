 package org.oregami.resources;
 
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.oregami.data.UserDao;
 import org.oregami.entities.user.User;
 import org.oregami.service.IUserService;
 import org.oregami.service.ServiceResult;
 import org.oregami.util.MailHelper;
 
 import com.google.inject.Inject;
 
 @Path("/user")
 @Produces(MediaType.APPLICATION_JSON)
 @Consumes(MediaType.APPLICATION_JSON)
 public class UserResource {
 
 	@Inject
 	private UserDao userDao;
 	
 	@Inject
 	private IUserService userService;
 
 	MailHelper mailHelper = MailHelper.instance();
 	
 	@POST
 	public Response createUser(User user) {
 		try {
 			ServiceResult<User> register = userService.register(user);
 			if (register.hasErrors()) {
				return Response.status(Status.BAD_REQUEST).type("text/plain")
		                .entity("Errors: " + register.getErrors()).build();
 			}
//			userDao.save(user);
//			mailHelper.sendMail("gene@kultpower.de", "test-subject", "Tolle neue Mail!\n" + user);
 			return Response.ok().build();
 		} catch (Exception e) {
 			return Response.status(javax.ws.rs.core.Response.Status.CONFLICT).type("text/plain")
 	                .entity(e.getMessage()).build();
 		}
 
 	}
 	
 	
 	@GET
 	public List<User> getUserlist() {
 		List<User> findAll = userDao.findAll();
 		return findAll;
 	}
 
 
 }
