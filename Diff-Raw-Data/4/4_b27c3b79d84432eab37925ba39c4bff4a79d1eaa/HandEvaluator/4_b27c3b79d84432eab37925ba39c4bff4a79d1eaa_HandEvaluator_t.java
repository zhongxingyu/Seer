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
 
 	public HandEvaluator(List<Player> playersLeft) {
 		playersToEvaluate = playersLeft;
 		evaluatedPlayers = new HashMap<Player, PlayerHand>();
 	}
 
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
 
 	private List<PlayerHand> sortPlayerPositions(List<PlayerHand> playerRanking) {
 		for (int i = 0; i < playerRanking.size() - 1; i++) {
 			if(playerRanking.get(i).getPosition() > playerRanking.get(i + 1).getPosition()) {
 				playerRanking.set(i, playerRanking.set(i + 1, playerRanking.get(i)));
 				i--;
 			}
 		}
 		int position = 0;
 		for (int i = 0; i < playerRanking.size() - 1; i++) {
 			if (playerRanking.get(i).getPosition() != playerRanking.get(i + 1).getPosition())
 				playerRanking.get(i).setPosition(position++);
 			else
 				playerRanking.get(i).setPosition(position);
 			if (i + 1 == playerRanking.size() - 1)
 				playerRanking.get(i + 1).setPosition(position);
 		}
 		return playerRanking;
 	}
 
 	private Card[][] createHandTable(Card[] hand) {
 		Card[][] tempHand = new Card[Suit.values().length][Rank.values().length];
 		for (Card card : hand) {
 			tempHand[card.getSuit().ordinal()][card.getRank().ordinal()] = card;
 		}
 		return tempHand;
 	}
 
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
 						if (((PlayerHand) prev).getHighCard().getRank()
 								.ordinal() < entry.getHighCard().getRank()
 								.ordinal()) {
 							incrementFollowingPlayers(playerPositions, entry);
 							i--;
 						} else if (((PlayerHand) prev).getHighCard().getRank()
 								.ordinal() == entry.getHighCard().getRank()
 								.ordinal()) {
 						} else {
 							incrementFollowingPlayers(playerPositions, prev);
 						}
 					} else if (entry.getHand().equals(Hand.STRAIGHT_FLUSH)
 							|| entry.getHand().equals(Hand.FULL_HOUSE)
 							|| entry.getHand().equals(Hand.FLUSH)
 							|| entry.getHand().equals(Hand.STRAIGHT)) {
 						if (prev.getHandScore() < entry.getHandScore()) {
 							incrementFollowingPlayers(playerPositions, entry);
 							playerPositions.set(i - 1, playerPositions.set(i, playerPositions.get(i - 1)));
 							i--;
 						} else if (prev.getHandScore() == entry.getHandScore()) {
 						} else {
 							incrementFollowingPlayers(playerPositions, prev);
 						}
 					} else if (entry.getHand().equals(Hand.ROYAL_FLUSH)) {
 						// do nothing, because it is the strongest hand
 						// empty so that shouldn't write many evaluations in
 						// next statement :)
 					} else {
 						if (prev.getKicker().getRank().ordinal() < entry
 								.getKicker().getRank().ordinal()) {
 							incrementFollowingPlayers(playerPositions, entry);
 							i--;
 						} else if (prev.getKicker().getRank().ordinal() == entry
 								.getKicker().getRank().ordinal()) {
 						} else {
 							incrementFollowingPlayers(playerPositions, prev);
 						}
 					}
 				}
 			} else {
 				prev = entry;
 			}
 		}
 		return playerPositions;
 	}
 
 	private void incrementFollowingPlayers(List<PlayerHand> playerPositions,
 			PlayerHand entry) {
 		for (PlayerHand playerHand : playerPositions) {
 			if (playerHand.getHand().ordinal() >= entry.getHand()
 							.ordinal()) {
 				if (!playerHand.equals(entry)) {
 					playerHand.setPosition(playerHand.getPosition() + 1);
 				}
 			}
 		}
 	}
 
 	private Hand getPlayerHand(Player player) {
 		return getHand(player.getHand());
 	}
 
 	public Hand getHand(Card[] hand) {
 		// In method calls have to set scoreCards!
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
 
 	private Card[][] cloneTable(Card[][] temp, Card[][] combination2) {
 		for (int i = 0; i < Rank.values().length; i++) {
 			for (int j = 0; j < Suit.values().length; j++) {
 				temp[j][i] = combination2[j][i];
 			}
 		}
 		return temp;
 	}
 
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
 
 	private void evaluateScore(List<Card> cards) {
 		int score = 0;
 		for (Card card : cards) {
 			score += card.getRank().ordinal();
 		}
 		playerHand.setHandScore(score);
 	}
 
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
 	}
 
 	private List<Player> playersToEvaluate;
 	private HashMap<Player, PlayerHand> evaluatedPlayers;
 	private Card[][] combination;
 	private Card[] currentHand;
 	private PlayerHand playerHand;
 	private List<Card> scoreCards;
 }
