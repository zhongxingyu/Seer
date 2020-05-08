 package com.secondhand.controller.model;
 
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 
 import com.secondhand.model.Player;
 import com.secondhand.model.powerup.PowerUp;
 
public final class PlayerController {
	
	private PlayerController() {}
 
 	public static TimerHandler createTimer(final Player player, final PowerUp powerUp) {
 		return new TimerHandler(powerUp.getDuration(), new ITimerCallback() {
 			private Player user = player; 
 			@Override
 			public void onTimePassed(final TimerHandler pTimerHandler) {
 				if (user.getPowerUps().contains(powerUp))
 					user.removePowerUp(powerUp);
 				//if (!powerUp.hasAnother(player)) {
 				// TODO: Unattach the powerups texture from player (ex: shield makes the player glow)
 				//}
 			}
 		});
 	}
 }
