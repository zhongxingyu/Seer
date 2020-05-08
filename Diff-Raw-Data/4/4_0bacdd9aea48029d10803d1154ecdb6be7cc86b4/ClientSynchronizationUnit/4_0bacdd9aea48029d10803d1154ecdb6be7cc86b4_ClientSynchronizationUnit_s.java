 package synclogic;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import model.SaveableClass;
 import model.User;
 import model.XmlSerializerX;
 import no.ntnu.fp.net.co.Connection;
 import no.ntnu.fp.net.co.ConnectionImpl;
 import nu.xom.ParsingException;
 
 public class ClientSynchronizationUnit extends SynchronizationUnit implements PropertyChangeListener {
 	
 	private MessageQueue sendQueue;
 	private Connection connection;
 	private LoginRequest loginRequest;
 	private UpdateRequest updateRequest;
 	private Thread thread;
 	private boolean stopThread = false;
 	
 	public ClientSynchronizationUnit(){
 		this.sendQueue = new MessageQueue();
 		this.sendQueue.addPropertyChangeListener(this);
 		thread = new Thread(new SendClass());
 	}
 	
 	public void addToSendQueue(String o) {
 		this.sendQueue.add(o);
 	}
 	
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		if (!this.thread.isAlive()){
 			this.stopThread = false;
 			this.thread.start();
 		}
 	}
 	
 	private synchronized void internalWait(int time){
 		try {
 			wait(time);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	class SendClass implements Runnable{
 
 		@Override
 		public void run() {
 			internalWait(50);
 			while(!stopThread){
 				while (!sendQueue.isEmpty()){
 					try {
 						connection.send(sendQueue.pop());
 					} catch (ConnectException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					internalWait(30);
 				}
 				internalWait(50);
 			}
 		}
 		
 	}
 	
 	/**
 	 * Sets up a connection to a server
 	 * @param ipAddress		The IP address for the server to connect to
 	 * @param port			The port to connect to
 	 */
 	public void connectToServer(String ipAddress, int port) throws IOException{
 		// TODO: Should the port really be 9999?
 		this.connection = new ConnectionImpl(9999);
 		try {
 			this.connection.connect(InetAddress.getByName(ipAddress), port);
 			internalWait(50);
 		} catch (SocketTimeoutException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 	
 	/**
 	 * Logs in the user
 	 * @param username		The username to log in
 	 * @param password		The password that belongs to the username
 	 * @throws ConnectException If anything related to the connection goes wrong
 	 */
 	public boolean logIn(String username, String password) throws ConnectException{
 		this.loginRequest = new LoginRequest(username, password);
 		try {
 			this.connection.send(XmlSerializerX.toXml(this.loginRequest, SaveableClass.LoginRequest));
 			LoginRequest respons = (LoginRequest) XmlSerializerX.toObject(this.connection.receive());
			update();
 			return respons.getLoginAccepted();
 		} catch (IOException e) {
 			throw new ConnectException();
 		} catch (ParseException e) {
 			throw new ConnectException();
 		} catch (ParsingException e) {
 			throw new ConnectException();
 		}
 	}
 	
 	/**
 	 * Checks for updates on the server
 	 * @return A list of ErrorMessages from the server
 	 * @throws ConnectException If anything related to the connection goes wrong
 	 */
 	public List<ErrorMessage> update() throws ConnectException{
 		this.updateRequest = new UpdateRequest();
 		List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();
 		try {
 			this.connection.send(XmlSerializerX.toXml(this.updateRequest, SaveableClass.UpdateRequest));
 			UpdateRequest respons = (UpdateRequest) XmlSerializerX.toObject(this.connection.receive());
 			
 			for (int i = 0; i<respons.size(); i++) {
 				if (respons.getObject(i) instanceof SyncListener){
 					SyncListener object = (SyncListener) respons.getObject(i);
 					if (getObjectFromID(object.getSaveableClass(), object.getObjectID()) != null){
 						fire(object.getSaveableClass(), object.getObjectID(), object);
 					} else{
 						addObject(object);
 					}
 				} else{
 					errorMessages.add((ErrorMessage) respons.getObject(i));
 				}
 			}
 			
 		} catch (IOException e) {
 			throw new ConnectException();
 		} catch (ParseException e) {
 			throw new ConnectException();
 		} catch (ParsingException e) {
 			throw new ConnectException();
 		}
 		return errorMessages;
 	}
 	
 	/**
 	 * Runs @
 	 * @return
 	 */
 	public List<User> getAllUsers() {
 //		try {
 //			update();
 //		} catch (ConnectException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 		this.listeners = new ArrayList<SyncListener>();
 		this.listeners.add((SyncListener) new User("Johan", "Reitan", "joharei", "123", "joharei@stud.ntnu.no", new Date(), 0));
 		this.listeners.add((SyncListener) new User("Nitharshaan", "Thevarajah", "nitharsh", "1234", "nitharsh@stud.ntnu.no", new Date(), 0));
 		this.listeners.add((SyncListener) new User("Ole", "O", "oleo", "12345", "oleo@stud.ntnu.no", new Date(), 1));
 		this.listeners.add((SyncListener) new User("Dole", "D", "doled", "123456", "doled@stud.ntnu.no", new Date(), 2));
 		this.listeners.add((SyncListener) new User("Doffen", "OD", "doffenOD", "1234567", "doffenod@stud.ntnu.no", new Date(), 3));
 
 		List<User> users = new ArrayList<User>();
 		for (SyncListener object : this.listeners) {
 			if (object.getSaveableClass() == SaveableClass.User){
 				users.add((User) object);
 			}
 		}
 		return users;
 	}
 	
 	/**
 	 * Closes this connection
 	 */
 	public void disconnect(){
 		while(true){
 			try {
 				if (this.sendQueue.isEmpty()){
 					internalWait(500);
 					this.connection.close();
 					this.stopThread = true;
 					return;
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			internalWait(50);
 		}
 	}
 	
 	
 	
 	public static void main(String[] args){
 		ClientSynchronizationUnit syncUnit = new ClientSynchronizationUnit();
 		try {
 			syncUnit.connectToServer("localhost", 1337);
 			syncUnit.logIn("joharei", "123");
 			System.out.println("Logged in!!");
 		} catch (ConnectException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 //		for (int i = 0; i<10; i++){
 //			syncUnit.addToSendQueue("Element " + i);
 //		}
 		syncUnit.disconnect();
 	}
 
 	@Override
 	public void addObject(SyncListener o) {
 		
 		
 	}
 	
 }
