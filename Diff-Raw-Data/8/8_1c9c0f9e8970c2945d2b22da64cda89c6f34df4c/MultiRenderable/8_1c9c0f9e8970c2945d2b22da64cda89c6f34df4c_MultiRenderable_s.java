 package com.punchline.javalib.entities.components.render;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import com.punchline.javalib.entities.components.ComponentManager;
 import com.punchline.javalib.entities.components.MultiComponent;
 
 /**
  * A Renderable {@link MultiComponent}. The Renderables are drawn in order.
  * 
  * @author Natman64
  * @created Aug 12, 2013
  */
 public class MultiRenderable extends MultiComponent<Renderable> implements Renderable {
 
 	// region Initialization
 
 	/**
 	 * Makes a MultiRenderable component.
 	 * 
 	 * @param base
 	 *            The base Renderable whose position and other fields will be
 	 *            returned by this component.
 	 * @param children
 	 *            All Renderable components that will be drawn above the base
 	 *            Renderable.
 	 */
 	public MultiRenderable(Renderable base, Renderable... children) {
 		super(base, children);
 	}
 
 	/**
 	 * Makes a MultiRenderable component.
 	 * 
 	 * @param base
 	 *            The base Renderable whose position and other fields will be
 	 *            returned by this component.
 	 * @param children
 	 *            All Renderable components that will be drawn above the base
 	 *            Renderable.
 	 */
 	public MultiRenderable(Renderable base, Array<Renderable> children) {
 		super(base, children);
 	}
 
 	// endregion
 
 	// region Events
 
 	@Override
 	public void onAdd(ComponentManager container) {
 		super.onAdd(container);
 	}
 
 	@Override
 	public void onRemove(ComponentManager container) {
 		super.onAdd(container);
 	}
 
 	// endregion
 
 	// region Accessors
 
 	@Override
 	public float getWidth() {
 		return base.getWidth();
 	}
 
 	@Override
 	public float getHeight() {
 		return base.getHeight();
 	}
 
 	@Override
 	public Vector2 getPosition() {
 		return base.getPosition();
 	}
 
 	@Override
 	public Vector2 getOrigin() {
 		return base.getOrigin();
 	}
 	
 	@Override
 	public float getRotation() {
 		return base.getRotation();
 	}
 
 	@Override
 	public int getLayer() {
 		return base.getLayer();
 	}
 
 	// endregion
 
 	// region Mutators
 
 	@Override
 	public void setPosition(Vector2 position) {
		Vector2 offset = position.cpy().sub(base.getPosition());

 		// Move all children relative to their old positions.
 		for (Renderable child : children) {
			child.setPosition(child.getPosition().add(offset));
 		}
 
 		return;
 	}
 
 	@Override
 	public void setRotation(float degrees) {
 		for (Renderable child : children) {
 			child.setRotation(degrees);
 		}
 	}
 
 	@Override
 	public void setScale(float scaleX, float scaleY) {
 		for (Renderable child : children) {
 			child.setScale(scaleX, scaleY);
 		}
 	}
 
 	@Override
 	public void setOrigin(Vector2 origin) {
 		for (Renderable child : children) {
 			Vector2 offset = child.getPosition().sub(base.getPosition());
 
			child.setOrigin(origin.cpy().add(offset));
 		}
 	}
 
 	@Override
 	public void setLayer(int layer) {
 		base.setLayer(layer);
 	}
 
 	// endregion
 
 	// region Rendering
 
 	@Override
 	public void draw(SpriteBatch spriteBatch, float deltaSeconds) {
 		for (Renderable child : children) {
 			child.draw(spriteBatch, deltaSeconds);
 		}
 	}
 
 	// endregion
 
 }
