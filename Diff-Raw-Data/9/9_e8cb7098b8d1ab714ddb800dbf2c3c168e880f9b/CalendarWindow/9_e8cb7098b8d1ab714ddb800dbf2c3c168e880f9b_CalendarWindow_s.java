 package client.gui.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.util.Date;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 import chronos.DateManagement;
 import chronos.Person;
 import chronos.Singleton;
 import client.gui.GBC;
 import client.gui.GBC.Align;
 import client.gui.MainFrame;
 import client.gui.view.calendarWindowHelper.BoxPanel;
 import client.gui.view.calendarWindowHelper.CalEventListPanel;
 import client.gui.view.calendarWindowHelper.CalEventPanel;
 import client.gui.view.calendarWindowHelper.CalLabel;
 import client.gui.view.calendarWindowHelper.ChangeWeekButton;
 import client.gui.view.calendarWindowHelper.DayPanel;
 import client.gui.view.calendarWindowHelper.NotificationPanel;
 import client.gui.view.calendarWindowHelper.PersonCheckBox;
 import client.model.CalendarModel;
 import client.model.CalendarModel.Weekday;
 import client.model.EventConfigModel.ViewType;
 import client.model.ChronosModel;
 import events.CalEvent;
 
 public class CalendarWindow extends ChronosWindow {
 
 	private CalendarModel model;
 	private JButton newEventButton;
 	private ChangeWeekButton prevButton, nextButton;
 	private JScrollPane eventsPane, othersCalPane, notificationsPane;
 	private BoxPanel eventsPanel, othersCalPanel, notificationsPanel;
 	private DayPanel mondayPanel, tuesdayPanel, wednesdayPanel, thursdayPanel, fridayPanel, saturdayPanel, sundayPanel;
 	private CalLabel dateLbl, weekNumberLbl, mondayLbl, tuesdayLbl, wednesdayLbl, thursdayLbl, fridayLbl, saturdayLbl, sundayLbl;
 	private int eventsPanelHeight = 250;
 	private int eventsPanelWidth = 140;
 	private JTabbedPane tabbedPane;
 	private int notifications;
 
 	public CalendarWindow(ChronosModel model, MainFrame frame) {
 		super(model, frame);
 		setModel(model);
 
 		GridBagLayout gbl = new GridBagLayout();
 		this.setLayout(gbl);
 
 		int i = 0;
 
 		newEventButton = new JButton("New event");
 		newEventButton.setPreferredSize(new Dimension(140, 25));
 		newEventButton.setMinimumSize(new Dimension(140, 25));
 		add(newEventButton, new GBC(i, 0));
 		newEventButton.addActionListener(new NewEventListener());
 
 		tabbedPane = new JTabbedPane();
 		tabbedPane.setBorder(BorderFactory.createEmptyBorder());
 		add(tabbedPane, new GBC(i, 3).setWeight(0, 0.5));
 
 		eventsPanel = new BoxPanel();
 		eventsPane = new JScrollPane(eventsPanel);
 
 		notificationsPanel = new BoxPanel();
 		notificationsPane = new JScrollPane(notificationsPanel);
 
 		tabbedPane.add(eventsPane);
 		tabbedPane.add(notificationsPane);
 		tabbedPane.setTitleAt(0, "Events");
 		tabbedPane.setTitleAt(1, "Invites");
 		tabbedPane.setMaximumSize(new Dimension(eventsPanelWidth + 30, eventsPanelHeight));
 		tabbedPane.setMinimumSize(new Dimension(eventsPanelWidth + 30, eventsPanelHeight));
 		tabbedPane.setPreferredSize(new Dimension(eventsPanelWidth + 30, eventsPanelHeight));
 
 		Border border = BorderFactory.createLineBorder(Color.white, 3);
 		othersCalPanel = new BoxPanel();
 
 		CalLabel othersLbl = new CalLabel("Others");
 
 		othersCalPane = new JScrollPane(othersCalPanel);
 		othersCalPane.setColumnHeaderView(othersLbl);
 		othersCalPane.setPreferredSize(new Dimension(eventsPanelWidth, 250));
 		othersCalPane.setMinimumSize(new Dimension(eventsPanelWidth, 250));
 		othersCalPane.setMaximumSize(new Dimension(eventsPanelWidth, 250));
 		othersCalPane.setBorder(border);
 		add(othersCalPane, new GBC(i, 4).setWeight(0, 0.5));
 
 		i++;
 		i++;
 		i++;
 
 		mondayPanel = new DayPanel();
 		add(mondayPanel, new GBC(i, 3, Align.NOT_RIGHT).setSpan(1, 2));
 
 		mondayLbl = new CalLabel("Monday");
 		add(mondayLbl, new GBC(i, 2).setAnchor(GridBagConstraints.CENTER));
 
 		i++;
 
 		tuesdayPanel = new DayPanel();
 		add(tuesdayPanel, new GBC(i, 3, Align.TOP_AND_BOTTOM).setSpan(1, 2));
 
 		tuesdayLbl = new CalLabel("Tuesday");
 		add(tuesdayLbl, new GBC(i, 2).setAnchor(GridBagConstraints.CENTER));
 
 		i++;
 
 		wednesdayPanel = new DayPanel();
 		add(wednesdayPanel, new GBC(i, 3, Align.TOP_AND_BOTTOM).setSpan(1, 2));
 
 		wednesdayLbl = new CalLabel("Wednesday");
 		add(wednesdayLbl, new GBC(i, 2).setAnchor(GridBagConstraints.CENTER));
 
 		prevButton = new ChangeWeekButton("<");
 		prevButton.addActionListener(new PrevButtonListener());
 		add(prevButton, new GBC(i, 0).setAnchor(GridBagConstraints.EAST).setFill(GridBagConstraints.NONE));
 
 		i++;
 
 		thursdayPanel = new DayPanel();
 		add(thursdayPanel, new GBC(i, 3, Align.TOP_AND_BOTTOM).setSpan(1, 2));
 
 		thursdayLbl = new CalLabel("Thursday");
 		add(thursdayLbl, new GBC(i, 2).setAnchor(GridBagConstraints.CENTER));
 
 		weekNumberLbl = new CalLabel("Week " + this.model.getCurrentDisplayedWeek());
 		add(weekNumberLbl, new GBC(i, 0).setAnchor(GridBagConstraints.CENTER));
 
 		i++;
 
 		fridayPanel = new DayPanel();
 		add(fridayPanel, new GBC(i, 3, Align.TOP_AND_BOTTOM).setSpan(1, 2));
 
 		fridayLbl = new CalLabel("Friday");
 		add(fridayLbl, new GBC(i, 2).setAnchor(GridBagConstraints.CENTER));
 
 		nextButton = new ChangeWeekButton(">");
 		nextButton.addActionListener(new NextButtonListener());
 		add(nextButton, new GBC(i, 0).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE));
 
 		i++;
 
 		saturdayPanel = new DayPanel();
 		add(saturdayPanel, new GBC(i, 3, Align.TOP_AND_BOTTOM).setSpan(1, 2));
 
 		saturdayLbl = new CalLabel("Saturday");
 		add(saturdayLbl, new GBC(i, 2).setAnchor(GridBagConstraints.CENTER));
 
 		dateLbl = new CalLabel(this.model.getCurrentDisplayedDateIntervall());
 		dateLbl.setHorizontalAlignment(SwingConstants.RIGHT);
 		add(dateLbl, new GBC(i, 0).setSpan(2, 1).setAnchor(GridBagConstraints.WEST));
 
 		i++;
 
 		sundayPanel = new DayPanel();
 		add(sundayPanel, new GBC(i, 3, Align.NOT_LEFT).setSpan(1, 2));
 
 		sundayLbl = new CalLabel("Sunday");
 		add(sundayLbl, new GBC(i, 2).setAnchor(GridBagConstraints.CENTER));
 
 		updateLabels();
 	}
 
 	@Override
 	public void setModel(ChronosModel model) {
 		this.model = (CalendarModel) model;
 		this.model.setView(this);
 	}
 
 	public void addOtherPerson(Person person, boolean isSelected) {
 		PersonCheckBox box = new PersonCheckBox(person, isSelected);
 		box.addActionListener(new CheckBoxListener());
 		othersCalPanel.add(box);
 		repaint();
 	}
 
 	/**
 	 * Adds one event
 	 */
 
 	public void addEvent(CalEvent event, Weekday weekday, Color personColor) {
 		CalEventPanel panel = new CalEventPanel(event, this, personColor, model);
 		eventsPanel.add(new CalEventListPanel(event, this, eventsPanelWidth, model));
 		switch (weekday) {
 		case MONDAY:
 			mondayPanel.add(panel);
 			break;
 		case TUESDAY:
 			tuesdayPanel.add(panel);
 			break;
 		case WEDNESDAY:
 			wednesdayPanel.add(panel);
 			break;
 		case THURSDAY:
 			thursdayPanel.add(panel);
 			break;
 		case FRIDAY:
 			fridayPanel.add(panel);
 			break;
 		case SATURDAY:
 			saturdayPanel.add(panel);
 			break;
 		case SUNDAY:
 			sundayPanel.add(panel);
 			break;
 		default:
 			break;
 		}
 
 		getFrame().revalidate();
 		repaint();
 	}
 
 	public void removeEvents() {
 		mondayPanel.removeAll();
 		tuesdayPanel.removeAll();
 		wednesdayPanel.removeAll();
 		thursdayPanel.removeAll();
 		fridayPanel.removeAll();
 		saturdayPanel.removeAll();
 		sundayPanel.removeAll();
 		eventsPanel.removeAll();
 		// updateUI();
 		repaint();
 	}
 
 	public void removeNotifications() {
 		notificationsPanel.removeAll();
 	}
 
 	public void updateLabels() {
 		Date currentDate = model.getCurrentDisplayedDate();
 		weekNumberLbl.setText("Week " + model.getCurrentDisplayedWeek());
 		dateLbl.setText(model.getCurrentDisplayedDateIntervall());
 		mondayLbl.setText("Monday " + DateManagement.getFormattedSimple(currentDate));
 		currentDate = DateManagement.getNextDay(currentDate);
 		tuesdayLbl.setText("Tuesday " + DateManagement.getFormattedSimple(currentDate));
 		currentDate = DateManagement.getNextDay(currentDate);
 		wednesdayLbl.setText("Wednesday " + DateManagement.getFormattedSimple(currentDate));
 		currentDate = DateManagement.getNextDay(currentDate);
 		thursdayLbl.setText("Thursday " + DateManagement.getFormattedSimple(currentDate));
 		currentDate = DateManagement.getNextDay(currentDate);
 		fridayLbl.setText("Friday " + DateManagement.getFormattedSimple(currentDate));
 		currentDate = DateManagement.getNextDay(currentDate);
 		saturdayLbl.setText("Saturday " + DateManagement.getFormattedSimple(currentDate));
 		currentDate = DateManagement.getNextDay(currentDate);
 		sundayLbl.setText("Sunday " + DateManagement.getFormattedSimple(currentDate));
 		currentDate = DateManagement.getNextDay(currentDate);
 		repaint();
 
 	}
 
 	private class NewEventListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			getFrame().getEventModel().setDefaultModel();
 			getFrame().getEventModel().setView(ViewType.NEW);
 		}
 	}
 
 	public JTabbedPane getTabbedPane() {
 		return tabbedPane;
 	}
 
 	public BoxPanel getNotificationsPanel() {
 		return notificationsPanel;
 	}
 
 	private class CheckBoxListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			PersonCheckBox checkBox = (PersonCheckBox) e.getSource();
 			Person person = checkBox.getPerson();
 			System.out.println("Clicked: " + checkBox.getPerson());
 			if (checkBox.isSelected()) {
 				model.addSelectedPerson(person);
 			} else {
 				model.removeSelectedPerson(person);
 				model.update();
 			}
 		}
 	}
 
 	private class PrevButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			model.prevWeek();
 			model.update();
 		}
 
 	}
 
 	private class NextButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			model.nextWeek();
 			model.update();
 		}
 	}
 
 	public void setNotifications(int notifications) {
 		this.notifications = notifications;
 	}
 
 	public int getNotifications() {
 		return notifications;
 	}
 
 	public void addNotification(CalEvent calEvent) {
 		notificationsPanel.add(new NotificationPanel(calEvent, this, eventsPanelWidth));
 		notifications++;
 	}
 
 	/**
 	 * bare tull
 	 */
 	public void internalRepaint() {
 		repaint();
 	}
 
 	public void removePersonCheckBoxes() {
 		othersCalPanel.removeAll();
 		repaint();
 	}
 
 	public synchronized void alarm(CalEvent calEvent) {
 		JOptionPane.showMessageDialog(null, "Less than 15 minutes until " + calEvent.getTitle() + " starts", Singleton.APP_NAME + " - Reminder", JOptionPane.WARNING_MESSAGE);
 	}
 }
