 package maelstrom.funge.gui;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 import maelstrom.funge.interpreter.*;
 import maelstrom.funge.interpreter.stack.*;
 import maelstrom.funge.event.*;
 
 
 public class FungeGui implements ActionListener, ChangeListener, RunStateChangeListener {
 
 	private static boolean updating = false;
 
 
 	private JFrame window;
 	private GridEditor gridEditor;
 	private StatusBar status;
 	private StackTableModel stackTableModel;
 
 	private JButton start;
 	private JButton stop;
 	private JButton step;
 	private JToggleButton pause;
 	private JToggleButton fullSpeed;
 	private JSlider speed;
 
 	private Grid cleanGrid;
 	private Funge funge;
 
 	public static void main(String[] args) {
 
 		SwingUtilities.invokeLater(new Runnable() {
 
 			public void run() {
 				System.setProperty("awt.useSystemAAFontSettings","on");
 				System.setProperty("swing.aatext", "true");
 
 				new FungeGui();
 			}
 		});
 
 	}
 
 	public FungeGui() {
 		// Create the window
 		window = new JFrame("Funge Intepreter");
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		window.setLayout(new BorderLayout());
 
 		// Make and place the grid editor
 		Grid grid = Grid.fromString(
 			">               v\n" +
 			"v\"Hello, World!\"< \" Put Hello World string on stack, backwards\n"+
 			">:#,_$v           \" Print the string out\n" +
 			"v ,+19<           \" Print new line\n" +
 			"\n" +
 			">0 1 \\:. 91+, \\:. 91+,v\n" +
 			"   v                  <\n" +
 			"   >:09pv  \" Store top number\n" +
 			"    v.:+<  \" Add two numbers, print result\n" +
 			"    >19+,v \" Print new line\n" +
 			"   ^\\g90 < \" Load from storage",
 			new Dimension(80, 26));
 		this.gridEditor = new GridEditor(grid);
 		window.add(this.gridEditor.createScrollPaneForEditor(), BorderLayout.CENTER);
 
 		// And the status bar
 		status = new StatusBar();
 		window.add(status, BorderLayout.SOUTH);
 		status.setPointer(gridEditor.getPointer());
 
 		// And the stack display
 		this.stackTableModel = new StackTableModel();
 		JScrollPane tableScroll = new JScrollPane(this.stackTableModel.createTableForModel());
 		tableScroll.setMinimumSize(new Dimension(150, 0));
 		tableScroll.setPreferredSize(new Dimension(200, 50));
 
 		window.add(tableScroll, BorderLayout.EAST);
 
 		// And the control bar up top
 		Container buttonPanel = this.makeButtonPanel();
 		window.add(buttonPanel, BorderLayout.NORTH);
 
 		setUpdating(true);
 		toggleButtons(true);
 
 		// Show the window
 		window.pack();
 		window.setVisible(true);
 
 		gridEditor.requestFocusInWindow();
 	}
 
 	/**
 	 * Make the button panel that runs along the top of the window.
 	 */
 	private Container makeButtonPanel() {
 		Container buttonPanel = new Container();
 		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 
 		start = new JButton(createIconButton("image/play_green.png"));
 		stop = new JButton(createIconButton("image/stop_red.png"));
 		pause = new JToggleButton(createIconButton("image/pause_green.png"));
 		step = new JButton(createIconButton("image/next_green.png"));
 		speed = new JSlider(1, 1000, 750);
 		fullSpeed = new JToggleButton(createIconButton("image/forward_green.png"));
 
 		buttonPanel.add(start);
 		buttonPanel.add(stop);
 		buttonPanel.add(pause);
 		buttonPanel.add(step);
 		buttonPanel.add(speed);
 		buttonPanel.add(fullSpeed);
 
 		Insets buttonInsets = new Insets(0,0,0,0);
 		start.setMargin(buttonInsets);
 		stop.setMargin(buttonInsets);
 		pause.setMargin(buttonInsets);
 		step.setMargin(buttonInsets);
 		fullSpeed.setMargin(buttonInsets);
 
 		start.addActionListener(this);
 		stop.addActionListener(this);
 		pause.addActionListener(this);
 		step.addActionListener(this);;
 		speed.addChangeListener(this);
 		fullSpeed.addActionListener(this);
 
 		start.setToolTipText("Start");
 		stop.setToolTipText("Stop");
 		pause.setToolTipText("Pause");
 		step.setToolTipText("Step");
 		fullSpeed.setToolTipText("Full speed");
 
 		return buttonPanel;
 	}
 
 	/**
 	 * Creates an ImageIcon out of the supplied path.
 	 * @param pathToIcon
 	 *        The image path, relative to this class file
 	 * @return
 	 *        The ImageIcon, or null on failure
 	 */
 	public ImageIcon createIconButton(String pathToIcon) {
 
 		java.net.URL imgURL = FungeGui.class.getResource(pathToIcon);
 
 		if (imgURL != null) {
 			return new ImageIcon(imgURL);
 		} else {
 			System.err.println("Couldn't find file: " + pathToIcon);
 			return null;
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == start) {
 			startInterpreter();
 		} else if (e.getSource() == pause) {
 			if (funge != null) {
 				if (pause.isSelected()) {
 					funge.pause();
 				} else {
 					funge.resume();
 				}
 			}
 		} else if (e.getSource() == step) {
 			if (funge != null) {
 				// Pause the interpreter
     			funge.pause();
 
     			// Run a single tick
     			funge.computeNext();
 			}
 		} else if (e.getSource() == stop) {
 			if (funge != null) {
 				funge.stop();
 			}
 		} else if (e.getSource() == fullSpeed) {
 
 			speed.setEnabled(!fullSpeed.isSelected());
 
 			// If funge is running,
 			if (funge != null) {
 				// set full speed on/off
 				FungeGui.setUpdating(!fullSpeed.isSelected());
 				funge.setFullSpeed(fullSpeed.isSelected());
 			}
 		}
 	}
 
     @Override
     public void stateChanged(ChangeEvent e) {
 	    if (e.getSource() == speed) {
 	    	if (funge != null) {
 				double value = 1 - (double)speed.getValue() / speed.getMaximum();
 				double frequency = value * value;
 	    		funge.setSleepTime((int)(frequency * 5000));
 	    	}
 	    }
     }
 
     public void runStateChanged(RunStateChangeEvent e) {
     	switch (e.getRunState()) {
     		case RunStateChangeEvent.START:
     			// The intepreter is always started by us, so we dont need to react to it
     			break;
     		case RunStateChangeEvent.PAUSE:
     			setPause(true);
     			break;
     		case RunStateChangeEvent.RESUME:
     			setPause(false);
     			break;
     		case RunStateChangeEvent.STEP:
     			// Nothing needs doing really
     			break;
     		case RunStateChangeEvent.STOP:
     			stopInterpreter();
     			break;
     	}
     }
 
 
 	/**
 	 * Toggle the program control buttons on and off
 	 * @param startOn
 	 *        True if start should be enabled and visible, false for stop/pause/step
 	 */
 	public void toggleButtons(boolean startOn) {
 		start.setEnabled(startOn);
 		start.setVisible(startOn);
 		gridEditor.setEnabled(startOn);
 
 		startOn = !startOn;
 
 		stop.setEnabled(startOn);
 		stop.setVisible(startOn);
 		step.setEnabled(startOn);
 		pause.setSelected(false);
 		pause.setEnabled(startOn);
 	}
 
 	/**
 	 * Start an instance of Funge interpreter, using the current grid as the program
 	 */
 	public void startInterpreter() {
 		toggleButtons(false);
 
 		cleanGrid = gridEditor.getGrid();
 
 		FungeGui.setUpdating(!fullSpeed.isSelected());
 
 		funge = new Funge(this.cleanGrid.clone());
 		funge.getGrid().addGridChangeListener(gridEditor);
 		funge.getPointer().addPointerChangeListener(gridEditor);
 		funge.setSleepTime(speed.getValue());
 		funge.addRunStateChangeListener(this);
 
 		Pointer pointer = funge.getPointer();
 		status.setPointer(pointer);
 		gridEditor.setPointer(pointer);
 
 		Stack stack = funge.getStack();
		stack.addStackChangeListener(stackDisplay);
 
 		System.out.println(" ** Starting Funge **");
 
 		funge.start();
 	}
 
 	/**
 	 * Pauses or enables the current interpreter
 	 * @param paused
 	 *        True to pause the interpreter, false to cause it to resume.
 	 */
 	public void setPause(boolean paused) {
 		if (paused) {
 			pause.setSelected(true);
 			FungeGui.setUpdating(true);
 		} else {
 			pause.setSelected(false);
 			FungeGui.setUpdating(!fullSpeed.isSelected());
 		}
 	}
 
 	public void stopInterpreter() {
 		System.out.println();
 		System.out.println(" ** Stopping Funge **");
 
 		funge = null;
 
 		gridEditor.setGrid(cleanGrid);
 		gridEditor.repaintAll();
 
 		FungeGui.setUpdating(true);
 
 		toggleButtons(true);
 	}
 
 	public static void setUpdating(boolean isUpdating) {
 		updating = isUpdating;
 	}
 
 	public static boolean isUpdating() {
 		return updating;
 	}
 
 }
