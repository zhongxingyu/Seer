 package fr.odai.zerozeroduck;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 
 import fr.odai.zerozeroduck.model.Block;
 import fr.odai.zerozeroduck.model.Duck;
 import fr.odai.zerozeroduck.model.Patate;
 import fr.odai.zerozeroduck.model.Patate.State;
 import fr.odai.zerozeroduck.model.Trap;
 import fr.odai.zerozeroduck.model.World;
 import fr.odai.zerozeroduck.utils.TextureSetup;
 
 public class WorldRenderer {
 	private World world;
 	private OrthographicCamera cam;
 
 	private static final float CAMERA_WIDTH = 10f;
 	private static final float CAMERA_HEIGHT = 7f;
 
 	boolean debug = false;
 
 	private SpriteBatch spriteBatch;
 
 	/* Nos textures */
 	private TextureRegion duckTexture;
 	private TextureRegion blockTexture;
 	private TextureRegion trapTexture;
 	
 	private TextureRegion patateTexture;
 	private TextureRegion patateFrame;
 
 
	private static final float PATATE_RUNNING_FRAME_DURATION = 60f / World.BPM / 8;
 	private Animation walkRightPatate;
 
 	/** for debug rendering **/
 	ShapeRenderer debugRenderer = new ShapeRenderer();
 
 	private float ppuX; // pixels per unit on the X axis
 	private float ppuY; // pixels per unit on the Y axis
 
 	private int width;
 	private int height;
 
 	public void setSize(int w, int h) {
 		this.width = w;
 		this.height = h;
 		ppuX = (float) width / CAMERA_WIDTH;
 		ppuY = (float) height / CAMERA_HEIGHT;
 	}
 
 	public WorldRenderer(World world, boolean debug) {
 		this.world = world;
 		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
 		this.cam.position.set(CAMERA_WIDTH / 2f, CAMERA_HEIGHT / 2f, 0);
 		this.cam.update();
 		this.debug = debug;
 		spriteBatch = new SpriteBatch();
 		loadTextures();
 	}
 
 	public void render() {
 		spriteBatch.begin();
 		drawBlocks();
 		drawDuck();
 		drawPatates();
 		drawTrap();
 		spriteBatch.end();
 		if (debug)
 			drawDebug();
 	}
 
 	private void loadTextures() {
 		TextureSetup.main(null);
 		TextureAtlas atlas = new TextureAtlas(
 				Gdx.files.internal("images/textures.pack"));
 		patateTexture = atlas.findRegion("Patate1");
 		TextureRegion[] walkRightFrames = new TextureRegion[4];
 		for (int i = 0; i <= 3; i++) {
 			walkRightFrames[i] = atlas.findRegion("Patate" + (i+1));
 		}
 		walkRightPatate= new Animation(PATATE_RUNNING_FRAME_DURATION, walkRightFrames);
 
 		blockTexture = atlas.findRegion("block");
 		duckTexture = atlas.findRegion("block");
 		trapTexture = atlas.findRegion("trap");
 	}
 
 	private void drawBlocks() {
 		for (Block block : world.getBlocks()) {
 			spriteBatch.draw(blockTexture, block.getPosition().x * ppuX,
 					block.getPosition().y * ppuY, Block.SIZE * ppuX, Block.SIZE
 							* ppuY);
 		}
 	}
 
 	private void drawPatates() {
 		for (Patate patate : world.getPatates()) {
 			patateFrame = patateTexture;
 			if(patate.getState().equals(State.WALKING)){
 				patateFrame = walkRightPatate.getKeyFrame(patate.getStateTime(), true);
 			}
 			spriteBatch.draw(patateFrame, patate.getPosition().x * ppuX, patate.getPosition().y * ppuY, patate.getBounds().width * ppuX, patate.getBounds().height * ppuY);
 		}
 	}
 
 	private void drawDuck() {
 		Duck bob = world.getDuck();
 		spriteBatch.draw(duckTexture, bob.getPosition().x * ppuX,
 				bob.getPosition().y * ppuY, Duck.SIZE * ppuX, Duck.SIZE * ppuY);
 	}
 
 	private void drawTrap() {
 		for (Trap trap : world.getTraps()) {
 			spriteBatch.draw(trapTexture, trap.getPosition().x * ppuX,
 					trap.getPosition().y * ppuY, Trap.SIZE * ppuX, Trap.SIZE
 							* ppuY);
 		}
 	}
 
 	private void drawDebug() {
 		// render blocks
 		debugRenderer.setProjectionMatrix(cam.combined);
 		debugRenderer.begin(ShapeType.Rectangle);
 		for (Block block : world.getBlocks()) {
 			Rectangle rect = block.getBounds();
 			float x1 = block.getPosition().x + rect.x;
 			float y1 = block.getPosition().y + rect.y;
 			debugRenderer.setColor(new Color(1, 0, 0, 1));
 			debugRenderer.rect(x1, y1, rect.width, rect.height);
 		}
 		for (Patate patate : world.getPatates()) {
 			Rectangle rect = patate.getBounds();
 			float x1 = patate.getPosition().x + rect.x;
 			float y1 = patate.getPosition().y + rect.y;
 			debugRenderer.setColor(new Color(1, 1, 0, 1));
 			debugRenderer.rect(x1, y1, rect.width, rect.height);
 		}
 		// render Duck
 		Duck duck = world.getDuck();
 		Rectangle rect = duck.getBounds();
 		float x1 = duck.getPosition().x + rect.x;
 		float y1 = duck.getPosition().y + rect.y;
 		debugRenderer.setColor(new Color(0, 1, 0, 1));
 		debugRenderer.rect(x1, y1, rect.width, rect.height);
 		debugRenderer.end();
 	}
 }
