 package Roma.Cards;
 
 import Roma.*;
 import Roma.PlayerInterfaceFiles.CancelAction;
 import Roma.PlayerInterfaceFiles.PlayerInterface;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 11/04/12
  * Desc:
  */
 public class Consiliarus extends CardBase {
     public final static String NAME = "Consiliarus";
     final static String TYPE = Card.CHARACTER;
     final static String DESCRIPTION = "The player picks up their character cards and can then lay them again " +
             "on any dice disc. Buildings can be covered.";
     final static int COST = 4;
     final static int DEFENCE = 4;
     final static boolean ACTIVATE_ENABLED = true;
 
     public final static int OCCURENCES = 2;
 
     @Override
     public CardHolder makeOne(PlayArea playArea){
         Card card = new Consiliarus(playArea);
         CardHolder cardHolder = new CardHolder(card);
         card.setContainer(cardHolder);
 
         return cardHolder;
     }
 
     public static ArrayList<CardHolder> playSet(PlayArea playArea){
         ArrayList<CardHolder> set = new ArrayList<CardHolder>();
         CardHolder cardHolder;
         Card card;
 
         for(int i = 0; i < OCCURENCES; i++){
             card = new Consiliarus(playArea);
             cardHolder = new CardHolder(card);
             card.setContainer(cardHolder);
             set.add(cardHolder);
         }
 
         return set;
     }
 
     Consiliarus(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
     }
 
     private static int FROM_TO_DIMENSIONS = 2;
 
 
 //    //TODO: refactor for testing reasons
 //    public void activate(Player player, int position) {
 //        boolean activated = true;
 //        DiceDiscs diceDiscs = playArea.getDiceDiscs();
 //
 //        ArrayList<CardHolder> characterCards = diceDiscs.setOfCards(player,CHARACTER);//all the cards
 //
 //        while (!characterCards.isEmpty()){
 //            playArea.printStats();
 //            playerInterface.printCardList(characterCards);
 //            CardHolder card = player.chooseCardIndex(characterCards);
 //            //TODO: Allow players to see the dice discs while placing their cards
 //            if (card ==null) { //i.e. cancelled
 //                PlayerInterface.printOut("You must choose a card", true);
 //            } else {
 //                int choice = player.getDiceDiscIndex("");
 //                if (choice!=-1){//I.e not cancel
 //                    diceDiscs.layCard(player.getPlayerID(), choice, card);
 //                } else {
 //                    characterCards.add(card);
 //                }
 //            }
 //        }
 //    }
 
     @Override
     public void gatherData(Player player, int position) throws CancelAction{
         ArrayList<Integer> activationData = new ArrayList<Integer>();
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         int[] fromIndices = new int[DiceDiscs.CARD_POSITIONS];
         int[] toIndices = new int[DiceDiscs.CARD_POSITIONS];
 
         //TODO: flesh out
         //Maybe collect which cards player desires to move first
         //then gather where to move the cards too?
         //problem is how to show the changes to the player in progress...
 
         player.commit();
 
         player.setActivationData(activationData);
     }
 
     //activation data should be alternating between fromIndex toIndex
 
     @Override
     public void activate(Player player, int position) {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
        CardManager cardManager = playArea.getCardManager();
         CardHolder[] activeCards = diceDiscs.getPlayerActives(player.getPlayerID());
         ArrayList<Integer> activationData = player.getActivationData();
        ArrayList<Integer> fromIndices = new ArrayList<Integer>();
        ArrayList<Integer> toIndices = new ArrayList<Integer>();
        ArrayList<CardHolder> cardList = new ArrayList<CardHolder>();
 
        while(!activationData.isEmpty()){
            fromIndices.add(activationData.remove(0));
            toIndices.add(activationData.remove(0));
        }

        for(int i : fromIndices){
            cardList.add(activeCards[i]);
            activeCards[i] = null;
        }

        for(int i : toIndices){
            if(activeCards[i] != null){
                cardManager.discard(activeCards[i]);
            }
            activeCards[i] = cardList.remove(0);
        }
     }
 
     @Override
     public void discarded() {
         //do nothing when discarded
     }
 }
