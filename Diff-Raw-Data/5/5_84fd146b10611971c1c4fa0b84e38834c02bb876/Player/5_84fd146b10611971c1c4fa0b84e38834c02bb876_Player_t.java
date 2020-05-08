 package org.jdominion;
 
 import java.io.ObjectStreamException;
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.jdominion.aiStrategies.IStrategy;
 import org.jdominion.decisions.Decision;
 import org.jdominion.effects.CardEffect;
 import org.jdominion.event.CardBought;
 import org.jdominion.event.CardDiscarded;
 import org.jdominion.event.CardGained;
 import org.jdominion.event.CardPlayFinished;
 import org.jdominion.event.CardPlayed;
 import org.jdominion.event.CardsDrawn;
 import org.jdominion.event.CardsRevealed;
 import org.jdominion.event.CardsSetAside;
 import org.jdominion.event.EventManager;
 import org.jdominion.event.GameEnded;
 import org.jdominion.location.DiscardPile;
 import org.jdominion.location.Location;
 
 public class Player implements Serializable, IPlayer {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private int id; // used by the Statistics-class to identify players over
 					// several simulated games
 	private String name;
 	private Hand hand;
 	private Game game;
 	private Deck deck;
 	private CardList discardPile;
 	private CardList cardsSetAside;
 	private CardList cardsInPlay;
 	private int turnCounter = 0;
 
 	private final IStrategy strategy;
 
 	// used by SerializedPlayer
 	protected Player(String name) {
 		this.name = name;
 		strategy = null;
 	}
 
 	public Player(String name, Deck deck, IStrategy strategy) {
 		this.name = name;
 		this.deck = deck;
 		for (Card cardInDeck : deck) {
 			cardInDeck.setOwner(this);
 		}
 		this.strategy = strategy;
 		this.strategy.setPlayer(this);
 		discardPile = new CardList();
 		this.cardsInPlay = new CardList();
 		this.hand = new Hand();
 		this.cardsSetAside = new CardList();
 		drawNewHand();
 	}
 
 	public Player(int id, String name, Deck deck, IStrategy strategy) {
 		this(name, deck, strategy);
 		this.id = id;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	public Hand getHand() {
 		return hand;
 	}
 
 	@Override
 	public int getHandSize() {
 		return hand.size();
 	}
 
 	@Override
 	public int getDeckSize() {
 		return deck.size();
 	}
 
 	@Override
 	public int getDiscardPileSize() {
 		return discardPile.size();
 	}
 
 	public CardList getCardsSetAside() {
 		return cardsSetAside;
 	}
 
 	public void incrementTurnCounter() {
 		turnCounter++;
 	}
 
 	public int getTurnCounter() {
 		return turnCounter;
 	}
 
 	public boolean hasActionCardInHand() {
 		return hand.contains(Card.Type.ACTION);
 	}
 
 	public CardList getCardsFromDeck(int numberOfCardsToGet) {
 		return drawCards(numberOfCardsToGet);
 	}
 
 	public Card revealCard() {
 		return revealCards(1).getFirst();
 	}
 
 	public CardList revealCards(int numberOfCardsToReveal) {
 		CardList cardsToReveal = drawCards(numberOfCardsToReveal);
 		EventManager.getInstance().handleEvent(new CardsRevealed(this, cardsToReveal));
 		return cardsToReveal;
 	}
 
 	public void revealCardFromHand(Card card) {
 		CardList cardList = new CardList();
 		cardList.add(card);
 		revealCardsFromHand(cardList);
 	}
 
 	public void revealCardsFromHand(CardList cardsToReveal) {
 		for (Card card : cardsToReveal) {
 			assert hand.contains(card);
 		}
 		EventManager.getInstance().handleEvent(new CardsRevealed(this, cardsToReveal));
 	}
 
 	public void revealHand() {
 		EventManager.getInstance().handleEvent(new CardsRevealed(this, hand));
 	}
 
 	public void drawCardsIntoHand(int numberOfCardsToDraw) {
 		CardList drawnCards = drawCards(numberOfCardsToDraw);
 		addCardsToHand(drawnCards);
 		EventManager.getInstance().handleEvent(new CardsDrawn(this, drawnCards.size()));
 	}
 
 	private CardList drawCards(int numberOfCardsToDraw) {
 		CardList cards = new CardList();
 		for (int i = 0; i < numberOfCardsToDraw; i++) {
 			Card card = drawCard();
 			if (card != null) {
 				cards.add(card);
 			}
 		}
 		return cards;
 	}
 
 	private Card drawCard() {
 		if (deck.size() == 0) {
 			shuffleDeck();
 		}
 		return deck.getTopCard();
 	}
 
 	public void drawNewHand() {
 		assert hand.size() == 0;
 		drawCardsIntoHand(Game.NUMBER_OF_CARDS_IN_HAND);
 	}
 
 	public void addCardToHand(Card card) {
 		CardList cards = new CardList();
 		cards.add(card);
 		addCardsToHand(cards);
 	}
 
 	// TODO: make an event for this
 	public void addCardsToHand(CardList cards) {
 		removeCardsFromOtherPlaces(cards);
 		hand.addAll(cards);
 		for (Card card : cards) {
 			card.isAddedToHand(this);
 		}
 	}
 
 	public void removeCardFromHand(Card card) {
 		hand.remove(card);
 		card.getsRemovedFromHand(this);
 	}
 
 	private void shuffleDeck() {
 
 		assert deck.isEmpty();
 		deck.addAll(discardPile);
 		deck.shuffle();
 		discardPile = new CardList();
 
 	}
 
 	public void discardCardsFromHand(CardList cardsToDiscard, Turn currentTurn, Supply supply) {
 		for (Card card : cardsToDiscard) {
 			assert hand.contains(card);
 			removeCardFromHand(card);
 			placeOnDiscardPile(card);
 			EventManager.getInstance().handleEvent(new CardDiscarded(this, card, currentTurn, supply));
 		}
 	}
 
 	public void placeOnDiscardPile(Card card) {
 		if (cardsInPlay.contains(card)) {
 			cardsInPlay.remove(card);
 		}
 		discardPile.add(card);
 	}
 
 	public void placeOnDiscardPile(CardList cards) {
 		for (Card card : new CardList(cards)) {
 			placeOnDiscardPile(card);
 		}
 	}
 
 	public void placeOnDeck(Card card) {
 		if (hand.contains(card)) {
 			removeCardFromHand(card);
 		}
 		deck.putOnTop(card);
 	}
 
 	// TODO: use this in more places
 	private void removeCardsFromOtherPlaces(CardList cards) {
 		for (Card card : cards) {
 			if (this.cardsSetAside.contains(card)) {
 				this.cardsSetAside.remove(card);
 			}
 			if (this.hand.contains(card)) {
 				this.removeCardFromHand(card);
 			}
 			if (this.discardPile.contains(card)) {
 				this.discardPile.remove(card);
 			}
 			if (this.deck.contains(card)) {
 				this.deck.remove(card);
 			}
 		}
 	}
 
 	public void trashCard(Card cardToTrash, Game game) {
		// already trashed
		if (game.getTrash().contains(cardToTrash)) {
			return;
		}

 		// TODO: make this more general to trash a card wherever it might be
 		// Cards should know their location for this
 		if (hand.contains(cardToTrash)) {
 			removeCardFromHand(cardToTrash);
 		}
 		if (getCardsInPlay().contains(cardToTrash)) {
 			getCardsInPlay().remove(cardToTrash);
 		}
 		game.getTrash().trashCard(cardToTrash, game.getCurrentTurn(), game.getSupply());
 	}
 
 	public void trashCards(CardList cardsToTrash, Game game) {
 		for (Card cardToTrash : cardsToTrash) {
 			this.trashCard(cardToTrash, game);
 		}
 	}
 
 	public void playCard(Card card, Turn currentTurn, Supply supply) {
 		if (hand.contains(card)) {
 			removeCardFromHand(card);
 		}
 		EventManager.getInstance().handleEvent(new CardPlayed(this, card, currentTurn, supply));
 		card.play(this, currentTurn, supply);
 		EventManager.getInstance().handleEvent(new CardPlayFinished(this, card, currentTurn, supply));
 	}
 
 	public void buyCard(Class<? extends Card> cardToBuy, Turn currentTurn, Supply supply) {
 		Card boughtCard = supply.takeCard(cardToBuy);
 		EventManager.getInstance().handleEvent(new CardBought(this, boughtCard, currentTurn, supply));
 		this.gainCard(boughtCard, currentTurn, supply);
 	}
 
 	public void gainCard(Class<? extends Card> card, Supply supply, Turn currentTurn) {
 		gainCard(card, supply, new DiscardPile(), currentTurn);
 	}
 
 	public void gainCard(Class<? extends Card> card, Supply supply, Location whereToPlaceCard, Turn currentTurn) {
 		if (supply.isCardAvailable(card)) {
 			gainCard(supply.takeCard(card), whereToPlaceCard, currentTurn, supply);
 		}
 	}
 
 	public void gainCard(Card gainedCard, Turn currentTurn, Supply supply) {
 		this.gainCard(gainedCard, new DiscardPile(), currentTurn, supply);
 	}
 
 	public void gainCard(Card gainedCard, Location whereToPlaceCard, Turn currentTurn, Supply supply) {
 		gainedCard.setOwner(this);
 		whereToPlaceCard.putCard(this, gainedCard);
 		EventManager.getInstance().handleEvent(new CardGained(this, gainedCard, currentTurn, supply));
 	}
 
 	public void setCardAside(Card card) {
 		EventManager.getInstance().handleEvent(new CardsSetAside(this, new CardList(card)));
 		this.cardsSetAside.add(card);
 		removeCardsFromOtherPlaces(new CardList(card));
 	}
 
 	private CardList getListOfAllCards(Turn currentTurn) {
 		CardList list = new CardList();
 		list.addAll(deck);
 		list.addAll(hand);
 		list.addAll(discardPile);
 		list.addAll(cardsSetAside);
 		if (currentTurn != null) {
 			list.addAll(getCardsInPlay());
 		}
 		return list;
 	}
 
 	public int countVictoryPoints(Turn currentTurn) {
 		int points = 0;
 		for (Card card : getListOfAllCards(currentTurn)) {
 			points += card.getVictoryPoints(this, getListOfAllCards(currentTurn));
 		}
 		return points;
 	}
 
 	public int countCoins(Turn currentTurn) {
 		int coins = 0;
 		for (Card card : getListOfAllCards(currentTurn)) {
 			coins += card.getCoins();
 		}
 
 		return coins;
 	}
 
 	public int countCards(Turn currentTurn) {
 		return getListOfAllCards(currentTurn).size();
 	}
 
 	public int countCardsOfClass(Class<? extends Card> cardClass, Turn currentTurn) {
 		int counter = 0;
 		for (Card card : getListOfAllCards(currentTurn)) {
 			if (card.getClass() == cardClass) {
 				counter++;
 			}
 		}
 		return counter;
 	}
 
 	public List<Class<? extends Card>> getNeededCards() {
 		return strategy.getNeededCards();
 	}
 
 	public void gameStarted(Game game) {
 		this.game = game;
 		strategy.gameStarted(game);
 	}
 
 	public void gameEnded(List<Player> winners, List<Player> players) {
 		EventManager.getInstance().handleEvent(new GameEnded(winners, players));
 	}
 
 	public void decide(Decision<?> decision, CardEffect effect) {
 		callCorrectDecisionMethod(decision, effect, hand, game.getCurrentTurn(), game.getSupply(), strategy);
 
 		// TODO: maybe move it somewhere else
 		if (!decision.isAnswered()) {
 			decision.chooseDefaultAnswer(hand, game.getCurrentTurn(), game.getSupply());
 		}
 	}
 
 	public static void callCorrectDecisionMethod(Decision<?> decision, CardEffect effect, Hand hand, Turn currentTurn, Supply supply, IStrategy strategy) {
 		// find the right decision method in the strategy to call
 		// TODO: improve this code or find a better way to solve this problem
 		Class<? extends Object> decisionClass = decision.getClass();
 		while (decisionClass != Object.class) {
 			Class<? extends Object> effectClass = effect.getClass();
 			while (effectClass != Object.class) {
 				try {
 					Method decideMethode = strategy.getClass().getMethod("decide", decisionClass, effectClass, Hand.class, Turn.class, Supply.class);
 					decideMethode.invoke(strategy, decision, effect, hand, currentTurn, supply);
 					break;
 				} catch (NoSuchMethodException e) {
 				} catch (IllegalAccessException e) {
 					throw new RuntimeException(e);
 				} catch (InvocationTargetException e) {
 					throw new RuntimeException(e);
 				}
 				effectClass = effectClass.getSuperclass();
 			}
 			decisionClass = decisionClass.getSuperclass();
 		}
 	}
 
 	private Object writeReplace() throws ObjectStreamException {
 		return new SerializedPlayer(name, getHandSize(), getDeckSize(), getDiscardPileSize());
 	}
 
 	public CardList getCardsInPlay() {
 		return cardsInPlay;
 	}
 
 }
