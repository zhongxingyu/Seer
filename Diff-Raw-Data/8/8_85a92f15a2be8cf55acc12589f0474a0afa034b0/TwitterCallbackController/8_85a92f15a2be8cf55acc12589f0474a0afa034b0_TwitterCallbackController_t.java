 /**
  *This is a controller class designed to handle callback requests comming from the twitter login api
  * 
  * Author: Ignacio Rodriguez
  * 
  * Based off the sample code found at https://github.com/yusuke/sign-in-with-twitter
  */
 
 package Controller;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 import Beans.User;
 
 public class TwitterCallbackController extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
         Twitter twitter = (Twitter)request.getSession().getAttribute("twitter");
 		RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
 		HttpSession session = request.getSession();
 		String verifier = request.getParameter("oauth_verifier");
 		try{
 			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
 			setupUser(accessToken, session);
 			session.removeAttribute("requestToken");
			session.setAttribute("loggedin", true);
 		}catch (TwitterException e) {
             throw new ServletException(e);
         }
         response.sendRedirect(request.getContextPath() + "/usr_home.html");
 	}
 
 	/**
 	 * A helper method to set the user id in the session and if necessary add the use to the database.
 	 * 
 	 * @param accessToken - The user's access token from twitter
 	 * @param session - The current session 
 	 */
 	private void setupUser(AccessToken accessToken, HttpSession session) {
 		String userName = accessToken.getScreenName();
 		try {
 			User user = new User();
 			int userid = user.addUser(userName);
 			session.setAttribute("userid", userid);
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 }
