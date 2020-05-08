 package com.secondhand.view.entities;
 
 import java.beans.PropertyChangeListener;
 
 import org.anddev.andengine.entity.shape.IShape;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.secondhand.model.Entity;
 
 public abstract class EntityView implements PropertyChangeListener {
	private final Entity entity;
	private final IShape shape;
 	//Need this for physics.
 	private Body body;
 	
	public EntityView(final Entity entity, final IShape shape){
 		this.entity = entity;
 		this.shape=shape;
 		entity.addPropertyChangeListener(this);
 		
 		//Will be here later
 		//body=entity.getPhysics().createType(shape,entity);
 	}
 	
 	public Entity getEntity(){
 		return entity;
 	}
 	
 	public Body getBody(){
 		return body;
 	}
 	
 	public IShape getShape(){
 		return shape;
 	}
 	
 }
