 package Roma;
 
 import Roma.Cards.*;
 import Roma.PlayerInterfaceFiles.CancelAction;
 import Roma.PlayerInterfaceFiles.PlayerInterface;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 /**
  * File Name:
  * Creator: Andrew Lem & Varun Nayyar
  * Date: 19/03/12
  * Desc: This Object models the DiceDiscs of the game
  *
  */
 
 public class DiceDiscs {
     private static final boolean DEBUG = true;
     public static final int BRIBERY_INDEX = 6;
     public static final int CARD_POSITIONS = 7;
     public static final int FIRST_INDEX = 0;
 
     private final PlayArea playArea;
 
     private CardHolder[][] activeCards = new CardHolder[Roma.MAX_PLAYERS][CARD_POSITIONS];
     //Has the card placed on each disc
 
     private ArrayList<ArrayList<Dice>> discs = new ArrayList<ArrayList<Dice>>();
     // Holds the dice on each disc
 
     private ArrayList<Dice> moneyDisc = new ArrayList<Dice>();
     // Holds the dice placed on the money disc
 
     private ArrayList<Dice> cardDisc = new ArrayList<Dice>();
     //Holds the dice placed on the cardDisc
 
     public DiceDiscs(PlayArea playArea) {
         this.playArea = playArea;
         for(int i = 0; i < CARD_POSITIONS; i++){
                 for(int j = 0; j < Roma.MAX_PLAYERS; j++){
                     activeCards[j][i] = null;
                 }
             discs.add(new ArrayList<Dice>());
         }
     }
 
     //Returns an array of activeCards (Cards on DiceDiscs)
     public CardHolder[] getPlayerActives(int playerID){
         return activeCards[playerID];
     }
 
     public CardHolder[][] getActiveCards(){
         return activeCards;
     }
 
     public ArrayList<CardHolder> listActiveCards(){
         ArrayList<CardHolder> cardList = new ArrayList<CardHolder>();
 
         for(int i = 0; i < Roma.MAX_PLAYERS; i++){
             cardList.addAll(Arrays.asList(activeCards[i]));
         }
 
         return cardList;
     }
 
     //Gets all the cards of a certain type ~ Building or Character from the activeCards
     public ArrayList<CardHolder> setOfCards(Player player, String type){
         assert (type.equalsIgnoreCase(Card.BUILDING)||type.equalsIgnoreCase(Card.CHARACTER));
         int playerID = player.getPlayerID();
         ArrayList<CardHolder> set = new ArrayList<CardHolder>();
         for (int i=0; i< activeCards[playerID].length;i++){
             if(activeCards[playerID][i]!=null){ //add if not null
                 set.add(activeCards[playerID][i]);
                 activeCards[playerID][i] = null; // remove cards
             }
         }
         return set;
     }
 
     public ArrayList<CardHolder> toList(int playerID){
         ArrayList<CardHolder> discList= new ArrayList<CardHolder>();
         Collections.addAll(discList, activeCards[playerID]);
         return discList;
     }
 
     //This function allows a Card to be placed on a dice Disc
     public void layCard(Player player, int position, CardHolder newCard) {
         ArrayList<WrapperMaker> enterPlayList = playArea.getEnterPlayList();
         int playerID = player.getPlayerID();
 
         goingToDiscard(player.getPlayerID(), position);
         for(WrapperMaker wrapperMaker : enterPlayList){
             if(wrapperMaker.getOwnerID() == player.getPlayerID()){
                 wrapperMaker.insertWrapper(newCard);
             }
         }
         newCard.enterPlay(player, position);
         activeCards[playerID][position] = newCard;
     }
 
    private void goingToDiscard(CardHolder[] playerActives, int position) {
        //TODO: check if this null check is ok
        //ONly sends to discard if not empty : I think this is Ok :) - VN
//        assert playerActives[position]!=null;
        if (playerActives[position]!=null){
            playerActives[position].goingToDiscard(playerActives, position);
         }
     }
 
     public boolean gatherData(Player player, int position) throws CancelAction {
         int playerID = player.getPlayerID();
         boolean activateEnabled = false;
         CardHolder targetCard = activeCards[playerID][position];
 
         if(targetCard != null){
             // There is a card there
             PlayerInterface.printOut("Card activating: " + activeCards[playerID][position].getName() + "...", true);
             activateEnabled = activeCards[playerID][position].isActivateEnabled();
             // Can it be activated(Eg Turris is passive) or is it blocked
 
             if(activateEnabled){
                 targetCard.gatherData(player, position);
             } else {
                 PlayerInterface.printOut("That card can't be activated", true);
             }
         } else {
             PlayerInterface.printOut("No card there!", true);
         }
 
         return activateEnabled;
     }
 
     public boolean planBriberyDisc(Player player, int dieValue) throws CancelAction{
         boolean activateEnabled = false;
         MoneyManager moneyManager = playArea.getMoneyManager();
         int position = BRIBERY_INDEX;
         if(moneyManager.enoughMoney(player.getPlayerID(), dieValue)){
             activateEnabled = gatherData(player, position);
         }
         return activateEnabled;
     }
 
     public void useBriberyDisc(Player player, int position, Dice die){
         MoneyManager moneyManager = playArea.getMoneyManager();
         moneyManager.loseMoney(player.getPlayerID(), die.getValue());
         activateCard(player, position, die);
     }
 
     public void activateCard(Player player, int position, Dice die) {
         discs.get(position).add(die);
         activeCards[player.getPlayerID()][position].activate(player, position);
     }
 
     public String getCardName(int player, int position){
         String cardName;
 
         if(activeCards[player][position] != null){
             cardName = activeCards[player][position].getName();
         } else {
             cardName = "";
         }
 
         return cardName;
     }
 
     public ArrayList<Dice> checkForDice(int playerID, int position){
         ArrayList<Dice> dicePresent = new ArrayList<Dice>();
         ArrayList<Dice> disc = discs.get(position);
 
         if(!disc.isEmpty()){
             for(Dice die : disc){
                 if(die.getPlayerID() == playerID){
                     dicePresent.add(die);
                 }
             }
         }
 
         return dicePresent;
     }
 
     public void useMoneyDisc(int playerID, Dice chosenDie) {
         MoneyManager moneyManager = playArea.getMoneyManager();
 
         moneyDisc.add(chosenDie);
         moneyManager.gainMoney(playerID, chosenDie.getValue());
     }
 
     public void useDrawDisc(int playerID, Dice chosenDie) {
         cardDisc.add(chosenDie);
     }
 
     public CardHolder getTargetCard(int playerID, int position){
         return activeCards[playerID][position];
     }
 
     public void discardTarget(int playerID, int position){
         CardHolder card = activeCards[playerID][position];
 
         if(card != null){
             card.discarded(playerID, position);
         }
     }
 
     public boolean checkAdjacent(int playerID, int position, String cardName){
         boolean adjacent = false;
 
         if(position > 1){
             if(activeCards[playerID][position - 1] != null && activeCards[playerID][position - 1].getName() == cardName){
                 adjacent = true;
             }
         }
         if(position < 6){
             if(activeCards[playerID][position + 1] != null && activeCards[playerID][position + 1].getName() == cardName){
                 adjacent = true;
             }
         }
 
         return adjacent;
     }
 
     public boolean checkAdjacentDown(int playerID, int position, String cardName){
         boolean adjacent = false;
         if(position > FIRST_INDEX){
             if(activeCards[playerID][position - 1] != null
                     && activeCards[playerID][position - 1].getName().equalsIgnoreCase(cardName)){
                 adjacent = true;
             }
         }
         return adjacent;
     }
 
     public boolean checkAdjacentUp(int playerID, int position, String cardName){
         boolean adjacent = false;
         if(position < BRIBERY_INDEX){
             if(activeCards[playerID][position + 1] != null
                     && activeCards[playerID][position + 1].getName().equalsIgnoreCase(cardName)){
                 adjacent = true;
             }
         }
         return adjacent;
     }
 
     public void returnTarget(int targetPlayerID, int position){
         Player targetPlayer = playArea.getPlayer(targetPlayerID);
         CardHolder targetCard = activeCards[targetPlayerID][position];
 
         targetCard.leavePlay();
         targetPlayer.addCardToHand(targetCard);
         activeCards[targetPlayerID][position] = null;
     }
 
     public void clearPlayerDice(int playerID){
         ArrayList<Dice> disc;
         ArrayList<Dice> removeDice = new ArrayList<Dice>();
 
         for(int i = 0; i < CARD_POSITIONS; i++){
             disc = discs.get(i);
             if(!disc.isEmpty()){
                 for(Dice die : disc){
                     if(die.getPlayerID() == playerID){
                         removeDice.add(die);
                     }
                 }
                 disc.removeAll(removeDice);
             }
         }
     }
 
     public void addDiceToDisc(int targetDisc, Dice die){
         discs.get(targetDisc).add(die);
     }
 
     public boolean battle(int targetPlayerID, int target, int battleValue){
         boolean kill = false;
         CardHolder targetCard = getTargetCard(targetPlayerID, target);
         int defense = targetCard.getDefense();
 
         PlayerInterface.printOut("BATTLE!", true);
         PlayerInterface.printOut("Defense to beat: " + defense, true);
         PlayerInterface.printOut("You rolled a: " + battleValue, true);
 
         if(battleValue >= defense){
             discardTarget(targetPlayerID, target);
             kill = true;
             PlayerInterface.printOut("Victory!", true);
         } else {
             PlayerInterface.printOut("Defeat!", true);
         }
 
         return kill;
     }
 }
