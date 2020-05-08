 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 public class AddEntry extends JPanel {
 	private static JTextField name, name1;
 	private static JTextArea desc, desc1;
 	private static JComboBox month, day, year, hour, minute, ampm, status, priority;
 	private static JComboBox month1, day1, year1, hour1, minute1, ampm1, repeating;
 	private static JFrame frame;
 	JRadioButtonMenuItem allDay, timeSelection;
 	JCheckBox repeatingLabel;
 
     public AddEntry() {
 		super(new GridLayout(1, 1));
 		name = new JTextField();
 		desc = new JTextArea();
 		name1 = new JTextField();
 		desc1 = new JTextArea();
 		
 		String[] months = { "January", "February", "March","April","May","June","July","August","September","October","November","December"};
 		month = new JComboBox(months);
 		month1 = new JComboBox(months);
 		
 		Integer[] days = new Integer[31];
 		for( int i=0; i<=30; i++ )
 			days[i] = i+1;
 		day = new JComboBox(days);
 		day1 = new JComboBox(days);
 		
 		Integer[] years = new Integer[100];
 		for( int i=0; i<100; i++ )
 			years[i] = 2012+i;
 		year = new JComboBox(years);
 		year1 = new JComboBox(years);
 		
 		Integer[] hours = new Integer[12];
 		for( int i=0; i<12; i++)
 			hours[i] = i+1;
 		hour = new JComboBox(hours);
 		hour1 = new JComboBox(hours);
 		
 		String[] minutes = new String[60];
 		for( int i=0; i<60; i++ )
 		{	if( i > 9 )
 			minutes[i] = Integer.toString(i);
 			else
 			minutes[i] = "0"+Integer.toString(i);
 		}
 		minute = new JComboBox(minutes);
 		minute1 = new JComboBox(minutes);
 		
 		String[] time = { "AM", "PM"};
 		ampm = new JComboBox(time);
 		ampm1 = new JComboBox(time);
 		
 		String[] status0 = { "To Do", "Doing", "Done" };
 		status = new JComboBox(status0);
 		
 		String[] priority0 = { "Very High", "High", "Normal", "Low", "Very Low" };
 		priority = new JComboBox(priority0);
 		
 		String[] repeating0 = { "Daily", "Weekly", "Monthly", "Yearly" };
 		repeating = new JComboBox(repeating0);
 		repeating.setEnabled(false);
         
         JTabbedPane tabbedPane = new JTabbedPane();
         ImageIcon icon = createImageIcon("tray.jpg");
         
         JComponent panel1 = makeTextPanel();
 		panel1.setLayout(new GridLayout(1,1));
         tabbedPane.addTab("Tasks", icon, panel1,
                 "Tasks");
 		JPanel tasksPanelMain = new JPanel(new BorderLayout());
 			tasksPanelMain.add(new JLabel(" "), BorderLayout.PAGE_START);
 			tasksPanelMain.add(new JLabel(" "), BorderLayout.PAGE_END);
 			tasksPanelMain.add(new JLabel("   "), BorderLayout.LINE_START);
 			tasksPanelMain.add(new JLabel("   "), BorderLayout.LINE_END);
 			JPanel taskPane = new JPanel(new BorderLayout());
 				JPanel taskHead = new JPanel(new GridLayout(1,1));
 					JPanel taskNameLabel = new JPanel(new BorderLayout());
 						taskNameLabel.add(new JLabel("Task Name:   \t"), BorderLayout.LINE_START);
 						taskNameLabel.add(name, BorderLayout.CENTER);
 					taskHead.add(taskNameLabel);
 				taskPane.add(taskHead, BorderLayout.PAGE_START);
 				JPanel taskBody = new JPanel(new GridLayout(2, 1));
 					JPanel taskDescMain = new JPanel(new BorderLayout());
 						taskDescMain.add(new JLabel(" "), BorderLayout.PAGE_START);
 						JPanel taskDescLabel = new JPanel(new FlowLayout());
 						taskDescMain.add(new JLabel("Description:   \t"), BorderLayout.LINE_START);
 						taskDescMain.add(desc, BorderLayout.CENTER);
 					taskBody.add(taskDescMain);
 					JPanel taskProp = new JPanel(new GridLayout(2,1));
 						JPanel taskDeadlineMain = new JPanel(new BorderLayout());
 							taskDeadlineMain.add(new JLabel("Deadline: \t"), BorderLayout.LINE_START);
 							JPanel taskDeadlineInfo = new JPanel(new GridLayout(2,1));
 								JPanel taskDate = new JPanel(new FlowLayout()); //DATE
 									taskDate.add(month);
 									taskDate.add(day);
 									taskDate.add(year);
 								taskDeadlineInfo.add(taskDate);
 								JPanel taskTime = new JPanel(new FlowLayout()); //TIME
 									taskTime.add(hour);
 									taskTime.add(minute);
 									taskTime.add(ampm);
 								taskDeadlineInfo.add(taskTime);
 							taskDeadlineMain.add(taskDeadlineInfo, BorderLayout.CENTER);
 						taskProp.add(taskDeadlineMain);
 						JPanel taskButtons = new JPanel(new GridLayout(3,1));
 							JPanel taskStatusMain = new JPanel( new BorderLayout());
 								taskStatusMain.add(new JLabel("Status:   \t"), BorderLayout.LINE_START);
 								taskStatusMain.add(status, BorderLayout.CENTER);
 							taskButtons.add(taskStatusMain);
 							JPanel taskPriorityMain = new JPanel( new BorderLayout());
 								taskPriorityMain.add(new JLabel("Priority:  \t"), BorderLayout.LINE_START);
 								taskPriorityMain.add(priority, BorderLayout.CENTER);
 							taskButtons.add(taskPriorityMain);
 							JButton addTaskListener = new JButton("Add Task");
 							addTaskListener.addActionListener(new addTask_Action());
 							taskButtons.add(addTaskListener);
 						taskProp.add(taskButtons);
 					taskBody.add(taskProp);
 				taskPane.add(taskBody);
 			tasksPanelMain.add(taskPane, BorderLayout.CENTER);
 		panel1.add(tasksPanelMain);
         
         JComponent panel2 = makeTextPanel();
 		panel2.setLayout(new GridLayout(1,1));
         tabbedPane.addTab("Events", icon, panel2,
                 "Events");
 		JPanel eventsPanelMain = new JPanel(new BorderLayout());
 			eventsPanelMain.add(new JLabel(" "), BorderLayout.PAGE_START);
 			eventsPanelMain.add(new JLabel(" "), BorderLayout.PAGE_END);
 			eventsPanelMain.add(new JLabel("   "), BorderLayout.LINE_START);
 			eventsPanelMain.add(new JLabel("   "), BorderLayout.LINE_END);
 			JPanel eventPane = new JPanel(new BorderLayout());
 				JPanel eventHead = new JPanel(new GridLayout(1,1));
 					JPanel eventNameLabel = new JPanel(new BorderLayout());
 						eventNameLabel.add(new JLabel("Event Name:  \t"), BorderLayout.LINE_START);
 						eventNameLabel.add(name1, BorderLayout.CENTER);
 					eventHead.add(eventNameLabel);
 				eventPane.add(eventHead, BorderLayout.PAGE_START);
 				JPanel eventBody = new JPanel(new GridLayout(2, 1));
 					JPanel eventDescMain = new JPanel(new BorderLayout());
 						eventDescMain.add(new JLabel(" "), BorderLayout.PAGE_START);
 						JPanel eventDescLabel = new JPanel(new FlowLayout());
 						eventDescMain.add(new JLabel("Description:   \t"), BorderLayout.LINE_START);
 						eventDescMain.add(desc1, BorderLayout.CENTER);
 					eventBody.add(eventDescMain);
 					JPanel eventProp = new JPanel(new GridLayout(5,1));
 						JPanel eventDeadlineMain = new JPanel(new BorderLayout());
 							eventDeadlineMain.add(new JLabel("Deadline: \t"), BorderLayout.LINE_START);
 							JPanel eventDate = new JPanel(new FlowLayout()); //DATE
 									eventDate.add(month1);
 									eventDate.add(day1);
 									eventDate.add(year1);
 							eventDeadlineMain.add(eventDate, BorderLayout.CENTER);
 						eventProp.add(eventDeadlineMain);
 							JPanel eventDeadlineInfo = new JPanel(new BorderLayout());	
 								timeSelection = new JRadioButtonMenuItem("Time",true);
 								eventDeadlineInfo.add(timeSelection, BorderLayout.LINE_START);
 								JPanel eventTime = new JPanel(new FlowLayout()); //TIME
 									eventTime.add(hour1);
 									eventTime.add(minute1);
 									eventTime.add(ampm1);
 								eventDeadlineInfo.add(eventTime, BorderLayout.CENTER);
 						allDay = new JRadioButtonMenuItem("All Day",false);
 						ButtonGroup bg = new ButtonGroup();
 							bg.add(timeSelection);
 							bg.add(allDay);
 							timeSelection.addItemListener( new time_Select() );
 							allDay.addItemListener( new allDay_Select() );
 							
 						eventProp.add(eventDeadlineInfo);
 						eventProp.add(allDay);
 							JPanel isRepeating = new JPanel(new BorderLayout());	
 								repeatingLabel = new JCheckBox("Repeat Event?");
 								repeatingLabel.addItemListener( new repeating_Select() );
 								isRepeating.add(repeatingLabel, BorderLayout.LINE_START);
 								isRepeating.add(repeating, BorderLayout.CENTER);
 						eventProp.add(isRepeating);
 						eventProp.add(new JButton("Add Event")); //BUTTON1
 					eventBody.add(eventProp);
 				eventPane.add(eventBody);
 			eventsPanelMain.add(eventPane, BorderLayout.CENTER);
 		panel2.add(eventsPanelMain);
         
         //Add the tabbed pane to this panel.
         add(tabbedPane);
         
         //The following line enables to use scrolling tabs.
         tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
     }
     
     protected JComponent makeTextPanel() {
         JPanel panel = new JPanel(false);
         panel.setLayout(new FlowLayout());
         return panel;
     }
     
     /** Returns an ImageIcon, or null if the path was invalid. */
     protected static ImageIcon createImageIcon(String path) {
         java.net.URL imgURL = AddEntry.class.getResource(path);
         if (imgURL != null) {
             return new ImageIcon(imgURL);
         } else {
             System.err.println("Couldn't find file: " + path);
             return null;
         }
     }
     
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from
      * the event dispatch thread.
      */
     public static void createAndShowGUI() {
         //Create and set up the window.
         JFrame frame = new JFrame("AddEntry");
         
         //Add content to the window.
         frame.add(new AddEntry(), BorderLayout.CENTER);
         
         //Display the window.
         frame.pack();
         frame.setVisible(true);
 		frame.setSize(325,450);
 		frame.setResizable(false);
     }
     
     public static void main(String[] args) {
         //Schedule a job for the event dispatch thread:
         //creating and showing this application's GUI.
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 //Turn off metal's use of bold fonts
 		UIManager.put("swing.boldMetal", Boolean.FALSE);
 		createAndShowGUI();
             }
         });
     }
 
     //nawawala yung addEntryListener pls fix
     class addTask_Action implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			String n = name.getText();
 			String dsc = desc.getText();
 			int mnth = (int)month.getSelectedIndex(); //January = 0; December = 11;
 			int dy = Integer.parseInt(dayconv)+1;  //returns exact day
 			int yr = Integer.parseInt(year.getSelectedItem()+""); //returns exact year
 			int stat = (int)status.getSelectedIndex(); // to-do = 0; doing = 1; done = 2;
 			String hrconv = hour.toString();
 			int hr = Integer.parseInt(hrconv)+1; //returns exact hour
 			int mins = (int)minute.getSelectedIndex(); //returns exact minutes
 			//useless ata
 			int ap = (int)ampm.getSelectedIndex(); // am = 0; pm = 1;
 
 			//PRIORITY still not in action haha
 
 			DatabaseRW.addTask(n, dsc, yr, mnth, dy, hr, mins, stat, 1);
 
 			frame.setVisible(false);
 			frame.dispose();
 			System.out.println(n);
 			System.out.println(dsc);
 			System.out.println(mnth);
 			System.out.println(dy);
 			System.out.println(yr);
 			System.out.println(stat);
 			System.out.println(hr);
 			System.out.println(mins);
 			System.out.println(ap);
 			Calendar.addEntryOpen++;
 		}
     }
 	
 	class time_Select implements ItemListener
 	{
 		public void itemStateChanged( ItemEvent e)
 		{
 
 			if( timeSelection.isSelected() )
 			{
 				
 				hour1.setEnabled(true);
 				minute1.setEnabled(true);
 				ampm1.setEnabled(true);
 			}
 		}
 	}
 	
 	class allDay_Select implements ItemListener
 	{
 		public void itemStateChanged( ItemEvent e)
 		{
 			if( allDay.isSelected() )
 			{	
 				hour1.setEnabled(false);
 				minute1.setEnabled(false);
 				ampm1.setEnabled(false);
 			}
 		}
 	}
 	
 	class repeating_Select implements ItemListener
 	{
 		public void itemStateChanged(ItemEvent e)
 		{
 			if( repeatingLabel.isSelected() )
 			{
 				repeating.setEnabled(true);
 			}
 			else
 			{
 				repeating.setEnabled(false);
 			}
 		}
 	}
 }
