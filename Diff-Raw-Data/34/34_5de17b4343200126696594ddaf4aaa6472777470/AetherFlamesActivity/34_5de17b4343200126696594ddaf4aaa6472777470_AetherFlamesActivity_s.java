 package com.jjaz.aetherflames;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
 import org.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
 import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
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
 import org.andengine.extension.multiplayer.protocol.util.WifiUtils;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.HorizontalAlign;
 import org.andengine.util.debug.Debug;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.bluetooth.BluetoothAdapter;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.opengl.GLES20;
 import android.os.Bundle;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.jjaz.aetherflames.messages.server.ConnectionCloseServerMessage;
 import com.jjaz.aetherflames.physics.DistributedFixedStepPhysicsWorld;
 
 public class AetherFlamesActivity extends SimpleBaseGameActivity implements AetherFlamesConstants
 {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	private static final String LOCALHOST_IP = "127.0.0.1";
 	
 	protected static final int CAMERA_WIDTH = 960;
 	protected static final int CAMERA_HEIGHT = 540;
 	
 	protected static final float WORLD_WIDTH = 30.0f;
 	protected static final float WORLD_HEIGHT = 16.875f;//WORLD_WIDTH*(CAMERA_HEIGHT/CAMERA_WIDTH);
 
 	protected static final float WORLD_TO_CAMERA = 32;
 	protected static final float CAMERA_TO_WORLD = 0.03125f;
 
 	protected static final float SHIP_START_PADDING = 1.25f;
 	protected static final float HEALTH_CRATE_START_PADDING = 3f;
 	
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	protected static Camera mCamera;
 	protected static Scene mScene;
 	protected static Engine mGameEngine;
 	protected static DistributedFixedStepPhysicsWorld mPhysicsWorld;
 	protected static VertexBufferObjectManager mVertexBufferObjectManager;
 	protected static Font mFont;
 
 	protected static BitmapTextureAtlas mSpaceTexture;
 	protected static ITextureRegion mSpaceTextureRegion;
 
 	protected static BitmapTextureAtlas mShipTextures;
 	protected static TiledTextureRegion mShipTextureRegion;
 
 	protected static BitmapTextureAtlas mHealthCrateTexture;
 	protected static ITextureRegion mHealthCrateTextureRegion;
 
 	protected static BitmapTextureAtlas mPlasmaBlastTexture; 
 	protected static ITextureRegion mPlasmaBlastTextureRegion; //TODO: change this to an animated sprite
 
 	protected static BitmapTextureAtlas mControlStickTexture;
 	protected static ITextureRegion mControlStickBaseTextureRegion;
 	protected static ITextureRegion mControlStickKnobTextureRegion;
 	
 	protected static BitmapTextureAtlas mButtonsTexture;
 	protected static ITextureRegion mButtonsBaseTextureRegion;
 	protected static ITextureRegion mButtonsKnobTextureRegion;
 	
 	private CollisionHandler mCollisionHandler;
 	private SceneUpdateHandler mSceneUpdateHandler;
 	
 	private AnalogOnScreenControl mControlStick;
 	private DigitalOnScreenControl mButtons;
 	
 	public static Map<Integer, Ship> ships;
 	
 	private static final int DIALOG_CHOOSE_SERVER_OR_CLIENT_ID = 0;
 	private static final int DIALOG_ENTER_SERVER_IP_ID = DIALOG_CHOOSE_SERVER_OR_CLIENT_ID + 1;
 	private static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_ENTER_SERVER_IP_ID + 1;
 	private static final int DIALOG_GAME_OVER = DIALOG_SHOW_SERVER_IP_ID + 1;
 	
	private int mID;
 	private String mServerIP = LOCALHOST_IP;
 	private SocketServer<SocketConnectionClientConnector> mSocketServer;
 	private ServerConnector<SocketConnection> mServerConnector;
 	private ClientGameManager mClientGameManager;
 
 	private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
 
	
 	public static int myShipColor;
 	
 	@Override
 	public EngineOptions onCreateEngineOptions()
 	{
 		AetherFlamesActivity.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), AetherFlamesActivity.mCamera);
 		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
 
		this.mID = 0;
		this.mClientGameManager = new ClientGameManager(this.mID);
 
 		this.showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
 		
 		return engineOptions;
 	}
 
 	@Override
 	public void onCreateResources()
 	{
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		AetherFlamesActivity.mSpaceTexture = new BitmapTextureAtlas(this.getTextureManager(), 960*2, 540, TextureOptions.BILINEAR);
 		AetherFlamesActivity.mSpaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(AetherFlamesActivity.mSpaceTexture, this, "spaceBackground.png", 0, 0);
 		AetherFlamesActivity.mSpaceTexture.load();
 		
 		AetherFlamesActivity.mShipTextures = new BitmapTextureAtlas(this.getTextureManager(), 512, 64, TextureOptions.BILINEAR);
 		AetherFlamesActivity.mShipTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(AetherFlamesActivity.mShipTextures, this, "ships.png", 0, 0, 8, 1);
 		AetherFlamesActivity.mShipTextures.load();
 		
 		AetherFlamesActivity.mControlStickTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
 		AetherFlamesActivity.mControlStickBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(AetherFlamesActivity.mControlStickTexture, this, "onscreen_control_base.png", 0, 0);
 		AetherFlamesActivity.mControlStickKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(AetherFlamesActivity.mControlStickTexture, this, "onscreen_control_knob.png", 128, 0);
 		AetherFlamesActivity.mControlStickTexture.load();
 		
 		AetherFlamesActivity.mButtonsTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
 		AetherFlamesActivity.mButtonsBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(AetherFlamesActivity.mButtonsTexture, this, "Buttons.png", 0, 0);
 		AetherFlamesActivity.mButtonsKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(AetherFlamesActivity.mButtonsTexture, this, "Empty.png", 128, 0);
 		AetherFlamesActivity.mButtonsTexture.load();
 
 		AetherFlamesActivity.mHealthCrateTexture = new BitmapTextureAtlas(this.getTextureManager(), 128, 128, TextureOptions.BILINEAR);
 		AetherFlamesActivity.mHealthCrateTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(AetherFlamesActivity.mHealthCrateTexture, this, "healthCrate.png", 0, 0);
 		AetherFlamesActivity.mHealthCrateTexture.load();
 		
 		AetherFlamesActivity.mPlasmaBlastTexture = new BitmapTextureAtlas(this.getTextureManager(), 64, 64, TextureOptions.BILINEAR);
 		AetherFlamesActivity.mPlasmaBlastTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(AetherFlamesActivity.mPlasmaBlastTexture, this, "Bullet.png", 0, 0);
 		AetherFlamesActivity.mPlasmaBlastTexture.load();
 
 		AetherFlamesActivity.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, 30, true, Color.WHITE);
 		AetherFlamesActivity.mFont.load();
 	}
 
 	@Override
 	public Scene onCreateScene()
 	{
 		AetherFlamesActivity.mGameEngine = this.mEngine;
 		AetherFlamesActivity.mGameEngine.registerUpdateHandler(new FPSLogger());
 
 		AetherFlamesActivity.mScene = new Scene();
 		AetherFlamesActivity.mScene.setBackground(new Background(0, 0, 0));
 		
 		AetherFlamesActivity.mVertexBufferObjectManager = this.getVertexBufferObjectManager();
 		AetherFlamesActivity.mPhysicsWorld = new DistributedFixedStepPhysicsWorld(30, new Vector2(0, 0), false, 8, 1, this.mClientGameManager);
 
 		this.mCollisionHandler = new CollisionHandler();
 		this.mSceneUpdateHandler = new SceneUpdateHandler();
 		AetherFlamesActivity.mPhysicsWorld.setContactListener(mCollisionHandler);
 		
 		this.mClientGameManager.setPhysicsWorld(AetherFlamesActivity.mPhysicsWorld);
 		
 		ships = new ConcurrentHashMap<Integer,Ship>();
 		
 		this.initBattlefield();
 		this.initShips();
 		this.initHealthCrates();
 		this.initOnScreenControls();
 		
 		AetherFlamesActivity.mScene.registerUpdateHandler(this.mSceneUpdateHandler);
 		
 		return AetherFlamesActivity.mScene;
 	}
 
 	@Override
 	public void onGameCreated()
 	{
 		
 	}
 	
 	protected void onCreate(final Bundle pSavedInstanceState) {
 		super.onCreate(pSavedInstanceState);
 		
 	}
 	
 	protected Dialog onCreateDialog(final int pID) {
 		switch(pID) {
 			case DIALOG_SHOW_SERVER_IP_ID:
 				try {
 					return new AlertDialog.Builder(this)
 					.setIcon(android.R.drawable.ic_dialog_info)
 					.setTitle("Your Server-IP ...")
 					.setCancelable(false)
 					.setMessage("The IP of your Server is:\n" + WifiUtils.getWifiIPv4Address(this))
 					.setPositiveButton(android.R.string.ok, null)
 					.create();
 				} catch (final UnknownHostException e) {
 					return new AlertDialog.Builder(this)
 					.setIcon(android.R.drawable.ic_dialog_alert)
 					.setTitle("Your Server-IP ...")
 					.setCancelable(false)
 					.setMessage("Error retrieving IP of your Server: " + e)
 					.setPositiveButton(android.R.string.ok, new OnClickListener() {
 						@Override
 						public void onClick(final DialogInterface pDialog, final int pWhich) {
 							AetherFlamesActivity.this.finish();
 						}
 					})
 					.create();
 				}
 			case DIALOG_ENTER_SERVER_IP_ID:
 				final EditText ipEditText = new EditText(this);
 				return new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_info)
 				.setTitle("Enter Server-IP ...")
 				.setCancelable(false)
 				.setView(ipEditText)
 				.setPositiveButton("Connect", new OnClickListener() {
 					@Override
 					public void onClick(final DialogInterface pDialog, final int pWhich) {
 						AetherFlamesActivity.this.mServerIP = ipEditText.getText().toString();
 						AetherFlamesActivity.this.initClient();
 					}
 				})
 				.setNegativeButton(android.R.string.cancel, new OnClickListener() {
 					@Override
 					public void onClick(final DialogInterface pDialog, final int pWhich) {
 						AetherFlamesActivity.this.finish();
 					}
 				})
 				.create();
 			case DIALOG_CHOOSE_SERVER_OR_CLIENT_ID:
 				return new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_info)
 				.setTitle("Be Server or Client ...")
 				.setCancelable(false)
 				.setPositiveButton("Client", new OnClickListener() {
 					@Override
 					public void onClick(final DialogInterface pDialog, final int pWhich) {
 						AetherFlamesActivity.this.showDialog(DIALOG_ENTER_SERVER_IP_ID);
 					}
 				})
 				.setNeutralButton("Server", new OnClickListener() {
 					@Override
 					public void onClick(final DialogInterface pDialog, final int pWhich) {
 						AetherFlamesActivity.this.toast("You can add and move sprites, which are only shown on the clients.");
 						AetherFlamesActivity.this.initServer();
 						AetherFlamesActivity.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
 					}
 				})
 				.setNegativeButton("Both", new OnClickListener() {
 					@Override
 					public void onClick(final DialogInterface pDialog, final int pWhich) {
 						AetherFlamesActivity.this.toast("You can add sprites and move them, by dragging them.");
 						AetherFlamesActivity.this.initServerAndClient();
 						AetherFlamesActivity.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
 					}
 				})
 				.create();
 			case DIALOG_GAME_OVER:
 				return new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_info)
 				.setTitle("Player " + pID + " Wins!")
 				.setCancelable(false)
 				.setPositiveButton("Ok", new OnClickListener() {
 					@Override
 					public void onClick(final DialogInterface pDialog, final int pWhich) {
 						//final Intent intent = new Intent(AetherFlamesActivity.this, BluetoothListDevicesActivity.class);
 						//AetherFlamesActivity.this.startActivityForResult(intent, REQUESTCODE_BLUETOOTH_CONNECT);
 					}
 				}).create();
 			default:
 				return super.onCreateDialog(pID);
 					/*.setNeutralButton("Server", new OnClickListener() {
 						@Override
 						public void onClick(final DialogInterface pDialog, final int pWhich) {
 							//AetherFlamesActivity.this.toast("You can add and move sprites, which are only shown on the clients.");
 							//AetherFlamesActivity.this.initServer();
 							//AetherFlamesActivity.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
 						}
 					})
 					.setNegativeButton("Both", new OnClickListener() {
 						@Override
 						public void onClick(final DialogInterface pDialog, final int pWhich) {
 							//AetherFlamesActivity.this.toast("You can add sprites and move them, by dragging them.");
 							//AetherFlamesActivity.this.initServerAndClient();
 							//AetherFlamesActivity.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
 						}
 					})*/
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		if(this.mSocketServer != null) {
 			try {
 				this.mSocketServer.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
 			} catch (final IOException e) {
 				Debug.e(e);
 			}
 			this.mSocketServer.terminate();
 		}
 
 		if(this.mServerConnector != null) {
 			this.mServerConnector.terminate();
 		}
 
 		super.onDestroy();
 	}
 	
 	private void initServerAndClient() {
 		this.initServer();
 
 		/* Wait some time after the server has been started, so it actually can start up. */
 		try {
 			Thread.sleep(500);
 		} catch (final Throwable t) {
 			Debug.e(t);
 		}
 
 		this.initClient();
 	}
 
 	private void initServer() {
 		this.mSocketServer = new AetherFlamesServer(new ClientConnectorListener()) {
 			@Override
 			protected SocketConnectionClientConnector newClientConnector(final SocketConnection pSocketConnection) throws IOException {
 				return new SocketConnectionClientConnector(pSocketConnection);
 			}
 		};
 
 		this.mSocketServer.start();
 	}
 
 	private void initClient() {
 		try {
 			this.mServerConnector = new SocketConnectionServerConnector(new SocketConnection(new Socket(this.mServerIP, SERVER_PORT)), new ServerConnectorListener());
 			this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					AetherFlamesActivity.this.finish();
 				}
 			});
 			
 			this.mClientGameManager.setServerConnector(this.mServerConnector);
			this.mClientGameManager.setMessagePool(this.mMessagePool);
 
 			this.mServerConnector.getConnection().start();
 		} catch (final Throwable t) {
 			Debug.e(t);
 		}
 	}
 
 	private void log(final String pMessage) {
 		Debug.d(pMessage);
 	}
 
 	private void toast(final String pMessage) {
 		this.log(pMessage);
 		this.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				Toast.makeText(AetherFlamesActivity.this, pMessage, Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 	
 	private void initOnScreenControls()
 	{		
 		this.mControlStick = new AnalogOnScreenControl(0, CAMERA_HEIGHT - AetherFlamesActivity.mControlStickBaseTextureRegion.getHeight(), AetherFlamesActivity.mCamera, AetherFlamesActivity.mControlStickBaseTextureRegion, AetherFlamesActivity.mControlStickKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new ShipMovementControlListener(ships.get(AetherFlamesActivity.myShipColor), this.mClientGameManager));
 		this.mControlStick.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		this.mControlStick.getControlBase().setAlpha(0.5f);
 		this.mControlStick.getControlBase().setScaleCenter(0, 128);
 		this.mControlStick.getControlBase().setScale(1.5f);
 		this.mControlStick.getControlKnob().setScale(1.5f);
 		this.mControlStick.refreshControlKnobPosition();
 
 		AetherFlamesActivity.mScene.setChildScene(this.mControlStick);
 		
 		this.mButtons = new DigitalOnScreenControl(CAMERA_WIDTH - AetherFlamesActivity.mButtonsBaseTextureRegion.getWidth(), CAMERA_HEIGHT - AetherFlamesActivity.mButtonsBaseTextureRegion.getHeight(), AetherFlamesActivity.mCamera, AetherFlamesActivity.mButtonsBaseTextureRegion, AetherFlamesActivity.mButtonsKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new ButtonControlListener(ships.get(AetherFlamesActivity.myShipColor)));
 		this.mButtons.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		this.mButtons.getControlBase().setAlpha(0.5f);
 		this.mButtons.getControlBase().setScaleCenter(128, 128);
 		this.mButtons.getControlBase().setScale(1.5f);
 		this.mButtons.getControlKnob().setScale(1.5f);
 		this.mButtons.refreshControlKnobPosition();
 
 		this.mControlStick.setChildScene(this.mButtons);
 	}
 
 	private void initShips()
 	{		
 		AetherFlamesActivity.myShipColor = Ship.BLUE_SHIP; //TODO: set this on game configuration using network
 
 		ships.put(Ship.WHITE_SHIP, new Ship(SHIP_START_PADDING, SHIP_START_PADDING, -45.0f, Ship.WHITE_SHIP, mClientGameManager));
 		ships.put(Ship.RED_SHIP, new Ship(WORLD_WIDTH/2, SHIP_START_PADDING, 0.0f, Ship.RED_SHIP, mClientGameManager));
 		ships.put(Ship.ORANGE_SHIP, new Ship(WORLD_WIDTH - SHIP_START_PADDING, SHIP_START_PADDING, 45.0f, Ship.ORANGE_SHIP, mClientGameManager));
 		
 		ships.put(Ship.YELLOW_SHIP, new Ship(SHIP_START_PADDING, WORLD_HEIGHT/2, -90.0f, Ship.YELLOW_SHIP, mClientGameManager));
 		ships.put(Ship.GREEN_SHIP, new Ship(WORLD_WIDTH - SHIP_START_PADDING, WORLD_HEIGHT/2, 90.0f, Ship.GREEN_SHIP, mClientGameManager));
 		
 		ships.put(Ship.BLUE_SHIP, new Ship(SHIP_START_PADDING, WORLD_HEIGHT - SHIP_START_PADDING, -135.0f, Ship.BLUE_SHIP, mClientGameManager));
 		ships.put(Ship.PURPLE_SHIP, new Ship(WORLD_WIDTH/2, WORLD_HEIGHT - SHIP_START_PADDING, 180.0f, Ship.PURPLE_SHIP, mClientGameManager));
 		ships.put(Ship.BLACK_SHIP, new Ship(WORLD_WIDTH - SHIP_START_PADDING, WORLD_HEIGHT - SHIP_START_PADDING, 135.0f, Ship.BLACK_SHIP, mClientGameManager));
 	}
 
 	private void initHealthCrates()
 	{
 		HealthCrate.spawn(WORLD_WIDTH/2 + HEALTH_CRATE_START_PADDING, WORLD_HEIGHT/2);
 		HealthCrate.spawn(WORLD_WIDTH/2 - HEALTH_CRATE_START_PADDING, WORLD_HEIGHT/2);
 		HealthCrate.spawn(WORLD_WIDTH/2, WORLD_HEIGHT/2 + HEALTH_CRATE_START_PADDING);
 		HealthCrate.spawn(WORLD_WIDTH/2, WORLD_HEIGHT/2 - HEALTH_CRATE_START_PADDING);
 	}
 
 	private void initBattlefield()
 	{
 		//space background
 		final Sprite background = new Sprite(0, 0, 960, 540, AetherFlamesActivity.mSpaceTextureRegion, this.getVertexBufferObjectManager());
 		AetherFlamesActivity.mScene.attachChild(background);
 
 		//battlefield boundaries
 		final Rectangle bottomOuter = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, mVertexBufferObjectManager);
 		final Rectangle topOuter = new Rectangle(0, 0, CAMERA_WIDTH, 2, mVertexBufferObjectManager);
 		final Rectangle leftOuter = new Rectangle(0, 0, 2, CAMERA_HEIGHT, mVertexBufferObjectManager);
 		final Rectangle rightOuter = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, mVertexBufferObjectManager);
 		
 		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
 		PhysicsFactory.createBoxBody(AetherFlamesActivity.mPhysicsWorld, bottomOuter, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(AetherFlamesActivity.mPhysicsWorld, topOuter, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(AetherFlamesActivity.mPhysicsWorld, leftOuter, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(AetherFlamesActivity.mPhysicsWorld, rightOuter, BodyType.StaticBody, wallFixtureDef);
 	}
 	
 	private class ServerConnectorListener implements ISocketConnectionServerConnectorListener {
 		@Override
 		public void onStarted(final ServerConnector<SocketConnection> pConnector) {
 			AetherFlamesActivity.this.toast("CLIENT: Connected to server.");
 		}
 
 		@Override
 		public void onTerminated(final ServerConnector<SocketConnection> pConnector) {
 			AetherFlamesActivity.this.toast("CLIENT: Disconnected from Server...");
 			AetherFlamesActivity.this.finish();
 		}
 	}
 
 	private class ClientConnectorListener implements ISocketConnectionClientConnectorListener {
 		@Override
 		public void onStarted(final ClientConnector<SocketConnection> pConnector) {
 			AetherFlamesActivity.this.toast("SERVER: Client connected: " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
 		}
 
 		@Override
 		public void onTerminated(final ClientConnector<SocketConnection> pConnector) {
 			AetherFlamesActivity.this.toast("SERVER: Client disconnected: " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
 		}
 	}
 }
