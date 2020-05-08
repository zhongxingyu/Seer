 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 public class ServerThread extends Thread {
 	Socket s = null;
 	String loggedInUser = null;
 	// hack for testing
 	List<String> users;
 	// ----------------
 	public ServerThread(Socket socket) {
 		super("ServerThread");
 		s = socket;
 
 
 		// hack for testing
 		users = new ArrayList<String>();
 		users.add("kai");
 		users.add("bill");
 		users.add("bob");
 		users.add("tyler");
 		// ----------------
 	}
 	public void run() {
 		try {
 			String line;
 			PrintWriter out = new PrintWriter(s.getOutputStream(), true);
 			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
 			System.out.println("New client connected");
 			out.println("220 Service ready");
 			boolean inMessage = false;
 			String messageRecipient = null;
 			while ((line = in.readLine()) != null) {
 				String[] command = line.split(" ");
 				System.out.println("Received \"" + line + "\" from client.");
 				if (command[0].equals("USER")) {
 					if (command.length != 2) {
 						out.println("500 Wrong number of arguments");
 						System.out.println("Wrong number of arguments");
 						continue;
 					} else if (this.loggedInUser != null) {
 						out.println("500 Already logged in");
 						System.out.println("Already logged in");
 						continue;
 					}
 					// hack for testing
 					for (int i = 0; i < users.size(); i++) {
 						if (users.get(i).equals(command[1])) {
 							this.loggedInUser = command[1];
 							break;
 						}
 					}
 					if (this.loggedInUser != null) {
 						out.println("230 Logged in");
 						System.out.println("Logged in");
 					} else {
 						out.println("500 Unknown user");
 						System.out.println("Unknown user");
 						continue;
 					}
 					// ----------------
 				} else if (line.startsWith("MESG")) {
 					if (command.length != 2) {
 						out.println("500 Wrong number of arguments");
 						System.out.println("Wrong number of arguments");
 						continue;
 					} else if (!command[1].startsWith("To:")) {
 						out.println("500 Incorrect argument");
 						System.out.println("Incorrect argument");
 						continue;
 					} else if (this.loggedInUser == null) {
 						out.println("500 No user logged in");
 						System.out.println("No user logged in");
 						continue;
 					} else if (inMessage) {
 						out.println("500 Message already started");
 						System.out.println("Message already started");
 						continue;
 					}
 					String recipient = command[1].substring(3);
 					if (!users.contains(recipient)) {
 						out.println("500 Unknown recipient");
 						System.out.println("Unknown recipient");
 						continue;
 					}
 					inMessage = true;
 					messageRecipient = recipient;
 					out.println("250 Ok");
 					System.out.println("Ok");
 				} else if (line.startsWith("DATA")) {
 					if (command.length != 1) {
 						out.println("500 Wrong number of arguments");
 						continue;
 					} else if (this.loggedInUser == null) {
 						out.println("500 No user logged in");
 						continue;
 					} else if (!inMessage) {
 						out.println("500 Must start a message before sending data");
 						continue;
 					}
 					out.println("354 Enter message, ending with a \".\" on a line by itself.");
 					System.out.println("Enter message, ending with a \".\" on a line by itself.");
 					Message m = new Message(this.loggedInUser, messageRecipient);
 					String dataLine;
 					while (!(dataLine = in.readLine()).equals(".")) {
 						m.writeln(dataLine);
 					}
 					int max_attempts = 2;
 					for (int i = 0; i < max_attempts; i++) {
 						if (!m.store()) {
 							System.out.println("Failed to store message, on attempt " + (i + 1) + "/" + max_attempts);
 						} else {
 							System.out.println("Message saved!");
 							break;
 						}
 					}
 					/*
 					System.out.println("Sending message to: " + messageRecipient);
 					System.out.println(messageData);
 					*/
 					inMessage = false;
 					messageRecipient = null;
 					out.println("250 Ok");
 					System.out.println("Ok");
 				} else if (line.startsWith("QUIT")) {
 					if (command.length != 1) {
 						out.println("500 Wrong number of arguments");
 						System.out.println("Wrong number of arguments");
 						continue;
 					} else if (this.loggedInUser == null) {
 						out.println("500 No user logged in");
 						System.out.println("No user logged in");
 						continue;
 					}
 					this.loggedInUser = null;
 					inMessage = false;
 					messageRecipient = null;
 					out.println("221 Logged out");
 					System.out.println("Logged out");
 				} else if (line.startsWith("RETR")) {
 					if (command.length != 1) {
 						out.println("500 Wrong number of arguments");
 						System.out.println("Wrong number of arguments");
 						continue;
 					} else if (this.loggedInUser == null) {
 						out.println("500 No user logged in");
 						System.out.println("No user logged in");
 						continue;
 					}
 					List<Message> messages = Message.load(this.loggedInUser);
 					if (messages.size() == 0) {
 						out.println("250 No messages");
 						System.out.println("No messages");
 					} else {
 						for (Message m : messages) {
 							out.println(m);
 							System.out.println(m);
 							out.println("|||");
 							System.out.println("|||");
 						}
						out.println("250 No more messages");
						System.out.println("No more messages");
 					}
 				} else {
 					out.println("500 Command \"" + command[0] + "\" was not recognized");
 				}
 			}
 			if (line == null) {
 				System.out.println("Connection was closed by the client");
 				// perform cleanup
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.out.println("Done!");
 	}
 }
