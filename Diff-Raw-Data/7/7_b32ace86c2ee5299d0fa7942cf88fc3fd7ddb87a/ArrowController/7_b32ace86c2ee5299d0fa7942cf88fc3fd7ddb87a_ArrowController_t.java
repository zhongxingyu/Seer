 package com.pix.mind.controllers;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.box2d.bodies.PixGuy;
 
 public class ArrowController extends PixGuyController {
 	private Stage stage;
 
 	public ArrowController(PixGuy pixGuy, Stage stage) {
 		super(pixGuy);
 		this.stage = stage;
 		Texture arrowTexture = new Texture(Gdx.files.internal("data/textures/leftArrow.png"));
 		Image leftArrow = new Image(arrowTexture);
 		Image rightArrow = new Image(arrowTexture);
 		float arrowWidth = arrowTexture.getHeight();
 		float arrowHeight = arrowTexture.getWidth();
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
 		if (Gdx.input.isTouched()) {
 			System.out.println("x: " + Gdx.input.getX() + " y: "
 					+ Gdx.input.getY());
			if (Gdx.input.getY() >  stage.getHeight() * 3 / 4) {
				if (Gdx.input.getX() <  stage.getWidth() / 6) {
 					pixGuy.moveLeft(Gdx.graphics.getDeltaTime());
 				}
				if (Gdx.input.getX() >  stage.getWidth() - stage.getWidth() / 6) {
 					this.pixGuy.moveRight(Gdx.graphics.getDeltaTime());
 				}
 			}
 			// Gdx.input.
 		}
 	}
 }
