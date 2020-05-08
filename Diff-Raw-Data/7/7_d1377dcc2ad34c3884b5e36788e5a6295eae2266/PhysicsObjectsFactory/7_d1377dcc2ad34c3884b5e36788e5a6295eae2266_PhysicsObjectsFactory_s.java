 package com.gemserk.games.taken;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class PhysicsObjectsFactory {
 
 	private final World world;
 
 	private final Vector2 tmp = new Vector2();
 
 	public PhysicsObjectsFactory(World world) {
 		this.world = world;
 	}
 
 	public Body createGround(Vector2 position, Vector2 size) {
 		Vector2[] vertices = new Vector2[] { //
 		new Vector2(-size.x * 0.5f, -size.y * 0.5f), //
 				new Vector2(size.x * 0.5f, -size.y * 0.5f), //
 				new Vector2(size.x * 0.5f, size.y * 0.5f), //
 				new Vector2(-size.x * 0.5f, size.y * 0.5f), //
 		};
 		return createGround(position, vertices);
 	}
 
 	public Body createGround(Vector2 position, final Vector2[] vertices) {
 		PolygonShape shape = new PolygonShape();
 		shape.set(vertices);
 
 		// shape.setAsBox(2f, 2f);
 
 		BodyDef groundBodyDef = new BodyDef();
 		groundBodyDef.type = BodyType.StaticBody;
 		groundBodyDef.position.set(position);
 		Body body = world.createBody(groundBodyDef);
 
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = shape;
 		fixtureDef.friction = 0.5f;
 		fixtureDef.density = 1f;
 
 		body.createFixture(fixtureDef);
 		shape.dispose();
 
 		return body;
 	}
 	
 	public Body createBox(Vector2 position, Vector2 size) {
 		PolygonShape shape = new PolygonShape();
 		shape.setAsBox(size.x * 0.5f, size.y * 0.5f);
 
 		// shape.setAsBox(2f, 2f);
 
 		BodyDef groundBodyDef = new BodyDef();
 		groundBodyDef.type = BodyType.StaticBody;
 		groundBodyDef.position.set(position);
 		Body body = world.createBody(groundBodyDef);
 
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = shape;
 		fixtureDef.friction = 0.5f;
 		fixtureDef.density = 1f;
 
 		body.createFixture(fixtureDef);
 		shape.dispose();
 
 		return body;
 	}
 	
 	public Vector2[] createRectangle(float width, float height) {
 		Vector2[] vertices = Box2dUtils.initArray(4);
 		Box2dUtils.createRectangle(width, height, vertices);
 		return vertices;
 	}
 	
 	public Body createPolygonBody(float x, float y, Vector2[] vertices, boolean fixedRotation, float friction, float density, float restitution) { 
 
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.type = BodyType.DynamicBody;
 		bodyDef.position.set(x, y);
 		bodyDef.fixedRotation = fixedRotation;
 
 		Body body = world.createBody(bodyDef);
 
 		PolygonShape shape = new PolygonShape();
 		shape.set(vertices);
 
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = shape;
 		fixtureDef.density = density;
 		fixtureDef.friction = friction;
 		fixtureDef.restitution =restitution;
 
 		body.createFixture(fixtureDef);
 		
 		shape.dispose();
 		
 		tmp.set(x,y);
 		body.setTransform(tmp, 0f);
 
 		return body;
 	}
 
 	public Body createDynamicRectangle(float x, float y, float width, float height, boolean fixedRotation, float friction) {
 
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.type = BodyType.DynamicBody;
 		// bodyDef.bullet = true;
 		bodyDef.position.set(x, y);
 		bodyDef.fixedRotation = fixedRotation;
 
 		Body body = world.createBody(bodyDef);
 
 		PolygonShape shape = new PolygonShape();
 		shape.setAsBox(width / 2f, height / 2f);
 
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = shape;
 		fixtureDef.density = 1f;
 		fixtureDef.friction = friction;
 
 		body.createFixture(fixtureDef);
 
 		shape.dispose();
 
 		// 0.7112 mts
 
 		// body.setTransform(tmp, (float) (direction.angle() / 180f * Math.PI));
 
 		return body;
 
 	}
 
 	public Body createDynamicCircle(float x, float y, float radius, boolean fixedRotation, float friction) {
 
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.type = BodyType.DynamicBody;
 		// bodyDef.bullet = true;
 		bodyDef.position.set(x, y);
 		bodyDef.fixedRotation = fixedRotation;
 
 		Body body = world.createBody(bodyDef);
 
 		CircleShape shape = new CircleShape();
 		// PolygonShape shape = new PolygonShape();
 		// shape.setPosition(position)
 		shape.setRadius(radius);
 		// shape.setAsBox(width / 2f, height / 2f);
 
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = shape;
 		fixtureDef.density = 1f;
 		fixtureDef.friction = friction;
 
 		body.createFixture(fixtureDef);
 
 		shape.dispose();
 
 		// 0.7112 mts
 
 		// body.setTransform(tmp, (float) (direction.angle() / 180f * Math.PI));
 
 		return body;
 
 	}
 
 }
