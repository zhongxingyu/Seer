 package servlets;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.*;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import quiz.*;
 import user.*;
 import database.*;
 
 
 
 /**
  * Servlet implementation class UserServlet
  */
 @WebServlet("/UserProfileServlet")
 public class UserProfileServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public UserProfileServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Connection conn = MyDB.getConnection();
 		
 		String user =  request.getParameter("username");
 		boolean sameUser = false;
 		User curUser = (User) request.getSession().getAttribute("user");
 		if (curUser.getName().equals(user)) {
 			sameUser = true;
 			request.setAttribute("friend_status", 0); //this is for your own page
 		}
 		
 		
 		try {
 			Statement stmt = conn.createStatement();
 			String query = "SELECT * FROM User WHERE username='" + user + "';";
 			ResultSet rs = stmt.executeQuery(query);
 			RequestDispatcher dispatch;
 			
 			if (rs.next()) {
 				String[] attrs = DataBaseObject.getRow(rs, QuizConstants.USER_N_COLUMNS);
 				User usr = new User(attrs, conn);
 				request.getSession().setAttribute("user", usr);
 				
 				if (!sameUser)
 					setFriendStatus(usr, curUser, request);
 				
 				HomePageQueries.getUserRecentQuizzes(request, conn, usr);
 				HomePageQueries.getAuthoring(request, conn, usr);
 				getActivity(usr, request);
 				
				dispatch = request.getRequestDispatcher("profile.jsp");
 				dispatch.forward(request, response);
 			}
 			
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		
 		//list of recent activities
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 	
 	private void setFriendStatus(User profile, User curUser, HttpServletRequest request) {
 		Connection conn = MyDB.getConnection();	
 		
 		try {
 			Statement stmt = conn.createStatement();
 			String query = "SELECT * FROM Message inner join Friend_Request on Message.id=Friend_Request.id" +
						" WHERE recipient='" + profile.getName() + "' AND sender='" + curUser.getName() + "';";
 			ResultSet rs = stmt.executeQuery(query);
 			if (rs.next()) {
 				request.setAttribute("friend_status", 1); //you sent a friend request to this person
 				return;
 			}
 			
 			query = "SELECT * FROM Message inner join Friend_Request on Message.id=Friend_Request.id" +
			" WHERE recipient='" + curUser.getName() + "' AND sender='" + profile.getName() + "';";
 			rs = stmt.executeQuery(query);
 			if (rs.next()) {
 				request.setAttribute("friend_status", 2); //this person sent a friend request to you
 				return;
 			}
 			request.setAttribute("friend_status", 3); //no friend relationship with this person
 			
 			
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void getActivity(User profile, HttpServletRequest request) {
 		Connection conn = MyDB.getConnection();	
 		List<Activity> activities = new ArrayList<Activity>();
 		
 		try {
 			Statement stmt = conn.createStatement();
 			String query = "SELECT * FROM Quiz WHERE author='" + profile.getName() + "';";
 			ResultSet rs = stmt.executeQuery(query);
 			
 			while (rs.next()) {
 				String[] attrs = DataBaseObject.getRow(rs, QuizConstants.QUIZ_N_COLS); //
 				Quiz quiz = new Quiz(attrs, conn);
 				activities.add(new Activity(profile, profile.getName() + " authored a quiz: " + quiz.getName(), rs.getInt("id")));
 			}
 			
 			query = "SELECT * FROM Quiz_Result WHERE username='" + profile.getName() + "';";
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				activities.add(new Activity(profile, profile.getName() + " took a quiz.", rs.getInt("quiz_id")));
 			}
 			
 			request.setAttribute("activities", activities); 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
