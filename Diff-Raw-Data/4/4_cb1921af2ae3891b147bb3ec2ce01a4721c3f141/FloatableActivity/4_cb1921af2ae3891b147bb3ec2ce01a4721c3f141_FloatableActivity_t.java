 package com.lge.app.floating;
 
 import java.util.*;
 
 import android.app.*;
 import android.content.*;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.graphics.*;
 import android.graphics.drawable.*;
 import android.os.*;
 import android.util.*;
 import android.view.*;
 import android.widget.*;
 
 /**
  * Activity that can be converted into a floating window. Extend this class
  * instead of the Activity class.
  * 
  */
 public abstract class FloatableActivity extends Activity {
 	/**
 	 * Intent extra for launching an activity in a floating mode, not full
 	 * screen mode. If this extra is set to true, the activity life cycle
 	 * methods such as onCreate, onStart, onResume are called but the full
 	 * screen UI is never displayed. Instead, just after onResume, the activity
 	 * immediately switches into the floating mode.
 	 * 
 	 * This extra is recognizable only to the activities extending
 	 * FloatableActivity.
 	 */
 	public final static String EXTRA_LAUNCH_AS_FLOATING = "com.lge.app.floating.launchAsFloating";
 
 	/**
 	 * If this is set to true, it is considered that this activity is being
 	 * started because it is returning from floating mode
 	 */
 	private final static String EXTRA_RETURN_FROM_FLOATING = "com.lge.app.floating.returnFromFloating";
 
 	private final static String TAG = FloatableActivity.class.getSimpleName();
 
 	// user has requested to switch into floating mode
 	// this is automatically cleared when the floating window is created and
 	// this activity is attached to the window
 	private volatile boolean mIsSwitchingToFloatingMode = false;
 
 	// just for speed up
 	private String mActivityName = null;
 
 	// the intent that recently started this activity
 	private Intent mCurrentIntent = null;
 
 	// the attached floating window
 	private FloatingWindow mFloatingWindow = null;
 
 	// the view to be shown in the floating window
 	private View mContentView = null;
 
 	// whether to automatically finish this activity when it is switched into
 	// the floating mode
 	private boolean mDontFinishActivity = false;
 
 	// task ID of this activity. this is used only when mDontFinishActivity is
 	// true
 	private int mTaskId = -1;
 
 	// current state of this activity
 	private State mState = State.CREATE;
 
 	private static enum State {
 		CREATE, START, RESUME, PAUSE, STOP, DESTROY, NEWINTENT, RESTART
 	}
 
 	private static class ReceiverRegisterInfo {
 		public BroadcastReceiver receiver;
 		public IntentFilter filter;
 		public String broadcastPermission;
 		public Handler scheduler;
 		public Intent intent;
 		public boolean isRegistered = false;
 	}
 
 	private List<ReceiverRegisterInfo> mReceiverRegisterInfos = new ArrayList<ReceiverRegisterInfo>();
 
 	/**
 	 * Set the view that is displayed for the floating mode. If we are currently
 	 * in the floating mode, the view is immediately changed. If not, the
 	 * setting will take effect just after this activity is switched into the
 	 * floating mode.
 	 * 
 	 * @param contentView
 	 *            the root of the view structure
 	 */
 	public void setContentViewForFloatingMode(View contentView) {
 		mContentView = contentView;
 		if (isInFloatingMode()) {
 			getFloatingWindow().setContentView(mContentView);
 		}
 	}
 
 	/**
 	 * Set the view that is displayed for the floating mode. If we are currently
 	 * in the floating mode, the view is immediately changed. If not, the
 	 * setting will take effect just after this activity is switched into the
 	 * floating mode.
 	 * 
 	 * @param resId
 	 *            resource Id of the view layout
 	 */
 	public void setContentViewForFloatingMode(int resId) {
 		View v = this.getLayoutInflater().inflate(resId, null);
 		if (v != null) {
 			setContentViewForFloatingMode(v);
 		}
 	}
 
 	/**
 	 * Get the view for the floating mode.
 	 * 
 	 * @return the root of the view structure
 	 */
 	public View getContentViewForFloatingMode() {
 		return mContentView;
 	}
 
 	/**
 	 * Shortcut for switchingToFloatingMode(true, false, true, null)
 	 * 
 	 */
 	public void switchToFloatingMode() {
 		switchToFloatingMode(true, false, true, null);
 	}
 
 	/**
 	 * Convenience method for switchToFloatingMode(FloatingWindow.LayoutParam)
 	 * 
 	 * @param useOverlay
 	 *            set true if the floating window supports the overlay feature
 	 * @param useOverlappingTitle
 	 *            set false if the floating window has title bar over the
 	 *            content area. If set true, the title bar will be overlapped
 	 *            with the content area.
 	 * @param isResizable
 	 *            set true if user should be able to resize the floating window.
 	 *            Resize can be done by dragging the right-bottom corner of the
 	 *            window.
 	 * @param initialRegion
 	 *            initial region of the floating window. If null, the default
 	 *            region (the last region) is used.
 	 */
 	public void switchToFloatingMode(boolean useOverlay, boolean useOverlappingTitle, boolean isResizable,
 			Rect initialRegion) {
 		FloatingWindow.LayoutParams params = new FloatingWindow.LayoutParams(this);
 		params.useOverlay = useOverlay;
 		params.useOverlappingTitle = useOverlappingTitle;
 		params.resizeOption = isResizable ? FloatingWindow.ResizeOption.ARBITRARY
 				: FloatingWindow.ResizeOption.DISABLED;
 		if (initialRegion != null) {
 			params.x = initialRegion.left;
 			params.y = initialRegion.top;
 			params.width = initialRegion.width();
 			params.height = initialRegion.height();
 		}
 		switchToFloatingMode(params);
 	}
 
 	/**
 	 * Switch this activity into the floating mode. If the activity is already
 	 * in floating mode, nothing happens. If not, this activity is automatically
 	 * terminated by calling finish(). Then a floating window is created and the
 	 * current UI is attached to the window.
 	 * 
 	 * Since this method automatically calls finish() method, onPause(),
 	 * onStop(), and onDestroy() methods will be called in sequence. It is
 	 * important that you should not deallocated any resource that will be used
 	 * in the floating mode.
 	 * 
 	 * In order for you to distinguish termination caused by calling
 	 * switchToFloatingMode() from the normal termination, you should use
 	 * isSwitchingToFloatingMode() method in your onPause(), onStop() and
 	 * onDestroy() method. You can only deallocated your resource only when
 	 * isSwitchingToFloatingMode() returns false.
 	 * 
 	 * @param params
 	 *            information about how the floating window is shown and behave
 	 */
 	public void switchToFloatingMode(FloatingWindow.LayoutParams params) {
 		Log.i(TAG, "switching to floating mode requested");
 
 		if (isInFloatingMode()) {
 			Log.i(TAG, "Activity " + mActivityName + " is currently in floating mode. No nothing.");
 			return; // TODO: generate an exception at this point
 		}
 
 		if (mIsSwitchingToFloatingMode) {
 			Log.i(TAG, "Activity " + mActivityName
 					+ " is currently switching to floating mode. Ignoring duplicated request.");
 			return; // TODO: generate an exception at this point
 		}
 
 		if (mState == State.PAUSE || mState == State.STOP || mState == State.DESTROY) {
 			Log.i(TAG, "Activity " + mActivityName + " is currnently in " + mState
 					+ " mode. In this mode, switching to floating is not possible.");
 			return; // TODO: generate an exception at this point
 		}
 
 		FloatingWindow window = getFloatingWindowManager().getFloatingWindowFor(mActivityName);
 		if (window != null) {
 			Log.w(TAG, "There already is a floating window for activity " + mActivityName);
 			// if this activity is originally started as a floating mode and
 			// there is a floating window for this activity, simply give
 			// focus to the window, move it to top. This activity is simply
 			// finished.
 			if (isStartedAsFloating()) {
 				window.gainFocus();
 				window.moveToTop();
 				finish();
 			}
 			// if this activity is originally started as a normal (full
 			// screen) mode and there is a floating window for this activity,
 			// close the
 			// window (because we only allow one floating window for an
 			// activity)
 			else {
 				window.close();
 			}
 		}
 
 		// if there is no floating window for this activity, create it
 		// attaching the window is automatically done in onStop()
 		else {
 			forceSwitchToFloatingMode(params);
 		}
 		// TODO: do we need to return FloatingWindow instance here?
 	}
 
 	private void forceSwitchToFloatingMode(FloatingWindow.LayoutParams params) {
 		// from now on, it is considered that this activity is switching to
 		// floating mode
 		mIsSwitchingToFloatingMode = true;
 		if (mCurrentIntent != null) {
 			mCurrentIntent.removeExtra(EXTRA_RETURN_FROM_FLOATING);
 		}
 
 		// try to recall the last known position and size.
 		// if failed since it is the first time, use the given size and position
 		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
 		params.x = prefs.getInt("floating_x", params.x);
 		params.y = prefs.getInt("floating_y", params.y);
 		params.width = prefs.getInt("floating_w", params.width);
 		params.height = prefs.getInt("floating_h", params.height);
 		// TODO: key names must be defined as constants
 
 		assert (mFloatingWindow == null);
 
 		// creates a floating window structure.
 		// note that this DOES NOT add the window to the underlying
 		// android window manager. It is done by the FloatingWindowService
 		mFloatingWindow = getFloatingWindowManager().createFloatingWindow(this, params);
 
 		// if this activity is currently running, dismiss it now.
 		// If it is not, dismiss it when it starts to run in onPostResume()
 		if (mState == State.RESUME) {
 			dismissCurrentActivity();
 		}
 	}
 
 	private void dismissCurrentActivity() {
 		Log.i(TAG, "dismiss current activity. activity=" + mActivityName);
 		// if user has not specified a content view for floating mode,
 		// use the current view automatically.
 		if (mContentView == null) {
 			if (getWindow() != null && getWindow().getDecorView() != null) {
 				mContentView = ((ViewGroup) getWindow().getDecorView()).getChildAt(0);
 			}
 		}
 
 		// by default, this activity is destroyed (from the activity stack) when
 		// it is switched into the floating mode. However, if
 		// setDontFinishOnFloatingMode(true)
 		// is invoked, this activity is not destroyed. Instead, it is made
 		// invisible and the
 		// current task is moved to background
 		if (!mDontFinishActivity) {
 			// unregister receivers before being destroyed
 			for (ReceiverRegisterInfo info : mReceiverRegisterInfos) {
 				if (info.isRegistered) {
 					try {
 						this.unregisterReceiver(info.receiver);
 					} catch (IllegalArgumentException e) {
 						Log.i(TAG, "receiver " + info.receiver + " is already unregistered");
 					}
 				}
 			}
 
 			finish();
 		} else {
 			setVisible(false);
 
 			// find the task id that this activity is in
 			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 			List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(100); // TODO:
 																					// 100
 																					// is
 																					// enough?
 			for (ActivityManager.RunningTaskInfo ti : tasks) {
 				ComponentName topActivityName = ti.topActivity;
 				if (topActivityName.getPackageName().equals(this.getPackageName())
 						&& topActivityName.getClassName().equals(this.getClass().getName())) {
 					Log.i(TAG, "task id=" + ti.id);
 					mTaskId = ti.id;
 					break;
 				}
 			}
 
 			// move current task to background
 			this.moveTaskToBack(true);
 		}
 	}
 
 	/**
 	 * Informs that this activity should not be finished even it is switched
 	 * into the floating mode. This this method is not invoked, the default
 	 * behavior is finishing this activity. For this method to take effect, it
 	 * must be invoked before switchToFloatingMode().
 	 * 
 	 * @param dontfinish
 	 *            true if it should not be killed.
 	 */
 	public void setDontFinishOnFloatingMode(boolean dontfinish) {
 		if (dontfinish) {
 			Log.i(TAG, "Activity " + mActivityName + " is configured to not be destroyed when in floating mode.");
 		}
 		mDontFinishActivity = dontfinish;
 	}
 
 	/**
 	 * Returns the window manager that manages floating windows in this app.
 	 * There is only one instance of the window manager in an app.
 	 * 
 	 * @return the floating window manager
 	 */
 	public FloatingWindowManager getFloatingWindowManager() {
 		return FloatingWindowManager.getDefault(this.getApplicationContext());
 	}
 
 	/**
 	 * Tests if this activity is started in a floating mode.
 	 * 
 	 * @return true if the activity is displayed in a floating mode. false if
 	 *         the activity is started to have a normal full screen window.
 	 */
 	public boolean isStartedAsFloating() {
 		return mCurrentIntent != null && mCurrentIntent.getBooleanExtra(EXTRA_LAUNCH_AS_FLOATING, false);
 	}
 
 	/**
 	 * Tests if this activity is switching from a floating mode to full mode
 	 * 
 	 * @return true if the activity has returned from a floating mode. false if
 	 *         not.
 	 */
 	public boolean isSwitchingToFullMode() {
 		return mCurrentIntent != null && mCurrentIntent.getBooleanExtra(EXTRA_RETURN_FROM_FLOATING, false)
 				&& !isSwitchingToFloatingMode();
 	}
 
 	/**
 	 * Tests if this activity is switching to the floating mode. Use this method
 	 * in onPause(), onStop() and onDestroy() method to test whether to
 	 * deallocated resources or not. If this returns true, DO NOT deallocate the
 	 * resources as they will be used in the floating mode.
 	 * 
 	 * @return true if this activity is currently switching to the floating
 	 *         mode.
 	 */
 	public boolean isSwitchingToFloatingMode() {
 		return mIsSwitchingToFloatingMode;
 	}
 
 	/**
 	 * Finishes the floating mode. This is actually the same as clicking the 'x'
 	 * button on the floating window.
 	 */
 	public void finishFloatingMode() {
 		if (!isInFloatingMode()) {
 			Log.i(TAG, "Activity " + mActivityName
 					+ " is currently not in floating mode, thus finishing is impossible.");
 			return;
 		}
 		if (mFloatingWindow != null) {
 			mFloatingWindow.close();
 		}
 	}
 
 	/**
 	 * Tests whether the activity is in floating mode.
 	 * 
 	 * @return true if in floating mode.
 	 */
 	public boolean isInFloatingMode() {
 		return mFloatingWindow != null && mIsAttached;
 	}
 
 	/**
 	 * Get the reference to the floating window.
 	 * 
 	 * @return the reference to the floating window. null if the floating window
 	 *         has not been created.
 	 */
 	public FloatingWindow getFloatingWindow() {
 		return mFloatingWindow;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		mState = State.CREATE;
 		Log.i(TAG, "on create activity=" + mActivityName + "(" + this + ")");
 		if (!FloatingWindowService.checkExistence(this)) {
 			Toast.makeText(this, "FloatingWindowService cannot be found. Please see logcat for further information.",
 					Toast.LENGTH_LONG).show();
 		}
 		mActivityName = this.getClass().getName();
 		Log.i(TAG, "on create activity=" + mActivityName);
 		super.onCreate(savedInstanceState);
 		mCurrentIntent = getIntent();
 
 		// if this activity is already running as a floating app..
 		// quit this activity and make the floating app to return to full mode
 		FloatingWindow window = getFloatingWindowManager().getFloatingWindowFor(mActivityName);
 		if (window != null && !this.isStartedAsFloating()) {
 			Log.i(TAG, "found floating window for activity" + mActivityName);
 			window.close(true);
 			finish();
 		}
 	}
 
 	@Override
 	protected void onStart() {
 		mState = State.START;
 		Log.i(TAG, "on start activity=" + mActivityName + "(" + this + ")");
 		super.onStart();
 
 		// mark that the floating window service can now be called
 		mServiceStartRequested = false;
 		
 		if (isStartedAsFloating() && !isInFloatingMode()) {
 			handleStartedAsFloatingMode();			
 		}
 	}
 
 	@Override
 	protected void onRestart() {
 		mState = State.RESTART;
 		Log.i(TAG, "on restart activity=" + mActivityName + "(" + this + ")");
 		super.onRestart();
 		
 		// task is brought to front.
 		if (isInFloatingMode() && mDontFinishActivity) {
 			Log.i(TAG, "found floating window for activity" + mActivityName);
 			if (isStartedAsFloating()) {
 				// if restarted as floating mode, the activity itself should go background again
 				this.moveTaskToBack(true);
 			}
 			else {				
 				// if restarted as full mode, make the floating window to return to full mode
 				FloatingWindow window = getFloatingWindowManager().getFloatingWindowFor(mActivityName);
 				window.close(true);
 			}
 		}
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		mState = State.NEWINTENT;
 		Log.i(TAG, "on new intent activity=" + mActivityName + "(" + this + ")");
 		super.onNewIntent(intent);
 		mCurrentIntent = intent;
 	}
 
 	@Override
 	// for debugging
 	protected void onResume() {
 		mState = State.RESUME;
 		Log.i(TAG, "on resume activity=" + mActivityName + "(" + this + ")");
 		super.onResume();
 	}
 
 	@Override
 	protected void onPostResume() {
 		super.onPostResume();
 
 		// mark that the floating window service can now be called
 		mServiceStartRequested = false;
 		//TODO: same code at onStart(). both needed?
 		
 		// if this activity is currently switching to floating mode
 		// dismiss the activity so that it no longer visible to user
 		if (mIsSwitchingToFloatingMode) {
 			dismissCurrentActivity();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		mState = State.PAUSE;
 		Log.i(TAG, "on pause activity=" + mActivityName + "(" + this + ")");
 		super.onPause();
 		startFloatingService();
 	}
 
 	@Override
 	protected void onStop() {
 		mState = State.STOP;
 		Log.i(TAG, "on stop activity=" + mActivityName + "(" + this + ")");
 		super.onStop();
 		startFloatingService();
 		if (mIsAttached && mIsSwitchingToFloatingMode && mDontFinishActivity) {
 			mIsSwitchingToFloatingMode = false;
 		}
 	}
 	
 	private boolean mServiceStartRequested = false;
 	
 	private void startFloatingService() {
 		if (mIsSwitchingToFloatingMode && !mServiceStartRequested) {
 			// starts the service so that the floating window is actually added
 			// to the screen and becomes visible
 			Intent intent = new Intent(this, FloatingWindowService.class);
 			intent.putExtra(FloatingWindowService.EXTRA_KEY, mActivityName);
 			startService(intent);
 			//prevent the floating window service from started twice
 			mServiceStartRequested = true;
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		mState = State.DESTROY;
 		Log.i(TAG, "on destroy activity=" + mActivityName + "(" + this + ")");
 		super.onDestroy();
 		if (mIsAttached && mIsSwitchingToFloatingMode && !mDontFinishActivity) {
 			mIsSwitchingToFloatingMode = false;
 		}
 	}
 
 	@Override
 	// for debugging
 	public void onDetachedFromWindow() {
 		super.onDetachedFromWindow();
 		Log.i(TAG, "on detached from window activity=" + mActivityName);
 	}
 
 	@Override
 	// for debugging
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		Log.i(TAG, "on attached from window activity=" + mActivityName);
 	}
 
 	private ReceiverRegisterInfo findRegisterInfo(BroadcastReceiver receiver) {
 		for (ReceiverRegisterInfo info : mReceiverRegisterInfos) {
 			if (info.receiver == receiver) {
 				return info;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
 		return this.registerReceiver(receiver, filter, null, null);
 	}
 
 	@Override
 	public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission,
 			Handler scheduler) {
 		Log.i(TAG, "register receiver=" + receiver + " filter=" + filter + " permission=" + broadcastPermission);
 
 		if (receiver == null) {
 			return super.registerReceiver(receiver, filter, broadcastPermission, scheduler);
 		}
 
 		// every non-null receiver registration is recorded
 		ReceiverRegisterInfo info = findRegisterInfo(receiver);
 		if (info != null && info.isRegistered) {
 			Log.i(TAG, "receiver " + receiver + " is already registered");
 			return info.intent;
 		} else {
 			if (info == null) {
 				info = new ReceiverRegisterInfo();
 				mReceiverRegisterInfos.add(info);
 			}
 			info.receiver = receiver;
 			info.filter = filter;
 			info.broadcastPermission = broadcastPermission;
 			info.scheduler = scheduler;
 			info.isRegistered = true;
 			info.intent = super.registerReceiver(receiver, filter, broadcastPermission, scheduler);
 			return info.intent;
 		}
 	}
 
 	@Override
 	public void unregisterReceiver(BroadcastReceiver receiver) {
 		Log.i(TAG, "unregister receiver: " + receiver);
 
 		if (receiver == null) {
 			super.unregisterReceiver(receiver);
 			return;
 		}
 
 		// every non-null receiver unregistration is marked
 		ReceiverRegisterInfo info = findRegisterInfo(receiver);
 		if (info == null || !info.isRegistered) {
 			Log.i(TAG, "receiver " + receiver + " is already unregistered");
 			return;
 		} else {
 			super.unregisterReceiver(info.receiver);
 			info.isRegistered = false;
 		}
 	}
 
 	private boolean mIsAttached = false;
 	
 	/* package */void handleAttachToFloatingWindow(FloatingWindow w) {
 		assert (mFloatingWindow != null);
 		mIsAttached = true;
 		if (mContentView != null) {
 			// if contentView is already added to a parent, remove it from
 			// the parent before adding it to the floating window
 			Log.i(TAG, "view is being transferred from full-screen window to floating window");
 			ViewParent p = mContentView.getParent();
 			if (p != null && p instanceof ViewGroup) {
 				((ViewGroup) p).removeView(mContentView);
 			}
 			mFloatingWindow.setContentView(mContentView);
 		}
 
 		if (!mDontFinishActivity) {
 			Log.i(TAG, "re-registering receivers for activity" + mActivityName);
 			for (ReceiverRegisterInfo info : mReceiverRegisterInfos) {
 				Log.i(TAG, "receiver=" + info.receiver);
 				getApplicationContext().registerReceiver(info.receiver, info.filter, info.broadcastPermission,
 						info.scheduler);
 				// TODO: handle IllegalArgumentException?
 			}
 		}
 				 
 		
 		// if we finish this activity for floating mode, the switching is not completed
 		// until we execute onDestroy()
 		if (!mDontFinishActivity) {
 			if (mState == State.PAUSE) {
 				//do nothing. switching is not completed yet.
 			}
 			else if (mState == State.STOP){
 				//do nothing. switching is not completed yet.
 			}
 			else if (mState == State.DESTROY) {
 				// switching is completed
 				mIsSwitchingToFloatingMode = false;
 			}
 			else {
 				// cannot happen
 				assert(true);
 			}
 		}
 		// if we don't finish this activity, the switching is not completed
 		// while we are in pause state
 		else {
 			if (mState == State.PAUSE) {
 				//do nothing. switching is not completed yet.
 			}
 			else if (mState == State.STOP) {
 				//switching is completed
 				mIsSwitchingToFloatingMode = false;
 			}
 			else {
 				// cannot happen
 				assert(true);
 			}
 		}
 
 		onAttachedToFloatingWindow(w);
 	}
 
 	/**
 	 * This is called when the floating window becomes visible to the user.
 	 * 
 	 * @param w
 	 *            the floating window
 	 */
 	public void onAttachedToFloatingWindow(FloatingWindow w) {
 		/* default implementation does nothing here */
 	}
 
 	/* package */void handleDetachFromFloatingWindow(FloatingWindow w, boolean isReturningToFullScreen) {
 		Log.i(TAG, "handleDetachFromFloatingWindow=" + mActivityName + "(" + this + ")");
 		boolean autoRelaunch = onDetachedFromFloatingWindow(w, isReturningToFullScreen);
 		mIsAttached = false;
 		mFloatingWindow = null; // from now on, isInFloatingMode() returns false
 		mIsSwitchingToFloatingMode = false; 
 		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
 		Editor editor = prefs.edit();
 		editor.putInt("floating_x", w.getLayoutParams().x);
 		editor.putInt("floating_y", w.getLayoutParams().y);
 		editor.putInt("floating_w", w.getLayoutParams().width);
 		editor.putInt("floating_h", w.getLayoutParams().height);
 		editor.commit();
 		if (autoRelaunch) {
 			if (isReturningToFullScreen) {
 				if (!mDontFinishActivity) {
 					Log.i(TAG, "unregistering receivers for activity" + mActivityName);
 					for (ReceiverRegisterInfo info : mReceiverRegisterInfos) {
 						Log.i(TAG, "receiver=" + info.receiver);
 						try {
 							getApplicationContext().unregisterReceiver(info.receiver);
 						} catch (IllegalArgumentException e) {
 							Log.i(TAG, "receiver " + info.receiver + " is already unregistered");
 						}
 					}
 					// if this activity is finished, then re launch the
 					// activity.
 					Intent intent = new Intent(mCurrentIntent);
 					intent.removeExtra(EXTRA_LAUNCH_AS_FLOATING);
 					intent.putExtra(EXTRA_RETURN_FROM_FLOATING, true);
 					Log.i(TAG, "relaunching. intent=" + intent.toString());
 					startActivity(intent);
 				} else {
 					// if this activity is not finished...
 
 					// 1. make it visible again and attach the view to the
 					// window
 					setVisible(true);
 					View myView = w.getContentView();
 					ViewGroup decorView = (ViewGroup)getWindow().getDecorView();
 					Log.i(TAG, "decor view =" + decorView);
 					w.setContentView(null);
 					if (mSavedWindowBackground != null) {
 						decorView.setBackgroundDrawable(mSavedWindowBackground);
 					}
 					decorView.addView(myView);
 					//FIX: dirty hack. removing the decor view and adding it again 
 					//in order to re-layout the entire window
 					WindowManager.LayoutParams params = getWindow().getAttributes();
 					if (params != null) {
 						try {
 							getWindow().getWindowManager().removeViewImmediate(decorView);
 							getWindow().getWindowManager().addView(decorView, params);
 						}
 						catch (IllegalStateException e) {
 							// do nothing
 						}
 						catch (IllegalArgumentException e) {
 							// do nothing
 						}
 					}
 					
 					// 2. modify the extra flag since the activity is now
 					// switching to
 					// full screen mode
 					mCurrentIntent.removeExtra(EXTRA_LAUNCH_AS_FLOATING);
 					mCurrentIntent.putExtra(EXTRA_RETURN_FROM_FLOATING, true);
 
 					// 3. bring the task to foreground
 					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 					am.moveTaskToFront(mTaskId, 0);
 				}
 			} else {
 				if (mDontFinishActivity) {
 					setVisible(false);
 					ColorDrawable nullDrwable = new ColorDrawable(0);
 					mSavedWindowBackground = getWindow().getDecorView().getBackground();
 					getWindow().setBackgroundDrawable(nullDrwable);
 					// since user has not requested to return to full screen,
 					// and the activity is still not finished, finish it here
 					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 					am.moveTaskToFront(mTaskId, 0);
 					finish();
 				}
 			}
 		}
 	}
 
 	/**
 	 * This is called when the floating window is closed. Floating window can be
 	 * closed in two ways. 1) user closes the window by pressing the 'x' button.
 	 * 2) user returns to the full screen mode by pressing the 'back' button.
 	 * This two cases can be distinguished by isReturningFullScreen argument. By
 	 * default this method re-launches the original activity when
 	 * isReturningToFullScreen is true. If you want to change this default
 	 * behavior, you have to override this method in your activity and should
 	 * return false in order to disable the default behavior.
 	 * 
 	 * @param w
 	 *            the floating window
 	 * @param isReturningToFullScreen
 	 *            true if user returns to the full screen mode. false if user
 	 *            just closes the window.
 	 * @return true if the automatic re-launch is required. false if the
 	 *         re-launch is handled by the activity itself.
 	 */
 	public boolean onDetachedFromFloatingWindow(FloatingWindow w, boolean isReturningToFullScreen) {
 		/* default implies automatic re-launch of the activity */
 		return true;
 	}
 	
	public void setViewForConfigChanged() {
		/* default implementation does nothing here */
	}
	
 	public void setViewForConfigChanged(Configuration newConfig) {
 		/* default implementation does nothing here */
 	}
 
 	private Drawable mSavedWindowBackground = null;
 	private void handleStartedAsFloatingMode() {
 		boolean proceed = onStartedAsFloatingMode();
 		if (proceed) {
 			Log.i(TAG, "Activity " + mActivityName
 					+ " started as floating mode. Automatically switching to floating mode");			
 			setVisible(false);
 			ColorDrawable nullDrwable = new ColorDrawable(0);
 			mSavedWindowBackground = getWindow().getDecorView().getBackground();
 			getWindow().setBackgroundDrawable(nullDrwable);
 			switchToFloatingMode();
 		}
 		else {
 			Log.i(TAG, "Activity " + mActivityName 
 					+ " started as floating mode, but app decided not to enter into floating mode");
 		}
 	}
 
 	/**
 	 * This is called when the activity is started as floating mode. If this
 	 * method is not overridden, the activity is by default switched into
 	 * floating mode. If you want to prevent this activity from switching into
 	 * floating mode, override this method and return false.
 	 * 
 	 * @return true if this activity is allowed to enter into floating mode. false if not.
 	 */
 	protected boolean onStartedAsFloatingMode() {
 		return true;
 	}
 }
