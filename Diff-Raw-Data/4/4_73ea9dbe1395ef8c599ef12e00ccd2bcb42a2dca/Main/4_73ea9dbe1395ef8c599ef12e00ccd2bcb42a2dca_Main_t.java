 package com.example.shooter.antistress;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.ImageFormat;
 import android.graphics.PixelFormat;
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
 
 public class Main extends Activity implements SurfaceHolder.Callback {
 	private static final String JPEG_FILE_PREFIX = "IMG_";
 	private static final String JPEG_FILE_SUFFIX = ".jpg";
 
 	private static final int PHOTO_WIDTH = 480;
 	private static final int PHOTO_HEIGHT = 720;
 
 	private SurfaceView cameraView;
 	private GifView weaponView;
 	private SurfaceHolder cameraHolder;
 	private SurfaceHolder weaponHolder;
 	private Camera camera;
 	private Button throwButton;
 	private Button saveButton;
 	private Button shareButton;
 	public Bitmap finalBitmap;
 	public Uri fileUri = Uri.EMPTY;
 
 	private CameraViewStatusCodes cameraViewStatus = CameraViewStatusCodes.WAITING;
 
 	private enum CameraViewStatusCodes {
 		ERROR, WAITING, AUTOFOCUSING, DRAWING, DRAWING_ENDED
 	};
 
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
 		weaponHolder.addCallback(weaponView);
 		weaponHolder.setFormat(PixelFormat.TRANSLUCENT);
 		weaponView.setZOrderMediaOverlay(true);
 
 		throwButton = (Button) findViewById(R.id.throwButton);
 		throwButton.setOnClickListener(myBtnOnClickListener);
 		saveButton = (Button) findViewById(R.id.saveButton);
 		saveButton.setOnClickListener(mySaveAndShareBtnOnClickListener);
 		shareButton = (Button) findViewById(R.id.shareButton);
 		shareButton.setOnClickListener(mySaveAndShareBtnOnClickListener);
 
 		Log.d("watch", "onCreate");
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (cameraViewStatus == CameraViewStatusCodes.WAITING) {
 			camera.stopPreview();
 		}
 		camera.release();
 		Log.d("watch", "onPause");
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// TODO onpause on resume causes saving and loading image
 		// (onSaveInstanceState)
 		camera = Camera.open();
 		Log.d("watch", "onResume");
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.d("watch", "SurfaceDestroyed");
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		Log.d("watch", "SurfaceChanged");
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		setCameraParameters(camera);
 		if (cameraViewStatus == CameraViewStatusCodes.WAITING) {
 			camera.startPreview();
 		}
 		Log.d("watch", "SurfaceCreated");
 	}
 
 	OnClickListener myBtnOnClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			Log.d("watch", "onClick " + cameraViewStatus.toString());
 			switch (cameraViewStatus) {
 			case WAITING: {
 				cameraViewStatus = CameraViewStatusCodes.AUTOFOCUSING;
 				throwButton.setVisibility(View.INVISIBLE);
 				camera.autoFocus(myAutoFocusCallback);
 			}
 				break;
 			case DRAWING_ENDED: {
 				weaponView.clear();
 				fileUri = Uri.EMPTY;
 				finalBitmap.recycle();
 				saveButton.setVisibility(View.INVISIBLE);
 				shareButton.setVisibility(View.INVISIBLE);
 				throwButton.setText(getString(R.string.throw_button_caption));
 
 				cameraViewStatus = CameraViewStatusCodes.WAITING;
 				setCameraParameters(camera);
 				camera.startPreview();
 			}
 				break;
 			case ERROR: {
 				Toast.makeText(getApplicationContext(),
 						"Something wrong with the camera", Toast.LENGTH_LONG)
 						.show();
 			}
 				break;
 			default: {
 				Toast.makeText(getApplicationContext(),
 						"Have no idea what this is :(", Toast.LENGTH_LONG)
 						.show();
 			}
 				break;
 			}
 		}
 	};
 
 	OnClickListener mySaveAndShareBtnOnClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			if (v.getId() == saveButton.getId()) {
 				fileUri = saveFinalImage();
 				if (fileUri != Uri.EMPTY) {
 					saveButton.setVisibility(View.INVISIBLE);
 					Toast.makeText(getApplicationContext(), "Saving succeful",
 							Toast.LENGTH_SHORT).show();
 				}
 			} else {
 				if (fileUri == Uri.EMPTY) {
 					onClick(saveButton);
 				}
 				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 				sharingIntent.setType("image/jpeg");
 				sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
 				startActivity(Intent.createChooser(sharingIntent,
 						getString(R.string.title_share_camera)));
 				shareButton.setVisibility(View.INVISIBLE);
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
 
 			try {
 				Bitmap fotoBitmap = BitmapFactory.decodeByteArray(data, 0,
 						data.length);
 
 				/*
 				 * int fotoBitmapWidth = fotoBitmap.getWidth(); int
 				 * fotoBitmapHeight = fotoBitmap.getHeight(); File tmpFile = new
 				 * File(getExternalCacheDir().getPath() + "tmpImage.dat");
 				 * tmpFile.getParentFile().mkdirs(); RandomAccessFile
 				 * randomAccessFile = new RandomAccessFile( tmpFile, "rw");
 				 * FileChannel fileChannel = randomAccessFile.getChannel();
 				 * MappedByteBuffer map = fileChannel.map(MapMode.READ_WRITE, 0,
 				 * fotoBitmapWidth * fotoBitmapHeight * 4);
 				 * fotoBitmap.copyPixelsToBuffer(map); fotoBitmap.recycle();
 				 * 
 				 * finalBitmap = Bitmap.createBitmap(fotoBitmapWidth,
 				 * fotoBitmapHeight, Config.ARGB_8888); map.position(0);
 				 * finalBitmap.copyPixelsFromBuffer(map); fileChannel.close();
 				 * randomAccessFile.close();
 				 */
 
 				if (!fotoBitmap.isMutable()) {
 					finalBitmap = Bitmap.createScaledBitmap(fotoBitmap,
 							PHOTO_WIDTH, PHOTO_HEIGHT, false);
 					fotoBitmap.recycle();
 				}
 
 				Canvas finalCanvas = new Canvas(finalBitmap);
 				weaponView.getFinalBitmap(finalCanvas);
 				finalCanvas.save();
 
 				/*
 				 * File tmpFile = new File(getExternalCacheDir().getPath() +
 				 * "/tmpImage.jpg"); tmpFile.getParentFile().mkdirs();
 				 * FileOutputStream fos = new FileOutputStream(tmpFile);
 				 * finalBitmap.compress(CompressFormat.JPEG, 100, fos);
 				 * fos.flush(); fos.close();
 				 * 
 				 * Intent intent = new Intent();
 				 * intent.setClass(getApplicationContext(), Share.class);
 				 * intent.putExtra("finalImagePath", tmpFile.getPath());
 				 * startActivity(intent);
 				 */
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 	};
 
 	private void Throw() {
 		weaponView.setGif(R.drawable.apple);
 		weaponView.play();
 	}
 
 	private Uri saveFinalImage() {
 		Uri uri = Uri.EMPTY;
 
 		if (Environment.getExternalStorageState().equals(
 				Environment.MEDIA_MOUNTED)) {
 			try {
 				File saveDir = new File(Environment
 						.getExternalStoragePublicDirectory(
 								Environment.DIRECTORY_PICTURES).getPath()
 						+ "/" + getString(R.string.app_name) + "/");
 				if (!saveDir.exists()) {
 					saveDir.mkdirs();
 				}
 				File imageFile = new File(saveDir,
 						JPEG_FILE_PREFIX
 								+ new SimpleDateFormat("yyyyMMdd_HHmmss")
 										.format(new Date()) + JPEG_FILE_SUFFIX);
 
 				FileOutputStream fos = new FileOutputStream(imageFile);
 				finalBitmap.compress(CompressFormat.JPEG, 95, fos);
 				uri = Uri.fromFile(imageFile);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 				// TODO: localize toasts
 				Toast.makeText(getApplicationContext(),
 						"Error creating file. Sorry :(", Toast.LENGTH_LONG)
 						.show();
 			} catch (Exception e) {
 				e.printStackTrace();
 				Toast.makeText(getApplicationContext(),
 						"Error writing file. Sorry :(", Toast.LENGTH_LONG)
 						.show();
 			}
 		} else {
 			Toast.makeText(getApplicationContext(),
 					"Your storage seems was unplugged", Toast.LENGTH_LONG)
 					.show();
 		}
 
 		return uri;
 	}
 	
 	private Camera getCamera(){
 		Camera cam = null;
 		try {
 			cam = Camera.open();
 		} catch (Exception e) {
 			Log.e("Openin camera", e.getMessage());
			Toast.makeText(getApplicationContext(), "Error opening camera. May be it unavailible or doesn't exist", Toast.LENGTH_LONG).show();
 			finish();			
 		}
 		return cam;
 	}
 
 	private void setCameraParameters(Camera cam) {
 		if(cam == null)
 			cam = getCamera();		
 		Camera.Parameters parameters = cam.getParameters();
 		parameters.setPictureFormat(ImageFormat.JPEG);
 		parameters.setRotation(90);
 		cam.setParameters(parameters);
 		cam.setDisplayOrientation(90);
 
 		try {
 			camera.setPreviewDisplay(cameraHolder);
 		} catch (IOException e) {
 			e.printStackTrace();
 			cameraViewStatus = CameraViewStatusCodes.ERROR;
 			camera.release();
 		}
 	}
 
 }
