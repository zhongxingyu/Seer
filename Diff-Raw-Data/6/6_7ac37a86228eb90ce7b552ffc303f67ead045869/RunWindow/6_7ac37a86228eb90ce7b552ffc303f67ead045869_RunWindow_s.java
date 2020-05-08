 package GUI;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 
 import Tape.*;
 import TuringMachine.*;
 
 public class RunWindow extends JDialog implements ActionListener, KeyListener {
 
 	/**
 	 * Represents the return values for this window
 	 */
 	public enum ReturnValue {
 		CANCEL, RUN
 	}
 	
 	static final long serialVersionUID = -3667258249137827980L;
 	private final String[][] robots = {{"IPS_03", "00:16:53:13:53:BB"}, {"NXT_03", "00:16:53:0F:DB:8E"}};
 	private final String[] description = {"LEGO-Tape", "Console-Tape", "Graphic-Tape"};
 	protected TuringMachine machine;
 	private JPanel inputContainer;
 	private JPanel tapeSettingsContainer;
 	private JPanel robotSettingsContainer;
 	private JScrollPane inputPane;
 	private JScrollPane tapeSettingsPane;
 	private JScrollPane robotSettingsPane;
 	private JPanel runCancelContainer;
 	private JTabbedPane tabbedPane;
 	@SuppressWarnings({"rawtypes"})	// because of java7
 	private JComboBox[] tapeCombo;
 	@SuppressWarnings("rawtypes")	// because of java7
 	private JComboBox[] robotCombo1;
 	@SuppressWarnings("rawtypes")	// because of java7
 	private JComboBox[] robotCombo2;
 	private JLabel[] tapeLabel;
 	private JLabel[] robotTapeLabel;
 	private JTextField[] tapeName;
 	private JTextField[] tapeInput;
 	private JButton runButton;
 	private JButton cancelButton;
 	private ReturnValue returnValue;
 	
 	/**
 	 * Constructs the run window
 	 * @param machine Turing machine needed to show the run settings
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })		// because of java7
 	public RunWindow(TuringMachine machine) {
 		this.setModal(true);
 		this.machine = machine;
 		
 		// window title and size
 		setTitle("Run");
 		setSize(700, 250);
 		this.setResizable(false);
 		
 		// initialize
 		initRunWindow(machine.getNumberOfTapes());
 		
 		// set enter and escape button listener
 		InputMap inputMap = runButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
 		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
 		inputMap.put(enter, "ENTER");
 		runButton.getActionMap().put("ENTER", new ClickAction(runButton));
 		
 		KeyStroke cancel = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
 		inputMap.put(cancel, "ESC");
 		runButton.getActionMap().put("ESC", new ClickAction(cancelButton));
 		cancelButton.addActionListener(this);
 		runButton.addActionListener(this);
 		
 		// layout for run and cancel button
 		Container contentPane = this.getContentPane();
 		BoxLayout runCancel = new BoxLayout(runCancelContainer, BoxLayout.X_AXIS);
 		runCancelContainer.setLayout(runCancel);
 		runCancelContainer.add(cancelButton);
 		runCancelContainer.add(Box.createHorizontalGlue());
 		runCancelContainer.add(runButton);
 		
 		// show window content
 		for (int i = 0; i < tapeCombo.length; i++) {
 			String tapeType = this.machine.getTapes().get(i).getType();
 			GridBagConstraints c = new GridBagConstraints();
 			
 			// initialize the fields to display the content
 			tapeCombo[i] = new JComboBox();
 			robotCombo1[i] = new JComboBox();
 			robotCombo2[i] = new JComboBox();
 			tapeLabel[i] = new JLabel(this.machine.getTapes().get(i).getName());
 			tapeName[i] = new JTextField(this.machine.getTapes().get(i).getName(), 20);
 			tapeName[i].addKeyListener(this);
 			tapeInput[i] = new JTextField(20);
 			
 			// initialize the combo boxes
 			for (int j = 0; j < description.length; j++) {
 				tapeCombo[i].addItem(description[j]);
 				tapeCombo[i].addActionListener(createItemListener(i));
 				if (tapeType.equals("LEGO")) {
 					tapeCombo[i].setSelectedItem(description[0]);
 				}
 				else if (tapeType.equals("console")) {
 					tapeCombo[i].setSelectedItem(description[1]);
 				}
 				else if (tapeType.equals("gui")) {
 					tapeCombo[i].setSelectedItem(description[2]);
 				}
 			}
 			// set input word for tape i
 			tapeInput[i].setText(machine.getTapes().get(i).getInputWord()); 
 			
 			// layout for tape settings tab
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.gridx = 0;
 			c.gridy = i;
 			c.insets = new Insets(5,5,5,5);
 			tapeSettingsContainer.add(tapeName[i], c);
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.gridx = 1;
 			c.gridy = i;
 			c.insets = new Insets(5,5,5,5);
 			tapeSettingsContainer.add(tapeCombo[i], c);
 			// layout for input tab
 			robotTapeLabel[i] = new JLabel();
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.gridx = 0;
 			c.gridy = i;
 			c.insets = new Insets(5,5,5,5);
 			inputContainer.add(tapeLabel[i], c);
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.gridx = 1;
 			c.gridy = i;
 			c.insets = new Insets(5,5,5,5);
 			inputContainer.add(tapeInput[i], c);
 			// layout for robots tab
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.gridx = 0;
 			c.gridy = i;
 			c.insets = new Insets(5,5,5,5);
 			robotSettingsContainer.add(robotTapeLabel[i], c);
 			for (int j = 0; j < robots.length; j++) {
 				robotCombo1[i].addItem(robots[j][0] + " - " + robots[j][1]);
 				robotCombo2[i].addItem(robots[j][0] + " - " + robots[j][1]);
 			}
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.gridx = 1;
 			c.gridy = i;
 			c.insets = new Insets(5,5,5,5);
 			robotSettingsContainer.add(robotCombo1[i], c);
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.gridx = 2;
 			c.gridy = i;
 			c.insets = new Insets(5,5,5,5);
 			robotSettingsContainer.add(robotCombo2[i], c);
 		}
 		// add scroll bars
 		inputPane = new JScrollPane(inputContainer);
 		tapeSettingsPane = new JScrollPane(tapeSettingsContainer);
 		robotSettingsPane = new JScrollPane(robotSettingsContainer);
 		
 		// add run and cancel button at the end
 		contentPane.add(runCancelContainer, BorderLayout.AFTER_LAST_LINE);
 		
 		// add tabs
 		tabbedPane.addTab("Input", inputPane);
 		tabbedPane.addTab("Tape settings", tapeSettingsPane);
 		tabbedPane.addTab("Robot settings", robotSettingsPane);
 		
 		// add to window
 		contentPane.add(tabbedPane);
 		refreshRobotSettings();
 	}
 	
 	public void refreshRobotSettings() {
 		ArrayList<Tape> tapes = this.machine.getTapes();
 		int counter = 0;
 		for (int i = 0; i < tapes.size(); i++) {
 			if (tapes.get(i).getType() == "LEGO") {
 				robotTapeLabel[i].setText(tapeName[i].getText());
 				robotTapeLabel[i].setVisible(true);
 				robotCombo1[i].setVisible(true);
 				robotCombo2[i].setVisible(true);
 				counter++;
 			}
 			else if (tapes.get(i).getType() == "console" || tapes.get(i).getType() == "gui") {
 				robotTapeLabel[i].setVisible(false);
 				robotCombo1[i].setVisible(false);
 				robotCombo2[i].setVisible(false);
 			}
 		}
 		if (counter == 0) {
 			tabbedPane.setEnabledAt(2, false);
 		}
 		else {
 			tabbedPane.setEnabledAt(2, true);
 		}
 	}
 	
 	/**
 	 * Initializes the window
 	 * @param tapes Number of tapes
 	 */
 	public void initRunWindow(int tapes) {
 		inputContainer  = new JPanel(new GridBagLayout());
 		tapeSettingsContainer = new JPanel(new GridBagLayout());
 		robotSettingsContainer = new JPanel(new GridBagLayout());
 		runCancelContainer = new JPanel();
 		tabbedPane = new JTabbedPane();
 		tapeCombo  = new JComboBox[tapes];
 		robotCombo1 = new JComboBox[tapes];
 		robotCombo2 = new JComboBox[tapes];
 		robotTapeLabel = new JLabel[tapes];
 		tapeLabel = new JLabel[tapes];
 		tapeName = new JTextField[tapes];
 		tapeInput = new JTextField[tapes];
		runButton = new JButton("run");
		cancelButton = new JButton("cancel");
 	}
 	
 	/**
 	 * Shows the window
 	 * @return RUN/CANCEL depending on the user decision
 	 */
 	public ReturnValue showDialog() {
 		this.setVisible(true);
 		
 		return returnValue;
 	}
 	
 	/**
 	 * Updates the input words for the tapes
 	 */
 	private void updateTapeWords() {
 		for (int i = 0; i < tapeInput.length; i++) {
 			Tape tempTape = this.machine.getTapes().get(i);
 			try {
 				tempTape.setInputWord(tapeInput[i].getText());
 			}
 			catch (TapeException te) {
 				ErrorDialog.showError("Error while setting new input word!", te);
 			}
 		}
 	}
 	
 	/**
 	 * Creates an ItemListener listening to changes in the combo boxes
 	 * @param index Number of the combo box
 	 * @return ItemListener for the combo box
 	 */
 	private ActionListener createItemListener(final int index) {
 		return new ActionListener() {
 			public void actionPerformed(ActionEvent e) { //TODO check if it works
 				if (tapeCombo[index].getSelectedItem().toString() == description[0]) {
 					MasterRobot ips_03 = new MasterRobot("IPS_03", "00:16:53:13:53:BB");
 					SlaveRobot nxt_03 = new SlaveRobot("NXT_03", "00:16:53:0F:DB:8E");
 					Tape tape_lego = new LEGOTape(machine.getTapes().get(index).getName() ,ips_03, nxt_03);
 					
 					try {
 						tape_lego.setInputWord(machine.getTapes().get(index).getInputWord());
 					} catch (TapeException e1) {
 						ErrorDialog.showError("Error changing Tapetype", e1);
 					}						
 					machine.getTapes().remove(index);						
 					machine.getTapes().add(index, tape_lego);
 					if (tabbedPane.getTabCount() == 3) {
 						refreshRobotSettings();
 					}
 				}
 				else if (tapeCombo[index].getSelectedItem().toString() == description[1]) {
 					Tape tape_console = new ConsoleTape(machine.getTapes().get(index).getName());
 					try {
 						tape_console.setInputWord(machine.getTapes().get(index).getInputWord());
 					} catch (TapeException e1) {
 						ErrorDialog.showError("Error changing Tapetype", e1);
 					}						
 					machine.getTapes().remove(index);						
 					machine.getTapes().add(index, tape_console);
 					if (tabbedPane.getTabCount() == 3) {
 						refreshRobotSettings();
 					}
 				}
 				else if (tapeCombo[index].getSelectedItem().toString() == description[2]) {
 					// TODO create graphic tape
 					if (tabbedPane.getTabCount() == 3) {
 						refreshRobotSettings();
 					}
 				}
 			}
 		};
 	}
 	
 	/**
 	 * Responds to a clicked button
 	 */
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == cancelButton) {
 			updateTapeWords();
 			this.setVisible(false);
 			dispose();
 			returnValue = ReturnValue.CANCEL;
 		}
 		else if(e.getSource() == runButton) {
 			updateTapeWords();
 			this.setVisible(false);
 			dispose();
 			returnValue = ReturnValue.RUN;
 		}
 	}
 	
 	/**
 	 * Responds to a released key
 	 */
 	public void keyReleased(KeyEvent e) {
 		for (int i = 0; i < tapeName.length; i++) {
 			tapeLabel[i].setText(tapeName[i].getText());
 			this.machine.getTapes().get(i).setName(tapeName[i].getText());
 		}
 	}
 	
 	/**
 	 * Responds to a pressed key
 	 */
 	public void keyPressed(KeyEvent e) {
 		
 	}
 	
 	/**
 	 * Responds to typed key
 	 */
 	public void keyTyped(KeyEvent e) {
 		
 	}
 	
 	/**
 	 * Used for the window focused actions
 	 */
 	public class ClickAction extends AbstractAction {
 		static final long serialVersionUID = -3667258249137827980L;
 		private JButton button;
 		
 		public ClickAction(JButton button) {
 			this.button = button;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			button.doClick();
 		}
 	}
 }
