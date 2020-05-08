 package com.totoro.incardisplay;
 
 import com.facebook.*;
 import com.facebook.model.*;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.totoro.incardisplay.util.SystemUiHider;
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  * 
  * @see SystemUiHider
  */
 public class Login extends Activity {
 	/**
 	 * Whether or not the system UI should be auto-hidden after
 	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
 	 */
 	private static final boolean AUTO_HIDE = true;
 
 	/**
 	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
 	 * user interaction before hiding the system UI.
 	 */
 	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
 
 	/**
 	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
 	 * will show the system UI visibility upon interaction.
 	 */
 	private static final boolean TOGGLE_ON_CLICK = true;
 
 	/**
 	 * The flags to pass to {@link SystemUiHider#getInstance}.
 	 */
 	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
 
 	/**
 	 * The instance of the {@link SystemUiHider} for this activity.
 	 */
 	private SystemUiHider mSystemUiHider;
 	
 	private Activity loginActivity;
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 	  super.onActivityResult(requestCode, resultCode, data);
 	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_login);
 		
 		loginActivity = this;
 		final View controlsView = findViewById(R.id.fullscreen_content_controls);
 		final View contentView = findViewById(R.id.fullscreen_content);
 
 		// Set up an instance of SystemUiHider to control the system UI for
 		// this activity.
 		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
 				HIDER_FLAGS);
 		mSystemUiHider.setup();
 		mSystemUiHider
 				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
 					// Cached values.
 					int mControlsHeight;
 					int mShortAnimTime;
 
 					@Override
 					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 					public void onVisibilityChange(boolean visible) {
 						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 							// If the ViewPropertyAnimator API is available
 							// (Honeycomb MR2 and later), use it to animate the
 							// in-layout UI controls at the bottom of the
 							// screen.
 							if (mControlsHeight == 0) {
 								mControlsHeight = controlsView.getHeight();
 							}
 							if (mShortAnimTime == 0) {
 								mShortAnimTime = getResources().getInteger(
 										android.R.integer.config_shortAnimTime);
 							}
 							controlsView
 									.animate()
 									.translationY(visible ? 0 : mControlsHeight)
 									.setDuration(mShortAnimTime);
 						} else {
 							// If the ViewPropertyAnimator APIs aren't
 							// available, simply show or hide the in-layout UI
 							// controls.
 							controlsView.setVisibility(visible ? View.VISIBLE
 									: View.GONE);
 						}
 
 						if (visible && AUTO_HIDE) {
 							// Schedule a hide().
 							delayedHide(AUTO_HIDE_DELAY_MILLIS);
 						}
 					}
 				});
 
 		// Set up the user interaction to manually show or hide the system UI.
 		contentView.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				if (TOGGLE_ON_CLICK) {
 					mSystemUiHider.toggle();
 				} else {
 					mSystemUiHider.show();
 				}
 			}
 		});
 
 		// Upon interacting with UI controls, delay any scheduled hide()
 		// operations to prevent the jarring behavior of controls going away
 		// while interacting with the UI.
 		findViewById(R.id.dummy_button).setOnTouchListener(
 				mDelayHideTouchListener);
 		
 		// Dummy button goes to car profile page
 		final Button dummy_button = (Button) findViewById(R.id.dummy_button);
         dummy_button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 try {
                 	ProfileCarDB db = new ProfileCarDB(v.getContext());
                 	db.deleteProfile();
                 	db.close();
                 } catch (Exception e) {
                 	
                 }
             }
         });
         
         //	facebook login button!!!!
 		final Button login_button = (Button) findViewById(R.id.login_button);
 		login_button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 try {
             		// start Facebook Login
             		Session.openActiveSession(loginActivity, true, new Session.StatusCallback() {
 
             			// callback when session changes state
             			@Override
             			public void call(Session session, SessionState state, Exception exception) {
             				if (session.isOpened()) {
             					// make request to the /me API
             					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
 
             					  // callback after Graph API response with user object
             					  @Override
             					  public void onCompleted(GraphUser user, Response response) {
             						  if (user != null) {
             							  TextView welcome = (TextView) findViewById(R.id.logo);
             							  welcome.setText("Hello " + user.getName() + "!");
             							}
             					  }
             					});
             				}
             			}
             		});
                	//Intent k = new Intent(Login.this, CarProfileForm.class);
                	//startActivity(k);
                 	
                 } catch (Exception e) {
                 	
                 }
             }
 		});
 
 		final Button logout_button = (Button) findViewById(R.id.login_with_email);
 		logout_button.setOnClickListener(new View.OnClickListener(){
 			public void onClick(View v) {
 				try {
 					if (Session.getActiveSession() != null) {
 						Session.getActiveSession().closeAndClearTokenInformation();
 						Session.getActiveSession().close();
 						Session.setActiveSession(null);
 						TextView welcome = (TextView) findViewById(R.id.logo);
 						welcome.setText("Goodbye");
 					} else {
 						TextView welcome = (TextView) findViewById(R.id.logo);
 						welcome.setText("You weren't logged in to start with");
 					}
 				} catch (Exception e) {
 					
 				}
 			}
 		});
 
 
 	}
 	
 	
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 
 		// Trigger the initial hide() shortly after the activity has been
 		// created, to briefly hint to the user that UI controls
 		// are available.
 		delayedHide(100);
 	}
 
 	/**
 	 * Touch listener to use for in-layout UI controls to delay hiding the
 	 * system UI. This is to prevent the jarring behavior of controls going away
 	 * while interacting with activity UI.
 	 */
 	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
 		@Override
 		public boolean onTouch(View view, MotionEvent motionEvent) {
 			if (AUTO_HIDE) {
 				delayedHide(AUTO_HIDE_DELAY_MILLIS);
 			}
 			return false;
 		}
 	};
 
 	Handler mHideHandler = new Handler();
 	Runnable mHideRunnable = new Runnable() {
 		@Override
 		public void run() {
 			mSystemUiHider.hide();
 		}
 	};
 
 	/**
 	 * Schedules a call to hide() in [delay] milliseconds, canceling any
 	 * previously scheduled calls.
 	 */
 	private void delayedHide(int delayMillis) {
 		mHideHandler.removeCallbacks(mHideRunnable);
 		mHideHandler.postDelayed(mHideRunnable, delayMillis);
 	}
 }
