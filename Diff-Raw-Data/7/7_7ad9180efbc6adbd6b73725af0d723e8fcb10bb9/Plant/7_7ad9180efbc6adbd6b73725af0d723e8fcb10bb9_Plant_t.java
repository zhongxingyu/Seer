 package org.anddev.andengine.pvb.plant;
 
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.entity.Entity;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.modifier.PathModifier;
 import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.anddev.andengine.entity.modifier.PathModifier.Path;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.extra.Enviroment;
 import org.anddev.andengine.extra.ExtraScene;
 import org.anddev.andengine.pvb.singleton.GameData;
 import org.anddev.andengine.util.modifier.IModifier;
 import org.anddev.andengine.util.modifier.ease.EaseSineInOut;
 
 public abstract class Plant extends Entity {
 	
 	protected float mShotHeight = 28f;
	protected float mShotSpeed = 200f;
	protected float mShotDelay = 4f;
 	protected boolean mCanShot = true;
 	
 	public Plant() {
 		Sprite shadow = new Sprite(2, 55, GameData.getInstance().mPlantShadow);
 		shadow.setAlpha(0.4f);
 		attachChild(shadow);
 	}
 	
 	public void onAttached() {
 		registerUpdateHandler(new TimerHandler(this.mShotDelay, true, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				if (Plant.this.mCanShot)
 					Plant.this.shot();
 			}
 		}));
 	}
 	
 	private void shot() {
 		float speed = (680 - getParent().getX() -  45) / this.mShotSpeed;
 		Sprite shot = new Sprite(getParent().getX() + 45, getParent().getY() + this.mShotHeight, GameData.getInstance().mShot);
 		Sprite shadow = new Sprite(0, 21, GameData.getInstance().mShotShadow);
		shadow.setAlpha(0.4f);
 		shot.attachChild(shadow);
 		
 		Path path = new Path(2).to(getParent().getX() + 45, getParent().getY() + this.mShotHeight).to(680, getParent().getY() + this.mShotHeight);
 		shot.registerEntityModifier(new PathModifier(speed, path, new IEntityModifierListener() {
 			@Override
 			public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 				Enviroment.getInstance().safeDetachEntity(pItem);
 			}}, EaseSineInOut.getInstance()));
 		Enviroment.getInstance().getScene().getChild(ExtraScene.GAME_LAYER).attachChild(shot);
 	}
 	
 }
