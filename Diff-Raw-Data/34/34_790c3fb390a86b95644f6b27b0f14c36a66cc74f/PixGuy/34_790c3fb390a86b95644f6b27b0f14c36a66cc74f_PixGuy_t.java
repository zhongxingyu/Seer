 package com.pix.mind.box2d.bodies;
 
import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.controllers.PixGuyController;
 
 public class PixGuy {
 	static public final String PIX_ID = "pixguy";
 	public static float pixWidth = 30;	
 	public static float pixHeight = 30;
 	
 	// pixmind main character
 	private float posX, posY;
 	private Body body;
 	static final float SPEED = 1;
 	public PixGuyController controller;
 	
 	public PixGuy(World world, float posX, float posY, float width, float height) {
 		// First we create a body definition
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.fixedRotation = true;
 		// We set our body to dynamic, for something like ground which doesn't
 		// move we would set it to StaticBody
 		bodyDef.type = BodyType.DynamicBody;
 		// Set our body's starting position in the world
 		setPosition(posX, posY);
 		bodyDef.position.set(this.posX, this.posY);
 
 		// Create our body in the world using our body definition
 		body = world.createBody(bodyDef);
	
 		// Create a polygon shape
 		PolygonShape groundBox = new PolygonShape();
 		// Set the polygon shape as a box which is twice the size of our view
 		// port and 20 high
 		// (setAsBox takes half-width and half-height as arguments)
 		// groundBox.setAsBox(camera.viewportWidth, 10.0f);
 		groundBox.setAsBox(width/2, height/2);
 		// Create a fixture definition to apply our shape to
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = groundBox;
 		fixtureDef.density = 0.5f;
 		fixtureDef.friction = 0.4f;
 		//fixtureDef.restitution = 0.5f; // Make it bounce a little bit
 		// Create our fixture and attach it to the body
 		Fixture fixture = body.createFixture(fixtureDef);
 		fixture.setUserData(PixGuy.PIX_ID);
 		
 		// Clean up after ourselves
 		groundBox.dispose();
 		this.pixHeight = height * PixMindGame.BOX_TO_WORLD;
 		this.pixWidth = width * PixMindGame.BOX_TO_WORLD;
 
 	}
 	
 	public float getPosX() {
 		return posX * PixMindGame.BOX_TO_WORLD - this.pixWidth/2;
 	}
 
 	public float getPosY() {
 		return posY * PixMindGame.BOX_TO_WORLD - this.pixHeight/2;
 	}
 
 	public void setController(PixGuyController controller){
 		this.controller = controller;
 	}
 
 	public void setPosition(float posX, float posY) {
 		this.posX = posX;
 		this.posY = posY;
 	}
 	
 	public void setActualPosition() {
 		controller.movements();
 		this.posY = body.getTransform().getPosition().y; 
 		body.setTransform(this.posX, this.posY, 0);
 	}
 	
 	
 	public void moveLeft(float deltaTime) {
	
		body.applyForce(new Vector2(-0.1f, 0),
				body.getWorldCenter(), true);
		this.posX = body.getTransform().getPosition().x;
 	}
 
 	public void moveRight(float deltaTime) {
		body.applyForce(new Vector2(0.1f, 0),
				body.getWorldCenter(), true);
		this.posX = body.getTransform().getPosition().x;
	
 	}
 }
