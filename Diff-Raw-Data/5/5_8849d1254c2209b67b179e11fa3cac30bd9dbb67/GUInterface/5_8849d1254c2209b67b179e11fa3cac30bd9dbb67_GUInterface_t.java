 package game.ui.gui;
 
 import game.logic.Game;
 import game.objects.Dragon;
 import game.ui.GameInterface;
 import game.ui.GameOptions;
 import game.ui.GameOutput;
 import game.ui.MazePictures;
 import game.ui.utilities.MazeInput;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 
 public class GUInterface extends GameInterface implements KeyListener {
 
 	public static final int SCROLLBAR_PIXELS = 3;
 
 	public static final int SPRITESIZE = 32;
 
 	public static Dimension MAXIMUM_WINDOW_SIZE = new Dimension(500,500);
 
 	private final MazePictures mazePictures = new MazePictures();
 	private JFrame frame;
 	private MazePanel mazePanel;
 	private InfoPanel infoPanel;
 	private JTextField rowsTextField;
 	private JTextField columnsTextField;
 
 	private File loadedFile;
 	private boolean useLoadedFile = false;
 
 	private boolean usePredefinedMaze = true;
 	private boolean useMultipleDragons = true;
 	private int dragonType = Dragon.SLEEPING;
 	private int maze_rows;
 	private int maze_columns;
 	private boolean goOn = true;
 
 	private GameOptions options = new GameOptions(false);
 
 	private JMenuBar menuBar;
 
 	private void startInterface() {
 		frame = new JFrame("Maze");
 
 		Dimension mazePanelDimension = new Dimension(game.getMaze().getColumns() * GUInterface.SPRITESIZE,
 				game.getMaze().getRows() * GUInterface.SPRITESIZE);
 
 		Dimension infoPanelDimension = new Dimension(game.getMaze().getColumns() * GUInterface.SPRITESIZE,
 				100);
 
 
 		Container c = frame.getContentPane();
 		c.setLayout(new GridBagLayout());
 		c.addKeyListener(this);
 		c.setFocusable(true);
 
 		mazePanel = new MazePanel(game, mazePictures, mazePanelDimension, MAXIMUM_WINDOW_SIZE);
 
 		infoPanel = new InfoPanel(infoPanelDimension, MAXIMUM_WINDOW_SIZE);
 
 		createMenuBar();
 
 		GridBagConstraints infoPanel_constraints = new GridBagConstraints();
 
 		infoPanel_constraints.weightx = 1;
		infoPanel_constraints.weighty = 0;
 		infoPanel_constraints.gridx = 0;
 		infoPanel_constraints.gridy = 0;
		infoPanel_constraints.fill = GridBagConstraints.HORIZONTAL;
 
 		c.add(infoPanel, infoPanel_constraints);
 
 		GridBagConstraints mazePanel_constraints = new GridBagConstraints();
 
 		mazePanel_constraints.weightx = 1;
 		mazePanel_constraints.weighty = 1;
 		mazePanel_constraints.gridx = 0;
 		mazePanel_constraints.gridy = 1;
 		mazePanel_constraints.fill = GridBagConstraints.BOTH;
 
 		c.add(mazePanel, mazePanel_constraints);
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(true);
 		frame.pack();
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 
 
 	private void createMenuBar() {
 		menuBar =  new JMenuBar();
 
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic(KeyEvent.VK_F);
 		fileMenu.getAccessibleContext().setAccessibleDescription(
 				"File menu");
 		menuBar.add(fileMenu);
 
 		JMenuItem saveGameMenuItem = new JMenuItem("Save Game", KeyEvent.VK_S);
 		saveGameMenuItem.getAccessibleContext().setAccessibleDescription(
 				"Saves the current game to a file");
 		fileMenu.add(saveGameMenuItem);
 
 		JMenuItem loadGameMenuItem = new JMenuItem("Load Game", KeyEvent.VK_L);
 		loadGameMenuItem.getAccessibleContext().setAccessibleDescription(
 				"Loads a game saved state");
 		fileMenu.add(loadGameMenuItem);
 
 		loadGameMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser fileChooser = new JFileChooser();
 
 				FileNameExtensionFilter filter = new FileNameExtensionFilter(GameOutput.SAVE_EXTENSION_DESCRIPTION, GameOutput.SAVE_EXTENSION_TYPE);
 				fileChooser.setFileFilter(filter);
 
 				boolean fileNotChosenOrCancelled = true;
 
 				while(fileNotChosenOrCancelled) {
 					if (fileChooser.showOpenDialog(fileChooser) == JFileChooser.APPROVE_OPTION) {
 
 						File file = fileChooser.getSelectedFile();
 
 						String filePath = file.getAbsolutePath();
 
 						if(filePath.endsWith(GameOutput.SAVE_EXTENSION) && file.exists()) {
 							loadedFile = fileChooser.getSelectedFile();
 							// load from file
 							game = GameOutput.load(loadedFile);
 							useLoadedFile = true;
 							frame.dispose();
 							startInterface();
 							fileNotChosenOrCancelled = false;
 						}
 						else
 							JOptionPane.showMessageDialog(frame,
 									"Invalid file chosen!",
 									"Invalid file",
 									JOptionPane.ERROR_MESSAGE);
 
 					}
 					else
 						fileNotChosenOrCancelled = false;
 				}
 			}
 		});
 
 		saveGameMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				GameOutput.showSaveGameDialog(game);
 			}
 		});
 
 		JMenu gameMenu = new JMenu("Game");
 		gameMenu.setMnemonic(KeyEvent.VK_G);
 		gameMenu.getAccessibleContext().setAccessibleDescription(
 				"Game options");
 		menuBar.add(gameMenu);
 
 		JMenuItem optionsGameMenuItem = new JMenuItem("Game options",
 				KeyEvent.VK_O);
 		optionsGameMenuItem.getAccessibleContext().setAccessibleDescription(
 				"Change options for next game");
 		gameMenu.add(optionsGameMenuItem);
 
 		JMenuItem changeKeysItem = new JMenuItem("Change keys", KeyEvent.VK_C);
 		changeKeysItem.getAccessibleContext().setAccessibleDescription(
 				"Choose which keys you use to play");
 		gameMenu.add(changeKeysItem);
 
 		changeKeysItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				new KeysPanel(frame, "Key Mappings");
 			}
 		});
 
 		optionsGameMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				int option = JOptionPane.showConfirmDialog(
 						frame,
 						"Change the game settings? (Next game will use these definitions)",
 						"Change settings",
 						JOptionPane.YES_NO_OPTION);
 				if(option == JOptionPane.YES_OPTION) {
 
 					int predefMazeOption = JOptionPane.showConfirmDialog(
 							frame,
 							"Create a user defined maze?",
 							"User defined maze",
 							JOptionPane.YES_NO_OPTION);
 
 					if(predefMazeOption == JOptionPane.YES_OPTION) {
 						usePredefinedMaze = false;
 						String rows, columns;
 
 						do {
 							rows = JOptionPane.showInputDialog(frame, "Number of rows? (Min. 6, Max. 500 but clipping will occur!)");
 						}
 						while(!MazeInput.isInteger(rows) && rows != null);
 
 						if(rows == null)
 							return;
 
 						do {
 							columns = JOptionPane.showInputDialog(frame, "Number of columns? (Min. 6, Max. 500 but clipping will occur!)");
 						}
 						while(!MazeInput.isInteger(columns) && columns != null);
 
 						if(columns == null)
 							return;
 
 						if(Integer.parseInt(rows)  < 6 || Integer.parseInt(columns) < 6
 								|| Integer.parseInt(columns) > 500 || Integer.parseInt(columns) > 500) {
 							JOptionPane.showMessageDialog(frame,
 									"Invalid row and/or column number detected, keeping old maze settings!",
 									"Invalid input error",
 									JOptionPane.ERROR_MESSAGE);
 						}
 						else {
 							maze_rows = Integer.parseInt(rows);
 							maze_columns = Integer.parseInt(columns);
 						}
 					}
 					else
 						usePredefinedMaze = true;
 
 					String[] possibilities = {"Randomly sleeping", "Always awake", "Static"};
 					String dragonOption = (String)JOptionPane.showInputDialog(
 							frame,
 							"Dragon type:",
 							"Dragon type",
 							JOptionPane.QUESTION_MESSAGE,
 							null,
 							possibilities,
 							possibilities[0]);
 
 					if(dragonOption == null)
 						return;
 					if(dragonOption.equals( "Randomly sleeping" ))
 						dragonType = Dragon.SLEEPING;
 					else if(dragonOption.equals( "Always awake" ))
 						dragonType = Dragon.NORMAL;
 					else
 						dragonType = Dragon.STATIC;
 
 					int multipleDragonsOption = JOptionPane.showConfirmDialog(
 							frame,
 							"Create a number of dragons proportional to the maze size?",
 							"Multiple dragons",
 							JOptionPane.YES_NO_OPTION);
 
 					if(multipleDragonsOption == JOptionPane.YES_OPTION)
 						useMultipleDragons = true;
 					else
 						useMultipleDragons = false;
 
 					useLoadedFile = false;
 					updateOptions();
 
 					JOptionPane.showMessageDialog(frame,
 							"The new game settings are now configured, restart the game to apply changes!",
 							"Settings changed",
 							JOptionPane.PLAIN_MESSAGE);
 				}
 			}
 		});
 
 		JMenuItem mazeEditorGameMenuItem = new JMenuItem("Maze Editor",
 				KeyEvent.VK_M);
 		mazeEditorGameMenuItem.getAccessibleContext().setAccessibleDescription(
 				"Opens the maze editor dialog");
 		gameMenu.add(mazeEditorGameMenuItem);
 
 		mazeEditorGameMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				new MazeEditorPanel(frame, game, mazePictures);
 			}
 		});
 
 		JMenuItem restartGameMenuItem = new JMenuItem("Restart game",
 				KeyEvent.VK_R);
 		restartGameMenuItem.getAccessibleContext().setAccessibleDescription(
 				"Restarts the game");
 		gameMenu.add(restartGameMenuItem);
 
 		restartGameMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				int option = JOptionPane.showConfirmDialog(
 						frame,
 						"Do you really want to restart the game?",
 						"Confirm exit",
 						JOptionPane.YES_NO_OPTION);
 				if(option == JOptionPane.YES_OPTION) {
 					frame.setVisible(false);
 					restartGame();
 					return;
 				}
 			}
 		});
 
 		JMenuItem exitGameMenuItem = new JMenuItem("Exit game",
 				KeyEvent.VK_E);
 		exitGameMenuItem.getAccessibleContext().setAccessibleDescription(
 				"Exits the game");
 		gameMenu.add(exitGameMenuItem);
 
 		exitGameMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				int option = JOptionPane.showConfirmDialog(
 						frame,
 						"Do you really want to exit the game?",
 						"Confirm exit",
 						JOptionPane.YES_NO_OPTION);
 				if(option == JOptionPane.YES_OPTION)
 					System.exit(0);
 			}
 		});
 
 
 		JMenu helpMenu = new JMenu("Help");
 		helpMenu.setMnemonic(KeyEvent.VK_H);
 		helpMenu.getAccessibleContext().setAccessibleDescription(
 				"Help menu");
 		menuBar.add(helpMenu);
 
 		JMenuItem keysHelpMenuItem = new JMenuItem("Default keys", KeyEvent.VK_K);
 		keysHelpMenuItem.getAccessibleContext().setAccessibleDescription(
 				"Explains default keys");
 		helpMenu.add(keysHelpMenuItem);
 
 		keysHelpMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(frame,
 						"Move the hero using WASD for the usual purposes\n\n" +
 								"Skip a move by pressing ENTER\n\n" +
 								"Command the eagle to fetch your sword using E\n",
 								"Default keys",
 								JOptionPane.PLAIN_MESSAGE);
 			}
 		});
 
 		JMenuItem infoHelpMenuItem = new JMenuItem("How to play", KeyEvent.VK_I);
 		infoHelpMenuItem.getAccessibleContext().setAccessibleDescription(
 				"Explains how to play the game");
 		helpMenu.add(infoHelpMenuItem);
 
 		infoHelpMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(frame,
 						"You have to escape this confusing dungeon!\n\n" +
 								"\nThe exit is closed until all the dragons have been slayed.\n" +
 								"If you touch a dragon unarmed, you will die, so be careful while\n" +
 								"you don't have the sword on your hands!\n" +
 								"\nSleeping dragons are harmless, so use that for your advantage.\n" +
 								"Fortunately for you, these dragons aren't very smart, so they will roam\n" +
 								"around the maze aimlessly.\n" +
 								"\nOnce you're armed, you can slay all the dragons you encounter.\n" +
 								"\nUse your loyal companion to get you the sword! Launch your eagle\n" +
 								"and it will retrieve your sword for you! But be careful, dragons can\n" +
 								"kill your eagle while it's picking up the sword or waiting for you!\n" +
 								"\n\nGood luck on your journey, mighty hero!\n\n",
 								"How to play",
 								JOptionPane.PLAIN_MESSAGE);
 			}
 		});
 
 		frame.setJMenuBar(menuBar);
 	}
 
 
 	public void startGame() {
 		startOptions();
 	}
 
 	public void startOptions() {
 
 		final JFrame optionsFrame = new JFrame("Maze: New Game");
 		optionsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
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
 				dragonType = Dragon.SLEEPING;
 			}
 		});
 
 		JRadioButton staticButton = new JRadioButton("Static");
 		staticButton.setBounds(10, 84, 234, 23);
 		dragonTypeButtons.add(staticButton);
 		dragonOptionsPanel.add(staticButton);
 
 		staticButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				dragonType = Dragon.STATIC;
 			}
 		});
 
 		JRadioButton alwaysAwakeButton = new JRadioButton("Always awake");
 		alwaysAwakeButton.setBounds(10, 58, 234, 23);
 		dragonTypeButtons.add(alwaysAwakeButton);
 		dragonOptionsPanel.add(alwaysAwakeButton);
 
 		alwaysAwakeButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				dragonType = Dragon.NORMAL;
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
 
 		noMultipleDragonsButton.addActionListener(new ActionListener(){
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
 						maze_rows = options.rows = Integer.parseInt(rowsTextField.getText()); 
 						maze_columns = options.columns = Integer.parseInt(columnsTextField.getText());
 
 						if(options.rows < 6 || options.columns < 6) {
 							JOptionPane.showMessageDialog(optionsFrame,
 									"Please specify a row number and a column number equal or bigger than 5!",
 									"Maze size error",
 									JOptionPane.ERROR_MESSAGE);
 
 							tabbedPane.setSelectedIndex(0);
 						}
 						else if(options.rows > 500 || options.columns > 500) {
 							JOptionPane.showMessageDialog(optionsFrame,
 									"Please specify a row number and a column number no bigger than 500!",
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
 		optionsFrame.setLocationRelativeTo(null);
 		optionsFrame.setVisible(true);
 
 	}
 
 	private void restartGame() {
 		if(useLoadedFile) {
 			goOn = true;
 			game = GameOutput.load(loadedFile);
 		}
 		else {
 			goOn = true;
 			game = new Game(options);
 		}
 		startInterface();
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		for (GameKey gameKey : GameKeys.keyList) {
 			if (e.getKeyCode() == gameKey.getKey())
 				updateGame(gameKey.getChar());
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		return;
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		return;
 	}
 
 	private void updateGame(char input) {
 
 		if(goOn){ 
 			goOn = game.heroTurn(input);
 
 			mazePanel.repaint();
 
 			goOn = game.dragonTurn(goOn);
 			mazePanel.repaint();
 
 			goOn = game.checkState(goOn);
 
 			mazePanel.repaint();
 			GameOutput.printEventQueue(game.getEvents(), infoPanel);
 		}
 
 
 		if(!goOn) {
 			int option = JOptionPane.showConfirmDialog(
 					frame,
 					"Game is over! Would you like to start a new game?",
 					"Game over",
 					JOptionPane.YES_NO_OPTION);
 			if(option == JOptionPane.YES_OPTION) {
 				frame.setVisible(false);
 				restartGame();
 				return;
 			}
 			else
 				System.exit(0);
 		}
 	}
 
 
 	private void updateOptions() {
 		if(options.randomMaze = !usePredefinedMaze) {
 			options.rows = maze_rows;
 			options.columns = maze_columns;
 		}
 
 		options.dragonType = dragonType;
 
 		options.multipleDragons = useMultipleDragons;
 
 		options.randomSpawns = true;
 	}
 
 	public static Dimension getFormattedPreferredDimension(Dimension oldPreferred, Dimension maximumDimension) {
 		Dimension formattedDimension = new Dimension(oldPreferred);
 
 		if(oldPreferred.height > maximumDimension.height)
 			formattedDimension.height = maximumDimension.height;
 
 		if(oldPreferred.width > maximumDimension.width)
 			formattedDimension.width = maximumDimension.width;
 
 		return formattedDimension;
 	}
 
 }
