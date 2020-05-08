 package com.videoplaza.poker.game.util;
 
 import static ch.lambdaj.Lambda.extract;
 import static ch.lambdaj.Lambda.on;
 import static ch.lambdaj.Lambda.sort;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.videoplaza.poker.game.model.Card;
 import com.videoplaza.poker.game.model.HandType;
 import com.videoplaza.poker.game.model.Player;
 import com.videoplaza.poker.game.model.Suit;
 
 public class PokerUtil {
 
    public static int bestHand(List<Card> list) {
       assert list.size() >= 5;
       if (list.size() > 5) {
          int bestValue = 0;
          for (int i = 0; i < list.size(); i++) {
             List<Card> newList = new ArrayList<Card>(list);
             newList.remove(i);
             int value = bestHand(newList);
             if (value > bestValue)
                bestValue = value;
          }
          return bestValue;
       }
 
       list = sort(list, on(Card.class).getRank());
       if (isStraightFlush(list))
          return HandType.STRAIGHT_FLUSH.getValue()
                + (list.get(4).getRank() == 14 && list.get(3).getRank() == 5 ? list.get(3).getRank() : list.get(4).getRank());
       if (isFours(list))
          return HandType.FOUR_OF_A_KIND.getValue()
                + (list.get(0).getRank() == list.get(1).getRank() ? list.get(0).getRank() * 20 + list.get(4).getRank() : list.get(4).getRank() * 20
                      + list.get(0).getRank());
       if (isFullHouse(list))
          return HandType.FULL_HOUSE.getValue() + list.get(2).getRank() * 20
                + (list.get(0).getRank() == list.get(2).getRank() ? list.get(4).getRank() : list.get(0).getRank());
       if (isFlush(list))
          return HandType.FLUSH.getValue() + list.get(4).getRank();
       if (isStraight(list))
          return HandType.STRAIGHT.getValue() + (list.get(4).getRank() == 14 && list.get(3).getRank() == 5 ? list.get(3).getRank() : list.get(4).getRank());
       if (isThrees(list)) {
          int value = HandType.THREE_OF_A_KIND.getValue();
          value += list.get(2).getRank() * 400;
          if (list.get(0).getRank() == list.get(2).getRank()) {
             value += list.get(4).getRank() * 20 + list.get(3).getRank();
          } else if (list.get(1).getRank() == list.get(2).getRank()) {
             value += list.get(4).getRank() * 20 + list.get(0).getRank();
          } else {
             value += list.get(1).getRank() * 20 + list.get(0).getRank();
          }
          return value;
       }
       if (isTwoPair(list)) {
          int value = HandType.TWO_PAIRS.getValue();
          if (list.get(0).getRank() == list.get(1).getRank()) {
             if (list.get(2).getRank() == list.get(3).getRank()) {
                value += list.get(2).getRank() * 400 + list.get(0).getRank() * 20 + list.get(4).getRank();
             } else {
                value += list.get(4).getRank() * 400 + list.get(0).getRank() * 20 + list.get(2).getRank();
             }
          } else {
             value += list.get(4).getRank() * 400 + list.get(2).getRank() * 20 + list.get(0).getRank();
          }
          return value;
       }
 
       if (list.get(0).getRank() == list.get(1).getRank())
          return HandType.PAIR.getValue() + list.get(0).getRank() * 8000 + list.get(4).getRank() * 400 + list.get(3).getRank() * 20 + list.get(2).getRank();
       if (list.get(1).getRank() == list.get(2).getRank())
          return HandType.PAIR.getValue() + list.get(1).getRank() * 8000 + list.get(4).getRank() * 400 + list.get(3).getRank() * 20 + list.get(0).getRank();
       if (list.get(2).getRank() == list.get(3).getRank())
          return HandType.PAIR.getValue() + list.get(2).getRank() * 8000 + list.get(4).getRank() * 400 + list.get(1).getRank() * 20 + list.get(0).getRank();
       if (list.get(3).getRank() == list.get(4).getRank())
          return HandType.PAIR.getValue() + list.get(3).getRank() * 8000 + list.get(2).getRank() * 400 + list.get(1).getRank() * 20 + list.get(0).getRank();
 
       return list.get(4).getRank() * 160000 + list.get(3).getRank() * 8000 + list.get(2).getRank() * 400 + list.get(1).getRank() * 20 + list.get(0).getRank();
    }
 
    public static float getHandRank(List<Card> holeCards, List<Card> cardsOnTable) {
       assert holeCards.size() == 2;
       List<Integer> handValues = new ArrayList<Integer>();
 
       List<Card> deck = getDeck();
       deck.removeAll(holeCards);
       deck.removeAll(cardsOnTable);
       System.out.println(deck.size());
 
       for (int i = 0; i < deck.size() - 1; i++) {
          for (int j = i + 1; j < deck.size(); j++) {
             List<Card> hand = new ArrayList<Card>(cardsOnTable);
             hand.add(deck.get(i));
             hand.add(deck.get(j));
             handValues.add(bestHand(hand));
          }
       }
 
       List<Card> hand = new ArrayList<Card>(cardsOnTable);
       hand.addAll(holeCards);
       int myHandValue = bestHand(hand);
       handValues.add(bestHand(hand));
 
       Collections.sort(handValues);
 
       System.out.println("Rank of your hand is " + handValues.indexOf(myHandValue) + " of " + handValues.size());
       System.out.println(myHandValue);
       System.out.println(handValues.get(handValues.size() - 1));
       return (float) handValues.indexOf(myHandValue) / (float) handValues.size();
    }
 
    /**
     * 
     * @param playerHoleCards
     * @param cardsOnTable
     * @return index of winning player or -1 in case of a tie
     */
    public static List<Integer> getWinningHand(List<List<Card>> playerHoleCards, List<Card> cardsOnTable) {
      assert cardsOnTable.size() == 7;
       List<Integer> winners = new ArrayList<Integer>();
       int bestHandSoFar = 0;
       for (int player = 0; player < playerHoleCards.size(); player++) {
          List<Card> hand = new ArrayList<Card>(cardsOnTable);
          hand.addAll(playerHoleCards.get(player));
          int playerHandValue = bestHand(hand);
          if (playerHandValue > bestHandSoFar) {
             winners.clear();
             winners.add(player);
             bestHandSoFar = playerHandValue;
          } else if (playerHandValue == bestHandSoFar) {
             winners.add(player);
          }
       }
       return winners;
    }
 
    public static List<Player> getWinningPlayers(List<Player> players, List<Card> cardsOnTable) {
      assert cardsOnTable.size() == 7;
       List<Player> winners = new ArrayList<Player>();
       List<Player> activePlayers = new ArrayList<Player>();
       List<List<Card>> playerHoleCards = new ArrayList<List<Card>>();
       for (Player player : players) {
          if (player.isIn()) {
             activePlayers.add(player);
          }
       }
       for (Player player : activePlayers) {
          playerHoleCards.add(player.getHoleCards());
       }
       List<Integer> indices = getWinningHand(playerHoleCards, cardsOnTable);
       for (Integer index : indices) {
          winners.add(activePlayers.get(index));
       }
       return winners;
    }
 
    private static List<Card> getDeck() {
       List<Card> deck = new ArrayList<Card>();
       for (Suit suit : Suit.values()) {
          for (int i = 2; i <= 14; i++) {
             deck.add(new Card(suit, i));
          }
       }
       return deck;
    }
 
    private static double[] getProbabilitiesHelper(List<List<Card>> playerHoleCards, List<Card> cardsOnTable, int startAtDeckIndex) {
       assert cardsOnTable.size() <= 5;
       double[] totalWins = new double[playerHoleCards.size()];
       List<Card> deck = getDeck();
       for (List<Card> holeCards : playerHoleCards)
          deck.removeAll(holeCards);
       deck.removeAll(cardsOnTable);
 
       if (cardsOnTable.size() == 5) {
          List<Integer> winningPlayers = getWinningHand(playerHoleCards, cardsOnTable);
          for (Integer bestPlayer : winningPlayers) {
             totalWins[bestPlayer] = 1 / winningPlayers.size();
          }
       } else {
          for (int i = startAtDeckIndex; i < deck.size(); i++) {
             List<Card> newCardsOnTable = new ArrayList<Card>(cardsOnTable);
             newCardsOnTable.add(deck.get(i));
             double[] wins = getProbabilitiesHelper(playerHoleCards, newCardsOnTable, i);
             for (int player = 0; player < playerHoleCards.size(); player++) {
                totalWins[player] += wins[player];
             }
          }
       }
 
       return totalWins;
    }
 
    private static boolean isFlush(List<Card> list) {
       List<Suit> suits = extract(list, on(Card.class).getSuit());
       HashSet<Suit> suitSet = new HashSet<Suit>(suits);
       return suitSet.size() == 1;
    }
 
    private static boolean isFours(List<Card> list) {
       return (list.get(0).getRank() == list.get(3).getRank() || list.get(1).getRank() == list.get(4).getRank());
    }
 
    private static boolean isFullHouse(List<Card> list) {
 
       return list.get(0).getRank() == list.get(1).getRank() && list.get(3).getRank() == list.get(4).getRank()
             && (list.get(0).getRank() == list.get(2).getRank() || list.get(4).getRank() == list.get(2).getRank());
    }
 
    private static boolean isStraight(List<Card> list) {
       for (int i = 0; i < list.size() - 1; i++) {
          // Exceptions for aces, i.e. 2,3,4,5,A
          if (i == list.size() - 2 && list.get(i).getRank() == 5 && list.get(i + 1).getRank() == 14)
             continue;
          if (list.get(i).getRank() != list.get(i + 1).getRank() - 1)
             return false;
       }
       return true;
    }
 
    private static boolean isStraightFlush(List<Card> list) {
       return isStraight(list) && isFlush(list);
    }
 
    private static boolean isThrees(List<Card> list) {
       if (list.get(0).getRank() == list.get(2).getRank())
          return true;
       if (list.get(4).getRank() == list.get(2).getRank())
          return true;
       if (list.get(1).getRank() == list.get(3).getRank())
          return true;
       return false;
    }
 
    private static boolean isTwoPair(List<Card> list) {
       Set<Integer> ranks = new HashSet<Integer>();
       ranks.addAll(extract(list, on(Card.class).getRank()));
       return ranks.size() == 3;
    }
 
    static double[] getProbabilities(List<List<Card>> playerHoleCards, List<Card> cardsOnTable) {
       double[] result = new double[playerHoleCards.size()];
       double[] wins = getProbabilitiesHelper(playerHoleCards, cardsOnTable, 0);
       int count = 0;
       for (int player = 0; player < playerHoleCards.size(); player++) {
          count += wins[player];
       }
       for (int player = 0; player < playerHoleCards.size(); player++) {
          result[player] = (wins[player]) / (count);
       }
       return result;
    }
 }
