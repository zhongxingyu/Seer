 /**
  * 
  */
 package edu.ncsu.csc573.project.commlayer;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.SocketAddress;
 import org.apache.log4j.Logger;
 import edu.ncsu.csc573.project.common.ConfigurationManager;
 import edu.ncsu.csc573.project.common.messages.EnumOperationType;
 import edu.ncsu.csc573.project.common.messages.IRequest;
 import edu.ncsu.csc573.project.common.messages.IResponse;
 import edu.ncsu.csc573.project.common.messages.RequestMessage;
 import edu.ncsu.csc573.project.controllayer.RequestProcessor;
 
 /**
  * This class handles individual client request.
  * 
  * @author doogle-dev
  * 
  */
 public class ClientHandler implements Runnable {
 	private Socket conncetedSocket;
 	private Logger logger;
 
 	public ClientHandler(Socket connSock) {
 		logger = Logger.getLogger(ClientHandler.class);
 		conncetedSocket = connSock;
 	}
 
 	
 	public void run() {
 		SocketAddress clientAddress = conncetedSocket.getRemoteSocketAddress();
 		RequestProcessor reqProcessor = new RequestProcessor();
 		logger.info("Handling client " + clientAddress);
 		boolean isFileTransfer = false;
 		// Expect register or login request from the client
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					conncetedSocket.getInputStream()));
 
 			// read each request and see till it logout
 			// sleep till the data is ready from the client
 			IRequest req = null;
 			IResponse response = null;
 			do {
 				logger.debug("Waiting for request from client " + conncetedSocket.getRemoteSocketAddress());
 				while (!br.ready()) {
 					//logger.debug("Waiting for request from client "
 					//		+ conncetedSocket.getRemoteSocketAddress());
 					try {
 						Thread.sleep(100);
 					} catch (Exception e) {
 						logger.error("Unexpected error from client" + conncetedSocket.getRemoteSocketAddress(), e);
 					}
 				}
 				logger.debug("Receiving request from client");
 				
 				StringBuffer sb = new StringBuffer();
 				int c;
 				while ((c = br.read()) != -1 && sb.indexOf("</request>") == -1) {
 					//logger.debug(c);
 					sb.append((char) c);
 				}
 
 				logger.debug("received data from client " + sb.toString());
 				
 				/*
 				 * Handle file requests 
 				 */
 
 				if(sb.indexOf("File:") != -1) {
 					File toBeUploadedFile = new File(ConfigurationManager.getInstance().getPublishDirectory(),getFileName(sb));
 					transferFile(conncetedSocket.getOutputStream(), toBeUploadedFile);
 					//br.read();
 					return ;
 				}
 				PrintWriter pw = new PrintWriter(new BufferedWriter(
 						new OutputStreamWriter(
 								conncetedSocket.getOutputStream())));
 				
 				try {
 					req = RequestMessage.createRequest(sb.toString().trim());
 					logger.info("Request from client " + conncetedSocket.getRemoteSocketAddress() + " is : "
 							+ req.getRequestInXML());
 					response = reqProcessor.processRequest(req);
 					pw.println(response.getRequestInXML());
 					pw.flush();
 				} catch (Exception e) {
 					logger.error("Unable to parse request", e);
 				}
 				//logger.info("Waiting for requests from client :" + clientAddress);
 			} while (req == null || req.getOperationType() != EnumOperationType.LOGOUT);
 			logger.info("Client" + conncetedSocket +" successfully logged out.");
 		} catch (IOException e) {
 			logger.error("Failed to read data from the client " + conncetedSocket.getRemoteSocketAddress());
 		} finally {
 			String connSockAddr = conncetedSocket.toString();
 			if(conncetedSocket != null && !isFileTransfer) {
 				try {
 					conncetedSocket.close();
 					logger.info("Successfully closed connection for client " + connSockAddr);
 				} catch (IOException e) {
 					logger.error("Failed to close " + conncetedSocket.getRemoteSocketAddress(), e);
 				}
 			}
 		}
 	}
 
 
 	private void transferFile(OutputStream os, File toBeUploadedFile) {
 		PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
 		BufferedReader br = null;
 		try {
 			br = new BufferedReader(new FileReader(toBeUploadedFile));
 			String temp;
 			while((temp= br.readLine()) != null) {
 				pw.println(temp);
 				pw.flush();
 			}
 			pw.flush();
 			logger.info("Done ! Sending file contents");
 		} catch (FileNotFoundException e) {
 			logger.error("Unable to find file: " + toBeUploadedFile, e);
 		} catch (IOException e) {
 			logger.error("Unable to read file: " + toBeUploadedFile, e);
 		} finally {
 			if(pw != null) {
 				pw.close();
 			} if(br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					logger.error("Unable to close input stream reader", e);
 				}
 			}
 		}
		
		logger.info("Successfully transfered file");
 	}
 
 
 	public static String getFileName(StringBuffer sb) {
 		int endIndex = sb.indexOf("</request>")-System.lineSeparator().length();
 		int stIndex = sb.indexOf("File:");
 		return sb.substring(stIndex+"File:".length(), endIndex);
 	}
 }
