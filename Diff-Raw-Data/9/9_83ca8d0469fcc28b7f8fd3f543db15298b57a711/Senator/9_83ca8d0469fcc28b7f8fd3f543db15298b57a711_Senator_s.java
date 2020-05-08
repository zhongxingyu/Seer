 package Roma.Cards;
 
 import Roma.DiceDiscs;
 import Roma.PlayArea;
 import Roma.Player;
 import Roma.PlayerInterfaceFiles.CancelAction;
 import Roma.PlayerInterfaceFiles.PlayerInterface;
 import Roma.Roma;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 11/04/12
  * Desc:
  */
 public class Senator extends CardBase {
     private static int COST_SCALE = 0;
     public final static String NAME = "Senator";
     final static String TYPE = Card.CHARACTER;
     final static String DESCRIPTION = "Enables the player to lay as many character cards as " +
             "they wish free of " +
             "charge. The player is allowed to cover any cards.";
     final static int COST = 3;
     final static int DEFENCE = 3;
     final static boolean ACTIVATE_ENABLED = true;
 
     public final static int OCCURENCES = 2;
 
     @Override
     public CardHolder makeOne(PlayArea playArea) {
         CardBase card = new Senator(playArea);
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
             card = new Senator(playArea);
             cardHolder = new CardHolder(card, playArea);
             card.setContainer(cardHolder);
             card.setCardHolder(cardHolder);
             set.add(cardHolder);
         }
 
         return set;
     }
 
     Senator(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
 
     }
 
     @Override
     public void gatherData(Player player, int position) throws CancelAction {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         ArrayList<Integer> activationData = player.getActivationData();
         ArrayList<CardHolder> hand = player.getHand();
         int[] handIndices = new int[hand.size()];
         int[] discIndices = new int[hand.size()];
         int handIndex = 0;
         int discIndex = 0;
         CardHolder[][] activeCards = diceDiscs.getActiveCards();
         CardHolder[][] newActiveCards = new CardHolder[Roma.MAX_PLAYERS][DiceDiscs.CARD_POSITIONS];
 
         for(int i = 0; i < Roma.MAX_PLAYERS; i++){
            System.arraycopy(activeCards[i], 0, newActiveCards[i], 0, Roma.MAX_PLAYERS);
         }
 
         PlayerInterface.printOut("Play character cards from your hand for free", true);
         if (player.countType(hand, CHARACTER) == 0) {
             PlayerInterface.printOut("No characters in hand!", true);
             player.cancel();
         }
 
         for (int i = 0; i < hand.size(); i++) {
             handIndices[i] = CANCEL;
             discIndices[i] = CANCEL;
         }
 
         //get player input for which cards to lay
         //collect player input
         int i = 0;
         while (handIndex != CANCEL || discIndex != CANCEL) {
             try {
                 handIndex = player.getCardIndex(hand, Card.CHARACTER, handIndices);
                 handIndices[i] = handIndex;
                 discIndex = player.getDiceDiscIndex(activeCards, false, false);
                 discIndices[i] = discIndex;
                 newActiveCards[player.getPlayerID()][discIndex] = hand.get(handIndex);
                 i++;
             } catch (CancelAction cancelAction) {
                 handIndex = CANCEL;
                 discIndex = CANCEL;
             }
         }
 
         for (int j = 0; j < i; j++) {
             activationData.add(handIndices[j]);
             activationData.add(discIndices[j]);
         }
         player.commit();
     }
 
     //activationData: ([cardHandIndex][positionIndex])*repeated as desired
 
     @Override
     public void activate(Player player, int position) {
         CardHolder card;
         ArrayList<CardHolder> hand = player.getHand();
         WrapperMaker wrapperMaker = new WrapperMaker(player.getPlayerID());
         wrapperMaker.setCostScale(COST_SCALE);
         Wrapper wrapper = null;
         ArrayList<Integer> activationData = player.getActivationData();
         ArrayList<Integer> handIndices = new ArrayList<Integer>();
         ArrayList<Integer> discIndices = new ArrayList<Integer>();
         ArrayList<CardHolder> cards = new ArrayList<CardHolder>();
         int discIndex;
 
         //wrap building cards in hand with costScale = 0 modifier
         for (int i = 0; i < hand.size(); i++) {
             card = hand.get(i);
             if (card.getType().equalsIgnoreCase(Card.CHARACTER)) {
                 wrapper = wrapperMaker.insertWrapper(card);
 
                 //add wrappers to endActionClear list
                 playArea.addToEndActionList(wrapper);
             }
         }
 
         //retrieve data from activationData
         while (!activationData.isEmpty()) {
             handIndices.add(activationData.remove(0));
             discIndices.add(activationData.remove(0));
         }
 
         //get cards from hand without removing to preserve indices
         for (int i : handIndices) {
             cards.add(hand.get(i));
         }
 
         //remove cards chosen from hand
         hand.removeAll(cards);
 
         for (int i = 0; i < cards.size(); i++) {
             card = cards.get(i);
             discIndex = discIndices.get(i);
             player.layCard(card, discIndex);
         }
     }
 }
