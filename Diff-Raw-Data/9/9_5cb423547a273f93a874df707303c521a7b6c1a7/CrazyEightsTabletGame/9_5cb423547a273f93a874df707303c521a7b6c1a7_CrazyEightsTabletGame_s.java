 package com.worthwhilegames.cardgames.crazyeights;
 
 import static com.worthwhilegames.cardgames.crazyeights.C8Constants.NUMBER_OF_CARDS_PER_HAND;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import android.util.Log;
 
 import com.worthwhilegames.cardgames.R;
 import com.worthwhilegames.cardgames.shared.Card;
 import com.worthwhilegames.cardgames.shared.CardGame;
 import com.worthwhilegames.cardgames.shared.Constants;
 import com.worthwhilegames.cardgames.shared.Deck;
 import com.worthwhilegames.cardgames.shared.Game;
 import com.worthwhilegames.cardgames.shared.Player;
 import com.worthwhilegames.cardgames.shared.Util;
 
 /**
  * A class for keeping track of the logic and game state for the game type crazy eights
  */
 public class CrazyEightsTabletGame implements Game {
 
 	/**
 	 * A tag for the class name
 	 */
 	private static final String TAG = CrazyEightsTabletGame.class.getName();
 
 	/**
 	 * A variable for and instance of the crazy eights game type
 	 */
 	private static CrazyEightsTabletGame instance = null;
 
 	/**
 	 * A private variable for a list of players in the current game
 	 */
 	private List<Player> players;
 
 	/**
 	 * A private variable representing the game deck for the crazy eights game
 	 */
 	private Deck gameDeck;
 
 	/**
 	 * A private variable to represent the difficulty of computers in the current game
 	 */
 	private String computerDifficulty = Constants.EASY;
 
 	/**
 	 * An iterator for removing cards from the shuffled deck
 	 */
 	private Iterator<Card> iter;
 
 	/**
 	 * A list of all the cards in the shuffle deck
 	 */
 	private List<Card> shuffledDeck;
 
 	/**
 	 * A list of all the cards in the discard pile
 	 */
 	private List<Card> discardPile;
 
 	/**
 	 * Represents whether the game is currently active
 	 */
 	private boolean gameActive = false;
 
 	/**
 	 * Create a new instance of the tablet game so that multiple classes are able to reference
 	 * the same card game and only one instance will be made available. This method uses the default
 	 * constructor.
 	 * 
 	 * @return an instance of CrazyEightsTabletGame
 	 */
 	public static CrazyEightsTabletGame getInstance() {
 		if (instance == null) {
 			instance = new CrazyEightsTabletGame();
 		}
 
 		return instance;
 	}
 
 	/**
 	 * Clear the game instance
 	 */
 	public static void clearInstance() {
 		instance = null;
 	}
 
 	/**
 	 * A constructor for the crazy eights game type. This constructor will initialize the all the variables
 	 * for a game of crazy eights including the rules, players, deck, shuffled deck pile and the discard pile.
 	 */
 	private CrazyEightsTabletGame() {
 		players = new ArrayList<Player>();
 		gameDeck = new Deck(CardGame.CrazyEights);
 		shuffledDeck = gameDeck.getCardIDs();
 		discardPile = new ArrayList<Card>();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#getPlayers()
 	 */
 	@Override
 	public List<Player> getPlayers() {
 		return players;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#getShuffledDeck()
 	 */
 	@Override
 	public List<Card> getShuffledDeck() {
 		return shuffledDeck;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#setComputerDifficulty(java.lang.String)
 	 */
 	@Override
 	public void setComputerDifficulty(String diff){
 		this.computerDifficulty = diff;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#getComputerDifficulty()
 	 */
 	@Override
 	public String getComputerDifficulty() {
 		return this.computerDifficulty;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#setup()
 	 */
 	@Override
 	public void setup() {
 		// Shuffle the card ID's
 		this.shuffleDeck();
 
 		// Deal the initial cards to all the players in the game
 		this.deal();
 
 		// Discard pile first one
 		discardPile.add(iter.next());
 
 		// Remove the last card returned by iter.next()
 		iter.remove();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#shuffleDeck()
 	 */
 	@Override
 	public void shuffleDeck() {
 		//create a random number generator
 		Random generator = new Random();
 
 		//shuffle the deck
 		Collections.shuffle(shuffledDeck, generator);
 
 		//set the iterator to go through the shuffled deck
 		iter = shuffledDeck.iterator();
 	}
 
 	/**
 	 * This method will shuffle the discard pile and replace the current draw
 	 * pile with the old discard pile. After this method call the shuffled deck
 	 * will only have the top card remaining.
 	 */
 	public void shuffleDiscardPile() {
 		Card card = discardPile.remove(discardPile.size() - 1);
 
 		// Make copy of discard pile to be new shuffled deck
 		// add to the shuffled deck in case there are still
 		// some cards left that are unaccounted for
 		shuffledDeck.addAll(discardPile);
 
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "shuffleDiscardPile: shuffledDeck: " + shuffledDeck.size() + " - discardPile: " + discardPile.size());
 		}
 
 		// Remove all the cards from the discard pile
 		discardPile.removeAll(discardPile);
 
 		// Place the last card discarded back on the discard pile
 		discardPile.add(card);
 
 		// Shuffle the deck
 		this.shuffleDeck();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#deal()
 	 */
 	@Override
 	public void deal() {
 		gameActive = true;
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "deal: numberOfPlayers: " + players.size());
 
 			for (Player p : players) {
 				Log.d(TAG, "pre deal: player[" + p.getId() + "] has " + p.getNumCards() + " cards");
 				Log.d(TAG, "          player[" + p.getId() + "]: " + p);
 			}
 		}
 
 		// Count the number of human players
 		for (Player p : players) {
 			if (!p.getIsComputer()) {
 			}
 		}
 
 		// Deal the given number of cards to each player
 		for (int i = 0; i < NUMBER_OF_CARDS_PER_HAND; i++) {
 			for (Player p : players) {
 				// give them a card
 				p.addCard(iter.next());
 
 				if (Util.isDebugBuild()) {
 					Log.d(TAG, "p.addCard: player[" + p.getId() + "] has " + p.getNumCards() + " cards");
 				}
 
 				//remove the last card returned by iter.next()
 				iter.remove();
 			}
 		}
 
 		if (Util.isDebugBuild()) {
 			for (Player p : players) {
 				Log.d(TAG, "postdeal: player[" + p.getId() + "] has " + p.getNumCards() + " cards");
 				Log.d(TAG, "          player[" + p.getId() + "]: " + p);
 			}
 		}
 	}
 
 	/**
 	 * Get the discard pile
 	 * 
 	 * @return the discard pile
 	 */
 	public List<Card> getDiscardPile() {
 		return discardPile;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#discard(com.worthwhilegames.cardgames.shared.Player, com.worthwhilegames.cardgames.shared.Card)
 	 */
 	@Override
 	public void discard(Player player, Card card) {
 		discardPile.add(card);
 		player.removeCard(card);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#isGameOver(com.worthwhilegames.cardgames.shared.Player)
 	 */
 	@Override
 	public boolean isGameOver(Player player) {
 		//check to see if the player has any cards left
 		if (player.getNumCards() == 0) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#draw(com.worthwhilegames.cardgames.shared.Player)
 	 */
 	@Override
 	public Card draw(Player player) {
 		if (!iter.hasNext()) {
 			this.shuffleDiscardPile();
 		}
 
 		if (!iter.hasNext()) {
 			return null;
 		}
 
 		// Get a card out of the shuffled pile and add to the players hand
 		Card card = iter.next();
 		player.addCard(card);
 
 		// Remove the last card returned by iter.next()
 		iter.remove();
 
 		// Shuffle the deck if the player drew the last card
 		if (shuffledDeck.isEmpty()) {
 			shuffleDiscardPile();
 		}
 
 		return card;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#dropPlayer(java.lang.String)
 	 */
 	@Override
 	public void dropPlayer(String playerMacAddress) {
 		if (Util.isDebugBuild()) {
 			Log.d(TAG, "dropPlayer: " + playerMacAddress);
 		}
 
 		Player p = null;
 
 		for (Player player : players) {
 			if (player.getId().equals(playerMacAddress)) {
 				p = player;
 				break;
 			}
 		}
 
 		if (gameActive) {
 			if (p != null) {
 				p.setIsComputer(true);
 				p.setComputerDifficulty(computerDifficulty);
 			} else {
 				if (Util.isDebugBuild()) {
 					Log.d(TAG, "dropPlayer: couldn't find player with id: " + playerMacAddress);
 				}
 			}
 		} else {
 			// If the game hasn't been started yet, just remove them from the list
 			players.remove(p);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#getDiscardPileTop()
 	 */
 	@Override
 	public Card getDiscardPileTop() {
 		return discardPile.get(discardPile.size() - 1);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#getNumPlayers()
 	 */
 	@Override
 	public int getNumPlayers() {
 		return players.size();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#getMaxNumPlayers()
 	 */
 	@Override
 	public int getMaxNumPlayers() {
		if (players.isEmpty()) {
 			return C8Constants.MAX_NUM_PLAYERS;
 		} else {
 			int count = 0;
 			for (Player p : players) {
 				if (p.isDisconnected()) {
 					count++;
 				}
 			}
 
 			return players.size() - count;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#addPlayer(com.worthwhilegames.cardgames.shared.Player)
 	 */
 	@Override
 	public void addPlayer(Player p) {
 		players.add(p);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.worthwhilegames.cardgames.shared.Game#isActive()
 	 */
 	@Override
 	public boolean isActive() {
 		return gameActive;
 	}
 
 	@Override
 	public Card getCardAtPosition(int position) {
 		if( position == 2 ){
 			return this.getDiscardPileTop();
 		} else if ( position == 4 ) {
 			//TODO customizable back
 			return new Card(0,0,R.drawable.back_blue_1, 0);
 		}
 		return null;
 	}
 }
