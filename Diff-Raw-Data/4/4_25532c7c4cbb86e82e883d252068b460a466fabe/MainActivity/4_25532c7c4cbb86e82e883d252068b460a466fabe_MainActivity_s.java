 package com.ecahack.fanburst;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.ecahack.fanburst.WSClient.WSClientListener;
 
 import android.graphics.Typeface;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.os.Bundle;
 import android.os.CountDownTimer;
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
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 import android.widget.ViewFlipper;
 
 public class MainActivity extends Activity implements OnClickListener, Callback, OnTouchListener, WSClientListener {
 
 	private TimeSyncService mTimeSync = new TimeSyncService();
 	private Button mRegisterButton;
 	private Button mFlashButton;
 	private ToggleButton mActiveButton;
 	private TextView mActiveUsersView;
 	private TextView mPatternTextView;
 	private TextView mTimerView;
 	private ImageView mBulbView;
 	private ViewFlipper flipper;
 	private RelativeLayout mTimerLayout;
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
 
 		mActiveButton = (ToggleButton)this.findViewById(R.id.togglebutton);
 
 		mActiveUsersView = (TextView)this.findViewById(R.id.activeUsersTextView);
 		mPatternTextView = (TextView)this.findViewById(R.id.patternTextView);
 		mTimerView = (TextView)this.findViewById(R.id.timer);
 		mTimerLayout = (RelativeLayout)this.findViewById(R.id.timerView);
 		mTimerLayout.setVisibility(View.GONE);
 
 		mBulbView = (ImageView)this.findViewById(R.id.bulbImageView);
 
 		mWSClient = new WSClient(this);
 		mWSClient.connect();
 
 		boolean hasCamera = checkCameraHardware(getApplicationContext());
 		if (hasCamera) 
 			initCamera();
 		else 
 			showNoCameraDialog();
 
 		setupFonts();
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
 					mCamera.startPreview();
 					turnOn();
 					long startInterval = startAt - mTimeSync.getCurrentTimestamp();
 					//startPatternAfterDelay(startAt, startInterval, pattern, interval);
 					runTimerWithSec(startInterval,startAt, startInterval, pattern, interval);
 					mPatternTextView.setText(name);
 					mActiveButton.setText("");
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
 			mFlashButton.setSelected(true);
 			turnOn();
 			break;
 		case MotionEvent.ACTION_UP: 
 			mFlashButton.setSelected(false);
 			turnOff();
 			break;
 		}
 		return true;
 	}
 
 	public void onToggleClicked(View view) {
 		ToggleButton btn = (ToggleButton)view;
 		boolean on = btn.isChecked();
 		if (on) {
 			mActiveButton.setText(getString(R.string.wait_users));
 			sendActivateRequest();
 		} else {
 			sendDeactivateRequest();
 			mTimerLayout.setVisibility(View.GONE);
 		}
 	}
 
 	private void runTimerWithSec(final long sec, final long startAt, long delay, final ArrayList<Integer> pattern, final long interval) {
 		mTimerLayout.setVisibility(View.VISIBLE);
 		final Animation in = new AlphaAnimation(0.0f, 1.0f);
 		in.setDuration(500);
 
 		final Animation out = new AlphaAnimation(1.0f, 0.0f);
 		out.setDuration(500);
 
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
 				mTimerLayout.setVisibility(View.GONE);
 				mActiveButton.setText("!!!");
 				MainActivity.this.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						runPattern(startAt, pattern, interval, 0);
 					}
 				});
 			}
 
 			@Override
 			public void onTick(long millisUntilFinished) {
 				mTimerView.setText(String.valueOf(seconds));
 				mTimerView.startAnimation(in);
 				seconds--;
 			}
 
 		}.start();
 	}
 	
 	private void startPatternAfterDelay(final long startAt, long delay, final ArrayList<Integer> pattern, final long interval) {
 		Timer timer = new Timer();
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				MainActivity.this.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						runPattern(startAt, pattern, interval, 0);
 					}
 				});
 			}
 		}, delay-500);
 	}
 	
 	private void runPattern(final long startAt, final ArrayList<Integer> list, final long interval, final int i) {
 		new CountDownTimer(list.size()*interval+1000, interval) {
 			int step = 0;
 
 			@Override
 			public void onTick(long millisUntilFinished) {
 				if(step>=list.size()) {
 					turnOff();
 					return;
 				}
 				
 				if(step==0) {
 					Log.d("WSClient", "spin lock");
 					turnOff();
 					for(;;) {
 
 						if(mTimeSync.getCurrentTimestamp()>=startAt) {
 							break;
 						}
 					}
 				}
 				
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
 				mActiveButton.setText(getString((mActiveButton.isChecked()) ? R.string.wait_users : R.string.press_and_patricipate));
 				turnOff();
 			}
 		}.start();
 	}
 
 	private void sendRegisterInfo() {
 		boolean sent = mWSClient.sendRegisterInfo(getDeviceId(), getUserSector(), getUserRow(), getUserPlace());
 		if (sent) {
 			InputMethodManager imm = (InputMethodManager)getSystemService(
 					Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(this.findViewById(R.id.sectorTextView).getWindowToken(), 0);
 			imm.hideSoftInputFromWindow(this.findViewById(R.id.rowTextView).getWindowToken(), 0);
 			imm.hideSoftInputFromWindow(this.findViewById(R.id.placeTextView).getWindowToken(), 0);
 			flipper.showNext();	
 		}
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
			//mCamera.startPreview();
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
 
 	private void setupFonts(){
 		Typeface font = Typeface.createFromAsset(getAssets(), "calibri.ttf");  
 		LinearLayout ll = (LinearLayout) findViewById(R.id.main_layout);
 		int childcount = ll.getChildCount();
 		for (int i=0; i < childcount; i++){
 			View v = ll.getChildAt(i);
 			if(v instanceof TextView) {
 				((TextView)v).setTypeface(font);
 			}
 			else if (v instanceof Button) {
 				((Button)v).setTypeface(font);
 			}
 		}
 	}
 }
