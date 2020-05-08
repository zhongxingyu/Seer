 package game.gui;
 
 import game.logic.Game;
 import game.ui.GameInterface;
 import game.ui.GameOptions;
 import game.ui.GameOutput;
 import game.ui.MazePictures;
 import general_utilities.MazeInput;
 import general_utilities.WaitTime;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 
 public class GUInterface extends GameInterface {
 
 	public static final int SPRITESIZE = 32;
 	private final MazePictures mazePictures = new MazePictures();
 	private JFrame frame = new JFrame("Maze");
 	private MazePanel mazePanel;
 	private InfoPanel infoPanel;
 	private JTextField rowsTextField;
 	private JTextField columnsTextField;
 
 	private boolean usePredefinedMaze = true;
 	private boolean useMultipleDragons = true;
 	private int dragonType;
 
 	private GameOptions options = new GameOptions(false);
 
 	private void startInterface() {
 
 		Dimension mazePanelDimension = new Dimension(game.getMaze().getColumns() * GUInterface.SPRITESIZE,
 				game.getMaze().getRows() * GUInterface.SPRITESIZE);
 
 		Dimension infoPanelDimension = new Dimension(game.getMaze().getColumns() * GUInterface.SPRITESIZE,
 				100);
 
 		Container c = frame.getContentPane();
 		c.setLayout(new BorderLayout());
 
 		mazePanel = new MazePanel(game, mazePictures, mazePanelDimension);
 		infoPanel = new InfoPanel(infoPanelDimension);
 		c.add(mazePanel);
 		c.add(infoPanel, BorderLayout.PAGE_START);
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(false);
 		frame.pack();
 		frame.setVisible(true);
 
 		mainLoop();
 	}
 
 
 	public void startGame() {
 		startOptions();
 	}
 
 	public void startOptions() {
 
 		final JFrame optionsFrame = new JFrame("Maze: New Game");
 
 		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setPreferredSize(new Dimension(500, 250));
 		optionsFrame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
 
 		JPanel mazeSizePanel = new JPanel();
 		tabbedPane.addTab("Maze Size", null, mazeSizePanel, null);
 		mazeSizePanel.setLayout(null);
 
 		JLabel predefMazeLabel = new JLabel("Use the predefined maze?");
 		predefMazeLabel.setBounds(8, 0, 256, 30);
 		mazeSizePanel.add(predefMazeLabel);
 
 		ButtonGroup predefMaze = new ButtonGroup();
 
 		JRadioButton yesPredefButton = new JRadioButton("Yes");
 		predefMaze.add(yesPredefButton);
 		yesPredefButton.setSelected(true);
 		yesPredefButton.setBounds(0, 40, 54, 23);
 		mazeSizePanel.add(yesPredefButton);
 
 		yesPredefButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				columnsTextField.setEnabled(false);
 				rowsTextField.setEnabled(false);
 				usePredefinedMaze = true;
 			}
 		});
 
 		JRadioButton noPredefButton = new JRadioButton("No, let me create a custom one");
 		predefMaze.add(noPredefButton);
 		noPredefButton.setBounds(56, 36, 299, 30);
 		mazeSizePanel.add(noPredefButton);
 
 		noPredefButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				columnsTextField.setEnabled(true);
 				rowsTextField.setEnabled(true);
 				usePredefinedMaze = false;
 			}
 		});
 
 		JLabel lblRows = new JLabel("Rows:");
 		lblRows.setBounds(8, 110, 46, 14);
 		mazeSizePanel.add(lblRows);
 
 		JLabel lblColumns = new JLabel("Columns:");
 		lblColumns.setBounds(8, 135, 65, 14);
 		mazeSizePanel.add(lblColumns);
 
 		rowsTextField = new JTextField();
 		rowsTextField.setBounds(71, 107, 86, 20);
 		rowsTextField.setEnabled(false);
 		mazeSizePanel.add(rowsTextField);
 		rowsTextField.setColumns(10);
 
 		columnsTextField = new JTextField();
 		columnsTextField.setBounds(71, 132, 86, 20);
 		columnsTextField.setEnabled(false);
 		mazeSizePanel.add(columnsTextField);
 		columnsTextField.setColumns(10);
 
 		JPanel dragonOptionsPanel = new JPanel();
 		tabbedPane.addTab("Dragon Options", null, dragonOptionsPanel, null);
 
 		JLabel dragonTypeLabel = new JLabel("What kind of dragons would you like in the maze?");
 		dragonTypeLabel.setBounds(10, 11, 377, 14);
 		dragonTypeLabel.setAlignmentY(Component.TOP_ALIGNMENT);
 		dragonTypeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		ButtonGroup dragonTypeButtons = new ButtonGroup();
 
 		JRadioButton randomlySleepingButton = new JRadioButton("Randomly sleeping");
 		randomlySleepingButton.setSelected(true);
 		randomlySleepingButton.setBounds(10, 32, 234, 23);
 		dragonTypeButtons.add(randomlySleepingButton);
 		dragonOptionsPanel.setLayout(null);
 		dragonOptionsPanel.add(dragonTypeLabel);
 		dragonOptionsPanel.add(randomlySleepingButton);
 
 		randomlySleepingButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				dragonType = 2;
 			}
 		});
 
 		JRadioButton staticButton = new JRadioButton("Static");
 		staticButton.setBounds(10, 84, 234, 23);
 		dragonTypeButtons.add(staticButton);
 		dragonOptionsPanel.add(staticButton);
 
 		staticButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				dragonType = 0;
 			}
 		});
 
 		JRadioButton alwaysAwakeButton = new JRadioButton("Always awake");
 		alwaysAwakeButton.setBounds(10, 58, 234, 23);
 		dragonTypeButtons.add(alwaysAwakeButton);
 		dragonOptionsPanel.add(alwaysAwakeButton);
 
 		alwaysAwakeButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				dragonType = 1;
 			}
 		});
 
 		JLabel multipleDragonLabel = new JLabel("Generate multiple dragons?");
 		multipleDragonLabel.setBounds(10, 131, 262, 14);
 		dragonOptionsPanel.add(multipleDragonLabel);
 
 		ButtonGroup multipleDragonButtons = new ButtonGroup();
 
 		JRadioButton yesMultipleDragonsButton = new JRadioButton("Yes");
 		yesMultipleDragonsButton.setSelected(true);
 		yesMultipleDragonsButton.setToolTipText("Dragons will be generated proportionally to maze size!");
 		yesMultipleDragonsButton.setBounds(10, 152, 109, 23);
 		multipleDragonButtons.add(yesMultipleDragonsButton);
 		dragonOptionsPanel.add(yesMultipleDragonsButton);
 
 		yesMultipleDragonsButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				useMultipleDragons = true;
 			}
 		});
 
 		JRadioButton noMultipleDragonsButton = new JRadioButton("No");
 		noMultipleDragonsButton.setToolTipText("Only one dragon will be generated!");
 		noMultipleDragonsButton.setBounds(10, 178, 109, 23);
 		multipleDragonButtons.add(noMultipleDragonsButton);
 		dragonOptionsPanel.add(noMultipleDragonsButton);
 
 		yesMultipleDragonsButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				useMultipleDragons = false;
 			}
 		});
 
 		JButton btnEnterTheMaze = new JButton("Enter the maze!");
 		btnEnterTheMaze.setBounds(251, 84, 136, 61);
 		dragonOptionsPanel.add(btnEnterTheMaze);
 
 		btnEnterTheMaze.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 
 				//Get Maze options from user
 				options.randomMaze = !usePredefinedMaze;
 
 				//Get Dragon options from user
 				options.dragonType = dragonType;
 
 				//Get Multiple dragon options
 				options.multipleDragons = useMultipleDragons;
 
 				options.randomSpawns = true;
 
 				if(!usePredefinedMaze) {
 					if(MazeInput.isInteger(rowsTextField.getText()) && MazeInput.isInteger(columnsTextField.getText())) {
 						options.rows = Integer.parseInt(rowsTextField.getText()); 
 						options.columns = Integer.parseInt(columnsTextField.getText());
 
 						if(options.rows < 6 || options.columns < 6) {
 							JOptionPane.showMessageDialog(optionsFrame,
 									"Please specify a row number and a column number equal or bigger than 5!",
 									"Maze size error",
 									JOptionPane.ERROR_MESSAGE);
 
 							tabbedPane.setSelectedIndex(0);
 						}
 						else {
 							game = new Game(options);
 
 							optionsFrame.setVisible(false);
 							startInterface();
 						}
 					}
 					else {
 						JOptionPane.showMessageDialog(optionsFrame,
 								"Please input numbers on the rows/columns fields, not gibberish.",
 								"Maze size input error",
 								JOptionPane.ERROR_MESSAGE);
 
 						tabbedPane.setSelectedIndex(0);
 					}
 				}
				else {
					game = new Game(options);

					optionsFrame.setVisible(false);
					startInterface();
				}
 			}
 		});
 
 		optionsFrame.setResizable(false);
 		optionsFrame.pack();
 		optionsFrame.setVisible(true);
 
 	}
 
 	private void mainLoop() {
 
 		boolean goOn = true;
 		char input = ' ';
 
 		while(goOn){
 
 			input = mazePanel.getNextKey();
 			while( input == '\n'){
 				WaitTime.wait(50);
 				//GameOutput.printGame(game, mazePanel.getGraphics(), mazePictures);
 				mazePanel.repaint();
 				input = mazePanel.getNextKey();
 			}
 
 			goOn = game.heroTurn(input);
 
 			//GameOutput.printGame(game, mazePanel.getGraphics(), mazePictures);
 			mazePanel.repaint();
 			WaitTime.wait(125);
 
 			goOn = game.dragonTurn(goOn);
 			//GameOutput.printGame(game, mazePanel.getGraphics(), mazePictures);
 			mazePanel.repaint();
 			WaitTime.wait(125);
 
 			goOn = game.checkState(goOn);
 
 			/*GameOutput.clearScreen();
 			GameOutput.printEventQueue(game.getEvents() );*/
 			//GameOutput.printGame(game, mazePanel.getGraphics(), mazePictures);
 			mazePanel.repaint();
 			GameOutput.printEventQueue(game.getEvents(), infoPanel);
 			WaitTime.wait(125);
 
 		}
 	}
 }
