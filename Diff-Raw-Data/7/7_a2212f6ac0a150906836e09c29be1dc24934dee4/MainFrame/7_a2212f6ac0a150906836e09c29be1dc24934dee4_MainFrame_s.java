 package uk.ac.gla.dcs.tp3.w.ui;
 
 import uk.ac.gla.dcs.tp3.w.algorithm.Algorithm;
 import uk.ac.gla.dcs.tp3.w.parser.Parser;
 import uk.ac.gla.dcs.tp3.w.print.LaTeXFile;
 import uk.ac.gla.dcs.tp3.w.gen.GenerateLeague;
 import uk.ac.gla.dcs.tp3.w.league.*;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.*;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.*;
 import javax.swing.RowSorter.SortKey;
 
 
 public class MainFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private static final double SPACING = 1.25;
 	private HashMap<String, Division> divisions;
 	private Table table;
 	private String[] leagues = { "National", "American" };
 	private String[] divisionNames = { "West", "Central", "East" };
 	private DateTime displayDate, startDate, endDate;
 	private int numDaysToMove = 1;
 	private JLabel dateLabel;
 	private JPanel screenPanel;
 	private final JFileChooser fc = new JFileChooser();
 	private Parser p = new Parser();
 	private LaTeXFile LF;
 	private TableMouseListener listener;
 	//values for the comboBoxes, need them here to allow editing in inner classes
 	private Integer[] days = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
 			15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30 };
 	private String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
 			"Aug", "Sep", "Oct", "Nov", "Dec" };
 	private Integer[] years = { 2011, 2012 };
 
 	public MainFrame(HashMap<String, Division> d) {
 		divisions = d;
 		initUI();
 		LF = new LaTeXFile();
 	}
 
 	public final void initUI() {
 		// Set up the panels with appropriate layout managers
 		screenPanel = new JPanel(new BorderLayout());
 		JPanel tablePanel = new JPanel(new BorderLayout());
 		JPanel navPanel = new JPanel(new BorderLayout());
 		JPanel topPanel = new JPanel(new BorderLayout());
 
 		// Add panels to the screen panel and then add that to the JFrame
 		screenPanel.add(topPanel, BorderLayout.PAGE_START);
 		screenPanel.add(tablePanel, BorderLayout.CENTER);
 		screenPanel.add(navPanel, BorderLayout.PAGE_END);
 		getContentPane().add(screenPanel);
 
 		// Display menu bar
 		initMenuBar();
 
 		// Add the JTable for showing league data
 		initTable(tablePanel);
 
 		// Calculate the start and end dates, set table to
 		// display the end date
 		calcStartDate();
 		calcEndDate();
 		updateMatchesPlayed();
 
 		// Add the division and league radio buttons
 		initTopPanel(topPanel);
 
 		// Add the panel that shows the previous/next week buttons
 		initNavPanel(navPanel);
 
 		// Set the JFrame's attributes
 		setTitle("Team W - Algorithms for Sports Eliminations");
 		setLocation(100, 100);
 		setVisible(true);
 		pack();
 		setSize((int) (getWidth() * SPACING), getHeight());
 		setMinimumSize(new Dimension(getWidth(), getHeight()));
 	}
 
 	// Set up menu bar
 	private void initMenuBar() {
 		JMenuBar menuBar;
 		JMenu menu, submenu;
 		JMenuItem menuItem;
 
 		/*
 		 * File Menu
 		 */
 		menuBar = new JMenuBar();
 		menu = new JMenu("File");
 		menuBar.add(menu);
 
 		menuItem = new JMenuItem("Open...");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int value = fc.showOpenDialog(screenPanel);
 				if (value == JFileChooser.APPROVE_OPTION) {
 					File file = fc.getSelectedFile();
 					boolean valid = false;
 					try {
 						System.out.println(file.getName());
 						valid = p.parse(file.getAbsolutePath());
 					} catch (Exception ee) {
 						JOptionPane.showMessageDialog(screenPanel,
 								"Invalid file format");
 						return;
 					}
 					if (valid) {
 						divisions = p.getDivisions();
 						table.changeDivisions(divisions);
 						listener.updateDivision(divisions);
 						// Calculate the start and end dates, set table to
 						// display the end date
 						calcStartDate();
 						calcEndDate();
 						updateMatchesPlayed();
 						dateLabel.setText("Current date: "
 								+ displayDate.toString());
 						JOptionPane.showMessageDialog(screenPanel,
 								"Valid file format");
 					} else {
 						JOptionPane.showMessageDialog(screenPanel,
 								"Invalid file format");
 					}
 				}
 			}
 		});
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Generate League...");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int value = fc.showSaveDialog(screenPanel);
 				if (value == JFileChooser.APPROVE_OPTION) {
 					String filename = fc.getSelectedFile().getAbsolutePath();
 					if (!filename.contains(".txt"))
 						filename = filename + ".txt";
 					GenerateLeague gl = new GenerateLeague(filename);
 					if (gl.generate()) {
 						JOptionPane.showMessageDialog(screenPanel,
 								"File Generated!");
 					} else {
 						JOptionPane.showMessageDialog(screenPanel,
 								"File could not be created.");
 					}
 				}
 			}
 		});
 		menu.add(menuItem);
 
 		submenu = new JMenu("Print");
 
 		menuItem = new JMenuItem("Add to print");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				LF.addDivisionFromJTable(table, displayDate);
 				JOptionPane.showMessageDialog(
 						screenPanel,
 						"Added: " + table.getCurrent() + " "
 								+ displayDate.genDate());
 			}
 		});
 
 		submenu.add(menuItem);
 
 		submenu.addSeparator();
 		menuItem = new JMenuItem("Print...");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (LF.getNumContents() == 0) {
 					JOptionPane.showMessageDialog(screenPanel,
 							"Nothing to be printed!");
 					return;
 				}
 				int value = fc.showSaveDialog(screenPanel);
 				if (value == JFileChooser.APPROVE_OPTION) {
 					String filename = fc.getSelectedFile().getName();
 					String directory = fc.getSelectedFile().getParentFile()
 							.getAbsolutePath();
 					System.out.println(directory);
 					System.out.println(filename);
 					if (LF.write(filename, directory))
 						JOptionPane.showMessageDialog(screenPanel,
 								"File Printed");
 					System.out.println("Print " + table.getCurrent());
 				}
 			}
 		});
 		submenu.add(menuItem);
 		menu.add(submenu);
 
 		menuItem = new JMenuItem("Exit");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		menu.add(menuItem);
 
 		/*
 		 * About Menu
 		 */
 		menu = new JMenu("About");
 		menuBar.add(menu);
 
 		menuItem = new JMenuItem("The Team");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(screenPanel, "We are Team W.");
 			}
 		});
 		menu.add(menuItem);
 
 		this.setJMenuBar(menuBar);
 	}
 
 	// get the start date in the fixtures
 	private void calcStartDate() {
 		DateTime date = divisions.get(table.getCurrent()).getFixtures().get(0)
 				.getDateTime();
 		for (Division d : divisions.values()) {
 			for (Match m : d.getFixtures()) {
 				if (m.getDateTime().before(date))
 					date = m.getDateTime();
 			}
 		}
 		startDate = date;
 		System.out.println("Starting date is: " + date.toString());
 	}
 
 	// get the end date in the fixtures
 	private void calcEndDate() {
 		DateTime date = divisions.get(table.getCurrent()).getFixtures().get(0)
 				.getDateTime();
 		for (Division d : divisions.values()) {
 			for (Match m : d.getFixtures()) {
 				if (!m.getDateTime().before(date))
 					date = m.getDateTime();
 			}
 		}
 		displayDate = date;
 		endDate = new DateTime(date);
 		System.out.println("End date is: " + displayDate.toString());
 	}
 
 	// loop through every game played in the current division,
 	// check if date is less than/equal to current date,
 	// if not unplay match
 	private void updateMatchesPlayed() {
 		for (Division d : divisions.values()) {
 			for (Match m : d.getFixtures()) {
 				if (m.getDateTime().before(displayDate)) {
 					m.playMatch();
 				} else {
 					m.unplayMatch();
 				}
 			}
 			for (Team t : d.getTeams()) {
 				t.setEliminated(false);
 				ArrayList<Team> teams = t.getEliminatedBy();
 				t.getEliminatedBy().removeAll(teams);
 			}
 			(new Algorithm(d)).updateDivisionElim();
 		}
 		table.setCurrent(table.getCurrent());
 	}
 
 	private void initTopPanel(JPanel topPanel) {
 		dateLabel = new JLabel("Current date: " + displayDate.toString(),
 				JLabel.CENTER);
 		topPanel.add(dateLabel, BorderLayout.NORTH);
 		JPanel radioPanel = new JPanel();
 		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
 		initRadioButtons(radioPanel);
 		topPanel.add(radioPanel, BorderLayout.SOUTH);
 	}
 
 	private void initRadioButtons(JPanel radioPanel) {
 		ButtonGroup leagueGroup = new ButtonGroup();
 		ButtonGroup divisionGroup = new ButtonGroup();
 
 		ActionListener leagueListener = new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				JRadioButton rb = null;
 				if (event.getSource() instanceof JRadioButton)
 					rb = (JRadioButton) event.getSource();
 				else
 					return;
 				String s = table.getCurrent();
 				String[] sa = s.split(" ");
 				sa[0] = rb.getText();
 				table.setCurrent(sa[0] + " " + sa[1]);
 			}
 		};
 		ActionListener divisionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				JRadioButton rb = null;
 				if (event.getSource() instanceof JRadioButton)
 					rb = (JRadioButton) event.getSource();
 				else
 					return;
 				String s = table.getCurrent();
 				String[] sa = s.split(" ");
 				sa[1] = rb.getText();
 				table.setCurrent(sa[0] + " " + sa[1]);
 			}
 		};
 
 		JPanel leaguePanel = new JPanel();
 		leaguePanel.add(new JLabel("League: "));
 		for (String s : leagues) {
 			JRadioButton rButton1 = new JRadioButton(s);
 			if (s.equals("National"))
 				rButton1.setSelected(true);
 			leagueGroup.add(rButton1);
 			leaguePanel.add(rButton1);
 			rButton1.addActionListener(leagueListener);
 		}
 		radioPanel.add(leaguePanel, BorderLayout.WEST);
 
 		JPanel divisionPanel = new JPanel();
 		divisionPanel.add(new JLabel("Division: "));
 		for (String s : divisionNames) {
 			JRadioButton rButton3 = new JRadioButton(s);
 			if (s.equals("West"))
 				rButton3.setSelected(true);
 			rButton3.addActionListener(divisionListener);
 			divisionGroup.add(rButton3);
 			divisionPanel.add(rButton3);
 		}
 		radioPanel.add(divisionPanel, BorderLayout.CENTER);
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private void initNavPanel(JPanel navPanel) {
 		JButton backButton = new JButton("Previous Day");
 		backButton.setToolTipText("Move to previous day of results");
 		backButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				// decrement the day
 				// then update the model
 				for (int i = 0; i < numDaysToMove; i++) {
 					if (!displayDate.equals(startDate)) {
 						System.out.println("Back");
 						displayDate.decrementDate();
 					} else
 						displayDate = new DateTime(startDate);
 				}
 				System.out.println("Current date is now "
 						+ displayDate.toString());
 				dateLabel.setText("Current date: " + displayDate.toString());
 				updateMatchesPlayed();
 			}
 		});
 		navPanel.add(backButton, BorderLayout.WEST);
 		JButton nextButton = new JButton("Next Day");
 		nextButton.setToolTipText("Move to next day of results");
 		nextButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				// increment the day
 				// then update the model
 				for (int i = 0; i < numDaysToMove; i++) {
 					if (!displayDate.equals(endDate)) {
 						System.out.println("Next");
 						displayDate.incrementDate();
 					} else
 						displayDate = new DateTime(endDate);
 				}
 				System.out.println("Current date is now "
 						+ displayDate.toString());
 				dateLabel.setText("Current date: " + displayDate.toString());
 				updateMatchesPlayed();
 			}
 		});
 		navPanel.add(nextButton, BorderLayout.EAST);
 
 		// add comboBoxes
 		final JComboBox yearBox = new JComboBox(years);
 		yearBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				displayDate.setYear((Integer)yearBox.getSelectedItem());
 				updateMatchesPlayed();
 			}
 		});
 		final JComboBox monthBox = new JComboBox(months);
 		//need to be able to reference this in monthBox's handler
 		final JComboBox dayBox = new JComboBox(days);
 		monthBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				//set the month and set the day box's values to match the selected month
 				String m = (String)monthBox.getSelectedItem();
 				displayDate.setMonth(Month.getMonthNumber(m.toUpperCase()));
 				int daysInMonth = Month.daysInMonth(displayDate.getMonth(), displayDate.getYear());
 				days = new Integer[daysInMonth];
 				for(int i = 0; i < daysInMonth; i++)
 					days[i] = i + 1;
 				System.out.println(displayDate);
 				updateMatchesPlayed();
 			}
 		});
 		
 		dayBox.setSelectedIndex(0);
 		dayBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				displayDate.setDate((Integer)dayBox.getSelectedItem());
 				updateMatchesPlayed();
 			}
 		});
 
 		JPanel dateSelectionPanel = new JPanel();
 		dateSelectionPanel.add(dayBox);
 		dateSelectionPanel.add(monthBox);
 		dateSelectionPanel.add(yearBox);
 
 		navPanel.add(dateSelectionPanel);
 
 	}
 
 	private void initTable(JPanel tablePanel) {
 		table = new Table(divisions);
 		table.setFillsViewportHeight(true);
 		table.setAutoCreateRowSorter(true);
 		listener = new TableMouseListener(table, divisions, this);
 		table.addMouseListener(listener);
 		DefaultRowSorter<?, ?> sorter = ((DefaultRowSorter<?, ?>) table
 				.getRowSorter());
 		ArrayList<SortKey> list = new ArrayList<SortKey>();
 		list.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
 		sorter.setSortKeys(list);
 		sorter.sort();
 		JScrollPane sP = new JScrollPane(table);
 		tablePanel.add(sP);
 	}
 }
