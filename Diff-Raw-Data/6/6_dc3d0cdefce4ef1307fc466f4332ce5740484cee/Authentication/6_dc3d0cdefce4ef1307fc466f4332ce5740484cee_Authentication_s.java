 package evalimised.server;
 
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import com.google.appengine.api.rdbms.AppEngineDriver;
 import com.google.cloud.sql.jdbc.Connection;
 import com.google.cloud.sql.jdbc.PreparedStatement;
 import com.sun.xml.internal.bind.v2.model.core.ID;
 
 @Path("/authenticate")
 public class Authentication {
 	@GET
 	@Produces(MediaType.TEXT_PLAIN)
 	public String authenticate(@QueryParam("name") String name){
		
 		String fname = name.split(" ")[0];
 		String lname = name.split(" ")[1];
 		
 		
 		Connection c = null;
 		ArrayList<Integer> ID = new ArrayList<Integer>();
 		try {
 			DriverManager.registerDriver(new AppEngineDriver());
 			c = (Connection) DriverManager.getConnection("jdbc:google:rdbms://valimisrakendus:e-valimised/valimisedDB");
 			String selectUserID = "SELECT ID FROM Isik WHERE Isik.Eesnimi="+"\""+fname+"\""+"&& Isik.Perenimi="+"\""+lname+"\"";
 			String selectUserCandidateID = "SELECT Kandidaat.ID FROM Kandidaat,Isik WHERE Isik.Eesnimi="+"\""+fname+"\""+  "&& Isik.Perenimi="+"\""+lname+"\"" + "&& Kandidaat.IsikID=Isik.ID";
 			ResultSet rs = c.createStatement().executeQuery(selectUserID);
 			ResultSet rs2 = c.createStatement().executeQuery(selectUserCandidateID);
 			
 			while(rs.next()){
 				ID.add(rs.getInt(1));
 			}
 			while(rs2.next()){
 				ID.add(rs2.getInt(1));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}finally {
 			if (c != null) 
 				try {
 					c.close();
 				} catch (SQLException ignore) {
 				}
 		}
 		if(ID.size()==0){
 			return "No_account";
 		}
 		else{
 			return ID.get(0)+";"+ID.get(1);
 		}
 		
 	}
 	
 	
 	@POST
 	@Produces("application/plain")
 	public String createPerson(@FormParam ("name") String name, 
 								@FormParam ("email") String email,
 								@FormParam ("bdate") String bdate
 								){
 		
 		String fname = name.split(" ")[0];
 		String lname = name.split(" ")[1];
 		
 		
 		try {
 			DriverManager.registerDriver(new AppEngineDriver());
 			Connection c = (Connection) DriverManager.getConnection("jdbc:google:rdbms://valimisrakendus:e-valimised/valimisedDB");
 			
 			ResultSet maxId= c.createStatement().executeQuery("SELECT MAX(ID) FROM Isik");
 			maxId.next();
 			
 			String statement_isik ="INSERT INTO Isik VALUES(?,?,?,?,?)";
 			PreparedStatement stmt_isik = (PreparedStatement) c.prepareStatement(statement_isik);
 			stmt_isik.setInt(1, maxId.getInt(1)+1);
 			stmt_isik.setString(2, fname);
 			stmt_isik.setString(3, lname); 
 			stmt_isik.setString(4, email); 
 			stmt_isik.setString(5, bdate);
 			stmt_isik.execute();
 			
 			
 			
 			c.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return "Account created";
 	}
 	
 }
