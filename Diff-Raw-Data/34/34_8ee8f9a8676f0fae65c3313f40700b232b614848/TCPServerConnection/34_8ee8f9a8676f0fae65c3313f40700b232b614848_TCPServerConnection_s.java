 package network.tcp.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import analyticsServer.event.EventFactory;
 
 import network.security.Base64StringDecorator;
 import network.security.IStringStream;
 import network.security.RSAStringDecorator;
 import network.security.SimpleStringStream;
 import network.security.StaticStream;
 import network.udp.server.UDPNotificationThread;
 
 import client.command.ClientCommandList;
 
 
 import command.CommandParser;
 
 import server.ServerMain;
 import server.ServerStatus;
 import server.logic.IUserRelated;
 import server.logic.User;
 import server.logic.UserDATABASE;
 import server.logic.UserNotification;
 
 public class TCPServerConnection implements Runnable, IUserRelated{
 	private Socket client;
 	//private static final int NTHREADS=10;
 
 	private BufferedReader in = null;
 	private PrintWriter out=null;
 	private CommandParser parser = null;
 	private User user = null;
 	private InetAddress address=null;
 	private int clientPort;
 	private int confirmAES=0;	
 	private boolean listening=true;
 	private String aesSecretKey, aesIVParam; 
 	
 	private IStringStream inputDecoder, outputEncoder;
 
 	public TCPServerConnection(Socket client){
 		this.client=client;
 		parser= new CommandParser(true,this);
 		parser.setCommandList(new ClientCommandList(this));
 		address=client.getInetAddress();
 		
 		// decorator for streams
 		initDecoderSetting();
 	}
 
 	@Override
 	public void run() {
 		try {
 			out= new PrintWriter(client.getOutputStream(),true);
 
 			in= new BufferedReader(new InputStreamReader(client.getInputStream()));
 			String input;
 			String answer;
 			while(((input=in.readLine())!=null)){
 				
 				
 				//DEBUG - Cypher-Stream
 				//System.out.println("\n------------------\n" + "[QUERY RAW]" + input + "\n------------------\n");
 				
 				// use input-decorator on all commands except plaintext list & end
 				if(!input.equals("!list") && !input.equals("!end")) {
 					try {
 						input = inputDecoder.getIncomingStream(input);
 					} catch (Exception e) {
 						System.out.println("An illegal command was caught and rejected.");
 						input = "";
 					}
 				}
 				
 				//DEBUG - Regular input
 				//System.out.println("\n------------------\n" + "[QUERY DEC]" + input + "\n------------------\n");
 				
 				
 				if(!listening)
 					break;
 				if(input.equals("!end")){
 					System.out.println("Received END command for user " + getUser());
 					if(!user.equals(""))
 						parser.parse("!logout");
 					break;
 
 				}
 				
 				//waiting for confirmation of server-challenge
 				if(confirmAES!=0) {
 					// reset to init-status
 					if(Integer.valueOf(input)!=confirmAES) {
 						initDecoderSetting();
 						input="";
 						System.out.println("AES authentication failed!");
 					} else {
 						System.out.println("AES authentication successful!");
 					}
 					input="";
 					confirmAES=0;
 				}
 				
 				
 				answer= parser.parse(input);
 				
 				if(answer.length()>0)
 					out.println(outputEncoder.putOutgoingStream(answer)); // Use current encoding (Plain, RSA, AES ...) on output-stream
 
 			}
 
 		} catch (IOException e) {
 			if(user!=null){
 				System.out.println("User " + user.getName() + " was disconnected.");
 				ServerStatus.getInstance().notifyAnalyticsServer(EventFactory.createUserEvent(user.getName(), 2));
 				parser.parse("!logout");
 			}
 
 		} catch(RejectedExecutionException e){
 			if(user!=null){
 				System.out.println("REJ EX");
 				ServerStatus.getInstance().notifyAnalyticsServer(EventFactory.createUserEvent(user.getName(), 2));
 			}
 		}
 		finally{
 			/*
 			if(user!=null){
 				//user disconnected event...
 				ServerStatus.getInstance().notifyAnalyticsServer(EventFactory.createUserEvent(user.getName(), 2));
 			}
 			*/
 			if(listening)
 				shutdown();
 		}
 	}
 
 	public synchronized void shutdown(){
 		System.out.println("TCP connection shutdown");
 		listening=false;
 
 		try {
 			//System.out.println("remote-kill client");
 			//notify("!kill"); //remote kill via TCP
 			out.println("!kill");
 			out.close();
 
 			//executor.shutdown();
 			//executor.awaitTermination(1, TimeUnit.SECONDS);
 
 
 			//System.out.println("waiting done");
 			in.close();
 
 			client.close();
 			/*
 			System.out.println("close in!");
 			in.close();
 			System.out.println("in closed");
 			client.shutdownInput();
 			System.out.println("shutdownInput");
 			client.shutdownOutput();
 			System.out.println("shutdownOutput");
 			 */
 			user=null;
 			//System.out.println("TCPConnection shutdown complete");
 
 		}catch(RejectedExecutionException e){
 
 		}
 		catch (IOException e) {
 
 		} //catch (InterruptedException e) {
 
 		//}
 		finally{ 
 			System.out.println("TCPConnection shutdown complete");
 			//System.out.println("shutdown accomplished");
 			try {
 				this.finalize();
 			} catch (Throwable e) {
 
 			}finally{
 				//System.out.println(TCPServerConnection.class.getSimpleName()+" finalized");
 			}
 		}
 	}
 
 	public synchronized void goOffline(){
 		System.out.println("TCP connection shutdown");
 		listening=false;
 
 		try {
 			if(user!=null)
 				UserDATABASE.getInstance().logout(user.getName());
 			
 			user=null;
 			out.close();
 			in.close();
 			client.close();
 
		}catch(RejectedExecutionException | IOException e){
 			System.out.println("Error while setting userstatus to offline.");
 		}
 	}
 	
 	public void setUser(User user){
 		this.user=user;
 	}
 
 	public User getUserObject() {
 		return user;
 	}
 
 	public InetAddress getInetAddress(){
 		return client.getInetAddress();
 	}
 
 	public void setPort(int port) {
 		this.clientPort = port;
 	}
 	public int getClientPort() {
 		return clientPort;
 	}
 
 	/*
 	private synchronized void notify(String message) throws RejectedExecutionException{
 		UDPNotificationThread thread;
 		thread= new UDPNotificationThread(address,clientPort,message);
 		executor.execute(thread);
 	}
 	 */
 
 	@Override
 	public void setUser(String user) {
 		this.user=new User(user);
 		this.user.setAddress(address);
 		this.user.setPort(clientPort);
 		this.user.setLoggedIn(true);
 	}
 
 	@Override
 	public String getUser() {
 		if(user!=null)
 			return user.getName();
 		else return "";
 	}
 
 	public void setUserObject(User user) {
 		this.user=user;
 
 	}
 
 	public synchronized void print(ArrayList<UserNotification> arrayList){
 		for(UserNotification note: arrayList){
 			//System.out.println(note.getMessage());
 			out.println(outputEncoder.putOutgoingStream(note.getMessage()));
 			note.setSent(true);
 		}
 	} 
 
 	public synchronized void print(String message){
 		out.println(outputEncoder.putOutgoingStream(message));
 	}
 	
 	/*
 	 * Sets a flag for the server to wait for AES encoded challenge
 	 */
 	public void confirmAESHandshake(int challenge) {
 		confirmAES = challenge;
 	}
 
 	public IStringStream getInputDecoder() {
 		return inputDecoder;
 	}
 
 	public void setInputDecoder(IStringStream inputDecoder) {
 		this.inputDecoder = inputDecoder;
 	}
 
 	public IStringStream getOutputEncoder() {
 		return outputEncoder;
 	}
 
 	public void setOutputEncoder(IStringStream outputEncoder) {
 		this.outputEncoder = outputEncoder;
 	}
 	
 	public String getAesSecretKey() {
 		return aesSecretKey;
 	}
 
 	public void setAesSecretKey(String aesSecretKey) {
 		this.aesSecretKey = aesSecretKey;
 	}
 
 	public String getAesIVParam() {
 		return aesIVParam;
 	}
 
 	public void setAesIVParam(String aesIVParam) {
 		this.aesIVParam = aesIVParam;
 	}
 
 	/*
 	 * Set/reset the decoder/encoder decorators
 	 */
 	public void initDecoderSetting() {
 		inputDecoder = new RSAStringDecorator(new SimpleStringStream(), null, ServerMain.getSetup().getServerKey());
 		outputEncoder = new SimpleStringStream();
 	}
 	
 }
