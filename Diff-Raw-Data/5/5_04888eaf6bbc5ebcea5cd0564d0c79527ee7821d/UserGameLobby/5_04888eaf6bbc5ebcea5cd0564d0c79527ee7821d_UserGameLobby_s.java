 package Actions;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.SQLFeatureNotSupportedException;
 import java.sql.Statement;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 //Returns GameLobby;#ofPeople;NameOfEachPlayer..;...;
 
 
 @WebServlet(urlPatterns={"/UserGameLobby"})
 public class UserGameLobby extends HttpServlet implements DataSource {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -972529346689447377L;
 
 	Connection connection = null;
 	private String Game =  null;
 	private int GameID = 0;
 	private int NumPlayers = 0;
 
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 		throws ServletException, IOException {
 		if(request.getParameter("Game") != null){ 
 			this.setGame((String) request.getParameter("Game").toString());
 			GameID =  Integer.parseInt(Game);
 		}
 		
 		try {
 		    System.out.println("Loading driver...");
 		    Class.forName("com.mysql.jdbc.Driver");
 		    System.out.println("Driver loaded!");
 		} catch (ClassNotFoundException e) {
 		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
 		}
 		
 		UserGameLobby v = new UserGameLobby();
         try {
 			connection = v.getConnection();
 			System.out.println("connection made");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		PrintWriter out = response.getWriter();
 		if(connection != null){
 			Statement stmt;
 			ResultSet rs;
 			try {
 				stmt = connection.createStatement();
 				//Query to get the numPlayers in a game
				rs = stmt.executeQuery("SELECT Count(PlayerUserID) AS CountOfPlayers FROM tblPlayers GROUP BY PlayerGameID HAVING PlayerGameID="+GameID+";");
 				if(rs.next()){
 					//get number of players
 					NumPlayers = rs.getInt(1);
 				}
 				
 				//Query to get the usernames of players in a game
				rs = stmt.executeQuery("SELECT tblUsers.Username FROM tblUsers INNER JOIN tblPlayers ON tblUsers.UserID = tblPlayers.PlayerUserID WHERE tblPlayers.PlayerGameID="+ GameID +";");
 				if(!rs.isBeforeFirst()){
 					//this situation shouldn't be possible
 					out.println("GameLobby;0;None;-1");
 				}
 				else{
 					out.print("GameLobby;"+ NumPlayers);
 					while(rs.next()){
 						out.print(";" + rs.getString(1));
 					}
 				}
 				rs.close();
 				stmt.close();
 				connection.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		
 		}
 		
 	}
 
 
 
 	private void setGame(String strGame) {
 		// TODO Auto-generated method stub
 		Game = strGame;
 	}
 
 
 
 	@Override
 	public PrintWriter getLogWriter() throws SQLException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public void setLogWriter(PrintWriter out) throws SQLException {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void setLoginTimeout(int seconds) throws SQLException {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public int getLoginTimeout() throws SQLException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 	@Override
 	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public <T> T unwrap(Class<T> iface) throws SQLException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public boolean isWrapperFor(Class<?> iface) throws SQLException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public Connection getConnection() throws SQLException {
         if (connection != null) {
             System.out.println("Cant craete a Connection");
     } else {
             connection = DriverManager.getConnection(
                             "jdbc:mysql://ourdbinstance.cbvrc3frdaal.us-east-1.rds.amazonaws.com:3306/dbAppData", "AWSCards", "Cards9876");
     }
     return connection;
 	}
 
 	@Override
 	public Connection getConnection(String username, String password)
 			throws SQLException {
 		// TODO Auto-generated method stub
         if (connection != null) {
                 System.out.println("Cant create a Connection");
         } else {
                 connection = DriverManager.getConnection(
                                 "jdbc:mysql://ourdbinstance.cbvrc3frdaal.us-east-1.rds.amazonaws.com:3306/dbAppData", username, password);
         }
         return connection;
 	}
 	
 	
 	
 	
 	
 }
