 package server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import model.Document;
 import model.User;
 
 public class Server {
 	
 	private List<Document> currentDocuments;
 	private int port = 4444;
 	private final ServerSocket serverSocket;
 	private Map<String, User> name_userMappings;
 	private Set<String> onlineUsers;
 	private Map<Integer, String> socketUserMappings;
 	private List<PrintWriter> outputStreamWriters;
 	
 	private final Object lock = new Object();
 	
 	private LinkedBlockingQueue<ServerRequest> queue;
 	
 	/**
 	 * Initializes the EtherpadServer with the default port number
 	 * @throws IOException If there is an error creating the server socket
 	 */
 	public Server() throws IOException {
 		currentDocuments = new ArrayList<Document>();
 		name_userMappings = new HashMap<String,User>();
 		serverSocket = new ServerSocket(port);
 		
 		onlineUsers = new HashSet<String> ();
 		socketUserMappings = new HashMap<Integer, String> ();
 		
 		outputStreamWriters = new ArrayList<PrintWriter> ();
 		
 		queue = new LinkedBlockingQueue<ServerRequest> ();
 	}
 	
 	/**
 	 * Initializes the EtherpadServer with the given port number
 	 * @param port The port number to which the server publishes messages
 	 * @throws IOException If there is an error creating the server socket
 	 */
 	public Server(int port) throws IOException {
 		this.port = port;
 		this.serverSocket = new ServerSocket(port);
 		currentDocuments = new ArrayList<Document> ();
 		name_userMappings = new HashMap<String, User> ();
 		
 		onlineUsers = new HashSet<String> ();
 		socketUserMappings = new HashMap<Integer, String> ();
 		
 		outputStreamWriters = new ArrayList<PrintWriter> ();
 	}
 	
 	/**
      * Run the server, listening for client connections and handling them.  
      * Never returns unless an exception is thrown.
      * @throws IOException if the main server socket is broken
      * (IOExceptions from individual clients do *not* terminate serve()).
      */
     public void serve() throws IOException {
     	int ID = 0;
         while (true) {
             // block until a client connects
             Socket socket = serverSocket.accept();
             ID++; //ID keeps track of the socket ID, to take care of users exiting
             Thread socketThread = new Thread(new RunnableServer(socket, ID));
             socketThread.start();
         }
     }
     
     /**
      * Class defined to help start a new thread every time a new client connects to the server
      */
     private class RunnableServer implements Runnable {
     	Socket socket;
     	int ID;
     	
     	public RunnableServer(Socket socket, int ID) {
     		this.socket = socket;
     		this.ID = ID;
     	}
     	
 		@Override
 		public void run() {
 			try {
 				handleConnection(socket, ID);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}			
 		}
     	
     }
     
     /**
      * Handle a single client connection.  Returns when client disconnects.
      * @param socket socket where the client is connected
      * @throws IOException if connection has an error or terminates unexpectedly
      */
 	public void handleConnection(Socket socket, int ID) throws IOException {
 		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
         
         try {
         	synchronized (lock) {
         		outputStreamWriters.add(out);
         	}
         	out.println("id: "+ID);
 	        for (String line = in.readLine(); line!=null; line=in.readLine()) {
 	        	ServerRequest serverRequest = new ServerRequest(ID, line);
 	        	queue.add(serverRequest);
 	            
 	        }
         } catch (IOException e) {
         	if (socketUserMappings.containsKey(ID)) {
         		String userName = socketUserMappings.get(ID);
         		onlineUsers.remove(userName);
         		socketUserMappings.remove(ID);
         		
         	}
         	outputStreamWriters.remove(out);
         	out.close();
         	in.close();
         }
 
 	}
 	
 	/**
 	 * Attends to the different requests made by the different clients.
 	 * @throws InterruptedException throws an interrupted exception when popping out of the block queue is interrupted
 	 */
	public synchronized void attendRequest() throws InterruptedException {
 		while (true) {
 			ServerRequest serverRequest = queue.take();
 			int ID = serverRequest.getID();
 			String request = serverRequest.getLine();
 			RequestType requestType = serverRequest.getType();
 			
 			String response = handleRequest(request, ID, requestType);
 
             for (PrintWriter outputStream : outputStreamWriters) {
             	outputStream.println(response);
             }
 		}
 	}
   
     /**
      * handler for client input
      * 
      * make requested mutations on game state if applicable, then return 
      * appropriate message to the user.
      * 
      * @param input The request from the client to the server
      * @return Response from the server to the client
      */
     private String handleRequest(String input, int ID, RequestType requestType) {
     	String userName = "";
     	
     	switch (requestType) {
     	case LOGIN:
 			userName = input;
 			return logIn(userName, ID);
 			
     	case NEWDOC:
 			
 			String[] newdocTokens = input.split(" ");
 			if (newdocTokens.length == 2) {
 				userName = newdocTokens[0];
 				String docName = newdocTokens[1];
 				return newDoc(userName, docName);
 			}
 			
 			break;
 			
 		case OPENDOC: {
 			String[] tokens = input.split(" ");
 			if (tokens.length == 2) {
 				userName = tokens[0];
 				String docName = tokens[1];
 				return openDoc(userName, docName);
 			}
 			
 			break;
 		}
 	
 		case CHANGEDOC:
 			return changeDoc(input);
 			
 		case EXITDOC:
 			
 			String[] exitdocSplit = input.split(" ");
 			userName = exitdocSplit[0];
 			String docName = exitdocSplit[1];
 			return exitDoc(userName, docName);
 			
 		case LOGOUT:
 			
 			String[] inputSplit = input.split(" ");
 			userName = inputSplit[0];
 			return logOut(userName);
 			
 		default:
 			return "Invalid request";
     	}
     	
     	return "Invalid request";
     	
 	}
     
     /**
      * Makes a change to the document, as per the instructions of the client
      * @param input
      * @return
      */
     private String changeDoc(String input) {
     	String[] inputSplit = input.split("\\|");
 		String docName = inputSplit[0];
 		Document currentDocument = getDoc(docName);
 		currentDocument.updateVersion();
 		int versionNumber = currentDocument.getVersion();
 		String docContent = null;
 		int position = -1;
 		int length = -1;
 		if (inputSplit.length == 4) {
 			position = Integer.valueOf(inputSplit[1]);
 			String change = inputSplit[2];
 			length = Integer.valueOf(inputSplit[3]);
 			String content;
 			if (change.equals("\t")) {
 				content = currentDocument.insertContent("\n", position);
 			} else {
 				content = currentDocument.insertContent(change, position);
 			}
 			currentDocument.updateContent(content);
 			docContent = content.replace("\n", "\t");
 			
 		} else if (inputSplit.length == 3) {
 			
 			position = Integer.valueOf(inputSplit[1]);
 			length = Integer.valueOf(inputSplit[2]);
 			String content = currentDocument.deleteContent(position, length);
 			currentDocument.updateContent(content);
 			docContent = currentDocument.toString();	
 			
 		}
 		currentDocument.setLastEditDateTime();
 		if (docContent != null && position != -1 && length != -1) {
 			return "changed|" + docName + "|" + docContent + "|" + position + "|" + length + "|" + versionNumber;
 		}
 		
 		throw new RuntimeException("Should not reach here");
     }
     
     /**
      * Logs the user in
      * @param userName Username of the user who logs into the system
      * @return 
      */
     private String logIn(String userName, int ID) {
 		if (onlineUsers.contains(userName)) {
 			return "notloggedin";
 		} else {
 			name_userMappings.put("username", new User(userName, ""));
 			onlineUsers.add(userName);
 			socketUserMappings.put(ID, userName);
 			StringBuilder stringBuilder = new StringBuilder("loggedin " + userName + " " + ID);
 			stringBuilder.append("\n");
 			for (Document document : currentDocuments){
 				stringBuilder.append(document.getName());
 				stringBuilder.append("\t");
 				stringBuilder.append(document.getDate());
 				stringBuilder.append("\t");
 				String collaborators = document.getCollab();
 				stringBuilder.append(collaborators);
 				stringBuilder.append("\n");
 			}
 			stringBuilder.append("enddocinfo");
 			return stringBuilder.toString();
 		}
     }
     
     /**
      * Logs the user out
      * @param userName The name of the user to be logged out
      * @return
      */
     private String logOut(String userName) {
 		onlineUsers.remove(userName);
 		return "loggedout " + userName;
     }
     
     /**
      * Creates a new document
      * @param userName The name of the user that creates the new document
      * @param docName The name of the newly created document
      * @return Response from the server to the client
      */
     private String newDoc(String userName, String docName) {
     	Document newDoc = new Document("asdf", docName, userName);
 		currentDocuments.add(newDoc);
 		String date = newDoc.getDate();
 		return "created|" + userName + "|" + docName + "|" + userName + "|" + date; 
     }
     
     /**
      * Opens a new document
      * @param userName The name of the user that opens the document
      * @param docName The name of the document that is being opened
      * @return Response from the server to the clients; all GUIs are updated
      */
     private String openDoc(String userName, String docName) {
     	
 		Document currentDocument = getDoc(docName);
 		currentDocument.addCollaborator(userName);
 		String docContent = currentDocument.toString();
 		docContent = docContent.replace("\n", "\t");
 		String collaborators = currentDocument.getCollab();
 		return "update|" + docName + "|" + collaborators + "\nopened|" + userName + "|" + docName + "|" + docContent + "|" + collaborators; 
 		
     }
     
     /**
      * Exits the document
      * @param userName The name of the user that is exiting the document
      * @param docName The name of the document that is being exited
      * @return Response from the server to the client
      */
     private String exitDoc(String userName, String docName) {
 		StringBuilder stringBuilder = new StringBuilder("exiteddoc " + userName + " " + docName);
 		stringBuilder.append("\n");
 		for (Document document : currentDocuments){
 			stringBuilder.append(document.getName());
 			stringBuilder.append("\t");
 			stringBuilder.append(document.getDate());
 			stringBuilder.append("\t");
 			String collaborators = document.getCollab();
 			stringBuilder.append(collaborators);
 			stringBuilder.append("\n");
 		}
 		stringBuilder.append("enddocinfo");
 		return stringBuilder.toString();
     }
 	
 	/**
 	 * Adds a new Document to the list of currently active documents
 	 * @param newDocument The document object of the new document being added to the collection
 	 */
 	public void addDocument(Document newDocument) {
 		currentDocuments.add(newDocument);
 	}
 	
 	/**
 	 * Returns the list of documents that are stored in the server
 	 * @return A List of document objects that are stored in the server
 	 */
 	public List<Document> getDocuments() {
 		return currentDocuments;
 	}
 	
 	/**
 	 * Returns the document object corresponding to the given document name
 	 * @param docName Name of the document object to be retrieved
 	 * @return An object of type Document
 	 */
 	private Document getDoc(String docName) {
 		for (Document document: currentDocuments) {
 			String name = document.getName();
 			if (docName.equals(name)) {
 				return document;
 			}
 		}
 		throw new RuntimeException("Document not found");
 	}
 	
 	/**
 	 * Starts the etherpad server
 	 * This server stores documents and can be accessed by clients running the controller. 
 	 * @param args Unused
 	 */
 	public static void main(String[] args) {
 		final Server etherpadServer;
 		try {
 			etherpadServer = new Server();
 			
 			//Serving thread handles new connections made to the server
 			Thread servingThread = new Thread(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						System.out.println("Listening for requests");
 						etherpadServer.serve();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			
 			//Attending thread attends to the different requests made by the clients
 			Thread attendingThread = new Thread(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						System.out.println("Attending requests");
 						etherpadServer.attendRequest();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			
 			servingThread.start();
 			attendingThread.start();
 			
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 			
 
 	}
 
 }
