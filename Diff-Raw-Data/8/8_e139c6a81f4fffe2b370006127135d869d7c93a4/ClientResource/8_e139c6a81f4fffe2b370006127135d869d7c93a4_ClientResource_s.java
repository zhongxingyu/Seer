 package ca.wasabistudio.chat.rs;
 
 import java.util.List;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import ca.wasabistudio.chat.connector.Connector;
 import ca.wasabistudio.chat.entity.Client;
 import ca.wasabistudio.chat.repo.ClientRepository;
 import ca.wasabistudio.chat.support.RequestErrorException;
 import ca.wasabistudio.chat.support.Session;
 import ca.wasabistudio.chat.support.SessionException;
 
 /**
  * REST services for working with users of the chat system.
  *
  * @author wasabi
  */
 @Path("/client")
 public class ClientResource {
 
 	@Autowired
 	private ClientRepository clientRepo;
 
 	private Connector connector;
 	private Session session;
 
 	public void setConnector(Connector connector) {
 		this.connector = connector;
 	}
 
 	public void setSession(Session session) {
 		this.session = session;
 	}
 
 	/**
 	 * Get the current session id.
 	 *
 	 * @param request current HttpServletRequest instance
 	 * @return current session id
 	 */
 	@GET
 	@Path("/session")
 	@Produces("application/json")
 	public String getSession(@Context HttpServletRequest request) {
 		return request.getSession().getId();
 	}
 
 	/**
 	 * Allow a user to enter a room.
 	 *
 	 * @param sessionId forums sessionId of the user
 	 * @param config current ServletConfig instance
 	 * @param request current HttpServletRequest instance
 	 * @return forums username of the user
 	 */
 	@POST
 	@Path("/enter/{sessionId}")
	@Produces("text/plain")
 	@Transactional
	public String enter(@PathParam("sessionId") String sessionId,
 			@Context ServletConfig config,
 			@Context HttpServletRequest request) {
 		if ((sessionId == null) || "".equals(sessionId)) {
 			String message = "Session id cannot be empty.";
 			throw new RequestErrorException(message);
 		}
 
 		boolean production = isProductionMode(config);
 		String username = sessionId;
 		if (production) {
 			if (!connector.validateSession(sessionId, getIp(request))) {
 				throw new SessionException("Unable to enter.");
 			}
 
 			username = connector.getUsername(sessionId);
 		}
 
 		// remove the client if one already exists
 		removeClient(username);
 
 		Client client = new Client(username);
 		client.setSessionId(production ? sessionId : null);
 		client.setChatSessionId(request.getSession().getId());
 		client.sync();
 		clientRepo.save(client);
 		session.setClient(client);
		return username;
 	}
 
 	@Transactional(propagation=Propagation.REQUIRES_NEW)
 	private void removeClient(String username) {
 		Client client = clientRepo.findOne(username);;
 		if (client != null) {
 			client.exitAllRooms();
 			clientRepo.delete(client);
 		}
 	}
 
 	/**
 	 * Get all clients currently in the chat room.
 	 *
 	 * @return all clients currently in the chat room
 	 */
 	@GET
 	@Path("/list")
 	@Produces("application/json")
 	public List<Client> getClients() {
 		return clientRepo.findAll();
 	}
 
 	private String getIp(HttpServletRequest request) {
 		return request.getRemoteAddr();
 	}
 
 	private boolean isProductionMode(ServletConfig config) {
 		if (config == null) {
 			return false;
 		}
 		String mode = config.getServletContext().getInitParameter("mode");
 		return "PRODUCTION".equals(mode);
 	}
 
 }
