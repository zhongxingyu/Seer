 package com.pix.mind.levels;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.world.PixMindBox2DInitialization;
 import com.pix.mind.world.PixMindGuiInitialization;
 import com.pix.mind.world.PixMindScene2DInitialization;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 public class PixMindLevel implements Screen {
 	
 	// VARIABLES COMUNES A TODOS LOS NIVELES ///////////
 	PixMindGame game;
 	PixMindWorldRenderer worldRenderer;
 	PixMindScene2DInitialization scene2D;
 	PixMindBox2DInitialization box2D;
 	PixMindGuiInitialization gui;
 	int nActiveColors;
 	// variables of the screen
 
 	// aspect ratio and levelSize needs to be 1.3 ALWAYS
 	// (to support minimum resolution device)
 
 	float levelSizeHeight = 1000;
 	float levelSizeWidth = 1333;
 
 	// this point must be the first platform contact + its half height
 	float cameraBeginsY = 210;
 
 	// point of start of pixGuy in meters...
 
 	float pixGuyBeginsX = 4;
 	float pixGuyBeginsY = 4;
 	////////////////////////////////////////////////////
 
 	public PixMindLevel(PixMindGame game, float levelSizeWidth, float levelSizeHeight, float cameraBeginsY, float pixGuyBeginsX, float pixGuyBeginsY, int nActiveColors) {
 		super();
 		this.game = game;
 		this.nActiveColors = nActiveColors;
 		this.levelSizeHeight = levelSizeHeight;
 		this.levelSizeWidth = levelSizeWidth;
 		this.cameraBeginsY = cameraBeginsY;
 		this.pixGuyBeginsX = pixGuyBeginsX;
 		this.pixGuyBeginsY = pixGuyBeginsY;
 		
 	}
 
 	@Override
 	public void render(float delta) {
 		// TODO Auto-generated method stub
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		worldRenderer.render(delta);
 		
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void show(){
 		
 		scene2D = new PixMindScene2DInitialization(levelSizeWidth, levelSizeHeight);
 	//	scene2D.setLevelSize(levelSizeWidth, levelSizeHeight);
 		
 		
 		
 		box2D = new PixMindBox2DInitialization (scene2D, game, nActiveColors);
 		box2D.setCameraBeginsY(cameraBeginsY);		
 		box2D.setPixGuyPosition(pixGuyBeginsX, pixGuyBeginsY);
 //		box2D.getContactListener().setNextLevel(new LevelOne(game));
		box2D.addActivatedColor(Color.BLUE);
 		
 		gui = new PixMindGuiInitialization(scene2D, box2D, game);
 		
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	// GETTERS & SETTERS
 	
 	public void setNextLevel(PixMindLevel nextLevel){
 		gui.getMenuInGame().setNextLevelScreen(nextLevel);
 	}
 	public void setActiveLevel(PixMindLevel activeLevel){
 		gui.getMenuInGame().setActiveLevelScreen(activeLevel);
 	}
 
 }
