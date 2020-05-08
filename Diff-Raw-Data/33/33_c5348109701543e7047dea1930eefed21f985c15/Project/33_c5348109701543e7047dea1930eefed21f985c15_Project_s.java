 package chronomatic.server;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
 import org.json.JSONArray;
 import org.json.ResultsetConverter;
 
 import chronomatic.database.Database;
 import chronomatic.database.DatabaseContainer;
 
 @Path("project/")
 public class Project {
 	
 	@GET
 	@Path("create/{sessionKey}/{projectName}/{startDate}/{endDate}/{opdrachtgeversID}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String create(@PathParam("sessionKey") String sessionKey,@PathParam("projectName") String projectName,@PathParam("startDate") int startDate,@PathParam("endDate") int endDate, @PathParam("opdrachtgeversID") int opdrachtgeversID) 
 	{ 
 		Connection con = DatabaseContainer.getConnection();
 		
 		int userID = Authentication.getUserId(sessionKey);
 		
 		String query = "INSERT INTO projecten (naam,start_datum,eind_datum,gebruikers_ID,opdrachtgevers_ID) VALUES('" + projectName + "', " + startDate + "," + endDate + "," + userID + "," + opdrachtgeversID + ")";
 		
 		JSONArray returnObject = new JSONArray();
 		try{
 			if(userID > 0) { 
 				ResultSet rs = Database.executeGetQuery(con,query);
 				returnObject = ResultsetConverter.convert(rs);
 			}
 			else{ 
 				JSONObject error = new JSONObject();
 				error.put("error","Session is not valid");
 				returnObject.put(error);
 			}
 		}
 		catch(Exception e) { 
 			e.printStackTrace();
 		}
 		return returnObject.toString();
 	}
 	
 	@GET
 	@Path("read/{sessionKey}/{projectID}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String read(@PathParam("sessionKey") String sessionKey,@PathParam("projectID") int projectID) { 
 		Connection con = DatabaseContainer.getConnection();
 		
 		int userID = Authentication.getUserId(sessionKey);
 		
 		String query = "SELECT naam,start_datum,eind_datum,gebruikers_ID,opdrachtgevers_ID FROM projecten WHERE ID = " + projectID + " AND gebruikers_ID = " + userID;
 		
 		JSONObject error = new JSONObject();
 		JSONArray returnObject = new JSONArray();
 		try {					
 			ResultSet rs = Database.executeQuery(con, query);
 			if(rs.next()) { 
 				if(rs.getInt(4) == userID) {
 					rs.previous();
 					returnObject = ResultsetConverter.convert(rs);
 				}
 				else
 				{
 					error.put("error","This project does not belong to you.");
 					returnObject.put(error);
 				}			
 			}
 			else {
 				error.put("error","This project does not exist (anymore)");
 				returnObject.put(error);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return returnObject.toString();
 	}
 	
 	@GET
 	@Path("update/{sessionKey}/{projectID}/{projectName}/{startDate}/{endDate}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String update(@PathParam("sessionKey") String sessionKey,@PathParam("projectID") int projectID,@PathParam("projectName") String projectName,@PathParam("startDate") int startDate,@PathParam("endDate") int endDate,@PathParam("opdrachtgeversID") int opdrachtgeversID) 
 	{ 
 		Connection con = DatabaseContainer.getConnection();
 		int userID = Authentication.getUserId(sessionKey);
 		String query = "UPDATE projecten SET eind_datum = "+endDate+", naam = '" + projectName + "', start_datum = "+startDate+" WHERE ID = "+projectID+" AND gebruikers_ID = " + userID;
 		System.out.println(query);
 		JSONObject returnObject = new JSONObject();
 		try {
 			returnObject.put("result", Database.executeNullQuery(con, query));
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}		
 		return returnObject.toString();
 	}
 	
 	@GET
 	@Path("delete/{sessionKey}/{projectID}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String destroy(@PathParam("sessionKey") String sessionKey,@PathParam("projectID") int projectID) { 
 		Connection con = DatabaseContainer.getConnection();
 		
 		int userID = Authentication.getUserId(sessionKey);
 		
 		String query = "DELETE FROM projecten WHERE ID = " + projectID + " AND gebruiker_ID = " + userID;
 		
 		JSONObject error = new JSONObject();
 		JSONArray returnObject = new JSONArray();
 		
 		try {					
 			if(userID > 0) 
 				error.put("result", Database.executeNullQuery(con, query));
 			else 
 				error.put("error", "Invalid session");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		returnObject.put(error);
 		return returnObject.toString();
 	}
 	
 	@GET
 	@Path("tasks/{sessionKey}/{projectID}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String getTasks(@PathParam("sessionKey") String sessionKey, @PathParam("projectID") int projectID) 
 	{ 
 		Connection con = DatabaseContainer.getConnection();
 		
 		int userID = Authentication.getUserId(sessionKey);
 		
 		String query = "SELECT T.ID,begin_tijd,verwacht_eind,commentaar,voltooid, T.naam FROM taken AS T INNER JOIN projecten AS P ON T.projecten_ID = P.ID AND P.gebruikers_ID = " + userID + " AND P.ID = " + projectID;
 		JSONArray returnObject = new JSONArray();
 		try {
 			if(userID > 0) 
 			{
 				ResultSet rs = Database.executeQuery(con, query);
 				
 				returnObject = ResultsetConverter.convert(rs);
 			}
 			else { 
 				JSONObject error = new JSONObject();
 				error.put("error","The session key is invalid");
 			}
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return returnObject.toString();
 	}
 	
 	@GET 
 	@Path("opdrachtgever/{sessionKey}/{projectID}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String getOpdrachtGever(@PathParam("sessionKey") String sessionKey, @PathParam("projectID") int projectID) { 
 		Connection con = DatabaseContainer.getConnection();
 		int userID = Authentication.getUserId(sessionKey);
 		
 		String query = "SELECT bedrijfsnaam, email,O.ID,O.naam,telefoonnummer,voornaam FROM opdrachtgevers AS O INNER JOIN projecten AS P ON P.ID = " + projectID + " AND P.opdrachtgevers_ID = O.ID AND O.gebruikers_ID = " + userID;
 		
 		JSONArray returnObject = new JSONArray();
 		
 		try{
 			if(userID > 0) { 
 				ResultSet rs = Database.executeQuery(con,query);
 				
 				returnObject = ResultsetConverter.convert(rs);
 			}
 			else{ 
 				JSONObject error = new JSONObject();
 				error.put("error","Session is not valid");
 				returnObject.put(error);
 			}
 		}
 		catch(Exception e) { 
 			e.printStackTrace();
 		}
 		return returnObject.toString();
 	}
 	
 }
