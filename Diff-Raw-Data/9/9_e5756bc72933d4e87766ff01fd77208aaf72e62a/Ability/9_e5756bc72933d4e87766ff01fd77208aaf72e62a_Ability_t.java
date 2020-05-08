 package com.vgdc.merge.entities.abilities;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.math.Vector2;
 import com.vgdc.merge.entities.Entity;
 import com.vgdc.merge.entities.EntityData;
 import com.vgdc.merge.entities.Projectile;
 
 public abstract class Ability {
 
 	public String itemName;
 	private Vector2 offset = new Vector2();
 	
 	public void onUse(Entity entity)
 	{
 		onUse(entity, false);
 	}
 	
 	public abstract void onUse(Entity entity, boolean retrievable);
 	
 	public Projectile createProjectile(EntityData data, Entity entity, boolean retrievable)
 	{
 		Projectile projectile = new Projectile(data, entity.getWorld());
 		ArrayList<Ability> abilities = projectile.getAbilities();
 		if(retrievable)
 		{
 			if(abilities.size()==0)
 				abilities.add(this);
 			else
 				abilities.set(0, this);
 		}
 		projectile.setTeam(entity.getTeam());
 		boolean flip = entity.getRenderer().isFlipped();
 		projectile.setPosition(new Vector2(entity.getPosition().x+(flip ? - offset.x : offset.x), entity.getPosition().y+offset.y));
 		projectile.getRenderer().flip(flip);
		projectile.setFacingLeft(entity.facingLeft());
 		entity.getWorld().getEntityManager().addEntity(projectile);
 		return projectile;
 	}
 
 }
