 package com.liongrid.gameengine;
 
 import java.io.InvalidObjectException;
 
 import com.liongrid.gameengine.tools.FixedSizeArray;
 
 /**
  * @author Lastis
  * This class extends ObjectHandler. This is because CollisionArea shares all of the
  * methods that ObjectHandler contains, even though all of the methods have been 
  * overridden. 
  */
 public class CollisionArea extends BaseObject implements 
 		ObjectHandlerInterface<BaseObject>{
 	
 	/**
 	 * This is simply a two dimensional array.
 	 */
 	private FixedSizeArray<FixedSizeArray<Collideable.CollisionArea>> types;
 	private FixedSizeArray<Collideable.CollisionArea> pendingAdditions;
 	private FixedSizeArray<Collideable.CollisionArea> pendingRemovals;
 	
 	
 	public CollisionArea(int typeCnt, int capacity) {
 		types = 
 			new FixedSizeArray<FixedSizeArray<Collideable.CollisionArea>>(typeCnt);
 		pendingAdditions = new FixedSizeArray<Collideable.CollisionArea>(capacity);
 		pendingRemovals = new FixedSizeArray<Collideable.CollisionArea>(capacity);
 		int length = typeCnt;
 		for(int i = 0; i < length; i++){
 			types.add(new FixedSizeArray<Collideable.CollisionArea>(capacity));
 		}
 	}
 	
 
 	/** 
 	 * Will be casted to Collideable.CollisionArea. Make sure the BaseObjects used 
 	 * implements one of the sub-interfaces!
 	 * 
 	 * @see com.liongrid.gameengine.ObjectHandlerInterface#add(com.liongrid.gameengine.BaseObject)
 	 */
 	public void add(BaseObject o) throws IllegalObjectException{
 		try {
 			pendingAdditions.add((Collideable.CollisionArea) o);
 		} catch (ClassCastException e) {
 			throw new IllegalObjectException();
 		}
 	}
 
 	/**
 	 * Will be casted to Collideable.CollisionArea. Make sure the BaseObjects used 
 	 * implements one of the sub-interfaces!
 	 * 
 	 * @see com.liongrid.gameengine.ObjectHandlerInterface#remove(com.liongrid.gameengine.BaseObject)
 	 */
 	public void remove(BaseObject o) throws IllegalObjectException{
 		try {
 			pendingRemovals.add((Collideable.CollisionArea) o);
 		} catch (ClassCastException e) {
 			throw new IllegalObjectException();
 		}
 	}
 
 	public void commitUpdates() {
 		int i;
 		int j;
 		int length;
 		int[] type;
 		Collideable.CollisionArea shape;
 		
 		length = pendingRemovals.getCount();
 		for(i = 0; i < length; i++){
 			shape = pendingRemovals.get(i);
 			type = shape.getType();
 			for(j = 0; j < type.length; j++){
				types.get(j).remove(shape, true);
 			}
 		}
 		pendingRemovals.clear();
 		
 		length = pendingAdditions.getCount();
 		for(i = 0; i < length; i++){
 			shape = pendingAdditions.get(i);
 			type = shape.getType();
 			for(j = 0; j < type.length; j++){
				types.get(j).add(shape);
 			}
 		}
 		pendingAdditions.clear();
 	}
 
 	public FixedSizeArray<BaseObject> getObjects() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void update(float dt, BaseObject parent) {
 		commitUpdates();
 		
 		int length = types.getCount();
 		for(int i = 0; i < length; i++){
 			FixedSizeArray<Collideable.CollisionArea> shapes = types.get(i);
 			int count = shapes.getCount();
 			for(int j = 0; j < count; j++){
 				Collideable.CollisionArea shape1 = shapes.get(j);
 				collides(shape1, i, j);
 			}
 		}
 	}
 
 	private void collides(Collideable.CollisionArea shape1, int typeI, int shapeI) {
 		int length = types.getCount();
 		for(int i = typeI; i < length; i++){
 			FixedSizeArray<Collideable.CollisionArea> shapes = types.get(i);
 			int count = shapes.getCount();
 			for(int j = shapeI + 1; j < count; j++){
 				Collideable.CollisionArea shape2 = shapes.get(j);
 				if(Collision.collides(shape1, shape2)){
 					shape1.collides(shape2);
 					shape2.collides(shape1);
 				}
 			}
 		}
 	}
 
 
 	@Override
 	public void reset() {
 		
 	}
 }
