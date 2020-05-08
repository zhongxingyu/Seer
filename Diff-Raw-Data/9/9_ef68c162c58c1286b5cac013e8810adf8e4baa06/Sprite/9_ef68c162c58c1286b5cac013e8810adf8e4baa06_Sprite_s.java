 package com.punchline.javalib.entities.components.render;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.javalib.entities.components.ComponentManager;
 import com.punchline.javalib.utils.SpriteSheet;
 
 /**
  * Component wrapper for a LibGDX {@link com.badlogic.gdx.graphics.g2d.Sprite
  * Sprite}.
  * 
  * @author Natman64
  * 
  */
 public class Sprite implements Renderable {
 
 	// region Fields/Initialization
 
 	private com.badlogic.gdx.graphics.g2d.Sprite sprite;
 
 	private int layer = 0;
 
 	/**
 	 * Constructs a Sprite using the given SpriteSheet. This should always be
 	 * preferred over the other constructors because SpriteSheet improves
 	 * performance.
 	 * 
 	 * @param spriteSheet
 	 *            The SpriteSheet to use.
 	 * @param key
 	 *            The key of the Sprite's TextureRegion.
 	 * @param origin
 	 *            The Sprite's origin.
 	 */
 	public Sprite(SpriteSheet spriteSheet, String key, Vector2 origin) {
 		sprite = new com.badlogic.gdx.graphics.g2d.Sprite(
 				spriteSheet.getRegion(key));
 
 		setOrigin(origin);
 	}
 
 	/**
 	 * Constructs a Sprite using the given SpriteSheet. This should always be
 	 * preferred over the other constructors because SpriteSheet improves
 	 * performance.
 	 * 
 	 * @param spriteSheet
 	 *            The SpriteSheet to use.
 	 * @param key
 	 *            The key of the Sprite's TextureRegion.
 	 * @param origin
 	 *            The Sprite's origin.
 	 */
 	public Sprite(SpriteSheet spriteSheet, String key) {
 		TextureRegion region = spriteSheet.getRegion(key);
 
 		sprite = new com.badlogic.gdx.graphics.g2d.Sprite(region);
 
 		Vector2 origin = new Vector2(region.getRegionWidth() / 2,
 				region.getRegionHeight() / 2);
 
 		setOrigin(origin);
 	}
 
 	/**
 	 * Constructs a Sprite.
 	 * 
 	 * @param texture
 	 *            The Sprite's {@link com.badlogic.gdx.graphics.Texture Texture}
 	 *            .
 	 * @param source
 	 *            The Sprite's source rectangle.
 	 * @param origin
 	 *            The Sprite's origin, in relation to it's bottom left corner.
 	 */
 	public Sprite(Texture texture, Rectangle source, Vector2 origin) {
 		sprite = new com.badlogic.gdx.graphics.g2d.Sprite(texture,
 				(int) source.x, (int) source.y, (int) source.width,
 				(int) source.height);
 
 		setOrigin(origin);
 	}
 
 	/**
 	 * Constructs a Sprite with its origin at its center.
 	 * 
 	 * @param texture
 	 *            The Sprite's {@link com.badlogic.gdx.graphics.Texture Texture}
 	 *            .
 	 * @param source
 	 *            The Sprite's source rectangle.
 	 */
 	public Sprite(Texture texture, Rectangle source) {
 		this(texture, source, new Vector2(source.width / 2, source.height / 2));
 	}
 
 	/**
 	 * Constructs a Sprite with its origin at its center.
 	 * 
 	 * @param texture
 	 *            The Sprite's {@link com.badlogic.gdx.graphics.Texture Texture}
 	 *            .
 	 * @param source
 	 *            The Sprite's source rectangle.
 	 */
 	public Sprite(Texture texture, TextureRegion source) {
 		this(texture, new Rectangle(source.getRegionX(), source.getRegionY(),
 				source.getRegionWidth(), source.getRegionHeight()));
 	}
 
 	/**
 	 * Constructs a Sprite with no source rectangle, and its origin at its
 	 * center.
 	 * 
 	 * @param texture
 	 *            The Sprite's {@link com.badlogic.gdx.graphics.Texture Texture}
 	 *            .
 	 */
 	public Sprite(Texture texture) {
 		this(texture, new Rectangle(0, 0, texture.getWidth(),
 				texture.getHeight()));
 	}
 
 	/**
 	 * Default constructor for blank/null sprite.
 	 */
 	public Sprite() {
 	}
 
 	// endregion
 
 	// region Events
 
 	@Override
 	public void onAdd(ComponentManager container) {
 	}
 
 	@Override
 	public void onRemove(ComponentManager container) {
 	}
 
 	// endregion
 
 	// region Accessors
 
 	@Override
 	public float getWidth() {
 		return sprite.getWidth();
 	}
 
 	@Override
 	public float getHeight() {
 		return sprite.getHeight();
 	}
 
 	@Override
 	public Vector2 getPosition() {
		return new Vector2(sprite.getX() + getWidth() / 2, sprite.getY()
				+ getHeight() / 2);
 	}
 
 	@Override
 	public float getRotation() {
 		return sprite.getRotation();
 	}
 
 	@Override
 	public int getLayer() {
 		return layer;
 	}
 
 	// endregion
 
 	// region Mutators
 
 	/**
 	 * Sets the Sprite's center position.
 	 * 
 	 * @param position
 	 *            The new position.
 	 */
 	@Override
 	public void setPosition(Vector2 position) {
		sprite.setPosition(position.x - getWidth() / 2, position.y
				- getHeight() / 2);
 	}
 
 	/**
 	 * Sets the Sprite's rotation, in degrees.
 	 * 
 	 * @param degrees
 	 *            The desired rotation.
 	 */
 	@Override
 	public void setRotation(float degrees) {
 		sprite.setRotation(degrees);
 	}
 
 	@Override
 	public void setScale(float scaleX, float scaleY) {
 		sprite.setScale(scaleX, scaleY);
 	}
 
 	@Override
 	public void setOrigin(Vector2 origin) {
 		sprite.setOrigin(origin.x, origin.y);
 	}
 
 	@Override
 	public void setLayer(int layer) {
 		this.layer = layer;
 	}
 
 	// endregion
 
 	// region Rendering
 
 	@Override
 	public void draw(SpriteBatch spriteBatch, float deltaSeconds) {
 		sprite.draw(spriteBatch);
 	}
 
 	// endregion
 
 }
