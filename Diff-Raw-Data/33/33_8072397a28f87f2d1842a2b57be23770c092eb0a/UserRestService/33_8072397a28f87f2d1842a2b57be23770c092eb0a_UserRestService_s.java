 package com.jdc.themis.dealer.service.rest;
 
 import javax.annotation.security.RolesAllowed;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.jdc.themis.dealer.service.UserService;
 import com.jdc.themis.dealer.utils.Constant;
 import com.jdc.themis.dealer.utils.RestServiceErrorHandler;
 import com.jdc.themis.dealer.utils.Utils;
 import com.jdc.themis.dealer.web.domain.AddNewUserRequest;
 import com.jdc.themis.dealer.web.domain.GeneralResponse;
 import com.jdc.themis.dealer.web.domain.GetUserInfoResponse;
 import com.jdc.themis.dealer.web.domain.ResetPasswordRequest;
 
 @Service
 @RolesAllowed({Constant.DEALER_ROLE, Constant.HEAD_ROLE, Constant.ADMIN_ROLE, Constant.SUPER_ROLE})
 public class UserRestService {
 
 	@Autowired
 	private UserService userService;
 
 	@Context
 	private HttpServletRequest req;
 
 	/**
 	 * Query user information.
 	 * 
 	 * @param username
 	 * @return
 	 */
 	@GET
 	@Path("/roles")
 	@Produces({ "application/json", "application/xml" })
 	@RestServiceErrorHandler
 	public Response getUserRoles() {
 		return Response.ok(userService.getUserRoles()).build();
 	}
 	
 	/**
 	 * Query user information.
 	 * 
 	 * @param username
 	 * @return
 	 */
 	@GET
 	@Path("/info")
 	@Produces({ "application/json", "application/xml" })
 	@RestServiceErrorHandler
 	public Response getUser(@QueryParam("username") String username) {
 		return Response.ok(userService.getUser(username)).build();
 	}
 
 	@POST
 	@RolesAllowed({Constant.ADMIN_ROLE, Constant.SUPER_ROLE})
 	@Produces({ "application/json", "application/xml" })
 	@Consumes({ "application/json", "application/xml" })
 	@Path("/add")
 	@RestServiceErrorHandler
 	public Response addNewUser(final AddNewUserRequest request) {
 		final GeneralResponse response = new GeneralResponse();
 		response.setErrorMsg("");
 		response.setSuccess(true);
 		userService.addNewUser(request);
 		response.setTimestamp(Utils.currentTimestamp());
 		return Response.ok(response).build();
 	}
 
 	@POST
	@RolesAllowed({Constant.ADMIN_ROLE, Constant.SUPER_ROLE})
 	@Produces({ "application/json", "application/xml" })
 	@Consumes({ "application/json", "application/xml" })
 	@Path("/resetpwd")
 	@RestServiceErrorHandler
 	public Response resetPassword(final ResetPasswordRequest request) {
 		final GeneralResponse response = new GeneralResponse();
 		response.setErrorMsg("");
 		response.setSuccess(true);
 		userService.resetPassword(request);
 		response.setTimestamp(Utils.currentTimestamp());
 		return Response.ok(response).build();
 	}
 
 	@POST
 	@Path("/logout")
 	public Response logout() {
 	  GetUserInfoResponse user = null;
 	    try {
 	      user = userService.getCurrentUser(req);
 
 	      if (null == user) {
             return Response.status(Status.FORBIDDEN).build();
 	      }
 	      HttpSession session = req.getSession(false);
 
 	      if (null != session) {
 	        session.invalidate();
 	      }
 	    } catch (Exception e) {
 	      return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();
 	    }
 
 	    return Response.status(Status.OK).build();
 	}
 }
