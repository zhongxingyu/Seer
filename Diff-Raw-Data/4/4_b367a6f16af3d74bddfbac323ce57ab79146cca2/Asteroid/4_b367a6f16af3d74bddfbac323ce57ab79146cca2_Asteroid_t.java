 package ch.kanti_wohlen.asteroidminer.entities;
 
 import ch.kanti_wohlen.asteroidminer.Textures;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class Asteroid extends Entity {
 
 	public Asteroid(World world, Vector2 location, float radius) {
 		this(world, location, radius, null);
 	}
 
 	public Asteroid(World world, Vector2 location, float radius, Vector2 velocity) {
 		super(world, createBodyDef(location, velocity), createCircle(radius));
 	}
 
 	@Override
 	public void render(SpriteBatch batch) {
 		Sprite s = Textures.ASTEROID;
 		positionSprite(s);
 		s.draw(batch);
 	}
 
 	@Override
 	public boolean isRemoved() {
 		return false;
 	}
 
 	private static BodyDef createBodyDef(Vector2 position, Vector2 velocity) {
 		final BodyDef bodyDef = new BodyDef();
 		bodyDef.type = BodyType.DynamicBody;
 		bodyDef.position.set(position);
 		bodyDef.angle = MathUtils.random(2 * MathUtils.PI);
		if (velocity != null) {
			bodyDef.linearVelocity.set(velocity);
		}
 		bodyDef.gravityScale = 0.1f;
 
 		return bodyDef;
 	}
 
 	private static FixtureDef createCircle(float radius) {
 		final FixtureDef fixture = new FixtureDef();
 		fixture.density = 100f;
 		fixture.restitution = 0.9f;
 		final CircleShape cs = new CircleShape();
 		cs.setRadius(radius);
 		fixture.shape = cs;
 		return fixture;
 	}
 }
