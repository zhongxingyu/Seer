 package edu.smcm.gamedev.butterseal;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
 
 public class ButterSeal implements ApplicationListener {
 	public static enum AssetInfo {
 		TITLE(718, 546, ""), // flame
 		ICE_CAVE(-1, -1, "data/maps/ice-cave.tmx");
 		
 		/**
 		 * Intended height of this sprite in pixels 
 		 */
 		private final int height;
 		/**
 		 * Intended width of this sprite in pixels
 		 */
 		private final int width;
 		/**
 		 * file path of the sprite
 		 */
 		private final String assetPath;
 		
 		/**
 		 * Creates a new SpriteDimension
 		 * @param height the intended height of this sprite in pixels
 		 * @param width the intended width of this sprite in pixels
 		 */
 		AssetInfo(int width, int height, String assetPath) {
 			this.height = height;
 			this.width = width;
 			this.assetPath = assetPath;
 		}
 	}
 	
 	private OrthographicCamera camera;
 	private AssetManager assetManager;
 	private BitmapFont font;
 	private SpriteBatch batch;
 	private TiledMap map;
 	private TiledMapRenderer renderer;
 	
 	@Override
 	public void create() {		
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 		
 		camera = new OrthographicCamera();
 		assetManager = new AssetManager();
 		batch = new SpriteBatch();
 		font = new BitmapFont();
 		
 		camera.setToOrtho(false, (w / h) * 10, 10);
 		camera.update();
 		
 		assetManager.setLoader(TiledMap.class,
 				new TmxMapLoader(
 						new InternalFileHandleResolver()));
 		assetManager.load(AssetInfo.ICE_CAVE.assetPath, TiledMap.class);
 		assetManager.finishLoading();
 
 		map = assetManager.get(AssetInfo.ICE_CAVE.assetPath);
 
		renderer = new OrthogonalTiledMapRenderer(map, 1f/50f);
 	}
 
 	@Override
 	public void dispose() {
 		batch.dispose();
 		assetManager.dispose();
 	}
 
 	@Override
 	public void render() {		
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		camera.update();
 		renderer.setView(camera);
 		renderer.render();
 		batch.begin();
 		font.draw(batch, "Hello, World", 20, Gdx.graphics.getHeight()-20);
 		batch.end();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 }
