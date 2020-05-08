 /**
  * Author: Fernando Serena (fserena@ciclope.info)
  * Organization: Ciclope Group (UPM)
  * Project: GLORIA
  */
 package eu.gloria.gs.services.api.resources;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import eu.gloria.gs.services.api.data.EmailValidator;
 import eu.gloria.gs.services.api.data.PasswordValidator;
 import eu.gloria.gs.services.api.data.UserDataAdapter;
 import eu.gloria.gs.services.api.data.dbservices.UserDataAdapterException;
 import eu.gloria.gs.services.api.data.dbservices.UserEntry;
 import eu.gloria.gs.services.api.security.SHA1;
 import eu.gloria.gs.services.core.client.GSClientProvider;
 import eu.gloria.gs.services.log.action.LogAction;
 import eu.gloria.gs.services.repository.user.UserRepositoryException;
 import eu.gloria.gs.services.repository.user.UserRepositoryInterface;
 import eu.gloria.gs.services.repository.user.data.UserInformation;
 
 /**
  * @author Fernando Serena (fserena@ciclope.info)
  * 
  */
 
 @Path("/users")
 public class Users extends GResource {
 
 	@Context
 	HttpServletRequest request;
 
 	private static UserDataAdapter userAdapter = (UserDataAdapter) context
 			.getBean("userDataAdapter");
 	private static UserRepositoryInterface users = GSClientProvider
 			.getUserRepositoryClient();
 
 	private boolean validatePassword(String password) {
 
 		// Min 6 max 20.
 		PasswordValidator validator = new PasswordValidator();
 
 		return password != null && validator.validate(password);
 	}
 
 	private boolean validateEmail(String email) {
 
 		EmailValidator validator = new EmailValidator();
 
 		return email != null && validator.validate(email);
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/authenticate")
 	public Response authenticateUser(@QueryParam("verify") boolean verify) {
 
 		if (verify) {
 			return Response.ok(new ArrayList<>()).build();
 		}
 
 		this.setupRegularAuthorization(request);
 
 		String user = this.getUsername(request);
 		String password = this.getPassword(request);
 
 		String userAgent = (String) request.getAttribute("agent");
 		String remote = (String) request.getAttribute("remote");
 		String language = (String) request.getAttribute("language");
 
 		try {
 
 			List<UserEntry> activeSessions = userAdapter
 					.getUserInformation(user);
 
 			if (activeSessions != null) {
 				for (UserEntry entry : activeSessions) {
 					String regRemote = entry.getRemote();
 					String regAgent = entry.getAgent();
 					if (remote.equals(regRemote) && userAgent.equals(regAgent)) {
 						return Response.ok(
 								JSONConverter.toJSON(entry.getToken())).build();
 					} else {
 						userAdapter.deactivateToken(entry.getToken());
 					}
 				}
 			}
 
 			String token = userAdapter.createToken(user, password, language,
 					userAgent, remote);
 			return Response.ok(JSONConverter.toJSON(token)).build();
 		} catch (UserDataAdapterException e) {
 			return Response.serverError().entity(e.getMessage()).build();
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/info")
 	public Response getUserInformation() {
 
 		String user = this.getUsername(request);
 
 		this.setupPublicAuthorization();
 
 		try {
 			UserInformation userInfo = users.getUserInformation(user);
 			return Response.ok(JSONConverter.toJSON(userInfo)).build();
 		} catch (UserRepositoryException e) {
 			return Response.serverError().entity(e.getMessage()).build();
 		}
 	}
 
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/register")
 	public Response registerUser(RegisterUserRequest regInfo) {
 
 		this.setupPublicAuthorization();
 
 		LogAction log = new LogAction();
 		log.put("action", "register");
 		log.put("email", regInfo.getEmail());
 		log.put("alias", regInfo.getAlias());
 
 		try {
 			if (users.containsAlias(regInfo.getAlias())) {
 				log.put("reason", "already exists");
 				log.put("id", "alias");
 				throw new UserDataAdapterException(log);
 			}
 			if (users.containsUser(regInfo.getEmail())) {
 				log.put("reason", "already exists");
 				log.put("id", "email");
 				throw new UserDataAdapterException(log);
 			}
 
 		} catch (UserDataAdapterException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (UserRepositoryException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		}
 
 		log.put("on", "verification");
 
 		boolean validEmail = this.validateEmail(regInfo.getEmail());
 		boolean validPassword = this.validatePassword(regInfo.getPassword());
 
 		if (!validEmail) {
 			return this.processError(Status.NOT_ACCEPTABLE, "validation",
 					"email");
 		}
 
 		if (!validPassword) {
 			return this.processError(Status.NOT_ACCEPTABLE, "validation",
 					"password");
 		}
 
 		try {
 			userAdapter.createClearVerification(regInfo.getAlias(),
 					regInfo.getEmail(), regInfo.getPassword());
 			return this.processSuccess();
 
 		} catch (UserDataAdapterException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (Exception e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR,
 					"verification email", e.getMessage());
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/deactivate")
 	public Response deactivateUser() {
 
 		this.setupPublicAuthorization();
 
 		String username = this.getUsername(request);
 		LogAction log = new LogAction();
 		log.put("action", "deactivate");
 		log.put("email", username);
 
 		try {
 
 			users.deactivateUser(username, this.getPassword(request));
 			return this.processSuccess();
 
 		} catch (UserRepositoryException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		}
 	}
 
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/update")
 	public Response updateUserInfo(RegisterUserRequest upInfo) {
 
 		this.setupRegularAuthorization(request);
 
 		try {
 
 			boolean validPassword = this.validatePassword(upInfo.getPassword());
 			if (validPassword) {
 				String encodedPassword = SHA1.encode(upInfo.getPassword());
 				users.changePassword(this.getUsername(request), encodedPassword);
 
 				userAdapter
 						.deactivateOtherTokens(this.getUsername(request), "");
 
 				return this.processSuccess();
 			} else {
				return this.processError(Status.NOT_ACCEPTABLE, "validation",
						"password");
 			}
 
 		} catch (Exception e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR,
 					"update account", e.getMessage());
 		}
 	}
 
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/reset")
 	public Response resetUser(ResetUserRequest resetInfo) {
 
 		this.setupPublicAuthorization();
 
 		String alias = resetInfo.getAlias();
 		String email = resetInfo.getEmail();
 
 		boolean validEmail = this.validateEmail(email);
 
 		if (!validEmail) {
 			return this.processError(Status.NOT_ACCEPTABLE, "validation",
 					"email");
 		}
 
 		LogAction log = new LogAction();
 		log.put("alias", alias);
 		log.put("email", resetInfo.getEmail());
 
 		try {
 			if (!users.containsUser(email)) {
 				log.put("reason", "user does not exist");
 				throw new UserDataAdapterException(log);
 			}
 
 			UserInformation userInfo = users.getUserInformation(resetInfo
 					.getEmail());
 
 			if (alias == null || userInfo.getAlias() == null
 					|| userInfo.getAlias().equals("")) {
 				alias = userInfo.getName();
 			}
 
 			if (userInfo.getAlias() != null) {
 				alias = userInfo.getAlias();
 			}
 
 			if (!userAdapter.containsVerification(alias, resetInfo.getEmail())) {
 				userAdapter.createEncodedVerification(alias, email,
 						userInfo.getPassword());
 			}
 
 		} catch (UserDataAdapterException e) {
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (UserRepositoryException e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR, e);
 		}
 
 		try {
 			userAdapter.requestReset(alias, resetInfo.getEmail());
 			return this.processSuccess();
 
 		} catch (UserDataAdapterException e) {
 			e.getAction().put("on", "reset");
 			return this.processError(Status.NOT_ACCEPTABLE, e);
 		} catch (Exception e) {
 			return this.processError(Status.INTERNAL_SERVER_ERROR,
 					"reset account", e.getMessage());
 		}
 	}
 }
