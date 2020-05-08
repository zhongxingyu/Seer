 package server;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import data.Family;
 import data.MemberUpdate;
 import data.Rating;
 import data.RatingUpdate;
 import data.Update;
 import data.User;
 
 public class Server {
 	private final static int PORT = 4444;
 	private ServerSocket serverSocket;
 	private HashMap<String, User> userID;
 	private HashMap<String, Family> familyID;
 	private HashMap<String, Double> publicRatings;
 	private HashMap<String, Integer> publicCount;
 
 	/**
 	 * Make a Server that listens for connections on port.
 	 * 
 	 * @param port
 	 *            port number, requires 0 <= port <= 65535.
 	 * @throws IOException
 	 */
 	public Server(int port) throws IOException {
 		serverSocket = new ServerSocket(port);
 		userID = new HashMap<String, User>();
 		familyID = new HashMap<String, Family>();
 		publicRatings = new HashMap<String, Double>();
 		publicCount = new HashMap<String, Integer>();
 	}
 
 	/**
 	 * Run the server, listening for client connections and handling them. Never
 	 * returns unless an exception is thrown.
 	 * 
 	 * @throws IOException
 	 *             if the main server socket is broken (IOExceptions from
 	 *             individual clients do *not* terminate serve()).
 	 */
 	public void serve() throws IOException {
 		System.out.println("Listening for connectionssssss..");
 		while (true) {
 			// block until a client connects
 			Socket socket = serverSocket.accept();
 
 			// handle the client
 			(new ClientThread(socket)).start();
 		}
 	}
 
 	/**
 	 * Handle a single client connection. Returns when client disconnects.
 	 * 
 	 * @param socket where client is connected
 	 * @throws IOException if connection has an error or terminates unexpectedly
 	 */
 	private void handleConnection(Socket socket) throws IOException {
 
 		BufferedReader in = new BufferedReader(new InputStreamReader(
 				socket.getInputStream()));
 		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
 
 		try {
 			for (String line = in.readLine(); line != null; line = in.readLine()) {
 				System.out.println(line);
 				String output = handleRequest(line);
 				if (output != null) {
 					out.println(output);
 				}
 			}
 		} catch (SocketException e) {
 			System.out.println("Client disconnected");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		finally {
 			out.close();
 			in.close();
 		}
 	}
 
 	/**
 	 * handler for client input
 	 * 
 	 * @param input
 	 * @return
 	 */
 	private String handleRequest(String input) {
 		/*
 		 * Input: "GET_UPDATE userID timestamp" -> return new updates, if any
 		 * 
 		 * Input: "NEWUSER" -> return a unique 8 digit ID
 		 * 
 		 * Input: "GET_PUBLIC upc" -> might return null
 		 * 
 		 * Input: "SEND_LOG log" -> return nothing.
 		 * 
 		 * Update String Representation:
 		 * 
 		 * MemberUpdate: "MEMBER_UPDATE familyID userID boolean timestamp" -> true means add to family, false means remove 
 		 * RatingUpdate: "RATING_UPDATE upc userID rating timestamp" where rating is an enum instance
 		 */
 
 		String id = ".+@.+";
 		String upc = "([0-9]+)";
 		String getUpdate = "(GET_UPDATE " + id + " [0-9]+)";
 		String memberUpdate = "(MEMBER_UPDATE " + id + " " + id + " (true|false)) [0-9]+";
 		String ratingUpdate = "(RATING_UPDATE " + upc + " " + id + " (GOOD|BAD|NEUTRAL) [0-9]+)";
 		String getPublic = "GET_PUBLIC " + upc;
		String sendLog = "SEND_LOG";
 
 		String[] args = input.split(" ");
 		
 		if (input.matches("NEWUSER .+@.+")) {
 			String newID = args[1];
 			userID.put(newID, new User(newID));
 			return newID;
 		} else if (input.equals("NEWFAMILY "+id)) {
 			String newID = args[1];
 			familyID.put(newID, new Family(newID));
 			return newID;
 		}
 		else if (input.matches(getUpdate)) {			
 			String output = "";
 			int ID = Integer.parseInt(args[1]);
 			long timeStamp = Long.parseLong(args[2]);
 			if (userID.containsKey(ID)) {
 				User user = userID.get(ID);
 				System.out.println(ID +" trying to get updates");
 				ArrayList<Update> newUpdates = user.getFutureUpdates(new Time(timeStamp));
 				for (Update u : newUpdates){
 					System.out.println("update: " + u);
 					output += u.toString() + "\n";
 				}
 			}
 			return output;
 		}
 
 		else if (input.matches(memberUpdate)) {
 			User user = userID.get(Integer.parseInt(args[2]));
 			long timeStamp = Long.parseLong(args[4]);
 			Family family = familyID.get(Integer.parseInt(args[1]));
 			boolean add = Boolean.parseBoolean(args[3]);
 
 			user.receiveUpdate(new MemberUpdate(family, user, add, new Date(
 					timeStamp)));
 		}
 
 		else if (input.matches(ratingUpdate)) {
 			User user = userID.get(Integer.parseInt(args[2]));
 			Rating rating = Rating.valueOf(args[3]);
 			String code = args[1];
 			long timeStamp = Long.parseLong(args[4]);
 			user.receiveUpdate(new RatingUpdate(code, rating, new Date(
 					timeStamp), user));
 			Double original = (double) 0;
 			int count = 0;
 			if (publicRatings.containsKey(code)) {
 				count = publicCount.get(code);
 				original = publicRatings.get(code) * count;
 			}
 			count += 1;
 			original += rating.ordinal() - 2;
 			original /= count;
 			publicCount.put(code, count);
 			publicRatings.put(code, original);
 		} else if (input.matches(getPublic)) {
 			String code = args[1];
 			if (publicRatings.containsKey(code))
 				return publicRatings.get(code).toString() + "\n";
 			return "UNRATED\n";
		} else if (input.substring(0,8).equals(sendLog)){
 			String log = input.substring(8);
 			String filename = "";
 			for(int i = 0; i < log.length(); i++){
 				if(log.charAt(i) == '\n')
 					break;
 				filename += log.charAt(i);
 			}
 			File logFile = new File(filename);
 			if (!logFile.exists()) {
 				try {
 					logFile.createNewFile();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			try {
 				// BufferedWriter for performance, true to set append to file flag
 				BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
 						true));
 				buf.append(log);
 				buf.newLine();
 				buf.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return "\n";
 	}
 
 	public static void main(String[] args) {
 		try {
 			Server server = new Server(PORT);
 			server.serve();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Thread class that takes a Socket and handles the client connection for
 	 * that socket
 	 */
 	public class ClientThread extends Thread {
 		private final Socket socket;
 
 		public ClientThread(Socket socket) {
 			this.socket = socket;
 		}
 
 		public void run() {
 			try {
 				handleConnection(socket);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					socket.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
