 package se.chalmers.chessfeudserver;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.AsyncContext;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation for handling request from the andriod-app.
  * 
  * @author Simon Almgren
  * 
  *         Copyright (c) 2012 Simon Almgren
  */
 public class DbHandler extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private Connection dbConnection;
 
 	private ResultSet rs;
 	private Statement s;
 	private static HashMap tags;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public DbHandler() {
 		super();
 		try {
 			this.init();
 		} catch (ServletException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Loads the jdbc driver.
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		String loginUser = "root";
 		String loginPw = "awesomeness";
 		String loginURL = "jdbc:mysql://localhost/cfdb";
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			dbConnection = DriverManager.getConnection(loginURL, loginUser,
 					loginPw);
 			s = dbConnection.createStatement();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		tags = new HashMap();
 		tags.put("login", 0);
 		tags.put("newMove", 1);
 		tags.put("getGames", 2);
 		tags.put("getStats", 3);
 		tags.put("addUser", 4);
 		tags.put("incWins", 5);
 		tags.put("incLosses", 6);
 		tags.put("incDraws", 7);
 		tags.put("newGame", 8);
 		tags.put("deleteGame", 9);
 		tags.put("newInquirie", 10);
 		tags.put("userExists", 11);
 		tags.put("getFinishedGames", 12);
 		tags.put("setGameFinished", 13);
 		tags.put("deleteUser", 14);
 
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(final HttpServletRequest request,
 			final HttpServletResponse response) throws ServletException,
 			IOException {
 		final int intrequest = (Integer) tags.get(request.getParameter("tag"));
 		final HashMap<String, String[]> h = (HashMap<String, String[]>) request
 				.getParameterMap();
 		final AsyncContext async = request.startAsync();
 		async.setTimeout(TimeUnit.MINUTES.toMillis(5));
 		async.start(new Runnable() {
 			public void run() {
 				doRequest(h, intrequest, response);
 				async.complete();
 			}
 		});
 	}
 
 	private void doRequest(HashMap<String, String[]> h, int request,
 			HttpServletResponse response) {
 
 		try {
 			response.setContentType("text");
 			ServletOutputStream out = response.getOutputStream();
 			switch (request) {
 			case 0:
 				out.print(authenticate(((String[]) h.get("username"))[0],
 						((String[]) h.get("password"))[0]));
 				break;
 			case 1:
 				newMove(((String[]) h.get("user1"))[0],
 						((String[]) h.get("user2"))[0],
 						((String[]) h.get("board"))[0]);
 				break;
 			case 2:
 				List<String> games = getGamesInProgress(((String[]) h
 						.get("username"))[0]);
 				StringBuilder sb = new StringBuilder();
 				for (String s : games) {
 					sb.append(s);
 					sb.append(";");
 				}
 				out.print(sb.toString());
 				break;
 			case 3:
 				out.print(getStatistics(((String[]) h.get("username"))[0]));
 				break;
 			case 4:
 				out.print(addUser(((String[]) h.get("email"))[0],
 						((String[]) h.get("username"))[0],
 						((String[]) h.get("password"))[0]));
 				break;
 			case 5:
 				incWins(((String[]) h.get("username"))[0]);
 				break;
 			case 6:
 				incLosses(((String[]) h.get("username"))[0]);
 				break;
 			case 7:
 				incDraws(((String[]) h.get("username"))[0]);
 				break;
 			case 8:
 				out.print(newGame(((String[]) h.get("user1"))[0],
 						((String[]) h.get("user2"))[0],
 						((String[]) h.get("board"))[0]));
 				break;
 			case 9:
 				deleteGame(((String[]) h.get("user1"))[0],
 						((String[]) h.get("user2"))[0]);
 				break;
 			case 10:
 				newInquirie(((String[]) h.get("user"))[0],
 						((String[]) h.get("target"))[0]);
 				break;
 			case 11:
 				out.print(userExists(((String[]) h.get("username"))[0]));
 				break;
 			case 12:
 				List<String> finishedGames = getFinishedGames(((String[]) h
 						.get("username"))[0]);
 				StringBuilder strbuilder = new StringBuilder();
 				for (String s : finishedGames) {
 					strbuilder.append(s);
 					strbuilder.append(";");
 				}
 				out.print(strbuilder.toString());
 				break;
 			case 13:
 				setGameFinished(((String[]) h.get("user1"))[0],
 						((String[]) h.get("user2"))[0]);
 				break;
 			case 14:
 				out.print(deleteUser(((String[]) h.get("username"))[0]));
 			}
 
 		} catch (NumberFormatException e) {
 			try {
 				response.getOutputStream().print(false);
 			} catch (IOException e1) {}
 		} catch (SQLException e) {
 			try {
 				response.getOutputStream().print(false);
 			} catch (IOException e1) {}
 		} catch (IOException e) {
 			try {
 				response.getOutputStream().print(false);
 			} catch (IOException e1) {}
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		this.doGet(request, response);
 	}
 
 	/**
 	 * Checks if the user exists in the database and the two passwordhashes
 	 * match.
 	 * 
 	 * @param userName
 	 *            the player logging in
 	 * @param password
 	 *            the password hash.
 	 * @return a boolean true if login sucseeded or false otherwise.
 	 * @throws SQLException
 	 */
 	private boolean authenticate(String userName, String password)
 			throws SQLException {
 		if (userExists(userName)) {
 			rs = s.executeQuery("select password from auth where userName='"
 					+ userName + "'");
 			rs.first();
 			if (rs.getString(1).equals(password)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if the username currently is registred in the database.
 	 * 
 	 * @param userName
 	 * @return
 	 * @throws SQLException
 	 */
 	private boolean userExists(String userName) throws SQLException {
 		rs = s.executeQuery("select count(*) from auth where userName='"
 				+ userName + "'");
 		rs.first();
 		if (rs.getInt(1) > 0) {
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean gameExists(String user1, String user2) throws SQLException {
 		rs = s.executeQuery("select count(*) from game where user1='"+user1+"' and user2='"+user2+"'");
 		rs.first();
 		if (rs.getInt(1) > 0) {
 			return true;
 		}
 		rs = s.executeQuery("select count(*) from game where user1='"+user2+"' and user2='"+user1+"'");
 		rs.first();
 		if(rs.getInt(1) > 0) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Deletes a user from the database, returns false if it didnt work.
 	 * 
 	 * @param userName
 	 * @throws SQLException
 	 */
 	private boolean deleteUser(String userName) throws SQLException {
 		s.executeUpdate("delete from auth where userName='" + userName + "'");
 		s.executeUpdate("delete from statistics where userName='" + userName
 				+ "'");
 		return !userExists(userName);
 	}
 
 	/**
 	 * Checks for games in the database where the user is participating and adds
 	 * them to an arraylist to return.
 	 * 
 	 * @param userName
 	 * @return
 	 * @throws SQLException
 	 */
 	private List<String> getGamesInProgress(String userName)
 			throws SQLException {
 		List<String> games = new ArrayList<String>();
 		rs = s.executeQuery("select * from game where user1='" + userName
 				+ "' or user2='" + userName + "'");
 		while (rs.next()) {
 			games.add(rs.getString(1) + "/" + rs.getString(2) + "/"
 					+ rs.getString(3) + "/" + rs.getString(4) + "/"
 					+ rs.getString(5));
 		}
 		return games;
 	}
 
 	/**
 	 * Checks for games in the table for finishedgames in the database where the
 	 * user is playing and returns an arraylist with them.
 	 * 
 	 * @param userName
 	 * @return an arraylist with all the finished games.
 	 * @throws SQLException
 	 */
 	private List<String> getFinishedGames(String userName) throws SQLException {
 		removeOutdatedGames();
 		List<String> games = new ArrayList<String>();
 		rs = s.executeQuery("select * from finishedgames where user1='"
 				+ userName + "' or user2='" + userName + "'");
 		while (rs.next()) {
 			games.add(rs.getString(1) + "/" + rs.getString(2) + "/"
 					+ rs.getString(3) + "/" + rs.getString(4) + "/"
 					+ rs.getString(5));
 		}
 		return games;
 	}
 
 	/**
 	 * Contacts the database and removes all outdated games.
 	 * 
 	 * @throws SQLException
 	 */
 	private void removeOutdatedGames() throws SQLException {
 		s.executeUpdate("delete from finishedgames where timestamp < (NOW() - INTERVAL 3 DAY)");
 	}
 
 	/**
 	 * Moves a game from the game-database to the finishedgames-db.
 	 * 
 	 * @param user1
 	 *            one of the players.
 	 * @param user2
 	 *            the other player.
 	 * @throws SQLException
 	 */
 	private void setGameFinished(String user1, String user2)
 			throws SQLException {
 		rs = s.executeQuery("select * from game where (user1='" + user1
 				+ "' and user2='" + user2 + "') or (user1='" + user2
 				+ "' and user2='" + user1 + "')");
 		rs.first();
 		s.executeUpdate("insert into finishedgames(user1,user2,board,turns) values('"
 				+ rs.getString(1)
 				+ "','"
 				+ rs.getString(2)
 				+ "','"
 				+ rs.getString(3) + "','" + rs.getString(4) + "')");
 		s.executeUpdate("delete from game where user1='" + user1
 				+ "' and user2='" + user2 + "' or user1='" + user2
 				+ "' and user2='" + user1 + "'");
 
 	}
 
 	/**
 	 * Returns wins losses and draws as well as number of moves done all with a
 	 * / between each other.
 	 * 
 	 * @param userName
 	 * @return
 	 * @throws SQLException
 	 */
 	private String getStatistics(String userName) throws SQLException {
 		rs = s.executeQuery("select * from statistics where username='"
 				+ userName + "'");
 		rs.first();
 		return (rs.getString(2) + "/" + rs.getString(3));
 	}
 
 	/**
 	 * Adds a user to the database.
 	 * 
 	 * @param email
 	 * @param userName
 	 * @param password
 	 * @throws SQLException
 	 */
 	private boolean addUser(String email, String userName, String password)
 			throws SQLException {
 		if (!userExists(userName)) {
 			s.executeUpdate("insert into auth(email, username, password) values('"
 					+ email + "','" + userName + "','" + password + "')");
 			s.executeUpdate("insert into statistics(username, wld, moves) values('"
 					+ userName + "','0/0/0','0')");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Increments wins in the database for the username specified.
 	 * 
 	 * @param userName
 	 * @throws SQLException
 	 */
 	private void incWins(String userName) throws SQLException {
 		rs = s.executeQuery("select wld from statistics where username='"
 				+ userName + "'");
 		rs.first();
 		String[] wld = rs.getString(1).split("/");
 		int w = Integer.parseInt(wld[0]) + 1;
 		s.executeUpdate("update statistics set wld='" + w + "/" + wld[1] + "/"
 				+ wld[2] + "' where username='" + userName + "'");
 	}
 
 	/**
 	 * Increments losses in the database for the username specified.
 	 * 
 	 * @param userName
 	 * @throws SQLException
 	 */
 	private void incLosses(String userName) throws SQLException {
 		rs = s.executeQuery("select wld from statistics where username='"
 				+ userName + "'");
 		rs.first();
 		String[] wld = rs.getString(1).split("/");
 		int l = Integer.parseInt(wld[1]) + 1;
 		s.executeUpdate("update statistics set wld='" + wld[0] + "/" + l + "/"
 				+ wld[2] + "' where username='" + userName + "'");
 	}
 
 	/**
 	 * Increments draws in the database for the username specified.
 	 * 
 	 * @param userName
 	 * @throws SQLException
 	 */
 	private void incDraws(String userName) throws SQLException {
 		rs = s.executeQuery("select wld from statistics where username='"
 				+ userName + "'");
 		rs.first();
 		String[] wld = rs.getString(1).split("/");
 		int d = Integer.parseInt(wld[2]) + 1;
 		s.executeUpdate("update statistics set wld='" + wld[0] + "/" + wld[1]
 				+ "/" + d + "' where username='" + userName + "'");
 	}
 
 	/**
 	 * Adds a new game to the database.
 	 * 
 	 * @param user1
 	 * @param user2
 	 * @param gameBoard
 	 * @throws SQLException
 	 */
 	private boolean newGame(String user1, String user2, String gameBoard)
 			throws SQLException {
 		if (userExists(user2) && !gameExists(user1, user2)) {
 			s.executeUpdate("insert into game(user1, user2, board, turns) values('"
 					+ user1
 					+ "','"
 					+ user2
 					+ "','"
 					+ gameBoard.split("/")[0]
 					+ "','" + "0')");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * A new gameboard is inserted into the database replacing the old one.
 	 * 
 	 * @param user1
 	 * @param user2
 	 * @param newBoard
 	 * @throws SQLException
 	 */
 	private void newMove(String user1, String user2, String newModel)
 			throws SQLException {
 		String[] boardandturns = newModel.split("/");
 		String newBoard = boardandturns[0];
 		int turns = Integer.parseInt(boardandturns[1]);
 		rs = s.executeQuery("select moves from statistics where username='"
 				+ user1 + "'");
 		rs.first();
 		int moves = Integer.parseInt(rs.getString(1));
 		s.executeUpdate("update statistics set moves='" + (moves + 1)
 				+ "' where username='" + user1 + "'");
 		s.executeUpdate("update game set board='" + newBoard
 				+ "' where user1='" + user1 + "' and (user2='" + user2 + "')");
 		s.executeUpdate("update game set board='" + newBoard
 				+ "' where user1='" + user2 + "' and (user2='" + user1 + "')");
 		s.executeUpdate("update game set turns='" + turns + "' where user1='"
 				+ user1 + "' and (user2='" + user2 + "')");
		s.executeUpdate("update game set turns='" + turns + "' where user2='"
				+ user1 + "' and (user2='" + user1 + "')");
 	}
 
 	/**
 	 * Adds a new inquirie to the database.
 	 * 
 	 * @param user
 	 * @param target
 	 * @throws SQLException
 	 */
 	private void newInquirie(String user, String target) throws SQLException {
 		s.executeUpdate("insert into inquiries(user, target, timestamp) values('"
 				+ user + "','" + target + "',CURRENT_TIMESTAMP)");
 	}
 
 	/**
 	 * Deletes a game between two users from the database.
 	 * 
 	 * @param user1
 	 * @param user2
 	 * @throws SQLException
 	 */
 	private void deleteGame(String user1, String user2) throws SQLException {
 		s.executeUpdate("delete from game where user1='" + user1
 				+ "' and user2='" + user2 + "'");
 		s.executeUpdate("delete from game where user1='" + user2
 				+ "' and user2='" + user1 + "'");
 	}
 
 }
 
