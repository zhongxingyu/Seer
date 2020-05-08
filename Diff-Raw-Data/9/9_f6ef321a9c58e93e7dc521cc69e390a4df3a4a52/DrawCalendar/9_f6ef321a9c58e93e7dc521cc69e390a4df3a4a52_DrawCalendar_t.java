 package calendar;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.sql.*;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.*;
 
 /**
  * @author Kelly Hutchison <kmh5754 at psu.edu>
  */
 public class DrawCalendar extends JPanel implements ActionListener {
     //<editor-fold defaultstate="collapsed" desc="Variables">
 
     private GridBagLayout layout;                 // layout of applet
     private GridBagConstraints constraints;       // helper for the layout
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
     private String currentMonth;
     private String currentYear;
     private Calendar cal;
     private GregorianCalendar checkYear;
     private int curYear;
     private int curMonth;
     private int curDay;
     private Integer dayClicked;
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
     private JFrame popUp = null;
     private JFrame schedule;
     private JButton next;
     private JButton back;
     private ArrayList<MyButton> bttns = new ArrayList<>();
     private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
     private Connection dbConn = null;
     private Statement stmt;
     private ResultSet results;
     private String sql;
     //private Boolean future=true;
     //</editor-fold>
 
     // Default Constructor
     public DrawCalendar() throws InstantiationException, ClassNotFoundException, IllegalAccessException, SQLException, IOException {
         // set up graphics window 
         super(); // this can be omitted 
 
         setBackground(Color.WHITE);
         layout = new GridBagLayout();             // set up layout
         setLayout(layout);
         constraints = new GridBagConstraints();
         cal = Calendar.getInstance();
 
         checkYear = new GregorianCalendar();
         dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
         curMonth = cal.get(Calendar.MONTH) + 1; // zero based
         curYear = cal.get(Calendar.YEAR);
         curDay = cal.get(Calendar.DATE);
         isLeapYear = checkYear.isLeapYear(curYear);
 
         // Default dayClicked to -1
         dayClicked = -1;
 
         // set currentYear string to the current year
         currentYear = Integer.toString(curYear);
 
         // Set current month 
         // set the currentMonth string to the current month
         //<editor-fold defaultstate="collapsed" desc="Sets currentMonth">
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
         //</editor-fold>
 
         DBManager.connect();
         dbConn = DBManager.getConnection();
         stmt = dbConn.createStatement();
 
         try {
 
             // test to see if we need to run the initial sql script
             DatabaseMetaData dbMeta = dbConn.getMetaData();
             String[] tableTypes = {"TABLE"};
             results = dbMeta.getTables(null, null, "%", tableTypes);
 
             if (!results.next()) {
                 // no table found, we need to execute the initial sql statements
                 // first read the sql script
                 InputStream is = DrawCalendar.class.getResourceAsStream("initSQL.sql");
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr);
                 StringBuilder sb = new StringBuilder();
                 String line;
                 line = br.readLine();
                 while (line != null) {
                     sb.append(line);
                     if (line.contains(";")) {
                         sql = sb.toString();
                         sb = new StringBuilder();
                         // executeQuery cannot process ; so remove it from sql
                         sql = sql.substring(0, sql.indexOf(';'));
                         // run sql here
                         if (sql.toUpperCase().contains("SELECT")) {
                             // SELECT statement
                             stmt.executeQuery(sql);
                         } else {
                             // All ohter type of statements
                             stmt.execute(sql);
                         }
                     }
                     line = br.readLine();
                 }
             }
 
             results.close();
             stmt.close();
         } // don't have much about exception handling yet
         catch (SQLException e) {
             // could not start Derby engine
             e.printStackTrace();
         }
 
 
         for (int i = 0; i < 31; i++) {
             MyButton mb = new MyButton(i);
             bttns.add(mb);
         }
         
        f = new Font("Arial BLACK", Font.PLAIN, 20);
         for (int i = 1; i <= 31; i++) {
             bttns.get(i - 1).setFont(f);
             bttns.get(i - 1).setBackground(Color.WHITE);
             bttns.get(i - 1).setOpaque(false);
             bttns.get(i - 1).setBorderPainted(false);
         }
        f = new Font("Arial BLACK", Font.BOLD, 20);
        for (int i = 0; i < 31; i++) {
            bttns.get(curDay - 1).setFont(f);
            bttns.get(curDay - 1).setBackground(Color.CYAN);
            bttns.get(curDay - 1).setOpaque(true);
            bttns.get(curDay - 1).setBorderPainted(true);
        }
 
         int counter = 0;
         try {
             stmt = dbConn.createStatement();
             results = stmt.executeQuery("select count(*) from "
                     + "calendar where datetime ='" + curMonth
                     + bttns.get(0).getDay() + curYear + "'");
             while (results.next()) {
                 counter = results.getInt(1);
             }
 
             if (counter == 0) {
                 for (int j = 0; j < 31; j++) {
                     stmt.execute("insert into calendar (datetime,memo) values ('"
                             + curMonth + bttns.get(j).getDay() + curYear + "', '')");
                 }
             }
 
             stmt.close();
             results.close();
         } catch (SQLException sqlex) {
             sqlex.printStackTrace();
         }
 
         pan1 = new JPanel();                     // Instantiate JPanel
 
         f = new Font("Arial BLACK", Font.BOLD, 30); // Change font size on Font variable f
         month = new JLabel("     " + currentMonth + " " + currentYear + "     ");    // Assign JLabel a value
         month.setFont(f);                               // Assign JLabel to font f
 
         back = new JButton("Previous Month");
         next = new JButton("Next Month");
         back.addActionListener(this);
         next.addActionListener(this);
 
         pan1.setLayout(new GridBagLayout());         // set layout of JPanel to Grid Bag Layout
         GridBagConstraints gbc = new GridBagConstraints();      // set constraints
         gbc.insets = new Insets(5, 50, 5, 50);
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.BOTH;
         layout.setConstraints(pan1, gbc);
         pan1.add(back, gbc);
 
         gbc.gridx = 1;
         gbc.gridy = 0;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.BOTH;
         layout.setConstraints(pan1, gbc);
         pan1.add(month, gbc);//, gbc);        // add month variable to panel
 
         gbc.gridx = 2;
         gbc.gridy = 0;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.BOTH;
         layout.setConstraints(pan1, gbc);
         pan1.add(next, gbc);
 
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
         gbc = new GridBagConstraints();
 
         gbc.fill = GridBagConstraints.BOTH;
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         gbc.insets = new Insets(space, space, space, space);       // Set spacing between buttons
 
         weekdayPanel.setLayout(new GridBagLayout());
 
         weekdayPanel.setBackground(Color.CYAN);
         //weekdayPanel.add(sunday);
 
         // Find first day of week of month
         //<editor-fold defaultstate="collapsed" desc="Sets first day of the week of month">
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
         //</editor-fold>
 
 
         gbc.gridx = 0;
         gbc.gridy = 1;
         gbc.gridwidth = 7;
         gbc.gridheight = 1;
         layout.setConstraints(weekdayPanel, gbc);
         add(weekdayPanel);
 
         f = new Font("Arial", Font.PLAIN, 20);  // Assign new font for dates
 
         // set default fonts of day numbers
         for (int i = 0; i < 31; i++) {
             bttns.get(i).setFont(f);
         }
 
         // Set un-focusable 
         // So when application runs, none of buttons are initially selected
         for (int i = 0; i < 31; i++) {
             bttns.get(i).setFocusable(false);
         }
 
         f = new Font("Arial BLACK", Font.BOLD, 20);
         // Set current date block to background color 
         // Set current day block to be bold
         for (int i = 1; i <= 31; i++) {
             if (curDay == i && curMonth == cal.get(Calendar.MONTH)) {
                 bttns.get(i - 1).setFont(f);
                 bttns.get(i - 1).setBackground(Color.CYAN);
                 bttns.get(i - 1).setOpaque(true);
                 bttns.get(i - 1).setBorderPainted(true);
             }
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
 
         for (int i = 0; i < 7; i++) {
             constraints.gridx = i;
             constraints.gridy = 2;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(bttns.get(i), constraints);
             bttns.get(i).addActionListener(this);
             add(bttns.get(i));
         }
 
         for (int i = 0; i < 7; i++) {
             constraints.gridx = i;
             constraints.gridy = 3;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(bttns.get(i + 7), constraints);
             bttns.get(i + 7).addActionListener(this);
             add(bttns.get(i + 7));
         }
 
         for (int i = 0; i < 7; i++) {
             constraints.gridx = i;
             constraints.gridy = 4;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(bttns.get(i + 14), constraints);
             bttns.get(i + 14).addActionListener(this);
             add(bttns.get(i + 14));
         }
 
         for (int i = 0; i < 7; i++) {
             constraints.gridx = i;
             constraints.gridy = 5;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(bttns.get(i + 21), constraints);
             bttns.get(i + 21).addActionListener(this);
             add(bttns.get(i + 21));
         }
 
         if ((isLeapYear) || (!isLeapYear && curMonth != 2)) {
             constraints.gridx = 0;              // define constraints for button
             constraints.gridy = 6;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(bttns.get(28), constraints);
             bttns.get(28).addActionListener(this);
             add(bttns.get(28));
         }
 
         if (curMonth != 2) {
             constraints.gridx = 1;                // define constraints for button
             constraints.gridy = 6;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(bttns.get(29), constraints);
             bttns.get(29).addActionListener(this);
             add(bttns.get(29));                        // add button
         }
 
         if (curMonth == 1 || curMonth == 3 || curMonth == 5 || curMonth == 7
                 || curMonth == 8 || curMonth == 10 || curMonth == 12) {
             constraints.gridx = 2;             // define constraints for button
             constraints.gridy = 6;
             constraints.gridwidth = 1;
             constraints.gridheight = 1;
             layout.setConstraints(bttns.get(30), constraints);
             bttns.get(30).addActionListener(this);
             add(bttns.get(30));                        // add button
         }
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {           // Process "click" on drawIt, position combo box
         // Determine which button is clicked
         for (int i = 0; i < 31; i++) {
             if (e.getSource() == bttns.get(i)) {
                 popUpMenu();
                 dayClicked = i + 1;
             }
         }
 
         if (e.getSource() == next) {
             if (curMonth < 12) {
                 ++curMonth;
             } else {
                 curMonth = 1;
                 curYear += 1;
             }
             repaint();
 
         }
 
         if (e.getSource() == back) {
             if(curMonth > 2){
             --curMonth;
             }
             else{
                 curMonth = 12;
                 --curYear;
             }
             repaint();
         }
 
         // alarm button
         if (e.getSource() == alarmButton) {
             try {
                 alarm = new Alarm(bttns.get(dayClicked - 1), curMonth, curYear);
                 repaint();
             } 
             catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
                 Logger.getLogger(DrawCalendar.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         // Event button
         if (e.getSource() == eventButton) {
             event = new Event(bttns.get(dayClicked - 1), curMonth, curYear);
             repaint();
         }
 
         // memo button
         if (e.getSource() == reminderButton) {
             //   memo = new Memo(monthArray, dayClicked - 1, indexDay1++);
             repaint();
         }
 
         if (e.getSource() == viewEvents) {
             try {
                 viewDate();
             } catch (SQLException ex) {
                 Logger.getLogger(DrawCalendar.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         repaint();
     }
 
     public void popUpMenu() {
         String currentTitle = dayClicked.toString();
 
         if (popUp != null) {
             popUp.dispose();
         }
 
         popUp = new JFrame("Select: " + currentTitle);
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
         viewEvents.setFont(newFont);
 
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
 
     @Override
     public void paint(Graphics g) {
         super.paint(g);
         //g.setColor(Color.GREEN);
 
         f = new Font("Arial BLACK", Font.ITALIC, 20);
         int count = 0;
         // Change color of day number when something is stored on that day
         // Day 1
         // Change Month Label
         switch (curMonth) {
             case 1: {
                 month.setText("January " + curYear);
                 break;
             }
             case 2: {
                 month.setText("February " + curYear);
                 break;
             }
             case 3: {
                 month.setText("March " + curYear);
                 break;
             }
             case 4: {
                 month.setText("April " + curYear);
                 break;
             }
             case 5: {
                 month.setText("May " + curYear);
                 break;
             }
             case 6: {
                 month.setText("June " + curYear);
                 break;
             }
             case 7: {
                 month.setText("July " + curYear);
                 break;
             }
             case 8: {
                 month.setText("August " + curYear);
                 break;
             }
             case 9: {
                 month.setText("September " + curYear);
                 break;
             }
             case 10: {
                 month.setText("October " + curYear);
                 break;
             }
             case 11: {
                 month.setText("November " + curYear);
                 break;
             }
             case 12: {
                 month.setText("December " + curYear);
                 break;
             }
         }
         
         // If not current month and year, unhighlight "today" btn
         if (curMonth != cal.get(Calendar.MONTH) && curYear != cal.get(Calendar.YEAR)) {
             f = new Font("Arial BLACK", Font.PLAIN, 20);
             // Set current date block to background color 
             // Set current day block to be bold
             for (int i = 1; i <= 31; i++) {
                 bttns.get(i - 1).setFont(f);
                 bttns.get(i - 1).setBackground(Color.WHITE);
                 bttns.get(i - 1).setOpaque(false);
                 bttns.get(i - 1).setBorderPainted(false);
             }
         }
         // If go back to current month and year, highlight "today" btn        
         if (curMonth == cal.get(Calendar.MONTH) && curYear == cal.get(Calendar.YEAR)) {
             f = new Font("Arial BLACK", Font.BOLD, 20);
             // Set current date block to background color 
             // Set current day block to be bold
             for (int i = 1; i <= 31; i++) {
                 bttns.get(curDay - 1).setFont(f);
                 bttns.get(curDay - 1).setBackground(Color.CYAN);
                 bttns.get(curDay - 1).setOpaque(true);
                 bttns.get(curDay - 1).setBorderPainted(true);
             }
         }
         
         // Find first day of week of month
         //<editor-fold defaultstate="collapsed" desc="Sets first day of the week of month">
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.BOTH;
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         gbc.insets = new Insets(space, space, space, space);       // Set spacing between buttons
 
         weekdayPanel.setLayout(new GridBagLayout());
         weekdayPanel.setBackground(Color.CYAN);
 
         cal.set(Calendar.MONTH, curMonth);
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
         //</editor-fold>
 
         for (int i = 0; i < 31; i++) {
             if (dayClicked - 1 == i) {
                 try {
                     dbConn = DBManager.getConnection();
                     stmt = dbConn.createStatement();
                     results = stmt.executeQuery("select count(*) from "
                             + "event where datetime ='" + curMonth
                             + bttns.get(i).getDay() + curYear + "'");
                     while (results.next()) {
                         count = results.getInt(1);
                     }
 
                     if (count == 0) {
                         results = stmt.executeQuery("select count(*) from "
                                 + "calendar where memo != ''");
 
                         while (results.next()) {
                             count = results.getInt(1);
                         }
                     }
 
                     if (count == 0) {
                         results = stmt.executeQuery("select count(*) from "
                                 + "alarm where datetime ='" + curMonth
                                 + bttns.get(i).getDay() + curYear + "'");
 
 
                         while (results.next()) {
                             count = results.getInt(1);
                         }
                     }
 
                     stmt.close();
                     results.close();
 
                 } catch (SQLException ex) {
                     System.out.println("paint");
                     ex.printStackTrace();
                 }
 
 
                 if (count != 0) {
                     bttns.get(i).setFont(f);
                     bttns.get(i).setForeground(Color.BLUE);
                 }
             }
         }
     }
 
     public void viewDate() throws SQLException {
         schedule = new JFrame("Schedule");
         // monthLabel, dateLabel, yearLabel, listOfEvents
 
         //<editor-fold defaultstate="collapsed" desc="Set current month label">
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
         //</editor-fold>
 
         dateLabel = new JLabel(dayClicked + ", ");
         yearLabel = new JLabel(curYear + " ");
         listOfEvents = new JTextArea();
         for (int i = 0; i < 31; i++) {
             if (dayClicked == i) {
                 try {
                     stmt = dbConn.createStatement();
                     results = stmt.executeQuery("select memo from "
                             + "calendar where datetime = '" + curMonth
                             + bttns.get(i - 1).getDay() + curYear + "'");
                     listOfEvents.append("Memos: ");
                     while (results.next()) {
                         listOfEvents.append(results.getString("memo") + '\n');
                     }
                     listOfEvents.append("Events: " + '\n');
                     results = stmt.executeQuery("                  " + "select name, starttime, endtime from "
                             + "event where datetime = '" + curMonth
                             + bttns.get(i - 1).getDay() + curYear + "'");
                     while (results.next()) {
                         listOfEvents.append("                  " + results.getString("name") + ' '
                                 + results.getString("starttime") + ' ' + results.getString("endtime") + '\n');
                     }
                     listOfEvents.append("Alarms: " + '\n');
                     results = stmt.executeQuery("select name, starttime from "
                             + "alarm where datetime = '" + curMonth
                             + bttns.get(i - 1).getDay() + curYear + "'");
                     while (results.next()) {
                         listOfEvents.append("                  " + results.getString("name") + ' '
                                 + results.getString("starttime") + '\n');
                     }
 
                     stmt.close();
                     results.close();
 
                 } catch (SQLException ex) {
                     System.out.println("View Events");
                     ex.printStackTrace();
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
 
     public static void main(String[] args) throws InstantiationException, ClassNotFoundException, IllegalAccessException, SQLException, IOException {
         DrawCalendar myCalendar = new DrawCalendar(); // window for drawing 
         JFrame application = new JFrame("Calendar"); // the program itself 
         application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // set frame to exit when it is closed 
         application.setSize(800, 800);      // Set default size of JFrame Calendar
         application.setLocation(230, 0);
         application.setResizable(true);     // Allow JFrame to be resized
         application.add(myCalendar);        // add calender GUI to JFrame
         application.setVisible(true);       // Set visibility of JFrame
     }
 }
