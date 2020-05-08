 package models;
 
 import exception.GameException;
 import util.IdGenerator;
 import util.RichUtil;
 
 /**
  * Player model.
  * 
  * @author GuoLin
  *
  */
 public class Player {
 
     public Integer id;
 
     public String name;
 
     public Role role;
     
     public Integer currentCellId;
 
     public int cash;
     
     private static final int DEFAULT_CASH = 10008;
     
     public Player(String name) {
         this(name, Role.RANDOM);
     }
 
     public Player(String name, Role role) {
         this.id = IdGenerator.generate();
         this.name = name;
         this.role = role;
         this.cash = DEFAULT_CASH;
     }
 
     /**
      * Random a start position for the player in the given map.
      * The cell id would be set to the value of the cell.
      * @param map The given map.
      */
     public void randomStart(GameMap map) {
        currentCellId = RichUtil.randomCell(map).id;
     }
     
     /**
      * Roll the dice and return the value of the dice.
      * A {@link DiceEvent} would be generated in the process.
      * @return The value of the dice.
      */
     public int roll() {
         DiceEvent diceEvent = new DiceEvent();
         Event.events.publish(diceEvent);
         return diceEvent.value;
     }
     
     /**
      * The player move for the given steps.
      * {@link GameException} would be thrown in the following situation(s):
      *     One of the cell on the road cannot be stepped in.
      * A {@link MoveEvent} would be generated in the process.
      * @param step The step to move.
      */
     public void move(int step) {
         while (step-- > 0) {
             currentCell = currentCell.next;
             if (currentCell == null) {
                 throw new GameException("Cannot move to the target cell");
             }
         }
         MoveEvent moveEvent = new MoveEvent(step);
         Event.events.publish(moveEvent);
     }
     
     /**
      * The logic of a player to buy the cell where he currently stands on.
      * {@link GameException} would be thrown in the following situations:
      *     The current cell is cannot be bought.
      *     The player do not have enough cash to afford the cell.
      * A {@link CashChangeEvent} would be generated in the process.
      */
     public void buyCell() {
         if (currentCell == null || !currentCell.canBuy()) {
             throw new GameException("Not a estate cell, cannot buy it");
         }
         EstateCell estateCell = (EstateCell)currentCell;
         if (!canAfford(estateCell)) {
             throw new GameException("Not enough money to buy this cell");
         }
         this.cash -= estateCell.price;
         Event.events.publish(new CashChangeEvent(-estateCell.price, this.name));
         estateCell.changeOwner(this);
     }
     
     /**
      * The player passed the cell which belongs to other player.
      * The cash of current player would be deducted by the price of the cell.
      * The cash of the owner would be added by the same amount of money.
      * You could know that the transaction is free of tax.
      * Two events of {@link CashChangeEvent} would be generated to indicate the changes of the two players in cash.
      */
     public void pass() {
         if (currentCell != null && currentCell.needPass() && !((EstateCell)currentCell).owner.equals(this)) {
             EstateCell estateCell = (EstateCell)currentCell;
             this.cash -= estateCell.price;
             Event.events.publish(new CashChangeEvent(-estateCell.price, this.name));
             estateCell.owner.cash += estateCell.price;
             Event.events.publish(new CashChangeEvent(estateCell.price, estateCell.owner.name));
         }
     }
     
     public boolean canAfford(EstateCell estateCell) {
         return cash >= estateCell.price;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((name == null) ? 0 : name.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Player other = (Player) obj;
         if (name == null) {
             if (other.name != null) {
                 return false;
             }
         } else if (!name.equals(other.name)) {
             return false;
         }
         return true;
     }
     
     static class DiceEvent extends Event {
         
         public int value;
         
         public DiceEvent() {
             this.value = RichUtil.randomDice();
         }
         
     }
     
     static class MoveEvent extends Event {
         
         public final int step;
         
         public MoveEvent(int step) {
             this.step = step;
         }
         
     }
     
     static class CashChangeEvent extends Event {
         
         public final int cashChange;
         
         public final String playerName;
         
         public CashChangeEvent(int cashChange, String playerName) {
             this.cashChange = cashChange;
             this.playerName = playerName;
         }
         
     }
 
 }
