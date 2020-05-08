 package com.ecahack.fanburst;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.ecahack.fanburst.WSClient.WSClientListener;
 
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.util.Log;
 import android.view.*;
 import android.view.SurfaceHolder.Callback;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 import android.widget.ViewFlipper;
 
 public class MainActivity extends Activity implements OnClickListener, Callback, OnTouchListener, WSClientListener {
 
 	private TimeSyncService mTimeSync = new TimeSyncService();
 	private Button mRegisterButton;
 	private Button mFlashButton;
 	private TextView mActiveUsersView;
 	private TextView mPatternTextView;
 	private TextView mTimerView;
 	private ImageView mBulbView;
 	private ViewFlipper flipper;
 	private boolean mPatternRunning;
 	private boolean isFlashOn;
 
 	private WSClient mWSClient;
 
 	Camera mCamera;
 	private SurfaceHolder mHolder;
 
 	private static final String TAG = "FanBurst";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		flipper = (ViewFlipper) findViewById(R.id.flipper);
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		flipper.addView(inflater.inflate(R.layout.registation, null));
 		flipper.addView(inflater.inflate(R.layout.activation, null));
 
 		flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
 		flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
 
 		mRegisterButton = (Button)this.findViewById(R.id.registerButton);
 		mRegisterButton.setOnClickListener(this);
 
 		mFlashButton = (Button)this.findViewById(R.id.flashOnShakeButton);
 		mFlashButton.setOnTouchListener(this);
 		
 		mActiveUsersView = (TextView)this.findViewById(R.id.activeUsersTextView);
 		mPatternTextView = (TextView)this.findViewById(R.id.patternTextView);
 		mTimerView = (TextView)this.findViewById(R.id.timer);
 		
 		mBulbView = (ImageView)this.findViewById(R.id.bulbImageView);
 		
 		mWSClient = new WSClient(this);
 		mWSClient.connect();
 
 		boolean hasCamera = checkCameraHardware(getApplicationContext());
 		if (hasCamera) 
 			initCamera();
 		else 
 			showNoCameraDialog();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}   
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return false;
 	}
 
 	@Override
 	public void updateStats(final long active, long users) {
 		MainActivity.this.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				mActiveUsersView.setText(String.valueOf(active));
 			}
 		});	
 	}
 
 	@Override
 	public void updateTimeSync(long sentTime, long serverTime) {
 		mTimeSync.collectResponse(sentTime, serverTime);
 
 		if(mTimeSync.getSamplesLength() < 5) {
 			sentTimesyncRequest();
 		} else {
 			mTimeSync.finish();
 			Log.d(TAG, String.format("Result timeshift %d", mTimeSync.getTimeshift()));
 		}
 	}
 
 	@Override
 	public void showPattern(final String name, final long startAt, final long interval, final ArrayList<Integer> pattern) {
 		if (!mPatternRunning) {
 			mPatternRunning = true;
 			MainActivity.this.runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					long startInterval = startAt - mTimeSync.getCurrentTimestamp();
 					runTimerWithSec(startInterval);
 					mPatternTextView.setText(name);
 					startPatternAfterDelay(startInterval, pattern, interval);
 				}
 			});	
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == mRegisterButton) {
 			sendRegisterInfo();
 			sentTimesyncRequest();
 		}
 	}
 
 	@Override
 	public boolean onTouch( View button , MotionEvent theMotion ) {
 		if (mPatternRunning)
 			return true;
 		switch ( theMotion.getAction() ) {
 		case MotionEvent.ACTION_DOWN: 
 			turnOn();
 			break;
 		case MotionEvent.ACTION_UP: 
 			turnOff();
 			break;
 		}
 		return true;
 	}
 	
 	public void onToggleClicked(View view) {
 		boolean on = ((ToggleButton) view).isChecked();
 		if (on) {
 			sendActivateRequest();
 		} else {
 			sendDeactivateRequest();
 		}
 	}
 	
 	private void runTimerWithSec(final long sec) {
 		final Animation in = new AlphaAnimation(0.0f, 1.0f);
	    in.setDuration(500);
 	    
 	    final Animation out = new AlphaAnimation(1.0f, 0.0f);
	    out.setDuration(400);
 	    
 	    in.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationStart(Animation animation) {
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) {	
 			}
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				mTimerView.startAnimation(out);
 			}
 		});
 	    
 		new CountDownTimer(sec, 1000) {
 			long seconds = sec/1000;
 			
 			@Override
 			public void onFinish() {
 				mTimerView.setText("");
 			}
 
 			@Override
 			public void onTick(long millisUntilFinished) {
 				mTimerView.setText(String.valueOf(seconds));
 				mTimerView.startAnimation(in);
 				seconds--;
 			}
 		
 		}.start();
 	}
 	
 	private void startPatternAfterDelay(long delay, final ArrayList<Integer> pattern, final long interval) {
 		Timer timer = new Timer();
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				MainActivity.this.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						runPattern(pattern, interval, 0);
 					}
 				});
 			}
 		}, delay);
 	}
 	
 	private void runPattern(final ArrayList<Integer> list, final long interval, final int i) {
 		new CountDownTimer(list.size()*interval+50, interval) {
 			int step = 0;
 			
 			@Override
 			public void onTick(long millisUntilFinished) {
 				if(step>=list.size())
 					return;
 				Integer brightness = list.get(step);
 				if (brightness == 1)
 					turnOn();
 				else
 					turnOff();
 				step++;
 			}
 			
 			@Override
 			public void onFinish() {
 				mPatternRunning = false;
 				mPatternTextView.setText("");
 				turnOff();
 			}
 		}.start();
 	}
 
 	private void sendRegisterInfo() {
 		mWSClient.sendRegisterInfo(getDeviceId(), getUserSector(), getUserRow(), getUserPlace());
 		flipper.showNext();	
 	}
 
 	private void sentTimesyncRequest() {
 		mWSClient.sentTimesyncRequest(mTimeSync.getCurrentTimestamp());
 	}
 
 	private void sendDeactivateRequest() {
 		mWSClient.sendDeactivateRequest();
 	}
 
 	private void sendActivateRequest() {
 		mWSClient.sendActivateRequest();
 	}
 
 	private String getDeviceId() {
 		return  UniqueIdentifier.id(getApplicationContext());
 	}
 
 	private String getUserSector() {
 		return getEditTextValue(R.id.sectorTextView);
 	}
 
 	private String getUserPlace() {
 		return getEditTextValue(R.id.placeTextView);
 	}
 
 	private String getUserRow() {
 		return getEditTextValue(R.id.rowTextView);
 	}
 
 	private String getEditTextValue(int id) {
 		EditText editText = (EditText) this.findViewById(id);
 		return editText.getText().toString();
 	}
 
 	private void initCamera() {
 		SurfaceView preview = (SurfaceView) findViewById(R.id.surface);
 		mHolder = preview.getHolder();
 		mHolder.addCallback(this);
 		mCamera = Camera.open();
 		try {
 			mCamera.setPreviewDisplay(mHolder);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void turnOn() {
 		if (!isFlashOn) {
 			isFlashOn = true;
 			Parameters params = mCamera.getParameters();
 			params.setFlashMode(Parameters.FLASH_MODE_TORCH);
 			mCamera.setParameters(params);      
 			mCamera.startPreview();
 			mBulbView.setImageResource(R.drawable.ic_img_bulb_on);
 		}
 	}
 
 	private void turnOff() {
 		if (isFlashOn) {
 			isFlashOn = false;
 			Parameters params = mCamera.getParameters();
 			params.setFlashMode(Parameters.FLASH_MODE_OFF);
 			mCamera.setParameters(params);
 			mBulbView.setImageResource(R.drawable.ic_img_bulb);
 		}
 	}
 
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		mHolder = holder;
 		try {
 			mCamera.setPreviewDisplay(mHolder);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		mCamera.stopPreview();
 		mHolder = null;
 	}
 
 	private boolean checkCameraHardware(Context context) {
 		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private void showNoCameraDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("No camera");
 		builder.setMessage("Camera is necessary for application.");
 		builder.setPositiveButton("OK", null);
 		builder.show();
 	}
 }
