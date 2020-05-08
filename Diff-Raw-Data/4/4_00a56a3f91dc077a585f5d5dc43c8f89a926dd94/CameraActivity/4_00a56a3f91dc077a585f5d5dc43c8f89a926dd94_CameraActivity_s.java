 package com.example.lightdetector;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.opencv.android.BaseLoaderCallback;
 import org.opencv.android.CameraBridgeViewBase;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 import org.opencv.core.Core;
 import org.opencv.core.Mat;
 import org.opencv.core.Point;
 import org.opencv.core.Scalar;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.digitallighterserver.Configuration;
 import com.example.digitallighterserver.ConnectionService;
 import com.example.digitallighterserver.ConnectionService.LocalBinder;
 import com.example.digitallighterserver.DLSApplication;
 import com.example.digitallighterserver.DeviceLocatingStrategy;
 import com.example.digitallighterserver.DeviceMapper;
 import com.example.digitallighterserver.DeviceMapperSimple;
 import com.example.digitallighterserver.DeviceMapperTree;
 import com.example.digitallighterserver.DeviceTracker;
 import com.example.digitallighterserver.MediaPlayer;
 import com.example.digitallighterserver.R;
 import com.example.digitallighterserver.ServiceObserver;
 
 public class CameraActivity extends Activity implements CvCameraViewListener2,
 		Observer, ServiceObserver {
 	private static final String MEDIA_SOURCE = Configuration.MEDIA_SOURCE;
 
 	PointCollector collector;
 
 	static int tilesX = Configuration.TILES_X;
 	static int tilesY = Configuration.TILES_Y;
 
 	TextView info;
 	BlockingQueue<HashMap<String, ArrayList<Point>>> buffer = new LinkedBlockingQueue<HashMap<String, ArrayList<Point>>>();
 
 	int fpsCounter;
 	ConnectionService mService;
 	boolean mBound = false;
 
 	DeviceLocatingStrategy dl;
 	MediaPlayer mediaPlayer = null;
 
 	private CameraBridgeViewBase mOpenCvCameraView;
 
 	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
 		@Override
 		public void onManagerConnected(int status) {
 			switch (status) {
 			case LoaderCallbackInterface.SUCCESS: {
 				Log.i("Yo", "OpenCV loaded successfully");
 
 				mOpenCvCameraView.enableView();
 				// mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
 			}
 				break;
 			default: {
 				super.onManagerConnected(status);
 			}
 				break;
 			}
 		}
 	};
 
 	/** Defines callbacks for service binding, passed to bindService() */
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// We've bound to LocalService, cast the IBinder and get
 			// LocalService instance
 			LocalBinder binder = (LocalBinder) service;
 			mService = binder.getService();
 			mBound = true;
 			mService.setObserver(CameraActivity.this);
 
 			dl = mapperFactory();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			mBound = false;
 		}
 	};
 
 	private DeviceMapper mapperFactory() {
 		return (Configuration.USE_TREE_DETECTION) ? new DeviceMapperTree(
 				mService, tilesX, tilesY, CameraActivity.this)
 				: new DeviceMapperSimple(mService, tilesX, tilesY,
 						CameraActivity.this);
 	}
 
 	public void startDetection(View v) {
		if (mediaPlayer.stop()) {
 			if (!(dl instanceof DeviceMapper)) { // repeated detection
 				dl = mapperFactory();
 				((DeviceMapper) dl).reset();
 				mediaPlayer = null;
 			}
 		} else {
 			// thread is not stopped yet
 			Toast.makeText(DLSApplication.getContext(),
 					"Sorry, playing is in progress. Try again.",
 					Toast.LENGTH_SHORT).show();
 		}
 
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (mOpenCvCameraView != null)
 			mOpenCvCameraView.disableView();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
 				mLoaderCallback);
 		Intent serviceIntent = new Intent(this, ConnectionService.class);
 		bindService(serviceIntent, mConnection,
 				Context.BIND_ADJUST_WITH_ACTIVITY);
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		if (mOpenCvCameraView != null)
 			mOpenCvCameraView.disableView();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		setContentView(R.layout.color_blob_detection_surface_view);
 
 		info = (TextView) findViewById(R.id.info_txt);
 		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
 		mOpenCvCameraView.setCvCameraViewListener(this);
 	}
 
 	@Override
 	public void onCameraViewStarted(int width, int height) {
 
 	}
 
 	@Override
 	public void onCameraViewStopped() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
 		if (dl != null) {
 			if (dl.nextFrame(inputFrame.rgba())) { // mapping is finished
 				// replace mapper with tracker
 				dl = new DeviceTracker(tilesX, tilesY, dl.getDevices());
 				// create media player which runs in separate thread
 				mediaPlayer = new MediaPlayer(tilesX, tilesY, dl, mService,
 						MEDIA_SOURCE);
 				mediaPlayer.addObserver(this);
 				mediaPlayer.play();
 			}
 		}
 
 		int middleX = inputFrame.rgba().width() / 2;
 		int middleY = inputFrame.rgba().height() / 2;
 		double[] color = inputFrame.rgba().get(middleX, middleY);
 		Log.i("BARVA", "" + color[0] + " " + color[1] + " " + color[2]);
 
 		Mat image = drawTilesGrid(inputFrame.rgba(), tilesX, tilesY);
 		if (buffer.size() > 0) {
 
 			if (buffer.size() < 20) {
 
 				for (String colorItem : buffer.peek().keySet()) {
 					for (Point tile : buffer.peek().get(colorItem)) {
 						image = drawTile(image, (int) tile.x, (int) tile.y,
 								ColorManager.getCvColor(colorItem));
 					}
 				}
 				buffer.remove();
 			} else {
 				buffer.clear();
 			}
 		}
 		return image;
 	}
 
 	/*
 	 * public void onPointCollectorUpdate(HashMap<String, ArrayList<Point>>
 	 * update) { if (buffer.size() > 20) { buffer.clear(); } buffer.add(update);
 	 * }
 	 */
 
 	public static Mat drawTilesGrid(Mat input, int tilesX, int tilesY) {
 		Mat output = new Mat();
 		input.copyTo(output);
 
 		int unit = (output.width() - 1) / tilesX;
 		for (int i = 0; i < tilesX; ++i)
 			Core.line(output, new Point(i * unit, 0), new Point(i * unit,
 					output.height()), ColorManager.getCvColor(ColorManager.RED));
 
 		unit = (output.height() - 1) / tilesY;
 		for (int i = 0; i < tilesY; ++i)
 			Core.line(output, new Point(0, i * unit), new Point(output.width(),
 					i * unit), ColorManager.getCvColor(ColorManager.RED));
 
 		return output;
 	}
 
 	private static Mat drawTile(Mat input, int x, int y, Scalar color) {
 		Mat output = new Mat(input.height(), input.width(), input.type(),
 				new Scalar(0, 0, 0));
 		input.copyTo(output);
 
 		int unitX = output.width() / tilesX;
 		int unitY = output.height() / tilesY;
 		Core.rectangle(output, new Point(unitX * x, unitY * y), new Point(unitX
 				* (x + 1), unitY * (y + 1)), color, 5);
 
 		// Core.addWeighted(input, 1.0, output, 0.5, 0, output);
 		return output;
 	}
 
 	@Override
 	public void update(Observable observable, Object data) {
 		HashMap<String, ArrayList<Point>> blobs = new HashMap<String, ArrayList<Point>>(
 				(HashMap<String, ArrayList<Point>>) data);
 		buffer.add(blobs);
 	}
 
 	@Override
 	public void onServiceDataUpdate() {
 		// When something change inside service like new device connected this
 		// function will be invoked
 	}
 }
