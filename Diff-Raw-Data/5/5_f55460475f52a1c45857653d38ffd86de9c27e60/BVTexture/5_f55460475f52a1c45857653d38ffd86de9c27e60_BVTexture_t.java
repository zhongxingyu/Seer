 package com.mobi.badvibes.view.graphics;
 
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.mobi.badvibes.view.GameDimension;
 
 public class BVTexture extends Texture {
 
 	protected Vector2 textureDimensions;
 	protected Vector2 scaledDimensions;
 	public BVTexture(FileHandle file) {
 		super(file);
 		textureDimensions = new Vector2(super.getWidth(), super.getHeight());
 		
 		Vector2 scale = GameDimension.Scale();
 		scaledDimensions = textureDimensions.cpy().mul(scale.x, scale.y);
 	}
 	
 	@Override
 	public int getWidth() {
 		return (int)scaledDimensions.x;
 	}
 	
 	@Override
 	public int getHeight() {
 		return (int)scaledDimensions.y;
 	}
 	
 	public Vector2 getTextureDimensions(){ 
 		return textureDimensions.cpy();
 	}
 	
 	public Vector2 centerAt(Vector2 pos){
 		return pos.cpy().div(2).sub(scaledDimensions.cpy().div(2));
 	}
 	
 	public void draw(SpriteBatch spriteBatch, Vector2 pos){
         spriteBatch.draw(this,
 				 pos.x, pos.y,
				 (int)scaledDimensions.x, (int)scaledDimensions.y,
 				 0, 0,
				 (int)scaledDimensions.x, (int)scaledDimensions.y,
 				 false, true);
 	}
 }
