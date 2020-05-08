 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 
 import model.Wall;
 import model.command.Command;
 import model.object.Player;
 import controller.FPKeyListener;
 
 public class FirstPersonView extends Observable implements Observer {
 
 	private GridLayout gridLayout;
 	private BorderLayout borderLayout;
 	private MapView map;
 	private FirstPersonRoom currentRoom;
 	private JPanel gamePanel;
 	private JFrame mainFrame;
 	private Player player;
 	private boolean gameOver;

 	private MenuListener menuListener;
 
 	private JMenu pickMenu;
 	private JMenu attackMenu;
 	private JMenu dropMenu;
 	private JMenu eatMenu;
 	private JMenu helpMenu;
 
 	private static final String GAME_OVER = "GAME OVER";
	private static final String NEW_GAME = "NEW_GAME";
 
 	private static final String SOUTH = "south";
 	private static final String EAST = "east";
 	private static final String WEST = "west";
 	private static final String NORTH = "north";
 
 	public FirstPersonView(String name, FPKeyListener listener) {
 
 		final int SHORTCUT_MASK = Toolkit.getDefaultToolkit()
 				.getMenuShortcutKeyMask();
 
 		gamePanel = new JPanel();
 		mainFrame = new JFrame(name);
 
 		// Initialize the layout
 		gridLayout = new GridLayout(1, 2);
 		borderLayout = new BorderLayout();
 		gamePanel.setLayout(gridLayout);
 		mainFrame.getContentPane().setLayout(borderLayout);
 		mainFrame.add(gamePanel, BorderLayout.CENTER);
 
 		// Initialize the map
 		map = new MapView("Map");
 
 		// Setup the window
 		mainFrame.setSize(1200, 600);
 		mainFrame.setResizable(false);
 		// mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		// mainFrame.setBackground(Color.BLACK);
 
 		// Initialize the key listener
 		mainFrame.addKeyListener(listener);
 
 		currentRoom = new FirstPersonRoom(null);
 		player = new Player(null, 0, 0);
 		gameOver = false;
 
 		// Initialize the JMenu
 		menuListener = new MenuListener();
 		JMenuBar menuBar = new JMenuBar();
 		mainFrame.setJMenuBar(menuBar);
 
 		JMenu gameMenu = new JMenu("Game");
 		menuBar.add(gameMenu);
 
 		JMenuItem newGame = new JMenuItem("New");
 		newGame.addActionListener(menuListener);
 		newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
 				SHORTCUT_MASK));
 		newGame.setToolTipText("new");
 		gameMenu.add(newGame);
 
 		JMenuItem loadGame = new JMenuItem("Load");
 		loadGame.addActionListener(menuListener);
 		loadGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
 				SHORTCUT_MASK));
 		loadGame.setToolTipText("load");
 		gameMenu.add(loadGame);
 
 		JMenuItem saveGame = new JMenuItem("Save");
 		saveGame.addActionListener(menuListener);
 		saveGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 				SHORTCUT_MASK));
 		saveGame.setToolTipText("save");
 		gameMenu.add(saveGame);
 
 		JMenuItem quitGame = new JMenuItem("Quit");
 		quitGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
 				SHORTCUT_MASK));
 		quitGame.setToolTipText("quit");
 		quitGame.addActionListener(menuListener);
 		gameMenu.add(quitGame);
 
 		JMenu mnGo = new JMenu("Go");
 		menuBar.add(mnGo);
 
 		JMenuItem goNorth = new JMenuItem("North");
 		goNorth.setToolTipText("go,north");
 		goNorth.addActionListener(menuListener);
 		mnGo.add(goNorth);
 
 		JMenuItem goSouth = new JMenuItem("South");
 		goSouth.setToolTipText("go,south");
 		goSouth.addActionListener(menuListener);
 		mnGo.add(goSouth);
 
 		JMenuItem goEast = new JMenuItem("East");
 		goEast.setToolTipText("go,east");
 		goEast.addActionListener(menuListener);
 		mnGo.add(goEast);
 
 		JMenuItem goWest = new JMenuItem("West");
 		goWest.setToolTipText("go,west");
 		goWest.addActionListener(menuListener);
 		mnGo.add(goWest);
 
 		JMenu turnMenu = new JMenu("Turn");
 		menuBar.add(turnMenu);
 
 		JMenuItem turnLeft = new JMenuItem("Left");
 		turnLeft.setToolTipText("turn,left");
 		turnLeft.addActionListener(menuListener);
 		turnMenu.add(turnLeft);
 
 		JMenuItem turnRight = new JMenuItem("Right");
 		turnRight.setToolTipText("turn,right");
 		turnRight.addActionListener(menuListener);
 		turnMenu.add(turnRight);
 
 		JMenu editMenu = new JMenu("Edit");
 		menuBar.add(editMenu);
 
 		JMenuItem undoMenu = new JMenuItem("Undo");
 		undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
 				SHORTCUT_MASK));
 		undoMenu.setToolTipText("undo");
 		undoMenu.addActionListener(menuListener);
 		editMenu.add(undoMenu);
 
 		JMenuItem redoMenu = new JMenuItem("Redo");
 		redoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
 				SHORTCUT_MASK));
 		redoMenu.setToolTipText("redo");
 		redoMenu.addActionListener(menuListener);
 		editMenu.add(redoMenu);
 
 		pickMenu = new JMenu("Pick Up");
 		menuBar.add(pickMenu);
 
 		dropMenu = new JMenu("Drop");
 		menuBar.add(dropMenu);
 
 		attackMenu = new JMenu("Attack");
 		menuBar.add(attackMenu);
 
 		eatMenu = new JMenu("Eat");
 		menuBar.add(eatMenu);
 
 		JMenuItem eat = new JMenuItem("Eat");
 		eat.setToolTipText("eat");
 		eat.addActionListener(menuListener);
 		eatMenu.add(eat);
 
 		helpMenu = new JMenu("Help");
 		menuBar.add(helpMenu);
 
 		JMenuItem control = new JMenuItem("Instruction");
 		control.setToolTipText("instruction");
 		control.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(null, instructionString(),
 						"alert", JOptionPane.INFORMATION_MESSAGE, null);
 			}
 
 			private Object instructionString() {
 				return "~~~~~~~~~~~~~~~Instructions~~~~~~~~~~~~~~~ \n 1) How to kill a monster :\n"
 						+ "when you see a monster click on the moster to kill it\n "
 						+ "you might need multiple clicks to kill it\n\n"
 						+ "2) Controls :\n"
 						+ "	Up arrow ~~ go through door\n"
 						+ "	left/right arrow ~~ turn players direction\n\n"
 						+ "3) Command : \n "
 						+ "player can choose desired actions from the menu bar\n"
 						+ "\n\n"
 						+ "4) Player can eat a plant to restore health only "
 						+ "if the player has a plant in his bag pack\n\n"
 						+ "5) Player does 2x damagae when he/she has a sword in the bag pack\n\n"
 						+ "6) Player can go through door without killing mosters if there " +
 						"is a pogoStick in the bag pack\n\n" +
 						"7) GAME OBJECTIVE : \n" +
 						"FIND AND KILL ALL MONSTERS BEFORE THEY KILL YOU ! \n\n";
 			}
 		});
 
 		JMenuItem about = new JMenuItem("About");
 		about.setToolTipText("instruction");
 		about.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(null, instructionString(),
 						"alert", JOptionPane.INFORMATION_MESSAGE, null);
 			}
 
 			private Object instructionString() {
 				return "*************************************************\n"+
 						"                 World of Zuul                 \n"+
 						"                     V 1.0                     \n"+
 						"                                               \n"+
 						"                  Developed By:                \n"+
 						"                   Tanzeel Rana                \n"+
 						"               Muhammad Mohd Johari            \n"+
 						"                    Sean Byron                 \n"+
 						"                  Ethan Aubuchon               \n"+
 						"                                               \n"+
 						"             Created October 23, 2012          \n"+
 						"*************************************************\n";
 			}
 		});
 
 
 		helpMenu.add(control);
 		helpMenu.add(about);
 	}
 
 	public void update(Observable arg0, Object arg1) {
 		if (arg1 instanceof Player) {
 			player = (Player) arg1;
 			currentRoom = (FirstPersonRoom) player.getCurrentPlayerRoom();
 			map.update(arg0, arg1);
 			refreshView();
 		}
 
 		if (arg1 instanceof String) {
 			String command = (String) arg1;
 			if (command.equals(GAME_OVER)) {
 				gameOver = true;
 				refreshView();
				JOptionPane.showMessageDialog(mainFrame, "Thanks for playing!", "Goodbye", JOptionPane.INFORMATION_MESSAGE);
				mainFrame.setVisible(false);
			} else if (command.equals(NEW_GAME)) {
				gameOver = false;
				mainFrame.setVisible(true);
				refreshView();
 			} else {
 				JOptionPane.showMessageDialog(mainFrame, command, "Message",
 						JOptionPane.INFORMATION_MESSAGE);
 			}
 		}
 	}
 
 	private void refreshView() {
 		// Remove everything from the gamePanel and mainFrame
 		gamePanel.removeAll();
 		mainFrame.getContentPane().removeAll();
 
 		// Remove everything from the glass pane
 		JPanel glassPane = (JPanel) mainFrame.getGlassPane();
 		glassPane.removeAll();
 		glassPane.setLayout(null);
 
 		if (!gameOver) {
 			// Add the 3D perspective and the map perspective
 			gamePanel.add(currentRoom.getView(player.getLookingDirection()));
 			gamePanel.add(map.getContentPane());
 
 			JLabel arrow = new JLabel("");
 			if (player.getLookingDirection().equals(NORTH)) {
 				arrow.setIcon(new ImageIcon(FirstPersonRoom.class
 						.getResource("/img/firstperson/arrow/north.png")));
 			} else if (player.getLookingDirection().equals(SOUTH)) {
 				arrow.setIcon(new ImageIcon(FirstPersonRoom.class
 						.getResource("/img/firstperson/arrow/south.png")));
 			} else if (player.getLookingDirection().equals(EAST)) {
 				arrow.setIcon(new ImageIcon(FirstPersonRoom.class
 						.getResource("/img/firstperson/arrow/east.png")));
 			} else if (player.getLookingDirection().equals(WEST)) {
 				arrow.setIcon(new ImageIcon(FirstPersonRoom.class
 						.getResource("/img/firstperson/arrow/west.png")));
 			}
 
 			arrow.setBounds(875, 290, 40, 40);
 			glassPane.add(arrow);
 			glassPane.setVisible(true);
 
 			// Add the health panel
 			HealthPanel healthPanel = new HealthPanel(player.getHealth(),
 					player.getCurrentWeight(), player.getMaxWeight());
 			healthPanel.setVisible(true);
 			mainFrame.add(healthPanel, BorderLayout.SOUTH);
 
 			// Refresh the Pick Up and Attack menu's
 			Wall wall = currentRoom.getWall(player.getLookingDirection());
 
 			pickMenu.removeAll();
 			attackMenu.removeAll();
 			dropMenu.removeAll();
 
 			if (wall.getItem() != null) {
 				String itemName = wall.getItem().getItemName();
 				JMenuItem temp = new JMenuItem(itemName);
 				temp.setToolTipText("pick," + itemName);
 				temp.addActionListener(menuListener);
 				pickMenu.add(temp);
 			}
 
 			if (wall.getMonster() != null && wall.getMonster().isAlive()) {
 				String monsterName = wall.getMonster().getName();
 				JMenuItem temp = new JMenuItem(monsterName);
 				temp.setToolTipText("attack," + monsterName);
 				temp.addActionListener(menuListener);
 				attackMenu.add(temp);
 			}
 
 			ArrayList<String> items = player.getItemsInPosession();
 			for (int i = 0; i < items.size(); i++) {
 				JMenuItem temp = new JMenuItem(items.get(i));
 				temp.setToolTipText("drop," + items.get(i));
 				temp.addActionListener(menuListener);
 				dropMenu.add(temp);
 			}
 
 			// Repaint the gamePanel
 			gamePanel.validate();
 			gamePanel.repaint();
 
 			// Add the gamePanel to mainFrame
 			mainFrame.add(gamePanel, BorderLayout.CENTER);
 		} else {
 			JLabel temp = new JLabel("GAME OVER");
 			temp.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
 			temp.setHorizontalAlignment(SwingConstants.CENTER);
 			temp.setForeground(Color.RED);
 			mainFrame.add(temp, BorderLayout.CENTER);
 		}
 
 		// Repaint the mainFrame
 		mainFrame.validate();
 		mainFrame.repaint();
 
 	}
 
 	public void show() {
 		mainFrame.setVisible(true);
 	}
 
 	private final class MenuListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			if (arg0.getSource() instanceof JMenuItem && !gameOver) {
 				JMenuItem source = (JMenuItem) arg0.getSource();
 
 				if (source.getToolTipText() != null) {
 					String commands[] = source.getToolTipText().split(",");
 					if (commands.length == 2) {
 						setChanged();
 						notifyObservers(new Command(commands[0], commands[1]));
 					} else if (commands.length == 1) {
 
 						if (commands[0].equals("save")) {
 							String path = JOptionPane
 									.showInputDialog(mainFrame,
 											"Please enter the path where you'd like the game saved:");
 							if (path != null) {
 								setChanged();
 								notifyObservers(new Command(commands[0], path));
 							}
 							return;
 						} else if (commands[0].equals("load")) {
 							String path = JOptionPane
 									.showInputDialog(mainFrame,
 											"Please enter the path to the game you want to load:");
 							if (path != null) {
 								setChanged();
 								notifyObservers(new Command(commands[0], path));
 							}
 							return;
 						} else {
 							setChanged();
 							notifyObservers(new Command(commands[0], null));
 						}
 					}
 				}
 			}
 		}
 	}
 
 }
