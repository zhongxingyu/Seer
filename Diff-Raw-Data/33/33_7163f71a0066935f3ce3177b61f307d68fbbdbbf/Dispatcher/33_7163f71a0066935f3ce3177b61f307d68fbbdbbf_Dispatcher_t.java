 package main;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import objects.Message;
 
 @WebServlet("/")
 // Mapped to hoster/id
 // hoster/ does NOT work, as it goes to the MainServlet
 public class Dispatcher extends HttpServlet {
 	// Job: To take requests from client and reroute them to their proper
 	// chatroom
 
 	// List of chatrooms
 	public static LinkedList<Chatroom> chatroomList = new LinkedList<Chatroom>();
 
 	// Diffie Hellman Extension
 	private static final String DIFFIE_HELLMAN = "/DHKE";
 
 	//Hoster
	public static final String HOSTER = "amgine4-michael99man.rhcloud.com/";
 	
 	private static final long serialVersionUID = -8110153131733414341L;
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws IOException {
 		process(Chatroom.Type.GET, request, response);
 	}
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws IOException {
 		process(Chatroom.Type.POST, request, response);
 	}
 
 	private void process(Chatroom.Type t, HttpServletRequest request,
 			HttpServletResponse response) throws IOException {
 
 		String id = request.getRequestURL().toString();
 
 		// To account for changing hoster URL
 		int offset = id.indexOf(HOSTER);
 		
 		id = id.substring(offset + HOSTER.length(), id.length());
 
 		boolean dhke = false;
 		if (id.indexOf(DIFFIE_HELLMAN) != -1) {
 			dhke = true;
 			id = id.substring(0, id.indexOf(DIFFIE_HELLMAN));
 		}
 
 		if (getChatroom(id) == null) {
			// This would only happen if a browser tried to access an invalid URL
 			System.out.println("ERROR: CHATROOM '" + id + "' NOT FOUND");
 			response.getWriter().println("Chatroom '" + id + "' doesn't exist!");
 			return;
 		} else if (dhke) {
 			if (!Functions.isClient(request)) {
 				response.getWriter().println(
 						"YOU'VE COME TO THE WRONG NEIGHBORHOOD, BROWSER...");
 				return;
 			} else {
 				if (getChatroom(id).DHready) {
 					getChatroom(id).dhProcess(t, request, response);
 				}
 				return;
 			}
 		}
 
 		Chatroom cr = getChatroom(id);
 		if (Functions.isClient(request)) {
 			// System.out.println("CHATROOM \"" + id +
 			// "\" REQUESTED BY CLIENT");
 
 			// Sends the request and response to the chatroom for processing
 			cr.process(t, request, response);
 		} else {
 			System.out.println("CHATROOM \"" + id + "\" REQUESTED BY BROWSER");
 
 			PrintWriter out = response.getWriter();
 			out.println("Welcome to chatroom \"" + id + "\"!");
 
 			for (Message m : cr.msgList) {
 				out.println(m.date + " - " + m.time + " " + m.sender.name
 						+ ": " + m.message);
 			}
 			out.close();
 		}
 	}
 
 	public static void addChatroom(Chatroom cr) {
 		chatroomList.add(cr);
 	}
 
 	public static Chatroom getChatroom(String ID) {
 		for (Chatroom cr : chatroomList) {
 			if (cr.id.equalsIgnoreCase(ID)) {
 				// Returns the chatroom
 				return cr;
 			}
 		}
 		return null;
 	}
 }
