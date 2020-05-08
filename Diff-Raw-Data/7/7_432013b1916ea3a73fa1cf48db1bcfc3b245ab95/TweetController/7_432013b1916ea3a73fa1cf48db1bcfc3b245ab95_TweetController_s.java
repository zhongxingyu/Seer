 package uk.co.kyleharrison.omc.servlets;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import me.prettyprint.hector.api.exceptions.HectorException;
 
 import uk.co.kyleharrison.omc.connectors.TweetConnector;
 import uk.co.kyleharrison.omc.connectors.UserConnector;
 import uk.co.kyleharrison.omc.model.Session;
import uk.co.kyleharrison.omc.model.CassandraConnection;;
 import uk.co.kyleharrison.omc.stores.TweetStore;
 import uk.co.kyleharrison.omc.stores.UserStore;
 
 /**
  * Servlet implementation class NewPostController
  */
 @WebServlet("/NewPostController")
 public class TweetController extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public TweetController() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		HttpSession session = request.getSession();
 		Session currentUserSession = (Session)session.getAttribute("session");
 		request.setAttribute("Session", currentUserSession);
 		RequestDispatcher rd = getServletContext().getRequestDispatcher("/newpost.jsp");
 		rd.forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		HttpSession session = request.getSession();
 		Session currentUserSession = (Session)session.getAttribute("session");
 		String full_name = currentUserSession.getUsername();
 		String body = request.getParameter("body");
 		String tags = request.getParameter("tags");
 		
 		//PostCreator creator = new PostCreator(full_name, body, tags);
 		
 		// Create Tweet
 		try
 		{
			if (createTweet(full_name,body,tags))
 			{
 				RequestDispatcher rd = getServletContext().getRequestDispatcher("/post_success.jsp");
 				rd.forward(request, response);
 			}
 			else
 			{
 				RequestDispatcher rd = getServletContext().getRequestDispatcher("/post_fail.jsp");
 				rd.forward(request, response);
 			}
 		}catch(HectorException e)
 		{
 			System.out.println("Exception creating post");
 		}
 		
 	}
 	
 	public void GetTweets(HttpServletRequest request, HttpServletResponse response,int Format, String username) throws ServletException, IOException{
 
 		TweetConnector connect = new TweetConnector();
 		List<TweetStore> tweets = connect.getTweets(username);
 		UserConnector connector = new UserConnector();
 		if (tweets == null || tweets.size() < 1) return;
 		
 		UserStore store = connector.getUserByUsername(tweets.get(0).getUser());
 		store = connector.getUserByEmail(store.getEmail());
 		String avatarurl = store.getAvatarUrl();
 		
 		HttpSession session=request.getSession();
 		UserStore sessionUser =(UserStore)session.getAttribute("User");
 		
 		for (TweetStore tweet: tweets)
 		{
 			tweet.setAvatarUrl(avatarurl);
 		}
 		Collections.sort(tweets);
 		
 	}
 
 }
