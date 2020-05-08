 /**
  * The DayPanel is the panel on the left hand side of the application underneath the calendar. 
  * It holds buttons to add/remove patients, add/remove practitioners, change the time slot 
  * for the day, and so forth. It also contains a table holding the names of patients with 
  * appointments. 
  */
 
 package gui.main;
 
 import gui.sub.SelectPatientUI;
 import gui.sub.SelectPractitionerUI;
 import gui.sub.SelectTimeSlotUI;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import backend.DataService.DataServiceImpl;
 import backend.DataTransferObjects.DayDto;
 
 import java.util.Date;
 import java.util.List;
 
 import backend.DataTransferObjects.PatientDto;
 import backend.DataTransferObjects.PractitionerDto;
 import backend.DataTransferObjects.SchedulePractitionerDto;
 import gui.TimeSlot;
 
 public class DayPanel extends JPanel {
 	/** The side panel with the time slot information for the day panel. */
 	SidePanel sidePanel;
 	
 	/** The day associated with the day panel; each day has its own day panel. */
 	private DayDto day;
 	/** The list of practitioners scheduled for the day. */
 	private List<SchedulePractitionerDto> schedulePractitionerList;
 	private JButton switchViewButton = new PanelButton("Month View");
 	private JButton patientButton = new PanelButton("Schedule Patient");
 	private JButton addPracButton = new PanelButton("Schedule Practitioner");
 	private JButton removePracButton = new PanelButton("Remove Practitioner From Current Day");
 	private JButton setTimeSlotButton = new PanelButton("Hours of Operation");
 	private JButton searchButton = new PanelButton("Search");
 	private JButton apptConfirmationButton = new PanelButton("Appointment Confirmations");
 	private JButton waitListButton = new PanelButton("Wait List");
 	
 	/** The panel that displays a scheduled practitioner and the appointments that practitioner has for the day. */
 	private RoomPanel rp;
 	private AppointmentBlock ab;
 	private AppointmentSubpanel as;
 	private MainWindow mw;
 	
 	/** Constructs a day panel given a day object and the main window. */
 	public DayPanel(DayDto day, MainWindow mw) {
 		this.mw = mw;
 		setBackground(Color.WHITE);
 		this.day = day;
 		
 		switchViewButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		patientButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		addPracButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		removePracButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		setTimeSlotButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		searchButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		apptConfirmationButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		waitListButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		
 		
 		//We don't want these focusable, so they won't disrupt the focus of the main calendar area
 		switchViewButton.setAction(switchViewAction);
 		switchViewButton.setFocusable(false);
 		switchViewButton.setActionCommand("switchView");
 		patientButton.setAction(addPatAction);
 		patientButton.setFocusable(false);
 		patientButton.setEnabled(false);
 		patientButton.setActionCommand("patient");
 		addPracButton.setFocusable(false);
 		addPracButton.setAction(addPracAction);
 		addPracButton.setActionCommand("addPrac");
 		removePracButton.setAction(removePracAction);
 		removePracButton.setFocusable(false);
 		removePracButton.setEnabled(false);
 		removePracButton.setActionCommand("removePrac");
 		setTimeSlotButton.setAction(changeTimeSlotAction);
 		setTimeSlotButton.setFocusable(false);
 		setTimeSlotButton.setActionCommand("setTimeSlot");
 		searchButton.setAction(searchAction);
 		searchButton.setFocusable(false);
 		searchButton.setActionCommand("search");
 		apptConfirmationButton.setAction(apptConfirmationAction);
 		apptConfirmationButton.setFocusable(false);
 		apptConfirmationButton.setActionCommand("apptConfirmation");
 		waitListButton.setAction(waitListAction);
 		waitListButton.setFocusable(true);
 		waitListButton.setActionCommand("waitList");
 		
 		JPanel buttonPanel = new JPanel(new GridLayout(0,1));
 		
 		buttonPanel.add(switchViewButton);
 		buttonPanel.add(addPracButton);
 		buttonPanel.add(removePracButton);
 		buttonPanel.add(patientButton);
 		buttonPanel.add(apptConfirmationButton);
 		buttonPanel.add(waitListButton);
 		buttonPanel.add(searchButton);
 		buttonPanel.add(setTimeSlotButton);
 		
 		
 		add(buttonPanel);
 	}
 	
 	/** Returs the day associated with the day panel. */
 	public DayDto getDay() {
 		return day;
 	}
 	
 	/**
 	 * Used by the AppointmentSubpanel to register itself. Useful for 
 	 * rebuilding the subpanel
 	 * @param as the AppointmentSubpanel
 	 */
 	public void registerAppointmentSubpanel(AppointmentSubpanel as) {
 		this.as = as;
 	}
 	
 	/** Sets the "remove practitioner" button as clickable. */
 	public void setRemovePracButtonEnabled(boolean b, RoomPanel rp) {
 		this.rp = rp;
 		removePracButton.setEnabled(b);
 	}
 	
 	/** Sets the "schedule patient" button as clickable. */
 	public void setPatButtonEnabled(boolean b, AppointmentBlock a) {
 		ab = a;
 		patientButton.setEnabled(b);
 		if (b) {
 			if (ab.getAppointment().getPatientID() != null)
 				patientButton.setAction(removePatAction);
 			else {
 				patientButton.setAction(addPatAction);
 			}
 		}
 		
 	}
 
 	/** Assigns the side panel containing time slot information to this day panel. */
 	public void setSidePanel(SidePanel sidePanel) {
 		this.sidePanel = sidePanel;
 	}
 	
 	/** Sets the time slots in the side panel for the day. */
 	private void setTimeSlot(TimeSlot timeSlot) {
 		if (timeSlot == null) return;
 		DataServiceImpl.GLOBAL_DATA_INSTANCE.setHoursForDay(day, timeSlot.getStartTime(), timeSlot.getEndTime());
 		day.setEnd(timeSlot.getEndTime());
 		day.setStart(timeSlot.getStartTime());
 		as.resetHours(timeSlot);
 		// Edit the practitioner's times
 		sidePanel.refreshTimeSlot(timeSlot);
 	}
 	
 	/** Removes a practitioner from the day's schedule and cancels their appointments. */
 	public void clearRoom(RoomPanel panel) {
 		
 		if (JOptionPane.showConfirmDialog(mw, "Are you sure you want to remove this practitioner from the schedule? \nThis will cancel any appointments that have been set for this day.", "Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 			if (panel == null) return;
 			DataServiceImpl.GLOBAL_DATA_INSTANCE.removePractitionerFromDay
 				(panel.getRoom().getPractSchedID(), day);
 			as.removeRoom(panel.getRoom());
 		}
 	}
 	
 	
 	/** 
 	 * Checks whether currently in month view, and determines the correct text to display for switching views.  
 	 * @author Aakash
 	 * */
 	public void isMonthViewValidate(){
 		if (mw.inMonthView()) switchViewButton.setText("<html>Switch to <br>Day View</html>");
 		else switchViewButton.setText("<html>Switch to <br>Month View</html>");
 	}
 
 	
 	/** Removes a scheduled practitioner from the day upon clicking the "remove practitioner" button. */
 	private final AbstractAction removePracAction = new AbstractAction("<html>Remove Practitioner<br>From Current Day</html>") {
 		public void actionPerformed(ActionEvent e) {
 			
 			clearRoom(rp);
 		}
 	};
 	
 	/** Determines the correct text to display for switching between day and month views. */
 	public final AbstractAction switchViewAction = new AbstractAction("<html>Switch to Month View</html>") {
 		public void actionPerformed(ActionEvent e) {
 			if (mw.inMonthView()) switchViewButton.setText("<html>Switch to <br>Month View</html>");
 			else switchViewButton.setText("<html>Switch to <br>Day View</html>");
 			mw.switchView();
 			DayPanel dp= mw.getDayPanel();
 			dp.isMonthViewValidate();
 		}
 	};
 	
 	/** Shows the warning dialog when attempting to cancel an appointment. */
 	private final AbstractAction removePatAction = new AbstractAction("<html>Cancel Appointment</html>") {
 		public void actionPerformed(ActionEvent e) {
 			AppointmentBlock block = ab;
 			if (JOptionPane.showConfirmDialog(mw, "Are you sure you'd like to cancel this appointment?", "Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) block.clearAppt();
 		}
 	};
 	
 	/** Shows the dialog for scheduling a patient to an appointment. */
 	private final AbstractAction addPatAction = new AbstractAction("<html>Schedule Patient</html>") {
 		public void actionPerformed(ActionEvent e) {
			AppointmentBlock onThisAppt = ab;
 			PatientDto patient = SelectPatientUI.ShowDialog(ab.getParent());
 			if (patient != null) {
				onThisAppt.setPatient(patient.getPatID());
 			}
 		}
 	};
 	
 	/** Shows the dialog for scheduling a practitioner on a day. */
 	private final AbstractAction addPracAction = new AbstractAction("<html>Schedule Practitioner</html>") {
 		public void actionPerformed(ActionEvent e) {
 			PractitionerDto p = SelectPractitionerUI.ShowDialog(mw);
 			if (p==null) return;
 			SchedulePractitionerDto room = DataServiceImpl.GLOBAL_DATA_INSTANCE.addPractitionerToDay(
 					p, day, day.getStart(), day.getEnd());
                         if (room != null){
                             as.addRoom(room);
                         }
 		}
 	};
 	
 	/** Shows the dialog for changing the hours of operation for a given day. */
 	private final AbstractAction changeTimeSlotAction = new AbstractAction("<html>Hours of Operation</html>") {
 		public void actionPerformed(ActionEvent e) {
 			setTimeSlot(SelectTimeSlotUI.ShowDialog(mw));
 		}
 	};
 	
 	// TODO: Add search button functionality
 	/** Determines the correct text to display when the search split pane is active. */
 	private final AbstractAction searchAction = new AbstractAction("<html>Search</html>") {
 		public void actionPerformed(ActionEvent e) {
 			if (mw.showingSearch()) {
 				searchButton.setText("<html>Search</html>");
 			} else {
 				searchButton.setText("<html>Hide Search</html>");
 				apptConfirmationButton.setText("<html>Appointment <br> Confirmation</html>");
 				waitListButton.setText("<html>Wait List</html>");
 			}
 			mw.toggleSearch();
 		}
 	};
 	
 	// TODO: Add no shows functionality
 	/** Determines the correct text to display when the appointment confirmation split pane is active. */
 	private final AbstractAction apptConfirmationAction = new AbstractAction("<html>Appointment <br> Confirmation</html>") {
 		public void actionPerformed(ActionEvent e) {
 			if (mw.showingApptConfirmation()) {
 				apptConfirmationButton.setText("<html>Appointment <br> Confirmation</html>");
 			} else {
 				apptConfirmationButton.setText("<html>Hide Appointment <br> Confirmation</html>");
 				searchButton.setText("<html>Search</html>");
 				waitListButton.setText("<html>Wait List</html>");
 			}
 			mw.toggleApptConfirmation();
 		}
 	};
 	
 	/** Determines the correct text to display when the waitlist split pane is active. */
 	private final AbstractAction waitListAction = new AbstractAction("<html>Wait List</html>") {
 		public void actionPerformed(ActionEvent e) {
 			if (mw.showingWaitList()) {
 				waitListButton.setText("<html>Wait List</html>");
 			}
 			else {
 				waitListButton.setText("<html>Hide Wait List</html>");
 				searchButton.setText("<html>Search</html>");
 				apptConfirmationButton.setText("<html>Appointment <br> Confirmation</html>");
 			}
 			mw.toggleWaitList();
 		}
 	};
 	
 	/** Changes the text to display if the waitlist pane is currently active. */
 	public void showingWaitList() {
 		if (mw.showingWaitList()) waitListButton.setText("<html>Hide Wait List</html>");
 	}
 	
 }
