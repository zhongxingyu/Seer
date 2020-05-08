 package com.randombit.uskoci.game;
 
 import com.randombit.uskoci.card.dao.CardDAO;
 import com.randombit.uskoci.card.dao.CardDAOSimple;
 import com.randombit.uskoci.card.dao.SingletonCardDB;
 import com.randombit.uskoci.card.model.Card;
 
 import java.util.*;
 
 public class GameControllerImpl implements GameController {
 
 
     private CardDAO cardDAO = new CardDAOSimple();
 
     private List<Card> cardDeck = new ArrayList<Card>(cardDAO.getAllCards());
 
     // Map of cards in all players hands
     private Map<String, List<Card>> playerCardMap = new HashMap<String, List<Card>>();
 
     private int currentPlayerId;
 
     public void setCardDAO(CardDAO cardDAO) {
         this.cardDAO = cardDAO;
         List<Card> cardDeck = new ArrayList<Card>(cardDAO.getAllCards());
     }
 
     private List<Card> cardsOnTheTable = Collections.<Card>emptyList();
 
     private int numberOfPlayersJoined;
 
     public List<Card> getCardDeck() {
         return cardDeck;
     }
 
     public void setCardDeck(List<Card> cardDeck) {
         this.cardDeck = cardDeck;
     }
 
     public int getCurrentPhase() {
         return currentPhase;
     }
 
     public int setNextPhase() {
         currentPhase++;
         if (currentPhase > NO_OF_PHASES) {
             currentPhase = 1;
             currentPlayerId++;
         }
         return currentPhase;
     }
 
     private int currentPhase;
 
 
     private static final int MAX_NUMBER_OF_PLAYERS = 5;
     private static final int MIN_NUMBER_OF_PLAYERS = 3;
     private static final int BEGINNING_NUMBER_OF_CARDS = 4;
     private static final int NO_OF_CARDS_DRAWN_A_TURN = 1;
     private static final int NO_OF_PHASES = 6;
 
 
     // TODO implement methods
     public int getCurrentPlayerId() {
         return currentPlayerId;
     }
 
     public int setNextPlayer() {
         return currentPlayerId++;
     }
 
     public List<Card> getCardsInTheDeck() {
         return cardDeck;
     }
 
     private void dealCards(int numberOfPlayers) {
         Collections.shuffle(cardDeck);
         for (int i = 1; i < numberOfPlayers + 1; i++) {
             List<Card> cardsDealtToPlayer = cardDeck.subList(0, BEGINNING_NUMBER_OF_CARDS);
             playerCardMap.put(String.valueOf(i), new ArrayList<Card>(cardsDealtToPlayer));
            cardDeck.removeAll(playerCardMap.get(String.valueOf(i)));
         }
     }
 
     // Reset field values
     public String resetGame() {
         cardDeck = new ArrayList<Card>(cardDAO.getAllCards());
 
         playerCardMap = new HashMap<String, List<Card>>();
 
         Random randomGenerator = new Random();
 
         currentPlayerId = randomGenerator.nextInt(numberOfPlayersJoined - 1) + 1;
 
         currentPhase = 1;
 
         return "Game reset";
     }
 
     public boolean startGame(int numberOfPlayersJoined) {
         boolean gameStarted = false;
         if (numberOfPlayersJoined <= MAX_NUMBER_OF_PLAYERS || numberOfPlayersJoined >= MIN_NUMBER_OF_PLAYERS) {
             this.numberOfPlayersJoined = numberOfPlayersJoined;
             resetGame();
             dealCards(this.numberOfPlayersJoined);
             gameStarted = true;
         }
         return gameStarted;
     }
 
     public List<Card> getPlayerCards(int playerId) {
         return playerCardMap.get(String.valueOf(playerId));
     }
 
     @Override
     public Card drawCard(int playerId) {
         Card cardDrawn = cardDeck.remove(0);
         playerCardMap.get(String.valueOf(playerId)).add(cardDrawn);
         return cardDrawn;
     }
 }
