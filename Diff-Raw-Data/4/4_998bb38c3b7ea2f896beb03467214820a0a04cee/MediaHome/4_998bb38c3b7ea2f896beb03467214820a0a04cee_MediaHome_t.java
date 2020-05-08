 package fr.enseirb.odroidx.home;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 import fr.enseirb.odroidx.home.R;
 
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PaintFlagsDrawFilter;
 import android.graphics.PixelFormat;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.PaintDrawable;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.view.animation.LayoutAnimationController;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MediaHome extends Activity {
 
 	/**
 	 * Tag used for logging errors.
 	 */
 	private static final String LOG_TAG = "Home";
 
 	/**
 	 * Keys during freeze/thaw.
 	 */
 	private static final String KEY_SAVE_GRID_OPENED = "grid.opened";
 
 	private static ArrayList<ApplicationInfo> mApplications;
 
 	private final BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();
 
 	private GridView mGrid;
 
 	private LayoutAnimationController mShowLayoutAnimation;
 	private LayoutAnimationController mHideLayoutAnimation;
 
 	private boolean mBlockAnimation;
 
 	private TextView mClock;
 	private boolean mTickerStopped;
 	private Handler mHandler;
 	private Runnable mTicker;
 	private Calendar mCalendar;
 
 	private ArrayList<ImageView> buttons;
 	private OnClickListener mButtonClickedListener;
 	private OnTouchListener mButtonTouchFeedbackListener;
 
 	private boolean isFirstCommandReceivedFromRemote;
 	private int selectedButton;
 
 	private Animation mGridEntry;
 	private Animation mGridExit;
 	private Animation mFadeIn;
 	private Animation mFadeOut;
 
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 
 		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
 
 		setContentView(R.layout.activity_home);
 
 		registerIntentReceivers();
 
 		loadApplications(true);
 
 		this.startService(new Intent("RemoteControlService.intent.action.Launch"));
 
 		STBRemoteControlCommunication stbrcc = new STBRemoteControlCommunication(this);
 		stbrcc.doBindService();
 
 		bindApplications();
 		bindButtons();
 		bindClock();
 
 		mGridEntry = AnimationUtils.loadAnimation(this, R.anim.grid_entry);
 		mGridExit = AnimationUtils.loadAnimation(this, R.anim.grid_exit);
 		mFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
 		mFadeIn.setFillAfter(true);
 		mFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
 		mFadeOut.setFillAfter(true);
 		
 		isFirstCommandReceivedFromRemote = true;
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 
 		// Close the menu
 		if (Intent.ACTION_MAIN.equals(intent.getAction())) {
 			getWindow().closeAllPanels();
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		// Stop the clock thread
 		mTickerStopped = true;
 
 		// Remove the callback for the cached drawables or we leak
 		// the previous Home screen on orientation change
 		final int count = mApplications.size();
 		for (int i = 0; i < count; i++) {
 			mApplications.get(i).icon.setCallback(null);
 		}
 
 		unregisterReceiver(mApplicationsReceiver);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle state) {
 		super.onRestoreInstanceState(state);
 		final boolean opened = state.getBoolean(KEY_SAVE_GRID_OPENED, false);
 		if (opened) {
 			showApplications(false);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putBoolean(KEY_SAVE_GRID_OPENED, mGrid.getVisibility() == View.VISIBLE);
 	}
 
 	/**
 	 * Registers various intent receivers. The current implementation registers
 	 * only a wallpaper intent receiver to let other applications change the
 	 * wallpaper.
 	 */
 	private void registerIntentReceivers() {
 
 		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
 		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
 		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
 		filter.addDataScheme("package");
 		registerReceiver(mApplicationsReceiver, filter);
 	}
 
 	/**
 	 * Creates a new appplications adapter for the grid view and registers it.
 	 */
 	private void bindApplications() {
 		if (mGrid == null) {
 			mGrid = (GridView) findViewById(R.id.all_apps);
 		}
 		mGrid.setAdapter(new ApplicationsAdapter(this, mApplications));
 		mGrid.setSelection(0);
 
 	}
 
 	/**
 	 * Binds actions to the various buttons.
 	 */
 	private void bindButtons() {
 
 		buttons = new ArrayList<ImageView>();
 		buttons.add((ImageView) findViewById(R.id.upload));
 		buttons.add((ImageView) findViewById(R.id.connect_remote));
 
 		buttons.add((ImageView) findViewById(R.id.show_all_apps));
 		buttons.get(2).setOnClickListener(new ShowApplications());
 
 		mGrid.setOnItemClickListener(new ApplicationLauncher());
 
 		buttons.add((ImageView) findViewById(R.id.play_vod));
 		buttons.add((ImageView) findViewById(R.id.play_tv));
 		buttons.add((ImageView) findViewById(R.id.parameters));
 
 		mButtonClickedListener = new OnClickListener() {
 
 			public void onClick(View v) {
 				String packageName = "";
 				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MediaHome.this);
 				switch (v.getId()) {
 				case R.id.upload:
 					packageName = getString(R.string.package_upload);
 					break;
 				case R.id.connect_remote:
 					packageName = getString(R.string.package_remote);
 					break;
 				case R.id.play_vod:
 					boolean useVlc = preferences.getBoolean("useVlcForVOD", false);
 					if(useVlc) {
 						packageName = getString(R.string.package_vlc);
 					}
 					else {
 						packageName = getString(R.string.package_play_vod);
 					}
 					break;
 				case R.id.play_tv:
 					packageName = getString(R.string.package_play_tv);
 				}
 				try {
 					Intent launchAppIntent = getPackageManager().getLaunchIntentForPackage(packageName);
 					String key = getString(R.string.server_ip_extra_name);
 					launchAppIntent.putExtra(key,preferences.getString(key,""));
 					startActivity(launchAppIntent);
 				}
 				catch (Exception e) {
 					Toast.makeText(MediaHome.this, packageName + " is not installed", Toast.LENGTH_LONG).show();
 				}
 			}
 		};
 		buttons.get(0).setOnClickListener(mButtonClickedListener);
 		buttons.get(1).setOnClickListener(mButtonClickedListener);
 		buttons.get(3).setOnClickListener(mButtonClickedListener);
 		buttons.get(4).setOnClickListener(mButtonClickedListener);
 		mButtonTouchFeedbackListener = new OnTouchListener() {
 
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					v.setAlpha(0.5f);
 					break;
 				case MotionEvent.ACTION_UP:
 					v.setAlpha(1.0f);
 					break;
 				}
 				return false;
 			}
 		};
 		for(ImageView button : buttons) {
 			button.setOnTouchListener(mButtonTouchFeedbackListener);
 		}
 
 		buttons.get(5).setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent goToParameters = new Intent(MediaHome.this, Parameters.class);
 				startActivity(goToParameters);
 			}
 		});
 
 	}
 
 	/**
 	 * Binds clock
 	 */
 	private void bindClock() {
 		mClock = (TextView) findViewById(R.id.clock);
 
 		mTickerStopped = false;
 		mHandler = new Handler();
 
 		mCalendar = Calendar.getInstance();
 		/**
 		 * requests a tick on the next hard-second boundary
 		 */
 		mTicker = new Runnable() {
 			public void run() {
 				if (mTickerStopped) return;
 				mCalendar.setTimeInMillis(System.currentTimeMillis());
 				mClock.setText(formatTimeValue(mCalendar.get(Calendar.HOUR_OF_DAY)) + ":" + formatTimeValue(mCalendar.get(Calendar.MINUTE)));
 				long now = SystemClock.uptimeMillis();
 				long next = now + (1000 - now % 1000);
 				mHandler.postAtTime(mTicker, next);
 			}
 		};
 		mTicker.run();
 	}
 
 	/**
 	 * @param nonFormatedTime time to format to HH or MM
 	 * @return formated string
 	 */
 	String formatTimeValue(int nonFormatedTime) {
 		String formatedString = String.valueOf(nonFormatedTime);
 		if (nonFormatedTime < 10) {
 			formatedString = "0" + nonFormatedTime;
 		}
 		return formatedString;
 	}
 
 
 	/**
 	 * Loads the list of installed applications in mApplications.
 	 */
 	private void loadApplications(boolean isLaunching) {
 		if (isLaunching && mApplications != null) {
 			return;
 		}
 
 		PackageManager manager = getPackageManager();
 
 		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
 		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 
 		final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
 		Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));
 
 		if (apps != null) {
 			final int count = apps.size();
 
 			if (mApplications == null) {
 				mApplications = new ArrayList<ApplicationInfo>(count);
 			}
 			mApplications.clear();
 
 			for (int i = 0; i < count; i++) {
 				ApplicationInfo application = new ApplicationInfo();
 				ResolveInfo info = apps.get(i);
 
 				application.title = info.loadLabel(manager);
 				application.setActivity(new ComponentName(
 						info.activityInfo.applicationInfo.packageName,
 						info.activityInfo.name),
 						Intent.FLAG_ACTIVITY_NEW_TASK
 						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 				application.icon = info.activityInfo.loadIcon(manager);
 
 				mApplications.add(application);
 			}
 		}
 	}
 
 	/**
 	 * Shows all of the applications by playing an animation on the grid.
 	 */
 	private void showApplications(boolean animate) {
 		if (mBlockAnimation) {
 			return;
 		}
 		mBlockAnimation = true;
 
 		if (mShowLayoutAnimation == null) {
 			mShowLayoutAnimation = AnimationUtils.loadLayoutAnimation(
 					this, R.anim.show_applications);
 		}
 
 		if (animate) {
 			mGridEntry.setAnimationListener(new ShowGrid());
 			mGrid.startAnimation(mGridEntry);
 		}
 
 		mGrid.setVisibility(View.VISIBLE);
 
 		if (!animate) {
 			mBlockAnimation = false;
 		}
 
 	}
 
 	/**
 	 * Hides all of the applications by playing an animation on the grid.
 	 */
 	private void hideApplications() {
 		if (mBlockAnimation) {
 			return;
 		}
 		mBlockAnimation = true;
 
 		if (mHideLayoutAnimation == null) {
 			mHideLayoutAnimation = AnimationUtils.loadLayoutAnimation(
 					this, R.anim.hide_applications);
 		}
 
 		mGridExit.setAnimationListener(new HideGrid());
 		mGrid.startAnimation(mGridExit);
 		mGrid.setVisibility(View.INVISIBLE);
 
 	}
 
 
 	/**
 	 * Receives notifications when applications are added/removed.
 	 */
 	private class ApplicationsIntentReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			loadApplications(false);
 			bindApplications();
 		}
 	}
 
 	/**
 	 * GridView adapter to show the list of all installed applications.
 	 */
 	private class ApplicationsAdapter extends ArrayAdapter<ApplicationInfo> {
 		private Rect mOldBounds = new Rect();
 
 		public ApplicationsAdapter(Context context, ArrayList<ApplicationInfo> apps) {
 			super(context, 0, apps);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			final ApplicationInfo info = mApplications.get(position);
 
 			if (convertView == null) {
 				final LayoutInflater inflater = getLayoutInflater();
 				convertView = inflater.inflate(R.layout.application, parent, false);
 			}
 
 			Drawable icon = info.icon;
 
 			if (!info.filtered) {
 				final Resources resources = getContext().getResources();
 
 
 				// Problem : dimension are too low compare to available space
 				//
 				// int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
 				// int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
 				//
 
 				// 78 DIP correspond to text title size
 				// 68 keeps 5 DIP free space on each side
 				int maxSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 68, resources.getDisplayMetrics());
 
 				// Get the originale icon sizes to get ratio
 				int iconWidth = icon.getIntrinsicWidth();
 				int iconHeight = icon.getIntrinsicHeight();
 				final float ratio = (float) iconWidth / iconHeight;
 
 				// Resize the icon size to fit available size
 				if (iconWidth > iconHeight) {
 					iconWidth = maxSize;
 					iconHeight = (int) (maxSize / ratio);
 				} else if (iconHeight > iconWidth) {
 					iconWidth = (int) (maxSize * ratio);
 					iconHeight = maxSize;
 				} else if (iconHeight == iconWidth) {
 					iconWidth = iconHeight = maxSize;
 				}
 
 				final int freeHeightSpace = maxSize-iconHeight;
 				final int freeWidthSpace = maxSize-iconWidth;
 
 
 				if (icon instanceof PaintDrawable) {
 					PaintDrawable painter = (PaintDrawable) icon;
 					painter.setIntrinsicWidth(iconWidth);
 					painter.setIntrinsicHeight(iconHeight);
 				}
 
 				final Bitmap.Config c =
 						icon.getOpacity() != PixelFormat.OPAQUE ?
 								Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
 				final Bitmap thumb = Bitmap.createBitmap(maxSize, maxSize, c);
 				final Canvas canvas = new Canvas(thumb);
 				canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
 				// Copy the old bounds to restore them later
 				// If we were to do oldBounds = icon.getBounds(),
 				// the call to setBounds() that follows would
 				// change the same instance and we would lose the
 				// old bounds
 				mOldBounds.set(icon.getBounds());
 				icon.setBounds(freeWidthSpace/2, freeHeightSpace/2, iconWidth+freeWidthSpace/2, iconHeight+freeHeightSpace/2);
 				icon.draw(canvas);
 				icon.setBounds(mOldBounds);
 				icon = info.icon = new BitmapDrawable(thumb);
 				info.filtered = true;
 			}
 
 			final TextView textView = (TextView) convertView.findViewById(R.id.label);
 			textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
 			textView.setText(info.title);
 
 			return convertView;
 		}
 	}
 
 	/**
 	 * Shows and hides the applications grid view.
 	 */
 	private class ShowApplications implements View.OnClickListener {
 		public void onClick(View v) {
 			if (mGrid.getVisibility() != View.VISIBLE) {
 				showApplications(true);
 			} else {
 				hideApplications();
 			}
 		}
 	}
 
 	/**
 	 * Hides the applications grid when the layout animation is over.
 	 */
 	private class HideGrid implements Animation.AnimationListener {
 		public void onAnimationStart(Animation animation) {
 		}
 
 		public void onAnimationEnd(Animation animation) {
 			mBlockAnimation = false;
 		}
 
 		public void onAnimationRepeat(Animation animation) {
 		}
 	}
 
 	/**
 	 * Shows the applications grid when the layout animation is over.
 	 */
 	private class ShowGrid implements Animation.AnimationListener {
 		public void onAnimationStart(Animation animation) {
 		}
 
 		public void onAnimationEnd(Animation animation) {
 			mBlockAnimation = false;
 			// ViewDebug.stopHierarchyTracing();
 		}
 
 		public void onAnimationRepeat(Animation animation) {
 		}
 	}
 
 	/**
 	 * Starts the selected activity/application in the grid view.
 	 */
 	private class ApplicationLauncher implements AdapterView.OnItemClickListener {
 		public void onItemClick(AdapterView parent, View v, int position, long id) {
 			ApplicationInfo app = (ApplicationInfo) parent.getItemAtPosition(position);
 			startActivity(app.intent);
 		}
 	}
 
 
 
 
 
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event) {
 		if (event.getAction() == KeyEvent.ACTION_DOWN) {
 			switch (event.getKeyCode()) {
 			case KeyEvent.KEYCODE_BACK:
 				return true;
 			case KeyEvent.KEYCODE_HOME:
 				return true;
 			}
 		} else if (event.getAction() == KeyEvent.ACTION_UP) {
 			switch (event.getKeyCode()) {
 			case KeyEvent.KEYCODE_BACK:
 				if (!event.isCanceled()) {
 					// Do BACK behavior.
 					if (mGrid.getVisibility() == View.VISIBLE) {
 						hideApplications();
 					}
 				}
 				return true;
 			case KeyEvent.KEYCODE_HOME:
 				if (!event.isCanceled()) {
 					// Do HOME behavior.
 				}
 				return true;
 			}
 		}
 
 		return super.dispatchKeyEvent(event);
 	}
 
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if(isFirstCommandReceivedFromRemote){
 			isFirstCommandReceivedFromRemote = false;
 			for(ImageView button : buttons) {
 				button.setAlpha(0.3f);
 			}
 			setSelectedButton(2, -1);
 		}
 		else {
 			switch(keyCode) {
 			case KeyEvent.KEYCODE_DPAD_LEFT:
 				if(selectedButton == 0) {}
 				else {
 					setSelectedButton(selectedButton-1, selectedButton);
 				}
 				break;
 			case KeyEvent.KEYCODE_DPAD_RIGHT:
 				if(selectedButton == buttons.size()-1) {}
 				else {
 					setSelectedButton(selectedButton+1, selectedButton);
 				}
 				break;
 			case KeyEvent.KEYCODE_DPAD_CENTER:
 				buttons.get(selectedButton).performClick();
 				break;
 			}
 		}
 
 		return true;
 	}
 
 	private void setSelectedButton(int which, int previous) {
 		selectedButton = which;
 		int tmp = 0;
 		if(which >= 0 && which <= buttons.size()-1) {
			AlphaAnimation tmpAnim = new AlphaAnimation(buttons.get(which).getAlpha(),1.0f);
			tmpAnim.setFillAfter(true);
			buttons.get(which).startAnimation(tmpAnim);
 			//buttons.get(which).setAlpha(1.0f);
 		}
 		if(previous >= 0 && previous <= buttons.size()-1) {
 			//buttons.get(previous).startAnimation(mFadeOut);
 			AlphaAnimation tmpAnim = new AlphaAnimation(buttons.get(previous).getAlpha(),0.3f);
 			tmpAnim.setFillAfter(true);
 			buttons.get(previous).startAnimation(tmpAnim);
 			//buttons.get(previous).setAlpha(0.3f);
 		}
 	}
 
 }
