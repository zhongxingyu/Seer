 package com.sun.darkstar.example.snowman.game.entity.controller;
 
 import com.jme.math.Vector3f;
 import com.sun.darkstar.example.snowman.common.entity.enumn.EState;
 import com.sun.darkstar.example.snowman.common.entity.view.View;
 import com.sun.darkstar.example.snowman.exception.ObjectNotFoundException;
 import com.sun.darkstar.example.snowman.game.entity.scene.CharacterEntity;
 import com.sun.darkstar.example.snowman.game.entity.view.scene.CharacterView;
 import com.sun.darkstar.example.snowman.game.entity.view.util.ViewManager;
 import com.sun.darkstar.example.snowman.game.input.Controller;
 import com.sun.darkstar.example.snowman.game.input.enumn.EInputType;
 import com.sun.darkstar.example.snowman.game.physics.util.PhysicsManager;
 import com.sun.darkstar.example.snowman.game.task.enumn.ETask;
 import com.sun.darkstar.example.snowman.game.task.util.TaskManager;
 
 /**
  * <code>CharacterController</code> extends <code>Controller</code> to define
  * the base type controller that controls a <code>SnowmanEntity</code>.
  * 
  * @author Yi Wang (Neakor)
  * @version Creation date: 07-24-2008 11:24 EST
  * @version Modified date: 08-06-2008 11:50 EST
  */
 public class CharacterController extends Controller {
 	/**
 	 * The movement tolerance value.
 	 */
 	protected final float tolerance;
 	/**
 	 * The last known state.
 	 */
 	private EState lastState;
//	/**
//	 * The flag indicates if a snow ball has been thrown for the current attack.
//	 */
//	private boolean thrown;
 
 	/**
 	 * Constructor of <code>CharacterController</code>.
 	 * @param entity The <code>CharacterEntity</code> instance.
 	 * @param type The <code>EInputType</code> enumeration.
 	 */
 	public CharacterController(CharacterEntity entity, EInputType type) {
 		super(entity, type);
 		this.tolerance = 0.1f;
 	}
 
 	@Override
 	protected void updateLogic(float interpolation) {
 		if(!this.getEntity().isAlive()) return;
 		switch(this.getEntity().getState()) {
 		case Attacking:
 			if(this.lastState != EState.Attacking) {
 				this.getEntity().setDestination(null);
 				this.entity.resetVelocity();
 				TaskManager.getInstance().createTask(ETask.CreateSnowball, this.getEntity(), this.getEntity().getTarget());
			}
//			if(!this.thrown) {// && ((CharacterView)ViewManager.getInstance().getView(this.entity)).isCurrentHalf()) {
//				TaskManager.getInstance().createTask(ETask.CreateSnowball, this.getEntity(), this.getEntity().getTarget());
//				this.thrown = true;
//			} else
			if(((CharacterView)ViewManager.getInstance().getView(this.entity)).isCurrentComplete()) {
 				this.getEntity().setState(EState.Idle);
 				ViewManager.getInstance().markForUpdate(this.entity);
				//this.thrown = false;
 				// Update target scale and speed.
 				ViewManager.getInstance().markForUpdate(this.getEntity().getTarget());
 			}
 			break;
 		case Hit:
 			if(((CharacterView)ViewManager.getInstance().getView(this.entity)).isCurrentComplete()) {
 				this.getEntity().setState(EState.Idle);
 				ViewManager.getInstance().markForUpdate(this.entity);
 			}
 			break;
 		default:
 			if(!this.getEntity().getVelocity().equals(Vector3f.ZERO)) {
 				if(this.validatePosition()) {
 					this.getEntity().setDestination(null);
 					this.entity.resetVelocity();
 					this.getEntity().setState(EState.Idle);
 					ViewManager.getInstance().markForUpdate(entity);
 				} else {
 					PhysicsManager.getInstance().markForUpdate(this.entity);
 				}
 			}
 			break;
 		}
 		this.lastState = this.getEntity().getState();
 	}
 
 	/**
 	 * Validate if the current position is within the tolerance range of the destination.
 	 * @return True if the character is considered reached the destination. False otherwise.
 	 */
 	private boolean validatePosition() {
 		CharacterEntity character = ((CharacterEntity)this.entity);
 		if(character.getDestination() == null) return true;
 		try {
 			View view = (View)ViewManager.getInstance().getView(character);
 			float dx = view.getLocalTranslation().x - character.getDestination().x;
 			float dz = view.getLocalTranslation().z - character.getDestination().z;
 			if((dx * dx) + (dz * dz) <= this.tolerance) return true;
 		} catch (ObjectNotFoundException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	@Override
 	public CharacterEntity getEntity() {
 		return (CharacterEntity)this.entity;
 	}
 }
