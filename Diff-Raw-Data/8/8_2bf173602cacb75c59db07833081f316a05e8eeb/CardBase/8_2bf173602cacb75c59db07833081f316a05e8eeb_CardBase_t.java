 package Roma.Cards;
 
 import Roma.*;
 import Roma.History.ActionData;
 import Roma.PlayerInterfaceFiles.CancelAction;
 import Roma.PlayerInterfaceFiles.PlayerInterface;
 
 import java.util.ArrayList;
 
 public abstract class CardBase implements Card {
     public final int CANCEL = PlayerInterface.CANCEL;
     private final boolean activateEnabled;
     private final String name;
     private final String type;
     private final String description;
     private final int cost;
     private final int defence;
     private final boolean wrapper = false;
 
     final PlayArea playArea;
     private ArrayList<Integer> playerActions;
     Card container;
 
     public CardBase(String name, String type, String description,
                 int cost, int defense, PlayArea playArea, boolean activateEnabled) {
         this.name = name;
         this.type = type;
         this.description = description;
         this.cost = cost;
         this.defence = defense;
         this.playArea = playArea;
         this.activateEnabled = activateEnabled;
     }
 
     public Card getContents(){
         return null;
     }
     public void setContents(Card card){
         assert(false);
     }
 
     public Card getContainer() {
         return container;
     }
 
     public void setContainer(Card holder) {
         container = holder;
     }
 
     public boolean isWrapper(){
         return wrapper;
     }
 
     public String getName() {
         return name;
     }
 
     public String getType() {
         return type;
     }
 
     public String getDescription() {
         return description;
     }
 
     public int getCost() {
         return cost;
     }
 
     public int getDefense() {
         return defence;
     }
 
     public PlayArea getPlayArea() {
         return playArea;
     }
 
     public String toString() {
         return "Card Name: " + name + "; Type: " + type + "\nDescription: " + description + "\nCost: "
                 + cost + "; Defence: " + defence;
     }
 
     public boolean isActivateEnabled() {
         return activateEnabled;
     }
 
     @Override
     public abstract void gatherData(Player player, int position) throws CancelAction;
     @Override
     public abstract void activate(Player player, int position);
 
     @Override
     public void enterPlay(Player player, int position) {
        //no enter play action by default
     }
 
     @Override
     public void discarded(CardHolder[] playerActiveCards, int position) {
         CardManager cardManager = playArea.getCardManager();
         cardManager.discard(playerActiveCards[position]);
         playerActiveCards[position] = null;
         leavePlay();
     }
 
     @Override
     public void leavePlay() {
         container.leavePlay();
     }
 
     public abstract CardHolder makeOne(PlayArea playArea);
 
     public int otherPlayer(int player){
         return ((player==Roma.PLAYER_ONE)? Roma.PLAYER_TWO: Roma.PLAYER_ONE);
     }
 }
