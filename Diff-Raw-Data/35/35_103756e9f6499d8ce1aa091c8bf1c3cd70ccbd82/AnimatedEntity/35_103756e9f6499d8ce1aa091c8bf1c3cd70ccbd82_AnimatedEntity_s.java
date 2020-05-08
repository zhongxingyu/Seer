 package com.jpii.navalbattle.game.entity;
 
 import com.jpii.navalbattle.pavo.grid.EntityManager;
 import com.jpii.navalbattle.pavo.grid.GridedEntityTileOrientation;
 import com.jpii.navalbattle.pavo.grid.Location;
 
 public class AnimatedEntity extends MoveableEntity {
 	private static final long serialVersionUID = 1L;
 	private GridedEntityTileOrientation[] animationids;
 
 	/**
 	 * @param em The EnitityManager.
 	 * @param loc The Location of the Entity.
 	 * @param animationFrameIds The animation frame sequences.
 	 */
 	public AnimatedEntity(EntityManager em, Location loc,byte orientation,int team, GridedEntityTileOrientation... animationFrameIds) {
 		super(em, loc, animationFrameIds[0],orientation,team);
 		animationids = animationFrameIds;
 		handle = 3;
 	}
 	
 	public void setCurrentFrame(int index) {
 		if (index >= getTotalFrames())
 			throw new java.lang.ArrayIndexOutOfBoundsException("The indicated frame is not in the given range of frames.");
 		setId(animationids[index]);
 	}
 	
 	public int getTotalFrames() {
 		return animationids.length;
 	}
 	
 	int nextIndex = 0;
 	boolean direction = true;
 	boolean alternate = true;
 	
 	public void setAlternatingDirection(boolean t) {
 		alternate = t;
 	}
 	
 	public boolean isAlternatingDirections() {
 		return alternate;
 	}
 	
 	public void updateFrame() {
 		setCurrentFrame(nextIndex);
 		if (alternate) {
 		if (direction)
 			nextIndex++;
 		else
 			nextIndex--;
 		if (nextIndex >= getTotalFrames()) {
 			direction = false;
 			nextIndex--;
 		}
 		if (nextIndex == -1) {
 			direction = true;
 			nextIndex = 0;
 		}
 		}
 		else {
 			if (nextIndex >= getTotalFrames() - 1) {
 				nextIndex = 0;
 			}
 			else
 				nextIndex++;
 		}
 	}
 }
