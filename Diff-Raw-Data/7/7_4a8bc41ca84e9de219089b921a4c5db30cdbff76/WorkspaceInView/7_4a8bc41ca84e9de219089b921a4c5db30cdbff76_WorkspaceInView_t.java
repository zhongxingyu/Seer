 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.ResourceBundle;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 import model.IModel;
 import util.DataSource;
 import util.Location;
 
 public class WorkspaceInView extends JComponent {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private int myID;
 	private static final int FIELD_SIZE = 20;
 	private static final String DEFAULT_RESOURCE_PACKAGE = "view.resources.";
 	private static final String USER_DIR = "user.dir";
 	private static final JFileChooser FILE_CHOOSER = new JFileChooser(System
 			.getProperties().getProperty(USER_DIR));
 	private static final String RESOURCE_LOCATION = "/images/";
 	private static final Color DEFAULT_COLOR = Color.white;
 
 	private JTextArea myCommandHistoryTextArea;
 	private JLabel myTurtlePositionLabel;
 	private JLabel myTurtleHeadingLabel;
 	private JTextField myCommandLineTextField;
 
 	private JButton myClearButton;
 	private JButton myToggleGridButton;
 	private JButton myChangeBackgroundButton;
 	private JButton myToggleHighlightButton;
 	private JTextArea myUserVariables;
 	private JTextArea myUserFuncs;
 	private Image myBackgroundImage;
 	private Map<String, Color> myColorCollection;
 
 	private ResourceBundle myResources;
 	private IModel myModel;
 	private Canvas myCanvas;
 	private DataSource myDataSource;
 
 	public WorkspaceInView(IModel model, Dimension canvasBounds,
 			String language, int id) {
 
 		myID = id;
 		myModel = model;
 		myResources = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE
 				+ language);
 		myCanvas = new Canvas(canvasBounds);
 		myModel.switchToWorkspace(id);
 		myDataSource = myModel.getDataSource();
 
 		initialize();
 		myCanvas.update(myDataSource.getPaintableIterator(), null);
 		// the following lines are for testing!
 		showVariables("testing testing");
 		showFunctions("testing testing");
 
 	}
 
 	/**
 	 * adds components to GUI
 	 */
 	private void initialize() {
 		setLayout(new BorderLayout());
 		loadColorCollection();
 		this.add(makeCommandLinePanel(), BorderLayout.SOUTH);
 		this.add(makeCommandHistory(), BorderLayout.WEST);
 		this.add(makeUserDefinedFuncAndVarDisplay(), BorderLayout.EAST);
 		this.add(makeTurtleDisplay(), BorderLayout.CENTER);
 		setVisible(true);
 	}
 
 	private void loadColorCollection() {
 		myColorCollection = new HashMap<String, Color>();
 		myColorCollection.put("grey", Color.LIGHT_GRAY);
 		myColorCollection.put("yellow", Color.yellow);
 		myColorCollection.put("green", Color.green);
 
 	}
 
 	private JComponent makeCommandHistory() {
 		JPanel commandHistoryPanel = new JPanel();
 		commandHistoryPanel.setLayout(new BorderLayout());
 		commandHistoryPanel.add(
 				new JLabel(myResources.getString("CommandHistory")),
 				BorderLayout.NORTH);
 		myCommandHistoryTextArea = new JTextArea(FIELD_SIZE, FIELD_SIZE);
 		myCommandHistoryTextArea.setEditable(false);
 		JScrollPane scrollPane = new JScrollPane(myCommandHistoryTextArea);
 		scrollPane
 				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		commandHistoryPanel.add(scrollPane, BorderLayout.CENTER);
 		commandHistoryPanel.add(makeClearButton(), BorderLayout.SOUTH);
 
 		return commandHistoryPanel;
 
 	}
 
 	/**
 	 * Displays the canvas and the heading and position label for the turtle on
 	 * the canvas.
 	 * 
 	 * @return JComponent representing the canvas and turtle state information
 	 */
 	private JComponent makeTurtleDisplay() {
 
 		JPanel turtleInfoPanel = new JPanel();
 		turtleInfoPanel.setLayout(new BorderLayout());
 
 		JPanel canvasPanel = new JPanel();
 		canvasPanel.add(myCanvas);
 		myCanvas.setBorder(BorderFactory.createLineBorder(Color.black));
 		turtleInfoPanel.add(canvasPanel, BorderLayout.CENTER);
 
 		JPanel state = new JPanel();
 
 		myTurtlePositionLabel = new JLabel();
 		myTurtleHeadingLabel = new JLabel();
 		state.add(myTurtlePositionLabel);
 		state.add(myTurtleHeadingLabel);
 		updateHeadingLabel(myDataSource.getTurtleHeading());
 		updatePositionLabel(myDataSource.getTurtlePosition());
 		state.add(makeChangeBackgroundButton());
 		state.add(makeToggleHighlight());
 		state.add(makeToggleGridButton());
 		turtleInfoPanel.add(state, BorderLayout.SOUTH);
 
 		return turtleInfoPanel;
 
 	}
 
 	/**
 	 * Creates the comand line, including a typable command line text box and a
 	 * label.
 	 * 
 	 * @return JComponent representing the command line panel
 	 */
 	private JComponent makeCommandLinePanel() {
 		JPanel commandLinePanel = new JPanel();
 		commandLinePanel.setLayout(new BoxLayout(commandLinePanel,
 				BoxLayout.LINE_AXIS));
 
 		commandLinePanel.add(new JLabel(myResources.getString("CommandLine")));
 		commandLinePanel.add(makeCommandLine());
 		return commandLinePanel;
 	}
 
 	/**
 	 * Creates a command line that passes info to the Model and clears the text
 	 * field upon hitting enter.
 	 * 
 	 * @return JTextField representing the command line text field
 	 */
 	private JTextField makeCommandLine() {
 		myCommandLineTextField = new JTextField(FIELD_SIZE);
 		myCommandLineTextField.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String givenCommand = myCommandLineTextField.getText();
 				showMessage(myResources.getString("TextBoxCommand")
 						+ givenCommand);
 				myModel.executeCommand(givenCommand);
 				myCommandLineTextField.setText("");
 				update();
 			}
 		});
 		return myCommandLineTextField;
 	}
 
 	private JComponent makeUserDefinedFuncAndVarDisplay() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout());
 		Border raisedetched = BorderFactory
 				.createEtchedBorder(EtchedBorder.RAISED);
 		myUserVariables = new JTextArea(FIELD_SIZE / 2, FIELD_SIZE);
 		myUserVariables.setEditable(false);
 
 		myUserFuncs = new JTextArea(FIELD_SIZE / 2, FIELD_SIZE);
 		myUserFuncs.setEditable(false);
 
 		JPanel top = new JPanel();
 		top.setLayout(new BorderLayout());
 		top.add(myUserVariables);
 		top.add(new JLabel(myResources.getString("my_variables")),
 				BorderLayout.NORTH);
 		top.setBorder(raisedetched);
 
 		JPanel bottom = new JPanel();
 		bottom.setLayout(new BorderLayout());
 		bottom.add(myUserFuncs);
 		bottom.add(new JLabel(myResources.getString("my_functions")),
 				BorderLayout.NORTH);
 		bottom.setBorder(raisedetched);
 
 		panel.add(top, BorderLayout.WEST);
 		panel.add(bottom, BorderLayout.EAST);
 		return panel;
 
 	}
 
 	/**
 	 * Makes the button that clears the command history.
 	 * 
 	 * @return JButton representing the clear button
 	 */
 	private JButton makeClearButton() {
 		myClearButton = new JButton(myResources.getString("ClearCommand"));
 		myClearButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				clearCommandWindow();
 			}
 		});
 		return myClearButton;
 	}
 
 	private JButton makeToggleGridButton() {
 		myToggleGridButton = new JButton(myResources.getString("toggle_grid"));
 		myToggleGridButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				myCanvas.toggleGrid();
 				update();
 			}
 		});
 		return myToggleGridButton;
 	}
 
 	private JButton makeChangeBackgroundButton() {
 		myChangeBackgroundButton = new JButton(
 				myResources.getString("change_background"));
 		myChangeBackgroundButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int response = FILE_CHOOSER.showDialog(null,
 						myResources.getString("select_image"));
 				if (response == JFileChooser.APPROVE_OPTION) {
 					String fileName = FILE_CHOOSER.getSelectedFile().getName();
 					Image myImage = new ImageIcon(getClass().getResource(
 							RESOURCE_LOCATION + fileName)).getImage();
 					myModel.addBackgroundImage(myImage);
 				}
 				updateAndSuppressOutput(); // deal with turtle disappearance after action
 			}
 
 		});
 		return myChangeBackgroundButton;
 
 	}
 
 	private JButton makeToggleHighlight() {
 		myToggleHighlightButton = new JButton(
 				myResources.getString("toggle_highlight"));
 		myToggleHighlightButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 			        myDataSource.toggleHighlighter();
 				updateAndSuppressOutput(); // deal with turtle disappearance after action
 
 			}
 
 		});
 		return myToggleHighlightButton;
 	}
 
 	public void showMessage(String message) {
 		myCommandHistoryTextArea.append(message + "\n");
 	}
 
 	/**
 	 * Clears the command window.
 	 */
 	private void clearCommandWindow() {
 		myCommandHistoryTextArea.setText("");
 	}
 
 	/**
 	 * show user-defined variables on relevant panel
 	 * 
 	 * @param var
 	 */
 
 	private void showVariables(String var) {
 		myUserVariables.setText(var);
 	}
 
 	/**
 	 * show user-defined functions on relevant panel
 	 * 
 	 * @param func
 	 */
 
 	private void showFunctions(String func) {
 		myUserFuncs.setText(func);
 	}
 
 	/**
 	 * Updates the position label with the new location.
 	 * 
 	 * @param location
 	 */
 	public void updatePositionLabel(Location location) {
 		myTurtlePositionLabel.setText(myResources.getString("Position") + " "
 				+ (int) location.getX() + ", " + (int) location.getY());
 	}
 
 	/**
 	 * updates the heading label with the new heading.
 	 * 
 	 * @param heading
 	 */
 	public void updateHeadingLabel(int heading) {
 		myTurtleHeadingLabel.setText(myResources.getString("Heading") + " "
 				+ heading);
 
 	}
 
 	public void update() {
 		myCanvas.update(myDataSource.getPaintableIterator(),
 				myDataSource.getBackgroundImage());
 		updateHeadingLabel(myDataSource.getTurtleHeading());
 		updatePositionLabel(myDataSource.getTurtlePosition());
 		showMessage("" + myDataSource.getReturnValue());
 
 	}
 	
 	public void updateAndSuppressOutput () {
 	    myCanvas.update(myDataSource.getPaintableIterator(),
                             myDataSource.getBackgroundImage());
 	    updateHeadingLabel(myDataSource.getTurtleHeading());
             updatePositionLabel(myDataSource.getTurtlePosition());
 	}
 
 }
