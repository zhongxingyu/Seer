 package com.secondhand.model.entity;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 import java.util.Stack;
 
 
 // Manages all the entities of the GameWorld
 class EntityManager implements PropertyChangeListener{
 	
 	private List<Entity> entityList;
 	private List<Enemy> enemyList;
 	private final Stack<Entity> scheduledForDeletionEntities;
 
 	private Player player;
 	
 	public EntityManager() {
 		this.scheduledForDeletionEntities = new Stack<Entity>();
 	}
 
 	public void setPlayer(final Player player) {
 		this.player = player;
 		// no need to listen to player, because the player is never deleted,
 		// it is merely repositioned when eaten up.
 	}
 	
 	public void setEntityList(final List<Entity> entityList) {
 		this.entityList = entityList;
 		for(final Entity entity:entityList){
 			entity.addListener(this);
 		}
 	}
 	
 	public void setEnemyList(final List<Enemy> enemyList) {
 		this.enemyList = enemyList;
 	}
 	
 	public List<Entity> getEntityList() {
 		return entityList;
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 	
 	private void moveEnemies() {
 		// enemies are in both lists because we want them
 		// for easy access and for the posibility of attacking
 		// each other. it could be preferable to change it later
 		// if we can come up with a better way
 		for (final Enemy enemy : enemyList) {
 			enemy.moveEnemy(player, entityList);
 		}
 	}
 	
 	public void updateEntities() {
 		// remove bodies scheduled for deletion.
 		while(!scheduledForDeletionEntities.empty()) {
 				final Entity entity = scheduledForDeletionEntities.pop();
 				
 				entity.removeListener(this);
 				removeEntityFromList(entity);
 				if(entity instanceof Enemy)
 					removeEnemyFromList((Enemy)entity);
 				entity.deleteBody();
 		}
 		
		player.updateSize();
 		moveEnemies();
 		this.player.moveToNeededPositionIfNecessary();
 	}
 	
 	public void removeEntityFromList(final Entity entity) {
 		this.entityList.remove(entity);
 	}
 	
 	private void removeEnemyFromList(final Enemy enemy) {
 		this.enemyList.remove(enemy);
 	}
 	
 	private void scheduleEntityForDeletion(final Entity entity) {
 		this.scheduledForDeletionEntities.add(entity);
 	}
 
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 		if(event.getPropertyName().equalsIgnoreCase(Entity.IS_SCHEDULED_FOR_DELETION)){
 			scheduleEntityForDeletion((Entity) (event.getNewValue()));
 		}
 	}
 	
 	// remove the property change listeners from all the entities.
 	public void unregisterFromEntities() {
 		for(Entity entity: this.entityList) {
 			entity.removeListener(this);
 		}
 	}
 	
 }
