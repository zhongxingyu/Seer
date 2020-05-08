 package ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 import model.Game;
 import model.GameState;
 import model.MeepleStruct;
 import model.PlayerStruct;
 import net.client.ClientProtocol;
 import net.client.SocketClient;
 import net.client.SocketClientProtocol;
 import net.server.ServerProtocol;
 import net.server.SocketServer;
 
 public class GameUi extends JFrame implements ActionListener, MouseListener,
 		DocumentListener, MessageSender {
 
 	private static final long serialVersionUID = 1L;
 
 	// Network settings.
 	private SocketClient gameClient = null;
 	private String server = "localhost";
 	private int port = 4444;
 
 	// Game settings & info.
 	private TileUi currentTile = null;
 	private GameState gameState = null;
 	private int tileSize = TileUi.tileSize * TileUi.tileTypeSize;
 	private int gameBoardWidth = 0;
 	private int gameBoardHeight = 0;
 
 	private HashMap<Integer, PlayerStruct> players = null;
 
 	private int player = 0; // The player who this client represents.
 	private int currentPlayer = 0; // The player whose turn it currently is.
 
 	private void showMessageDialog(String text) {
 		JOptionPane.showMessageDialog(this, text);
 	}
 
 	// Each menu screen (including the game screen) is contained within their
 	// own JPanel's, which are set as the frame content pane when appropriate.
 
 	// UI settings.
 	private String title = "Carcassonne";
 	private String resourceLoc = "/ui/resources/";
 	private URL iconUrl = getClass().getResource(resourceLoc + "icon.png");
 	private Image iconImage = Toolkit.getDefaultToolkit().getImage(iconUrl);
 
 	private String[] windowedResolutions = { "800 x 600", "1024 x 768",
 			"1280 x 800", "1280 x 1024", "1366 x 768", "1440 x 900",
 			"1680 x 1050", "1920 x 1080" };
 
 	private String[] windowedSettings = { "Fullscreen", "Windowed" };
 
 	private String currentWindowedMode = "Windowed";
 	private Dimension currentWindowedResolution = new Dimension(800, 600);
 	private int currentVolume = 25;
 	private boolean currentSoundEnabled = false;
 
 	// Main JPanels which are to be swapped as the frame content.
 	private JPanel titleScreenContentPane;
 	private JPanel optionsScreenContentPane;
 	private JPanel gameLobbyContentPane;
 	private JPanel gameContentPane;
 
 	// Other Ui elements which need to be declared globally (for action events).
 	private JComboBox windowedResolutionDropDown;
 	private JCanvas gameBoardWindow;
 	private JCanvas currentTilePanel;
 	private JButton drawTileButton;
 	private JButton endTurnButton;
 
 	// Variables to keep track of players in the game lobby, and in the game.
 	private HashMap<Integer, JPlayerSettingsPanel> playerSettingsPanels = new HashMap<Integer, JPlayerSettingsPanel>();
 	private HashMap<Integer, JPlayerStatusPanel> playerStatusPanels = new HashMap<Integer, JPlayerStatusPanel>();
 	private JPanel playerSettingsPanelContainer;
 
 	/**
 	 * Constructor for the game Ui.
 	 */
 	public GameUi() {
 
 		// Initialize game menus.
 		// The game screen is initialized after we get the game parameters from
 		// the server as we can then create the players list for scoring.
 		initTitleScreen();
 		initOptionsScreen();
 		initLobbyScreen();
 
 		// Set the title bar and icon.
 		setTitle(title);
 		setIconImage(iconImage);
 
 		// Show the title screen to begin with.
 		setContentPane(titleScreenContentPane);
 
 		// Other frame/program settings.
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setResizable(false);
 		setPreferredSize(currentWindowedResolution);
 
 		// Remove player from lobby when they exit via the window exit button.
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 
 				// Messages can only be sent if we are connected to a server.
 				// eg. In the lobby or in a game.
 				if (gameClient != null) {
 
 					// If the game has not started yet then the player must be
 					// in the lobby. So we leave the lobby. Otherwise, send the
 					// exit message to leave the game.
 					if (gameState == null) {
 						String msg = "LEAVELOBBY;player;" + player;
 						sendMessage(msg);
 					} else {
 						// Close the socket client.
 						sendMessage(SocketClientProtocol.EXIT);
 					}
 				}
 			}
 		});
 
 		pack();
 		setVisible(true);
 		setLocationRelativeTo(null);
 	}
 
 	@Override
 	public void sendMessage(String message) {
 		if (gameClient != null) {
 			gameClient.sendMessage(message);
 		}
 	}
 
 	// Menu Layout:
 	//
 	// title
 	// - options
 	// - lobby (play)
 	// - quit
 	//
 	// options
 	// - title
 	// - change network
 	// - change music
 	// - change video
 	// - change sound
 	//
 	// lobby
 	// - title
 	// - game options
 	// - play
 	//
 	// play
 	// -title
 	// -quit
 	//
 	/**
 	 * Initialize the title screen.
 	 * 
 	 * This method creates the title screen content pane, and populates it. The
 	 * menu consists of buttons to navigate to the multiplayer game lobby, the
 	 * options menu, and to quit the game.
 	 */
 	private void initTitleScreen() {
 
 		titleScreenContentPane = new JPanel(new BorderLayout());
 
 		// Create our navigation components.
 		JLabel titleLabel = new JLabel("Carcassonne");
 		titleLabel.setVerticalTextPosition(JLabel.BOTTOM);
 		titleLabel.setHorizontalTextPosition(JLabel.CENTER);
 		titleLabel.setHorizontalAlignment(JLabel.CENTER);
 
 		JButton hostGameButton = new JButton("Host Game");
 		hostGameButton.setVerticalTextPosition(AbstractButton.CENTER);
 		hostGameButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		hostGameButton.setActionCommand("hostGame");
 		hostGameButton.addActionListener(this);
 
 		JButton joinGameButton = new JButton("Join Game");
 		joinGameButton.setVerticalTextPosition(AbstractButton.CENTER);
 		joinGameButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		joinGameButton.setActionCommand("joinGame");
 		joinGameButton.addActionListener(this);
 
 		JButton optionsButton = new JButton("Options");
 		optionsButton.setVerticalTextPosition(AbstractButton.CENTER);
 		optionsButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		optionsButton.setActionCommand("showOptionsScreen");
 		optionsButton.addActionListener(this);
 
 		JButton exitButton = new JButton("Quit Game");
 		exitButton.setVerticalTextPosition(AbstractButton.CENTER);
 		exitButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		exitButton.setActionCommand("exitGame");
 		exitButton.addActionListener(this);
 
 		// After creating the components, place them on the title screen.
 		JPanel titleContainer = new JPanel(new GridBagLayout());
 
 		GridBagConstraints gc = new GridBagConstraints();
 		gc.insets = new Insets(2, 2, 2, 2);
 
 		gc.gridx = 0;
 		gc.gridy = 0;
 		gc.gridwidth = 2;
 		gc.gridheight = 1;
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		titleContainer.add(hostGameButton, gc);
 		gc.gridy = 1;
 		titleContainer.add(joinGameButton, gc);
 		gc.gridy = 2;
 		gc.gridwidth = 1;
 		gc.fill = GridBagConstraints.NONE;
 		gc.insets = new Insets(2, 2, 20, 2);
 		titleContainer.add(optionsButton, gc);
 		gc.gridx = 1;
 		titleContainer.add(exitButton, gc);
 
 		titleScreenContentPane.add(titleLabel, BorderLayout.PAGE_START);
 		titleScreenContentPane.add(titleContainer, BorderLayout.PAGE_END);
 	}
 
 	/**
 	 * Initialize the options screen.
 	 * 
 	 * This method creates the options screen content pane, and populates it.
 	 * The menu consists of video & audio options such as; controls to change
 	 * the display resolution & type, sound volume setting, toggling of whether
 	 * the game music is enabled, and finally a control to allow the player to
 	 * return to the main menu. The initial values are set to those set near the
 	 * beginning of this file (currentWindowedMode, currentVolume, &c).
 	 */
 	private void initOptionsScreen() {
 
 		optionsScreenContentPane = new JPanel(new BorderLayout());
 
 		JLabel titleLabel = new JLabel("Options");
 		titleLabel.setVerticalTextPosition(JLabel.BOTTOM);
 		titleLabel.setHorizontalTextPosition(JLabel.CENTER);
 		titleLabel.setHorizontalAlignment(JLabel.CENTER);
 
 		// Network controls.
 		JLabel networkSettingsLabel = new JLabel("Network");
 		networkSettingsLabel.setVerticalTextPosition(JLabel.BOTTOM);
 		networkSettingsLabel.setHorizontalTextPosition(JLabel.CENTER);
 		networkSettingsLabel.setHorizontalAlignment(JLabel.CENTER);
 
 		// TODO property command feels very hacky
 		JTextField serverField = new JTextField(30);
 		serverField.setText(server);
 		serverField.getDocument().putProperty("command", "setServerHost");
 		serverField.getDocument().addDocumentListener(this);
 
 		JTextField portField = new JTextField(10);
 		portField.setText(Integer.toString(port));
 		portField.getDocument().putProperty("command", "setServerPort");
 		portField.getDocument().addDocumentListener(this);
 
 		// Video controls.
 		JLabel videoSettingsLabel = new JLabel("Video");
 		videoSettingsLabel.setVerticalTextPosition(JLabel.BOTTOM);
 		videoSettingsLabel.setHorizontalTextPosition(JLabel.CENTER);
 		videoSettingsLabel.setHorizontalAlignment(JLabel.CENTER);
 
 		windowedResolutionDropDown = new JComboBox(windowedResolutions);
 		windowedResolutionDropDown.setSelectedItem("800 x 600");
 		windowedResolutionDropDown.setActionCommand("changeResolution");
 		windowedResolutionDropDown.addActionListener(this);
 
 		JComboBox windowedModeDropDown = new JComboBox(windowedSettings);
 		windowedModeDropDown.setSelectedItem("Windowed");
 		windowedModeDropDown.setActionCommand("changeWindowedMode");
 		windowedModeDropDown.addActionListener(this);
 
 		// Audio controls.
 		// TODO music
 		JLabel audioSettingsLabel = new JLabel("Sound");
 		audioSettingsLabel.setVerticalTextPosition(JLabel.BOTTOM);
 		audioSettingsLabel.setHorizontalTextPosition(JLabel.CENTER);
 		audioSettingsLabel.setHorizontalAlignment(JLabel.CENTER);
 
 		JPanel volumeControlPanel = new VolumeControlPanel(currentVolume);
 
 		JButton soundToggleButton = new JButton("Music: "
 				+ (currentSoundEnabled ? "ON" : "OFF"));
 
 		soundToggleButton.setVerticalTextPosition(AbstractButton.CENTER);
 		soundToggleButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		soundToggleButton.setActionCommand("toggleSound");
 		soundToggleButton.addActionListener(this);
 
 		// Navigation controls.
 		JButton backButton = new JButton("Back");
 		backButton.setVerticalTextPosition(AbstractButton.CENTER);
 		backButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		backButton.setActionCommand("showTitleScreen");
 		backButton.addActionListener(this);
 
 		// Place the created components in the container.
 		JPanel optionsContainer = new JPanel(new GridBagLayout());
 
 		GridBagConstraints gc = new GridBagConstraints();
 		gc.insets = new Insets(2, 2, 2, 2);
 		gc.gridwidth = 1;
 		gc.gridheight = 1;
 
 		gc.gridx = 0;
 		gc.gridy = 0;
 		optionsContainer.add(networkSettingsLabel, gc);
 		gc.gridy = 1;
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		optionsContainer.add(serverField, gc);
 		gc.gridy = 2;
 		optionsContainer.add(portField, gc);
 
 		gc.gridx = 1;
 		gc.gridy = 0;
 		optionsContainer.add(videoSettingsLabel, gc);
 		gc.gridy = 1;
 		optionsContainer.add(windowedResolutionDropDown, gc);
 		gc.gridy = 2;
 		optionsContainer.add(windowedModeDropDown, gc);
 
 		gc.gridx = 2;
 		gc.gridy = 0;
 		optionsContainer.add(audioSettingsLabel, gc);
 		gc.gridy = 1;
 		optionsContainer.add(volumeControlPanel, gc);
 		gc.gridy = 2;
 		optionsContainer.add(soundToggleButton, gc);
 
 		gc.gridx = 1;
 		gc.gridy = 3;
 		gc.insets = new Insets(2, 2, 20, 2);
 		optionsContainer.add(backButton, gc);
 
 		optionsScreenContentPane.add(titleLabel, BorderLayout.PAGE_START);
 		optionsScreenContentPane.add(optionsContainer, BorderLayout.PAGE_END);
 	}
 
 	// Functions to satisfy DocumentListener for server & port textFields.
 	@Override
 	public void changedUpdate(DocumentEvent e) {
 		doUpdate(e);
 	}
 
 	@Override
 	public void removeUpdate(DocumentEvent e) {
 		doUpdate(e);
 	}
 
 	@Override
 	public void insertUpdate(DocumentEvent e) {
 		doUpdate(e);
 	}
 
 	private void doUpdate(DocumentEvent e) {
 
 		Document document = e.getDocument();
 		String command = (String) e.getDocument().getProperty("command");
 
 		try {
 			String text = document.getText(0, document.getLength());
 
 			if ("setServerHost".equals(command)) {
 				server = text;
 			} else if ("setServerPort".equals(command) && text.length() > 0) {
 				port = Integer.parseInt(text);
 			}
 
 		} catch (NumberFormatException nfException) {
 			// User didn't input a valid port number.
 			JOptionPane.showMessageDialog(this, "Port must be a number.");
 
 		} catch (BadLocationException blException) {
 			// Something genuinely went wrong... which shouldn't happen.
 		}
 	}
 
 	/**
 	 * Initialize the lobby screen.
 	 * 
 	 * This method creates the lobby screen content pane, and populates it. The
 	 * pane consists of a list of players which are waiting to play, along with
 	 * controls to allow them to begin the game or to quit.
 	 */
 	private void initLobbyScreen() {
 
 		GridBagConstraints gc;
 		Insets insets = new Insets(2, 2, 2, 2);
 
 		// Containers.
 		// Chat window. TODO
 		JPanel chatContainer = new JPanel();
 		chatContainer.setBorder(BorderFactory.createLineBorder(Color.black));
 
 		// Player list.
 		// It is updated as other players join & leave.
 		playerSettingsPanelContainer = new JPanel(new GridBagLayout());
 
 		// Back button
 		JButton backButton = new JButton("Quit");
 		backButton.setVerticalTextPosition(AbstractButton.CENTER);
 		backButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		backButton.setActionCommand("leaveLobby");
 		backButton.addActionListener(this);
 
 		// Start game button
 		JButton startButton = new JButton("Ready!");
 		startButton.setVerticalTextPosition(AbstractButton.CENTER);
 		startButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		startButton.setActionCommand("startGame");
 		startButton.addActionListener(this);
 
 		// Add buttons to the navigation panel.
 		JPanel navigationContainer = new JPanel(new GridBagLayout());
 
 		gc = new GridBagConstraints();
 		gc.insets = insets;
 		gc.gridx = 0;
 		gc.gridy = 0;
 
 		navigationContainer.add(backButton, gc);
 
 		gc.gridx++;
 		navigationContainer.add(startButton, gc);
 
 		// Add navigation and player list to the game settings.
 		JPanel gameSettingsContainer = new JPanel(new GridBagLayout());
 
 		gc = new GridBagConstraints();
 		gc.insets = insets;
 		gc.gridx = 0;
 		gc.gridy = 0;
 
 		gc.weightx = 1;
 		gc.weighty = 0.95;
 		gc.anchor = GridBagConstraints.FIRST_LINE_END;
 		gc.fill = GridBagConstraints.NONE;
 		gameSettingsContainer.add(playerSettingsPanelContainer, gc);
 
 		gc.gridy++;
 		gc.weightx = 1;
 		gc.weighty = 0.05;
 		gc.anchor = GridBagConstraints.LAST_LINE_END;
 		gc.fill = GridBagConstraints.NONE;
 		gameSettingsContainer.add(navigationContainer, gc);
 
 		// Create the main content pane and add components to it.
 		// Chat on left side, player list on right with start & exit options on
 		// the bottom right.
 		gameLobbyContentPane = new JPanel(new GridBagLayout());
 
 		gc = new GridBagConstraints();
 		gc.insets = insets;
 		gc.gridx = 0;
 		gc.gridy = 0;
 		gc.fill = GridBagConstraints.BOTH;
 
 		gc.weightx = 0.45;
 		gc.weighty = 1;
 		gc.anchor = GridBagConstraints.LINE_START;
 		gameLobbyContentPane.add(chatContainer, gc);
 
 		gc.gridx++;
 		gc.weightx = 0.55;
 		gc.weighty = 1;
 		gc.anchor = GridBagConstraints.LINE_END;
 		gameLobbyContentPane.add(gameSettingsContainer, gc);
 	}
 
 	/**
 	 * Initialize the game screen.
 	 * 
 	 * This uses values obtained by the 'INIT' message to set game properties.
 	 * After this, the game controls are created, player status list is created
 	 * from player settings list, and then the game begins.
 	 */
 	private void initGameScreen() {
 
 		GridBagConstraints gc;
 		Insets insets = new Insets(2, 2, 2, 2);
 
 		// Set game properties, set viewport on the map.
 		int gameWidth = gameBoardWidth * tileSize;
 		int gameHeight = gameBoardHeight * tileSize;
 		Point scrollPos = new Point(gameWidth / 2, gameHeight / 2);
 
 		gameContentPane = new JPanel(new BorderLayout());
 		gameBoardWindow = new JCanvas(gameWidth, gameHeight);
 
 		JScrollPane gameBoardWindowScrollPane = new JScrollPane(gameBoardWindow);
 		gameBoardWindowScrollPane.getViewport().setViewPosition(scrollPos);
 
 		// Add mouse listener for game window.
 		gameBoardWindow.addMouseListener(this);
 
 		// Top part of the info window has the score information.
 		JPanel playerInfoPanel = new JPanel(new GridBagLayout());
 
 		// Create the player status panels, and fill the info panel with them.
 		gc = new GridBagConstraints();
 		gc.insets = insets;
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		gc.anchor = GridBagConstraints.LINE_START;
 		gc.weightx = 1;
 		gc.weighty = 1 / players.size();
 		gc.gridx = 0;
 		gc.gridy = 0;
 
 		// Add the players to the status panel (in ascending order).
 		List<Integer> playerReps = new ArrayList<Integer>(players.keySet());
 		Collections.sort(playerReps);
 		Iterator<Integer> playerRepsIter = playerReps.iterator();
 
 		while (playerRepsIter.hasNext()) {
 
 			Integer playerRep = playerRepsIter.next();
 			PlayerStruct player = players.get(playerRep);
 
 			String name = player.getName();
 			int score = player.getScore();
 			Color color = player.getColor();
 
 			JPlayerStatusPanel playerStatusPanel;
 			playerStatusPanel = new JPlayerStatusPanel(name, score, color);
 
 			playerStatusPanels.put(playerRep, playerStatusPanel);
 			playerInfoPanel.add(playerStatusPanel, gc);
 			gc.gridy++;
 		}
 
 		// Create the buttons for tile manipulation & turn/window control.
 		// Draw Pile.
 		URL drawTileUrl = getClass().getResource(resourceLoc + "tile-back.jpg");
 		ImageIcon drawTileImage = new ImageIcon(drawTileUrl);
 
 		int drawTileWidth = drawTileImage.getIconWidth();
 		int drawTileHeight = drawTileImage.getIconHeight();
 
 		Dimension drawTileDimension;
 		drawTileDimension = new Dimension(drawTileWidth, drawTileHeight);
 
 		drawTileButton = new JButton(drawTileImage);
 		drawTileButton.setPreferredSize(drawTileDimension);
 		drawTileButton.setActionCommand("drawTile");
 		drawTileButton.addActionListener(this);
 
 		// Zooming. TODO
 		JButton zoomInButton = new JButton("+");
 		zoomInButton.setActionCommand("zoomIn");
 		zoomInButton.addActionListener(this);
 		zoomInButton.setEnabled(false);
 
 		JButton zoomOutButton = new JButton("-");
 		zoomOutButton.setActionCommand("zoomOut");
 		zoomOutButton.addActionListener(this);
 		zoomOutButton.setEnabled(false);
 
 		// Tile Rotation.
 		JButton rotateCWButton = new JButton("--\\");
 		rotateCWButton.setActionCommand("rotateCW");
 		rotateCWButton.addActionListener(this);
 
 		JButton rotateCCWButton = new JButton("/--");
 		rotateCCWButton.setActionCommand("rotateCCW");
 		rotateCCWButton.addActionListener(this);
 
 		// Current Tile.
 		currentTilePanel = new JCanvas(tileSize, tileSize);
 
 		// End turn button.
 		// Action for this button should only be allowed to let the player
 		// skip placing their meeple.
 		endTurnButton = new JButton("End my turn");
 		endTurnButton.setActionCommand("endTurn");
 		endTurnButton.addActionListener(this);
 
 		// Bottom part of the info window has the controls.
 		JPanel playerControlsPanel = new JPanel(new GridBagLayout());
 
 		// Fill in the player controls panel.
 		gc = new GridBagConstraints();
 		gc.insets = insets;
 		gc.gridx = 0;
 		gc.gridy = 0;
 
 		gc.gridwidth = 2;
 		gc.gridheight = 2;
 		gc.fill = GridBagConstraints.NONE;
 		playerControlsPanel.add(drawTileButton, gc);
 
 		gc.fill = GridBagConstraints.BOTH;
 		gc.gridx = 2;
 		gc.gridy = 0;
 		gc.gridwidth = 1;
 		gc.gridheight = 1;
 		playerControlsPanel.add(zoomInButton, gc);
 
 		gc.gridy = 1;
 		playerControlsPanel.add(zoomOutButton, gc);
 
 		gc.gridx = 0;
 		gc.gridy = 2;
 		playerControlsPanel.add(rotateCCWButton, gc);
 
 		gc.gridx = 2;
 		playerControlsPanel.add(rotateCWButton, gc);
 
 		gc.gridx = 0;
 		gc.gridy = 3;
 		gc.gridwidth = 3;
 		gc.gridheight = 3;
 		gc.fill = GridBagConstraints.NONE;
 		playerControlsPanel.add(currentTilePanel, gc);
 
 		gc.gridx = 0;
 		gc.gridy = 6;
 		gc.gridwidth = 3;
 		gc.gridheight = 1;
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		playerControlsPanel.add(endTurnButton, gc);
 
 		// Add the player info and game controls to the info container.
 		JPanel infoContainer = new JPanel(new BorderLayout());
 
 		infoContainer.add(playerInfoPanel, BorderLayout.NORTH);
 		infoContainer.add(playerControlsPanel, BorderLayout.SOUTH);
 
 		// Add the game board canvas and info container to the content pane.
 		gameContentPane.add(gameBoardWindowScrollPane, BorderLayout.CENTER);
 		gameContentPane.add(infoContainer, BorderLayout.EAST);
 	}
 
 	// Gameplay actions for non-current players are not allowed.
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		// Actions for gameplay (activated via buttons).
 		if (gameState != null && currentPlayer == player) {
 
 			// Event handlers for tile rotation.
 			if (gameState.equals(GameState.PLACE_TILE)) {
 
 				if ("rotateCW".equals(e.getActionCommand())) {
 
 					String message = "ROTATETILE;currentPlayer;" + player
 							+ ";direction;clockwise";
 					sendMessage(message);
 				}
 
 				if ("rotateCCW".equals(e.getActionCommand())) {
 
 					String message = "ROTATETILE;currentPlayer;" + player
 							+ ";direction;counterClockwise";
 					sendMessage(message);
 				}
 			}
 
 			// Let a player end their turn if they don't want to place a meeple.
 			if (gameState.equals(GameState.PLACE_MEEPLE)) {
 
 				if ("endTurn".equals(e.getActionCommand())) {
 
 					String msg = "ENDTURN;currentPlayer;" + player;
 					sendMessage(msg);
 				}
 			}
 
 			// Each turn begins with a player drawing a tile from the draw
 			// pile. Here we allow a player to draw the tile. After they
 			// have, we draw it to the screen on the current tile panel.
 			if (gameState.equals(GameState.DRAW_TILE)) {
 
 				if ("drawTile".equals(e.getActionCommand())) {
 
 					String message = "DRAWTILE;currentPlayer;" + player;
 					sendMessage(message);
 				}
 			}
 		}
 
 		// Non-gameplay actions.
 		// Main screen.
 		if ("showOptionsScreen".equals(e.getActionCommand())) {
 			// TODO allow screen size & options changes during game
 			setContentPane(optionsScreenContentPane);
 			validate();
 			repaint();
 
 		} else if ("exitGame".equals(e.getActionCommand())) {
 
 			// Exit the program.
 			System.exit(0);
 
 		} else if ("hostGame".equals(e.getActionCommand())
 				|| "joinGame".equals(e.getActionCommand())) {
 
 			if ("hostGame".equals(e.getActionCommand())) {
 
 				// Start up Server.
 				// TODO perhaps server should stop when hosting player
 				// leaves the game lobby? failover?
 				new SocketServer(port, ServerProtocol.class).start();
 
 				// Reset the server hostname in case it was changed.
 				server = "localhost";
 			}
 
 			// Start up client.
 			ClientProtocol clientProtocol = new ClientProtocol(this);
 			gameClient = new SocketClient(server, port, clientProtocol);
 
 			if (gameClient.bind() == 1) {
 				showMessageDialog("Error connecting to server.");
 			}
 
 			// Send a message that we want to join the/a game.
 			sendMessage("JOINLOBBY");
 
 			setContentPane(gameLobbyContentPane);
 			validate();
 			repaint();
 		}
 
 		// Lobby screen.
 		if ("startGame".equals(e.getActionCommand())) {
 
 			// The user who starts the game will check game start values such as
 			// checking for color and number of players. Then when the message
 			// returns from the server it is sent to all clients, telling them
 			// to begin the game.
 
 			// TODO in the future the game will only start when all players are
 			// ready.
 
 			HashMap<Integer, PlayerStruct> players = getPlayersFromLobby();
 
 			// Check that we have a correct amount of players.
 			if (players.size() > Game.getMaxPlayers()) {
 
 				showMessageDialog("A game can have at most "
 						+ Game.getMaxPlayers() + " players.");
 
 				return;
 			}
 
 			// Check that all the players colors are different
 			Iterator<Integer> playersIter = players.keySet().iterator();
 			HashSet<Color> usedColors = new HashSet<Color>();
 
 			while (playersIter.hasNext()) {
 
 				int playerRep = playersIter.next();
 				Color color = players.get(playerRep).getColor();
 
 				if (usedColors.contains(color)) {
 
 					showMessageDialog("Players need to be different colors.");
 					return;
 				}
 				usedColors.add(color);
 			}
 
 			// Start the game; query for the initialization info.
 			String msg = "INIT;numPlayers;" + Integer.toString(players.size());
 			sendMessage(msg);
 
 		} else if ("leaveLobby".equals(e.getActionCommand())) {
 
 			// Send the message to notify we are leaving the game lobby.
 			String msg = "LEAVELOBBY;player;" + player;
 			sendMessage(msg);
 
 			// Close the socket client.
 			sendMessage(SocketClientProtocol.EXIT);
 
 			// Return to the main game screen.
 			setContentPane(titleScreenContentPane);
 			validate();
 			repaint();
 		}
 
 		// Options screen.
 		if ("showTitleScreen".equals(e.getActionCommand())) {
 			setContentPane(titleScreenContentPane);
 			validate();
 			repaint();
 
 		} else if ("changeWindowedMode".equals(e.getActionCommand())) {
 
 			// Get the users selection.
 			JComboBox cb = (JComboBox) e.getSource();
 			String mode = (String) cb.getSelectedItem();
 
 			if ("Fullscreen".equals(mode)
 					&& !"Fullscreen".equals(currentWindowedMode)) {
 
 				// Fullscreen the window by removing the title bar, resizing
 				// to the screen size, and moving to fill the screen.
 				dispose();
 				setUndecorated(true);
 				setSize(Toolkit.getDefaultToolkit().getScreenSize());
 				setLocation(0, 0);
 				setVisible(true);
 
 				// Disable the resolution setting.
 				windowedResolutionDropDown.setEnabled(false);
 
 			} else if ("Windowed".equals(mode)
 					&& !"Windowed".equals(currentWindowedMode)) {
 
 				// Change the resolution to match the current selection.
 				dispose();
 				setUndecorated(false);
 				setSize(currentWindowedResolution);
 				setLocationRelativeTo(null);
 				setVisible(true);
 
 				// Re-enable the resolution setting.
 				windowedResolutionDropDown.setEnabled(true);
 			}
 
 			currentWindowedMode = mode;
 
 		} else if ("changeResolution".equals(e.getActionCommand())
 				&& !currentWindowedMode.equals("Fullscreen")) {
 
 			JComboBox cb = (JComboBox) e.getSource();
 			String res = (String) cb.getSelectedItem();
 
 			String[] resArray = res.split(" ");
 
 			int width = Integer.parseInt(resArray[0]);
 			int height = Integer.parseInt(resArray[2]);
 
 			setSize(width, height);
 			currentWindowedResolution = new Dimension(width, height);
 
 		} else if ("toggleSound".equals(e.getActionCommand())) {
 
 			JButton toggleSoundButton = (JButton) e.getSource();
 
 			if (currentSoundEnabled) {
 				toggleSoundButton.setText("Music: OFF");
 				currentSoundEnabled = false;
 			} else {
 				toggleSoundButton.setText("Music: ON");
 				currentSoundEnabled = true;
 				// TODO Code to play song file; looped.
 			}
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 
 		// Detect if there has been a mouse click on the board canvas object.
 		if (e.getComponent() == gameBoardWindow && currentPlayer == player
 				&& gameState != null) {
 
 			// We'll do some click calculations outside of the state-specific
 			// checks to prevent code duplication. First get the clicked
 			// position, and then convert it to a tile location.
 			int xPos = e.getX();
 			int yPos = e.getY();
 
 			// Example:
 			// x click is 52, with each tile having 5 TileType's at size
 			// 10 each. Then the player has clicked 52 / 5*10 = 1st tile.
 			// Our array is zero-indexed, so this is correct. Otherwise we'd
 			// get the mathematical ceiling.
 			int xBoard = xPos / tileSize;
 			int yBoard = yPos / tileSize;
 
 			int xTile = (xPos % tileSize) / TileUi.tileTypeSize;
 			int yTile = (yPos % tileSize) / TileUi.tileTypeSize;
 
 			// Check that we are in the proper game state.
 			// We are looking to place either a tile or a meeple.
 			if (gameState.equals(GameState.PLACE_TILE)) {
 
 				String message = "PLACETILE;currentPlayer;" + player
 						+ ";xBoard;" + xBoard + ";yBoard;" + yBoard;
 				sendMessage(message);
 			}
 
 			if (gameState.equals(GameState.PLACE_MEEPLE)) {
 
 				String message = "PLACEMEEPLE;currentPlayer;" + player
 						+ ";xBoard;" + xBoard + ";yBoard;" + yBoard + ";xTile;"
 						+ xTile + ";yTile;" + yTile;
 				sendMessage(message);
 			}
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	// TODO: one client leaving only removes him from the game,
 
 	// TODO: game continues when a player leaves.. all their meeples are removed
 	// & they are removed from the game.
 
	// TODO: some sort of bug w/ endturn

 	// TODO: disable end turn button on non-current player
 
	// TODO: incorrect state when player attempts to place meeple on claimed
	// feature

 	/**
 	 * Exit the game.
 	 */
 	public void exit() {
 		gameClient = null;
 	}
 
 	/**
 	 * Assign this player an identifier.
 	 * 
 	 * @param player
 	 *            The identifier for the player (this client).
 	 */
 	public void assignPlayer(int player) {
 		this.player = player;
 	}
 
 	/**
 	 * Update the game lobby. After updating the playerSettingsPanels, it calls
 	 * a corresponding function to update the ui component of the game lobby.
 	 * 
 	 * @param players
 	 *            A hashmap which maps the player id (representation) to a
 	 *            player structure.
 	 */
 	public void updateLobby(HashMap<Integer, PlayerStruct> players) {
 
 		playerSettingsPanels.clear();
 
 		List<Integer> playerRepList = new ArrayList<Integer>(players.keySet());
 		Collections.sort(playerRepList);
 		Iterator<Integer> playersIter = playerRepList.iterator();
 
 		while (playersIter.hasNext()) {
 
 			int rep = playersIter.next();
 			PlayerStruct player = players.get(rep);
 
 			String name = player.getName();
 			Color col = player.getColor();
 
 			// TODO remove numberrep from psp?
 			JPlayerSettingsPanel psp = new JPlayerSettingsPanel(rep, name, col);
 
 			playerSettingsPanels.put(rep, psp);
 		}
 
 		// Update ui to match.
 		updateLobbyUi();
 	}
 
 	private void updateLobbyUi() {
 
 		// Clear our current ui player lists.
 		// PlayerStruct list is populated @ game start; we can leave it be.
 		playerSettingsPanelContainer.removeAll();
 
 		// Layout settings we need for the player settings panels.
 		GridBagConstraints gc = new GridBagConstraints();
 		gc.insets = new Insets(2, 2, 2, 2);
 		gc.gridx = 0;
 		gc.gridy = 0;
 
 		// Add all of the players to the ui lobby screen (in ascending order).
 		Set<Integer> playerRepsSet = playerSettingsPanels.keySet();
 		List<Integer> playerReps = new ArrayList<Integer>(playerRepsSet);
 		Collections.sort(playerReps);
 		Iterator<Integer> playerRepsIter = playerReps.iterator();
 
 		while (playerRepsIter.hasNext()) {
 
 			int playerRep = playerRepsIter.next();
 			JPlayerSettingsPanel psp = playerSettingsPanels.get(playerRep);
 
 			playerSettingsPanelContainer.add(psp, gc);
 			gc.gridy++;
 		}
 
 		playerSettingsPanelContainer.revalidate();
 		playerSettingsPanelContainer.repaint();
 	}
 
 	/**
 	 * Start a game. This method receives all relevant info to move clients from
 	 * the lobby into a game which has just been started.
 	 * 
 	 * @param currentPlayer
 	 *            the player which will have the first turn.
 	 * @param width
 	 *            the width of the game board in tiles.
 	 * @param height
 	 *            the height of the game board in tiles.
 	 */
 	public void init(int currentPlayer, int width, int height) {
 
 		if (gameState != null) {
 			return;
 		}
 
 		gameState = GameState.START_GAME;
 
 		this.currentPlayer = currentPlayer;
 		gameBoardWidth = width;
 		gameBoardHeight = height;
 
 		// Users which didn't click the button to start game need values init'd.
 		players = getPlayersFromLobby();
 
 		initGameScreen();
 		showCurrentPlayer(currentPlayer);
 		endTurnButton.setEnabled(false);
 
 		// Switch to the correct content pane.
 		setContentPane(gameContentPane);
 		validate();
 		repaint();
 
 		// Update the game state.
 		gameState = GameState.DRAW_TILE;
 	}
 
 	// Generate the player list from the players which are in the lobby.
 	private HashMap<Integer, PlayerStruct> getPlayersFromLobby() {
 
 		HashMap<Integer, PlayerStruct> players = new HashMap<Integer, PlayerStruct>();
 
 		Iterator<Integer> playerRepsIter;
 		playerRepsIter = playerSettingsPanels.keySet().iterator();
 
 		while (playerRepsIter.hasNext()) {
 
 			int playerRep = playerRepsIter.next();
 			JPlayerSettingsPanel psp = playerSettingsPanels.get(playerRep);
 
 			String name = psp.getPlayerName();
 			Color color = psp.getPlayerColor();
 
 			players.put(playerRep, new PlayerStruct(name, color));
 		}
 
 		return players;
 	}
 
 	/**
 	 * Allow a user to draw a tile. This method receives the player which has
 	 * drawn the tile along with any information needed to identify the tile.
 	 * 
 	 * @param currentPlayer
 	 *            the player whose turn it is.
 	 * @param identifier
 	 *            the tile identifier.
 	 * @param orientation
 	 *            the tile orientation.
 	 */
 	public void drawTile(int currentPlayer, String identifier, int orientation) {
 
 		if (gameState != GameState.DRAW_TILE) {
 			return;
 		}
 
 		// Create the tile and add it to the Gui.
 		TileUi tileUi = new TileUi(identifier, orientation);
 
 		currentTile = tileUi;
 
 		currentTilePanel.add(tileUi);
 		currentTilePanel.repaint();
 
 		// After drawing a tile the user must place it. Update game state and
 		// disable the draw tile button.
 		gameState = GameState.PLACE_TILE;
 		drawTileButton.setEnabled(false);
 	}
 
 	/**
 	 * Allow a player to rotate the tile that they have drawn (but not placed
 	 * yet).
 	 * 
 	 * @param currentPlayer
 	 *            the player whose turn it is.
 	 * @param direction
 	 *            the direction to rotate the tile. This can be "clockwise" or
 	 *            "counterClockwise".
 	 */
 	public void rotateTile(int currentPlayer, String direction) {
 
 		if (gameState != GameState.PLACE_TILE) {
 			return;
 		}
 
 		if (direction.equals("clockwise")) {
 			currentTile.rotateClockwise();
 		}
 
 		if (direction.equals("counterClockwise")) {
 			currentTile.rotateCounterClockwise();
 		}
 
 		currentTilePanel.repaint();
 	}
 
 	/**
 	 * Function called after parsing the place tile message. It carries out any
 	 * actions necessary to actually place for the tile ui. It also does any
 	 * actions needed for game state changes.
 	 * 
 	 * @param xBoard
 	 *            the x board position to place the tile.
 	 * @param yBoard
 	 *            the y board position to place the tile.
 	 */
 	public void placeTile(int currentPlayer, int xBoard, int yBoard, int error) {
 
 		if (gameState != GameState.PLACE_TILE) {
 			return;
 		}
 
 		if (error != 0) {
 			showMessageDialog("Can't place tile there.");
 			return;
 		}
 
 		// Transfer the tile from the current tile panel to the game board.
 		currentTilePanel.clear();
 		currentTile.setx(xBoard * tileSize);
 		currentTile.sety(yBoard * tileSize);
 		gameBoardWindow.add(currentTile);
 		currentTile = null;
 
 		gameBoardWindow.repaint();
 		currentTilePanel.repaint();
 
 		// Update the game state.
 		// In the Place Meeple game state, we will allow the player to also
 		// end their turn.
 		gameState = GameState.PLACE_MEEPLE;
 		endTurnButton.setEnabled(true);
 	}
 
 	/**
 	 * Function called after parsing the place meeple message. It carries out
 	 * any actions necessary to actually place the meeple wrt/ the ui. It also
 	 * does any actions needed for game state changes.
 	 * 
 	 * @param xBoard
 	 *            the x board position to place the meeple.
 	 * @param yBoard
 	 *            the y board position to place the meeple.
 	 * @param xTile
 	 *            the x tile position to place the meeple.
 	 * @param yTile
 	 *            the y tile position to place the meeple.
 	 */
 	public void placeMeeple(int currentPlayer, int xBoard, int yBoard,
 			int xTile, int yTile, int error) {
 
 		if (gameState != GameState.PLACE_MEEPLE) {
 			return;
 		}
 
 		if (error != 0) {
 			showMessageDialog("Can't place meeple there.");
 			return;
 		}
 
 		int mx = (xBoard * tileSize) + (xTile * TileUi.tileTypeSize);
 		int my = (yBoard * tileSize) + (yTile * TileUi.tileTypeSize);
 
 		Color playerColor = players.get(currentPlayer).getColor();
 		MeepleUi meepleUi = new MeepleUi(playerColor, mx, my);
 
 		gameBoardWindow.add(meepleUi);
 		gameBoardWindow.repaint();
 
 		String msg = "ENDTURN;currentPlayer;" + player;
 		sendMessage(msg);
 	}
 
 	/**
 	 * Main method called to handle the SCORE message.
 	 * 
 	 * @param message
 	 *            the semi-parsed message (split into an ArrayList at
 	 *            semi-colons).
 	 */
 	public void score(Set<MeepleStruct> meeplePositions) {
 
 		Iterator<MeepleStruct> meeples = meeplePositions.iterator();
 
 		while (meeples.hasNext()) {
 
 			MeepleStruct ms = meeples.next();
 
 			int msxb = ms.getxBoard();
 			int msxt = ms.getxTile();
 			int msyb = ms.getyBoard();
 			int msyt = ms.getyTile();
 
 			int mx = (msxb * tileSize) + (msxt * TileUi.tileTypeSize);
 			int my = (msyb * tileSize) + (msyt * TileUi.tileTypeSize);
 
 			// Meeples are equal if they are located on the same tile, at
 			// the same position. So color we pass in doesn't matter. This
 			// is okay since we don't allow more than one meeple to be
 			// placed at the same position anyway.
 			MeepleUi meeple = new MeepleUi(new Color(0), mx, my);
 
 			gameBoardWindow.remove(meeple);
 		}
 
 		gameBoardWindow.repaint();
 	}
 
 	/**
 	 * End the turn. This method advances the current player, as well as
 	 * updating the ui to match. Any other ui related actions are done, such as
 	 * enabling or disabling controls.
 	 * 
 	 * @param player
 	 *            the player who's turn is ending (numberRep).
 	 */
 	public void endTurn(int player) {
 
 		currentPlayer = (player + 1) % players.size();
 		showCurrentPlayer(currentPlayer);
 
 		endTurnButton.setEnabled(false);
 		drawTileButton.setEnabled(true);
 
 		gameState = GameState.DRAW_TILE;
 	}
 
 	// TODO hideCurrentPlayer / showCurrentPlayer cleanup
 	/**
 	 * Update the UI to show which player's turn it is.
 	 * 
 	 * @param player
 	 *            an integer representing the current player (numberRep).
 	 */
 	private void showCurrentPlayer(int player) {
 
 		// Reset all players to not be the current player wrt/ the ui.
 		Iterator<Integer> playerRepsIter;
 		playerRepsIter = playerStatusPanels.keySet().iterator();
 
 		while (playerRepsIter.hasNext()) {
 			int playerRep = playerRepsIter.next();
 			playerStatusPanels.get(playerRep).setCurrentPlayer(false);
 		}
 
 		// Set the current player wrt/ the ui.
 		playerStatusPanels.get(player).setCurrentPlayer(true);
 	}
 
 	public void playerInfo(int player, int currentPlayer, int playerScore,
 			int meeplesPlaced) {
 
 		playerStatusPanels.get(player).setScore(playerScore);
 	}
 
 	/**
 	 * Actions to be taken after receiving a game info message.
 	 * 
 	 * @param currentPlayer
 	 *            The player whose turn it is.
 	 * 
 	 * @param drawPileEmpty
 	 *            Whether the draw pile is empty.
 	 */
 	public void gameInfo(int currentPlayer, boolean drawPileEmpty) {
 
 		if (drawPileEmpty) {
 			gameState = GameState.END_GAME;
 		}
 
 		// TODO score screen or something
 	}
 
 }
