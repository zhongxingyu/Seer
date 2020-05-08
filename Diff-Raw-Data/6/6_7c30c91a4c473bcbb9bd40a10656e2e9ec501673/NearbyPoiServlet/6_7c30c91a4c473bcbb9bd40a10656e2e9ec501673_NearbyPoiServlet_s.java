 package com.nearme;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 /**
  * This servlet responds to HTTP GET requests of the form
  * 
  * http://server/nearme/pois/LAT/LONG/RADIUS
  * 
  * where
  * 
  * LAT is a latitude
  * LONG is a longitude
  * RADIUS is a radius (in metres)
  * 
  * and returns a JSON data structure with a list of Points of Interest
  * 
  * @author twhume
  *
  */
 
 public class NearbyPoiServlet extends GenericNearMeServlet {
 
 	private static final long serialVersionUID = 4851880984536596503L; // Having this stops Eclipse moaning at us
 
 	private static Logger logger = Logger.getLogger(NearbyPoiServlet.class);
 	
 	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
 		res.setContentType("application/json");
		logger.debug("url=" + req.getPathInfo() + "?" + req.getQueryString());
		PoiQuery pq = new PoiQuery(req.getPathInfo() + "?" + req.getQueryString());
 
 		try {
 			PoiFinder pf = new DatabasePoiFinder(datasource);
 			UserDAO uf = new UserDAOImpl(datasource);
 	
 			/* Look up the user, and set their last known position to be the one they've supplied.
 			 * If we don't know them, make a new record for them.
 			 */
 			
 			User u = uf.readByDeviceId(pq.getAndroidId());
 			if (u==null) {
 				u = new User();
 				u.setDeviceId(pq.getAndroidId());
 				u.setMsisdnHash("unknown");
 				u = uf.write(u);
 			}
 			Position currentPos = new Position(pq.getLatitude(), pq.getLongitude());
 			currentPos.setWhen(new Date());
 			u.setLastPosition(currentPos);
 			uf.write(u);
 
 			/* Get a list of all nearby points of interest and add in nearby friends */
 
 			List<Poi> points = new ArrayList<Poi>();
 			logger.debug("types="+pq.getTypes());
 			if (pq.getTypes().size()>0) points.addAll(pf.find(pq));
 			logger.info("found " + points.size() + " POIs for user " + u.getId() + " within " + pq.getRadius() + " of (" + pq.getLatitude() + "," + pq.getLongitude() + ")");
 			List<Poi> friends = uf.getNearestUsers(u, pq.getRadius());
 			logger.info("found " + friends.size() + " friends for user " + u.getId() + " within " + pq.getRadius() + " of (" + pq.getLatitude() + "," + pq.getLongitude() + ")");
 			points.addAll(friends);
 			
 			/* Use GSon to serialise this list onto a JSON structure, and send it to the client.
 			 * This is a little bit complicated because we're asking it to serialise a list of stuff;
 			 * see the manual at https://sites.google.com/site/gson/gson-user-guide#TOC-Collections-Examples
 			 * if you /really/ want to find out why...
 			 */
 			
 			Gson gson = new Gson();
 			Type listOfPois = (Type) (new TypeToken<List<Poi>>(){}).getType();
 			res.getOutputStream().print(gson.toJson(points, listOfPois));
 			logger.debug("delivered POIs OK");
 		} catch (SQLException e) {
 			logger.error(e);
 			e.printStackTrace();
 			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 		
 }
