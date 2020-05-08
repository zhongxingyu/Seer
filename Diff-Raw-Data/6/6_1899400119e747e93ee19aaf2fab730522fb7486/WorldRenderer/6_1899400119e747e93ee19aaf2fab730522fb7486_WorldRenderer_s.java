 package com.picotech.lightrunnerlibgdx;
 
 import java.io.FileNotFoundException;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.picotech.lightrunnerlibgdx.GameScreen.GameState;
 
 public class WorldRenderer {
 	public OrthographicCamera camera;
 	private SpriteBatch batch;
 	private World world;
 	private StatLogger statlogger;
 	private ShapeRenderer sr;
 	private int width, height;
 	public boolean terminate = false;
 
 	// private Texture titleScreen;
 
 	public WorldRenderer(World world) {
 		this.world = world;
 		width = Gdx.graphics.getWidth();
 		height = Gdx.graphics.getHeight();
 		camera = new OrthographicCamera(1, height / width);
 		batch = new SpriteBatch();
 		sr = new ShapeRenderer();
 		loadContent();
 
 	}
 
 	private void loadContent() {
 		// titleScreen = new
 		// Texture(Gdx.files.internal("LightRunnerTitle.png"));
 		world.loadContent();
 	}
 
 	public void render(GameState state) {
 		if (state == GameState.PLAYING || state == GameState.MENU) {
 			if (state == GameState.PLAYING
 					|| (state == GameState.MENU && world.menu.menuState == Menu.MenuState.MAIN))
 				world.update();
 
 			world.draw(batch, sr);
 			if (world.player.alive == false) {
 				state = GameState.GAMEOVER;
 
 				// this is new
 				try {
 					world.toStatLogger(world.statlogger);
					world.statlogger.writeToFile();
 					// line below just for testing
 					// System.out.print("Wrote to file");
				} catch (FileNotFoundException e) {
 				}
 				// to remove later
 				terminate = true;
 			}
 		}
 		batch.begin();
 		if (state == GameState.LOADING) {
 			batch.draw(Assets.loadingScreen, 0, 0);
 		} else if (state == GameState.GAMEOVER) {
 
 		}
 		batch.end();
 	}
 }
