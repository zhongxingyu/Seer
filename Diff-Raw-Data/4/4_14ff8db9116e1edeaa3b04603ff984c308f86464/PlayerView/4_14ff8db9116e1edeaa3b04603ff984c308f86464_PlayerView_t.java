 package com.secondhand.view.entities;
 
 import java.beans.PropertyChangeEvent;
 
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.secondhand.model.Player;
 import com.secondhand.view.resource.Sounds;
 
 // what? the controller should be handling the PropertyChangeListener, not the view!
 public class PlayerView extends BlackHoleView {
 
 	public PlayerView(final PhysicsWorld physicsWorld, final Player player){
 		super(physicsWorld, player);
 	}
 	
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 		final String propertyName = event.getPropertyName();
 		final Player player =  (Player) getEntity();
 		
		if (propertyName.equals(Player.POWER_UP_SOUND)) {
 			Sounds.getInstance().powerUpSound.play();
 		}else if (propertyName.equals(Player.ADD_POWER_UP)) {
 			this.shape.setColor(1f, 0, 0);
 		} else if (propertyName.equals(Player.REMOVE_POWER_UP)) {
 			if (player.getPowerUps().isEmpty()) {
 				this.shape.setColor(1f, 1f, 1f);
 			}
 		} else if (propertyName.equals(Player.GROW_SOUND)) {
 			Sounds.getInstance().growSound.play();
 		} else if (propertyName.equals(Player.BIGGER_ENTITY_COLLISION_SOUND)) {
 			Sounds.getInstance().obstacleCollisionSound.play();
 		}  else if (propertyName.equals(Player.PLAYER_KILLED_SOUND)) {
 			Sounds.getInstance().playerKilledSound.play();
 		}
 	}
 
 }
