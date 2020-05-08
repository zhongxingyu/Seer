 package se.chalmers.segway.game;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 
 import se.chalmers.segway.entities.Player;
 import se.chalmers.segway.scenes.GameScene;
 import se.chalmers.segway.scenes.SceneManager;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.Manifold;
 
 public class PlayerContact implements ContactListener {
 
 	private Player player;
 	private Engine engine;
 
 	/**
 	 * @return the player
 	 */
 	public Player getPlayer() {
 		return player;
 	}
 
 	/**
 	 * @param player
 	 *            the player to set
 	 */
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 
 	/**
 	 * @return the engine
 	 */
 	public Engine getEngine() {
 		return engine;
 	}
 
 	/**
 	 * @param engine
 	 *            the engine to set
 	 */
 	public void setEngine(Engine engine) {
 		this.engine = engine;
 	}
 
 	@Override
 	public void beginContact(Contact contact) {
 		final Fixture x1 = contact.getFixtureA();
 		final Fixture x2 = contact.getFixtureB();
 
 		if (x1.getBody().getUserData() != null
 				&& x2.getBody().getUserData() != null) {
 			if (((String) x2.getBody().getUserData()).contains("platform")
 					|| ((String) x2.getBody().getUserData())
 							.contains("Platform")) {
 				player.setContact(true);
 			}
 		}
 
 		if (x1.getBody().getUserData().equals("player")
 				&& x2.getBody().getUserData().equals("platform3")) {
 			x2.getBody().setType(BodyType.DynamicBody);
 		}
 
 		if (x1.getBody().getUserData().equals("player")
 				&& x2.getBody().getUserData().equals("platform2")) {
 			engine.registerUpdateHandler(new TimerHandler(0.2f,
 					new ITimerCallback() {
 						public void onTimePassed(
 								final TimerHandler pTimerHandler) {
 							pTimerHandler.reset();
 							engine.unregisterUpdateHandler(pTimerHandler);
 							x2.getBody().setType(BodyType.DynamicBody);
 						}
 					}));
 		}
 
 		if (x1.getBody().getUserData().equals("player")
 				&& ((String)x2.getBody().getUserData()).contains("zone")) {
 			GameScene gs = (GameScene) SceneManager.getInstance().getCurrentScene();
 			if(x2.getBody().getUserData().equals("zone_up")){
 				gs.getPhysicsWorld().setGravity(new Vector2(0, 17));
 			} else if (x2.getBody().getUserData().equals("zone_down")){
 				gs.getPhysicsWorld().setGravity(new Vector2(0, -34));
 			} else if (x2.getBody().getUserData().equals("zone_left")){
				gs.getPhysicsWorld().setGravity(new Vector2(-17, -17));
 			} else if (x2.getBody().getUserData().equals("zone_right")){
				gs.getPhysicsWorld().setGravity(new Vector2(17, -17));
 			}
 		}
 
 	}
 
 	@Override
 	public void endContact(Contact contact) {
 		final Fixture x1 = contact.getFixtureA();
 		final Fixture x2 = contact.getFixtureB();
 
 		if (x1.getBody().getUserData() != null
 				&& x2.getBody().getUserData() != null) {
 			if (x1.getBody().getUserData().equals("player")
 					&& ((String)x2.getBody().getUserData()).contains("zone")) {
 				GameScene gs = (GameScene) SceneManager.getInstance().getCurrentScene();
 				gs.getPhysicsWorld().setGravity(new Vector2(0, -17));
 			}
 			if (!x2.getBody().getUserData().equals("player")) {
 				player.setContact(false);
 			}
 		}
 
 	}
 
 	@Override
 	public void preSolve(Contact contact, Manifold oldManifold) {
 
 	}
 
 	@Override
 	public void postSolve(Contact contact, ContactImpulse impulse) {
 
 	}
 
 }
