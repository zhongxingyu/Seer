 package pl.psnc.dl.wf4ever.auth;
 
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.connection.DlibraConnectionRegistry;
 import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
 import pl.psnc.dlibra.service.AccessDeniedException;
 import pl.psnc.dlibra.service.DLibraException;
 
 import com.sun.jersey.api.container.MappableContainerException;
 import com.sun.jersey.core.util.Base64;
 import com.sun.jersey.spi.container.ContainerRequest;
 import com.sun.jersey.spi.container.ContainerRequestFilter;
 
 public class SecurityFilter
 	implements ContainerRequestFilter
 {
 
 	private final static Logger logger = Logger.getLogger(SecurityFilter.class);
 
 	private static final String REALM = "RO SRS";
 
 	@Context
 	private UriInfo uriInfo;
 
 	@Context
 	private HttpServletRequest httpRequest;
 
 
 	public ContainerRequest filter(ContainerRequest request)
 	{
 		try {
 			DLibraDataSource dLibraDataSource = authenticate(request);
 			httpRequest.setAttribute(Constants.DLIBRA_DATA_SOURCE,
 				dLibraDataSource);
 		}
 		catch (AccessDeniedException e) {
 			throw new MappableContainerException(new AuthenticationException(
 					"Incorrect login/password\r\n", REALM));
 		}
 		catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		}
 		catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 		catch (UnknownHostException e) {
 			throw new RuntimeException(e);
 		}
 		catch (DLibraException e) {
 			throw new RuntimeException(e);
 		}
 
 		return request;
 	}
 
 
 	private DLibraDataSource authenticate(ContainerRequest request)
 		throws MalformedURLException, RemoteException, AccessDeniedException,
 		UnknownHostException, DLibraException
 	{
 		//TODO allow only secure https connections
 		//		logger.info("Connection secure? " + isSecure());
 		logger.info("Request to: " + uriInfo.getAbsolutePath() + " | method:  "
 				+ request.getMethod());
 
 		// Extract authentication credentials
 		String authentication = request
 				.getHeaderValue(ContainerRequest.AUTHORIZATION);
		if (authentication == null) {
			throw new MappableContainerException(new AuthenticationException(
					"Authentication credentials are required\r\n", REALM));
		}
 		if (authentication.startsWith("Basic ")) {
 			authentication = authentication.substring("Basic ".length());
 		}
 		// this is the recommended OAuth 2.0 method
 		else if (authentication.startsWith("Bearer ")) {
 			authentication = authentication.substring("Bearer ".length());
 		}
 		else {
 			throw new MappableContainerException(
 					new AuthenticationException(
 							"Only HTTP Basic and OAuth 2.0 Bearer authentications are supported\r\n",
 							REALM));
 		}
 		String[] values = new String(Base64.base64Decode(authentication))
 				.split(":");
 		if (values.length < 2) {
 			throw new MappableContainerException(new AuthenticationException(
 					"Invalid syntax for username and password\r\n", REALM));
 		}
 		String username = values[0];
 		String password = values[1];
 		if ((username == null) || (password == null)) {
 			throw new MappableContainerException(new AuthenticationException(
 					"Missing username or password\r\n", REALM));
 		}
 
 		logger.debug("Request from user: " + username + " | password: "
 				+ password);
 
 //		// extract workspace name from uri and compare with username
 //		// skip this check if we are adding or deleting workspace
 //		{
 //			// part of path after workspaces/
 //			String a = uriInfo.getPath().substring(
 //				uriInfo.getPath().indexOf("/") + 1);
 //			int idx = a.indexOf("/");
 //			if (idx > 0) {
 //				String workspaceId = a.substring(0, idx);
 //				if (!workspaceId.equals(username)) {
 //					throw new MappableContainerException(
 //							new ForbiddenException("Access denied\r\n"));
 //				}
 //			}
 //		}
 		return DlibraConnectionRegistry.getConnection().getDLibraDataSource(
 			username, password);
 
 	}
 
 
 	public boolean isSecure()
 	{
 		return "https".equals(uriInfo.getRequestUri().getScheme());
 	}
 }
