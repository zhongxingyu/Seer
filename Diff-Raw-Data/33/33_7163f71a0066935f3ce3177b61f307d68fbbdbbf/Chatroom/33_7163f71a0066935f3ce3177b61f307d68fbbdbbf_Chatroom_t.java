 package main;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import encryption.DHServlet;
 
 import objects.Message;
 import objects.User;
 
 //Not a servlet. Uses the Dispatcher to receive info
 public class Chatroom {
 
 	// Commands
 	private static final String SEND_MSG = "SEND_MESSAGE";
 	private static final String NAME = "NAME";
 	// To tell client that there is nothing to receive (header)
 	private static final String NOTHING_NEW = "NOTHING_NEW";
 	// POST parameter to show message length
 	private static final String AMOUNT = "AMOUNT";
 
 	public String id;
 	public LinkedList<User> userList = new LinkedList<User>();
 
 	public LinkedList<Message> msgList = new LinkedList<Message>();
 
 	// True if ready to begin DHKE (right after received message length)
 	public boolean DHready = false;
 
 	private DHServlet dhEngine;
 
	//User representation
	private User instance;
	
 	public Chatroom(String id) {
 		this.id = id;
 		Dispatcher.addChatroom(this);
		instance = new User("Server", id);
 	}
 
 	public enum Type {
 		POST, GET
 	}
 
 	// Will have to deal with PullThread's GET Requests
 	public void process(Type t, HttpServletRequest request,
 			HttpServletResponse response) throws IOException {
 		// Receives this from dispatcher servlet
 		if (t.equals(Type.POST)) {
 			if (Functions.contains(request.getParameterNames(), AMOUNT)) {
 				int amount = Integer.parseInt(request
 						.getParameter(AMOUNT));
 
 				System.out.println(getUser(request.getParameter(NAME)).name
 						+ " WANTS TO GENERATE " + amount + " DH KEYS");
 
 				// Ready to perform DHKE
 				// When the Threads call now, the clients will be notified
 				dhEngine = new DHServlet(amount, this);
 				DHready = true;
 			} else if (Functions
 					.contains(request.getParameterNames(), SEND_MSG)) {
 				User u = getUser(request.getParameter(NAME));
 				String s = request.getParameter("ENCRYPTED");
 				Message m = new Message(request.getParameter(SEND_MSG), u, s.equalsIgnoreCase("TRUE"));
 				msgList.add(m);
 				System.out.println("Received message: " + m.message);
 				response.getWriter().println(
 						"SERVLET HAS RECEIVED MESSAGE, " + u.name);
 			}
 		} else if (t.equals(Type.GET)) {
 
 			// Called only when the Thread pulls
 			if (Functions.has(request, "PULL", "true")) {
 				User u = getUser(request.getHeader(NAME));
 				PrintWriter out = response.getWriter();
 
 				if (DHready) {
 					out.println("DHKE_READY: (" + dhEngine.amount + ")");
 					out.close();
 					return;
 				}
 
 				boolean sent = false;
 				for (Message m : msgList) {
 					// If the message has not yet been received, and the
 					// PullThread does NOT belong to the client that sent the
 					// message...
 					if (!m.received && !(m.sender.equals(u))) {
 						System.out.println("SENT MESSAGE TO CLIENT");
 						out.print("(Message: " + m.message + ")");
 						out.print("(Encrypted: " + (m.encrypted ? "TRUE)" : "FALSE)"));
 						out.print("(Sender: " + m.sender.name + ")");
 						out.print("(Date: " + m.date + ")");
 						out.print("(Time: " + m.time + ")");
 
 						// Creates a new row to aid parsing
 						out.println();
 
 						m.received = true;
 						System.out.println("SENT MESSAGE \"" + m.message
 								+ "\" TO USER \"" + u.name + "\"");
 						sent = true;
 					}
 				}
 
 				if (!sent) {
 					// Nothing to be pulled!
 					out.print(NOTHING_NEW);
 				}
 			}
 		}
 	}
 
 	// Diffie-Hellman processing
 	public void dhProcess(Type t, HttpServletRequest request,
 			HttpServletResponse response) {
 		
 		String s = "";
 		if (t.equals(Type.GET)) {
 			// This is a request
 			// Either for Mod, Base or Other client's computed value
 			s = dhEngine.processGet(request);
 		} else if (t.equals(Type.POST)) {
 			s = dhEngine.processPost(request);
 		}
 		
 		if (s.equals("")){
 			s = "ERROR";
 		}
 		
 		try {
 			System.out.println("RETURNED: " + s);
 			response.getWriter().print(s);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public User getUser(String userID) {
 		for (User u : userList) {
 			if (u.name.equalsIgnoreCase(userID)) {
 				return u;
 			}
 		}
 		return null;
 	}
 
 	public boolean hasUser(String userID) {
 		for (User u : userList) {
 			if (u.name.equalsIgnoreCase(userID)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void addUser(User user) {
 		userList.add(user);
		msgList.add(new Message(user.name + " has joined the chatroom!", instance, false));
 
 		System.out.println("USER \"" + user.name
 				+ "\" HAS BEEN ADDED TO CHATROOM \"" + id + "\"");
 	}
 
 	//When DHServlet has finished
 	public void done() {
 		DHready = false;	
 		dhEngine = null;
 	}
 }
