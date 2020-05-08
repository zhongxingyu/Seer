 package valve;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 
 import org.apache.catalina.Manager;
 import org.apache.catalina.Session;
 import org.apache.catalina.connector.Request;
 import org.apache.catalina.connector.Response;
 import org.apache.catalina.valves.ValveBase;
 import org.apache.log4j.Logger;
 
 /**
  * This valve changes the session ID of an incoming secure request. The session
 * id is only changed once for the first secure request. It does not destroy
  * the previous session, rather it renames it so it is no longer found by that
  * ID.
  * 
  * @author sven mueller
  * @version $Revision:$
  */
 public class SessionFixationValve extends ValveBase {
 
 	private final static Logger LOG = Logger.getLogger(SessionFixationValve.class.getName());
 
 	private static final String SESSION_RENAMED = "session.renamed";
 
 	@Override
 	public void invoke(Request request, Response response) throws IOException, ServletException {
 
 		// get existing session session
 		Session session = request.getSessionInternal();
 
 		// is there already a session?
 		if (session != null && request.getConnector().getSecure() && session.getSession().getAttribute(SESSION_RENAMED) == null) {
 
 			// get manager, needed to rename session
 			Manager manager = request.getContext().getManager();
 
 			String oldSessionId = session.getId();
 
 			// "rename" existing session
 			manager.changeSessionId(session);
 
 			// set session id to request
 			request.changeSessionId(session.getId());
 
 			// mark session as "renamed"
 			session.getSession().setAttribute(SESSION_RENAMED, true);
 
 			if (LOG.isDebugEnabled()) {
 				LOG.debug(String.format("Session renamed from '%s' to '%s'.", oldSessionId, session.getSession().getId()));
 			}
 		}
 
 		getNext().invoke(request, response);
 	}
 }
 
 /*
  * $Log$
 */
