 package edu.usu.cs.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InvalidClassException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.io.StreamCorruptedException;
 import java.io.StringWriter;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.EventObject;
 import java.util.Locale;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComboBox;
 import javax.swing.JDesktopPane;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.RepaintManager;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 /*
  * Breaks doContents(), doGlossary(), doJavaDoc()
 import org.eclipse.swt.graphics.DeviceData;
 import org.eclipse.swt.widgets.Display;
 */
 
 import edu.usu.cs.algorithms.Algorithm;
 import edu.usu.cs.graph.Data;
 import edu.usu.cs.graph.Edge;
 import edu.usu.cs.graph.Graph;
 import edu.usu.cs.graph.GraphException;
 import edu.usu.cs.graph.GraphWarning;
 import edu.usu.cs.graph.Node;
 import edu.usu.cs.graph.Path;
 import edu.usu.cs.graph.PathContainer;
 import edu.usu.cs.graph.SwingBridge;
 import edu.usu.cs.graph.DataVisitor;
 
 /**
  * Main Window for Graph Editor
  * Creation date: (3/7/2002 3:54:25 PM)
  * @author Randy Secrist
  */
 public final class GEdit extends JFrame implements LogChangedListener, CascadeConstants {
 	// Serial Version Id
     static final long serialVersionUID = 4690616532841573617L;
     
 	// Graph Instance Variables:
 	private static GEdit _instance = null;  // singleton instance
 	private JDesktopPane mainPain;
 	private MBI mbi;  // JMenuBar
 	private JTextArea logTxt = new JTextArea();
 	private JScrollPane logScrollPane = new JScrollPane(logTxt);
 	private Log log = new Log();  // status logger
 	private boolean isJWS;
 
 	// Graph State Variables:	
 	private boolean balanceOn;  // Init in constructor
 	private boolean traverseWindowUp;
 
 	// Log Window Variables:
 	private JDialog logDialog = new JDialog(this);
 
 	// MemoryUsage Window Variables:
 	private JDialog memoryDialog;
 	private final MemoryMonitor memoryMonitor = new MemoryMonitor();
 	private int MENU_EDITABLE = 1;
 	private int MENU_LOCKED = 0;
 	private int MENU_STATE;
 
 	// Data Variables (Stored In Nodes):
 	private Data dataSpawn = null;
 	private static final String DEFAULT_DATA_WRAPPER = "edu.usu.cs.graph.StringObj";
 	private static String namedWrapper;
 	private Graph theGraph = null;  // data container
 	
 	// File Variables:
 	private String pathname;
 	private Preferences properties; // Init in constructor
 	private final String serialExtension = ".grf";
 	private final String asciiExtension = ".txt";
 	
 	// Source - Destination Input Dialog Variables:
 	private JDialog srcDstDialog;
 	
 	/**
 	 * Constructs a new application instance.
 	 */
 	private GEdit() {
 	    super();
 	    
 	    // Redirect Console
 	    System.setOut(log); System.setErr(log);
 	    
 	    // Initialize State Variables
 	    properties = Preferences.getInstance(this, log);
 	    balanceOn = properties.getBalanceStatus();
 	    nodeViewMenuCheckBoxState = properties.getNodeViewStatus();
 	    edgeViewMenuCheckBoxState = properties.getEdgeViewStatus();
 	    
 	    // Register Log Update Listener
 	    log.addLogChangedListener(this);
 	    
 	    // Ensure dataSpawn is initialized
 	    this.getData("A");
 	    
 	    // Determine Application Type
 	    isJWS = ("0".equals(System.getProperty("javawebstart.version", "0"))) ? false : true;
 	    
 	    // Register RepaintManager
 	    RepaintManager.setCurrentManager(new ThreadAccess(true));
 
 	    // Set up window properties:
 	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 3 == JFrame.EXIT_ON_CLOSE
 	    this.setTitle("Graph Editor - Secrist Family Network"); // Group H - Prof. Jones (CS 2370)
 	    this.mainPain = new JDesktopPane();
 	    mainPain.setBackground(Color.gray);
 	    mainPain.setPreferredSize(new Dimension(930,690));
 	    
 	    // Set layout
 	    Container contentPane = this.getContentPane();
 	    contentPane.setLayout(new BorderLayout());
 	    contentPane.add(mainPain, BorderLayout.CENTER);
 	    
 	    // Add Menu Bar
 	    mbi = MBI.getInstance();
 	    this.getRootPane().setJMenuBar(mbi.getJMenuBar());
 
 	    // Set Icon Image
 	    try {
 	    	InputStream stream = this.getClass().getResourceAsStream("/images/Gedit32a.jpg");
 	        this.setIconImage(ImageIO.read(stream));
 	    }
 	    catch (IOException e) {
 	        log.write("Unable to read Image input stream:\nPlease ensure Gedit32a.jpg exists, in the application resource directory.");   
 	    }
 	    
 	    this.pack();
 	    	    
 	    // Set application in center of screen.
 	    Dimension initSize = new Dimension((int) this.getBounds().getWidth(), (int) this.getBounds().getHeight());
 	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 	    int newX = (int) (0 + (screenSize.width / 2) - (initSize.getWidth() / 2));
 	    int newY = (int) (0 + (screenSize.height / 2) - (initSize.getHeight() / 2));
 	    this.setLocation(new Point(newX, newY));
 	}
 	
 	/**
 	 * File Menu - Exit Item - Event Handler
 	 * (Handles what happens when someone clicks on Exit)
 	 */
 	public void doExit() {
 		if (this.doClose()) {
 			this.dispose();
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * File Menu - New Item - Event Handler
 	 * (Handles what happens when someone clicks on New)
 	 *	 
 	 * Creates a new (empty) graph.
 	 */
 	public void doNew() {
 		if (theGraph == null) {
 			theGraph = new Graph();
 			theGraph.setDirected(this.showDirectedPopup());
 			log.write("New Graph Instance Created!");
 			this.processMenuState(MENU_EDITABLE);
 			this.processAlgorithmMenuState();
 			this.drawIsland(0);
 			return;
 		}
 
 		// replace graph
 		if (!this.doClose()) return; // prompts for save
 		theGraph = new Graph();
 		theGraph.setDirected(this.showDirectedPopup());
 		log.write("New Graph Instance Created!");
 		this.processMenuState(MENU_EDITABLE);
 		this.processAlgorithmMenuState();
 		this.drawIsland(0);
 	}
 	
 	/**
 	 * File Menu - Open Item - Event Handler
 	 * (Handles what happens when someone clicks on Open)
 	 */
 	public void doOpen() {
 		if (!this.doClose()) return; // prompts for save
 		
 		Frame openFrame = new Frame();
 		JFileChooser jfc = new JFileChooser();
 		jfc.setFileFilter(new FileListFilter(FileListFilter.SERIALIZE));
 		int option = jfc.showOpenDialog(openFrame);
 		if (option == JFileChooser.CANCEL_OPTION) return;
 		
 		String path = jfc.getSelectedFile().getPath();
 		
 		// Deserialize graph
 		try {
 			FileInputStream fis = new FileInputStream(path);
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			theGraph = (Graph) ois.readObject();
 			log.write("Graph Loaded from: " + path);
 		}
 		catch (java.lang.Throwable e) {
 			if (e instanceof StreamCorruptedException) {
 				this.showWarningPopup("Warning", jfc.getSelectedFile().getName() + " is an invalid Graph file type.", JOptionPane.OK_OPTION);
 				log.write("doOpen::GEdit - Invalid File Type: " + jfc.getSelectedFile().getName());
 				return;
 			}
 			else if (e instanceof InvalidClassException) {
 				this.showWarningPopup("Warning", "This file was saved under a different version and is incompatiable.", JOptionPane.OK_OPTION);
 				log.write("doOpen::GEdit - Invalid Class Exception - " + e.getLocalizedMessage());
 				return;
 			}
 			else if (e instanceof FileNotFoundException) {
 				this.showWarningPopup("Warning", "Unable access file!\n" + e.getLocalizedMessage(), JOptionPane.OK_OPTION);
 				log.write("doOpen::GEdit - FileNotFoundException - " + e.getLocalizedMessage());
 				return;
 			}
 			else if (e instanceof IOException) {
 				this.showWarningPopup("Warning", "Unable access file!\n" + e.getLocalizedMessage(), JOptionPane.OK_OPTION);
 				log.write("doOpen::GEdit - IOException - " + e.getLocalizedMessage());
 				return;
 			}
 			else if (e instanceof ClassNotFoundException) {
 				this.showWarningPopup("Warning", "Graph Class Not Found!", JOptionPane.OK_OPTION);
 				log.write("doOpen::GEdit - ClassNotFoundException - " + e.getLocalizedMessage());
 				return;
 			}
 			else {
 				this.showWarningPopup("Warning", "General Exception: ", JOptionPane.OK_OPTION);
 				log.write("doOpen::GEdit - General Exception - " + e.getLocalizedMessage());
 				return;
 			}
 		 }
 
 		// Update Menu Bar
 		this.processMenuState(MENU_EDITABLE);
 		this.processAlgorithmMenuState();
 
 		// Open Island Windows
 		this.doHardRefresh();
 	}
 	
 	/**
 	 * File Menu - Save Item - Event Handler
 	 * (Handles what happens when someone clicks on Save)
 	 */
 	public void doSave() {
 		if (theGraph == null) {
 			this.showWarningPopup("Warning", "Nothing to Save!", JOptionPane.OK_OPTION);			
 			return;
 		}
 
 		log.write("doSave::GEdit - saved state: " + theGraph.getSaved());
 		
 		if (theGraph.getSaved() && pathname != null) {
 			// If already saved - overwrite to old file
 			try {
 				FileOutputStream fos = new FileOutputStream(pathname);
 				ObjectOutputStream oos = new ObjectOutputStream(fos);
 
 				oos.writeObject(theGraph);
 				oos.flush();
 				oos.close();
 				log.write("Graph Saved to: " + pathname);
 			}
 			catch (java.lang.Throwable e) {
 				if (e instanceof FileNotFoundException) {
 					this.showWarningPopup("Warning", "File Not Found: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 					return;
 				}
 				else if (e instanceof IOException) {
 					this.showWarningPopup("Warning", "IOException: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 					return;
 				}
 				else {
 					this.showWarningPopup("Warning", "General Error: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 					return;
 				}
 			}
 		}
 		else {
 			this.doSaveAs();
 		}
 	}
 	
 	/**
 	 * File Menu - SaveAs Item - Event Handler
 	 * (Handles what happens when someone clicks on SaveAs)
 	 */
 	public void doSaveAs() {
 		if (theGraph == null) {
 			this.showWarningPopup("Warning", "Nothing to Save!", JOptionPane.OK_OPTION);			
 			return;
 		}
 		
 		Frame saveFrame = new Frame();
 		JFileChooser jfc = new JFileChooser();
 		jfc.setFileFilter(new FileListFilter(FileListFilter.SERIALIZE));
 		int option = jfc.showSaveDialog(saveFrame);
 		if (option == JFileChooser.CANCEL_OPTION) return;
 
 		String path = jfc.getSelectedFile().getPath();
 		if ( path.indexOf(".grf") == -1 ) path += ".grf";
 			
 		// Serialize Graph
 		try {						
 			pathname = path;
 			theGraph.setSaved(true);
 			
 			FileOutputStream fos = new FileOutputStream(path);
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 				
 			oos.writeObject(theGraph);
 			oos.flush();
 			oos.close();
 			
 			log.write("Graph Saved to: " + path);
 		}
 		catch (java.lang.Throwable e) {
 			if (e instanceof FileNotFoundException) {
 				this.showWarningPopup("Warning", "File Not Found: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 				return;
 			}
 			else if (e instanceof IOException) {
 				this.showWarningPopup("Warning", "IOException: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 				return;
 			}
 			else {
 				this.showWarningPopup("Warning", "General Error: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * View Menu - Memory Usage Item - Event Handler
 	 * (Handles what happens when someone clicks on Memory Usage)
 	 *
 	 * Shows the memory monitor in a dialog window.
 	 */
 	public void doShowMemory() {
 		if (memoryDialog != null && memoryDialog.isVisible()) return;
 		else {
 			memoryDialog = new JDialog(this, "Memory Usage Window");
 			WindowListener l = new WindowAdapter() {
 				public void windowClosing(WindowEvent e) { memoryMonitor.surf.stop(); }
 				public void windowDeiconified(WindowEvent e) { memoryMonitor.surf.start(); }
 				public void windowIconified(WindowEvent e) { memoryMonitor.surf.stop(); }
 			};
 			memoryDialog.addWindowListener(l);
 			memoryDialog.getContentPane().add(memoryMonitor, BorderLayout.CENTER);
 			memoryDialog.setSize(new Dimension(200,200));
 			memoryDialog.pack();
 
 			// Set Memory Dialog at 0,0 of desktop pane.
 			Point p = mainPain.getLocationOnScreen();
 			memoryDialog.setLocation(p);
 			memoryDialog.setVisible(true);
 			
 			// Start Thread
 			memoryMonitor.surf.start();
 		}
 	}
 
 	/**
 	 * This is called to instantiate a data object using a delimited string.
 	 * The form of the delimited string depends on the type of data object
 	 * stored in a given node.
 	 *
 	 * <p>This string that is passed is parsed by that
 	 * type's constructor and will throw exceptions if any errors occur.
 	 *
 	 */
 	private Data getData(String val) {
 		if (dataSpawn == null) {
 			// create data
 			try {				
 				dataSpawn = ((Data) Class.forName(namedWrapper).newInstance()).getInstance(val);
 			}
 			catch (Exception e) {
 				dataSpawn = null;
 				log.write("getData::GEdit - Unable to instantiate named data wrapper - ("+namedWrapper+") - using default!");
 			}
 
 			// use default wrapper?
 			if (dataSpawn == null) {
 				try {
 					dataSpawn = ((Data) Class.forName(DEFAULT_DATA_WRAPPER).newInstance()).getInstance(val);
 				}
 				catch (Exception e) {
 					// Unable to create data object - give up.
 					e.printStackTrace();
 					System.exit(1);
 				}
 			}
 			
 			return dataSpawn;
 		}
 		else return dataSpawn.getInstance(val);
 	}
 	
 	/**
 	 * Returns the entire log file to whoever wants it.
 	 */
 	public Log getLog() {
 		if (log != null) return log;
 		else {
 			log = new Log();
 			return log;
 		}
 	}
 	
 	/**
 	 * Returns an instance of the menu bar to whoever needs it.
 	 */
 	public MBI getMBI() {
 		return mbi;
 	}
 	
 	/**
 	 * Updates the log window when the log changes.
 	 * Only needs to do this if the window is visible.
 	 */
 	public void logChanged(EventObject e) {
 		// Replace entire log window if log is visible
 		if (logDialog.isVisible()) {
 			Object[] theLog = Log.getLog();
 			StringBuffer buf = new StringBuffer();
 			for (int i = 0; i < theLog.length; i++) {
 				buf.append((String) theLog[i]);
 			}
 
 			// Refresh Log
 			this.doLogDialog(buf.toString());
 		}
 		// Ignore any update events when window isn't visible since
 		// these are handled by the doLogWindow method.
 	}
 	
 	/**
 	 * This is where the magic begins...
 	 * @param argv The options
 	 */
 	public static void main (String[] argv) {
 		/*
 		// is there anything to do?
 		if (argv.length == 0) {
 			printUsage();
 			System.exit(1);
 		}
 		*/
 
 		// process arguments
 		for (int i = 0; i < argv.length; i++) {
 			String arg = argv[i];
 			if (arg.startsWith("-")) {
 				String option = arg.substring(1);
 				if (option.equals("d")) {
 					// get data wrapper name
 					if (++i == argv.length) {
 						System.err.println("error: Missing argument to -d option.");
 					}
 					namedWrapper = argv[i];				
 				}
 				if (option.equals("h")) {
 					printUsage();
 					continue;
 				}
 			}
 		}
 
 		// Set default look and feel.
 		try {
 			String osName = System.getProperty("os.name");
 			if (osName.indexOf("Linux") >= 0)
 				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
 			else if (osName.indexOf("Windows") >= 0)
 				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 			else if (osName.indexOf("Mac") >= 0)
 				UIManager.setLookAndFeel("com.apple.laf.AquaLookAndFeel");
 			else
 				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
 			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
 			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
 		}
 		catch (Exception exc) {
 			System.err.println("Error loading L&F: " + exc);
 		}
 		
 		// Ensure the event dispatch thread builds the instance.
 		Runnable handoff = new Runnable() {
 			public void run() {
 				GEdit ge = GEdit.getInstance();
 				ge.setVisible(true);
 			}			
 		};
 		SwingUtilities.invokeLater(handoff);
 	}
 	
 	/**
 	 * Prints the usage.
 	 */
 	private static void printUsage() {
 
 		System.err.println("usage: java dom.GraphDriver (options) uri ...");
 		System.err.println();
 
 		System.err.println("options:");
 		System.err.println("  -d name  Select data wrapper by name.");
 		System.err.println("  -h       This help screen.");
 		System.err.println();
 
 		System.err.println("defaults:");
 		System.err.println("  Data Wrapper:     " + DEFAULT_DATA_WRAPPER);
 	}
 	
 	/**
 	 * Displays a standard warning message which can be used to
 	 * alert the user of problems or critical messages.
 	 */
 	private void showWarningPopup(String title, Object msg, int type) {
 		Toolkit.getDefaultToolkit().beep();
 		JOptionPane.showMessageDialog(this, msg, title, type);			
 	}
 
 	// Inner Class Definitions:
 	/**
 	 * Implements an abstract table model used for displaying textual edge and nodes.
 	 *
 	 * Creation date: (4/16/2002 3:54:25 PM)
 	 * @author Randy Secrist
 	 */
 	public class GraphEditTableModel extends javax.swing.table.AbstractTableModel {
 		/**
 		 * Serial Version UID
 		 */
 		private static final long serialVersionUID = 5065578263838839432L;
 		
 		public static final int NODE_DATA_TABLE = 0;
 		public static final int EDGE_WEIGHT_TABLE = 1;		
 
 		private String[] colNames;
 		private Object[][] data;
 		private int tableType;
 		/**
 		 * GraphTableModel constructor comment.
 		 */
 		public GraphEditTableModel(int type) {
 			super();
 			this.tableType = type;
 			if (type == GEdit.GraphEditTableModel.EDGE_WEIGHT_TABLE) 
 				this.initEdgeTable();
 			else if (type == GEdit.GraphEditTableModel.NODE_DATA_TABLE)
 				this.initNodeTable();
 		}
 		/**
 		 * getColumnCount method comment.
 		 */
 		public int getColumnCount() {
 			return colNames.length;
 		}
 		public String getColumnName(int col) {
 			return colNames[col];
 		}
 		/**
 		 * getRowCount method comment.
 		 */
 		public int getRowCount() {
 			return data.length;
 		}
 		/**
 		 * getValueAt method comment.
 		 */
 		public Object getValueAt(int row, int col) {
 			return data[row][col];
 		}
 		private void initEdgeTable() {
 			String[] colNames = { "Edge", "Weight" };
 			this.colNames = colNames;
 
 			Edge[] edges = theGraph.getEdges();
 			Object[][] data = new Object[edges.length][2];
 			for (int i = 0; i < edges.length; i++) {
 				Node n1 = theGraph.getNode(edges[i].getSource());
 				Node n2 = theGraph.getNode(edges[i].getDest());
 				data[i][0] = "(" + n1.getData().getDisplayName() + ")" + " to " + "(" + n2.getData().getDisplayName() + ")";
 				data[i][1] = String.valueOf(edges[i].getWeight());
 			}
 
 			this.data = data;
 		}
 		private void initNodeTable() {
 			String[] colNames = { "ID", "Type", "String Representation" };
 			this.colNames = colNames;
 
 			Node[] nodes = theGraph.getNodes();
 			Object[][] data = new Object[nodes.length][3];
 			for (int i = 0; i < nodes.length; i++) {
 				Node n1 = nodes[i];
 				data[i][0] = new Integer(n1.getId());
 				data[i][1] = n1.getData().getClass().getName();				
 				data[i][2] = n1.getData().toString();
 			}
 
 			this.data = data;
 		}
 		/*
 		 * Don't need to implement this method unless there are
 		 * items in your table that are editable.
 		 */
 		public boolean isCellEditable(int row, int col) {
 			// Columns editable in an edge table.
 			if (tableType == EDGE_WEIGHT_TABLE) {
 				if (col == 1) {
 					return true;
 				}
 				else return false;
 			}
 			else if (tableType == NODE_DATA_TABLE) {
 				if (col == 2) {
 					return true;
 				}
 				else return false;
 			}
 			return false;
 		}
 		private void printDebugData() {
 			int numRows = getRowCount();
 			int numCols = getColumnCount();
 
 			for (int i = 0; i < numRows; i++) {
 				System.out.print("    row " + i + ":");
 				for (int j = 0; j < numCols; j++) {
 					System.out.print("  " + data[i][j]);
 				}
 				System.out.println();
 			}
 			System.out.println("--------------------------");
 		}
 		public void setValueAt(Object value, int row, int col) {
 			System.out.println(
 				"Setting value at "
 					+ row
 					+ ","
 					+ col
 					+ " to "
 					+ value
 					+ " (an instance of "
 					+ value.getClass()
 					+ ")");
 
 			data[row][col] = value;
 
 			if (tableType == EDGE_WEIGHT_TABLE) {
 				// Update the graph with new weight
 				if (theGraph != null) {
 					String s = (String) value;
 
 					try {
 						theGraph.modifyEdgeWeight(theGraph.getEdges()[row], Double.parseDouble(s));
 					}
 					catch (java.lang.Throwable e) {
 						StringWriter strWriter = new StringWriter();
 						e.printStackTrace(new PrintWriter(strWriter));
 						log.write("setValueAt::GraphEditTableModel - Negative Edge Detected: " + strWriter.toString());
 						showWarningPopup("Warning", "Edges must contain be positive numbers. - \n" + e.getLocalizedMessage(), JOptionPane.OK_OPTION);
 						return;
 					}
 					
 					// repaint windows
 					doSoftRefresh();
 				}
 			}
 			else if (tableType == NODE_DATA_TABLE) {
 				// Update the graph with new weight
 				if (theGraph != null) {
 					String s = (String) value;
 					try {
						theGraph.modifyData(theGraph.getNodes()[row], dataSpawn.getInstance(s));
 					}
 					catch (java.lang.Throwable e) {
 						StringWriter strWriter = new StringWriter();
 						e.printStackTrace(new PrintWriter(strWriter));
 						log.write("setValueAt::GraphEditTableModel - Duplicate Data Detected: " + strWriter.toString());
 						showWarningPopup("Warning", "Duplicate data types within a graph are not allowed. - \n" + e.getLocalizedMessage(), JOptionPane.OK_OPTION);
 						return;
 					}
 					
 				}
 			}
 				
 			fireTableCellUpdated(row, col);
 
 			System.out.println("New value of data:");
 			this.printDebugData();
 		}
 	}
 
 	/**
 	 * Implements an abstract table model used for edit weights, and
 	 * edit nodes tables.
 	 *
 	 * Creation date: (3/7/2002 3:54:25 PM)
 	 * @author Randy Secrist
 	 */
 	public class GraphInfoTableModel extends javax.swing.table.AbstractTableModel {
 		/**
 		 * Serial Version UID
 		 */
 		private static final long serialVersionUID = -7415820588752411153L;
 		
 		public static final int NODE_TABLE = 0;
 		public static final int EDGE_TABLE = 1;		
 
 		private String[] colNames;
 		private Object[][] data;
 		private int tableType;
 		/**
 		 * GraphInfoTableModel constructor comment.
 		 */
 		public GraphInfoTableModel(int type) {
 			super();
 			this.tableType = type;
 			if (type == GEdit.GraphInfoTableModel.EDGE_TABLE) 
 				this.initEdgeTable();
 			else if (type == GEdit.GraphInfoTableModel.NODE_TABLE)
 				this.initNodeTable();
 		}
 		/**
 		 * getColumnCount method comment.
 		 */
 		public int getColumnCount() {
 			return colNames.length;
 		}
 		public String getColumnName(int col) {
 			return colNames[col];
 		}
 		/**
 		 * getRowCount method comment.
 		 */
 		public int getRowCount() {
 			return data.length;
 		}
 		/**
 		 * getValueAt method comment.
 		 */
 		public Object getValueAt(int row, int col) {
 			return data[row][col];
 		}
 		private void initEdgeTable() {
 			String[] colNames = { "Edge (By Node ID)", "Island ID", "Weight"};
 			this.colNames = colNames;
 
 			Object[][] data = new Object[Globals.getInstance().getNumEdges()][3];
 			for (int i = 0; i < Globals.getInstance().getNumEdges(); i++) {
 				EdgeWrapper e = Globals.getInstance().getEdge(i);
 				data[i][0] = "(" + e.getEdge().getSource() + ")" + " to " + "(" + e.getEdge().getDest() + ")";
 				data[i][1] = new Integer(e.islandId);
 				data[i][2] = String.valueOf(e.getEdge().getWeight());
 			}
 
 			this.data = data;
 		}
 		private void initNodeTable() {
 			String[] colNames = { "ID", "Island ID", "Data Representation" };
 			this.colNames = colNames;
 	
 			Object[][] data = new Object[Globals.getInstance().getNumNodes()][3];
 			for (int i = 0; i < Globals.getInstance().getNumNodes(); i++) {
 				NodeWrapper n = Globals.getInstance().getNode(i);
 				data[i][0] = new Integer(n.getNode().getId());
 				data[i][1] = new Integer(n.islandId);
 				data[i][2] = n.getNode().getData().toString();
 			}
 
 			this.data = data;
 		}
 	}
 
 
 
 	/**
 	  * cascades the given internal frame based upon supplied count
 	  *
 	  * @param f the internal frame to cascade
 	  * @count the count to use in cascading the internal frame
 	  *
 	  * @return a Point object representing the location 
 	  *     assigned to the internal frame upon the virtual desktop
 	  */
 	private Point cascadeInternalFrame(JInternalFrame f, int count) {
 
 	    int windowWidth = f.getWidth();
 	    int windowHeight = f.getHeight();
 
 	    Rectangle viewP = mainPain.getBounds();
 
 	    // get # of windows that fit horizontally
 	    int numFramesWide = (viewP.width - windowWidth) / X_OFFSET;
 	    if (numFramesWide < 1) {
 	        numFramesWide = 1;
 	    }
 	    // get # of windows that fit vertically
 	    int numFramesHigh = (viewP.height - windowHeight) / Y_OFFSET;
 	    if (numFramesHigh < 1) {
 	        numFramesHigh = 1;
 	    }
 
 	    // position relative to the current viewport (viewP.x/viewP.y)
 	    // (so new windows appear onscreen)
 	    int xLoc = viewP.x + X_OFFSET * ((count + 1) - (numFramesWide - 1) * (count / numFramesWide));
 	    int yLoc = viewP.y + Y_OFFSET * ((count + 1) - numFramesHigh * (count / numFramesHigh));
 
 	    return new Point(xLoc, yLoc);
 
 	}
 
 	/**
 	 * Help Menu - About Item - Event Handler
 	 * (Handles what happens when someone clicks on About)
 	 */
 	public void doAbout() {
 		JDialog aboutDialog = new JDialog(this, "About Graph Edit");
 		aboutDialog.getContentPane().setLayout(new BorderLayout());
 		aboutDialog.setResizable(false);
 
 		JPanel northPanel = new JPanel(true);
 		JLabel logo;
 		try {
 			logo = new JLabel(this.getImageIcon("/images/Gedit32a.jpg", "About Icon"));
 		}
 		catch (Throwable e) {
 			log.write("Unable to load About Dialog Icon.\nPlease ensure resource directory exists in the application root directory.");
 			logo = new JLabel("GEDIT!");
 		}
 		northPanel.add(logo);
 
 		JPanel centerPanel = new JPanel(new ColumnLayout(), true);
 		centerPanel.setBorder(new TitledBorder(new EtchedBorder(), "Utah State University Contributors (2002)"));
 		
 		JLabel anthony = new JLabel("Anthonie Nichols - Facilitator");	
 		JLabel chad = new JLabel("Chad Coats - Manager");
 		JLabel karl = new JLabel("Karl Smith - Design");
 		JLabel randy = new JLabel("Randy Secrist - Design");
 		JLabel seth = new JLabel("Seth Humphries - Tool Smith");
 		
 		centerPanel.add(anthony);
 		centerPanel.add(chad);
 		centerPanel.add(karl);
 		centerPanel.add(randy);
 		centerPanel.add(seth);
 		
 		JPanel southPanel = new JPanel(new ColumnLayout(), true);
 		southPanel.setBorder(new TitledBorder(new EtchedBorder(), "Current Maintainer (2010)"));
 		
 		JLabel maintainer = new JLabel("Randy Secrist");
 		JLabel maintainer_info = new JLabel("(randy.secrist AT gmail.com)");
 		southPanel.add(maintainer);
 		southPanel.add(maintainer_info);
 		
 		aboutDialog.getContentPane().add(northPanel, BorderLayout.NORTH);
 		aboutDialog.getContentPane().add(centerPanel,BorderLayout.CENTER);
 		aboutDialog.getContentPane().add(southPanel, BorderLayout.SOUTH);
 			
 		// Set dialog in center of frame.
 		aboutDialog.setSize(new Dimension((int) centerPanel.getPreferredSize().getWidth()+100, (int) centerPanel.getPreferredSize().getHeight()+150));
 		Dimension parentSize = this.getSize();
 		Point parentLocation = this.getLocationOnScreen();
 		Dimension dlgSize = aboutDialog.getSize();
 		int newX = (int) ((parentLocation.getX()) + (parentSize.width / 2) - (dlgSize.getWidth() / 2));
 		int newY = (int) ((parentLocation.getY()) + (parentSize.height / 2) - (dlgSize.getHeight() / 2));
 		aboutDialog.setLocation(new Point(newX, newY));
 		aboutDialog.setVisible(true);
 	}
 
 	/**
 	 * Eddit Menu - Add Edge - Event Handler
 	 * (Handles what happens when someone clicks on Add Edge)
 	 */
 	public void doAddEdge() {
  		Node[] nodes = theGraph.getNodes();
 
  		// Messages
  		Object[] message = new Object[6]; 
  		message[0] = "Source Node:";
  		
  		JComboBox srcNodes = new JComboBox();
  		srcNodes.getAccessibleContext().setAccessibleName("node.source");
  		for (int i = 0; i < nodes.length; i++) {
 	 		srcNodes.addItem(nodes[i]);
  		}
  		message[1] = srcNodes;
  		
  		message[2] = "Destination Node:";
   		JComboBox destNodes = new JComboBox();
  		destNodes.getAccessibleContext().setAccessibleName("node.destination");
  		for (int i = 0; i < nodes.length; i++) {
 	 		destNodes.addItem(nodes[i]);
  		}
  		message[3] = destNodes;
 
  		message[4] = "Edge Weight:";
  		JTextField weightTxt = new JTextField();
  		message[5] = weightTxt;
  
  		// Options
  		String[] options = { 
 	 		"Ok", 
 	 		"Cancel"
 	 	};
  		int result = JOptionPane.showOptionDialog( 
 	 		this,                                       // the parent that the dialog blocks 
 	 		message,                                    // the dialog message array 
 	 		"Add Edge Option:",                         // the title of the dialog window 
 	 		JOptionPane.DEFAULT_OPTION,                 // option type 
  		    JOptionPane.QUESTION_MESSAGE,               // message type 
  		    null,                                       // optional icon, use null to use the default icon 
  		    options,                                    // options string array, will be made into buttons 
  		    options[0]                                  // option that should be made into a default button 
  		);
 
  		switch(result) { 
  		   case 0: // yes
  		     NodeWrapper n1 = new NodeWrapper((Node) srcNodes.getSelectedItem());
  		     NodeWrapper n2 = new NodeWrapper((Node) destNodes.getSelectedItem());
  		     this.doAddEdge(weightTxt.getText(), n1, n2);
  		     break; 
  		   case 1: // cancel
  		     break; 
  		   default: 
  		     break; 
  		}
  	}                              
 
 
 
 	/**
 	 * Edit Menu - Add Node Item - Event Handler
 	 * (Handles what happens when someone clicks on Add Node)
 	 */
 	public void doAddNode() {
 	        DataVisitor v = new SwingBridge();
 		String s = v.showInputDialog(dataSpawn, this);
 		if (s == null || s.length() <= 0) return;
 		Data obj = this.getData(s);
 		
 		// Present with a global list of nodes to connect it with below.
 		try {
 			theGraph.addNode(obj);
 			log.write("Node Added to Graph - " + obj.getDisplayName());			
 			this.doHardRefresh();
 		}
 		catch (Throwable e) {
 			e.printStackTrace();
 			this.showWarningPopup("Add New Node Error!", "We apologize but there was a problem adding a new node to the graph.\n\n" + e.getLocalizedMessage(), JOptionPane.OK_OPTION);
 		}
 	}
 
 	/**
 	 * View Menu - Balance Graph Item - Event Handler
 	 * (Handles what happens when someone clicks on Balance Graph)
 	 *
 	 * This reaches into each island window and toggles the thread
 	 * that runs each surfaces balance algorithm.
 	 */
 	public synchronized void doBalance() {
 		if (theGraph == null) return;
 		if (!balanceOn) balanceOn = true;
 		else balanceOn = false;
 
 		if (balanceOn) {
 			// Start all balance threads
 			JInternalFrame[] frames = mainPain.getAllFrames();
 			for (int i = 0; i < frames.length; i++) { // for each frame
 				Component[] components = frames[i].getContentPane().getComponents();
 				for (int j = 0; j < components.length; j++) { // for each component
 					if (components[j] instanceof GraphPanel) {
 						// Start each thread.
 						((GraphPanel) components[j]).surf.start();
 					}
 				}
 			}			
 		}
 		else {
 			// Stop all balance threads
 			JInternalFrame[] frames = mainPain.getAllFrames();
 			for (int i = 0; i < frames.length; i++) { // for each frame
 				Component[] components = frames[i].getContentPane().getComponents();
 				for (int j = 0; j < components.length; j++) { // for each component
 					if (components[j] instanceof GraphPanel) {
 						// Stop each thread.
 						((GraphPanel) components[j]).surf.stop();
 					}
 				}
 			}
 		}
 	}
 
 	public void doCascade() {
 	    JInternalFrame[] frames = mainPain.getAllFrames();
 	    JInternalFrame f;
 
 	    int frameCounter = 0;
 
 	    for (int i = frames.length - 1; i >= 0; i--) {
 	        f = frames[i];
 
 	        // don't include iconified frames in the cascade
 	        if (!f.isIcon()) {
 		        f.setSize(f.getPreferredSize());
 	            f.setLocation(cascadeInternalFrame(f, frameCounter++));
 	        }
 	    }
 	}
 
 	/**
 	 * This method clears and disposes all island windows.
 	 */
 	public void doClearGraph() {
 		// Wipe global (painted) nodes and edges
 		Globals.getInstance().clearAll();
 		
 		// Clear All SubWindows.
 		JInternalFrame[] frames = mainPain.getAllFrames();
 		for (int i = 0; i < frames.length; i++) {
 			Component[] components = frames[i].getContentPane().getComponents();
 			for (int j = 0; j < components.length; j++) {
 				//log.write(""+components[j]);
 				if (components[j] instanceof GraphPanel) {
 					
 					// Call stop to ensure thread is stopped.
 					((GraphPanel) components[j]).surf.stop();
 				}
 			}
 			mainPain.remove(frames[i]);
 			frames[i].dispose();
 		}
 
 		// Clear all view menu check box toggles.
 		mbi.clearGroupStates();
 
 		// Garbage Collect
 		Runtime.getRuntime().gc();
 
 		this.repaint();
 	}
 
 	/**
 	 * Restores the application to a new state with locked menus,
 	 * and updates algorithms menu for current state.
 	 *
 	 * WARNING! - THIS METHOD ALSO RESETS GLOBALS - BUT,
 	 * (GLOBALS DOES NOT RESET IT'S OWN GLOBAL ALGORITHM LIST)
 	 *
 	 * Returns false if the user wishes to cancel this closing event,
 	 * true otherwise.
 	 */
 	public boolean doClose() {
 		if (theGraph != null && !theGraph.getSaved()) {
 			int opt = JOptionPane.showConfirmDialog(this, "Do you want to save before doing this?", "Save Option", JOptionPane.YES_NO_CANCEL_OPTION);
 			if (opt == JOptionPane.YES_OPTION) {
 				this.doSave();
 			}
 			else if (opt == JOptionPane.CANCEL_OPTION) return false;
 		}
 		
 		theGraph = null;
 
 		// Clear All IslandWindows.
 		this.doClearGraph();
 
 		// Set Menu to locked state
 		this.processMenuState(MENU_LOCKED);
 		this.processAlgorithmMenuState();
 
 		// Reset Globals
 		Globals.getInstance().reset();
 		
 		// Garbage Collect
 		Runtime.getRuntime().gc();
 		return true;
 	}
 
 	/**
 	 * Displays an HTML Help File.
 	 *
 	 * <p>The HTML file man include hyperlinks since HyperLinkActivator
 	 * will update this pane when the mouse clicks on a link.
 	 *
 	 * <p>However because this only displays using HTML 3.2, the html
 	 * needs to be pretty basic and can't implement and javascript.
 	 * (Not unless U want to code a JS parser...)
 	 */
 	public void doContents() {
 		Runnable worker = new Runnable() {
             public void run() {
             	/*
             	Display display = new Display(new DeviceData());
             	SWTBrowser browser = new SWTBrowser();
         		browser.setTitle("GEdit Contents");
         		browser.setHomeUrl("http://www.secristfamily.com/randy/GEdit/html/contents.html");
         		browser.createSShell();
         		browser.open();
         		
         		while (!browser.isDisposed()) {
         			if (!display.readAndDispatch())
         				display.sleep();
         		}
         		display.dispose();
         		*/
             }
         };
 		SwingUtilities.invokeLater(worker);
 	}
 
 	/**
 	 * Edit Menu - Delete Island Item - Event Handler
 	 * (Handles what happens when someone clicks on Delete Island)
 	 */
 	public void doDeleteIsland() {
 		JInternalFrame frame = this.getSelectedFrame();
 			
 		Component[] c = frame.getContentPane().getComponents();
 		for (int i = 0; i < c.length; i++) {
 			if (c[i] instanceof GraphPanel) {
 				
 				int frameId = ((GraphPanel)c[i]).surf.islandId;
 				theGraph.removeAllnodes(frameId);
 				// Clean up globals:
 				for (int j = 0; j < Globals.getInstance().getNumNodes(); j++) {
 					NodeWrapper nw = Globals.getInstance().getNode(j);
 					if (frameId == nw.islandId) {
 						Globals.getInstance().removeNode(nw);
 						j--;
 					}
 				}
 
 				log.write("Graph Island " + (frameId+1) + " was deleted.");
 				
 				// Stop thread associated with island window.
 				((GraphPanel) c[i]).surf.stop();
 			}
 		}
 		frame.dispose();		
 	}
 
 
 	/**
 	 * Displays an excel like table which allows a user to edit
 	 * edge weights.
 	 *
 	 * When this window is open it should disable the parent frame
 	 * to prevent this tables state from being corrupted from further
 	 * graph changes.
 	 */
 	public void doEdgeWeights() {
 		JDialog edgeWeightWin = new JDialog(this);
 		edgeWeightWin.setTitle("Edit Edge Weights:");
 		edgeWeightWin.setLocationRelativeTo(this);
 
 		// Specify window behavior.
 		// (This needs to set enabled and disabled (of the parent frame).)
 		edgeWeightWin.addWindowListener(new WindowAdapter() {
 			boolean gotFocus = false;
 			public void windowClosing(WindowEvent we) {
 				setEnabled(true);
 				we.getWindow().dispose();
 			}
 			public void windowOpened(WindowEvent we) {
 				setEnabled(false);
 			}
 		});
 		
 		edgeWeightWin.setSize(new Dimension(400,300));
 		edgeWeightWin.setBackground(Color.white);
 		
 		// Add Table & Set Visible
 		JTable table = new JTable(new GraphEditTableModel(GraphEditTableModel.EDGE_WEIGHT_TABLE));
 		table.setPreferredScrollableViewportSize(new Dimension(300,200));
 
 		JScrollPane tablePane = new JScrollPane(table);
 
 		// Setup scroll pane & add to dialog.
 		tablePane.setPreferredSize(table.getPreferredScrollableViewportSize());
 		tablePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		tablePane.setBorder(new TitledBorder(new EtchedBorder(), "Edge Weight Table"));
 		edgeWeightWin.getContentPane().add(tablePane,BorderLayout.CENTER);
 		edgeWeightWin.setVisible(true);
 	}
 
 	/**
 	 * Displays an excel like table which allows a user to edit
 	 * edge weights.
 	 *
 	 * When this window is open it should disable the parent frame
 	 * to prevent this tables state from being corrupted from further
 	 * graph changes.
 	 */
 	public void doEditNodeData() {
    	    JDialog nodeDataWin = new JDialog(this);
 	    nodeDataWin.setTitle("Edit Node Data:");
 		    
 	    // Specify window behavior.
 	    // (This needs to set enabled and disabled (of the parent frame).)
 	    nodeDataWin.addWindowListener(new WindowAdapter() {
 	        boolean gotFocus = false;
 	        public void windowClosing(WindowEvent we) {
 	            setEnabled(true);
 	            we.getWindow().dispose();
 	        }
 	        public void windowOpened(WindowEvent we) {
 	            setEnabled(false);
 	        }
 	    });
 
 	    nodeDataWin.setSize(new Dimension(550, 300));
 	    nodeDataWin.setBackground(Color.white);
 
 	    // Add Table & Set Visible
 	    JTable table = new JTable(new GraphEditTableModel(GraphEditTableModel.NODE_DATA_TABLE));
 	    table.setPreferredScrollableViewportSize(new Dimension(300, 200));
 
 	    JScrollPane tablePane = new JScrollPane(table);
 
 	    // Setup scroll pane & add to dialog.
 	    tablePane.setPreferredSize(table.getPreferredScrollableViewportSize());
 	    tablePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 	    tablePane.setBorder(new TitledBorder(new EtchedBorder(), "Node Data Table"));
 	    nodeDataWin.getContentPane().add(tablePane, BorderLayout.CENTER);
 	    //GEdit.getInstance().setEnabled(false);
 	    nodeDataWin.setVisible(true);
 	}
 
 	/**
 	 * File Menu - Export Item - Event Handler
 	 * (Handles what happens when someone clicks on Export)
 	 */
 	public void doExport() {
 		if (theGraph == null) {
 			this.showWarningPopup("Warning", "Nothing to Export!", JOptionPane.OK_OPTION);			
 			return;
 		}
 		
 		Frame saveFrame = new Frame();
 		JFileChooser jfc = new JFileChooser();
 		jfc.setFileFilter(new FileListFilter(FileListFilter.DELIMIT));
 		int option = jfc.showSaveDialog(saveFrame);
 		if (option == JFileChooser.CANCEL_OPTION) return;
 
 		String path = jfc.getSelectedFile().getPath();
 		if ( path.indexOf(".txt") == -1 ) path += ".txt";
 			
 		// Export Graph
 		try {						
 			theGraph.setSaved(true);
 
 			BufferedWriter bWriter = new BufferedWriter(new FileWriter(path));
 			theGraph.exportGraph(bWriter);
 			bWriter.flush();
 			bWriter.close();
 			log.write("Graph Exported to: " + path);
 		}
 		catch (java.lang.Throwable e) {
 			if (e instanceof FileNotFoundException) {
 				this.showWarningPopup("Warning", "File Not Found: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 				return;
 			}
 			else if (e instanceof IOException) {
 				this.showWarningPopup("Warning", "IOException: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 				return;
 			}
 			else {
 				this.showWarningPopup("Warning", "General Error: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Displays an HTML Help File.
 	 *
 	 * <p>The HTML file man include hyperlinks since HyperLinkActivator
 	 * will update this pane when the mouse clicks on a link.
 	 *
 	 * <p>However because this only displays using HTML 3.2, the html
 	 * needs to be pretty basic and can't implement and javascript.
 	 * (Not unless U want to code / insert a JS parser...)
 	 */
 	public void doGlossary() {
 		Runnable worker = new Runnable() {
             public void run() {
             	/*
             	Display display = new Display(new DeviceData());
             	SWTBrowser browser = new SWTBrowser();
         		browser.setTitle("GEdit Glossary");
         		browser.setHomeUrl("http://www.secristfamily.com/randy/GEdit/html/glossary.html");
         		browser.createSShell();
         		browser.open();
         		
         		while (!browser.isDisposed()) {
         			if (!display.readAndDispatch())
         				display.sleep();
         		}
         		display.dispose();
         		*/
             }
         };
 		SwingUtilities.invokeLater(worker);
 	}
 
 	/**
 	 * Help Menu - Graph Info Item - Event Handler
 	 * (Handles what happens when someone clicks on Graph Info)
 	 */
 	public void doGraphInfo() {
 		JDialog graphDialog = new JDialog(this, "Graph Information");
 		graphDialog.setModal(true);
 		graphDialog.getContentPane().setLayout(new BorderLayout());
 		graphDialog.setResizable(false);
 
 		JPanel panel = new JPanel(new ColumnLayout(), true);
 		panel.setBorder(new TitledBorder(new EtchedBorder(), "Graph State Information"));
 
 		if (theGraph != null) {
 			// Restore Globals:
 			if (Globals.getInstance().getNumNodes() > 0) this.doHardRefresh();
 			
 			String direction;
 			String weight;
 			String balance;
 			if (theGraph.getDirected()) direction = new String("Directed");
 			else direction = new String ("Not Directed");
 			if (theGraph.getWeighted()) weight = new String("Weighted");
 			else weight = new String ("Not Weighted");
 			if (balanceOn) balance = new String("On");
 			else balance = new String ("Off");
 
 			// Current time:
 			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
 			String currentTime = df.format(new java.util.Date());
 
 			JPanel labelPanel = new JPanel(new ColumnLayout(), true);
 			labelPanel.setBorder(new TitledBorder(new EtchedBorder(), "General Info"));			
 			JLabel dir = new JLabel("Graph Direction: " + direction);
 			JLabel wgt = new JLabel("Graph Weighted Status: " + weight);
 			JLabel blnc = new JLabel("Balance Algorithm: " + balance);
 			JLabel nodeNum = new JLabel("Number of nodes: " + theGraph.getNodes().length);
 			JLabel edgeNum = new JLabel("Number of edges: " + theGraph.getEdges().length);
 			JLabel algsNum = new JLabel("Number of Loaded Algorithms: " + Globals.getInstance().getAlgorithms().length);
 			JLabel date = new JLabel("Current Time: " + currentTime);
 			labelPanel.add(dir);
 			labelPanel.add(wgt);
 			labelPanel.add(blnc);
 			labelPanel.add(nodeNum);
 			labelPanel.add(edgeNum);
 			labelPanel.add(algsNum);
 			labelPanel.add(date);
 
 			// Build node and edge tables
 			JTable nodeTable = new JTable(new GraphInfoTableModel(GraphInfoTableModel.NODE_TABLE));
 			JTable edgeTable = new JTable(new GraphInfoTableModel(GraphInfoTableModel.EDGE_TABLE));
 			nodeTable.setPreferredScrollableViewportSize(new Dimension(325,100));
 			edgeTable.setPreferredScrollableViewportSize(new Dimension(325,100));
 			JScrollPane nodeScrollPane = new JScrollPane(nodeTable);
 			JScrollPane edgeScrollPane = new JScrollPane(edgeTable);
 			nodeScrollPane.setPreferredSize(nodeTable.getPreferredScrollableViewportSize());
 			edgeScrollPane.setPreferredSize(edgeTable.getPreferredScrollableViewportSize());
 			nodeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			edgeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			nodeScrollPane.setBorder(new TitledBorder(new EtchedBorder(), "Node Data"));
 			edgeScrollPane.setBorder(new TitledBorder(new EtchedBorder(), "Edge Data"));
 
 			panel.add(labelPanel);
 			panel.add(nodeScrollPane);
 			panel.add(edgeScrollPane);
 		}
 		else {
 			JLabel msg = new JLabel("A Graph has not been loaded!");
 			panel.add(msg);
 		}
 		
 		graphDialog.getContentPane().add(panel,BorderLayout.NORTH);
 			
 		// Set dialog in center of frame.
 		graphDialog.setSize(new Dimension((int) panel.getPreferredSize().getWidth()+50, (int) panel.getPreferredSize().getHeight()+50));
 		Dimension parentSize = this.getSize();
 		Point parentLocation = this.getLocationOnScreen();
 		Dimension dlgSize = graphDialog.getPreferredSize();
 		int newX = (int) ((parentLocation.getX()) + (parentSize.width / 2) - (dlgSize.getWidth() / 2));
 		int newY = (int) ((parentLocation.getY()) + (parentSize.height / 2) - (dlgSize.getHeight() / 2));
 		graphDialog.setLocation(new Point(newX, newY));	
 		graphDialog.setVisible(true);
 	}
 
 	/**
 	 * This method clears and redraws all windows using new graph data.
 	 *
 	 * This re-synchs what has been painted with the graph state.
 	 */
 	public void doHardRefresh() {		
 		// Clear Graph
 		this.doClearGraph();
 		
 		// Update State Variables:
 		this.updatePreferences();
 		mbi.getJMenuBar().getMenu(2).getItem(0).setSelected(properties.getBalanceStatus());
 
 		// Restore All Windows
 		for (int i = 0; i < theGraph.getIslandcount(); i++) {
 			this.drawIsland(i);					
 		}
 
 		// Restore Global Lists
 		for (int i = 0; i < theGraph.getIslandcount(); i++) {
 			this.restoreGlobals(i, theGraph.getNodes(i), theGraph.getEdges(i));
 		}
 
 		// Update Menu Bar's
 		this.processAlgorithmMenuState();
 
 		//System.out.println(this.printGraphState());
 	}
 
 	/**
 	 * File Menu - Import Item - Event Handler
 	 * (Handles what happens when someone clicks on Import)
 	 */
 	public void doImport() {
 		if (theGraph != null && !theGraph.getSaved()) {
 			int opt = JOptionPane.showConfirmDialog(this, "Do you want to save the open graph first?", "Import Graph Option", JOptionPane.YES_NO_CANCEL_OPTION);
 			if (opt == JOptionPane.YES_OPTION) {
 				this.doSave();
 			}
 			else if (opt == JOptionPane.CANCEL_OPTION) return;
 		}
 		
 		Frame openFrame = new Frame();
 		JFileChooser jfc = new JFileChooser();
 		jfc.setFileFilter(new FileListFilter(FileListFilter.DELIMIT));
 		int option = jfc.showOpenDialog(openFrame);
 		if (option == JFileChooser.CANCEL_OPTION) return;
 		
 		String path = jfc.getSelectedFile().getPath();	
 		try {
 			BufferedReader bReader = new BufferedReader(new FileReader(path));
 			theGraph = Graph.importGraph(bReader);
 			bReader.close();
 			log.write("Graph Imported from: " + path);
 		}
 		catch (java.lang.Throwable e) {
 			StringWriter strWriter = new StringWriter();
 			e.printStackTrace(new PrintWriter(strWriter));
 			if (e instanceof IOException) {
 				this.showWarningPopup("Warning", "We apologize but a Graph Import error has occured: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);
 				log.write("doImport::GEdit - IOException - " + strWriter.toString());
 				return;
 			}
 		}
 		
 		// Update Menu Bar
 		this.processMenuState(MENU_EDITABLE);
 		this.processAlgorithmMenuState();
 
 		// Open Island Windows
 		this.doHardRefresh();
 	}
 	
 	public void doLogDialog() {
 		this.doLogDialog(null);
 	}
 
 	/**
 	 * View Menu - Log Window Item - Event Handler
 	 * Handles the Log Window Menu Item Event.
 	 *
 	 * <p>WARNING! - Never write to the log file in this method
 	 * since it is called by the log action listener, this would
 	 * result in an infite loop.</p>
 	 * 
 	 * <p>Since a UI Model is updated, this methos runs within
 	 * the Event Dispatch Thread.  Normal repaint operations
 	 * (outside of models) are safe, and don't need to do this.</p>
 	 */
 	public void doLogDialog(final String text) {
 		Runnable handoff = new Runnable() {
 			public void run() {
 				if (text != null) {
 					logTxt.setText(text);
 				}
 				if (logDialog.isVisible()) {
 					// Refresh
 					// Can also do setVisible false / true combo.
 					logDialog.repaint();
 				}
 				else {
 					logDialog.setTitle("GEdit Log Window");
 					logDialog.setSize(new Dimension(475,300));
 					logDialog.getContentPane().setLayout(new BorderLayout());
 					logTxt.setEditable(false);
 					
 					/*
 					// Set Font (may want to globalize this)
 					GraphicsEnvironment gEnv = 
 						GraphicsEnvironment.getLocalGraphicsEnvironment();
 					String envfonts[] = gEnv.getAvailableFontFamilyNames();
 					boolean setFont = false;
 					for (String font : envfonts) {
 						if (font.equals("GE Inspira")) {
 							logTxt.setFont(new Font(font, Font.PLAIN, 12));
 							setFont = true;
 							break;
 						}
 					}
 					if (!setFont) {
 						logTxt.setFont(new Font(Font.SERIF, Font.PLAIN, 12));
 					}
 					*/
 					
 					logTxt.setFont(new Font(Font.SERIF, Font.PLAIN, 12));
 					logTxt.setLineWrap(true);
 					logTxt.setWrapStyleWord(true);
 
 					// Insert Entire Log
 					Object[] theLog = Log.getLog();
 					StringBuffer buf = new StringBuffer();
 					for (int i = 0; i < theLog.length; i++) {
 						buf.append((String) theLog[i]);
 					}		
 					logTxt.setText(buf.toString());
 
 					// Build Buttons
 					JPanel buttons = new JPanel(true);
 					buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
 					JButton clear = new JButton("Clear");
 					clear.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent act) {
 							log.clear();
 						}
 					});
 					buttons.add(clear);
 					JButton ok = new JButton("OK");
 					ok.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent act) {
 							logDialog.setVisible(false);
 						}
 					});
 					buttons.add(ok);
 					logDialog.getRootPane().setDefaultButton(ok);
 					
 					// Setup scroll pane & add to dialog.
 					logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 					logScrollPane.setPreferredSize(new Dimension(250, 250));
 					logScrollPane.setBorder(new TitledBorder(new EtchedBorder(), "Log Window"));
 					logDialog.getContentPane().add(logScrollPane, BorderLayout.CENTER);
 					logDialog.getContentPane().add(buttons, BorderLayout.SOUTH);
 					
 					// Set dialog in center of screen.
 					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 					Dimension dlgSize = logDialog.getSize();
 					int newX = (int) ((screenSize.width / 2) - (dlgSize.getWidth() / 2));
 					int newY = (int) ((screenSize.height / 2) - (dlgSize.getHeight() / 2));
 					logDialog.setLocation(new Point(newX, newY));	
 					logDialog.setVisible(true);
 				}
 			}
 		};
 		SwingUtilities.invokeLater(handoff);
 	}
 
 	/**
 	 * Edit Menu - Preferences Item - Event Handler
 	 * (Handles what happens when someone clicks on Prefernces)
 	 */
 	public void doPreferences() {
 		properties.setVisible(true);
 	}
 
 	/**
 	 * Edit Menu - Remove Edge Item - Event Handler
 	 * (Handles what happens when someone clicks on Remove Edge)
 	 */
 	public void doRemoveEdge() {
 		Edge[] edges = theGraph.getEdges();
  		// Messages 		
  		Object[] message = new Object[1]; 
 
  		JComboBox edgeSelection = new JComboBox();
  		edgeSelection.getAccessibleContext().setAccessibleName("edge.list");
  		for (int i = 0; i < edges.length; i++) {
 	 		Node n1 = theGraph.getNode(edges[i].getSource());
 	 		Node n2 = theGraph.getNode(edges[i].getDest());
 	 		String s = new String(n1.getData().getDisplayName() + " to " + n2.getData().getDisplayName());
 	 		edgeSelection.addItem(s);
  		}
  		message[0] = edgeSelection;
 		
  		// Options
  		String[] options = { 
 	 		"Ok", 
 	 		"Cancel"
 	 	};
  		int result = JOptionPane.showOptionDialog( 
 	 		this,                                       // the parent that the dialog blocks 
 	 		message,                                    // the dialog message array 
 	 		"Remove Edge Option:",                      // the title of the dialog window 
 	 		JOptionPane.DEFAULT_OPTION,                 // option type 
  		    JOptionPane.QUESTION_MESSAGE,               // message type 
  		    null,                                       // optional icon, use null to use the default icon 
  		    options,                                    // options string array, will be made into buttons 
  		    options[0]                                  // option that should be made into a default button 
  		);
 
  		try {
 	 		// Validate and Add
 	 		switch(result) { 
 	 		   case 0: // yes
 	 		     int index = edgeSelection.getSelectedIndex();
 	 		     theGraph.removeEdge(edges[index]);
 	 		     this.doHardRefresh(); 
 	 		     break; 
 	 		   case 1: // cancel
 	 		     break; 
 	 		   default: 
 	 		     break; 
 	 		}
  		}
  		catch (Exception e) {
 	 		log.write("doRemoveEdge::GEdit - Failure to add Edge - " + e.getMessage());
 			this.showWarningPopup("Warning", "Failure to add Edge:\n\n" + e.getMessage(), JOptionPane.OK_OPTION);			
  		}
 	}
 
 	/**
 	 * Edit Menu - Remove Node Item - Event Handler
 	 * (Handles what happens when someone clicks on Remove Node)
 	 */
 	public void doRemoveNode() {
 		NodeWrapper temp = Globals.getInstance().getSelectedNode();
 		if (temp == null) {
 			this.showWarningPopup("Remove Node", "You must first select a node to remove.", JOptionPane.OK_OPTION);
 			return;
 		}
 		
 		
 		if (balanceOn) { // Halt threads while this is done.
 			this.doBalance();  // shuts off
 			Node n = temp.getNode();
 			Globals.getInstance().setSelectedNode(null);
 			theGraph.removeNode(n.getId());
 			this.doHardRefresh();
 			// Hard refresh restores preferences - which sets balance for us.
 			//this.doBalance();  // turns back on
 		}
 		else {
 			Node n = temp.getNode();
 			Globals.getInstance().setSelectedNode(null);
 			theGraph.removeNode(n.getId());
 			this.doHardRefresh();
 		}
 	}
 	
 	public void doRunAlgorithm(ActionEvent e) {
 		String theAction = e.getActionCommand();
 		try {
 			Algorithm[] algorithms = Globals.getInstance().getAlgorithms();
 			for(int i = 0; i < algorithms.length; i++) {
 				if(theAction.equalsIgnoreCase(algorithms[i].getMenuName())) {
 					this.showTraversalDialog(algorithms[i].getMenuName(), algorithms[i].runMe(theGraph));					
 				}
 			}
 		}
 		catch (java.lang.Throwable ex) {
 			StringWriter strWriter = new StringWriter();
 			ex.printStackTrace(new PrintWriter(strWriter));
 			log.write(theAction + "\n" + strWriter.toString());
 			this.showWarningPopup(theAction + " Error!", "We apologize but there was a problem running this algorithm on the graph.\n\nPlease contact support for help.", JOptionPane.OK_OPTION);
 			return;
 		}
 	}
 
 	/**
 	 * This method merely repaints the subwindows.  It does not
 	 * refresh with new graph data.
 	 */
 	public void doSoftRefresh() {
 		// Refresh All SubWindows
 		JInternalFrame[] frames = mainPain.getAllFrames();
 		for (int i = 0; i < frames.length; i++) {
 			frames[i].getContentPane().setVisible(false);
 			frames[i].getContentPane().setVisible(true);
 		}
 	}
 
 	public void doTile() {
 	    Rectangle viewP = mainPain.getBounds();
 
 	    int totalNonIconFrames = 0;
 
 	    JInternalFrame[] frames = mainPain.getAllFrames();
 
 	    for (int i = 0; i < frames.length; i++) {
 	        if (!frames[i].isIcon()) { // don't include iconified frames...
 	            totalNonIconFrames++;
 	        }
 	    }
 
 	    int curCol = 0;
 	    int curRow = 0;
 	    int i = 0;
 
 	    if (totalNonIconFrames > 0) {
 	        // compute number of columns and rows then tile the frames
 	        int numCols = (int) Math.sqrt(totalNonIconFrames);
 
 	        int frameWidth = viewP.width / numCols;
 
 	        for (curCol = 0; curCol < numCols; curCol++) {
 	            int numRows = totalNonIconFrames / numCols;
 	            int remainder = totalNonIconFrames % numCols;
 
 	            if ((numCols - curCol) <= remainder) {
 	                numRows++; // add an extra row for this guy
 	            }
 
 	            int frameHeight = viewP.height / numRows;
 
 	            for (curRow = 0; curRow < numRows; curRow++) {
 	                while (frames[i].isIcon()) { // find the next visible frame
 	                    i++;
 
 	                }
 
 	                frames[i].setBounds(
 	                    curCol * frameWidth,
 	                    curRow * frameHeight,
 	                    frameWidth,
 	                    frameHeight);
 	                i++;
 	            }
 	        }
 
 	    }
 	}
 
 	/**
 	 * Reads the event and sets the surface edgeLabel parameter
 	 * to match the action event string.  This has the effect of
 	 * toggling which label is displayed on the surface.
 	 */
 	public void doToggleViewState(ActionEvent e) {
 		String theAction = e.getActionCommand();
 		boolean nodeUpdated = false;
 		boolean edgeUpdated = false;
 		if (theAction.equalsIgnoreCase(MBI.NODE_NAME) ||
 			theAction.equalsIgnoreCase(MBI.NODE_ID)) {
 				mbi.getNodeList().setGroupState((JCheckBoxMenuItem) e.getSource());
 				this.nodeViewMenuCheckBoxState = theAction;
 				nodeUpdated = true;
 		}
 		else if (theAction.equalsIgnoreCase(MBI.EDGE_STRESS) ||
 				 theAction.equalsIgnoreCase(MBI.EDGE_WEIGHTS)) {
 				mbi.getEdgeList().setGroupState((JCheckBoxMenuItem) e.getSource());
 				this.edgeViewMenuCheckBoxState = theAction;
 				edgeUpdated = true;
 		}
 		
 		JInternalFrame[] frames = mainPain.getAllFrames();
 		for (int i = 0; i < frames.length; i++) {
 			Component[] components = frames[i].getContentPane().getComponents();
 			for (int j = 0; j < components.length; j++) {
 				if (components[j] instanceof GraphPanel) {
 					if (nodeUpdated) {
 						// Change all components surf.nodeLabel.
 						GraphPanel p = (GraphPanel) components[j];
 
 						// if label is equal to the action event.
 						if (p.surf.nodeLabel.equalsIgnoreCase(theAction))
 							p.surf.nodeLabel = "";
 						else
 							p.surf.nodeLabel = theAction;
 					}
 					else if (edgeUpdated) {
 						// Change all components surf.edgeLabel.
 						GraphPanel p = (GraphPanel) components[j];
 
 						// if label is equal to the action event.
 						if (p.surf.edgeLabel.equalsIgnoreCase(theAction))
 							p.surf.edgeLabel = "";
 						else
 							p.surf.edgeLabel = theAction;
 					}
 				}
 			}
 		}
 		this.doSoftRefresh();
 	}
 
 	/**
 	 * Draws an island window to the screen.  In process, this
 	 * has to create a graph surface, and an island window to place
 	 * that surface into.
 	 */
 	private synchronized void drawIsland(int id) {
 		// Create Island Surface
 		GraphPanel gPanel = new GraphPanel(id);
 
 		// Drop Island Surface in Subwindow
 		IslandWindow iw = new IslandWindow(id, "Graph Island - " + (id+1), mainPain);
 		iw.setPreferredSize(preferredSurfaceSize);
 		iw.getContentPane().add(gPanel, BorderLayout.CENTER);
 		iw.pack();
 		mainPain.add(iw);
 		iw.setVisible(true);
 
 		// Set State Variables:
 		mbi.getNodeList().setGroupState(nodeViewMenuCheckBoxState); gPanel.surf.nodeLabel = nodeViewMenuCheckBoxState;
 		mbi.getEdgeList().setGroupState(edgeViewMenuCheckBoxState); gPanel.surf.edgeLabel = edgeViewMenuCheckBoxState;			
 		if (theGraph.getDirected()) gPanel.surf.directed = true;
 		if (balanceOn) gPanel.surf.start();
 
 		this.doTile();
 	}
 
 	/**
 	 * Returns a instance of GEdit.
 	 * @return A instance of GEdit.
 	 */
 	public static synchronized GEdit getInstance() {
 		if (_instance == null) {
 			_instance = new GEdit();
 			return _instance;
 		}
 		else
 			return _instance;
 	}
 	
 	/**
 	 * Returns the size of the desktop pane.
 	 */
 	Dimension getDesktopPaneSize() {
 	    return mainPain.getSize();
 	}
 
 	/**
 	 * Returns the specified surface size that GEdit is telling
 	 * the surface to be.
 	 */
 	public Dimension getSurfaceSize() {
 		return preferredSurfaceSize;
 	}
 
 	public String printGraphState() {
 		StringBuffer buf = new StringBuffer();	
 		buf.append("Internal Graph Composition:\n");
 		buf.append("--------------------------\n");
 		buf.append("Nodes:\n");
 		buf.append("Name\tIsland Id\n");
 		for (int i = 0; i < Globals.getInstance().getNumNodes(); i++) {
 			NodeWrapper n = Globals.getInstance().getNode(i);
 			buf.append(n.getNode().getData().getDisplayName() + '\t' + n.islandId + '\n');
 		}
 		
 		buf.append("\n--------------------------\n");
 		buf.append("Edges:\n");
 		buf.append("Edge\tWeight\tIsland Id\n");
 		for (int i = 0; i < Globals.getInstance().getNumEdges(); i++) {
 			EdgeWrapper e = Globals.getInstance().getEdge(i);
 			buf.append(theGraph.getNode(e.getEdge().getSource()).getData().getDisplayName() + "::" + theGraph.getNode(e.getEdge().getDest()).getData().getDisplayName() + '\t' + e.getEdge().getWeight() + '\t' + e.islandId + '\n');
 		}
 		return buf.toString();
 	}
 
 	/**
 	 * Determines which algorithms can be run by the current graph
 	 * state and updates the menu by asking each algorithm if it
 	 * can run the current graph.
 	 */
 	private void processAlgorithmMenuState() {
 		Algorithm[] algorithms = Globals.getInstance().getAlgorithms();
 		JMenu algMenu = mbi.getJMenuBar().getMenu(4);
 		
 		if (theGraph != null) {
 			for (int i = 0; i < algorithms.length; i++) {
 				if (algorithms[i].works(theGraph))
 					algMenu.getItem(i).setEnabled(true);
 				else algMenu.getItem(i).setEnabled(false);
 			}
 		}
 		else {
 			for (int i = 0; i < algorithms.length; i++) {
 				algMenu.getItem(i).setEnabled(false);
 			}
 		}
 	}
 
 	/**
 	 * Boolean toggle of static menu items.
 	 * Calling this once disables a bunch of menu items,
 	 * calling it twice re-enables the same ones.
 	 */ 
 	public void processMenuState(int NEW_STATE) {
 		if (MENU_STATE == NEW_STATE) return;
 
 		MENU_STATE = NEW_STATE;
 		
 		// Update file menu
 		int[] i = {2,4,5,8};
 		this.setEnableDisableMenuItems(0, i);
 		// Update edit menu
 		int[] j = {0,1,3,4,5,6,8,9};
 		this.setEnableDisableMenuItems(1, j);
 		// Update view menu
 		int[] k = {0,2,3,5,6};
 		this.setEnableDisableMenuItems(2, k);
 	}
 
 	/**
 	 * Rebuilds the global node and edge lists which are used
 	 * by the GraphSurface to paint the nodes.
 	 */
 	private void restoreGlobals(int id, Node[] incNodes, Edge[] incEdges) {
 		// Unselect any selected nodes:
 		Globals.getInstance().setSelectedNode(null);
 
 		Dimension mySurfaceSize = this.getIslandWindowSurfaceSize(id);
 		if (mySurfaceSize == null) mySurfaceSize = preferredSurfaceSize;
 
 		// nodes
 		// remove null
 		for (int i = 0; i < incNodes.length; i++) {
 			if (incNodes[i] != null) {				
 				NodeWrapper n = new NodeWrapper(incNodes[i]);				
 				n.x = this.getRandomIntBetween(mySurfaceSize.width-20, 20);
 				n.y = this.getRandomIntBetween(mySurfaceSize.height-20, 20);
 				n.islandId = id;				
 				Globals.getInstance().addNode(n);
 			}
 		}
 
 		// edges
 		// remove null
 		for (int j = 0; j < incEdges.length; j++) {
 			if (incEdges[j] != null) {
 				EdgeWrapper e = new EdgeWrapper(incEdges[j]);
 				e.from = e.getEdge().getSource();
 				e.to = e.getEdge().getDest();
 				e.len = 150;
 				e.islandId = id;
 				Globals.getInstance().addEdge(e);
 			}
 		}
 	}
 
 	/**
 	 * This references Menu's and MenuItem's directly according to
 	 * the order in which they were added in MBI's constructor.
 	 *
 	 * This method merely switches a menu item, or a group of menu
 	 * items on or off.  It does not call, or update any other variables.
 	 *
 	 * @see GEdit#processMenu(int)
 	 * @see MBI 
 	 */
 	private void setEnableDisableMenuItems(int menu, int[] toChange) {
 		JMenuBar mBar = mbi.getJMenuBar();
 		
 		// Stay within mBar array
 		if (menu < 0 || menu >= mBar.getMenuCount()) return;
 		
 		JMenu editMenu = mBar.getMenu(menu);
 
 		for (int i = 0; i < toChange.length; i++) {
 			// Stay within item count
 			if (toChange[i] < 0 || toChange[i] >= editMenu.getItemCount()) return;
 			JMenuItem item = editMenu.getItem(toChange[i]);
 			if (item != null && (item instanceof JMenuItem || item instanceof JCheckBoxMenuItem)) {
 				boolean status = item.isEnabled();
 				item.setEnabled(!status); // switch status
 			}
 		}
 	}
 
 	/**
 	 * Promps a user to select either a directed or undirected graph state.
 	 *
 	 * Returns true if directed is selected, false otherwise.
 	 */
 	private boolean showDirectedPopup() {
  		// Messages 		
  		Object[] message = new Object[2];
 
  		message[0] = "Please select the graph type below:";
 
  		JComboBox directedSelection = new JComboBox();
  		directedSelection.getAccessibleContext().setAccessibleName("directed.selection");
  		directedSelection.addItem("Directed");
  		directedSelection.addItem("Not Directed");
  		directedSelection.setSelectedIndex(0); 
  		message[1] = directedSelection;
 		
   
  		// Options
  		String[] options = { 
 	 		"Ok"
 	 	}; 		
  		int result = JOptionPane.showOptionDialog( 
 	 		this,                                       // the parent that the dialog blocks 
 	 		message,                                    // the dialog message array 
 	 		"Directed / Not Directed:",                 // the title of the dialog window 
 	 		JOptionPane.DEFAULT_OPTION,                 // option type 
  		    JOptionPane.QUESTION_MESSAGE,               // message type 
  		    null,                                       // optional icon, use null to use the default icon 
  		    options,                                    // options string array, will be made into buttons 
  		    options[0]                                  // option that should be made into a default button 
  		);
 
  		switch(result) { 
  		   case 0: // yes
  		     String s = (String) directedSelection.getSelectedItem();
  		     if (s.equalsIgnoreCase("Directed")) return true;
  		     else return false;
  		   default: 
  		     break; 
 		}
  		
  		// this is important - so recurse until we have an answer... :)
  		return this.showDirectedPopup();
 	}
 
 
 
 	/**
 	 * This method tells the Preferences class to upate itself,
 	 * as well as manges the check boxes in the View menu.
 	 */
 	private void updatePreferences() {		
 		properties = Preferences.forceReload(this, log);
 		balanceOn = properties.getBalanceStatus();
 		nodeViewMenuCheckBoxState = properties.getNodeViewStatus();
 		edgeViewMenuCheckBoxState = properties.getEdgeViewStatus();
 		mbi.getNodeList().setGroupState(nodeViewMenuCheckBoxState);
 		mbi.getEdgeList().setGroupState(edgeViewMenuCheckBoxState);
 	}
 
 	/**
 	 * Used to listen to events generated by the Traversal Controls.
 	 * @author Randy Secrist
 	 */
 	public class TraversalControlListener implements ActionListener, Runnable {
 		private PathContainer pc;
 		private String algorithm;
 		private Thread pulse;
 		private long sleepTime = 1000;
 		private boolean skipPulse;
 		
 		private int pathIndex;
 		private int pathNumber;
 		
 		public JButton BEGIN; 
 		public JButton BACK; 
 		public JButton STOP; 
 		public JButton PLAY; 
 		public JButton FORWARD;
 		public JButton END;
 		public JLabel PATH_DESC;
 
 		public JComboBox PATH_LIST = new JComboBox();
 		public JTextArea PATH_TXT = new JTextArea();
 
 		public TraversalControlListener(String algorithm, PathContainer pc) {
 			this.pc = pc;
 			this.algorithm = algorithm;
 			
 			// Set up Buttons
 			try {
 				BEGIN = new JButton(getImageIcon("/images/Rewind16.gif", "Rewind Button"));
 				BACK = new JButton(getImageIcon("/images/StepBack16.gif", "Step Back Button"));
 				STOP = new JButton(getImageIcon("/images/Stop16.gif", "Stop Button"));
 				PLAY = new JButton(getImageIcon("/images/Play16.gif", "Play Button"));
 				FORWARD = new JButton(getImageIcon("/images/StepForward16.gif", "Step Forward Button"));
 				END = new JButton(getImageIcon("/images/FastForward16.gif", "Fast Forward Button"));
 			}
 			catch (Throwable e) {
 				log.write("Unable to load Traversal Control Graphics.\nPlease ensure resource directory exists in the application root directory.");
 				BEGIN = new JButton("<<");
 				BACK = new JButton("<");
 				STOP = new JButton("X");
 				PLAY = new JButton("Play");
 				FORWARD = new JButton(">");
 				END = new JButton(">>");
 			}
 
 			PATH_DESC = new JLabel("Path List");
 			
 			PATH_TXT.setEditable(false);
 			PATH_TXT.setFont(new Font("Serif", Font.PLAIN, 12));
 			PATH_TXT.setLineWrap(false);
 		}
 		
 		/**
 		 * Determines what happens when a traversal control is activated.
 		 * @param e The ActionEvent associated with the control.
 		 */
 		public void actionPerformed(ActionEvent e) {
 			Object theAction = e.getSource();
 
 			// Update current index agains PathContainer, and
 			// modify Globals
 			if (theAction.equals(PATH_LIST)) {
 				Globals.getInstance().resetAfterTraversal();
 				pathNumber = (((Integer) PATH_LIST.getSelectedItem()).intValue()) - 1;
 				pathIndex = 0;
 				PATH_TXT.setText("");
 				this.stop();
 			}
 			else if (!checkBounds()) {
 				log.write("actionPerformed::TraversalControlLisener - CHECK BOUNDS FAILED!");				
 				Globals.getInstance().resetAfterTraversal();
 				pathIndex = 0;
 				PATH_TXT.setText("");
 
 				// Pass Shutdown to Thread
 				this.stop();
 			}			
 			else if (theAction.equals(BEGIN)) {
 				Globals.getInstance().resetAfterTraversal();
 				pathIndex = 0;
 				PATH_TXT.setText("");
 				repaint();	
 			}
 			else if (theAction.equals(BACK)) {
 				stepBackward();
 				repaint();
 			}
 			else if (theAction.equals(STOP)) {
 				this.stop();
 			}
 			else if (theAction.equals(PLAY)) {
 				this.start();
 			}
 			else if (theAction.equals(FORWARD)) {
 				if(!stepForward()) {
 					this.stop();
 				}
 				repaint();
 			}
 			else if (theAction.equals(END)) {
 				while (stepForward());
 				this.stop();
 				repaint();
 			}
 			else {
 				log.write("actionPerformed::TraversalControlLisener - INVALID TRAVERSAL ACTION!");
 			}
 		}
 		public int getIndex() {
 			return pathIndex;
 		}
 		public void setIndex(int i) {
 			this.pathIndex = i;
 		}
 		public int getPathNumber() {
 			return pathNumber;
 		}
 		public void setPathNumber(int i) {
 			this.pathNumber = i;
 		}
 		public JScrollPane buildTraversalTextScrollPane() {
 			PATH_TXT.setText(new String());
 			JScrollPane pathScrollPane = new JScrollPane(PATH_TXT);
 			pathScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			pathScrollPane.setPreferredSize(new Dimension(150, 175));
 			pathScrollPane.setBorder(new TitledBorder(new EtchedBorder(), "Results for " + algorithm));
 			return pathScrollPane;
 		}
 		private boolean checkBounds() {
 			if (pathNumber < 0 || pathNumber >= pc.size()) {
 				log.write("checkBounds::TraversalControlListener - Selected Path Number is out of Path Container's Bounds!");
 				return false;
 			}
 
 			Path p = pc.getPath(pathNumber);
 
 			if (p == null) {
 				log.write("Path Object is Null!");
 				return false;
 			}
 
 			Node[] nodes = p.getNodePath();
 			Edge[] edges = p.getEdgePath();
 
 			boolean notNull = (nodes != null) && (edges != null);
 			boolean sameLength = (nodes.length == edges.length);
 
 			if (!(notNull && sameLength)) {
 				log.write("checkBounds::TraversalControlListener - Path Nodes and Edges are null or not the same length!");
 				return false;
 			}
 
 			if (pathIndex < 0 || pathIndex > nodes.length) {
 				log.write("checkBounds::TraversalControlListener - Path Index is out of Path Bounds!");				
 				return false;
 			}
 
 			return true;
 		}
 		/**
 		 * Starts the traversal pulse.
 		 */
 		public void start() {
 			pulse = new Thread(this);
 			pulse.setPriority(Thread.MIN_PRIORITY);
 			pulse.setName("Traversal Thread");
 			pulse.start();
 		}
 		/**
 		 * Stops this pulse.
 		 */
 		public synchronized void stop() {
 			pulse = null;
 			notify();
 		}
 		/**
 		 * Runs the traversal thread.
 		 */
 		public void run() {
 			Thread me = Thread.currentThread();
 			while (pulse == me) {
 				// Manipulate Traversal Here.
 				if (skipPulse) {
 					skipPulse = false;
 				} 
 				else {
 					ActionEvent e = new ActionEvent(FORWARD, ActionEvent.ACTION_PERFORMED, FORWARD.getName());
 					this.actionPerformed(e);
 				}
 				try {
 					Thread.sleep(sleepTime);
 				}
 				catch (InterruptedException ex) {
 					break;
 				}
 			}
 		}
 		public boolean stepForward() {
 			if (pathIndex == pc.getPath(pathNumber).getNodePath().length) {
 				return false;
 			}
 
 			Path p = pc.getPath(pathNumber);
 			boolean stop = false;
 
 			Node n = p.getNodePath()[pathIndex];
 			Edge e = p.getEdgePath()[pathIndex];
 			if((n == null) && (e == null)) {
 				pathIndex++;
 			}
 
 			while (!stop) {
 				if(pathIndex == p.getNodePath().length) {
 					stop = true;
 					continue;
 				}
 
 				n = p.getNodePath()[pathIndex];
 				e = p.getEdgePath()[pathIndex];
 				if((n == null) && (e == null)) {
 					stop = true;
 					continue;
 				}
 
 				// Now, we paint everything
 				if(n != null) {
 					for(int i = 0; i < Globals.getInstance().getNumNodes(); i++) {
 						NodeWrapper nw = Globals.getInstance().getNode(i);						
 						if(n == nw.getNode()) {
 							nw.isTraversed = true;
 						}
 					}
 				}
 				if(e != null) {
 					for(int i = 0; i < Globals.getInstance().getNumEdges(); i++) {
 						EdgeWrapper ew = Globals.getInstance().getEdge(i);						
 						if(e == ew.getEdge()) {
 							ew.isTraversed = true;
 						}
 					}
 				}
 				pathIndex++;
 			}
 			updatePathText();
 
 			return true;
 		}
 		public boolean stepBackward() {
 			if (pathIndex == 0)
 				return false;
 
 			Path p = pc.getPath(pathNumber);
 			boolean stop = false;
 
 			if(pathIndex == p.getNodePath().length)
 				pathIndex--;
 
 			Node n = p.getNodePath()[pathIndex];
 			Edge e = p.getEdgePath()[pathIndex];
 			if((n == null) && (e == null))
 				pathIndex--;
 				
 			while (!stop) {
 //				pathIndex--;
 				if(pathIndex < 0) {
 					pathIndex = 0;
 					stop = true;
 					continue;
 				}
 
 				n = p.getNodePath()[pathIndex];
 				e = p.getEdgePath()[pathIndex];
 				if((n == null) && (e == null)) {
 					stop = true;
 					continue;
 				}
 
 				// Now, we paint everything
 				if(n != null) {
 					for(int i = 0; i < Globals.getInstance().getNumNodes(); i++) {
 						NodeWrapper nw = Globals.getInstance().getNode(i);
 						if(n == nw.getNode()) {
 							nw.isTraversed = false;
 						}
 					}
 				}
 				if(e != null) {
 					for(int i = 0; i < Globals.getInstance().getNumEdges(); i++) {
 						EdgeWrapper ew = Globals.getInstance().getEdge(i);
 						if(e == ew.getEdge()) {
 							ew.isTraversed = false;
 						}
 					}
 				}
 				pathIndex--;
 			}
 			updatePathText();
 
 			return true;
 		}
 		public void updatePathText() {
 			StringBuffer buf = new StringBuffer();
 			Path p = pc.getPath(pathNumber);
 
 			double totalWeight = 0;
 
 			for(int i = 0; i < pathIndex; i++) {
 				Node n = p.getNodePath()[i];
 				Edge e = p.getEdgePath()[i];
 
 				// Now, we paint everything
 				if(e != null) {
 					totalWeight += e.getWeight();
 					buf.append("Path #" + (pathNumber+1) + "\tEdge\t" + theGraph.getNode(e.getSource()).getData().getDisplayName() + "::" + e.getSource() + " to " + theGraph.getNode(e.getDest()).getData().getDisplayName() + "::" + e.getDest() + "\tWeight: " + e.getWeight() + "\tSum: " + totalWeight + "\n");
 				}
 				if(n != null) {
 					buf.append("Path #" + (pathNumber+1) + "\tNode\t" + n.getData().getDisplayName() + "::" + n.getId() + "\n");
 				}
 			}
 			if (pathIndex == p.getNodePath().length) {
 				buf.append("------------------");
 				buf.append("\nTotal Path Weight: \t" + totalWeight + "\n");
 				buf.append("------------------");				
 			}
 			PATH_TXT.setText(buf.toString());
 		}
 		public JPanel buildTraversalControls(PathContainer pc) {
 			JPanel p = new JPanel(new FlowLayout(), true);
 
 			// Add Path Drop Down Selection
 	 		PATH_LIST.getAccessibleContext().setAccessibleName("path.number");
 	 		for (int i = 0; i < pc.size(); i++) {
 		 		PATH_LIST.addItem(new Integer((i+1)));
 	 		}
 
 			BEGIN.addActionListener(this);
 			BACK.addActionListener(this);
 			STOP.addActionListener(this);
 			PLAY.addActionListener(this);
 			FORWARD.addActionListener(this);
 			END.addActionListener(this);
 			PATH_LIST.addActionListener(this);
 			
 			p.add(BEGIN);
 			p.add(BACK);
 			p.add(STOP);
 			p.add(PLAY);
 			p.add(FORWARD);
 			p.add(END);
 			JPanel pathListPanel = new JPanel(true);
 			pathListPanel.add(PATH_DESC);
 			pathListPanel.add(PATH_LIST);
 			p.add(pathListPanel);
 
 			return p;
 		}
 	}
 
 	/**
 	 * Eddit Menu - Add Edge - Event Handler
 	 * (Handles what happens when someone clicks on Add Edge)
 	 */
 	public void doAddEdge(NodeWrapper source, NodeWrapper dest) {
 		if ( theGraph.isEdge(source.getNode().getId(), dest.getNode().getId()) ) return;
 		
  		// Messages
  		Object[] message = new Object[2]; 
  		message[0] = "Edge Weight:";
  		
  		JTextField weightTxt = new JTextField();
  		message[1] = weightTxt; 
   
  		// Options
  		String[] options = { 
 	 		"Ok",
 	 		"Cancel"
 	 	};
  		
  		int result = JOptionPane.showOptionDialog( 
 	 		this,                                       // the parent that the dialog blocks 
 	 		message,                                    // the dialog message array 
 	 		"Add Edge Option:",                         // the title of the dialog window 
 	 		JOptionPane.DEFAULT_OPTION,                 // option type 
  		    JOptionPane.QUESTION_MESSAGE,               // message type 
  		    null,                                       // optional icon, use null to use the default icon 
  		    options,                                    // options string array, will be made into buttons 
  		    options[0]                                  // option that should be made into a default button 
  		);
  		
  		switch(result) { 
  		   case 0: // yes
  		     this.doAddEdge(weightTxt.getText(), source, dest);
  		     break;
  		   case 1: // cancel
  		     break; 
  		   default: 
  		     break; 
  		} 		
 	}
 
 	public boolean getTraversalStatus() {
 		return traverseWindowUp;
 	}
 
 	/**
 	 * This dialog displays a textual representation of the
 	 * result of a particular traversal algorithm, as well as
 	 * the VCR controls used by the editor to step and cycle
 	 * through the traversal paths.
 	 */
 	private void showTraversalDialog(String algorithm, PathContainer pc) {
 		// Sanity checks:
 		if (pc == null || pc.isEmpty()) {
 			log.write("showTraversalDialog::GEdit - PathContainer.isEmpty returned true or was null!");
 			return;
 		}
 		if (traverseWindowUp) return;
 
 		// Lock Editable Abilities:
 		this.processMenuState(MENU_LOCKED);
 		
 		// Traversal Control Variables:
 		JDialog traversalDialog = new JDialog(this);
 		WindowListener l = new WindowAdapter() {
 			public void windowClosing(WindowEvent e) { 
 				traverseWindowUp = false;
 				processMenuState(MENU_EDITABLE);
 				
 				// Clean up Globals
 				Globals.getInstance().resetAfterTraversal();
 				repaint();
 			}
 		};
 		traversalDialog.addWindowListener(l);
 		traversalDialog.setTitle("Traversal Controls: " + algorithm);
 		traversalDialog.setSize(new Dimension(400,275));
 		traversalDialog.getContentPane().setLayout(new BorderLayout());
 
 		TraversalControlListener tcl = new TraversalControlListener(algorithm, pc);
 
 		// Add Path Text Window
 		traversalDialog.getContentPane().add(tcl.buildTraversalTextScrollPane(), BorderLayout.NORTH);
 
 		// Add Traversal Controls
 		traversalDialog.getContentPane().add(tcl.buildTraversalControls(pc), BorderLayout.CENTER);
 
 		// Set dialog in center of screen.
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		Dimension dlgSize = traversalDialog.getSize();
 		int newX = (int) ((screenSize.width / 2) - (dlgSize.getWidth() / 2));
 		int newY = (int) ((screenSize.height / 2) - (dlgSize.getHeight() / 2));
 		traversalDialog.setLocation(new Point(newX, newY));
 		traversalDialog.setVisible(true);
 		traverseWindowUp = true;
 	}
 
 	private String edgeViewMenuCheckBoxState;  // Init in constructor
 	private String nodeViewMenuCheckBoxState;  // Init in constructor
 	private Dimension preferredSurfaceSize = new Dimension(300,300);
 
 	/**
 	 * Eddit Menu - Add Edge - Event Handler
 	 * (Handles what happens when someone clicks on Add Edge)
 	 */
 	public void doAddEdge(String weightTxt, NodeWrapper source, NodeWrapper dest) {
 		if ( theGraph.isEdge(source.getNode().getId(), dest.getNode().getId()) ) return;		
 
 		// Ensure we got a number for weight
  		double weight;
  		if (weightTxt == null || weightTxt.length() <= 0 || weightTxt.equalsIgnoreCase("")) weight = 0;
  		else weight = Double.parseDouble(weightTxt);
  		Edge edge = new Edge(source.getNode().getId(), dest.getNode().getId(), weight);
  		
 		try {			
 			theGraph.addEdge(edge);
 			log.write("Edge Added to Graph: " + theGraph.getNode(edge.getSource()).getData().getDisplayName() + "::" + edge.getSource() + " to " + theGraph.getNode(edge.getDest()).getData().getDisplayName() + "::" + edge.getDest());			
 			this.doHardRefresh();
 		}
  		catch (java.lang.Throwable e) {
 	 		if (e instanceof NumberFormatException) {
 		 		log.write("doAddEdge::GEdit - Edge weights must be Non-Negative. - " + e.getMessage());
 				this.showWarningPopup("Warning", "Edge weights must be Non-Negative!\n\nPlease ensure you enter a number!", JOptionPane.OK_OPTION);
 	 		}
 	 		else if (e instanceof GraphException) {
 		 		log.write("doAddEdge::GEdit - Failure to add Edge: " + e.getMessage());
 				this.showWarningPopup("Warning", "We apologize but an error occured while adding an edge.\n\n" + e.getMessage(), JOptionPane.OK_OPTION);
 				// Rollback
 				theGraph.removeEdge(edge);
 	 		}
 	 		else if (e instanceof GraphWarning) {
 				this.doHardRefresh();
 		 		log.write(e.getMessage());
 				this.showWarningPopup("Warning", e.getMessage(), JOptionPane.OK_OPTION);
 	 		}
 	 		else {
 		 		log.write("doAddEdge::GEdit - General Error: " + e.getMessage());
 				this.showWarningPopup("Warning", "We apologize but an error occured while adding an edge.\n\n" + e.getMessage(), JOptionPane.OK_OPTION);
 				// Rollback
 				theGraph.removeEdge(edge);
 	 		}
  		}
 	}
 
 	/**
 	 * Displays an HTML Help File.
 	 *
 	 * <p>The HTML file man include hyperlinks since HyperLinkActivator
 	 * will update this pane when the mouse clicks on a link.
 	 *
 	 * <p>However because this only displays using HTML 3.2, the html
 	 * needs to be pretty basic and can't implement and javascript.
 	 * (Not unless U want to code / insert a JS parser...)
 	 */
 	public void doJavaDoc() {
 		Runnable worker = new Runnable() {
             public void run() {
             	/*
             	Display display = new Display(new DeviceData());
             	SWTBrowser browser = new SWTBrowser();
         		browser.setTitle("Source Code Documentation");
         		browser.setHomeUrl("http://www.secristfamily.com/randy/GEdit/api");
         		browser.createSShell();
         		browser.open();
         		
         		while (!browser.isDisposed()) {
         			if (!display.readAndDispatch())
         				display.sleep();
         		}
         		display.dispose();
         		*/
             }
         };
 		SwingUtilities.invokeLater(worker);
         // TODO - share a single thread for SWT UI
 		// new Thread(null, worker, "doJavaDoc").start();
 	}
 
 	public void doRemoveEdges() {
 		theGraph.removeAlledges();
 		this.doHardRefresh();
 	}
 
 	public void doSaveIslandAsGraph(int islandId) {
 		boolean isDirected = theGraph.getDirected();
 		Node[] nodes = theGraph.getNodes(islandId);
 		Edge[] edges = theGraph.getEdges(islandId);
 		int[] newId = new int[nodes.length];
 
 		log.write("doSaveIslandAsGraph::GEdit - " + nodes.length + "::" + edges.length);
 		
 		Graph newGraph = new Graph(nodes.length);
 
 		try {
 			newGraph.setDirected(isDirected);
 			for (int i = 0; i < nodes.length; i++) {
 				newId[i] = newGraph.addNode(nodes[i].getData());
 			}
 
 			// Translate Edge id's and create new edges:
 			for (int j = 0; j < edges.length; j++) {
 				int oldSrc = edges[j].getSource();
 				int oldDst = edges[j].getDest();
 
 				int newSrc = 0;
 				int newDst = 0;
 				for (int k = 0; k < nodes.length; k++) {
 					if (oldSrc == nodes[k].getId()) newSrc = newId[k];
 					if (oldDst == nodes[k].getId()) newDst = newId[k];
 				}
 
 				Edge newEdge = new Edge(newSrc, newDst, edges[j].getWeight());
 				newGraph.addEdge(newEdge);
 			}
 		}
 		catch (GraphException e) {
 	 		log.write("doSaveIslandAsGraph::GEdit - Failure to save island as new graph: " + e.getMessage());
 			this.showWarningPopup("Warning", "We apologize but an error occured while saving this island as a new graph.\n\n" + e.getMessage(), JOptionPane.OK_OPTION);			
 		}
 		catch (GraphWarning e) {
 			// Ignore any warnings since we are only building a subgraph.
 		}
 
 		// Graph is built - serialize
 		Frame saveFrame = new Frame();
 		JFileChooser jfc = new JFileChooser();
 		jfc.setFileFilter(new FileListFilter(FileListFilter.SERIALIZE));
 		int option = jfc.showSaveDialog(saveFrame);
 		if (option == JFileChooser.CANCEL_OPTION) return;
 
 		String path = jfc.getSelectedFile().getPath();
 		if ( path.indexOf(".grf") == -1 ) path += ".grf";
 			
 		// Serialize Graph
 		try {						
 			pathname = path;
 			newGraph.setSaved(true);
 			
 			FileOutputStream fos = new FileOutputStream(path);
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 				
 			oos.writeObject(newGraph);
 			oos.flush();
 			oos.close();
 			
 			log.write("Island Graph Saved to: " + path);
 		}
 		catch (java.lang.Throwable e) {
 			if (e instanceof FileNotFoundException) {
 				this.showWarningPopup("Warning", "File Not Found: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 				return;
 			}
 			else if (e instanceof IOException) {
 				this.showWarningPopup("Warning", "IOException: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 				return;
 			}
 			else {
 				this.showWarningPopup("Warning", "General Error: " + e.getLocalizedMessage(), JOptionPane.OK_OPTION);			
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Creates an icon from an image contained in the "resource" directory.
 	 * 
 	 * This needs to be done in such a way that the icon can reside inside a JAR file.
 	 * 
 	 * @return ImageIcon
 	 */
 	ImageIcon getImageIcon(String filename, String description) {
 		URL url = this.getClass().getResource(filename);
 		return new ImageIcon(url, description);
 	}
 	
 	/**
 	 * Returns a byte array from a given input stream.
 	 * @param in The input stream to examine for bytes.
 	 * @return A byte array.
 	 * @throws ResourceFailure If a problem reading the input stream occurs.
 	 */
 	byte[] getBytes(InputStream in) throws ResourceFailure {
 	    ByteArrayOutputStream out = new ByteArrayOutputStream();
 	    byte[] buffer = new byte[(1 << 10)*2];
 	    int read;
 		try {
 			while ((read = in.read(buffer)) >= 0)
 				out.write(buffer, 0, read);
 			in.close();
 		}
 		catch (IOException e) {
 			throw new ResourceFailure(e);
 		}
 		buffer = out.toByteArray();
 		return buffer;
 	}
 
 	/**
 	 * Queries and returns the dimension of an island window given it's
 	 * iternal id number.  Each island window contains an id number which
 	 * the main program (this) can use to identify it.
 	 * 
 	 * @return Dimension
 	 */
 	private Dimension getIslandWindowSurfaceSize(int id) {
 		JInternalFrame[] frames = mainPain.getAllFrames();
 		for (int i = 0; i < frames.length; i++) {
 			Component[] components = frames[i].getContentPane().getComponents();
 			for (int j = 0; j < components.length; j++) {
 				if (components[j] instanceof GraphPanel) {					
 					GraphPanel p = ((GraphPanel) components[j]);
 					if (id == p.surf.islandId) return p.surf.getSize();
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a random integer (inclusive) if given a valid range.
 	 * Returns -1 if given a bogus range.
 	 * 
 	 * @return int
 	 */
 	private int getRandomIntBetween(int max, int min) {
 		if (min > max) return -1;
 		if (min == max) return -1;
 
 		int current = -1;
 		while (current == -1) {
 			double temp = max*Math.random();
 			if (temp > min) current = (int) temp;			
 		}
 		return current;
 	}
 
 	  /**
 	   * Remove this when using with JDK 1.3 as it is provided by super class.
 	   * @returns Returns: the currently active JInternalFrame or null.
 	   * 
 	   * @return JInternalFrame
 	   * @deprecated This is provided by the JDesktopPane class since JDK 1.3.
 	   */
 	   private JInternalFrame getSelectedFrame() {
 		   JInternalFrame[] frames = mainPain.getAllFrames();
 		   for (int i = 0; i < frames.length; i++) {
 			   if (frames[i].isSelected()) return frames[i];
 		   }
 		   return null;
 	   }
 
 	/**
 	 * Modifies the data in a node by asking the data type currently in
 	 * use to gather new input from the user.
 	 * 
 	 * If this program is to ever work with more than one data type,
 	 * then this will need to be modifed to ask the user what data
 	 * type to store in the node.
 	 */
 	public void modifyNodeData(Node n) {
 		try {
 			DataVisitor v = new SwingBridge();
 			String s = v.showInputDialog(dataSpawn, this);
 			if (s != null) 
 				theGraph.modifyData(n, dataSpawn.getInstance(s));
 			this.doSoftRefresh();
 		}
 		catch (Throwable e) {
 			if (e instanceof GraphWarning)
 				return;
 			StringWriter strWriter = new StringWriter();
 			e.printStackTrace(new PrintWriter(strWriter));
 			log.write(strWriter.toString());
 			this.showWarningPopup("Modify Node Data Error!", "We apologize but there was a problem while modifing node data.  Please contact support for help.", JOptionPane.OK_OPTION);
 		}
 	}
 }
