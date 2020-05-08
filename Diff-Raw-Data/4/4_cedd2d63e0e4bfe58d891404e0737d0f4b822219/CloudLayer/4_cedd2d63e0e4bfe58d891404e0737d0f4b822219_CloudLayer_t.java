 package com.lpatalas.spacegame;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 class CloudLayer {
 	private final Color color;
 	private float scroll;
 	private final float scrollSpeed;
 	private final Texture texture;
 
 	public CloudLayer(String textureName, float scrollSpeed, Color color) {
 		this.color = color;
 		this.scrollSpeed = scrollSpeed;
 		this.texture = createTexture(textureName);
 	}
 
 	private Texture createTexture(String textureName) {
 		Texture texture = new Texture(Gdx.files.internal(textureName));
 		texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
 		texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
 		return texture;
 	}
 
	public void move(float deltaTime) {
 		scroll -= deltaTime * scrollSpeed;
 		if (scroll < -1.0f) {
 			scroll += 1.0f;
 		}
 	}
 
 	public void render(SpriteBatch spriteBatch) {
 		spriteBatch.setColor(color);
 
 		float u1 = -scroll;
 		float v1 = 0;
 		float u2 = -scroll + 1.0f;
 		float v2 = 1.0f;
 		spriteBatch.draw(texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), u1, v1, u2, v2);
 	}
 }
