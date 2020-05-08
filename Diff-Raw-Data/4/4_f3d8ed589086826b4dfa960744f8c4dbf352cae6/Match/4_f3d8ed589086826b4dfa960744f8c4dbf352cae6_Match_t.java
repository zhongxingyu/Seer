 /*
  * Copyright 2012 Cameron Zemek <grom358@gmail.com>.
  */
 package pokeraichallenge;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import poker.Card;
 import poker.CardSet;
 import poker.Hand;
 
 /**
  *
  * @author Cameron Zemek <grom358@gmail.com>
  */
 public class Match {
     private Settings settings;
     private List<Player> players;
     private int roundNo;
     private int smallBlind;
     private int bigBlind;
     private int pot;
     private int sidepot;
     private int currentBet;
     private int minRaise;
     private int onButton, onTurn;
     private List<Card> board;
 
     public Match(Settings settings, List<Player> players) {
         this.settings = settings;
         this.players = players;
         this.roundNo = 0;
         this.smallBlind = 10;
         this.bigBlind = 20;
         this.onButton = 0;
     }
 
     /**
      * Play match and return the winner
      */
     public Player play() {
         // Tell players about the settings
         printAll("Settings gameType " + settings.getGameType());
         printAll("Settings gameMode " + settings.getGameMode());
         printAll("Settings timeBank " + settings.getTimeBank());
         printAll("Settings timePerMove " + settings.getTimePerMove());
         printAll("Settings handsPerLevel " + settings.getHandsPerLevel());
         for (Player player : players) {
             player.println("Settings yourBot " + player.getName());
         }
 
         // Print set positions
         printAll(players.get(0).getName() + " seat 0");
         printAll(players.get(1).getName() + " seat 1");
 
         for (;;) {
             if (players.get(0).getStack() == 0) {
                 return players.get(1);
             }
             if (players.get(1).getStack() == 0) {
                 return players.get(0);
             }
             playRound();
             // Move button
             onButton = (onButton + 1) % players.size();
         }
     }
 
     /**
      * Play a single hand
      */
     private void playRound() {
         printAll("Match round " + (roundNo + 1));
         printAll("Match smallBlind " + smallBlind);
         printAll("Match bigBlind " + bigBlind);
         printAll("Match onButton " + players.get(onButton).getName());
 
         // Reset bets
         for (Player player : players) {
             player.resetBet();
         }
 
         // Post the blinds
         post(players.get(onButton), smallBlind);
         post(players.get((onButton + 1) % players.size()), bigBlind);
 
         // Shuffle deck
         List<Card> deck = Card.newDeck();
         Collections.shuffle(deck);
 
         // Deal starting hands
         for (Player player : players) {
             List<Card> hand = new ArrayList<>(2);
             hand.add(deck.remove(deck.size() - 1));
             hand.add(deck.remove(deck.size() - 1));
             player.setCards(hand);
             String strHand = toString(hand);
             player.println(player.getName() + " hand " + strHand);
             System.out.println(player.getName() + " hand " + strHand);
         }
 
         board = new ArrayList<>(5);
 
         // Pre-flop
         pot = bigBlind;
         sidepot = smallBlind;
         currentBet = minRaise = bigBlind;
         onTurn = (onButton + 2) % players.size();
         if (bettingRound()) {
             // Flop
             currentBet = 0;
             onTurn = (onButton + 1) % players.size();
             board.add(deck.remove(deck.size() - 1));
             board.add(deck.remove(deck.size() - 1));
             board.add(deck.remove(deck.size() - 1));
             printAll("Match table " + toString(board));
             if (bettingRound()) {
                 // Turn
                 currentBet = 0;
                 onTurn = (onButton + 1) % players.size();
                 board.add(deck.remove(deck.size() - 1));
                 printAll("Match table " + toString(board));
                 if (bettingRound()) {
                     // River
                     currentBet = 0;
                     onTurn = (onButton + 1) % players.size();
                     board.add(deck.remove(deck.size() - 1));
                     printAll("Match table " + toString(board));
                     if (bettingRound()) {
                         showdown();
                     }
                 }
             }
         }
 
         roundNo++;
         if (roundNo % settings.getHandsPerLevel() == 0) {
             smallBlind <<= 1;
             bigBlind <<= 1;
         }
     }
 
     private boolean bettingRound() {
         // Skip betting if player is all-in
         if (players.get(0).isAllIn() || players.get(1).isAllIn()) {
             return true;
         }
 
         // Print stack information
         for (Player player : players) {
             System.out.println(player.getName() + " stack " + player.getStack());
         }
 
         int playersToAct = 2;
         while (playersToAct > 0) {
             Move move = bet();
             if (move.getAction() == Move.Action.FOLD) {
                 // The remaining bot wins
                 int prize = pot + sidepot;
                 Player winner = players.get(onTurn);
                 winner.giveChips(prize);
                 printAll(winner.getName() + " wins " + prize);
                 return false;
             } else if (move.getAction() == Move.Action.RAISE) {
                 playersToAct = players.size() - 1;
            } else {
                playersToAct--;
             }
         }
 
         // Reset bets
         for (Player player : players) {
             player.resetBet();
         }
 
         return true;
     }
 
     private Move bet() {
         Player player = players.get(onTurn);
         Player opponent = players.get((onTurn + 1) % players.size());
 
         Move move = requestMove(player);
         int requiredAmount = currentBet - player.getBet();
 
         // Change check to fold if there is a current bet
         if (requiredAmount > 0 && move.getAction() == Move.Action.CHECK) {
             move = Move.FOLD;
         }
 
         // Change call to check if no current bet
         if (requiredAmount == 0 && move.getAction() == Move.Action.CALL) {
             move = Move.CHECK;
         }
 
         if (move.getAction() == Move.Action.RAISE) {
             // If opponent is all in, change to call
             if (opponent.isAllIn()) {
                 move = Move.CALL;
             } else if (player.getStack() < minRaise) {
                 move = Move.CALL;
             }
         }
 
         // Handle moves
         int amount = move.getAmount();
         if (move.getAction() == Move.Action.CALL) {
             amount = Math.min(requiredAmount, player.getStack());
             amount = player.takeChips(amount);
             pot += sidepot + requiredAmount;
             sidepot = 0;
         } else if (move.getAction() == Move.Action.RAISE) {
             pot += player.takeChips(sidepot); // Call existing bet
             amount = Math.max(minRaise, amount); // Must raise by minimum
             amount = Math.min(amount, player.getStack()); // Cap raise to players stack
             amount = player.takeChips(amount);
             sidepot = amount;
             currentBet += amount; // Increase required bet
             minRaise = amount;
         }
 
         printAll(player.getName() + " " + move.getAction().toString().toLowerCase() + " " + amount);
 
         onTurn = (onTurn + 1) % players.size();
         return move;
     }
 
     private void showdown() {
         for (Player player : players) {
             printAll(player.getName() + " hand " + toString(player.getCards()));
         }
 
         int p1, p2;
 
         List<Card> hand = new ArrayList<>(board);
         hand.addAll(players.get(0).getCards());
         p1 = Hand.eval(new CardSet(hand)).getValue();
 
         hand = new ArrayList<>(board);
         hand.addAll(players.get(1).getCards());
         p2 = Hand.eval(new CardSet(hand)).getValue();
 
         int prize = pot;
         if (p1 == p2) {
             // Split pot
             prize = prize >> 1;
             players.get(0).giveChips(prize);
             players.get(1).giveChips(prize);
             printAll(players.get(0).getName() + " wins " + prize);
             printAll(players.get(1).getName() + " wins " + prize);
         } else if (p1 < p2) {
             Player winner = players.get(1);
             winner.giveChips(prize);
             printAll(winner.getName() + " wins " + prize);
         } else {
             Player winner = players.get(0);
             winner.giveChips(prize);
             printAll(winner.getName() + " wins " + prize);
         }
     }
 
     private void post(Player player, int amount) {
         amount = player.takeChips(amount);
         printAll(player.getName() + " post " + amount);
     }
 
     private String toString(List<Card> cards) {
         StringBuilder sb = new StringBuilder();
         sb.append('[');
         boolean isFirst = true;
         for (Card card : cards) {
             if (isFirst) {
                 isFirst = false;
             } else {
                 sb.append(',');
             }
             sb.append(card);
         }
         sb.append(']');
         return sb.toString();
     }
 
     /**
      * Print a message to all bots
      */
     private void printAll(String message) {
         for (Player player : players) {
             player.println(message);
         }
         System.out.println(message);
     }
 
     /**
      * Request an action from the bot
      */
     private Move requestMove(Player player) {
         // Print stack information
         for (Player p : players) {
             player.println(p.getName() + " stack " + p.getStack());
         }
         player.println("Match pot " + pot);
         player.println("Match sidepots [" + sidepot + "]");
         String line = player.go();
         try {
             return Move.valueOf(line);
         } catch (IllegalArgumentException e) {
             return Move.FOLD;
         }
     }
 }
