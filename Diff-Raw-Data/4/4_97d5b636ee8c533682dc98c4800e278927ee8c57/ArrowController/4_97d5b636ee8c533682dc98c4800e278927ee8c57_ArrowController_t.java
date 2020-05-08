 package com.pix.mind.controllers;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
 import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.box2d.bodies.PixGuy;
 
 public class ArrowController extends PixGuyController {
 	private Stage stage;
 	
 	public ArrowController(final PixGuy pixGuy, final Stage stage) {
 		super(pixGuy);
 		this.stage = stage;
 		Drawable arrowTexture =	 PixMindGame.getSkin().getDrawable("leftArrow");
 		
 		Image leftArrow = new Image(arrowTexture);
 		Image rightArrow = new Image(arrowTexture);
		float arrowWidth = 60;
		float arrowHeight = 60;
 		leftArrow.setSize(arrowWidth, arrowHeight);
 		rightArrow.setSize(arrowWidth, arrowHeight);
 		rightArrow.setOrigin(arrowWidth / 2, arrowHeight/2);
 		rightArrow.rotate(180);
 		leftArrow.setPosition(arrowWidth / 10, arrowHeight / 10);
 		rightArrow.setPosition(
 				stage.getWidth() - arrowWidth / 10 - arrowWidth,
 				arrowHeight / 10);
 		this.stage.addActor(leftArrow);
 		this.stage.addActor(rightArrow);
 	
 	}
 
 	@Override
 	public void movements() {
 		
 		if(isActive()){
 			
 		if (Gdx.input.isTouched()) {	
 		//	System.out.println(Gdx.input.getY()/fromScreenToFixedScreenWidth + " " + Gdx.input.getX()/fromScreenToFixedScreenHeight );
 			if (Gdx.input.getY()/PixMindGame.fromRealScreenToFixedScreenWidth > PixMindGame.h-100) {
 				if (Gdx.input.getX()/PixMindGame.fromRealScreenToFixedScreenHeight <  100 ) {				
 					pixGuy.moveLeft(Gdx.graphics.getDeltaTime());					
 				}
 				if (Gdx.input.getX()/PixMindGame.fromRealScreenToFixedScreenHeight >  PixMindGame.w-100) {					
 					pixGuy.moveRight(Gdx.graphics.getDeltaTime());					
 				}
 			}			
 		}else{//if it is not touched, set horizontal velocity to 0 to eliminate inercy.			
 			pixGuy.body.setLinearVelocity(0,pixGuy.body.getLinearVelocity().y);			
 		}
 		}
 		
 		
 	}
 
 	
 }
