 package ms.client.networking;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import ms.client.log.Log;
 import ms.client.logic.Task;
 
 import org.apache.log4j.Logger;
 
 public class ServerConnection {
 
 	private Logger logger = Log.getLogger();
 	private final String OK = "OK";
 	private int port = 0;
 	private String host = null;
 
 	public ServerConnection(String host, int port) throws UnknownHostException,
 			IOException {
 		this.port = port;
 		this.host = host;
 	}
 
 	private Socket newConnection() throws UnknownHostException, IOException {
 		return new Socket(host, port);
 	}
 
 	private void terminateConnection(Socket socket) throws IOException {
 		sendMessage("END", socket);
 		if (receiveMessage(socket).equals("END OK"))
 			socket.close();
 	}
 
 	private void sendMessage(String message, Socket mediastopfSocket)
 			throws IOException {
 		logger.info(message);
		PrintWriter sender = new PrintWriter(new OutputStreamWriter(
				mediastopfSocket.getOutputStream()), true);
 		sender.println(message);
 	}
 
 	private String receiveMessage(Socket mediastopfSocket) throws IOException {
 		BufferedReader clientreceiver = new BufferedReader(
 				new InputStreamReader(mediastopfSocket.getInputStream()));
 		return clientreceiver.readLine();
 	}
 
 	public void sendFile(String filename) throws IOException {
 		Socket mediastopfSocket = null;
 		mediastopfSocket = newConnection();
 		File file = new File(filename);
 		if (!file.exists())
 			return;
 
 		String reply;
 		sendMessage("TRANSFER", mediastopfSocket);
 		reply = receiveMessage(mediastopfSocket);
 		if (!reply.equals("TRANSFER READY"))
 			return;
 		sendMessage(filename, mediastopfSocket);
 		reply = receiveMessage(mediastopfSocket);
 		if (!reply.equals("TRANSFER NAME OK"))
 			return;
 		Long size = file.length();
 		sendMessage(size.toString(), mediastopfSocket);
 		reply = receiveMessage(mediastopfSocket);
 		if (!reply.equals("TRANSFER SIZE OK"))
 			return;
 
 		byte[] filebuffer = new byte[size.intValue()];
 		FileInputStream mediafilestream = new FileInputStream(filename);
 		BufferedInputStream bis = new BufferedInputStream(mediafilestream);
 
 		bis.read(filebuffer, 0, filebuffer.length);
 
 		OutputStream sender = mediastopfSocket.getOutputStream();
 		logger.info("Sending File: " + filename + "...");
 		sender.write(filebuffer, 0, filebuffer.length);
 		sender.flush();
 
 		reply = receiveMessage(mediastopfSocket);
 		sendMessage("ENDTRANSFER", mediastopfSocket);
 		terminateConnection(mediastopfSocket);
 	}
 
 	public ArrayList<Task> getTaskList() throws IOException {
 		Socket mediastopfSocket = null;
 		mediastopfSocket = newConnection();
 		ArrayList<Task> list = new ArrayList<Task>();
 		sendMessage("INFO", mediastopfSocket);
 		logger.info("Receiving Info data...");
 		String reply;
 		while (true) {
 			reply = receiveMessage(mediastopfSocket);
 			if (reply.equals("ENDINFO"))
 				break;
 			else {
 				list.add(new Task(0, reply));
 				logger.info(reply);
 				sendMessage(OK, mediastopfSocket);
 			}
 		}
 		terminateConnection(mediastopfSocket);
 		logger.info("INFO transfer finished");
 		return list;
 	}
 }
