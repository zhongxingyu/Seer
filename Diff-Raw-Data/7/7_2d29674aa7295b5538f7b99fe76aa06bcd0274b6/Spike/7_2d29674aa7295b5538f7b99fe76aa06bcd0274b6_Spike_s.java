 package com.teamcoffee.game.models;
 
 import com.badlogic.gdx.math.Vector2;
 import com.teamcoffee.game.views.Audio;
 import com.teamcoffee.game.views.Level;
 
 public class Spike extends Entity{
 	
 	public Spike(Vector2 position, float width, float height){
 		super(position, width, height, "data/Spikes.png", 1, 1);
 	}
 
 	@Override
 	public void onCollide(int direction, MoveableEntity a, Level level) {
<<<<<<< HEAD
		float locationX = this.getPosition().x-5;
=======
 		Audio.death();
		float locationX = this.getPosition().x;
>>>>>>> cd34fe868046bc03d1070581ce589c0b698f39ed
 		float locationY = this.getPosition().y + this.getHeight();
 		level.addEntity(new Corpse(new Vector2(locationX, locationY),96,48));
 		level.respawnPlayer();
 	}
 
 
 }
