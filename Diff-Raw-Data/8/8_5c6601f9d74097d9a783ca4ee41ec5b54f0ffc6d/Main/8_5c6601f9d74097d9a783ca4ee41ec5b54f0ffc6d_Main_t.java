 package com.example.shooter.antistress;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.Application;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.ApplicationInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ImageFormat;
 import android.graphics.PixelFormat;
 import android.graphics.PorterDuff;
 import android.graphics.Bitmap.CompressFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.PictureCallback;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class Main extends Activity implements SurfaceHolder.Callback,
 		Camera.AutoFocusCallback, Camera.PreviewCallback,
 		Camera.PictureCallback {
 	private static final int SAVE_IMAGE_RESULT_SUCCES = 0;
 	private static final int SAVE_IMAGE_RESULT_FAIL = 1;
 	private static final int SAVE_IMAGE_RESULT_UNKNOW = 3;
 	
 	private static final String JPEG_FILE_PREFIX = "IMG_";
 	private static final String JPEG_FILE_SUFFIX = ".jpg";
 
 	private SurfaceView cameraView;
 	private GifView weaponView;
 	private SurfaceHolder cameraHolder;
 	private SurfaceHolder weaponHolder;
 	private Camera camera;
 	private Button throwButton;
 	private Button saveButton;
 	private Button shareButton;
 	public Bitmap finalBitmap;
 
 	private CameraViewStatusCodes cameraViewStatus = CameraViewStatusCodes.ERROR;
 	private enum CameraViewStatusCodes {  ERROR, WAITING, AUTOFOCUSING, DRAWING, DRAWING_ENDED };
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		setContentView(R.layout.main);
 
 		cameraView = (SurfaceView) findViewById(R.id.cameraSurfaceView);
 		cameraHolder = cameraView.getHolder();
 		cameraHolder.addCallback(this);
 		cameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		weaponView = ((GifView) findViewById(R.id.throwingObjectSurfaceView));
 		weaponHolder = weaponView.getHolder();
 		weaponHolder.setFormat(PixelFormat.TRANSLUCENT);
 		weaponView.setZOrderOnTop(true);
 		weaponView.setGif(R.drawable.apple);
 		throwButton = (Button)findViewById(R.id.throwButton);
 		throwButton.setOnClickListener(myBtnOnClickListener);
 		saveButton = (Button)findViewById(R.id.saveButton);
 		saveButton.setOnClickListener(mySaveAndShareBtnOnClickListener);
 		shareButton = (Button)findViewById(R.id.shareButton);
 		shareButton.setOnClickListener(mySaveAndShareBtnOnClickListener);
 		weaponHolder.addCallback(weaponView);
 	}
 
 	@Override
 	public void onPictureTaken(byte[] data, Camera camera) {
 
 	}
 	@Override
 	public void onPreviewFrame(byte[] data, Camera camera) {
 
 	}
 	@Override
 	public void onAutoFocus(boolean arg0, Camera arg1) {
 
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 	}
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 	}
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		try {
 			Camera.Parameters parameters = camera.getParameters();
 			parameters.setPictureFormat(ImageFormat.JPEG);
 			parameters.setRotation(90);
 
 			camera.setParameters(parameters);
 
 			camera.setDisplayOrientation(90);
 			camera.setPreviewCallback(this);
 			camera.setPreviewDisplay(holder);
 			camera.startPreview();
 			cameraViewStatus = CameraViewStatusCodes.WAITING;
 		} catch (IOException e) {
 			Log.d("Throwy", "Exception");
 			e.printStackTrace();
 			cameraViewStatus = CameraViewStatusCodes.ERROR;
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		camera.setPreviewCallback(null);
 		camera.stopPreview();
 		camera.release();
 		camera = null;
 	}
 
 	protected void onResume() {
 		super.onResume();
 		camera = Camera.open();
 	}
 
 	OnClickListener myBtnOnClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			switch (cameraViewStatus) {
 			case WAITING: {
 				cameraViewStatus = CameraViewStatusCodes.AUTOFOCUSING;
 				throwButton.setVisibility(View.INVISIBLE);
 				camera.autoFocus(myAutoFocusCallback);
 			}break;
 			case DRAWING_ENDED: {
 				weaponView.clear();
 				saveButton.setVisibility(View.INVISIBLE);
 				shareButton.setVisibility(View.INVISIBLE);
				throwButton.setText(getString(R.string.throw_button_caption));
 
 				cameraViewStatus = CameraViewStatusCodes.WAITING;
 				camera.startPreview();
 			}break;
 			case ERROR: {
 				Toast.makeText(getApplicationContext(),
 						"Something wrong with the camera", Toast.LENGTH_LONG).show();
 			}break;
 			default: {
 				Toast.makeText(getApplicationContext(),
 						"Have no idea what this is :(", Toast.LENGTH_LONG).show();
 			}break;
 			}
 		}
 	};
 	
 	OnClickListener mySaveAndShareBtnOnClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			Uri fileUri = saveFinalImage();
 			saveButton.setVisibility(View.INVISIBLE);
 			if (fileUri != Uri.EMPTY) {
 				if (v.getId() == saveButton.getId()) {
 					Toast.makeText(getApplicationContext(), "Saving succeful",
 							Toast.LENGTH_SHORT).show();
 				} else {
 					Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 					sharingIntent.setType("image/jpeg");
 					sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
 					startActivity(Intent.createChooser(sharingIntent,
 							getString(R.string.title_share_camera)));
 					shareButton.setVisibility(View.INVISIBLE);
 				}
 			}
 		}
 	};
 
 	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {
 		@Override
 		public void onAutoFocus(boolean success, Camera camera) {
 			camera.takePicture(null, null, null, myPictureCallback);
 		}
 	};
 	
 	PictureCallback myPictureCallback = new PictureCallback() {
 		@Override
 		public void onPictureTaken(byte[] data, Camera camera) {
 			cameraViewStatus = CameraViewStatusCodes.DRAWING;
 			Throw();
 			cameraViewStatus = CameraViewStatusCodes.DRAWING_ENDED;
 			throwButton.setVisibility(View.VISIBLE);
 			saveButton.setVisibility(View.VISIBLE);
 			shareButton.setVisibility(View.VISIBLE);
			throwButton.setText(getString(R.string.throw_button_back_caption));
 			
 			Canvas finalCanvas = new Canvas(finalBitmap);
 			finalCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
 			finalCanvas.drawBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), 0, 0, null);
 			weaponView.getFinalBitmap(finalCanvas);
 			finalCanvas.save();
 			
 		}
 	};
 	
 	private void Throw() {
 		weaponView.setGif(R.drawable.apple);
 		weaponView.play();
 	}
 	
 	private Uri saveFinalImage(){
 		Uri uri = Uri.EMPTY;
 		
 		if (Environment.getExternalStorageState().equals(
 				Environment.MEDIA_MOUNTED)) {
 			try {
 				File saveDir = new File(Environment
 						.getExternalStoragePublicDirectory(
 								Environment.DIRECTORY_PICTURES).getPath()
						+ getString(R.string.app_name) + "/");
 				if (!saveDir.exists()) {
 					saveDir.mkdirs();
 				}
 				File imageFile = new File(saveDir,
 						JPEG_FILE_PREFIX + 
 						new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + 
 						JPEG_FILE_SUFFIX);
 				
 				FileOutputStream fos = new FileOutputStream(imageFile);
 				finalBitmap.compress(CompressFormat.JPEG, 95, fos);
 				uri = Uri.fromFile(imageFile);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 				// TODO: localize toasts
 				Toast.makeText(getApplicationContext(), "Error creating file. Sorry :(", Toast.LENGTH_LONG).show();
 			} catch (IOException e){
 				e.printStackTrace();
 				Toast.makeText(getApplicationContext(), "Error writing file. Sorry :(", Toast.LENGTH_LONG).show();
 			}
 		} else {
 			Toast.makeText(getApplicationContext(), "Your storage seems was unplugged", Toast.LENGTH_LONG).show();
 		}
 		
 		return uri;
 	}
 	
 }
