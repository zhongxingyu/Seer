 package ru.allgage.geofriend.server;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.Random;
 
 /**
  * Client socket handler thread.
  */
 public class SocketHandler implements Runnable {
 	final String ERROR_HEADER = "error";
 	final String LOGGED_IN_HEADER = "logged in";
 	final String USER_STATUS_HEADER = "status";
 
 	final Socket socket;
 	final UserDAO userDAO;
 	final StatusDAO statusDAO;
 
 	User loggedUser;
 
 	/**
 	 * Creates a socket handler.
 	 *
 	 * @param socket    client socket.
 	 * @param userDAO   user data access object.
 	 * @param statusDAO status data access object.
 	 */
 	SocketHandler(Socket socket, UserDAO userDAO, StatusDAO statusDAO) {
 		this.socket = socket;
 		this.userDAO = userDAO;
 		this.statusDAO = statusDAO;
 		System.out.println(socket.getInetAddress().toString());
 	}
 
 	/**
 	 * Runs the handler.
 	 */
 	@Override
 	public void run() {
 		try (Socket socket = this.socket;
 			 DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
 			 DataInputStream din = new DataInputStream(socket.getInputStream())) {
 
 			String auth = din.readUTF();
 			if (auth.equals("login")) {
 				String user = din.readUTF();
 				String pass = din.readUTF();
 
 				loggedUser = userDAO.load(user, pass);
 				if (loggedUser == null) {
 					writeError(dout, "invalid login or password");
 					return;
 				}
 			} else if (auth.equals("register")) {
 				String user = din.readUTF();
 				String pass = din.readUTF();
 				String email = din.readUTF();
 
 				loggedUser = userDAO.create(user, pass, email);
 				if (loggedUser == null) {
 					writeError(dout, "cannot register user");
 					return;
 				}
 			} else {
 				writeError(dout, "invalid login sequence");
 				return;
 			}
 
 			writeLoggedIn(dout);
 
 			String str = din.readUTF(); // TODO: investigate what is it.
 			Random rnd = new Random();
 
 			while (true) {
 				String command = din.readUTF();
 				if (command.equals("updateStatus")) {
					double lat = din.readDouble();
					double lng = din.readDouble();
 					String status = din.readUTF();
 					if (statusDAO.create(loggedUser, lat, lng, status)) {
 						writeMessages(dout, "success");
 					} else {
 						writeError(dout, "error updating status");
 					}
 				} else if(command.equals("updateAllStatuses")) {
 					for (Status status : statusDAO.getStatuses()) {
 						writeStatus(dout, status);
 					}
 				} else {
 					writeError(dout, "invalid command sequence");
 					return;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Writes one or many strings to the stream, flushing it afterwards.
 	 *
 	 * @param stream   data stream.
 	 * @param messages messages to write.
 	 * @throws IOException thrown in case something going wrong.
 	 */
 	private void writeMessages(DataOutputStream stream, String... messages) throws IOException {
 		for (String message : messages) {
 			stream.writeUTF(message);
 		}
 
 		stream.flush();
 	}
 
 	/**
 	 * Writes the proper-formed error to the data stream.
 	 *
 	 * @param stream  data stream.
 	 * @param message error message.
 	 * @throws IOException thrown in case something going wrong.
 	 */
 	private void writeError(DataOutputStream stream, String message) throws IOException {
 		writeMessages(stream, ERROR_HEADER, message);
 	}
 
 	/**
 	 * Writes the "logged in" message to the data stream.
 	 *
 	 * @param stream data stream.
 	 * @throws IOException thrown in case something going wrong.
 	 */
 	private void writeLoggedIn(DataOutputStream stream) throws IOException {
 		writeMessages(stream, LOGGED_IN_HEADER);
 	}
 
 	/**
 	 * Writes the status message to the data stream.
 	 *
 	 * @param stream data stream.
 	 * @param status user status.
 	 * @throws IOException thrown in case something going wrong.
 	 */
 	private void writeStatus(DataOutputStream stream, Status status) throws IOException {
 		stream.writeUTF(USER_STATUS_HEADER);
 		stream.writeUTF(status.getUser().getLogin());
 		stream.writeDouble(status.getLatitude());
 		stream.writeDouble(status.getLongitude());
 		stream.writeUTF(status.getText());
 		stream.flush();
 	}
 }
