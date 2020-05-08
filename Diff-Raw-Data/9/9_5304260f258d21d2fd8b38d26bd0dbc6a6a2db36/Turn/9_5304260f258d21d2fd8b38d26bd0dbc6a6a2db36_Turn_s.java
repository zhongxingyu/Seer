 package org.jdominion;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdominion.Card.Type;
 import org.jdominion.decisions.ChooseActionCardToPlay;
 import org.jdominion.decisions.ChooseCardToBuy;
 import org.jdominion.decisions.ChooseTreasureCardToPlay;
 import org.jdominion.effects.CardEffectTreasure;
 import org.jdominion.effects.NullEffect;
 import org.jdominion.event.EndOfTurn;
 import org.jdominion.event.EventManager;
 import org.jdominion.event.StartOfTurn;
 
 public class Turn implements Serializable {
 
 	public enum Phase {
 		Action, Buy, Cleanup
 	}
 	private static final long serialVersionUID = 1L;
 	private int turnNumber;
 	private int availableActions = 1;
 	private int extraMoney = 0;
 	private int availableBuys = 1;
 	private Phase currentPhase;
 
 	private Player activePlayer;
 	private Game game;
 
 	private CardList playedCards = new CardList(true);
 
 	public int getTurnNumber() {
 		return turnNumber;
 	}
 
 	public void addActions(int value) {
 		assert value > 0;
 		this.availableActions += value;
 	}
 
 	public int getAvailableActions() {
 		return availableActions;
 	}
 
 	public void addExtraMoney(int value) {
		assert value > 0;
 		this.extraMoney += value;
 	}
 
 	public int getExtraMoney() {
 		return extraMoney;
 	}
 
 	public void setExtraMoney(int extraMoney) {
 		this.extraMoney = extraMoney;
 	}
 
 	public void addBuys(int value) {
 		assert value > 0;
 		availableBuys += value;
 	}
 
 	public int getAvailableBuys() {
 		return availableBuys;
 	}
 
 	public Phase getCurrentPhase() {
 		return currentPhase;
 	}
 
 	public void setActivePlayer(Player activePlayer) {
 		this.activePlayer = activePlayer;
 	}
 
 	public Player getActivePlayer() {
 		return activePlayer;
 	}
 
 	private void setGame(Game game) {
 		this.game = game;
 	}
 
 	public Game getGame() {
 		return game;
 	}
 
 	/**
 	 * Returns the list of cards which have been played this turn. This List may contain duplicated elements if a card
 	 * was played more than once (e.g. due to throne room)
 	 * 
 	 * @return CardList
 	 */
 	public CardList getPlayedCards() {
 		return playedCards;
 	}
 
 	public Turn(Game game, Player activePlayer, int turnNumber) {
 		this.setGame(game);
 		this.setActivePlayer(activePlayer);
 		this.turnNumber = turnNumber;
 	}
 
 	public void doTurn() {
 		this.currentPhase = Phase.Action;
 		EventManager.getInstance().handleEvent(new StartOfTurn(this));
 		playActionCards(activePlayer, game.getSupply());
 		this.currentPhase = Phase.Buy;
 		playTreasureCards(activePlayer, game.getSupply());
 		buyCards(activePlayer, game.getSupply());
 		this.currentPhase = Phase.Cleanup;
 		cleanUp(activePlayer);
 		EventManager.getInstance().handleEvent(new EndOfTurn(this));
 	}
 
 	public List<Player> getOtherPlayers() {
 		List<Player> otherPlayers = new ArrayList<Player>(getGame().getPlayers());
 		otherPlayers.remove(getActivePlayer());
 
 		return otherPlayers;
 	}
 
 	public Player getPrevPlayer() {
 		int indexOfPrevPlayer = game.getPlayers().indexOf(getActivePlayer()) - 1;
 		if (indexOfPrevPlayer < 0) {
 			indexOfPrevPlayer = game.getPlayers().size() - 1;
 		}
 		return game.getPlayers().get(indexOfPrevPlayer);
 	}
 
 	public Player getNextPlayer() {
 		int indexOfNextPlayer = (game.getPlayers().indexOf(getActivePlayer()) + 1) % game.getPlayers().size();
 		return game.getPlayers().get(indexOfNextPlayer);
 	}
 
 	private void playActionCards(Player activePlayer, Supply supply) {
 		while (activePlayer.hasActionCardInHand() && (this.availableActions > 0)) {
 			ChooseActionCardToPlay playDecision = new ChooseActionCardToPlay(activePlayer.getHand());
 			activePlayer.decide(playDecision, new NullEffect());
 			if (playDecision.isCanceled()) {
 				return;
 			}
 			Card choosenCard = playDecision.getAnswer().getFirst();
 			assert choosenCard.isOfType(Card.Type.ACTION);
 			this.availableActions--;
 			playCard(activePlayer, supply, choosenCard);
 		}
 	}
 
 	public void playCard(Player activePlayer, Supply supply, Card cardToPlay) {
 		// the card could be already in the play Area, because it was played twice by throne room
 		if (!activePlayer.getCardsInPlay().contains(cardToPlay)) {
 			// also check if the card was played before, because maybe it removed itself from the play area
 			// (e.g. a throne roomed feast)
 			if (!this.playedCards.contains(cardToPlay)) {
 				activePlayer.getCardsInPlay().add(cardToPlay);
 			}
 		}
 
 		this.playedCards.add(cardToPlay);
 
 		activePlayer.playCard(cardToPlay, this, supply);
 	}
 
 	private void playTreasureCards(Player activePlayer, Supply supply) {
 		playBasicTreasureCards(activePlayer, supply);
 		playOtherTreasureCards(activePlayer, supply);
 	}
 
 	private void playBasicTreasureCards(Player activePlayer, Supply supply) {
 		for (Card cardInHand : new CardList(activePlayer.getHand())) {
 			if (isBasicTreasureCard(cardInHand)) {
 				playCard(activePlayer, supply, cardInHand);
 			}
 		}
 	}
 
 	private boolean isBasicTreasureCard(Card card) {
 		return card.isOfType(Type.TREASURE) && card.getEffects().size() == 1 && card.getEffects().get(0).getClass() == CardEffectTreasure.class;
 	}
 
 	private void playOtherTreasureCards(Player activePlayer, Supply supply) {
 		while (activePlayer.getHand().contains(Type.TREASURE)) {
 			ChooseTreasureCardToPlay playDecision = new ChooseTreasureCardToPlay(activePlayer.getHand());
 			activePlayer.decide(playDecision, new NullEffect());
 			if (playDecision.isCanceled()) {
 				return;
 			}
 			Card choosenCard = playDecision.getAnswer().getFirst();
 			assert choosenCard.isOfType(Card.Type.TREASURE);
 			playCard(activePlayer, supply, choosenCard);
 		}
 	}
 
 	private void buyCards(Player activePlayer, Supply supply) {
 		int availableCoins = this.extraMoney;
 		while (this.availableBuys > 0) {
 
 			ChooseCardToBuy buyDecision = new ChooseCardToBuy(supply.createSupplyWithMaximumCost(availableCoins), availableBuys, availableCoins);
 			activePlayer.decide(buyDecision, new NullEffect());
 
 			if (buyDecision.isCanceled()) {
 				return;
 			}
 
 			Class<? extends Card> cardToBuy = buyDecision.getAnswer();
 
 			int cost = supply.getCardCost(cardToBuy);
 
 			assert cost <= availableCoins;
 			assert supply.isCardAvailable(cardToBuy);
 
 			activePlayer.buyCard(cardToBuy, this, supply);
 
 			availableCoins -= cost;
 			this.availableBuys--;
 		}
 	}
 
 	private void cleanUp(Player activePlayer) {
 		activePlayer.placeOnDiscardPile(activePlayer.getCardsInPlay());
 		activePlayer.discardCardsFromHand(activePlayer.getHand(), this, getGame().getSupply());
 		activePlayer.drawNewHand();
 	}
 
 }
