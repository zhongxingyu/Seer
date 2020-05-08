 package framePackage;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.border.LineBorder;
 
 import data.CalendarModel;
 import data.Meeting;
 import data.Notification;
 import data.Person;
 
 public class AppointmentView implements PropertyChangeListener {
 
 	private ArrayList<Meeting> meetings;
 	private ArrayList<Notification> notifications;
 	private Person user;
 	private JPanel meetingPanel;
 	private JPanel mainPanel;
 	private JPanel legendPanel;
 	private JPanel headerPanel;
 	private GridBagConstraints mc;
 	private CalendarModel calendarModel;
 	private JScrollPane jsp;
 	private ArrayList<Integer> addedMeetings;
 	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
 			"dd.MMM.yyyy");
 	private static final SimpleDateFormat timeFormat = new SimpleDateFormat(
 			"HH:mm");
 	private static final int[] sizes = { 50, 100, 50, 300, 100, 90, 90 };
 
 	public AppointmentView(CalendarModel calendarModel) {
 		this.calendarModel = calendarModel;
 		calendarModel.addPropertyChangeListener(this);
 		mainPanel = new JPanel();
 		meetingPanel = new JPanel();
 		meetingPanel.setLayout(new GridBagLayout());
 
 		jsp = new JScrollPane(meetingPanel);
 		jsp.setPreferredSize(new Dimension(800,500));
 		createHeaders();
 		createLegend();
 		mainPanel.add(headerPanel, BorderLayout.NORTH);
 		mainPanel.add(jsp, BorderLayout.CENTER);
 		mainPanel.add(legendPanel, BorderLayout.SOUTH);
 		mainPanel.setBorder(new LineBorder(Color.black));
 		mainPanel.setPreferredSize(new Dimension(814, 457));
 	}
 
 	private void createLegend() {
 		legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
 		JLabel leader = new JLabel(": Mteleder");
 		leader.setIcon(new ImageIcon("res/icons/icon_star.png"));
 		JLabel participates = new JLabel(": Deltar");
 		participates.setIcon(new ImageIcon("res/icons/icon_check.png"));
 		JLabel notParticipates = new JLabel(": Deltar ikke");
 		notParticipates.setIcon(new ImageIcon("res/icons/icon_cross.png"));
 		JLabel unanswered = new JLabel(": Venter svar");
 		unanswered.setIcon(new ImageIcon("res/icons/icon_question.png"));
 		legendPanel.add(leader);
 		legendPanel.add(participates);
 		legendPanel.add(notParticipates);
 		legendPanel.add(unanswered);
 		legendPanel.setPreferredSize(new Dimension(800, 30));
 		legendPanel.setBorder(new LineBorder(Color.black));
 	}
 
 	private void createHeaders() {
 		headerPanel = new JPanel(new GridBagLayout());
 		JLabel[] headers = new JLabel[7];
 		headers[0] = new JLabel("Status");
 		headers[1] = new JLabel("Dato");
 		headers[2] = new JLabel("Tid");
 		headers[3] = new JLabel("Avtale");
 		headers[4] = new JLabel("Sted");
 		headers[5] = new JLabel("Endre");
 		headers[6] = new JLabel("Mer info");
 
 		mc = new GridBagConstraints();
 		mc.gridy = 0;
 		mc.anchor = GridBagConstraints.LINE_END;
 		for (int i = 0; i < headers.length; i++) {
 			headers[i].setPreferredSize(new Dimension(sizes[i], 20));
 			mc.gridx += i;
 			headerPanel.add(headers[i]);
 		}
 	}
 
 	private void refreshMeetings() {
 		mainPanel.remove(legendPanel);
 		mainPanel.remove(jsp);
 		jsp.remove(meetingPanel);
 		meetingPanel = new JPanel(new GridBagLayout());
 		jsp = new JScrollPane(meetingPanel);
 		jsp.setPreferredSize(new Dimension(800,500));
 		mainPanel.add(jsp, BorderLayout.CENTER);
 		mainPanel.add(legendPanel,BorderLayout.SOUTH);
 		mc.anchor = GridBagConstraints.NORTHWEST;
 		mc.gridy = 0;
 		for (int i = 0; i < notifications.size(); i++) {
 		 Notification n = notifications.get(i);
 		 Meeting meeting = n.getMeeting();
 		 addedMeetings.add(meeting.getMeetingID());
 		 JComponent[] items = new JComponent[7];
 		 	if (!meeting.getCreator().getUsername().equals(user.getUsername())) {
 				switch (n.getApproved()) {
 				case 'w':
 					items[0] = new JLabel(new ImageIcon("res/icons/icon_question.png"));
 					break;
 				case 'y':
 					items[0] = new JLabel(new ImageIcon("res/icons/icon_check.png"));
 					break;
 				case 'n':
 					items[0] = new JLabel(new ImageIcon("res/icons/icon_cross.png"));
 					break;
 				}
 			} else {
 				items[0] = new JLabel(new ImageIcon("res/icons/icon_star.png"));
 			}
 			if (meetings.size() == 0 && i == notifications.size()-1) {
 				mc.weighty = 1;
 			}
 			JButton changeBtn = new JButton("endre");
 			JButton infoBtn = new JButton("mer info...");
 			
 			items[1] = new JLabel();
 			items[2] = new JLabel();
 			items[3] = new JLabel(meeting.getTitle());
 			items[4] = new JLabel(meeting.getLocation());
 			items[5] = changeBtn;
 			items[6] = infoBtn;
 
 			GregorianCalendar startDate = new GregorianCalendar();
 			startDate.setTimeInMillis(meeting.getStartTime());
 			((JLabel) items[1]).setText(dateFormat.format(startDate.getTime()));
 			((JLabel) items[2]).setText(timeFormat.format(startDate.getTime()));
 			
 			if(meeting.getCreator().getUsername().equals(user.getUsername())) {
 				changeBtn.addActionListener(new ChangeButtonListener(meeting));
 			} else {
 				changeBtn.setEnabled(false);
 			}
 			infoBtn.addActionListener(new InfoButtonListener(meeting));
 			mc.gridy = mc.gridy +1;
 			for (int j = 0; j < items.length; j++) {
 				JComponent item = items[j];
 				item.setPreferredSize(new Dimension(sizes[j], 20));
 				mc.gridx = j;
 				meetingPanel.add(item, mc);
 			}
 			mc.weightx = 0;
 		 }
 		
 		for (int i = 0; i < meetings.size(); i++) {
 			Meeting meeting = meetings.get(i);
			if(addedMeetings.contains(meeting.getMeetingID())) continue;
 			JComponent[] items = new JComponent[7];
 			if (i == meetings.size()-1) {
 				mc.weighty = 1;
 			}
 			
 			JButton changeBtn = new JButton("endre");
 			JButton infoBtn = new JButton("mer info...");
 			
 			items[0] = new JLabel(new ImageIcon("res/icons/icon_star.png"));
 			items[1] = new JLabel();
 			items[2] = new JLabel();
 			items[3] = new JLabel(meeting.getTitle());
 			items[4] = new JLabel(meeting.getLocation());
 			items[5] = changeBtn;
 			items[6] = infoBtn;
 
 			GregorianCalendar startDate = new GregorianCalendar();
 			startDate.setTimeInMillis(meeting.getStartTime());
 			((JLabel) items[1]).setText(dateFormat.format(startDate.getTime()));
 			((JLabel) items[2]).setText(timeFormat.format(startDate.getTime()));
 
 			if(meeting.getCreator().getUsername().equals(user.getUsername())) {
 				changeBtn.addActionListener(new ChangeButtonListener(meeting));
 			} else {
 				changeBtn.setEnabled(false);
 			}
 			infoBtn.addActionListener(new InfoButtonListener(meeting));
 			
 			mc.gridy = mc.gridy +1;
 			for (int j = 0; j < items.length; j++) {
 				JComponent item = items[j];
 				item.setPreferredSize(new Dimension(sizes[j], 20));
 				mc.gridx = j;
 				meetingPanel.add(item, mc);
 			}
 			mc.weightx = 0;
 		}
 		mc.weighty = 0;
 		mainPanel.validate();
 		
 	}
 	private class ChangeButtonListener implements ActionListener{
 		private Meeting meeting;
 		
 		public ChangeButtonListener(Meeting meeting) {
 			this.meeting = meeting;
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			new NewAppointmentView(meeting);
 		}
 		
 	}
 	private class InfoButtonListener implements ActionListener{
 		private Meeting meeting;
 		
 		public InfoButtonListener(Meeting meeting) {
 			this.meeting = meeting;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			new AppointmentOverView(meeting);
 		}
 		
 	}
 
 	public JPanel getPanel() {
 		return mainPanel;
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		switch (evt.getPropertyName()) {
 		case CalendarModel.CALENDAR_LOADED_Property:
 			addedMeetings = new ArrayList<Integer>();
 			user = calendarModel.getUser();
 			notifications = calendarModel.getAllNotificationsOfPerson(user);
 			meetings = calendarModel.getAppointments(true);
 			refreshMeetings();
 			break;
 		case CalendarModel.MEETINGS_CHANGED_Property:
 			addedMeetings = new ArrayList<Integer>();
 			user = calendarModel.getUser();
 			notifications = calendarModel.getAllNotificationsOfPerson(user);
 			meetings = calendarModel.getAppointments(true);
 			refreshMeetings();
 			break;
 		case CalendarModel.NOTIFICATIONS_CHANGED_Property:
 			addedMeetings = new ArrayList<Integer>();
 			user = calendarModel.getUser();
 			notifications = calendarModel.getAllNotificationsOfPerson(user);
 			meetings = calendarModel.getAppointments(true);
 			refreshMeetings();
 			break;
 		}
 
 	}
 }
