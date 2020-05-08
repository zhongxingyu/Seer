 package collabsearch;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Map;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.net.URLDecoder;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import collabsearch.Session.Domain;
 import collabsearch.Session.Domain.Page;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 
 /**
  * 
  * 
  * @author Kamil Olesiejuk
  * 
  */
 @SuppressWarnings("serial")
 public class CollabSearchServlet extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		resp.setContentType("text/plain");
 		String reqType = req.getParameter("type");
 		// Retrieve and return results for given query
 		if (reqType.equals("query")) {
 
 			String q = URLDecoder.decode(req.getParameter("q"), "UTF-8");
 			System.out.println(q);
 
 			makeResponse(resp, q);
 			
 		} else if (reqType.equals("session")) {
 			ObjectMapper mapper = new ObjectMapper();
 			Session session = new Session();
 			try {
 				session = mapper.readValue(
 						URLDecoder.decode(req.getParameter("data"), "UTF-8"),
 						Session.class);
 			} catch (IOException e) {
 				resp.getWriter().println("Error reading session data");
 				// log stack trace
 
 				e.printStackTrace();
 				System.out.println(URLDecoder.decode(req.getParameter("data"),
 						"UTF-8"));
 			}
 
 			String query = session.getQuery();
 			resp.getWriter().println(
 					"Data for '" + query + "' session recieved!");
 			String userid = req.getParameter("userid");
 			storeSessionData(userid, session);
 
 		} else {
 			resp.getWriter().println("Error sending session data");
 		}
 	}
 
 	private void makeResponse(HttpServletResponse resp, String q)
 			throws IOException {
 
 		ObjectMapper mapper = new ObjectMapper();
 		Objectify ofy = ObjectifyService.begin();
 		Iterable<Key<Document>> docKeys = ofy.query(Document.class)
 				.filter("query =", q).fetchKeys();
 
 		System.out.println(docKeys.iterator().hasNext());
 
 		Map<Key<Document>, Document> docMap = ofy.get(docKeys);
 		Result res = new Result(new ArrayList<Document>(docMap.values()));
 		try {
			resp.getWriter().println(mapper.writeValueAsString(res));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void storeSessionData(String uid, Session s) {
 
 		Objectify ofy = ObjectifyService.begin();
 		SearchUser user = new SearchUser(uid);
 		user.save();
 
 		@SuppressWarnings("unused")
 		String userId = s.getUserId();
 		String query = s.getQuery();
 		int sessionTime = s.getTime();
 
 		ArrayList<Domain> domains = s.getDomains();
 
 		for (Domain d : domains) {
 
 			ArrayList<Page> pages = d.getPages();
 
 			for (Page p : pages) {
 				Document doc = new Document(query, p, user, sessionTime);
 
 				Iterable<Key<Document>> docKeys = ofy.query(Document.class)
 						.filter("url =", doc.getUrl()).fetchKeys();
 				if (!docKeys.iterator().hasNext()) {
 					user.addDocument(doc);
 				} else {
 					Key<Document> docKey = docKeys.iterator().next();
 					Document tempD = ofy.get(docKey);
 					if (doc.equals(tempD)) {
 						ofy.delete(docKey);
 					}
 					user.addDocument(doc);
 				}
 			}
 		}
 	}
 }
