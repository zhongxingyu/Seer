 package gui;
 
 import generator.CurrentDate;
 import generator.Period;
 import generator.ScheduleDataStorage;
 import generator.ScheduleGeneratorDriver;
 import generator.SchoolType;
 
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 public class PersonalScheduleGenerator {
 
 	private JFrame frame;
 	private final ButtonGroup schoolType = new ButtonGroup();
 	private JTextField txtPeriod1;
 	private JTextField txtPeriod2;
 	private JTextField txtPeriod3;
 	private JTextField txtPeriod4;
 	private JTextField txtPeriod5;
 	private JTextField txtPeriod6;
 	private JTextField txtPeriod7;
 
 	private JTextField name1;
 	private JTextField day1;
 	private JTextField period1;
 	private JTextField name2;
 	private JTextField day2;
 	private JTextField period2;
 	private JTextField name3;
 	private JTextField day3;
 	private JTextField period3;
 	private JTextField name4;
 	private JTextField day4;
 	private JTextField period4;
 	private JTextField name5;
 	private JTextField day5;
 	private JTextField period5;
 
 	private String[] sixthOptions = new String[] { "1", "2", "3", "4" };
 	private String[] sixthSimpleOptions = new String[] { "1 & 2", "3 & 4" };
 	private String[] months = new String[12];
 	private String[] days = new String[31];
 	private String[] years = { "2013", "2014" };
 	private JTextField txtMycaltxt;
 
 	private JComboBox<Object> startMonthsList;
 	private JComboBox<Object> startDaysList;
 	private JComboBox<Object> startYearsList;
 
 	private JComboBox<Object> endMonthsList;
 	private JComboBox<Object> endDaysList;
 	private JComboBox<Object> endYearsList;
 
 	private JRadioButton rdbtnUpperSchool;
 	private JRadioButton rdbtnMiddleSchool;
 	private JCheckBox chckbxPeriod;
 	private JCheckBox chckbxPeriod_6;
 	private JCheckBox chckbxPeriod_5;
 	private JCheckBox chckbxPeriod_4;
 	private JCheckBox chckbxPeriod_3;
 	private JCheckBox chckbxPeriod_2;
 	private JCheckBox chckbxPeriod_1;
 
 	private JComboBox<Object> sixthFieldLangSS;
 	private JComboBox<Object> sixthFieldMath;
 	private JComboBox<Object> sixthFieldScience;
 	private JComboBox<Object> sixthFieldRotation;
 
 	private JCheckBox chckbxIncludeBreaksLunch;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					PersonalScheduleGenerator window = new PersonalScheduleGenerator();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public PersonalScheduleGenerator() {
 
 		for (int i = 1; i <= 12; i++) {
 			months[i - 1] = "" + i;
 		}
 		for (int i = 1; i <= 31; i++) {
 			days[i - 1] = "" + i;
 		}
 
 		frame = new JFrame();
 		frame.setTitle("Kent Denver Personal Schedule Generator 2013-2014 (BETA)");
 		frame.setResizable(false);
 		frame.setBounds(50, 50, 500, 710);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 		initializePeriodMode();
 
 		JMenuBar menuBar = new JMenuBar();
 		frame.setJMenuBar(menuBar);
 
 		JMenu mode = new JMenu("Mode");
 
 		JMenuItem periodMode = new JMenuItem("Period Mode");
 		periodMode.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.getContentPane().removeAll();
 				initializePeriodMode();
 				frame.getContentPane().validate();
 				frame.getContentPane().repaint();
 			}
 		});
 		JMenuItem singleBoxMode = new JMenuItem("\"Single Box\" Mode");
 		singleBoxMode.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.getContentPane().removeAll();
 				initializeSingleMode();
 				frame.getContentPane().validate();
 				frame.getContentPane().repaint();
 			}
 
 		});
 		JMenuItem midElectiveMode = new JMenuItem("Middle School Elective Mode");
 		midElectiveMode.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.getContentPane().removeAll();
 				initializeMidElectiveMode();
 				frame.getContentPane().validate();
 				frame.getContentPane().repaint();
 			}
 
 		});
 
 		JMenuItem sixthGradePeriodMode = new JMenuItem("Sixth Grade Mode");
 		sixthGradePeriodMode.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.getContentPane().removeAll();
 				initializeSixthPeriodMode();
 				frame.getContentPane().validate();
 				frame.getContentPane().repaint();
 			}
 
 		});
 
 		JMenuItem sixthGradePeriodSelectionMode = new JMenuItem("Sixth Grade Period Selection Mode");
 		sixthGradePeriodSelectionMode.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.getContentPane().removeAll();
 				initializeSixthPeriodSelectionMode();
 				frame.getContentPane().validate();
 				frame.getContentPane().repaint();
 			}
 
 		});
 
 		mode.add(periodMode);
 		mode.add(singleBoxMode);
 		mode.add(midElectiveMode);
 		mode.add(sixthGradePeriodMode);
 		mode.add(sixthGradePeriodSelectionMode);
 
 		menuBar.add(mode);
 	}
 
 	protected void initializePeriodMode() {
 
 		JLabel lblNewLabel = new JLabel(
 				"<html>Welcome to the Kent Denver Personal Schedule Generator! </br>Fill out the fields below or use the Mode menu to see different options.  </br> Once you are content with your settings, press submit and upload the generated file (created in the same directory as this program) to your calendar.</html>");
 		lblNewLabel.setBounds(26, 15, 445, 90);
 		frame.getContentPane().add(lblNewLabel);
 
 		JLabel lblNewLabel_1 = new JLabel(
 				"<html>Select whether you would like your schedule to use the middle or upper school schedule.");
 		lblNewLabel_1.setBounds(41, 85, 217, 102);
 		frame.getContentPane().add(lblNewLabel_1);
 
 		rdbtnUpperSchool = new JRadioButton("Upper School");
 		schoolType.add(rdbtnUpperSchool);
 		rdbtnUpperSchool.setSelected(true);
 		rdbtnUpperSchool.setBounds(284, 108, 141, 23);
 		frame.getContentPane().add(rdbtnUpperSchool);
 
 		rdbtnMiddleSchool = new JRadioButton("7th and 8th Grade");
 		schoolType.add(rdbtnMiddleSchool);
 		rdbtnMiddleSchool.setBounds(284, 141, 141, 23);
 		frame.getContentPane().add(rdbtnMiddleSchool);
 
 		JLabel lblNewLabel_2 = new JLabel(
 				"<html> You can change the name that each period will have on your calendar.  Make sure that the fields below reflect what you would like to see on your calendar.  You can uncheck the box next to any period that you do not wish to add to your calendar.");
 		lblNewLabel_2.setBounds(31, 176, 438, 67);
 		frame.getContentPane().add(lblNewLabel_2);
 
 		txtPeriod1 = new JTextField();
 		txtPeriod1.setText("Period 1");
 		txtPeriod1.setBounds(291, 255, 134, 28);
 		frame.getContentPane().add(txtPeriod1);
 		txtPeriod1.setColumns(10);
 
 		txtPeriod2 = new JTextField();
 		txtPeriod2.setText("Period 2");
 		txtPeriod2.setColumns(10);
 		txtPeriod2.setBounds(291, 290, 134, 28);
 		frame.getContentPane().add(txtPeriod2);
 
 		txtPeriod3 = new JTextField();
 		txtPeriod3.setText("Period 3");
 		txtPeriod3.setColumns(10);
 		txtPeriod3.setBounds(291, 325, 134, 28);
 		frame.getContentPane().add(txtPeriod3);
 
 		txtPeriod4 = new JTextField();
 		txtPeriod4.setText("Period 4");
 		txtPeriod4.setColumns(10);
 		txtPeriod4.setBounds(291, 360, 134, 28);
 		frame.getContentPane().add(txtPeriod4);
 
 		txtPeriod5 = new JTextField();
 		txtPeriod5.setText("Period 5");
 		txtPeriod5.setColumns(10);
 		txtPeriod5.setBounds(291, 395, 134, 28);
 		frame.getContentPane().add(txtPeriod5);
 
 		txtPeriod6 = new JTextField();
 		txtPeriod6.setText("Period 6");
 		txtPeriod6.setColumns(10);
 		txtPeriod6.setBounds(291, 430, 134, 28);
 		frame.getContentPane().add(txtPeriod6);
 
 		txtPeriod7 = new JTextField();
 		txtPeriod7.setText("Period 7");
 		txtPeriod7.setColumns(10);
 		txtPeriod7.setBounds(291, 465, 134, 28);
 		frame.getContentPane().add(txtPeriod7);
 
 		chckbxPeriod = new JCheckBox("Period 1:");
 		chckbxPeriod.setSelected(true);
 		chckbxPeriod.setBounds(85, 257, 128, 23);
 		frame.getContentPane().add(chckbxPeriod);
 
 		chckbxPeriod_6 = new JCheckBox("Period 2:");
 		chckbxPeriod_6.setSelected(true);
 		chckbxPeriod_6.setBounds(85, 292, 128, 23);
 		frame.getContentPane().add(chckbxPeriod_6);
 
 		chckbxPeriod_5 = new JCheckBox("Period 3:");
 		chckbxPeriod_5.setSelected(true);
 		chckbxPeriod_5.setBounds(85, 327, 128, 23);
 		frame.getContentPane().add(chckbxPeriod_5);
 
 		chckbxPeriod_4 = new JCheckBox("Period 4:");
 		chckbxPeriod_4.setSelected(true);
 		chckbxPeriod_4.setBounds(85, 362, 128, 23);
 		frame.getContentPane().add(chckbxPeriod_4);
 
 		chckbxPeriod_3 = new JCheckBox("Period 5:");
 		chckbxPeriod_3.setSelected(true);
 		chckbxPeriod_3.setBounds(85, 397, 128, 23);
 		frame.getContentPane().add(chckbxPeriod_3);
 
 		chckbxPeriod_2 = new JCheckBox("Period 6:");
 		chckbxPeriod_2.setSelected(true);
 		chckbxPeriod_2.setBounds(85, 432, 128, 23);
 		frame.getContentPane().add(chckbxPeriod_2);
 
 		chckbxPeriod_1 = new JCheckBox("Period 7:");
 		chckbxPeriod_1.setSelected(true);
 		chckbxPeriod_1.setBounds(85, 467, 128, 23);
 		frame.getContentPane().add(chckbxPeriod_1);
 
 		JLabel lblStartDate = new JLabel("Start Date:");
 		lblStartDate.setBounds(73, 504, 99, 16);
 		frame.getContentPane().add(lblStartDate);
 
 		startMonthsList = new JComboBox<Object>(months);
 		startMonthsList.setSelectedIndex(7);
 		startMonthsList.setBounds(175, 500, 70, 27);
 		frame.getContentPane().add(startMonthsList);
 
 		startDaysList = new JComboBox<Object>(days);
 		startDaysList.setSelectedIndex(20);
 		startDaysList.setBounds(248, 500, 75, 27);
 		frame.getContentPane().add(startDaysList);
 
 		startYearsList = new JComboBox<Object>(years);
 		startYearsList.setSelectedIndex(0);
 		startYearsList.setBounds(326, 500, 99, 27);
 		frame.getContentPane().add(startYearsList);
 
 		JLabel lblEndDate = new JLabel("End Date:");
 		lblEndDate.setBounds(73, 539, 99, 16);
 		frame.getContentPane().add(lblEndDate);
 
 		endMonthsList = new JComboBox<Object>(months);
 		endMonthsList.setSelectedIndex(4);
 		endMonthsList.setBounds(175, 535, 70, 27);
 		frame.getContentPane().add(endMonthsList);
 
 		endDaysList = new JComboBox<Object>(days);
 		endDaysList.setSelectedIndex(22);
 		endDaysList.setBounds(248, 535, 75, 27);
 		frame.getContentPane().add(endDaysList);
 
 		endYearsList = new JComboBox<Object>(years);
 		endYearsList.setSelectedIndex(1);
 		endYearsList.setBounds(326, 535, 99, 27);
 		frame.getContentPane().add(endYearsList);
 
 		JButton btnSubmit = new JButton("Submit");
 		btnSubmit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ScheduleDataStorage data = new ScheduleDataStorage();
 				data.setStartDate(new CurrentDate(Integer.parseInt(months[startMonthsList.getSelectedIndex()]), Integer
 						.parseInt(days[startDaysList.getSelectedIndex()]), Integer.parseInt(years[startYearsList
 						.getSelectedIndex()])));
 				data.setEndDate(new CurrentDate(Integer.parseInt(months[endMonthsList.getSelectedIndex()]), Integer
 						.parseInt(days[endDaysList.getSelectedIndex()]), Integer.parseInt(years[endYearsList
 						.getSelectedIndex()])));
 				data.setIncludeBreaksAndLunch(chckbxIncludeBreaksLunch.isSelected());
 				if (chckbxPeriod.isSelected()) {
 					data.getPeriodsToInclude()[0] = 1;
 					data.getPeriodNames()[0] = txtPeriod1.getText();
 				}
 				if (chckbxPeriod_6.isSelected()) {
 					data.getPeriodsToInclude()[1] = 2;
 					data.getPeriodNames()[1] = txtPeriod2.getText();
 				}
 				if (chckbxPeriod_5.isSelected()) {
 					data.getPeriodsToInclude()[2] = 3;
 					data.getPeriodNames()[2] = txtPeriod3.getText();
 				}
 				if (chckbxPeriod_4.isSelected()) {
 					data.getPeriodsToInclude()[3] = 4;
 					data.getPeriodNames()[3] = txtPeriod4.getText();
 				}
 				if (chckbxPeriod_3.isSelected()) {
 					data.getPeriodsToInclude()[4] = 5;
 					data.getPeriodNames()[4] = txtPeriod5.getText();
 				}
 				if (chckbxPeriod_2.isSelected()) {
 					data.getPeriodsToInclude()[5] = 6;
 					data.getPeriodNames()[5] = txtPeriod6.getText();
 				}
 				if (chckbxPeriod_1.isSelected()) {
 					data.getPeriodsToInclude()[6] = 7;
 					data.getPeriodNames()[6] = txtPeriod7.getText();
 				}
 				data.setSchool(rdbtnUpperSchool.isSelected() ? SchoolType.HIGH : SchoolType.SEVENEIGHT);
 				data.setFilename(txtMycaltxt.getText());
 				@SuppressWarnings("unused")
 				ScheduleGeneratorDriver maker = new ScheduleGeneratorDriver(data);
 				JOptionPane.showMessageDialog(frame, txtMycaltxt.getText() + " written sucessfully.");
 
 			}
 		});
 		btnSubmit.setBounds(96, 630, 117, 29);
 		frame.getContentPane().add(btnSubmit);
 
 		JButton btnQuit = new JButton("Quit");
 		btnQuit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		btnQuit.setBounds(276, 630, 117, 29);
 		frame.getContentPane().add(btnQuit);
 
 		JLabel lblOutputFilename = new JLabel("Output filename:");
 		lblOutputFilename.setBounds(113, 601, 141, 16);
 		frame.getContentPane().add(lblOutputFilename);
 
 		txtMycaltxt = new JTextField();
 		txtMycaltxt.setText("MyCal.txt");
 		txtMycaltxt.setBounds(243, 595, 134, 28);
 		frame.getContentPane().add(txtMycaltxt);
 		txtMycaltxt.setColumns(10);
 
 		chckbxIncludeBreaksLunch = new JCheckBox("Include breaks, lunch and student life");
 		chckbxIncludeBreaksLunch.setBounds(113, 567, 352, 23);
 		frame.getContentPane().add(chckbxIncludeBreaksLunch);
 
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initializeSingleMode() {
 
 		JLabel lblNewLabel = new JLabel(
				"<html>Welcome to the Kent Denver Personal Schedule Generator! This mode allows you to generate events for meetings that occur once in a cycle (e.g. Period 1 on G Days).  There are five slots for these sort of meetings in the form below, if you do not need all five leave the remainder blank. If you need more, just run the program a second time.    ");
 		lblNewLabel.setBounds(27, 32, 438, 102);
 		frame.getContentPane().add(lblNewLabel);
 
 		JLabel lblNewLabel_1 = new JLabel(
 				"<html>Select whether you would like your schedule to use the middle or upper school schedule.");
 		lblNewLabel_1.setBounds(37, 130, 217, 102);
 		frame.getContentPane().add(lblNewLabel_1);
 
 		rdbtnUpperSchool = new JRadioButton("Upper School");
 		schoolType.add(rdbtnUpperSchool);
 		rdbtnUpperSchool.setSelected(true);
 		rdbtnUpperSchool.setBounds(283, 146, 141, 23);
 		frame.getContentPane().add(rdbtnUpperSchool);
 
 		rdbtnMiddleSchool = new JRadioButton("7th and 8th Grade");
 		schoolType.add(rdbtnMiddleSchool);
 		rdbtnMiddleSchool.setBounds(283, 186, 141, 23);
 		frame.getContentPane().add(rdbtnMiddleSchool);
 
 		JLabel lblDayType = new JLabel("Day Type");
 		lblDayType.setBounds(263, 228, 61, 16);
 		frame.getContentPane().add(lblDayType);
 
 		JLabel lblPeriod = new JLabel("Period");
 		lblPeriod.setBounds(381, 228, 61, 16);
 		frame.getContentPane().add(lblPeriod);
 
 		JLabel lblNameOfThis = new JLabel("Name of this Event");
 		lblNameOfThis.setBounds(57, 228, 149, 16);
 		frame.getContentPane().add(lblNameOfThis);
 
 		name1 = new JTextField();
 		name1.setBounds(21, 253, 204, 28);
 		frame.getContentPane().add(name1);
 		name1.setColumns(10);
 
 		day1 = new JTextField();
 		day1.setBounds(237, 253, 108, 28);
 		frame.getContentPane().add(day1);
 		day1.setColumns(10);
 
 		period1 = new JTextField();
 		period1.setBounds(357, 253, 97, 28);
 		frame.getContentPane().add(period1);
 		period1.setColumns(10);
 
 		name2 = new JTextField();
 		name2.setColumns(10);
 		name2.setBounds(21, 301, 204, 28);
 		frame.getContentPane().add(name2);
 
 		day2 = new JTextField();
 		day2.setColumns(10);
 		day2.setBounds(237, 301, 108, 28);
 		frame.getContentPane().add(day2);
 
 		period2 = new JTextField();
 		period2.setColumns(10);
 		period2.setBounds(357, 301, 97, 28);
 		frame.getContentPane().add(period2);
 
 		name3 = new JTextField();
 		name3.setColumns(10);
 		name3.setBounds(21, 352, 204, 28);
 		frame.getContentPane().add(name3);
 
 		day3 = new JTextField();
 		day3.setColumns(10);
 		day3.setBounds(237, 352, 108, 28);
 		frame.getContentPane().add(day3);
 
 		period3 = new JTextField();
 		period3.setColumns(10);
 		period3.setBounds(357, 352, 97, 28);
 		frame.getContentPane().add(period3);
 
 		name4 = new JTextField();
 		name4.setColumns(10);
 		name4.setBounds(21, 402, 204, 28);
 		frame.getContentPane().add(name4);
 
 		day4 = new JTextField();
 		day4.setColumns(10);
 		day4.setBounds(237, 402, 108, 28);
 		frame.getContentPane().add(day4);
 
 		period4 = new JTextField();
 		period4.setColumns(10);
 		period4.setBounds(357, 402, 97, 28);
 		frame.getContentPane().add(period4);
 
 		name5 = new JTextField();
 		name5.setColumns(10);
 		name5.setBounds(21, 451, 204, 28);
 		frame.getContentPane().add(name5);
 
 		day5 = new JTextField();
 		day5.setColumns(10);
 		day5.setBounds(237, 451, 108, 28);
 		frame.getContentPane().add(day5);
 
 		period5 = new JTextField();
 		period5.setColumns(10);
 		period5.setBounds(357, 451, 97, 28);
 		frame.getContentPane().add(period5);
 
 		JLabel lblStartDate = new JLabel("Start Date:");
 		lblStartDate.setBounds(57, 495, 99, 16);
 		frame.getContentPane().add(lblStartDate);
 
 		startMonthsList = new JComboBox<Object>(months);
 		startMonthsList.setSelectedIndex(7);
 		startMonthsList.setBounds(159, 491, 70, 27);
 		frame.getContentPane().add(startMonthsList);
 
 		startDaysList = new JComboBox<Object>(days);
 		startDaysList.setSelectedIndex(20);
 		startDaysList.setBounds(232, 491, 75, 27);
 		frame.getContentPane().add(startDaysList);
 
 		startYearsList = new JComboBox<Object>(years);
 		startYearsList.setSelectedIndex(0);
 		startYearsList.setBounds(310, 491, 99, 27);
 		frame.getContentPane().add(startYearsList);
 
 		JLabel lblEndDate = new JLabel("End Date:");
 		lblEndDate.setBounds(57, 530, 99, 16);
 		frame.getContentPane().add(lblEndDate);
 
 		endMonthsList = new JComboBox<Object>(months);
 		endMonthsList.setSelectedIndex(4);
 		endMonthsList.setBounds(159, 526, 70, 27);
 		frame.getContentPane().add(endMonthsList);
 
 		endDaysList = new JComboBox<Object>(days);
 		endDaysList.setSelectedIndex(22);
 		endDaysList.setBounds(232, 526, 75, 27);
 		frame.getContentPane().add(endDaysList);
 
 		endYearsList = new JComboBox<Object>(years);
 		endYearsList.setSelectedIndex(1);
 		endYearsList.setBounds(310, 526, 99, 27);
 		frame.getContentPane().add(endYearsList);
 
 		JLabel lblOutputFilename = new JLabel("Output filename:");
 		lblOutputFilename.setBounds(112, 581, 141, 16);
 		frame.getContentPane().add(lblOutputFilename);
 
 		txtMycaltxt = new JTextField();
 		txtMycaltxt.setText("MyCal.txt");
 		txtMycaltxt.setBounds(242, 575, 134, 28);
 		frame.getContentPane().add(txtMycaltxt);
 		txtMycaltxt.setColumns(10);
 
 		JButton btnSubmit = new JButton("Submit");
 		btnSubmit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ScheduleDataStorage data = new ScheduleDataStorage();
 				data.setStartDate(new CurrentDate(Integer.parseInt(months[startMonthsList.getSelectedIndex()]), Integer
 						.parseInt(days[startDaysList.getSelectedIndex()]), Integer.parseInt(years[startYearsList
 						.getSelectedIndex()])));
 				data.setEndDate(new CurrentDate(Integer.parseInt(months[endMonthsList.getSelectedIndex()]), Integer
 						.parseInt(days[endDaysList.getSelectedIndex()]), Integer.parseInt(years[endYearsList
 						.getSelectedIndex()])));
 				data.setIncludeBreaksAndLunch(false);
 				String period = period1.getText();
 				String day = day1.getText();
 				String name = name1.getText();
 				if (!name.equals("") && !day.equals("") && !period.equals("")) {
 					int temp = Integer.parseInt(period);
 					System.out.println(period);
 					data.getPeriodsToInclude()[temp - 1] = temp;
 					data.getSingleBox().put(day.toUpperCase().charAt(0), temp);
 					data.getPeriodNames()[temp - 1] = name;
 				}
 				period = period2.getText();
 				day = day2.getText();
 				name = name2.getText();
 				if (!name.equals("") && !day.equals("") && !period.equals("")) {
 					int temp = Integer.parseInt(period);
 					data.getPeriodsToInclude()[temp - 1] = temp;
 					data.getSingleBox().put(day.toUpperCase().charAt(0), temp);
 					data.getPeriodNames()[temp - 1] = name;
 				}
 				period = period3.getText();
 				day = day3.getText();
 				name = name3.getText();
 				if (!name.equals("") && !day.equals("") && !period.equals("")) {
 					int temp = Integer.parseInt(period);
 					data.getPeriodsToInclude()[temp - 1] = temp;
 					data.getSingleBox().put(day.toUpperCase().charAt(0), temp);
 					data.getPeriodNames()[temp - 1] = name;
 				}
 				period = period4.getText();
 				day = day4.getText();
 				name = name4.getText();
 				if (!name.equals("") && !day.equals("") && !period.equals("")) {
 					int temp = Integer.parseInt(period);
 					data.getPeriodsToInclude()[temp - 1] = temp;
 					data.getSingleBox().put(day.toUpperCase().charAt(0), temp);
 					data.getPeriodNames()[temp - 1] = name;
 				}
 				period = period5.getText();
 				day = day5.getText();
 				name = name5.getText();
 				if (!name.equals("") && !day.equals("") && !period.equals("")) {
 					int temp = Integer.parseInt(period);
 					data.getPeriodsToInclude()[temp - 1] = temp;
 					data.getSingleBox().put(day.toUpperCase().charAt(0), temp);
 					data.getPeriodNames()[temp - 1] = name;
 				}
 				data.setSchool(rdbtnUpperSchool.isSelected() ? SchoolType.HIGH : SchoolType.SEVENEIGHT);
 				data.setFilename(txtMycaltxt.getText());
 				@SuppressWarnings("unused")
 				ScheduleGeneratorDriver maker = new ScheduleGeneratorDriver(data);
 				JOptionPane.showMessageDialog(frame, txtMycaltxt.getText() + " written sucessfully.");
 			}
 
 		});
 		btnSubmit.setBounds(96, 630, 117, 29);
 		frame.getContentPane().add(btnSubmit);
 
 		JButton btnQuit = new JButton("Quit");
 		btnQuit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		btnQuit.setBounds(276, 630, 117, 29);
 		frame.getContentPane().add(btnQuit);
 
 	}
 
 	private void initializeMidElectiveMode() {
 
 		JLabel lblNewLabel = new JLabel(
 				"<html>Welcome to the Kent Denver Personal Schedule Generator! This mode allows you to generate a file that just holds the meeting times of the middle school elective.");
 		lblNewLabel.setBounds(27, 32, 438, 102);
 		frame.getContentPane().add(lblNewLabel);
 
 		JLabel lblStartDate = new JLabel("Start Date:");
 		lblStartDate.setBounds(60, 225, 99, 16);
 		frame.getContentPane().add(lblStartDate);
 
 		startMonthsList = new JComboBox<Object>(months);
 		startMonthsList.setSelectedIndex(7);
 		startMonthsList.setBounds(162, 221, 70, 27);
 		frame.getContentPane().add(startMonthsList);
 
 		startDaysList = new JComboBox<Object>(days);
 		startDaysList.setSelectedIndex(20);
 		startDaysList.setBounds(235, 221, 75, 27);
 		frame.getContentPane().add(startDaysList);
 
 		startYearsList = new JComboBox<Object>(years);
 		startYearsList.setSelectedIndex(0);
 		startYearsList.setBounds(313, 221, 99, 27);
 		frame.getContentPane().add(startYearsList);
 
 		JLabel lblEndDate = new JLabel("End Date:");
 		lblEndDate.setBounds(60, 338, 99, 16);
 		frame.getContentPane().add(lblEndDate);
 
 		endMonthsList = new JComboBox<Object>(months);
 		endMonthsList.setSelectedIndex(4);
 		endMonthsList.setBounds(162, 334, 70, 27);
 		frame.getContentPane().add(endMonthsList);
 
 		endDaysList = new JComboBox<Object>(days);
 		endDaysList.setSelectedIndex(22);
 		endDaysList.setBounds(235, 334, 75, 27);
 		frame.getContentPane().add(endDaysList);
 
 		endYearsList = new JComboBox<Object>(years);
 		endYearsList.setSelectedIndex(1);
 		endYearsList.setBounds(313, 334, 99, 27);
 		frame.getContentPane().add(endYearsList);
 
 		JButton btnSubmit = new JButton("Submit");
 		btnSubmit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ScheduleDataStorage data = new ScheduleDataStorage();
 				data.setStartDate(new CurrentDate(Integer.parseInt(months[startMonthsList.getSelectedIndex()]), Integer
 						.parseInt(days[startDaysList.getSelectedIndex()]), Integer.parseInt(years[startYearsList
 						.getSelectedIndex()])));
 				data.setEndDate(new CurrentDate(Integer.parseInt(months[endMonthsList.getSelectedIndex()]), Integer
 						.parseInt(days[endDaysList.getSelectedIndex()]), Integer.parseInt(years[endYearsList
 						.getSelectedIndex()])));
 				data.setFilename(txtMycaltxt.getText());
 				data.setMidSchoolElective(true);
 				data.setSchool(SchoolType.SEVENEIGHT);
 				@SuppressWarnings("unused")
 				ScheduleGeneratorDriver maker = new ScheduleGeneratorDriver(data);
 				JOptionPane.showMessageDialog(frame, txtMycaltxt.getText() + " written sucessfully.");
 			}
 
 		});
 		btnSubmit.setBounds(100, 542, 117, 29);
 		frame.getContentPane().add(btnSubmit);
 
 		JButton btnQuit = new JButton("Quit");
 		btnQuit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		btnQuit.setBounds(280, 542, 117, 29);
 		frame.getContentPane().add(btnQuit);
 
 		JLabel lblOutputFilename = new JLabel("Output filename:");
 		lblOutputFilename.setBounds(117, 443, 141, 16);
 		frame.getContentPane().add(lblOutputFilename);
 
 		txtMycaltxt = new JTextField();
 		txtMycaltxt.setText("MyCal.txt");
 		txtMycaltxt.setBounds(247, 437, 134, 28);
 		frame.getContentPane().add(txtMycaltxt);
 		txtMycaltxt.setColumns(10);
 
 	}
 
 	protected void initializeSixthPeriodMode() {
 
 		JLabel lblNewLabel = new JLabel(
 				"<html>Welcome to the Kent Denver Personal Schedule Generator! </br>This mode generates the sixth grade schedule.  Fill out the fields below then press submit.  </br> Once you are content with your settings, press submit and upload the generated file (created in the same directory as this program) to your calendar.</html>");
 		lblNewLabel.setBounds(25, 29, 445, 118);
 		frame.getContentPane().add(lblNewLabel);
 
 		JLabel lblLanguageArts = new JLabel("Language Arts / Social Studies group number: ");
 		lblLanguageArts.setBounds(53, 200, 302, 16);
 		frame.getContentPane().add(lblLanguageArts);
 
 		JLabel lblMathGroupNumber = new JLabel("Math group number:");
 		lblMathGroupNumber.setBounds(53, 260, 302, 16);
 		frame.getContentPane().add(lblMathGroupNumber);
 
 		JLabel lblScienceGroupNumber = new JLabel("Science group number:");
 		lblScienceGroupNumber.setBounds(53, 320, 302, 16);
 		frame.getContentPane().add(lblScienceGroupNumber);
 
 		JLabel lblRotationGroupNumber = new JLabel("Rotation group number:");
 		lblRotationGroupNumber.setBounds(53, 380, 302, 16);
 		frame.getContentPane().add(lblRotationGroupNumber);
 
 		sixthFieldLangSS = new JComboBox<Object>(sixthOptions);
 		sixthFieldLangSS.setBounds(356, 200, 64, 27);
 		sixthFieldLangSS.setSelectedIndex(0);
 		frame.getContentPane().add(sixthFieldLangSS);
 
 		sixthFieldMath = new JComboBox<Object>(sixthOptions);
 		sixthFieldMath.setBounds(356, 260, 64, 27);
 		sixthFieldMath.setSelectedIndex(0);
 		frame.getContentPane().add(sixthFieldMath);
 
 		sixthFieldScience = new JComboBox<Object>(sixthOptions);
 		sixthFieldScience.setBounds(356, 320, 64, 27);
 		sixthFieldScience.setSelectedIndex(0);
 		frame.getContentPane().add(sixthFieldScience);
 
 		sixthFieldRotation = new JComboBox<Object>(sixthOptions);
 		sixthFieldRotation.setBounds(356, 380, 64, 27);
 		sixthFieldRotation.setSelectedIndex(0);
 		frame.getContentPane().add(sixthFieldRotation);
 
 		JLabel lblStartDate = new JLabel("Start Date:");
 		lblStartDate.setBounds(73, 472, 99, 16);
 		frame.getContentPane().add(lblStartDate);
 
 		startMonthsList = new JComboBox<Object>(months);
 		startMonthsList.setSelectedIndex(7);
 		startMonthsList.setBounds(175, 468, 70, 27);
 		frame.getContentPane().add(startMonthsList);
 
 		startDaysList = new JComboBox<Object>(days);
 		startDaysList.setSelectedIndex(20);
 		startDaysList.setBounds(248, 468, 75, 27);
 		frame.getContentPane().add(startDaysList);
 
 		startYearsList = new JComboBox<Object>(years);
 		startYearsList.setSelectedIndex(0);
 		startYearsList.setBounds(326, 468, 99, 27);
 		frame.getContentPane().add(startYearsList);
 
 		JLabel lblEndDate = new JLabel("End Date:");
 		lblEndDate.setBounds(73, 507, 99, 16);
 		frame.getContentPane().add(lblEndDate);
 
 		endMonthsList = new JComboBox<Object>(months);
 		endMonthsList.setSelectedIndex(4);
 		endMonthsList.setBounds(175, 503, 70, 27);
 		frame.getContentPane().add(endMonthsList);
 
 		endDaysList = new JComboBox<Object>(days);
 		endDaysList.setSelectedIndex(22);
 		endDaysList.setBounds(248, 503, 75, 27);
 		frame.getContentPane().add(endDaysList);
 
 		endYearsList = new JComboBox<Object>(years);
 		endYearsList.setSelectedIndex(1);
 		endYearsList.setBounds(326, 503, 99, 27);
 		frame.getContentPane().add(endYearsList);
 
 		JButton btnSubmit = new JButton("Submit");
 		btnSubmit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ScheduleDataStorage data = new ScheduleDataStorage();
 				data.setStartDate(new CurrentDate(Integer.parseInt(months[startMonthsList.getSelectedIndex()]), Integer
 						.parseInt(days[startDaysList.getSelectedIndex()]), Integer.parseInt(years[startYearsList
 						.getSelectedIndex()])));
 				data.setEndDate(new CurrentDate(Integer.parseInt(months[endMonthsList.getSelectedIndex()]), Integer
 						.parseInt(days[endDaysList.getSelectedIndex()]), Integer.parseInt(years[endYearsList
 						.getSelectedIndex()])));
 				data.setFilename(txtMycaltxt.getText());
 				data.setMidSchoolElective(true);
 				data.setSchool(SchoolType.SIXTH);
 				data.setIncludeBreaksAndLunch(true);
 				data.setSixth(true);
 				data.setSixthPrefs(new int[] { Integer.parseInt(sixthOptions[sixthFieldLangSS.getSelectedIndex()]),
 						Integer.parseInt(sixthOptions[sixthFieldMath.getSelectedIndex()]),
 						Integer.parseInt(sixthOptions[sixthFieldScience.getSelectedIndex()]),
 						Integer.parseInt(sixthOptions[sixthFieldRotation.getSelectedIndex()]) });
 				data.setPeriodsToInclude(Period.sixthGradeClasses);
 				@SuppressWarnings("unused")
 				ScheduleGeneratorDriver maker = new ScheduleGeneratorDriver(data);
 				JOptionPane.showMessageDialog(frame, txtMycaltxt.getText() + " written sucessfully.");
 			}
 		});
 		btnSubmit.setBounds(96, 630, 117, 29);
 		frame.getContentPane().add(btnSubmit);
 
 		JButton btnQuit = new JButton("Quit");
 		btnQuit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		btnQuit.setBounds(276, 630, 117, 29);
 		frame.getContentPane().add(btnQuit);
 
 		txtMycaltxt = new JTextField();
 		txtMycaltxt.setText("MyCal.txt");
 		txtMycaltxt.setBounds(183, 560, 134, 28);
 		frame.getContentPane().add(txtMycaltxt);
 		txtMycaltxt.setColumns(10);
 	}
 
 	protected void initializeSixthPeriodSelectionMode() {
 		JLabel lblNewLabel = new JLabel(
 				"<html>Welcome to the Kent Denver Personal Schedule Generator! </br>Fill out the fields below then press submit.  </br> Once you are content with your settings, press submit and upload the generated file (created in the same directory as this program) to your calendar.</html>");
 		lblNewLabel.setBounds(24, 29, 445, 67);
 		frame.getContentPane().add(lblNewLabel);
 
 		JLabel lblNewLabel_2 = new JLabel(
 				"<html> You can uncheck the box next to any period that you do not wish to add to your calendar. Make sure to select the correct section to the right of the check boxes.</html>");
 		lblNewLabel_2.setBounds(28, 118, 438, 67);
 		frame.getContentPane().add(lblNewLabel_2);
 
 		chckbxPeriod = new JCheckBox("Language Arts");
 		chckbxPeriod.setSelected(true);
 		chckbxPeriod.setBounds(85, 183, 128, 23);
 		frame.getContentPane().add(chckbxPeriod);
 
 		chckbxPeriod_6 = new JCheckBox("Social Studies");
 		chckbxPeriod_6.setSelected(true);
 		chckbxPeriod_6.setBounds(85, 209, 128, 23);
 		frame.getContentPane().add(chckbxPeriod_6);
 
 		chckbxPeriod_5 = new JCheckBox("Math");
 		chckbxPeriod_5.setSelected(true);
 		chckbxPeriod_5.setBounds(85, 257, 128, 23);
 		frame.getContentPane().add(chckbxPeriod_5);
 
 		chckbxPeriod_4 = new JCheckBox("Science");
 		chckbxPeriod_4.setSelected(true);
 		chckbxPeriod_4.setBounds(85, 283, 128, 23);
 		frame.getContentPane().add(chckbxPeriod_4);
 
 		chckbxPeriod_3 = new JCheckBox("Rotations");
 		chckbxPeriod_3.setSelected(true);
 		chckbxPeriod_3.setBounds(85, 335, 128, 23);
 		frame.getContentPane().add(chckbxPeriod_3);
 
 		sixthFieldLangSS = new JComboBox<Object>(sixthSimpleOptions);
 		sixthFieldLangSS.setBounds(272, 193, 64, 27);
 		sixthFieldLangSS.setSelectedIndex(0);
 		frame.getContentPane().add(sixthFieldLangSS);
 
 		sixthFieldScience = new JComboBox<Object>(sixthSimpleOptions);
 		sixthFieldScience.setBounds(272, 267, 64, 27);
 		sixthFieldScience.setSelectedIndex(0);
 		frame.getContentPane().add(sixthFieldScience);
 
 		sixthFieldRotation = new JComboBox<Object>(sixthOptions);
 		sixthFieldRotation.setBounds(272, 333, 64, 27);
 		sixthFieldRotation.setSelectedIndex(0);
 		frame.getContentPane().add(sixthFieldRotation);
 
 		chckbxPeriod_1 = new JCheckBox("Electives");
 		chckbxPeriod_1.setSelected(true);
 		chckbxPeriod_1.setBounds(85, 383, 128, 23);
 		frame.getContentPane().add(chckbxPeriod_1);
 
 		JLabel lblStartDate = new JLabel("Start Date:");
 		lblStartDate.setBounds(57, 426, 99, 16);
 		frame.getContentPane().add(lblStartDate);
 
 		startMonthsList = new JComboBox<Object>(months);
 		startMonthsList.setSelectedIndex(7);
 		startMonthsList.setBounds(159, 422, 70, 27);
 		frame.getContentPane().add(startMonthsList);
 
 		startDaysList = new JComboBox<Object>(days);
 		startDaysList.setSelectedIndex(20);
 		startDaysList.setBounds(232, 422, 75, 27);
 		frame.getContentPane().add(startDaysList);
 
 		startYearsList = new JComboBox<Object>(years);
 		startYearsList.setSelectedIndex(0);
 		startYearsList.setBounds(310, 422, 99, 27);
 		frame.getContentPane().add(startYearsList);
 
 		JLabel lblEndDate = new JLabel("End Date:");
 		lblEndDate.setBounds(57, 479, 99, 16);
 		frame.getContentPane().add(lblEndDate);
 
 		endMonthsList = new JComboBox<Object>(months);
 		endMonthsList.setSelectedIndex(4);
 		endMonthsList.setBounds(159, 475, 70, 27);
 		frame.getContentPane().add(endMonthsList);
 
 		endDaysList = new JComboBox<Object>(days);
 		endDaysList.setSelectedIndex(22);
 		endDaysList.setBounds(232, 475, 75, 27);
 		frame.getContentPane().add(endDaysList);
 
 		endYearsList = new JComboBox<Object>(years);
 		endYearsList.setSelectedIndex(1);
 		endYearsList.setBounds(310, 475, 99, 27);
 		frame.getContentPane().add(endYearsList);
 
 		JButton btnSubmit = new JButton("Submit");
 		btnSubmit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ArrayList<Integer> periods = new ArrayList<Integer>();
 				ScheduleDataStorage data = new ScheduleDataStorage();
 				data.setStartDate(new CurrentDate(Integer.parseInt(months[startMonthsList.getSelectedIndex()]), Integer
 						.parseInt(days[startDaysList.getSelectedIndex()]), Integer.parseInt(years[startYearsList
 						.getSelectedIndex()])));
 				data.setEndDate(new CurrentDate(Integer.parseInt(months[endMonthsList.getSelectedIndex()]), Integer
 						.parseInt(days[endDaysList.getSelectedIndex()]), Integer.parseInt(years[endYearsList
 						.getSelectedIndex()])));
 				data.setFilename(txtMycaltxt.getText());
 				data.setSchool(SchoolType.SIXTH);
 				if (chckbxPeriod_1.isSelected()) {
 					data.setMidSchoolElective(true);
 				} else {
 					data.setMidSchoolElective(false);
 				}
 				if (chckbxIncludeBreaksLunch.isSelected()) {
 					data.setIncludeBreaksAndLunch(true);
 					periods.add(Period.GEOBASEBALL);
 				} else {
 					data.setIncludeBreaksAndLunch(false);
 				}
 				data.setSixth(true);
 				data.setSixthPrefs(new int[] {
 						Integer.parseInt(sixthOptions[sixthFieldLangSS.getSelectedIndex() == 0 ? 0 : 2]),
 						Integer.parseInt(sixthOptions[sixthFieldScience.getSelectedIndex() == 0 ? 0 : 2]),
 						Integer.parseInt(sixthOptions[sixthFieldScience.getSelectedIndex() == 0 ? 0 : 2]),
 						Integer.parseInt(sixthOptions[sixthFieldRotation.getSelectedIndex()]) });
 				System.out.println(Integer.parseInt(sixthOptions[sixthFieldScience.getSelectedIndex() == 0 ? 0 : 2]));
 				if (chckbxPeriod.isSelected()) {
 					if (sixthFieldLangSS.getSelectedIndex() == 0) {
 						periods.add(Period.LANG12);
 					} else {
 						periods.add(Period.LANG34);
 					}
 				}
 				if (chckbxPeriod_6.isSelected()) {
 					if (sixthFieldLangSS.getSelectedIndex() == 0) {
 						periods.add(Period.SS12);
 					} else {
 						periods.add(Period.SS34);
 					}
 				}
 				if (chckbxPeriod_5.isSelected()) {
 					if (sixthFieldLangSS.getSelectedIndex() == 1) {
 						periods.add(Period.LANG12);
 					} else {
 						periods.add(Period.LANG34);
 					}
 				}
 				if (chckbxPeriod_4.isSelected()) {
 					if (sixthFieldLangSS.getSelectedIndex() == 1) {
 						periods.add(Period.SS12);
 					} else {
 						periods.add(Period.SS34);
 					}
 				}
 				if (chckbxPeriod_3.isSelected()) {
					for (int i = Period.ROT1; i <= Period.ROT16; i++) {
 						periods.add(i);
 					}
 				}
 				data.setPeriodsToInclude(makeArray(periods));
 				@SuppressWarnings("unused")
 				ScheduleGeneratorDriver maker = new ScheduleGeneratorDriver(data);
 				JOptionPane.showMessageDialog(frame, txtMycaltxt.getText() + " written sucessfully.");
 			}
 
 			private int[] makeArray(ArrayList<Integer> periods) {
 				int[] temp = new int[periods.size()];
 				for (int i = 0; i < temp.length; i++) {
 					temp[i] = periods.get(i);
 				}
 				return temp;
 			}
 		});
 		btnSubmit.setBounds(96, 630, 117, 29);
 		frame.getContentPane().add(btnSubmit);
 
 		JButton btnQuit = new JButton("Quit");
 		btnQuit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		btnQuit.setBounds(276, 630, 117, 29);
 		frame.getContentPane().add(btnQuit);
 
 		JLabel lblOutputFilename = new JLabel("Output filename:");
 		lblOutputFilename.setBounds(113, 580, 141, 16);
 		frame.getContentPane().add(lblOutputFilename);
 
 		txtMycaltxt = new JTextField();
 		txtMycaltxt.setText("MyCal.txt");
 		txtMycaltxt.setBounds(243, 574, 134, 28);
 		frame.getContentPane().add(txtMycaltxt);
 		txtMycaltxt.setColumns(10);
 
 		chckbxIncludeBreaksLunch = new JCheckBox(
 				"<html>Include breaks, lunch, geobaseball, and student life <br>(assembly, homeroom, etc.)</html>");
 		chckbxIncludeBreaksLunch.setBounds(37, 530, 433, 40);
 		frame.getContentPane().add(chckbxIncludeBreaksLunch);
 	}
 
 }
