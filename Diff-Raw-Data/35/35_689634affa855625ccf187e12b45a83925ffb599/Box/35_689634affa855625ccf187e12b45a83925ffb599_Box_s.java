 import java.io.Serializable;
 
 /**
  * This class represents the boxes inside or around a room or hallway.
  * @author Jordan Becker
  * @author Robin Kebaili
  * @author Théo Gerriet
  * @version 1
  *
  */
 public class Box implements Serializable {
 
 	//Fields
 		
 		/**
 		 * This char represents the box into the dungeon.
 		 * For example '.' for floor, '|' or '_' for wall, '#' for corridor, '^' for trap, '+' for room-door, '%' for level-door
 		 */
 		private char rep;
 		
 		/**
 		 * Defines if the current box is walkable by any actor
 		 */
 		private boolean walkable;
 		
 		/**
 		 * Not sure if we will keep this field, but tells if the player already walked on this box
 		 */
 		private boolean walked;
 		
 		/**
 		 * Defines if the current box is whether hidden or not
 		 */
 		private boolean visible;
 		
 		/**
 		 * Defines if the current box has an Actor in it, by default it has no Actor
 		 */
 		private Actor actor = null;
 		
 		/**
 		 * Defines if the current box has an Item in it, by default it has no Item
 		 */
 		private Item item = null;
 		
 		/**
 		 * Constant box which represents a horizontal wall. Used for room generation.
 		 */
 		public final static Box HORIZONTAL_WALL = new Box('-',false,false);
 		
 		/**
 		 * Constant box which represents a vertical wall. Used for room generation.
 		 */
 		public final static Box VERTICAL_WALL = new Box('|',false,false);
 		
 		/**
 		 * Constant box which represents a floor. Used for room generation.
 		 */
 		public final static Box FLOOR = new Box ('.',true,false);
 		
 		/**
 		 * Constant box which represents a door. Used for room generation.
 		 */
 		public final static Box DOOR = new Box('+',true,false);
 		
 		/**
 		 * Constant box which represents a invisible door. Used for room generation
 		 */
 		
 		/**
 		 * Constant box which represents a stair. Used for room generation.
 		 */
 		public final static Box STAIRS = new Box('%',true,false);
 		
 		/**
 		 * Constant box which represents a corridor. Used for room generation.
 		 */
 		public final static Box CORRIDOR = new Box('#',true,false);
 		
 		
 		
 	//Constructor
 		/**
 		 * This empty constructor builds a default box, which is represented by ' ' (space), not walkable and not visible
 		 */
 		public Box() {
 			this.rep=' ';
 			this.walkable=false;
 			this.visible=false;
 		}
 		
 		/**
 		 * This constructor builds a box which is represented by r, walkable regarding w and visible regarding v
 		 * @param r Char, defines the current representation of the box
 		 * @param w Boolean, defines the walkable state of the box
 		 * @param v Boolean, defines the visible state of the box
 		 */
 		public Box(char r, boolean w, boolean v) {
 			this.rep=r;
 			this.walkable=w;
 			this.visible=v;
 		}
 	
 		/**
 		 * Build a Box with the same characteristics of the box in parameter.
 		 * @param b copied box
 		 */
 		public Box(Box b){
 			if (b != null){
 				this.rep = b.getRep();
 				this.walkable = b.isWalkable();
 				this.visible = b.isVisible();
 			}
 		}
 		
 	//Methods
 		
 		/**
 		 * Tells the current state of walkableness of the box.
 		 * Returns TRUE if the current box is walkable by the player
 		 * @return Boolean, TRUE if the current box is walkable, FALSE if the current box is not walkable.
 		 */
 		public boolean isWalkable() {
 			return this.walkable;
 		}
 		
 		/**
 		 * Sets the walkable state of the current box
 		 * @param walkable, boolean that will define the walkableness of the box (TRUE if yes, FALSE if not)
 		 */
 		public void setWalkable(boolean walkable) {
 			this.walkable=walkable;
 		}
 		
 		/**
 		 * Tells if the current box is whether visible or invisible
 		 * Returns TRUE if the current box is walkable by the player
 		 * @return Boolean, TRUE if the current box is visible, FALSE if the current box is not visible
 		 */
 		public boolean isVisible() {
 			return this.visible;
 		}
 		
 		/**
 		 * Sets the walkable state of the current box
 		 * @param visible, boolean that will define the visibility of the box (TRUE if yes, FALSE if not)
 		 */
 		public void setVisible(boolean visible) {
 			this.visible=visible;
 		}
 		
 		/**
 		 * Tells if the current box has whether been walked by or not
 		 * Returns TRUE if the current box has been walked by the player, FALSE if not
 		 * @return Boolean, TRUE if the current box has been walked in by the player, FALSE if the current box has not been walked by the player yet
 		 */
 		public boolean isWalked() {
 			return this.walked;
 		}
 		
 		/**
 		 * Sets the walked state of the current box
 		 * @param walked, boolean that will define the walked state of the box (TRUE if yes, FALSE if not)
 		 */
 		public void setWalked(boolean walked) {
 			this.walked=walked;
 		}
 		
 		/**
 		 * @return the actor
 		 */
 		public Actor getActor() {
 			return actor;
 		}
 
 		/**
 		 * @param actor the actor to set
 		 */
 		public void setActor(Actor actor) {
 			this.actor = actor;
 			if(actor != null)
 				setWalkable(false);
 			else
 				setWalkable(true);
 			
 		}
 
 		/**
 		 * @return the item
 		 */
 		public Item getItem() {
 			return item;
 		}
 
 		/**
 		 * @param item the item to set
 		 */
 		public void setItem(Item item) {
 			this.item = item;
 		}
 
 		/**
 		 * Getter for the rep field of the box.
 		 * @return Charater which represents the box
 		 */
 		public char getRep(){
 			if(actor!=null)
 				return actor.getRepresentation();
 			if(item!=null)
 				return item.getRepresentation();
 			return this.rep;
 		}
 		
 		public char getRealRep(){
 			return rep;
 		}
 		
 		/**
 		 * Returns the representation of the box as a String
 		 */
 		public String toString() {
 			String res=new String(""+rep);
 			return res;
 		}
 }
 
