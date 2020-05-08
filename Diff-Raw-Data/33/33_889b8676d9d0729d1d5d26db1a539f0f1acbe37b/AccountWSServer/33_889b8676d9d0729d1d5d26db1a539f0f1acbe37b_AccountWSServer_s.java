 package org.bioinfo.gcsa.ws;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.bioinfo.gcsa.lib.account.beans.Bucket;
 import org.bioinfo.gcsa.lib.account.db.AccountManagementException;
 import org.bioinfo.gcsa.lib.account.io.IOManagementException;
 
 @Path("/account/{accountId}")
 public class AccountWSServer extends GenericWSServer {
 	private String accountId;
 
 	public AccountWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest,
 			@DefaultValue("") @PathParam("accountId") String accountId) throws IOException, AccountManagementException {
 		super(uriInfo, httpServletRequest);
 		this.accountId = accountId;
 
 		logger.info("HOST: " + uriInfo.getRequestUri().getHost());
 		logger.info("----------------------------------->");
 	}
 
 	@GET
 	@Path("/create")
 	public Response create(@DefaultValue("") @QueryParam("password") String password,
 			@DefaultValue("") @QueryParam("name") String name, @DefaultValue("") @QueryParam("email") String email) {
 		try {
 			if (accountId.toLowerCase().equals("anonymous")) {
 				cloudSessionManager.createAnonymousAccount(sessionIp);
 			} else {
 				cloudSessionManager.createAccount(accountId, password, name, email, sessionIp);
 			}
 			return createOkResponse("OK");
 		} catch (AccountManagementException | IOManagementException e) {
 			logger.error(e.toString());
 			return createErrorResponse("could not create the account");
 		}
 	}
 
 	@GET
 	@Path("/login")
 	public Response login(@DefaultValue("") @QueryParam("password") String password) {
 		try {
 			String res;
 			if (accountId.toLowerCase().equals("anonymous")) {
 				res = cloudSessionManager.createAnonymousAccount(sessionIp);
 			} else {
 				res = cloudSessionManager.login(accountId, password, sessionIp);
 			}
 			return createOkResponse(res);
 		} catch (AccountManagementException | IOManagementException e) {
 			logger.error(e.toString());
 			return createErrorResponse("could not login");
 		}
 	}
 
 	@GET
 	@Path("/logout")
 	public Response logout() {
 		try {
 			if (accountId.toLowerCase().equals("anonymous")) {
 				cloudSessionManager.logoutAnonymous(sessionId);
 			} else {
 				cloudSessionManager.logout(accountId, sessionId);
 			}
 			return createOkResponse("OK");
 		} catch (AccountManagementException | IOManagementException e) {
 			logger.error(e.toString());
 			return createErrorResponse("could not logout");
 		}
 	}
 
 	@GET
 	@Path("/info")
 	public Response getInfoAccount(@DefaultValue("") @QueryParam("last_activity") String lastActivity) {
 		try {
 			String res = cloudSessionManager.getAccountInfo(accountId, sessionId, lastActivity);
 			return createOkResponse(res);
 		} catch (AccountManagementException e) {
 			logger.error(e.toString());
			return createErrorResponse("could get account information");
 		}
 	}
 
 	@GET
 	@Path("/profile/change_password")
 	public Response changePassword(@DefaultValue("") @QueryParam("old_password") String old_password,
 			@DefaultValue("") @QueryParam("new_password1") String new_password1,
 			@DefaultValue("") @QueryParam("new_password2") String new_password2) {
 		try {
 			cloudSessionManager.changePassword(accountId, sessionId, old_password, new_password1, new_password2);
 			return createOkResponse("OK");
 		} catch (AccountManagementException e) {
 			logger.error(e.toString());
 			return createErrorResponse("could not change password");
 		}
 	}
 
 	@GET
 	@Path("/profile/reset_password")
 	public Response resetPassword(@DefaultValue("") @QueryParam("email") String email) {
 		try {
 			cloudSessionManager.resetPassword(accountId, email);
 			return createOkResponse("OK");
 		} catch (AccountManagementException e) {
 			logger.error(e.toString());
 			return createErrorResponse("could not reset password");
 		}
 	}
 
 	@GET
 	@Path("/profile/change_email")
 	public Response changeEmail(@DefaultValue("") @QueryParam("new_email") String new_email) {
 		try {
 			cloudSessionManager.changeEmail(accountId, sessionId, new_email);
 			return createOkResponse("OK");
 		} catch (AccountManagementException e) {
 			logger.error(e.toString());
 			return createErrorResponse("could not change email");
 		}
 	}
 	
 	
 //	@GET
 //	@Path("/delete/")
 //	public Response deleteAccount() {
 //		try {
 //			cloudSessionManager.deleteAccount(accountId, sessionId);
 //			return createOkResponse("OK");
 //		} catch (AccountManagementException e) {
 //			logger.error(e.toString());
 //			return createErrorResponse("could not delete the account");
 //		}
 //	}
 	
 	
 	
 	
 	//OLD
 
 	// @GET
 	// @Path("/pipetest/{accountId}/{password}") //Pruebas
 	// public Response pipeTest(@PathParam("accountId") String
 	// accountId,@PathParam("password") String password){
 	// return createOkResponse(userManager.testPipe(accountId, password));
 	// }
 
 	// @GET
 	// @Path("/getuserbyaccountid")
 	// public Response getUserByAccountId(@QueryParam("accountid") String
 	// accountId,
 	// @QueryParam("sessionid") String sessionId) {
 	// return createOkResponse(userManager.getUserByAccountId(accountId,
 	// sessionId));
 	// }
 	//
 	// @GET
 	// @Path("/getuserbyemail")
 	// public Response getUserByEmail(@QueryParam("email") String email,
 	// @QueryParam("sessionid") String sessionId) {
 	// return createOkResponse(userManager.getUserByEmail(email, sessionId));
 	// }
 
 	// @GET
 	// @Path("/{accountId}/createproject")
 	// public Response createProject(@PathParam("accountId") String accountId,
 	// @QueryParam("project") Project project, @QueryParam("sessionId") String
 	// sessionId){
 	// return createOkResponse(userManager.createProject(project, accountId,
 	// sessionId));
 	// }
 
 	// @GET
 	// @Path("/createproject/{accountId}/{password}/{accountName}/{email}")
 	// public Response register(@Context HttpServletRequest
 	// httpServletRequest,@PathParam("accountId") String
 	// accountId,@PathParam("password") String
 	// password,@PathParam("accountName") String accountName,
 	// @PathParam("email") String email){
 	// String IPaddr = httpServletRequest.getRemoteAddr().toString();
 	// String timeStamp;
 	// SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
 	// Calendar calendar = Calendar.getInstance();
 	// Date now = calendar.getTime();
 	// timeStamp = sdf.format(now);
 	// Session session = new Session(IPaddr);
 	//
 	// try {
 	// userManager.createUser(accountId,password,accountName,email,session);
 	// } catch (AccountManagementException e) {
 	// return createErrorResponse(e.toString());
 	// }
 	// return createOkResponse("OK");
 	// }
 
 }
