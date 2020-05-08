 package framePackage;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import client.Program;
 
 import data.Alarm;
 import data.CalendarModel;
 import data.Meeting;
 import data.MeetingRoom;
 import data.Notification;
 import data.Person;
 import data.Team;
 
 public class AppointmentOverView {
 
 	private JFrame frame;
 	private JLabel headLine, lblMoreInfo, lblParticipant, lblInfo,
 			lblYourStatus, lblStatus;
 	private JList participantList;
 	private DefaultListModel listModel;
 	private JTextArea moreInfo;
 	private JComboBox<ImageIcon> yourStatus;
 	private JPanel overViewPanel;
 	private Meeting meeting;
 	private Person user;
 	private JButton change, delete;
 	private ImageIcon check, cross, question,star;
 	private List<Notification> notifications;
 	private Notification userNotification;
 	private NewAppointmentView newAppointment;
 	private CalendarModel calendarModel;
 	private Alarm alarm;
 	private JLabel lblcreator;
 	private JLabel creator;
 
 	public AppointmentOverView(Meeting meeting) {
 		this.calendarModel = Program.calendarModel;
 		this.meeting = meeting;
 		this.user = calendarModel.getUser();
 		notifications = calendarModel.getAllNotificationsOfMeeting(meeting);
 		for (Notification n : notifications) {
 			if(n.getPerson().getUsername().equals(user.getUsername())) {
 				userNotification = n;
 			}
 		}
 		initialize();
 	}
 
 	private void initialize() {
 		check = new ImageIcon("res/icons/icon_check.png");
 		cross = new ImageIcon("res/icons/icon_cross.png");
 		question = new ImageIcon("res/icons/icon_question.png");
 		star = new ImageIcon("res/icons/icon_star.png");
 
 		overViewPanel = new JPanel(new GridBagLayout());
 		overViewPanel.setPreferredSize(new Dimension(750, 500));
 		overViewPanel.setVisible(true);
 		GridBagConstraints c = new GridBagConstraints();		
 		
 		frame =  new JFrame();
 		frame.setContentPane(overViewPanel);
 		frame.pack();
 		frame.setVisible(true);
 		
 		headLine = new JLabel(meeting.getTitle());
 		c.gridx = 0;
 		c.gridy = 0;
 		headLine.setPreferredSize(new Dimension(300, 25));
 		headLine.setFont(new Font(headLine.getFont().getName(),headLine.getFont().getStyle(), 20 ));
 		overViewPanel.add(headLine, c);
 
 		lblInfo = new JLabel(getTime() + " p rom: " + getLoc());
 		c.gridx = 0;
 		c.gridy = 1;
 		lblInfo.setPreferredSize(new Dimension(300,25));
 		overViewPanel.add(lblInfo, c);
 		
 		c.insets = new Insets(10,0,10,0);
 		
 		lblMoreInfo = new JLabel("Beskrivelse:");
 		c.gridx = 0;
 		c.gridy = 3;
 		overViewPanel.add(lblMoreInfo, c);
 
 		lblParticipant = new JLabel("Deltakere:");
 		c.gridx = 0;
 		c.gridy = 4;
 		overViewPanel.add(lblParticipant, c);
 
 		lblYourStatus = new JLabel("Din status:");
 		c.gridx = 0;
 		c.gridy = 5;
 		overViewPanel.add(lblYourStatus, c);
 
 		change = new JButton("Endre avtale");
 		c.gridx = 0;
 		c.gridy = 6;
 		overViewPanel.add(change, c);
 		change.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				newAppointment = new NewAppointmentView(meeting);
 				overViewPanel.setVisible(true);
 				frame.setVisible(false);
 			}
 		});
 		
 		lblcreator = new JLabel("Mte leder: ");
 		c.gridx = 0;
 		c.gridy = 2;
 		overViewPanel.add(lblcreator,c);
 		
 		creator = new JLabel(meeting.getCreator().getFirstName() + " " + meeting.getCreator().getLastName());
 		c.gridx = 1;
 		c.gridy = 2;
 		overViewPanel.add(creator, c);
 
 		delete = new JButton("Slett avtale");
 		c.gridx = 1;
 		c.gridy = 6;
 		overViewPanel.add(delete, c);
 		delete.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				calendarModel.removeMeeting(meeting);
 				frame.setVisible(false);
 			}
 		});
 		
 		if(meeting.getCreator().getUsername().equals(calendarModel.getUser().getUsername()) == false ){
 			delete.setEnabled(false);
 			change.setEnabled(false);
 		}
 
 		yourStatus = new JComboBox<ImageIcon>();
 		yourStatus.addItem(check);
 		yourStatus.addItem(cross);
 		yourStatus.addItem(question);
 		c.gridx = 1;
 		c.gridy = 5;
 		overViewPanel.add(yourStatus, c);
 		c.gridx = 2;
 		c.gridy = 5;
 		lblStatus = new JLabel();
 		lblStatus.setPreferredSize(new Dimension(70, 25));
 		overViewPanel.add(lblStatus, c);
 		if (userNotification != null ) {
 			if (userNotification.getApproved() == 'y') {
 				yourStatus.setSelectedItem(check);
 				lblStatus.setText("Deltar");
 			}
 			if (userNotification.getApproved() == 'n') {
 				yourStatus.setSelectedItem(cross);
 				lblStatus.setText("Deltar Ikke");
 			}
 			if (userNotification.getApproved() == 'w') {
 				yourStatus.setSelectedItem(question);
 				lblStatus.setText("Vet Ikke");
 			}
 		}
 		
		if(!meeting.getTeam().getMembers().contains(calendarModel.getUser())){
			yourStatus.setEnabled(false);
		}
		
 		if(calendarModel.getUser().getUsername().equals(meeting.getCreator().getUsername()) ){
 			change.setEnabled(true);
 			delete.setEnabled(true);
 			yourStatus.setEnabled(true);
 		}
 		
 		yourStatus.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (yourStatus.getSelectedItem() == check) {
 					lblStatus.setText("Deltar");
 					calendarModel.setStatus('y',userNotification);
 					closeFrame();
 				}
 				if (yourStatus.getSelectedItem() == cross) {
 					lblStatus.setText("Deltar Ikke");
 					calendarModel.setStatus('n',userNotification);
 					closeFrame();
 				}
 				if (yourStatus.getSelectedItem() == question) {
 					lblStatus.setText("Ikke svart");
 					calendarModel.setStatus('w',userNotification);
 					closeFrame();
 				}
 			}
 		});
 
 		moreInfo = new JTextArea(meeting.getDescription());
 		moreInfo.setEditable(false);
 		moreInfo.setFocusable(false);
 		moreInfo.setPreferredSize(new Dimension(320, 100));
 		c.gridx = 1;
 		c.gridy = 3;
 		overViewPanel.add(moreInfo, c);
 
 		listModel = new DefaultListModel();
 		for (int i = 0; i < notifications.size(); i++) {
 			listModel.addElement(notifications.get(i));
 		}
 		participantList = new JList<Notification>();
 		participantList.setModel(listModel);
 		participantList.setFixedCellWidth(300);
 		participantList.setCellRenderer(new overViewRender());
 		JScrollPane myJScrollPane = new JScrollPane(participantList);
 		myJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		c.gridx = 1;
 		c.gridy = 4;
 		overViewPanel.add(myJScrollPane, c);
 	}
 
 	private String getTime() {
 		GregorianCalendar cal = new GregorianCalendar();
 		GregorianCalendar cal2 = new GregorianCalendar();
 		cal.setTimeInMillis(meeting.getStartTime());
 		cal2.setTimeInMillis(meeting.getEndTime());
 		SimpleDateFormat spl1 = new SimpleDateFormat("dd.MMMM");
 		SimpleDateFormat spl2 = new SimpleDateFormat("HH:mm");
 		String time = spl1.format(cal.getTime()) + ". Fra kl "
 				+ spl2.format(cal.getTime()) + " til "
 				+ spl2.format(cal2.getTime());
 		return time;
 	}
 
 	private String getLoc() {
 		return meeting.getLocation();
 	}
 
 	public JPanel getPanel() {
 		return overViewPanel;
 	}
 	
 	public void showFrame(){
 		frame.setVisible(true);
 	}
 	
 	public void closeFrame(){
 		System.out.println("DAVIVVIVIIVIVD");
 		frame.setVisible(false);
 	}
 
 }
