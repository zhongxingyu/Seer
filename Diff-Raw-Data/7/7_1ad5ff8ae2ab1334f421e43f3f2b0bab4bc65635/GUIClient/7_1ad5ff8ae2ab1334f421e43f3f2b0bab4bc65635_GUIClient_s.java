 /*  Copyright (C) 2010 - 2011  Fabian Neundorf, Philip Caroli,
  *  Maximilian Madlung,	Usman Ghani Ahmed, Jeremias Mechler
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ojim.client.gui;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.ojim.client.ClientBase;
 import org.ojim.client.gui.CardBar.CardWindow;
 import org.ojim.client.gui.GameField.GameField;
 import org.ojim.client.gui.OLabel.FontLayout;
 import org.ojim.client.gui.PopUpFrames.AboutFrame;
 import org.ojim.client.gui.PopUpFrames.CreateGameFrame;
 import org.ojim.client.gui.PopUpFrames.HelpFrame;
 import org.ojim.client.gui.PopUpFrames.JoinGameFrame;
 import org.ojim.client.gui.PopUpFrames.SettingsFrame;
 import org.ojim.client.gui.RightBar.ChatMessage;
 import org.ojim.client.gui.RightBar.ChatWindow;
 import org.ojim.client.gui.RightBar.PlayerInfoWindow;
 import org.ojim.language.Localizer;
 import org.ojim.language.LanguageDefinition;
 import org.ojim.logic.state.GameState;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Field;
 import org.ojim.logic.state.fields.Street;
 import org.ojim.rmi.client.NetClient;
 import org.ojim.rmi.server.ImplNetOjim;
 import org.ojim.rmi.server.StartNetOjim;
 import org.ojim.server.OjimServer;
 
 import com.sun.org.apache.xerces.internal.impl.RevalidationHandler;
 
 /**
  * Diese Klasse ist der GUI Client
  * 
  */
 public class GUIClient extends ClientBase {
 
 	private GUISettings settings;
 	private GameField gameField;
 	private ChatWindow chatWindow;
 	private PlayerInfoWindow playerInfoWindow;
 	private CardWindow cardWindow = new CardWindow();
 
 	private CreateGameFrame createGameFrame;
 	private JoinGameFrame joinGameFrame;
 	private SettingsFrame settingsFrame;
 	private HelpFrame helpFrame;
 	private AboutFrame aboutFrame;
 
 	private String name;
 
 	private MenuBar menubar;
 	private JPanel window = new JPanel();
 	private JPanel rightWindow1 = new JPanel();
 	private JPanel downWindow = new JPanel();
 	private JPanel downRight = new JPanel();
 	private JButton buyButton = new JButton();
 	private JButton rollButton = new JButton();
 	private JButton endTurnButton = new JButton();
 	private JButton button = new JButton();
 
 	private JPanel leftWindow = new JPanel();
 	private JPanel rightWindow = new JPanel();
 
 	private JButton jeremiasButton = new JButton();
 
 	private JFrame GUIFrame;
 
 	private JPanel pane = new JPanel(new OJIMLayout());
 	private Localizer language;
 
 	private boolean notInit = true;
 	private boolean haveIalreadyRolled = false;
 
 	private OjimServer server;
 
 	private MenuState menuState;
 
 	/**
 	 * Mit diesem Konstruktor wird der GUI Client gestartet
 	 */
 	public GUIClient() {
 
 		// Nur zu Debugzwecken auf game
 		setMenuState(MenuState.mainMenu);
 		// setMenuState(MenuState.game);
 
 		language = new Localizer();
 
 		LanguageDefinition[] langs = language.getLanguages();
 		if (langs.length == 0) {
 			System.out.println("No languagefile found.");
 		}
 
 		if (langs.length > 0)
 			language.setLanguage(langs[0]);
 
 		settings = new GUISettings();
 		settings.loadSettings();
 
 		playerInfoWindow = new PlayerInfoWindow(this);
 
 		createGameFrame = new CreateGameFrame(language, this);
 		joinGameFrame = new JoinGameFrame(language, this);
 		settingsFrame = new SettingsFrame(language, settings);
 		helpFrame = new HelpFrame(language);
 		aboutFrame = new AboutFrame(language);
 
 		createGameFrame.setTitle(language.getText("create game"));
 		joinGameFrame.setTitle(language.getText("join game"));
 		settingsFrame.setTitle(language.getText("settings"));
 		helpFrame.setTitle(language.getText("help"));
 		aboutFrame.setTitle(language.getText("about"));
 
 		gameField = new GameField(this);
 
 		GUIFrame = new JFrame(language.getText("ojim"));
 
 		playerInfoWindow.setLanguage(language);
 		chatWindow = new ChatWindow(language, this);
 
 		menubar = new MenuBar(language, this);
 		GUIFrame.setJMenuBar(menubar);
 		changeLanguage(langs[0].name);
 
 		GUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		GUIFrame.setMinimumSize(new Dimension(550, 450));
 
 		// LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
 
 		draw();
 
 	}
 
 	/**
 	 * Diese Methode war früher zum Updaten da
 	 */
 	private void draw() {
 
 		// GUIFrame = new JFrame(language.getText("ojim"));
 		/*
 		 * try { for (LookAndFeelInfo info :
 		 * UIManager.getInstalledLookAndFeels()) { if
 		 * ("Windows".equals(info.getName())) {
 		 * UIManager.setLookAndFeel(info.getClassName()); break; } else if
 		 * ("Nimbus".equals(info.getName())) {
 		 * UIManager.setLookAndFeel(info.getClassName()); break; } else if
 		 * ("Metal".equals(info.getName())) {
 		 * UIManager.setLookAndFeel(info.getClassName()); break; } } // Keines
 		 * der Standarddesigns vorhanden. Nimm das was du hast. } catch
 		 * (Exception e) { for (LookAndFeelInfo info :
 		 * UIManager.getInstalledLookAndFeels()) { try {
 		 * UIManager.setLookAndFeel(info.getClassName()); } catch (Exception e1)
 		 * { // Kein Look and Feel e1.printStackTrace(); } } }
 		 */
 
 		switch (menuState) {
 
 		case mainMenu:
 
 			break;
 
 		case waitRoom:
 
 			break;
 
 		case game:
 
 			// gameField.init(getGameState());
 
 			break;
 
 		}
 		if (GUIFrame != null) {
 			GUIFrame.setVisible(true);
 		}
 	}
 
 	/**
 	 * Die Startmethode des GUI Clients
 	 * 
 	 * @param args
 	 *            Parameter die nicht benutzt werden
 	 */
 	public static void main(String[] args) {
 		new GUIClient();
 	}
 
 	/**
 	 * Den Spielstatus ändern
 	 * 
 	 * @param menuState
 	 *            auf diesen Status wird gesetzt
 	 */
 	public void setMenuState(MenuState menuState) {
 		this.menuState = menuState;
 		draw();
 	}
 
 	@Override
 	public void onBuy(Player player, BuyableField field) {
 		gameField.playerBuysField(player, field);
 		if (player.getId() == getMe().getId()) {
 			cardWindow.addCard(field);
 		} else {
 			// Sollte man bei einer Auktion überboten sein worden.
 			cardWindow.removeCard(field);
 		}
 
 		if (player.getId() == getMe().getId()) {
 			downRight.remove(buyButton);
 		}
 
 		draw();
 
 		// System.out.println("Meista, da hat wer was gekauft!");
 		// draw();
 
 	}
 
 	@Override
 	public void onCashChange(Player player, int cashChange) {
 		playerInfoWindow.changeCash(player, getGameState().getPlayerByID(
 				player.getId()).getBalance());
 		// draw();
 
 		// Geld kleiner 0 Workaround weil getIsBankrupt nicht geht
 		if (player.getIsBankrupt()
 				|| getGameState().getPlayerByID(player.getId()).getBalance() < 0) {
 			playerInfoWindow.setBancrupt(player);
 			gameField.playerIsBancrupt(player);
 		} else {
 
 			for (int i = 0; i < GameState.FIELDS_AMOUNT; i++) {
 				if (getGameState().getFieldAt(i) instanceof org.ojim.logic.state.fields.FreeParking) {
 					this.gameField
 							.setFreeParkingMoney(((org.ojim.logic.state.fields.FreeParking) getGameState()
 									.getFieldAt(i)).getMoneyInPot());
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void onConstruct(Street street) {
 		System.out.println(street.getName() + " wurde upgegradet");
 		gameField.buildOnStreet(street);
 		draw();
 	}
 
 	@Override
 	public void onDestruct(Street street) {
 		gameField.destroyOnStreet(street);
 		draw();
 	}
 
 	@Override
 	public void onMessage(String text, Player sender, boolean privateMessage) {
 		chatWindow.write(new ChatMessage(sender, privateMessage, text));
 		// draw();
 	}
 
 	@Override
 	public void onMortgageToogle(BuyableField street) {
 		System.out.println("onMortage wurde für "+street.getName()+" aufgerufen");
 		cardWindow.switchCardStatus(street);
 		gameField.switchFieldStatus(street);
 		draw();
 	}
 
 	@Override
 	public void onMove(Player player, int position) {
 		// TODO: (v. xZise) position kann negativ sein (z.B. Gefängnis)
 		// this.menuState = MenuState.game;
 		// gameField.playerMoves(this.getGameState().getFieldAt(Math.abs(position)),
 		// player);
 		// gameField.init(GameState.FIELDS_AMOUNT, this.getGameState());
 
 		gameField.playerMoves(getGameState().getFieldAt(Math.abs(position)),
 				player);
 
 		/*
 		 * Falls Bancrupt in Move nicht geht for(int i = 0; i <
 		 * getGameState().getPlayers().length; i++){ if
 		 * (getGameState().getPlayerByID(i).getIsBankrupt()) {
 		 * playerInfoWindow.setBancrupt(getGameState().getPlayerByID(i));
 		 * gameField.playerIsBancrupt(getGameState().getPlayerByID(i));
 		 * System.out.println("Bancrupt2"); } }
 		 */
 
 		if (player.getId() == getMe().getId()) {
 
 			try {
 				if (((BuyableField) (getGameState().getFieldAt(getMe()
 						.getPosition()))).getPrice() <= getMe().getBalance()
 						&& ((BuyableField) (getGameState().getFieldAt(getMe()
 								.getPosition()))).getOwner() == null) {
 
 					downRight.add(buyButton);
 				}
 			} catch (Exception e) {
 				// System.out.println("Kein buyablefield");
 			}
 		}
 
 		draw();
 	}
 
 	@Override
 	public void onTrade(Player actingPlayer, Player partnerPlayer) {
 
 		System.out.println("Wurde angehandelt Meista!");
 		if (getTradeState() == TradeState.WAITING_PROPOSAL
 				|| getTradeState() == TradeState.WAITING_PROPOSED) {
 			if (actingPlayer.getId() == getMe().getId()) {
 				this.showTrade(partnerPlayer.getId());
 			} else if (partnerPlayer.getId() == getMe().getId()) {
 				this.showTrade(actingPlayer.getId());
 
 			}
 			chatWindow.write(new ChatMessage(null, false, ""
 					+ actingPlayer.getName() + " handelt mit "
 					+ partnerPlayer.getName()));
 		}
 		if (getTradeState() == TradeState.ACCEPTED
 				|| getTradeState() == TradeState.DECLINED) {
 			if (actingPlayer.getId() == getMe().getId()) {
 				this.gameField.endTrade();
 			} else if (partnerPlayer.getId() == getMe().getId()) {
 				this.gameField.endTrade();
 
 			}
 			chatWindow.write(new ChatMessage(null, false, ""
 					+ actingPlayer.getName() + " handelte mit "
 					+ partnerPlayer.getName()));
 			gameField.init(getGameState(), this);
 			for(int i = 0; i < getGameState().getPlayers().length; i++){
 				playerInfoWindow.changeCash(getGameState().getPlayerByID(i), getGameState().getPlayerByID(i).getBalance());
 			}
 		}
 	}
 
 	@Override
 	public void onBankruptcy() {
 		System.out.println("-- DEBUG -- on Bankruptcy ");
 		chatWindow.write(new ChatMessage(null, false,
 				"-- DEBUG -- on Bankruptcy"));
 	}
 
 	@Override
 	public void onCardPull(String text, boolean communityCard) {
 		// Passiert nix?
 		System.out.println("-- DEBUG -- on CardPull " + text);
 		chatWindow.write(new ChatMessage(null, false,
 				"-- DEBUG -- on CardPull " + text));
 	}
 
 	@Override
 	public void onDiceValues(int[] diceValues) {
 		gameField.dices(diceValues);
 
 	}
 
 	@Override
 	public void onStartGame(Player[] players) {
 
 		if (notInit) {
 			notInit = false;
 			gameField.init(getGameState(), this);
 			playerInfoWindow.setLanguage(language);
 			gameField.setLanguage(language);
 
 			// System.out.println("Es gibt "
 			// + this.getGameState().getPlayers().length + " Spieler.");
 			for (int i = 0; players.length > i; i++) {
 				// System.out.println(this.getGameState().getPlayers()[i].getName()+" wurde hinzugefügt mit "+this.getGameState().getPlayers()[i].getBalance()+" Kohle.");
 				this.playerInfoWindow.addPlayer(players[i]);
 			}
 
 			this.menuState = MenuState.game;
 			this.menubar.setMenuBarState(menuState);
 
 			GUIFrame.remove(window);
 
 			rightWindow1.add(playerInfoWindow);
 			rightWindow1.add(chatWindow);
 
 			downWindow.add(cardWindow);
 
 			ActionListener buyListener = new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					accept();
 					// System.out
 					// .println("BUYYYYYYYY THISSSSSS!!!! I NEEEED IT SOOOO MUCH");
 
 				}
 			};
 			buyButton.addActionListener(buyListener);
 
 			ActionListener rollListener = new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					// System.out.println("Rolly Rolly");
 					haveIalreadyRolled = true;
 					rollDice();
 				}
 			};
 
 			rollButton.addActionListener(rollListener);
 
 			ActionListener endTurnListener = new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					System.out
 							.println("Turn is ENDED!!! FREE THE HOSTAGE OR I WILL END IT WITH ENDTURN()!!!! OK YOU WANTED IT LIKE THIF!");
 					haveIalreadyRolled = false;
 					decline();
 					endTurn();
 				}
 			};
 
 			endTurnButton.addActionListener(endTurnListener);
 
 			buyButton.setLayout(new FontLayout());
 
 			rollButton.setLayout(new FontLayout());
 			endTurnButton.setLayout(new FontLayout());
 
 			jeremiasButton.add(new JLabel("JeremiasButton"));
 			ActionListener jeremiasListener = new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					rollDice();
 					rollDice();
 					rollDice();
 					accept();
 					endTurn();
 
 				}
 			};
 			;
 			;
 			jeremiasButton.addActionListener(jeremiasListener);
 			downRight.add(jeremiasButton);
 
 			downRight.setLayout(new GridLayout(1, 0));
 			rightWindow1.setLayout(new GridLayout(0, 1));
 
 			downRight.add(rollButton);
 			downRight.add(endTurnButton);
 
 			pane.add(gameField);
 			pane.add(rightWindow1);
 			pane.add(downWindow);
 			pane.add(downRight);
 
 			pane.setLayout(new OJIMLayout());
 
 			for (int i = 0; players.length > i; i++) {
 				try {
 					gameField.playerMoves(getGameState()
 							.getFieldAt(Math.abs(0)), getGameState()
 							.getPlayerByID(i));
 				} catch (NullPointerException e) {
 
 				}
 			}
 
 			pane.setSize(new Dimension(settings.getWidth(), settings
 					.getHeight()));
 			GUIFrame.add(pane);
 
 		}
 	}
 
 	@Override
 	public void onTurn(Player player) {
 		playerInfoWindow.turnOn(player);
 		// System.out.println("Player has changed to "+player.getName());
 		if (player.getId() != getMe().getId()) {
 			downRight.remove(rollButton);
 			downRight.remove(endTurnButton);
 			downRight.repaint();
 			downRight.revalidate();
 		} else {
 			downRight.add(rollButton);
 			downRight.add(endTurnButton);
 		}
 	}
 
 	@Override
 	public void setName(String name) {
 		super.setName(name);
 	}
 
 	/**
 	 * Öffnet ein neues Frame für die Spielerstellung
 	 */
 	public void openCreateGameWindow() {
 		createGameFrame.setVisible(true);
 
 	}
 
 	/**
 	 * Beendet das Spiel
 	 */
 	public void leaveGame() {
 
 		System.out.println("END!!");
 
 		pane.removeAll();
 		window.removeAll();
 
 		pane = new JPanel();
 		window = new JPanel();
 		pane.revalidate();
 		window.revalidate();
 
 		GUIFrame.remove(pane);
 		GUIFrame.remove(window);
 
 		GUIFrame.repaint();
 		server.endGame();
 
 		menuState = MenuState.mainMenu;
 
 	}
 
 	/**
 	 * Öffnet ein neues Frame für das Spielbeitreten
 	 */
 	public void openJoinGameWindow() {
 		joinGameFrame.showJoin();
 		joinGameFrame.setVisible(true);
 
 	}
 
 	/**
 	 * Öffnet ein neues Frame für das Spielbeitreten
 	 */
 	public void openServerListWindow() {
 		joinGameFrame.showServerList();
 		joinGameFrame.setVisible(true);
 
 	}
 
 	/**
 	 * Öffnet ein neues Frame für das Beitreten per Direkter Verbindung
 	 */
 	public void openDirectConnectionWindow() {
 		joinGameFrame.showDirectConnection();
 		joinGameFrame.setVisible(true);
 
 	}
 
 	/**
 	 * Öffnet ein neues Frame für den Abouttext
 	 */
 	public void openAboutWindow() {
 		aboutFrame.draw();
 		aboutFrame.setVisible(true);
 
 	}
 
 	/**
 	 * Öffnet ein neues Frame für den Hilfetext
 	 */
 	public void openHelpWindow() {
 		helpFrame.draw();
 		helpFrame.setVisible(true);
 
 	}
 
 	/**
 	 * Ändert die Sprache
 	 * 
 	 * @param languageName
 	 *            neue Sprache
 	 */
 	public void changeLanguage(String languageName) {
 		for (int i = 0; i < language.getLanguages().length; i++) {
 			if (language.getLanguages()[i].name.equals(languageName)) {
 				language.setLanguage(language.getLanguages()[i]);
 				resetLanguage();
 			}
 		}
 
 	}
 
 	/**
 	 * setzt die Sprache der verschiedenen Elemente auf die Klassensprache
 	 */
 	private void resetLanguage() {
 		GUIFrame.setTitle(language.getText("ojim"));
 		createGameFrame.setTitle(language.getText("create game"));
 		createGameFrame.setLanguage(language);
 		joinGameFrame.setTitle(language.getText("join game"));
 		joinGameFrame.setLanguage(language);
 		settingsFrame.setTitle(language.getText("settings"));
 		settingsFrame.setLanguage(language);
 		helpFrame.setTitle(language.getText("help"));
 		helpFrame.setLanguage(language);
 		aboutFrame.setTitle(language.getText("about"));
 		aboutFrame.setLanguage(language);
 		menubar.language(language);
 		chatWindow.setLanguage(language);
 		playerInfoWindow.setLanguage(language);
 		buyButton.setText(language.getText("buy"));
 		endTurnButton.setText(language.getText("endturn"));
 		rollButton.setText(language.getText("roll"));
 		gameField.setLanguage(language);
 		button.setText(language.getText("ready"));
 
 		draw();
 
 	}
 
 	@Override
 	public void onAuction(AuctionState auctionState) {
 
 		if (auctionState.value == 3) {
 			gameField.removeAuction();
 		} else {
 			
 			downRight.remove(buyButton);
 			this.GUIFrame.repaint();
 
 			gameField.showAuction(getAuctionState(), getAuctionedEstate(),
 					getBidder(), getHighestBid());
 		}
 	}
 
 	/**
 	 * Öffnet das Einstellungenfenster
 	 */
 	public void openSettingsWindow() {
 		settingsFrame.draw();
 		settingsFrame.setVisible(true);
 
 	}
 
 	/**
 	 * Startet ein neues Spiel und öffnet den Warteraum
 	 * 
 	 * @param string
 	 * @param k
 	 * @param j
 	 */
 	public void startServer(String serverName, int maxPlayers, int kiPlayers) {
 
 		setName(settings.getPlayerName());
 
 		menuState = MenuState.waitRoom;
 
 		server = new OjimServer(serverName);
 
 		server.initGame(maxPlayers, kiPlayers);
 
 		connect(server);
 		// connect("192.168.0.1",60);
 
 		leftWindow.remove(chatWindow);
 
 		leftWindow.setLayout(new GridLayout(0, 1));
 
 		rightWindow.remove(playerInfoWindow);
 		rightWindow.remove(button);
 
 		button.setText(language.getText("ready"));
 
 		window.setLayout(new GridLayout(1, 0));
 		rightWindow.setLayout(new GridLayout(0, 1));
 
 		playerInfoWindow.setLanguage(language);
 
 		rightWindow.add(playerInfoWindow);
 		leftWindow.add(chatWindow);
 
 		ActionListener clickedOnReady = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ready();
 
 			}
 		};
 		;
 		;
 
 		button.addActionListener(clickedOnReady);
 
 		rightWindow.add(button);
 
 		for (int i = 0; this.getGameState().getPlayers().length > i; i++) {
 			// System.out.println(this.getGameState().getPlayers()[i].getName()+" wurde hinzugefügt mit "+this.getGameState().getPlayers()[i].getBalance()+" Kohle.");
 			this.playerInfoWindow
 					.addPlayer(this.getGameState().getPlayers()[i]);
 		}
 
 		window.add(leftWindow);
 		window.add(rightWindow);
 
 		GameField.addMe(getMe());
 
 		GUIFrame.add(window);
 
 		createGameFrame.setVisible(false);
 		draw();
 
 	}
 
 	@Override
 	public void onNewPlayer(Player player) {
 		draw();
 
 	}
 
 	@Override
 	public void onPlayerLeft(Player player) {
 		draw();
 
 	}
 
 	/**
 	 * Verschickt eine Nachricht im Chat
 	 * 
 	 * @param text
 	 *            die zu verschickende Nachricht
 	 */
 	public void sendOutMessage(String text) {
 		sendMessage(text);
 		draw();
 	}
 
 	/**
 	 * Startet eine Verbindung über eine IP
 	 * 
 	 * @param ip
 	 *            die IP zu der verbunden werden muss
 	 * @param port
 	 *            der Port
 	 */
 	public void startIpConnection(String ip, int port) {
 		connect(ip, port);
 		System.out
 				.println("Starte Verbindung zu IP :" + ip + ", Port: " + port);
 	}
 
 	public void upgradeField(int position, int parseInt) {
 		construct((Street) getGameState().getFieldAt(position));
 
 	}
 
 	public void downgradeField(int position, int parseInt) {
 		destruct((Street) getGameState().getFieldAt(position));
 
 	}
 
 	public void trade(Player tradePartner, int cash, LinkedList<String> myFields, LinkedList<String> hisFields, int outOfJailCards) {
 		initTrade(tradePartner);
 		offerCash(cash);
 		if (!myFields.isEmpty()) {
 			for(int i = 0; i < GameState.FIELDS_AMOUNT; i++){
 				if(getGameState().getFieldAt(i).getName().equals(myFields.getFirst())){
 					System.out.println("Meine Felder: "+getGameState().getFieldAt(i).getName());
 					offerEstate((BuyableField)getGameState().getFieldAt(i));
 					
 				}
 			}
 		}
 		if (!hisFields.isEmpty()) {
 			for(int i = 0; i < GameState.FIELDS_AMOUNT; i++){
 				if(getGameState().getFieldAt(i).getName().equals(hisFields.getFirst())){
 					System.out.println("Seine Felder: "+getGameState().getFieldAt(i).getName());
 					requireEstate((BuyableField)getGameState().getFieldAt(i));
 					
 				}
 			}
 		}
 		
 		for (int i = 0; i < outOfJailCards; i++) {
 			offerGetOutOfJailCard();
 		}
 		System.out.println("Ok Meista, hab nun gehandelt!!");
 		proposeTrade();
 
 	}
 
 	public void showTrade(int player) {
 		if (this.menuState == menuState.game) {
 			gameField.showTrade(getMe(), getGameState().getPlayerByID(player),
 					getRequiredCash(), getRequiredEstates(),
 					getNumberOfRequiredGetOutOfJailCards(), getOfferedCash(),
 					getOfferedEstate(), getNumberOfOfferedGetOutOfJailCards());
 		}
 	}
 
 	public void acceptBid(int amount) {
 		placeBid(amount);
 		accept();
 
 	}
 
 	/**
 	 * Diese Funktion fügt eine Straße mit einem Mindestgebot zur Auktion hinzu
 	 * @param text Straßenname
 	 * @param newBidRate Auktionsminimumangebot
 	 */
 	public void startNewAuction(String text, JTextField newBidRate) {
 		
 		
 	}
 
 	public org.ojim.logic.state.fields.Field getFieldByPosition(String position) {
 		try {
 		return getGameState().getFieldAt(Integer.parseInt(position));
 		} catch (NullPointerException e){
 			System.out.println("GetFieldByPosition GUI Client NPE");
 			return null;
 		}
 	}
 
 	public void noTrade() {
 		decline();
 		
 	}
 
 	public void swtichCard(Field field) {
 		toggleMortgage((BuyableField)field);
 		
 	}
 
 	public Player getPlayerMe() {
 		return getMe();
 	}
 
 }
