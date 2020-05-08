 package com.ralibi.dodombaan;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.LimitedFPSEngine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.WakeLockOptions;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
 import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
 import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;
 import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
 import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
 import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector;
 import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
 import org.andengine.extension.multiplayer.protocol.server.SocketServer;
 import org.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener;
 import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
 import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
 import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
 import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
 import org.andengine.extension.multiplayer.protocol.util.MessagePool;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.ui.activity.BaseGameActivity;
 import org.andengine.util.debug.Debug;
 
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 import com.ralibi.dodombaan.manager.ResourcesManager;
 import com.ralibi.dodombaan.manager.SceneManager;
 import com.ralibi.dodombaan.multiplayer.ConnectionCloseServerMessage;
 
 public class MainActivity extends BaseGameActivity implements IOnSceneTouchListener {
 
 	//---------------------------------------------
     // VARIABLES
     //---------------------------------------------
 	
 
 //	private static final String EXAMPLE_UUID = "6c7067e0-88aa-11e2-9e96-0800200c9a66";
 //	private static final String LOCALHOST_IP = "127.0.0.1";
 //	private static final int SERVER_PORT = 4444;
 //	
 //	public static final short FLAG_MESSAGE_SERVER_MOVE_SHEEP = 1;
 //	
 //	
 //
 //	public static final short FLAG_MESSAGE_CLIENT_CONNECTION_CLOSE = Short.MIN_VALUE;
 //	public static final short FLAG_MESSAGE_CLIENT_CONNECTION_ESTABLISH = FLAG_MESSAGE_CLIENT_CONNECTION_CLOSE + 1;
 //	public static final short FLAG_MESSAGE_CLIENT_CONNECTION_PING = FLAG_MESSAGE_CLIENT_CONNECTION_ESTABLISH + 1;
 //	
 //	public static final short FLAG_MESSAGE_SERVER_CONNECTION_CLOSE = Short.MIN_VALUE;
 //	public static final short FLAG_MESSAGE_SERVER_CONNECTION_ESTABLISHED = FLAG_MESSAGE_SERVER_CONNECTION_CLOSE + 1;
 //	public static final short FLAG_MESSAGE_SERVER_CONNECTION_REJECTED_PROTOCOL_MISSMATCH = FLAG_MESSAGE_SERVER_CONNECTION_ESTABLISHED + 1;
 //	public static final short FLAG_MESSAGE_SERVER_CONNECTION_PONG = FLAG_MESSAGE_SERVER_CONNECTION_REJECTED_PROTOCOL_MISSMATCH + 1;
 //	
 //	
 //	
 //	public String mServerIP = LOCALHOST_IP;
 //	public SocketServer<SocketConnectionClientConnector> mSocketServer;
 //	public ServerConnector<SocketConnection> mServerConnector;
 //
 //	public final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
 //	
 	
 	
 	
 	private Camera camera;
 	// private ResourcesManager resourcesManager;
 	
 
 	private void initMessagePool() {
 //		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_MOVE_SHEEP, MoveSheepServerMessage.class);
 	}
 	
 	@Override
 	public Engine onCreateEngine(EngineOptions pEngineOptions) 
 	{
 	    return new LimitedFPSEngine(pEngineOptions, 60);
 	}
 	
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		camera = new Camera(0, 0, 800, 480);
 	    EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(800, 480), this.camera);
 	    engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
 	    engineOptions.getTouchOptions().setNeedsMultiTouch(true);
 	    engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
 	    
 	    // MainActivity.this.initServerAndClient();
 	    
 	    return engineOptions;
 	}
 
 	@Override
 	public void onCreateResources(
 			OnCreateResourcesCallback pOnCreateResourcesCallback)
 			throws IOException {
 		
 		ResourcesManager.prepareManager(mEngine, this, camera, getVertexBufferObjectManager());
 	    // resourcesManager = ResourcesManager.getInstance();
 	    pOnCreateResourcesCallback.onCreateResourcesFinished();
 	    
 	}
 
 	@Override
 	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
 			throws IOException {
 		SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);
 	}
 
 	@Override
 	public void onPopulateScene(Scene pScene,
 			OnPopulateSceneCallback pOnPopulateSceneCallback)
 			throws IOException {
 		mEngine.registerUpdateHandler(new TimerHandler(1f, new ITimerCallback() 
 	    {
 	            public void onTimePassed(final TimerHandler pTimerHandler) 
 	            {
 	                mEngine.unregisterUpdateHandler(pTimerHandler);
 	                SceneManager.getInstance().createMenuScene();
 	                // set menu scene using scene manager
 	                // disposeSplashScene();
 	                // READ NEXT ARTICLE FOR THIS PART.
 	                	                
 	                
 	            }
 	    }));
 	    pOnPopulateSceneCallback.onPopulateSceneFinished();
 	}
 
 	@Override
 	protected void onDestroy()
 	{
 //		if(this.mSocketServer != null) {
 //			try {
 //				this.mSocketServer.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
 //			} catch (final IOException e) {
 //				Debug.e(e);
 //			}
 //			this.mSocketServer.terminate();
 //		}
 //
 //		if(this.mServerConnector != null) {
 //			this.mServerConnector.terminate();
 //		}
 
 		super.onDestroy();
		if (this.isGameLoaded())
		{
			System.exit(0);	
		}	
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) 
 	{  
 	    if (keyCode == KeyEvent.KEYCODE_BACK)
 	    {
 	        SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
 	    }
 	    return false; 
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		switch(pSceneTouchEvent.getAction()) {
 		case TouchEvent.ACTION_DOWN:
 			Debug.d("Activity DOWN");
 			return true;
 		case TouchEvent.ACTION_MOVE:
 			SceneManager.getInstance().getCurrentScene().unTouchScrollMenu();
 			return true;
 		case TouchEvent.ACTION_UP:
 			SceneManager.getInstance().getCurrentScene().unTouchScrollMenu();
 			return true;
 		}
 		return false;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 //	
 //
 //	private void initServerAndClient() {
 ////		this.initServer();
 //
 //		/* Wait some time after the server has been started, so it actually can start up. */
 //		try {
 //			Thread.sleep(500);
 //		} catch (final Throwable t) {
 //			Debug.e(t);
 //		}
 //
 ////		this.initClient();
 //	}
 //
 ////	private void initServer() {
 ////		this.mSocketServer = new SocketServer<SocketConnectionClientConnector>(SERVER_PORT, new ExampleClientConnectorListener(), new ExampleServerStateListener()) {
 ////			@Override
 ////			protected SocketConnectionClientConnector newClientConnector(final SocketConnection pSocketConnection) throws IOException {
 ////				return new SocketConnectionClientConnector(pSocketConnection);
 ////			}
 ////		};
 ////
 ////		this.mSocketServer.start();
 ////	}
 ////
 ////	private void initClient() {
 ////		try {
 ////			this.mServerConnector = new SocketConnectionServerConnector(new SocketConnection(new Socket(this.mServerIP, SERVER_PORT)), new ExampleServerConnectorListener());
 ////
 ////			this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<SocketConnection>() {
 ////				@Override
 ////				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 ////					MainActivity.this.finish();
 ////				}
 ////			});
 ////
 ////			this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_MOVE_SHEEP, MoveSheepServerMessage.class, new IServerMessageHandler<SocketConnection>() {
 ////				@Override
 ////				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 ////					final MoveSheepServerMessage moveSheepServerMessage = (MoveSheepServerMessage)pServerMessage;
 ////					MainActivity.this.addFace(moveSheepServerMessage.mID, moveSheepServerMessage.mX, moveSheepServerMessage.mY);
 ////				}
 ////			});
 ////
 ////
 ////			this.mServerConnector.getConnection().start();
 ////		} catch (final Throwable t) {
 ////			Debug.e(t);
 ////		}
 ////	}
 ////	
 //
 //
 ////	private void log(final String pMessage) {
 ////		Debug.d(pMessage);
 ////	}
 ////
 ////	private void toast(final String pMessage) {
 ////		this.log(pMessage);
 ////		this.runOnUiThread(new Runnable() {
 ////			@Override
 ////			public void run() {
 ////				Toast.makeText(MainActivity.this, pMessage, Toast.LENGTH_SHORT).show();
 ////			}
 ////		});
 ////	}
 //
 //	
 //	
 //	
 //
 //	// ===========================================================
 //	// Inner and Anonymous Classes
 //	// ===========================================================
 ////
 ////	public static class MoveSheepServerMessage extends ServerMessage {
 ////		private int mID;
 ////		private float mX;
 ////		private float mY;
 ////
 ////		public MoveSheepServerMessage() {
 ////
 ////		}
 ////
 ////		public MoveSheepServerMessage(final int pID, final float pX, final float pY) {
 ////			this.mID = pID;
 ////			this.mX = pX;
 ////			this.mY = pY;
 ////		}
 ////
 ////		public void set(final int pID, final float pX, final float pY) {
 ////			this.mID = pID;
 ////			this.mX = pX;
 ////			this.mY = pY;
 ////		}
 ////
 ////		@Override
 ////		public short getFlag() {
 ////			return FLAG_MESSAGE_SERVER_MOVE_SHEEP;
 ////		}
 ////
 ////		@Override
 ////		protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
 ////			this.mID = pDataInputStream.readInt();
 ////			this.mX = pDataInputStream.readFloat();
 ////			this.mY = pDataInputStream.readFloat();
 ////		}
 ////
 ////		@Override
 ////		protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
 ////			pDataOutputStream.writeInt(this.mID);
 ////			pDataOutputStream.writeFloat(this.mX);
 ////			pDataOutputStream.writeFloat(this.mY);
 ////		}
 ////	}
 ////	
 ////	private class ExampleServerConnectorListener implements ISocketConnectionServerConnectorListener {
 ////		@Override
 ////		public void onStarted(final ServerConnector<SocketConnection> pConnector) {
 ////			MainActivity.this.toast("CLIENT: Connected to server.");
 ////		}
 ////
 ////		@Override
 ////		public void onTerminated(final ServerConnector<SocketConnection> pConnector) {
 ////			MainActivity.this.toast("CLIENT: Disconnected from Server...");
 ////			MainActivity.this.finish();
 ////		}
 ////	}
 ////
 ////	private class ExampleServerStateListener implements ISocketServerListener<SocketConnectionClientConnector> {
 ////		@Override
 ////		public void onStarted(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
 ////			MainActivity.this.toast("SERVER: Started.");
 ////		}
 ////
 ////		@Override
 ////		public void onTerminated(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
 ////			MainActivity.this.toast("SERVER: Terminated.");
 ////		}
 ////
 ////		@Override
 ////		public void onException(final SocketServer<SocketConnectionClientConnector> pSocketServer, final Throwable pThrowable) {
 ////			Debug.e(pThrowable);
 ////			MainActivity.this.toast("SERVER: Exception: " + pThrowable);
 ////		}
 ////	}
 ////	
 ////	private class ExampleClientConnectorListener implements ISocketConnectionClientConnectorListener {
 ////		@Override
 ////		public void onStarted(final ClientConnector<SocketConnection> pConnector) {
 ////			MainActivity.this.toast("SERVER: Client connected: " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
 ////		}
 ////
 ////		@Override
 ////		public void onTerminated(final ClientConnector<SocketConnection> pConnector) {
 ////			MainActivity.this.toast("SERVER: Client disconnected: " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
 ////		}
 ////	}
 }
