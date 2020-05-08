 package framePackage;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 
 import client.Program;
 
 @SuppressWarnings("serial")
 public class DefaultView extends JPanel {
 
 	// her er main frame work, som kan brukes p�year og month views ogs�
 	private JFrame frame;
 	private JLabel lblday;
 	private JLabel lblcalendar;
 	private JToggleButton calendar;
 	private JToggleButton meeting;
 	private ButtonGroup calendarSelect;
 	private JButton logOut;
 	private JPanel sharedCalendar;
 
 	private JPanel timePanel;
 	private JLabel calendarTitle;
 	private JButton prevBtn;
 	private JButton nextBtn;
 	private ButtonGroup dayWeekMonthSelect;
 	private JToggleButton dayBtn;
 	private JToggleButton weekBtn;
 	private JToggleButton monthBtn;
 	private CalendarView mainView;
 	public DayView dayView;
 	public WeekView weekView;
 	public MonthView monthView;
 	private SharedCalendarView sharedCView;
 	private AppointmentView appointmentView;
 	private NotiPanelView notiPanel;
 	private Dato dato;
 	private JComponent calendarPanel;
 	private GridBagConstraints timePanelContraints;
 
 
 	private GridBagConstraints backGroundConstraints;
 
 
 
 	public DefaultView(String username) {
 		dato = new Dato();
 		dayView = new DayView(Program.calendarModel);
 		weekView = new WeekView(Program.calendarModel);
 		monthView = new MonthView(Program.calendarModel);
		notiPanel = new NotiPanelView();
 		sharedCView = new SharedCalendarView(Program.calendarModel);
 		appointmentView = new AppointmentView(Program.calendarModel);
 		initialize();
 		Program.calendarModel.init(username);
 	}
 
 	private void initialize() {
 		frame = new JFrame();
 		frame.setLayout(new GridBagLayout());
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		backGroundConstraints = new GridBagConstraints();
 		backGroundConstraints.anchor = GridBagConstraints.LINE_END;
 		lblday = new JLabel("Idag: " + dato.getDate());
 		backGroundConstraints.gridx = 0;
 		backGroundConstraints.gridy = 0;
 		frame.add(lblday, backGroundConstraints);
 
 		calendar = new JToggleButton("Kalender");
 		calendar.setSelected(true);
 		meeting = new JToggleButton("Avtale");
 		calendarSelect = new ButtonGroup();
 		calendarSelect.add(calendar);
 		calendarSelect.add(meeting);
 		calendar.addActionListener(new CalendarMeetingListener());
 		meeting.addActionListener(new CalendarMeetingListener());
 
 		backGroundConstraints.gridx = 1;
 		backGroundConstraints.gridy = 0;
 		backGroundConstraints.weightx = 0.5;
 		backGroundConstraints.anchor = GridBagConstraints.LINE_END;
 		frame.add(calendar, backGroundConstraints);
 		backGroundConstraints.gridx = 2;
 		backGroundConstraints.gridy = 0;
 		backGroundConstraints.anchor = GridBagConstraints.LINE_START;
 		frame.add(meeting, backGroundConstraints);
 		backGroundConstraints.weightx = 0;
 
 		logOut = new JButton("logout");
 		backGroundConstraints.gridx = 3;
 		backGroundConstraints.gridy = 0;
 		logOut.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				new NewAppointmentView(null, Program.calendarModel, null);
 			}
 		});
 		frame.add(logOut, backGroundConstraints);
 		
 		backGroundConstraints.gridx = 0;
 		backGroundConstraints.gridy = 1;
 		frame.add(sharedCView.getPanel(),backGroundConstraints);
 
 		backGroundConstraints.gridx = 0;
 		backGroundConstraints.gridy = 2;
 		frame.add(notiPanel.getPanel(), backGroundConstraints);
 
 		// her i fra kommer selve dayView delen, tar vekk lbltid s�kan dette
 		// brukes p�andre frames
 
 		timePanel = new JPanel(new GridBagLayout());
 
 		timePanel.setBorder(BorderFactory.createLineBorder(Color.black, 2));
 		backGroundConstraints.gridx = 1;
 		backGroundConstraints.gridy = 1;
 		backGroundConstraints.gridheight = 2;
 		backGroundConstraints.gridwidth = 2;
 		backGroundConstraints.fill = GridBagConstraints.BOTH;
 		backGroundConstraints.weightx = 1;
 		backGroundConstraints.weighty = 1;
 		frame.add(timePanel, backGroundConstraints);
 
 		timePanelContraints = new GridBagConstraints();
 
 		mainView = dayView;
 		JPanel prevNextPanel = new JPanel();
 		calendarTitle = new JLabel(mainView.getTitle());
 		calendarTitle.setPreferredSize(new Dimension(150,20));
 		prevBtn = new JButton("<");
 		nextBtn = new JButton(">");
 		PrevNextListener prevNextListener = new PrevNextListener();
 		prevBtn.addActionListener(prevNextListener);
 		nextBtn.addActionListener(prevNextListener);
 		prevNextPanel.add(prevBtn);
 		prevNextPanel.add(calendarTitle);
 		prevNextPanel.add(nextBtn);
 		timePanelContraints.gridx = 0;
 		timePanelContraints.gridy = 0;
 		timePanelContraints.weightx = 1;
 		timePanel.add(prevNextPanel, timePanelContraints);
 
 		dayWeekMonthSelect = new ButtonGroup();
 		DayWeekMonthListener dwmListener = new DayWeekMonthListener(); 
 		dayBtn = new JToggleButton("Dag");
 		weekBtn = new JToggleButton("Uke");
 		monthBtn = new JToggleButton("Måned");
 		dayBtn.addActionListener(dwmListener);
 		weekBtn.addActionListener(dwmListener);
 		monthBtn.addActionListener(dwmListener);
 		dayWeekMonthSelect.add(dayBtn);
 		dayWeekMonthSelect.add(weekBtn);
 		dayWeekMonthSelect.add(monthBtn);
 		//setter inn if her for hva som skal v鎟e selected
 		dayBtn.setSelected(true);
 		timePanelContraints.weightx = 0;
 		timePanelContraints.gridx = 1;
 		timePanelContraints.gridy = 0;
 		timePanel.add(dayBtn, timePanelContraints);
 		timePanelContraints.gridx = 2;
 		timePanelContraints.gridy = 0;
 		timePanel.add(weekBtn, timePanelContraints);
 		timePanelContraints.gridx = 3;
 		timePanelContraints.gridy = 0;
 		timePanel.add(monthBtn, timePanelContraints);
 		//		
 		timePanelContraints.gridwidth = 5;
 		timePanelContraints.fill = GridBagConstraints.BOTH;
 		timePanelContraints.weightx = 1;
 		timePanelContraints.weighty = 1;
 		timePanelContraints.gridx = 0;
 		timePanelContraints.gridy = 1;
 		calendarPanel = mainView.getPanel();
 		timePanel.add(calendarPanel, timePanelContraints);
 		//		
 	}
 
 	public JFrame getFrame() {
 		return frame;
 	}
 	
 
 
 	private class PrevNextListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if(e.getSource() == prevBtn) {
 				mainView.prev();
 			} else {
 				mainView.next();
 			}
 			calendarTitle.setText(mainView.getTitle());
 		}
 	}
 	public void setView(CalendarView view) {
 		if(view == dayView) {
 			dayBtn.setSelected(true);
 		} else if(view == weekView) {
 			weekBtn.setSelected(true);
 		} else if(view == monthView) {
 			monthBtn.setSelected(true);
 		}
 		mainView =  view;
 		calendarTitle.setText(mainView.getTitle());
 		timePanel.remove(calendarPanel);
 		calendarPanel = mainView.getPanel();
 		timePanel.add(calendarPanel, timePanelContraints);
 		timePanel.validate();
 	}
 	
 	private class DayWeekMonthListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if(e.getSource() == dayBtn) {
 				setView(dayView);
 			} else if(e.getSource() == weekBtn) {
 				setView(weekView);
 			} else {
 				setView(monthView);
 			}
 		}
 	}
 	private class CalendarMeetingListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if(e.getSource() == calendar) {
 				frame.remove(appointmentView.getPanel());
 				frame.add(timePanel,backGroundConstraints);
 				timePanel.repaint();
 			} else if(e.getSource() == meeting) {
 				frame.remove(timePanel);
 				frame.add(appointmentView.getPanel(),backGroundConstraints);
 				appointmentView.getPanel().repaint();
 			}
 			frame.validate();
 		}
 	}
 }
