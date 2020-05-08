 package com.jjaz.aetherflames;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
 import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;
 import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
 import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
 import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
 import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
 import org.andengine.extension.multiplayer.protocol.util.MessagePool;
 import org.andengine.util.debug.Debug;
 
 import com.badlogic.gdx.math.Vector2;
 
 import com.jjaz.aetherflames.messages.client.CollisionClientMessage;
 import com.jjaz.aetherflames.messages.client.DoneClientMessage;
 import com.jjaz.aetherflames.messages.client.ShipUpdateClientMessage;
 import com.jjaz.aetherflames.messages.client.NewBulletClientMessage;
 import com.jjaz.aetherflames.messages.server.CollisionServerMessage;
 import com.jjaz.aetherflames.messages.server.DoneServerMessage;
 import com.jjaz.aetherflames.messages.server.GameEndServerMessage;
 import com.jjaz.aetherflames.messages.server.GameStartServerMessage;
 import com.jjaz.aetherflames.messages.server.NewBulletServerMessage;
 import com.jjaz.aetherflames.messages.server.ShipUpdateServerMessage;
 import com.jjaz.aetherflames.physics.DistributedFixedStepPhysicsWorld;
 
 public class ClientGameManager implements AetherFlamesConstants {
 
 	public static final float FIELD_EMPTY = -1;
 	
 	// Update queue
 	private ArrayList<ClientMessage> updateQueue;
 
 	// Game Data
 	private DistributedFixedStepPhysicsWorld physicsWorld;
 	private Map<Integer, Ship> ships;
 	private int nextBulletID;
 	private int myID;
 	private int frameCount;
 
 	// Networking variables
 	private ServerConnector<SocketConnection> serverConnector;
 	private MessagePool<IMessage> messagePool;
 
 	// Constructors
 	/**
 	 * Default constructor
 	 */
 	public ClientGameManager() {
 		this.updateQueue = new ArrayList<ClientMessage>();
 		this.nextBulletID = myID * 1000000; // makes sure there are enough IDs
 		this.frameCount = 0;
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param id This player's game ID.
 	 */
 	public ClientGameManager(int id) {
 		this();
 		this.myID = id;
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param w The game's distributed physics world.
 	 * @param id This player's game ID.
 	 * @param m A hash table of the ships involved in the game.
 	 * @param connector Connection to the game server.
 	 * @param pool Message pool
 	 */
	public ClientGameManager(DistributedFixedStepPhysicsWorld w, int id, Map<Integer, Ship> m, ServerConnector<SocketConnection> connector, MessagePool<IMessage> pool) {
 		this();
 		this.physicsWorld = w;
 		this.myID = id;
 		this.ships = m;
 		this.serverConnector = connector;
		this.messagePool = pool;
 		
		initMessagePool();
 		registerMessageHandlers();
 	}
 
 	// Methods
 	/**
 	 * Initializes the message pool
 	 */
 	private void initMessagePool() {
 		this.messagePool.registerMessage(FLAG_MESSAGE_CLIENT_CONNECTION_ESTABLISH, ShipUpdateClientMessage.class);
 		this.messagePool.registerMessage(FLAG_MESSAGE_CLIENT_CONNECTION_CLOSE, ShipUpdateClientMessage.class);
 		this.messagePool.registerMessage(FLAG_MESSAGE_CLIENT_NEW_BULLET, NewBulletClientMessage.class);
 		this.messagePool.registerMessage(FLAG_MESSAGE_CLIENT_SHIP_UPDATE, ShipUpdateClientMessage.class);
 		this.messagePool.registerMessage(FLAG_MESSAGE_CLIENT_COLLISION, CollisionClientMessage.class);
 		this.messagePool.registerMessage(FLAG_MESSAGE_CLIENT_DONE, DoneClientMessage.class);
 	}
 	
 	/**
 	 * Registers all gameplay message types to be handled by the designated methods in the ClientGameManager class
 	 */
 	private void registerMessageHandlers() {
 		try {
 			this.serverConnector.registerServerMessage(FLAG_MESSAGE_SERVER_SHIP_UPDATE, ShipUpdateServerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					final ShipUpdateServerMessage shipUpdateMessage = (ShipUpdateServerMessage)pServerMessage;
 					ClientGameManager.this.handleShipUpdateMessage(shipUpdateMessage);
 				}
 			});
 
 			this.serverConnector.registerServerMessage(FLAG_MESSAGE_SERVER_NEW_BULLET, NewBulletServerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					final NewBulletServerMessage newBulletMessage = (NewBulletServerMessage)pServerMessage;
 					ClientGameManager.this.handleNewBulletMessage(newBulletMessage);
 				}
 			});
 			
 			this.serverConnector.registerServerMessage(FLAG_MESSAGE_SERVER_COLLISION, CollisionServerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					final CollisionServerMessage collisionMessage = (CollisionServerMessage)pServerMessage;
 					ClientGameManager.this.handleCollisionMessage(collisionMessage);
 				}
 			});
 			
 			this.serverConnector.registerServerMessage(FLAG_MESSAGE_SERVER_DONE, DoneServerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					ClientGameManager.this.nextFrame();
 				}
 			});
 
 		} catch (final Throwable t) {
 			Debug.e(t);
 		}
 	}
 	
 	/**
 	 * Creates a new set of players
 	 * 
 	 * @param numPlayers NUmber of players in the game
 	 */
 	public void startGame(int numPlayers) {
 
 	}
 
 	/**
 	 * Queues a force application message for the local player's ship
 	 * 
 	 * @param force The force to be applied
 	 */
 	public synchronized void queueApplyForceEvent(Vector2 force) {
 		ShipUpdateClientMessage message = (ShipUpdateClientMessage)this.messagePool.obtainMessage(FLAG_MESSAGE_CLIENT_SHIP_UPDATE);
 		Ship myShip = this.ships.get(myID);
 
 		// queue the new message
 		message.setShipUpdate(this.myID, myShip.getHealth(), FIELD_EMPTY, FIELD_EMPTY, force.x, force.y, FIELD_EMPTY, FIELD_EMPTY);
 		this.updateQueue.add(message);
 	}
 
 	/**
 	 * Queues a change-of-angular-velocity message for the local player's ship
 	 * 
 	 * @param omega The new angular velocity
 	 */
 	public synchronized void setAngularVelocity(float angularVelocity) {
 		ShipUpdateClientMessage message = (ShipUpdateClientMessage)this.messagePool.obtainMessage(FLAG_MESSAGE_CLIENT_SHIP_UPDATE);
 		Ship myShip = this.ships.get(myID);
 
 		// queue the new message
 		message.setShipUpdate(this.myID, myShip.getHealth(), FIELD_EMPTY, angularVelocity, FIELD_EMPTY, FIELD_EMPTY, FIELD_EMPTY, FIELD_EMPTY);
 		this.updateQueue.add(message);
 	}
 	
 	public synchronized void queueTurnInstantAndThrustEvent(Vector2 direction) {
 		ShipUpdateClientMessage message = (ShipUpdateClientMessage)this.messagePool.obtainMessage(FLAG_MESSAGE_CLIENT_SHIP_UPDATE);
 		Ship myShip = this.ships.get(myID);
 		
 		float angle = (float) Math.atan2(-direction.x, direction.y);
 
 		// queue the new message
 		message.setShipUpdate(this.myID, myShip.getHealth(), angle, FIELD_EMPTY, direction.x, direction.y, FIELD_EMPTY, FIELD_EMPTY);
 		this.updateQueue.add(message);
 	}
 
 	/**
 	 * Queues a create bullet message
 	 * 
 	 * @param name The name of the bullet type
 	 * @param velocity The velocity of the bullet
 	 * @param center The starting coordinate of the bullet
 	 */
 	public synchronized void queueNewBulletEvent(int type, Vector2 velocity, Vector2 center, float angle) {
 		NewBulletClientMessage message = (NewBulletClientMessage)messagePool.obtainMessage(FLAG_MESSAGE_CLIENT_NEW_BULLET);
 		int bulletID = nextBulletID;
 		nextBulletID++;
 		
 		message.setNewBullet(myID, bulletID, type, velocity.x, velocity.y, center.x, center.y, angle);
 		this.updateQueue.add(message);
 	}
 
 	/**
 	 * Sends all updates since the last frame to the game server
 	 */
 	public synchronized void sendUpdates() {
 		try {
 			for (ClientMessage message : this.updateQueue) {
 				this.serverConnector.sendClientMessage(message);
 				this.messagePool.recycleMessage(message);
 			}
 			DoneClientMessage doneMessage = (DoneClientMessage)this.messagePool.obtainMessage(FLAG_MESSAGE_CLIENT_DONE);
 			this.serverConnector.sendClientMessage(doneMessage);
 			this.messagePool.recycleMessage(doneMessage);
 		} catch (IOException e) {
 			Debug.e(e);
 		}
 	}
 
 	/**
 	 * Handles a ship update message from the server
 	 * 
 	 * @param message The message to handle
 	 */
 	public void handleShipUpdateMessage(ShipUpdateServerMessage message) {
 		int id = message.mShipID;
 		Ship ship = this.ships.get(id);
 
 		// get the message parameters
 		float angle = message.mOrientation;
 		float angularVelocity = message.mAngularVelocity;
 		Vector2 force = new Vector2(message.mVectorX, message.mVectorY);
 		Vector2 point = new Vector2(message.mPosX, message.mPosY);
 
 		if (angularVelocity != FIELD_EMPTY) {
 			ship.turn(angularVelocity);
 		}
 		if (force.x != FIELD_EMPTY && force.y != FIELD_EMPTY) {
 			if (angle == (float)Math.atan2(-force.x, force.y)) {
 				ship.turnInstantAndThrust(force);
 			} else {
 				ship.fireThrusters((float)Math.sqrt(force.x * force.x + force.y * force.y));
 			}
 		} 
 	}
 
 	/**
 	 * Handles a new bullet message from the server
 	 * 
 	 * @param message The message to handle
 	 */
 	public void handleNewBulletMessage(NewBulletServerMessage message) {
 		int id = message.mShipID;
 		Ship ship = this.ships.get(id);
 		ArrayList<ProjectileWeapon> weapons = ship.getAvailableWeapons();
 		ProjectileWeapon weapon;
 		int type = message.mBulletType;
 		Vector2 center = new Vector2(message.mPosX, message.mPosY);
 		Vector2 velocity = new Vector2(message.mVectorX, message.mVectorY);
 		float angle = message.mAngle;
 		
 		// find the desired weapon
 		int i = 0;
 		while (i < weapons.size() && weapons.get(i).type != type) {
 			i++;
 		}
 		
 		// fire the weapon if it is legitimate
 		if(i < weapons.size()) {
 			weapon = weapons.get(i);
 			weapon.fire(center, velocity, angle);
 		}	
 	}
 
 	/**
 	 * Handles a new collision message from the server
 	 * 
 	 * @param message The message to handle
 	 */
 	public void handleCollisionMessage(CollisionServerMessage message) {
 		// TODO: HANDLE THESE MESSAGES IF NECESSARY
 		/*int shipID = message.mShipID;
 		int bulletID = message.mBulletID;
 
 		int newHealth = this.shipHealths.get(shipID) - 10;
 		this.shipHealths.set(shipID, newHealth);
 
 		// destroy the ship if its dead
 		if (newHealth <= 0) {
 			this.physicsWorld.destroyBody(this.ships.get(shipID));
 		}
 
 		// remove the bullet from the world
 		this.physicsWorld.destroyBody(this.bullets.get(bulletID));*/
 	}
 
 	/**
 	 * Makes all changes from the previous frame and sets up the next frame
 	 */
 	public void nextFrame() {
 		this.physicsWorld.step();
 		this.frameCount++;
 	}
 
 	// Setters
 	public void setPhysicsWorld(DistributedFixedStepPhysicsWorld w) {
 		this.physicsWorld = w;
 	}
 
 	public void setID(int id) {
 		this.myID = id;
 	}
 
 	public void setShips(Map<Integer, Ship> m) {
 		this.ships = m;
 	}
 	
 	public void setServerConnector(ServerConnector<SocketConnection> connector) {
 		this.serverConnector = connector;
 		registerMessageHandlers();
 	}
 
 	public void setMessagePool(MessagePool<IMessage> pool) {
 		this.messagePool = pool;
 		initMessagePool();
 	}
 
 	// Getters
 	public DistributedFixedStepPhysicsWorld gePhysicsWorld() {
 		return this.physicsWorld;
 	}
 
 	public int getID() {
 		return this.myID;
 	}
 
 	public int getFrameCount() {
 		return this.frameCount;
 	}
 }
