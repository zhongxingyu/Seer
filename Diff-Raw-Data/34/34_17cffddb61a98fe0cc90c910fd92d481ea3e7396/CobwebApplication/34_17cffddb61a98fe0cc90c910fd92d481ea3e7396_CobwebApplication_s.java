 /*  $$$$$:  Comments by Liang
  *
  *  $$$$$$: Codes modified and/or added by Liang
  */
 
 /// test
 
 package driver;
 
 import ga.GeneticCode;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowStateListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 
 import cobweb.LocalUIInterface;
 import cobweb.LocalUIInterface.TickEventListener;
 import cobweb.UIInterface.UIClient;
 
 public class CobwebApplication extends JFrame implements UIClient {
 
 	private static final long serialVersionUID = 1L;
 
 	static CobwebApplication CA;
 	// $$$$$$ Add a greeting string for the textWindow.  Mar 25
 	static final String GREETINGS = "Welcome to COBWEB 2\n"
 		+ "====================\n"
 		+ "This text area is for information output only.\n"
 		+ "Send us any errors/suggestions please!\n\n";
 	JTextArea textArea;
 
 	private Window textWindow;
 	private boolean textWindowHide = false;  // $$$$$$ indicating whether the output window is chosen to show.  (Mar 29)  Apr 22
 	private JButton hide; // $$$$$$ Mar 25
 
 	private final int maxfiles = 100;
 
 	private String fileNames[];
 
 	private int pauseAt[];
 
 	private int filecount = 0;
 
 	private Parser prsNames[];
 
 	private String inputFile;
 
 	private String midFile; // $$$$$$  added for supporting "Modify Current Data", to temporary save the name when adding a file.  Feb 14
 	private String currentFile; // $$$$$$ added for saving current used file name.  Mar 14
 
 	private cobweb.UIInterface uiPipe;
 
 	private DisplayPanel displayPanel;
 
 	private PauseButton pauseButton;
 
 	private StepButton stepButton;
 
 	private final Mouse mymouse = new Mouse();;  // $$$$$$ added to avoid duplicately information lines shown in textWindow.  Apr 1
 
 	/* selection mode */
 	private int mode = 0;
 
 	private int type = -1;
 
 	String newline = "\n";
 
 	public JTextField tickField;
 
 	private JMenuItem stoneMenu;
 
 	private JMenuItem observeMenu;
 
 	private JMenu foodMenu;
 
 	private JMenu agentMenu;
 
 	private final CobwebEventListener theListener;
 
 	private boolean invokedByModify; // $$$$$$ The value is determined by whether a "Test Data" window is invoked by one of "Modify This File"
 									 //          and "Modify Current Data" or by one of "Open", "Create New Data" and "Retrieve Default Data".  Mar 14
 
 	// $$$$$$ Reserved file names.  Feb 8
 	public static final String INITIAL_OR_NEW_INPUT_FILE_NAME = "initial_or_new_input_(reserved)";
 	public static final String DEFAULT_DATA_FILE_NAME = "default_data_(reserved)";
 	public static final String CURRENT_DATA_FILE_NAME = "current_data_(reserved)";
 
 	//$$$$$$ Frequently-used file suffixes.  Feb 11
 	public static final String XML_FILE_SUFFIX = ".xml";
 	public static final String TEMPORARY_FILE_SUFFIX = ".temp";
 
 	int randomSeedReminder = 0;  // $$$$$$ added for checkValidityOfGAInput() method in GUI.  Feb 25
 	int modifyingDefaultDataReminder = 0; // $$$$$$  added for openCurrentFile() method.  Mar 25
 
 	public static void main(String[] param) {
 		MyUncaughtExceptionHandler handler = new MyUncaughtExceptionHandler();
 		Thread.setDefaultUncaughtExceptionHandler(handler);
 
 		CA = new CobwebApplication(param);
 		if (param.length > 0) {
 			CA.inputFile = param[0];
 		} else {
 			CA.inputFile = INITIAL_OR_NEW_INPUT_FILE_NAME + XML_FILE_SUFFIX;  //  $$$$$$ change the name from original "input.xml".  Jan 31
 		}
 		CA.setCurrentFile(CA.inputFile);  // $$$$$$ added on Mar 14
 		//CA.setEnabled(false);  // $$$$$$ to make sure the "Cobweb Application" frame disables when ever the "Test Data" window showing.  Feb 28
 		// $$$$$  a file named as the above name will be automatically created or modified when everytime running the
 		// $$$$$     following code.  Please refer to GUI.GUI.close.addActionListener, "/* write UI info to xml file */".   Jan 24
 		GUI.createAndShowGUI(CA, CA.inputFile);
 
 		// $$$$$$ Added to check if the new data file is hidden.  Feb 22
 		File inf = new File(CA.inputFile);
 		if ( inf.isHidden() != false  || ((inf.exists() != false) && (inf.canWrite() == false)) ){
 			JOptionPane.showMessageDialog(GUI.frame,
 					"Caution:  The initial data file \"" + CA.inputFile + "\" is NOT allowed to be modified.\n" +
 					"\n                  Any modification of this data file will be neither implemented nor saved.");
 		}
 	}
 
 	private boolean clipped = false;
 
 	// constructor
 	CobwebApplication(String[] param) {
 		super("Cobweb Application");
 
 		/*** $$$$$$ For cancelling the output info text window, remove some codes in the field to the below block.  Apr 22*/
 		if (cobweb.globals.usingTextWindow == true) {
 			textArea = new JTextArea(GREETINGS, 40, 48);
 			textWindow = new Window(this);
 		}
 
 		addWindowStateListener(new WindowStateListener() {
 
 			public void windowStateChanged(WindowEvent e) {
				if (e.getNewState() == Frame.NORMAL) {
					clipped = false;
				} else {
 					clipped = true;
 				}
 			}
 
 		});
 
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				CobwebApplication.this.quitApplication();
 			}
 			// These events control the visibility of our output window (Pierre)
 			/*// $$$$$$ Below silenced on Mar 25
 			public void windowDeiconified(WindowEvent e) {
 				//textWindow.toFront();
 			}
 
 			public void windowIconified(WindowEvent e) {
 				//textWindow.toBack();
 			} */
 
 			// $$$$$$ The following is modified for correctly showing in both Windows and Linux.  Mar 29
 			@Override
 			public void windowActivated(WindowEvent e) { // without this block, in Linux textWindow would fail keep on top when the simulation is running
 				if (cobweb.globals.usingTextWindow == true && textWindowHide == false) { // if text window is chosen to show.  $$$$$$ "usingTextWindow" Apr 22
 					if (textWindow.isVisible() == false) { // if textWindow was set to invisible by the following block windowDeactivated
 						textWindow.setVisible(true);
 					} else {
 						textWindow.toFront();
 					}
 				}
 				//textWindow.toFront();
 			}
 
 			@Override
 			public void windowDeactivated(WindowEvent e) {
 				if (cobweb.globals.usingTextWindow == true && textWindowHide == false) { // if text window is chosen to show.  $$$$$$ "usingTextWindow" Apr 22
 					textWindow.setVisible(false);
 				}
 				//textWindow.toBack(); // if set textWindow toFront when window-Activated and toBack when Deactivated, problems may occur in Windows
 			}
 		});
 
 		setLayout(new BorderLayout());
 		setSize(580, 650);
 
 		// Create the various widgits to make the application go.
 
 		// A listener, to process events
 		theListener = new CobwebEventListener();
 
 		JMenuBar myMenuBar = makeMenuBar();
 
 		setJMenuBar(myMenuBar);
 
 		setVisible(true);
 
 		/*** $$$$$$ Cancel textWindow  Apr 22*/
 		if (cobweb.globals.usingTextWindow == true) {
 			// $$$$$$ Move the below block from "UIsettings()" method to here so that this "Info" window will only be created once at the beginning.  Mar 18
 			textArea.setEditable(false);
 			textArea.setFont(new Font("Courier", Font.PLAIN, 11));
 			textWindow.add(textArea, "North"); // $$$$$$ add "North" on Mar 25
 
 			// $$$$$$ Added on Mar 25
 			hide = new JButton("Hide");
 			hide.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					textWindow.setVisible(false);
 					textWindowHide = true; // $$$$$$ (Mar 29)  Apr 22
 					if (uiPipe == null) {
 						GUI.frame.toFront();
 					}
 				}
 			});
 			textWindow.add(hide, "South");
 
 			int text_x, text_y;
 			int size_x, size_y;
 			size_x = 400;// 80*6;
 			size_y = 650;// 40*11;  // $$$$$$ was "600".  Mar 25
 			try {
 				text_x = (Toolkit.getDefaultToolkit().getScreenSize()).width
 						- size_x;
 			} catch (Exception e) {
 				text_x = 0;
 			}
 			try {
 				text_y = (Toolkit.getDefaultToolkit().getScreenSize()).height
 						- size_y;
 			} catch (Exception e) {
 				text_y = 0;
 			}
 			textWindow.setBounds(text_x, text_y - 25, size_x, size_y);
 			//textWindow.setAlwaysOnTop(true);  // $$$$$$ silenced on Mar 18
 			textWindow.pack();
 			//textWindow.setVisible(true); // $$$$$$ silenced on Mar 29
 			//textWindow.toFront();  // $$$$$$ silenced on Mar 14
 		}
 
 	}
 	private JMenuBar makeMenuBar() {
 		// Build the menu items
 		JMenuItem openMenu = new JMenuItem("Open");
 		openMenu.setActionCommand("Open");
 		openMenu.addActionListener(theListener);
 
 		// $$$$$$ Add "Set Default Data" menu.  Feb 21
 		JMenuItem setMenu = new JMenuItem("Set Default Data");
 		setMenu.setActionCommand("Set Default Data");
 		setMenu.addActionListener(theListener);
 
 		// $$$$$$ Add "Retrieve Default Data" menu.  Feb 4
 		JMenuItem defaultMenu = new JMenuItem("Retrieve Default Data");
 		defaultMenu.setActionCommand("Retrieve Default Data");
 		defaultMenu.addActionListener(theListener);
 		// $$$$$$ Add "Modify Current Data" menu.  Feb 12
 		JMenuItem currentDataMenu = new JMenuItem("Modify Current Data");
 		currentDataMenu.setActionCommand("Modify Current Data");
 		currentDataMenu.addActionListener(theListener);
 
 		JMenuItem NewDataFileMenu = new JMenuItem(
 				"Create New Data");
 		NewDataFileMenu.setActionCommand("Create New Data");
 		NewDataFileMenu.addActionListener(theListener);
 		JMenuItem MultFileMenu = new JMenuItem(
 				"Set Multiple Files");
 		MultFileMenu.setActionCommand("Set Multiple Files");
 		MultFileMenu.addActionListener(theListener);
 		JMenuItem modifyMenu = new JMenuItem("Modify This File");
 		modifyMenu.setActionCommand("Modify This File");
 		modifyMenu.addActionListener(theListener);
 		JMenuItem saveMenu = new JMenuItem("Save");
 		saveMenu.setActionCommand("Save");
 		saveMenu.addActionListener(theListener);
 		JMenuItem logMenu = new JMenuItem("Log");
 		logMenu.setActionCommand("Log");
 		logMenu.addActionListener(theListener);
 		//JMenuItem trackAgentMenu = new JMenuItem("Track Agent");
 		//trackAgentMenu.setActionCommand("Track Agent");
 		//trackAgentMenu.addActionListener(theListener);
 		JMenuItem quitMenu = new JMenuItem("Quit");
 		quitMenu.setActionCommand("Quit");
 		quitMenu.addActionListener(theListener);
 		JMenuItem reportMenu = new JMenuItem("Report");
 		reportMenu.setActionCommand("Report");
 		reportMenu.addActionListener(theListener);
 
 		JMenuItem aboutMenu = new JMenuItem("About");
 		aboutMenu.setActionCommand("About");
 		aboutMenu.addActionListener(theListener);
 		JMenuItem creditsMenu = new JMenuItem("Credits");
 		creditsMenu.setActionCommand("Credits");
 		creditsMenu.addActionListener(theListener);
 
 		/*** $$$$$$ Cancel textWindow  Apr 22*/
 		// $$$$$$ Add "Show/Hide Info" menu.  Mar 14
 		JMenuItem showInfoMenu = null;
 		if (cobweb.globals.usingTextWindow == true) {
 			showInfoMenu = new JMenuItem("Show/Hide Info");
 			showInfoMenu.setActionCommand("Show/Hide Info");
 			showInfoMenu.addActionListener(theListener);
 		}
 
 		observeMenu = new JMenuItem("Observation Mode");
 		observeMenu.setActionCommand("Observation Mode");
 		observeMenu.addActionListener(theListener);
 
 		stoneMenu = new JMenuItem("Select Stones");
 		stoneMenu.setActionCommand("Select Stones");
 		stoneMenu.addActionListener(theListener);
 
 		foodMenu = new JMenu("Select Food");
 		agentMenu = new JMenu("Select Agents");
 
 		JMenuItem removeStones = new JMenuItem(
 				"Remove All Stones");
 		removeStones.setActionCommand("Remove All Stones");
 		removeStones.addActionListener(theListener);
 
 		JMenuItem removeFood = new JMenuItem("Remove All Food");
 		removeFood.setActionCommand("Remove All Food");
 		removeFood.addActionListener(theListener);
 
 		JMenuItem removeAgents = new JMenuItem(
 				"Remove All Agents");
 		removeAgents.setActionCommand("Remove All Agents");
 		removeAgents.addActionListener(theListener);
 
 		// $$$$$$ Added on Feb 29
 		JMenuItem removeWaste = new JMenuItem(
 				"Remove All Waste");
 		removeWaste.setActionCommand("Remove All Waste");
 		removeWaste.addActionListener(theListener);
 
 		JMenuItem removeAll = new JMenuItem("Remove All");
 		removeAll.setActionCommand("Remove All");
 		removeAll.addActionListener(theListener);
 
 
 		// Assemble the items into menus
 		JMenu EditMenu = new JMenu("Edit");
 		EditMenu.add(observeMenu);
 		EditMenu.add(stoneMenu);
 		EditMenu.add(agentMenu);
 		EditMenu.add(foodMenu);
 		EditMenu.add(new JSeparator());
 		EditMenu.add(removeStones);
 		EditMenu.add(removeFood);
 		EditMenu.add(removeAgents);
 		EditMenu.add(removeWaste);  // $$$$$$ added on Feb 29
 		EditMenu.add(removeAll);
 
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.add(openMenu);
 		fileMenu.add(NewDataFileMenu);
 		fileMenu.add(MultFileMenu);
 		fileMenu.add(modifyMenu);
 
 		// $$$$$$ Add "Retrieve Default Data" menu.  Feb 4
 		fileMenu.add(defaultMenu);
 		// $$$$$$ Add "Modify Current Data" menu.  Feb 12
 		fileMenu.add(currentDataMenu);
 		// $$$$$$ Add "Set Default Data" menu.  Feb 21
 		fileMenu.add(new JSeparator());
 		fileMenu.add(setMenu);
 
 		fileMenu.add(new JSeparator());
 		fileMenu.add(saveMenu);
 		fileMenu.add(reportMenu);
 		fileMenu.add(logMenu);
 		//fileMenu.add(trackAgentMenu);
 		fileMenu.add(new JSeparator());
 		fileMenu.add(quitMenu);
 
 		JMenu helpMenu = new JMenu("Help");
 
 		/*** $$$$$$ Cancel textWindow  Apr 22*/
 		if (cobweb.globals.usingTextWindow == true) {
 			// $$$$$$ Add "Show/Hide Info" menu.  Mar 14
 			helpMenu.add(showInfoMenu);
 			helpMenu.add(new JSeparator());
 		}
 
 		helpMenu.add(aboutMenu);
 		//helpMenu.add(new JSeparator());  // $$$$$$ silenced on Mar 28
 		helpMenu.add(creditsMenu);
 
 		JMenu viewMenu = new JMenu("View");
 		JCheckBoxMenuItem viewLinearAI = new JCheckBoxMenuItem("AI Weight Stats", false);
 
 		viewLinearAI.addItemListener(new ItemListener() {
 
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					aiGraph = new LinearAIGraph();
 					aiGraph.pack();
 					aiGraph.setVisible(true);
 				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
 					aiGraph.setVisible(false);
 					aiGraph.setEnabled(false);
 					aiGraph = null;
 				}
 			}
 		});
 		viewMenu.add(viewLinearAI);
 
 		// Assemble the menus into a menu bar
 		JMenuBar myMenuBar = new JMenuBar();
 		myMenuBar.add(fileMenu);
 		myMenuBar.add(EditMenu);
 		myMenuBar.add(viewMenu);
 		myMenuBar.add(helpMenu);
 		return myMenuBar;
 	}
 
 	private JFrame aiGraph;
 
 	// $$$$$$ get UI.  Mar 14
 	public cobweb.UIInterface getUI() {
 		return uiPipe;
 	}
 
 	// $$$$$$ Implemented on Mar 14
 	public boolean isInvokedByModify() {
 		return invokedByModify;
 	}
 	public void setInvokedByModify(boolean b) {
 		invokedByModify = b;
 	}
 
 	// $$$$$$ Implemented on Mar 14
 	public String getCurrentFile() {
 		return currentFile;
 	}
 	public void setCurrentFile(String input) {
 		currentFile = input;
 	}
 
 
 	public void refreshAll(cobweb.UIInterface ui) {
 		displayPanel.repaint();
 		pauseButton.repaint();
 		pauseButton.updateLabel();
 		stepButton.repaint();
 		//if (tickField != null && !tickField.getText().equals("")) {tickField.setText("");}  // $$$$$$ reset tickField.  Mar 14
 	}
 
 	public void refresh(cobweb.UIInterface theUIInterface) {
 		if (displayPanel != null && pauseButton != null && stepButton != null) {
 			displayPanel.repaint();
 			pauseButton.repaint();
 			stepButton.repaint();
 		}
 	}
 
 	public void openMultFiles(Parser p[], int time[], int numfiles) {
 		uiPipe = new cobweb.LocalUIInterface(this, p, time,
 				numfiles);
 		UIsettings();
 	}
 
 	public void openFile(Parser p) {
 		if (uiPipe != null) {
 			uiPipe.load(this, p);
 		} else {
 			uiPipe = new LocalUIInterface(this, p);
 			UIsettings();
 		}
 		//this.toFront(); // $$$$$$ added for CA frame going to front when anytime this method is called.  Feb 22
 	}
 
 	JPanel mainPanel;
 
 	JLabel tickDisplay;
 
 	JPanel controls;
 
 	public void UIsettings() {
 
 		if (cobweb.globals.usingTextWindow == true) {uiPipe.addTextArea(textArea);}		/*** $$$$$$ Cancel textWindow  Apr 22*/
 
 		if (mainPanel == null) {
 			mainPanel = new JPanel();
 			mainPanel.setLayout(new BorderLayout());
 			add(mainPanel);
 		}
 
 		uiPipe.setRefreshTimeout(100);
 		uiPipe.setFrameSkip(0);
 		if (displayPanel == null) {
 			displayPanel = new DisplayPanel(uiPipe, 10, 10);
 		} else {
 			displayPanel.setUI(uiPipe);
 		}
 
 
 
 		mainPanel.add(displayPanel, BorderLayout.CENTER);
 		if (controls == null) {
 			controls = new JPanel();
 			//controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
 			mainPanel.add(controls, BorderLayout.NORTH);
 		}
 		if (tickDisplay == null) {
 			tickDisplay = new JLabel();
 			tickDisplay.setPreferredSize(new Dimension(90, 20));
 			controls.add(tickDisplay);
 		}
 		if (tickField == null) {
 			controls.add(new JLabel("Stop at"));
 			tickField = new JTextField(8);
 			tickField.setPreferredSize(new Dimension(40, 20));
 			controls.add(tickField);
 		}
 
 		if (pauseButton == null) {
 			pauseButton = new PauseButton(uiPipe);
 			controls.add(pauseButton);
 			stepButton = new StepButton(uiPipe);
 			controls.add(stepButton);
 			controls.add(new JLabel("   Adjust Speed:"));
 			MyScrollbar sb = new MyScrollbar(uiPipe);
 			controls.add(sb);
 		} else {
 			pauseButton.setUI(uiPipe);
 		}
 
 		JMenuItem foodtype[] = new JMenuItem[uiPipe
 				.countAgentTypes()];
 		JMenuItem agentype[] = new JMenuItem[uiPipe
 				.countAgentTypes()];
 		foodMenu.removeAll();
 		agentMenu.removeAll();
 		for (int i = 0; i < uiPipe.countAgentTypes(); i++) {
 			foodtype[i] = new JMenuItem("Food Type " + (i + 1));
 			foodtype[i].setActionCommand("Food Type " + (i + 1));
 			foodtype[i].addActionListener(theListener);
 			foodMenu.add(foodtype[i]);
 
 			agentype[i] = new JMenuItem("Agent Type " + (i + 1));
 			agentype[i].setActionCommand("Agent Type " + (i + 1));
 			agentype[i].addActionListener(theListener);
 			agentMenu.add(agentype[i]);
 		}
 
 		/*** $$$$$$ Cancel textWindow  Apr 22*/
 		if (cobweb.globals.usingTextWindow == true) {
 			if (textArea.getText().endsWith(GREETINGS) == false) {
 				textArea.setText(GREETINGS);  // $$$$$$ reset the output window.  Mar 25
 			}
 		}
 
 
 		tickField.addFocusListener(new FocusAdapter(){
 			@Override
 			public void focusLost(FocusEvent e) {
 				tickField.repaint();
 			}
 			@Override
 			public void focusGained(FocusEvent e) {
 				tickField.repaint();
 			}
 		});
 
 		//Mouse mymouse = new Mouse(); // $$$$$$ moved to this class's field to avoid duplicate mouse additions to displayPanel.  Apr 1
 		displayPanel.addMouseListener(mymouse);
 		displayPanel.addMouseMotionListener(mymouse);
 		//addMouseListener(mymouse);  // $$$$$$ do not know what for. silenced on Apr 1
 
 		uiPipe.setTickField(tickField);
 
 		uiPipe.AddTickEventListener(new TickEventListener() {
 			public void TickPerformed(long currentTick) {
 				tickDisplay.setText("Tick: " + Long.toString(currentTick) + "  ");
 			}
 		});
 
 		uiPipe.setPauseButton(pauseButton); // $$$$$$ Mar 20
 
 		validate();
 		uiPipe.start();
 	} // end of UISettings
 
 	/* $$$$$$ Modify this method to save test parameters rather than to save the state of the simulation.  see cobweb.LocalUIInterface#save  Feb 12
 	public void saveFile(String filePath) {
 		if (uiPipe != null) {
 			try {
 				uiPipe.save(filePath);
 			} catch (Throwable e) {
 				textArea.append("Save failed:" + e.getMessage());
 			}
 		}
 	}
 	*/
 	public void saveFile(String savingFile) {
 		try {
 			// $$$$$$ The following block added to handle a readonly file.  Feb 22
 			File sf = new File(savingFile);
 			if ( (sf.isHidden() != false) || ((sf.exists() != false) && (sf.canWrite() == false)) ) {
 				JOptionPane.showMessageDialog(GUI.frame,   // $$$$$$ change from "this" to "GUI.frame" specifically for MS Windows.  Feb 22
 						"Caution:  File \"" + savingFile + "\" is NOT allowed to be written to.",
 						"Warning", JOptionPane.WARNING_MESSAGE);
 			} else {
 			// $$$$$ The following line used to be the original code.  Feb 22
 			copyFile(getCurrentFile(), savingFile); // $$$$$$ modified on Mar 14
 			}
 		} catch (Throwable e) {
 			JOptionPane.showMessageDialog(this,  // $$$$$$ added on Apr 22
 					"Save failed: " + e.getMessage(),
 					"Warning", JOptionPane.WARNING_MESSAGE);
 			if (cobweb.globals.usingTextWindow == true) {textArea.append("Save failed:" + e.getMessage());} 	/*** $$$$$$ Cancel textWindow  Apr 22*/
 		}
 	}
 
 	public void logFile(String filePath) {
 		if (uiPipe != null) {
 			try {
 				uiPipe.log(filePath);
 			} catch (Throwable e) {
 				JOptionPane.showMessageDialog(this,  // $$$$$$ added on Apr 22
 						"Log failed: " + e.getMessage(),
 						"Warning", JOptionPane.WARNING_MESSAGE);
 				if (cobweb.globals.usingTextWindow == true) {textArea.append("Log failed:" + e.getMessage());} 	/*** $$$$$$ Cancel textWindow  Apr 22*/
 			}
 		}
 	}
 
 	public void reportFile(String filePath) {
 		if (uiPipe != null) {
 			try {
 				uiPipe.report(filePath);
 			} catch (Throwable e) {
 				JOptionPane.showMessageDialog(this,  // $$$$$$ added on Apr 22
 						"Report failed: " + e.getMessage(),
 						"Warning", JOptionPane.WARNING_MESSAGE);
 				if (cobweb.globals.usingTextWindow == true) {textArea.append("Report failed:" + e.getMessage());} 	/*** $$$$$$ Cancel textWindow  Apr 22*/
 			}
 		}
 	}
 	/*
 	public void trackAgentFile(String filePath) {
 		uiPipe
 				.writeToTextWindow("Track Agent is disabled for now! Please use the logging function instead.\n");
 	}
 	*/
 	public void quitApplication() {
 		if (uiPipe != null) {
 			uiPipe.killScheduler();
 		}
 		System.exit(0);
 	}
 
 	private class CobwebEventListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 
 			if (e.getActionCommand().compareTo("Open") == 0) {
 				pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				setInvokedByModify(false);  // $$$$$$ added on Mar 14  // need to implement only if using the old "Open" behaviour in Version 2006
 				CobwebApplication.this.openFileDialog();
 			// $$$$$$ Add "Set Default Data" menu.  Feb 21
 			} else if (e.getActionCommand().compareTo("Set Default Data") == 0) {
 				pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				CobwebApplication.this.setDefaultData();
 			// $$$$$$ Add "Retrieve Default Data" menu.  Feb 4
 			} else if (e.getActionCommand().compareTo("Retrieve Default Data") == 0) {
 				pauseUI(); // $$$$$$ Feb 12
 				if (GUI.frame.isVisible() == true) {
 					GUI.frame.dispose();  // $$$$$ for allowing only one "Test Data" window to show up
 				}
 				//CobwebApplication.this.setEnabled(false);  // $$$$$$ another way, to make sure the "Cobweb Application" frame disables when ever "Test Data" window showing
 				setInvokedByModify(false);  // $$$$$$ added on Mar 14
 				CobwebApplication.this.retrieveDefaultData();
 			// $$$$$$ Added for "Modify Current Data" menu.  Feb 12
 			} else if (e.getActionCommand().compareTo("Modify Current Data") == 0) {
 				pauseUI(); // $$$$$$ Feb 12
 				if (GUI.frame.isVisible() == true) {
 					GUI.frame.dispose();  // $$$$$ for allowing only one "Test Data" window to show up
 				}
 				//CobwebApplication.this.setEnabled(false);  // $$$$$$ another way, to make sure the "Cobweb Application" frame disables when ever "Test Data" window showing
 				setInvokedByModify(true);  // $$$$$$ added on Mar 14
 				CobwebApplication.this.openCurrentData();
 			// $$$$$$ Modified on Mar 14
 			} else if (e.getActionCommand().compareTo("Create New Data") == 0) {
 				pauseUI(); // $$$$$$ Feb 12
 				if (GUI.frame.isVisible() == true) {
 					GUI.frame.dispose();  // $$$$$ for allowing only one "Test Data" window to show up
 				}
 				//CobwebApplication.this.setEnabled(false); // $$$$$$ another way, to make sure the "Cobweb Application" frame disables when ever "Test Data" window showing
 				setInvokedByModify(false);  // $$$$$$ added on Mar 14
 				CobwebApplication.this.createNewData();  // $$$$$$ implemented on Mar 14
 			} else if (e.getActionCommand().compareTo("Modify This File") == 0) {
 				pauseUI(); // $$$$$$ Feb 12
 				if (GUI.frame.isVisible() == true) {
 					GUI.frame.dispose();  // $$$$$ for allowing only one "Test Data" window to show up
 				}
 				//CobwebApplication.this.setEnabled(false);  // $$$$$$ another way, to make sure the "Cobweb Application" frame disables when ever "Test Data" window showing
 				setInvokedByModify(true);  // $$$$$$ added on Mar 14
 				CobwebApplication.this.openCurrentFile();
 			} else if (e.getActionCommand().compareTo("Set Multiple Files") == 0) {
 				pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				CobwebApplication.this.setMultFilesDialog(347, 265);
 			} else if (e.getActionCommand().compareTo("Save") == 0) {
 				pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				if (GUI.frame.isVisible() == false) {
 					GUI.createAndShowGUI(CA, CA.getCurrentFile());// $$$$$$ changed from "GUI.frame.setVisible(true);". Mar 17
 				}
 				CobwebApplication.this.saveFileDialog();
 				// $$$$$$ Modified for very first time running.  Feb 28
 				if (uiPipe != null) {
 					GUI.frame.dispose();  // $$$$$$ Feb 8  $$$$$$ change from "setVisible(false)".  Mar 17
 					//CobwebApplication.this.toFront();  // $$$$$$ added on Feb 22
 				}
 			} else if (e.getActionCommand().compareTo("Log") == 0) {
 				pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				// $$$$$$ Check whether "Log" menu is clicked before the simulation runs.  Feb 12
 				if (uiPipe == null) {
 					JOptionPane.showMessageDialog(GUI.frame,  // $$$$$$ change from "displayPanel" to "GUI.frame" specifically for MS Windows.  Feb 22
 							"To create a log file, please press \"OK\" to launch the Cobweb Application first.");
 				} else if (uiPipe.getTime() != 0) {
 					JOptionPane.showMessageDialog(CA,  // $$$$$$ change from "displayPanel" to "GUI.frame" specifically for MS Windows.  Feb 22
 							"To get a log file, the \"Log\" menu should be clicked before the simulation runs.",
 							"Warning", JOptionPane.WARNING_MESSAGE);
 				} else {
 					CobwebApplication.this.logFileDialog();
 				}
 			/*} else if (e.getActionCommand().compareTo("Track Agent") == 0) {
 				pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				CobwebApplication.this.trackAgentFileDialog(); */
 			} else if (e.getActionCommand().compareTo("Quit") == 0) {
 				CobwebApplication.this.quitApplication();
 			// $$$$$$ Implement "Show/Hide Info" menu.  Mar 14
 			} else if (e.getActionCommand().compareTo("Show/Hide Info") == 0) {
 				disposeGUIframe();
 				if (textWindow.isVisible() == false) {
 					textWindow.setVisible(true);
 					textWindowHide = false;  // $$$$$$ (Mar 29)  Apr 22
 				} else {
 					textWindow.setVisible(false);
 					textWindowHide = true;  // $$$$$$ (Mar 29)  Apr 22
 				}
 			} else if (e.getActionCommand().compareTo("About") == 0) {
 				//pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				CobwebApplication.this.aboutDialog();
 			} else if (e.getActionCommand().compareTo("Credits") == 0) {
 				//pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				CobwebApplication.this.creditsDialog();
 			} else if (e.getActionCommand().compareTo("Report") == 0) {
 				pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				// $$$$$$ Modified on Feb 29
 				if (uiPipe == null) {
 					JOptionPane.showMessageDialog(GUI.frame,  // $$$$$$ change from "displayPanel" to "GUI.frame" specifically for MS Windows.  Feb 22
 							"To create a report file, please press \"OK\" to launch the Cobweb Application first.");
 				} else {
 					CobwebApplication.this.reportDialog();
 				}
 				//CobwebApplication.this.reportDialog();
 			} else if (e.getActionCommand().compareTo("Observation Mode") == 0) {
 				//pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				mode = 0;
 			} else if (e.getActionCommand().compareTo("Select Stones") == 0) {
 				//pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				/* switch to stone selection mode */
 				mode = 1;
 			} else if (e.getActionCommand().compareTo("Remove All") == 0) {
 				//pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				/* remove all */
 				// $$$$$$ modified on Feb 29
 				if (uiPipe != null) {
 					mode = 0;
 					uiPipe.removeComponents(mode);
 				}
 				//mode = 0;
 				//uiPipe.removeComponents(mode);
 			} else if (e.getActionCommand().compareTo("Remove All Stones") == 0) {
 				//pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				/* remove all stones */
 				// $$$$$$ modified on Feb 29
 				if (uiPipe != null) {
 					mode = -1;
 					uiPipe.removeComponents(mode);
 				}
 				//mode = -1;
 				//uiPipe.removeComponents(mode);
 			} else if (e.getActionCommand().compareTo("Remove All Food") == 0) {
 				//pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				/* remove all food */
 				// $$$$$$ modified on Feb 29
 				if (uiPipe != null) {
 					mode = -2;
 					uiPipe.removeComponents(mode);
 				}
 				//mode = -2;
 				//uiPipe.removeComponents(mode);
 			} else if (e.getActionCommand().compareTo("Remove All Agents") == 0) {
 				//pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				/* remove all agents */
 				// $$$$$$ modified on Feb 29
 				if (uiPipe != null) {
 					mode = -3;
 					uiPipe.removeComponents(mode);
 				}
 				//mode = -3;
 				//uiPipe.removeComponents(mode);
 
 			// $$$$$$ Added on Feb 29
 			} else if (e.getActionCommand().compareTo("Remove All Waste") == 0) {
 				//pauseUI(); // $$$$$$ Feb 12
 				disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 				/* remove all agents */
 				// $$$$$$ modified on Feb 29
 				if (uiPipe != null) {
 					mode = -4;
 					uiPipe.removeComponents(mode);
 				}
 				//mode = -4;
 				//uiPipe.removeComponents(mode);
 			}
 
 			// Handles Foodtype and AgentType selections:
 			for (int i = 0; i < 4; i++) {
 				if (e.getActionCommand().compareTo("Food Type " + (i + 1)) == 0) {
 					//pauseUI(); // $$$$$$ Feb 12
 					disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 					/* switch to food selection mode */
 					mode = 2;
 					type = i;
 				} else if (e.getActionCommand().compareTo(
 						"Agent Type " + (i + 1)) == 0) {
 					//pauseUI(); // $$$$$$ Feb 12
 					disposeGUIframe();  // added to ensure no popup GUI frame when hitting a menu.  Feb 29
 					/* switch to agent selection mode */
 					mode = 3;
 					type = i;
 				}
 			}
 		}
 
 		// $$$$$$ If a "Test Data" window is open (visible), dispose it (when hitting a menu).  Feb 29
 		private void disposeGUIframe() {if (uiPipe != null && GUI.frame.isVisible() != false) {
 			GUI.frame.dispose();
 		}}
 
 		// $$$$$$ A facilitating method to ensure the UI to pause.  Feb 12
 		private void pauseUI(){
 			if (uiPipe != null && uiPipe.isPaused() == false) { // $$$$$$ changed from "if (uiPipe.isPaused() == false) {", for the very first run.  Feb 28
 				uiPipe.pause();
 				pauseButton.updateLabel();
 			}
 		}
 	}
 
 
 	public void openFileDialog() {
 		FileDialog theDialog = new FileDialog(GUI.frame,  // $$$$$$ modified from "this".  Feb 29
 				"Open a State File", FileDialog.LOAD);
 		theDialog.setVisible(true);
 		String directory = theDialog.getDirectory();
 		String file = theDialog.getFile();
 
 		// $$$$$$ Modify the following block to check whether the file exists.  Feb 21 $$$$$$ Remodified on Mar 14
 		if (file != null && directory != null) {
 			File of = new File(directory + file);
 			if (of.exists() != false) {
 				setCurrentFile(directory + file); // $$$$$ donot need this line if using the below block instead
 				/* $$$$$ If wanting the "Test Data" window to show up, use the below block instead.  Feb 28  // silence this block on Mar 31
 				Parser p = new Parser(getCurrentFile());
 				// $$$$$$ Changed on Feb 29
 				if (uiPipe != null) {
 					uiPipe.killScheduler();
 					uiPipe = null;
 				}
 				//uiPipe.killScheduler();
 				//uiPipe = null;
 				if (GUI.frame.isVisible() == true) GUI.frame.dispose();  // $$$$$ for allowing only one "Test Data" window to show up.  Feb 28
 				//if (tickField != null && !tickField.getText().equals("")) {tickField.setText("");}  // $$$$$$ reset tickField.  Mar 14
 				CA.openFile(p);
 				*/
 
 				///* $$$$$$ If NOT wanting the "Test Data" window to show up, use the above block instead.  Feb 28  // implement this block on Mar 31
 				if (GUI.frame.isVisible() == true) {
 					GUI.frame.dispose();  // $$$$$ for allowing only one "Test Data" window to show up.  Feb 28
 				}
 				GUI.createAndShowGUI(CA, getCurrentFile());
 				//CobwebApplication.this.setEnabled(false);  // $$$$$$ to make sure the "Cobweb Application" frame disables when ever the "Test Data" window showing
 		 		// $$$$$$ Modified on Feb 28
 				//if (uiPipe != null) {
 				//	uiPipe.reset();
 				//	refreshAll(uiPipe);
 				//}
 				//*/
 			} else {
 				//if (uiPipe != null && GUI.frame.isVisible() == true) GUI.frame.dispose(); // $$$$$ for allowing only one "Test Data" window to show up.  Feb 28
 				JOptionPane.showMessageDialog(this,  // $$$$$ change from "GUI.frame".  Mar 17
 						"File \" " + directory + file + "\" could not be found!",
 						"Warning", JOptionPane.WARNING_MESSAGE);
 				if (uiPipe == null) {
 					GUI.frame.toFront();  // $$$$$$ Mar 17
 				}
 			}
 		}
 	}
 
 	// $$$$$$ Added for the "Retrieve Default Data" menu.  Feb 18
 	private void retrieveDefaultData() {
 		// $$$$$$ Two fashions for retrieving default data:
 		// $$$$$$ The first fashion for retrieving default data -- using the file default_data_(reserved).xml if one is provided.  Feb 11
 		String defaultData = DEFAULT_DATA_FILE_NAME + XML_FILE_SUFFIX; // $$$$$$ Feb 21
 
 		File df = new File(defaultData);  // $$$$$$ default_data_(reserved).xml   Feb 11
 		boolean isTheFirstFashion = false;
 		if (df.exists() != false) {
 			if (df.canWrite() != false) { // $$$$$$ added on Feb 21
 				df.setReadOnly();
 			}
 			isTheFirstFashion = true;
 		}
 
 		String tempDefaultData = DEFAULT_DATA_FILE_NAME + TEMPORARY_FILE_SUFFIX;
 		File tdf = new File(tempDefaultData);  // $$$$$$ temporary file default_data_(reserved).temp   Feb 11
 		tdf.deleteOnExit();
 
 		if (isTheFirstFashion != false) { // $$$$$$ Use the first fashion.  Feb 11
 			// $$$$$$ Copy default_data_(reserved).xml to the temporary file.  Feb 11
 			try {
 				copyFile(defaultData, tempDefaultData);
 			} catch (Throwable te) {
 				isTheFirstFashion = false;
 			}
 		}
 
 		if (isTheFirstFashion == false) { // $$$$$$ Use the second (stable) fashion as backup.  Feb 11
 			if (tdf.exists() != false) { // $$$$$$ added on Feb 21
 				tdf.delete(); // delete the potential default_data file created by last time pressing "Retrieve Default Data" menu.  Feb 8
 			}
 
 			// $$$$$$ set default of GA.  Feb 1
 	 		GeneticCode.meiosis_mode_index = GeneticCode.DEFAULT_MEIOSIS_MODE_INDEX;
 	 		String[][] DEFAULT_GENETICS = {
 					{ "Agent Type 1", "00011110", "00011110", "00011110" },
 					{ "Agent Type 2", "00011110", "00011110", "00011110" },
 					{ "Agent Type 3", "00011110", "00011110", "00011110" },
 					{ "Agent Type 4", "00011110", "00011110", "00011110" },
 					{ "Linked Phenotype", "None", "None", "None" } };
 			GUI.genetic_table = new JTable(DEFAULT_GENETICS, GUI.genetic_table_col_names);
 		}
 
 		// $$$$$$ Modified on Mar 14
 	 	GUI.createAndShowGUI(CA, tempDefaultData);
 		if (uiPipe == null) {setCurrentFile(tempDefaultData);}  // $$$$$$ added on Mar 14
  		// $$$$$$ Modified on Feb 28
 		/*if (uiPipe != null) {
 			uiPipe.reset();
 			refreshAll(uiPipe);
 		}*/
 	}
 
 	// $$$$$$ Implement the "Set Default Data" menu, using the default_data_(reserved).xml file.  Feb 21
 	private void setDefaultData() {
 		String defaultData = DEFAULT_DATA_FILE_NAME + XML_FILE_SUFFIX;
 		// $$$$$ prepare the file default_data_(reserved).xml to be writable
 		File df = new File(defaultData);
 		if (df.isHidden() != false) {
 			JOptionPane.showMessageDialog(this,   // $$$$$$ change from "this" to "GUI.frame" specifically for MS Windows.  Feb 22.  Change back on Mar 17
 					"Cannot set default data:  file \"" + defaultData + "\" is hidden.",
 					"Warning", JOptionPane.WARNING_MESSAGE);
 			if (uiPipe == null) {
 				GUI.frame.toFront();  // $$$$$$ Mar 17
 			}
 			return;
 		}
 		if ( (df.exists() != false) && (df.canWrite() == false) ) {
 			df.setWritable(true);
 		}
 
 		if ( (df.exists() == false) || (df.canWrite() == true) ) {
 			FileDialog setDialog = new FileDialog(GUI.frame,   // $$$$$$ modified from "this".  Feb 29
 					"Set Default Data", FileDialog.LOAD);
 			setDialog.setVisible(true);
 
 			// $$$$$$ The following codes modified on Feb 22
 			if (setDialog.getFile() != null) {
 				String directory = setDialog.getDirectory();
 				String file = setDialog.getFile();
 				String chosenFile = directory + file;
 				// $$$$$$ Modified on Mar 13
 				File f = new File(chosenFile);
 				if (f.exists() != false) {
 					try {
 						copyFile(chosenFile, defaultData);
 						//df.setReadOnly(); // $$$$$$ disallow write again
 					} catch (Throwable te) {
 						JOptionPane.showMessageDialog(setDialog,
 								"Fail to set default data!\n" +
 								"\nPossible cause(s): " + te.getMessage(),
 								"Warning", JOptionPane.WARNING_MESSAGE);
 
 						/*** $$$$$$ Cancel textWindow  Apr 22*/
 						if (cobweb.globals.usingTextWindow == true) {
 							textArea.append("Set default data failed: " + te.getMessage());
 						}
 
 						//df.delete(); // $$$$$ do not need to keep the file default_data_(reserved).xml any more
 					}
 				} else {
 					if (uiPipe != null && GUI.frame.isVisible() == true) {
 						GUI.frame.dispose(); // $$$$$ for allowing only one "Test Data" window to show up.  Feb 28
 					}
 					JOptionPane.showMessageDialog(this,
 							"File \" " + chosenFile + "\" could not be found!",
 							"Warning", JOptionPane.WARNING_MESSAGE);
 					if (uiPipe == null) {
 						GUI.frame.toFront();  // $$$$$$ Mar 17
 					}
 				}
 			}
 
 		} else { // $$$$$ write permission failed to set
 			JOptionPane.showMessageDialog(this,   // $$$$$$ change from "this" to "GUI.frame" specifically for MS Windows.  Feb 22
 					"Fail to set default data!\n" +
 					"\nPossible cause(s): Permission for the current folder may not be attained.",
 					"Warning", JOptionPane.WARNING_MESSAGE);
 
 			/*** $$$$$$ Cancel textWindow  Apr 22*/
 			if (cobweb.globals.usingTextWindow == true) {
 				textArea.append("Set default data failed: Permission for the current folder may not be attained.");
 			}
 
 			if (uiPipe == null) {
 				GUI.frame.toFront();  // $$$$$$ Mar 17
 				//df.delete();// $$$$$ do not need to keep the file default_data_(reserved).xml any more
 			}
 		}
 		// $$$$$$ Disallow write again to make sure the default data file would not be modified by outer calling.  Feb 22
 		if (df.canWrite() != false) {
 			df.setReadOnly();
 		}
 	}
 
     // $$$$$$ Implement "create New Data".  Mar 14
 	public void createNewData() {
 		// $$$$$  a file named as the below name will be automatically created or modified when everytime running the
 		// $$$$$    following code.  Please refer to GUI.GUI.close.addActionListener, "/* write UI info to xml file */".  Jan 24
 		String newInput = INITIAL_OR_NEW_INPUT_FILE_NAME + XML_FILE_SUFFIX;  // $$$$$$ added for implementing "Modify Current Data".  Feb 12
 		GUI.createAndShowGUI(CA, newInput);  //  $$$$$$ change the name from original "input.xml".  Jan 31
 		if (uiPipe == null) {setCurrentFile(newInput);}  // $$$$$$ added on Mar 14
 		// $$$$$$ Added to check if the new data file is hidden.  Feb 22
 		File inf = new File(newInput);
 		if ( inf.isHidden() != false  || ((inf.exists() != false) && (inf.canWrite() == false))) {
 			JOptionPane.showMessageDialog(GUI.frame,   // $$$$$$ change from "this" to "GUI.frame" specifically for MS Windows.  Feb 22
 					"Caution:  The new data file \"" + newInput + "\" is NOT allowed to be modified.\n" +
 					"\n                  Any modification of this data file will be neither implemented nor saved.");
 		}
 		/* // resets the counter to 0. There is no other work needing to be
 		// done in the UI because
 		// the UI comprises of the interface. The simulation itself will
 		// be reloaded.
  		// $$$$$$ Modified on Feb 28
 		if (uiPipe != null) {
 			uiPipe.reset();
 			refreshAll(uiPipe);
 		}*/
 	}
 
 	public void openCurrentFile() {  // $$$$$ "Modify This File" method
 		// $$$$$  a file named as the below name will be automatically created or modified when everytime running the
 		//          following code.  Please refer to GUI.GUI.close.addActionListener, "/* write UI info to xml file */".  Jan 24
 		// $$$$$$ modify a file previously accessed by "Modify Current Data".  Mar 18
 		if (getCurrentFile().equals(CURRENT_DATA_FILE_NAME + TEMPORARY_FILE_SUFFIX)) {
 			try {
 				copyFile(getCurrentFile(), midFile);
 			} catch (Throwable te) {
 				// $$$$$$ added on Feb 21
 				JOptionPane.showMessageDialog(this,  // $$$$$$ modified from "this".  Feb 29
 						"Modify this file failed: " + te.getMessage(),
 						"Warning", JOptionPane.WARNING_MESSAGE);
 				if (cobweb.globals.usingTextWindow == true) {textArea.append("Modify this file failed: " + te.getMessage());} /*** $$$$$$ Cancel textWindow  Apr 22*/
 			}
 			setCurrentFile(midFile);
 		}
 
 		GUI.createAndShowGUI(CA, getCurrentFile());  // $$$$$$ modified on Mar 14
 
 		// $$$$$$ Added on Mar 25
 		if (getCurrentFile().equals(DEFAULT_DATA_FILE_NAME + TEMPORARY_FILE_SUFFIX)) {
 			if (modifyingDefaultDataReminder == 0) {
 				// $$$$$ Ask if need to remind again.  Mar 25
 				Object[] options = {"Yes, please",
 									"No, thanks"};
 				int n = JOptionPane.showOptionDialog(GUI.frame,
 													 "Default data would not be affected by \"Modify This File\" menu.\n" +
 													 "\nTo set up new default data, please use \"Set Default Data\" menu instead.\n" +
 													 "\n\nWould you like to be reminded next time?",
 													 "Modifying Default Data Reminder",
 													 JOptionPane.YES_NO_OPTION,
 													 JOptionPane.QUESTION_MESSAGE,
 													 null,     //do not use a custom Icon
 													 options,  //the titles of buttons
 													 options[0]); //default button titl
 
 				modifyingDefaultDataReminder = n;
 			}
 		}
 	}
 
 	// $$$$$$ Added for "Modify Current Data" menu. This method modifies only the data, but NOT the input file.  Feb 12
 	private void openCurrentData() {
 		String currentData = CURRENT_DATA_FILE_NAME + TEMPORARY_FILE_SUFFIX;
 		File cf = new File(currentData);
 		cf.deleteOnExit();
 		// $$$$$$ Implement a medium file for modification.   Feb 12
 		midFile = getCurrentFile();  // $$$$$$ added on Mar 14
 		if (midFile.equals(currentData) == false) { // $$$$$ if not accessed by choosing "Modify Current Data" menu
 			try {
 				copyFile(midFile, currentData);
 			} catch (Throwable te) {
 				// $$$$$$ added on Feb 21
 				JOptionPane.showMessageDialog(this,  // $$$$$$ modified from "this".  Feb 29
 						"Modify current data failed: " + te.getMessage(),
 						"Warning", JOptionPane.WARNING_MESSAGE);
 				if (cobweb.globals.usingTextWindow == true) {textArea.append("Modify current data failed: " + te.getMessage());} /*** $$$$$$ Cancel textWindow  Apr 22*/
 			}
 		}
 		GUI.createAndShowGUI(CA, currentData);
 	}
 
 
 	public void saveFileDialog() {
 		FileDialog theDialog = new FileDialog(GUI.frame,    // $$$$$$ modified from "this".  Feb 29
 				"Choose a file to save state to", FileDialog.SAVE);
 		theDialog.setVisible(true);
 		//String savingFileName = "";
 		if (theDialog.getFile() != null) {
 			// $$$$$$ Check if the saving filename is one of the names reserved by CobwebApplication.  Feb 22
 			//String savingFileName;
 			//savingFileName = theDialog.getFile();
 
 			/* Block silenced by Andy because he finds it annoying not being able to modify the default input. */
 
 			/*
 			if ( (savingFileName.contains(INITIAL_OR_NEW_INPUT_FILE_NAME) != false)
 					  || (savingFileName.contains(CURRENT_DATA_FILE_NAME) != false)
 					  || (savingFileName.contains(DEFAULT_DATA_FILE_NAME) != false)) {
 				JOptionPane.showMessageDialog(GUI.frame,
 				"Save State: The filename\"" + savingFileName + "\" is reserved by Cobweb Application.\n" +
 						"                       Please choose another file to save.",
 						"Warning", JOptionPane.WARNING_MESSAGE);
 				saveFileDialog();
 			} else {  // $$$$$ If filename not reserved.  Feb 22*/
 				saveFile(theDialog.getDirectory() + theDialog.getFile());
 			//}
 		}
 	}
 
 	public void logFileDialog() {
 		FileDialog theDialog = new FileDialog(this,    // $$$$$$ modified from "this".  Feb 29
 				"Choose a file to save log to", FileDialog.SAVE);
 		theDialog.setVisible(true);
 		if (theDialog.getFile() != null) {
 			logFile(theDialog.getDirectory() + theDialog.getFile());
 		}
 	}
 
 	public void reportDialog() {
 		FileDialog theDialog = new FileDialog(this,    // $$$$$$ modified from "this".  Feb 29
 				"Choose a file to save report to", FileDialog.SAVE);
 		theDialog.setVisible(true);
 		if (theDialog.getFile() != null) {
 			reportFile(theDialog.getDirectory() + theDialog.getFile());
 		}
 	}
 	/*
 	// $$$$$$ Modified on Feb 22
 	public void trackAgentFileDialog() {
 		boolean isTrackAgentUsed = false; // $$$$$$ added on Feb 22
 		if (isTrackAgentUsed == true) {
 			FileDialog theDialog = new FileDialog(GUI.frame,    // $$$$$$ modified from "this".  Feb 29
 					"Choose a file to save Track Agent report to",
 					FileDialog.SAVE);
 			theDialog.setVisible(true);
 			if (theDialog.getFile() != null)
 				trackAgentFile(theDialog.getDirectory() + theDialog.getFile());
 		// $$$$$$ The following added on Feb 22
 		} else {
 			JOptionPane.showMessageDialog(GUI.frame,
 					"Track Agent is disabled for now!  Please use the logging function instead."); // $$$$$$ added on Feb 22
 			// $$$$$$ Modified from "uiPipe.writeToTextWindow("Track Agent is disabled for now! Please use the logging function instead.\n");"   Feb 28
 			if (uiPipe != null) uiPipe.writeToTextWindow("Track Agent is disabled for now! Please use the logging function instead.\n");
 		}
 	}
 	*/
 	public void aboutDialog() {
 		final javax.swing.JDialog whatDialog = new javax.swing.JDialog(GUI.frame,  // $$$$$$ change from Dialog mult.  Feb 18
 				"About Cobweb", true);								   // $$$$$$ change from "this" to "GUI.frame" specifically for MS Windows.  Feb 22
 		whatDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);  // $$$$$$ added on Feb 18
 		JPanel info = new JPanel();
 		info.add(new JLabel("Cobweb 2003/2004"));
 		info.add(new JLabel(""));
 		info.add(new JLabel("is a product of"));
 		info.add(new JLabel("Environment Canada"));
 
 		JPanel term = new JPanel();
 		JButton close = new JButton("Close");
 		term.add(close);
 
 		close.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				whatDialog.setVisible(false);
 			}
 		});
 
 		whatDialog.add(info, "Center");
 		whatDialog.add(term, "South");
 		whatDialog.setSize(200, 150);
 		whatDialog.setVisible(true);
 	}
 
 	public void creditsDialog() {
 		final javax.swing.JDialog theDialog = new javax.swing.JDialog(GUI.frame, "Credits",  // $$$$$$ change from Dialog mult.  Feb 18
 				true);								  				  // $$$$$$ change from "this" to "GUI.frame" specifically for MS Windows.  Feb 22
 		theDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);  // $$$$$$ added on Feb 18
 		@SuppressWarnings("unused")
 		JPanel info = new JPanel();
 		JPanel credit = new JPanel();
 		JButton brad = new JButton("Brad Bass, PhD");
 		JButton jeff = new JButton("Jeff Hill");
 		JButton jin = new JButton("Jin Soo Kang");
 		credit.add(new JLabel("Coordinator"));
 		credit.add(brad);
 		credit.add(new JLabel("_______________"));
 		credit.add(new JLabel("Programmers"));
 		credit.add(jeff);
 		credit.add(jin);
 
 		JPanel term = new JPanel();
 		JButton close = new JButton("Close");
 		term.add(close);
 
 		brad.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				String[] S = { "Brad Bass, PhD",
 						"Adaptations and Impacts Research Group",
 						"Environment Canada at Univ of Toronto",
 						"Inst. for Environmental Studies",
 						"33 Willcocks Street", "Toronto, Ont M5S 3E8 CANADA",
 						"TEL: (416) 978-6285  FAX: (416) 978-3884",
 						"brad.bass@ec.gc.ca" };
 
 				CobwebApplication.this.creditDialog(theDialog, S, 300, 300); // $$$$$$  change from "this" to parentDialog.  Feb 22
 			}
 		});
 
 		jeff.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				String[] S = { "Main Structural Programming By", "",
 						"Jeff Hill", "oni1@home.com" };
 
 				CobwebApplication.this.creditDialog(theDialog, S, 250, 150); // $$$$$$  change from "this" to parentDialog.  Feb 22
 			}
 		});
 
 		jin.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				String[] S = { "Update & Additional Programming By", "",
 						"Jin Soo Kang", "Undergraduate, Computer Science",
 						"University of Toronto", "jin.kang@utoronto.ca",
 						"[2000 - 2001]" };
 
 				CobwebApplication.this.creditDialog(theDialog, S, 300, 250); // $$$$$$  change from "this" to parentDialog.  Feb 22
 			}
 		});
 
 		close.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				theDialog.setVisible(false);
 			}
 		});
 
 		theDialog.add(credit, "Center");
 		theDialog.add(term, "South");
 		theDialog.setSize(150, 265);
 		theDialog.setVisible(true);
 
 	}
 
 	private void creditDialog(JDialog parentDialog, String[] S, int length, int width) { // $$$$$$  modified on Feb 22
 
 		final javax.swing.JDialog creditDialog = new javax.swing.JDialog(parentDialog,  // $$$$$$  change from "this" to parentDialog.  Feb 22
 				"Click on Close to continue", true);
 
 		JPanel credit = new JPanel();
 		for (int i = 0; i < S.length; ++i) {
 			credit.add(new JLabel(S[i]), "Center");
 		}
 
 		JPanel term = new JPanel();
 		/* new */
 		// $$$$$$ Silence the unused "Open" button.  Feb 22
 		//Button choosefile = new Button("Open");
 		//term.add(choosefile);
 		JButton close = new JButton("Close");
 		term.add(close);
 		/* new */
 		//choosefile.addActionListener(new event.ActionListener() {
 		//	public void actionPerformed(event.ActionEvent evt) { /* openFileDialog() ; */
 		//	}
 		//});
 		close.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				creditDialog.setVisible(false);
 			}
 		});
 
 		creditDialog.add(credit, "Center");
 		creditDialog.add(term, "South");
 
 		creditDialog.setSize(length, width);
 		creditDialog.setVisible(true);
 
 	}
 
 	private void setMultFilesDialog(int length, int width) {
 		prsNames = new Parser[maxfiles];
 		pauseAt = new int[maxfiles];
 		final javax.swing.JDialog mult = new javax.swing.JDialog(this,  // $$$$$$ change from Dialog mult.  Feb 14
 				"Multiple File Setting", false);				 // $$$$$$ change from "this" to "GUI.frame" specifically for MS Windows.  Feb 22
 		mult.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);  // $$$$$$ added on Feb 14
 		final JTextField jtf = new JTextField(20);
 		final JTextField num_ticks = new JTextField(6);
 		num_ticks.setText("100");
 		final JTextArea fnames = new JTextArea(4, 35);
 		fnames.setEditable(false);
 
 		JButton b0 = new JButton("Browse");
 		JButton b1 = new JButton("Run");
 		JButton b2 = new JButton("Add File");
 		JButton b3 = new JButton("Cancel");
 
 		JPanel p2 = new JPanel();
 		p2.setBorder(BorderFactory.createTitledBorder(BorderFactory
 				.createLineBorder(Color.gray), "File Name"));
 		p2.setLayout(new BorderLayout());
 		p2.add(jtf, BorderLayout.CENTER);
 		p2.add(b0, BorderLayout.EAST);
 
 		JPanel p3 = new JPanel();
 		p3.setBorder(BorderFactory.createTitledBorder(BorderFactory
 				.createLineBorder(Color.gray), "Number of Steps"));
 		p3.setLayout(new BorderLayout());
 		p3.add(new JLabel("Run this file for"), BorderLayout.CENTER);
 		p3.add(num_ticks, BorderLayout.EAST);
 
 		JPanel p4 = new JPanel();
 		p4.setLayout(new BorderLayout());
 		p4.add(b3, BorderLayout.WEST);
 		p4.add(b2, BorderLayout.CENTER);
 		p4.add(b1, BorderLayout.EAST);
 
 		JPanel p1 = new JPanel();
 		// p1.setLayout(new GridLayout(7, 1))
 		p1.add(p2);
 		p1.add(p3);
 		p1.add(new JLabel(" "));
 		p1.add("South", fnames);
 		p1.add(new JLabel("                             "));
 		p1.add(p4);
 
 		b0.addActionListener(new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser chooser = new JFileChooser();
 
 				String filename = jtf.getText();
 				if (filename != null) {
 					File thefile = new File(filename);
 					if (thefile.isDirectory()) {
 						chooser.setCurrentDirectory(thefile);
 					} else {
 						chooser.setSelectedFile(thefile);
 					}
 				}
 
 				// set the title of the FileDialog
 				StringBuffer sb = new StringBuffer("Select File");
 				chooser.setDialogTitle(sb.toString());
 
 				// get the file
 				String fn;
 				int retVal = chooser.showOpenDialog(null);
 				if (retVal == JFileChooser.APPROVE_OPTION) {
 					fn = chooser.getSelectedFile().getAbsolutePath();
 				} else {
 					return;
 				}
 
 				if (fn != null) {
 					// display the path to the chosen file
 					jtf.setText(fn);
 				}
 			}
 
 			public static final long serialVersionUID = 0x4DCEE6AA76B8E16DL;
 		});
 
 		b1.addActionListener(new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				// $$$$$$ The following codes were modified on Feb 18
 				boolean canRun = true;
 				String filename = jtf.getText();
 				if (filename != null && filename.compareTo("") != 0) {
 					if (getFileCount() < maxfiles) {
 						// $$$$$$ Ask if need to add this file to the multiply files list.  Feb 25
 						Object[] options = {"Yes, please",
 											"No, thanks"};
 						int n = JOptionPane.showOptionDialog(mult,
 															 "Do you want to add the file \"" + filename +
 															 "\" to the list?",
 															 "Multiple File Running",
 															 JOptionPane.YES_NO_OPTION,
 															 JOptionPane.QUESTION_MESSAGE,
 															 null,     //do not use a custom Icon
 															 options,  //the titles of buttons
 															 options[0]); //default button titl
 
 						/* $$$$$ or	default icon, custom title
 						int n = JOptionPane.showConfirmDialog(
 						    		mult,
 									"Do you want to add the file \"" + filename +
 									"\" to the list?",
 									"Multiple File Running",
 						    		JOptionPane.YES_NO_OPTION);
 						*/
 
 						if (n == 0) {
 							int status = 0;
 							try {
 								status = addfileNames(filename, (new Integer(num_ticks
 										.getText())));
 							} catch (NumberFormatException ex) {
 								// TODO Auto-generated catch block
 								ex.printStackTrace();
 							} catch (FileNotFoundException ex) {
 								// TODO Auto-generated catch block
 								ex.printStackTrace();
 							}
 							if (status == 1) {
 								//canRun = true;
 								midFile = filename;  // $$$$$$ added for supporting "Modify Current Data".  Feb 14
 							// $$$$$$ added on Feb 18
 							} else if (status == -2) {
 								canRun = false;
 								JOptionPane.showMessageDialog(mult,
 										"File \" " + filename + "\" could not be found!",
 										"Warning", JOptionPane.WARNING_MESSAGE);
 							// $$$$$ The following "Invalid filename" check is invalid.  Feb 18
 							} else if (status == 0) {
 								canRun = false;
 								JOptionPane.showMessageDialog(mult,
 										"Invalid filename: \"" + filename + "\" !",
 										"Warning", JOptionPane.WARNING_MESSAGE);
 							} else if (status == -1) {
 								canRun = false;
 								JOptionPane.showMessageDialog(mult,
 										"Invalid input!",
 										"Warning", JOptionPane.WARNING_MESSAGE);
 							}
 						}
 
 						/* $$$$$ The old way, without asking if adding the file to the list
 						int status = addfileNames(filename, (new Integer(num_ticks
 								.getText())));
 						if (status == 1) {
 							//canRun = true;
 							midFile = filename;  // $$$$$$ added for supporting "Modify Current Data".  Feb 14
 						// $$$$$$ added on Feb 18
 						} else if (status == -2) {
 							canRun = false;
 							JOptionPane.showMessageDialog(mult,
 									"File \" " + filename + "\" could not be found!",
 									"Warning", JOptionPane.WARNING_MESSAGE);
 						// $$$$$ The following "Invalid filename" check is invalid.  Feb 18
 						} else if (status == 0) {
 							canRun = false;
 							JOptionPane.showMessageDialog(mult,
 									"Invalid filename: \"" + filename + "\" !",
 									"Warning", JOptionPane.WARNING_MESSAGE);
 						} else if (status == -1) {
 							canRun = false;
 							JOptionPane.showMessageDialog(mult,
 									"Invalid input!",
 									"Warning", JOptionPane.WARNING_MESSAGE);
 						}
 						*/
 
 					} else {
 						canRun = false;
 						JOptionPane.showMessageDialog(mult,
 								"You can NOT add more than " + maxfiles + " files!",
 								"Warning", JOptionPane.WARNING_MESSAGE);
 					}
 				}
 
 				if (canRun == false) {
 					jtf.setText("");
 				} else {
 					if (getFileCount() != 0) {
 						if (GUI.frame.isVisible() == true) {GUI.frame.dispose();}  // $$$$$ for allowing only one "Test Data" window to show up
 						//if (tickField != null && !tickField.getText().equals("")) {tickField.setText("");}  // $$$$$$ reset tickField.  Mar 14
 						openMultFiles(prsNames, pauseAt, filecount);
 						setFileCount(0);  // $$$$$$ reset filecount. Now "Set Multiple Files" menu can work more than once.  Feb 18
 						setCurrentFile(midFile);  // $$$$$$ added for supporting "Modify Current Data".  Feb 18  &&&&&& modified on Mar 14
 						mult.setVisible(false);
 						mult.dispose();  // $$$$$$ added on Feb 14
 						} else {
 							JOptionPane.showMessageDialog(mult,
 									"Please enter a filename.");
 						}
 				}
 			}
 			public static final long serialVersionUID = 0xB69B9EE3DA1EAE11L;
 		});
 
 		b2.addActionListener(new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				// $$$$$$ The following codes were modified on Feb 18
 				String filename = jtf.getText();
 				if (filename != null && filename.compareTo("") != 0) {
 					if (getFileCount() < maxfiles) {
 						int status = 0;
 						try {
 							status = addfileNames(filename, (new Integer(num_ticks
 									.getText())));
 						} catch (NumberFormatException ex) {
 							// TODO Auto-generated catch block
 							ex.printStackTrace();
 						} catch (FileNotFoundException ex) {
 							// TODO Auto-generated catch block
 							ex.printStackTrace();
 						}
 						if (status == 1) {
 							fnames.append("file added: " + filename + " ");
 							fnames.append(new Integer(num_ticks.getText())
 									+ " steps" + newline);
 							midFile = filename;  // $$$$$$ added for supporting "Modify Current Data".  Feb 14
 						// $$$$$$ added on Feb 18
 						} else if (status == -2) {
 							JOptionPane.showMessageDialog(mult,
 									"File \" " + filename + "\" could not be found!",
 									"Warning", JOptionPane.WARNING_MESSAGE);
 						// $$$$$ The following "Invalid filename" check is invalid.  Feb 18
 						} else if (status == 0) {
 							JOptionPane.showMessageDialog(mult,
 									"Invalid filename: \"" + filename + "\" !",
 									"Warning", JOptionPane.WARNING_MESSAGE);
 						} else if (status == -1) {
 							JOptionPane.showMessageDialog(mult,
 									"Invalid input!",
 									"Warning", JOptionPane.WARNING_MESSAGE);
 						}
 					} else {
 						JOptionPane.showMessageDialog(mult,
 								"You can NOT add more than " + maxfiles + " files!",
 								"Warning", JOptionPane.WARNING_MESSAGE);
 					}
 				}
 
 				jtf.setText("");
 			}
 			public static final long serialVersionUID = 0x16124B6CEDFF67E9L;
 		});
 
 		b3.addActionListener(new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				mult.setVisible(false);
 				mult.dispose();  // $$$$$$ added on Feb 14
 				if (uiPipe != null) {
 					CobwebApplication.this.toFront(); // $$$$$$ added for CA frame going to front when cancelling.  Feb 22; Modified on Feb 28
 				}
 			}
 			public static final long serialVersionUID = 0xEAE8EA9DF8593309L;
 		});
 
 		mult.add(p1, "Center");
 		mult.setSize(length, width);
 		mult.setVisible(true);
 
 	}
 
 	public int addfileNames(String filename, Integer step) throws FileNotFoundException {
 		// $$$$$$ added on Feb 18
 		File f = new File(filename);
 		if (f.exists() == false) {
 			return -2;
 		}
 
 		Parser prsfile = new Parser(filename);
 		if (step.intValue() >= 0) {
 			prsNames[filecount] = prsfile;
 			pauseAt[filecount] = step.intValue();
 			filecount++;
 			return 1;
 		}
 		return -1;
 	}
 
 	public int getFileCount() {  // changed from filecount().  Feb 18
 		return filecount;
 	}
 
 	// $$$$$$ filecount modifier.  Feb 18
 	public void setFileCount(int c) {
 		filecount = c;
 	}
 
 	public void printfilenames() {
 		for (int i = 0; i < filecount; i++) {
 			System.out.println(fileNames[i]);
 			System.out.println(pauseAt[i]);
 			System.out.println();
 		}
 	}
 
 	/* NEWWW */
 	private class Mouse extends MouseAdapter {
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			if (mode != 0) {
 				convertCoords(e.getX(), e.getY());  // $$$$$$ no need for observation mode.  Mar 28
 			}
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			convertCoords(e.getX(), e.getY());
 		}
 
 		int storedX = -1;
 
 		int storedY = -1;
 
 		long storedTick = -1;
 
 		public void convertCoords(int x, int y) {
 			int realX = -1;
 			int realY = -1;
 			long realTick = uiPipe.getTime();
 			{
 				if (
 				/* Check the x-axis bounds */
 				x >= displayPanel.getBorderWidth()
 						&& x < (displayPanel.getTileW() * displayPanel
 								.getWidthInTiles())
 								+ displayPanel.getBorderWidth()
 						&&
 						/* Check the y-axis bounds */
 						y >= displayPanel.getBorderHeight()
 						&& y < (displayPanel.getTileH() * displayPanel
 										.getHeightInTiles())
 								+ displayPanel.getBorderHeight()) {
 					realX = (x - displayPanel.getBorderWidth())
 							/ displayPanel.getTileW();
 					realY = (y - displayPanel.getBorderHeight())
 							/ displayPanel.getTileH();
 					// Avoid flickering
 					if (storedX != realX || storedY != realY
 							|| storedTick != realTick) {
 						uiPipe.updateclick(realX, realY, mode, type);
 					}
 					// Update
 					storedX = realX;
 					storedY = realY;
 					storedTick = realTick;
 				}
 			}
 		}
 	} // Mouse
 
 	// $$$$$$ A file copy method.  Feb 11
 	public static void copyFile(String src, String dest)  throws IOException {
 		FileReader in = new FileReader(src);
 		FileWriter out = new FileWriter(dest);
 		int c;
 
 		while ((c = in.read()) != -1) {
 			out.write(c);
 		}
 
 		in.close();
 		out.close();
 	}
 	public boolean isClipped() {
 		return clipped;
 	}
 
 } // CobwebApplication
