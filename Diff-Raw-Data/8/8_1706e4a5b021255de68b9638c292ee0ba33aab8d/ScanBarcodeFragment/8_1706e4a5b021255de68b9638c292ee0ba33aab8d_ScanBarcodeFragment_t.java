 package com.manassorn.shopbox;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Map;
 
 import android.app.Fragment;
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.google.zxing.BarcodeFormat;
 import com.google.zxing.DecodeHintType;
 import com.google.zxing.Result;
 import com.manassorn.shopbox.camera.CameraManager;
 
 public class ScanBarcodeFragment extends Fragment implements OnTouchListener, Callback, OnFocusChangeListener {
 	private static final String TAG = ScanBarcodeFragment.class.getSimpleName();
 	private View barcodeLaserView;
 	private Button barcodePushButton;
 	private CameraManager cameraManager;
 	private boolean hasSurface;
 	private ScanBarcodeFragmentHandler handler;
 	private Collection<BarcodeFormat> decodeFormats;
 	private Map<DecodeHintType, ?> decodeHints;
 	private String characterSet;
 	private BeepManager beepManager;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_scan_barcode, container, false);
 
 		barcodeLaserView = view.findViewById(R.id.barcode_laser);
 		barcodePushButton = (Button) view.findViewById(R.id.barcode_push_button);
 		barcodePushButton.setOnTouchListener(this);
 	    beepManager = new BeepManager(getActivity());
 	    
 	    EditText editText = (EditText) view.findViewById(R.id.search_barcode);
 	    editText.setOnFocusChangeListener(this);
 	    
 	    return view;
 	}
 
 	@Override
 	public void onPause() {
 		if (handler != null) {
 			handler.quitSynchronously();
 			handler = null;
 		}
 		cameraManager.closeDriver();
 		if (!hasSurface) {
 			SurfaceView surfaceView = (SurfaceView) getView().findViewById(R.id.camera_preview);
 			SurfaceHolder surfaceHolder = surfaceView.getHolder();
 			surfaceHolder.removeCallback(this);
 		}
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
		cameraManager = new CameraManager(getActivity());
 
 		SurfaceView surfaceView = (SurfaceView) getView().findViewById(R.id.camera_preview);
 		SurfaceHolder surfaceHolder = surfaceView.getHolder();
 //		Drawable drawable = getResources().getDrawable(R.drawable.sample_barcode);
 //		surfaceView.setBackground(drawable);
 		if (hasSurface) {
 			// The activity was paused but not stopped, so the surface still
 			// exists. Therefore
 			// surfaceCreated() won't be called, so init the camera here.
 			initCamera(surfaceHolder);
 		} else {
 			// Install the callback and wait for surfaceCreated() to init the
 			// camera.
 			surfaceHolder.addCallback(this);
 			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		}
 
 		beepManager.updatePrefs();
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			barcodeLaserView.setVisibility(View.VISIBLE);
 			beepManager.playBeepSoundAndVibrate();
 		}
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 			barcodeLaserView.setVisibility(View.GONE);
 		}
 		return false;
 	}
 
 	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
 		// lastResult = rawResult;
 		String code = String.valueOf(rawResult);
 		beepManager.playBeepSoundAndVibrate();
 		Toast.makeText(getActivity(), "Barcode: " + code, Toast.LENGTH_LONG);
 	}
 
 	private void initCamera(SurfaceHolder surfaceHolder) {
 		if (surfaceHolder == null) {
 			throw new IllegalStateException("No SurfaceHolder provided");
 		}
 		if (cameraManager.isOpen()) {
 			// Log.w(TAG,
 			// "initCamera() while already open -- late SurfaceView callback?");
 			return;
 		}
 		try {
 			cameraManager.openDriver(surfaceHolder);
 			// Creating the handler starts the preview, which can also throw a
 			// RuntimeException.
 			if (handler == null) {
 				handler = new ScanBarcodeFragmentHandler(this, decodeFormats, decodeHints,
 						characterSet, cameraManager);
 			}
 			// decodeOrStoreSavedBitmap(null, null);
 		} catch (IOException ioe) {
 			// Log.w(TAG, ioe);
 			// displayFrameworkBugMessageAndExit();
 		} catch (RuntimeException e) {
 			// Barcode Scanner has seen crashes in the wild of this variety:
 			// java.?lang.?RuntimeException: Fail to connect to camera service
 			// Log.w(TAG, "Unexpected error initializing camera", e);
 			// displayFrameworkBugMessageAndExit();
 		}
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		hasSurface = false;
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		if (holder == null) {
 			// Log.e(TAG,
 			// "*** WARNING *** surfaceCreated() gave us a null surface!");
 		}
 		if (!hasSurface) {
 			hasSurface = true;
 			initCamera(holder);
 		}
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 
 	}
 
 	@Override
 	public void onFocusChange(View v, boolean hasFocus) {
 		switch(v.getId()) {
 			case R.id.search_barcode:
 				SelectProductActivity activity = (SelectProductActivity) getActivity();
 				activity.openEnterBarcode();
 		}
 	}
 
 }
