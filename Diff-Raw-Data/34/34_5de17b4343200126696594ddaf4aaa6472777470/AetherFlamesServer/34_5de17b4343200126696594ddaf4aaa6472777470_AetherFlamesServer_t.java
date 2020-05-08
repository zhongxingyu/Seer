 package com.jjaz.aetherflames;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.andengine.engine.handler.IUpdateHandler;
 import com.jjaz.aetherflames.AetherFlamesConstants;
 import com.jjaz.aetherflames.messages.client.ConnectionCloseClientMessage;
 import com.jjaz.aetherflames.messages.client.ConnectionEstablishClientMessage;
 import com.jjaz.aetherflames.messages.client.NewBulletClientMessage;
 import com.jjaz.aetherflames.messages.client.ShipUpdateClientMessage;
 import com.jjaz.aetherflames.messages.client.CollisionClientMessage;
 import com.jjaz.aetherflames.messages.client.DoneClientMessage;
 import com.jjaz.aetherflames.messages.server.CollisionServerMessage;
 import com.jjaz.aetherflames.messages.server.GameEndServerMessage;
 import com.jjaz.aetherflames.messages.server.GameStartServerMessage;
 import com.jjaz.aetherflames.messages.server.NewBulletServerMessage;
 import com.jjaz.aetherflames.messages.server.ShipUpdateServerMessage;
 import com.jjaz.aetherflames.messages.server.DoneServerMessage;
 import com.jjaz.aetherflames.messages.server.ConnectionEstablishedServerMessage;
 import com.jjaz.aetherflames.messages.server.ConnectionRejectedProtocolMissmatchServerMessage;
 import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
 import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
 import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;
 import org.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
 import org.andengine.extension.multiplayer.protocol.server.SocketServer;
 import org.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener.DefaultSocketServerListener;
 import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
 import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
 import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
 import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
 import org.andengine.extension.multiplayer.protocol.util.MessagePool;
 import org.andengine.util.debug.Debug;
 
 public class AetherFlamesServer extends
 		SocketServer<SocketConnectionClientConnector> implements
 		AetherFlamesConstants, IUpdateHandler {
 
 
 	private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
 	
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	public AetherFlamesServer(final ISocketConnectionClientConnectorListener pSocketConnectionClientConnectorListener) {
 		super(SERVER_PORT, pSocketConnectionClientConnectorListener, new DefaultSocketServerListener<SocketConnectionClientConnector>());
 
 		this.initMessagePool();
 		
 		numDone = 0;
 
 	}		
 
 	private void initMessagePool() {
 		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_CONNECTION_ESTABLISHED, ShipUpdateServerMessage.class);
 		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ShipUpdateServerMessage.class);
 		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_NEW_BULLET, NewBulletServerMessage.class);
 		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_SHIP_UPDATE, ShipUpdateServerMessage.class);
 		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_COLLISION, CollisionServerMessage.class);
 		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_GAME_START, GameStartServerMessage.class);
 		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_GAME_END, GameEndServerMessage.class);
 		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_DONE, DoneServerMessage.class);
 	}
 
 	@Override
 	public void onUpdate(float pSecondsElapsed) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void reset() {
 		// TODO Auto-generated method stub
 
 	}
 	
 	LinkedList<ServerMessage> messages;
 	int numDone; // needs to reach number of players
	final int NUM_PLAYERS = 1;
 	
 	@Override
 	protected SocketConnectionClientConnector newClientConnector(final SocketConnection pSocketConnection) throws IOException {
 		final SocketConnectionClientConnector clientConnector = new SocketConnectionClientConnector(pSocketConnection);
 
 		clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_NEW_BULLET, NewBulletClientMessage.class, new IClientMessageHandler<SocketConnection>() {
 			@Override
 			public void onHandleMessage(final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage) throws IOException {
 				final NewBulletClientMessage newBulletClientMessage = (NewBulletClientMessage)pClientMessage;
 				final NewBulletServerMessage newBulletServerMessage = new NewBulletServerMessage();
 				newBulletServerMessage.setNewBullet(newBulletClientMessage.mShipID, newBulletClientMessage.mBulletID, newBulletClientMessage.mBulletType,
 													newBulletClientMessage.mVectorX, newBulletClientMessage.mVectorY,
 													newBulletClientMessage.mPosX, newBulletClientMessage.mPosY, newBulletClientMessage.mAngle);
 				messages.addLast(newBulletServerMessage);
 			}
 		});
 
 		clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_SHIP_UPDATE, ShipUpdateClientMessage.class, new IClientMessageHandler<SocketConnection>() {
 			@Override
 			public void onHandleMessage(final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage) throws IOException {
 				final ShipUpdateClientMessage shipUpdateClientMessage = (ShipUpdateClientMessage)pClientMessage;
 				final ShipUpdateServerMessage shipUpdateServerMessage = new ShipUpdateServerMessage();
 				shipUpdateServerMessage.setShipUpdate(shipUpdateClientMessage.mShipID, shipUpdateClientMessage.mHealth,
 													shipUpdateClientMessage.mOrientation, shipUpdateClientMessage.mAngularVelocity,
 													shipUpdateClientMessage.mVectorX, shipUpdateClientMessage.mVectorY,
 													shipUpdateClientMessage.mPosX, shipUpdateClientMessage.mPosY);
 				messages.addLast(shipUpdateServerMessage);
 			}
 		});
 
 		clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_COLLISION, CollisionClientMessage.class, new IClientMessageHandler<SocketConnection>() {
 			@Override
 			public void onHandleMessage(final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage) throws IOException {
 				final CollisionClientMessage collisionClientMessage = (CollisionClientMessage)pClientMessage;
 				final CollisionServerMessage collisionServerMessage = new CollisionServerMessage();
 				collisionServerMessage.setCollision(collisionClientMessage.mShipID, collisionClientMessage.mBulletID);
 				messages.addLast(collisionServerMessage);
 			}
 		});
 		
 		clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_CONNECTION_CLOSE, ConnectionCloseClientMessage.class, new IClientMessageHandler<SocketConnection>() {
 			@Override
 			public void onHandleMessage(final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage) throws IOException {
 				pClientConnector.terminate();
 			}
 		});
 
 		clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_CONNECTION_ESTABLISH, ConnectionEstablishClientMessage.class, new IClientMessageHandler<SocketConnection>() {
 			@Override
 			public void onHandleMessage(final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage) throws IOException {
 				final ConnectionEstablishClientMessage connectionEstablishClientMessage = (ConnectionEstablishClientMessage) pClientMessage;
 				if(connectionEstablishClientMessage.getProtocolVersion() == PROTOCOL_VERSION) {
 					final ConnectionEstablishedServerMessage connectionEstablishedServerMessage = (ConnectionEstablishedServerMessage) AetherFlamesServer.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_CONNECTION_ESTABLISHED);
 					try {
 						pClientConnector.sendServerMessage(connectionEstablishedServerMessage);
 					} catch (IOException e) {
 						Debug.e(e);
 					}
 					AetherFlamesServer.this.mMessagePool.recycleMessage(connectionEstablishedServerMessage);
 				} else {
 					final ConnectionRejectedProtocolMissmatchServerMessage connectionRejectedProtocolMissmatchServerMessage = (ConnectionRejectedProtocolMissmatchServerMessage) AetherFlamesServer.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_CONNECTION_REJECTED_PROTOCOL_MISSMATCH);
 					connectionRejectedProtocolMissmatchServerMessage.setProtocolVersion(PROTOCOL_VERSION);
 					try {
 						pClientConnector.sendServerMessage(connectionRejectedProtocolMissmatchServerMessage);
 					} catch (IOException e) {
 						Debug.e(e);
 					}
 					AetherFlamesServer.this.mMessagePool.recycleMessage(connectionRejectedProtocolMissmatchServerMessage);
 				}
 			}
 		});
 
 		clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_DONE, DoneClientMessage.class, new IClientMessageHandler<SocketConnection>() {
 			@Override
 			public void onHandleMessage(final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage) throws IOException {
 				// can use this to send out all the client messages
 				numDone += 1;
 				if (numDone == NUM_PLAYERS) {
 					messages.addLast(new DoneServerMessage(true));
 					try {
 						Iterator<ServerMessage> iterator = messages.iterator();
 						while (iterator.hasNext()) {
 							ServerMessage message = iterator.next();
 							AetherFlamesServer.this.sendBroadcastServerMessage(message);
 							AetherFlamesServer.this.mMessagePool.recycleMessage(message);
 						}
 					} catch (IOException e) {
 						Debug.e(e);
 					}
 					messages.clear();
 					numDone = 0; // reset
 				}
 			}
 		});
 
 		return clientConnector;
 	}
 
 }
