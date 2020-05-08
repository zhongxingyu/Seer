 package eu.liveandgov.wp1.backend;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.UnavailableException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class InspectionServlet
  */
 @WebServlet("/InspectionServlet")
 public class InspectionServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	PostgresqlDatabase db;
     /**
      * @throws UnavailableException 
      * @see HttpServlet#HttpServlet()
      */
     public InspectionServlet() throws UnavailableException {
         super();
         db = new PostgresqlDatabase("myuser", "mypassword");
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String[] points = request.getParameterValues("points[]");
 		System.out.println("---");
 		// 'Point(24.95667 60.16946)'
 		
 		try {
 			PreparedStatement knnQuery = db.connection.prepareStatement("select routecode, s1.kkj2 <-> ST_Transform(ST_GeometryFromText(?,4326),2392) as distance from routes s1 order by s1.kkj2 <-> ST_Transform(ST_GeometryFromText(?,4326),2392) asc limit 10;");
 			Map<String,Integer> counts = new HashMap<String,Integer>();
 			for (String latLon : points) {
 				String lonLat = "POINT(" + latLon.substring(latLon.indexOf(',')+2, latLon.indexOf(')')) 
 						+ " " +  latLon.substring(7, latLon.indexOf(',')) + ")";
 				knnQuery.setString(1, lonLat);
 				knnQuery.setString(2, lonLat);
 				
 				System.out.println(lonLat);
 				// find nearest route:
 				try {
 					ResultSet rs = knnQuery.executeQuery();
 					while (rs.next()) {
 						if(counts.containsKey(rs.getString(1))) {
 							int c = counts.get(rs.getString(1));
 							c++;
 							counts.put(rs.getString(1), c);
 						}
 						else {
 							counts.put(rs.getString(1), 1);
 						}
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			String routcode = "";
 			int max = 0;
 		    Iterator<Entry<String, Integer>> it = counts.entrySet().iterator();
 		    while (it.hasNext()) {
 		    	Entry<String, Integer> pairs = it.next();
 		    	if(pairs.getValue() > max) {
 		    		max = pairs.getValue();
 		    		routcode = pairs.getKey();
 		    	}
 		        it.remove(); // avoids a ConcurrentModificationException
 		    }
 			System.out.println("route: " +routcode + " " +  max);
 			knnQuery.close();
 			
 			Statement s = db.createStatement();
 			try {
 				ResultSet rs = s.executeQuery("SELECT routecode, routedir, ST_AsGeoJSON(ST_MakeLine(ST_Transform(r.kkj2,4326) ORDER BY stoporder)) as route from routes as r group by routecode, routedir having routecode = '"+routcode+"';");
 				response.setContentType("application/json");
 				PrintWriter out = response.getWriter();
 				String json = "{\"routes\":["; 
 				while (rs.next()) {
 					if(json.length() > 11) {
 						json += ",\n";
 					}
 					json += "{";
 					json += "\"routecode\":\""+rs.getString(1)+"\",";
 					json += "\"routedir\":\""+rs.getString(2)+"\",";
 					json += "\"geojson\":"+rs.getString(3);
 					json += "}";
 				}
 				json += "]}";
 				out.println(json);
 				System.out.println(json);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
