 package ddmp.projecttetra.entity;
 
 import java.util.List;
 
 import org.andengine.engine.Engine;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 import ddmp.projecttetra.RegionManager;
 import ddmp.projecttetra.TetraActivity;
 import ddmp.projecttetra.Utilities;
 
 /**
  * A moon in the game.
  */
 public class Moon extends Entity {
 	
 	private static final FixtureDef MOON_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, true);
 	private static final float MOON_MIN_SIZE = 0.08f; //In percent of camera height
 	private static final float MOON_MAX_SIZE = 0.12f;	//In percent of camera height
 	
 	private Entity planet;
 	
 	public static Moon createMoon(Engine engine, PhysicsWorld physicsWorld, float x, float y,
 			Entity planet) {
 		float scale = Utilities.getRandomFloatBetween(MOON_MIN_SIZE, MOON_MAX_SIZE);
 		float size = scale * TetraActivity.CAMERA_HEIGHT;
 		Sprite sprite = new Sprite(x, y, size, size, RegionManager.getInstance().get(
 				RegionManager.Region.MOON), engine.getVertexBufferObjectManager());
 		Body body = PhysicsFactory.createCircleBody(physicsWorld, sprite, BodyType.StaticBody,
 				MOON_FIXTURE_DEF);
 		return new Moon(engine, physicsWorld, sprite, body, planet);
 	}
 	
 	private Moon(Engine engine, PhysicsWorld physicsWorld, Sprite sprite, Body body,
 			Entity planet) {
 		super(engine, physicsWorld, sprite, body);
 		this.planet = planet;
 	}
 	
 	@Override
 	public void onUpdate(float pSecondsElapsed) {
 		if(planet.isDestroyed()) {
 			destroySelf();
 			return;
 		}
 		checkForCollisionWithCommet();
 	}
 	
 	private void checkForCollisionWithCommet() {
 		List<Contact> contacts = physicsWorld.getContactList();
 		Contact contact = null;
 		int size = contacts.size();
 		for(int i = 0; i < size; i++) {
 			contact = contacts.get(i);
 			if(contact.isTouching()) {
				Object aData = contact.getFixtureA().getUserData();
				Object bData = contact.getFixtureB().getUserData();
 				if((aData == this || bData == this) && 
 						(aData instanceof Comet || bData instanceof Comet)) {
 					/* This has collided with comet, break apart. */
 					breakApart();
 					destroySelf();
 				}
 			}
 		}
 	}
 	
 	private void breakApart() {
 		ITextureRegion moonTextureRegion = RegionManager.getInstance().get(RegionManager.Region.MOON);
 		ITexture texture = moonTextureRegion.getTexture();
 		int tX = (int) moonTextureRegion.getTextureX();
 		int tY = (int) moonTextureRegion.getTextureY();
 		int tW = (int) moonTextureRegion.getWidth();
 		int tH = (int) moonTextureRegion.getHeight();
 		ITextureRegion reg1 = TextureRegionFactory.extractFromTexture(texture, tX, tY, tW/2, tH/2);
 		ITextureRegion reg2 = TextureRegionFactory.extractFromTexture(texture, tX+tW/2, tY, tW/2, tH/2);
 		ITextureRegion reg3 = TextureRegionFactory.extractFromTexture(texture, tX, tY+tH/2, tW/2, tH/2);
 		ITextureRegion reg4 = TextureRegionFactory.extractFromTexture(texture, tX+tW/2, tY+tH/2, tW/2, tH/2);
 		
 		Sprite sprite = (Sprite) bodySpriteConnector.getShape();
 		float sX = sprite.getX();
 		float sY = sprite.getY();
 		float size = sprite.getWidth() / 2;
 		MoonPiece.createMoonPiece(engine, physicsWorld, sX, sY, size, reg1).registerSelf();
 		MoonPiece.createMoonPiece(engine, physicsWorld, sX+size, sY, size, reg2).registerSelf();
 		MoonPiece.createMoonPiece(engine, physicsWorld, sX, sY+size, size, reg3).registerSelf();
 		MoonPiece.createMoonPiece(engine, physicsWorld, sX+size, sY+size, size, reg4).registerSelf();
 	}
 
 	@Override
 	public void reset() {
 		
 	}
 
 }
