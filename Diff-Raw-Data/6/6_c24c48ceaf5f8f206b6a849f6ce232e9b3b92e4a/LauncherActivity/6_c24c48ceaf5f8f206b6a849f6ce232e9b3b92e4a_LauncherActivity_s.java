 /**
  * 
  */
 package com.madhackerdesigns.neverbelate.ui;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.madhackerdesigns.neverbelate.Eula;
 import com.madhackerdesigns.neverbelate.R;
 import com.madhackerdesigns.neverbelate.service.StartupReceiver;
 import com.madhackerdesigns.neverbelate.settings.NeverBeLateSettings;
 import com.madhackerdesigns.neverbelate.settings.PreferenceHelper;
 import com.madhackerdesigns.neverbelate.util.AdHelper;
 import com.madhackerdesigns.neverbelate.util.Logger;
 import com.pontiflex.mobile.webview.sdk.AdManagerFactory;
 import com.pontiflex.mobile.webview.sdk.IAdManager;
 
 /**
  * @author smccormack
  *
  */
 public class LauncherActivity extends Activity implements Eula.OnEulaAgreedTo {
 
 	// Static constants
 	private static final boolean ADMOB = true;
 	private static final int DLG_COMING_SOON = 0;
 	private static final int DLG_FIRST_TIME = 1;
 	private static final int DLG_QUICK_TOUR = 2;
 	private static final int DLG_WHATS_NEW = 3;
 	private static final long FIVE_MINUTES = 5*60*1000;
 	private static final String KEY_AD_LAST_SHOWN = "app_state.ad_last_shown";
 	private static final String KEY_FIRST_TIME = "app_state.first_time";
 	private static final String KEY_QUICK_TOUR = "app_state.quick_tour";
 	private static final String KEY_WHATS_NEW = "app_state.whats_new";
     private static final String LOG_TAG = "NeverBeLateService";
     private static final boolean PONTIFLEX = true;
 	private static final String PREF_APP_STATE = "app_state";
	private static final String MY_ID = "a14d952e8939105";
 
 	// Private fields
 	private AdHelper mAdHelper;
 	private CheckBox mEnableBtn;
 	private PreferenceHelper mPrefs;
 	private SharedPreferences mAppState;
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {		
 		super.onCreate(savedInstanceState);
 		loadHelpers();
 		
 		// Show TOS and EULA for acceptance
 		Eula.show(this);
 		
 		// Set content view to launcher layout and load application resources
 		setContentView(R.layout.launcher);
 		
 		// Setup the Enable checkbox
 		mEnableBtn = (CheckBox) findViewById(R.id.btn_enable);
 		mEnableBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@SuppressWarnings("deprecation")
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				// Change the enabled state and enforce the change
 				mPrefs.setNeverLateEnabled(isChecked);
 				sendBroadcast(new Intent(LauncherActivity.this, StartupReceiver.class));
 				
 				// If this is first time use, encourage user to create new event
 				if (! mAppState.getBoolean(KEY_FIRST_TIME, false)) {
 					mAppState.edit().putBoolean(KEY_FIRST_TIME, true).commit();
 					showDialog(DLG_FIRST_TIME);
 				}
 			}
 			
 		});
 		
 		// Load the config button
 		Button configBtn = (Button) findViewById(R.id.btn_configure);
 		configBtn.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				// Send user to the Settings menu
 				Intent intent = new Intent(LauncherActivity.this, NeverBeLateSettings.class);
 	        	startActivity(intent);
 			}
 			
 		});
 		
 		// Load the share button
 		Button shareBtn = (Button) findViewById(R.id.btn_share);
 		shareBtn.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				shareNeverLate();
 			}
 			
 		});
 		
 		// Create a coming soon dialog for features not yet implemented
 		((Button) findViewById(R.id.btn_create_event)).setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_EDIT);
 				intent.setType("vnd.android.cursor.item/event");
 				startActivity(intent);
 			}
 			
 		});
 		
 		// Point the Quick Tour button to the new Quick Tour
 		((Button) findViewById(R.id.btn_quick_tour)).setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				startActivity(new Intent(LauncherActivity.this, QuickTourActivity.class));
 			}
 			
 		});
 		
 		// Set the NeverLate logo to launch the testing activity, REMOVE BEFORE PUBLISHING!
 		ImageView logo = (ImageView) findViewById(R.id.logo);
 		logo.setOnLongClickListener(new OnLongClickListener() {
 
 			public boolean onLongClick(View v) {
 				// Launch the testing activity
 				Intent i = new Intent(LauncherActivity.this, TestActivity.class);
 				startActivity(i);
 				return true;
 			}
 			
 		});
 		
 		// Load up an AdMob banner
 		if (ADMOB) {
 			LinearLayout layout = (LinearLayout) findViewById(R.id.ad_view);
 			if (layout != null) {
				AdView adView = new AdView(this, AdSize.BANNER, MY_ID);
 		    	layout.addView(adView);
 		    	AdRequest adRequest = new AdRequest();
 		    	adView.loadAd(adRequest);
 		    	Logger.d(LOG_TAG, "AdMob banner loaded.");
 			}
 		}
 	}
 
 	protected void shareNeverLate() {
 		// Send out a share intent for the this app's URI in the market
 		Resources res = getResources();
 		Intent i = new Intent(Intent.ACTION_SEND);
 		i.setType("text/plain");
 		i.putExtra(Intent.EXTRA_SUBJECT, res.getString(R.string.share_subject));
 		i.putExtra(Intent.EXTRA_TEXT, res.getString(R.string.share_message));
 		startActivity(Intent.createChooser(i, res.getString(R.string.share_menu_title)));
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateDialog(int)
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		
 		switch (id) {
 		case DLG_COMING_SOON:
 			builder.setTitle(R.string.dlg_coming_soon_title)
 				   .setMessage(R.string.dlg_coming_soon_msg)
 			       .setCancelable(true)
 			       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                dialog.cancel();
 			           }
 			       });
 			break;
 		case DLG_FIRST_TIME:
 			builder.setTitle(R.string.dlg_first_time_title)
 				   .setMessage(R.string.dlg_first_time_msg)
 			       .setCancelable(true)
 			       .setPositiveButton(R.string.create_event, new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			        	   Intent intent = new Intent(Intent.ACTION_EDIT);
 			               intent.setType("vnd.android.cursor.item/event");
 			               startActivity(intent);
 			        	   dialog.cancel();
 			           }
 			       });
 			break;
 		case DLG_WHATS_NEW:
 			builder.setTitle(R.string.whats_new_title)
 				   .setMessage(R.string.whats_new_message)
 				   .setCancelable(true)
 				   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 					
 						public void onClick(DialogInterface dialog, int which) {
 							// Store the new version of What's new as shown and dismiss dialog
 							int current = getResources().getInteger(R.integer.whats_new_version);
 							mAppState.edit().putInt(KEY_WHATS_NEW, current).commit();
 							dialog.cancel();
 						}
 					})
 					.setOnCancelListener(new OnCancelListener() {
 					
 						@SuppressWarnings("deprecation")
 						public void onCancel(DialogInterface dialog) {
 							// Check if the QuickTour has been viewed, and challenge if not
 							boolean seenQT = mAppState.getBoolean(KEY_QUICK_TOUR, false);
 							if (!seenQT) {
 								showDialog(DLG_QUICK_TOUR);
 							}
 						}
 						
 					});
 			break;
 		case DLG_QUICK_TOUR:
 			builder.setTitle(R.string.dlg_qt_title)
 				   .setMessage(R.string.dlg_qt_msg)
 				   .setCancelable(true)
 				   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 					
 						public void onClick(DialogInterface dialog, int which) {
 							// Mark QT as seen, and then start the QT activity
 							mAppState.edit().putBoolean(KEY_QUICK_TOUR, true).commit();
 							startActivity(new Intent(LauncherActivity.this, QuickTourActivity.class));
 						}
 				   })
 				   .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							// Still mark as seen to not annoy the user
 							mAppState.edit().putBoolean(KEY_QUICK_TOUR, true).commit();
 						}
 				   })
 				   .setOnCancelListener(new OnCancelListener() {
 
 						public void onCancel(DialogInterface dialog) {
 							// Still mark as seen to not annoy the user
 							mAppState.edit().putBoolean(KEY_QUICK_TOUR, true).commit();
 						}
 					   
 				   });
 		}
 		
 		return builder.create();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onPause()
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		// Release the preference helper resources
 		dropHelpers();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		// Load up the preference helper and set the checked state
 		loadHelpers();
 		if (mEnableBtn == null) { mEnableBtn = (CheckBox) findViewById(R.id.btn_enable); }
 		mEnableBtn.setChecked(mPrefs.isNeverLateEnabled());
 	}
 
 	@SuppressWarnings("deprecation")
 	public void onEulaAgreedTo() {
 		// Show either What's New or Pontiflex registration after confirmation of EULA agreement
 		int previous = mAppState.getInt(KEY_WHATS_NEW, 0);
 		int current = getResources().getInteger(R.integer.whats_new_version);
 		if (previous < current) { 
 			Logger.d("Showing Whats new dialog");
 			showDialog(DLG_WHATS_NEW);
 		} else {
 			long adLastShown = mAppState.getLong(KEY_AD_LAST_SHOWN, 0);
 			long now = new Date().getTime();
 			if (PONTIFLEX && now > (adLastShown + FIVE_MINUTES)) {
 				AdHelper adHelper = mAdHelper;
 				if (adHelper.isTimeToShowAd()) {
 					adHelper.setAdShown(true);
 					Logger.d("Showing Pontiflex registration form");
 					mAppState.edit().putLong(KEY_AD_LAST_SHOWN, now).commit();
 //					IAdManager adManager = AdManagerFactory.createInstance(getApplication());
 //					adManager.showAd();
 					// Initialize the Pontiflex Ad Manager
 					IAdManager adManager = AdManagerFactory.createInstance(getApplication());
 					// set registration mode to ad hoc, user can skip registration
 					adManager.setRegistrationRequired(false);
 					adManager.setRegistrationMode(IAdManager.RegistrationMode.RegistrationAdHoc);
 					// when you want to show registration dialog, invoke the startRegistrationActivity method on adManager
 					adManager.startRegistrationActivity();
 				}
 				adHelper.setWarningDismissed(true);
 			}
 		}
 	}
 	
 	private void loadHelpers() {
 		if (mPrefs == null) { mPrefs = new PreferenceHelper(this); }
 		if (mAppState == null) { 
 			mAppState = getSharedPreferences(PREF_APP_STATE, Activity.MODE_PRIVATE);
 		}
 		if (mAdHelper == null) { mAdHelper = new AdHelper(getApplicationContext()); }
     }
 	
 	private void dropHelpers() {
 		mPrefs = null;
 		mAppState = null;
 		mAdHelper = null;
 	}
 	
 }
