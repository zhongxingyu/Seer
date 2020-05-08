 package entities.npcs;
 
 import map.Cell;
 
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 
 import entities.NonPlayableEntity;
 
 public class NPC extends NonPlayableEntity{
 
	public NPC(Cell currentCell, Rectangle hitbox, int maxhealth) {
		super(currentCell, hitbox, maxhealth);
 	}
 	
 	@Override
 	protected NPC clone() {
		return new NPC(currentCell, new Rectangle(getX(), getY(), getWidth(), getHeight()),getMaxHealth());
 	}
 
 	public void update(Input input, int delta) {
 		//TODO
 	}
 	
 	public void render() {
 		//TODO
 	}
 	
 }
