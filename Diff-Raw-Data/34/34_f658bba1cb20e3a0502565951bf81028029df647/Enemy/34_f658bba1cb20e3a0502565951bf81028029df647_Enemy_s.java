 package fi.hackoid;
 
 import java.util.Random;
 
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import android.util.Log;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Filter;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class Enemy {
 
 	private static BitmapTextureAtlas textureAtlas;
 	private static BitmapTextureAtlas deathTextureAtlas;
 	
 	private static TiledTextureRegion textureRegion;
 	private static TiledTextureRegion deathTextureRegion;
 
 	AnimatedSprite animatedSprite;
 
 	private Main main;
 
 	Body body;
 
 	boolean dead = false;
 
 	private Random random = new Random();
 
	private static FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
 	private int frameTime = 80;
 	private long[] frameTimes = new long[] { frameTime, frameTime, frameTime, frameTime, frameTime, frameTime,
 			frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime, frameTime,
 			frameTime, frameTime, frameTime, frameTime, frameTime };
 	
 	private int deathFrameTime = 150;
 	private long[] deathFrameTimes = new long[] { deathFrameTime, deathFrameTime, deathFrameTime, deathFrameTime, deathFrameTime,
 			deathFrameTime, deathFrameTime, deathFrameTime, deathFrameTime, deathFrameTime			
 	};
 
 	public Enemy(Main main) {
 		this.main = main;
 		createResources(main);
 		createScene(main.getVertexBufferObjectManager(), Main.CAMERA_WIDTH, Main.CAMERA_HEIGHT, main.world);
 		main.scene.attachChild(animatedSprite);
 	}
 
 	public void createResources(Main main) {
 		if (textureAtlas != null && deathTextureAtlas != null) {
 			return;
 		}
 		
 		textureAtlas = new BitmapTextureAtlas(main.getTextureManager(), 2048, 2048	, TextureOptions.BILINEAR);
 		textureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, main,
 				"monster_teekkari_animationframes.png", 0, 0, 10, 6);
 		textureAtlas.load();
 	}
 
 	public void createScene(VertexBufferObjectManager vertexBufferObjectManager, int cameraWidth, int cameraHeight,
 			PhysicsWorld world) {
 		float x = main.player.animatedSprite.getX() + 700 + random.nextInt(3000);
 		float y = 200;
 
 		animatedSprite = new AnimatedSprite(x, y, textureRegion, vertexBufferObjectManager);
 		animatedSprite.setScaleCenterY(textureRegion.getHeight());
 		animatedSprite.setScale(1);
 
 		animatedSprite.animate(frameTimes, 20, 39, true);
 
 		animatedSprite.registerUpdateHandler(world);
 
 		FIXTURE_DEF.filter.groupIndex = -4;
 		body = PhysicsFactory.createBoxBody(world, animatedSprite, BodyType.DynamicBody, FIXTURE_DEF);
 		body.setUserData("enemy");
 
 		world.registerPhysicsConnector(new PhysicsConnector(animatedSprite, body, true, false));
 
 		body.setLinearVelocity(-20, 0);
 	}
 
 	public Body getPhysicsBody() {
 		return body;
 	}
 
 	public AnimatedSprite getAnimatedSprite() {
 		return animatedSprite;
 	}
 
 	public void die() {
 		dead = true;
 		body.setLinearVelocity(0, 0);
 		Filter fil = body.getFixtureList().get(0).getFilterData();
 		fil.groupIndex = -2;
 		body.getFixtureList().get(0).setFilterData(fil);
 		
 		float playerX = main.player.animatedSprite.getX();
 		float enemyX = animatedSprite.getX();
 		
 		Log.w("debug", "playerX: " + playerX + " enemyX: " + enemyX);
 		
 		if(playerX > enemyX)
 		{
 			animatedSprite.animate(deathFrameTimes, 50, 59, false);
 		}
 		else
 		{
 			animatedSprite.animate(deathFrameTimes, 40, 49, false);
 		}
 		//animatedSprite.setRotation(270);
 
 
 	}
 
 }
