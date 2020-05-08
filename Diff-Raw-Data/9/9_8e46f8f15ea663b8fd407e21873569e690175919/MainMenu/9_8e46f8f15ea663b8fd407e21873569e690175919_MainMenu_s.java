 package zamours;
 
 
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenManager;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 
 public class MainMenu implements Screen {
 	private float xDoigt, yDoigt;
 	boolean maintenu;
 
 	private SpriteBatch batch;
 	private Texture background, play, playPressed, options, optionsPressed;
 	private Sprite spritebackground, spritePlay, spritePlayPressed, spriteOptions, spriteOptionsPressed;
 	private Rectangle rectangleQuest1, rectangleQuest2;
 	int screenWidth, screenHeight, spaceBetweenAnswers, spaceBetweenQuestAnswers, positionQuestion1;
 	private TweenManager tweenManager;
 	
 	Jeu game;
 
 	MainMenu(Jeu game){
 		this.game = game;
 	}
 	
 
 	@Override
 	public void show() {
 		if (Jeu.getDesactiveMusicMenu() == false){
 			Jeu.musicMenu.play();
 		}else {
 			Jeu.musicMenu.stop();
 		}
 		//musicMenu.setVolume(0.5f); // permet de baisser le volume de la musique du menu
 		Texture.setEnforcePotImages(false);
 		
 		screenWidth = Gdx.graphics.getWidth();
 		screenHeight = Gdx.graphics.getHeight();
 		spaceBetweenAnswers = screenHeight / 6;
 		spaceBetweenQuestAnswers = screenHeight / 4;
 		positionQuestion1 = screenHeight / 2 + screenHeight / 7;
 		
 		batch = new SpriteBatch();
 		background = new Texture(
 				Gdx.files.internal("background_main_menu_s2.png"));
 		spritebackground = new Sprite(background);
 		spritebackground.setSize(screenWidth, screenHeight);
 
 		/********************************** Placement des boutons ****************************************************************/
 
 		play = new Texture(Gdx.files.internal("Play.png"));
 		playPressed = new Texture(Gdx.files.internal("PlayPressed.png"));
 		options = new Texture(Gdx.files.internal("Options.png"));
 		optionsPressed = new Texture(Gdx.files.internal("OptionsPressed.png"));
 		
 		spritePlay = new Sprite(play);
 		spritePlayPressed = new Sprite(playPressed);
 		spriteOptions = new Sprite(options);
 		spriteOptionsPressed = new Sprite(optionsPressed);
 		
 		spritePlay.setPosition(32, 550);
 		spritePlayPressed.setPosition(32, 550);
 		spriteOptions.setPosition(32, 400);
 		spriteOptionsPressed.setPosition(32, 400);
 
 		/*********************************************************************************************************************************/
 		/******************************************Effet**********************************************************************************/
 		tweenManager = new TweenManager();
 		Tween.registerAccessor(Actor.class, new ActorAccessor());
 		
 		Timeline.createSequence().beginSequence()
 		.push(Tween.set(spritePlay, ActorAccessor.ALPHA).target(0))
 		.push(Tween.set(spriteOptions, ActorAccessor.ALPHA).target(0))
 		.push(Tween.to(spritePlay, ActorAccessor.ALPHA , .6f).target(1))
		.push(Tween.to(spriteOptions, ActorAccessor.ALPHA, .3f).target(1))
 		.end().start(tweenManager);
 		/********************************************************************************************************************************/
 	}
 
 
 
 	@Override
 	public void dispose() {
 		batch.dispose();
 	}
 
 	@Override
 	public void pause() {
 
 	}
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 
 		manipulerMenu(); // gestion des input
 		
 		batch.begin();
 		spritebackground.draw(batch);
 		afficheBouton();
 		batch.end();
 		
 		tweenManager.update(delta);
 
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 	}
 
 	@Override
 	public void resume() {
 
 	}
 
 	@Override
 	public void hide() {
 
 		dispose();
 	}
 
 	public void afficheBouton() {
 		rectangleQuest1 = new Rectangle(67, screenHeight - 544 - 71, 348, 58);
 		rectangleQuest2 = new Rectangle(67, screenHeight - 383 - 80, 348, 58);
 
 		if (rectangleQuest1.contains(xDoigt, yDoigt) && maintenu) {
 
 			spritePlayPressed.draw(batch);
 
 		} else {
 			spritePlay.draw(batch);
 		}
 		if (rectangleQuest2.contains(xDoigt, yDoigt) && maintenu) {
 			spriteOptionsPressed.draw(batch);
 		} else {
 			spriteOptions.draw(batch);
 
 		}
 	}
 	
 	public void changementScreen(int x, int y){
 		if (rectangleQuest1.contains(x, y) && maintenu){
 			game.setScreen(new ChoixQuiz(game));
 		} else if (rectangleQuest2.contains(x, y) && maintenu){
 			game.setScreen(new Options(game));
 		}
 	}
 	
 	public void soundTouchDown(int x, int y){
 		if(rectangleQuest1.contains(x, y) || rectangleQuest2.contains(x, y)){
 			if (Jeu.getDesactiveMusicMenu() == false){
 				Jeu.musicMenu.setVolume(0.5f);
 			}else {
 				Jeu.musicMenu.setVolume(0.f);
 			}
 			if (Jeu.getDesactiveSoundTouchDown() == false)
 				Jeu.soundTouchDown.play();
 		}
 	}
 	
 	public void manipulerMenu() {
 		Gdx.input.setInputProcessor(new InputProcessor() {
 
 			@Override
 			public boolean touchUp(int x, int y, int pointer, int bouton) {
 				changementScreen(x, y);
 				xDoigt = 0;
 				yDoigt = 0;
 				maintenu = false;
 				return false;
 			}
 
 			@Override
 			public boolean touchDown(int x, int y, int pointer, int bouton) {
 				soundTouchDown(x, y);
 				xDoigt = x;
 				yDoigt = y;
 				maintenu = true;
 				return false;
 			}
 
 			@Override
 			public boolean touchDragged(int arg0, int arg1, int arg2) {
 				return false;
 			}
 
 			@Override
 			public boolean scrolled(int arg0) {
 				return false;
 			}
 
 			@Override
 			public boolean mouseMoved(int arg0, int arg1) {
 				return false;
 			}
 
 			@Override
 			public boolean keyUp(int arg0) {
 				return false;
 			}
 
 			@Override
 			public boolean keyTyped(char arg0) {
 				return false;
 			}
 
 			@Override
 			public boolean keyDown(int arg0) {
 				return false;
 			}
 		});
 	}
 }
