 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 //package calendar;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 /**
  * Draws Calendar GUI
  *
  * @param layout GridBagLayout of application
  * @param constraints helper for the layout
  * @param button1 button #1 of 31 corresponding to date on Calendar
  * @param button2 button #2 of 31 corresponding to date on Calendar
  * @param button3 button #3 of 31 corresponding to date on Calendar
  * @param button4 button #4 of 31 corresponding to date on Calendar
  * @param button5 button #5 of 31 corresponding to date on Calendar
  * @param button6 button #6 of 31 corresponding to date on Calendar
  * @param button7 button #7 of 31 corresponding to date on Calendar
  * @param button8 button #8 of 31 corresponding to date on Calendar
  * @param button9 button #9 of 31 corresponding to date on Calendar
  * @param button10 button #10 of 31 corresponding to date on Calendar
  * @param button11 button #11 of 31 corresponding to date on Calendar
  * @param button12 button #12 of 31 corresponding to date on Calendar
  * @param button13 button #13 of 31 corresponding to date on Calendar
  * @param button14 button #14 of 31 corresponding to date on Calendar
  * @param button15 button #15 of 31 corresponding to date on Calendar
  * @param button16 button #16 of 31 corresponding to date on Calendar
  * @param button17 button #17 of 31 corresponding to date on Calendar
  * @param button18 button #18 of 31 corresponding to date on Calendar
  * @param button19 button #19 of 31 corresponding to date on Calendar
  * @param button20 button #20 of 31 corresponding to date on Calendar
  * @param button21 button #21 of 31 corresponding to date on Calendar
  * @param button22 button #22 of 31 corresponding to date on Calendar
  * @param button23 button #23 of 31 corresponding to date on Calendar
  * @param button24 button #24 of 31 corresponding to date on Calendar
  * @param button25 button #25 of 31 corresponding to date on Calendar
  * @param button26 button #26 of 31 corresponding to date on Calendar
  * @param button27 button #27 of 31 corresponding to date on Calendar
  * @param button28 button #28 of 31 corresponding to date on Calendar
  * @param button29 button #29 of 31 corresponding to date on Calendar
  * @param button30 button #30 of 31 corresponding to date on Calendar
  * @param button31 button #31 of 31 corresponding to date on Calendar
  * @param space space integer for spacing between buttons
  * @param month JLabel variable to title the month and year on calendar
  * @param pan1 JPanel variable to contain the month and year on calendar
  * @author Kelly Hutchison <kmh5754 at psu.edu>
  */
 public class DrawCalendar extends JPanel implements ActionListener {
 
     private GridBagLayout layout;                 // layout of applet
     private GridBagConstraints constraints;       // helper for the layout
     private JButton button1;                      // button #1 of 31 to Calendar layout
     private JButton button2;                      // button #2 of 31 to Calendar layout
     private JButton button3;                      // button #3 of 31 to Calendar layout
     private JButton button4;                      // button #4 of 31 to Calendar layout
     private JButton button5;                      // button #5 of 31 to Calendar layout
     private JButton button6;                      // button #6 of 31 to Calendar layout
     private JButton button7;                      // button #7 of 31 to Calendar layout
     private JButton button8;                      // button #8 of 31 to Calendar layout
     private JButton button9;                      // button #9 of 31 to Calendar layout
     private JButton button10;                     // button #10 of 31 to Calendar layout
     private JButton button11;                     // button #11 of 31 to Calendar layout
     private JButton button12;                     // button #12 of 31 to Calendar layout
     private JButton button13;                     // button #13 of 31 to Calendar layout
     private JButton button14;                     // button #14 of 31 to Calendar layout  
     private JButton button15;                     // button #15 of 31 to Calendar layout
     private JButton button16;                     // button #16 of 31 to Calendar layout
     private JButton button17;                     // button #17 of 31 to Calendar layout
     private JButton button18;                     // button #18 of 31 to Calendar layout
     private JButton button19;                     // button #19 of 31 to Calendar layout
     private JButton button20;                     // button #20 of 31 to Calendar layout  
     private JButton button21;                     // button #21 of 31 to Calendar layout
     private JButton button22;                     // button #22 of 31 to Calendar layout
     private JButton button23;                     // button #23 of 31 to Calendar layout
     private JButton button24;                     // button #24 of 31 to Calendar layout
     private JButton button25;                     // button #25 of 31 to Calendar layout
     private JButton button26;                     // button #26 of 31 to Calendar layout
     private JButton button27;                     // button #27 of 31 to Calendar layout
     private JButton button28;                     // button #28 of 31 to Calendar layout
     private JButton button29;                     // button #29 of 31 to Calendar layout
     private JButton button30;                     // button #30 of 31 to Calendar layout
     private JButton button31;                     // button #31 of 31 to Calendar layout
     private JButton viewEvents;
     private final int space = 3;                  // Spacing between buttons
     private JLabel month;                         // JLabel to display month and year
     private JPanel pan1;                           // JPanel label to contain month JLabel
     private JPanel pan2;
     private JPanel pan3;
     private JPanel pan4;
     private JPanel weekdayPanel;
     private JButton alarmButton;
     private JButton reminderButton;
     private JButton eventButton;
     private String[][] january;
     private String[][] february;
     private String[][] march;
     private String[][] april;
     private String[][] may;
     private String[][] june;
     private String[][] july;
     private String[][] august;
     private String[][] september;
     private String[][] october;
     private String[][] november;
     private String[][] december;
     private String currentMonth;
     private String currentYear;
     private Calendar cal;
     private GregorianCalendar checkYear;
     private int curYear;
     private int curMonth;
     private int curDay;
     private int dayClicked;
     private int dayOfWeek;
     private Font f;
     private JLabel sunday;
     private JLabel monday;
     private JLabel tuesday;
     private JLabel wednesday;
     private JLabel thursday;
     private JLabel friday;
     private JLabel saturday;
     private JLabel monthLabel;
     private JLabel dateLabel;
     private JLabel yearLabel;
     private JTextArea listOfEvents;
     private Boolean isLeapYear;
     private Alarm alarm;
     private Event event;
     private Memo memo;
     private JFrame popUp;
     private JFrame schedule;
     private int indexDay1 = 0;
     private int indexDay2 = 0;
     private int indexDay3 = 0;
     private int indexDay4 = 0;
     private int indexDay5 = 0;
     private int indexDay6 = 0;
     private int indexDay7 = 0;
     private int indexDay8 = 0;
     private int indexDay9 = 0;
     private int indexDay10 = 0;
     private int indexDay11 = 0;
     private int indexDay12 = 0;
     private int indexDay13 = 0;
     private int indexDay14 = 0;
     private int indexDay15 = 0;
     private int indexDay16 = 0;
     private int indexDay17 = 0;
     private int indexDay18 = 0;
     private int indexDay19 = 0;
     private int indexDay20 = 0;
     private int indexDay21 = 0;
     private int indexDay22 = 0;
     private int indexDay23 = 0;
     private int indexDay24 = 0;
     private int indexDay25 = 0;
     private int indexDay26 = 0;
     private int indexDay27 = 0;
     private int indexDay28 = 0;
     private int indexDay29 = 0;
     private int indexDay30 = 0;
     private int indexDay31 = 0;
 
     // Default Constructor
     public DrawCalendar() { // set up graphics window 
         super(); // this can be omitted 
         setBackground(Color.WHITE);
         layout = new GridBagLayout();             // set up layout
         setLayout(layout);
         constraints = new GridBagConstraints();
         cal = Calendar.getInstance();
 
         checkYear = new GregorianCalendar();
         dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
         curMonth = cal.get(cal.MONTH) + 1; // zero based
         curYear = cal.get(cal.YEAR);
         curDay = cal.get(cal.DATE);
         isLeapYear = checkYear.isLeapYear(curYear);
 
         // set currentYear string to the current year
         currentYear = Integer.toString(curYear);
 
         // Set current month 
         // set the currentMonth string to the current month
         if (curMonth == 1) {
             currentMonth = "JANUARY";
         }
         if (curMonth == 2) {
             currentMonth = "FEBRUARY";
         }
         if (curMonth == 3) {
             currentMonth = "MARCH";
         }
         if (curMonth == 4) {
             currentMonth = "APRIL";
         }
         if (curMonth == 5) {
             currentMonth = "MAY";
         }
         if (curMonth == 6) {
             currentMonth = "JUNE";
         }
         if (curMonth == 7) {
             currentMonth = "JULY";
         }
         if (curMonth == 8) {
             currentMonth = "AUGUST";
         }
         if (curMonth == 9) {
             currentMonth = "SEPTEMBER";
         }
         if (curMonth == 10) {
             currentMonth = "OCTOBER";
         }
         if (curMonth == 11) {
             currentMonth = "NOVEMBER";
         }
         if (curMonth == 12) {
             currentMonth = "DECEMBER";
         }
         if (curMonth == 1) {
             currentMonth = "JANUARY";
         }
         if (curMonth == 2) {
             currentMonth = "FEBRUARY";
         }
         if (curMonth == 3) {
             currentMonth = "MARCH";
         }
         if (curMonth == 4) {
             currentMonth = "APRIL";
         }
         if (curMonth == 5) {
             currentMonth = "MAY";
         }
         if (curMonth == 6) {
             currentMonth = "JUNE";
         }
         if (curMonth == 7) {
             currentMonth = "JULY";
         }
         if (curMonth == 8) {
             currentMonth = "AUGUST";
         }
         if (curMonth == 9) {
             currentMonth = "SEPTEMBER";
         }
         if (curMonth == 10) {
             currentMonth = "OCTOBER";
         }
         if (curMonth == 11) {
             currentMonth = "NOVEMBER";
         }
         if (curMonth == 12) {
             currentMonth = "DECEMBER";
         }
 
         // initialize buttons
         button1 = new JButton("1");
         button2 = new JButton("2");
         button3 = new JButton("3");
         button4 = new JButton("4");
         button5 = new JButton("5");
         button6 = new JButton("6");
         button7 = new JButton("7");
         button8 = new JButton("8");
         button9 = new JButton("9");
         button10 = new JButton("10");
         button11 = new JButton("11");
         button12 = new JButton("12");
         button13 = new JButton("13");
         button14 = new JButton("14");
         button15 = new JButton("15");
         button16 = new JButton("16");
         button17 = new JButton("17");
         button18 = new JButton("18");
         button19 = new JButton("19");
         button20 = new JButton("20");
         button21 = new JButton("21");
         button22 = new JButton("22");
         button23 = new JButton("23");
         button24 = new JButton("24");
         button25 = new JButton("25");
         button26 = new JButton("26");
         button27 = new JButton("27");
         button28 = new JButton("28");
         button29 = new JButton("29");
         button30 = new JButton("30");
         button31 = new JButton("31");
 
         pan1 = new JPanel();                     // Instantiate JPanel
 
         // set up arrays 
         january = new String[31][50];
         february = new String[28][50];
         march = new String[31][50];
         april = new String[30][50];
         may = new String[29][50];
         june = new String[30][50];
         july = new String[31][50];
         august = new String[31][50];
         september = new String[30][50];
         october = new String[31][50];
         november = new String[30][50];
         december = new String[31][50];
 
         f = new Font("Arial BLACK", Font.BOLD, 30); // Change font size on Font variable f
         month = new JLabel(currentMonth + " " + currentYear);    // Assign JLabel a value
         month.setFont(f);                               // Assign JLabel to font f
 
         pan1.setLayout(new GridBagLayout());         // set layout of JPanel to Grid Bag Layout
         //GridBagConstraints gbc = new GridBagConstraints();      // set constraints
         pan1.add(month);//, gbc);        // add month variable to panel
         pan1.setVisible(true);       // set visibility of panel
         pan1.setBackground(Color.LIGHT_GRAY);     // set background of panel to a color 
 
         f = new Font("Arial BLACK", Font.BOLD, 20);  // Assign new font for days
 
         weekdayPanel = new JPanel();        // instantiate panel
         // Create Labels
         sunday = new JLabel("SUN");
         monday = new JLabel("MON");
         tuesday = new JLabel("TUE");
         wednesday = new JLabel("WED");
         thursday = new JLabel("THU");
         friday = new JLabel("FRI");
         saturday = new JLabel("SAT");
 
         // Center JLabels
         sunday.setHorizontalAlignment(JLabel.CENTER);
         monday.setHorizontalAlignment(JLabel.CENTER);
         tuesday.setHorizontalAlignment(JLabel.CENTER);
         wednesday.setHorizontalAlignment(JLabel.CENTER);
         thursday.setHorizontalAlignment(JLabel.CENTER);
         friday.setHorizontalAlignment(JLabel.CENTER);
         saturday.setHorizontalAlignment(JLabel.CENTER);
 
         // Set font of JLabels
         sunday.setFont(f);
         monday.setFont(f);
         tuesday.setFont(f);
         wednesday.setFont(f);
         thursday.setFont(f);
         friday.setFont(f);
         saturday.setFont(f);
         GridBagConstraints gbc = new GridBagConstraints();
 
         gbc.fill = GridBagConstraints.BOTH;
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         gbc.insets = new Insets(space, space, space, space);       // Set spacing between buttons
 
         weekdayPanel.setLayout(new GridBagLayout());
 
         weekdayPanel.setBackground(Color.CYAN);
         //weekdayPanel.add(sunday);
 
         // Find first day of week of month
 
        cal.set(Calendar.DAY_OF_MONTH, 1);  
        dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); 
         if (dayOfWeek == 1) {
             // Add week day names
             gbc.gridx = 0;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(sunday, gbc);
 
             gbc.gridx = 1;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(monday, gbc);
 
             gbc.gridx = 2;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(tuesday, gbc);
 
             gbc.gridx = 3;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(wednesday, gbc);
 
             gbc.gridx = 4;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(thursday, gbc);
 
             gbc.gridx = 5;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(friday, gbc);
 
             gbc.gridx = 6;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(saturday, gbc);
         }
 
 
         if (dayOfWeek == 2) {
             // Add week day names
             gbc.gridx = 0;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(monday, gbc);
 
             gbc.gridx = 1;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(tuesday, gbc);
 
             gbc.gridx = 2;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(wednesday, gbc);
 
             gbc.gridx = 3;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(thursday, gbc);
 
             gbc.gridx = 4;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(friday, gbc);
 
             gbc.gridx = 5;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(saturday, gbc);
 
             gbc.gridx = 6;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(sunday, gbc);
         }
 
         if (dayOfWeek == 3) {
             // Add week day names
             gbc.gridx = 0;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(tuesday, gbc);
 
             gbc.gridx = 1;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(wednesday, gbc);
 
             gbc.gridx = 2;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(thursday, gbc);
 
             gbc.gridx = 3;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(friday, gbc);
 
             gbc.gridx = 4;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(saturday, gbc);
 
             gbc.gridx = 5;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(sunday, gbc);
 
             gbc.gridx = 6;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(monday, gbc);
         }
 
         if (dayOfWeek == 4) {
             // Add week day names
             gbc.gridx = 0;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(wednesday, gbc);
 
             gbc.gridx = 1;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(thursday, gbc);
 
             gbc.gridx = 2;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(friday, gbc);
 
             gbc.gridx = 3;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(saturday, gbc);
 
             gbc.gridx = 4;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(sunday, gbc);
 
             gbc.gridx = 5;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(monday, gbc);
 
             gbc.gridx = 6;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(tuesday, gbc);
         }
 
 
         if (dayOfWeek == 5) {
             // Add week day names
             gbc.gridx = 0;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(thursday, gbc);
 
             gbc.gridx = 1;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(friday, gbc);
 
             gbc.gridx = 2;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(saturday, gbc);
 
             gbc.gridx = 3;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(sunday, gbc);
 
             gbc.gridx = 4;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(monday, gbc);
 
             gbc.gridx = 5;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(tuesday, gbc);
 
             gbc.gridx = 6;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(wednesday, gbc);
         }
 
         if (dayOfWeek == 6) {
             // Add week day names
             gbc.gridx = 0;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(friday, gbc);
 
             gbc.gridx = 1;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(saturday, gbc);
 
             gbc.gridx = 2;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(sunday, gbc);
 
             gbc.gridx = 3;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(monday, gbc);
 
             gbc.gridx = 4;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(tuesday, gbc);
 
             gbc.gridx = 5;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(wednesday, gbc);
 
             gbc.gridx = 6;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(thursday, gbc);
         }
 
 
 
         if (dayOfWeek == 7) {
             // Add week day names
             gbc.gridx = 0;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(saturday, gbc);
 
             gbc.gridx = 1;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(sunday, gbc);
 
             gbc.gridx = 2;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(monday, gbc);
 
             gbc.gridx = 3;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(tuesday, gbc);
 
             gbc.gridx = 4;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(wednesday, gbc);
 
             gbc.gridx = 5;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(thursday, gbc);
 
             gbc.gridx = 6;
             gbc.gridy = 1;
             gbc.gridwidth = 1;
             gbc.gridheight = 1;
             gbc.fill = GridBagConstraints.BOTH;
             layout.setConstraints(weekdayPanel, gbc);
             add(friday, gbc);
         }
 
        cal.set(Calendar.DAY_OF_MONTH, curDay); 
 
 
         gbc.gridx = 0;
         gbc.gridy = 1;
         gbc.gridwidth = 7;
         gbc.gridheight = 1;
         layout.setConstraints(weekdayPanel, gbc);
         add(weekdayPanel);
 
         f = new Font("Arial", Font.PLAIN, 15);  // Assign new font for dates
 
         // set default fonts of day numbers
         button1.setFont(f);
         button2.setFont(f);
         button3.setFont(f);
         button4.setFont(f);
         button5.setFont(f);
         button6.setFont(f);
         button7.setFont(f);
         button8.setFont(f);
         button9.setFont(f);
         button10.setFont(f);
         button11.setFont(f);
         button12.setFont(f);
         button13.setFont(f);
         button14.setFont(f);
         button15.setFont(f);
         button16.setFont(f);
         button17.setFont(f);
         button18.setFont(f);
         button19.setFont(f);
         button20.setFont(f);
         button21.setFont(f);
         button22.setFont(f);
         button23.setFont(f);
         button24.setFont(f);
         button25.setFont(f);
         button26.setFont(f);
         button27.setFont(f);
         button28.setFont(f);
         button29.setFont(f);
         button30.setFont(f);
         button31.setFont(f);
 
         // Set un-focusable 
         // So when application runs, none of buttons are initially selected
         button1.setFocusable(false);
         button2.setFocusable(false);
         button3.setFocusable(false);
         button4.setFocusable(false);
         button5.setFocusable(false);
         button6.setFocusable(false);
         button7.setFocusable(false);
         button8.setFocusable(false);
         button9.setFocusable(false);
         button10.setFocusable(false);
         button11.setFocusable(false);
         button12.setFocusable(false);
         button13.setFocusable(false);
         button14.setFocusable(false);
         button15.setFocusable(false);
         button16.setFocusable(false);
         button17.setFocusable(false);
         button18.setFocusable(false);
         button19.setFocusable(false);
         button20.setFocusable(false);
         button21.setFocusable(false);
         button22.setFocusable(false);
         button23.setFocusable(false);
         button24.setFocusable(false);
         button25.setFocusable(false);
         button26.setFocusable(false);
         button27.setFocusable(false);
         button28.setFocusable(false);
         button29.setFocusable(false);
         button30.setFocusable(false);
         button31.setFocusable(false);
 
         f = new Font("Arial BLACK", Font.BOLD, 15);
         // Set current date block to background color 
         // Set current day block to be bold
         if (curDay == 1) {
             button1.setFont(f);
             button1.setBackground(Color.CYAN);
             button1.setOpaque(true);
             button1.setBorderPainted(true);
         }
         if (curDay == 2) {
             button2.setFont(f);
             button2.setBackground(Color.CYAN);
             button2.setOpaque(true);
             button2.setBorderPainted(true);
         }
         if (curDay == 3) {
             button3.setFont(f);
             button3.setBackground(Color.CYAN);
             button3.setOpaque(true);
             button3.setBorderPainted(true);
         }
         if (curDay == 4) {
             button4.setFont(f);
             button4.setBackground(Color.CYAN);
             button4.setOpaque(true);
             button4.setBorderPainted(true);
         }
         if (curDay == 5) {
             button5.setFont(f);
             button5.setBackground(Color.CYAN);
             button5.setOpaque(true);
             button5.setBorderPainted(true);
         }
         if (curDay == 6) {
             button6.setFont(f);
             button6.setBackground(Color.CYAN);
             button6.setOpaque(true);
             button6.setBorderPainted(true);
         }
         if (curDay == 7) {
             button7.setFont(f);
             button7.setBackground(Color.CYAN);
             button7.setOpaque(true);
             button7.setBorderPainted(true);
         }
         if (curDay == 8) {
             button8.setFont(f);
             button8.setBackground(Color.CYAN);
             button8.setOpaque(true);
             button8.setBorderPainted(true);
         }
         if (curDay == 9) {
             button9.setFont(f);
             button9.setBackground(Color.CYAN);
             button9.setOpaque(true);
             button9.setBorderPainted(true);
         }
         if (curDay == 10) {
             button10.setFont(f);
             button10.setBackground(Color.CYAN);
             button10.setOpaque(true);
             button10.setBorderPainted(true);
         }
         if (curDay == 11) {
             button11.setFont(f);
             button11.setBackground(Color.CYAN);
             button11.setOpaque(true);
             button11.setBorderPainted(true);
         }
         if (curDay == 12) {
             button12.setFont(f);
             button12.setBackground(Color.CYAN);
             button12.setOpaque(true);
             button12.setBorderPainted(true);
         }
         if (curDay == 13) {
             button13.setFont(f);
             button13.setBackground(Color.CYAN);
             button13.setOpaque(true);
             button13.setBorderPainted(true);
         }
         if (curDay == 14) {
             button14.setFont(f);
             button14.setBackground(Color.CYAN);
             button14.setOpaque(true);
             button14.setBorderPainted(true);
         }
         if (curDay == 15) {
             button15.setFont(f);
             button15.setBackground(Color.CYAN);
             button15.setOpaque(true);
             button15.setBorderPainted(true);
         }
         if (curDay == 16) {
             button16.setFont(f);
             button16.setBackground(Color.CYAN);
             button16.setOpaque(true);
             button16.setBorderPainted(true);
         }
         if (curDay == 17) {
             button17.setFont(f);
             button17.setBackground(Color.CYAN);
             button17.setOpaque(true);
             button17.setBorderPainted(true);
         }
         if (curDay == 18) {
             button18.setFont(f);
             button18.setBackground(Color.CYAN);
             button18.setOpaque(true);
             button18.setBorderPainted(true);
         }
         if (curDay == 19) {
             button19.setFont(f);
             button19.setBackground(Color.CYAN);
             button19.setOpaque(true);
             button19.setBorderPainted(true);
         }
         if (curDay == 20) {
             button20.setFont(f);
             button20.setBackground(Color.CYAN);
             button20.setOpaque(true);
             button20.setBorderPainted(true);
         }
         if (curDay == 21) {
             button21.setFont(f);
             button21.setBackground(Color.CYAN);
             button21.setOpaque(true);
             button21.setBorderPainted(true);
         }
         if (curDay == 22) {
             button22.setFont(f);
             button22.setBackground(Color.CYAN);
             button22.setOpaque(true);
             button22.setBorderPainted(true);
         }
         if (curDay == 23) {
             button23.setFont(f);
             button23.setBackground(Color.CYAN);
             button23.setOpaque(true);
             button23.setBorderPainted(true);
         }
         if (curDay == 24) {
             button24.setFont(f);
             button24.setBackground(Color.CYAN);
             button24.setOpaque(true);
             button24.setBorderPainted(true);
         }
         if (curDay == 25) {
             button25.setFont(f);
             button25.setBackground(Color.CYAN);
             button25.setOpaque(true);
             button25.setBorderPainted(true);
         }
         if (curDay == 26) {
             button26.setFont(f);
             button26.setBackground(Color.CYAN);
             button26.setOpaque(true);
             button26.setBorderPainted(true);
         }
         if (curDay == 27) {
             button27.setFont(f);
             button27.setBackground(Color.CYAN);
             button27.setOpaque(true);
             button27.setBorderPainted(true);
         }
         if (curDay == 28) {
             button28.setFont(f);
             button28.setBackground(Color.CYAN);
             button28.setOpaque(true);
             button28.setBorderPainted(true);
         }
         if (curDay == 29) {
             button29.setFont(f);
             button29.setBackground(Color.CYAN);
             button29.setOpaque(true);
             button29.setBorderPainted(true);
         }
         if (curDay == 30) {
             button30.setFont(f);
             button30.setBackground(Color.CYAN);
             button30.setOpaque(true);
             button30.setBorderPainted(true);
         }
         if (curDay == 31) {
             button31.setFont(f);
             button31.setBackground(Color.CYAN);
             button31.setOpaque(true);
             button31.setBorderPainted(true);
         }
 
         // Set constraint weights
         constraints.fill = GridBagConstraints.BOTH;
         constraints.weightx = 1.0;
         constraints.weighty = 1.0;
         constraints.insets = new Insets(space, space, space, space);       // Set spacing between buttons
 
         // Set constraints for each button and JPanel on calendar
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.gridwidth = 7;
         constraints.gridheight = 1;
         layout.setConstraints(pan1, constraints);
         add(pan1);                        // add panel          
 
         // Add Date Buttons
         constraints.gridx = 0;
         constraints.gridy = 2;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button1, constraints);
         button1.addActionListener(this);
         add(button1);                        // add button                           
 
         constraints.gridx = 1;               // define constraints for button
         constraints.gridy = 2;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button2, constraints);
         button2.addActionListener(this);
         add(button2);                        // add button
 
         constraints.gridx = 2;               //  define constraints for button
         constraints.gridy = 2;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button3, constraints);
         button3.addActionListener(this);
         add(button3);                        // add button
 
         constraints.gridx = 3;               // define constraints for button
         constraints.gridy = 2;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button4, constraints);
         button4.addActionListener(this);
         add(button4);                        // add button
 
         constraints.gridx = 4;               // define constraints for button
         constraints.gridy = 2;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button5, constraints);
         button5.addActionListener(this);
         add(button5);                        // add button
 
         constraints.gridx = 5;               // define constraints for button
         constraints.gridy = 2;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button6, constraints);
         button6.addActionListener(this);
         add(button6);                        // add button
 
         constraints.gridx = 6;               // define constraints for button
         constraints.gridy = 2;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button7, constraints);
         button7.addActionListener(this);
         add(button7);                        // add button
 
         constraints.gridx = 0;               // define constraints for button
         constraints.gridy = 3;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button8, constraints);
         button8.addActionListener(this);
         add(button8);                        // add button
 
         constraints.gridx = 1;               // define constraints for button
         constraints.gridy = 3;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button9, constraints);
         button9.addActionListener(this);
         add(button9);                        // add button
 
         constraints.gridx = 2;                // define constraints for button
         constraints.gridy = 3;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button10, constraints);
         button10.addActionListener(this);
         add(button10);                        // add button
 
         constraints.gridx = 3;                // define constraints for button
         constraints.gridy = 3;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button11, constraints);
         button11.addActionListener(this);
         add(button11);                        // add button
 
         constraints.gridx = 4;                // define constraints for button
         constraints.gridy = 3;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button12, constraints);
         button12.addActionListener(this);
         add(button12);                        // add button
 
         constraints.gridx = 5;                // define constraints for button
         constraints.gridy = 3;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button13, constraints);
         button13.addActionListener(this);
         add(button13);                        // add button
 
         constraints.gridx = 6;                // define constraints for button
         constraints.gridy = 3;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button14, constraints);
         button14.addActionListener(this);
         add(button14);                        // add button
 
         constraints.gridx = 0;                // define constraints for button
         constraints.gridy = 4;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button15, constraints);
         button15.addActionListener(this);
         add(button15);                        // add button
 
         constraints.gridx = 1;                // define constraints for button
         constraints.gridy = 4;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button16, constraints);
         button16.addActionListener(this);
         add(button16);                        // add button
 
         constraints.gridx = 2;                  // define constraints for button
         constraints.gridy = 4;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button17, constraints);
         button17.addActionListener(this);
         add(button17);                        // add button                           
 
         constraints.gridx = 3;               // define constraints for button
         constraints.gridy = 4;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button18, constraints);
         button18.addActionListener(this);
         add(button18);                        // add button
 
         constraints.gridx = 4;               // define constraints for button
         constraints.gridy = 4;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button19, constraints);
         button19.addActionListener(this);
         add(button19);                        // add button
 
         constraints.gridx = 5;               // define constraints for button
         constraints.gridy = 4;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button20, constraints);
         button20.addActionListener(this);
         add(button20);                        // add button
 
         constraints.gridx = 6;               // define constraints for button
         constraints.gridy = 4;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button21, constraints);
         button21.addActionListener(this);
         add(button21);                        // add button
 
         constraints.gridx = 0;               // define constraints for button
         constraints.gridy = 5;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button22, constraints);
         button22.addActionListener(this);
         add(button22);                        // add button
 
         constraints.gridx = 1;               // define constraints for button
         constraints.gridy = 5;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button23, constraints);
         button23.addActionListener(this);
         add(button23);                        // add button
 
         constraints.gridx = 2;               // define constraints for button
         constraints.gridy = 5;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button24, constraints);
         button24.addActionListener(this);
         add(button24);                        // add button
 
         constraints.gridx = 3;               // define constraints for button
         constraints.gridy = 5;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button25, constraints);
         button25.addActionListener(this);
         add(button25);                        // add button
 
         constraints.gridx = 4;                // define constraints for button
         constraints.gridy = 5;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button26, constraints);
         button26.addActionListener(this);
         add(button26);                        // add button
 
         constraints.gridx = 5;                // define constraints for button
         constraints.gridy = 5;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button27, constraints);
         button27.addActionListener(this);
         add(button27);                        // add button
 
         constraints.gridx = 6;                // define constraints for button
         constraints.gridy = 5;
         constraints.gridwidth = 1;
         constraints.gridheight = 1;
         layout.setConstraints(button28, constraints);
         button28.addActionListener(this);
         add(button28);                        // add button
 
         // if leapYear then add to any month
         // if not leapYear, add to all but february
         if ((isLeapYear) || (!isLeapYear && curMonth != 2)) {
             constraints.gridx = 0;              // define constraints for button
             constraints.gridy = 6;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(button29, constraints);
             button29.addActionListener(this);
             add(button29);
         }// add button
 
         if (curMonth != 2) {
             constraints.gridx = 1;                // define constraints for button
             constraints.gridy = 6;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(button30, constraints);
             button30.addActionListener(this);
             add(button30);                        // add button
         }
 
         if (curMonth == 1 || curMonth == 3 || curMonth == 5 || curMonth == 7
                 || curMonth == 8 || curMonth == 10 || curMonth == 12) {
             constraints.gridx = 2;             // define constraints for button
             constraints.gridy = 6;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(button31, constraints);
             button31.addActionListener(this);
             add(button31);                        // add button
         }
     }
 
     /**
      *
      * @param e
      */
     @Override
     public void actionPerformed(ActionEvent e) {           // Process "click" on drawIt, position combo box
 
         String[][] monthArray;
 
         // Determine which button is clicked
 
         if (e.getSource() == button1) {
             popUpMenu();
             dayClicked = 1;
         }
         if (e.getSource() == button2) {
             popUpMenu();
             dayClicked = 2;
         }
         if (e.getSource() == button3) {
             popUpMenu();
             dayClicked = 3;
         }
         if (e.getSource() == button4) {
             popUpMenu();
             dayClicked = 4;
         }
         if (e.getSource() == button5) {
             popUpMenu();
             dayClicked = 5;
         }
         if (e.getSource() == button6) {
             popUpMenu();
             dayClicked = 6;
         }
         if (e.getSource() == button7) {
             popUpMenu();
             dayClicked = 7;
         }
         if (e.getSource() == button8) {
             popUpMenu();
             dayClicked = 8;
         }
         if (e.getSource() == button9) {
             popUpMenu();
             dayClicked = 9;
         }
         if (e.getSource() == button10) {
             popUpMenu();
             dayClicked = 10;
         }
         if (e.getSource() == button11) {
             popUpMenu();
             dayClicked = 11;
         }
         if (e.getSource() == button12) {
             popUpMenu();
             dayClicked = 12;
         }
         if (e.getSource() == button13) {
             popUpMenu();
             dayClicked = 13;
         }
         if (e.getSource() == button14) {
             popUpMenu();
             dayClicked = 14;
         }
         if (e.getSource() == button15) {
             popUpMenu();
             dayClicked = 15;
         }
         if (e.getSource() == button16) {
             popUpMenu();
             dayClicked = 16;
         }
         if (e.getSource() == button17) {
             popUpMenu();
             dayClicked = 17;
         }
         if (e.getSource() == button18) {
             popUpMenu();
             dayClicked = 18;
         }
         if (e.getSource() == button19) {
             popUpMenu();
             dayClicked = 19;
         }
         if (e.getSource() == button20) {
             popUpMenu();
             dayClicked = 20;
         }
         if (e.getSource() == button21) {
             popUpMenu();
             dayClicked = 21;
         }
         if (e.getSource() == button22) {
             popUpMenu();
             dayClicked = 22;
         }
         if (e.getSource() == button23) {
             popUpMenu();
             dayClicked = 23;
         }
         if (e.getSource() == button24) {
             dayClicked = 24;
             popUpMenu();
 
         }
         if (e.getSource() == button25) {
             popUpMenu();
             dayClicked = 25;
         }
         if (e.getSource() == button26) {
             popUpMenu();
             dayClicked = 26;
         }
         if (e.getSource() == button27) {
             popUpMenu();
             dayClicked = 27;
         }
         if (e.getSource() == button28) {
             popUpMenu();
             dayClicked = 28;
         }
         if (e.getSource() == button29) {
             popUpMenu();
             dayClicked = 29;
         }
         if (e.getSource() == button30) {
             popUpMenu();
             dayClicked = 30;
         }
         if (e.getSource() == button31) {
             popUpMenu(); // pass a key to popUpMenu in order to access the info stored for date 31
             dayClicked = 31;
         }
 
         // alarm button
         if (e.getSource() == alarmButton) {
 
             switch (curMonth) {
                 case 1:
                     monthArray = january;
                     break;
                 case 2:
                     monthArray = february;
                     break;
                 case 3:
                     monthArray = march;
                     break;
                 case 4:
                     monthArray = april;
                     break;
                 case 5:
                     monthArray = may;
                     break;
                 case 6:
                     monthArray = june;
                     break;
                 case 7:
                     monthArray = july;
                     break;
                 case 8:
                     monthArray = august;
                     break;
                 case 9:
                     monthArray = september;
                     break;
                 case 10:
                     monthArray = october;
                     break;
                 case 11:
                     monthArray = november;
                     break;
                 case 12:
                     monthArray = december;
                     break;
                 default:
                     monthArray = november;
             }
 
 
             try {
                 switch (dayClicked) {
                     case 1:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay1++);
                         break;
                     case 2:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay2++);
                         break;
                     case 3:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay3++);
                         break;
                     case 4:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay4++);
                         break;
                     case 5:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay5++);
                         break;
                     case 6:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay6++);
                         break;
                     case 7:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay7++);
                         break;
                     case 8:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay8++);
                         break;
                     case 9:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay9++);
                         break;
                     case 10:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay10++);
                         break;
                     case 11:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay11++);
                         break;
                     case 12:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay12++);
                         break;
                     case 13:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay13++);
                         break;
                     case 14:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay14++);
                         break;
                     case 15:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay15++);
                         break;
                     case 16:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay16++);
                         break;
                     case 17:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay17++);
                         break;
                     case 18:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay18++);
                         break;
                     case 19:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay19++);
                         break;
                     case 20:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay20++);
                         break;
                     case 21:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay21++);
                         break;
                     case 22:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay22++);
                         break;
                     case 23:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay23++);
                         break;
                     case 24:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay24++);
                         break;
                     case 25:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay25++);
                         break;
                     case 26:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay26++);
                         break;
                     case 27:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay27++);
                         break;
                     case 28:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay28++);
                         break;
                     case 29:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay29++);
                         break;
                     case 30:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay30++);
                         break;
                     case 31:
                         alarm = new Alarm(monthArray, dayClicked - 1, indexDay31++);
                         break;
                 }
             } catch (LineUnavailableException ex) {
                 Logger.getLogger(DrawCalendar.class.getName()).log(Level.SEVERE, null, ex);
             } catch (UnsupportedAudioFileException ex) {
                 Logger.getLogger(DrawCalendar.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 Logger.getLogger(DrawCalendar.class.getName()).log(Level.SEVERE, null, ex);
             }
 
         }
 
 
         // Event button
         if (e.getSource() == eventButton) {
             switch (curMonth) {
                 case 1:
                     monthArray = january;
                     break;
                 case 2:
                     monthArray = february;
                     break;
                 case 3:
                     monthArray = march;
                     break;
                 case 4:
                     monthArray = april;
                     break;
                 case 5:
                     monthArray = may;
                     break;
                 case 6:
                     monthArray = june;
                     break;
                 case 7:
                     monthArray = july;
                     break;
                 case 8:
                     monthArray = august;
                     break;
                 case 9:
                     monthArray = september;
                     break;
                 case 10:
                     monthArray = october;
                     break;
                 case 11:
                     monthArray = november;
                     break;
                 case 12:
                     monthArray = december;
                     break;
                 default:
                     monthArray = november;
             }
 
             switch (dayClicked) {
                 case 1:
                     event = new Event(monthArray, dayClicked - 1, indexDay1++);
                     break;
                 case 2:
                     event = new Event(monthArray, dayClicked - 1, indexDay2++);
                     break;
                 case 3:
                     event = new Event(monthArray, dayClicked - 1, indexDay3++);
                     break;
                 case 4:
                     event = new Event(monthArray, dayClicked - 1, indexDay4++);
                     break;
                 case 5:
                     event = new Event(monthArray, dayClicked - 1, indexDay5++);
                     break;
                 case 6:
                     event = new Event(monthArray, dayClicked - 1, indexDay6++);
                     break;
                 case 7:
                     event = new Event(monthArray, dayClicked - 1, indexDay7++);
                     break;
                 case 8:
                     event = new Event(monthArray, dayClicked - 1, indexDay8++);
                     break;
                 case 9:
                     event = new Event(monthArray, dayClicked - 1, indexDay9++);
                     break;
                 case 10:
                     event = new Event(monthArray, dayClicked - 1, indexDay10++);
                     break;
                 case 11:
                     event = new Event(monthArray, dayClicked - 1, indexDay11++);
                     break;
                 case 12:
                     event = new Event(monthArray, dayClicked - 1, indexDay12++);
                     break;
                 case 13:
                     event = new Event(monthArray, dayClicked - 1, indexDay13++);
                     break;
                 case 14:
                     event = new Event(monthArray, dayClicked - 1, indexDay14++);
                     break;
                 case 15:
                     event = new Event(monthArray, dayClicked - 1, indexDay15++);
                     break;
                 case 16:
                     event = new Event(monthArray, dayClicked - 1, indexDay16++);
                     break;
                 case 17:
                     event = new Event(monthArray, dayClicked - 1, indexDay17++);
                     break;
                 case 18:
                     event = new Event(monthArray, dayClicked - 1, indexDay18++);
                     break;
                 case 19:
                     event = new Event(monthArray, dayClicked - 1, indexDay19++);
                     break;
                 case 20:
                     event = new Event(monthArray, dayClicked - 1, indexDay20++);
                     break;
                 case 21:
                     event = new Event(monthArray, dayClicked - 1, indexDay21++);
                     break;
                 case 22:
                     event = new Event(monthArray, dayClicked - 1, indexDay22++);
                     break;
                 case 23:
                     event = new Event(monthArray, dayClicked - 1, indexDay23++);
                     break;
                 case 24:
                     event = new Event(monthArray, dayClicked - 1, indexDay24++);
                     break;
                 case 25:
                     event = new Event(monthArray, dayClicked - 1, indexDay25++);
                     break;
                 case 26:
                     event = new Event(monthArray, dayClicked - 1, indexDay26++);
                     break;
                 case 27:
                     event = new Event(monthArray, dayClicked - 1, indexDay27++);
                     break;
                 case 28:
                     event = new Event(monthArray, dayClicked - 1, indexDay28++);
                     break;
                 case 29:
                     event = new Event(monthArray, dayClicked - 1, indexDay29++);
                     break;
                 case 30:
                     event = new Event(monthArray, dayClicked - 1, indexDay30++);
                     break;
                 case 31:
                     event = new Event(monthArray, dayClicked - 1, indexDay31++);
                     break;
 
                 /*   if(Integer.getInteger(event.sHour) < Integer.getInteger(event.eHour)) {
                  EventException ee = new EventException();
                  throw ee;
                  }
                  } catch (EventException ex) {
                  JOptionPane.showMessageDialog(null, "You must enter a start time"
                  + " after your end time.");
                  }
                  * */
             }
         }
 
         // memo button
         if (e.getSource() == reminderButton) {
             switch (curMonth) {
                 case 1:
                     monthArray = january;
                     break;
                 case 2:
                     monthArray = february;
                     break;
                 case 3:
                     monthArray = march;
                     break;
                 case 4:
                     monthArray = april;
                     break;
                 case 5:
                     monthArray = may;
                     break;
                 case 6:
                     monthArray = june;
                     break;
                 case 7:
                     monthArray = july;
                     break;
                 case 8:
                     monthArray = august;
                     break;
                 case 9:
                     monthArray = september;
                     break;
                 case 10:
                     monthArray = october;
                     break;
                 case 11:
                     monthArray = november;
                     break;
                 case 12:
                     monthArray = december;
                     break;
                 default:
                     monthArray = november;
             }
             try {
                 switch (dayClicked) {
                     case 1:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay1++);
                         break;
                     case 2:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay2++);
                         break;
                     case 3:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay3++);
                         break;
                     case 4:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay4++);
                         break;
                     case 5:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay5++);
                         break;
                     case 6:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay6++);
                         break;
                     case 7:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay7++);
                         break;
                     case 8:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay8++);
                         break;
                     case 9:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay9++);
                         break;
                     case 10:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay10++);
                         break;
                     case 11:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay11++);
                         break;
                     case 12:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay12++);
                         break;
                     case 13:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay13++);
                         break;
                     case 14:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay14++);
                         break;
                     case 15:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay15++);
                         break;
                     case 16:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay16++);
                         break;
                     case 17:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay17++);
                         break;
                     case 18:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay18++);
                         break;
                     case 19:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay19++);
                         break;
                     case 20:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay20++);
                         break;
                     case 21:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay21++);
                         break;
                     case 22:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay22++);
                         break;
                     case 23:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay23++);
                         break;
                     case 24:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay24++);
                         break;
                     case 25:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay25++);
                         break;
                     case 26:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay26++);
                         break;
                     case 27:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay27++);
                         break;
                     case 28:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay28++);
                         break;
                     case 29:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay29++);
                         break;
                     case 30:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay30++);
                         break;
                     case 31:
                         memo = new Memo(monthArray, dayClicked - 1, indexDay31++);
                         break;
                 }
             } catch (LineUnavailableException ex) {
                 Logger.getLogger(DrawCalendar.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 Logger.getLogger(DrawCalendar.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         if (e.getSource() == viewEvents) {
             viewDate();
         }
 
         //repaint();
         // I need to make 12 arrays, each has 30 indices, each index has a list. 
         //When person creates an alarmButton, alert, or eventButton it calls nicole/christina's class 
         // then their class creates eventButton/alarm/alert and they will pass me a string holding tht info so i 
         // can add it to the list in the array so can display
         // no need for delete function, too messy     
     }
 
     public void popUpMenu() {
         popUp = new JFrame("Select");
         Font newFont = new Font("Arial BLACK", Font.BOLD, 15); // Change font size on Font variable f
 
         alarmButton = new JButton("Add Alarm");       // create new alarmButton button
         reminderButton = new JButton("Add Memo");     // create new reminderButton button
         eventButton = new JButton("Add Event");           // create new eventButton button
         viewEvents = new JButton("View Today's Schedule");
 
         // add action listeners
         alarmButton.addActionListener(this);
         reminderButton.addActionListener(this);
         eventButton.addActionListener(this);
         viewEvents.addActionListener(this);
 
         // Set button fonts
         alarmButton.setFont(newFont);
         reminderButton.setFont(newFont);
         eventButton.setFont(newFont);
         viewEvents.setFont(f);
 
         pan2 = new JPanel();
         pan2.setLayout(new GridBagLayout());         // set layout of JPanel to Grid Bag Layout
         pan2.add(alarmButton, new GridBagConstraints());        // add month variable to panel
         pan2.add(eventButton, new GridBagConstraints());        // add month variable to panel
         pan2.add(reminderButton, new GridBagConstraints());        // add month variable to panel
         pan2.add(viewEvents, new GridBagConstraints());
 
         popUp.setLocation(230, 300);           // set location of pop up menu on the screen
         popUp.setBackground(Color.YELLOW);
         popUp.setLayout(new FlowLayout());
         popUp.setSize(800, 100);      // Set default size of JFrame Calendar
         popUp.setResizable(false);     // Allow JFrame to be resized
         popUp.add(pan2);        // add calender GUI to JFrame
         popUp.setVisible(true);       // Set visibility of JFrame
     }
 
     public void viewDate() {
         schedule = new JFrame("Schedule");
         // monthLabel, dateLabel, yearLabel, listOfEvents
         if (curMonth == 1) {
             monthLabel = new JLabel("January ");
         }
         if (curMonth == 2) {
             monthLabel = new JLabel("February ");
         }
         if (curMonth == 3) {
             monthLabel = new JLabel("March ");
         }
         if (curMonth == 4) {
             monthLabel = new JLabel("April ");
         }
         if (curMonth == 5) {
             monthLabel = new JLabel("May ");
         }
         if (curMonth == 6) {
             monthLabel = new JLabel("June ");
         }
         if (curMonth == 7) {
             monthLabel = new JLabel("July ");
         }
         if (curMonth == 8) {
             monthLabel = new JLabel("August ");
         }
         if (curMonth == 9) {
             monthLabel = new JLabel("September ");
         }
         if (curMonth == 10) {
             monthLabel = new JLabel("October ");
         }
         if (curMonth == 11) {
             monthLabel = new JLabel("November ");
         }
         if (curMonth == 12) {
             monthLabel = new JLabel("December ");
         }
         dateLabel = new JLabel(dayClicked + ", ");
         yearLabel = new JLabel(curYear + " ");
         listOfEvents = new JTextArea();
 
         // Determine which array to use       
         if (curMonth == 1) {
             // Define current day
             if (dayClicked == 1) {
                 for (int j = 0; j < indexDay1; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 for (int j = 0; j < indexDay2; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 for (int j = 0; j < indexDay3; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 for (int j = 0; j < indexDay4; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 for (int j = 0; j < indexDay5; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 for (int j = 0; j < indexDay6; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 for (int j = 0; j < indexDay7; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 for (int j = 0; j < indexDay8; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 for (int j = 0; j < indexDay9; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 for (int j = 0; j < indexDay10; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 for (int j = 0; j < indexDay11; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 for (int j = 0; j < indexDay12; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 for (int j = 0; j < indexDay13; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 for (int j = 0; j < indexDay14; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 for (int j = 0; j < indexDay15; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 for (int j = 0; j < indexDay16; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 for (int j = 0; j < indexDay17; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 for (int j = 0; j < indexDay18; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 for (int j = 0; j < indexDay19; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 for (int j = 0; j < indexDay20; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 for (int j = 0; j < indexDay21; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 for (int j = 0; j < indexDay22; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 for (int j = 0; j < indexDay23; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 for (int j = 0; j < indexDay24; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 for (int j = 0; j < indexDay25; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 for (int j = 0; j < indexDay26; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 for (int j = 0; j < indexDay27; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 for (int j = 0; j < indexDay28; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 for (int j = 0; j < indexDay29; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 30) {
                 for (int j = 0; j < indexDay30; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 31) {
                 for (int j = 0; j < indexDay31; j++) {
                     if (january[dayClicked - 1][j] != null) {
                         listOfEvents.append(january[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
 
         // FEBRUARY!!!!
 
         if (curMonth == 2) {
             // Define current day
             if (dayClicked == 1) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay1; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay2; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay3; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay4; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay5; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay6; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay7; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay8; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay9; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay10; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay11; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay12; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay13; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay14; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay15; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay16; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay17; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay18; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay19; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay20; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay21; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay22; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay23; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay24; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay25; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay26; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay27; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay28; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay29; j++) {
                     if (february[dayClicked - 1][j] != null) {
                         listOfEvents.append(february[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
 
         // march
 
         if (curMonth == 3) {
             // Define current day
             if (dayClicked == 1) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay1; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay2; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay3; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay4; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay5; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay6; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay7; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay8; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay9; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay10; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay11; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay12; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay13; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay14; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay15; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay16; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay17; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay18; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay19; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay20; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay21; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay22; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 for (int j = 0; j < indexDay23; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay24; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay25; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay26; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay27; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay28; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay29; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 30) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay30; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 31) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay31; j++) {
                     if (march[dayClicked - 1][j] != null) {
                         listOfEvents.append(march[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
 
         // april
         if (curMonth == 4) {
             // Define current day
             if (dayClicked == 1) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay1; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay2; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay3; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay4; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay5; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay6; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay7; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay8; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay9; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay10; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay11; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay12; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay13; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay14; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay15; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay16; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay17; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay18; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay19; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay20; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay21; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay22; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay23; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay24; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay25; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay26; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay27; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay28; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay29; j++) {
                     if (may[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 30) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay30; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(april[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
 
         // may
         if (curMonth == 5) {
             // Define current day
             if (dayClicked == 1) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay1; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay2; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay3; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay4; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay5; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay6; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay7; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay8; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay9; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay10; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay11; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay12; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay13; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay14; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay15; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay16; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay17; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay18; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay19; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay20; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay21; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay22; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay23; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay24; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay25; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay26; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay27; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay28; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay29; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 30) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay30; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 31) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay31; j++) {
                     if (april[dayClicked - 1][j] != null) {
                         listOfEvents.append(may[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
 
         // june
         if (curMonth == 6) {
             listOfEvents.setText("");
             // Define current day
             if (dayClicked == 1) {
                 for (int j = 0; j < indexDay1; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay2; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay3; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay4; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay5; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay6; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay7; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay8; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay9; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay10; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay11; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay12; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay13; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay14; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay15; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay16; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay17; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay18; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay19; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay20; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay21; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay22; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay23; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay24; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay25; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay26; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay27; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay28; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay29; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 30) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay30; j++) {
                     if (june[dayClicked - 1][j] != null) {
                         listOfEvents.append(june[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
 
         // july
         if (curMonth == 7) {
             // Define current day
             if (dayClicked == 1) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay1; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay2; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay3; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay4; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay5; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay6; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay7; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay8; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay9; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay10; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay11; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay12; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay13; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay14; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay15; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay16; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay17; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay18; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay19; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay20; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay21; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay22; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay23; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay24; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay25; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay26; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay27; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay28; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay29; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 30) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay30; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 31) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay31; j++) {
                     if (july[dayClicked - 1][j] != null) {
                         listOfEvents.append(july[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
 
         // august
         if (curMonth == 8) {
             // Define current day
             if (dayClicked == 1) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay1; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay2; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay3; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay4; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay5; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay6; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay7; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay8; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay9; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay10; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay11; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay12; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay13; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay14; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay15; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay16; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay17; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay18; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay19; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay20; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay21; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay22; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay23; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay24; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay25; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay26; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay27; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay28; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay29; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 30) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay30; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 31) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay31; j++) {
                     if (august[dayClicked - 1][j] != null) {
                         listOfEvents.append(august[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
 
         // september
         if (curMonth == 9) {
             // Define current day
             if (dayClicked == 1) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay1; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay2; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay3; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay4; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay5; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay6; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay7; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay8; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay9; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay10; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay11; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay12; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay13; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay14; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay15; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay16; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay17; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay18; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay19; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay20; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay21; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay22; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay23; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay24; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay25; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay26; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay27; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay28; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay29; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 30) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay30; j++) {
                     if (september[dayClicked - 1][j] != null) {
                         listOfEvents.append(september[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
 
         // october
         if (curMonth == 10) {
             // Define current day
             if (dayClicked == 1) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay1; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay2; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay3; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay4; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay5; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay6; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay7; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay8; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay9; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay10; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay11; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay12; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay13; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay14; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay15; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay16; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay17; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay18; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay19; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay20; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay21; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay22; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay23; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay24; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay25; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay26; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay27; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay28; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay29; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 30) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay30; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 31) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay31; j++) {
                     if (october[dayClicked - 1][j] != null) {
                         listOfEvents.append(october[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
 
         // november
         if (curMonth == 11) {
             // Define current day
             if (dayClicked == 1) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay1; j++) {  // SO DOESN'T DISPLAY NULL
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay2; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay3; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay4; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay5; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay6; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay7; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay8; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay9; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay10; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay11; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay12; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay13; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay14; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay15; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay16; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay17; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay18; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay19; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay20; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay21; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay22; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay23; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay24; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay25; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay26; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay27; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay28; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay29; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 30) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay30; j++) {
                     if (november[dayClicked - 1][j] != null) {
                         listOfEvents.append(november[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
 
         // december
         if (curMonth == 12) {
             // Define current day
             if (dayClicked == 1) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay1; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 2) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay2; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 3) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay3; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 4) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay4; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
 
             if (dayClicked == 5) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay5; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 6) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay6; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 7) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay7; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 8) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay8; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 9) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay9; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 10) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay10; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 11) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay11; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 12) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay12; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 13) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay13; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 14) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay14; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 15) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay15; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 16) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay16; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 17) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay17; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 18) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay18; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 19) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay19; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 20) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay20; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 21) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay21; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 22) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay22; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 23) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay23; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 24) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay24; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 25) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay25; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 26) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay26; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 27) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay27; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 28) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay28; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 29) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay29; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 30) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay30; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
             if (dayClicked == 31) {
                 listOfEvents.setText("");
                 for (int j = 0; j < indexDay31; j++) {
                     if (december[dayClicked - 1][j] != null) {
                         listOfEvents.append(december[dayClicked - 1][j] + "\n");
                     }
                 }
             }
         }
         f = new Font("Arial BLACK", Font.BOLD, 24); // Change font size on Font variable f
 
         monthLabel.setFont(f);
         dateLabel.setFont(f);
         yearLabel.setFont(f);
 
         f = new Font("Arial BLACK", Font.PLAIN, 15); // Change font size on Font variable f
         listOfEvents.setFont(f);
         listOfEvents.setEditable(false);
 
         pan3 = new JPanel();
         pan4 = new JPanel();
 
         pan3.add(monthLabel);
         pan3.add(dateLabel);
         pan3.add(yearLabel);
         pan4.add(listOfEvents);
 
         schedule.setLocation(430, 300);           // set location of pop up menu on the screen
         schedule.setBackground(Color.GREEN);
         schedule.setLayout(new BorderLayout());
         schedule.setSize(500, 300);      // Set default size of JFrame Calendar
         schedule.setResizable(true);     // Allow JFrame to be resized
         schedule.add(pan3, BorderLayout.NORTH);        // add calender GUI to JFrame
         schedule.add(pan4, BorderLayout.CENTER);
         schedule.setVisible(true);       // Set visibility of JFrame
     }
 
     public static void main(String[] args) {
         DrawCalendar myCalendar = new DrawCalendar(); // window for drawing 
         JFrame application = new JFrame("Calendar"); // the program itself 
         Scanner input = new Scanner(System.in);
         application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // set frame to exit when it is closed 
         application.setSize(800, 800);      // Set default size of JFrame Calendar
         application.setLocation(230, 0);
         application.setResizable(true);     // Allow JFrame to be resized
         application.add(myCalendar);        // add calender GUI to JFrame
         application.setVisible(true);       // Set visibility of JFrame
     }
 }
