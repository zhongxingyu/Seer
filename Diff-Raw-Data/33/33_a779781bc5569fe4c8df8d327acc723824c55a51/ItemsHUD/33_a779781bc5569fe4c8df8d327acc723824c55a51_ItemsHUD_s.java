 package com.bugfullabs.qube.hud;
 
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.extension.texturepacker.opengl.texture.util.texturepacker.TexturePack;
 import org.anddev.andengine.input.touch.TouchEvent;
 
 import com.bugfullabs.qube.util.SpriteButton;
 
 
 
 public class ItemsHUD extends HUD{
 	
 	public static final int ITEMS_BUTTON_NUMBER = 5;
 	public static final int BUTTON_PADDING = 8;
 
 	
 	public static final int BG_ID = 0;
 	public static final int STOP_ID = 6;
 	public static final int PLAY_ID = 7;
 	public static final int STAR_ID = 8;
 
 	
 	
 	public static final int ITEMS = 1;
 	public static final int GAME = 2;
 	
 	private SpriteButton mItemsButtons[];
 	private TexturePack HUDTexturePack;
 	
 	private Sprite mStars[];
 	
 	private int mStarsNumber = 0;
 	
 	private boolean isPlay = false;
 	
 	public ItemsHUD(Camera pCamera, TexturePack pTexturePack){
 		
 		super();
 		super.setCamera(pCamera);
 	
 		HUDTexturePack = pTexturePack;
 		
 		mItemsButtons = new SpriteButton[ItemsHUD.ITEMS_BUTTON_NUMBER + 1];
 
 		
 		
 		mStars = new Sprite[3];
 		
 		this.setBackgroundEnabled(false);
 		
 		for(int i = 0; i < ItemsHUD.ITEMS_BUTTON_NUMBER; i++){
 		
 			final int id = i;
 			
 			mItemsButtons[i] = new SpriteButton(this, 730, 32+i*(ItemsHUD.BUTTON_PADDING+64), HUDTexturePack.getTexturePackTextureRegionLibrary().get(i+1), HUDTexturePack.getTexturePackTextureRegionLibrary().get(BG_ID)){
 				@Override
 				public void onButtonTouchEvent(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY){
 				ItemsHUD.this.onButtonTouchEvent(id, pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);	
 				}
 			};
 			
 		}
 		
 		mItemsButtons[ITEMS_BUTTON_NUMBER] = new SpriteButton(this, 730, 32+(ITEMS_BUTTON_NUMBER)*(ItemsHUD.BUTTON_PADDING+64), HUDTexturePack.getTexturePackTextureRegionLibrary().get(PLAY_ID), HUDTexturePack.getTexturePackTextureRegionLibrary().get(BG_ID)){
 			
 			@Override
 			public void onButtonPressed(){
 				
 				if(!isPlay){
 				ItemsHUD.this.onPlay();
 				}else{
 				ItemsHUD.this.onStop();
 				}
 				}
 		};		
 		
 		
 		for(int i = 0; i < 3; i++){
 			this.mStars[i] = new Sprite(48*i, 12, HUDTexturePack.getTexturePackTextureRegionLibrary().get(STAR_ID));
 		}
 		
 		this.setType(ITEMS);		
 	}
 
 
 	public void show(){
 		this.setVisible(true);
 		this.setTouchAreaBindingEnabled(true);
 	}
 	
 	public void hide(){
 		this.setVisible(false);
 		this.setTouchAreaBindingEnabled(false);
 	}
 	
 	public void setType(int value){
 		switch(value){
 		
 		case ITEMS:
 			for(int i = 0; i < ITEMS_BUTTON_NUMBER; i++){
 			this.mItemsButtons[i].setCanBeTouched(true);
 			this.mItemsButtons[i].setVisible(true);
 			}
 			for(int i = 0; i < 3; i++){
 			this.detachChild(mStars[i]);
 			}
 			this.mStarsNumber = 0;
 			
 			this.mItemsButtons[ITEMS_BUTTON_NUMBER].setSprite(HUDTexturePack.getTexturePackTextureRegionLibrary().get(PLAY_ID));
 			
 			this.isPlay = false;
 			
 			break;
 			
 		case GAME:
 
 			for(int i = 0; i < ITEMS_BUTTON_NUMBER; i++){
 			this.mItemsButtons[i].setCanBeTouched(false);
 			this.mItemsButtons[i].setVisible(false);
 			}
 			
 			this.mItemsButtons[ITEMS_BUTTON_NUMBER].setSprite(HUDTexturePack.getTexturePackTextureRegionLibrary().get(STOP_ID));
 			
 			
 			this.isPlay = true;
 			
 			break;
 
 		}
 	}
 	
 	public void setStars(int stars){
 		if(stars > this.mStarsNumber){
 			this.mStarsNumber = stars;
 			for(int i = stars; i<3; i++){
 				this.attachChild(this.mStars[i]);
 			}
 		}
 		
 	}
 	
 	public SpriteButton getButton(int id){
 		return this.mItemsButtons[id];
 	}
 	
 	
 	protected void onButtonTouchEvent(final int id, final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY){
 		
 	}
 	
 	protected void onPlay(){
 		
 	}
 	
 	protected void onStop() {
 		
 	}
 
 	
 }
