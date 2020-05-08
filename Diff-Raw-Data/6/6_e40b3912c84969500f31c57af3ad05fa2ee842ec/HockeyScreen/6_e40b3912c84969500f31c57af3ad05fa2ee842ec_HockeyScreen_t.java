 package org.gleason.superhockey.screens;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.gleason.superhockey.model.ArenaBarrier;
 import org.gleason.superhockey.model.GameActor;
 import org.gleason.superhockey.model.Pad;
 import org.gleason.superhockey.model.Puck;
 import org.gleason.superhockey.model.ScoreBoard;
 import org.gleason.superhockey.model.TargetBox;
 import android.view.MotionEvent;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Peripheral;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class HockeyScreen implements Screen, ContactListener {
 
 	private int count = 0;
 	private World world;
 	private Vector2 gravity = new Vector2(0, 0f);
 	private SpriteBatch batch;
 
 	private GameActor leftPad;
 	private GameActor puck;
 	private List<GameActor> targetBoxes = new ArrayList<GameActor>();
 	private List<GameActor> barriers = new ArrayList<GameActor>();
 	private List<GameActor> deadActors = new ArrayList<GameActor>();
 	private Box2DDebugRenderer debugRenderer;
 	private OrthographicCamera camera;
 	private boolean accelerometerPresent;
 
 	private static final float TIME_STEP = 1.0f / 45.0f;
 	private static final int VELOCITY_ITTERATIONS = 2;
 	private static final int POSITION_ITTERATIONS = 8;
 
 	private float accelX;
 	private float accelY;
 	private float accelZ;
 
 	private float topValue;
 	private float bottomValue;
 	private float rightValue;
 	private float leftValue;
 	private ScoreBoard scoreBoard;
 	private static final float BORDER_VAL = 10;
 	private Sprite bkgSprite;
 
 	static
 	{
 		System.loadLibrary("gl-jni");
 	}
 	
 	public HockeyScreen() {
 		world = new World(getGravity(), true);
 //		World.setVelocityThreshold(0.0f);
 		world.setContactListener(this);
 		setBatch(new SpriteBatch());
 		scoreBoard = new ScoreBoard();
 		setLeftPad(Pad.create(world, 50.0f, 30.0f, false));
 		setPuck(Puck.create(world, Gdx.graphics.getWidth() / 2,
 				Gdx.graphics.getHeight() / 2, false));
 
 		topValue = Gdx.graphics.getHeight() - BORDER_VAL;
 		bottomValue = BORDER_VAL;
 		rightValue = Gdx.graphics.getWidth() - BORDER_VAL;
 		leftValue = BORDER_VAL;
 
 		Texture mTexture = new Texture(Gdx.files.internal("Background.jpg"));
 		bkgSprite = new Sprite(mTexture,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
 		bkgSprite.setPosition(0, 0);
 		
 		genBoxMap();
 
 		barriers.add(ArenaBarrier.create(world, (Gdx.graphics.getWidth() / 2),
 				10, (Gdx.graphics.getWidth() / 2) - BORDER_VAL, 0, false));
 		barriers.add(ArenaBarrier.create(world, BORDER_VAL,
 				Gdx.graphics.getHeight() / 2, 0, (Gdx.graphics.getHeight() / 2)
 						- BORDER_VAL, false));
 		barriers.add(ArenaBarrier.create(world, Gdx.graphics.getWidth() / 2,
 				Gdx.graphics.getHeight() - BORDER_VAL, Gdx.graphics.getWidth()
 						/ 2 - BORDER_VAL, 0, false));
 		barriers.add(ArenaBarrier.create(world, Gdx.graphics.getWidth()
 				- BORDER_VAL, Gdx.graphics.getHeight() / 2, 0,
 				Gdx.graphics.getHeight() / 2 - BORDER_VAL, false));
 
 		setCamera(new OrthographicCamera());
 		getCamera().viewportHeight = GameActor.convertPixelsToMeters(Gdx.graphics.getHeight());
 		getCamera().viewportWidth = GameActor.convertPixelsToMeters(Gdx.graphics.getWidth());
 		getCamera().position.set(getCamera().viewportWidth * .5f,
 				getCamera().viewportHeight * .5f, 0f);
 		getCamera().update();
 		setDebugRenderer(new Box2DDebugRenderer());
 		((Puck) puck).startBody();
 		setAccelerometerPresent(Gdx.input
 				.isPeripheralAvailable(Peripheral.Accelerometer));
 	}
 
 	private void genBoxMap() {
 			for (float y = (topValue / 2 + 200); y < (topValue - 100); y = y
 					+ TargetBox.HEIGHT * 2) {
 				for (float x = BORDER_VAL + TargetBox.WIDTH + 50; x < rightValue
 						- (TargetBox.WIDTH * 2); x = x + (TargetBox.WIDTH * 2)) {
 					TargetBox tb = (TargetBox) TargetBox.create(world, x, y, false);
 					targetBoxes.add(tb);
 				}
 			}
 
 	}
 
 	public void refreshGravity() {
 		world.setGravity(getGravity());
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void render(float arg0) {
 		((Puck) puck).accelerate();
 
 		if (needsStoppedUp() && goingUp) {
 			leftPadStop();
 		}
 		if (needsStoppedDown() && goingDown) {
 			leftPadStop();
 		}
 		if (needsStoppedRight() && goingRight) {
 			leftPadStop();
 		}
 		if (needsStoppedLeft() && goingLeft) {
 			leftPadStop();
 		}
 		String test = resetScreen();
 //		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 //		Gdx.gl.glClearColor(1, 1, 1, 1);
 		getDebugRenderer().render(world, camera.combined);
 		Iterator<GameActor> i = deadActors.iterator();
 		while (i.hasNext()) {
 			GameActor walkingDead = i.next();
 			world.destroyBody(walkingDead.getBody());
 			targetBoxes.remove(walkingDead);
 			i.remove();
 		}
 		if(puck.getBody().getAngle()<.01){
 			String test1 = "test";
 		}
 		world.step(TIME_STEP, VELOCITY_ITTERATIONS, POSITION_ITTERATIONS);
 		batch.begin();
 		bkgSprite.draw(batch);
 		puck.drawSprite(batch);
 		for(GameActor box : targetBoxes){
 			box.drawSprite(batch);
 		}
 		leftPad.drawSprite(batch);
 		batch.end();
 	}
 
 	private boolean needsStoppedUp() {
 		Pad pad = (Pad) leftPad;
 		if (pad.getEndY() > topValue) {
 			return true;
 		}
 		return false;
 	}
 
 	private boolean needsStoppedDown() {
 		Pad pad = (Pad) leftPad;
 		if (pad.getStartY() < bottomValue) {
 			return true;
 		}
 		return false;
 	}
 
 	private boolean needsStoppedRight() {
 		Pad pad = (Pad) leftPad;
 		float point1 = pad.getEndX();
		if (pad.getEndX() > rightValue) {
 			return true;
 		}
 		return false;
 	}
 
 	private boolean needsStoppedLeft() {
 		Pad pad = (Pad) leftPad;
		if (pad.getStartX() < leftValue) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void beginContact(Contact contact) {
 
 	}
 
 	@Override
 	public void endContact(Contact contact) {
 		// TODO Auto-generated method stub
 		Body bodyA = contact.getFixtureA().getBody();
 		Body bodyB = contact.getFixtureB().getBody();
 		for (GameActor targetBox : targetBoxes) {
 			TargetBox tb = (TargetBox) targetBox;
 
 			Body targetBoxBody = tb.getBody();
 			if (targetBoxBody != null
 					&& (bodyA.equals(targetBoxBody) || bodyB
 							.equals(targetBoxBody))) {
 
 				deadActors.add(tb);
 				scoreBoard.addPoints(tb.getScoreValue());
 			}
 		}
 	}
 
 	@Override
 	public void postSolve(Contact arg0, ContactImpulse arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void preSolve(Contact arg0, Manifold arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onTouch(MotionEvent ev) {
 		moveHorizantal(ev);
 	}
 
 	private float startX = 0;
 	private Boolean goingLeft = false;
 	private Boolean goingRight = false;
 
 	private void moveHorizantal(MotionEvent ev) {
 		float x = Gdx.graphics.getWidth() - ev.getX();
 		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
 			startX = x;
 		} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
 			float currentMotion = x - startX;
 			if (currentMotion < 0 && !goingRight && !needsStoppedRight()) {
 				leftPad.getBody().setLinearVelocity(10f, 0);
 				goingRight = true;
 				goingLeft = false;
 			} else if (currentMotion == 0) {
 				if (leftPad.getBody().getPosition().x >= x) {
 					leftPadStop();
 				}
 
 			} else if (currentMotion > 0 && !goingDown && !needsStoppedLeft()) {
 				leftPad.getBody().setLinearVelocity(-10f, 0);
 				goingLeft = true;
 				goingRight = false;
 			}
 		}
 	}
 
 	private float startY = 0;
 	private Boolean goingUp = false;
 	private Boolean goingDown = false;
 
 	private void moveVerticle(MotionEvent ev) {
 		float y = Gdx.graphics.getHeight() - ev.getY();
 		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
 			startY = y;
 		} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
 			float currentMotion = y - startY;
 			if (currentMotion > 0 && !goingUp && !needsStoppedUp()) {
 				leftPad.getBody().setLinearVelocity(0, 10000f);
 				goingUp = true;
 				goingDown = false;
 			} else if (currentMotion == 0) {
 
 			} else if (currentMotion < 0 && !goingDown && !needsStoppedDown()) {
 				leftPad.getBody().setLinearVelocity(0, -10000f);
 				goingDown = true;
 				goingUp = false;
 			}
 		}
 	}
 
 	private void leftPadStop() {
 		startY = 0;
 		leftPad.getBody().setLinearVelocity(0, 0f);
 		goingUp = false;
 		goingDown = false;
 	}
 
 	public native String resetScreen();
 	
 	/**
 	 * @return the count
 	 */
 	public int getCount() {
 		return count;
 	}
 
 	/**
 	 * @param count
 	 *            the count to set
 	 */
 	public void setCount(int count) {
 		this.count = count;
 	}
 
 	/**
 	 * @return the world
 	 */
 	public World getWorld() {
 		return world;
 	}
 
 	/**
 	 * @param world
 	 *            the world to set
 	 */
 	public void setWorld(World world) {
 		this.world = world;
 	}
 
 	/**
 	 * @return the gravity
 	 */
 	public Vector2 getGravity() {
 		return gravity;
 	}
 
 	/**
 	 * @param gravity
 	 *            the gravity to set
 	 */
 	public void setGravity(Vector2 gravity) {
 		this.gravity = gravity;
 	}
 
 	/**
 	 * @return the batch
 	 */
 	public SpriteBatch getBatch() {
 		return batch;
 	}
 
 	/**
 	 * @param batch
 	 *            the batch to set
 	 */
 	public void setBatch(SpriteBatch batch) {
 		this.batch = batch;
 	}
 
 	public GameActor getLeftPad() {
 		return leftPad;
 	}
 
 	public void setLeftPad(GameActor leftPad) {
 		this.leftPad = leftPad;
 	}
 
 	public Box2DDebugRenderer getDebugRenderer() {
 		return debugRenderer;
 	}
 
 	public void setDebugRenderer(Box2DDebugRenderer debugRenderer) {
 		this.debugRenderer = debugRenderer;
 	}
 
 	/**
 	 * @return the camera
 	 */
 	public OrthographicCamera getCamera() {
 		return camera;
 	}
 
 	/**
 	 * @param camera
 	 *            the camera to set
 	 */
 	public void setCamera(OrthographicCamera camera) {
 		this.camera = camera;
 	}
 
 	public boolean isAccelerometerPresent() {
 		return accelerometerPresent;
 	}
 
 	public void setAccelerometerPresent(boolean accelerometerPresent) {
 		this.accelerometerPresent = accelerometerPresent;
 	}
 
 	/**
 	 * @return the accelX
 	 */
 	public float getAccelX() {
 		return accelX;
 	}
 
 	/**
 	 * @param accelX
 	 *            the accelX to set
 	 */
 	public void setAccelX(float accelX) {
 		this.accelX = accelX;
 	}
 
 	/**
 	 * @return the accelY
 	 */
 	public float getAccelY() {
 		return accelY;
 	}
 
 	/**
 	 * @param accelY
 	 *            the accelY to set
 	 */
 	public void setAccelY(float accelY) {
 		this.accelY = accelY;
 	}
 
 	/**
 	 * @return the accelZ
 	 */
 	public float getAccelZ() {
 		return accelZ;
 	}
 
 	/**
 	 * @param accelZ
 	 *            the accelZ to set
 	 */
 	public void setAccelZ(float accelZ) {
 		this.accelZ = accelZ;
 	}
 
 	/**
 	 * @return the puck
 	 */
 	public GameActor getPuck() {
 		return puck;
 	}
 
 	/**
 	 * @param puck
 	 *            the puck to set
 	 */
 	public void setPuck(GameActor puck) {
 		this.puck = puck;
 	}
 
 }
