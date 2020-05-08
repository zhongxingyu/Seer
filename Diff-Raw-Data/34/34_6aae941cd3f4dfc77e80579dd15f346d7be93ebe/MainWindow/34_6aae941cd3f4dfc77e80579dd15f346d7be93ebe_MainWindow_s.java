 package schedutron;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.io.IOException;
 import java.text.DateFormatSymbols;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 public class MainWindow extends JFrame {
 
 	/**	GUID for the Window */
 	private static final long serialVersionUID = 2278553101678377064L;
 
   // XXX: Yes, I know this is ugly, all the window's data is inside the object.
  
 
   /** The schedule currently loaded into the program */
   private Schedule schedule = new Schedule();
   /** The starting of the schedule display's range */
   private Date start = new Date();
   /** The end of the schedule display's range */
   private Date end = new Date();
   private int numIntervals;
   private int rowheight;
 
   private JButton catalogButton;
   private JButton addButton;
   private JButton removeButton;
   
   private JList availableList;
   private JList scheduledList;
   
   ClassSelectorModel classSelector;
   
   private static final Color[] palette = {
     new Color(145,169,201),
     new Color(145,201,162),
     new Color(230,183,113)
   };
 
   /** Array for accessing the grid line labels in the grid */
   private JLabel[][] cells; 
 
   private JPanel panel;
   private JPanel selector_panel;
  private JPanel list_selected;
   // TODO: Add panel for catalog and schedule
   
   private void generateGrid()
   {
     GridBagConstraints c = new GridBagConstraints();
     c.gridy = 0;
     c.ipadx = 64;
     DateFormatSymbols dfs = new DateFormatSymbols();
     // TODO: base this on current Calendar date and allow incrementing of weeks
     String[] weekdays = dfs.getShortWeekdays();
     for (int i = 1; i <= 7; i++ ) {
       c.gridx = i;
       String weekday = weekdays[i];
       System.out.println(weekday);
       panel.add(new JLabel(weekday), c);
     }
     SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
     try {
       start = sdf.parse("12:00 AM");
     } catch (ParseException e) {
       e.printStackTrace();
     }
     Date time = start;
     c.gridx = 0;
     c.ipady = 8;
     c.ipadx = 24;
     numIntervals = 24; // TODO: Either generalize or make this standard
     Calendar calendar = Calendar.getInstance();
     calendar.setTime(time);
     for ( int i = 1; i <= numIntervals; i++ ) {
       c.gridy = i;
       panel.add(new JLabel(sdf.format(calendar.getTime())), c);
       calendar.add(Calendar.HOUR, 1);
     }
     drawCourses();
   }
 
   private void drawCourses() {
     if (schedule.getCourses() == null) {
       return;
     }
     Calendar calendar = Calendar.getInstance();
     GridBagConstraints c = new GridBagConstraints();
     c.fill = GridBagConstraints.BOTH;
     int numRows = 24;     // TODO: Put us somewhere nicer
     int numCols = 7;
     cells = new JLabel[numRows][numCols];
     for (int row = 1; row <= numRows; row++) {
       c.gridy = row;
       for (int col = 1; col <= numCols; col++) {
         c.gridx = col;
         JLabel cell = new JLabel();
         if (row % 2 == 0) {
           cell.setBackground(new Color(0xd9,0xdc,0xdd));
           cell.setOpaque(true);
         }
         cell.setBorder(BorderFactory.createMatteBorder(1,1,0,0,Color.BLACK));
         panel.add(cell, c);
         cells[row-1][col-1] = cell;
       }
     }
     for (Course course: schedule.getCourses()) {
       for (TimeBlock time : course.getTimes()) {
         Color color = palette[schedule.getCourses().indexOf(course) % palette.length];
         String dayCodes = "SMTWRFU";
         calendar.setTime(time.getStart());
         int startHr = calendar.get(Calendar.HOUR_OF_DAY);
         calendar.setTime(time.getEnd());
         // offset by -1 minutes so times on the hour don't fill in end hours
         // (ie. prevent an event that ends at 12 from looking like it ends at 1)
         calendar.add(Calendar.MINUTE, -1);
         // TODO: Better rounding method? Should a course that ends at 12:05 
         // render as ending at 12:00 or 1:00? Or should we change the division
         // to 30 minutes to avoid (at least for most purposes at U of L) this 
         // problem?
         int endHr = calendar.get(Calendar.HOUR_OF_DAY);
         // TODO: Use better name than c... names from online examples suck!
         c.gridx = dayCodes.indexOf(time.getDay()) + 1;
         c.gridy = startHr + 1;
         c.gridheight = (endHr - startHr) + 1;
         c.fill = GridBagConstraints.BOTH;
         for (int row = c.gridy; row < c.gridy + c.gridheight; row++) {
         	panel.remove(cells[row-1][c.gridx-1]);
         }
         JLabel label = new JLabel(course.getNumber(), JLabel.CENTER);
         label.setBackground(color); 
         label.setBorder(BorderFactory.createMatteBorder(1,1,0,0,Color.BLACK));
         label.setOpaque(true);
         panel.add(label, c);
       }
     }
   }
 
   private JLabel MapBlock(TimeBlock block) {
     // TODO: Refactor draw courses stuff to here
     String days = "SMTWRFU";
     return null;
   }
 
   /// tset
   private void generateList() {
     // clear availableList data
     // clear scheduledList data
   }
   
   
 
 
   public MainWindow() throws IOException {
     panel = new JPanel(new GridBagLayout());
     schedule = new Schedule();
     SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
     // TODO: Remove hard-coded courses
     Course course1 = null;
     Course course2 = null;
     Course course3 = null;
     Course course4 = null;
     Course course5 = null;
     ArrayList<Course> unselected_courses = null;
     try {
       course1 = new Course("CECS 550", "Software Engineering", "MW",
         sdf.parse("5:30 PM"), sdf.parse("6:45 PM"), 3);
       course2 = new Course("ENGR 330", "Linear Algebra", "MW",
         sdf.parse("12:00 PM"), sdf.parse("12:50 PM"), 2);
       course3 = new Course("CECS 535", "Intro to Databases", "MW",
         sdf.parse("1:00 PM"), sdf.parse("2:15 PM"), 3);
       course4 = new Course("ECE 412", "Intro to Embedded Systems", "TR",
         sdf.parse("9:30 AM"), sdf.parse("10:45 AM"), 3);
       course5 = new Course("SOC 202", "Social Problems", "T",
         sdf.parse("11:00 AM"), sdf.parse("12:15 PM"), 2);
       
       
 
       FileManager fileMan = new FileManager();
       unselected_courses = fileMan.makeClasses();
 
     } catch (ParseException e) {
       e.printStackTrace();
     }
 //    schedule.addCourse(course1);
 //    schedule.addCourse(course2);
 //    schedule.addCourse(course3);
 //    schedule.addCourse(course4);
 //    schedule.addCourse(course5);
 
     generateGrid();
     generateList();
     this.add(panel,BorderLayout.WEST);
 
     Course[] courses;
     Course[] temp = {course1, course2, course3, course4, course5};
     courses = temp;
     
 
 
     		//new ArrayList<Course>(Arrays.asList(courses));
     //set up list selector for courses
     selector_panel = new JPanel(new GridBagLayout());
     classSelector = new ClassSelectorModel(unselected_courses);
     classSelector.addListeners();
     classSelector.setPanel(selector_panel);
 
     
     this.add(selector_panel, BorderLayout.CENTER);
     pack();
   }
   // TODO: Handle button clicks, etc.
   
 	public static <Course> Course[] concatCourses(Course[] first, Course[] second) {
 		  Course[] result = Arrays.copyOf(first, first.length + second.length);
 		  System.arraycopy(second, 0, result, first.length, second.length);
 		  return result;
 	}
   
 
 
   
 }
