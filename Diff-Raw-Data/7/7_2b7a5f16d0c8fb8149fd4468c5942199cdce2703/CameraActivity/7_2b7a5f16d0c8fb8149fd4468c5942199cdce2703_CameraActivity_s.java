 package com.alphadog.tribe.activities;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.alphadog.tribe.R;
 
 public class CameraActivity extends Activity {
 
 	protected static final String IMAGE_PATH = "IMAGE_PATH";
 	private SurfaceView preview;
 	private SurfaceHolder previewHolder;
 	private Camera camera;
 	private long reviewId;
 	
 	@Override
 	public void onCreate(Bundle savedInstance) {
 		super.onCreate(savedInstance);
 	
 		reviewId = NewReviewActivity.getCurrentTime();
 		
 		setContentView(R.layout.camera);
 		
 		bindCameraComponents();
 		
 		initalizeCameraToTakePicture();
 	}
 	
 	private void bindCameraComponents() {
 		ImageView cameraButton = (ImageView)findViewById(R.id.camera_click);
 		cameraButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				takePicture();
 			}
 		});
 	}
 
 	private void initalizeCameraToTakePicture() {
 		Log.i("NewReviewActivity", "Initializing the camera surface now");
 		preview=(SurfaceView)findViewById(R.id.camera_preview);
 		previewHolder=preview.getHolder();
 		previewHolder.addCallback(surfaceCallback);
 		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode==KeyEvent.KEYCODE_CAMERA || keyCode==KeyEvent.KEYCODE_SEARCH) {
 			 takePicture();
 			 return(true);
 		}
 		return(super.onKeyDown(keyCode, event));
 	}
 	
 	private void takePicture() {
		 camera.takePicture(null, null, photoCallback);
 	}
 	
 	SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
 		public void surfaceCreated(SurfaceHolder holder) {
 			camera=Camera.open();
 
 			try {
 				camera.setPreviewDisplay(previewHolder);
 			}
 			catch (Throwable t) {
 				Log.e("CameraActivity","Exception in setPreviewDisplay()", t);
 				Toast.makeText(CameraActivity.this, t.getMessage(),Toast.LENGTH_LONG).show();
 			}
 		}
 		
 	    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
 	        final double ASPECT_TOLERANCE = 0.05;
 	        double targetRatio = (double) w / h;
 	        if (sizes == null) return null;
 
 	        Size optimalSize = null;
 	        double minDiff = Double.MAX_VALUE;
 
 	        int targetHeight = h;
 
 	        // Try to find an size match aspect ratio and size
 	        for (Size size : sizes) {
 	            double ratio = (double) size.width / size.height;
 	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
 	            if (Math.abs(size.height - targetHeight) < minDiff) {
 	                optimalSize = size;
 	                minDiff = Math.abs(size.height - targetHeight);
 	            }
 	        }
 
 	        // Cannot find the one match the aspect ratio, ignore the requirement
 	        if (optimalSize == null) {
 	            minDiff = Double.MAX_VALUE;
 	            for (Size size : sizes) {
 	                if (Math.abs(size.height - targetHeight) < minDiff) {
 	                    optimalSize = size;
 	                    minDiff = Math.abs(size.height - targetHeight);
 	                }
 	            }
 	        }
 	        return optimalSize;
 	    }
 
 		public void surfaceChanged(SurfaceHolder holder,int format, int width,int height) {
 			Camera.Parameters parameters=camera.getParameters();
 
 			List<Size> sizes = parameters.getSupportedPreviewSizes();
 			Size optimalSize = getOptimalPreviewSize(sizes, width, height);
 			parameters.setPreviewSize(optimalSize.width, optimalSize.height);
 			parameters.setPictureFormat(PixelFormat.JPEG);
 			camera.setParameters(parameters);
 			camera.startPreview();
 		}
 
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			Log.i("NewReviewActivity", "Surface destroy called and so stopping preview");
 			camera.stopPreview();
 			camera.release();
 			camera=null;
 		}
 	};
 
 	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
 		 public void onPictureTaken(final byte[] data, final Camera camera) {
 			 Log.i("NewReviewActivity", "Callback for picture click happened. Storing picture data in session; so that we can upload the picture later");
 
 			 new Thread(new Runnable() {
 				@Override
 				public void run() {
 					String imagePath = savePictureInCache(data);
 					//start new activity since snap has been taken.
 					Intent newReviewIntent = new Intent(CameraActivity.this, NewReviewActivity.class);
 					newReviewIntent.putExtra(NewReviewActivity.PENDING_REVIEW_ID, reviewId);
 					newReviewIntent.putExtra(CameraActivity.IMAGE_PATH, imagePath);
 					startActivity(newReviewIntent);
 				}
 			}).start();
 			 
 			 CameraActivity.this.finish();
 		 }
 	};
 	
 	private String savePictureInCache(byte[] pictureData) {
 		if(pictureData != null) {
 			//create a new file with review id as it's name
 			String photoName = reviewId + ".jpg";
 			File photo=new File(getDir("gv_img_cache", Context.MODE_PRIVATE), photoName);
 	
 			//Delete the photo that exists with same name.
 			//Should never happen in ideal scenario
 			if (photo.exists()) {
 				photo.delete();
 			}
 	
 			try {
 				FileOutputStream fos = new FileOutputStream(photo.getPath());
 				fos.write(pictureData);
 				fos.close();
 	
 				Log.i(this.getClass().getName(), "Saved picture with path:"+photo.getPath());
 				//update pending review record with the path
 				return photo.getPath();
 			}
 			catch (java.io.IOException e) {
 				Log.e("SavePhotoTask", "Exception in photoCallback for photo name :"+ photoName, e);
 			}
 		}
 		return null;
 	}
 
 	
 }
