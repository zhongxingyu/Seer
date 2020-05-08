 package gui;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.swing.*;
 
 import org.jdesktop.swingx.JXDatePicker;
 
 import core.CalendarProgram;
 import db.Appointment;
 import db.MeetingPoint;
 import db.User;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 public class EditAppointmentPanel extends JPanel implements ActionListener{
 	
 	protected Appointment appointment;
 	protected JXDatePicker startPick;
 	protected JXDatePicker endPick;
 	protected JTextArea descriptionArea;
 	protected JTextField titleField;
 	protected JTextField startField;
 	protected JTextField endField;
 	protected JCheckBox meetingBox;
 	protected JButton saveAppButton;
 	protected JButton cancelButton;
 	protected boolean approved = false;
 	protected CalendarProgram cp;
 	protected JCheckBox alarmBox;
 	protected JTextField alarmValueField;
 	protected JComboBox valueTypePick;
 	protected JLabel beforeStartLabel;
 	protected MeetingPanel meetingPanel;
 	protected JButton saveButton;
 	protected JButton deleteButton;
 	
 	public EditAppointmentPanel(CalendarProgram cp, Appointment appointment, Boolean firstTime) {
 		
 		//creates a default appointment object based on today, with unique id
 		this.appointment = appointment.getCopy();
 		//reference to the main program
 		this.cp=cp;
 		setBackground(Color.LIGHT_GRAY); 
 		//Pick start
 		startPick = new JXDatePicker();
 		startPick.addActionListener(this);
 		startPick.setDate(new Date());
 		
 		//title label
 		JLabel titleLabel;
 			titleLabel = new JLabel("Title: ");
 			
 		//title field
 		titleField = new JTextField();
 		titleField.addActionListener(this);
 		titleField.setActionCommand("Title");
 		titleField.setColumns(10);
 		
 		//start label
 		JLabel startLabel = new JLabel("Start:");
 		startLabel.setVerticalAlignment(SwingConstants.TOP);
 		
 		//pick end
 		endPick = new JXDatePicker();
 		endPick.addActionListener(this);
 		endPick.setDate(new Date());
 		
 		//set start time
 		startField = new JTextField();
 		startField.setToolTipText("HH:MM");
 		//TODO format properly
 		startField.setText(appointment.getStart().get(Calendar.HOUR)+":"+appointment.getStart().get(Calendar.MINUTE));
 		startField.addActionListener(this);
 		startField.setActionCommand("Start time");
 		startField.setColumns(6);
 		
 		//end label
 		JLabel endLabel = new JLabel("End:");
 		endLabel.setVerticalAlignment(SwingConstants.TOP);
 		
 		//set end time
 		endField = new JTextField();
 		endField.setToolTipText("HH:MM");
 		//TODO format properly
 		endField.setText(appointment.getEnd().get(Calendar.HOUR)+":"+appointment.getEnd().get(Calendar.MINUTE));
 		startField.addActionListener(this);
 		startField.setActionCommand("End time");
 		endField.setColumns(6);
 		
 		//description label
 		JLabel descriptionLabel = new JLabel("Description:");
 		
 		//description textarea
 		descriptionArea = new JTextArea();
 		startField.setActionCommand("Start time");
 		descriptionArea.setColumns(35);
 		descriptionArea.setRows(4);
 		
 		//checkbox for alarms
 		alarmBox = new JCheckBox("Alarm");
 		alarmBox.addActionListener(this);
 		alarmBox.setActionCommand("alarm");
 		
 		//field for the value you want
 		alarmValueField = new JTextField();
 		alarmValueField.setText("10");
 		alarmValueField.setVisible(false);
 		
 		beforeStartLabel = new JLabel("Before Start:");
 		beforeStartLabel.setVisible(false);
 		alarmValueField.setColumns(3);
 		
 		//pick what type the value should representent e.g. hours, minutes days.
 		String[] valueTypes = {"Minutes", "Hours", "Days"};
 		valueTypePick = new JComboBox(valueTypes);
 		valueTypePick.setVisible(false);
 		
 		
 		//checkbox for the meetings
 		meetingBox = new JCheckBox("Meeting");
 		meetingBox.addActionListener(this);
 		meetingBox.setActionCommand("Meeting");
 		
 		//Add and hide meetingPanel
 		meetingPanel = new MeetingPanel(cp.getCachedUsers(), appointment.getParticipants());
 		meetingPanel.setVisible(false);
 		
 		//add or save appointment
 			saveAppButton = new JButton("Save");
 			saveAppButton.addActionListener(this);
 			saveAppButton.setActionCommand("Save");					
 		
 		//delete appointment
 		System.out.println("we are in an edit appointment panel!");
 		deleteButton = new JButton("Delete");
 		deleteButton.setForeground(Color.BLACK);
 		deleteButton.addActionListener(this);
 		deleteButton.setActionCommand("Delete");
 		
 		//cancel appointment
 		cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(this);
 		cancelButton.setActionCommand("Cancel");
 		GroupLayout groupLayout = new GroupLayout(this);
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(meetingBox)
 							.addContainerGap(412, Short.MAX_VALUE))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(descriptionLabel)
 							.addContainerGap(417, Short.MAX_VALUE))
 						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
 							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 										.addGroup(groupLayout.createSequentialGroup()
 											.addComponent(alarmBox, GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
 											.addGap(186))
 										.addGroup(groupLayout.createSequentialGroup()
 											.addComponent(alarmValueField, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
 											.addPreferredGap(ComponentPlacement.RELATED)))
 									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 										.addComponent(valueTypePick, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE)
 										.addGroup(groupLayout.createSequentialGroup()
 											.addGap(8)
 											.addComponent(beforeStartLabel))))
 								.addGroup(groupLayout.createSequentialGroup()
 									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 										.addComponent(titleLabel)
 										.addComponent(startLabel)
 										.addComponent(endLabel))
 									.addGap(142)
 									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 										.addComponent(titleField, GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
 										.addGroup(groupLayout.createSequentialGroup()
 											.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 												.addComponent(endPick, GroupLayout.PREFERRED_SIZE, 227, GroupLayout.PREFERRED_SIZE)
 												.addComponent(startPick, GroupLayout.PREFERRED_SIZE, 227, GroupLayout.PREFERRED_SIZE))
 											.addPreferredGap(ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
 											.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
 												.addComponent(startField, 0, 0, Short.MAX_VALUE)
 												.addComponent(endField, GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)))))
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(saveAppButton, GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
 									.addGap(18)
 									.addComponent(deleteButton, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE)
 									.addGap(18)
 									.addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 144, GroupLayout.PREFERRED_SIZE))
 								.addComponent(meetingPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 							.addGap(41))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(descriptionArea, GroupLayout.PREFERRED_SIZE, 482, GroupLayout.PREFERRED_SIZE)
 							.addContainerGap())))
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGap(14)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(titleField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 										.addComponent(startPick, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 										.addComponent(startField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 										.addComponent(startLabel, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)))
 								.addComponent(titleLabel))
 							.addGap(18)
 							.addComponent(endLabel))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGap(88)
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(endPick, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(endField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
 					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(descriptionLabel)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(descriptionArea, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(alarmBox)
 						.addComponent(beforeStartLabel))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(valueTypePick, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(alarmValueField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(meetingBox)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(meetingPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(cancelButton)
 						.addComponent(saveAppButton)
 						.addComponent(deleteButton))
 					.addGap(62))
 		);
 		setLayout(groupLayout);
 
 		updateFields(appointment);
 		
 	}
 	
 	public void displayTime(JTextField field, Date date){
 		int hours = date.getHours();
 		int minutes = date.getMinutes();
 		
 		//Fix zeros for hour
 		if(hours < 10){
 			field.setText("0" + hours + ":");
 		}
 		else{
 			field.setText(hours + ":");
 		}
 		
 		//Fix zeros for minute
 		if(minutes < 10){
 			field.setText(field.getText() + "0" + minutes);
 		}
 		else{
 			field.setText(field.getText() + minutes);
 		}
 	}
 	
 	public void updateFields(Appointment app) {
 		titleField.setText(app.getTitle());
 		startPick.setDate(app.getStartAsDate()); //?
 		endPick.setDate(app.getEndAsDate());
 		
		displayTime
 		
		endField.setText(app.getEndAsDate().getHours() + ":" + app.getEndAsDate().getMinutes());
 		descriptionArea.setText(app.getDescription());
 		
 		
 		//Update alarmfields
 		boolean b = app.hasAlarm();
 		if(b){
 			alarmBox.setSelected(b);
 			alarmValueField.setVisible(b);
 			valueTypePick.setVisible(b);
 			beforeStartLabel.setVisible(b);
 			
 			
 			int minuteDiff = app.getStartAsDate().getMinutes() - app.getAlarm().getAlarmTime().getTime().getMinutes();
 			int hourDiff = app.getStartAsDate().getHours() - app.getAlarm().getAlarmTime().getTime().getHours();
 			int dayDiff = app.getStartAsDate().getDate() - app.getAlarm().getAlarmTime().getTime().getDate();
 			
 			
 			if(dayDiff != 0){
 				valueTypePick.setSelectedItem("Days");
 				alarmValueField.setText(Integer.toString(dayDiff));
 			}
 			
 			else if(minuteDiff == 0 && dayDiff == 0){
 				valueTypePick.setSelectedItem("Hours");
 				alarmValueField.setText(Integer.toString(hourDiff));
 			}
 			
 			else{
 				valueTypePick.setSelectedItem("Minutes");
 				alarmValueField.setText(Integer.toString(minuteDiff + hourDiff*60));
 			}
 		}
 		
 		//Update meetinginformation
 		boolean a = app.isMeeting();
 		if(a){
 			meetingBox.setSelected(a);
 			meetingPanel.setVisible(a);
 			
 			//Update MeetingPoint
 			meetingPanel.filterPlaces(appointment.getParticipants(), meetingPanel.allPlaces);
 			meetingPanel.comboBox.setSelectedItem(app.getMeetingPoint());
 		}
 	}
 	
 	
 	public void actionPerformed(ActionEvent event) {
 		System.out.println(event.getActionCommand());
 
 		//add alarm options
 		if(event.getActionCommand().equals("alarm")){
 				boolean bool = alarmBox.isSelected();
 				alarmValueField.setVisible(bool);
 				valueTypePick.setVisible(bool);
 				beforeStartLabel.setVisible(bool);
 				//if checkbox gets unchecked it sets alarmfield to null
 				if(!bool)
 					setAlarm(bool);
 		}
 		//cancel the appointment
 		if(event.getActionCommand().equals("Cancel")){
 			cp.displayMainProgram(this);
 		}
 		if(event.getActionCommand().equals("Delete")){
 			String title = "Delete appointment";
 			String mordi = "Are you sure you want to delete this appointment?";
 		    int reply = JOptionPane.showConfirmDialog(cp,mordi,title, JOptionPane.YES_NO_OPTION);
 		    if (reply == JOptionPane.YES_OPTION){
 			cp.deleteAppointment(this.appointment);
 			cp.displayMainProgram(this);
 		    }
 		}
 		//add meeting options
 		if(event.getActionCommand().equals("Meeting")){
 			meetingPanel.setVisible(meetingBox.isSelected());
 		}
 		
 		//add Appointment
 		if(event.getActionCommand().equals("Save") || event.getActionCommand().equals("Add")){
 			
 			//start with a fresh instance
 //			appointment = new Appointment(getUser());
 			
 			//set title
 			appointment.setTitle(titleField.getText());
 			
 			//set dates
 			appointment.setStart(startPick.getDate());
 			appointment.setEnd(endPick.getDate());
 			if(appointment.getStart()==null || appointment.getEnd()==null){
 				approved=false;
 				JOptionPane.showMessageDialog(this, "You must pick dates.","Date error",JOptionPane.ERROR_MESSAGE);
 			} else {
 				approved=true;
 			}
 			
 			//check format and set time
 			if(approved) {
 				appointment.setStart(addTime(startField.getText(), appointment.getStart()));
 				appointment.setEnd(addTime(endField.getText(), appointment.getEnd()));
 			//check that start is before end
 				checkStartVsEndTime();
 			//set description
 				appointment.setDescription(descriptionArea.getText());
 			}
 			//set alarm
 			if(alarmBox.isSelected()) {
 				setAlarm(true);
 			}
 			
 			//meeting
 			if(meetingBox.isSelected()){
 				appointment.setMeeting(true);
 				appointment.setMeetingPoint((MeetingPoint) meetingPanel.comboBox.getSelectedItem());
 				appointment.setParticipants(meetingPanel.getParticipants());
 			} else {
 				appointment.setMeeting(false);
 				appointment.setMeetingPoint(null);
 			}
 			
 			//if approved
 			if(approved) {
 				System.out.println("approved yay!");
 				if (event.getActionCommand().equals("Save")) {
 					System.out.println("updating");
 					cp.updateAppointment(appointment);					
 				} else {
 					System.out.println("creating");
 					cp.addAppointment(appointment);
 				}
 				cp.displayMainProgram(this);
 			}
 		}
 		
 		if(event.getActionCommand().equals("Delete")){
 			cp.displayMainProgram(this);
 			cp.deleteAppointment(appointment);
 			System.out.println("******* sent delete command from EAP *******");
 			//TODO delete-method in CalendarProgram
 		}
 		//cancel the appointment
 		if(event.getActionCommand().equals("Cancel")) {
 			System.out.println("trying to cancel");
 			cp.displayMainProgram(this);
 		}
 	}
 	
 	private void setAlarm(boolean bool) {
 		if(bool){
 			appointment.setAlarm(getAlarmValue());
 		}
 		else
 			appointment.setAlarm(null);
 	}
 	
 	private GregorianCalendar getAlarmValue() {
 		String type = (String) valueTypePick.getSelectedItem();
 		GregorianCalendar alarm = (GregorianCalendar) appointment.getStart().clone();
 		int value = Integer.parseInt(alarmValueField.getText());
 		if(value<0){
 			JOptionPane.showMessageDialog(this, "No negative numbers in alarmfield","Alarm Error",JOptionPane.ERROR_MESSAGE);
 			approved=false;
 			return appointment.getAlarm().getAlarmTime();
 		}
 		if(type.equals("Minute"))
 			alarm.set(Calendar.MINUTE,alarm.get(Calendar.MINUTE) - value);
 		else if(type.equals("Hour"))
 			alarm.set(Calendar.HOUR,alarm.get(Calendar.HOUR) - value);
 		else
 			alarm.set(Calendar.DATE,alarm.get(Calendar.DATE) - value);
 		return alarm;
 		
 	}
 
 	
 	private void checkStartVsEndTime() {
 		if(appointment.getEnd().before(appointment.getStart())){
 			JOptionPane.showMessageDialog(this, "End-date is after start-date","Date error",JOptionPane.ERROR_MESSAGE);
 			approved=false;
 		}
 		approved=true;
 	}
 	
 	private GregorianCalendar addTime(String text, GregorianCalendar date) {
 		String[] time = text.split(":");
 		int hours;
 		int mins;
 		
 		try{
 			checkTimeFormat(time);
 			hours = Integer.parseInt(time[0]);
 			mins = Integer.parseInt(time[1]);
 			if(hours>23||hours<0 || mins >60 || mins<0)
 				throw new IllegalArgumentException();
 			date.set(GregorianCalendar.HOUR,hours);
 			date.set(GregorianCalendar.MINUTE,mins);
 			approved=true;
 		}catch(Exception ex){
 			JOptionPane.showMessageDialog(this, "Wrong time-format","Time Error",JOptionPane.ERROR_MESSAGE);
 			approved=false;
 		}
 		return date;
 	}
 
 
 	private void checkTimeFormat(String[] text) throws IllegalArgumentException{
 		if(text.length!=2)
 			throw new IllegalArgumentException();
 		if(text[1].length()!=2 && text[0].length()!=2)
 			throw new IllegalArgumentException();
 	}
 
 	
 	//Testing
 	/*public static void main(String[] args){
 		JFrame frame = new JFrame();
 		frame.pack();
         frame.setSize (500,630);
         frame.setVisible(true);
 	}*/
 	
 
 }
