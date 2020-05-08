 package esposito.fall_detection;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 
 import android.location.*;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
 import android.util.Log;
 import android.view.View;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.RectF;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.CoreConnectionPNames;
 //import org.openintents.sensorsimulator.hardware.Sensor;
 //import org.openintents.sensorsimulator.hardware.SensorEvent;
 //import org.openintents.sensorsimulator.hardware.SensorEventListener;
 //import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;
 
 public class FallDetection extends Activity {
 
 	static final int PROGRESS_DIALOG = 0;
 	ProgressThread progressThread;
 	ProgressDialog progressDialog;
 	private SensorManager mSensorManager;
 	private GraphView mGraphView;
 	private LocationManager locationManager;
 	private LocationUpdateHandler locationUpdateHandler;
 	long RssTime = 0;
 	float RssVal = 0;
 	long VveTime = 0;
 	float VveVal = 0;
 	boolean fall_detected = false;
 	boolean handling_fall = false;
 	public double lat;
 	public double lon;
 
 	private class GraphView extends View implements SensorEventListener {
 		private Bitmap mBitmap;
 		private Paint mPaint = new Paint();
 		private Canvas mCanvas = new Canvas();
 		private Path mPath = new Path();
 		private RectF mRect = new RectF();
 		private float mLastValues[] = new float[3];
 		private float mScale[] = new float[3];
 		private int mColors[] = new int[3 * 2];
 		private float mLastX;
 		private final float RssTreshold = 2.8f;
 		private float mRssValues[] = new float[256];
 		private int mRssCount = 0;
 		private int mRssIndex = 0;
 		private long RssStartTime = 0;
 		private final float VveWindow = 0.6f;
 		private final float VveTreshold = -0.7f;
 		private final int OriOffset = 1000;
 		private final int OriWindow = 2000;
 		private long OriStartTime = 0;
 		private final float OriTreshold = 60;
 		private final float OriConstraint = 0.75f;
 		private float OriValues[] = new float[256];
 		private int ori_index = 0;
 		private float mYOffset;
 		private float mXOffset;
 		private float mMaxX;
 		private float mSpeed = 1.0f;
 		private float mWidth;
 		private float mHeight;
 
 		public GraphView(Context context) {
 			super(context);
 			mColors[0] = Color.argb(192, 255, 64, 64);
 			mColors[1] = Color.argb(192, 64, 128, 64);
 			mColors[2] = Color.argb(192, 64, 64, 255);
 			mColors[3] = Color.argb(192, 64, 255, 255);
 			mColors[4] = Color.argb(192, 128, 64, 128);
 			mColors[5] = Color.argb(192, 255, 255, 64);
 
 			mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
 			mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
 			mPath.arcTo(mRect, 0, 180);
 		}
 
 		@Override
 		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
 			mCanvas.setBitmap(mBitmap);
 			mCanvas.drawColor(0xFFFFFFFF);
 			mYOffset = h / 3.0f;
 			mXOffset = 17;
 			mScale[0] = (float) -(mYOffset * (1.0f / Math.sqrt(Math.pow(
 					SensorManager.STANDARD_GRAVITY * 4, 2) * 3)));
 			mScale[1] = (float) -(mYOffset * (1.0f / ((Math.sqrt(Math.pow(
 					SensorManager.STANDARD_GRAVITY * 4, 2) * 3) - SensorManager.STANDARD_GRAVITY) * VveWindow)));
 			mScale[2] = -(mYOffset * (1.0f / 90));
 			mWidth = w;
 			mHeight = h;
 			if (mWidth < mHeight) {
 				mMaxX = w;
 			} else {
 				mMaxX = w - 50;
 			}
 			mLastX = mMaxX;
 			super.onSizeChanged(w, h, oldw, oldh);
 		}
 
 		@Override
 		protected void onDraw(Canvas canvas) {
 			synchronized (this) {
 				if (mBitmap != null) {
 					final Paint paint = mPaint;
 
 					if (mLastX >= mMaxX) {
 						mLastX = mXOffset;
 						final Canvas cavas = mCanvas;
 						final float yoffset = mYOffset;
 						final float maxx = mMaxX;
 						paint.setColor(0xFFAAAAAA);
 						cavas.drawColor(0xFFFFFFFF);
 						// Fal Impact graph
 						cavas.drawText("Fall Impact", mXOffset, 25, paint);
 						cavas.drawLine(mXOffset, yoffset, maxx, yoffset, paint);
 						cavas.drawLine(mXOffset, yoffset, mXOffset, 28, paint);
 						cavas.drawText("0", 7, yoffset, paint);
 						cavas.drawText("2", 7, yoffset + 2
 								* SensorManager.STANDARD_GRAVITY * mScale[0],
 								paint);
 						cavas.drawText("4", 7, yoffset + 4
 								* SensorManager.STANDARD_GRAVITY * mScale[0],
 								paint);
 						// Vertical Velocity graph
 						cavas.drawText("Vertical Velocity", mXOffset, yoffset
 								* (3.0f / 2) + SensorManager.STANDARD_GRAVITY
 								* mScale[1] - 15, paint);
 						cavas.drawLine(mXOffset, yoffset * (3.0f / 2), maxx,
 								yoffset * (3.0f / 2), paint);
 						cavas.drawLine(mXOffset, yoffset * (3.0f / 2)
 								- SensorManager.STANDARD_GRAVITY * mScale[1],
 								mXOffset, yoffset * (3.0f / 2)
 										+ SensorManager.STANDARD_GRAVITY
 										* mScale[1] - 12, paint);
 						cavas.drawText("-1", 4, yoffset * (3.0f / 2)
 								- SensorManager.STANDARD_GRAVITY * mScale[1],
 								paint);
 						cavas.drawText("0", 7, yoffset * (3.0f / 2), paint);
 						cavas.drawText("1", 7, yoffset * (3.0f / 2)
 								+ SensorManager.STANDARD_GRAVITY * mScale[1],
 								paint);
 						// Posture graph
 						cavas.drawText("Posture", mXOffset, yoffset * 3 + 90
 								* mScale[2] - 18, paint);
 						cavas.drawLine(mXOffset, yoffset * 3, maxx,
 								yoffset * 3, paint);
 						cavas.drawLine(mXOffset, yoffset * 3, mXOffset, yoffset
 								* 3 + 90 * mScale[2] - 15, paint);
 						cavas.drawText("0", 7, yoffset * 3, paint);
 						cavas.drawText("45", 2, yoffset * 3 + 45 * mScale[2],
 								paint);
 						cavas.drawText("90", 2, yoffset * 3 + 90 * mScale[2],
 								paint);
 						paint.setColor(0xFFFF0000);
 						float ytresholdRss = yoffset + RssTreshold
 								* SensorManager.STANDARD_GRAVITY * mScale[0];
 						cavas.drawLine(mXOffset, ytresholdRss, maxx,
 								ytresholdRss, paint);
 						float ytresholdVve = yoffset * (3.0f / 2) + VveTreshold
 								* SensorManager.STANDARD_GRAVITY * mScale[1];
 						cavas.drawLine(mXOffset, ytresholdVve, maxx,
 								ytresholdVve, paint);
 						float ytresholdOri = yoffset * 3 + OriTreshold
 								* mScale[2];
 						cavas.drawLine(mXOffset, ytresholdOri, maxx,
 								ytresholdOri, paint);
 					}
 					canvas.drawBitmap(mBitmap, 0, 0, null);
 				}
 			}
 		}
 
 		public void onSensorChanged(SensorEvent event) {
 			synchronized (this) {
 				if (mBitmap != null) {
 					if (fall_detected) {
 						if (!handling_fall) {
 							handling_fall = true;
 							handle_fall();
 						}
 					} else {
 						final Canvas canvas = mCanvas;
 						final Paint paint = mPaint;
 						Date date = new Date();
 						if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 							float deltaX = mSpeed;
 							float newX = mLastX + deltaX;
 							// Calculalte RSS
 							float rss = (float) Math.sqrt(Math.pow(
 									event.values[0], 2)
 									+ Math.pow(event.values[1], 2)
 									+ Math.pow(event.values[2], 2));
 							if (rss > RssTreshold
 									* SensorManager.STANDARD_GRAVITY
 									&& RssTime == 0) {
 								if (VveTime == 0) {
 									RssVal = rss;
 									RssTime = date.getTime();
 								}
 								paint.setColor(0xFF0000FF);
 								canvas.drawText("v", newX - 3, mYOffset + 4
 										* SensorManager.STANDARD_GRAVITY
 										* mScale[0], paint);
 							}
 							float draw_rss = mYOffset + rss * mScale[0];
 							paint.setColor(mColors[0]);
 							canvas.drawLine(mLastX, mLastValues[0], newX,
 									draw_rss, paint);
 							mLastValues[0] = draw_rss;
 							// Calculate Vve numeric integral over RSS
 							if (RssStartTime == 0) {
 								RssStartTime = date.getTime();
 								mRssCount++;
 							} else if (date.getTime() - RssStartTime <= VveWindow * 1000
 									&& mRssCount < mRssValues.length) {
 								mRssIndex = mRssCount++;
 							} else {
 								mRssIndex = ++mRssIndex % mRssCount;
 							}
 							mRssValues[mRssIndex] = rss
 									- SensorManager.STANDARD_GRAVITY;
 							float vve = 0;
 							for (int i = 0; i < mRssCount; i++) {
 								vve += mRssValues[i];
 							}
 							vve = (vve * VveWindow) / mRssCount;
 							if (vve < VveTreshold
 									* SensorManager.STANDARD_GRAVITY
 									&& VveTime == 0) {
 								VveVal = vve;
 								VveTime = date.getTime();
 								paint.setColor(0xFF0000FF);
 								canvas.drawText("^", newX - 3, mYOffset
 										* (3.0f / 2)
 										- SensorManager.STANDARD_GRAVITY
 										* mScale[1] - 10, paint);
 							}
 							vve = mYOffset * (3.0f / 2) + vve * mScale[1];
 							paint.setColor(mColors[1]);
 							canvas.drawLine(mLastX, mLastValues[1], newX, vve,
 									paint);
 							mLastValues[1] = vve;
 							// Increment graph position
 							mLastX += mSpeed;
 						} else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
 							// Calculate orientation
 							float deltaX = mSpeed;
 							float newX = mLastX + deltaX;
 							float ori = (90 - Math.abs(event.values[1]));
 							float draw_ori = mYOffset * 3 + ori * mScale[2];
 							paint.setColor(mColors[2]);
 							canvas.drawLine(mLastX, mLastValues[2], newX,
 									draw_ori, paint);
 							mLastValues[2] = draw_ori;
 							// Calculate Position feature
 							long wait_interval = (RssTime != 0 ? date.getTime()
 									- RssTime : (VveTime != 0 ? date.getTime()
 									- VveTime : 0));
 							if (wait_interval >= OriOffset) {
 								if (OriStartTime == 0)
 									OriStartTime = date.getTime();
 								else if (date.getTime() - OriStartTime < OriWindow) {
 									OriValues[ori_index++] = ori;
 									canvas.drawLine(mLastX, mYOffset * 3 + 90
 											* mScale[2] - 2, newX, mYOffset * 3
 											+ 90 * mScale[2] - 2, paint);
 								} else {
 									int count = 0;
 									for (int i = 0; i < ori_index; i++) {
 										if (OriValues[i] > OriTreshold)
 											count++;
 									}
 									if (count / ori_index >= OriConstraint) {
 										// A fall has been detected => Time to
 										// take
 										// action!!!
 										paint.setColor(0xFF0000FF);
 										canvas.drawText("v", newX - 4, mYOffset
 												* 3 + 90 * mScale[2] - 2, paint);
 										fall_detected = true;
 									}
 									// Reset variables for next fall
 									OriStartTime = ori_index = 0;
 								}
 							}
 						}
 						invalidate();
 					}
 				}
 			}
 		}
 
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		}
 	}
 
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case PROGRESS_DIALOG:
 			progressDialog = new ProgressDialog(FallDetection.this);
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			progressDialog.setMessage("Sending fall notification...");
 			progressDialog.setTitle("A fall was Detected!");
 			progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel",
 					buttonListener);
 			progressDialog.setMax(10);
 			return progressDialog;
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		switch (id) {
 		case PROGRESS_DIALOG:
 			progressDialog.setProgress(0);
 			progressThread = new ProgressThread(handler);
 			progressThread.start();
 		}
 	}
 
 	// Define the Handler that receives messages from the thread and update the
 	// progress
 	final Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			int total = msg.arg1;
 			if (total < 11) {
 				progressDialog.setProgress(total);
 			} else if (total == 11) {
 				progressDialog.setProgress(0);
 				dismissDialog(PROGRESS_DIALOG);
 				progressThread.setState(ProgressThread.STATE_DONE);
 				postDetectedFall(); // report the fall
 			}
 		}
 	};
 
 	/** Nested class that performs progress calculations (counting) */
 	class ProgressThread extends Thread {
 		Handler mHandler;
 		final static int STATE_DONE = 0;
 		final static int STATE_RUNNING = 1;
 		int mState;
 		int total;
 
 		ProgressThread(Handler h) {
 			mHandler = h;
 		}
 
 		public void run() {
 			mState = STATE_RUNNING;
 			total = 0;
 			while (mState == STATE_RUNNING) {
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					Log.e("ERROR", "Thread Interrupted");
 				}
 				Message msg = mHandler.obtainMessage();
 				msg.arg1 = total;
 				mHandler.sendMessage(msg);
 				total++;
 			}
 		}
 
 		/*
 		 * sets the current state for the thread, used to stop the thread
 		 */
 		public void setState(int state) {
 			mState = state;
 		}
 	}
 
 	// Create an anonymous implementation of OnClickListener
 	private OnClickListener buttonListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			progressThread.setState(ProgressThread.STATE_DONE);
 			// reset recorded values
 			reset_fall_values();
 		}
 	};
 
 	// Displays a dialog that a fall has been detected.
 	public void handle_fall() {
 		// Start requesting location
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
 				0, new LocationUpdateHandler());
 
 		// Uncomment to create a location update for demonstration purposes
 		// Location location = new Location(LocationManager.GPS_PROVIDER);
 		// location.setLatitude(53.240407);
 		// location.setLongitude(6.535999);
 		// location.setTime((new Date()).getTime());
 		// locationUpdateHandler.onLocationChanged(location);
 
 		showDialog(PROGRESS_DIALOG);
 	}
 
 	// Post fall details to a REST web service
 	private void postDetectedFall() {
 		// Making an HTTP post request and reading out the response
 		HttpClient httpclient = new DefaultHttpClient();
 		httpclient.getParams().setParameter(
 				CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
 		HttpPost httppost = new HttpPost("http://195.240.74.93:3000/falls");
 		// set post data
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 		nameValuePairs.add(new BasicNameValuePair("datetime",
 				(VveTime != 0 ? Long.toString(VveTime) : (RssTime != 0 ? Long
 						.toString(RssTime) : ""))));
 		nameValuePairs.add(new BasicNameValuePair("rss", (RssVal == 0 ? ""
 				: Float.toString(RssVal))));
 		nameValuePairs.add(new BasicNameValuePair("vve", (VveVal == 0 ? ""
 				: Float.toString(VveVal))));
 		nameValuePairs.add(new BasicNameValuePair("lat", Double.toString(lat)));
 		nameValuePairs.add(new BasicNameValuePair("lon", Double.toString(lon)));
 		try {
 			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 		} catch (UnsupportedEncodingException e) {
 			// notify failure
 		}
 		HttpResponse response;
 		String response_content = "";
 		try {
 			response = httpclient.execute(httppost);
 			if (response.getStatusLine().getStatusCode() == 200) {
 				HttpEntity entity = response.getEntity();
 				if (entity != null) {
 					InputStream instream = entity.getContent();
 					BufferedReader reader = new BufferedReader(
 							new InputStreamReader(instream));
 					StringBuilder builder = new StringBuilder();
 					String line = null;
 					while ((line = reader.readLine()) != null) {
 						builder.append(line + "\n");
 					}
 					response_content = builder.toString();
 				}
 			}
 		} catch (Exception e) {
 			// notify failure
 		}
 		if (response_content == "fall_created") {
 			// notify success
 		} else {
 			// notify failure
 		}
 		// reset recorded values
 		reset_fall_values();
 	}
 
 	public void reset_fall_values() {
 		// reset recorded values
 		RssVal = VveVal = VveTime = RssTime = 0;
 		fall_detected = false;
 		handling_fall = false;
 	}
 
 	/**
 	 * Initialization of the Activity after it is first created. Must at least
 	 * call {@link android.app.Activity#setContentView setContentView()} to
 	 * describe what is to be displayed in the screen.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// Be sure to call the super class.
 		super.onCreate(savedInstanceState);
 		mGraphView = new GraphView(this);
 		setContentView(mGraphView);
 
 		// real code
 		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		// simulation code
 //		mSensorManager = SensorManagerSimulator.getSystemService(this,
 //				SENSOR_SERVICE);
 //		mSensorManager.connectSimulator();
 
 		// initialize location manager
 		locationUpdateHandler = new LocationUpdateHandler();
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		//set the screen in landscape mode
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 	}
 
 	public class LocationUpdateHandler implements LocationListener {
 
 		public void onLocationChanged(Location loc) {
 			lat = loc.getLatitude();
 			lon = loc.getLongitude();
 		}
 
 		public void onProviderDisabled(String provider) {
 		}
 
 		public void onProviderEnabled(String provider) {
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mSensorManager.registerListener(mGraphView,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_FASTEST);
 		mSensorManager.registerListener(mGraphView,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
 				SensorManager.SENSOR_DELAY_FASTEST);
 		mSensorManager.registerListener(mGraphView,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
 				SensorManager.SENSOR_DELAY_FASTEST);
 	}
 
 	@Override
 	protected void onStop() {
 		mSensorManager.unregisterListener(mGraphView);
 		super.onStop();
 	}
 }
