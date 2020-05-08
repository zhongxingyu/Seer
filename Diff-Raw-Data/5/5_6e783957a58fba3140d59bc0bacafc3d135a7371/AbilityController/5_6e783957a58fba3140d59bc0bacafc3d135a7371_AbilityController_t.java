 package com.vgdc.merge.entities.controllers;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.math.Vector2;
 import com.vgdc.merge.entities.Entity;
 import com.vgdc.merge.entities.EntityData;
 import com.vgdc.merge.entities.EntityType;
 import com.vgdc.merge.entities.Item;
 import com.vgdc.merge.entities.abilities.Ability;
 import com.vgdc.merge.entities.physics.MovingBody;
 import com.vgdc.merge.world.World;
 
 public abstract class AbilityController extends Controller {	
 	public void onDeath() {
 		Entity e = getEntity();
 		World world = e.getWorld();
 		e.delete();
 		ArrayList<Ability> a = e.getAbilities();
 		if (a != null && a.size() > 0 && a.get(0) != null) {
 			System.out.println("drop it!");
 			String itemName = a.get(0).itemName;
 			if (itemName != null) {
 				EntityData itemData = world.getAssets().entityDataMap.get(itemName);
 				Item item = new Item(itemData, world);
 				world.getEntityManager().addEntity(item);
 				MovingBody body = item.getMovingBody();
 				item.getPhysicsBody().setCollidableWith(EntityType.Item, false);
 				item.getPhysicsBody().setCollidableWith(EntityType.Projectile, false);
 				body.setPosition(e.getPosition().cpy());
 				body.setElasticity(.5f);
 				Vector2 vel = e.getMovingBody().getVelocity();
 				if(vel.x > 0){
					body.setVelocity(new Vector2(10,20));
 				}
 				else{
					body.setVelocity(new Vector2(-10,20));
 				}
 
 				((ItemController) item.getController()).setRelocating(true);
 				//item.getMovingBody().setVelocity(new Vector2(e.getMovingBody().getVelocity()));
 			}
 		}
 		
 	}
 }
