 package Roma.Cards;
 
 import Roma.DiceDiscs;
 import Roma.PlayArea;
 import Roma.Player;
 import Roma.PlayerInterfaceFiles.CancelAction;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 19/05/12
  * Desc:
  */
 public class GrimReaper extends CardBase {
     public final static String NAME = "Grimreaper";
     final static String TYPE = Card.CHARACTER;
     final static String DESCRIPTION = "Once placed on a disc this card provides a chance to cheat death. " +
             "The player's other character cards are returned to the player's hand rather than to the discard pile " +
             "whenever they are successfully attacked and defeated by the opponent..";
     final static int COST = 6;
     final static int DEFENCE = 3;
     final static boolean ACTIVATE_ENABLED = false;
     private GrimWrapperMaker grimWrapperMaker;
 
     public final static int OCCURENCES = 1;
 
     public GrimReaper(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
     }
 
     @Override
     public CardHolder makeOne(PlayArea playArea) {
         CardBase card = new GrimReaper(playArea);
         CardHolder cardHolder = new CardHolder(card, playArea);
         card.setContainer(cardHolder);
         card.setCardHolder(cardHolder);
 
         return cardHolder;
     }
 
     public static ArrayList<CardHolder> playSet(PlayArea playArea) {
         ArrayList<CardHolder> set = new ArrayList<CardHolder>();
         CardHolder cardHolder;
         CardBase card;
 
         for (int i = 0; i < OCCURENCES; i++) {
             card = new GrimReaper(playArea);
             cardHolder = new CardHolder(card, playArea);
             card.setContainer(cardHolder);
             card.setCardHolder(cardHolder);
             set.add(cardHolder);
         }
 
         return set;
     }
 
     @Override
     public void gatherData(Player player, int position) throws CancelAction {
         System.err.println("Turris being activated somehow!");
     }
 
     @Override
     public void activate(Player player, int position) {
         System.err.println("Turris being activated somehow!");
     }
 
     @Override
     public void enterPlay(Player player, int position) {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         CardHolder[] friendlyCards = diceDiscs.getPlayerActives(player.getPlayerID());
         grimWrapperMaker = new GrimWrapperMaker(player.getPlayerID());
 
         for (int i = 0; i < friendlyCards.length; i++) {
             if (i != position && friendlyCards[i] != null) {
                 grimWrapperMaker.insertWrapper(friendlyCards[i]);
             }
         }
 
         playArea.addToEnterPlayList(grimWrapperMaker);
     }
 
     @Override
     public void leavePlay() {
         ArrayList<Wrapper> wrapperList = grimWrapperMaker.getWrapperList();
 
         for (Wrapper wrapper : wrapperList) {
             wrapper.deleteThisWrapper();
         }
 
         playArea.removeFromEnterPlayList(grimWrapperMaker);
         grimWrapperMaker.clearWrapperList();
         grimWrapperMaker = null;
     }
 
     private class GrimWrapper extends Wrapper {
         public final static String NAME = "Grim Wrapper";
 
         public GrimWrapper(Card card) {
             super(card);
             name = NAME;
         }
 
         @Override
         public void goingToDiscard(int targetPlayerID, int position) {
             DiceDiscs diceDiscs = playArea.getDiceDiscs();
             diceDiscs.returnTarget(targetPlayerID, position);
         }
     }
 
     private class GrimWrapperMaker extends WrapperMaker {
         public GrimWrapperMaker(int playerID) {
             super(playerID);
         }
 
         @Override
         public Wrapper insertWrapper(CardHolder card) {
             Wrapper wrapper = new GrimWrapper(card);
            wrapperList.add(wrapper);
             return wrapper;
         }
     }
 }
