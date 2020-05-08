 package chronomatic.server;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import java.security.SecureRandom;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.math.BigInteger;
 import chronomatic.database.*;
 
 import org.json.*;
 
 @Path("auth/")
 public class Authentication {
 	
 	/***
 	 * Check of de session key nog geldig is.
 	 * @param sessionKey
 	 * @return
 	 */
 	@GET
 	@Path("check/{sessionKey}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String getInfo(@PathParam("sessionKey") String sessionKey) { 
 		JSONObject returnObject = new JSONObject();
 		
 		try { 
 			if(getUserId(sessionKey) > 0) {
 				returnObject.put("success"," true");
 				returnObject.put("state"," logged in");
 			}
 			
 		}
 		catch(Exception e) { 
 			e.printStackTrace();
 		}
 		return returnObject.toString();
 	}
 	
 	/**
 	 * Aanmaken van sessie, indien de gebruiker juiste inloggegegevens heeft meegestuurd.
 	 * @param username
 	 * @param password
 	 * @return
 	 * JSON met gebruikersnaam en een sessie ID (random)
 	 */
 	@GET
 	@Path("login/{username}/{password}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String login(@PathParam("username") String username, @PathParam("password") String password) {
 		Connection con = DatabaseContainer.getConnection();
 		JSONObject returnObject = new JSONObject();
 		String query = "SELECT ID,gebruikersnaam FROM gebruikers WHERE gebruikersnaam = '" + username + "' AND passwoord = '" + password + "'";
 		try{
 			ResultSet rs = Database.executeQuery(con, query); 
 			if(rs.next()) {
 				String sessionKey = generateSessionID();
 				
 				long unixTimestamp = System.currentTimeMillis()/1000;
 				String sessionQuery = "INSERT INTO sessies (session_key,time_out,last_activity,begin,gebruiker_ID) VALUES ('" + sessionKey + "',3600," + unixTimestamp + "," + unixTimestamp + ", " + rs.getInt(1) + ")";
 				System.out.println(sessionQuery);
 				String checkedUsername = rs.getString(2);
 				
 				// Opslaan van sessie
 				if(Database.executeNullQuery(con, sessionQuery))  {
 					returnObject.put("username", checkedUsername);
 					returnObject.put("key", sessionKey);
 				}
 				else 
 					returnObject.put("error","Error saving session data");
 			}
 			else { 
 				// Foutieve login
 				returnObject.put("error","Wrong password/username.");
 			}
 		}
 		catch (Exception e){
 			System.out.println(e.toString());
 		}
 		return "[" + returnObject.toString() + "]";
 	}
 	
 	@GET
 	@Path("users/")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String getUsers() { 
 		String query = "SELECT * FROM gebruikers";
 		
 		Connection con = DatabaseContainer.getConnection();
 		
 		try { 
 			ResultSet rs = Database.executeQuery(con, query);
 			return ResultsetConverter.convert(rs).toString();
 		}
 		catch(Exception e) { 
 			System.out.println("Error fetching user table");
 		}
 		return null;
 	}
 	
 	
 	private String generateSessionID() { 
 		SecureRandom random = new SecureRandom();
 		return new BigInteger(130, random).toString(32);
 	}
 	
 	/***
 	 * Retourneert de gebruikersID gekoppelt aan de sessie sleutel. Indien de sleutel niet bestaat of is verlopen wordt er 0 geretourneerd
 	 * @param sessionKey
 	 * @return int gebruiker_ID
 	 */
 	public static int getUserId(String sessionKey) {
 		Connection con = DatabaseContainer.getConnection();
 		
 		String query = "SELECT gebruiker_ID FROM sessies WHERE session_key = '" + sessionKey + "'";
 		
 		try {
 			ResultSet rs = Database.executeQuery(con, query);
 			if(rs.next()) 
 				return rs.getInt(1);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return 0;
 	}
 }
