 package com.secondhand.view.entity;
 
 import java.beans.PropertyChangeEvent;
 
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.resource.SoundType;
 import com.secondhand.view.resource.Sounds;
 
 public class PlayerView extends BlackHoleView {
 
 	public PlayerView(final PhysicsWorld physicsWorld, final Player player){
 		super(physicsWorld, player);
 	}
 
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 		super.propertyChange(event);
 		final String propertyName = event.getPropertyName();
 		
 		if (propertyName.equalsIgnoreCase(Player.SOUND)) {
 
 			if(Sounds.getInstance().getPlayerSound(((SoundType)event.getNewValue())) != null){
 				Sounds.getInstance().getPlayerSound((SoundType)(event.getNewValue())).play();
 			}
 
 		}else if (  propertyName.equalsIgnoreCase(Player.COLOR)) {
 			
 			final Player player = (Player)event.getSource();
 			final float[] RGB = player.getRGB();
 			this.shape.setColor(RGB[0], RGB[1], RGB[2]);
 
 		}
 	}
 }
