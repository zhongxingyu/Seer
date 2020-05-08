 package gui;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import javax.swing.JButton;
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Insets;
 import javax.swing.JLabel;
 import java.awt.Color;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.Properties;
 
 import javax.swing.JList;
 
 import db.Appointment;
 import db.MeetingPoint;
 import db.Notification;
 import db.NotificationType;
 import db.User;
 import core.CalendarProgram;
 import javax.swing.JComboBox;
 
 public class MenuPanel extends JPanel {
 	private JComboBox<Notification> notificationList;
 	private int selectedIndex;
 	JLabel lblNotifications;
 	CalendarProgram cp;
 	JButton btnEditAppointment;
 	/**
 	 * Create the panel.
 	 */
 	public MenuPanel(CalendarProgram cp) {
 		//set CalendarProgram
 		this.cp = cp;
 		
 		//set GridBagLayout
 		setBackground(new Color(51, 204, 204));
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 
 		//set helloLabel
 		JLabel lblHello = new JLabel("Hello");
 		lblHello.setForeground(new Color(0, 0, 0));
 		lblHello.setBackground(new Color(0, 0, 255));
 		GridBagConstraints gbc_lblHello = new GridBagConstraints();
 		gbc_lblHello.insets = new Insets(0, 0, 5, 5);
 		gbc_lblHello.gridx = 2;
 		gbc_lblHello.gridy = 1;
 		add(lblHello, gbc_lblHello);
 		
 		//set newAppointmentButton
 		JButton btnNewA = new JButton("New appointment");
 		btnNewA.addActionListener(new AddAppointmentListener(cp));
 		GridBagConstraints gbc_btnNewA = new GridBagConstraints();
 		gbc_btnNewA.gridwidth = 5;
 		gbc_btnNewA.insets = new Insets(0, 0, 5, 5);
 		gbc_btnNewA.gridx = 0;
 		gbc_btnNewA.gridy = 2;
 		add(btnNewA, gbc_btnNewA);
 		
 		//set showCalendarsButton
 		JButton btnShowCalendars = new JButton("Show calendars");
 		btnShowCalendars.addActionListener(new TestListener(cp));
 		GridBagConstraints gbc_btnShowCalendars = new GridBagConstraints();
 		gbc_btnShowCalendars.gridwidth = 5;
 		gbc_btnShowCalendars.insets = new Insets(0, 0, 5, 5);
 		gbc_btnShowCalendars.gridx = 0;
 		gbc_btnShowCalendars.gridy = 3;
 		add(btnShowCalendars, gbc_btnShowCalendars);
 		
 		//set EditAppointmentButton
 		btnEditAppointment = new JButton("Edit appointment");
 		btnEditAppointment.setEnabled(false);
 		btnEditAppointment.addActionListener(new EditAppointmentListener(cp));
 		GridBagConstraints gbc_btnEditAppointment = new GridBagConstraints();
 		gbc_btnEditAppointment.insets = new Insets(0, 0, 5, 5);
 		gbc_btnEditAppointment.gridx = 2;
 		gbc_btnEditAppointment.gridy = 4;
 		add(btnEditAppointment, gbc_btnEditAppointment);
 		
 		//set NotificationsLabel
 		lblNotifications = new JLabel("You have 0 notifications");
 		GridBagConstraints gbc_lblNotifications = new GridBagConstraints();
 		gbc_lblNotifications.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNotifications.gridx = 2;
 		gbc_lblNotifications.gridy = 5;
 		add(lblNotifications, gbc_lblNotifications);
 		
 		//set notificationList
 		notificationList = new JComboBox<Notification>();
 		notificationList.setMaximumRowCount(5);
 		notificationList.setPreferredSize(new Dimension(250,20));
 		notificationList.addActionListener(new NotificationListListener(notificationList));
 		GridBagConstraints gbc_comboBox = new GridBagConstraints();
 		gbc_comboBox.anchor = GridBagConstraints.NORTH;
 		gbc_comboBox.gridwidth = 8;
 		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
 		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBox.gridx = 0;
 		gbc_comboBox.gridy = 6;
 		add(notificationList, gbc_comboBox);
 		
 		//add label "Display Notifications"
 		Notification note = new Notification(-1, new Appointment(-1, -1, null, null,
 				null, null, false), NotificationType.WELCOME);
 		note.setMessage("Display Notifications");
 		notificationList.addItem(note);
 		
 		//set logoutbutton
 		JButton btnLog = new JButton("Logout");
 		btnLog.addActionListener(new logoutListener(cp));
 		GridBagConstraints gbc_btnLog = new GridBagConstraints();
 		gbc_btnLog.gridwidth = 3;
 		gbc_btnLog.insets = new Insets(0, 0, 0, 5);
 		gbc_btnLog.gridx = 1;
 		gbc_btnLog.gridy = 13;
 		add(btnLog, gbc_btnLog);
 
 	}
 	
 	//method for adding notifications
 	public void addNotification(Notification notification){
 		NotificationType notificationType = notification.getNotificationType();
 		Appointment appointment = notification.getAppointment();
 		if(appointment == null){
 			GregorianCalendar gc = new GregorianCalendar();
 			GregorianCalendar gc1 = new GregorianCalendar();
 			appointment = new Appointment(1, 2, "halla", gc, gc1, "halla", true);
 		}
 		if(notificationType == NotificationType.CANCELLED){
 			notification.setMessage("Meeting with title: " + appointment.getTitle() + " is cancelled");
 		}
 		else if(notificationType == NotificationType.CHANGED){
 			notification.setMessage("Meeting with title: " + appointment.getTitle() + " is changed");
 		}
 		else if(notificationType == NotificationType.INVITE){
 			notification.setMessage("Someone invited you to the event with title: " + appointment.getTitle());
 		}
 		else if(notificationType == NotificationType.REJECTION){
 			notification.setMessage("Someone rejected the event with title: " + appointment.getTitle());
 		}
 		notificationList.addItem(notification);
 		update();
 	}
 	
 	//updatefunction for setting notificationslabel properly
 	private void update() {
 		int antall = notificationList.getItemCount();
 		if(antall == 1){
 			lblNotifications.setText(("You have: 0 notifications"));
 		}
 		else if(antall == 2){
 			lblNotifications.setText("You have: 1 notification");
 		}
 		else{
 			lblNotifications.setText("You have: " + Integer.toString(antall-1) + " notifications");
 		}
 	}
 	//logoutListener for logoutbutton
 	class logoutListener implements ActionListener{
 		CalendarProgram cp;
 		public logoutListener(CalendarProgram cp){
 			this.cp = cp;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			//cp.logout();
 			String title = "Log out";
 			String mordi = "Are you sure you want to logout?";
 		    int reply = JOptionPane.showConfirmDialog(cp,mordi,title, JOptionPane.YES_NO_OPTION);
 		    if (reply == JOptionPane.YES_OPTION)
 		    {
 		    	cp.logout();
		    	System.exit(0);
 		    }
 			//cp.displayLogin();
 		}
 		
 	}
 	
 	//addappointmentlistener for addappointmentbutton
 	class AddAppointmentListener implements ActionListener{
 		CalendarProgram cp;
 		public AddAppointmentListener(CalendarProgram cp){
 			this.cp = cp;
 		}
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
			cp.createEditAppointmentPanel(null);
 		}
 		
 	}
 	
 	//editappointmentlistener for editappointmentbutton
 	class EditAppointmentListener implements ActionListener{
 		CalendarProgram cp;
 		public EditAppointmentListener(CalendarProgram cp){
 			this.cp = cp;
 		}
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			cp.createEditAppointmentPanel(cp.getSelectedAppointment());
 		}
 		
 	}
 	//just us the show calendars button to send notifications to server for testing
 	class TestListener implements ActionListener{
 		CalendarProgram cp;
 		public TestListener(CalendarProgram cp){
 			this.cp=cp;
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			System.out.println("show cal button clicked");
 			Appointment app = new Appointment(cp.getUser());
 			app.setMeeting(true);
 			app.setMeetingPoint(new MeetingPoint(3, "Blackie", 15));
 			app.addParticipant(new User(8, "ALeksander", "email", "passord"));
 			app.addParticipant(new User(1, "espen", "master@commander.net", "hunter2"));
 			cp.sendNotification(new Notification(cp.getUser().getId(), app, NotificationType.WELCOME,"test notification"));
 		}
 	}
 	
 	//listener for notificationlist
 	class NotificationListListener implements ActionListener{
 		JComboBox<Notification> notificationList;
 		
 		public NotificationListListener(JComboBox<Notification> notificationList) {
 			this.notificationList = notificationList;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			int index = notificationList.getSelectedIndex();
 			Notification note = (Notification) notificationList.getSelectedItem();
 			if(index != -1 && index != 0){
 				notificationList.removeItem(note);
 				notificationList.setSelectedIndex(0);
 				cp.setFocusInCalendar(note);
 				}
 			update();
 		}
 		
 	}
 	
 	//main method for testing
 	 public static void main(String args[]) {
 			JFrame frame = new JFrame("...");
 			CalendarProgram cp1 = new CalendarProgram();
 			MenuPanel mp = new MenuPanel(cp1);
 			frame.getContentPane().add(mp);
 			frame.pack();
 			frame.setSize(200, 400);
 			frame.setVisible(true);
 	}
 	 
 	//method for setting editbutton enabled
 	public void setEditButtonEnabled() {
 		btnEditAppointment.setEnabled(true);
 		
 	}
 	
 	//method for setting editbutton disabled
 	public void setEditButtonDisabled() {
 		btnEditAppointment.setEnabled(false);
 		
 	}  
 
 }
