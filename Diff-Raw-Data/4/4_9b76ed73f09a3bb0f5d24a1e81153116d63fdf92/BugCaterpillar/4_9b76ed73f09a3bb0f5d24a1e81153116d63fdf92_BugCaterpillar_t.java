 package org.anddev.amatidev.pvb.bug;
 
 import org.anddev.amatidev.pvb.singleton.GameData;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.modifier.LoopEntityModifier;
 import org.anddev.andengine.entity.modifier.MoveYModifier;
 import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.entity.sprite.Sprite;
 
 public class BugCaterpillar extends Bug {
 	
 	public BugCaterpillar(final float y) {
 		super(y, GameData.getInstance().mBugCaterpillar2);
 		getFirstChild().attachChild(new Sprite(-10, -68, GameData.getInstance().mBugCaterpillar2));
 		getFirstChild().attachChild(new Sprite(-20, -68, GameData.getInstance().mBugCaterpillar2));
 		getFirstChild().attachChild(new Sprite(0, -68, GameData.getInstance().mBugCaterpillar));
 		
 		move(-71, -66, getFirstChild().getChild(0));
 		move(-66, -71, getFirstChild().getChild(1));
 		move(-71, -66, getFirstChild().getChild(2));
 		
 		this.mLife = 15;
 		this.mSpeed = 13f;
 		this.mPoint = 8;
 		this.mAttack = 2.5f;
 	}
 	
 	protected IShape getBody() {
 		return ((IShape) getFirstChild().getChild(3));
 	}
 	
 	protected void colorDamage() {
 		getFirstChild().getChild(0).setColor(3f, 3f, 3f);
 		getFirstChild().getChild(1).setColor(3f, 3f, 3f);
 		getFirstChild().getChild(2).setColor(3f, 3f, 3f);
 		getFirstChild().getChild(3).setColor(3f, 3f, 3f);
 		registerUpdateHandler(new TimerHandler(0.1f, false, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				BugCaterpillar.this.getFirstChild().getChild(0).setColor(1f, 1f, 1f);
 				BugCaterpillar.this.getFirstChild().getChild(1).setColor(1f, 1f, 1f);
 				BugCaterpillar.this.getFirstChild().getChild(2).setColor(1f, 1f, 1f);
 				BugCaterpillar.this.getFirstChild().getChild(3).setColor(1f, 1f, 1f);
 			}
 		}));
 	}
 	
 	private void move(final int pStartY, final int pEndY, final IEntity pElem) {
 		pElem.registerEntityModifier(
 				new LoopEntityModifier(
 						null, 
 						-1, 
 						null,
 						new SequenceEntityModifier(
 								new MoveYModifier(2f, pStartY, pEndY),
 								new MoveYModifier(2f, pEndY, pStartY)
 						)
 				));
 	}
 	
 }
