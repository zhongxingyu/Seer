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
 
 package org.ojim.server;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.ojim.client.ai.AIClient;
 import org.ojim.iface.IClient;
 import org.ojim.iface.Rules;
 import org.ojim.log.OJIMLogger;
 import org.ojim.logic.ServerLogic;
 import org.ojim.logic.rules.GameRules;
 import org.ojim.logic.state.Auction;
 import org.ojim.logic.state.Card;
 import org.ojim.logic.state.GameState;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.ServerGameState;
 import org.ojim.logic.state.ServerPlayer;
 import org.ojim.logic.state.Trade;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.CardField;
 import org.ojim.logic.state.fields.Field;
 import org.ojim.logic.state.fields.FreeParking;
 import org.ojim.logic.state.fields.GoField;
 import org.ojim.logic.state.fields.GoToJail;
 import org.ojim.logic.state.fields.InfrastructureField;
 import org.ojim.logic.state.fields.InfrastructureFieldGroup;
 import org.ojim.logic.state.fields.Jail;
 import org.ojim.logic.state.fields.Station;
 import org.ojim.logic.state.fields.StationFieldGroup;
 import org.ojim.logic.state.fields.Street;
 import org.ojim.logic.state.fields.StreetFieldGroup;
 import org.ojim.logic.state.fields.TaxField;
 
 import edu.kit.iti.pse.iface.IServer;
 import edu.kit.iti.pse.iface.IServerAuction;
 import edu.kit.iti.pse.iface.IServerTrade;
 
 /**
  * 
  * @author Philip
  * 
  */
 public class OjimServer implements IServer, IServerAuction, IServerTrade {
 
 	/**
 	 * The name of the Server
 	 */
 	private String name;
 
 	/**
 	 * Can Clients connect to the server?
 	 */
 	private boolean isOpen = false;
 
 	/**
 	 * Is the Game started?
 	 */
 	private boolean gameStarted;
 
 	/**
 	 * The amount of connected Clients
 	 */
 	private int connectedClients;
 
 	/**
 	 * The amount of Clients that can be connected
 	 */
 	private int maxClients;
 
 	/**
 	 * All connected Clients
 	 */
 	private List<IClient> clients;
 
 	/**
 	 * The GameState
 	 */
 	private ServerGameState state;
 
 	/**
 	 * The Logic
 	 */
 	private ServerLogic logic;
 
 	/**
 	 * The Rules
 	 */
 	private GameRules rules;
 
 	/**
 	 * GameCards that need to be accepted/declined
 	 */
 	private List<Card> currentCards;
 
 	/**
 	 * current Auction, if null => no Auction
 	 */
 	private Auction auction;
 
 	/**
 	 * current Trade, if null => no Trade
 	 */
 	private Trade trade;
 
 	/**
 	 * List of all AI-Clients
 	 */
 	private AIClient aiClients[];
 
 	/**
 	 * the Logger TODO private?
 	 */
 	Logger logger;
 
 	private boolean initComplete = false;
 
 	/**
 	 * Creates a new Server. Has to be opened by initGame
 	 * 
 	 * @param name
 	 *            The name of the Server
 	 */
 	public OjimServer(String name) {
 		this.name = name;
 		this.gameStarted = false;
 		this.currentCards = new LinkedList<Card>();
 		// AI added for AI
 		this.state = new ServerGameState();
 		this.rules = new GameRules(this.state, new Rules());
 		this.logic = new ServerLogic(this.state, this.rules);
 		this.logger = OJIMLogger.getLogger(this.getClass().toString());
 	}
 
 	/**
 	 * Initializes the Server and opens it
 	 * 
 	 * @param playerCount
 	 *            maximum Player (AI and GUI) count
 	 * @param aiCount
 	 *            Amount of AI-Players
 	 * @return successful?
 	 */
 	public synchronized boolean initGame(int playerCount, int aiCount) {
 
 		this.initComplete = false;
 		if (isOpen) {
 			return false;
 		}
 
 		// Make sure no negative numbers appear and there are players at all
 		if (playerCount < 0 || aiCount < 0 || playerCount + aiCount == 0) {
 			return false;
 		}
 
 		// Init the GameFields
 		Field[] fields = new Field[GameState.FIELDS_AMOUNT];
 		this.loadDefaultGameStateFields(fields);
 		for (Field field : fields) {
 			this.state.setFieldAt(field, field.getPosition());
 		}
 
 		this.connectedClients = 0;
 		this.maxClients = playerCount;
 		clients = new LinkedList<IClient>();
 
 		aiClients = new AIClient[aiCount];
 		// AI Add AIClients to the Game
 		for (int i = 0; i < aiCount; i++) {
 			// AI changed
 			aiClients[i] = new AIClient(this);
 			logger.log(Level.CONFIG, "AI Client " + i + " added!");
 			aiClients[i].setReady();
 		}
 		// AI added
 		logger.log(Level.CONFIG, "All AI clients added");
 		// Open the Game
 		isOpen = true;
 		initComplete = true;
 		if (checkAllPlayersReady()) {
 			this.startGame();
 		}
 		/*
 		 * if (playerCount == aiCount) { while (!state.getGameIsWon()) { try {
 		 * wait(300); } catch (InterruptedException e) { // TODO Auto-generated
 		 * catch block e.printStackTrace(); } } }
 		 */
 		return true;
 	}
 
 	/**
 	 * Ends a Game and prepares the Server for the next Game
 	 * 
 	 * @return successful?
 	 */
 	public synchronized boolean endGame() {
 
 		// Stops the Game
 		this.gameStarted = false;
 		if (!this.isOpen) {
 			return true;
 		}
 
 		// Closing the Game
 		isOpen = false;
 		gameStarted = false;
 		this.logic.endGame();
 
 		// Disconnecting all Clients
 		while (clients.size() > 0) {
 			IClient client = clients.get(0);
 			disconnect(client);
 			client = null;
 		}
 
 		this.clients = null;
 
 		// Setting the Fields to the Standard Values
 		this.connectedClients = 0;
 		this.maxClients = 0;
 		clients = null;
 
 		return true;
 	}
 
 	/**
 	 * Disconnects a Client from the Game
 	 * 
 	 * @param client
 	 *            The Client to disconnect
 	 */
 	private synchronized void disconnect(IClient client) {
 		assert client != null;
 		for (IClient oneClient : this.clients) {
 			// TODO Add Language
 			oneClient.informPlayerLeft(getIdOfClient(client));
 			oneClient.informMessage("Client has been disconnected!", -1, false);
 		}
 		state.getPlayerByID(getIdOfClient(client)).setBankrupt();
 		this.clients.remove(client);
 		client = null;
 	}
 
 	private synchronized int getIdOfClient(IClient client) {
 		assert client != null;
 		for (Player player : state.getPlayers()) {
 			assert player instanceof ServerPlayer;
 			if (((ServerPlayer) player).getClient().equals(client)) {
 				return player.getId();
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Displays a String currently on the Console
 	 * 
 	 * @param string
 	 *            String to display
 	 */
 	private void display(String string) {
 		System.out.println(string);
 	}
 
 	@Override
 	public synchronized boolean initTrade(int actingPlayer, int partnerPlayer) {
 		// If there is already a Trade in process, dont create a new one
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		if (trade != null) {
 			if (trade.getTradeState() < 2) {
 				return false;
 			}
 		}
 		ServerPlayer acting = state.getPlayerByID(actingPlayer);
 		ServerPlayer partner = state.getPlayerByID(partnerPlayer);
 		if (acting != null && partnerPlayer == -1) {
 			trade = new Trade(acting, state.getBank());
 			return true;
 		}
 
 		if (acting != null && partner != null) {
 			trade = new Trade(acting, partner);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized int getTradeState() {
 		if (trade != null) {
 			return trade.getTradeState();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int getPartner() {
 		if (trade != null && trade.getPartner() != null) {
 			return trade.getPartner().getId();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized boolean offerCash(int playerID, int amount) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		Player player = state.getPlayerByID(playerID);
 		if (trade != null && trade.getTradeState() == 0 && player != null
 				&& player.equals(trade.getActing())) {
 			trade.setOfferedCash(amount);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean offerGetOutOfJailCard(int playerID) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		Player player = state.getPlayerByID(playerID);
 		if (trade != null && trade.getTradeState() == 0 && player != null
 				&& player.equals(trade.getActing())) {
 			trade.setOfferedNumberOfGetOutOfJailCards(trade
 					.getOfferedNumberOfGetOutOfJailCards() + 1);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean offerEstate(int playerID, int position) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		Player player = state.getPlayerByID(playerID);
 		Field field = state.getFieldAt(position);
 		if (trade != null && trade.getTradeState() == 0 && player != null
 				&& player.equals(trade.getActing()) && field != null
 				&& field instanceof BuyableField) {
 			return trade.addOfferedEstate((BuyableField) field);
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean requireCash(int playerID, int amount) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		Player player = state.getPlayerByID(playerID);
 		if (trade != null && trade.getTradeState() == 0 && player != null
 				&& player.equals(trade.getActing())) {
 			trade.setRequiredCash(amount);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean requireGetOutOfJailCard(int playerID) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		Player player = state.getPlayerByID(playerID);
 		if (trade != null && trade.getTradeState() == 0 && player != null
 				&& player.equals(trade.getActing())) {
 			trade.setRequiredNumberOfGetOutOfJailCards(trade
 					.getRequiredNumberOfGetOutOfJailCards() + 1);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean requireEstate(int playerID, int position) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		Player player = state.getPlayerByID(playerID);
 		Field field = state.getFieldAt(position);
 		if (trade != null && trade.getTradeState() == 0 && player != null
 				&& player.equals(trade.getActing()) && field != null
 				&& field instanceof BuyableField) {
 			return trade.addOfferedEstate((BuyableField) field);
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized int[] getOfferedEstates() {
 		if (trade != null) {
 			int[] out = new int[trade.getOfferedEstates().size()];
 			int i = 0;
 			for (BuyableField field : trade.getOfferedEstates()) {
 				if (i < out.length) {
 					out[i] = field.getPosition();
 					i++;
 				}
 			}
 			return out;
 		}
 		return new int[0];
 	}
 
 	@Override
 	public synchronized int getOfferedCash() {
 		if (trade != null) {
 			return trade.getOfferedCash();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int getNumberOfOfferedGetOutOfJailCards() {
 		if (trade != null) {
 			return trade.getOfferedNumberOfGetOutOfJailCards();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int[] getRequiredEstates() {
 		if (trade != null) {
 			int[] out = new int[trade.getRequiredEstates().size()];
 			int i = 0;
 			for (BuyableField field : trade.getRequiredEstates()) {
 				if (i < out.length) {
 					out[i] = field.getPosition();
 					i++;
 				}
 			}
 			return out;
 		}
 		return new int[0];
 	}
 
 	@Override
 	public synchronized int getRequiredCash() {
 		if (trade != null) {
 			return trade.getRequiredCash();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int getNumberOfRequiredGetOutOfJailCards() {
 		if (trade != null) {
 			return trade.getRequiredNumberOfGetOutOfJailCards();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized boolean cancelTrade(int playerID) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		ServerPlayer player = state.getPlayerByID(playerID);
 		if (trade != null && trade.getTradeState() == 0 && player != null
 				&& player.equals(trade.getActing())) {
 			trade = null;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean proposeTrade(int playerID) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		ServerPlayer player = state.getPlayerByID(playerID);
 		if (trade != null && trade.getTradeState() == 0 && player != null
 				&& player.equals(trade.getActing())) {
 			trade.setTradeState(1);
 			if (trade.getPartner() != null) {
 				trade.getPartner()
 						.getClient()
 						.informTrade(trade.getActing().getId(),
 								trade.getPartner().getId());
 				return true;
 			} else {
 				trade.setTradeState(3);
 				trade.executeTrade(logic);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized int getAuctionState() {
 		if (auction != null) {
 			return auction.getAuctionState();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int getAuctionedEstate() {
 		if (auction != null) {
 			return auction.getObjective().getPosition();
 		}
 		return 0;
 	}
 
 	@Override
 	public synchronized int getHighestBid() {
 		if (auction != null) {
 			return auction.getHighestBid();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int getBidder() {
 		if (auction != null) {
			return auction.getHighestBidder().getId();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized boolean placeBid(int playerID, int amount) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		Player player = state.getPlayerByID(playerID);
 		if (auction != null && player != null) {
 			return auction.placeBid(player, amount);
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized int getPlayerPiecePosition(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player == null) {
 			return -1;
 		}
 		return player.getPosition();
 	}
 
 	/**
 	 * Adds a GameCard that needs accepting/declining
 	 * 
 	 * @param card
 	 *            the Card to add
 	 */
 	public synchronized void addCurrentCard(Card card) {
 		if (state.getGameIsWon()) {
 			return;
 		}
 		this.currentCards.add(card);
 	}
 
 	/**
 	 * Gets all GameCards that need accepting/declining
 	 * 
 	 * @return List of Cards that need accept()/decline()
 	 */
 	public synchronized List<Card> getCurrentCards() {
 		return this.currentCards;
 	}
 
 	@Override
 	public synchronized int addPlayer(IClient client) {
 
 		for (int i = 0; i < maxClients; i++) {
 			if (state.getPlayerByID(i) == null) {
 
 				this.clients.add(client);
 				Player newPlayer = new ServerPlayer(client.getName(), 0,
 						state.getRules().startMoney, i, i, client);
 				state.setPlayer(newPlayer);
 				this.connectedClients++;
 				client.setPlayerId(newPlayer.getId());
 
 				// Inform all Players except the new one that a new Player is
 				// there
 				for (Player player : state.getPlayers()) {
 					if (!player.equals(newPlayer)) {
 						((ServerPlayer) player).getClient().informNewPlayer(i);
 					}
 				}
 
 				for (Player player : state.getPlayers()) {
 					client.informNewPlayer(player.getId());
 				}
 
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized void setPlayerReady(int playerID) {
 
 		// AI added
 		assert (state != null);
 		state.getPlayerByID(playerID).setIsReady(true);
 		// AI added
 		logger.log(Level.INFO, "Number of connected players = "
 				+ connectedClients);
 
 		// Are all players Ready? then start the game
 		if (checkAllPlayersReady()) {
 			// If all (non-AI) Players are ready, start the Game
 			startGame();
 		}
 	}
 
 	private synchronized boolean checkAllPlayersReady() {
 		if (this.connectedClients == this.maxClients && initComplete) {
 			for (Player player : state.getPlayers()) {
 				// Check if the Player is ready
 				if (!player.getIsReady()) {
 
 					// AI Clients don't need to be set to ready
 					if (player instanceof ServerPlayer
 							&& !(((ServerPlayer) player).getClient() instanceof AIClient)) {
 						return false;
 					}
 				}
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Starts a Game
 	 */
 	private synchronized void startGame() {
 		this.gameStarted = true;
 
 		logic.startGame();
 	}
 
 	@Override
 	public synchronized String getPlayerName(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null) {
 			String name = player.getName();
 			if (name == null || name == "") {
 				name = "Player" + player.getId();
 				if (player instanceof ServerPlayer) {
 					name = ((ServerPlayer) player).getName();
 				}
 			}
 			return name;
 		}
 		return "";
 	}
 
 	@Override
 	public synchronized int getPlayerColor(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null) {
 			return player.getColor();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized Rules getRules() {
 		if (this.state.getRules() != null) {
 			return state.getRules();
 		}
 		return null;
 	}
 
 	@Override
 	public synchronized String getEstateName(int position) {
 		Field field = state.getFieldAt(position);
 		String name = "";
 		if (field != null) {
 			name = field.getName();
 			if (field instanceof Street) {
 				name = ((Street) field).getFieldGroup().getName() + ": " + name;
 			}
 		}
 		return name;
 	}
 
 	@Override
 	public synchronized int getEstateColorGroup(int position) {
 		Field field = state.getFieldAt(position);
 		if (field != null) {
 			return field.getColorGroup();
 		}
 		return 0;
 	}
 
 	@Override
 	public synchronized int getEstateHouses(int position) {
 		Field field = state.getFieldAt(position);
 		if (field != null && field instanceof Street) {
 			return ((Street) field).getBuiltLevel();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int getEstatePrice(int position) {
 		Field field = state.getFieldAt(position);
 		if (field != null && field instanceof BuyableField) {
 			return ((BuyableField) field).getPrice();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int getEstateRent(int position, int houses) {
 		Field field = state.getFieldAt(position);
 		if (field != null && field instanceof Street) {
 			return ((Street) field).getRent(houses);
 		} else if (field != null && field instanceof BuyableField) {
 			return ((BuyableField) field).getRent();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized String getGameStatusMessage(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null && player instanceof ServerPlayer) {
 			return ((ServerPlayer) player).getGameStatusMessage();
 		}
 		return "";
 	}
 
 	@Override
 	public synchronized boolean isMortgaged(int position) {
 		Field field = state.getFieldAt(position);
 		if (field != null && field instanceof BuyableField) {
 			return ((BuyableField) field).isMortgaged();
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized int getOwner(int position) {
 		Field field = state.getFieldAt(position);
 		if (field != null && field instanceof BuyableField) {
 			Player owner = ((BuyableField) field).getOwner();
 			if (owner != null) {
 				return owner.getId();
 			}
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int getDiceValue() {
 		return state.getDices().getResultSum();
 	}
 
 	@Override
 	public synchronized int[] getDiceValues() {
 		return state.getDices().getResult();
 	}
 
 	@Override
 	public synchronized int getPlayerCash(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null) {
 			return player.getBalance();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int getPlayerOnTurn() {
 		if (state.getActivePlayer() != null) {
 			return state.getActivePlayer().getId();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int getNumberOfGetOutOfJailCards(int playerID) {
 		Player player = this.state.getPlayerByID(playerID);
 		if (player != null) {
 			return player.getNumberOfGetOutOfJailCards();
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized int getNumberOfHousesLeft() {
 		return state.getBank().getHouses();
 	}
 
 	@Override
 	public synchronized int getNumberOfHotelsLeft() {
 		return state.getBank().getHotels();
 	}
 
 	@Override
 	public synchronized boolean rollDice(int playerID) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		ServerPlayer player = this.state.getPlayerByID(playerID);
 
 		if (player == null || !rules.isPlayerOnTurn(player)
 				|| this.gameStarted == false
 				|| !this.rules.isRollRequiredByActivePlayer()
 				|| this.state.getGameIsWon()) {
 			return false;
 		}
 		if (player.getJail() != null) {
 
 			// Roll and Inform everyone
 			state.getDices().roll();
 			informDiceAll();
 
 			// Player has not rolled a Double and stays in jail
 			if (!state.getDices().isDouble()) {
 				state.setActivePlayerNeedsToRoll(false);
 				return true;
 			} else {
 				// Get the Player out of Jail
 				logic.playerRolledOutOfJail(player);
 				// Inform all that the Player is now out of Prison (position
 				// > -1)
 				informMoveAll(player);
 			}
 		}
 
 		int doubles = 0;
 		while (state.getActivePlayerNeedsToRoll()) {
 
 			// Roll the Dices and inform everyone about it
 			state.getDices().roll();
 			informDiceAll();
 
 			// Now move the Player forward
 			logic.movePlayerForDice(player, state.getDices().getResultSum());
 
 			// If the Player has not rolled a double, stop rolling
 			if (!state.getDices().isDouble()) {
 				state.setActivePlayerNeedsToRoll(false);
 			} else {
 				doubles++;
 				if (doubles >= GameRules.MAX_DOUBLES_ALLOWED) {
 					// Player has to get to jail
 					logic.sendPlayerToJail(player, state.getDefaultJail());
 					return true;
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Informs all Clients that the Dice has been rolled
 	 */
 	private synchronized void informDiceAll() {
 		for (IClient client : clients) {
 			client.informDiceValues(state.getDices().getResult());
 		}
 	}
 
 	/**
 	 * Inform all Clients that a move has occured
 	 * 
 	 * @param player
 	 *            the moving Player
 	 */
 	private synchronized void informMoveAll(Player player) {
 		for (IClient client : clients) {
 			client.informMove(player.getId(), player.getSignedPosition());
 		}
 	}
 
 	@Override
 	public synchronized boolean accept(int playerID) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		ServerPlayer player = state.getPlayerByID(playerID);
 		// Does a Trade need Confirmation?
 		if (trade != null && player != null && trade.getTradeState() == 1
 				&& player.equals(trade.getPartner())) {
 			trade.setTradeState(3);
 			trade.executeTrade(logic);
 			((ServerPlayer) trade.getActing()).getClient().informTrade(
 					trade.getActing().getId(),
 					(trade.getPartner() == null ? -1 : trade.getPartner()
 							.getId()));
 		}
 
 		if (player == null || !rules.isPlayerOnTurn(player)) {
 			return false;
 		}
 		// First check if a Action needs Confirmation
 		Card card = state.getFirstWaitingCard();
 		if (card != null) {
 			card.accept();
 			state.RemoveWaitingCard(card);
 			return true;
 		}
 		Field field = state.getFieldAt(player.getPosition());
 		if (field instanceof BuyableField
 				&& ((BuyableField) field).getOwner() == null
 				&& player.getBalance() > ((BuyableField) field).getPrice()) {
 			logic.buyStreet();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean decline(int playerID) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 
 		ServerPlayer player = state.getPlayerByID(playerID);
 
 		if (trade != null && player != null && trade.getTradeState() == 1
 				&& player.equals(trade.getPartner())) {
 			trade.setTradeState(2);
 			((ServerPlayer) trade.getActing()).getClient().informTrade(
 					trade.getActing().getId(),
 					(trade.getPartner() == null ? -1 : trade.getPartner()
 							.getId()));
 		}
 
 		if (player == null || playerID != state.getActivePlayer().getId()) {
 			return false;
 		}
 
 		// First check if a Action needs Confirmation
 		Card card = state.getFirstWaitingCard();
 		if (card != null) {
 			card.decline();
 			state.RemoveWaitingCard(card);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean endTurn(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 
 		if (player != null && rules.isPlayerOnTurn(player)
 				&& !state.getGameIsWon()) {
 			if (player.getJail() != null) {
 				player.waitInJail();
 			}
 			if (!rules.isRollRequiredByActivePlayer()) {
 				// Player is bankrupt
 				if (player.getBalance() < 0) {
 					this.logic.setPlayerBankrupt(player);
 				}
 				if (this.auction == null || this.auction.getAuctionState() >= 3) {
 					Field field = state.getFieldAt(player.getPosition());
 
 					if (field instanceof BuyableField
 							&& ((BuyableField) field).getOwner() == null) {
 						this.auction = new Auction(state, logic, rules,
 								(BuyableField) field);
 					} else {
 						logic.startNewTurn();
 						if (this.state.getGameIsWon()) {
 							this.endGame();
 						}
 					}
 				}
 
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean declareBankruptcy(int playerID) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		Player player = state.getPlayerByID(playerID);
 		if (player != null) {
 			this.logic.setPlayerBankrupt(player);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean construct(int playerID, int position) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		return changeLevel(playerID, position, 1);
 	}
 
 	/**
 	 * Changes the BuiltLevel of a Field
 	 * 
 	 * @param playerID
 	 *            the Player that want to change
 	 * @param position
 	 *            The Position of the Field
 	 * @param levelChange
 	 *            the levelChange
 	 * @return
 	 */
 	private boolean changeLevel(int playerID, int position, int levelChange) {
 		Player player = state.getPlayerByID(playerID);
 		Field field = state.getFieldAt(position);
 		if (player != null) {
 			if (field != null) {
 				if (rules.isFieldUpgradable(player, field, levelChange)) {
 					logic.upgrade((Street) field, levelChange);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean deconstruct(int playerID, int position) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		return changeLevel(playerID, position, -1);
 	}
 
 	@Override
 	public synchronized boolean toggleMortgage(int playerID, int position) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		Player player = state.getPlayerByID(playerID);
 		Field field = state.getFieldAt(position);
 		if (player != null) {
 			if (field != null) {
 				if (rules.isFieldMortgageable(player, field)) {
 					logic.toggleMortgage((BuyableField) field);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized void sendMessage(String text, int sender) {
 		for (IClient client : this.clients) {
 			client.informMessage(text, sender, false);
 		}
 	}
 
 	@Override
 	public synchronized void sendPrivateMessage(String text, int sender,
 			int reciever) {
 		if (reciever >= 0 && reciever < this.connectedClients) {
 			Player player = state.getPlayerByID(reciever);
 			if (player != null && player instanceof ServerPlayer) {
 				((ServerPlayer) player).getClient().informMessage(text, sender,
 						true);
 			}
 		}
 	}
 
 	/**
 	 * Gets the Name of the Server
 	 * 
 	 * @return Servername
 	 */
 	public synchronized String getName() {
 		return this.name;
 	}
 
 	/**
 	 * Gets the number of Clients that can connect
 	 * 
 	 * @return maximum Clientconnections
 	 */
 	public synchronized int getMaxClients() {
 		return this.maxClients;
 	}
 
 	public synchronized int getConnectedClients() {
 		return this.connectedClients;
 	}
 
 	@Override
 	public synchronized int getTurnsInPrison(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null) {
 			if (this.rules.isPlayerInPrison(player)) {
 				return player.getJail().getRoundsToWait();
 			} else {
 				return 0;
 			}
 		}
 		return -1;
 	}
 
 	@Override
 	public synchronized boolean useGetOutOfJailCard(int playerID) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		ServerPlayer player = state.getPlayerByID(playerID);
 		if (player != null && rules.isPlayerInPrison(player)) {
 			if (rules.canPlayerGetOutOfJail(player, true)) {
 				logic.playerUsesGetOutOfJailCard(player);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public synchronized List<IClient> getClients() {
 		return this.clients;
 	}
 
 	@Override
 	public synchronized boolean payFine(int playerID) {
 		if (state.getGameIsWon()) {
 			return false;
 		}
 		Player player = state.getPlayerByID(playerID);
 		if (player != null && rules.isPlayerInPrison(player)) {
 			if (player.getBalance() >= player.getJail().getMoneyToPay()) {
 				logic.playerUsesFineForJail(player);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public int getMaximumBuiltLevel() {
 		return 5;
 	}
 
 	@Override
 	public synchronized int getEstateHousePrice(int position) {
 		Field field = state.getFieldAt(position);
 		if (field != null && field instanceof Street) {
 			StreetFieldGroup group = ((Street) field).getFieldGroup();
 			if (group != null) {
 				return group.getHousePrice();
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Returns the money the player has to pay to leave the jail.
 	 * 
 	 * @param position
 	 *            The position of the jail.
 	 * @return The money the player has to pay. If there is no money the return
 	 *         is undefined;.
 	 */
 	public synchronized int getMoneyToPay(int position) {
 		Field field = state.getFieldAt(position);
 		if (field != null && field instanceof Jail) {
 			((Jail) field).getMoneyToPay();
 		}
 		return -1;
 	}
 
 	/**
 	 * Returns the number of rounds the player has to wait if the player is in
 	 * jail.
 	 * 
 	 * @param position
 	 *            The position of the jail.
 	 * @return The number of rounds the player has to wait. If this is no jail
 	 *         it return is undefined.
 	 */
 	public synchronized int getRoundsToWait(int position) {
 		Field field = state.getFieldAt(position);
 		if (field != null && field instanceof Jail) {
 			return ((Jail) field).getRoundsToWait();
 		}
 		return -1;
 	}
 
 	private synchronized CardField newEventCardField(int position) {
 		return new CardField("Ereignis- karte", position, false,
 				this.state.getEventCards(), this.logic);
 	}
 
 	private synchronized CardField newCommunityCardField(int position) {
 		return new CardField("Gemein- schafts- karte", position, true,
 				this.state.getCommunityCards(), this.logic);
 	}
 
 	private void loadDefaultGameStateFields(Field[] fields) {
 
 		// Initialise field groups
 		StationFieldGroup stations = new StationFieldGroup();
 		StreetFieldGroup[] streets = new StreetFieldGroup[8];
 		streets[0] = new StreetFieldGroup(0, "Dagobah", 1000);
 		streets[1] = new StreetFieldGroup(1, "Hoth", 1000);
 		streets[2] = new StreetFieldGroup(2, "Tatooine", 2000);
 		streets[3] = new StreetFieldGroup(3, "Yavin&nbsp;Vier", 2000);
 		streets[4] = new StreetFieldGroup(4, "Wolkenstadt", 3000);
 		streets[5] = new StreetFieldGroup(5, "Todesstern", 3000);
 		streets[6] = new StreetFieldGroup(6, "Endor", 4000);
 		streets[7] = new StreetFieldGroup(7, "Coruscant", 4000);
 		InfrastructureFieldGroup infrastructures = new InfrastructureFieldGroup();
 
 		// Add Streets
 		fields[0] = new GoField("Los", 0, this.logic);
 		fields[1] = streets[0].addField(new Street("Sumpf", 1, new int[] { 40,
 				200, 600, 1800, 3200, 5000 }, 0, 1200, logic));
 		fields[2] = this.newEventCardField(2);
 		fields[3] = streets[0].addField(new Street("Jodas Hütte", 3, new int[] {
 				80, 400, 1200, 3600, 6400, 9000 }, 0, 1200, logic));
 		fields[4] = new TaxField("Landungs- steuer", 4, 4000, this.logic);
 		fields[5] = stations.addField(new Station("TIE-Fighter", 5, 4000));
 		fields[6] = streets[1].addField(new Street("Echo-Basis", 6, new int[] {
 				120, 600, 1800, 5400, 8000, 11000 }, 0, 2000, logic));
 		fields[7] = this.newCommunityCardField(7);
 		fields[8] = streets[1].addField(new Street("Eis-Steppen", 8, new int[] {
 				120, 600, 1800, 5400, 8000, 11000 }, 0, 2000, logic));
 		fields[9] = streets[1].addField(new Street("Nordgebirge", 9, new int[] {
 				160, 800, 2000, 6000, 9000, 12000 }, 0, 2400, logic));
 		fields[10] = new Jail("Gefängnis", 10, 1000, 3);
 		fields[11] = streets[2].addField(new Street("Lars Heimstatt", 11,
 				new int[] { 200, 1000, 3000, 9000, 12500, 15000 }, 0, 2800,
 				logic));
 		fields[12] = infrastructures.addField(new InfrastructureField(
 				"Kern-Reaktor", 12, 3000, this.logic));
 		fields[13] = streets[2].addField(new Street("Mos Eisley", 13,
 				new int[] { 200, 1000, 3000, 9000, 12500, 15000 }, 0, 2800,
 				logic));
 		fields[14] = streets[2].addField(new Street("Jabbas Palast", 14,
 				new int[] { 240, 1200, 3600, 10000, 14000, 18000 }, 0, 3200,
 				logic));
 		fields[15] = stations.addField(new Station("Millenium Falke", 15, 4000,
 				this.logic));
 		fields[16] = streets[3].addField(new Street("Kommandozentrale", 16,
 				new int[] { 280, 1400, 4000, 11000, 15000, 19000 }, 0, 3600,
 				logic));
 		fields[17] = this.newEventCardField(17);
 		fields[18] = streets[3].addField(new Street("Massassi Tempel", 18,
 				new int[] { 280, 1400, 4000, 11000, 15000, 19000 }, 0, 3600,
 				logic));
 		fields[19] = streets[3].addField(new Street("Tempel-Thronsaal", 19,
 				new int[] { 320, 1600, 4400, 12000, 16000, 20000 }, 0, 4000,
 				logic));
 		fields[20] = new FreeParking("Frei Parken", 20, this.logic);
 		fields[21] = streets[4].addField(new Street("Andockbucht", 21,
 				new int[] { 360, 1800, 5000, 14000, 17500, 21000 }, 0, 4400,
 				logic));
 		fields[22] = this.newCommunityCardField(22);
 		fields[23] = streets[4].addField(new Street("Karbon- Gefrierkammer",
 				23, new int[] { 360, 1800, 5000, 14000, 17500, 21000 }, 0,
 				4400, logic));
 		fields[24] = streets[4].addField(new Street("Reaktor- Kontrollraum",
 				24, new int[] { 400, 2000, 6000, 15000, 18500, 22000 }, 0,
 				4800, logic));
 		fields[25] = stations.addField(new Station("X-Wing Fighter", 25, 4000,
 				this.logic));
 		fields[26] = streets[5].addField(new Street("Lande-Deck", 26,
 				new int[] { 440, 2200, 6600, 16000, 19500, 23000 }, 0, 5200,
 				logic));
 		fields[27] = streets[5].addField(new Street("Thronsaal", 27, new int[] {
 				440, 2200, 6600, 16000, 19500, 23000 }, 0, 5200, logic));
 		fields[28] = infrastructures.addField(new InfrastructureField(
 				"Wasser- Farm", 28, 3000, this.logic));
 		fields[29] = streets[5].addField(new Street("Hauptreaktor", 29,
 				new int[] { 480, 2400, 7200, 17000, 20500, 24000 }, 0, 5600,
 				logic));
 		fields[30] = new GoToJail("Gehe ins Gefängnis", 30, this.logic,
 				(Jail) fields[10]);
 		// Fabians Strange gelbe Karte wurde hiermit hoch offiziell von Max
 		// entfernt!
 		// fields[30] = streets[5].addField(new Street("foobar", 30, new int[] {
 		// 480, 2400, 7200, 17000, 20500, 24000 }, 0, 5600, logic));
 		fields[31] = streets[6].addField(new Street("Wald", 31, new int[] {
 				520, 2600, 7800, 18000, 22000, 25500 }, 0, 6000, logic));
 		fields[32] = streets[6].addField(new Street("Schildgenerator", 32,
 				new int[] { 520, 2600, 7800, 18000, 22000, 25500 }, 0, 6000,
 				logic));
 		fields[33] = this.newEventCardField(33);
 		fields[34] = streets[6].addField(new Street("Ewok-Dorf", 34, new int[] {
 				560, 3000, 9000, 20000, 24000, 28000 }, 0, 6400, logic));
 		fields[35] = stations
 				.addField(new Station("Stern-Zerstörer", 35, 4000));
 		fields[36] = this.newCommunityCardField(36);
 		fields[37] = streets[7].addField(new Street("Platz des Volkes", 37,
 				new int[] { 700, 3500, 10000, 22000, 16000, 30000 }, 0, 7000,
 				logic));
 		fields[38] = new TaxField("Kopf-Geld Prämie", 38, 2000);
 		fields[39] = streets[7].addField(new Street("Imperialer Palast", 39,
 				new int[] { 1000, 4000, 12000, 28000, 34000, 40000 }, 0, 8000,
 				logic));
 
 		stations.setRent(new int[] { 500, 1000, 2000, 4000 });
 		infrastructures.setFactors(new int[] { 80, 200 });
 
 		// Add Cards
 	}
 
 }
