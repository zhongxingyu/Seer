 package core;
 
 import gui.CalendarPanel;
 import gui.EditAppointmentPanel;
 import gui.LoginPanel;
 import gui.MenuPanel;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import core.alarm.AlarmHandler;
 import core.alarm.AlarmListener;
 import db.Action;
 import db.Appointment;
 import db.Callback;
 import db.Notification;
 import db.Status;
 import db.User;
 
 public class CalendarProgram extends JFrame implements AlarmListener {
 	
 	//gui
 	private JPanel contentPane;
 	private EditAppointmentPanel eap;
 	private LoginPanel loginPanel;
 	private MenuPanel menuPanel;
 	private CalendarPanel calendarPanel;
 	
 	//model
 	private HashMap<User, ArrayList<Appointment>> appointments;
 	private User currentUser;
 	private ArrayList<User> cachedUsers;
 
 	//tools
 	private Thread alarmHandlerThread;
 	private ClientFactory cf;
 	
 	//Alarm
 	private AlarmHandler alarmHandler;
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					CalendarProgram frame = new CalendarProgram();
 					frame.setSize(1200, 600);
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public CalendarProgram() {
 		
 		System.out.println("creating main program");
 		appointments = new HashMap<User, ArrayList<Appointment>>();
 		
 		//tool for talking with server
 		cf = new ClientFactory();
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//TODO: possibly overide this method to also close threads
 		setBounds(100, 100, 450, 300);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new BorderLayout(0, 0));
 		loginPanel = new LoginPanel(this);
 		contentPane.add(loginPanel, BorderLayout.CENTER);
 		
 		System.out.println(cachedUsers);
 		
 	}
 
 	public void displayLogin() {
 		menuPanel.setVisible(false);
 		calendarPanel.setVisible(false);
 		loginPanel = new LoginPanel(this);
 		contentPane.add(loginPanel, BorderLayout.CENTER);
 	}
 	
 	public void displayMainProgram(JPanel panel){
 		panel.setVisible(false);
 		menuPanel.setVisible(true);
 		calendarPanel.setVisible(true);
 	}
 	public boolean logIn(String userName, String password) {
 		System.out.println("trying to log in");
 		if (userName.length() > 0 && password.length() > 0 ) {
 			User temp = cf.sendAction(new User(0 ,userName ,null, password), Action.LOGIN);
 			System.out.println("got return value " + temp + " with status " + temp.getAction());
 			if (temp.getAction().equals(Action.SUCCESS)) {
				loginPanel.correctInput.setVisible(true);
 				System.out.println("*** Got valid login ***");
 				currentUser = temp;
 				return true;
			}
 		}
		loginPanel.wrongInput.setVisible(true);
 		return false;
 	}
 
 	//called when user logs inn
 	// added some appointments and notifications for testing. Unpossible to do in main...
 	public void CreateMainProgram() {
 		menuPanel = new MenuPanel(this);
 		
 		contentPane.add(menuPanel, BorderLayout.WEST);
 		calendarPanel = new CalendarPanel(this);
 		calendarPanel.setBackground(Color.RED);
 		contentPane.add(calendarPanel, BorderLayout.CENTER);
 		
 		//Initialize values
 		appointments = cf.sendAction(currentUser, Action.GET_ALL_USERS_ALL_APPOINTMENTS);
 		cachedUsers = cf.sendAction(currentUser, Action.GET_ALL_USERS);
 		
 		calendarPanel.setUserAndAppointments(appointments.get(currentUser)); // TODO: kikk p� denne
 		//loadAppointments();
 		alarmSetup();
 		//fetching notifications, after easch call it wait 5 mins
 		new Thread(new Runnable() {
 			public void run() {
 				while(true){
 					long wait = 300000;//millisec = 5 min
 					ArrayList<Notification> notifications = fetchNotifications();
 					System.out.println("Notifications: "+notifications);
 					for (Notification notification : notifications) {
 						menuPanel.addNotification(notification);						
 					}
 					try {
 						System.out.println("notification thread will sleep for "+wait+" millies");
 						Thread.sleep(wait);
 					} catch (InterruptedException e) {
 						System.out.println("notification thread interupted");
 					}
 					System.out.println("notification thread woke up");
 				}
 			}
 		}).start();//starts the thread
 	}
 	
 	// use this method to add notifications to the notificationsList
 	//TODO make this method send notifications when it should
 	private void addNotification(Notification notification) {
 		menuPanel.addNotification(notification);
 		
 	}
 	//this should be called every 5 minuts to fetch notifications
 	public ArrayList<Notification> fetchNotifications(){
 		return cf.sendAction(currentUser, Action.GET_NOTIFICATION);
 	}
 	public void sendNotification(Notification notification){
 		Notification callback = cf.sendAction(notification, Action.NOTIFICATION);
 		System.out.println(callback.getNotificationType());
 	}
 	//get appointments and starts to check them in a new thread, signing up for notifications from alarmHandler.
 	private void alarmSetup() {
 		alarmHandler = new AlarmHandler(appointments.get(currentUser));
 		alarmHandler.addAlarmEventListener(this);
 		alarmHandlerThread = new Thread(alarmHandler);
 		alarmHandlerThread.start();
 	}
 
 	public void logout(){
 		cf.sendAction(currentUser, Action.DISCONNECT);
 	}
 	
 	public HashMap<User, Status> getAllAppointmentUsers(Appointment app) {
 		System.out.println("getting participants with statuses for " + app.getId());
 		HashMap<User, Status> callback = cf.sendAction(app, Action.GET_ALL_APPOINTMENT_USERS);
 		if (callback != null) {
 			return callback;
 		} else {
 			System.out.println("could not return statuses");
 			return new HashMap<User, Status>();
 		}
 	}
 	
 	public void addAppointment(Appointment app){
 		System.out.println("we have these participants!");
 		System.out.println(app.getParticipants());
 		Appointment callback = cf.sendAction(app, Action.INSERT);
 		if (callback.getAction().equals(Action.SUCCESS)) {
 			calendarPanel.addAppointmentToModel(callback);
 			appointments.get(currentUser).add(callback);
 			if(callback.hasAlarm()){
 				alarmHandler.addAppointment(callback);
 				alarmHandlerThread.interrupt();
 				System.out.println("Thread: "+alarmHandlerThread.interrupted());
 			}
 			System.out.println("our superlist now contains the following"+appointments.size()+"elements: " + appointments);
 		} else {
 			System.out.println("Warning: Returned with status code " + callback.getAction());
 		}
 	}
 	
 	public void deleteAppointment(Appointment appointment) {
 		Callback c = cf.sendAction(appointment, Action.DELETE);
 		if (c.getAction().equals(Action.SUCCESS)){
 			System.out.println("det gikk bra");
 			calendarPanel.removeAppointment(appointment);
 			appointments.get(currentUser).remove(appointment);
 			calendarPanel.updateCalendar();
 		} else {
 			System.out.println("Warning: Returned with status code " + c.getAction());
 		}
 	}
 	
 	public void updateAppointment(Appointment appointment){
 		System.out.println("we have these participants!");
 		System.out.println(appointment.getParticipants());
 		// TODO: make edit appointment return the edited version
 		Appointment callback = cf.sendAction(appointment, Action.UPDATE);
 		if (callback.getAction().equals(Action.SUCCESS)) {
 			appointments.get(currentUser).remove(appointment);
 			appointments.get(currentUser).add(callback);
 			calendarPanel.removeAppointment(appointment);
 			calendarPanel.addAppointmentToModel(callback);
 			//calendarPanel.removeAppointment(appointment);
 			//calendarPanel.addAppointmentToModel(appointment);			
 		} else {
 			System.out.println("Warning: Returned with status code " + callback.getAction());
 		}
 	}
 	
 	public void createEditAppointmentPanel(Appointment appointment){
 		menuPanel.setVisible(false);
 		calendarPanel.setVisible(false);
 		if (appointment == null) {
 			eap = new EditAppointmentPanel(this,new Appointment(currentUser), true);
 		} else {
 			eap = new EditAppointmentPanel(this,appointment, false);			
 		}
 		contentPane.add(eap, BorderLayout.CENTER);
 		eap.setBackground(Color.LIGHT_GRAY);
 	}
 		
 	@Override
 	public void alarmEvent(Appointment appointment) {
 		String timeToAlarm = ""+new Date(appointment.getStart().getTimeInMillis()-appointment.getAlarm().getAlarmTime().getTimeInMillis());
 		JOptionPane.showMessageDialog(this, "You have appointment: "+appointment.getTitle()+" in: "+timeToAlarm,"Appointment alarm",JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public void setFocusInCalendar(Notification note) {
 		calendarPanel.setFocusToAppointment(note.getAppointment());
 	}
 
 	public void setEditButtonEnabled() {
 		menuPanel.setEditButtonEnabled();
 		
 	}
 	public void setEditButtonDisabled() {
 		menuPanel.setEditButtonDisabled();
 	}
 
 	public Appointment getSelectedAppointment() {
 		return calendarPanel.getSelectedEvent().getModel();
 	}
 	
 	public User getUser(){
 		return currentUser;
 	}
 
 	public ArrayList<User> getUsers() {
 		return cf.sendAction(new User(), Action.GET_ALL_USERS);
 	}
 	//method for getting all appointments from user
 	public ArrayList<Appointment> getApointmentsFromUser(User user){
 		return appointments.get(user.getId());
 	}
 	
 	public ArrayList<User> getCachedUsers() {
 		return this.cachedUsers;
 	}
 
 }
