 package chronomatic.server;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.ResultsetConverter;
 
 import chronomatic.database.Database;
 import chronomatic.database.DatabaseContainer;
 
 @Path("opdrachtgever/")
 public class Opdrachtgever {
 	
 	@Path("create/{sessionKey}/{bedrijfsnaam}/{naam}/{voornaam}/{email}/{telefoonnummer}")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String createOpdrachtgever(@PathParam("sessionKey") String sessionKey,
 										@PathParam("bedrijfsnaam") String bedrijfsnaam, 
 										@PathParam("naam") String naam, 
 										@PathParam("voornaam") String voornaam, 
 										@PathParam("email") String email, 
 										@PathParam("telefoonnummer") String telefoonnummer ){ 
 		
 		Connection con = DatabaseContainer.getConnection();
 		int userID = Authentication.getUserId(sessionKey);
 		
 		String query = "INSERT INTO opdrachtgevers (gebruikers_ID,bedrijfsnaam,naam,voornaam,email,telefoonnummer) VALUES('" + userID + "', '" + bedrijfsnaam + "', '" + naam + "', '" + voornaam + "', '" + email + "', '" + telefoonnummer + "')";
 		
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
 	
 	@Path("read/{sessionKey}/{opdrachtgeverID}")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String getOpdrachtGever(@PathParam("sessionKey") String sessionKey, @PathParam("opdrachtgeverID") int opdrachtgeverID) { 
 		Connection con = DatabaseContainer.getConnection();
 		int userID = Authentication.getUserId(sessionKey);
 		
 		String query = "SELECT bedrijfsnaam, naam, voornaam, email, telefoonnummer, gebruikers_ID FROM opdrachtgevers WHERE ID = " + opdrachtgeverID + " AND gebruikers_ID = " + userID;
 		
 		JSONArray returnObject = new JSONArray();
 		
 		try{
 			if(userID > 0) { 
 				ResultSet rs = Database.executeQuery(con,query);
 				System.out.println(rs);
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
 	
 	@Path("update/{sessionKey}/{bedrijfsnaam}/{naam}/{voornaam}/{email}/{telefoonnummer}")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateOpdrachtgever(@PathParam("sessionKey") String sessionKey,
 										@PathParam("opdrachtgeverID") int opdrachtgeverID,
 										@PathParam("bedrijfsnaam") String bedrijfsnaam, 
 										@PathParam("naam") String naam, 
 										@PathParam("voornaam") String voornaam, 
 										@PathParam("email") String email, 
 										@PathParam("telefoonnummer") String telefoonnummer, 
 										@PathParam("gebruikers_ID") String gebruikers_ID ){ 
 		
 		int userID = Authentication.getUserId(sessionKey);
 		Connection con = DatabaseContainer.getConnection();
 		
 		JSONObject returnObject = new JSONObject();
 		// De sessie is nog geldig.
 		try {
 			if(userID > 0) { 
 				String query = "UPDATE opdrachtgevers SET bedrijfsnaam = '"+bedrijfsnaam+"', email = '" + email + "', naam = '"+naam+"', telefoonnummer = '"+telefoonnummer+"', voornaam = '"+voornaam+ "', gebruikers_ID = '"+gebruikers_ID+"' WHERE ID = "+opdrachtgeverID+" AND gebruikers_ID = "+ userID;
 			
 				if(Database.executeNullQuery(con, query))
 					returnObject.put("success", true);
 				else 
 					returnObject.put("success", false);
 			}
 		}
 		catch(Exception e) { 
 			e.printStackTrace();
 		}
 		
 		return returnObject.toString();
 	}
 	
 	@Path("delete/{sessionKey}/{opdrachtgeverID}")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String destroyOpdrachtGever(@PathParam("sessionKey") String sessionKey, @PathParam("opdrachtgeverID") int opdrachtgeverID) { 
 		Connection con = DatabaseContainer.getConnection();
 		int userID = Authentication.getUserId(sessionKey);
 		
 		JSONObject returnObject = new JSONObject();
 		String query = "DELETE FROM opdrachtgevers WHERE ID = " + opdrachtgeverID + " AND gebruikers_ID = " + userID;
 		try{
 			if(userID > 0) { 
 			
 				if(Database.executeNullQuery(con, query))
 					returnObject.put("success", true);
 				else 
 					returnObject.put("success", false);
 			}
 		}
 		catch(Exception e) { 
 			e.printStackTrace();
 		}
 		return "["+returnObject.toString()+"]";
 	}
 }
