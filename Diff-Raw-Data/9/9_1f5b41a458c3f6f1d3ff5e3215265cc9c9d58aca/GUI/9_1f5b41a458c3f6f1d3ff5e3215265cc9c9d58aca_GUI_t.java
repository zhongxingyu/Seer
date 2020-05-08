 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 /**
  * A graphical user interface for Bammi. The main window
  * that the user interacts with during use.
  * 
  * @authors Martin Mtzell, Sean Wenstrm
  * @version 2013-05-13
  */
 public class GUI extends JFrame{
 	
 	private static final int PIE_SIZE = 30; //pieSize to send to gameField
 	private static final int DFLT_SIZE = 10; //default size of map
 	
 	private JPanel panel;
 	private GameField gameField;
 	
 	private ArrayList<Player> players;
 	private Color[] colors = new Color[] {null, null, Color.GREEN, Color.YELLOW, Color.BLACK};
 	
 	private int gamefieldSize;
 	
 	
 	/**
 	 * Constructs a new GUI.
 	 */
 	public GUI() {
 		super("Bammi");
 		
 		int size = DFLT_SIZE; //
 		players = new ArrayList<Player>();
 		players.add(new User(Color.BLUE));
 		players.add(new User(Color.RED));
 		Game.setPlayers(players);
 		
 		
 		makeMenuBar();
 		panel = new JPanel();
 		gameField = new GameField(Init.generate(size, this), size, PIE_SIZE);
 		panel.add(gameField);
 		panel.add(new Label("test"));
 		getContentPane().add(panel);
 		pack();
 		setVisible(true);
 	}
 	
 	/**
 	 * Creates a menu bar for the window with the menus
 	 * "Game" and "Help".
 	 */
 	private void makeMenuBar(){
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		//The game menu
 		JMenu gameMenu = new JMenu("Game");
 		menuBar.add(gameMenu);
 		
 		JMenuItem newGame = new JMenuItem("New game");
 		newGame.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				newGame();
 			}
 		});
 		gameMenu.add(newGame);
 		
 		//Settings submenu
 		
 		JMenu settings = makeSettingsMenu();
 		gameMenu.add(settings);
 		
 		JMenuItem quit = new JMenuItem("Quit");
 		quit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				quit();
 			}
 		});
 		gameMenu.add(quit);
 		
 		//The help menu
 		JMenu helpMenu = new JMenu("Help");
 		menuBar.add(helpMenu);
 		
 		JMenuItem howToPlay = new JMenuItem("How to play");
 		howToPlay.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				howToPlay();
 			}
 		});
 		helpMenu.add(howToPlay);
 		
 		JMenuItem aboutBammi = new JMenuItem("About bammi");
 		aboutBammi.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				aboutBammi();
 			}
 		});
 		helpMenu.add(aboutBammi);
 	}
 	
 	
 	/**
 	 * Starts a new game.
 	 */
 	private void newGame(){
 	//	GameField = new GameField();
 	}
 	 
 	 
 	/**
 	 * Quits the application.
 	 */
 	private void quit(){
 		System.exit(0);
 	}
 	
 	/**
 	 * Brings up a new frame, displaying the rules.
 	 */
 	private void howToPlay(){
 		//TODO
 	}
 	
 	/**
 	 * Brings up a new frame with information about the game.
 	 */
 	private void aboutBammi(){
 		//TODO
 	}
 	
 	/**
 	 * Sets up the "settings" submenu.
 	 */
 	private JMenu makeSettingsMenu(){
 		JMenu settings = new JMenu("Settings");
 		
 		JMenuItem players = new JMenuItem("Add player");
 		players.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				settings_addPlayer(); 
 			}
 		});
 		settings.add(players);
 		
 		JMenuItem remplayer = new JMenuItem("Remove player");
 		remplayer.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				settings_remPlayer();
 			}
 		});
 		settings.add(remplayer);
 		
 		JMenuItem size = new JMenuItem("Map size");
 		size.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e){
 				settings_showMapSize();
 			}
 		});
 		settings.add(size);
 		
 		return settings;
 	}
 	
 	/**
 	 * Displays a dialog box for entering the new size.
 	 */
 	private void settings_showMapSize(){
 		//TODO
 	}
 	
 	/**
 	 * Displays a dialog for entering the new player.
 	 */
 	private void settings_addPlayer(){
 		int size = colors.length;
 		for(Color c : colors){
 			if(c == null) size--;
 		}
 		Color[] options = new Color[size];
		int actualIndex = 0;
		for(Color c : colors){
			if(c != null){
				options[actualIndex] = c;
				actualIndex++;
			}
		}
		Color chosenColor = options[JOptionPane.showOptionDialog(this, "Please select a color other than null:", "New player" , JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0])];
 		if(chosenColor != null) players.add(new User(chosenColor));
 		
 		Game.newGame(players);
 	}
 	
 	private void settings_remPlayer(){
 		Color[] c = new Color[players.size()];
 		int i = 0;
 		for(Player p : players){
 			c[i] = p.getColor();
 			i++;
 		}
 		Color chosenColor = c[JOptionPane.showOptionDialog(this, "Please select a player to remove:", "Remove player" , JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, c, c[0])];
 		for(i = 0; i<players.size(); i++){
 			if(players.get(i).getColor() == chosenColor) players.remove(i);
 		}
 		
 		//place color back as choosable
 		for(i = 0; i < colors.length; i++){
 			if(colors[i] == null){
 				colors[i] = chosenColor;
 				break;
 			}
 		}
 		
 		Game.newGame(players);
 		
 	}
 	
 }
