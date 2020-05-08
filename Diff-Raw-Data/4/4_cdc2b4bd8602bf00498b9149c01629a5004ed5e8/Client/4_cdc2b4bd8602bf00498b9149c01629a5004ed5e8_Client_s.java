 package network;
 
 
 
 import java.io.BufferedReader;
 import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 
 import logging.Logger;
 import logging.Message;
 import events.EventListener;
 import events.MessageRecivedEvent;
 import events.PromptFileAcceptEvent;
 import events.PromptFileAcceptListener;
 import events.UsernameRecivedEvent;
 
 public class Client {
 	
 	/*
 	 * Packets
 	 * 1= string
 	 * 2= file num packets
 	 * 3 = file request
 	 * 4 = file yes
 	 * 5 = file no
 	 * 6 = username
 	 * 7 = reply username
 	 * 8 = connected
 	 * 9 = disconnect
 	 */
 	private static final String PATTERN = 
 	        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
 	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
 	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
 	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
 	public static final int SOCKET=1238,PACKET_LENGTH=1000, BUFFER_LENGTH= 1000000;
 	private InputStream in;
 	private volatile DataOutputStream out;
 	private volatile Socket socket;
 	private CommunicationHandler communicationHandler;
 	private String username="";
 	private List _messageListeners = new ArrayList(),_usernameListeners=new ArrayList(),_promptFileAccpetListeners= new ArrayList();
 	private int timeout    = 10000;
 	private int maxTimeout = 25000;
 	private Logger _logger;
 	private File file;
 
 	
 	public Client(Logger logger){
 		_logger = logger;
 	}
 	
 	public void init(String ip){
 		try {
 			socket= new Socket(ip,SOCKET);
 			in = socket.getInputStream();
 			out = new DataOutputStream(socket.getOutputStream());
 			communicationHandler = new CommunicationHandler(); 
 			communicationHandler.start();
 			
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void init(Socket s){
 		socket=s;
 		try {
 			in = socket.getInputStream();
 			out = new DataOutputStream(socket.getOutputStream());
 			communicationHandler = new CommunicationHandler(); 
 			communicationHandler.start();
 			sendConnected();
 		} catch (IOException e) {
 			_logger.log(new Message(e.getMessage(),Message.Type.Error,e));
 		}
 		
 	}
 	
 	public void init(){
 		try {
 			ServerSocket serverSocket = new ServerSocket(SOCKET);
 			socket = serverSocket.accept();
 			serverSocket.close();
 			in = socket.getInputStream();
 			out = new DataOutputStream(socket.getOutputStream());
 			communicationHandler = new CommunicationHandler(); 
 			communicationHandler.start();
 			sendConnected();
 		} catch (IOException e) {
 			System.out.println("Could not connect on port :"+SOCKET);
 			e.printStackTrace();
 		}
 	}
 	
 	public void sendMessage(String s){
 		s=username+": "+s;
 		byte[] stringInBytes=s.getBytes();
 		byte[] buffer= new byte[PACKET_LENGTH];
 		buffer[0]=1;
 		
 		if(stringInBytes.length<PACKET_LENGTH){
 			System.arraycopy(stringInBytes, 0, buffer, 1, stringInBytes.length);
 			sendPacket(buffer);
 		}
 		else{
 			for(int i=1;i<stringInBytes.length;i+=PACKET_LENGTH-1){
 				System.arraycopy(stringInBytes, i-1, buffer, 1, PACKET_LENGTH-1);
 				sendPacket(buffer);
 			}
 		}
 	}
 	
 	public void sendPacket(byte[] packet){
 		try {
 			socket.getOutputStream().write(packet);
 			socket.getOutputStream().flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void setUsername(String s){
 		username=s;
 	}
 	
 	public String getUsername(){
 		return username;
 	}
 	
 	private void sendUsername(boolean reply){
 		byte[] stringInBytes=username.getBytes();
 		byte[] buffer= new byte[PACKET_LENGTH];
 		if(reply)
 			buffer[0]=7;
 		else
 			buffer[0]=6;
 		
 		
 			System.arraycopy(stringInBytes, 0, buffer, 1, stringInBytes.length);
 			sendPacket(buffer);
 		
 	}
 	
 
 	public static boolean validateIP(final String ip){          
 
 	      Pattern pattern = Pattern.compile(PATTERN);
 	      Matcher matcher = pattern.matcher(ip);
 	      return matcher.matches();             
 	}
 	
 	private synchronized void fireMessageRecivedEvent(String s){
 		MessageRecivedEvent event = new MessageRecivedEvent(s);
 		Iterator i = _messageListeners.iterator();
 		while(i.hasNext())
 			((EventListener) i.next()).handleMessageRecivedEvent(event);
 	}
 	
 	private synchronized void fireUsernameEvent(String s){
 		UsernameRecivedEvent event = new UsernameRecivedEvent(s);
 		Iterator i = _messageListeners.iterator();
 		while(i.hasNext())
 			((EventListener) i.next()).handleUsernameRecivedEvent(event);
 	}
 	
 	private synchronized void firePromptFileAcceptEvent(String s){
 		PromptFileAcceptEvent event = new PromptFileAcceptEvent(s,this);
 		Iterator i = _promptFileAccpetListeners.iterator();
 		while(i.hasNext())
 			((PromptFileAcceptListener)i.next()).handlePromptFileAcceptEvent(event);
 	}
 	
 	private void sendConnected(){
 		byte[] buffer = new byte[PACKET_LENGTH];
 		buffer[0]=8;
 		sendPacket(buffer);
 	}
 	
 	public synchronized void addMessageEventListener(EventListener e){
 		_messageListeners.add(e);
 	}
 	public synchronized void removeMessageEventListener(EventListener e){
 		_messageListeners.remove(e);
 	}
 	public synchronized void addUsernameEventListener(EventListener e){
 		_usernameListeners.add(e);
 	}
 	public synchronized void removeUsernameEventListener(EventListener e){
 		_usernameListeners.remove(e);
 	}
 	public synchronized void addPromptFileAcceptEventListener(PromptFileAcceptListener e){
 		_promptFileAccpetListeners.add(e);
 	}
 	public synchronized void removePromptFileAcceptEventListener(PromptFileAcceptListener e){
 		_promptFileAccpetListeners.remove(e);
 	}
 	
 	public void close(){
 		byte buffer[] = new byte[PACKET_LENGTH];
 		buffer[0]= 9;
 		sendPacket(buffer);
 		_logger.log(new Message("said good-bye", Message.Type.Report));
 		communicationHandler.connected=false;
 		try {
 			
 			socket.close();
 		} catch (IOException e) {
 			
 			e.printStackTrace();
 			System.out.println("I DODT GET CLOS-ED");
 		}
 	}
 	
 	public void sendFileInfo(File f){ //sends the file size to the other user
 		file =f;
 		String s=username+","+file.getName()+","+file.length();
 		byte[] stringInBytes=s.getBytes();
 		byte[] buffer= new byte[PACKET_LENGTH];
 		buffer[0]=3;
 		System.arraycopy(stringInBytes, 0, buffer, 1, stringInBytes.length);
 		sendPacket(buffer);
 		
 	}
 	
 	private void sendFile(){
 		try {
 			FileReader fr = new FileReader(file);
 			long numBytes = file.length();
 			char[] cbuf;
 			byte[] buffer = new byte[PACKET_LENGTH];
			System.arraycopy(ByteBuffer.allocate(4).putInt((int)numBytes/PACKET_LENGTH+1).array(), 0, buffer, 1, 4);
 			buffer[0]=2;
 			sendPacket(buffer);
 			for(int i=0;i<=numBytes/PACKET_LENGTH;i++){
 				if(numBytes > (i+1)*PACKET_LENGTH)
 				cbuf = new char[PACKET_LENGTH];
 				else{
 					cbuf = new char[(int) (numBytes-((PACKET_LENGTH)*i))];
 				}
 				fr.read(cbuf);
 				sendPacket(new String(cbuf).getBytes());
 				_logger.log(new Message("send file part "+i+" out of"+ numBytes/PACKET_LENGTH,Message.Type.Report));
 				Thread.sleep(100);
 			}
 		} catch (FileNotFoundException e) {
 			_logger.log(new Message("When getting file to send in \"Client\"", Message.Type.Error, e));
 		} catch (IOException e) {
 			_logger.log(new Message("When reading file to send in \"Client\"", Message.Type.Error, e));
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void respondAcceptFile(boolean repsonse){
 		byte[] buffer ;
 		if(repsonse)
 			 buffer = new byte[]{4};
 		else 
 			buffer= new byte[]{5};
 		sendPacket(buffer);
 	}
 	
 	
 	class CommunicationHandler extends Thread{
 		public boolean connected=true;
 		public void run(){
 			byte[] buffer = new byte[PACKET_LENGTH];  
 			while(connected){
 				try {
 					in.read(buffer);
 					_logger.log(new Message("got packet "+buffer[0],Message.Type.Report));
 					if(buffer[0]==1){
 						String temp = new String(buffer,1,PACKET_LENGTH-1);
 						fireMessageRecivedEvent(temp.trim());
 					
 					}
 					else if(buffer[0]==2){
 						ByteBuffer wrapped = ByteBuffer.wrap(buffer, 1, PACKET_LENGTH-1);
 						int numPackets=wrapped.getInt();
 						DataBuffer db = new DataBuffer(_logger);
 						FileBuffer fb = new FileBuffer(db,_logger);
 						for(int i=0;i<numPackets;i++){
 							in.read(buffer);
 							db.intakeData(buffer);
 						}
 					}
 					else if(buffer[0]==3){
 						String message = new String(buffer,1,PACKET_LENGTH-1);
 						
 						String[] parts = message.split(",");
 						firePromptFileAcceptEvent(parts[0]+"is trying to send you a file - "+parts[1]+" (it is "+parts[2]+" bytes long,i think...)");
 						
 						
 					}
 					else if(buffer[0]==4){
 						sendFile();
 					}
 					else if(buffer[0]==5){
 						fireMessageRecivedEvent("They denied your file ;( cry cry");
 					}
 					else if(buffer[0]==6){
 						System.out.println("trying to set the username");
 						String temp = new String(buffer,1,PACKET_LENGTH-1);
 						fireUsernameEvent(temp);
 						sendUsername(true);
 					}else if(buffer[0]==7){
 						String temp = new String(buffer,1,PACKET_LENGTH-1);
 						fireUsernameEvent(temp.trim());
 					}
 					else if(buffer[0]==8){
 						System.out.println("other got connected trying to send the username");
 						sendUsername(false);
 					}
 					else if(buffer[0]==9){
 						connected=false;
 						socket.close();
 					}
 					else if(buffer[0]==0){
 						fireMessageRecivedEvent("<Message Lost>");
 						_logger.log(new Message("Message Lost",Message.Type.Warning));
 					}
 					
 					else{
 						_logger.log(new Message("Got an error with a packet. Dont know what the ID is\nThe unknown ID is:"+buffer[0],Message.Type.Warning));
 						System.out.println("bytes:[");
 						String bytes ="bytes:[ ";
 						for(int i=0; i<buffer.length;i++)
 							bytes+=buffer[i]+",";
 						bytes+=" ]\n";
 						_logger.log(new Message(bytes,Message.Type.Report));
 						_logger.log(new Message("Text"+new String(buffer,1,PACKET_LENGTH-1),Message.Type.Report));
 						System.out.println("Text"+new String(buffer,1,PACKET_LENGTH-1));
 					}
 							
 						
 					
 					
 				} catch (Exception e) {
 					
 					System.out.println("Disconnected :P");
 					return;
 				}
 			}
 		}
 		
 		
 	}
 	
 
 }
