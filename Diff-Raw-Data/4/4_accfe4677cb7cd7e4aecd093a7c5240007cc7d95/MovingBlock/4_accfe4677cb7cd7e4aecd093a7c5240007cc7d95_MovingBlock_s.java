 package edu.chl.codenameg.model.entity;
 
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenManager;
 import edu.chl.codenameg.model.EntityTweenAccessor;
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.model.Hitbox;
 import edu.chl.codenameg.model.Position;
 import edu.chl.codenameg.model.Vector2D;
 
 public class MovingBlock extends Block {
 	private Vector2D v2d = new Vector2D(0, 0);
 	boolean moving = false;
 	private int travelTime;
 	private Position endPos;
 	private Position startPos;
 	private TweenManager manager = new TweenManager();
 
 	@Override
 	public void collide(Entity e) {
 		super.collide(e);
 		if (e instanceof PlayerCharacter) {
 			PlayerCharacter landedPlayer = (PlayerCharacter) e;
 			landedPlayer.getVector2D().add(this.v2d);
 		}
 	}
 
	public MovingBlock(Hitbox hb, Position ps,Position endPos) {
 		super(hb, ps);
 		this.endPos=endPos;
 		Tween.registerAccessor(Entity.class, new EntityTweenAccessor());
 		Tween.to(this, EntityTweenAccessor.POSITION_XY, this.travelTime)
 				.target(endPos.getX(), endPos.getY()).start(manager);
 	}
 
 	public Vector2D getVector2D() {
 		return new Vector2D(this.v2d);
 	}
 
 	public void update(int elapsedTime) {
 		manager.update(elapsedTime);
 	}
 
 	// We can now create as many interpolations as we need !
 
 }
