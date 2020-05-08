 package se.chalmers.kangaroo.model;
 
 import se.chalmers.kangaroo.constants.Constants;
 import se.chalmers.kangaroo.model.creatures.BlackAndWhiteCreature;
 import se.chalmers.kangaroo.model.creatures.BullCreature;
 import se.chalmers.kangaroo.model.creatures.CrabCreature;
 import se.chalmers.kangaroo.model.creatures.SumoCreature;
 import se.chalmers.kangaroo.model.creatures.TurtleCreature;
 import se.chalmers.kangaroo.model.iobject.RedBlueButton;
 import se.chalmers.kangaroo.model.item.DoubleJumpItem;
 import se.chalmers.kangaroo.model.item.ImmortalItem;
 import se.chalmers.kangaroo.model.item.StopTimeItem;
 
 
 /**
  * Creates tiles or interactiveTiles depending on ID.
  * 
  * @author alburgh
  * 
  */
 public class Factory {
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
 		if (Constants.INTERACTIVE_TILES.contains(" " + i+ " ")) {
 			return new InteractiveTile(i, x, y); 
 		}else if(Constants.ITEM_IDS.contains(" "+i+" ")
 				|| Constants.IOBJECTS_IDS.contains(" "+i+" ")
 				|| Constants.CREATURE_IDS.contains(" "+i+" ")){
 			return new Tile(0, x, y);
 		}
 		else {
 		
 			return new Tile(i, x, y);
 		}
 	}
 	/**
 	 * Creates different items depending on the ID.
 	 * @param i, the id of the item
 	 * @return the item created
 	 */
 	public Item createItem(int i, int x, int y){
 		switch(i){
 		case 51:
 			return new DoubleJumpItem(i, x, y);
 		case 102:
 			return new StopTimeItem(i, 5, x, y);
 		case 53:
 			return new ImmortalItem(i, x, y);
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
 		case 111:
 			return new CrabCreature(p);
 		case 112:
 			return new TurtleCreature(p);
 		case 113:
 			return new BlackAndWhiteCreature(p);
 		case 114:
 			return new BullCreature(p);
 		case 115:
 			return new SumoCreature(p);
 		default:
 			return null;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param i, the id of the creature
 	 * @return the interactive object created
 	 */
 	public InteractiveObject createIObjects(int i, int x, int y, GameMap gm){
 		switch(i){
 		case 71:
 			return new RedBlueButton(new Position(x, y), i, gm);
 		case 72:
 			return new RedBlueButton(new Position(x, y), i, gm);
 		default:
 			return null;
 		}
 	}
 }
