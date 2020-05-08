 package automaatnehindaja;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.text.SimpleDateFormat;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 @WebServlet("/LogsServlet")
 public class LogsServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(LogsServlet.class);
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("application/json");
 
 		JSONObject json = new JSONObject();
 		
 		Connection c = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		String statement;
 		
 		String pattern = "yyyy-MM-dd HH:mm:ss";
 		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
 		
 		try {
 			
 			Class.forName("com.mysql.jdbc.Driver");
 			c = DriverManager.getConnection(
 					"jdbc:mysql://localhost:3306/automaatnehindaja", "ahindaja",
 					"k1rven2gu");
 			
			statement = "SELECT DATED, LEVEL, MESSAGE FROM logs;";
 			stmt = c.prepareStatement(statement);
 			rs = stmt.executeQuery();
 			
 			while (rs.next()) {
				json.append("date", formatter.format(rs.getTimestamp(1)));
 				json.append("level", rs.getString(2));
 				json.append("message", rs.getString(3));
 				
 			}
 			
 			c.close();
 			response.getWriter().write(json.toString());
 			
 		}
 		catch (JSONException e) {
 			logger.debug("JSONEXCEPTION", e);
 		}
 		catch (Exception e) {
 			  System.out.println("Error: " + e.getMessage());
 		}
 	}
 
 }
