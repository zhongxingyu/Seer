 package Roma.Cards;
 
 import Roma.*;
 import Roma.PlayerInterfaceFiles.CancelAction;
 import Roma.PlayerInterfaceFiles.PlayerInterface;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 11/04/12
  * Desc: Architectus Card
  */
 public class Architectus extends CardBase {
     public final static String NAME = "Architectus";
     final static String TYPE = Card.CHARACTER;
     final static String DESCRIPTION = "Enables the player to lay as many building cards as they wish free " +
             "of charge. The player is allowed to cover any cards.";
     final static int COST = 3;
     final static int DEFENCE = 4;
     final static boolean ACTIVATE_ENABLED = true;
 
     public final static int OCCURENCES = 2;
 
     @Override
     public CardHolder makeOne(PlayArea playArea){
         Card card = new Architectus(playArea);
         CardHolder cardHolder = new CardHolder(card);
         card.setContainer(cardHolder);
 
         return cardHolder;
     }
 
     public static ArrayList<CardHolder> playSet(PlayArea playArea){
         ArrayList<CardHolder> set = new ArrayList<CardHolder>();
         CardHolder cardHolder;
         Card card;
 
         for(int i = 0; i < OCCURENCES; i++){
             card = new Architectus(playArea);
             cardHolder = new CardHolder(card);
             card.setContainer(cardHolder);
             set.add(cardHolder);
         }
 
         return set;
     }
 
 
     Architectus(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
 
     }
 
     private static int COST_SHIFT = 0;
     private static int COST_SCALE = 0;
     private static int DEFENSE_SHIFT = 0;
     private static int DEFENSE_SCALE = 1;
 
     @Override
     public void gatherData(Player player, int position) throws CancelAction{
         ArrayList<Integer> activationData = new ArrayList<Integer>();
         ArrayList<CardHolder> hand = player.getHand();
         int[] handIndices = new int[hand.size()];
         int[] discIndices = new int[hand.size()];
         int handIndex = 0;
         int discIndex = 0;
 
         player.commit();
 
         //get player input for which cards to lay
         //collect player input
         int i = 0;
         while(handIndex != CANCEL || discIndex != CANCEL){
             try {
                 handIndex = player.getCardIndex(hand, Card.BUILDING, handIndices);
                 handIndices[i] = handIndex;
                 discIndex = player.getDiceDiscIndex("");
                 discIndices[i] = discIndex;
                 i++;
             } catch (CancelAction cancelAction) {
                 handIndex = CANCEL;
                 discIndex = CANCEL;
             }
         }
 
        for(int j = 0; j < i; j++){
            activationData.add(handIndices[j]);
            activationData.add(discIndices[j]);
         }
 
         player.setActivationData(activationData);
     }
 
     @Override
     public void activate(Player player, int position) {
         CardHolder card;
         ArrayList<CardHolder> hand = player.getHand();
         WrapperMaker wrapperMaker = new WrapperMaker(COST_SHIFT, COST_SCALE, DEFENSE_SHIFT, DEFENSE_SCALE);
         Wrapper wrapper = null;
         ArrayList<Integer> activationData = player.getActivationData();
         ArrayList<Integer> handIndices = new ArrayList<Integer>();
         ArrayList<Integer> discIndices = new ArrayList<Integer>();
         ArrayList<CardHolder> cards = new ArrayList<CardHolder>();
         int discIndex;
 
         //wrap building cards in hand with costScale = 0 modifier
         for(int i = 0; i < hand.size(); i++){
             card = hand.get(i);
             if(card.getType().equalsIgnoreCase(Card.BUILDING)){
                 wrapper = wrapperMaker.insertWrapper(card);
 
                 //add wrappers to endActionClear list
                 playArea.addToEndActionList(wrapper);
             }
         }
 
         //retrieve data from activationData
         while(!activationData.isEmpty()){
             handIndices.add(activationData.remove(0));
             discIndices.add(activationData.remove(0));
         }
 
         //get cards from hand without removing to preserve indices
         for(int i : handIndices){
             cards.add(hand.get(i));
         }
 
         //remove cards chosen from hand
         hand.removeAll(cards);
 
         for(int i = 0; i < cards.size(); i++){
             card = cards.get(i);
             discIndex = discIndices.get(i);
             player.layCard(card, discIndex);
         }
     }
 
     @Override
     public void discarded() {
         //do nothing when discarded
     }
 }
