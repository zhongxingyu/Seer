 package com.buildcoolrobots.games.pgcgame.CoreTypes;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.InstanceNotFoundException;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector2;
 
 public class BGSprite extends Sprite {
 	private static BGSprite _currentBG = null;
 	
 	Vector2 Speed = new Vector2(1,0);
 	
 	public BGSprite(Texture texture) throws InstanceAlreadyExistsException {
 		super(texture);

 		if(_currentBG != null) {
 			throw new InstanceAlreadyExistsException("This class is a singleton; use BGSprite.getInstance()");
 		}
 		
 		_currentBG = this;
 	}
 	
 	public static BGSprite getInstance() throws InstanceNotFoundException {
         if(_currentBG == null)
         {
             throw new InstanceNotFoundException("BGSprite is not initialized. Please create a new instance.");
         }
 
         return _currentBG;
       
 	}
 	
 	
 	public void LoadContent(Vector2 position, Texture background) {
 		this.setPosition(position.x, position.y);
 		this.setTexture(background);
 	}
 	
 	public void render() {
 		this.setPosition(this.getX() - Speed.x, this.getY());
 		
		if (this.getX() + this.getTexture().getWidth() <= this.getWidth()) {
			this.setX(0);
		}
 	}
 	
 }
