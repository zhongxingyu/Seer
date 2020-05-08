 /*
  * Copyright (C) 2013 Andrew Tulay
  * @mail: mhyhre@gmail.com
  * 
  * This work is licensed under a Creative Commons 
  * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
  * You may obtain a copy of the License at
  *
  *		http://creativecommons.org/licenses/by-nc-nd/3.0/legalcode
  *
  */
 
 package mhyhre.lightrabbit.Scenes;
 
 import java.util.Random;
 
 import mhyhre.lightrabbit.MainActivity;
 import mhyhre.lightrabbit.MhyhreScene;
 import mhyhre.lightrabbit.R;
 
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.batch.SpriteBatch;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 
 import android.util.Log;
 
 public class SceneLoader extends MhyhreScene {
 
 	private float AlphaTime = 0.0f;
 	private float AlphaTime2 = -0.3f;
 	private float AlphaTime3 = 0.0f;
 	
 	boolean soundPlayed = false;
 
 	Background backGround;
 
 	public Text mCaptionTapScreen;
 
 	private ITextureRegion mSplashTextureRegion;
 	private ITextureRegion mCloudTR;
 
 	public Rectangle TapRect;
 	public Sprite mSpriteLogo;
 	public SpriteBatch cloudsSpriteBatch;
 
 	private boolean Loaded = false;
 	private boolean Clicked = false;
 
 	final static int CloudCount = 20;
 	private static Cloud CloudBase[] = new Cloud[CloudCount];
 
 	Random mRandom = new Random();
 
 	public SceneLoader() {
 
 		backGround = new Background(0.0f, 0.0f, 0.0f);
 		setBackground(backGround);
 		setBackgroundEnabled(true);
 
 		MainActivity.Res.LoadResourcesForPreloader();
 
 		mSplashTextureRegion = TextureRegionFactory.extractFromTexture(MainActivity.Res.getTextureAtlas("tex_01"), 0, 0, 384, 128, false);
 		mCloudTR = TextureRegionFactory.extractFromTexture(MainActivity.Res.getTextureAtlas("tex_01"), 384, 0, 128, 128, false);
 
 		// Tap text
 		String TextMessage = MainActivity.Me.getString(R.string.textTap);
 		mCaptionTapScreen = new Text(320, 400, MainActivity.Res.getFont("Furore"), TextMessage, MainActivity.Me.getVertexBufferObjectManager());
 		mCaptionTapScreen.setPosition(MainActivity.getHalfWidth() - mCaptionTapScreen.getWidth() / 2, (MainActivity.getHeight()/ 5) * 4);
 		mCaptionTapScreen.setVisible(false);
 		mCaptionTapScreen.setAlpha(0.0f);
 
 		// creating a logo-sprite
 		mSpriteLogo = new Sprite(0, 0, mSplashTextureRegion, MainActivity.Me.getVertexBufferObjectManager());
 		mSpriteLogo.setPosition(MainActivity.getHalfWidth() - mSplashTextureRegion.getWidth() / 2, MainActivity.getHalfHeight() - mSplashTextureRegion.getHeight() / 2);
 		mSpriteLogo.setAlpha(0.0f);
 
 		// tap-zone
 		TapRect = new Rectangle(0, 0, MainActivity.getWidth(), MainActivity.getHeight(), MainActivity.Me.getVertexBufferObjectManager()) {
 			@Override
 			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN && !Clicked) {
 					unregisterTouchArea(TapRect);
 					Clicked = true;
 					Log.i(MainActivity.DebugID, "Splash Screen [ Tap ] button");
 					MainActivity.vibrate(30);
 				}
 				return true;
 			}
 		};
 		TapRect.setVisible(false);
 		TapRect.setAlpha(AlphaTime3);
 		TapRect.setColor(0, 0, 0.1f);
 
 		setupClouds();
 
 		attachChild(mSpriteLogo);
 		attachChild(mCaptionTapScreen);
 		attachChild(TapRect);
 
 		Loaded = true;
 
 		// Alpha Timer
 		registerUpdateHandler(new TimerHandler(0.02f, true, new ITimerCallback() {
 			// @Override
 			public void onTimePassed(final TimerHandler pTimerHandler) {
 
 				if (mCaptionTapScreen.isVisible()) {
 
 					if(AlphaTime2 > 0.3f && soundPlayed == false){
						MainActivity.Res.getSound("switchOn").play();
 						soundPlayed = true;
 					}
 					
 					// Splash on - logo
 					if (AlphaTime2 < 0.99f) {
 						AlphaTime2 += 0.01f;
 						if (AlphaTime2 < 0.0f) {
 							mSpriteLogo.setAlpha(0.0f);
 						} else {
 							mSpriteLogo.setAlpha(AlphaTime2);
 
 							backGround.setColor(0.1f * AlphaTime2, 0.2f * AlphaTime2, 0.7f * AlphaTime2);
 
 							for (int i = 0; i < CloudCount; i++) {
 								CloudBase[i].SetColor(1, AlphaTime2, AlphaTime2);
 							}
 						}
 					}
 
 					// Splash on - tap text
 					if (AlphaTime2 > 0.5f) {
 						AlphaTime += 0.05f;
 						if (AlphaTime > Math.PI)
 							AlphaTime = 0.0f;
 						mCaptionTapScreen.setAlpha((float) Math.sin(AlphaTime));
 					}
 				}
 
 				if (Clicked) {
 
 					// Splash off scene by rect
 					if (AlphaTime3 >= 1.0f) {
 						unregisterUpdateHandler(pTimerHandler);
 						SceneRoot.SetState(SceneStates.MainMenu);
 					} else {
 						AlphaTime3 += 0.1f;
 					}
 					TapRect.setVisible(true);
 					TapRect.setAlpha(AlphaTime3);
 				}
 
 			}
 		}));
 
 	}
 
 	private void setupClouds() {
 
 		cloudsSpriteBatch = new SpriteBatch(MainActivity.Res.getTextureAtlas("tex_01"), CloudCount, MainActivity.Me.getVertexBufferObjectManager());
 		cloudsSpriteBatch.setPosition(0, 0);
 
 		for (int i = 0; i < CloudCount; i++) {
 			CloudBase[i] = new Cloud();
 			CloudBase[i].SetSize((float) mCloudTR.getWidth(), (float) mCloudTR.getHeight());
 			CloudBase[i].SetPosition((float) mRandom.nextInt((int)MainActivity.getWidth()) - 5.0f, MainActivity.getHalfHeight() + (float) mRandom.nextInt((int)MainActivity.getHalfHeight()) * 0.66f - 5.0f);
 
 			CloudBase[i].SetMoveSpeed((mRandom.nextFloat() * 0.5f) - 0.25f, (mRandom.nextFloat() * 0.2f) - 0.1f);
 			CloudBase[i].SetScale(3.0f + mRandom.nextFloat() * 3.0f);
 			CloudBase[i].SetRotation(mRandom.nextFloat() * 360.0f);
 
 			CloudBase[i].SetColor(1.f, 0.f, 0.f);
 
 			CloudBase[i].Update(0, MainActivity.getWidth(), 0, MainActivity.getHeight());
 
 			cloudsSpriteBatch.draw(this.mCloudTR, CloudBase[i].PosX, CloudBase[i].PosY, CloudBase[i].SizeX, CloudBase[i].SizeY, CloudBase[i].Rotation, CloudBase[i].Scale, CloudBase[i].Scale,
 					CloudBase[i].Red, CloudBase[i].Green, CloudBase[i].Blue, 0.8f);
 		}
 
 		cloudsSpriteBatch.submit();
 		attachChild(cloudsSpriteBatch);
 	}
 	
 	@Override
 	public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
 
 		return super.onSceneTouchEvent(pSceneTouchEvent);
 	}
 
 	@Override
 	protected void onManagedUpdate(float pSecondsElapsed) {
 
 		if (Loaded) {
 
 			for (int i = 0; i < CloudCount; i++) {
 				CloudBase[i].Update(0, MainActivity.getWidth(), 0, MainActivity.getHeight());
 
 				cloudsSpriteBatch.draw(this.mCloudTR, CloudBase[i].PosX, CloudBase[i].PosY, CloudBase[i].SizeX, CloudBase[i].SizeY, CloudBase[i].Rotation, CloudBase[i].Scale, CloudBase[i].Scale,
 						CloudBase[i].Red, CloudBase[i].Green, CloudBase[i].Blue, 0.75f);
 			}
 			cloudsSpriteBatch.submit();
 
 		}
 
 		super.onManagedUpdate(pSecondsElapsed);
 	}
 
 }
