 package scenes.Main;
 
 import logic.Engine;
 import logic.Consts.DataDir;
 
 import org.lwjgl.opengl.Display;
 
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenEquations;
 import aurelienribon.tweenengine.TweenManager;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.shipvgdc.sugdk.tween.Tweens;
 
 public class Scene implements Screen {
 
 	Stage ui;
 	OrthographicCamera camera;
 	private boolean ready;
 	
 	TweenManager tm;
 	private Sprite field;
 	private Sprite statBars;
 	private Sprite scoreField;
 	
 	public Scene()
 	{
 		ui = new Stage();
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false);
 		ui.setCamera(camera);
 		tm = new TweenManager();
 	}
 	
 	@Override
 	public void dispose() {
 		unload();
 	}
 
 	@Override
 	public void hide() {
 		unload();
 	}
 
 	@Override
 	public void pause() {}
 
 	@Override
 	public void render(float delta) {
 		
 		//wait for assets to load
 		if (Engine.assets.update())
 		{
 			if (!ready)
 			{
 				create();
 				ready = true;
 			}
 			camera.update();
 			tm.update(delta);
 			ui.act(delta);
 			SpriteBatch sb = ui.getSpriteBatch();
 			sb.begin();
 			field.draw(sb);
 			statBars.draw(sb);
 			scoreField.draw(sb);
 			sb.end();
 			ui.draw();
 		}
 		
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		ui.setViewport(240, 320, false);
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void show() {
 		load();
 	}
 	
 	public void create()
 	{
 		statBars = new StatBars();
 		statBars.setPosition(0, 74);
 		
 		KeyDisplay keydisp = new KeyDisplay();
 		ui.addActor(keydisp);
 		
 		Texture t = Engine.assets.get(DataDir.Levels + "field001.png", Texture.class);
 		t.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
 		field = new Sprite(t);
 		scoreField = new ScoreField();
 		scoreField.setPosition(50, 295);
 		
 		Timeline.createSequence()
 			.push(Tween.set(field, Tweens.UV).target(0.0f, 1.0f, 1.0f, 2.0f))
 			.push(Tween.to(field, Tweens.UV, 5.0f).target(0.0f, 1.0f, 0.0f, 1.0f).ease(TweenEquations.easeNone))
 			.repeat(-1, 0f)
 		.start(tm);
 		
 		Gdx.input.setInputProcessor(ui);
 	}
 	
 	/**
 	 * Loads all visual assets of the scene into memory
 	 */
 	public void load()
 	{
 		KeyDisplay.loadAssets();
 		StatBars.loadAssets();
 		ScoreField.loadAssets();
 		Engine.assets.load(DataDir.Levels + "field001.png", Texture.class);
 		
 		ready = false;
 	}
 	
 	/**
 	 * Removes all visual assets of the scene from memory
 	 */
 	public void unload()
 	{
 		ui.clear();
 	}
 
 }
