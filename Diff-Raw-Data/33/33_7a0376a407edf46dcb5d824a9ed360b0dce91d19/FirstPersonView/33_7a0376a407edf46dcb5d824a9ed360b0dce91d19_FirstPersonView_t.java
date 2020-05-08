 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
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
 	
 	private static final String GAME_OVER = "GAME OVER";
 	
 	private static final String SOUTH = "south";
 	private static final String EAST = "east";
 	private static final String WEST = "west";
 	private static final String NORTH = "north";
 	
 	public FirstPersonView(String name, FPKeyListener listener) {
 		gamePanel = new JPanel();
 		mainFrame = new JFrame(name);
 		
 		//Initialize the layout
 		gridLayout = new GridLayout(1,2);
 		borderLayout = new BorderLayout();
 		gamePanel.setLayout(gridLayout);
 		mainFrame.getContentPane().setLayout(borderLayout);
 		mainFrame.add(gamePanel, BorderLayout.CENTER);
 		
 		//Initialize the map
 		map = new MapView("Map");
 		
 		//Setup the window
 		mainFrame.setSize(1200,600);
 		mainFrame.setResizable(false);
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainFrame.setBackground(Color.BLACK);
 		
 		//Initialize the key listener
 		mainFrame.addKeyListener(listener);
 		
 		currentRoom = new FirstPersonRoom(null);
 		player = new Player(null, null, 0, 0);
 		gameOver = false;
 		
 		//Initialize the JMenu
 		menuListener = new MenuListener();
 		JMenuBar menuBar = new JMenuBar();
 		mainFrame.setJMenuBar(menuBar);
 		
 		JMenu gameMenu = new JMenu("Game");
 		menuBar.add(gameMenu);
 		
 //		JMenuItem newGame = new JMenuItem("New");
 //		gameMenu.add(newGame);
 //		
 //		JMenuItem loadGame = new JMenuItem("Load");
 //		gameMenu.add(loadGame);
 //		
 //		JMenuItem saveGame = new JMenuItem("Save");
 //		gameMenu.add(saveGame);
 		
 		JMenuItem quitGame = new JMenuItem("Quit");
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
 		undoMenu.setToolTipText("undo");
 		undoMenu.addActionListener(menuListener);
 		editMenu.add(undoMenu);
 		
 		JMenuItem redoMenu = new JMenuItem("Redo");
 		redoMenu.setToolTipText("redo");
 		redoMenu.addActionListener(menuListener);
 		editMenu.add(redoMenu);
 		
 		pickMenu = new JMenu("Pick Up");
 		menuBar.add(pickMenu);
 		
		dropMenu = new JMenu("Drop");
		menuBar.add(dropMenu);
		
 		attackMenu = new JMenu("Attack");
		menuBar.add(attackMenu);	
 	}
 
 	public void update(Observable arg0, Object arg1) {
 		if (arg1 instanceof Player) {
 			player = (Player) arg1;
 			currentRoom = (FirstPersonRoom) player.getCurrentPlayerRoom();
 			map.update(arg0, arg1);
 			refreshView();
 		}
 		
 		if (arg1 instanceof String) {
 			String command = (String)arg1;
 			if (command.equals(GAME_OVER)) {
 				gameOver = true;
 				refreshView();
 			}
 		}
 	}
 	
 	private void refreshView() {
 		//Remove everything from the gamePanel and mainFrame
 		gamePanel.removeAll();
 		mainFrame.getContentPane().removeAll();
 
 		//Remove everything from the glass pane
 		JPanel glassPane = (JPanel) mainFrame.getGlassPane();
 		glassPane.removeAll();
 		glassPane.setLayout(null);
 		
 		if (!gameOver) {
 			//Add the 3D perspective and the map perspective
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
 			
 			//Add the health panel
 			HealthPanel healthPanel = new HealthPanel(player.getHealth(), player.getCurrentWeight(), player.getMaxWeight());
 			healthPanel.setVisible(true);
 			mainFrame.add(healthPanel, BorderLayout.SOUTH);
 			
 			//Refresh the Pick Up and Attack menu's
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
			for (int i=0; i<items.size(); i++) {
				JMenuItem temp = new JMenuItem(items.get(i));
				temp.setToolTipText("drop," + items.get(i));
				temp.addActionListener(menuListener);
				dropMenu.add(temp);
			}
			
 			//Repaint the gamePanel
 			gamePanel.validate();
 			gamePanel.repaint();
 			
 			//Add the gamePanel to mainFrame
 			mainFrame.add(gamePanel, BorderLayout.CENTER);
 		} else {
 			JLabel temp = new JLabel("GAME OVER");
 			temp.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
 			temp.setHorizontalAlignment(SwingConstants.CENTER);
 			temp.setForeground(Color.RED);
 			mainFrame.add(temp, BorderLayout.CENTER);
 		}
 		
 		//Repaint the mainFrame
 		mainFrame.validate();
 		mainFrame.repaint();
 		
 	}
 	
 	public void show() {
 		mainFrame.setVisible(true);
 		JOptionPane.showMessageDialog(mainFrame, "Welcome to Zuul! An incredibly boring adventure game. Use the left and right arrow keys to\n" +
 				"look around the room. Click on a door to go through it, click on items to pick them up, and click on Monsters to\n" +
 				"attack them. Enjoy!","Welcome", 0);
 	}
 	
 	private final class MenuListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			if (arg0.getSource() instanceof JMenuItem) {
 				JMenuItem source = (JMenuItem) arg0.getSource();
 				
 				if (source.getToolTipText() != null) {
 					String commands[] = source.getToolTipText().split(",");
 					if (commands.length == 2) {
 						setChanged();
 						notifyObservers(new Command(commands[0], commands[1]));
 					} else if (commands.length == 1) {
 						setChanged();
 						notifyObservers(new Command(commands[0], null));
 					}
 				}
 			}
 		}
 	}
 
 
 }
