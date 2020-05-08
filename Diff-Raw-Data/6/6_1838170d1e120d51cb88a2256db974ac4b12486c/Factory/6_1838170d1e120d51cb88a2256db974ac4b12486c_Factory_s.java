 package se.chalmers.kangaroo.model;
 
 import se.chalmers.kangaroo.model.creatures.CrabCreature;
 import se.chalmers.kangaroo.model.iobject.OnOffButton;
 import se.chalmers.kangaroo.model.iobject.RedBlueButton;
 import se.chalmers.kangaroo.model.item.DoubleJumpItem;
 import se.chalmers.kangaroo.model.item.StopTimeItem;
 
 /**
  * Creates tiles or interactiveTiles depending on ID.
  * 
  * @author alburgh
  * 
  */
 public class Factory {
 	private static String INTERACTIVE_TILES = "ABCDEFGH"; // TODO: CHANGE
 	/**
 	 * Creates a simple factory.
 	 */
 	public Factory() {
 		super();
 	}
 
 	/**
 	 * Creates Tiles (or interactiveTiles) based on ID.
 	 * 
 	 * @param i, the id of the tile
 	 * @return the tile created
 	 */
 	public Tile createTile(int i, int x, int y) {
 		if (INTERACTIVE_TILES.contains("" + i)) {
 			return null; // <-interactivetile TODO
 		} else {
 			return new Tile(i, x, y);
 		}
 	}
 	/**
 	 * Creates different items depending on the ID.
 	 * @param i, the id of the item
 	 * @return the item created
 	 */
 	public Item createItem(int i){
 		switch(i){
 		case 101:
 			return new DoubleJumpItem();
 		case 102:
 			return new StopTimeItem(5);
 		default:
 			return null;
 		}
 	}
 	/**
 	 * Creates different creatures depending on the ID.
 	 * @param i, the id of the creature
 	 * @return the creature created
 	 */
 	public Creature createCreature(int i, Position p){
 		switch(i){
 		case 81:
 			return new CrabCreature(p, Direction.DIRECTION_WEST);
 		case 82:
 			return new TurtleCreature(p, Direction.DIRECTION_WEST);
 		default:
 			return null;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param i, the id of the creature
 	 * @return the interactive object created
 	 */
 	public InteractiveObject createIObjects(int i, GameMap gm){
 		switch(i){
 		case 121:
 			return new RedBlueButton(true, gm);
 		case 122:
 			return new OnOffButton(gm);
 		default:
 			return null;
 		}
 	}
 }
