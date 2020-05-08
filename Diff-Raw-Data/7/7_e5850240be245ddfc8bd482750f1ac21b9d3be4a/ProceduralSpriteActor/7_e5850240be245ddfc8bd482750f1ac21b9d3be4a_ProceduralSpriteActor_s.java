 package com.razh.laser;
 
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 
 public class ProceduralSpriteActor extends EntityActor {
 	private static Sprite mSprite;
 
 	public ProceduralSpriteActor() {
 		super();
 
 		if (mSprite == null) {
 			createSprite();
 		}
 	}
 
 	private void createSprite() {
 		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
 		Texture texture = new Texture(pixmap);
 		mSprite = new Sprite(texture);
 		texture.dispose();
 		pixmap.dispose();
 	}
 
 	@Override
 	public void draw(SpriteBatch spriteBatch, float parentAlpha) {
 		draw(spriteBatch, parentAlpha, null);
 	}
 
 	public void draw(SpriteBatch spriteBatch, float parentAlpha, ShaderProgram shaderProgram) {
 		float width = getWidth();
 		float height = getHeight();
 
 		mSprite.setRotation(getRotation());
 		mSprite.setSize(width, height);
		mSprite.setPosition(getX() - 0.5f * width, getY() - 0.5f * height);
 		mSprite.setColor(getColor());
 
 		if (shaderProgram != null) {
 			setUniforms(shaderProgram);
 		}
 
 		mSprite.draw(spriteBatch, parentAlpha);
 	}
 
 	public Sprite getSprite() {
 		return mSprite;
 	}
 
 	public void setSprite(Sprite sprite) {
 		mSprite.set(sprite);
 	}
 
 	public void setUniforms(ShaderProgram shaderProgram) {
 		shaderProgram.setUniformf("color", mSprite.getColor());
 	}
 }
