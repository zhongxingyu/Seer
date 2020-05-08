 package Roma;
 
 import Roma.Cards.*;
 import Roma.History.ActionData;
 import Roma.PlayerInterfaceFiles.CancelAction;
 import Roma.PlayerInterfaceFiles.PlayerInterface;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 
 public class Player {
     public final int CANCEL = PlayerInterface.CANCEL;
     private final String name;
     private int playerID;
 
     private PlayArea playArea;
     private ArrayList<CardHolder> hand = new ArrayList<CardHolder>();
     private ArrayList<Dice> freeDice;
     private PlayerInterface playerInterface;
     private ActionData currentAction;
 
 
     //static constructors
     public static Player makeRealPlayer(int playerID, PlayArea playArea){
         return new Player(playerID, playArea);
     }
 
     public static Player makeDummyPlayer(int playerID, PlayArea playArea){
         return new Player(playerID, playArea, true);
     }
 
     private Player(int playerID, PlayArea playArea) {
         this.playArea = playArea;
         this.playerID = playerID;
         playerInterface = playArea.getPlayerInterface();
         this.name = playerInterface.getPlayerName(playerID);
     }
 
     private Player(int playerID, PlayArea playArea, boolean testing){
         this.playArea = playArea;
         this.playerID = playerID;
         playerInterface = playArea.getPlayerInterface();
         this.name = "dummyPlayer" + playerID;
     }
 
 
     //Simple Getters
 
     public String getName() {
         return name;
     }
 
     public int getPlayerID() {
         return playerID;
     }
 
     public ArrayList<CardHolder> getHand() {
         return hand;
     }
 
     public PlayerInterface getPlayerInterface() {
         return playerInterface;
     }
 
     public ArrayList<Dice> getFreeDice() {
         return freeDice;
     }
 
     public ActionData getCurrentAction() {
         return currentAction;
     }
 
     public void setActivationData(ArrayList<Integer> activationData){
         currentAction.setActivationData(activationData);
     }
 
     public ArrayList<Integer> getActivationData(){
         return currentAction.getActivationData();
     }
 
     public int getOtherPlayerID(){
         return (getPlayerID() + 1) % Roma.MAX_PLAYERS;
     }
 
 
     //small simple functions
 
     public void addCardToHand(CardHolder c){
         if(c!=null) hand.add(c);
     }
 
     public void addCardListToHand(ArrayList<CardHolder> cardList){
         hand.addAll(cardList);
     }
 
     public int handSize() {
         return hand.size();
     }
 
     public void commit() throws CancelAction{
         final String strPrompt = "Commit to this action?";
         final String strOption1 = "Ok";
 
         int option;
 
         option = playerInterface.readInput(strPrompt, true, strOption1);
 
         if(option == 1){
             currentAction.setCommit(true);
         } else if(option == CANCEL){
             cancel();
         }
     }
 
     public void cancel() throws CancelAction{
         throw new CancelAction();
     }
 
     //first action of turn
     public void rollActionDice() {
         final int YES = 1;
         final int NO = 2;
 
         boolean reroll = false;
         boolean validChoice = false;
         int option = CANCEL;
 
         freeDice = playArea.getDiceHolder().rollPlayerDice(playerID);
         playerInterface.printDiceList(freeDice);
 
         if(playArea.getDiceHolder().checkTriple(playerID)){
             while(!validChoice){
                 option = playerInterface.readInput("You rolled a triple, would you like to reroll?",
                         false, "Yes",
                         "No");
                 if(option == YES){
                     reroll = true;
                     validChoice = true;
                 } else if (option == NO){
                     reroll = false;
                     validChoice = true;
                 } else {
                     PlayerInterface.printOut("Please choose either yes or no.", true);
                 }
             }
             if(reroll){
                 rollActionDice();
             }
         }
     }
 
     //Main method that allows players to perform an action
     public boolean planningPhase(ActionData actionData) throws CancelAction{
         //internal #defines
         final String strPrompt = "Select Option:";
         final String strOption1 = "View action dice";
         final String strOption2 = "View Hand";
         final String strOption3 = "Show game stats";
         final String strOption4 = "End turn";
         final int VIEW_ACTION_DIE = 1;
         final int VIEW_HAND = 2;
         final int SHOW_GAME_STATS = 3;
         final int END_TURN = 4;
 
         int option = 0;
         boolean endTurn = false;
 
         currentAction = actionData;
 
         //choose an action
         option = playerInterface.readInput(strPrompt, false, strOption1, strOption2, strOption3, strOption4);
 
         if(option == VIEW_ACTION_DIE){
             currentAction.setUseDice(true);
             viewActionDice();
         } else if(option == VIEW_HAND){
             currentAction.setLayCard(true);
             viewHand();
         } else if(option == SHOW_GAME_STATS){
             playArea.printStats();
         } else if(option == END_TURN){
             endTurn = true;
         } else {
             PlayerInterface.printOut("Please choose a valid option.", true);
         }
 
         return endTurn;
     }
 
     private void viewActionDice() throws CancelAction {
         int chosenDieIndex;
         int chosenDieValue;
 
         chosenDieIndex = getDieIndex(freeDice);
         chosenDieValue = freeDice.get(chosenDieIndex).getValue();
 
         currentAction.setActionDiceIndex(chosenDieIndex);
         currentAction.setDiceValue(chosenDieValue);
 
         useActionDie(chosenDieValue);
     }
 
     //choose a dice disc
     //return int
     public int getDieIndex(ArrayList<Dice> diceList) throws CancelAction{
         final String strPrompt = "Which die do you want to use?";
         String[] diceValues = new String[diceList.size() + 1];
         int diceIndex = CANCEL;
 
         for(int i = 0; i < diceList.size(); i++){
             diceValues[i] = diceList.get(i).getValue().toString();
         }
         diceValues[diceList.size()] = "Cancel";
 
         diceIndex = playerInterface.readIndex(strPrompt, false, diceValues);
 
         if(diceIndex == CANCEL) cancel();
 
         return diceIndex;
     }
 
     private void useActionDie(int chosenDieValue) throws CancelAction {
         final int ACTIVATE_CARD = 1;
         final int BRIBERY = 2;
         final int MONEY = 3;
         final int DRAW_CARD = 4;
         final String strPrompt = "Use on:";
         final String strOption1 = "Activate card";
         final String strOption2 = "Bribery Disc";
         final String strOption3 = "Money Disc";
         final String strOption4 = "Card Disc";
 
         int option = CANCEL;
         boolean validChoice = false;
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         int position = chosenDieValue - 1;
 
         while(!validChoice){
             option = playerInterface.readInput(strPrompt, true, strOption1, strOption2, strOption3, strOption4);
             if(option == ACTIVATE_CARD){
                 if(diceDiscs.gatherData(this, position)){
                     currentAction.setPosition(position);
                     currentAction.setDiscType(ActionData.DICE);
                     currentAction.setCardName(diceDiscs.getCardName(playerID, position));
                     validChoice = true;
                 } else {
                     validChoice = false;
                 }
             } else if(option == BRIBERY){
                 if(diceDiscs.planBriberyDisc(this, chosenDieValue)){
                     position = DiceDiscs.BRIBERY_INDEX;
                     currentAction.setPosition(position);
                     currentAction.setDiscType(ActionData.BRIBERY);
                     currentAction.setCardName(diceDiscs.getCardName(playerID, position));
                     validChoice = true;
                 } else {
                     validChoice = false;
                 }
             } else if(option == MONEY){
                 commit();
                 currentAction.setDiscType(ActionData.MONEY);
                 validChoice = true;
             } else if(option == DRAW_CARD){
                 commit();
                 currentAction.setDiscType(ActionData.CARD);
                 currentAction.setDrawCardIndex(drawCardIndex(chosenDieValue));
                 validChoice = true;
             } else if(option == CANCEL){
                 cancel();
             } else {
                 PlayerInterface.printOut("Please choose a valid action", true);
             }
         }
     }
 
     private int drawCardIndex(int value) {
         ArrayList<CardHolder> tempHand = new ArrayList<CardHolder>();
         CardManager cardManager = playArea.getCardManager();
         int chosenCardIndex = CANCEL;
         boolean validChoice = false;
 
         PlayerInterface.printOut("Drawing " + value + " cards...", true);
 
         tempHand.addAll(cardManager.viewTopCards(value));
         while(!validChoice){
             try {
                 chosenCardIndex = getCardIndex(tempHand,"");
                 validChoice = true;
             } catch (CancelAction cancelAction) {
                 PlayerInterface.printOut("Have to choose a card", true);
             }
         }
 
         return chosenCardIndex;
     }
 
     private void viewHand() throws CancelAction{
         MoneyManager moneyManager = playArea.getMoneyManager();
         int chosenCardIndex = CANCEL;
         int chosenPosition = CANCEL;
 
         chosenCardIndex = getCardIndex(hand, "");
         if(chosenCardIndex == CANCEL) cancel();
         currentAction.setCardIndex(chosenCardIndex);
 
         if(moneyManager.enoughMoney(playerID, hand.get(chosenCardIndex).getCost())){
             PlayerInterface.printOut("Laying card...", true);
         } else {
             cancel();
         }
 
         chosenPosition = getDiceDiscIndex("");
         if(chosenPosition == CANCEL) cancel();
         currentAction.setTargetDisc(chosenPosition);
 
         commit();
     }
 
     //TODO: refactor this to include a filter
     public int getCardIndex(ArrayList<CardHolder> cardList, String type, int... chosenIndices) throws CancelAction{
         return getCardIndex(cardList);
     }
 
 
     public int getCardIndex(ArrayList<CardHolder> cardList) throws CancelAction{
         final String
                 strPrompt = "Possible actions:",
                 strOption1 = "Choose a card",
                 strOption2 = "Check description",
                 strOption3 = "Print card list";
 
         final int
                 CHOOSE_CARDS = 1,
                 CHECK_DESC = 2,
                 PRINT_CARDS = 3;
 
         int choice = CANCEL;
         int action = 0;
         boolean validChoice = false;
 
         playerInterface.printFilteredCardList(cardList);
 
         if(cardList.size() == 0){
             PlayerInterface.printOut("There are no cards!", true);
         } else {
             while(!validChoice){
                 action = playerInterface.readInput(strPrompt, true, strOption1, strOption2, strOption3);
                 if(action == CHOOSE_CARDS){
                     PlayerInterface.printOut("Card number: ", false);
                     choice = playerInterface.getIndex(cardList.size());
                     validChoice = true;
                 } else if(action == CHECK_DESC){
                     checkDesc(cardList);
                 } else if(action == PRINT_CARDS){
                     playerInterface.printFilteredCardList(cardList);
                 } else if(action == CANCEL){
                     cancel();
                 } else {
                     PlayerInterface.printOut("Please choose a valid action", true);
                 }
                 //Now check their choice is valid
 
                 if(choice == CANCEL){
                     //Cancelled
                 } else if (cardList.get(choice)==null){
                     //Card is empty
                     PlayerInterface.printOut("You have chosen an empty card", true);
                 } else if (!cardList.get(choice).getPlayable()){
                     PlayerInterface.printOut("You have made an invalid card choice", true);
                 }
             }
         }
         return choice;
     }
 
     private void checkDesc(ArrayList<CardHolder> cardList){
         int action;
         PlayerInterface.printOut("Check which card number: ", false);
         action = playerInterface.getIndex(cardList.size());
         PlayerInterface.printOut(cardList.get(action).toString(), true);
     }
 
 
 
     //TODO: refactor this to include a filter
     //TODO: refactor to take in a 2D array of CardHolders diceDiscs <- needed for cards that rearrange
     public int getDiceDiscsIndex(CardHolder[][] diceDiscs, boolean filterCurrent, boolean filterOther)
     throws CancelAction{
         assert (diceDiscs[Roma.PLAYER_ONE].length==DiceDiscs.CARD_POSITIONS);
         assert(diceDiscs[Roma.PLAYER_ONE].length == diceDiscs[Roma.PLAYER_TWO].length);
 
         int other = getOtherPlayerID();
 
         ArrayList<CardHolder> currPlayer = new ArrayList<CardHolder>();
        ArrayList<CardHolder> opposingPLayer= new ArrayList<CardHolder>();
 
         Collections.addAll(currPlayer, diceDiscs[playerID]);
        Collections.addAll(opposingPLayer, diceDiscs[other]);
 
         int option = CANCEL;
         final String
                 strPrompt = "Dice Discs:",
                 strOption[] = {"Check Description of your Cards",
                         "Check Description of your Opponent's Card",
                         "Choose Disc"};
 
         final int
                 DESC_OWN = 1,
                 DESC_OPP = 2,
                 CHOOSE_DISC = 3;
         boolean validChoice = false;
         int choice = CANCEL;
 
 
         while(!validChoice){
            playerInterface.printFilteredDiceList(currPlayer, opposingPLayer,
                     filterCurrent, filterOther);
             //Print's out a nice version of the dice lists
             option = playerInterface.readInput(strPrompt, true, strOption);
             if (option == DESC_OWN){
                 checkDesc(currPlayer);
             } else if (option == DESC_OPP){
                checkDesc(opposingPLayer);
             } else if(option == CHOOSE_DISC){
                 PlayerInterface.printOut("Which Disc: ",false);
                 choice = playerInterface.getIndex(DiceDiscs.CARD_POSITIONS);
                 validChoice = true;
             } else if (option==CANCEL){
                 cancel();
             } else {
                 PlayerInterface.printOut("Invalid Input, please try again.", true);
             }
 
            //TODO: Throw an error if they choose an invalid card
         }
         return choice;
     }
 
 
     public int getDiceDiscIndex(String type, int... chosenIndices) throws CancelAction{
         final String strPrompt = "Which disc?";
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         final int NUM_OPTIONS = DiceDiscs.CARD_POSITIONS;
         String [] dicePrompt = new String[NUM_OPTIONS];
 
         for (int i=0; i<NUM_OPTIONS-1;i++){
             dicePrompt[i] = "Dice Disc " + (i+1) + ": " +diceDiscs.getCardName(playerID, i);
         }
         dicePrompt[DiceDiscs.BRIBERY_INDEX] = "Bribery Disc: " + diceDiscs.getCardName(playerID, DiceDiscs.BRIBERY_INDEX);
 
         int option = CANCEL;
 
         boolean validChoice = false;
 
         while(!validChoice){
             option = playerInterface.readInput(strPrompt, true, dicePrompt);
             if(option > 0 && option <= DiceDiscs.CARD_POSITIONS){
                 validChoice = true;
             } else if (option == CANCEL) {
                 cancel();
             } else {
                 PlayerInterface.printOut("Please choose a valid action", true);
             }
         }
         option--;
         return option;
     }
 
     public void performActions(ActionData actionData){
         currentAction = actionData;
         if(actionData.isUseDice()){
             useDice(actionData);
         } else if(actionData.isLayCard()){
             CardHolder chosenCard = hand.remove(actionData.getCardIndex());
             layCard(chosenCard, actionData.getTargetDisc());
         } else {
             System.err.println("WTF action data error!");
             assert(false);
         }
     }
 
     public void layCard(CardHolder chosenCard, int chosenPosition){
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         MoneyManager moneyManager = playArea.getMoneyManager();
 
         moneyManager.loseMoney(playerID, chosenCard.getCost());
         diceDiscs.layCard(playerID, chosenPosition, chosenCard);
     }
 
     private void useDice(ActionData actionData) {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         Dice chosenDie = freeDice.remove(actionData.getActionDiceIndex());
 
         if(actionData.getDiscType().equalsIgnoreCase(ActionData.DICE)){
             diceDiscs.activateCard(this, actionData.getPosition(), chosenDie);
 
         } else if(actionData.getDiscType().equalsIgnoreCase(ActionData.BRIBERY)){
             diceDiscs.useBriberyDisc(this, actionData.getPosition(), chosenDie);
 
         } else if(actionData.getDiscType().equalsIgnoreCase(ActionData.MONEY)){
             diceDiscs.useMoneyDisc(playerID, chosenDie);
 
         } else if(actionData.getDiscType().equalsIgnoreCase(ActionData.CARD)){
             diceDiscs.useDrawDisc(playerID, chosenDie);
             drawCards(chosenDie.getValue(), actionData.getDrawCardIndex());
 
         } else {
             System.err.println("WTF action data error!");
             assert(false);
         }
     }
 
 
 
     //input value
     //
 
     //Or maybe just have a function that requests a number from the player?
     //With "autoResponse" values when in testing mode?
     public void drawCards(int value, int cardDrawIndex) {
         ArrayList<CardHolder> tempHand = new ArrayList<CardHolder>();
         CardManager cardManager = playArea.getCardManager();
         CardHolder chosenCard = null;
 
         for (int i = 0; i < value; i++) {
             tempHand.add(cardManager.drawACard());
         }
 
         chosenCard = tempHand.remove(cardDrawIndex);
         cardManager.discard(tempHand);
 
         hand.add(chosenCard);
     }
 
     public void checkPlayable() {
         MoneyManager moneyManager = playArea.getMoneyManager();
         for (CardHolder card : hand) {
             if (card.getCost() < moneyManager.getPlayerMoney(playerID)) {
                 card.setPlayable(true);
             }
         }
     }
 
     public int countType(ArrayList<CardHolder> cardList, String type) {
         int count = 0;
 
         for(CardHolder card : cardList){
             if(card.getType().equalsIgnoreCase(type)){
                 count++;
             }
         }
         return count;
     }
 }
