 package entities.npcs;
 
 import map.Cell;
 
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 
 import entities.NonPlayableEntity;
 
 public class NPC extends NonPlayableEntity{
 
	public NPC(Rectangle hitbox, int maxhealth) {
		super(hitbox, maxhealth);
 	}
 	
 	@Override
 	protected NPC clone() {
		return new NPC(new Rectangle(getX(), getY(), getWidth(), getHeight()),getMaxHealth());
 	}
 
 	public void update(Input input, int delta) {
 		//TODO
 	}
 	
 	public void render() {
 		//TODO
 	}
 	
 }
