 package poker.arturka;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import message.data.Card;
 import message.data.Player;
 import message.data.Card.Rank;
 import message.data.Card.Suit;
 
 public class HandEvaluator {
 
 	private static final int CARDS_TO_EVALUATE = 5;
 	private static final int FOUR_OF_A_KIND_COUNT = 4;
 	private static final int THREE_OF_A_KIND_COUNT = 3;
 	private static final int TWO_PAIR_COUNT = 2;
 	private static final int ONE_PAIR_COUNT = 2;
 	private static final int CARD_COUNT = 7;
 
 	/**
 	 * Creates an instance of an HandEvaluator object.
 	 * 
 	 * @param playersLeft
 	 *            Players that are currently in game and whose hands need to be
 	 *            evaluated.
 	 * @param tableCards
 	 *            Additional player cards that are on the table.
 	 */
 	public HandEvaluator(List<Player> playersLeft, List<Card> tableCards) {
 		playersToEvaluate = playersLeft;
 		evaluatedPlayers = new HashMap<Player, PlayerHand>();
 		this.tableCards = tableCards;
 	}
 
 	/**
 	 * Gets List<PlayerHand> of evaluated players in ranking order starting with
 	 * 0. If more then one player has the same getPosition() value, then the pot
 	 * should be split between them.
 	 * 
 	 * @return Returns the evaluated and sorted List<PlayerHand>.
 	 */
 	public List<PlayerHand> getPlayerHandEvaluation() {
 		List<PlayerHand> playerRanking = new ArrayList<PlayerHand>();
 		for (Player player : playersToEvaluate) {
 			playerHand = new PlayerHand(player);
 			getPlayerHand(player);
 			evaluatedPlayers.put(player, playerHand);
 		}
 		playerRanking = sortEvaluatedPlayers();
 		playerRanking = sortPlayerPositions(playerRanking);
 		return playerRanking;
 	}
 
 	/**
 	 * Sorts List<PlayerHand> list by player hand positions. Then assigns
 	 * sequential player hand ranks to position variable.
 	 * 
 	 * @param playerRanking
 	 *            List of ranked players.
 	 * @return List of sorted player hand list.
 	 */
 	private List<PlayerHand> sortPlayerPositions(List<PlayerHand> playerRanking) {
 		for (int i = 0; i < playerRanking.size() - 1; i++) {
 			if (playerRanking.get(i).getPosition() > playerRanking.get(i + 1)
 					.getPosition()) {
 				playerRanking.set(i,
 						playerRanking.set(i + 1, playerRanking.get(i)));
 				i--;
 			}
 		}
 		int position = 0;
 		for (int i = 0; i < playerRanking.size() - 1; i++) {
 			if (playerRanking.get(i).getPosition() != playerRanking.get(i + 1)
 					.getPosition())
 				playerRanking.get(i).setPosition(position++);
 			else
 				playerRanking.get(i).setPosition(position);
 			if (i + 1 == playerRanking.size() - 1)
 				playerRanking.get(i + 1).setPosition(position);
 		}
 		return playerRanking;
 	}
 
 	/**
 	 * Creates a two dimension Card array, marking those fields that the player
 	 * has. This approach seems to be more resource efficient.
 	 * 
 	 * @param hand
 	 *            Current player hand cards.
 	 * @return Table of current user hand.
 	 */
 	private Card[][] createHandTable(Card[] hand) {
 		Card[][] tempHand = new Card[Suit.values().length][Rank.values().length];
 		for (Card card : hand) {
 			tempHand[card.getSuit().ordinal()][card.getRank().ordinal()] = card;
 		}
 		return tempHand;
 	}
 
 	/**
 	 * At first assigns hand ordinal to PlayerHand.position variables. Then
 	 * sorts the player hand list by player hard ordinal so that the strongest
 	 * cards are closer to the start of list. Then compares hands of the same
 	 * kind depending on kicker, highCard, handScore.
 	 * 
 	 * @return List of player hands with updated positions comparing to other
 	 *         hands.
 	 */
 	private List<PlayerHand> sortEvaluatedPlayers() {
 		List<PlayerHand> playerPositions = new ArrayList<PlayerHand>();
 		// Adds hands to a position list.
 		for (Entry<Player, PlayerHand> entry : evaluatedPlayers.entrySet()) {
 			entry.getValue().setPosition(
 					entry.getValue().getHand().ordinal() + 1);
 			playerPositions.add(entry.getValue());
 		}
 		for (int i = 0; i < playerPositions.size() - 1; i++) {
 			if (playerPositions.get(i).getHand().ordinal() > playerPositions
 					.get(i + 1).getHand().ordinal())
 				playerPositions.set(i,
 						playerPositions.set(i + 1, playerPositions.get(i)));
 		}
 		// Sorts position list hands if they are the same
 		PlayerHand prev = null;
 		PlayerHand entry = null;
 		for (int i = 0; i < playerPositions.size(); i++) {
 			entry = playerPositions.get(i);
 			if (prev != null) {
 				prev = playerPositions.get(i - 1);
 				if (prev.getHand() == entry.getHand()) {
 					if (entry.getHand().equals(Hand.HIGH_HAND)) {
 						if (prev.getHighCard().getRank().ordinal() < entry
 								.getHighCard().getRank().ordinal()) {
 							incrementFollowingPlayers(playerPositions, entry);
 							i--;
 						} else if (prev.getHighCard().getRank().ordinal() > entry
 								.getHighCard().getRank().ordinal()) {
 							incrementFollowingPlayers(playerPositions, prev);
 						}
 					} else if (entry.getHand().equals(Hand.STRAIGHT_FLUSH)
 							|| entry.getHand().equals(Hand.FULL_HOUSE)
 							|| entry.getHand().equals(Hand.FLUSH)
 							|| entry.getHand().equals(Hand.STRAIGHT)) {
 						if (prev.getHandScore() < entry.getHandScore()) {
 							incrementFollowingPlayers(playerPositions, entry);
 							playerPositions.set(
 									i - 1,
 									playerPositions.set(i,
 											playerPositions.get(i - 1)));
 							i--;
 						} else if (prev.getHandScore() > entry.getHandScore()) {
 							incrementFollowingPlayers(playerPositions, prev);
 						}
 					} else if (entry.getHand().equals(Hand.ROYAL_FLUSH)) {
 						// do nothing, because it is the strongest hand
 						// empty so that shouldn't write many evaluations in
 						// next statement :)
 					} else {
 						if (prev.getKicker() != null
 								&& entry.getKicker() != null) {
 							if (prev.getKicker().getRank().ordinal() < entry
 									.getKicker().getRank().ordinal()) {
 								incrementFollowingPlayers(playerPositions,
 										entry);
 								i--;
 							} else if (prev.getKicker().getRank().ordinal() > entry
 									.getKicker().getRank().ordinal()) {
 								incrementFollowingPlayers(playerPositions, prev);
 							}
 						}
 					}
 				}
 			} else {
 				prev = entry;
 			}
 		}
 		return playerPositions;
 	}
 
 	/**
 	 * After comparison of two similar hands, one hand remains at the same
 	 * position but all other player hand positions that follow, need to be
 	 * incremented by 1.
 	 * 
 	 * @param playerPositions
 	 *            List of current player position after hands.
 	 * @param entry
 	 *            Currently compared PlayerHand object, this object position
 	 *            needs to stay the same as before.
 	 */
 	private void incrementFollowingPlayers(List<PlayerHand> playerPositions,
 			PlayerHand entry) {
 		for (PlayerHand playerHand : playerPositions) {
 			if (playerHand.getHand().ordinal() >= entry.getHand().ordinal()) {
 				if (!playerHand.equals(entry)) {
 					playerHand.setPosition(playerHand.getPosition() + 1);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets the specified player hand and adds table cards to it.
 	 * 
 	 * @param player
 	 *            Player that holds the needed hand.
 	 * @return Hand of enumerator hand that describes the current user card
 	 *         combination.
 	 */
 	private Hand getPlayerHand(Player player) {
 		Card[] temp = new Card[CARD_COUNT];
		for (int i = 0; i < temp.length; i++) {
 			temp[i] = tableCards.get(i);
 		}
 		temp[CARD_COUNT - 1] = player.getHand()[0];
 		temp[CARD_COUNT - 2] = player.getHand()[1];
 		return getHand(temp);
 	}
 
 	/**
 	 * Evaluates through all the possible combinations and returns Hand type
 	 * once requirements are met. If no combination is found, then the return
 	 * value is HIGH_CARD and which is the highest ranked card of players hand.
 	 * 
 	 * @param hand
 	 *            Cards that player has of current moot.
 	 * @return Player hand type.
 	 */
 	public Hand getHand(Card[] hand) {
 		currentHand = sortHand(hand);
 		combination = createHandTable(hand);
 		if (handIsRoyalFlush(combination))
 			return playerHand.setHand(Hand.ROYAL_FLUSH);
 		else if (handIsStraightFlush(combination))
 			return playerHand.setHand(Hand.STRAIGHT_FLUSH);
 		else if (handIsSameKind(combination, FOUR_OF_A_KIND_COUNT, true))
 			return playerHand.setHand(Hand.FOUR_OF_A_KIND);
 		else if (handIsFullHouse(combination))
 			return playerHand.setHand(Hand.FULL_HOUSE);
 		else if (handIsFlush(combination))
 			return playerHand.setHand(Hand.FLUSH);
 		else if (handIsStraight(combination))
 			return playerHand.setHand(Hand.STRAIGHT);
 		else if (handIsSameKind(combination, THREE_OF_A_KIND_COUNT, true))
 			return playerHand.setHand(Hand.THREE_OF_A_KIND);
 		else if (handIsTwoPair(combination))
 			return playerHand.setHand(Hand.TWO_PAIR);
 		else if (handIsSameKind(combination, ONE_PAIR_COUNT, true))
 			return playerHand.setHand(Hand.ONE_PAIR);
 		else {
 			playerHand.setHighCard(currentHand[0]);
 			return playerHand.setHand(Hand.HIGH_HAND);
 		}
 	}
 
 	/**
 	 * Checks through all the table, starting from the highest ranked fields
 	 * looking for a combination of two pairs.
 	 * 
 	 * @param combination2
 	 *            Table of players cards.
 	 * @return Evaluation of whether the hand has Two Pair combination.
 	 */
 	private boolean handIsTwoPair(Card[][] combination2) {
 		Card[][] temp = new Card[Suit.values().length][Rank.values().length];
 		temp = cloneTable(temp, combination2);
 		int skCount = 0;
 		for (int i = Rank.values().length - 1; i > -1; i--) {
 			scoreCards = new ArrayList<Card>();
 			for (int j = 0; j < Suit.values().length; j++) {
 				if (temp[Suit.values().length - j - 1][i] != null) {
 					if (skCount < 2)
 						scoreCards.add(temp[Suit.values().length - j - 1][i]);
 					skCount++;
 				}
 				if (skCount == TWO_PAIR_COUNT) {
 					evaluateScore(scoreCards);
 					setPlayerHand();
 					return true;
 				}
 			}
 			scoreCards = null;
 			skCount = 0;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks through all the table, starting from the highest ranked fields
 	 * looking for a combination Straight where 5 cards will be in row.
 	 * 
 	 * @param combination2
 	 *            Table of players cards.
 	 * @return Evaluation of whether the hand has Straight combination.
 	 */
 	private boolean handIsStraight(Card[][] combination2) {
 		Card[][] temp = new Card[Suit.values().length][Rank.values().length];
 		temp = cloneTable(temp, combination2);
 		int sCount = 0;
 		boolean continued = false;
 		boolean oneFound = false;
 		for (int i = Rank.values().length - 1; i > 0; i--) {
 			scoreCards = new ArrayList<Card>();
 			for (int j = 0; j < Suit.values().length; j++) {
 				if (temp[j][i] != null) {
 					for (int k = 0; k < Suit.values().length; k++) {
 						if (temp[k][i - 1] != null && !oneFound) {
 							scoreCards.add(temp[k][i - 1]);
 							sCount++;
 							continued = true;
 							oneFound = true;
 						}
 					}
 				}
 				if (sCount == 4) {
 					evaluateScore(scoreCards);
 					setPlayerHand();
 					return true;
 				}
 			}
 			oneFound = false;
 			if (!continued) {
 				scoreCards = null;
 				sCount = 0;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks through all the table, iterating through the suits, looking for a
 	 * card count higher then 5.
 	 * 
 	 * @param combination2
 	 *            Table of players cards.
 	 * @return Evaluation of whether the hand has Flush combination.
 	 */
 	private boolean handIsFlush(Card[][] combination2) {
 		Card[][] temp = new Card[Suit.values().length][Rank.values().length];
 		temp = cloneTable(temp, combination2);
 		int fCount = 0;
 		for (int i = 0; i < Suit.values().length; i++) {
 			scoreCards = new ArrayList<Card>();
 			for (int j = Rank.values().length - 1; j > -1; j--) {
 				if (temp[i][j] != null) {
 					scoreCards.add(temp[i][j]);
 					fCount++;
 				}
 				if (fCount == 4) {
 					evaluateScore(scoreCards);
 					setPlayerHand();
 					return true;
 				}
 			}
 			scoreCards = null;
 			fCount = 0;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks through all the table, looking for a Three Of A Kind and a Two
 	 * Pair combination, starting from the highest ranks. handScore is evaluated
 	 * only by Three Of A Kind cards.
 	 * 
 	 * @param combination2
 	 *            Table of players cards.
 	 * @return Evaluation of whether the hand has Full House combination.
 	 */
 	private boolean handIsFullHouse(Card[][] combination2) {
 		Card[][] temp = new Card[Suit.values().length][Rank.values().length];
 		temp = cloneTable(temp, combination2);
 		int tokCount = 0;
 		boolean threeOfAKind = false;
 		boolean twoOfAKind = false;
 		boolean lineCleared = false;
 		for (int i = 0; i < Rank.values().length; i++) {
 			scoreCards = new ArrayList<Card>();
 			for (int j = 0; j < Suit.values().length; j++) {
 				if (temp[j][i] != null) {
 					scoreCards.add(temp[j][i]);
 					tokCount++;
 				}
 				if (tokCount == 3 && !lineCleared) {
 					for (int j2 = 0; j2 < Suit.values().length; j2++) {
 						temp[j2][i] = null;
 					}
 					evaluateScore(scoreCards);
 					lineCleared = true;
 					threeOfAKind = true;
 				}
 			}
 			if (!threeOfAKind)
 				scoreCards = null;
 			tokCount = 0;
 		}
 		if (handIsSameKind(temp, 2, false))
 			twoOfAKind = true;
 		if (twoOfAKind && threeOfAKind) {
 			setPlayerHand();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Creates a duplicate table, so that any actions that are made to the
 	 * content of the table wouldn't affect further evaluation.
 	 * 
 	 * @param temp
 	 *            Empty table that will store original tables values.
 	 * @param combination2
 	 *            Original player hand table.
 	 * @return Copy of the original player hand table.
 	 */
 	private Card[][] cloneTable(Card[][] temp, Card[][] combination2) {
 		for (int i = 0; i < Rank.values().length; i++) {
 			for (int j = 0; j < Suit.values().length; j++) {
 				temp[j][i] = combination2[j][i];
 			}
 		}
 		return temp;
 	}
 
 	/**
 	 * Checks through all the table looking for a specified amount of similar
 	 * cards. Additional condition is provided if a method is called for Full
 	 * House combination evaluation.
 	 * 
 	 * @param combination2
 	 *            Table of player hand cards.
 	 * @param count
 	 *            Count of how many cards should be similar.
 	 * @param b
 	 *            Condition if a method is called not for Full House evaluation.
 	 * @return Evaluation of whether the hand has Similar card combination.
 	 */
 	private boolean handIsSameKind(Card[][] combination2, int count, boolean b) {
 		Card[][] temp = new Card[Suit.values().length][Rank.values().length];
 		temp = cloneTable(temp, combination2);
 		int skCount = 0;
 		for (int i = Rank.values().length - 1; i > -1; i--) {
 			if (b)
 				scoreCards = new ArrayList<Card>();
 			for (int j = 0; j < Suit.values().length; j++) {
 				if (temp[j][i] != null) {
 					if (b)
 						scoreCards.add(temp[j][i]);
 					skCount++;
 				}
 				if (skCount == count) {
 					if (b) {
 						evaluateScore(scoreCards);
 						setPlayerHand();
 					}
 					return true;
 				}
 			}
 			if (b)
 				scoreCards = null;
 			skCount = 0;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks through all the table, looking for a 5 cards in a row of the same
 	 * suit.
 	 * 
 	 * @param combination2
 	 *            Table of players cards.
 	 * @return Evaluation of whether the hand has Straight Flush combination.
 	 */
 	private boolean handIsStraightFlush(Card[][] combination2) {
 		Card[][] temp = new Card[Suit.values().length][Rank.values().length];
 		temp = cloneTable(temp, combination2);
 		int sfCount = 0;
 		for (int i = 0; i < Suit.values().length; i++) {
 			scoreCards = new ArrayList<Card>();
 			for (int j = Rank.values().length - 1; j > 0; j--) {
 				if (temp[i][j] != null && temp[i][j - 1] != null) {
 					scoreCards.add(temp[i][j]);
 					sfCount++;
 					if (sfCount == 4)
 						scoreCards.add(temp[i][j - 1]);
 				}
 				if (sfCount == 4) {
 					evaluateScore(scoreCards);
 					setPlayerHand();
 					return true;
 				}
 			}
 			scoreCards = null;
 			sfCount = 0;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks through all the table, looking for a 5 cards in a row of the same
 	 * suit where the first card rank is ACE.
 	 * 
 	 * @param combination2
 	 *            Table of players cards.
 	 * @return Evaluation of whether the hand has Royal Flush combination.
 	 */
 	private boolean handIsRoyalFlush(Card[][] combination2) {
 		Card[][] temp = new Card[Suit.values().length][Rank.values().length];
 		temp = cloneTable(temp, combination2);
 		int rfCount = 0;
 		for (int i = 0; i < Suit.values().length; i++) {
 			scoreCards = new ArrayList<Card>();
 			for (int j = 0; j < CARDS_TO_EVALUATE; j++) {
 				if (temp[i][Rank.values().length - j - 1] != null) {
 					scoreCards.add(temp[i][Rank.values().length - j - 1]);
 					rfCount++;
 				}
 				if (rfCount == 5) {
 					evaluateScore(scoreCards);
 					setPlayerHand();
 					return true;
 				}
 			}
 			scoreCards = null;
 			rfCount = 0;
 		}
 		return false;
 	}
 
 	/**
 	 * Sorts the array of player cards by Card rank ordinal. The highest rank
 	 * card will be at the begging of the array.
 	 * 
 	 * @param hand
 	 *            Array of player cards.
 	 * @return Sorted array of player cards.
 	 */
 	private Card[] sortHand(Card[] hand) {
 		for (int i = 1; i < hand.length; i++) {
 			if (hand[i - 1].getRank().ordinal() < hand[i].getRank().ordinal()) {
 				Card prev = hand[i - 1];
 				hand[i - 1] = hand[i];
 				hand[i] = prev;
 				i--;
 			}
 		}
 		return hand;
 	}
 
 	/**
 	 * Evaluates the score of player card combination. The higher the rank of
 	 * the cards, the higher the score of the combination. Is used to determine
 	 * which players hand is stronger if the hands are of the same type. Assigns
 	 * score to the PlayerHand handScore variable.
 	 * 
 	 * @param cards
 	 *            List of player cards which score needs to be evaluated.
 	 */
 	private void evaluateScore(List<Card> cards) {
 		int score = 0;
 		for (Card card : cards) {
 			score += card.getRank().ordinal();
 		}
 		playerHand.setHandScore(score);
 	}
 
 	/**
 	 * Assigns value of the kicker (if necessary) and List<Card> playerHand.
 	 */
 	private void setPlayerHand() {
 		Card[] temp = new Card[CARDS_TO_EVALUATE];
 		for (int i = 0; i < CARDS_TO_EVALUATE; i++) {
 			if (i > scoreCards.size() - 1) {
 				if (i == scoreCards.size())
 					playerHand.setKicker(temp[i]);
 				temp[i] = currentHand[i - scoreCards.size()];
 			} else
 				temp[i] = scoreCards.get(i);
 		}
 		temp = sortHand(temp);
 		if (temp != null && playerHand != null)
 			playerHand.setPlayerHand(temp);
 	}
 
 	/* Private instance variables. */
 	private List<Player> playersToEvaluate;
 	private HashMap<Player, PlayerHand> evaluatedPlayers;
 	private Card[][] combination;
 	private Card[] currentHand;
 	private PlayerHand playerHand;
 	private List<Card> scoreCards;
 	private List<Card> tableCards;
 }
