 package fi.hackoid;
 
 import java.util.Random;
 
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class SpearProjectile {
 
 	private BitmapTextureAtlas textureAtlas;
 	private TextureRegion textureRegion;
 
 	Sprite sprite;
 
 	Body body;
 	Main main;
 
 	private Random random = new Random();
 
 	private static FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0f);
 	static {
 		FIXTURE_DEF.filter.groupIndex = -2;
 	}
 
 	public SpearProjectile(Main main, PhysicsWorld world, AnimatedSprite sourceSprite, boolean right) {
 		createResources(main);
 		createScene(main.getVertexBufferObjectManager(), world, sourceSprite, right);
 		main.scene.attachChild(sprite);
 		synchronized (main.beers) {
 			main.spears.add(this);
 		}
 		this.main = main;
 	}
 
 	public void createResources(Main main) {
 		textureAtlas = new BitmapTextureAtlas(main.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
 		textureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.textureAtlas, main,
 				"projectile_spear.png", 0, 0);
 		textureAtlas.load();
 	}
 
 	public void createScene(VertexBufferObjectManager vertexBufferObjectManager, PhysicsWorld world,
 			AnimatedSprite sourceSprite, boolean right) {
 		float projectileX = sourceSprite.getX() + sourceSprite.getWidth() / 2;
 		float projectileY = sourceSprite.getY();
 
 		sprite = new Sprite(projectileX, projectileY, textureRegion, vertexBufferObjectManager);
 		sprite.setScaleCenterY(textureRegion.getHeight());
 		sprite.setScale(1);
 		sprite.setRotation(90);
 
 		sprite.registerUpdateHandler(world);
 
 		body = PhysicsFactory.createBoxBody(world, sprite, BodyType.DynamicBody, FIXTURE_DEF);
 		body.setUserData("spear");
 
 		world.registerPhysicsConnector(new PhysicsConnector(sprite, body, true, false));
 
 		body.setGravityScale(0.1f);
 		body.setLinearDamping(0.05f);
		body.setLinearVelocity((1 + random.nextInt(5)) * (right ? 1 : -1), -random.nextInt(5));
 	}
 
 	public void destroy() {
 		synchronized (main.spears) {
 			main.spears.remove(this);
 		}
 		synchronized (main.spearsToBeRemoved) {
 			main.spearsToBeRemoved.add(this);
 		}
 	}
 }
