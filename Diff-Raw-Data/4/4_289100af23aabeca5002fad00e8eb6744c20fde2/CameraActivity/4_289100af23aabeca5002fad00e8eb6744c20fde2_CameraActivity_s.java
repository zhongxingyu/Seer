 package com.baixing.activity;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build.VERSION;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.util.Pair;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.baixing.broadcast.CommonIntentAction;
 import com.baixing.data.GlobalDataManager;
 import com.baixing.entity.BXLocation;
 import com.baixing.entity.BXThumbnail;
 import com.baixing.tracking.TrackConfig;
 import com.baixing.tracking.Tracker;
 import com.baixing.util.BitmapUtils;
 import com.baixing.util.PerformEvent.Event;
 import com.baixing.util.PerformanceTracker;
 import com.baixing.util.Util;
 import com.baixing.util.ViewUtil;
 import com.baixing.util.hardware.AccelerometerSensorDetector;
 import com.baixing.util.hardware.OrientationSensorDetector;
 import com.baixing.util.hardware.RotationDetector;
 import com.baixing.util.hardware.RotationDetector.Orien;
 import com.baixing.util.post.ImageUploader;
 import com.baixing.view.CameraPreview;
 import com.quanleimu.activity.R;
 /**
  * 
  * @author liuchong
  *
  */
 public class CameraActivity extends Activity  implements OnClickListener, SensorEventListener {
 	public static final String TAG = "CAMPREV";
 	
 	private static final int REQ_PICK_GALLERY = 1;
 	
 	private static final int MAX_IMG_COUNT = 6;
 	private static final int MIN_PICTURE_TAKEN_GAP = 500;//user should wait at least 0.5 seconds before taking another picture. 
 	
 	private SensorManager sensorMgr;
 	private Sensor sensor;
 	private RotationDetector detector = new RotationDetector() {
 		
 		@Override
 		public Orien updateSensorEvent(SensorEvent sensorEvent) {
 			return Orien.DEFAULT;
 		}
 	};
 	
 	private CameraPreview mPreview;
     Camera mCamera;
     private boolean isInitialized = false;
     boolean isFrontCam; //If current camera is facing or front camera.
     boolean isLandscapeMode;
     private long lastClickTime = 0;
     private boolean isCameraLock = false;
     
 //    int cameraCurrentlyLocked;
     
     Orien currentOrien = Orien.DEFAULT;
     
     private Vector<BXThumbnail> originalList = new Vector<BXThumbnail>();
     private Vector<BXThumbnail> deleteList = new Vector<BXThumbnail>();
     private Vector<BXThumbnail> imageList = new Vector<BXThumbnail>();
 //    private List<UploadingCallback> callbacks = new ArrayList<UploadingCallback>();
 //    private ArrayList<String> imagePaths = new ArrayList<String>();
     
     private OnDeleteListener deleteListener;
     
     /*
      * Internal message. 
      */
     private static final int MSG_PIC_TAKEN = 0;
     private static final int MSG_SAVE_DONE = 1;
     private static final int MSG_ORIENTATION_CHANGE = 2;
     private static final int MSG_UPDATE_THUMBNAILS = 3;
     private static final int MSG_UPLOADING_STATUS_UPDATE = 4;
     private static final int MSG_PAUSE_ME = 6;
     private static final int MSG_INIT_CAME = 7;
     private static final int MSG_CANCEL_STORE_PIC = 8;
     private static final int MSG_RESUME_ME = 9;
     private static final int MSG_TAKEPIC_DELAY = 10;
     
 
     /*
      * Internal message parameters: image uploading status.
      */
     private static final int STATE_UPLOADING = 1;
     private static final int STATE_FAIL = 2;
     private static final int STATE_DONE = 3;
     
     private Handler handler;
     
     private void updateCapState() {
     	boolean enable = imageList.size() < MAX_IMG_COUNT; 
 		findViewById(R.id.cap).setEnabled(enable && isInitialized);
 		findViewById(R.id.pick_gallery).setEnabled(enable);
     }
     
     class InternalHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch(msg.what) {
 			case MSG_TAKEPIC_DELAY:
 			{
 				BooleanWrapper bW = (BooleanWrapper) msg.obj;
 				if (bW.isTrue) {
 					return;
 				}
 				
 				Camera cam = mCamera;
 				if (cam != null && !isCameraLock) {
 					cam.cancelAutoFocus();
 					cam.takePicture(null, null, mPicture);
 				}
 				break;
 			}
 			case MSG_PAUSE_ME:
 			{
 				isInitialized = false;
 				sensorMgr.unregisterListener(CameraActivity.this);
 				ViewGroup cameraP = (ViewGroup) findViewById(R.id.camera_parent).findViewById(R.id.camera_root);
 				if (mPreview != null) {
 					cameraP.removeView(mPreview);
 				}
 				
 				if (mCamera != null) {
 		            mPreview.setCamera(null);
 		            mCamera.release();
 		            mCamera = null;
 		        }
 				break;
 			}
 			case MSG_CANCEL_STORE_PIC: {
 				updateCapState();
 				Camera cam = mCamera;
 				if (isInitialized && cam != null) {
 					cam.startPreview();
 				}
 				break;
 			}
 			case MSG_INIT_CAME : {
 				PerformanceTracker.stamp(Event.E_Start_Init_Camera);
 				ViewGroup cameraP = (ViewGroup) findViewById(R.id.camera_parent).findViewById(R.id.camera_root);
 				if (mPreview != null) {
 					cameraP.removeView(mPreview);
 				}
 
 				initializeCamera();
 				
 				TextView txt = (TextView) findViewById(R.id.cam_not_available_tip);
 				txt.setVisibility(mCamera == null ? View.VISIBLE : View.GONE);
 				if (mCamera != null) {
 					mPreview = new CameraPreview(CameraActivity.this);
 					mPreview.setRotateDisplay(!isLandscapeMode);
 					cameraP.addView(mPreview, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 					
 					mPreview.setCamera(mCamera);
 				}
 				
 				isInitialized = true;
 				isCameraLock = false;
 				Log.d(TAG, "initialize camera done.");
 				updateCapState();
 				PerformanceTracker.stamp(Event.E_End_Init_Camera);
 				break;
 			}
 			case MSG_RESUME_ME:{
 //		        Profiler.markStart("sensorRegister");
 		        if (sensor != null) {
 		        	sensorMgr.registerListener(CameraActivity.this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
 		        }
 //		        Profiler.markEnd("sensorRegister");
 				break;
 			}
 			case MSG_PIC_TAKEN:
 				Camera cam = mCamera;
 				if (isInitialized && cam != null) {
 					Log.d(TAG, "try start another preview!");
 					cam.startPreview();
 				}
 				Log.d(TAG, "start another preview succed!");
 				break;
 			case MSG_SAVE_DONE:
 				BXThumbnail newPicPair = (BXThumbnail)  msg.obj;
 				if (newPicPair != null) {
 					ImageUploader.getInstance().startUpload(newPicPair.getLocalPath(), newPicPair.getThumbnail(), null);
 					boolean succed = appendResultImage(newPicPair);
 					if (succed) {
 						addImageUri(newPicPair);
 					}
 				} else {
 					ViewUtil.showToast(CameraActivity.this, "获取照片失败", false);
 				}
 				
 				updateCapState();
 				
 				break;
 			case MSG_ORIENTATION_CHANGE:
 				View capV = findViewById(R.id.cap);
 				if (isInitialized) {
 					Parameters params = mCamera.getParameters();
 					params.setRotation(currentOrien.orientationDegree);
 					mCamera.setParameters(params);
 				}
 				
 				if (isInitialized && capV != null && capV.isEnabled()) { //If capture button is disabled, do not need auto focus.
 					autoFocusWhenOrienChange();
 				}
 				break;
 			case MSG_UPDATE_THUMBNAILS:
 				if (imageList != null && imageList.size() > 0) {
 					
 					for (BXThumbnail t : imageList) {
 						appendResultImage(t);
 					}
 					
 				}
 				else {
 					ViewUtil.showToast(CameraActivity.this, getString(R.string.tip_camera_before_post), true);
 				}
 				break;
 				
 			case MSG_UPLOADING_STATUS_UPDATE:
 				Pair<Bitmap, ImageView> p = (Pair<Bitmap, ImageView>) msg.obj;
 				
 				if (p.second.getParent() == null || ((View)p.second.getParent()).getTag() == null) {
 					return;
 				}
 				
 				Log.w(TAG, "start to update view " + p.second.getParent().hashCode());
 				if (p.second != null) {
 					switch (msg.arg1) {
 					case STATE_UPLOADING:
 						((View) p.second.getParent()).findViewById(R.id.loading_status).setVisibility(View.VISIBLE);
 						p.second.setImageBitmap(p.first);
 						break;
 					case STATE_FAIL:
 						((View) p.second.getParent()).findViewById(R.id.loading_status).setVisibility(View.GONE);
 						p.second.setImageResource(R.drawable.icon_load_fail);
 						break;
 					case STATE_DONE:
 						((View) p.second.getParent()).findViewById(R.id.loading_status).setVisibility(View.GONE);
 						p.second.setImageBitmap(p.first);
 						break;
 					}
 				}
 				break;
 			}
 		}
     
     }
     
     private ArrayList<String> getLocalUrls() {
     	ArrayList<String> list = new ArrayList<String>();
     	for (BXThumbnail t : imageList) {
     		list.add(t.getLocalPath());
     	}
     	
     	return list;
     }
     
     private void addImageUri(BXThumbnail  p) {
     	imageList.add(p);
     	updateCapState();
     	if (imageList.size() == MAX_IMG_COUNT) {
     		finishTakenPic();
     	}
     }
     
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         if (keyCode == KeyEvent.KEYCODE_BACK)
         {
         	cancelTakenPic();
         }
         
         else{
         	return super.onKeyDown(keyCode, event);
         }
         
         return true;
     }
     
     private void cancelTakenPic() {
     	onCancelEdit();
     	int resultCode = getIntent().getExtras().getInt(CommonIntentAction.EXTRA_COMMON_FINISH_CODE, Activity.RESULT_CANCELED);
     	
     	Intent backIntent = (Intent) getIntent().getExtras().get(CommonIntentAction.EXTRA_COMMON_INTENT);//new Intent(this, QuanleimuMainActivity.class);
 		Bundle bundle = new Bundle();
 		bundle.putBoolean(CommonIntentAction.EXTRA_COMMON_IS_THIRD_PARTY, true);
 		bundle.putInt(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, getIntent().getExtras().getInt(CommonIntentAction.EXTRA_COMMON_REQUST_CODE));
 		bundle.putInt(CommonIntentAction.EXTRA_COMMON_RESULT_CODE, resultCode);
 		
 		backIntent.putExtras(bundle);
 		backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		this.startActivity(backIntent);
 		this.finish();
     }
     
     private void finishTakenPic() {
     	
     	//Cancel the delete list.
     	for (BXThumbnail t : deleteList) {
     		ImageUploader.getInstance().cancel(t.getLocalPath());
     	}
     	
 //    	Intent backIntent = (Intent) getIntent().getExtras().get(CommonIntentAction.EXTRA_COMMON_INTENT);//new Intent(this, QuanleimuMainActivity.class);
 		Bundle bundle = new Bundle();
 		bundle.putBoolean(CommonIntentAction.EXTRA_COMMON_IS_THIRD_PARTY, true);
 		bundle.putInt(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, getIntent().getExtras().getInt(CommonIntentAction.EXTRA_COMMON_REQUST_CODE));
 		bundle.putInt(CommonIntentAction.EXTRA_COMMON_RESULT_CODE, RESULT_OK);
 		
 		Intent data = new Intent();
 		data.putStringArrayListExtra(CommonIntentAction.EXTRA_IMAGE_LIST, getLocalUrls());
 		bundle.putParcelable(CommonIntentAction.EXTRA_COMMON_DATA, data);
 		
 		setResult(Activity.RESULT_OK,data);
 		this.finish();
 		
 		
 //		backIntent.putExtras(bundle);
 //		backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 //		this.startActivity(backIntent);
 //		this.finish();
     }
     
     private void deleteImageUri(BXThumbnail t) {
     	imageList.remove(t);
     	deleteList.add(t);
     	if (!originalList.contains(t.getLocalPath())) {
     		ImageUploader.getInstance().cancel(t.getLocalPath());
     	}
     	updateCapState();
     }
     
 //    private ViewGroup findFirstBlankImage(ViewGroup root) {
 //		final int count = root.getChildCount();
 //		for (int i=0; i<count; i++) {
 //			ViewGroup child = (ViewGroup) root.getChildAt(i);
 //			if (child.getTag() == null) {
 //				return child;
 //			}
 //		}
 //    	
 //    	return null;
 //    }
     
     
     private boolean appendResultImage(BXThumbnail thumbnail) {
     	
     	if (thumbnail == null) {
     		return false;
     	}
     	
     	ViewGroup vp = (ViewGroup) this.findViewById(R.id.result_parent);
     	LayoutInflater inflater = LayoutInflater.from(vp.getContext());
     	ViewGroup imageRoot = (ViewGroup) inflater.inflate(R.layout.single_image_layout, null);
 //    	ViewGroup imageRoot = findFirstBlankImage(vp);
 //    	if (imageRoot == null) {
 //    		return false; // you should nerver encounter this case.
 //    	}
     	vp.addView(imageRoot);
     	imageRoot.setTag(thumbnail.getLocalPath());
 		
 		final View deleteCmd = imageRoot.findViewById(R.id.delete_preview);
 		deleteCmd.setVisibility(View.VISIBLE);
 		deleteCmd.setOnClickListener(deleteListener);
 		deleteCmd.setTag(thumbnail);
 		
 		View deleteBtn = imageRoot.findViewById(R.id.delete_btn);
 		deleteBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				deleteCmd.performClick();
 			}
 		});
 		
 		try
 		{
 			ImageView img = (ImageView) imageRoot.findViewById(R.id.result_image);
 			if (thumbnail.getThumbnail() != null) {
 				img.setImageBitmap(thumbnail.getThumbnail());
 			}
 			UploadingCallback cbk = new UploadingCallback(img);
 //			callbacks.add(cbk);
 			imageRoot.setTag(cbk);
 			ImageUploader.getInstance().registerCallback(thumbnail.getLocalPath(), cbk);
 			
 			TextView nextLabel = (TextView) findViewById(R.id.right_btn_txt);
 			nextLabel.setText("完成");
 			
 			return true;
 		}
 		catch (Throwable t) {
 			Log.d(TAG, "error when add image view " + imageRoot);
 		}
 		
 		return false;
     }
     
     class UploadingCallback implements ImageUploader.Callback {
     	WeakReference<ImageView> viewRef;
     	private boolean disable = false;
     	UploadingCallback(ImageView v) {
     		viewRef = new WeakReference<ImageView>(v);
     	}
     	
     	public void disable() {
     		disable = true;
     		
 //    		ImageView img = viewRef.get();
 //			if (img != null && handler != null) { //When activity is destroyed, handler will be null.
 //				Message msg = handler.obtainMessage();
 //				msg.what = MSG_CANCEL_UPLOAD;
 //				msg.obj = img;
 //				handler.sendMessage(msg);
 //			}
     	}
     	
 		@Override
 		public void onUploadDone(String imagePath, String serverUrl,
 				Bitmap thumbnail) {
 			notifyHandler(STATE_DONE, imagePath, thumbnail);
 		}
 		@Override
 		public void onUploading(String imagePath, Bitmap thumbnail) {
 			notifyHandler(STATE_UPLOADING, imagePath, thumbnail);
 		}
 		@Override
 		public void onUploadFail(String imagePath, Bitmap thumbnail) {
 			notifyHandler(STATE_FAIL, imagePath, thumbnail);
 		}
 		
 		private void notifyHandler(int status, String imagePath, Bitmap thumbnail) {
 			Log.d(TAG, "image status update to : " + status);
 			
 			if (thumbnail != null) { //Update thumbnail.
 				for (BXThumbnail t : imageList) {
 					if (imagePath.equals(t.getLocalPath())) {
 						t.setThumbnail(thumbnail);
 					}
 				}
 			}
 			
 			if (disable) {
 				return;
 			}
 			
 			ImageView img = viewRef.get();
 			if (img != null && handler != null) { //When activity is destroyed, handler will be null.
 				Message msg = handler.obtainMessage();
 				msg.what = MSG_UPLOADING_STATUS_UPDATE;
 				msg.arg1 = status;
 				msg.obj = new Pair<Bitmap, ImageView>(thumbnail, viewRef.get());
 				handler.sendMessage(msg);
 			}
 		}
     }
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		PerformanceTracker.stamp(Event.E_CameraActivity_OnCreate_Start);
 		super.onCreate(savedInstanceState);
 		//Sensor to update current orientation. 
 		sensorMgr = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
 		sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		if (sensor != null) {
 			detector = new AccelerometerSensorDetector();
 		} else {
 			sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);
 			if (sensor != null) {
 				detector = new OrientationSensorDetector();
 			} 
 		}
 		
 //		Profiler.markStart("cameOnCreate");
 		handler = new InternalHandler(); //Make sure handler instance is created on main thread.
 		String nextBtnLabel = getIntent().getStringExtra(CommonIntentAction.EXTRA_FINISH_ACTION_LABEL);
 		if (VERSION.SDK_INT < 14) {
 			isLandscapeMode = true;
 			this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 //			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 			setContentView(R.layout.image_selector_land);
 		}
 		else
 		{
 			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 			setContentView(R.layout.image_selector);
			nextBtnLabel = nextBtnLabel.replace("\n", "");
 		}
 					
 		
 		//Take picture action.
 		findViewById(R.id.cap).setOnClickListener(this);
 		findViewById(R.id.finish_cap).setOnClickListener(this);
 		findViewById(R.id.cap).setEnabled(false);//Do not let user take pitcure before initialize.
 		findViewById(R.id.cam_focus_area).setOnClickListener(this);
 		
 		if (nextBtnLabel != null) {
 			TextView nextLabel = (TextView) findViewById(R.id.right_btn_txt);
 			nextLabel.setText(nextBtnLabel);
 		}
 		
 
 		
 		deleteListener = new OnDeleteListener();
 		
 		findViewById(R.id.pick_gallery).setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent goIntent = new Intent(Intent.ACTION_GET_CONTENT);
 				goIntent.addCategory(Intent.CATEGORY_OPENABLE);
 				goIntent.setType("image/*");
 				startActivityForResult(goIntent, REQ_PICK_GALLERY);
 			}
 		});
 		
 		ImageUploader.getInstance().attachActivity(this); //Attach activity.
 		if (this.getIntent().hasExtra(CommonIntentAction.EXTRA_IMAGE_LIST)) {
 			ArrayList<String> list = this.getIntent().getStringArrayListExtra(CommonIntentAction.EXTRA_IMAGE_LIST);
 			this.getIntent().removeExtra(CommonIntentAction.EXTRA_IMAGE_LIST);
 			this.imageList.clear();
 			this.originalList.clear();
 			for (String p : list) {
 				this.originalList.add(BXThumbnail.createThumbnail(p, null));
 			}
 			this.imageList.addAll(originalList);
 		}
 		
 		handler.sendEmptyMessageDelayed(MSG_UPDATE_THUMBNAILS, 500);
 		PerformanceTracker.stamp(Event.E_CameraActivity_OnCreate_Leave);
 //		Profiler.markEnd("cameOnCreate");
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		ArrayList<String> imgKeys = new ArrayList<String>();
 		if (imageList != null) {
 			for (BXThumbnail t : imageList) {
 				imgKeys.add(t.getLocalPath());
 			}
 		}
 		
 		outState.putStringArrayList(CommonIntentAction.EXTRA_IMAGE_LIST, imgKeys);
 	}
 	
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		
 		ArrayList<String> imgKeys = savedInstanceState.getStringArrayList(CommonIntentAction.EXTRA_IMAGE_LIST);
 		if (imgKeys != null) {
 			this.imageList.clear();
 			this.originalList.clear();
 			for (String p : imgKeys) {
 				this.originalList.add(BXThumbnail.createThumbnail(p, null));
 			}
 			this.imageList.addAll(originalList);
 		}
 		
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	protected void onDestroy() {
 //		this.handler = null; //Do not handle action any more.
 		
 //		for (Callback callback : this.callbacks) {
 //			ImageUploader.getInstance().removeCallback(callback);
 //		}
 //		this.callbacks.clear();
 		
 		super.onDestroy();
 		Log.d(TAG, "cam destroyed!");
 	}
 	
 	@SuppressLint("NewApi") 
 	public void initializeCamera(){
 //		Profiler.markStart("initializeCame");
 	    Camera c = null;
 	    try {
     		c = Camera.open(); // attempt to get a Camera instance, this will open the default facing camera if there is any.
     		isFrontCam = false;
 	    }
 	    catch (Exception e){
 	        // Camera is not available (in use or does not exist)
 	    	Log.d("CAM", "fail to open cam " + e.getMessage());
 	    }
 	    
 	    if (c == null) { //For devices which only have front camera.
 	    	try {
 	    		c = Camera.open(0);
 	    		CameraInfo info = new CameraInfo();
 	    		Camera.getCameraInfo(0, info);
 	    		isFrontCam = info.facing == CameraInfo.CAMERA_FACING_FRONT;
 	    	}
 	    	catch (Throwable t) {
 	    		Log.w(TAG, "Open camera failed.");
 	    	}
 	    }
 	    
 	    mCamera = c;
 	    
 	    if (mCamera != null && this.getIntent().hasExtra("location")) {
 	    	BXLocation loc = (BXLocation) this.getIntent().getSerializableExtra("location");
 	    	mCamera.getParameters().setGpsLatitude(loc.fLat);
 	    	mCamera.getParameters().setGpsLongitude(loc.fLon);
 	    }
 //	    Profiler.markEnd("initializeCame");
 //	    return c; // returns null if camera is unavailable
 	}
 
 	@Override
 	protected void onPause() {
 		handler.sendEmptyMessage(MSG_PAUSE_ME);
 		Log.d(TAG, "cam paused");
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		PerformanceTracker.stamp(Event.E_CameraActivity_onResume);
 		GlobalDataManager.getInstance().setLastActiveActivity(this.getClass());
 		Tracker.getInstance().pv(TrackConfig.TrackMobile.PV.CAMERA).end();
 //		Profiler.markStart("cameOnResume");
 		super.onResume();
 		
         handler.sendEmptyMessage(MSG_INIT_CAME);
         handler.sendEmptyMessageDelayed(MSG_RESUME_ME, 500);//Do not block the main thread.
 //        Profiler.markEnd("cameOnResume");
         
 	}
 	
 	//An indicator to indicate if user.
 	private void autoFocusWhenOrienChange() {
 		try {
 			Camera cam = mCamera;
 			if (cam != null) {
 				cam.autoFocus(new Camera.AutoFocusCallback() {
 					public void onAutoFocus(boolean success, Camera camera) {
 						//Do nothing.
 					}
 				});
 			}
 		}
 		catch (Throwable t) {
 			Log.d(TAG, "error occur when do autofocus.");
 		}
 	}
 	
 	private PictureCallback mPicture = new PictureCallback() {
 
 	    @Override
 	    public void onPictureTaken(byte[] data, Camera camera) {
 
 	    	isCameraLock = false;
 	    	if (!Util.isExternalStorageWriteable()) {
 	    		ViewUtil.showToast(CameraActivity.this, "请检查SD卡状态", false);
 	    		handler.sendEmptyMessage(MSG_CANCEL_STORE_PIC);
 	    		return;
 	    	}
 	    	postAppendPhoto(data);
 	        
 //	        handler.sendEmptyMessage(MSG_PIC_TAKEN);
 	    }
 	};
 	
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 		case R.id.cap:
 			takePic();
 			break;
 		case R.id.finish_cap:
 			finishTakenPic();
 			break;
 		case R.id.cam_focus_area:
 			if (mCamera != null) {
 				mCamera.autoFocus(null);
 			}
 			break;
 		}
 	}
 	
 	private void onCancelEdit() {
 		for (BXThumbnail t : imageList ) {
 			if (originalList.indexOf(t) == -1) {
 				ImageUploader.getInstance().cancel(t.getLocalPath());
 			}
 		}
 	}
 	
 	public void takePic() {
 		long currentTime = System.currentTimeMillis();
 		Log.w(TAG, "click to take pic " + currentTime);
 		if (!isInitialized) {
 			return;
 		}
 		
 		final long gap = currentTime - lastClickTime;
 		boolean isProcessing = lastClickTime != 0 &&  (gap > 0 ) && gap < MIN_PICTURE_TAKEN_GAP;
 		lastClickTime = currentTime;
 		if (isProcessing) {
 			return;
 		}
 		
 		View capV = findViewById(R.id.cap);
 		capV.setEnabled(false);
 		Log.d(TAG, "autofocus before take pitcure");
 		final BooleanWrapper bWrapper = new BooleanWrapper();
 		try {
 			if (isCameraLock) {
 				return;
 			}
 			isCameraLock = true;
 			mCamera.autoFocus(new AutoFocusCallback() {
 				@Override
 				public void onAutoFocus(boolean focused, Camera cam) {
 					if (bWrapper.isTrue) {
 						return;
 					}
 					bWrapper.isTrue = true;
 					Camera camera = mCamera;
 					if (camera != null) {
 						startGlint();
 						Log.d(TAG, "start invoke take picture");
 						camera.takePicture(null, null, mPicture);
 						Log.d(TAG, "take picture invoke return");
 						camera.cancelAutoFocus(); //Avoid deprecate auto-focus notification. 
 					}
 				}
 			});
 		} catch (Throwable t) {
 			isCameraLock = false;
 			//For HTC auto focus fail issue.
 			Log.w(TAG, "auto focus exception : " + t.getMessage());
 		}
 		
 		//For some device, auto focus never return for unknown reason.
 		Message msg = handler.obtainMessage(MSG_TAKEPIC_DELAY, bWrapper);
 		handler.sendMessageDelayed(msg, 1500);
 	}
 	
 	private void startGlint() {
 		final View v = findViewById(R.id.cam_glint_area);
 		v.setVisibility(View.VISIBLE);
 		Animation glint = AnimationUtils.loadAnimation(this, R.anim.glint);
 		glint.setAnimationListener(new AnimationListener() {
 			
 			@Override
 			public void onAnimationStart(Animation animation) {
 				
 			}
 			
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				
 			}
 			
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				v.setVisibility(View.GONE);
 			}
 		});
 		v.startAnimation(glint);
 		
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent sensorEvent) {
 //		if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
 //			float pitch = sensorEvent.values[1];
 //			float roll = sensorEvent.values[2];
 //			
 //			Orien newOrien = updateOrien(pitch, roll);
 //			if (newOrien != null && currentOrien != newOrien) {
 //				Log.w(TAG, "orientation changed from " + currentOrien.des + " to " + newOrien.des);
 //				currentOrien = newOrien;
 //				handler.sendEmptyMessage(MSG_ORIENTATION_CHANGE);
 //			}
 //				
 //		}
 		Orien newOrien = detector.updateSensorEvent(sensorEvent);
 		if (newOrien != null && currentOrien != newOrien) {
 			Log.w(TAG, "orientation changed from " + currentOrien.des + " to " + newOrien.des);
 			currentOrien = newOrien;
 			handler.sendEmptyMessage(MSG_ORIENTATION_CHANGE);
 
 		}
 	}
 	
 	class OnDeleteListener implements View.OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			ViewGroup vp = (ViewGroup) v.getParent();//(ViewGroup) findViewById(R.id.result_parent);
 			UploadingCallback callback = (UploadingCallback) vp.getTag();
 			if (callback == null) {
 				return;
 			}
 			callback.disable();
 			ImageUploader.getInstance().removeCallback(callback);
 			vp.setTag(null);
 			
 			ImageView img = (ImageView) vp.findViewById(R.id.result_image);
 			img.setImageResource(R.drawable.bg_transparent);
 			vp.findViewById(R.id.loading_status).setVisibility(View.GONE);
 			BXThumbnail thumbnail = (BXThumbnail) v.getTag();
 			deleteImageUri(thumbnail);
 			v.setVisibility(View.INVISIBLE);
 			
 			
 			//Append this view to the tail of the child list.
 			ViewGroup imgContainer = (ViewGroup) vp.getParent();
 			vp.setVisibility(View.GONE);
 			imgContainer.destroyDrawingCache();//Clear the drawing status.
 			Log.e(TAG, "following view will be remove " + vp.hashCode());
 			imgContainer.removeView(vp);
 			
 //			LayoutInflater inflator = LayoutInflater.from(v.getContext());
 //			View newV = inflator.inflate(R.layout.single_image_layout, null);
 //			imgContainer.addView(newV);
 //			Log.d(TAG, "following view will be add " + newV.hashCode());
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == REQ_PICK_GALLERY && resultCode == RESULT_OK) {
 			postAppendData(data);
 		}
 	}
 	
 	private String getRealPathFromURI(Uri contentUri) {
 		return BitmapUtils.getRealPathFromURI(this, contentUri);
 	}
 	
 	private BXThumbnail findFromList(String path, List<BXThumbnail> list, boolean deleteIt) {
 		BXThumbnail t = null;
 		for (BXThumbnail tt : list) {
 			if (tt.getLocalPath().equals(path)) {
 				t = tt;
 				break;
 			}
 		}
 		
 		if (t != null && deleteIt) {
 			list.remove(t);
 		}
 		
 		return t;
 	}
 	
 	private void postAppendData(final Intent data) {
 		
 		AsyncTask<Uri, Integer, BXThumbnail> task = new AsyncTask<Uri, Integer, BXThumbnail>() {
 
 			@Override
 			protected BXThumbnail doInBackground(Uri... params) {
 				String path = getRealPathFromURI(params[0]);
 				BXThumbnail tb = findFromList(path, deleteList, true);
 				if (tb == null) {
 					tb = BitmapUtils.copyAndCreateThrmbnail(path, CameraActivity.this);
 				}
 				
 				return tb;
 			}
 
 			@Override
 			protected void onPostExecute(BXThumbnail result) {
 				Message msg = handler.obtainMessage(MSG_SAVE_DONE, result);
 		        handler.sendMessage(msg);
 		        showOneMoreMsg();
 			}
 		};
 		
 		task.execute(data.getData());
 	}
 	
 	private void postAppendPhoto(final byte[] cameraData) {
 		
 		handler.sendEmptyMessage(MSG_PIC_TAKEN);
 		AsyncTask<byte[], Integer, BXThumbnail> task = new AsyncTask<byte[], Integer, BXThumbnail>() {
 
 			@Override
 			protected BXThumbnail doInBackground(byte[]... params) {
 				//Do not do rotation because we set rotation parameter to camera, the output buffer should already be rotated by camera.
 				return BitmapUtils.saveAndCreateThumbnail(params[0], /*currentOrien.orientationDegree*/0, CameraActivity.this, isFrontCam);
 			}
 
 			@Override
 			protected void onPostExecute(BXThumbnail result) {
 				Message msg = handler.obtainMessage(MSG_SAVE_DONE, result);
 		        handler.sendMessage(msg);
 		        showOneMoreMsg();
 			}
 		};
 		task.execute(cameraData);
 	}
 	
 	private void showOneMoreMsg() {
 		boolean full = (MAX_IMG_COUNT -1) == imageList.size();
 		if (!full) {
         	ViewUtil.showToast(CameraActivity.this,  "再来一张吧，你还能再添加" + (MAX_IMG_COUNT-imageList.size() -1) + "张", false);
         }
 	}
 	
 	
 	protected void finalize() {
 		try {
 			if (mCamera != null) {
 				mCamera.release();
 			}
 		} catch (Throwable t) {
 			Log.d(TAG, "close camera failed when finalize this class.");
 		}
 		
 		try {
 			super.finalize();
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		Log.d(TAG, "came finalize");
 	}
 	
 	private class BooleanWrapper {
 		boolean isTrue;
 	}
 	
 }
