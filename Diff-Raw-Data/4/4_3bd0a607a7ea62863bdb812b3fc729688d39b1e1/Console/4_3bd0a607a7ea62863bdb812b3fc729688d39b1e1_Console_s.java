 package com.git.ifly6;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.TextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Date;
 import java.util.Scanner;
 
 import javax.swing.Box;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EmptyBorder;
 import javax.swing.text.DefaultEditorKit;
 
 import com.apple.eawt.Application;
 
 /**
  * Main Class for Utilities Pro 3.x. It initialises the GUI and contains all
  * relevant pieces of data fundamental to the execution of the programme.
  * Furthermore, it contains all necessary ActionListeners and GUI related
  * methods (basically integrating the older Console Interface, Parameters, and
  * Console classes from the last major version of Utilities Pro-2.x)
  * 
  * @author ifly6
  * @version 3.x
  */
 public class Console {
 
 	private JFrame frame;
 
 	/**
 	 * Naming system is: <major>.<minor>_<revision> or <major>.<minor>_dev<#>
 	 */
 	public static String version = "3.0_dev07";
 
 	/**
 	 * The Keyword is like "Sandy Bridge". There is a defined list of them. For
 	 * 3.x, its is 3.0 = iceland, 3.1 = iceberg, 3.2 = icepool, 3.3 = skyfall,
 	 * 3.4 = icefield, 3.5 = everest, 3.6 = icemont, 3.7 = icewell, 3.8 =
 	 * icedtea
 	 */
 	public static String keyword = "iceland";
 
 	/**
 	 * Used for greeting the user. It should be replaced from Unknown to the
 	 * iNet name of the user inside Console.Main
 	 */
 	protected static String computername = "Unknown";
 
 	/**
 	 * Used in the all following File systems, as the user name of the user is
 	 * not the same throughout all computers.
 	 */
 	public static String userName = System.getProperty("user.name");
 
 	/**
 	 * A place in ~/Library/Application Support/ where we store all of our
 	 * configuration files.
 	 */
 	public static String UtilitiesPro_DIR = "/Users/" + userName
 			+ "/Library/Application Support/Utilities Pro";
 
 	/**
 	 * The place to put any files we download.
 	 */
 	public static String Downloads_DIR = "/Users/" + userName + "/Downloads/";
 
 	/**
 	 * Runtime Handler. Can be called from anywhere to execute a String[]. When
 	 * we finish a system to return a Process, this shared resource will be
 	 * removed. However, as it appears that it is not happening, it will likely
 	 * never be removed.
 	 */
 	public static Runtime rt = Runtime.getRuntime();
 
 	/**
 	 * List of all the internal commands inside a String Array. All unused
 	 * commands should be stated as nulls.
 	 */
 	public static String[] commText = new String[16];
 
 	/**
 	 * JTextArea for the output of the programme. Combines the Error and Output
 	 * Streams into one field.
 	 */
 	private static JTextArea outText;
 
 	/**
 	 * JTextArea for the output of the log. Receives strings to append to the
 	 * log from the method "log(String)"
 	 */
 	private static JTextArea logText;
 
 	/**
 	 * TextField for the input of commands. When command engine is run, it
 	 * retrieves the contents of this field, then processes it.
 	 */
 	private static TextField inputField;
 
 	/**
 	 * Process is declared here to allow other classes to terminate that process
 	 * should it be necessary.
 	 */
 	public static Process process;
 
 	/**
 	 * Launch the application. Executes on a pipeline, going first to read the
 	 * GUI configuration file, with the Look and Feel of the GUI. Then it moves
 	 * to analyse whether there is a command-line argument for updating, then
 	 * launches the GUI.
 	 * 
 	 * @param inputArgs
 	 *            TODO When launched from command line with "-u", the programme
 	 *            will update Utilities Pro.
 	 */
 	@SuppressWarnings("deprecation")
 	public static void main(String[] inputArgs) {
 
 		// Set Properties before GUI Calls
 		System.setProperty("apple.laf.useScreenMenuBar", "true");
 		System.setProperty("com.apple.mrj.application.apple.menu.about.name",
 				"Utilities Pro");
 
 		// Handling Mac Toolbar System
 		Application macApp = Application.getApplication();
 		MacHandler macAdapter = new MacHandler();
 		macApp.addApplicationListener(macAdapter);
 		macApp.setEnabledPreferencesMenu(true);
 
 		// Look and Feel
 		FileReader configRead = null;
 		String look = "Default";
 		try {
 			configRead = new FileReader(UtilitiesPro_DIR + "/config.txt");
 			Scanner scan = new Scanner(configRead);
 			look = scan.nextLine();
 		} catch (FileNotFoundException e1) {
 			try {
 				UIManager.setLookAndFeel(UIManager
 						.getSystemLookAndFeelClassName());
 			} catch (ClassNotFoundException e) {
 			} catch (InstantiationException e) {
 			} catch (IllegalAccessException e) {
 			} catch (UnsupportedLookAndFeelException e) {
 			}
 		}
 
 		// GUI Look and Feel
 		if (look.equals("CrossPlatformLAF")) {
 			try {
 				UIManager.setLookAndFeel(UIManager
 						.getCrossPlatformLookAndFeelClassName());
 			} catch (ClassNotFoundException e) {
 			} catch (InstantiationException e) {
 			} catch (IllegalAccessException e) {
 			} catch (UnsupportedLookAndFeelException e) {
 			}
 		} else {
 			try {
 				UIManager.setLookAndFeel(UIManager
 						.getSystemLookAndFeelClassName());
 			} catch (ClassNotFoundException e) {
 			} catch (InstantiationException e) {
 			} catch (IllegalAccessException e) {
 			} catch (UnsupportedLookAndFeelException e) {
 			}
 		}
 
 		/*
 		 * TODO Prevent this from throwing an array out of bounds exception.
 		 * 
 		 * if ("--update".equals(inputArgs[0]) || "-u".equals(inputArgs[0])) {
 		 * EventQueue.invokeLater(new Runnable() {
 		 * 
 		 * @Override public void run() { log("Utilities Pro Update Triggered");
 		 * String[] url = { "curl", "-o", Downloads_DIR,
 		 * "http://ifly6.no-ip.org/UtilitiesPro/UtilitiesPro-latest.jar" }; try
 		 * { rt.exec(url); } catch (IOException e) {
 		 * log("Utilities Pro Download Failed");
 		 * append("Utilities Pro Download Failed"); }
 		 * append("Utilities Pro Updated. File in ~/Downloads."); } }); }
 		 */
 
 		// == CMD-String Array Settings ==
 		commText[0] = "/changelog";
 		commText[1] = "/about";
 		commText[2] = "/help";
 		commText[3] = "/clear";
 		commText[4] = "/acknowledgements";
 		commText[5] = null;
 		commText[6] = "/licence";
 		commText[7] = "/save";
 		commText[8] = "/saveLog";
 		commText[9] = "/delete";
 		commText[10] = "/info";
 		commText[11] = "/mindterm";
 		commText[12] = "/terminate";
 		commText[13] = null;
 		commText[14] = null;
 		commText[15] = "quit";
 
 		try {
 			computername = InetAddress.getLocalHost().getHostName();
 		} catch (UnknownHostException e) {
 		}
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Console window = new Console();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create instance of the application.
 	 */
 	public Console() {
 		initialize();
 	}
 
 	/**
 	 * This system starts the main GUI for the programme. It also contains all
 	 * GUI data for the programme, causing a necessity for the method getters
 	 * and setters which are evident below.
 	 * 
 	 * @param frame
 	 *            JFrame for the programme
 	 * @param panel
 	 *            Panel for the Console's Tab
 	 * @param scrollPane_logText
 	 *            Pane for the Logging Tab
 	 * @param outText
 	 *            JTextArea for the Console's output
 	 * @param logText
 	 *            JTextArea for the Logging's output
 	 * @param inputField
 	 *            TextField (AWT) for input into the programme
 	 */
 	private void initialize() {
 		frame = new JFrame();
 
 		frame.setBounds(0, 0, 670, 735);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setTitle("Utilities Pro " + version);
 
 		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
 		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
 
 		JPanel panel = new JPanel();
 		tabbedPane.addTab("Console", null, panel, null);
 		panel.setLayout(new BorderLayout(0, 0));
 
 		inputField = new TextField();
 		inputField.setFont(new Font("Monaco", Font.PLAIN, 12));
 		panel.add(inputField, BorderLayout.SOUTH);
 		inputField.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				int keyCode = e.getKeyCode();
 				if (keyCode == 10) {
 					ExecEngine.process();
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 			}
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}
 		});
 
 		outText = new JTextArea();
 		outText.setEditable(false);
 		outText.setFont(new Font("Monaco", Font.PLAIN, 12));
 		JScrollPane scrollPane_outPane = new JScrollPane(outText);
		scrollPane_outPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
 		scrollPane_outPane.setViewportBorder(new EmptyBorder(5, 5, 5, 5));
 		panel.add(scrollPane_outPane, BorderLayout.CENTER);
 
 		logText = new JTextArea();
 		logText.setEditable(false);
 		logText.setFont(new Font("Monaco", Font.PLAIN, 12));
 		JScrollPane scrollPane_logText = new JScrollPane(logText);
 		tabbedPane.addTab("Log", null, scrollPane_logText,
 				"Shows a dynamic log of all functions run.");
 
 		JMenuBar menuBar = new JMenuBar();
 		frame.setJMenuBar(menuBar);
 
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 
 		JMenuItem mntmOpenConfig = new JMenuItem("Open Configuration Folder");
 		mntmOpenConfig.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				command("File>Open Configuration Folder");
 				FileCommands.configManage(1);
 			}
 		});
 		mnFile.add(mntmOpenConfig);
 
 		JMenuItem mntmDeleteConfig = new JMenuItem("Delete Configuration");
 		mntmDeleteConfig.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("File>Delete Configuration");
 				FileCommands.configManage(2);
 			}
 		});
 		mnFile.add(mntmDeleteConfig);
 
 		JSeparator separator = new JSeparator();
 		mnFile.add(separator);
 
 		JMenuItem mntmExportConsole = new JMenuItem("Export Console");
 		mntmExportConsole.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("File>Export Console");
 				FileCommands.export(1);
 			}
 		});
 		mnFile.add(mntmExportConsole);
 
 		JMenuItem mntmConsoleLog = new JMenuItem("Export Log");
 		mntmConsoleLog.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("File>Export Log");
 				FileCommands.export(2);
 			}
 		});
 		mnFile.add(mntmConsoleLog);
 
 		JMenu mnEdit = new JMenu("Edit");
 		menuBar.add(mnEdit);
 
 		JMenuItem mntmCut = new JMenuItem(new DefaultEditorKit.CutAction());
 		mntmCut.setText("Cut");
 		mntmCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
 				ActionEvent.META_MASK));
 		mnEdit.add(mntmCut);
 
 		JMenuItem mntmCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
 		mntmCopy.setText("Copy");
 		mntmCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
 				ActionEvent.META_MASK));
 		mnEdit.add(mntmCopy);
 
 		JMenuItem mntmPaste = new JMenuItem(new DefaultEditorKit.PasteAction());
 		mntmPaste.setText("Paste");
 		mntmPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
 				ActionEvent.META_MASK));
 		mnEdit.add(mntmPaste);
 
 		JSeparator separator_4 = new JSeparator();
 		mnEdit.add(separator_4);
 
 		JMenuItem mntmClearConsole = new JMenuItem("Clear Console");
 		mntmClearConsole.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// There is no need for command log here.
 				EditCommands.consoleClear();
 			}
 		});
 		mnEdit.add(mntmClearConsole);
 
 		JMenuItem mntmClearLog = new JMenuItem("Clear Log");
 		mntmClearLog.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// There is no need for command log here.
 				EditCommands.logClear();
 			}
 		});
 		mnEdit.add(mntmClearLog);
 
 		JMenu mnScripts = new JMenu("Scripts");
 		menuBar.add(mnScripts);
 
 		JMenuItem mntmPurge = new JMenuItem("Purge Memory");
 		mntmPurge.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Scripts>Purge Memory");
 				ScriptCommands.purge();
 			}
 		});
 		mnScripts.add(mntmPurge);
 
 		JMenuItem mntmRestartAirport = new JMenuItem("Restart Airport");
 		mntmRestartAirport.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Scripts>Restart Airport");
 				ScriptCommands.wireless();
 			}
 		});
 		mnScripts.add(mntmRestartAirport);
 
 		JSeparator separator_1 = new JSeparator();
 		mnScripts.add(separator_1);
 
 		JMenuItem mntmSystemInfo = new JMenuItem("System Information");
 		mntmSystemInfo.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Scripts>System Information");
 				ScriptCommands.readout();
 			}
 		});
 		mnScripts.add(mntmSystemInfo);
 
 		JMenuItem mntmDownloadMindterm = new JMenuItem("Download Mindterm");
 		mntmDownloadMindterm.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Scripts>Download Mindterm");
 				ScriptCommands.mindterm();
 			}
 		});
 		mnScripts.add(mntmDownloadMindterm);
 
 		JMenu mnCommand = new JMenu("Command");
 		menuBar.add(mnCommand);
 
 		JMenuItem mntmTerminateUtilitiesPro = new JMenuItem(
 				"Terminate Utilities Pro Process");
 		mntmTerminateUtilitiesPro.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Command>Terminate Utilities Pro Process");
 				CommandCommands.terminateUtility();
 			}
 		});
 		mnCommand.add(mntmTerminateUtilitiesPro);
 
 		JMenuItem mntmTerminateArbitraryProcess = new JMenuItem(
 				"Terminate Arbitrary Process");
 		mntmTerminateArbitraryProcess.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Command>Terminate Arbitrary Process");
 				CommandCommands.terminateChoose();
 			}
 		});
 		mnCommand.add(mntmTerminateArbitraryProcess);
 
 		JSeparator separator_3 = new JSeparator();
 		mnCommand.add(separator_3);
 
 		JMenuItem mntmBombard = new JMenuItem("Bombard");
 		mntmBombard.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Command>Bombard");
 				CommandCommands.bombard();
 			}
 		});
 		mnCommand.add(mntmBombard);
 
 		Component horizontalGlue = Box.createHorizontalGlue();
 		menuBar.add(horizontalGlue);
 
 		JMenu mnHelp = new JMenu("Help");
 		menuBar.add(mnHelp);
 
 		JMenuItem mntmAbout = new JMenuItem("About");
 		mntmAbout.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Help>About");
 				HelpCommands.about();
 			}
 		});
 		mnHelp.add(mntmAbout);
 
 		JMenuItem mntmHelp = new JMenuItem("Utilities Pro Help");
 		mntmHelp.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Help>Utilities Pro Help");
 				HelpCommands.helpList();
 			}
 		});
 		mnHelp.add(mntmHelp);
 
 		JMenuItem mntmBashHelp = new JMenuItem("Bash Help");
 		mntmBashHelp.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Help>Bash Help");
 				HelpCommands.bashHelp();
 			}
 		});
 		mnHelp.add(mntmBashHelp);
 
 		JSeparator separator_2 = new JSeparator();
 		mnHelp.add(separator_2);
 
 		JMenuItem mntmQuit = new JMenuItem("Quit");
 		mntmQuit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// No Command log necessary.
 				System.exit(0);
 			}
 		});
 		mnHelp.add(mntmQuit);
 
 		String greet = "Welcome, " + userName + " to Utilities Pro - "
 				+ version + " '" + keyword + "'\n===========";
 		outText.append(greet);
 	}
 
 	/**
 	 * @since 2.2_01
 	 * @param in
 	 *            String to append into the JTextArea outText
 	 * @see com.me.ifly6.ConsoleIf
 	 */
 	public static void append(String in) {
 		outText.append("\n" + in);
 	}
 
 	/**
 	 * @since 2.2_02
 	 * @param in
 	 *            String to append (with a space) into the JTextArea outText
 	 * @see com.me.ifly6.ConsoleIf
 	 */
 	public static void out(String in) {
 		outText.append("\n " + in);
 	}
 
 	/**
 	 * @since 2.2_01
 	 * @param in
 	 *            String to append (with a date) into the JTextArea logText
 	 */
 	public static void log(String in) {
 		logText.append("\n" + new Date() + " " + in);
 	}
 
 	/**
 	 * @since 3.0_dev07
 	 * @param in
 	 *            String to append with the bash prompt to JTextArea outText.
 	 *            Also appends to logText.
 	 */
 	public static void command(String in) {
 		append(computername + "~ $ " + in);
 		log(computername + "~ $ " + in);
 	}
 
 	/**
 	 * @since 3.0_dev02
 	 * @return String with contents of JTextArea outText
 	 */
 	public static String getOutText() {
 		return outText.getText();
 	}
 
 	/**
 	 * @since 3.0_dev02
 	 * @return String with contents of JTextArea logText
 	 */
 	public static String getLogText() {
 		return logText.getText();
 	}
 
 	/**
 	 * As it deals with the GUI's implementation (JTextArea), Java forces its
 	 * location to be inside the GUI's declaration class.
 	 * 
 	 * @author ifly6
 	 * @since 3.0_dev02
 	 * @param which
 	 *            integer value, determines which JTextArea to clear (1,
 	 *            outText; 2, logText; 3, inputField)
 	 */
 	public static void clearText(int which) {
 		if (which == 1) {
 			outText.setText(null);
 		}
 		if (which == 2) {
 			logText.setText(null);
 		}
 		if (which == 3) {
 			inputField.setText(null);
 		}
 	}
 
 	/**
 	 * Used to create (if necessary) all folders for Utilities Pro. Creates
 	 * ~/Library/Application Support/Utilities Pro folder and verifies that
 	 * ~/Downloads exists. This programme should be run on a Mac, as both are
 	 * only applicable under the File Structure of one (or very similar Linux
 	 * distributions)
 	 * 
 	 * @author ifly6
 	 * @since 2.2_01
 	 */
 	public static void mkdir() {
 		File folder = new File(UtilitiesPro_DIR);
 		folder.mkdirs();
 		folder = new File(Downloads_DIR);
 		folder.mkdirs();
 	}
 
 	/**
 	 * @since 3.0_dev05
 	 * @return a string with the contents of TextArea inputField
 	 */
 	public static String getInputField() {
 		String a = inputField.getText();
 		inputField.setText(null);
 		return a;
 	}
 }
