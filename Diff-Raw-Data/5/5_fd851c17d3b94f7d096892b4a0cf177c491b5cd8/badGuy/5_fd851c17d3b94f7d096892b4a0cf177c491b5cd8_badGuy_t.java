 package net.castegaming.game.entities;
 
 import net.castegaming.game.enums.EntityType;
 
public class BadGuy extends Enemy{
 
	public BadGuy(EntityType type, double x, double y) {
 		super(EntityType.BADGUY, x, y);
 		onSpawn();
 	}
 
 	@Override
 	public void onSpawn() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onTouch() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void update() {
 		// TODO Auto-generated method stub
 		
 	}
 }
