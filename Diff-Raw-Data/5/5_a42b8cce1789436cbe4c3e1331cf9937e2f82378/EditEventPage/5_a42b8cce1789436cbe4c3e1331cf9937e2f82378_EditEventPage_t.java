 /**
  * Name:    Amuthan Narthana and Nicholas Dyszel
  * Section: 2
  * Program: Scheduler Project
  * Date:    11/1/2012
  */
 
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.event.*;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import javax.swing.*;
 
 /**
  * EditEventPage class to display panel for user to create/edit an event
  * 
  * @author Nicholas Dyszel
  * @version 1.0  11/1/2012
  */
 public class EditEventPage extends PagePanel implements ActionListener, ItemListener {
 //    private JPanel     titlePanel;
 //    private String     title;               // object to store title
     private JTextField titleField;          // text field for user to enter title
 //    private JPanel     locationPanel;
 //    private String     location;            // object to store location
     private JTextField locationField;       // text field for user to enter location
     private String     attendees;           // who is attending
     private JTextField addAttendeeField;    // text field for user to add new attendee
     private JList      attendeesList;       // list of all attendees for user to edit
 //    private JPanel     datePanel;
 //    private Calendar   date;                // Date object to store in event
     private JComboBox  monthDropDown;       // drop-down list to choose month
     private JTextField dayField;            // text field for user to enter day
     private JTextField yearField;           // text field for user to enter year
 //    private JPanel     timesPanel;
 //    private TimeBlock  times;               // block of time stored in event
     private JTextField startHourField;      // text field for user to enter hour of start time
     private JTextField startMinField;       // text field for user to enter minute of start time
     private JComboBox  startAMPMDropDown;   // drop-down list to choose AM/PM of start time
     private JTextField endHourField;        // text field for user to enter hour of end time
     private JTextField endMinField;         // text field for user to enter minute of end time
     private JComboBox  endAMPMDropDown;     // drop-down list to choose AM/PM of end time
 //    private JPanel     recurPanel;
     private Boolean    isRecurring;         // is the event a recurring event?
     private JCheckBox  isRecurringBox;      // check box for user to select recurring event
     private RecurType  recurType;           // type of recurrance
                                                 // NOTE: recurrance parameters inferred from date
     private JComboBox  recurDropDown;       // drop-down list for types of recurring events
     private JPanel     stopDatePanel;
 //    private Calendar   stopDate;
     private JComboBox  stopMonthDropDown;
     private JTextField stopDayField;
     private JTextField stopYearField;
 //    private JPanel     buttonPanel;
     private JButton    ok;
     private JButton    cancel;
 
     private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                                             "Sep", "Oct", "Nov", "Dec"};
     private static final String[] RECUR_TYPES = {"Daily", "Weekly", "Monthly (by Date)",
                                                  "Monthly (by Day)", "Yearly"};
     
     // enumeration of different recurring event types
     private static enum RecurType { DAILY, WEEKLY, MONTHLY_BY_DATE, MONTHLY_BY_DAY, YEARLY };
     
     /**
      * Init constructor
      * Edits an existing event
      * @throws Exception  a TimeBlock exception
      */
     public EditEventPage() {
         super();
         
         JPanel titlePanel;
         JPanel locationPanel;
         JPanel datePanel;
         JPanel timesPanel;
         JPanel recurPanel;
         JPanel buttonPanel;
         
         setLayout(new BoxLayout((Container) this, BoxLayout.Y_AXIS));
         
         titlePanel = new JPanel();
         titlePanel.add(new JLabel("Name:"));
         titleField = new JTextField(30);
         titlePanel.add(titleField);
         add(titlePanel);
         
         locationPanel = new JPanel();
         locationPanel.add(new JLabel("Location:"));
         locationField = new JTextField(30);
         locationPanel.add(locationField);
         add(locationPanel);
         
         datePanel = new JPanel();
         datePanel.add(new JLabel("Date:"));
         monthDropDown = new JComboBox(MONTHS);
         datePanel.add(monthDropDown);
         dayField = new JTextField(2);
         datePanel.add(dayField);
         datePanel.add(new JLabel(","));
         yearField = new JTextField(4);
         datePanel.add(yearField);
         add(datePanel);
         
         timesPanel = new JPanel();
         timesPanel.add(new JLabel("Time:"));
         startHourField = new JTextField(2);
         timesPanel.add(startHourField); 
         timesPanel.add(new JLabel(":"));
         startMinField = new JTextField(2);
         timesPanel.add(startMinField);
         startAMPMDropDown = new JComboBox(new String[] {"AM", "PM"} );
         timesPanel.add(startAMPMDropDown);
         timesPanel.add(new JLabel("-"));
         endHourField = new JTextField(2);
         timesPanel.add(endHourField);
         timesPanel.add(new JLabel(":"));
         endMinField = new JTextField(2);
         timesPanel.add(endMinField);
         endAMPMDropDown = new JComboBox(new String[] {"AM", "PM"} );
         timesPanel.add(endAMPMDropDown);
         add(timesPanel);
         
         recurPanel = new JPanel();
         isRecurring = false;
         isRecurringBox = new JCheckBox("Recurring Event:");
         isRecurringBox.addItemListener(this);
         recurPanel.add(isRecurringBox);
         recurType = RecurType.DAILY;
         recurDropDown = new JComboBox(RECUR_TYPES);
         recurDropDown.addActionListener(this);
         recurDropDown.setEditable(false);
         recurPanel.add(recurDropDown);
         add(recurPanel);
         
         stopDatePanel = new JPanel();
         stopDatePanel.add(new JLabel("Stop Date:"));
         stopMonthDropDown = new JComboBox(MONTHS);
         stopMonthDropDown.setEditable(false);
         stopDatePanel.add(stopMonthDropDown);
         stopDayField = new JTextField(2);
         stopDayField.setEditable(false);
         stopDatePanel.add(stopDayField);
         stopDatePanel.add(new JLabel(","));
         stopYearField = new JTextField(4);
         stopYearField.setEditable(false);
         stopDatePanel.add(stopYearField);
         stopDatePanel.setVisible(false);
         add(stopDatePanel);
         
         buttonPanel = new JPanel();
         ok = new JButton("OK");
         ok.addActionListener(this);
         buttonPanel.add(ok);
         cancel = new JButton("Cancel");
         cancel.addActionListener(this);
         buttonPanel.add(cancel);
         add(buttonPanel);
         
         this.setEnabled(false);
     }
     
     /**
      * Activates panel
      * @throws Exception  invalid TimeBlock, though for this method, it will not be thrown
      */
     @Override
     public void activate() {
         Calendar today = Calendar.getInstance();
         TimeBlock times = new TimeBlock();
         Calendar stopDate = new GregorianCalendar();
         
         titleField.setText("");
         locationField.setText("");
         
         monthDropDown.setSelectedIndex(today.get(Calendar.MONTH));
         dayField.setText(Integer.toString(today.get(Calendar.DATE)));
         yearField.setText(Integer.toString(today.get(Calendar.YEAR)));
         
         startHourField.setText(Integer.toString(times.getStartHourAP()));
         startMinField.setText(Integer.toString(times.getStartMinute()));
         startAMPMDropDown.setSelectedIndex(times.isStartPM());
         endHourField.setText(Integer.toString(times.getEndHourAP()));
         endMinField.setText(Integer.toString(times.getEndMinute()));
         endAMPMDropDown.setSelectedIndex(times.isEndPM());
         
         isRecurring = false;
         recurType = RecurType.DAILY;
         recurDropDown.setSelectedIndex(0);
         recurDropDown.setEditable(false);
         
         stopDatePanel.setVisible(false);
         stopDate = today;
         stopDate.add(Calendar.YEAR, 1);
         stopMonthDropDown.setSelectedIndex(stopDate.get(Calendar.MONTH));
         stopMonthDropDown.setEditable(false);
         stopDayField.setText(Integer.toString(stopDate.get(Calendar.DATE)));
         stopDayField.setEditable(false);
         stopYearField.setText(Integer.toString(stopDate.get(Calendar.YEAR)));
         stopYearField.setEditable(false);
         
         this.setEnabled(true);
     }
     
     /**
      * Sets fields in edit menu to certain event
      * @throws Exception  invalid TimeBlock
      */
     public void setFields(Event eventToEdit) throws Exception {
         Calendar date = new GregorianCalendar();
         Calendar endDate = new GregorianCalendar();
         TimeBlock times;
         
         titleField.setText(eventToEdit.getName());
         locationField.setText(eventToEdit.getLocation());
         
         if (eventToEdit instanceof OneTimeEvent) {
             date.setTime(((OneTimeEvent) eventToEdit).getStartDate());
             endDate.setTime(((OneTimeEvent) eventToEdit).getEndDate());
             times = new TimeBlock(date.get(Calendar.HOUR_OF_DAY),
                                   date.get(Calendar.MINUTE),
                                   endDate.get(Calendar.HOUR_OF_DAY),
                                   endDate.get(Calendar.MINUTE));
             
             isRecurring = false;
             isRecurringBox.setSelected(false);
             recurDropDown.setEditable(false);
             stopDatePanel.setVisible(false);
             stopMonthDropDown.setEditable(false);
             stopDayField.setEditable(false);
             stopYearField.setEditable(false);
         } else {
             date = ((RecurringEvent) eventToEdit).getIntervalStart();
             endDate = ((RecurringEvent) eventToEdit).getIntervalEnd();
             times = ((RecurringEvent) eventToEdit).getTimes();
             
             isRecurring = true;
             isRecurringBox.setSelected(true);
             recurDropDown.setEditable(true);
             if (eventToEdit instanceof DailyRecurringEvent) {
                 recurType = RecurType.DAILY;
             } else if (eventToEdit instanceof WeeklyRecurringEvent) {
                 recurType = RecurType.WEEKLY;
             } else if (eventToEdit instanceof MonthlyDateRecurringEvent) {
                 recurType = RecurType.MONTHLY_BY_DATE;
             } else if (eventToEdit instanceof MonthlyDayRecurringEvent) {
                 recurType = RecurType.MONTHLY_BY_DAY;
             } else {
                 recurType = RecurType.YEARLY;
             }
             recurDropDown.setSelectedIndex(recurType.ordinal());
             
             stopDatePanel.setVisible(true);
             stopMonthDropDown.setEditable(true);
             stopMonthDropDown.setSelectedIndex(endDate.get(Calendar.MONTH));
             stopDayField.setEditable(true);
             stopDayField.setText(Integer.toString(endDate.get(Calendar.DATE)));
             stopYearField.setEditable(true);
             stopYearField.setText(Integer.toString(endDate.get(Calendar.YEAR)));
         }
         
         monthDropDown.setSelectedIndex(date.get(Calendar.MONTH));
         dayField.setText(Integer.toString(date.get(Calendar.DATE)));
         yearField.setText(Integer.toString(date.get(Calendar.YEAR)));
         
         startHourField.setText(Integer.toString(times.getStartHourAP()));
         startMinField.setText(Integer.toString(times.getStartMinute()));
         startAMPMDropDown.setSelectedIndex(times.isStartPM());
         endHourField.setText(Integer.toString(times.getEndHourAP()));
         endMinField.setText(Integer.toString(times.getEndMinute()));
         endAMPMDropDown.setSelectedIndex(times.isEndPM());
     }
     
     /**
      * Paints panel
      * @param g
      */
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
     }
 
     /**
      * Handles events to text fields and drop-down lists
      * @param e  action data
      */
     @Override
     public void actionPerformed(ActionEvent e) {
         int day;
         Event newEvent;
         String title;
         String location;
         int startHour;
         int startMin;
         int endHour;
         int endMin;
         Calendar date;
         Calendar endDate;
         
         if (e.getSource() == recurDropDown) {
             recurType = RecurType.values()[recurDropDown.getSelectedIndex()];
         } else if (e.getSource() == ok) {
             try {
                 title = titleField.getText();
                 location = locationField.getText();
                 
                 date = new GregorianCalendar();
                 date.set(Calendar.MONTH, monthDropDown.getSelectedIndex());
                 date.set(Calendar.DATE, Integer.parseInt(dayField.getText()));
                 date.set(Calendar.YEAR, Integer.parseInt(yearField.getText()));
                 
                 startHour = TimeBlock.apToMilitary(Integer.parseInt(startHourField.getText()),
                                                    startAMPMDropDown.getSelectedIndex()==1);
                 startMin = Integer.parseInt(startMinField.getText());
                 endHour = TimeBlock.apToMilitary(Integer.parseInt(endHourField.getText()),
                                                  endAMPMDropDown.getSelectedIndex()==1);
                 endMin = Integer.parseInt(endMinField.getText());
                 
                 if (isRecurring) {
                     endDate = new GregorianCalendar();
                     endDate.set(Calendar.MONTH, stopMonthDropDown.getSelectedIndex());
                     endDate.set(Calendar.DATE, Integer.parseInt(stopDayField.getText()));
                     endDate.set(Calendar.YEAR, Integer.parseInt(stopYearField.getText()));
                     
                     switch (recurType) {
                     case DAILY:
                         newEvent = new DailyRecurringEvent(title, location, null, null, startHour,
                                                            startMin, endHour, endMin, date, endDate);
                         break;
                     case WEEKLY:
                         newEvent = new WeeklyRecurringEvent(title, location, null, null, startHour,
                                                             startMin, endHour, endMin,
                                                             date.get(Calendar.DAY_OF_WEEK), date,
                                                             endDate);
                         break;
                     case MONTHLY_BY_DATE:
                         newEvent = new MonthlyDateRecurringEvent(title, location, null, null,
                                                                  startHour, startMin, endHour, endMin,
                                                                  date.get(Calendar.DATE), date,
                                                                  endDate);
                         break;
                     case MONTHLY_BY_DAY:
                         newEvent = new MonthlyDayRecurringEvent(title, location, null, null, startHour,
                                                                 startMin, endHour, endMin,
                                                                 date.get(Calendar.DAY_OF_WEEK),
                                                                 date.get(
                                                                         Calendar.DAY_OF_WEEK_IN_MONTH),
                                                                 date, endDate);
                         break;
                     case YEARLY:
                         newEvent = new YearlyRecurringEvent(title, location, null, null, startHour,
                                                             startMin, endHour, endMin,
                                                             date.get(Calendar.MONTH),
                                                             date.get(Calendar.DATE), date, endDate);
                         break;
                     }
                 } else {
                     date.set(Calendar.HOUR, startHour);
                     date.set(Calendar.MINUTE, startMin);
                     endDate = date;
                     endDate.set(Calendar.HOUR, endHour);
                     endDate.set(Calendar.MINUTE, endMin);
                    
                     newEvent = new OneTimeEvent(title, location, null, null, date.getTime(),
                                                 endDate.getTime());
                 }
                 // TODO: return newEvent somehow
                 
                 this.setEnabled(false);
             }
             catch (Exception ex) {}
         } else if (e.getSource() == cancel) {
             this.setEnabled(false);
         }
     }
 
     /**
      * Handles event to recurring event check box
      * @param e  event data
      */
     @Override
     public void itemStateChanged(ItemEvent e) {
         // if (e.getSource() == isRecurringBox)
         if (e.getStateChange() == ItemEvent.SELECTED) {
             recurDropDown.setEditable(true);
            stopDatePanel.setVisible(true);
             stopMonthDropDown.setEditable(true);
             stopDayField.setEditable(true);
             stopYearField.setEditable(true);
         } else {
             recurDropDown.setEditable(false);
            stopDatePanel.setVisible(false);
             stopMonthDropDown.setEditable(false);
             stopDayField.setEditable(false);
             stopYearField.setEditable(false);
         }
     }
 }
