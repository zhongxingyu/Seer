 /*******************************************************************************
  * Copyright (C) 2012 Henrik Voß, Sven Nobis and Nicolas Gramlich (AndEngine)
  * 
  * This file is part of VHackAndroidGame
  * (https://github.com/SvenTo/VHackAndroidGame)
  * 
  * VHackAndroidGame is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This source code is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this source code; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  ******************************************************************************/
 package de.dsi8.vhackandroidgame;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.TiledSprite;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.source.EmptyBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.bitmap.source.decorator.BaseBitmapTextureAtlasSourceDecorator;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 
 import com.google.zxing.BarcodeFormat;
 import com.google.zxing.MultiFormatWriter;
 import com.google.zxing.WriterException;
 import com.google.zxing.common.BitMatrix;
 
 import de.dsi8.dsi8acl.connection.impl.SocketConnection;
 import de.dsi8.dsi8acl.connection.model.ConnectionParameter;
 import de.dsi8.vhackandroidgame.communication.model.GameModeMessage;
 import de.dsi8.vhackandroidgame.communication.model.QRCodeMessage.QRCodePosition;
 import de.dsi8.vhackandroidgame.logic.contract.IPresentationLogic;
 import de.dsi8.vhackandroidgame.logic.contract.IPresentationView;
 import de.dsi8.vhackandroidgame.logic.contract.IServerLogicListener;
 import de.dsi8.vhackandroidgame.logic.impl.PresentationLogic;
 import de.dsi8.vhackandroidgame.logic.impl.VHackAndroidGameConfiguration;
 
 /**
  * (c) 2010 Nicolas Gramlich (c) 2011 Zynga
  * 
  * @author Nicolas Gramlich
  * @since 22:43:20 - 15.07.2010
  */
 public class RacerGameActivity extends AbstractConnectionActivity implements
 		IPresentationView {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	private static final String LOG_TAG = RacerGameActivity.class.getSimpleName();
 
 	private static final int RACETRACK_WIDTH = 64;
 
 	private static final int OBSTACLE_SIZE = 16;
 	public static final int CAR_SIZE = 16;
 
 	private static final int CAMERA_WIDTH = RACETRACK_WIDTH * 5;
 	private static final int CAMERA_HEIGHT = RACETRACK_WIDTH * 3;
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private Camera mCamera;
 
 	private BitmapTextureAtlas mVehiclesTexture;
 	private TiledTextureRegion mVehiclesTextureRegion;
 
 	private BitmapTextureAtlas mBoxTexture;
 	private ITextureRegion mBoxTextureRegion;
 
 	private Scene mScene;
 	
 	private Map<Integer, CarView> cars = new HashMap<Integer, RacerGameActivity.CarView>();
 
 	private IPresentationLogic presentationLogic;
 
 	private BitmapTextureAtlas qrCodeAtlas;
 
 	private TextureRegion qrCodeAtlasRegion;
 	
 	private Rectangle borderTop;
 	private Rectangle borderRight;
 	private Rectangle borderBottom;
 	private Rectangle borderLeft;
 
 	private Sprite barcodeCenter;
 	private Sprite barcodeTop;
 	private Sprite barcodeRight;
 	private Sprite barcodeBottom;
 	private Sprite barcodeLeft;
 	
 	@Override
 	protected void onCreate(Bundle pSavedInstanceState) {
 		super.onCreate(pSavedInstanceState);
		
		this.connectionParameter.setParameter("host", "192.168.11.27");
 	}
 	
 	@Override
 	protected void onConnected(SocketConnection connection) {
 		this.presentationLogic = new PresentationLogic(RacerGameActivity.this, connection);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 
 		if (presentationLogic != null) {
 			try {
 				presentationLogic.close();
 			} catch (IOException e) {
 				Log.w(LOG_TAG, "Can not close the connection the server.", e);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 
 		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
 				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onCreateResources() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		this.mVehiclesTexture = new BitmapTextureAtlas(this.getTextureManager(),
 				128, 16, TextureOptions.BILINEAR);
 		this.mVehiclesTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createTiledFromAsset(this.mVehiclesTexture, this, "vehicles.png", 0,
 						0, 6, 1);
 		this.mVehiclesTexture.load();
 
 		this.mBoxTexture = new BitmapTextureAtlas(this.getTextureManager(), 32, 32,
 				TextureOptions.BILINEAR);
 		this.mBoxTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mBoxTexture, this, "box.png", 0, 0);
 		this.mBoxTexture.load();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Scene onCreateScene() {
 		this.mScene = new Scene();
 		this.mScene.setBackground(new Background(0, 0, 0));
 
 		this.qrCodeAtlas = new BitmapTextureAtlas(this.getTextureManager(), 150,
 				750, TextureOptions.DEFAULT);
 
 		final VertexBufferObjectManager vertexBufferObjectManager = this
 				.getVertexBufferObjectManager();
 		this.borderBottom = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2,
 				vertexBufferObjectManager);
 		this.borderTop = new Rectangle(0, 0, CAMERA_WIDTH, 2,
 				vertexBufferObjectManager);
 		this.borderLeft = new Rectangle(0, 0, 2, CAMERA_HEIGHT,
 				vertexBufferObjectManager);
 		this.borderRight = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT,
 				vertexBufferObjectManager);
 
 		return this.mScene;
 	}
 	
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void addCar(int carId, float pX, float pY, float rotation) {
 		CarView carView = new CarView();
 		carView.id = carId;
 
 		carView.car = new TiledSprite(pX, pY, CAR_SIZE, CAR_SIZE,
 				this.mVehiclesTextureRegion, this.getVertexBufferObjectManager());
 		carView.car.setCurrentTileIndex(carId % 6);
 		carView.car.setRotation(rotation);
 
 
 		this.mScene.attachChild(carView.car);
 
 		this.cars.put(carId, carView);
 	}
 
 	@Override
 	public void removeCar(int carId) {
 		CarView carView = this.cars.remove(carId);
 		if (carView != null) {
 			// this.mPhysicsWorld.unregisterPhysicsConnector(carView.physicsConnector);
 			this.mScene.detachChild(carView.car);
 		}
 	}
 
 	public class CarView {
 		public int id;
 		public TiledSprite car;
 	}
 
 	@Override
 	public void showQRCode(String text, QRCodePosition position) {
 		// TODO: use the given position information.
 		MultiFormatWriter writer = new MultiFormatWriter();
 		try {
 			final BitMatrix bitmatrix = writer.encode(text, BarcodeFormat.QR_CODE,
 					150, 150);
 
 			final IBitmapTextureAtlasSource baseTextureSource = new EmptyBitmapTextureAtlasSource(
 					150, 150);
 			final BaseBitmapTextureAtlasSourceDecorator decoratedTextureAtlasSource = new BaseBitmapTextureAtlasSourceDecorator(
 					baseTextureSource) {
 				@Override
 				protected void onDecorateBitmap(Canvas pCanvas) throws Exception {
 					pCanvas.drawRGB(0, 0, 0);
 					this.mPaint.setColor(Color.WHITE);
 					for (int y = 0; y < bitmatrix.getHeight(); y++) {
 						for (int x = 0; x < bitmatrix.getWidth(); x++) {
 							if (!bitmatrix.get(x, y)) {
 								pCanvas.drawPoint(x, y, mPaint);
 							}
 						}
 					}
 				}
 
 				@Override
 				public BaseBitmapTextureAtlasSourceDecorator deepCopy() {
 					throw new RuntimeException();
 				}
 			};
 			final int bardcodeSize = CAMERA_HEIGHT - 2 * RACETRACK_WIDTH;
 
 			final TextureRegion qrCodeAtlasRegion;
 
 			switch (position) {
 				case CENTER:
 					qrCodeAtlasRegion = BitmapTextureAtlasTextureRegionFactory
 							.createFromSource(this.qrCodeAtlas, decoratedTextureAtlasSource,
 									0, 0);
 					this.qrCodeAtlas.load();
 					this.barcodeCenter = new Sprite(CAMERA_WIDTH / 2 - bardcodeSize / 2,
 							RACETRACK_WIDTH, bardcodeSize, bardcodeSize, qrCodeAtlasRegion,
 							this.getVertexBufferObjectManager());
 					this.mScene.attachChild(this.barcodeCenter);
 					break;
 				case TOP:
 					qrCodeAtlasRegion = BitmapTextureAtlasTextureRegionFactory
 							.createFromSource(this.qrCodeAtlas, decoratedTextureAtlasSource,
 									0, 150);
 					this.qrCodeAtlas.load();
 					this.barcodeTop = new Sprite(CAMERA_WIDTH / 2 - bardcodeSize / 2, 2,
 							bardcodeSize, bardcodeSize, qrCodeAtlasRegion,
 							this.getVertexBufferObjectManager());
 					this.mScene.attachChild(this.barcodeTop);
 					break;
 				case RIGHT:
 					qrCodeAtlasRegion = BitmapTextureAtlasTextureRegionFactory
 							.createFromSource(this.qrCodeAtlas, decoratedTextureAtlasSource,
 									0, 300);
 					this.qrCodeAtlas.load();
 					this.barcodeRight = new Sprite(CAMERA_WIDTH - 2 - bardcodeSize,
 							RACETRACK_WIDTH, bardcodeSize, bardcodeSize, qrCodeAtlasRegion,
 							this.getVertexBufferObjectManager());
 					this.mScene.attachChild(this.barcodeRight);
 					break;
 				case BOTTOM:
 					qrCodeAtlasRegion = BitmapTextureAtlasTextureRegionFactory
 							.createFromSource(this.qrCodeAtlas, decoratedTextureAtlasSource,
 									0, 450);
 					this.qrCodeAtlas.load();
 					this.barcodeBottom = new Sprite(CAMERA_WIDTH / 2 - bardcodeSize / 2,
 							CAMERA_HEIGHT - 2 - bardcodeSize, bardcodeSize, bardcodeSize,
 							qrCodeAtlasRegion, this.getVertexBufferObjectManager());
 					this.mScene.attachChild(this.barcodeBottom);
 					break;
 				case LEFT:
 					qrCodeAtlasRegion = BitmapTextureAtlasTextureRegionFactory
 							.createFromSource(this.qrCodeAtlas, decoratedTextureAtlasSource,
 									0, 600);
 					this.qrCodeAtlas.load();
 					this.barcodeLeft = new Sprite(2, RACETRACK_WIDTH, bardcodeSize,
 							bardcodeSize, qrCodeAtlasRegion,
 							this.getVertexBufferObjectManager());
 					this.mScene.attachChild(this.barcodeLeft);
 					break;
 				default:
 					// impossible
 					break;
 
 			}
 		} catch (WriterException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void updateBorders(boolean top, boolean right, boolean bottom,
 			boolean left) {
 		if (top) {
 			this.mScene.attachChild(this.borderTop);
 		} else {
 			this.mScene.detachChild(this.borderTop);
 		}
 		if (right) {
 			this.mScene.attachChild(this.borderRight);
 		} else {
 			this.mScene.detachChild(this.borderRight);
 		}
 		if (bottom) {
 			this.mScene.attachChild(this.borderBottom);
 		} else {
 			this.mScene.detachChild(this.borderBottom);
 		}
 		if (left) {
 			this.mScene.attachChild(this.borderLeft);
 		} else {
 			this.mScene.detachChild(this.borderLeft);
 		}
 	}
 
 
 	@Override
 	public void moveCar(int carId, float pX, float pY) {
 		CarView carView = this.cars.get(carId);
 		if (carView != null && carView.car != null) {
 			carView.car.setPosition(pX, pY);
 		}
 	}
 
 	@Override
 	public void rotateCar(int carId, float rotation) {
 		CarView carView = this.cars.get(carId);
 		if (carView != null && carView.car != null) {
 			carView.car.setRotation(rotation);
 		}
 	}
 }
