 package com.dat255_group3.screen;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.dat255_group3.controller.MyGdxGameController;
import com.dat255_group3.controller.OrthographicCameraController;
 import com.dat255_group3.controller.SoundController;
import com.dat255_group3.utils.CameraFactory;
import com.dat255_group3.utils.CoordinateConverter;
 
 /**
  * A class which represents the startmenu of the game. The user is given the
  * options of choosing a character, starting the game or to exit.
  * 
  * @author The Hans-Gunnar Crew
  * 
  */
 public class StartScreen implements Screen {
 
 	private MyGdxGameController myGdxGameController;
 	private Stage stage;
 	private TextureAtlas atlas;
 	private Skin skin;
 	private Table table;
 	private BitmapFont black;
 	private BitmapFont white;
	private OrthographicCamera camera;
 
 	// import aurelienribon.tweenengine.TweenManager;
 	// private TweenManager tweenmanager;
 
 	public StartScreen(MyGdxGameController myGdxGameController) {
 		this.myGdxGameController = myGdxGameController;
		this.stage = new Stage(CoordinateConverter.getCameraWidth(), 
								CoordinateConverter.getCameraHeight(), true);
		this.camera = CameraFactory.Create();
		stage.setCamera(camera);
		stage.setViewport(CoordinateConverter.getCameraWidth(), CoordinateConverter.getCameraHeight(), true);
		stage.getCamera().update();
 	}
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		// Update & draw the stage actors
 		stage.act(delta);
 		// Table.drawDebug(stage); //To be removed later on
 		stage.draw();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// In order to make it look good not depending on the screensize.
		stage.setViewport(CoordinateConverter.getCameraWidth(), CoordinateConverter.getCameraHeight(), true);
 		table.invalidateHierarchy();
		table.setSize(CoordinateConverter.getCameraWidth(), CoordinateConverter.getCameraHeight());
 	}
 
 	@Override
 	public void show() {
 		// Setting up the stage
		this.stage = new Stage(CoordinateConverter.getCameraWidth(), 
				CoordinateConverter.getCameraHeight(), true);
 		Gdx.input.setInputProcessor(stage);
		stage.setCamera(camera);
		
 		// Setting up the atlas, skin & fonts
 		atlas = new TextureAtlas(Gdx.files.internal("ui/button.pack"));
 		skin = new Skin(atlas);
 		black = new BitmapFont(Gdx.files.internal("font/black.fnt"), false);
 		white = new BitmapFont(Gdx.files.internal("font/whiteL.fnt"), false);
 		white.scale(1.2f);
 
 		// Setting up the table
 		table = new Table(skin);
		table.setBounds(0, 0, CoordinateConverter.getCameraWidth(),
							CoordinateConverter.getCameraHeight());
 
 		// Setting characteristics for the label
 		LabelStyle labelStyle = new LabelStyle();
 		labelStyle.font = white;
 		Label label = new Label("CookieGame", labelStyle);
 
 		// Setting up the characteristics for the button
 		TextButtonStyle textButtonStyle = new TextButtonStyle();
 		textButtonStyle.up = skin.getDrawable("button.up");
 		textButtonStyle.down = skin.getDrawable("button.down");
 		textButtonStyle.checked = skin.getDrawable("button.down");
 		textButtonStyle.pressedOffsetX = 1;
 		textButtonStyle.pressedOffsetY = -1;
 		textButtonStyle.font = black;
 		textButtonStyle.checkedFontColor = Color.RED;
 
 		// Setting buttons & listeners with the options of starting the game or
 		// to quit
 		TextButton startButton = new TextButton("Start", textButtonStyle);
 		startButton.pad(20);
 		startButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				myGdxGameController.getMyGdxGame().setIsGameStarted(false);
 				myGdxGameController.setScreen(new LevelScreen(
 						myGdxGameController));
 			}
 		});
 
 		TextButton exitButton = new TextButton("Exit", textButtonStyle);
 		exitButton.pad(20);
 		exitButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				Gdx.app.exit();
 			}
 		});
 
 		ImageButtonStyle soundButtonStyle = new ImageButtonStyle();
 		TextureAtlas soundAtlas = new TextureAtlas(
 				Gdx.files.internal("ui/music/sound.pack"));
 		Skin soundSkin = new Skin(soundAtlas);
 		soundButtonStyle.up = soundSkin.getDrawable("sound.down");
 		soundButtonStyle.down = soundSkin.getDrawable("sound.down");
 		soundButtonStyle.checked = soundSkin.getDrawable("sound.up");
 
 		ImageButton soundEButton = new ImageButton(soundButtonStyle);
 		soundEButton.pad(20);
 		soundEButton.toggle();
 		soundEButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				if (MyGdxGameController.soundEffectsOn()) {
 					myGdxGameController.soundEffectsOn(false);
 				} else {
 					myGdxGameController.soundEffectsOn(true);
 				}
 			}
 		});
 
 		ImageButtonStyle musicButtonStyle = new ImageButtonStyle();
 		TextureAtlas musicAtlas = new TextureAtlas(
 				Gdx.files.internal("ui/music/music.pack"));
 		Skin musicSkin = new Skin(musicAtlas);
 		musicButtonStyle.up = musicSkin.getDrawable("music.down");
 		musicButtonStyle.down = musicSkin.getDrawable("music.pressed");
 		musicButtonStyle.checked = musicSkin.getDrawable("music.up");
 
 		ImageButton musicButton = new ImageButton(musicButtonStyle);
 		musicButton.pad(20);
 		musicButton.toggle();
 		musicButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				if (SoundController.backgroundMusicIsPlaying()) {
 					SoundController.pauseBackgroundMusic();
 				} else {
 					SoundController.playBackgroundMusic();
 				}
 			}
 		});
 
 		Table table2 = new Table(skin);
 		table2.setBounds(0, 0, 100, 50);
 
 		// Adding to the table and actors to the stage
 		table.add(label);
 		// table.getCell(label).spaceBottom(50);
 		table.row();
 		table.add(startButton);
 		// table.getCell(startButton).spaceBottom(50);
 		table.row();
 		table.add(exitButton);
 		// table.getCell(exitButton).spaceBottom(100);
 		table.row();
 		table2.center();
 		table2.add(soundEButton).right();
 		table2.add(musicButton);
 		table.add(table2);
 		table.row();
 		stage.addActor(table);
 
 		table.debug(); // To be removed later on
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void dispose() {
 		stage.dispose();
 		skin.dispose();
 		atlas.dispose();
 		black.dispose();
 
 	}
 
 }
