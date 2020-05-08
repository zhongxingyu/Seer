 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.FontMetrics;
 import java.awt.Point;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 
 import model.TaskTriplet;
 import model.TripletEvent;
 import controller.ConnectionListener;
 import controller.TimerListener;
 import controller.TripletListener;
 
 /**
  * This code was edited or generated using CloudGarden's Jigloo
  * SWT/Swing GUI Builder, which is free for non-commercial
  * use. If Jigloo is being used commercially (ie, by a corporation,
  * company or business for any purpose whatever) then you
  * should purchase a license for each developer using Jigloo.
  * Please visit www.cloudgarden.com for details.
  * Use of Jigloo implies acceptance of these licensing terms.
  * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
  * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
  * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
  */
 
 /**
  * This class implements the Graphical User Interface for RoboCup@Work referee
  * system.
  * 
  * @author BRSU-MAS-ASTT-SoSe2012
  */
 public class MainGUI extends JFrame implements TripletListener,
 		ConnectionListener, TimerListener {
 	private static final long serialVersionUID = 1L;
 	private static final int GAP = 10;
 	private JScrollPane tripletTableScrollPane;
 	private JPanel editTripletPanel;
 	private JPanel statusPanel;
 	private JPanel westPanel;
 	private JButton saveButton;
 	private JButton openButton;
 	private JButton loadConfigButton;
 	private JLabel statusLine;
 	private JLabel timerLabel;
 	private JLabel maxTimeLabel;
 	private JButton deleteTripletButton;
 	private JButton addTripletButton;
 	private JButton sendTripletsButton;
 	private JToggleButton timerStartStopButton;
 	private JPanel boxPanel;
 	private JComboBox<String> orientationsBox;
 	private JComboBox<Short> pausesBox;
 	private JComboBox<String> placesBox;
 	private JToolBar toolBar;
 	private JPanel toolBarPanel;
 	private JPanel contentPanel;
 	private MapArea mapArea;
 	private JMenuItem helpMenuItem;
 	private JMenu helpMenu;
 	private JMenuItem deleteMenuItem;
 	private JMenuItem addMenuItem;
 	private JMenu editMenu;
 	private JMenuItem exitMenuItem;
 	private JMenuItem saveMenuItem;
 	private JLabel connectedIcon;
 	private JButton disconnectButton;
 	private JPanel upperServerPanel;
 	private JPanel middleServerPanel;
 	private JPanel lowerServerPanel;
 	private JLabel connectedLabel;
 	private JPanel serverPanel;
 	private JMenuItem openFileMenuItem;
 	private JMenu fileMenu;
 	private JMenuBar menuBar;
 	private DefaultComboBoxModel<String> placesCbm;
 	private DefaultComboBoxModel<String> orientationsCbm;
 	private DefaultComboBoxModel<Short> pausesCbm;
 	private JButton updateTripletButton;
 	private JButton downTripletButton;
 	private JButton upTripletButton;
 	private JMenuItem upMenuItem;
 	private JMenuItem downMenuItem;
 	private JMenuItem updateMenuItem;
 	private JMenuItem loadConfigMenuItem;
 	private JTable tripletTable;
 	private TripletTableM tripletTableM;
 	private DefaultTableCellRenderer rendTriplets;
 	private JButton competitionFinishedButton;
 	private JTabbedPane tabbedPane;
 	private JPanel bntPanel;
 	private JLabel boxLeftPanel;
 
 	private class TripletTableM extends DefaultTableModel {
 		private static final long serialVersionUID = 1L;
 
 		public Class getColumnClass(int column) {
 			if (column >= 1)
 				return Boolean.class;
 			else
 				return String.class;
 		}
 
 		public void clearColumn(int c) {
 			for (int i = 0; i < this.getRowCount(); i++) {
 				this.setValueAt(null, i, c);
 			}
 		}
 
 		public boolean isCellEditable(int row, int col) {
 			if (col == 0)
 				return false;
 			else
 				return true;
 		}
 	}
 
 	/** Default constructor */
 	public MainGUI() {
 		try {
 			UIManager.setLookAndFeel(UIManager
 					.getCrossPlatformLookAndFeelClassName());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		initGUI();
 	}
 
 	private void initGUI() {
 		this.setTitle("RoboCup@Work");
 		BorderLayout panelLayout = new BorderLayout();
 		this.setLayout(panelLayout);
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		{
 			createContentPanel();
 			createMenuBar();
 			this.pack();
 		}
 	}
 
 	private void createTripletTableScrollPaneInWestPanel() {
 		tripletTableScrollPane = new JScrollPane(
 				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		tripletTableM = new TripletTableM();
 		tripletTableM.addColumn("Triplets");
 		tripletTable = new JTable(tripletTableM);
 		rendTriplets = new DefaultTableCellRenderer();
 		tripletTable.getColumn("Triplets").setCellRenderer(rendTriplets);
 		rendTriplets.setHorizontalAlignment(JLabel.CENTER);
 		tripletTableScrollPane.setViewportView(tripletTable);
 		tripletTableScrollPane
 				.setPreferredSize(tripletTable.getPreferredSize());
 		bntPanel.add(tripletTableScrollPane, BorderLayout.WEST);
 	}
 
 	private void createWestPanelInContentPanel() {
 		westPanel = new JPanel();
 		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
 		westPanel.add(Box.createVerticalStrut(GAP));
 		//createHeaderInEditTripletPanel();
 		//createBoxPanelInEditTripletPanel();
 		//westPanel.add(Box.createVerticalGlue());
 		createEditTripletButtons();
 		addEditTripletButtonsToEditTripletPanel();
 		contentPanel.add(westPanel, BorderLayout.WEST);
 		createServerPanelInWestPanel();
 		// contentPanel.add(westPanel, BorderLayout.WEST);
 	}
 
 	private void createBoxPanelInEditTripletPanel() {
 		
 		boxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		boxPanel.setRequestFocusEnabled(false);
 		JLabel place = new JLabel("Place");
 		boxPanel.add(place);
 		placesBox = new JComboBox<String>();
 		boxPanel.add(placesBox);
 		boxPanel.add(new JLabel("Orientation "));
 		orientationsBox = new JComboBox<String>();
 		boxPanel.add(orientationsBox);
 		boxPanel.add(new JLabel("Pause "));
 		pausesBox = new JComboBox<Short>();
 		boxPanel.add(pausesBox);
 		bntPanel.add(boxPanel, BorderLayout.NORTH);
 	}
 
 	private void createEditTripletButtons() {
 		addTripletButton = new JButton();
 		addTripletButton.setAlignmentX(CENTER_ALIGNMENT);
 		updateTripletButton = new JButton();
 		updateTripletButton.setAlignmentX(CENTER_ALIGNMENT);
 		deleteTripletButton = new JButton();
 		deleteTripletButton.setAlignmentX(CENTER_ALIGNMENT);
 		upTripletButton = new JButton();
 		upTripletButton.setAlignmentX(CENTER_ALIGNMENT);
 		downTripletButton = new JButton();
 		downTripletButton.setAlignmentX(CENTER_ALIGNMENT);
 	}
 
 	private void addEditTripletButtonsToEditTripletPanel() {
 		westPanel.add(addTripletButton);
 		westPanel.add(Box.createVerticalStrut(GAP));
 		westPanel.add(updateTripletButton);
 		westPanel.add(Box.createVerticalStrut(GAP));
 		westPanel.add(deleteTripletButton);
 		westPanel.add(Box.createVerticalStrut(GAP));
 		westPanel.add(Box.createVerticalGlue());
 		westPanel.add(upTripletButton);
 		westPanel.add(Box.createVerticalStrut(GAP));
 		westPanel.add(downTripletButton);
 		westPanel.add(Box.createVerticalGlue());
 	}
 
 	private void createUpperServerPanel() {
 		upperServerPanel = new JPanel();
 		upperServerPanel.setLayout(new BoxLayout(upperServerPanel,
 				javax.swing.BoxLayout.LINE_AXIS));
 		connectedIcon = new JLabel(new ImageIcon(getClass().getResource(
 				"/view/resources/icons/status-busy.png")));
 		upperServerPanel.add(connectedIcon);
 		connectedLabel = new JLabel();
 		connectedLabel.setText("no team connected");
 		upperServerPanel.add(connectedLabel);
 		serverPanel.add(upperServerPanel);
 	}
 
 	private void createMiddleServerPanel() {
 		middleServerPanel = new JPanel();
 		middleServerPanel.setLayout(new BoxLayout(middleServerPanel,
 				javax.swing.BoxLayout.LINE_AXIS));
 		disconnectButton = new JButton();
 		disconnectButton.setEnabled(false);
 		disconnectButton.setHorizontalAlignment(SwingConstants.LEFT);
 		middleServerPanel.add(disconnectButton);
 		middleServerPanel.add(Box.createHorizontalStrut(GAP));
 		sendTripletsButton = new JButton();
 		sendTripletsButton.setEnabled(false);
 		sendTripletsButton.setHorizontalAlignment(SwingConstants.RIGHT);
 		middleServerPanel.add(sendTripletsButton);
 		serverPanel.add(middleServerPanel);
 	}
 
 	private void createLowerServerPanel() {
 		lowerServerPanel = new JPanel();
 		lowerServerPanel.setLayout(new BoxLayout(lowerServerPanel,
 				javax.swing.BoxLayout.LINE_AXIS));
 		timerStartStopButton = new JToggleButton("Timer Start");
 		timerStartStopButton.setEnabled(false);
 		timerStartStopButton.setAlignmentX(SwingConstants.LEFT);
 		lowerServerPanel.add(timerStartStopButton);
 		lowerServerPanel.add(Box.createHorizontalStrut(GAP));
 		timerLabel = new JLabel();
 		timerLabel.setAlignmentX(SwingConstants.CENTER);
 		timerLabel.setText("00:00");
 		lowerServerPanel.add(timerLabel);
 		lowerServerPanel.add(Box.createHorizontalStrut(GAP));
 		maxTimeLabel = new JLabel();
 		maxTimeLabel.setAlignmentX(SwingConstants.RIGHT);
 		maxTimeLabel.setVisible(false);
 		lowerServerPanel.add(maxTimeLabel);
 		serverPanel.add(lowerServerPanel);
 	}
 
 	private void createcompetitionFinishedButtonInServerPanel() {
 		competitionFinishedButton = new JButton("Competition Finished");
 		competitionFinishedButton.setEnabled(false);
 		competitionFinishedButton.setAlignmentX(CENTER_ALIGNMENT);
 		serverPanel.add(competitionFinishedButton);
 	}
 
 	private void createServerPanelInWestPanel() {
 		serverPanel = new JPanel();
 		serverPanel.setLayout(new BoxLayout(serverPanel,
 				javax.swing.BoxLayout.PAGE_AXIS));
 		serverPanel.add(Box.createVerticalStrut(GAP));
 		serverPanel.add(new JSeparator());
 		serverPanel.add(Box.createVerticalStrut(GAP));
 		createUpperServerPanel();
 		serverPanel.add(Box.createVerticalStrut(GAP));
 		createMiddleServerPanel();
 		serverPanel.add(Box.createVerticalStrut(GAP));
 		createLowerServerPanel();
 		serverPanel.add(Box.createVerticalStrut(GAP));
 		createcompetitionFinishedButtonInServerPanel();
 		westPanel.add(serverPanel, BorderLayout.SOUTH);
 	}
 
 	private void createMapAreaInBntPanel() {
 		mapArea = new MapArea();
 		// mapArea.setBackground(Color.white);
 		bntPanel.add(mapArea, JTabbedPane.CENTER);
 	}
 
 	private void createStatusPanelInContentPanel() {
 		statusPanel = new JPanel();
 		statusLine = new JLabel();
 		statusLine.setName("statusLine");
 		statusLine.setHorizontalAlignment(JLabel.CENTER);
 		// to avoid having an invisible status panel if empty
 		FontMetrics metrics = statusLine.getFontMetrics(statusLine.getFont());
 		int hight = metrics.getHeight();
 		// width is determined by the parent container
 		Dimension size = new Dimension(0, hight + GAP);
 		statusPanel.setPreferredSize(size);
 		statusPanel.add(statusLine);
 		contentPanel.add(statusPanel, BorderLayout.SOUTH);
 	}
 
 	private void createToolBarPanelInContentPanel() {
 		toolBarPanel = new JPanel();
 		toolBarPanel.setLayout(new BorderLayout());
 		toolBar = new JToolBar();
 		openButton = new JButton();
 		toolBar.add(openButton);
 		openButton.setName("openButton");
 		saveButton = new JButton();
 		toolBar.add(saveButton);
 		saveButton.setFocusable(false);
 		loadConfigButton = new JButton();
 		toolBar.add(loadConfigButton);
 		toolBarPanel.add(toolBar, BorderLayout.CENTER);
 		contentPanel.add(toolBarPanel, BorderLayout.NORTH);
 	}
 
 	private void createContentPanel() {
 		contentPanel = new JPanel();
 		contentPanel.setLayout(new BorderLayout());
 		createWestPanelInContentPanel();
 		createTabbedCompetitionPane();
 		createStatusPanelInContentPanel();
 		createToolBarPanelInContentPanel();
 		this.add(contentPanel, BorderLayout.CENTER);
 	}
 
 	private void createTabbedCompetitionPane() {
 		tabbedPane = new JTabbedPane();
 		bntPanel = new JPanel();
 		bntPanel.setLayout(new BorderLayout());
 		createTripletTableScrollPaneInWestPanel();
 		createMapAreaInBntPanel();
 		tabbedPane.addTab("BNT", null, bntPanel,
 		                  "BNT Test");
 		JPanel second = new JPanel();
 		tabbedPane.addTab("xxx", null, second,
                 "BNT Test");
 		contentPanel.add(tabbedPane, BorderLayout.CENTER);
 		createBoxPanelInEditTripletPanel();
 	}
 
 	private void createFileMenu() {
 		fileMenu = new JMenu();
 		fileMenu.setText("File");
 		openFileMenuItem = new JMenuItem();
 		fileMenu.add(openFileMenuItem);
 		saveMenuItem = new JMenuItem();
 		fileMenu.add(saveMenuItem);
 		fileMenu.add(new JSeparator());
 		loadConfigMenuItem = new JMenuItem();
 		fileMenu.add(loadConfigMenuItem);
 		fileMenu.add(new JSeparator());
 		exitMenuItem = new JMenuItem();
 		fileMenu.add(exitMenuItem);
 		menuBar.add(fileMenu);
 	}
 
 	private void createEditMenu() {
 		editMenu = new JMenu();
 		menuBar.add(editMenu);
 		editMenu.setText("Edit");
 		addMenuItem = new JMenuItem();
 		deleteMenuItem = new JMenuItem();
 		upMenuItem = new JMenuItem();
 		downMenuItem = new JMenuItem();
 		updateMenuItem = new JMenuItem();
 		editMenu.add(addMenuItem);
 		editMenu.add(updateMenuItem);
 		editMenu.add(deleteMenuItem);
 		editMenu.add(new JSeparator());
 		editMenu.add(upMenuItem);
 		editMenu.add(downMenuItem);
 	}
 
 	private void createHelpMenu() {
 		helpMenu = new JMenu();
 		menuBar.add(helpMenu);
 		helpMenu.setText("Help");
 		helpMenuItem = new JMenuItem();
 		helpMenuItem.setText("Help");
 		helpMenu.add(helpMenuItem);
 	}
 
 	private void createMenuBar() {
 		menuBar = new JMenuBar();
 		createFileMenu();
 		createEditMenu();
 		createHelpMenu();
 		this.setJMenuBar(menuBar);
 	}
 
 	/**
 	 * Get the GUI component representing the map area.
 	 * 
 	 * @return Reference to an object of type MapArea
 	 */
 	public MapArea getMapArea() {
 		return mapArea;
 	}
 
 	/**
 	 * Update appropriate GUI components with the provided valid positions.
 	 * 
 	 * @param positions
 	 *            A set of valid place labels and their pixel positions.
 	 */
 	public void setValidPositions(LinkedHashMap<String, Point> positions) {
 		String[] posString = new String[positions.size()];
 		int i = 0;
 		for (String pos : positions.keySet()) {
 			posString[i] = pos;
 			i++;
 		}
 		placesCbm = new DefaultComboBoxModel<String>(posString);
 		placesBox.setModel(placesCbm);
 		mapArea.setValidPositions(positions);
 	}
 
 	/**
 	 * Update appropriate GUI components with the provided valid orientations.
 	 * 
 	 * @param orientations
 	 *            A list of strings representing valid orientations.
 	 */
 	public void setValidOrientations(List<String> orientations) {
 		orientationsCbm = new DefaultComboBoxModel<String>(
 				orientations.toArray(new String[orientations.size()]));
 		orientationsBox.setModel(orientationsCbm);
 	}
 
 	/**
 	 * Update appropriate GUI components with the provided valid pause
 	 * durations.
 	 * 
 	 * @param pauses
 	 *            A list of short integers representing valid pauses.
 	 */
 	public void setValidPauses(List<Short> pauses) {
 		pausesCbm = new DefaultComboBoxModel<Short>(
 				pauses.toArray(new Short[pauses.size()]));
 		pausesBox.setModel(pausesCbm);
 	}
 
 	/**
 	 * Connect Load Config button to the corresponding action.
 	 * 
 	 * @param loadConfig
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectLoadConfigAction(Action loadConfig) {
 		loadConfigButton.setAction(loadConfig);
 		loadConfigMenuItem.setAction(loadConfig);
 	}
 
 	/**
 	 * Connect Save button to the corresponding action.
 	 * 
 	 * @param save
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectSaveAction(Action save) {
 		saveButton.setAction(save);
 		saveMenuItem.setAction(save);
 		saveButton.setEnabled(false);
 		saveMenuItem.setEnabled(false);
 	}
 
 	/**
 	 * Connect Open button to the corresponding action.
 	 * 
 	 * @param open
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectOpenAction(Action open) {
 		openButton.setAction(open);
 		openFileMenuItem.setAction(open);
 		openButton.setEnabled(false);
 		openFileMenuItem.setEnabled(false);
 	}
 
 	/**
 	 * Connect Exit button to the corresponding action.
 	 * 
 	 * @param exit
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectExitAction(Action exit) {
 		exitMenuItem.setAction(exit);
 	}
 
 	/**
 	 * Connect Send Triplets button to the corresponding action.
 	 * 
 	 * @param sendTriplets
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectSendTriplets(Action sendTriplets) {
 		sendTripletsButton.setAction(sendTriplets);
 		sendTripletsButton.setEnabled(false);
 	}
 
 	/**
 	 * Connect Disconnect button to the corresponding action.
 	 * 
 	 * @param disconnect
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectDisconnet(Action disconnect) {
 		disconnectButton.setAction(disconnect);
 		disconnectButton.setEnabled(false);
 	}
 
 	/**
 	 * Connect Help Menu Item to the corresponding action.
 	 * 
 	 * @param help
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectHelpAction(Action help) {
 		helpMenuItem.setAction(help);
 	}
 
 	/**
 	 * Connect Up Triplet button to the corresponding action.
 	 * 
 	 * @param upTriplet
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectUpTriplet(Action upTriplet) {
 		upTripletButton.setAction(upTriplet);
 		upMenuItem.setAction(upTriplet);
 	}
 
 	/**
 	 * Connect Down Triplet button to the corresponding action.
 	 * 
 	 * @param downTriplet
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectDownTriplet(Action downTriplet) {
 		downTripletButton.setAction(downTriplet);
 		downMenuItem.setAction(downTriplet);
 	}
 
 	/**
 	 * Connect Edit Triplet button to the corresponding action.
 	 * 
 	 * @param editTriplet
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectEditTriplet(Action editTriplet) {
 		updateTripletButton.setAction(editTriplet);
 		updateMenuItem.setAction(editTriplet);
 	}
 
 	/**
 	 * Connect Add Triplet button to the corresponding action.
 	 * 
 	 * @param addTriplet
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectAddTriplet(Action addTriplet) {
 		addTripletButton.setAction(addTriplet);
 		addMenuItem.setAction(addTriplet);
 	}
 
 	/**
 	 * Connect Delete Triplet button to the corresponding action.
 	 * 
 	 * @param deleteTriplet
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectDeleteTriplet(Action deleteTriplet) {
 		deleteTripletButton.setAction(deleteTriplet);
 		deleteMenuItem.setAction(deleteTriplet);
 	}
 
 	/**
 	 * Display folder browser dialog for open and save.
 	 * 
 	 * @param fType
 	 *            A FileType enumerator constant indicating the desired file
 	 *            type.
 	 * 
 	 * @param diagType
 	 *            A DialogType enumerator constant indicating whether Open or
 	 *            Save dialog should be displayed.
 	 */
 	public File showFolderDialog(FileType fType, DialogType diagType) {
 		JFileChooser fc = new JFileChooser();
 		if (fType == FileType.FILETYPE_TSP)
 			fc.setFileFilter(new TspFilter());
 		if (diagType == DialogType.DIALOG_SAVE
 				&& fc.showSaveDialog(contentPanel) == JFileChooser.APPROVE_OPTION) {
 			return fc.getSelectedFile();
 		}
 		if (diagType == DialogType.DIALOG_OPEN
 				&& fc.showOpenDialog(contentPanel) == JFileChooser.APPROVE_OPTION) {
 			return fc.getSelectedFile();
 		}
 		return null;
 	}
 
 	/**
 	 * Display a message on the status line of the GUI.
 	 * 
 	 * @param status
 	 *            A string representing the message to be displayed.
 	 */
 	public void setStatusLine(String status) {
 		statusLine.setText(status);
 	}
 
 	/**
 	 * Display user confirmation dialog.
 	 * 
 	 * @param message
 	 *            A string representing the message to be displayed in the
 	 *            dialog.
 	 * @param title
 	 *            A string representing the title of the dialog.
 	 * @return An integer representing the option selected by the user.
 	 */
 	public int getUserConfirmation(String message, String title) {
 		return JOptionPane.showConfirmDialog(this, message, title,
 				JOptionPane.YES_NO_OPTION);
 	}
 
 	/**
 	 * Display message dialog.
 	 * 
 	 * @param message
 	 *            A string representing the message to be displayed in the
 	 *            dialog.
 	 * @param title
 	 *            A string representing the title of the dialog.
 	 */
 	public void showMessageDialog(String message, String title) {
 		JOptionPane.showMessageDialog(this, message, title,
 				JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	/**
 	 * Get the GUI component that displays the list of valid orientations.
 	 * 
 	 * @return A reference to the orientations Combo Box.
 	 */
 	public JComboBox<String> getOrientationsBox() {
 		return orientationsBox;
 	}
 
 	/**
 	 * Get the GUI component that displays the list of valid pauses.
 	 * 
 	 * @return A reference to the pauses Combo Box.
 	 */
 	public JComboBox<Short> getPausesBox() {
 		return pausesBox;
 	}
 
 	/**
 	 * Get the GUI component that displays the list of valid places.
 	 * 
 	 * @return A reference to the places Combo Box.
 	 */
 	public JComboBox<String> getPlacesBox() {
 		return placesBox;
 	}
 
 	/**
 	 * Get the GUI component that displays the triplets in the task
 	 * specification.
 	 * 
 	 * @return A reference to the triplets table.
 	 */
 	public JTable getTripletsTable() {
 		return tripletTable;
 	}
 
 	/**
 	 * Get the text displayed in the GUI component that shows which team is now
 	 * in the competition.
 	 * 
 	 * @return A string representing the text displayed.
 	 */
 	public String getConnectedLabel() {
 		return connectedLabel.getText();
 	}
 
 	/**
 	 * Update the view to reflect the changes in the task specification.
 	 * 
 	 * @param evt
 	 *            A TripletEvent object containing the new list of task
 	 *            triplets.
 	 */
 	public void taskSpecChanged(TripletEvent evt) {
 		tripletTableM.clearColumn(0);
 		tripletTableM.setRowCount(evt.getTaskTripletList().size());
 		List<TaskTriplet> tTL = evt.getTaskTripletList();
 		for (int i = 0; i < tTL.size(); i++) {
 			tripletTableM.setValueAt(tTL.get(i).getTaskTripletString(), i, 0);
 		}
 	}
 
 	/**
 	 * Update the view after a team connected.
 	 * 
 	 * @param teamName
 	 *            A string containing the name of the team that has connected.
 	 */
 	public void teamConnected(String teamName) {
 		disconnectButton.setEnabled(true);
 		connectedIcon.setIcon(new ImageIcon(getClass().getResource(
 				"/view/resources/icons/status.png")));
 		connectedLabel.setText(teamName);
 		if (timerStartStopButton.getText().equals("Timer Stop")) {
 			sendTripletsButton.setEnabled(true);
 		}
 	}
 
 	/** Update the view after a team disconnected. */
 	public void teamDisconnected() {
 		disconnectButton.setEnabled(false);
 		connectedIcon.setIcon(new ImageIcon(getClass().getResource(
 				"/view/resources/icons/status-busy.png")));
 		connectedLabel.setText("no client connected");
 	}
 
 	/** Update the view after task specification has been sent to the team. */
 	public void taskSpecSent() {
 		sendTripletsButton.setEnabled(false);
 		disconnectButton.setEnabled(false);
 	}
 
 	/** Update the view after configuration file has been loaded. */
 	public void configFileLoaded() {
 		openButton.setEnabled(true);
 		openFileMenuItem.setEnabled(true);
 		saveButton.setEnabled(true);
 		saveMenuItem.setEnabled(true);
 		loadConfigButton.setEnabled(false);
 		loadConfigMenuItem.setEnabled(false);
 	}
 
 	/**
 	 * Select the entry in the places Combo Box.
 	 * 
 	 * @param place
 	 *            String containing the place label to be selected.
 	 */
 	public void setPlacesBoxSelected(String place) {
 		placesBox.setSelectedItem(place);
 	}
 
 	/**
 	 * Select the entry in the orientations Combo Box.
 	 * 
 	 * @param orientation
 	 *            String containing the orientation to be selected.
 	 */
 	public void setOrientationsBoxSelected(String orientation) {
 		orientationsBox.setSelectedItem(orientation);
 	}
 
 	/**
 	 * Select the entry in the pauses Combo Box.
 	 * 
 	 * @param pause
 	 *            Short integer containing the pause value to be selected.
 	 */
 	public void setPausesBoxSelected(Short pause) {
 		pausesBox.setSelectedItem(pause);
 	}
 
 	/**
 	 * Get the GUI component representing the timer start-stop button.
 	 * 
 	 * @return A reference to the toggle button.
 	 */
 	public JToggleButton getTimerStartStopButton() {
 		return timerStartStopButton;
 	}
 
 	public void addTimerListener(ItemListener timerListener) {
 		timerStartStopButton.addItemListener(timerListener);
 	}
 
 	/**
 	 * Set the text displayed on the Timer Start-Stop button.
 	 * 
 	 * @param text
 	 *            A string containing the text to be displayed.
 	 */
 	public void setTimerStartStopButtonText(String text) {
 		timerStartStopButton.setText(text);
 	}
 
 	/** Set the dimensions of buttons on the GUI. */
 	public void setButtonDimension() {
 		int width = 0;
 		Component[] comp = westPanel.getComponents();
 		// remember the widest Button
 		for (int i = 0; i < 11; i++) {
 			if (comp[i].getPreferredSize().width > width) {
 				width = comp[i].getPreferredSize().width;
 			}
 		}
 		// set all Button widths to the widest one
 		for (int i = 0; i < 11; i++) {
 			// don't change the glues!
 			if (comp[i].getPreferredSize().width != 0) {
 				Dimension dim = comp[i].getPreferredSize();
 				dim.width = width;
 				comp[i].setMaximumSize(dim);
 				comp[i].setPreferredSize(dim);
 				comp[i].setMinimumSize(dim);
 			}
 		}
 	}
 
 	/**
 	 * Set the view to competition mode or non-competition mode, as specified in
 	 * the parameter.
 	 * 
 	 * @param enable
 	 *            A boolean value that is true if competition mode is enabled,
 	 *            and false if otherwise.
 	 */
 	public void setCompetitionMode(Boolean enable) {
 		if (enable) {
 			if (tripletTable.getColumnCount() == 1) {
 				tripletTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 				tripletTableM.addColumn("Passed");
 				tripletTableM.addColumn("Failed");
 				tripletTable.getColumn("Triplets")
 						.setCellRenderer(rendTriplets);
 				rendTriplets.setHorizontalAlignment(JLabel.CENTER);
 				tripletTableScrollPane.setPreferredSize(tripletTable
 						.getPreferredSize());
				Component[] comp = editTripletPanel.getComponents();
 				for (int i = 0; i < comp.length; i++) {
 					// don't change the glues!
 					if (comp[i].getPreferredSize().width != 0) {
 						comp[i].setEnabled(false);
 					}
 				}
 			}
 			timerStartStopButton.setText("Timer Stop");
 			competitionFinishedButton.setEnabled(false);
 			if (disconnectButton.isEnabled()) {
 				sendTripletsButton.setEnabled(true);
 			}
 
 		} else {
 			tripletTableM.setColumnCount(1);
 			tripletTable.getColumn("Triplets").setCellRenderer(rendTriplets);
 			rendTriplets.setHorizontalAlignment(JLabel.CENTER);
 			tripletTableScrollPane.setPreferredSize(tripletTable
 					.getPreferredSize());
			Component[] comp = editTripletPanel.getComponents();
 			for (int i = 0; i < comp.length; i++) {
 				// don't change the glues!
 				if (comp[i].getPreferredSize().width != 0) {
 					comp[i].setEnabled(true);
 				}
 			}
 			competitionFinishedButton.setEnabled(false);
 		}
 		this.validate();
 	}
 
 	private Component competitionFinishedButton() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * Register objects that listen for mouse events in the triplet table area
 	 * of the GUI.
 	 * 
 	 * @param mL
 	 *            An object of type MouseListener.
 	 */
 	public void addtripletTableListener(MouseListener mL) {
 		//tripletTable.addMouseListener(mL);
 	}
 
 	public void setTableCellCorrected(int row, int column) {
 		if (column == 1)
 			tripletTableM.setValueAt(Boolean.FALSE, row, 2);
 		if (column == 2)
 			tripletTableM.setValueAt(Boolean.FALSE, row, 1);
 		//tripletTable.repaint();
 	}
 
 	/**
 	 * Connect Competition Finished button to the corresponding action.
 	 * 
 	 * @param competitionFinished
 	 *            Reference to an object of type Action that performs the
 	 *            functionality.
 	 */
 	public void connectCompetitionFinished(Action competitionFinished) {
 		competitionFinishedButton.setAction(competitionFinished);
 		competitionFinishedButton.setEnabled(false);
 	}
 
 	/** Get the reference to the GUI component Competition Finished button. */
 	public JButton getCompetitionFinishedButton() {
 		return competitionFinishedButton;
 	}
 
 	/** Get the reference to the GUI component Send Triplets button. */
 	public JButton getSendTripletsButton() {
 		return sendTripletsButton;
 	}
 
 	@Override
 	public void timerTick(String currentTime, boolean inTime) {
 		if (inTime)
 			timerLabel.setForeground(Color.black);
 		else
 			timerLabel.setForeground(Color.red);
 		timerLabel.setText(currentTime);
 	}
 
 	@Override
 	public void timerReset(String resetTime) {
 		timerLabel.setForeground(Color.black);
 		timerLabel.setText(resetTime);
 		maxTimeLabel.setVisible(false);
 	}
 
 	@Override
 	public void timerSetMaximumTime(String maxTime) {
 		maxTimeLabel.setVisible(true);
 		maxTimeLabel.setText(maxTime);
 	}
 
 	@Override
 	public void timerOverrun() {
 	}
 }
