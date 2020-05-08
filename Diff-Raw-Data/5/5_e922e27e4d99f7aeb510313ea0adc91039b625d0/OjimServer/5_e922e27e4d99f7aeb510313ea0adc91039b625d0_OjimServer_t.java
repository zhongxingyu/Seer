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
 import org.ojim.logic.state.fields.FieldGroup;
 import org.ojim.logic.state.fields.FreeParking;
 import org.ojim.logic.state.fields.GoField;
 import org.ojim.logic.state.fields.GoToJail;
 import org.ojim.logic.state.fields.InfrastructureField;
 import org.ojim.logic.state.fields.Jail;
 import org.ojim.logic.state.fields.Station;
 import org.ojim.logic.state.fields.StationFieldGroup;
 import org.ojim.logic.state.fields.Street;
 import org.ojim.logic.state.fields.StreetFieldGroup;
 import org.ojim.logic.state.fields.TaxField;
 
 import edu.kit.iti.pse.iface.IServer;
 import edu.kit.iti.pse.iface.IServerAuction;
 import edu.kit.iti.pse.iface.IServerTrade;
 
 public class OjimServer implements IServer, IServerAuction, IServerTrade {
 
 	private String name;
 	private boolean isOpen = false;
 	private boolean gameStarted;
 	private int connectedClients;
 	private int maxClients;
 	private List<IClient> clients;
 
 	private ServerGameState state;
 	private ServerLogic logic;
 	private GameRules rules;
 	private List<Card> currentCards;
 	private Auction auction;
 	private Trade trade;
 
 	//AI added
 	private AIClient aiClients[];
 
 	Logger logger;
 
 	public OjimServer(String name) {
 		this.name = name;
 		this.gameStarted = false;
 		this.currentCards = new LinkedList<Card>();
 		//AI added for AI
 		this.state = new ServerGameState();
 		this.rules = new GameRules(this.state, new Rules());
 		this.logic = new ServerLogic(this.state, this.rules);
 		this.logger = OJIMLogger.getLogger(this.getClass().toString());
 	}
 
 	boolean initGame(int playerCount, int aiCount) {
 
 		if (isOpen) {
 			return false;
 		}
 
 		// Make sure no negative numbers appear and there are players at all
 		if (playerCount < 0 || aiCount < 0 || playerCount + aiCount == 0) {
 			display("Player- and AICount must not be negative and one of them must be positive");
 			return false;
 		}
 
 		// Initializing Fields
 		this.connectedClients = 0;
 		this.maxClients = playerCount + aiCount;
 		clients = new LinkedList<IClient>();
 
 		aiClients = new AIClient[aiCount];
 		//AI Add AIClients to the Game
 		for (int i = 0; i < aiCount; i++) {
 			//AI changed
 			aiClients[i] = new AIClient(this, logic, i);
 			addPlayer((IClient) aiClients[i]);
 			logger.log(Level.CONFIG, "AI Client " + i + " added!");
 			aiClients[i].setReady();
 		}
 		//AI added
 		logger.log(Level.CONFIG, "All AI clients added");
 		// Open the Game
 		isOpen = true;
 		return true;
 	}
 
 	boolean endGame() {
 
 		this.gameStarted = false;
 		if (!isOpen) {
 			return true;
 		}
 		// Closing the Game
 		isOpen = false;
 
 		// Disconnecting all Clients
 		for (IClient client : clients) {
 			if (client instanceof AIClient) {
 				client = null;
 			} else {
 				disconnect(client);
 				client = null;
 			}
 		}
 
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
 	private void disconnect(IClient client) {
 		for (IClient oneClient : this.clients) {
 			if (oneClient.equals(oneClient)) {
 				// TODO Add Language
 				oneClient.informMessage("You have been Disconnected!", -1, true);
 				this.clients.remove(oneClient);
 			}
 			if (this.state.getActivePlayer().getId() != -1) {
 				// TODO Add AI as replacement
 				// TODO Add Language
 				for (IClient informClient : this.clients) {
 					informClient.informMessage("Client has been disconnected!", -1, false);
 				}
 			}
 		}
 
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
 	public boolean initTrade(int actingPlayer, int partnerPlayer) {
 		//If there is already a Trade in process, dont create a new one
 		if(trade != null) {
 			if(trade.getTradeState() < 2) {
 				return false;
 			}
 		}
 		ServerPlayer acting = state.getPlayerByID(actingPlayer);
 		ServerPlayer partner = state.getPlayerByID(partnerPlayer);
 		
 		if(acting != null && partner != null) {
 			trade = new Trade(acting, partner);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public int getTradeState() {
 		if(trade != null) {
 			return trade.getTradeState();
 		}
 		return -1;
 	}
 
 	@Override
 	public int getPartner() {
 		if(trade != null) {
 			return trade.getPartner().getId();
 		}
 		return -1;
 	}
 
 	@Override
 	public boolean offerCash(int playerID, int amount) {
 		Player player = state.getPlayerByID(playerID);
 		if(trade != null && trade.getTradeState() == 0 && player != null && player.equals(trade.getActing())) {
 			trade.setOfferdCash(amount);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean offerGetOutOfJailCard(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if(trade != null && trade.getTradeState() == 0 && player != null && player.equals(trade.getActing())) {
 			trade.setOfferedNumberOfGetOutOfJailCards(trade.getOfferedNumberOfGetOutOfJailCards() + 1);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean offerEstate(int playerID, int position) {
 		Player player = state.getPlayerByID(playerID);
 		Field field = state.getFieldAt(position);
 		if(trade != null && trade.getTradeState() == 0 && player != null && player.equals(trade.getActing()) && field != null && field instanceof BuyableField) {
 			return trade.addOfferedEstate((BuyableField)field);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean requireCash(int playerID, int amount) {
 		Player player = state.getPlayerByID(playerID);
 		if(trade != null && trade.getTradeState() == 0 && player != null && player.equals(trade.getActing())) {
 			trade.setRequiredCash(amount);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean requireGetOutOfJailCard(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if(trade != null && trade.getTradeState() == 0 && player != null && player.equals(trade.getActing())) {
 			trade.setRequiredNumberOfGetOutOfJailCards(trade.getRequiredNumberOfGetOutOfJailCards() + 1);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean requireEstate(int playerID, int position) {
 		Player player = state.getPlayerByID(playerID);
 		Field field = state.getFieldAt(position);
 		if(trade != null && trade.getTradeState() == 0 && player != null && player.equals(trade.getActing()) && field != null && field instanceof BuyableField) {
 			return trade.addOfferedEstate((BuyableField)field);
 		}
 		return false;
 	}
 
 	@Override
 	public int[] getOfferedEstates() {
 		if(trade != null) {
 			int[] out = new int[trade.getOfferedEstates().size()];
 			int i = 0;
 			for(BuyableField field : trade.getOfferedEstates()) {
 				if(i < out.length) {
 					out[i] = field.getPosition();
 					i++;
 				}
 			}
 			return out;
 		}
 		return new int[0];
 	}
 
 	@Override
 	public int getOfferedCash() {
 		if(trade != null) {
 			return trade.getOfferedCash();
 		}
 		return -1;
 	}
 
 	@Override
 	public int getNumberOfOfferedGetOutOfJailCards() {
 		if(trade != null) {
 			return trade.getOfferedNumberOfGetOutOfJailCards();
 		}
 		return -1;
 	}
 
 	@Override
 	public int[] getRequiredEstates() {
 		if(trade != null) {
 			int[] out = new int[trade.getRequiredEstates().size()];
 			int i = 0;
 			for(BuyableField field : trade.getRequiredEstates()) {
 				if(i < out.length) {
 					out[i] = field.getPosition();
 					i++;
 				}
 			}
 			return out;
 		}
 		return new int[0];
 	}
 
 	@Override
 	public int getRequiredCash() {
 		if(trade != null) {
 			return trade.getRequiredCash();
 		}
 		return -1;
 	}
 
 	@Override
 	public int getNumberOfRequiredGetOutOfJailCards() {
 		if(trade != null) {
 			return trade.getRequiredNumberOfGetOutOfJailCards();
 		}
 		return -1;
 	}
 
 	@Override
 	public boolean cancelTrade(int playerID) {
 		ServerPlayer player = state.getPlayerByID(playerID);
 		if(trade != null && trade.getTradeState() == 0 && player != null && player.equals(trade.getActing())) {
 			trade = null;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean proposeTrade(int playerID) {
 		ServerPlayer player = state.getPlayerByID(playerID);
 		if(trade != null && trade.getTradeState() == 0 && player != null && player.equals(trade.getActing())) {
 			trade.setTradeState(1);
 			trade.getPartner().getClient().informTrade(trade.getActing().getId(), trade.getPartner().getId());
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public int getAuctionState() {
 		if(auction != null) {
 			return auction.getAuctionState();
 		}
 		return -1;
 	}
 
 	@Override
 	public int getAuctionedEstate() {
 		if(auction != null) {
 		return auction.getObjective().getPosition();
 		}
 		return 0;
 	}
 
 	@Override
 	public int getHighestBid() {
 		if(auction != null) {
 			return auction.getHighestBid();
 		}
 		return -1;
 	}
 
 	@Override
 	public int getBidder() {
 		if(auction != null) {
 			return auction.getHighestBidder().getId();
 		}
 		return -1;
 	}
 
 	@Override
 	public boolean placeBid(int playerID, int amount) {
 		Player player = state.getPlayerByID(playerID);
 		if(auction != null && player != null) {
 			return auction.placeBid(player, amount);
 		}
 		return false;
 	}
 
 	@Override
 	public int getPlayerPiecePosition(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player == null) {
 			return -1;
 		}
 		return player.getPosition();
 	}
 	
 	public void addCurrentCard(Card card) {
 		this.currentCards.add(card);
 	}
 	
 	public List<Card> getCurrentCards() {
 		return this.currentCards;
 	}
 
 	@Override
 	public synchronized int addPlayer(IClient client) {
 
 		display("Add Player!");
 
 		for (int i = 0; i < maxClients; i++) {
 			if (state.getPlayerByID(i) == null) {
 				this.clients.add(client);
 				state.setPlayer(i, new ServerPlayer(client.getName(), 0, state.getRules().startMoney, i, i, client));
 				this.connectedClients++;
 				display("Player with id:" + i + " added!");
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	@Override
 	public void setPlayerReady(int playerID) {
 
 		display("Set Player " + playerID + " ready!");
 		//AI added
 		assert (state != null);
 		state.getPlayerByID(playerID).setIsReady(true);
 		//AI added
 		logger.log(Level.INFO, "Number of connected players = " + connectedClients);
 		// Are all players Ready? then start the game
 		//AI "test" client doesn't set itself ready, ignore it
 		//if (this.connectedClients == this.maxClients) {
 		if (this.connectedClients == aiClients.length) {
 			for (int i = 0; i < connectedClients; i++) {
 
 				// If at least 1 Player is not ready, don't start the game
 				if (!state.getPlayerByID(i).getIsReady()) {
 					display("Player " + i + " is not ready!");
 					return;
 				}
 			}
 			display("Starting Game!");
 			// All Players are ready, the Game can be started now
 			startGame();
 		}
 	}
 
 	private void startGame() {
 		//AI moved logic and rules
 
 		this.display("Started a Game!");
 		this.gameStarted = true;
 
 		Field[] fields = new Field[GameState.FIELDS_AMOUNT];
 		this.loadDefaultGameStateFields(fields);
 		for (Field field : fields) {
 			this.state.setFieldAt(field, field.getPosition());
 		}
 
 		logic.startGame();
 	}
 
 	@Override
 	public String getPlayerName(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null) {
 			return player.getName();
 		}
 		return "";
 	}
 
 	@Override
 	public int getPlayerColor(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null) {
 			return player.getColor();
 		}
 		return -1;
 	}
 
 	@Override
 	public Rules getRules() {
 		if (this.state.getRules() != null) {
 			return state.getRules();
 		}
 		return null;
 	}
 
 	@Override
 	public String getEstateName(int position) {
 		Field field = state.getFieldAt(position);
 		if (field != null) {
 			return field.getName();
 		}
 		return "";
 	}
 
 	@Override
 	public int getEstateColorGroup(int position) {
 		if (position < 0 || position >= GameState.FIELDS_AMOUNT) {
 			return -1;
 		}
 		Field field = state.getFieldAt(position);
 		if (field != null && field instanceof BuyableField) {
 			return ((BuyableField) field).getFieldGroup().getColor();
 		}
 		return -1;
 	}
 
 	@Override
 	public int getEstateHouses(int position) {
 		Field field = state.getFieldByID(position);
 		if (field != null && field instanceof Street) {
 			return ((Street) field).getBuiltLevel();
 		}
 		return -1;
 	}
 
 	@Override
 	public int getEstatePrice(int position) {
 		Field field = state.getFieldByID(position);
 		if (field != null && field instanceof BuyableField) {
 			((BuyableField) field).getPrice();
 		}
 		return -1;
 	}
 
 	@Override
 	public int getEstateRent(int position, int houses) {
 		Field field = state.getFieldByID(position);
 		if (field != null && field instanceof Street) {
 			((Street) field).getRent(houses);
 		}
 		return -1;
 	}
 
 	@Override
 	public String getGameStatusMessage(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null && player instanceof ServerPlayer) {
 			return ((ServerPlayer) player).getGameStatusMessage();
 		}
 		return "";
 	}
 
 	@Override
 	public boolean isMortgaged(int position) {
 		Field field = state.getFieldByID(position);
 		if (field != null && field instanceof BuyableField) {
 			return ((BuyableField) field).isMortgaged();
 		}
 		return false;
 	}
 
 	@Override
 	public int getOwner(int position) {
 		Field field = state.getFieldByID(position);
 		if (field != null && field instanceof BuyableField) {
			Player owner = ((BuyableField) field).getOwner();
			if(owner != null) {
				return owner.getId();
			}
 		}
 		return -1;
 	}
 
 	@Override
 	public int getDiceValue() {
 		return state.getDices().getResultSum();
 	}
 
 	@Override
 	public int[] getDiceValues() {
 		return state.getDices().getResult();
 	}
 
 	@Override
 	public int getPlayerCash(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null) {
 			return player.getBalance();
 		}
 		return -1;
 	}
 
 	@Override
 	public int getPlayerOnTurn() {
 		if (state.getActivePlayer() != null) {
 			return state.getActivePlayer().getId();
 		}
 		return -1;
 	}
 
 	@Override
 	public int getNumberOfGetOutOfJailCards(int playerID) {
 		Player player = this.state.getPlayerByID(playerID);
 		if (player != null) {
 			return player.getNumberOfGetOutOfJailCards();
 		}
 		return -1;
 	}
 
 	@Override
 	public int getNumberOfHousesLeft() {
 		return state.getBank().getHouses();
 	}
 
 	@Override
 	public int getNumberOfHotelsLeft() {
 		return state.getBank().getHotels();
 	}
 
 	@Override
 	public boolean rollDice(int playerID) {
 		ServerPlayer player = this.state.getPlayerByID(playerID);
 
 		display("Starting Roll");
 
 		if (player == null || player.equals(state.getActivePlayer())
 				|| this.gameStarted == false
 				|| !this.rules.isRollRequiredByActivePlayer()) {
 			return false;
 		}
 
 		if (this.rules.isPlayerInPrison(player)) {
 
 			// Still need to wait
 			if (player.getJail().getRoundsToWait() > 0) {
 
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
 			informMoveAll(player);
 		}
 		return true;
 	}
 
 	private void informDiceAll() {
 		for (IClient client : clients) {
 			client.informDiceValues(state.getDices().getResult());
 		}
 	}
 
 	private void informMoveAll(Player player) {
 		for (IClient client : clients) {
 			client.informMove(player.getId(), player.getPosition());
 		}
 	}
 
 	@Override
 	public boolean accept(int playerID) {
 		ServerPlayer player = state.getPlayerByID(playerID);
 		
 		//Does a Trade need Confirmation?
 		if(trade != null && player != null && trade.getTradeState() == 1 && player.equals(trade.getPartner())) {
 			trade.setTradeState(3);
 			trade.executeTrade(logic);
 		}
 		
 		if(player == null || playerID != state.getActivePlayer().getId()) {
 			return false;
 		}
 		//First check if a Action needs Confirmation
 		Card card = state.getFirstWaitingCard();
 		if(card != null) {
 			card.accept();
 			state.RemoveWaitingCard(card);
 			return true;
 		} 
 		Field field = state.getFieldAt(player.getPosition());
 		if(field instanceof BuyableField && ((BuyableField)field).getOwner() == null) {
 			logic.buyStreet();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean decline(int playerID) {
 		ServerPlayer player = state.getPlayerByID(playerID);
 		if(player == null || playerID != state.getActivePlayer().getId()) {
 			return false;
 		}
 		//First check if a Action needs Confirmation
 		Card card = state.getFirstWaitingCard();
 		if(card != null) {
 			card.decline();
 			state.RemoveWaitingCard(card);
 			return true;
 		} 
 		return false;
 	}
 
 	@Override
 	public boolean endTurn(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null && rules.isPlayerOnTurn(player) && !rules.isRollRequiredByActivePlayer()) {
 
 			// Player is bankrupt
 			if (player.getBalance() < 0) {
 				this.logic.setPlayerBankrupt(player);
 			}
 
 			logic.startNewTurn();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean declareBankruptcy(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null) {
 			this.logic.setPlayerBankrupt(player);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean construct(int playerID, int position) {
 		return changeLevel(playerID, position, 1);
 	}
 
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
 	public boolean deconstruct(int playerID, int position) {
 		return changeLevel(playerID, position, -1);
 	}
 
 	@Override
 	public boolean toggleMortgage(int playerID, int position) {
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
 	public void sendMessage(String text, int sender) {
 		for (IClient client : this.clients) {
 			client.informMessage(text, sender, false);
 		}
 	}
 
 	@Override
 	public void sendPrivateMessage(String text, int sender, int reciever) {
 		if (reciever >= 0 && reciever < this.connectedClients) {
 			Player player = state.getPlayerByID(reciever);
 			if (player != null && player instanceof ServerPlayer) {
 				((ServerPlayer) player).getClient().informMessage(text, sender, true);
 			}
 		}
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public int getMaxClients() {
 		return this.maxClients;
 	}
 
 	public int getConnectedClients() {
 		return this.connectedClients;
 	}
 
 	@Override
 	public int getTurnsInPrison(int playerID) {
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
 	public boolean useGetOutOfJailCard(int playerID) {
 		Player player = state.getPlayerByID(playerID);
 		if (player != null && rules.isPlayerInPrison(player)) {
 			if (rules.canPlayerGetOutOfJail(player, true)) {
 				logic.playerUsesGetOutOfJailCard(player);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public List<IClient> getClients() {
 		return this.clients;
 	}
 
 	@Override
 	public boolean payFine(int playerID) {
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
 	public int getEstateHousePrice(int position) {
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
 	 * @return The money the player has to pay. If there is no money the return is undefined;.
 	 */
 	public int getMoneyToPay(int position) {
 		Field field = state.getFieldAt(position);
 		if (field != null && field instanceof Jail) {
 			((Jail) field).getMoneyToPay();
 		}
 		return -1;
 	}
 
 	/**
 	 * Returns the number of rounds the player has to wait if the player is in jail.
 	 * 
 	 * @param position
 	 *            The position of the jail.
 	 * @return The number of rounds the player has to wait. If this is no jail it return is undefined.
 	 */
 	public int getRoundsToWait(int position) {
 		Field field = state.getFieldAt(position);
 		if (field != null && field instanceof Jail) {
 			return ((Jail) field).getRoundsToWait();
 		}
 		return -1;
 	}
 
 	private CardField newEventCardField(int position) {
 		return new CardField("Ereigniskarte", position, this.state.getEventCards(), this.logic);
 	}
 
 	private CardField newCommunityCardField(int position) {
 		return new CardField("Gemeinschaftskarte", position, this.state.getCommunityCards(), this.logic);
 	}
 
 	private void loadDefaultGameStateFields(Field[] fields) {
 
 		// Initialise field groups
 		StationFieldGroup stations = new StationFieldGroup();
 		StreetFieldGroup[] streets = new StreetFieldGroup[8];
 		streets[0] = new StreetFieldGroup(0, 1000);
 		streets[1] = new StreetFieldGroup(1, 1000);
 		streets[2] = new StreetFieldGroup(2, 2000);
 		streets[3] = new StreetFieldGroup(3, 2000);
 		streets[4] = new StreetFieldGroup(4, 3000);
 		streets[5] = new StreetFieldGroup(5, 3000);
 		streets[6] = new StreetFieldGroup(6, 4000);
 		streets[7] = new StreetFieldGroup(7, 4000);
 		FieldGroup infrastructures = new FieldGroup(FieldGroup.INFRASTRUCTURE);
 
 		// Add Streets
 		fields[0] = new GoField("Los", 0, this.logic);
 		fields[1] = streets[0].addField(new Street("Dagobah - Sumpf", 1, new int[] { 40, 200, 600, 1800, 3200, 5000 },
 				0, 1200, logic));
 		fields[2] = this.newEventCardField(2);// new CardField("Ereigniskarte",
 												// 2,
 												// this.state.getEventCards(),
 												// this.logic);
 		fields[3] = streets[0].addField(new Street("Dagobah - Jodas Hütte", 3, new int[] { 80, 400, 1200, 3600, 6400,
 				9000 }, 0, 1200, logic));
 		fields[4] = new TaxField("Landungssteuer", 4, 4000, this.logic);
 		fields[5] = stations.addField(new Station("TIE-Fighter", 5, 4000));
 		fields[6] = streets[1].addField(new Street("Hoth - EchoBasis", 6,
 				new int[] { 120, 600, 1800, 5400, 8000, 11000 }, 0, 2000, logic));
 		fields[7] = this.newCommunityCardField(7); // new
 													// CardField("Gemeinschaftskarte",
 													// 7,
 													// this.state.getCommunityCards(),
 													// this.logic);
 		fields[8] = streets[1].addField(new Street("Hoth - EisSteppen", 8, new int[] { 120, 600, 1800, 5400, 8000,
 				11000 }, 0, 2000, logic));
 		fields[9] = streets[1].addField(new Street("Hoth - Nordgebirge", 9, new int[] { 160, 800, 2000, 6000, 9000,
 				12000 }, 0, 2400, logic));
 		fields[10] = new Jail("Gefängnis", 10, 1000, 3);
 		fields[11] = streets[2].addField(new Street("Tatooine - Lars Heimstatt", 11, new int[] { 200, 1000, 3000, 9000,
 				12500, 15000 }, 0, 2800, logic));
 		fields[12] = infrastructures.addField(new InfrastructureField("Kern-Reaktor", 12, 3000, this.logic));
 		fields[13] = streets[2].addField(new Street("Tatooine - Mos Eisley", 13, new int[] { 200, 1000, 3000, 9000,
 				12500, 15000 }, 0, 2800, logic));
 		fields[14] = streets[2].addField(new Street("Tatooine - Jabbas Palast", 14, new int[] { 240, 1200, 3600, 10000,
 				14000, 18000 }, 0, 3200, logic));
 		fields[15] = stations.addField(new Station("Millenium Falke", 15, 4000, this.logic));
 		fields[16] = streets[3].addField(new Street("Yavin 4 - Kommandozentrale", 16, new int[] { 280, 1400, 4000,
 				11000, 15000, 19000 }, 0, 3600, logic));
 		fields[17] = this.newEventCardField(17); // new
 													// CardField("Ereigniskarte",
 													// 17,
 													// this.state.getEventCards(),
 													// this.logic);
 		fields[18] = streets[3].addField(new Street("Yavin 4 - Massassi Tempel", 18, new int[] { 280, 1400, 4000,
 				11000, 15000, 19000 }, 0, 3600, logic));
 		fields[19] = streets[3].addField(new Street("Yavin 4 - TempelThronsaal", 19, new int[] { 320, 1600, 4400,
 				12000, 16000, 20000 }, 0, 4000, logic));
 		fields[20] = new FreeParking("Frei Parken", 20, this.logic);
 		fields[21] = streets[4].addField(new Street("Wolkenstadt - Andockbucht", 21, new int[] { 360, 1800, 5000,
 				14000, 17500, 21000 }, 0, 4400, logic));
 		fields[22] = this.newCommunityCardField(22); // new
 														// CardField("Gemeinschaftskarte",
 														// 22,
 														// this.state.getCommunityCards(),
 														// this.logic);
 		fields[23] = streets[4].addField(new Street("Wolkenstadt - KarbonGefrierkammer", 23, new int[] { 360, 1800,
 				5000, 14000, 17500, 21000 }, 0, 4400, logic));
 		fields[24] = streets[4].addField(new Street("Wolkenstadt - ReaktorKontrollraum", 24, new int[] { 400, 2000,
 				6000, 15000, 18500, 22000 }, 0, 4800, logic));
 		fields[25] = stations.addField(new Station("X-Wing Fighter", 25, 4000, this.logic));
 		fields[26] = streets[5].addField(new Street("Todesstern - LandeDeck", 26, new int[] { 440, 2200, 6600, 16000,
 				19500, 23000 }, 0, 5200, logic));
 		fields[27] = streets[5].addField(new Street("Todesstern - Thronsaal", 27, new int[] { 440, 2200, 6600, 16000,
 				19500, 23000 }, 0, 5200, logic));
 		fields[28] = infrastructures.addField(new InfrastructureField("Wasser-Farm", 28, 3000, this.logic));
 		fields[29] = streets[5].addField(new Street("Todesstern - Hauptreaktor", 29, new int[] { 480, 2400, 7200,
 				17000, 20500, 24000 }, 0, 5600, logic));
 		fields[30] = new GoToJail("Gehe ins Gefängnis", 30, this.logic);
 		fields[31] = streets[6].addField(new Street("Endor - Wald", 31, new int[] { 520, 2600, 7800, 18000, 22000,
 				25500 }, 0, 6000, logic));
 		fields[32] = streets[6].addField(new Street("Endor - Schildgenerator", 32, new int[] { 520, 2600, 7800, 18000,
 				22000, 25500 }, 0, 6000, logic));
 		fields[33] = this.newEventCardField(33); // new
 													// CardField("Ereigniskarte",
 													// 33,
 													// this.state.getEventCards(),
 													// this.logic);
 		fields[34] = streets[6].addField(new Street("Endor - EwokDorf", 34, new int[] { 560, 3000, 9000, 20000, 24000,
 				28000 }, 0, 6400, logic));
 		fields[35] = stations.addField(new Station("Stern-Zerstörer", 35, 4000));
 		fields[36] = this.newCommunityCardField(36); // new
 														// CardField("Gemeinschaftskarte",
 														// 36,
 														// this.state.getCommunityCards(),
 														// this.logic);
 		fields[37] = streets[7].addField(new Street("Coruscant - Platz des Volkes", 37, new int[] { 700, 3500, 10000,
 				22000, 16000, 30000 }, 0, 7000, logic));
 		fields[38] = new TaxField("Kopf-Geld Prämie", 38, 2000);
 		fields[39] = streets[7].addField(new Street("Coruscant - Imperialer Palast", 39, new int[] { 1000, 4000, 12000,
 				28000, 34000, 40000 }, 0, 8000, logic));
 
 		stations.setRent(new int[] { 500, 1000, 2000, 4000 });
 
 		// Add Cards
 	}
 
 }
