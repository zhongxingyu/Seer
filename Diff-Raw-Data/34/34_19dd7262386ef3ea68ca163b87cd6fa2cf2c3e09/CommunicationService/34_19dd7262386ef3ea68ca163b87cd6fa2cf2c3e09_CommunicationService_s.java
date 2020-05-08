 /**
  * 
  */
 package edu.ncsu.csc573.project.commlayer;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import org.apache.log4j.Logger;
 import edu.ncsu.csc573.project.common.ConfigurationManager;
 import edu.ncsu.csc573.project.common.messages.EnumOperationType;
 import edu.ncsu.csc573.project.common.messages.IRequest;
 import edu.ncsu.csc573.project.common.messages.IResponse;
 import edu.ncsu.csc573.project.common.messages.ResponseMessage;
 
 /**
  * @author doogle-dev
  * 
  */
 public class CommunicationService implements ICommunicationService {
 
 	private Socket clientSocket;
 	private InetAddress BSSAddress;
 	private static Logger logger;
 	private PeerServer server;
 	private IPublishHandler publishHandler;
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.ncsu.csc573.project.commlayer.ICommunicationService#initialize(java
 	 * .lang.String, edu.ncsu.csc573.project.commlayer.IPublishHandler)
 	 */
 	public void initialize(String BootStrapServer,
 			IPublishHandler aPublishHandler) throws Exception {
 		// get a logger instance for this class
 		logger = Logger.getLogger(this.getClass());
 		logger.debug("Inside initialization of communication handler");
 		
 		publishHandler = aPublishHandler;
 		
 		/*
 		 * if the socket is already connected then initialization is already
 		 * done and so simply exiting.
 		 */
 		synchronized (CommunicationService.class) {
 			if (clientSocket != null && !clientSocket.isClosed() && server != null && server.isServerRunning()) {
 				logger.debug("Already connected to bootstrapserver");
 				logger.info("Already initialized");
 				return;
 			}
 			if(server == null || !server.isServerRunning()) {
 				logger.info("Start server");
 				server = new PeerServer();
 			}
 			initializeConnectedSocket(BootStrapServer); 
 		}
 
 	}
 
 	private void initializeConnectedSocket(String BootStrapServer)
 			throws Exception, UnknownHostException, SocketException,
 			SocketTimeoutException, IOException {
 		BSSAddress = getInetAddress(BootStrapServer);
 
 		int BSSport = ConfigurationManager.getInstance().getServerPort();
 		int timeOut = ConfigurationManager.getInstance().getTimeOut();
 
 		clientSocket = new Socket();
 		InetSocketAddress serverSocket = new InetSocketAddress(BSSAddress,
 				BSSport);
 		clientSocket.setKeepAlive(true);
 		clientSocket.setTcpNoDelay(true);
 		
 		logger.debug("Enabled Keepalive socket option");
 		try {
 			clientSocket.connect(serverSocket, timeOut);
 		} catch (SocketTimeoutException e) {
 			logger.error("Connection timed out", e);
 			cleanUp();
 			throw e;
 		} catch (IOException exp) {
 			logger.error("Connection refused", exp);
 			cleanUp();
 			throw exp;
 		}
 	}
 
 	private InetAddress getInetAddress(String BootStrapServer) throws Exception,
 			UnknownHostException {
 		InetAddress address = null;
 		try {
 			address = InetAddress.getByName(BootStrapServer);
 		} catch (UnknownHostException excpByHostName) {
 			try {
 				logger.info("Unable to find the address of the host: "
 						+ BootStrapServer + " by name");
 				logger.info("Trying by ip address");
 				address = InetAddress.getByAddress(BootStrapServer
 						.trim().getBytes());
 			} catch (UnknownHostException excpByIpAddress) {
 				logger.info("Unable to find the address of host: "
 						+ BootStrapServer + " even by ip address");
 				cleanUp();
 				throw excpByIpAddress;
 			}
 		}
 		return address;
 	}
 
 	private void cleanUp() throws Exception{
 		
 		if(server != null) {
 			server.stop();
 		}
 		while(!server.isServerRunning()) {
 			logger.info("Waiting for peer server to close");
 			Thread.sleep(100);
 		}
 		if(clientSocket != null) {
 			clientSocket.close();
 		}
 		logger.error("Unable to initialize Communication layer. Exiting from Application");
 	}
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.ncsu.csc573.project.commlayer.ICommunicationService#executeRequest
 	 * (edu.ncsu.csc573.project.common.messages.IRequest)
 	 */
 	public IResponse executeRequest(IRequest request) throws Exception{
 		BlockingThread bt = new BlockingThread(clientSocket, request);
 		bt.start();
 		try {
 			bt.join(ConfigurationManager.getInstance().getCLITimeOut());
 			if(!bt.isResponseReady()) {
 				logger.info("Failed to get response for the request : " + request.getOperationType());
 				throw new Exception();
 			} else {
 				logger.info("Received response : " + bt.getResponse());
 			}
 		} catch (InterruptedException e) {
 			
 		} finally{
 			if(bt.getResponse().getOperationType() == EnumOperationType.LOGOUTRESPONSE) {
 				clientSocket.close();
 			}
 		}
 		return bt.getResponse();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.ncsu.csc573.project.commlayer.ICommunicationService#publishRequest
 	 * (edu.ncsu.csc573.project.common.messages.IRequest,
 	 * edu.ncsu.csc573.project.commlayer.IResponseListener)
 	 */
 	public void publishRequest(IRequest request, IResponseListener listener) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.ncsu.csc573.project.commlayer.ICommunicationService#subscribeRequestTopic
 	 * (edu.ncsu.csc573.project.common.messages.EnumOperationType,
 	 * edu.ncsu.csc573.project.commlayer.IRequestListener)
 	 */
 	public void subscribeRequestTopic(EnumOperationType operationType,
 			IRequestListener reqListener) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.ncsu.csc573.project.commlayer.ICommunicationService#close()
 	 */
 	public void close() throws Exception {
 		if (clientSocket == null) {
 			throw new Exception("Communication Layer is not initialized");
 		}
 		try {
 			clientSocket.close();
 		} catch (Exception e) {
 			logger.error("Failed to close socket. Trying again");
 		}
 		server.stop();
 	}
 
 	public boolean isConnected() {
 		if (clientSocket == null) {
 			logger.info("Communication Layer is not initialized");
 			return false;
 		}
 		return clientSocket.isClosed();
 	}
 
 	class BlockingThread extends Thread {
 		private Socket clientSocket;
 		private IRequest request;
 		private IResponse response = null;
 		
 		BlockingThread(Socket clientSocket, IRequest request) {
 			super();
 			this.clientSocket = clientSocket;
 			this.request = request;
 		}
 
 		public void run() {
 
 			try {
 				/**
 				 * Request has to be transformed from IRequest to request object
 				 */
 				PrintWriter pw = new PrintWriter(new BufferedWriter(
 						new OutputStreamWriter(clientSocket.getOutputStream())));
 				pw.println(request.getRequestInXML());
 				//pw.println(System.);
 				pw.flush();
 
 				BufferedReader br = new BufferedReader(new InputStreamReader(
 						clientSocket.getInputStream()));
 				StringBuffer sb = new StringBuffer();
 				//String temp;
 			
 				while(!br.ready()) {
 					logger.info("No data received from server. Trying again...");
 					Thread.sleep(1000);  // to be removed
 				}
 				int ch;
 				while ((ch = br.read()) != -1 && sb.indexOf("</response>") == -1) {
 					sb.append((char)ch);
 				}
 				
 				
 				response = ResponseMessage.createResponse(sb.toString());
 				logger.info("Response is : " + response.getRequestInXML());
 				logger.info(response);
 				
 			} catch (Exception e) {
 				logger.error("Unable to parse response ", e);
 			}
 		}
 		
 		public IResponse getResponse() {
 			return response;
 		}
 		
 		public boolean isResponseReady() {
 			return (response == null ? false : true);
 		}
 	}
 
 	public boolean isPeerServerRunning() {
 		return server.isServerRunning();
 	}
 
 	public File getFile(String IPAddress, String fileName) {
 		PrintWriter pw = null;
 		Socket ftSoc = null;
 		BufferedReader br = null;
 		PrintWriter pwFile = null;
 		File newFile = null;
 		
 		try {
 			ftSoc = new Socket(getInetAddress(IPAddress), ConfigurationManager.getInstance().getFileTransferPort());
 			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(ftSoc.getOutputStream())));
 			logger.debug("Successfully opened socket for file transfer");
 			
 			pw.println("<request>");
 			pw.println("File:"+fileName);
 			pw.println("</request>");
 			pw.flush();
 			logger.debug("Sent request "+"File:"+fileName);
 			br = new BufferedReader(new InputStreamReader(ftSoc.getInputStream()));
 			String buff;
 			newFile = new File(ConfigurationManager.getInstance().getDownloadDirectory(),fileName.substring(0, fileName.indexOf("."))/*+"_"+System.currentTimeMillis()*/+".txt");
 			
 			/*
 			 * Rename file if it already exists. 
 			 */
 			if(newFile.exists()) {
 				newFile = getNewFile(newFile);
 			}
 			pwFile = new PrintWriter(new BufferedWriter(new FileWriter(newFile)));
			
 			while((buff = br.readLine())!=null) {
 				pwFile.println(buff);
 				pwFile.flush();
 				logger.trace("Read: " +buff);
 			}
 			pwFile.flush();
 			logger.info("Successfully saved downloaded file at " + newFile.getAbsolutePath());
 			pw.println("a");
 			pw.flush();
 		} catch (UnknownHostException e) {
 			logger.error("Unable to find host",e);
 		} catch (IOException e) {
 			logger.error("Unable to perform IO",e);
 		} catch (Exception e) {
 			logger.error("Unable to get file",e);
 		} finally {
 			if(ftSoc != null) {
 				try {
 					ftSoc.close();
 				} catch (IOException e) {
 					logger.error("Unable to close socket",e);
 				}
 			}
 			if(pw != null)
 				pw.close();
 			if(pwFile != null) 
 				pwFile.close();
 		}
 		return newFile;
 	}
 
 	private File getNewFile(File newFile) {
 		String oldName = newFile.getName();
 		int last_ud_char = oldName.lastIndexOf("_");
 		int suffix_start_index = oldName.indexOf(".txt");
 		String initialPart;
 		if( last_ud_char !=  -1) {
 			initialPart = oldName.substring(0, last_ud_char);
 			String file_count = oldName.substring(last_ud_char + 1, suffix_start_index);
 			try {
 				int count = Integer.parseInt(file_count) + 1;
 				String newName = initialPart + "_" + String.valueOf(count) + ".txt";
 				newFile = new File(ConfigurationManager.getInstance().getDownloadDirectory(),newName);
 			} catch(NumberFormatException e) {
 				initialPart = oldName.substring(0, suffix_start_index);
 				String newName =  initialPart + "_1" + ".txt";
 				newFile = new File(ConfigurationManager.getInstance().getDownloadDirectory(),newName);
 			}
 		} else {
 			initialPart = oldName.substring(0, suffix_start_index);
 			String newName =  initialPart + "_1" + ".txt";
 			newFile = new File(ConfigurationManager.getInstance().getDownloadDirectory(),newName);
 		}
 		return newFile;
 	}
 
 	public File getFileToUpload(String fileName) {
 		if(publishHandler != null) {
 			return publishHandler.getFileToUpload(fileName);
 		} else {
 			logger.error("PublishHandler is not initialized");
 			return null;
 		}
 	}
 }
