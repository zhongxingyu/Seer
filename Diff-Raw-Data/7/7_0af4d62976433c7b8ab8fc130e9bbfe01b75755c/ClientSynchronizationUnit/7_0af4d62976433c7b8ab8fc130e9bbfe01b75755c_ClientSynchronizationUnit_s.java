 package synclogic;
 
 import gui.GUILoggInInfo;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
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
 
 import javax.swing.Timer;
 
 import model.Appointment;
 import model.Meeting;
 import model.Notification;
 import model.Room;
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
 	private GUILoggInInfo notificationShower;
 	
 	private static final int TIME_BETWEEN_UPDATES = 20000;
 	
 	public ClientSynchronizationUnit(){
 		this.sendQueue = new MessageQueue();
 		this.sendQueue.addPropertyChangeListener(this);
 		this.thread = new Thread(new SendClass());
 		
 		ActionListener updater = new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Looking for updates");
 				synchronized (this) {
 					try {
 						List<ErrorMessage> errors = update();
 						for (ErrorMessage error : errors) {
 							handleError(error);
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				System.out.println("Done");
 			}
 		};
 		new Timer(TIME_BETWEEN_UPDATES, updater).start();
 
 	}
 	
 	public void setNotificationShower(GUILoggInInfo notificationShower) {
 		this.notificationShower = notificationShower;
 	}
 	
 	public void handleError(ErrorMessage error) {
 		System.out.println("Behandler error...");
 		if(error.getValidObject() == null) {
 			// Opprettelse av objekt var ikke lovlig
 			this.listeners.remove(this.getObjectFromID(error.getInvalidObject().getSaveableClass(), error.getInvalidObject().getObjectID()));
 			// TODO: La brukeren bestemme hva som skal skje
 		}
 		// TODO: Gjoer mer!
 	}
 	
 	public void addToSendQueue(String o) {
 		this.sendQueue.add(o);
 	}
 	
 	public void addToSendQueue(SyncListener o) {
 		this.sendQueue.add(XmlSerializerX.toXml(o, o.getSaveableClass()));
 	}
 	
 	public List<Room> getAvailableRooms(Date start, Date end) {
 		this.addToSendQueue(XmlSerializerX.toXml(new RoomAvailabilityRequest(start, end), SaveableClass.RoomAvailabilityRequest));
 		try {
 			return (List<Room>) ((RoomAvailabilityRequest) XmlSerializerX.toObject(this.connection.receive())).getAvailableRooms();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		System.out.println("Thread state: " + this.thread.getState());
 		if (!this.thread.isAlive()){
 			new Thread(new SendClass()).start();
 			this.stopThread = false;
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
 			if (respons.getLoginAccepted()){
 				update();
 			}
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
 			addToSendQueue(XmlSerializerX.toXml(this.updateRequest, SaveableClass.UpdateRequest));
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
		if(this.notificationShower != null) {
			this.notificationShower.loadNotifications();
		}
 		return errorMessages;
 	}
 	
 	/**
 	 * Returns a list of all the local users
 	 * @return A list of users
 	 */
 	public List<User> getAllUsers() {
 		List<User> users = new ArrayList<User>();
 		for (SyncListener object : this.listeners) {
 			if (object.getSaveableClass() == SaveableClass.User){
 				users.add((User) object);
 			}
 		}
 		return users;
 	}
 	
 	/**
 	 * Returns a list of all the local appointments
 	 * @return A list of users
 	 */
 	
 	public List<Appointment> getAllAppointments() {
 		List<Appointment> appointments = new ArrayList<Appointment>();
 		for (SyncListener object : this.listeners) {
 			if (object instanceof Meeting){
 				System.out.println(((Meeting) object).getDescription());
 				appointments.add((Meeting) object);
 			} else if (object instanceof Appointment){
 				appointments.add((Appointment) object);
 			} 
 		}
 		return appointments;
 	}
 	
 	/**
 	 * Closes this connection
 	 * @throws IOException 
 	 */
 	public void disconnect() throws IOException{
 		int counter = 0;
 		while(true){
 			try {
 				if (this.sendQueue.isEmpty()){
 					internalWait(500);
 					this.connection.close();
 					this.stopThread = true;
 					return;
 				}
 			} catch (IOException e) {
 				if (counter>2){
 					throw e;
 				}
 			}
 			counter++;
 			internalWait(50);
 		}
 	}
 	
 	
 	
 //	public static void main(String[] args){
 //		ClientSynchronizationUnit syncUnit = new ClientSynchronizationUnit();
 //		try {
 //			syncUnit.connectToServer("localhost", 1337);
 //			syncUnit.logIn("joharei", "123");
 //			System.out.println("Logged in!!");
 //		} catch (ConnectException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 ////		for (int i = 0; i<10; i++){
 ////			syncUnit.addToSendQueue("Element " + i);
 ////		}
 //		syncUnit.disconnect();
 //	}
 
 	@Override
 	public void addObject(SyncListener o) {
 		if (o instanceof User){
 			this.listeners.add(o);
 			for (Notification not : ((User) o).getNotifications()) {
 				this.listeners.add(not);
 			}
 		} else if (o instanceof Appointment){
 			this.listeners.add(o);
 		} 
 		
 	}
 }
