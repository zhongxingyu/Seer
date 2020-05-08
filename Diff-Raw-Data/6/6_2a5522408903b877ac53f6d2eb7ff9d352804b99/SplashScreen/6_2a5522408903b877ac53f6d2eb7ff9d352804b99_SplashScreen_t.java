 package com.aquasheep.average_jim.screens;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
 
 import com.aquasheep.average_jim.AverageJimGame;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 import com.badlogic.gdx.scenes.scene2d.Action;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.badlogic.gdx.utils.Scaling;
 
 public class SplashScreen extends AbstractScreen {
 	
 	public SplashScreen(AverageJimGame game) {
 		super(game);
 	}
 	
 	@Override
 	public void show() {
 		super.show();
 		
 		//Load splash texture
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		stage.clear();
 		
 		//Define texture region as a drawable
 		AtlasRegion splashRegion = getAtlas("splash-screen").findRegion("splash-screen/SplashScreenJim");
 		Drawable splashDrawable = new TextureRegionDrawable(splashRegion);
 		
 		//Create the Actor for splash image and make it take up whole screen
 		Image splashImage = new Image(splashDrawable, Scaling.stretch,Align.bottom | Align.left);
 		splashImage.setFillParent(true);
 
 		//Change image to 0 alpha so that fadeIn will work
 		splashImage.getColor().a = 0f;
 		
		//Initial delay is necessary for fadeIn to be visible on Android
		splashImage.addAction(delay(0.5f,sequence(fadeIn(0.75f),delay(5.0f, fadeOut(0.75f)), 
 			new Action() {
 				public boolean act(float delta) {
 					game.setScreen(new MenuScreen(game));
 					return true;
 				}
			})));
 		
 		stage.addActor(splashImage);
 		
 	}
 
 }
