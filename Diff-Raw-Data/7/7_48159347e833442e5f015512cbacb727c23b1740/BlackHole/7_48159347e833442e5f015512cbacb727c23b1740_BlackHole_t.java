 package com.secondhand.model.entity;
 
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.physics.Vector2;
 
 public abstract class BlackHole extends CircleEntity {
 
 	// only a 1/5 of the masses of the eaten bodies is used in the growth
 	private static final float GROWTH_FACTOR = 0.2f;
 
 	private boolean canEatInedibles;
 
 	private final Vector2 position;
 
 	// I put this in BlackHole, so enemy black holes will also have scores
 	// but placing it here made coding the eating logic much more convenient.
 	// but we can simply ignore the score for enemy black holes.
 	private int score;
 
 	public BlackHole(final Vector2 position, final float radius) {
 		super(position, radius, true);
 		this.position = position;
 
 		this.canEatInedibles = false;
 	}
 	
 	@Override
 	public float getRadius(){
 		return radius;
 	}
 
 	public float getPosX() {
 		return position.y;
 	}
 
 	public float getPosY() {
 		return position.y;
 	}
 
 	public boolean canEatInedibles() {
 		return this.canEatInedibles;
 	}
 
 	public void setCanEatInedibles(final boolean canEatInedibles) {
 		this.canEatInedibles = canEatInedibles;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public void increaseScore(final int increase) {
 		score += increase;
 	}
 
 	private void increaseSize(final float increase) {
 		setRadius(getRadius() + increase);
 	}
 
 	/**
 	 * If sizes are equal then false is returned.
 	 */
 	public boolean canEat(final Entity entity) {
 		if (!entity.isEdible() && !this.canEatInedibles()) {
 			return false;
 		}
 		return this.getRadius() > entity.getRadius();
 	}
 
 	protected void onGrow() {
 	}
 
 	protected void entityWasTooBigToEat(final Entity entity) {
 	}
 
 	public void eatEntityUtil(final Entity entity) {
 		if (!this.canEat(entity)) {
 			entityWasTooBigToEat(entity);
 			return;
 		}
 
 		this.increaseScore(entity.getScoreValue());
 
 		if(entity.getRadius() != 0) {
 			final float radiusInc = entity.getRadius() * GROWTH_FACTOR;
 			this.increaseSize(radiusInc);
 			onGrow();
 		}
 
 		entity.wasEaten();
 	}
 
 	public void eatEntity(final Entity entity) {
 		
 		if (entity instanceof BlackHole) {
 			final BlackHole otherBlackHole = (BlackHole) entity;
 			// instead of eating, you will be eaten!
 			if (otherBlackHole.canEat(this))
 				otherBlackHole.eatEntity(this);
 			else
 				this.eatEntityUtil(otherBlackHole);
 		} else {
 			// else, just eat the entity.
 			eatEntityUtil(entity);
 		}
 
 	}
 
 	@Override
 	public float getScoreWorth() {
 		return 3;
 	}
 
 	@Override
 	public void setRadius(final float radius) {
			
 		this.pcs.firePropertyChange("radius", this.getRadius(), radius);
		super.setRadius(radius);
 	}
 }
