 package com.draw.hackmit;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnHoverListener;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.view.Menu;
 import android.hardware.Camera;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.Toast;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.graphics.ImageFormat;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 /* Code partially taken from https://github.com/commonsguy/cw-advandroid/blob/master/Camera/Preview/src/com/commonsware/android/camera/PreviewDemo.java
  * License info below
  */
 /***
  * Copyright (c) 2008-2012 CommonsWare, LLC Licensed under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law
  * or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  * 
  * From _The Busy Coder's Guide to Advanced Android Development_
  * http://commonsware.com/AdvAndroid
  */
 
 public class MainActivity extends Activity implements OnHoverListener {
 
 	private SurfaceView preview = null;
 	private SurfaceHolder previewHolder = null;
 	private Camera camera = null;
 	private boolean inPreview = false;
 	private boolean cameraConfigured = false;
 	// Declare the global text variable used across methods
 	private TextView text;
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		// Initialize the layout variable and listen to hover events on it
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
 		layout.setOnHoverListener(this);
 		// Initialize the text widget so we can edit the text inside
 		text = (TextView) findViewById(R.id.text);
 
 		preview = (SurfaceView) findViewById(R.id.preview);
 		previewHolder = preview.getHolder();
 		previewHolder.addCallback(surfaceCallback);
 		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
 			Camera.CameraInfo info = new Camera.CameraInfo();
 
 			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
 				Camera.getCameraInfo(i, info);
 
 				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
 					camera = Camera.open(i);
 				}
 			}
 		}
 
 		if (camera == null) {
 			camera = Camera.open();
 		}
 
 		startPreview();
 	}
 
 	@Override
 	public void onPause() {
 		if (inPreview) {
 			camera.stopPreview();
 		}
 
 		camera.release();
 		camera = null;
 		inPreview = false;
 
 		super.onPause();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.camera) {
 			if (inPreview) {
 				camera.takePicture(null, null, photoCallback);
 				inPreview = false;
 			}
 		}
 
 		return (super.onOptionsItemSelected(item));
 	}
 
 	private Camera.Size getBestPreviewSize(int width, int height,
 			Camera.Parameters parameters) {
 		Camera.Size result = null;
 
 		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
 			if (size.width <= width && size.height <= height) {
 				if (result == null) {
 					result = size;
 				} else {
 					int resultArea = result.width * result.height;
 					int newArea = size.width * size.height;
 
 					if (newArea > resultArea) {
 						result = size;
 					}
 				}
 			}
 		}
 
 		return (result);
 	}
 
	private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
 		Camera.Size result = null;
 
 		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
 			if (result == null) {
 				result = size;
 			} else {
 				int resultArea = result.width * result.height;
 				int newArea = size.width * size.height;
 
				if (newArea < resultArea) {
 					result = size;
 				}
 			}
 		}
 
 		return (result);
 	}
 
 	private void initPreview(int width, int height) {
 		if (camera != null && previewHolder.getSurface() != null) {
 			try {
 				camera.setPreviewDisplay(previewHolder);
 			} catch (Throwable t) {
 				Log.e("PreviewDemo-surfaceCallback",
 						"Exception in setPreviewDisplay()", t);
 				Toast.makeText(MainActivity.this, t.getMessage(),
 						Toast.LENGTH_LONG).show();
 			}
 
 			if (!cameraConfigured) {
 				Camera.Parameters parameters = camera.getParameters();
 				Camera.Size size = getBestPreviewSize(width, height, parameters);
				Camera.Size pictureSize = getSmallestPictureSize(parameters);
 
 				if (size != null && pictureSize != null) {
 					parameters.setPreviewSize(size.width, size.height);
 					parameters.setPictureSize(pictureSize.width,
 							pictureSize.height);
 					parameters.setPictureFormat(ImageFormat.JPEG);
 					camera.setParameters(parameters);
 					cameraConfigured = true;
 				}
 			}
 		}
 	}
 
 	private void startPreview() {
 		if (cameraConfigured && camera != null) {
 			camera.startPreview();
 			inPreview = true;
 		}
 	}
 
 	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
 		public void surfaceCreated(SurfaceHolder holder) {
 			// no-op -- wait until surfaceChanged()
 		}
 
 		public void surfaceChanged(SurfaceHolder holder, int format, int width,
 				int height) {
 			initPreview(width, height);
 			startPreview();
 		}
 
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			// no-op
 		}
 	};
 	
 	Camera.PictureCallback photoCallback=new Camera.PictureCallback() {
 	    public void onPictureTaken(byte[] data, Camera camera) {
 	      new SavePhotoTask().execute(data);
 	      camera.startPreview();
 	      inPreview=true;
 	    }
 	  };
 
 	  class SavePhotoTask extends AsyncTask<byte[], String, String> {
 	    @Override
 	    protected String doInBackground(byte[]... jpeg) {
 	      File photo=
 	          new File(Environment.getExternalStorageDirectory(),
 	                   "Pictures");
 
 	      if (! photo.exists()){
 	          if (! photo.mkdirs()){
 	              Log.d("Camera", "failed to create directory");
 	              return null;
 	          }
 	      }
 
 	      // Create a media file name
 	      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 	      File mediaFile;
 	      mediaFile = new File(photo.getPath() + File.separator +
 	    		  "IMG_"+ timeStamp + ".jpg");
 	           
 	      try {
 	        FileOutputStream fos=new FileOutputStream(mediaFile.getPath());
 
 	        fos.write(jpeg[0]);
 	        fos.close();
 	      }
 	      catch (java.io.IOException e) {
 	        Log.e("Camera", "Exception in photoCallback", e);
 	      }
 
 	      return(null);
 	    }
 	  }
 
 	// For whenever a hover event is triggered on an element being listened to
 	public boolean onHover(View v, MotionEvent e) {
 		// Depending on what action is performed, set the text to that action
 		switch (e.getActionMasked()) {
 		case MotionEvent.ACTION_HOVER_ENTER:
 			text.setText("ACTION_HOVER_ENTER");
 			break;
 		case MotionEvent.ACTION_HOVER_MOVE:
 			text.setText("ACTION_HOVER_MOVE");
 			break;
 		case MotionEvent.ACTION_HOVER_EXIT:
 			text.setText("ACTION_HOVER_EXIT");
 			break;
 		}
 		// Along with the event name, also print the XY location of the data
 		text.setText(text.getText() + " - X: " + e.getX() + " - Y: " + e.getY());
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
