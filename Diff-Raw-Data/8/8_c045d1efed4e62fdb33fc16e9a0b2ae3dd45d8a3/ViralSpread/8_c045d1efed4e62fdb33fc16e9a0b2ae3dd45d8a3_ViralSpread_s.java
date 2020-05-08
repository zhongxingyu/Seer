 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 
// -----------1.0-------------
// TODO: Display edge probability (num. connections / (peopleCount*peopleCount))
 // -----------2.0-------------
 // TODO: Output data (evolution of each statistic over time) in a file when the simulation is complete
 // TODO: Allow saving graph(s) when the simulation is complete
 // TODO: Show more graphs when the simulation is complete
 // TODO: Show a graph of the degree distribution during the simulation
 // TODO: Allow configuring the contagiousness of each virus via the UI
 // TODO: Add an option to do a random walk instead of bounce
 // TODO: Show the diameter of the largest connected component
 // -----------3.0-------------
 // TODO: Make nodes with more hard connections optionally more contagious
 // TODO: If paused and you mouse over a node, show its hard connections in a different color
 // TODO: Show the largest hard-connected component in a different color
 // TODO: Add an option for nodes to grow in size as their degree increases
 // ---------------------------
 // NOTE: Example code to save the generated graph: http://www.exampledepot.com/egs/javax.imageio/Graphic2File.html
 public class ViralSpread {
 	
 	public static boolean debug = false;
 	
 	private static final double
 		SLOW_MAX_VELOCITY = 3.0,
 		MEDIUM_MAX_VELOCITY = 5.5,
 		FAST_MAX_VELOCITY = 8.0,
 		VERY_FAST_MAX_VELOCITY = 13.0;
 	private static final int DEFAULT_PEOPLECOUNT = 48;
 	private static final double DEFAULT_CONTAGIOUSNESS = 0.50;
 	private static final ShowConnections DEFAULT_SHOWCONNECTIONS = ShowConnections.HARD;
 	private static final int DEFAULT_VIRUSCOUNT = 8;
 	
 	private static double maxVelocity = FAST_MAX_VELOCITY;
 	private static double contagiousness = DEFAULT_CONTAGIOUSNESS;
 	private static int peopleCount = DEFAULT_PEOPLECOUNT;
 	private static ShowConnections connectionType = DEFAULT_SHOWCONNECTIONS;
 	private static int virusCount = DEFAULT_VIRUSCOUNT;
 
 	final JFrame frame;
 	final JButton startStopButton;
 	final JComboBox peopleSpeed;
 	final JComboBox defaultNodeCount;
 	final JComboBox defaultVirusCount;
 	final JComboBox defaultContagion;
 	final ScoreLabel totalCollisionsLabel;
 	final JComboBox connectionTypeSelect;
 	
 	public enum ShowConnections {
 		HARD,
 		SOFT,
 		HARD_SOFT,
 		NONE
 	}
 
 	public static void main(final String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				new ViralSpread();
 			}
 		});
 	}
 
 	public ViralSpread() {
     	// Set up the window.
     	frame = new JFrame("Contagion");
 		frame.setLocation(200, 0);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Set up the panel in which the game is actually played.
 		final GamePanel gamePanel = new GamePanel(this);
 		frame.add(gamePanel, BorderLayout.CENTER);
 		
 		// Set up the information panel.
 		final JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		Dimension d = new Dimension(224, 600); // enough room for the numbers to grow
 		infoPanel.setMinimumSize(d);
 		infoPanel.setPreferredSize(d);
 		frame.add(infoPanel, BorderLayout.LINE_END);
 		final JLabel numComponents = new JLabel("Number of components: 0");
 		final JLabel largestComponent = new JLabel("Largest component: 0");
 		TimerLabel timeElapsed = new TimerLabel(200, new TimerListener() {
 			public void execute(TimerLabel parent) {
 				parent.setText(Statistics.getElapsedTime());
 				numComponents.setText("Number of components: "+ Statistics.getNumComponents());
 				largestComponent.setText("Largest component: "+ Statistics.getLargestConnectedComponent());
 			}
 		});
 		infoPanel.add(timeElapsed);
 		infoPanel.add(new JLabel("                    "));
 		totalCollisionsLabel = new ScoreLabel("Total collisions: 0", "Total collisions", 0);
 		infoPanel.add(totalCollisionsLabel);
 		Statistics.setup(totalCollisionsLabel);
 		final JLabel numHardConnections = new JLabel("Number of hard connections: 0");
 		final JLabel numSoftConnections = new JLabel("Number of soft connections: 0");
 		final JLabel hardSoftRatio = new JLabel("Hard:soft ratio: 0");
 		final JLabel hardPerNode = new JLabel("Avg. hard connections per node: 0");
 		final JLabel maxPerNode = new JLabel("Max connections: 0");
 		final JLabel edgeProbability = new JLabel("Edge probability: 0");
 		final JLabel maxDistDisplay = new JLabel("Largest connection distance: 0");
 		final JLabel avgDistDisplay = new JLabel("Average connection distance: 0");
 		TimerLabel virusCountTable = new TimerLabel(1000, new TimerListener() {
 			public void execute(TimerLabel parent) {
 				parent.setText(Statistics.getVirusCountTable());
 				int numHard = Statistics.getNumHardConnections();
 				int numSoft = Statistics.getNumSoftConnections();
 				double hsRatio = Math.round((numSoft == 0 ? 0 : numHard / (double) numSoft) * 1000) / 1000.0;
 				double perNode = Math.round((numHard / (double) peopleCount) * 1000) / 1000.0;
 				double edgeProb = Math.round(numHard / (double) (peopleCount*peopleCount) * 1000) / 1000.0;
 				numHardConnections.setText("Number of hard connections: "+ numHard);
 				numSoftConnections.setText("Number of soft connections: "+ numSoft);
 				hardSoftRatio.setText("Hard:soft ratio: "+ hsRatio);
 				hardPerNode.setText("Avg. hard connections per node: "+ perNode);
 				maxPerNode.setText("Max connections: "+ Statistics.getMaxConnections());
 				edgeProbability.setText("Edge probability: "+ edgeProb);
 				maxDistDisplay.setText("Largest connection distance: "+ Statistics.getLargestPhysicalDistance());
 				avgDistDisplay.setText("Average connection distance: "+ Statistics.getAveragePhysicalDistance());
 			}
 		});
 		infoPanel.add(virusCountTable);
 		infoPanel.add(numHardConnections);
 		infoPanel.add(numSoftConnections);
 		infoPanel.add(hardSoftRatio);
 		infoPanel.add(hardPerNode);
 		infoPanel.add(maxPerNode);
 		infoPanel.add(edgeProbability);
 		infoPanel.add(maxDistDisplay);
 		infoPanel.add(avgDistDisplay);
 		infoPanel.add(numComponents);
 		infoPanel.add(largestComponent);
 		
 		
 		// Set up the control panel.
 		JPanel controlPanel = new JPanel();
 		frame.add(controlPanel, BorderLayout.PAGE_END);
 		startStopButton = new JButton("Start");
 		controlPanel.add(startStopButton);
 		startStopButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String text = startStopButton.getText();
 				if (text.equals("Start")) {
 					startStopButton.setText("Stop");
 					gamePanel.startTimer();
 				}
 				else if (text.equals("Stop")) {
 					startStopButton.setText("Start");
 					gamePanel.stopTimer();
 				}
 			}
 		});
 		final JButton resetButton = new JButton("Reset");
 		controlPanel.add(resetButton);
 		resetButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (startStopButton.getText().equals("Stop"))
 					startStopButton.setText("Start");
 				reset(gamePanel);
 			}
 		});
 		controlPanel.add(new JLabel("Speed: "));
 		String[] peopleSpeeds = {"Slow", "Medium", "Fast", "Very fast"};
 		peopleSpeed = new JComboBox(peopleSpeeds);
 		controlPanel.add(peopleSpeed);
 		peopleSpeed.setSelectedItem("Fast");
 		peopleSpeed.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String result = (String) peopleSpeed.getSelectedItem();
 				if (result.equals("Slow"))
 					maxVelocity = SLOW_MAX_VELOCITY;
 				else if (result.equals("Medium"))
 					maxVelocity = MEDIUM_MAX_VELOCITY;
 				else if (result.equals("Fast"))
 					maxVelocity = FAST_MAX_VELOCITY;
 				else if (result.equals("Very fast"))
 					maxVelocity = VERY_FAST_MAX_VELOCITY;
 				if (startStopButton.getText().equals("Start"))
 					reset(gamePanel);
 			}
 		});
 		controlPanel.add(new JLabel("Show connections: "));
 		String[] connectionTypes = {"Hard", "Soft", "Both", "None"};
 		connectionTypeSelect = new JComboBox(connectionTypes);
 		controlPanel.add(connectionTypeSelect);
 		connectionTypeSelect.setSelectedItem("Hard");
 		connectionTypeSelect.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String result = (String) connectionTypeSelect.getSelectedItem();
 				if (result.equals("Hard"))
 					connectionType = ShowConnections.HARD;
 				else if (result.equals("Soft"))
 					connectionType = ShowConnections.SOFT;
 				else if (result.equals("Both"))
 					connectionType = ShowConnections.HARD_SOFT;
 				else if (result.equals("None"))
 					connectionType = ShowConnections.NONE;
 				gamePanel.repaint();
 			}
 		});
 		controlPanel.add(new JLabel("Nodes: "));
 		Integer[] nodeOptions = new Integer[99];
 		for (int i = 2; i <= 100; i++)
 			nodeOptions[i-2] = i;
 		defaultNodeCount = new JComboBox(nodeOptions);
 		controlPanel.add(defaultNodeCount);
 		defaultNodeCount.setSelectedItem(DEFAULT_PEOPLECOUNT);
 		defaultNodeCount.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				peopleCount = (Integer) defaultNodeCount.getSelectedItem();
 				if (startStopButton.getText().equals("Start"))
 					reset(gamePanel);
 			}
 		});
 		controlPanel.add(new JLabel("Viruses: "));
 		Integer[] virusOptions = new Integer[8];
 		for (int i = 2; i <= 9; i++)
 			virusOptions[i-2] = i;
 		defaultVirusCount = new JComboBox(virusOptions);
 		controlPanel.add(defaultVirusCount);
 		defaultVirusCount.setSelectedItem(DEFAULT_VIRUSCOUNT);
 		defaultVirusCount.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				virusCount = (Integer) defaultVirusCount.getSelectedItem();
 				if (startStopButton.getText().equals("Start"))
 					reset(gamePanel);
 			}
 		});
 		controlPanel.add(new JLabel("Contagiousness: "));
 		Double[] contagionOptions = new Double[100];
 		for (int i = 1; i <= 100; i++) {
 			contagionOptions[i-1] = i/100.0;
 		}
 		defaultContagion = new JComboBox(contagionOptions);
 		controlPanel.add(defaultContagion);
 		defaultContagion.setSelectedIndex((int)(DEFAULT_CONTAGIOUSNESS * 100) - 1);
 		defaultContagion.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				contagiousness = (Double) defaultContagion.getSelectedItem();
 				if (startStopButton.getText().equals("Start"))
 					reset(gamePanel);
 			}
 		});
 
 		// Put the frame on the screen
 		frame.pack();
         frame.setVisible(true);
 	}
 	
 	// The number of people in the simulation. Avoid numbers above 100.
 	public static int getPeopleCount() {
 		return peopleCount;
 	}
 	
 	// Set the number of people. Assumed to be between 0 and 100.
 	public static void setPeopleCount(int c) {
 		peopleCount = c;
 	}
 
 	public static double getContagiousness() {
 		return contagiousness;
 	}
 
 	public static void setContagiousness(double c) {
 		contagiousness = c;
 	}
 
 	public static ShowConnections getConnectionType() {
 		return connectionType;
 	}
 
 	public static void setConnectionType(ShowConnections c) {
 		connectionType = c;
 	}
 
 	public static int getVirusCount() {
 		return virusCount;
 	}
 	
 	public static void setVirusCount(int c) {
 		virusCount = c;
 	}
 
 	// Maximum starting speed of any person in pixels per cycle.
 	public static double getMaxVelocity() {
 		return maxVelocity;
 	}
 	
 	// Maximum speed along a single dimension.
 	public static double getMax1DVelocity() {
 		return getMaxVelocity() / Math.sqrt(2);
 	}
 	
 	// Minimum starting speed of any person in pixels per cycle.
 	public static double getMinVelocity() {
 		return Math.max(0.0, getMaxVelocity()-1.5);
 	}
 	
 	// Set the maximum velocity. Assumed to be between 1.5 and 8.
 	public static void setMaxVelocity(double max) {
 		maxVelocity = max;
 	}
 	
 	// Show data after the simulation finishes.
 	public void showGraphs() {
 		JDialog dialog = new JDialog(frame);
 		GraphPanel contents = new GraphPanel();
 		dialog.setContentPane(contents);
 		dialog.pack();
 		dialog.setVisible(true);
 	}
 	
 	void disableStartStopButton() {
 		startStopButton.setEnabled(false);
 	}
 	
 	// Restart the simulation.
 	private void reset(GamePanel gamePanel) {
 		gamePanel.reset();
 		startStopButton.setEnabled(true);
 	}
 
 }
