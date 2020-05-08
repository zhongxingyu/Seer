 package dungeon.models;
 
 import dungeon.util.Vector;
 
 import java.awt.geom.Rectangle2D;
 import java.io.Serializable;
 
 /**
  * A dropped item that is lying around somewhere in a room.
  *
  * It can be either an amount of money (item == null) or an item (money == 0).
  */
 public class Drop implements Spatial, Serializable {
   public static final int SIZE = 500;
 
   private final int id;
 
   private final Position position;
 
   private final Item item;
 
   private final int money;
 
   public Drop (int id, Position position, Item item, int money) {
     this.id = id;
     this.position = position;
     this.item = item;
     this.money = money;
   }
 
   public int getId () {
     return this.id;
   }
 
   public Position getPosition () {
     return this.position;
   }
 
   /**
    * @return true if the drop is an item drop.
    */
   public boolean isItem () {
     return !this.isMoney();
   }
 
   public Item getItem () {
     return this.item;
   }
 
   /**
    * @return true if the drop is a money drop.
    */
   public boolean isMoney () {
     return this.money != 0;
   }
 
   public int getMoney () {
     return this.money;
   }
 
   @Override
   public Rectangle2D space () {
     return new Rectangle2D.Float(this.position.getX(), this.position.getY(), SIZE, SIZE);
   }
 
   @Override
   public Position getCenter () {
     return new Position(this.position.getVector().plus(new Vector(SIZE / 2, SIZE / 2)));
   }
 
   @Override
   public String toString () {
     if (this.isMoney()) {
       return "Drop#" + this.id + " with " + this.money + " coins";
     } else {
       return "Drop#" + this.id + " with " + this.item;
     }
   }
 
   @Override
   public boolean equals (Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
 
     Drop drop = (Drop)o;
 
    if (id != drop.id) return false;
 
     return true;
   }
 
   @Override
   public int hashCode () {
    return id;
   }
 }
