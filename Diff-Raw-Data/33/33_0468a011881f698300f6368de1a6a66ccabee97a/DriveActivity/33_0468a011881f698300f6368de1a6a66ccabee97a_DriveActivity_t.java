 package com.sphericalchickens.dragbot;
 
 import orbotix.robot.IGameControl;
 import orbotix.robot.IOrbotixServiceCallback;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.LinearGradient;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Shader;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.PowerManager;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.Interpolator;
 import android.view.animation.OvershootInterpolator;
 import android.widget.FrameLayout;
 import android.widget.Toast;
 
 public class DriveActivity extends Activity {
 	private static final String LOG_TAG = "DriveActivity";
 	private final Boolean DEBUG = true;
 	
 	// If true, do not attempt to connect to the robot service.
 	private final static boolean EMULATOR = true;
 
 	private IGameControl gameControl;
 
 	private static final int PREPARE_ROBOT_FINISHED = 1;
 	private static final int ROBOT_LOST_CONTROL = 2;
 	private static final float SPEED_THRESHOLD = (float) 0.05;
 	private static final float DEAD_ZONE = (float) 0.05;
 	private static final long COMMAND_DELAY = 250; // delay between sending
 													// commands to the robot
 	private long lastCommandTime = 0;
 	private float speedLeft = 0;
 	private float speedRight = 0;
 
 	private float new_leftSpeed;
 	private float new_rightSpeed;
 
 	private boolean bowling = false;
 
 	private int max_dim;
 	private int center_y;
 	private int center_x;
 	private int icon_offset_y;
 	private int icon_offset_x;
 	private double angle;
 	private double velocity;
 
 	private static final int DIALOG_SETUP_ID = 1;
 
 	private static final String DEBUG_TAG = "PlayArea";
 	private PowerManager.WakeLock screenWakeLock;
 
 	private void calculateVelocityVector(float x_offset, float y_offset) {
 		// No scaling if bowling
 		if (!bowling) {
 			if (Math.abs(x_offset) > max_dim) {
 				if (x_offset < -max_dim) {
 					x_offset = -max_dim;
 				} else {
 					x_offset = max_dim;
 				}
 			}
 
 			if (Math.abs(y_offset) > max_dim) {
 				if (y_offset < -max_dim) {
 					y_offset = -max_dim;
 				} else {
 					y_offset = max_dim;
 				}
 			}
 		}
 
 		float scaled_dx = x_offset;
 		float scaled_dy = y_offset;
 		
 		if (!bowling) {
 			scaled_dx = (x_offset - (center_x - icon_offset_x))
 					/ (center_x - icon_offset_x);
 			scaled_dy = (y_offset - (center_y - icon_offset_y))
 					/ (center_y - icon_offset_y);
 
 			if (scaled_dy < -1.0) {
 				scaled_dy = (float) -1.0;
 			}
 
 			if (scaled_dy > 1.0) {
 				scaled_dy = (float) 1.0;
 			}
 
 			if (scaled_dx < -1.0) {
 				scaled_dx = (float) -1.0;
 			}
 
 			if (scaled_dx > 1.0) {
 				scaled_dx = (float) 1.0;
 			}
 		}
 
 		Log.d(DEBUG_TAG, "Scale: " + scaled_dx + ", " + scaled_dy);
 		angle = Math.atan2(scaled_dy, scaled_dx);
 		angle = angle + (Math.PI / 4.0) + (Math.PI / 2.0);
 		Log.d(DEBUG_TAG, "Angle: " + angle);
 
 		double angle_degrees = angle / Math.PI * 180;
 		Log.d(DEBUG_TAG, "Angle degrees: " + angle_degrees);
 
 		velocity = Math.sqrt(scaled_dx * scaled_dx + scaled_dy * scaled_dy);
 		if (!bowling) {
 			velocity *= 1.25;
 		}
 		Log.d(DEBUG_TAG, "Velocity: " + velocity);
 
 		new_leftSpeed = (float) (velocity * Math.sin(angle));
 		new_rightSpeed = (float) (velocity * Math.cos(angle));
 
 		// Scale the motor speeds
 		double scale = 1.0;
 
 		if ((new_leftSpeed > new_rightSpeed) && Math.abs(new_leftSpeed) > 1.0) {
 			scale = 1.0 / Math.abs(new_leftSpeed);
 		}
 
 		if ((new_rightSpeed >= new_leftSpeed) && Math.abs(new_rightSpeed) > 1.0) {
 			scale = 1.0 / Math.abs(new_rightSpeed);
 		}
 
 		new_leftSpeed *= scale;
 		new_rightSpeed *= scale;
 
 		Log.d(DEBUG_TAG, "Speed: " + new_leftSpeed + ", " + new_rightSpeed);
 		// Toast.makeText(DragController.this, "" + speed_x + ", " + speed_y,
 		// Toast.LENGTH_SHORT).show();
 		// If we are bowling, set this in the onfling method.
 		setSpeed(new_leftSpeed, new_rightSpeed);
 	}
 
 	private class PlayAreaView extends View {
 
 		private GestureDetector gestures;
 		private Matrix translate;
 		private Bitmap droid;
 
 		public PlayAreaView(Context context) {
 			super(context);
 			translate = new Matrix();
 			gestures = new GestureDetector(DriveActivity.this,
 					new GestureListener(this));
 			droid = BitmapFactory.decodeResource(getResources(),
 					R.drawable.droid_g);
 		}
 
 		@Override
 		protected void onSizeChanged(int width, int height, int oldw, int oldh) {
 			// TODO Auto-generated method stub
 			super.onSizeChanged(width, height, oldw, oldh);
 			center_y = height / 2;
 			center_x = width / 2;
 			icon_offset_y = droid.getHeight() / 2;
 			icon_offset_x = droid.getWidth() / 2;
 			translate.setTranslate(center_x - icon_offset_x, center_y - icon_offset_y);
 			max_dim = Math.max(width, height);
 		}
 				
 		protected void onDraw(Canvas canvas) {
 			Matrix m = canvas.getMatrix();
 
 			// Draw a circle around the center.
 			Log.d(DEBUG_TAG, "Matrix: " + translate.toShortString());
 			Log.d(DEBUG_TAG, "Canvas: " + m.toShortString());
 
 			float[] mtx = new float[9];
 			translate.getValues(mtx);
 
 			float x_offset = mtx[Matrix.MTRANS_X];
 			float y_offset = mtx[Matrix.MTRANS_Y];
 
 			if (!bowling) {
				// calculateVelocityVector(x_offset, y_offset);
 			}
			
			newCalculateVelocityVector(x_offset, y_offset);
 
 			Paint paint = new Paint();
 			paint.setColor(Color.BLUE);
 			canvas.drawCircle(center_x, center_y, 10, paint);
 			paint.setStyle(Paint.Style.STROKE);
 			canvas.drawCircle(center_x, center_y, 100, paint);
 
 			int textLocation = 20;
 			if (bowling) {
 				canvas.drawText("Let's Bowl!", 0, textLocation, paint);
 				textLocation += 20;
 			}
 
 			String speedText = "" + speedLeft + ", " + speedRight;
 			canvas.drawText(speedText, 0, textLocation, paint);
 			textLocation += 20;
 			canvas.drawText("" + velocity, 0, textLocation, paint);
 			textLocation += 20;
 
 			// Show speed indicators for the left and right motors
 			float bottom;
 			float top;				
 
 			// LinearGradient  Cool bowling alley effect.
 //			int w = getWidth();
 //		    int h = getHeight();
 //			Paint p = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 //			Path pth = new Path();
 //			pth.moveTo(w*0.27f,0);
 //		    pth.lineTo(w*0.73f,0);
 //		    pth.lineTo(w*0.92f,h);
 //		    pth.lineTo(w*0.08f,h);
 //		    pth.lineTo(w*0.27f,0);
 //		    p.setColor(0xff800000);
 //		    p.setShader(new LinearGradient(0,0,0,h,0xff000000,0xffffffff,Shader.TileMode.CLAMP));
 //		    canvas.drawPath(pth,p);
 			
 			Paint gaugeGradient = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
 			gaugeGradient.setColor(0xff800000);
 			gaugeGradient.setShader(new LinearGradient(0, center_y, 5, center_y - 100, Color.GREEN, Color.RED, Shader.TileMode.CLAMP));
 			canvas.drawRect(getWidth() - 5 - 1, center_y - 100, getWidth() - 1, center_y, gaugeGradient);
 			canvas.drawRect(1, center_y - 100, 5, center_y, gaugeGradient);
 			gaugeGradient.setShader(new LinearGradient(0, center_y + 100, 5, center_y, Color.RED, Color.GREEN, Shader.TileMode.CLAMP));
 			canvas.drawRect(getWidth() - 5 - 1, center_y + 100, getWidth() - 1, center_y, gaugeGradient);
 			canvas.drawRect(1, center_y + 100, 5, center_y, gaugeGradient);
 			
 			// Speed gauge outline
 			Paint gaugeBorder = new Paint();
 			gaugeBorder.setColor(Color.LTGRAY);
 			gaugeBorder.setStyle(Paint.Style.STROKE);
 			canvas.drawRect(0, center_y - 100, 5, center_y + 100, gaugeBorder);
 			canvas.drawRect(getWidth() - 5 - 1, center_y - 100, getWidth() - 1, center_y + 100, 
 					gaugeBorder);
 			canvas.drawLine(0, center_y, 5, center_y, gaugeBorder);
 			canvas.drawLine(getWidth() - 5 - 1, center_y, getWidth() - 1, center_y, gaugeBorder);
 
 			bottom = center_y;
 			// Simple gauge
 //			top = center_y - (100 * speedLeft);
 //			canvas.drawRect(0, top, 5, bottom, paint);
 
 			Paint black = new Paint();
 			black.setColor(Color.BLACK);
 			
 			top = center_y - (100 * speedLeft);
 			if (speedLeft == 0) {
 			  canvas.drawRect(1, center_y + 100, 5, center_y + 1, black);
 			  canvas.drawRect(1, center_y - 100 + 1, 5, center_y, black);				
 			} else if (speedLeft > 0) {
 			  canvas.drawRect(1, center_y - 100 + 1, 5, top + 1, black);
 			  canvas.drawRect(1, center_y + 100, 5, center_y + 1, black);
 			} else {
 			  canvas.drawRect(1, center_y + 100, 5, top + 1, black);
 			  canvas.drawRect(1, center_y - 100 + 1, 5, center_y, black);
 			}
 
 			
 			// Right motor gauge.  Paint black over the part we don't want to show.
 			top = center_y - (100 * speedRight);
 			if (speedRight == 0) {
 			  canvas.drawRect(getWidth() - 5 - 1 + 1, center_y + 100, getWidth() - 1, center_y + 1, black);
 			  canvas.drawRect(getWidth() - 5 - 1 + 1, center_y - 100 + 1, getWidth() - 1, center_y, black);				
 			} else if (speedRight > 0) {
 			  canvas.drawRect(getWidth() - 5 - 1 + 1, center_y - 100 + 1, getWidth() - 1, top + 1, black);
 			  canvas.drawRect(getWidth() - 5 - 1 + 1, center_y + 100, getWidth() - 1, center_y + 1, black);
 			} else {
 			  canvas.drawRect(getWidth() - 5 - 1 + 1, center_y + 100, getWidth() - 1, top + 1, black);
 			  canvas.drawRect(getWidth() - 5 - 1 + 1, center_y - 100 + 1, getWidth() - 1, center_y, black);
 			}
 			
 			canvas.drawBitmap(droid, translate, null);
 		}
 
		private void newCalculateVelocityVector(float x, float y) {
			float x_offset = center_x - x - icon_offset_x;
			float y_offset = center_y - y - icon_offset_y;
			
			Log.d(DEBUG_TAG, "Scale: " + x_offset + ", " + y_offset);
			angle = Math.atan2(y_offset, x_offset);
			angle -= Math.PI / 4.0;
			Log.d(DEBUG_TAG, "Angle: " + angle);

			double angle_degrees = angle / Math.PI * 180;
			Log.d(DEBUG_TAG, "Angle degrees: " + angle_degrees);

			velocity = Math.sqrt(x_offset * x_offset + y_offset * y_offset);
			Log.d(DEBUG_TAG, "Velocity: " + velocity);

			new_leftSpeed = (float) (velocity * Math.sin(angle));
			new_rightSpeed = (float) (velocity * Math.cos(angle));
			
			new_leftSpeed /= 100;
			new_rightSpeed /= 100;
			
			Log.d(DEBUG_TAG, "left: " + new_leftSpeed);
			Log.d(DEBUG_TAG, "right: " + new_rightSpeed);

			setSpeed(new_leftSpeed, new_rightSpeed);
		}

 		@Override
 		public boolean onTouchEvent(MotionEvent event) {
 			return gestures.onTouchEvent(event);
 		}
 
 		public void onMove(float dx, float dy) {
 			translate.postTranslate(dx, dy);
 			invalidate();
 		}
 
 		private Matrix animateStart;
 		private Interpolator animateInterpolator;
 		private long startTime;
 		private long endTime;
 		private float totalAnimDx;
 		private float totalAnimDy;
 
 		public void onAnimateMove(float dx, float dy, long duration) {
 			animateStart = new Matrix(translate);
 			animateInterpolator = new OvershootInterpolator();
 			startTime = System.currentTimeMillis();
 			endTime = startTime + duration;
 			totalAnimDx = dx;
 			totalAnimDy = dy;
 			post(new Runnable() {
 				@Override
 				public void run() {
 					onAnimateStep();
 				}
 			});
 		}
 
 		private void onAnimateStep() {
 			long curTime = System.currentTimeMillis();
 			float percentTime = (float) (curTime - startTime)
 					/ (float) (endTime - startTime);
 			float percentDistance = animateInterpolator
 					.getInterpolation(percentTime);
 			float curDx = percentDistance * totalAnimDx;
 			float curDy = percentDistance * totalAnimDy;
 			translate.set(animateStart);
 			onMove(curDx, curDy);
 
 			Log.v(DEBUG_TAG, "We're " + percentDistance + " of the way there!");
 			if (percentTime < 1.0f) {
 				post(new Runnable() {
 					@Override
 					public void run() {
 						onAnimateStep();
 					}
 				});
 			}
 		}
 
 		public void onResetLocation() {
 			translate.reset();
 			translate.setTranslate(center_x - icon_offset_x, center_y - icon_offset_y);
 			invalidate();
 		}
 	}
 
 	private class GestureListener implements GestureDetector.OnGestureListener,
 			GestureDetector.OnDoubleTapListener {
 		PlayAreaView view;
 
 		public GestureListener(PlayAreaView view) {
 			this.view = view;
 		}
 
 		public boolean onDown(MotionEvent e) {
 			Log.v(DEBUG_TAG, "onDown");
 			return true;
 		}
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			Log.v(DEBUG_TAG, "onDoubleTap");
 			setSpeedReal(0, 0);
 			view.onResetLocation();
 			return true;
 		}
 
 		public boolean onDoubleTapEvent(MotionEvent e) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public boolean onFling(MotionEvent e1, MotionEvent e2,
 				final float velocityX, final float velocityY) {
 			Log.v(DEBUG_TAG, "onFling");
 			final float distanceTimeFactor = 0.4f;
 			final float totalDx = (distanceTimeFactor * velocityX / 2);
 			final float totalDy = (distanceTimeFactor * velocityY / 2);
 
 			Log.v(DEBUG_TAG, "onFling total distance: " + totalDx + ", "
 					+ totalDy);
 
 			view.onAnimateMove(totalDx, totalDy,
 					(long) (1000 * distanceTimeFactor));
 
 			if (bowling) {
 				calculateVelocityVector(totalDx, totalDy);
 				bowl(new_leftSpeed, new_rightSpeed, (int) (velocity * 5));
 			}
 			return true;
 		}
 
 		public void onLongPress(MotionEvent e) {
 			// TODO Auto-generated method stub
 		}
 
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			Log.v(DEBUG_TAG, "onScroll");
 
 			view.onMove(-distanceX, -distanceY);
 			return true;
 		}
 
 		public void onShowPress(MotionEvent e) {
 			// TODO Auto-generated method stub
 		}
 
 		public boolean onSingleTapUp(MotionEvent e) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 	}
 
 	private ServiceConnection serviceConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			try {
 				gameControl.unregisterCallback(serviceCallback);
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			gameControl = null;
 		}
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
 			gameControl = IGameControl.Stub.asInterface(serviceBinder);
 			try {
 				gameControl.registerCallback(serviceCallback);
 			} catch (RemoteException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			// Setup driving
 			try {
 				gameControl.prepareRobots();
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Lock to portrait for now.
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		// Need a splashscreen...
 
 		setContentView(R.layout.dragcontroller);
 
 		FrameLayout frame = (FrameLayout) findViewById(R.id.graphics_holder);
 		PlayAreaView image = new PlayAreaView(this);
 		frame.addView(image);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.preferences, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.bowling:
 			bowling = !bowling;
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	// @Override
 	// public boolean onPrepareOptionsMenu(Menu menu) {
 	// // TODO Auto-generated method stub
 	// MenuItem bowlingMenuLabel = (MenuItem) findViewById(R.id.bowling);
 	//
 	// if (bowling) {
 	// bowlingMenuLabel.setTitle("Joystick mode");
 	// } else {
 	// bowlingMenuLabel.setTitle("Let's Bowl");
 	// }
 	//
 	// return super.onPrepareOptionsMenu(menu);
 	// }
 
 	public void bowl(float leftSpeed, float rightSpeed, final int time) {
 		setSpeedReal(leftSpeed, rightSpeed);
 		new Thread() {
 			public void run() {
 				try {
 					Thread.sleep(time);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				setSpeedReal(0, 0);
 				bowlingHandler.sendEmptyMessage(0);
 			}
 		}.start();
 	}
 
 	private Handler bowlingHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			Toast.makeText(DriveActivity.this, "Bowling done",
 					Toast.LENGTH_SHORT).show();
 		}
 	};
 
 	@Override
 	protected void onResume() {
 		super.onStart();
 		if (DEBUG) {
 			Log.d(LOG_TAG, "onStart()");
 		}
 
 		// Keep screen on.
 		PowerManager power_manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		screenWakeLock = power_manager.newWakeLock(
 				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "DriveActivity");
 		screenWakeLock.acquire();
 
 		if (!EMULATOR) {
 			bindService(new Intent("orbotix.robot.IGameControl"),
 					serviceConnection, BIND_AUTO_CREATE);
 
 			showDialog(DIALOG_SETUP_ID);
 		} else {
 			Toast.makeText(this, "Emulator mode; no robot connection",
 					Toast.LENGTH_LONG);
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onStop();
 		if (DEBUG) {
 			Log.d(LOG_TAG, "onStop()");
 		}
 
 		if (gameControl != null) {
 			try {
 				gameControl.shutdownRobots();
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			unbindService(serviceConnection);
 		}
 
 		// Stop preventing the screen from sleeping
 		screenWakeLock.release();
 		screenWakeLock = null;
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 		switch (id) {
 		case DIALOG_SETUP_ID:
 			ProgressDialog progress_dialog = new ProgressDialog(this);
 			progress_dialog
 					.setTitle(getString(R.string.drive_setup_progress_title));
 			progress_dialog
 					.setMessage(getString(R.string.drive_setup_progress_message));
 			dialog = progress_dialog;
 			break;
 
 		default:
 			break;
 		}
 		return dialog;
 	}
 
 	private void showRobotLostControlAlert() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(this.getString(R.string.lost_control));
 		builder.setCancelable(false);
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				finish();
 			}
 		});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	private void showFailedToSetupAlert() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(this.getString(R.string.drive_failed_to_setup));
 		builder.setCancelable(false);
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				finish();
 			}
 		});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	private IOrbotixServiceCallback serviceCallback = new IOrbotixServiceCallback.Stub() {
 
 		@Override
 		public void robotLostControl() throws RemoteException {
 			Message message = serviceHandler.obtainMessage(ROBOT_LOST_CONTROL);
 			message.sendToTarget();
 		}
 
 		@Override
 		public void prepareRobotFinished(int failed) throws RemoteException {
 			Message message = serviceHandler.obtainMessage(
 					PREPARE_ROBOT_FINISHED, failed, 0);
 			message.sendToTarget();
 		}
 	};
 
 	private Handler serviceHandler = new Handler() {
 		public void handleMessage(android.os.Message msg) {
 			switch (msg.what) {
 			case PREPARE_ROBOT_FINISHED:
 				dismissDialog(DIALOG_SETUP_ID);
 				removeDialog(DIALOG_SETUP_ID); // needed only once
 
 				if (msg.arg1 > 0) {
 					showFailedToSetupAlert();
 				}
 				break;
 			case ROBOT_LOST_CONTROL:
 				showRobotLostControlAlert();
 				break;
 			default:
 				break;
 			}
 		};
 	};
 
 	public void setSpeed(float speedLeft, float speedRight) {
 		long newTime = System.currentTimeMillis();
 
 		// Make sure stops are sent.
 		if (Math.abs(speedLeft) < DEAD_ZONE && Math.abs(speedRight) < DEAD_ZONE) {
 			if (this.speedLeft != 0 || this.speedRight != 0) {
 				this.speedLeft = 0;
 				this.speedRight = 0;
 				setSpeedReal(speedLeft, speedRight);
 				setFullStop(true);
 				return;
 			}
 		}
 
 		// Make sure large speed changes get sent
 		if (Math.abs(speedLeft - this.speedLeft) > SPEED_THRESHOLD
 				|| Math.abs(speedRight - this.speedRight) > SPEED_THRESHOLD) {
 			setSpeedReal(speedLeft, speedRight);
 			return;
 		}
 
 		// Prevent the send queue from becoming flooded.
 		if ((newTime - lastCommandTime) < COMMAND_DELAY) {
 			return;
 		}
 
 		setSpeedReal(speedLeft, speedRight);
 	}
 
 	private void setSpeedReal(float speedLeft, float speedRight) {
 		Log.i(LOG_TAG, "Setting speeds to " + speedLeft + ", " + speedRight);
 		this.speedLeft = speedLeft;
 		this.speedRight = speedRight;
 		try {
 			if (gameControl != null && gameControl.hasRobotControl()) {
 				gameControl.setLeftMotorSpeed(speedLeft);
 				gameControl.setRightMotorSpeed(speedRight);
 				lastCommandTime = System.currentTimeMillis();
 			}
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void setFullStop(boolean fullStop) {
 		// Stop the motors
 		Log.i(LOG_TAG, "Fullstop: " + fullStop);
 		try {
 			if (gameControl != null && gameControl.hasRobotControl()) {
 				if (fullStop && !gameControl.isAtFullStop()) {
 					gameControl.setFullStop(true);
 					return;
 				}
 				if (!fullStop && gameControl.isAtFullStop()) {
 					gameControl.setFullStop(false);
 					return;
 				}
 			}
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// Add this to the SeekBars' change listener.
 	}
 }
