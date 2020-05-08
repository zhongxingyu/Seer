 package com.adudziec.defects;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.opencv.android.BaseLoaderCallback;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 import org.opencv.android.Utils;
 import org.opencv.core.Mat;
 import org.opencv.imgproc.Imgproc;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Camera.Size;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.SubMenu;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.WindowManager;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 public class MainActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
 	private MyCameraView cameraView;
 	private ImageView imageView;
 	private ViewFlipper viewFlipper;
 	private MenuItem[] resolutionMenuItems;
 
 	private MenuItem            mItemPreviewRGBA;
 	private MenuItem            mItemPreviewDeuteranope;	// problem with red
 	private MenuItem            mItemPreviewProtanope;		// problem with green
 	private MenuItem            mItemPreviewTritanope;		// problem with blue
 	private SubMenu	            mItemSource;				// set the Source
 	private Mat                 mRgba;
 	private Mat                 galleryOriginal;
 	private Mat                 galleryEffect;
 	private Bitmap				galleryBitmap = null;
 	private ImageProcessor		imageProcessor;
 	private Boolean				cameraSource = true;
 	public static ViewModes     viewMode = ViewModes.VIEW_MODE_RGBA;
 
 	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
 		@Override
 		public void onManagerConnected(int status) {
 			switch (status) {
 			case LoaderCallbackInterface.SUCCESS:
 			{
 				cameraView.enableView();
 
 			} break;
 			default:
 			{
 				super.onManagerConnected(status);
 			} break;
 			}
 		}
 	};
 
 	public MainActivity() {
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		imageProcessor = new ImageProcessor();
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		setContentView(R.layout.main_view);
 
 		cameraView = (MyCameraView) findViewById(R.id.main_camera_view);
 		cameraView.setVisibility(SurfaceView.VISIBLE);
 		cameraView.setOnTouchListener(MainActivity.this);
 		cameraView.setCvCameraViewListener(this);
 
 		imageView = (ImageView) findViewById(R.id.imageView1);
 		imageView.setVisibility(SurfaceView.INVISIBLE);
 		imageView.setOnTouchListener(MainActivity.this);
 
 		viewFlipper=(ViewFlipper)findViewById(R.id.ViewFlipper01);
 	}
 
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		if (cameraView != null)
 			cameraView.disableView();
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		if (cameraView != null)
 			cameraView.disableView();
 	}
 
 	public void onCameraViewStarted(int width, int height) {
 		mRgba = new Mat();
 		galleryOriginal = new Mat();
 		galleryEffect = new Mat();
 	}
 
 	public void onCameraViewStopped() {
 	}
 
 	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
 		mRgba = inputFrame.rgba();
 		switch (viewMode)
 		{
 		case VIEW_MODE_RGBA:
 			break;
 		case VIEW_MODE_DEUTERANOPE:
 			imageProcessor.deuteranope(mRgba);
 			break;
 		case VIEW_MODE_PROTANOPE:
 			imageProcessor.protanope(mRgba);
 			break;
 		case VIEW_MODE_TRITANOPE:
 			imageProcessor.tritanope(mRgba);
 			break;
 		default:
 			break;
 		}
 		return mRgba;
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		mItemPreviewRGBA  = menu.add("Normal");
 		mItemPreviewDeuteranope = menu.add("Deuteranope");
 		mItemPreviewProtanope = menu.add("Protanope");
 		mItemPreviewTritanope = menu.add("Tritanope");
 		mItemSource = menu.addSubMenu("Source");
 
 		resolutionMenuItems = new MenuItem[2];
 		resolutionMenuItems[0] = mItemSource.add(1, 0, Menu.NONE, "Gallery");
 		resolutionMenuItems[1] = mItemSource.add(1, 1, Menu.NONE, "Camera");
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item == mItemPreviewRGBA){
 			viewMode = ViewModes.VIEW_MODE_RGBA;
 			imageProcessor.setup(viewMode);
 			if (!cameraSource && galleryBitmap != null && !galleryEffect.empty()){
 				processImage();
 			}
 		}
 		else if (item == mItemPreviewDeuteranope){
 			viewMode = ViewModes.VIEW_MODE_DEUTERANOPE;
 			imageProcessor.setup(viewMode);
 			if (!cameraSource && galleryBitmap != null && !galleryEffect.empty()){
 				processImage();
 			}
 		}
 		else if (item == mItemPreviewProtanope){
 			viewMode = ViewModes.VIEW_MODE_PROTANOPE;
 			imageProcessor.setup(viewMode);
 			if (!cameraSource && galleryBitmap != null && !galleryEffect.empty()){
 				processImage();
 			}
 		}
 		else if (item == mItemPreviewTritanope){
 			viewMode = ViewModes.VIEW_MODE_TRITANOPE;
 			imageProcessor.setup(viewMode);
 			if (!cameraSource && galleryBitmap != null && !galleryEffect.empty()){
 				processImage();
 			}
 		}
 		else if (item.getGroupId() == 1){
 			int id = item.getItemId();
 			if (id == 0){
 				// Image from gallery

				Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, 0);
 				// Switch view
 				if (cameraSource){
 					cameraSource = false;
 					imageView.setVisibility(SurfaceView.VISIBLE);
 					cameraView.setVisibility(SurfaceView.INVISIBLE);
 					viewFlipper.showNext();
 				}
 			} else {
 				// Camera source
 				// Switch view
 				if (!cameraSource){
 					cameraSource = true;
 					cameraView.setVisibility(SurfaceView.VISIBLE);
 					imageView.setVisibility(SurfaceView.INVISIBLE);
 					viewFlipper.showPrevious();
 				}
 
 			}
 		}
 
 
 		return true;
 	}
 
 
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) { // save on touch
 		Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
 		// check if memory card is present
 		if(isSDPresent)
 		{
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
 			String currentDateandTime = sdf.format(new Date());
 			// create directory 
 			File folder = new File(Environment.getExternalStorageDirectory().toString()+"/omg_my_eyes/");
 			folder.mkdirs();
 			// define filename
 			String fileName = Environment.getExternalStorageDirectory().getPath() +	"/omg_my_eyes/img" + currentDateandTime + ".jpg";
 			Bitmap bmp = null;
 			if (cameraSource){
 				bmp = Bitmap.createBitmap(mRgba.cols(),mRgba.rows(),Bitmap.Config.ARGB_8888);
 				// convert cv::Mat to Bitmap
 				Utils.matToBitmap(mRgba, bmp, true);
 			} else {
 				bmp = galleryBitmap;
 			}
 			try {
 				FileOutputStream out = new FileOutputStream(fileName);
 				// try to save the image
 				Boolean result = bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
 				// print user message
 				if (result){
 					Toast.makeText(this, "Image saved in: memory_card/omg_my_eyes" , Toast.LENGTH_SHORT).show();
 				}
 				else
 				{
 					Toast.makeText(this, "Problem while saving", Toast.LENGTH_SHORT).show();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			Toast.makeText(this, "Can't touch this - no memory card.", Toast.LENGTH_SHORT).show();
 		}
 		return false;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == RESULT_OK){
 			Uri targetUri = data.getData();
 
 			try {
 				galleryBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
 				Utils.bitmapToMat(galleryBitmap, galleryOriginal);
 				if(galleryOriginal.empty()) {
 					Toast.makeText(this, "Cannot open image", Toast.LENGTH_LONG).show();
 				} else {
 					processImage();
 				}
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 	private void processImage(){
 		galleryEffect = imageProcessor.process(galleryOriginal, viewMode);
 		galleryBitmap = Bitmap.createBitmap(galleryEffect.cols(), galleryEffect.rows(), Bitmap.Config.ARGB_8888);
 		Utils.matToBitmap(galleryEffect, galleryBitmap);
 		imageView.setImageBitmap(galleryBitmap);
 	}
 
 }
