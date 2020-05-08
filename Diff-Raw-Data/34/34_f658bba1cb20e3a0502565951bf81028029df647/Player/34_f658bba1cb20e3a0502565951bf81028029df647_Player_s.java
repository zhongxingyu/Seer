 package fi.hackoid;
 
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class Player {
 
 	private BitmapTextureAtlas textureAtlas;
 	private TiledTextureRegion textureRegion;
 
 	AnimatedSprite animatedSprite;
 
 	private float oldSpeed;
 
 	boolean facingRight = true;
 
	private static FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
 
 	private int frameTime = 50;
 	private long[] frameTimes = new long[] { frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime,
 			frameTime, frameTime, frameTime, frameTime };
 
 	Body body;
 
 	public Player() {
 	}
 
 	public void createResources(Main main) {
 		textureAtlas = new BitmapTextureAtlas(main.getTextureManager(), 2048, 1024, TextureOptions.BILINEAR);
 		textureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, main,
 				"player_walking.png", 0, 0, 10, 4);
 		textureAtlas.load();
 	}
 
 	public void createScene(VertexBufferObjectManager vertexBufferObjectManager, int cameraWidth, int cameraHeight,
 			PhysicsWorld world) {
 		final float playerX = 100;
 		final float playerY = 200;
 
 		animatedSprite = new AnimatedSprite(playerX, playerY, textureRegion, vertexBufferObjectManager);
 		animatedSprite.setScaleCenterY(textureRegion.getHeight());
 		animatedSprite.setScale(1);
 
 		animatedSprite.setCurrentTileIndex(0);
 
 		animatedSprite.registerUpdateHandler(world);
 
 		FIXTURE_DEF.filter.groupIndex = -2;
 		body = PhysicsFactory.createBoxBody(world, animatedSprite, BodyType.DynamicBody, FIXTURE_DEF);
 		body.setUserData("player");
 
 		world.registerPhysicsConnector(new PhysicsConnector(animatedSprite, body, true, false));
 	}
 
 	public void run(float speed) {
		body.setLinearVelocity(speed / 20, body.getLinearVelocity().y);
 
 		if (speed == 0) {
 			animatedSprite.stopAnimation();
 			if (facingRight) {
 				animatedSprite.setCurrentTileIndex(0);
 			} else {
 				animatedSprite.setCurrentTileIndex(20);
 			}
 		} else if (oldSpeed == 0) {
 			animateRun();
 		} else if (facingRight && speed < 0 || !facingRight && speed > 0) {
 			facingRight = !facingRight;
 			animateRun();
 		}
 
 		oldSpeed = speed;
 	}
 
 	private void animateRun() {
 		if (facingRight) {
 			animatedSprite.animate(frameTimes, 0, 19, true);
 		} else {
 			animatedSprite.animate(frameTimes, 20, 39, true);
 		}
 	}
 
 	public void jump() {
 		if (Math.abs(body.getLinearVelocity().y) < 0.5) {
 			body.setLinearVelocity(body.getLinearVelocity().x, -15);
 		}
 	}
 
 }
