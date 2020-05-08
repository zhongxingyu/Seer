 package com.tacoid.puyopuyo;
 
 import java.util.Calendar;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.GLCommon;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.tacoid.puyopuyo.logic.GameLogic;
 import com.tacoid.puyopuyo.logic.State;
 
 public class GameSoloScreen implements Screen {
 	private static final int VIRTUAL_WIDTH = 768;
 	private static final int VIRTUAL_HEIGHT = 1280;
 	private static GameSoloScreen instance = null;
 	protected Stage stage;
 	protected GameLogic gameLogic;
 	private GridActor gridActor;
 	private InputProcessor controller;
 	protected boolean end = false;
 
 	private class DownButton extends Button {
 
 		private long last = 0;
 
 		public DownButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			x = 29;
 			y = 5;
 		}
 
 		@Override
 		public boolean touchDown(float x, float y, int pointer) {
 			if (end) return false;
 			super.touchDown(x, y, pointer);
 			gameLogic.down();
 			//gameLogic.dropPiece();
 			return true;
 		}
 
 		@Override
 		public void touchUp(float x, float y, int pointer) {
 			if (end) return;
 			super.touchUp(x, y, pointer);
 			gameLogic.up();			
 			if (Calendar.getInstance().getTimeInMillis() - last < 500) {
 				gameLogic.dropPiece();
 			}
 			
 			last  = Calendar.getInstance().getTimeInMillis();
 		}
 	}
 
 	private class LeftButton extends Button {
 
 		public LeftButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			x = 29;
 			y = 85;
 		}
 
 		@Override
 		public boolean touchDown(float x, float y, int pointer) {
 			if (end) return false;
 			super.touchDown(x, y, pointer);
 			gameLogic.moveLeft();
 			return true;
 		}
 	}
 
 	private class RotateLeftButton extends Button {
 
 		public RotateLeftButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			x = 29;
 			y = 162;
 		}
 
 		@Override
 		public boolean touchDown(float x, float y, int pointer) {
 			if (end) return false;
 			super.touchDown(x, y, pointer);
 			gameLogic.rotateLeft();
 			return true;
 		}
 	}
 
 	private class RotateRightButton extends Button {
 
 		public RotateRightButton(TextureRegion regionUp,
 				TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			x = 570;
 			y = 162;
 		}
 
 		@Override
 		public boolean touchDown(float x, float y, int pointer) {
 			if (end) return false;
 			super.touchDown(x, y, pointer);
 			gameLogic.rotateRight();
 			return true;
 		}
 	}
 
 	private class RightButton extends Button {
 
 		public RightButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			x = 570;
 			y = 85;
 		}
 
 		@Override
 		public boolean touchDown(float x, float y, int pointer) {
 			if (end) return false;
 			super.touchDown(x, y, pointer);
 			gameLogic.moveRight();
 			return true;
 		}
 
 	}
 
 	protected GameSoloScreen() {
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
 				false);
 		gameLogic = new GameLogic();
 		controller = new Controller(gameLogic, stage);
 
 		gridActor = new GridActor(gameLogic, 280, 320, 70);
 		NextPieceActor nextPieceActor = new NextPieceActor(gameLogic, 80, 920, 70);
 		ScoreActor scoreActor = new ScoreActor(gameLogic, 590, 1230);
 
 		TextureRegion backgroundRegion = new TextureRegion(
 				PuyoPuyo.getInstance().manager.get("images/fond_solo.png",
 						Texture.class), VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
 		Image background = new Image(backgroundRegion);
 
 		stage.addActor(background);
 		stage.addActor(gridActor);
 		stage.addActor(nextPieceActor);
 		stage.addActor(scoreActor);
 
 		TextureRegion leftRegion = new TextureRegion(
 				PuyoPuyo.getInstance().manager.get("images/left.png",
 						Texture.class), 80, 80);
 		TextureRegion leftDownRegion = new TextureRegion(
 				PuyoPuyo.getInstance().manager.get("images/left_down.png",
 						Texture.class), 80, 80);
 		stage.addActor(new LeftButton(leftRegion, leftDownRegion));
 
 		TextureRegion rightRegion = new TextureRegion(
 				PuyoPuyo.getInstance().manager.get("images/right.png",
 						Texture.class), 80, 80);
 		TextureRegion rightDownRegion = new TextureRegion(
 				PuyoPuyo.getInstance().manager.get("images/right_down.png",
 						Texture.class), 80, 80);
 		stage.addActor(new RightButton(rightRegion, rightDownRegion));
 
 		TextureRegion rotleftRegion = new TextureRegion(
 				PuyoPuyo.getInstance().manager.get("images/rotleft.png",
 						Texture.class), 80, 80);
 		TextureRegion rotleftDownRegion = new TextureRegion(
 				PuyoPuyo.getInstance().manager.get("images/rotleft_down.png",
 						Texture.class), 80, 80);
 		stage.addActor(new RotateLeftButton(rotleftRegion, rotleftDownRegion));
 
 		TextureRegion rotrightRegion = new TextureRegion(
 				PuyoPuyo.getInstance().manager.get("images/rotright.png",
 						Texture.class), 80, 80);
 		TextureRegion rotrightDownRegion = new TextureRegion(
 				PuyoPuyo.getInstance().manager.get("images/rotright_down.png",
 						Texture.class), 80, 80);
 		stage.addActor(new RotateRightButton(rotrightRegion, rotrightDownRegion));
 
 		TextureRegion downRegion = new TextureRegion(
 				PuyoPuyo.getInstance().manager.get("images/down.png",
 						Texture.class), 80, 80);
 		TextureRegion downDownRegion = new TextureRegion(
 				PuyoPuyo.getInstance().manager.get("images/down_down.png",
 						Texture.class), 80, 80);
 		stage.addActor(new DownButton(downRegion, downDownRegion));
 
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
 	}
 
 	public static GameSoloScreen getInstance() {
 		if (instance == null) {
 			instance = new GameSoloScreen();
 		}
 		return instance;
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
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
 	public void render(float delta) {
 		GLCommon gl = Gdx.gl;
 
 		if (gameLogic.getState() != State.LOST) {
 			// Update model
 			gameLogic.update(delta);
 		} else {
 			Gdx.input.setInputProcessor(null);
 			end = true;
 			// TODO
 		}
 
 		// clear previous frame
 		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		stage.act(delta);
 		stage.draw();
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
		stage.setViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, false);
		stage.getCamera().position
				.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void show() {
 		Gdx.input.setInputProcessor(controller );
 	}
 
 }
