 package com.jpii.navalbattle.turn;
 
 import java.util.ArrayList;
 
 import com.jpii.navalbattle.game.NavalGame;
 import com.jpii.navalbattle.game.NavalManager;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.game.entity.MoveableEntity;
 
 public class AI extends Player{
 	
 	NavalManager nm;
 	ArrayList<Entity> enemies;
 	
 	public AI(NavalManager nm,String name) {
 		super(name);
 		enemies = new ArrayList<Entity>();
 		this.nm = nm;
 	}
 	
 	public void addEnemyEntity(Entity e){
 		enemies.add(e);
 	}
 	
 	
 	public void takeTurn(){
 		for(int k = 0; k < getTotalEntities(); k++)
 		{
 			Entity ent = getEntity(k);
 			MoveableEntity currentEntity;
 			if(ent.getHandle()%10 == 1){
 				currentEntity = (MoveableEntity)ent;
 				if(currentEntity.getHandle()==21){
 					//AC
 					for (int x = 0; x < (currentEntity.getMovementLeft() * 2) + 1; x++) {
 						for (int y = 0; y < (currentEntity.getMovementLeft() * 2) + 1; y++) {
 							Entity location = currentEntity.getManager().findEntity(y, x);
 							Player temp = NavalGame.getManager().getTurnManager().findPlayer(location); 
 							if(!(temp.equals(this))&&!enemies.contains(location)){
 								//entity at spot is not owned by this AI
 								addEnemyEntity(location);
 							}
 						}
 					
 					
 				}
 				}
 				if(currentEntity.getHandle()==11){
 					//Sub
 				}
 				if(currentEntity.getHandle()==31){
 					//BS
 				}
 			}
 			
 		}
 		turnOver=true;
 	}
 	
 	public void endTurn(){
 		super.endTurn();
 	}
 }
