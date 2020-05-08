 package com.punchline.javalib.entities.systems.render;
 
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.components.physical.Transform;
 import com.punchline.javalib.entities.components.render.Renderable;
 import com.punchline.javalib.entities.systems.ComponentSystem;
 
 /**
  * System for rendering every {@link Entity} that has a {@link Renderable} component.
  * @author Nathaniel
  *
  */
 public final class RenderSystem extends ComponentSystem {
 	
 	private Camera camera;
 	private SpriteBatch spriteBatch;
 
 	/**
 	 * Constructs a RenderSystem.
 	 * @param camera The camera for rendering.
 	 */
 	public RenderSystem(Camera camera) {
 		super(Renderable.class);
 		
 		this.camera = camera;
 		
 		spriteBatch = new SpriteBatch();
 	}
 
 	@Override
 	public void dispose() {
 		spriteBatch.dispose();
 	}
 	
 	@Override
 	public void processEntities() {
 		
 		camera.update();
 		spriteBatch.setProjectionMatrix(camera.combined);
 		
 		spriteBatch.begin();
 		
 		super.processEntities();
 		
 		spriteBatch.end();
 		
 	}
 
 	@Override
 	protected void process(Entity e) {
 		
 		Renderable r = e.getComponent();
 		
 		if (e.hasComponent(Transform.class)) { 
 			Transform t = e.getComponent();
			
 			r.setPosition(t.getPosition());
 			r.setRotation((float)Math.toDegrees(t.getRotation()));
 		}
 		
 		r.draw(spriteBatch, deltaSeconds());
 		
 	}	
 	
 }
