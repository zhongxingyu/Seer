 package Servlets;
 
 import helpers.HTMLHelper;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
     	import Accounts.Account;
 import Accounts.AccountManager;
 
     	/**
     	 * Servlet implementation class AcctManagementServlet
     	 */
     	@WebServlet("/FriendManagementServlet")
     	public class FriendManagementServlet extends HttpServlet {
     		private static final long serialVersionUID = 1L;
     	    
     	    /**
     	     * @see HttpServlet#HttpServlet()
     	     */
     	    public FriendManagementServlet() {
     	        super();
     	    }
 
     	    /**
     	     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     	     */
     	    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	    	Account user = (Account) request.getSession().getAttribute("account");
     	    	AccountManager am = (AccountManager) request.getServletContext().getAttribute("accounts");
     	    	ArrayList<Account> requests = am.getFriendRequests(user.getId());
     	    	ArrayList<Account> friends = am.getFriends(user.getId());
     	    	response.setContentType("text/html");
     	    	PrintWriter out = response.getWriter();
     	    	out.println("<head>");
     	    	out.println(HTMLHelper.printCSSLink());
     	    	out.println("/<head>");
     	    	out.println("<body>");
     	    	out.println(HTMLHelper.printHeader((Account)request.getSession().getAttribute("account")));
     	    	
     			if(request.getSession().getAttribute("account") != null) out.println(HTMLHelper.printNewsFeed(am.getAnnouncements()));
     	    	
     	    	out.println(HTMLHelper.contentStart());
     	    	
     	    	out.println("<h3>Requests</h3>");  
     	    	out.println("<ul class=boxlisting>");
     	    	ArrayList<String> actions = new ArrayList<String>();
     	    	String dummyString = "";
     	    	for (Account a : requests) {
     	    		actions.clear();
     	    		dummyString = ("<form action=\"FriendManagementServlet\" method=\"post\">");
     	    		dummyString= dummyString +("<input type=\"hidden\" name=\"ID\" value=\""+a.getId()+"\">");
     	    		dummyString = dummyString + ("<input type=\"submit\"  name=\"action\" value=\"add\">");
     	    		actions.add(dummyString);
     	    		dummyString = ("<input type=\"submit\" style=\"background-color:red\" name=\"action\" value=\"delete\">");
     	    		dummyString = dummyString + ("</form>");
     	    		actions.add(dummyString);
     	    		out.println(HTMLHelper.printEnhancedUserListing(a.getName(), actions));
     	    	}
     	    	
     	    	out.println("</ul>");
     	    	out.println(HTMLHelper.contentEnd());
     	    	
     	    	out.println(HTMLHelper.contentStart());
     	    	out.println("<h3>Friends</h3>");
     	    	out.println("<ul class=boxlisting>");
     	    	
     	    	for (Account a : friends) {
     	    		actions.clear();
        	    		dummyString = ("<form id =\"friend\" action=\"FriendManagementServlet\" method=\"post\">");
        	    		dummyString= dummyString +("<input type=\"hidden\" name=\"ID\" value=\""+a.getId()+"\">");
        	    		dummyString= dummyString +("<input type=\"hidden\" name=\"action\" value=\"delete\">");
        	    		dummyString= dummyString +("<a class=quiz style=\"float:left; background-color:red\" href=\"#\" onclick=\"document.getElementById(\'friend\').submit();\"> Remove </a>");
        	    		dummyString= dummyString +("</form>");
        	    		actions.add(dummyString);
        	    		out.println(HTMLHelper.printEnhancedUserListing(a.getName(), actions));
     	    	}
     	    	out.println("</ul>");
 	    		
 	    		
     	    	out.println("<form action=\"ProfileCatalogServlet\" method=\"get\">");
     	    	out.println("Search Users: <input type=\"text\" name=\"search\"/>");
     	    	out.println("<input type=\"submit\" value=\"Search\"/>");
     			out.println("</form>");
 	    		out.println(HTMLHelper.contentEnd());
     	    	out.println("</body>");
     	    }
 
     	    /**
     	     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     	     */
     	    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	    	String action = request.getParameter("action");
     	    	int friendID = Integer.parseInt(request.getParameter("ID"));
     	    	AccountManager am = (AccountManager) request.getServletContext().getAttribute("accounts");
    	    	Account user = (Account) request.getSession().getAttribute("account");
    	    	int myID = user.getId();
    	    	String name = user.getName();
     	    	if (action.equals("delete")) {
     	    		am.deleteFriend(myID, friendID);
     	    	} else if (action.equals("add")) {
     	    		System.out.println(friendID + " " + myID);
     	    		am.makeFriend(myID, friendID);
     	    	}
    	    	request.getRequestDispatcher("/ProfileServlet?user="+name).forward(request, response);
     	    }
 }
