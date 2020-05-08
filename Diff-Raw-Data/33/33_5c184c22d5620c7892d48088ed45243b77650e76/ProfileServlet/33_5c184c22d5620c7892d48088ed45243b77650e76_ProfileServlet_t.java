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
 
 import Accounts.*;
 
     	/**
     	 * Servlet implementation class AcctManagementServlet
     	 */
     	@WebServlet("/ProfileServlet")
     	public class ProfileServlet extends HttpServlet {
     		private static final long serialVersionUID = 1L;
     	    
     	    /**
     	     * @see HttpServlet#HttpServlet()
     	     */
     	    public ProfileServlet() {
     	        super();
     	    }
 
     	    /**
     	     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     	     */
     	    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	    	AccountManager am = (AccountManager) request.getServletContext().getAttribute("accounts");
     	    	MailManager mm = (MailManager) request.getServletContext().getAttribute("mail");
     	    	Account profile = am.getAccount(request.getParameter("user"));
     	    	Account viewer = (Account) request.getSession().getAttribute("account");
    	    	System.out.println(viewer.getId());
     	    	boolean regViewer = (viewer != null);
     	    	boolean selfViewer = (viewer != null) && (viewer.getId() == profile.getId());
     	    	response.setContentType("text/html");
     	    	PrintWriter out = response.getWriter();
     	    	out.println("<head>");
     	    	out.println(HTMLHelper.printCSSLink());
     	    	out.println("</head>");
     	    	
     	    	
     	    	
     	    	out.println(HTMLHelper.printHeader((Account)request.getSession().getAttribute("account")));
     	    	
     	    	if(request.getSession().getAttribute("account") != null) out.println(HTMLHelper.printNewsFeed(am.getAnnouncements()));
     	    	
     	    	out.println(HTMLHelper.contentStart());
     	    	
     	    	//set and print profile title (name)
     	    	String title = profile.getName();
     	    	ArrayList<String> actions = new ArrayList<String>();
     	    	out.println(HTMLHelper.printTitle("", title, actions));
     	    	//done with profile title
     	    	
     	    	//set and print statistics
     	    	title = "Statistics";
     	    	actions.clear();
     	    	actions.add("Quizes Created: " + am.quizesAuthored(profile));
     	    	actions.add("Quizes Taken: " + am.quizesTaken(profile));
     	    	
     	    	String name = (selfViewer) ? "My" : profile.getName() + "'s";
     	    	actions.add("<a class=actionlist href = \"QuizCatalogServlet?&search="+profile.getName()+"&type=creator_id\">"+name+" Quizes</a>");
     	    	if (selfViewer) actions.add("<a class=actionlist href = \"HistoryServlet?user="+profile.getId()+"\">"+name+" History</a>");
     	    	
     	    	out.println(HTMLHelper.printActionList("", title, actions));
     	    	//done printing statistics
     	    	
     	    	//set up achievements and print
     	    	title = "Achievements";
     	    	actions.clear();
     	    	for(String s : profile.getAcheivementKeySet()) {
     	    		if (profile.getAcheivement(s)) actions.add(s);
     	    	}
     	    	out.println(HTMLHelper.printActionList("", title, actions));
     	    	//done with achievements
     	    	
     	    	
     	    	
     	    	if (selfViewer) {
     	    		//let's do mail
     	    		title = "Mail";
     	    		actions.clear();
     	    		int unread = mm.getUnread(viewer);
     	    		String unreadString = "";
     	    		if (unread > 0) unreadString = " ("+unread+")";
     	    		actions.add("<a class=actionlist href = \"MailManagementServlet?&index=inbox&user="+profile.getName()+"\">inbox"+unreadString+"</a>");
     	    		actions.add("<a class=actionlist href = \"MailManagementServlet?&index=outbox&user="+profile.getName()+"\">outbox</a>");
     	    		actions.add("<a class=actionlist href = \"newMessage.jsp?user="+profile.getName()+"\">Compose Mail</a>");
     	    		out.println(HTMLHelper.printActionList("", title, actions));
     	    		//mail done
     	    		
     	    		//set friends
     	    		title = "Friends";
     	    		actions.clear();
     	    		actions.add("<a class=actionlist href = \"FriendManagementServlet\">Friends</a>");
     	    		actions.add("<a class=actionlist href=\"ProfileCatalogServlet\"> Search Users </a>");
     	    		out.println(HTMLHelper.printActionList("", title, actions));
     	    		//done with friends
     	    		
     	    	} else if (regViewer) {
     	    		
     	    		//Actions
     	    		title = "Actions";
     	    		actions.clear();
     	    		actions.add("<a class=actionlist href = \"newMessage.jsp?&user="+viewer.getName()+"&to="+profile.getName()+"\">Send Mail</a>");
     	    		String dummyString = "";
     	    		dummyString = dummyString + ("<form id=\"friend\" action=\"FriendManagementServlet\" method=\"post\">");
     	    		dummyString = dummyString + ("<input type=\"hidden\" name=\"ID\" value=\""+profile.getId()+"\">");
     	    		if (am.isFriend(viewer.getId(), profile.getId())) {
     	    			dummyString = dummyString + ("<input type=\"hidden\" name=\"action\" value=\"delete\">");
     	    			dummyString = dummyString + ("<a class=actionlist href=\"#\" onclick=\"document.getElementById(\'friend\').submit();\"> Remove as friend </a>");
     	    		} else {
     	    			dummyString = dummyString + ("<input type=\"hidden\" name=\"action\" value=\"add\">");
     	    			dummyString = dummyString + ("<a class=actionlist href=\"#\" onclick=\"document.getElementById(\'friend\').submit();\"> Add as friend </a>");
     	    		}
     	    		dummyString = dummyString + ("</form>");
     	    		actions.add(dummyString);
     	    		out.println(HTMLHelper.printActionList("", title, actions));
     	    		//actions done
     	    		
     	    		
     	    		if (viewer.isAdmin()) {
     	    		
     	    			//set admin stuff
     	    			title = "Administration";
     	    			actions.clear();
     	    			dummyString = ("<form id=\"ban\" action=\"AcctManagementServlet\" method=\"post\">");
     	    			dummyString = dummyString +("<input type=\"hidden\" name=\"name\" value=\""+profile.getName()+"\">");
     	    		if (!profile.isBanned()) {
     	    			dummyString = dummyString +("<input type=\"hidden\" name=\"Action\" value=\"Ban\">");
     	    			dummyString = dummyString +("<a class=actionlist href=\"#\" onclick=\"document.getElementById(\'ban\').submit();\"> Ban user</a>");
     	    		} else {
     	    			dummyString = dummyString +("<input type=\"hidden\" name=\"Action\" value=\"Pardon\">");
     	    			dummyString = dummyString +("<a class=actionlist href=\"#\" onclick=\"document.getElementById(\'ban\').submit();\"> Pardon user</a>");
     	    		}
     	    		dummyString = dummyString +("</form>");
     	    		actions.add(dummyString);
     	    		
     	    		dummyString = ("<form id=\"admin\" action=\"AcctManagementServlet\" method=\"post\">");
     	    		dummyString = dummyString +("<input type=\"hidden\" name=\"name\" value=\""+profile.getName()+"\">");
     	    		if (!profile.isAdmin()) {
     	    			dummyString = dummyString +("<input type=\"hidden\" name=\"Action\" value=\"Promote\">");
     	    			dummyString = dummyString +("<a class=actionlist href=\"#\" onclick=\"document.getElementById(\'admin\').submit();\"> Promote user</a>");
     	    		} else {
     	    			dummyString = dummyString +("<input type=\"hidden\" name=\"Action\" value=\"Demote\">");
     	    			dummyString = dummyString +("<a class=actionlist href=\"#\" onclick=\"document.getElementById(\'admin\').submit();\"> Demote user</a>");
     	    		}
     	    		dummyString = dummyString +("</form>");
     	    		actions.add(dummyString);
     	    		out.println(HTMLHelper.printActionList("", title, actions));
     	    		
     	    		}
     	    	}
     	    	out.println(HTMLHelper.contentEnd());
     	    }
 
     	    /**
     	     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     	     */
     	    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	    	doGet(request,response);
     	    }
 }
