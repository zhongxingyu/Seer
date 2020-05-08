 package poker;
 
 import exceptions.EarlyFinishedHandException;
 import exceptions.OutOfCardsException;
 import players.Player;
 
 import java.util.*;
 
 /**
  * User: Sina
  * Date: Feb 29, 2012
  */
 public class Dealer {
     private GameSetting gameSetting;
     private Notifier notifier;
 
     // todo: get cardDeck from a factory so that outsiders can test certain scenarios repeatedly
     public Dealer(GameSetting gameSetting, List<Notifiable> externalNotifiables) {
         this.gameSetting = gameSetting;
         List<Notifiable> notifiables = new ArrayList<Notifiable>();
         notifiables.addAll(gameSetting.getPlayers());
         notifiables.addAll(externalNotifiables);
         notifier = new Notifier(notifiables);
     }
 
     public void runGame(int maxCount) throws OutOfCardsException {
         boolean tableOpen = true;
         int handCount = 0;
         int smallBlindIndex = 0;
         
         while (tableOpen) {
             HandSetting handSetting = new HandSetting(gameSetting, smallBlindIndex);
             notifier.notifyNewHandStarted(handSetting);
             try {
                 // PreFlop
                 handSetting.dealPreFlop();
                 doBettingRound(handSetting);
                 // Flop
                 handSetting.dealFlop();
                 notifier.showFlopToEveryOne(handSetting.getBoard().cards[0], handSetting.getBoard().cards[1], handSetting.getBoard().cards[2]);
                 doBettingRound(handSetting);
                 // Turn
                 handSetting.dealTurn();
                 notifier.showTurnToEveryOne(handSetting.getBoard().cards[3]);
                 doBettingRound(handSetting);
                 // River
                 handSetting.dealRiver();
                 notifier.showRiverToEveryOne(handSetting.getBoard().cards[4]);
                 doBettingRound(handSetting);
                 // Showdown
                 showDown(handSetting);
             } catch (EarlyFinishedHandException e) {
                 showDown(handSetting);
             }
             smallBlindIndex = (smallBlindIndex + 1) % gameSetting.getPlayers().size();
             handCount++;
             if (maxCount != -1 && handCount >= maxCount) {
                 tableOpen = false;
             }
         }
         notifier.notifyGameEnds();
     }
 
     private void doBettingRound(HandSetting handSetting) throws EarlyFinishedHandException {
         BettingRound bettingRound = new BettingRound(handSetting);
         while (!bettingRound.finished()) {
             Action action = bettingRound.nextBet();
             if (action.isFold()) {
                 handSetting.fold(action.getPlayer());
                 notifier.announce(action);
                 if (handSetting.getRemainingInPot() == 1) {
                     bettingRound.terminate();
                     throw new EarlyFinishedHandException(bettingRound.getLastBoardRaise().getPlayer());
                 }
             } else {
                 double stackDeduction = action.getBetDelta();
                 gameSetting.deductStack(action.getPlayer(), stackDeduction);
                 notifier.announce(action);
             }
         }
         bettingRound.terminate();
     }
 
     public void showDown(HandSetting handSetting) {
         // Create a map of getStartingPlayers who decide to show their hands with the hand type they have
         Map<Player, HandType> playerHandTypes = new HashMap<Player, HandType>();
         Pot pot = handSetting.getPot();
         Iterator<Player> iterator = pot.playerIterator();
         while (iterator.hasNext()) {
             Player player = iterator.next();
             Map<Player, PreflopCards> playerPocketCards = handSetting.getPlayerPocketCards();
             if (playerPocketCards.containsKey(player)) {
                 HandType handType = new HandTypeFinder(playerPocketCards.get(player), handSetting.getBoard()).findHandType();
                 if (player.decideShowOrMuck(handType)) {
                     playerHandTypes.put(player, handType);
                     notifier.notifyShowCards(player, handType);
                 } else {
                     notifier.notifyMuckCards(player);
                 }
             }
         }
 
         // decide how each side is divided based on retrieved hand types
         List<Player> playersInvolved = pot.getPlayersInvolved();
         for (Pot sidePot : pot.getSidePots()) {
             handlePot(playerHandTypes, playersInvolved, sidePot);
             playersInvolved.removeAll(sidePot.getAttachedPlayers());
         }
         handlePot(playerHandTypes, playersInvolved, pot);
     }
 
     private void handlePot(Map<Player, HandType> playerHandTypes, List<Player> playersInvolved, Pot pot) {
         List<Player> potWinners =  chooseWinners(playersInvolved, playerHandTypes);
         double eachShare = pot.getValue() / potWinners.size();
         for (Player player : potWinners) {
             gameSetting.increaseStack(player, eachShare);
         }
        notifier.announceWin(potWinners, pot.getValue());
     }
 
     private List<Player> chooseWinners(List<Player> playersInvolved, Map<Player, HandType> playerHandTypes) {
         List<Player> winners = new ArrayList<Player>();
         HandType bestHandType = null;
         for (Player player : playersInvolved) {
             HandType handType = playerHandTypes.get(player);
             if (bestHandType == null || handType.compareTo(bestHandType) > 0) {
                 winners = new ArrayList<Player>();
                 winners.add(player);
                 bestHandType = handType;
             } else if (handType.compareTo(bestHandType) == 0) {
                 winners.add(player);
             }
         }
         return winners;
     }
 
     class Notifier {
         private List<Notifiable> notifiables;
 
         public Notifier(List<Notifiable> notifiables) {
             this.notifiables = notifiables;
         }
 
         private void announce(Action action) {
             for (Notifiable notifiable : notifiables) {
                 notifiable.handle(action);
             }
         }
 
         public void notifyNewHandStarted(HandSetting handSetting) {
             for (Notifiable notifiable : notifiables) {
                 notifiable.newHand(handSetting);
             }
         }
 
         private void showFlopToEveryOne(Card card1, Card card2, Card card3) {
             for (Notifiable notifiable : notifiables) {
                 notifiable.flopIs(card1, card2, card3);
             }
         }
 
         private void showTurnToEveryOne(Card card) {
             for (Notifiable notifiable : notifiables) {
                 notifiable.turnIs(card);
             }
         }
 
         private void showRiverToEveryOne(Card card) {
             for (Notifiable notifiable : notifiables) {
                 notifiable.riverIs(card);
             }
         }
 
         public void notifyGameEnds() {
             for (Notifiable notifiable : notifiables) {
                 notifiable.gameEnds();
             }
         }
 
         public void notifyShowCards(Player player, HandType handType) {
             for (Notifiable notifiable : notifiables) {
                 notifiable.cardsShown(player, handType);
             }
         }
 
         public void notifyMuckCards(Player player) {
             for (Notifiable notifiable : notifiables) {
                 notifiable.cardsMucked(player);
             }
         }
 
        public void announceWin(List<Player> potWinners, double potValue) {
             for (Notifiable notifiable : notifiables) {
                notifiable.potWon(potWinners, potValue);
             }
         }
     }
 }
