 package com.app.augmentedbizz.ui.renderer;
 
 import java.io.IOException;
 import java.util.List;
 
 import com.app.augmentedbizz.R;
 import com.app.augmentedbizz.application.data.IndicatorDataListener;
 import com.app.augmentedbizz.application.data.ModelDataListener;
 import com.app.augmentedbizz.application.status.ApplicationState;
 import com.app.augmentedbizz.application.status.ApplicationStateListener;
 import com.app.augmentedbizz.logging.DebugLog;
 import com.app.augmentedbizz.services.entity.transfer.IndicatorServiceEntity.TargetIndicator;
 import com.app.augmentedbizz.ui.MainActivity;
 import com.app.augmentedbizz.ui.glview.AugmentedGLSurfaceView;
 import com.app.augmentedbizz.ui.scanner.ScannerResultListener;
 import com.app.augmentedbizz.util.Display;
 import com.app.augmentedbizz.util.TypeConversion;
 
 /**
  * Manager for graphical rendering.
  * 
  * @author Vladi
  *
  */
 public class RenderManager implements IndicatorDataListener, ModelDataListener, ScannerResultListener, ApplicationStateListener {
 	public static int depthSize = 16;
 	public static int stencilSize = 0;
 	private MainActivity mainActivity;
 	private boolean initialized = false;
 	
 	public RenderManager(MainActivity mainActivity) {
 		this.mainActivity = mainActivity;
 		this.mainActivity.getAugmentedBizzApplication().getApplicationStateManager().addApplicationStateListener(this);
 		this.mainActivity.getAugmentedBizzApplication().getDataManager().addModelDataListener(this);
 		this.mainActivity.getAugmentedBizzApplication().getDataManager().addIndicatorDataListener(this);
 	}
 	
 	/**
 	 * Initializes the render manager by setting up the GL surface view 
 	 * and passing necessary data to the native side.
 	 * 
 	 * @return true, if the initialization was successful
 	 */
 	public boolean initialize() {
 		if(getGlSurfaceView() == null) {
 			return false;
 		}
 
 		if(!initialized) {
 			getGlSurfaceView().setup(true, 
 									 depthSize, 
 									 stencilSize, 
 									 RenderManager.this.mainActivity.getAugmentedBizzApplication());
 			synchronized(this) {
 				//initialize the native components synchronized in the GL thread as we otherwise get context errors
 				getGlSurfaceView().queueEvent(new Runnable() {
 					@Override
 					public void run() {
 						initializeNative((short)Display.getScreenWidth(mainActivity), (short)Display.getScreenHeight(mainActivity));
 						synchronized(RenderManager.this) {
 							RenderManager.this.notify();
 						}
 					}
 				});
 				try {
 					wait();
 				}
 				catch (InterruptedException e) {
 				}
 			}
 			initialized = true;
 		}
 
 		return true;
 	}
 	
 	/**
 	 * @return The GL surface view of the main screen.
 	 */
 	public AugmentedGLSurfaceView getGlSurfaceView() {
 		return (AugmentedGLSurfaceView)mainActivity.findViewById(R.id.augmentedGLSurfaceView);
 	}
 	
 	/**
 	 * @return The renderer for the 3D augmentation
 	 */
 	public AugmentedRenderer getRenderer() {
 		return getGlSurfaceView().getRenderer();
 	}
 	
 	/**
 	 * Initialize on the native side.
 	 * 
 	 * @param width of the screen
 	 * @param height of the screen
 	 */
 	private native void initializeNative(short width, short height);
 	private native void setIndicatorTexture(Texture texture);
 	
 	/**
 	 * Native method for trackable size retrieval
 	 */
 	private native int getTrackableWidth();
 	private native int getTrackableHeight();
 	
 	/** 
 	 * Native methods for starting and stoping the camera. 
 	 */ 
     public native void startCamera();
     public native void stopCamera();
     
     private void callScanner(final int width, final int height, final byte[] bitmapData) {
     	DebugLog.logi("Received data for scanner. Bytes: " + bitmapData.length + ", width: " + width + ", height: " + height);
 		RenderManager.this.mainActivity.getAugmentedBizzApplication().getApplicationStateManager()
 			.setApplicationState(ApplicationState.SCANNING);
 		RenderManager.this.mainActivity.getAugmentedBizzApplication().getUIManager()
     		.getQRScanner().scanForQRCode(width, height, bitmapData, RenderManager.this);
 
     }
     		
 	@Override
 	public void onModelData(final OpenGLModel openGLModel, boolean retrievingNewerVersion) {
 		if(this.mainActivity.getAugmentedBizzApplication()
 			.getApplicationStateManager().
 			getApplicationState().equals(ApplicationState.LOADING)) {
 			getGlSurfaceView().queueEvent(new Runnable() {
 				@Override
 				public void run() {
 					//set the new model data
 					processAndSetScaleFactor(openGLModel);
 					setTexture(openGLModel.getTexture());
 					setModel(openGLModel.getVertices(),
 							openGLModel.getNormals(),
 							openGLModel.getTextureCoordinates(),
 							openGLModel.getIndices());
 				}
 			});
 			
 			if(retrievingNewerVersion) {
 				this.mainActivity.getAugmentedBizzApplication()
 					.getApplicationStateManager().
 					setApplicationState(ApplicationState.SHOWING_CACHE);
 			}
 		}
 	}
 	
 	/**
 	 * Native methods for setting the model data
 	 */
 	private native void setScaleFactor(float scaleFactor);
 	private native void setModel(float[] vertices, float[] normals, float [] texcoords, short[] indices);
 	private native void setTexture(Texture texture);
 	private native void setIndicators(float[] indicators);
 
 	@Override
 	public void onModelError(Exception e) {
 		DebugLog.loge("Unable to load model data", e);
 	}
 
 	@Override
 	public void onApplicationStateChange(ApplicationState lastState, ApplicationState nextState) {
 		if(nextState.equals(ApplicationState.INITIALIZED) && lastState.equals(ApplicationState.INITIALIZING)) {
 			DebugLog.logi("Starting camera.");
 			startCamera();
 			this.mainActivity.getAugmentedBizzApplication().getApplicationStateManager().setApplicationState(ApplicationState.TRACKING);
 		} else if(nextState.equals(ApplicationState.DEINITIALIZING)) {
 			DebugLog.logi("Stopping camera.");
 			stopCamera();
 		}
 	}
 
 	@Override
 	public void onScanningSuccess(int targetId) {
 		//change the state to SCANNED only if the SCANNING state wasn't lost in the meantime
 		ApplicationState currentState = this.mainActivity.getAugmentedBizzApplication()
 				.getApplicationStateManager().getApplicationState();
 		if(currentState.equals(ApplicationState.SCANNING)) {
 			this.mainActivity.getAugmentedBizzApplication()
 				.getApplicationStateManager()
 				.setApplicationState(ApplicationState.SCANNED);
 			this.mainActivity.getAugmentedBizzApplication().getDataManager().loadTarget(targetId);
 		}
 	}
 
 	@Override
 	public void onScanningResultless() {
 		DebugLog.logd("Scanning resultless. Returning to tracking state.");
 		this.mainActivity.getAugmentedBizzApplication().getApplicationStateManager()
 			.setApplicationState(ApplicationState.TRACKED);
 	}
 
 	@Override
 	public void onScanningFailed() {
 		DebugLog.loge("Scanning failed. Returning to tracking state.");
 		this.mainActivity.getAugmentedBizzApplication().getApplicationStateManager()
 			.setApplicationState(ApplicationState.TRACKED);
 	}
 
 	@Override
 	public void onIndicatorData(List<TargetIndicator> targetIndicators) {
 		if(this.mainActivity.getAugmentedBizzApplication()
 			.getApplicationStateManager().
 			getApplicationState().equals(ApplicationState.LOADING_INDICATORS)) {
 			
 			//setup the indicators in the native side
 			processAndSetIndicators(targetIndicators);
 			
 			this.mainActivity.getAugmentedBizzApplication()
 				.getApplicationStateManager().
 				setApplicationState(ApplicationState.SHOWING);
 		}
 	}
 
 	@Override
 	public void onIndicatorError(Exception e) {
 		DebugLog.loge("Unable to load indicator data", e);
 	}
 	
 	private void processAndSetScaleFactor(final OpenGLModel openGLModel) {
 		int trackableWidth = getTrackableWidth();
 		int trackableHeight = getTrackableHeight();
 		float bboxXLength = openGLModel.getXAxisBoundingLength();
 		float bboxYLength = openGLModel.getYAxisBoundingLength();
 		float bboxZLength = openGLModel.getZAxisBoundingLength();
 		
 		float scaleX = (float)trackableWidth / bboxXLength;
 		float scaleY = (float)trackableHeight / bboxYLength;
 		float scaleZ = (float)trackableHeight / bboxZLength;
 		
 		float scale = Math.min(scaleX, Math.min(scaleY, scaleZ)) * 0.75f;
 		
 		setScaleFactor(scale);
 	}
 	
 	private void processAndSetIndicators(final List<TargetIndicator> targetIndicators) {
 	
 		getGlSurfaceView().queueEvent(new Runnable() {
 			@Override
 			public void run() {
 				if(targetIndicators.size() == 0) {
 					setIndicators(new float[0]);
 				} else {
 					try {
 					RenderManager.this.setIndicatorTexture(TypeConversion.toTextureFrom(
 							RenderManager.this.mainActivity.getAssets().open("indicator.png")));
 					} catch (IOException e) {
 						DebugLog.loge("Unable to open indicator.png.");
 					}
 					float[] indicators = new float[targetIndicators.size() * 3];
 					for(int i = 0; i < targetIndicators.size(); ++i) {
 					TargetIndicator indicator = targetIndicators.get(i);
 						indicators[3*i] = indicator.getPositionX();
						indicators[3*i + 1] = indicator.getPositionZ();
						indicators[3*i + 2] = indicator.getPositionY();
 					}
 					
 					RenderManager.this.setIndicators(indicators);
 				}
 			}
 		});
 	}
 }
