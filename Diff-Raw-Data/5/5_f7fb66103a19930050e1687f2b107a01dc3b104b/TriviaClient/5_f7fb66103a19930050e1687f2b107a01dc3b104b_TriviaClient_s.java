 package net.bubbaland.trivia.client;
 
 // imports for GUI
 import java.awt.BorderLayout;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.Rectangle;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 // imports for RMI
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.Box;
 import javax.swing.ButtonGroup;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 import net.bubbaland.trivia.Round;
 import net.bubbaland.trivia.Trivia;
 import net.bubbaland.trivia.TriviaInterface;
 import net.bubbaland.trivia.UserList.Role;
 
 /**
  * Provides the root functionality for connecting to the trivia server and creating the associated GUI.
  * 
  * @author Walter Kolczynski
  * 
  */
 public class TriviaClient extends TriviaPanel implements ActionListener, WindowListener {
 
 	// The Constant serialVersionUID.
 	private static final long		serialVersionUID	= 5464403297756091690L;
 
 	// The refresh rate of the GUI elements (in milliseconds)
 	final private static int		REFRESH_RATE		= 500;
 	// The maximum number of retries the client will make when failing in communication with the server
 	protected static final int		MAX_RETRIES			= 10;
 	// The amount of time (in seconds) a user is considered "active"
 	final static private int		USER_LIST_WINDOW	= 10 * 60;
 	// The amount of time (in seconds) a user is considered disconnected
 	final static private int		USER_LIST_TIMEOUT	= 90;
 
 	// Height of the status bar at the bottom of the GUI
 	final static private int		STATUS_HEIGHT		= 14;
 	// Font size of the status text
 	final static private float		STATUS_FONT_SIZE	= 12.0f;
 
 	// URL for RMI server
 	final static private String		TRIVIA_SERVER_URL	= "rmi://www.bubbaland.net:1099/TriviaInterface";
 	// URL for Wiki
 	final static private String		WIKI_URL			= "https://sites.google.com/a/kneedeepintheses.org/information/Home";
 	// URL for the IRC client
 	final static private String		IRC_CLIENT_URL		= "http://webchat.freenode.net/";
 	// IRC channel to join on connection to IRC server
 	final static private String		IRC_CHANNEL			= "%23kneedeeptrivia";
 	// File name to store window positions
 	final static private String		SETTINGS_FILENAME	= ".trivia-settings";
 
 	// Initial tabs
 	final static private String[]	initialTabs			= { "Workflow", "Current", "History" };
 
 	// Queue sort option
 	public static enum QueueSort {
 		TIMESTAMP_ASCENDING, QNUMBER_ASCENDING, STATUS_ASCENDING, TIMESTAMP_DESCENDING, QNUMBER_DESCENDING, STATUS_DESCENDING
 	}
 
 	/**
 	 * GUI Components
 	 */
 	// Root frame for the application
 	final private JFrame							frame;
 	// The tabbed pane
 	final private DnDTabbedPane						book;
 	// The status bar at the bottom
 	final private JLabel							statusBar;
 
 	// final private Hashtable<String, TriviaPanel> tabHash;
 	final private ArrayList<TriviaPanel>			panelList;
 	final static private Hashtable<String, String>	tabDescriptionHash;
 	static {
 		tabDescriptionHash = new Hashtable<String, String>(0);
 		tabDescriptionHash.put("Workflow", "Main tab with summary information, open question list, and answer queue.");
 		tabDescriptionHash.put("Current", "Tab showing question data for the current round.");
 		tabDescriptionHash.put("History", "Tab that can show question data for any round.");
 		tabDescriptionHash.put("By Round", "Tab that displays score information for every round.");
 		tabDescriptionHash.put("Place Chart", "Chart showing the team's place in time");
 		tabDescriptionHash.put("Score Chart", "Chart showing the team's score in each round.");
 		tabDescriptionHash.put("Cumul. Score Chart", "Chart showing the team's total score in time.");
 		tabDescriptionHash.put("Team Comparison", "Chart comparing each team's score to our score in time.");
 	}
 
 
 	// The user's name
 	private volatile String							user;
 	// The Hide Closed menu item
 	private volatile boolean						hideClosed, hideDuplicates;
 	// Hashtable of active users and roles
 	private volatile Hashtable<String, Role>		activeUserHash;
 	// Hashtable of idle users and roles
 	private volatile Hashtable<String, Role>		passiveUserHash;
 	// Sort method for the queue
 	private volatile QueueSort						queueSort;
 
 	// Sort menu items
 	final private JMenuItem							hideClosedMenuItem;
 	final private JMenuItem							hideDuplicatesMenuItem;
 	final private JMenuItem							sortTimestampAscendingMenuItem;
 	final private JMenuItem							sortTimestampDescendingMenuItem;
 	final private JMenuItem							sortQNumberAscendingMenuItem;
 	final private JMenuItem							sortQNumberDescendingMenuItem;
 	final private JMenuItem							sortStatusAscendingMenuItem;
 	final private JMenuItem							sortStatusDescendingMenuItem;
 
 	// The local trivia object holding all contest data
 	private volatile Trivia							trivia;
 	// The remote server
 	private final TriviaInterface					server;
 
 	/**
 	 * Creates a new trivia client GUI
 	 * 
 	 * @param server
 	 *            The RMI Server
 	 */
 	private TriviaClient(JFrame parent, TriviaInterface server, boolean useFX) {
 
 		// Call parent constructor
 		super();
 		this.server = server;
 		this.frame = parent;
 
 		// Listen for the window close so we can save the size & position
 		this.frame.addWindowListener(this);
 
 		// Grab a copy of the current Trivia data structure from the server in the background
 		final TriviaFetcher fetcher = new TriviaFetcher(server, this);
 		fetcher.execute();
 
 		// Load the sort state from file
 		String loadedSort = loadProperty("queueSort");
 		if (loadedSort == null) {
 			loadedSort = "";
 		}
 		switch (loadedSort) {
 			case "Sort Timestamp Ascending":
 				this.queueSort = QueueSort.TIMESTAMP_ASCENDING;
 				break;
 			case "Sort Question Number Ascending":
 				this.queueSort = QueueSort.QNUMBER_ASCENDING;
 				break;
 			case "Sort Status Ascending":
 				this.queueSort = QueueSort.STATUS_ASCENDING;
 				break;
 			case "Sort Timestamp Descending":
 				this.queueSort = QueueSort.TIMESTAMP_DESCENDING;
 				break;
 			case "Sort Question Number Descending":
 				this.queueSort = QueueSort.QNUMBER_DESCENDING;
 				break;
 			case "Sort Status Descending":
 				this.queueSort = QueueSort.STATUS_DESCENDING;
 				break;
 			default:
 				this.queueSort = QueueSort.TIMESTAMP_ASCENDING;
 				break;
 		}
 
 		// Create a prompt requesting the user name
 		new UserLogin(this);
 
 		/**
 		 * Setup the menus
 		 */
 		final JMenuBar menuBar = new JMenuBar();
 
 		// Add the menu to the parent frame
 		parent.setJMenuBar(menuBar);
 
 		// Make Queue Menu
 		JMenu menu = new JMenu("Queue");
 		menu.setMnemonic(KeyEvent.VK_Q);
 		menuBar.add(menu);
 
		this.hideClosedMenuItem = new JCheckBoxMenuItem("Hide closed questions");
 		this.hideClosedMenuItem.setMnemonic(KeyEvent.VK_H);
 		this.hideClosedMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
 		String loadHideClosed = loadProperty("hideClosed");
 		if (loadHideClosed == null) {
 			loadHideClosed = "true";
 		}
 		if (loadHideClosed.equals("false")) {
 			this.hideClosedMenuItem.setSelected(false);
 			this.hideClosed = false;
 		} else {
 			this.hideClosedMenuItem.setSelected(true);
 			this.hideClosed = true;
 		}
 		this.hideClosedMenuItem.setActionCommand("Hide Closed");
 		this.hideClosedMenuItem.addActionListener(this);
 		menu.add(this.hideClosedMenuItem);
 
		this.hideDuplicatesMenuItem = new JCheckBoxMenuItem("Hide duplicate questions");
 		this.hideDuplicatesMenuItem.setMnemonic(KeyEvent.VK_D);
 		this.hideDuplicatesMenuItem.setDisplayedMnemonicIndex(5);
 		this.hideDuplicatesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
 		String loadedHideDuplicates = loadProperty("hideDuplicates");
 		if (loadedHideDuplicates == null) {
 			loadedHideDuplicates = "true";
 		}
 		if (loadHideClosed.equals("false")) {
 			this.hideDuplicatesMenuItem.setSelected(false);
 			this.hideDuplicates = false;
 		} else {
 			this.hideDuplicatesMenuItem.setSelected(true);
 			this.hideDuplicates = true;
 		}
 		this.hideDuplicatesMenuItem.setActionCommand("Hide Duplicates");
 		this.hideDuplicatesMenuItem.addActionListener(this);
 		menu.add(this.hideDuplicatesMenuItem);
 
 
 		final JMenu sortMenu = new JMenu("Sort by...");
 		sortMenu.setMnemonic(KeyEvent.VK_S);
 
 		final JMenu timestampSort = new JMenu("Timestamp");
 		timestampSort.setMnemonic(KeyEvent.VK_T);
 		sortMenu.add(timestampSort);
 
 		final ButtonGroup sortOptions = new ButtonGroup();
 		sortTimestampAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
 		sortTimestampAscendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
 		sortTimestampAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
 		sortTimestampAscendingMenuItem.setActionCommand("Sort Timestamp Ascending");
 		sortTimestampAscendingMenuItem.addActionListener(this);
 		if (this.queueSort == QueueSort.TIMESTAMP_ASCENDING) {
 			sortTimestampAscendingMenuItem.setSelected(true);
 		}
 		sortOptions.add(sortTimestampAscendingMenuItem);
 		timestampSort.add(sortTimestampAscendingMenuItem);
 
 		sortTimestampDescendingMenuItem = new JRadioButtonMenuItem("Descending");
 		sortTimestampDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK
 				+ ActionEvent.SHIFT_MASK));
 		sortTimestampDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
 		sortTimestampDescendingMenuItem.setActionCommand("Sort Timestamp Descending");
 		sortTimestampDescendingMenuItem.addActionListener(this);
 		if (this.queueSort == QueueSort.TIMESTAMP_DESCENDING) {
 			sortTimestampDescendingMenuItem.setSelected(true);
 		}
 		sortOptions.add(sortTimestampDescendingMenuItem);
 		timestampSort.add(sortTimestampDescendingMenuItem);
 
 		final JMenu qNumberSort = new JMenu("Question Number");
 		qNumberSort.setMnemonic(KeyEvent.VK_Q);
 		sortMenu.add(qNumberSort);
 
 		sortQNumberAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
 		sortQNumberAscendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 		sortQNumberAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
 		sortQNumberAscendingMenuItem.setActionCommand("Sort Question Number Ascending");
 		sortQNumberAscendingMenuItem.addActionListener(this);
 		if (this.queueSort == QueueSort.QNUMBER_ASCENDING) {
 			sortQNumberAscendingMenuItem.setSelected(true);
 		}
 		sortOptions.add(sortQNumberAscendingMenuItem);
 		qNumberSort.add(sortQNumberAscendingMenuItem);
 
 		sortQNumberDescendingMenuItem = new JRadioButtonMenuItem("Descending");
 		sortQNumberDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK
 				+ ActionEvent.SHIFT_MASK));
 		sortQNumberDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
 		sortQNumberDescendingMenuItem.setActionCommand("Sort Question Number Descending");
 		sortQNumberDescendingMenuItem.addActionListener(this);
 		if (this.queueSort == QueueSort.QNUMBER_DESCENDING) {
 			sortQNumberDescendingMenuItem.setSelected(true);
 		}
 		sortOptions.add(sortQNumberDescendingMenuItem);
 		qNumberSort.add(sortQNumberDescendingMenuItem);
 
 		final JMenu statusSort = new JMenu("Status");
 		statusSort.setMnemonic(KeyEvent.VK_S);
 		sortMenu.add(statusSort);
 
 		sortStatusAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
 		sortStatusAscendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 		sortStatusAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
 		sortStatusAscendingMenuItem.setActionCommand("Sort Status Ascending");
 		sortStatusAscendingMenuItem.addActionListener(this);
 		if (this.queueSort == QueueSort.STATUS_ASCENDING) {
 			sortStatusAscendingMenuItem.setSelected(true);
 		}
 		sortOptions.add(sortStatusAscendingMenuItem);
 		statusSort.add(sortStatusAscendingMenuItem);
 
 		sortStatusDescendingMenuItem = new JRadioButtonMenuItem("Descending");
 		sortStatusDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK
 				+ ActionEvent.SHIFT_MASK));
 		sortStatusDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
 		sortStatusDescendingMenuItem.setActionCommand("Sort Status Descending");
 		sortStatusDescendingMenuItem.addActionListener(this);
 		if (this.queueSort == QueueSort.STATUS_DESCENDING) {
 			sortStatusDescendingMenuItem.setSelected(true);
 		}
 		sortOptions.add(sortStatusDescendingMenuItem);
 		statusSort.add(sortStatusDescendingMenuItem);
 
 		menu.add(sortMenu);
 
 		// Make User Menu
 		menu = new JMenu("User");
 		menu.setMnemonic(KeyEvent.VK_U);
 		menuBar.add(menu);
 
 		final JMenu roleMenu = new JMenu("Change Role...");
 		roleMenu.setMnemonic(KeyEvent.VK_R);
 
 		final ButtonGroup roleOptions = new ButtonGroup();
 		JRadioButtonMenuItem roleOption = new JRadioButtonMenuItem("Researcher");
 		roleOption.setActionCommand("Researcher");
 		roleOption.setMnemonic(KeyEvent.VK_R);
 		roleOption.addActionListener(this);
 		roleOption.setSelected(true);
 		roleOption.setForeground(UserListPanel.RESEARCHER_COLOR);
 		roleOptions.add(roleOption);
 		roleMenu.add(roleOption);
 		this.setRole(Role.RESEARCHER);
 
 		roleOption = new JRadioButtonMenuItem("Caller");
 		roleOption.setActionCommand("Caller");
 		roleOption.setMnemonic(KeyEvent.VK_C);
 		roleOption.addActionListener(this);
 		roleOption.setSelected(false);
 		roleOption.setForeground(UserListPanel.CALLER_COLOR);
 		roleOptions.add(roleOption);
 		roleMenu.add(roleOption);
 
 		roleOption = new JRadioButtonMenuItem("Typist");
 		roleOption.setActionCommand("Typist");
 		roleOption.setMnemonic(KeyEvent.VK_T);
 		roleOption.addActionListener(this);
 		roleOption.setSelected(false);
 		roleOption.setForeground(UserListPanel.TYPIST_COLOR);
 		roleOptions.add(roleOption);
 		roleMenu.add(roleOption);
 
 		menu.add(roleMenu);
 
 		JMenuItem menuItem = new JMenuItem("Change Name", KeyEvent.VK_N);
 		menuItem.setDisplayedMnemonicIndex(7);
 		menuItem.setActionCommand("Change name");
 		menuItem.addActionListener(this);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Reset Window Positions");
 		menuItem.setActionCommand("Reset window positions");
 		menuItem.setMnemonic(KeyEvent.VK_W);
 		menuItem.addActionListener(this);
 		menu.add(menuItem);
 
 		// Make Info Menu
 		final JMenu infoMenu = new JMenu("Info");
 		infoMenu.setMnemonic(KeyEvent.VK_I);
 		menuItem = new JMenuItem("Open Wiki (broswer)", KeyEvent.VK_W);
 		menuItem.setActionCommand("Open wiki");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
 		menuItem.addActionListener(this);
 
 		infoMenu.add(menuItem);
 		menuBar.add(infoMenu);
 
 
 		// Make Admin Menu pinned to the right
 		menuBar.add(Box.createHorizontalGlue());
 		menu = new JMenu("Admin");
 		menu.setMnemonic(KeyEvent.VK_A);
 		menuBar.add(menu);
 
 		menuItem = new JMenuItem("Load State", KeyEvent.VK_L);
 		menuItem.setActionCommand("Load state");
 		menuItem.addActionListener(this);
 		menu.add(menuItem);
 
 		/**
 		 * Setup status bar at bottom
 		 */
 		// Set up layout constraints
 		final GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 
 		// Put the status bar at the bottom and do not adjust the size of the status bar
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 
 		// Create status bar
 		this.statusBar = this.enclosedLabel("", 0, STATUS_HEIGHT, this.getForeground(), this.getBackground(),
 				constraints, STATUS_FONT_SIZE, SwingConstants.LEFT, SwingConstants.CENTER);
 
 		/**
 		 * Create main content area
 		 */
 		// Create the tabbed pane
 		this.book = new DnDTabbedPane(this);
 
 		this.panelList = new ArrayList<TriviaPanel>(0);
 
 		for (String tabName : TriviaClient.initialTabs) {
 			this.book.addTab(tabName, this.getTab(tabName));
 		}
 		this.book.setSelectedIndex(this.book.indexOfTab(TriviaClient.initialTabs[0]));
 
 		// Put the split pane at the top of the window
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		// When the window resizes, adjust the split pane size
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 
 		if (useFX) {
 			/**
 			 * Create browser pane for IRC web client
 			 */
 			// Create panel that contains web browser for IRC
 			final String url = IRC_CLIENT_URL + "?nick=" + this.user + "&channels=" + IRC_CHANNEL;
 			final BrowserPanel browser = new BrowserPanel(url);
 			browser.setPreferredSize(new Dimension(0, 204));
 
 			/**
 			 * Create the split pane separating the tabbed pane and the broswer pane
 			 */
 			// Put the tabbed pane and browser panel in an adjustable vertical split pane
 			final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.book, browser);
 			splitPane.setResizeWeight(1.0);
 			this.add(splitPane, constraints);
 		} else {
 			this.add(this.book, constraints);
 		}
 
 		// Create timer that will poll server for changes
 		final Timer refreshTimer = new Timer();
 		refreshTimer.scheduleAtFixedRate(new RefreshTask(this), 0, REFRESH_RATE);
 
 		// Post welcome to status bar
 		this.log("Welcome " + this.user);
 	}
 
 	/**
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		final String command = e.getActionCommand();
 		switch (command) {
 			case "Change name":
 				// Triggered by change name, prompt for new name
 				new UserLogin(this);
 				break;
 			case "Load state":
 				// Triggered by change state, prompt for save file
 				new LoadStatePrompt(this.server, this);
 				break;
 			case "Hide Closed":
 				// Triggered by change to Hide Closed menu item
 				this.hideClosed = ( (JCheckBoxMenuItem) e.getSource() ).isSelected();
 				saveProperty("hideClosed", this.hideClosed + "");
 				this.update(true);
 				break;
 			case "Hide Duplicates":
 				// Triggered by change to Hide Closed menu item
 				this.hideDuplicates = ( (JCheckBoxMenuItem) e.getSource() ).isSelected();
 				saveProperty("hideDuplicates", this.hideDuplicates + "");
 				this.update(true);
 				break;
 			case "Sort Timestamp Ascending":
 				// Triggered by Timestamp Sort menu item
 				setSort(QueueSort.TIMESTAMP_ASCENDING);
 				this.update(true);
 				break;
 			case "Sort Question Number Ascending":
 				// Triggered by Question Number Sort menu item
 				setSort(QueueSort.QNUMBER_ASCENDING);
 				this.update(true);
 				break;
 			case "Sort Status Ascending":
 				// Triggered by Status Sort menu item
 				setSort(QueueSort.STATUS_ASCENDING);
 				this.update(true);
 				break;
 			case "Sort Timestamp Descending":
 				// Triggered by Timestamp Sort menu item
 				setSort(QueueSort.TIMESTAMP_DESCENDING);
 				this.update(true);
 				break;
 			case "Sort Question Number Descending":
 				// Triggered by Question Number Sort menu item
 				setSort(QueueSort.QNUMBER_DESCENDING);
 				this.update(true);
 				break;
 			case "Sort Status Descending":
 				// Triggered by Status Sort menu item
 				setSort(QueueSort.STATUS_DESCENDING);
 				this.update(true);
 				break;
 			case "Caller":
 				// Triggered by Caller Role menu item
 				this.setRole(Role.CALLER);
 				break;
 			case "Typist":
 				// Triggered by Typist Role menu item
 				this.setRole(Role.TYPIST);
 				break;
 			case "Researcher":
 				// Triggered by Researcher Role menu item
 				this.setRole(Role.RESEARCHER);
 				break;
 			case "Reset window positions":
 				// Triggered by Reset window positions menu item
 				TriviaClient.resetPositions();
 				break;
 			case "Open wiki":
 				// Triggered by Open wiki menu item
 				try {
 					Desktop.getDesktop().browse(new URI(WIKI_URL));
 				} catch (IOException | URISyntaxException exception) {
 					this.log("Couldn't open a browser window");
 				}
 		}
 	}
 
 	/**
 	 * Display disconnected dialog box and prompt for action
 	 */
 	public synchronized void disconnected() {
 
 		final String message = "Communication with server failed!";
 
 		final Object[] options = { "Retry", "Exit" };
 		final int option = JOptionPane.showOptionDialog(null, message, "Disconnected", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.ERROR_MESSAGE, null, options, options[1]);
 		if (option == 1) {
 			// Exit the client
 			System.exit(0);
 		}
 
 	}
 
 	/**
 	 * Get the tabbed content pane.
 	 * 
 	 * @return The tabbed content pane
 	 */
 	public JTabbedPane getBook() {
 		return this.book;
 	}
 
 	/**
 	 * Get the root frame for this application.
 	 * 
 	 * @return The root frame for the application
 	 */
 	public JFrame getFrame() {
 		return this.frame;
 	}
 
 	/**
 	 * Get the answer queue sort method.
 	 * 
 	 * @return The sort method
 	 */
 
 	public QueueSort getQueueSort() {
 		return this.queueSort;
 	}
 
 	/**
 	 * Return the local Trivia object. When updating the GUI, always get the current Trivia object first to ensure the
 	 * most recent data is used. Components should always use this local version to read data to limit server traffic.
 	 * 
 	 * @return The local Trivia object
 	 */
 	public Trivia getTrivia() {
 		return this.trivia;
 	}
 
 	/**
 	 * Gets the user name.
 	 * 
 	 * @return The user name
 	 */
 	public String getUser() {
 		return this.user;
 	}
 
 	/**
 	 * Get the hash of active users and roles.
 	 * 
 	 * @return The hashtable of users and roles
 	 */
 	public Hashtable<String, Role> getActiveUserHash() {
 		return this.activeUserHash;
 	}
 
 	/**
 	 * Get the hash of idle users and roles.
 	 * 
 	 * @return The hashtable of users and roles
 	 */
 	public Hashtable<String, Role> getPassiveUserHash() {
 		return this.passiveUserHash;
 	}
 
 	/**
 	 * Determine whether closed questions should be hidden in the answer queue.
 	 */
 	public boolean hideClosed() {
 		return this.hideClosed;
 	}
 
 	/**
 	 * Determine whether duplicate answers should be hidden in the answer queue.
 	 */
 	public boolean hideDuplicates() {
 		return this.hideDuplicates;
 	}
 
 
 	/**
 	 * Display message in the status bar and in console
 	 * 
 	 * @param message
 	 *            Message to log
 	 */
 	public void log(String message) {
 		// Display message in status bar
 		this.statusBar.setText(message);
 		// Print message to console
 		System.out.println("LOG: " + message);
 	}
 
 	/**
 	 * Change the user's role.
 	 * 
 	 * @param role
 	 */
 	private void setRole(Role role) {
 		int tryNumber = 0;
 		boolean success = false;
 		while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
 			tryNumber++;
 			try {
 				this.trivia = this.server.getTrivia();
 				this.server.setRole(this.user, role);
 				success = true;
 			} catch (final RemoteException e) {
 				this.log("Couldn't change role server (try #" + tryNumber + ").");
 			}
 		}
 
 		// Show disconnected dialog if we could not retrieve the Trivia data
 		if (!success || this.trivia == null) {
 			this.disconnected();
 			return;
 		}
 	}
 
 	/**
 	 * Sets the user name
 	 * 
 	 * @param user
 	 *            The new user name
 	 */
 	public void setUser(String user) {
 		int tryNumber = 0;
 		boolean success = false;
 		while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
 			tryNumber++;
 			try {
 				if (this.user == null) {
 					this.server.login(user);
 				} else {
 					this.server.changeUser(this.user, user);
 				}
 				success = true;
 			} catch (final RemoteException e) {
 				this.log("Couldn't change user name on server (try #" + tryNumber + ").");
 			}
 		}
 
 		if (!success) {
 			this.disconnected();
 			return;
 		}
 		this.user = user;
 	}
 
 	/**
 	 * Set the answer queue sort method and save to the settings file.
 	 * 
 	 * @param newSort
 	 *            The new sort method
 	 */
 	public void setSort(QueueSort newSort) {
 		this.queueSort = newSort;
 		switch (newSort) {
 			case TIMESTAMP_ASCENDING:
 				saveProperty("queueSort", "Sort Timestamp Ascending");
 				this.sortTimestampAscendingMenuItem.setSelected(true);
 				break;
 			case TIMESTAMP_DESCENDING:
 				saveProperty("queueSort", "Sort Timestamp Descending");
 				this.sortTimestampDescendingMenuItem.setSelected(true);
 				break;
 			case QNUMBER_ASCENDING:
 				saveProperty("queueSort", "Sort Question Number Ascending");
 				this.sortQNumberAscendingMenuItem.setSelected(true);
 				break;
 			case QNUMBER_DESCENDING:
 				saveProperty("queueSort", "Sort Question Number Descending");
 				this.sortQNumberDescendingMenuItem.setSelected(true);
 				break;
 			case STATUS_ASCENDING:
 				saveProperty("queueSort", "Sort Status Ascending");
 				this.sortStatusAscendingMenuItem.setSelected(true);
 				break;
 			case STATUS_DESCENDING:
 				saveProperty("queueSort", "Sort Status Descending");
 				this.sortStatusDescendingMenuItem.setSelected(true);
 				break;
 		}
 
 	}
 
 	/**
 	 * Update the GUI
 	 */
 	@Override
 	public synchronized void update(boolean force) {
 		// Update each individual tab in the GUI
 		for (final TriviaPanel page : this.panelList) {
 			page.update(force);
 		}
 	}
 
 	@Override
 	public void windowActivated(WindowEvent e) {
 	}
 
 	@Override
 	public void windowClosed(WindowEvent e) {
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 		savePosition(e.getWindow());
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 	}
 
 	@Override
 	public void windowIconified(WindowEvent e) {
 	}
 
 	@Override
 	public void windowOpened(WindowEvent e) {
 	}
 
 	/**
 	 * Create and show the GUI.
 	 */
 	private static void createAndShowGUI(boolean useFX) {
 		try {
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 				}
 			}
 		} catch (Exception e) {
 			// If Nimbus is not available, you can set the GUI to another look and feel.
 		}
 		// Create the application window
 		final JFrame frame = new JFrame("Trivia");
 		frame.setName("Main_Window");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Initialize server variable
 		TriviaInterface triviaServer = null;
 
 		// Initiate connection to RMI server
 		int tryNumber = 0;
 		boolean success = false;
 		while (tryNumber < MAX_RETRIES && success == false) {
 			tryNumber++;
 			try {
 				// Connect to RMI server
 				triviaServer = (TriviaInterface) Naming.lookup(TRIVIA_SERVER_URL);
 				success = true;
 			} catch (MalformedURLException | NotBoundException | RemoteException e) {
 				// Connection failed
 				System.out.println("Initial connection to server failed (try #" + tryNumber + ")");
 
 				if (tryNumber == MAX_RETRIES) {
 					// Maximum retries reached, pop up disconnected dialog
 					final String message = "Could not connect to server.";
 
 					final Object[] options = { "Retry", "Exit" };
 					final int option = JOptionPane.showOptionDialog(null, message, "Disconnected",
 							JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);
 
 					if (option == JOptionPane.OK_OPTION) {
 						// Retry connection
 						tryNumber = 0;
 					} else {
 						// Exit
 						System.exit(0);
 					}
 				}
 			}
 		}
 
 		System.out.println("Connected to trivia server (" + TRIVIA_SERVER_URL + ").");
 
 		// Initialize GUI and place in window
 		try {
 			frame.add(new TriviaClient(frame, triviaServer, useFX), BorderLayout.CENTER);
 		} catch (final Exception e) {
 			System.exit(0);
 		}
 
 		// Display the window.
 		loadPosition(frame);
 		frame.setVisible(true);
 	}
 
 	public TriviaPanel getTab(String tabName) {
 		TriviaPanel panel = null;
 		switch (tabName) {
 			case "Workflow":
 				panel = new WorkflowPanel(server, this);
 				break;
 			case "Current":
 				panel = new RoundPanel(server, this);
 				break;
 			case "History":
 				panel = new HistoryPanel(server, this);
 				break;
 			case "By Round":
 				panel = new ScoreByRoundPanel(server, this);
 				break;
 			case "Place Chart":
 				panel = new PlaceChartPanel(this);
 				break;
 			case "Score Chart":
 				panel = new ScoreByRoundChartPanel(this);
 				break;
 			case "Cumul. Score Chart":
 				panel = new CumulativePointsChartPanel(this);
 				break;
 			case "Team Comparison":
 				panel = new TeamComparisonPanel(this);
 				break;
 		}
 		this.panelList.add(panel);
 		return panel;
 	}
 
 	public Set<String> getTabNames() {
 		return TriviaClient.tabDescriptionHash.keySet();
 	}
 
 	public String getTabDescription(String tabName) {
 		return TriviaClient.tabDescriptionHash.get(tabName);
 	}
 
 	/**
 	 * Load the saved position and size of the window from file. If none found, use preferred size of components.
 	 * 
 	 * @param window
 	 *            The window whose position and size is to be loaded
 	 */
 	public static void loadPosition(Window window) {
 		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
 		final Properties props = new Properties();
 		try {
 			final BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
 			props.load(fileBuffer);
 
 			final String frameID = window.getName().replaceAll(" ", "_");
 
 			final int x = Integer.parseInt(props.getProperty(frameID + "_x"));
 			final int y = Integer.parseInt(props.getProperty(frameID + "_y"));
 			final int width = Integer.parseInt(props.getProperty(frameID + "_width"));
 			final int height = Integer.parseInt(props.getProperty(frameID + "_height"));
 
 			window.setBounds(x, y, width, height);
 
 		} catch (IOException | NumberFormatException e) {
 			System.out.println("Couldn't load window position, may not exist yet.");
 			window.pack();
 			window.setLocationRelativeTo(null);
 		}
 	}
 
 	/**
 	 * Load a property from the settings file.
 	 * 
 	 * @param propName
 	 *            The property name
 	 * @return The property's value
 	 */
 	public static String loadProperty(String propName) {
 		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
 		final Properties props = new Properties();
 		try {
 			final BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
 			props.load(fileBuffer);
 			return props.getProperty(propName);
 
 		} catch (IOException | NumberFormatException e) {
 			System.out.println("Couldn't load propety, may not exist yet.");
 		}
 		return "";
 	}
 
 	/**
 	 * Entry point for the client application. Only the first argument is used. If the first argument is "useFX", the
 	 * client will include an IRC client panel.
 	 * 
 	 * @param args
 	 *            Command line arguments; only "useFX" is recognized as an argument
 	 * 
 	 */
 	public static void main(String[] args) {
 		boolean useFX = false;
 		// Schedule a job to create and show the GUI
 		if (args.length > 0 && args[0].equals("useFX")) {
 			useFX = true;
 		}
 		SwingUtilities.invokeLater(new TriviaRunnable(useFX));
 	}
 
 	/**
 	 * Convert a cardinal number into its ordinal counterpart.
 	 * 
 	 * @param cardinal
 	 *            The number to convert to ordinal form
 	 * @return String with the ordinal representation of the number (e.g., 1st, 2nd, 3rd, etc.)
 	 * 
 	 */
 	public static String ordinalize(int cardinal) {
 		// Short-circuit for teen numbers that don't follow normal rules
 		if (10 < cardinal % 100 && cardinal % 100 < 14) return cardinal + "th";
 		// Ordinal suffix depends on the ones digit
 		final int modulus = cardinal % 10;
 		switch (modulus) {
 			case 1:
 				return cardinal + "st";
 			case 2:
 				return cardinal + "nd";
 			case 3:
 				return cardinal + "rd";
 			default:
 				return cardinal + "th";
 		}
 	}
 
 	/**
 	 * Save the position and size of the window to file.
 	 * 
 	 * @param window
 	 *            The window whose size and position is to be saved
 	 * 
 	 */
 	public static void savePosition(Window window) {
 		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
 		final Properties props = new Properties();
 		BufferedReader infileBuffer;
 		try {
 			infileBuffer = new BufferedReader(new FileReader(file));
 			props.load(infileBuffer);
 		} catch (final IOException e) {
 		}
 
 		final Rectangle r = window.getBounds();
 		final int x = (int) r.getX();
 		final int y = (int) r.getY();
 		final int width = (int) r.getWidth();
 		final int height = (int) r.getHeight();
 
 		final String frameID = window.getName().replaceAll(" ", "_");
 
 		props.setProperty(frameID + "_x", x + "");
 		props.setProperty(frameID + "_y", y + "");
 		props.setProperty(frameID + "_width", width + "");
 		props.setProperty(frameID + "_height", height + "");
 
 		try {
 			final BufferedWriter outfileBuffer = new BufferedWriter(new FileWriter(file));
 			props.store(outfileBuffer, "Trivia");
 			outfileBuffer.close();
 		} catch (final IOException e) {
 			System.out.println("Error saving window position.");
 		}
 	}
 
 	/**
 	 * Saves a property to the settings file.
 	 * 
 	 * @param propName
 	 *            Name of the property to save
 	 * @param value
 	 *            Value associated with the property
 	 */
 	public static void saveProperty(String propName, String value) {
 		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
 		final Properties props = new Properties();
 		BufferedReader infileBuffer;
 		try {
 			infileBuffer = new BufferedReader(new FileReader(file));
 			props.load(infileBuffer);
 		} catch (final IOException e) {
 		}
 
 		props.setProperty(propName, value);
 
 		try {
 			final BufferedWriter outfileBuffer = new BufferedWriter(new FileWriter(file));
 			props.store(outfileBuffer, "Trivia");
 			outfileBuffer.close();
 		} catch (final IOException e) {
 			System.out.println("Error saving property.");
 		}
 	}
 
 	/**
 	 * Clear all saved data from file.
 	 * 
 	 */
 	public static void resetPositions() {
 		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
 		final Properties props = new Properties();
 		try {
 			final BufferedWriter outfileBuffer = new BufferedWriter(new FileWriter(file));
 			props.store(outfileBuffer, "Trivia");
 			outfileBuffer.close();
 		} catch (final IOException e) {
 			System.out.println("Error clearing window positions.");
 		}
 	}
 
 	/**
 	 * Private class to handle background downloading of Trivia object from server.
 	 * 
 	 */
 	private class TriviaFetcher extends SwingWorker<Void, Void> {
 
 		final TriviaInterface	server;
 		final TriviaClient		client;
 
 		public TriviaFetcher(TriviaInterface server, TriviaClient client) {
 			super();
 			this.server = server;
 			this.client = client;
 		}
 
 		@Override
 		protected Void doInBackground() throws Exception {
 			/**
 			 * Create a local copy of the Trivia object
 			 */
 			int tryNumber = 0;
 			boolean success = false;
 			while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
 				tryNumber++;
 				try {
 					this.client.trivia = this.server.getTrivia();
 					success = true;
 				} catch (final RemoteException e) {
 					this.client.log("Couldn't retrive trivia data from server (try #" + tryNumber + ").");
 				}
 			}
 
 			// Show disconnected dialog if we could not retrieve the Trivia data
 			if (!success || this.client.trivia == null) {
 				this.client.disconnected();
 				return null;
 			}
 			return null;
 
 		}
 
 	}
 
 	private static class RefreshTask extends TimerTask {
 
 		final TriviaClient	client;
 
 		public RefreshTask(TriviaClient client) {
 			this.client = client;
 		}
 
 		@Override
 		public void run() {
 			Round[] newRounds = null;
 			final int[] oldVersions = client.trivia.getVersions();
 			int currentRound = 0;
 
 			// Synchronize the local Trivia data to match the server
 			int tryNumber = 0;
 			boolean success = false;
 			while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
 				tryNumber++;
 				try {
 					newRounds = client.server.getChangedRounds(client.getUser(), oldVersions);
 					currentRound = client.server.getCurrentRound();
 					client.activeUserHash = client.server.getActiveUsers(USER_LIST_WINDOW, USER_LIST_TIMEOUT);
 					client.passiveUserHash = client.server.getIdleUsers(USER_LIST_WINDOW, USER_LIST_TIMEOUT);
 					success = true;
 				} catch (final RemoteException e) {
 					client.log("Couldn't retrive trivia data from server (try #" + tryNumber + ").");
 				}
 			}
 
 			if (!success) {
 				client.disconnected();
 				return;
 			}
 
 			client.trivia.updateRounds(newRounds);
 			client.trivia.setCurrentRound(currentRound);
 
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					client.update();
 				}
 			});
 
 		}
 
 	}
 
 	/**
 	 * Custom Runnable class to allow passing of command line argument into invokeLater.
 	 * 
 	 */
 	private static class TriviaRunnable implements Runnable {
 		private final boolean	useFX;
 
 		public TriviaRunnable(boolean useFX) {
 			this.useFX = useFX;
 		}
 
 		@Override
 		public void run() {
 			createAndShowGUI(this.useFX);
 		}
 	}
 
 }
