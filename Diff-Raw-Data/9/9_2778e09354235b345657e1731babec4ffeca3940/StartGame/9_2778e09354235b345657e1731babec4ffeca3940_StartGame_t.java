 package kth.csc.inda.ads;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Toolkit;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 public class StartGame {
 	private static View view;
 	private static ArrayList<NSnake> players;
 	private static ArrayList<JLabel> labels;
 	private static NField field;
 	private static JLabel info;
 	private static DefaultListModel model;
 	private static CardLayout cl;
 	private static JButton playButton;
 	private static final int height = 90;
 	private static final int width = 90;
 	private static final int snakeStartLength = 5;
 	private static int nrOfPlayers;
 	private static int numPowerUps; // set number of powerups
 	private static final int MAX_PLAYERS = 4;
 	private static final Color[] PLAYER_COLORS = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA};
 
 	public static void main(String[] args) throws InterruptedException {
 		
 		view = new View(height, width);
 		Container container = view.getContentPane();
 		JPanel p1 = new JPanel(new FlowLayout());
 		JPanel p2 = new JPanel(new FlowLayout());
 		p1.setBackground(Color.BLACK);
 		p2.setBackground(Color.BLACK);
 		container.add(p1, BorderLayout.NORTH);
 		container.add(p2, BorderLayout.SOUTH);
 		info = new JLabel("");
 		info.setForeground(Color.WHITE);
 		p1.add(info);
 		JLabel footer = new JLabel(" 2013 by JJM");
 		footer.setForeground(Color.WHITE);
 		p2.add(footer);
 
 		// create main menu
 		JPanel startView = new JPanel(new BorderLayout());
 
 		// add logo
 		ImageIcon logoPic = new ImageIcon("ads.png");
 		JLabel logo = new JLabel(logoPic);
 		startView.add(logo, BorderLayout.NORTH);
 
 		// add player panel
 		JPanel playerPanel = new JPanel(new BorderLayout());
 		playerPanel.setBorder(new TitledBorder("Players"));
 		model = new DefaultListModel();
 		PlayerBar newPlayer = new PlayerBar() {
 			public void act() {
 				addPlayer();
 				togglePlayButton();
 			}
 		};
 		newPlayer.add(new JLabel(new ImageIcon("plus.png")));
 		JLabel l = new JLabel("Click to add player");
 		l.setBorder( new EmptyBorder(10, 0, 10, 16));
 		newPlayer.add(l);
 		model.addElement(newPlayer);
 
 		// create a custom cell renderer to properly display the playerbars
 		final class LCR implements ListCellRenderer {
 
 			public Component getListCellRendererComponent(JList list,
 					Object value, int index, boolean isSelected,
 					boolean cellHasFocus) {
 
 				return (PlayerBar) value;
 			}
 		}
 		final JList playerList = new JList(model);
 		playerList.setCellRenderer(new LCR());
 		playerList.setBackground(playerPanel.getBackground());
 		playerPanel.add(playerList, BorderLayout.CENTER);
 		addPlayer();
 
 		// add playerpanel to startview
 		startView.add(playerPanel, BorderLayout.CENTER);
 
 		// set main to display startview
 		view.main.add(startView, "Start");
 		cl = (CardLayout) view.main.getLayout();
 		cl.show(view.main, "Start");
 		view.pack();
 		// view.setSize(logoPic.getIconWidth(), 300);
 
 		// add listeners to playerbars
 		playerList.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				PlayerBar pb = (PlayerBar) playerList.getSelectedValue();
 				pb.act();
 			}
 		});
 
 		// create buttons
 		JPanel buttons = new JPanel(new BorderLayout());
 		playButton = new JButton("Play");
 		JButton aboutButton = new JButton("About");
 		JButton quitButton = new JButton("Quit");
 		playButton.setPreferredSize(new Dimension(80, 40));
 		buttons.add(playButton, BorderLayout.NORTH);
 		buttons.add(Box.createRigidArea(new Dimension(0, 10)));
 		JPanel buttonsSouth = new JPanel(new GridLayout(0, 1));
 		buttonsSouth.add(aboutButton);
 		buttonsSouth.add(quitButton);
 		buttons.add(buttonsSouth, BorderLayout.SOUTH);
 
 		// place buttons inside a flow for addded space
 		JPanel flow = new JPanel();
 		flow.add(buttons);
 		startView.add(flow, BorderLayout.EAST);
 
 		// add listeners to buttons
 		quitButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		playButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				nrOfPlayers = model.getSize() - 1;
 				Runnable startGame = new Runnable() {
 					public void run() {
						newGame();
 					}
 				};
 				Thread t = new Thread(startGame);
 				t.start();
 			}
 		});
 
 		// eftersom spelaknappen inte funkar
 		// uncomment raderna nedan for att spela
 //		nrOfPlayers = 3;
 //		newGame();
 	}
 
 	private static void togglePlayButton() {
 		if (model.getSize()<2) {
 			playButton.setEnabled(false);
 		} else {
 			playButton.setEnabled(true);
 		}
 	}
 	
 	/**
 	 * Adds a new player to the list.
 	 * 
 	 * @param pl
 	 *            player list to be appended.
 	 */
 	private static void addPlayer() {
 		int playerID = model.getSize();
 		if (playerID > MAX_PLAYERS)
 			return;
 		JToolBar newPlayer = new PlayerBar() {
 			// what to do when bar is clicked
 			public void act() {
 				// default action is remove last player
 				model.removeElementAt(model.getSize()-2);
 				togglePlayButton();	
 			}
 		};
 		newPlayer.setLayout(new BorderLayout());
 		JLabel l = new JLabel("Player "+Integer.toString(playerID));
 		l.setForeground(PLAYER_COLORS[playerID-1]);
 		newPlayer.add(l, BorderLayout.WEST);
 		l = new JLabel("(Click to remove)");
 		l.setBorder( new EmptyBorder(16, 16, 16, 16));
 		l.setForeground(Color.GRAY);
 		newPlayer.add(l, BorderLayout.CENTER);
 		l = new JLabel("Left: S, Right: D");
 		newPlayer.add(l, BorderLayout.EAST);
 		int idx = model.getSize() - 1;
 		model.add(idx, newPlayer);
 	}
 
 	private static class PlayerBar extends JToolBar {
 		int id;
 
 		PlayerBar() {
 		};
 
 		public void act() {
 		};
 	}
 
 	/**
 	 * Resets everything that needs to be reset.
 	 */
 	private static void reset() {
 		field.clear();
 	}
 
 	/**
 	 * Starts a new game with a specified amount of players.
 	 * 
 	 * @param nrOfPlayers
 	 * 
 	 * @throws InterruptedException
 	 */
 	private final static void newGame() {
 		// funkar endast om nedan blir false - ngt fel!
 		System.out.println(javax.swing.SwingUtilities.isEventDispatchThread());
 		// add the fieldView and create field
 		field = new NField(height, width);
 		reset();
 		cl.show(view.main, "Field");
 		view.pack();
 		// Create all the players.
 		players = createPlayers(field, nrOfPlayers);
 		// Add a bomb in the middle of the field.
 		field.place(new Bomb(), new Location(width / 2, height / 2));
 		// Add the players to the view.
 		view.SetPlayers(players);
 
 		// Place a set number of food objects on the field
 		Random rand = new Random();
 		for (int i = 0; i < numPowerUps; i++) {
 			Location temp = new Location(rand.nextInt(width),
 					rand.nextInt(height));
 			if (field.getObjectAt(temp) == null) {
 				field.place(new PowUpFood(), temp);
 			} else {
 				i--;
 			}
 		}
 
 		// Add enough labels for the specified amount of players.
 		JPanel panel = new JPanel(new FlowLayout());
 		panel.setBackground(Color.BLACK);
 		labels = new ArrayList<JLabel>();
 		for (int i = 0; i < players.size(); i++) {
 			JLabel l = new JLabel("player: " + (i + 1));
 			l.setForeground(players.get(i).getColor());
 			panel.add(l);
 			labels.add(l);
 		}
 		Container container = view.getContentPane();
 		container.add(panel, BorderLayout.SOUTH);
 		view.pack(); // To make it look good.
 
 		// Start a 5 second countdown before the game starts.
 		for (int j = 5; j > 0; j--) {
 			info.setText("New game starts in: " + j);
 			gameLoop(); // Slowly draw the snake up.
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {}
 		}
 		info.setText("Go!");
 
 		for (;;) {
 			if (gameLoop()) {
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Call it to move the snakes once. Also handles all the logic.
 	 * 
 	 * @throws InterruptedException
 	 */
 	private static boolean gameLoop() {
 		for (int i = 0; i < players.size(); i++) {
 			NSnake p = players.get(i);
 			Location newLoc = p.getNextMove();
 			ActAndDraw actor = field.getObjectAt(newLoc);
 			if (actor != null) {// Check if the snake will be moved to a
 								// occupied location
 				if (actor instanceof Powerup) {
 					// Try to place a new power up on the field. If one can't be
 					// placed in 5 tries give up.
 					Random rand = new Random();
 					int n = 0;
 					boolean placed = false;
 					while (!placed && n < 5) {
 						Location temp = new Location(rand.nextInt(width),
 								rand.nextInt(height));
 						if (field.getObjectAt(temp) != null) {
 							n++;
 						} else {
 							field.place(new PowUpFood(), temp);
 							placed = true;
 						}
 					}
 				}
 				if (actor.act(p)) {
 					p.move(newLoc);
 				}
 			} else {
 				p.move(newLoc);
 			}
 			System.out.println(p);
 			labels.get(i).setText(p.toString());
 			if (!p.isAlive()) {
 				players.remove(i);
 				labels.remove(i);
 			}
 		}
 		// Draw everything.
 		view.showStatus(field);
 
 		try {
 			Thread.sleep(100);
 		} catch (InterruptedException e) {}
 		if (players.isEmpty()) {
 			// Single player, lost.
 			JOptionPane.showMessageDialog(null, "You lost!", ":(",
 					JOptionPane.INFORMATION_MESSAGE);
 			return true;
 		} else if (players.size() == 1 && nrOfPlayers != 1) {
 			// Do something as someone has won.
 			JOptionPane.showMessageDialog(view, players.get(0).getName()
 					+ " has won!", ":)", JOptionPane.INFORMATION_MESSAGE);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Creates the players to be used.
 	 * 
 	 * @param field
 	 *            The field the snakes will be placed on.
 	 * @param nrOfPlayers
 	 *            The number of players. (1-4).
 	 * @return The created players.
 	 */
 	private static ArrayList<NSnake> createPlayers(NField field, int nrOfPlayers) {
 		ArrayList<NSnake> players = new ArrayList<NSnake>();
 		switch (nrOfPlayers) {
 		case 4:
 			players.add(new NSnake("YELLOW", Color.YELLOW, field, new Location(
 					height - 10, 10), Direction.RIGHT, new Controls(
 					KeyEvent.VK_V, KeyEvent.VK_SPACE, KeyEvent.VK_C,
 					KeyEvent.VK_B), snakeStartLength));
 		case 3:
 			players.add(new NSnake("BLUE", Color.BLUE, field, new Location(10,
 					width - 10), Direction.LEFT, new Controls(KeyEvent.VK_I,
 					KeyEvent.VK_K, KeyEvent.VK_J, KeyEvent.VK_L),
 					snakeStartLength));
 		case 2:
 			players.add(new NSnake("RED", Color.RED, field, new Location(
 					height - 10, width - 10), Direction.LEFT,
 					new Controls(KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A,
 							KeyEvent.VK_D), snakeStartLength));
 		case 1:
 			players.add(new NSnake("MAGENTA", Color.MAGENTA, field,
 					new Location(10, 10), Direction.RIGHT, new Controls(
 							KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT,
 							KeyEvent.VK_RIGHT), snakeStartLength));
 			break;
 		default:
 			throw new IllegalArgumentException(nrOfPlayers
 					+ " is not in the intervall 1-4");
 		}
 		return players;
 	}
 }
