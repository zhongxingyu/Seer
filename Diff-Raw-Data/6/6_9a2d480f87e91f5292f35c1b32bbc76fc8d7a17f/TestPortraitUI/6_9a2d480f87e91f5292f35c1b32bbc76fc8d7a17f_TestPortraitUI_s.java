 package com.mamewo.malarm_scirocco_test;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import junit.framework.Assert;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.suitebuilder.annotation.Smoke;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import asia.sonix.scirocco.SciroccoSolo;
 
 import com.jayway.android.robotium.solo.Solo;
 import com.mamewo.malarm24.*;
 
 public class TestPortraitUI
 	extends ActivityInstrumentationTestCase2<MalarmActivity>
 {
 	//TODO: get from device
 	static final
 	private int SCREEN_HEIGHT = 800;
 	static final
 	private int SCREEN_WIDTH = 480;
 
 	private final static String TAG = "malarm_scirocco_test";
 	protected SciroccoSolo solo_;
 	
 	public TestPortraitUI() {
 		super("com.mamewo.malarm24", MalarmActivity.class);
 	}
 
 	@Override
 	public void setUp() throws Exception {
 		solo_ = new SciroccoSolo(getInstrumentation(), getActivity(), "com.mamewo.malarm_test");
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		System.out.println("tearDown is called");
 		try {
 			//Robotium will finish all the activities that have been opened
 			solo_.finalize();
 		}
 		catch (Throwable e) {
 			e.printStackTrace();
 		}
 		getActivity().finish();
 		super.tearDown();
 	} 
 
 	public boolean selectPreference(int titleId) {
 		String targetTitle = solo_.getString(titleId);
 
 		TextView view = null;
 		do {
 			ArrayList<TextView> list = solo_.getCurrentTextViews(null);
 			for (TextView listText : list) {
 				Log.i(TAG, "listtext: " + listText.getText());
 				if(targetTitle.equals(listText.getText())){
 					view = listText;
 					break;
 				}
 			}
 		}
 		while(null == view && solo_.scrollDownList(0));
 		if (view == null) {
 			return false;
 		}
 		solo_.clickOnView(view);
 		return true;
 	}
 	
 	private void startPreferenceActivity() {
 		solo_.clickOnMenuItem(solo_.getString(R.string.pref_menu));
 		solo_.waitForActivity("MalarmPreference");
 		solo_.sleep(500);
 	}
 
 	@Smoke
 	public void testSetAlarm() throws Exception {
 		Date now = new Date(System.currentTimeMillis() + 60 * 1000);
 		solo_.setTimePicker(0, now.getHours(), now.getMinutes());
 		solo_.clickOnView(solo_.getView(R.id.alarm_button));
 		solo_.sleep(2000);
 		TextView targetTimeLabel = (TextView)solo_.getView(R.id.target_time_label);
 		TextView sleepTimeLabel = (TextView)solo_.getView(R.id.sleep_time_label);
 		Assert.assertTrue("check wakeup label", targetTimeLabel.getText().length() > 0);
 		Assert.assertTrue("check sleep label", sleepTimeLabel.getText().length() > 0);
 		solo_.goBack();
 		solo_.sleep(61 * 1000);
 		//TODO: wait for activity?
 		Assert.assertTrue("Switch alarm button wording", solo_.searchToggleButton(solo_.getString(R.string.stop_alarm)));
 		Assert.assertTrue("Correct alarm toggle button state", solo_.isToggleButtonChecked(solo_.getString(R.string.stop_alarm)));
 		Assert.assertTrue("check sleep label after wakeup", sleepTimeLabel.getText().length() == 0);
 		//TODO: check music?
 		//TODO: check vibration
 		//TODO: check notification
 		solo_.clickOnButton(solo_.getString(R.string.stop_alarm));
 		solo_.sleep(1000);
 		solo_.takeScreenShot();
 		Assert.assertTrue("check wakeup label", targetTimeLabel.getText().length() == 0);
 		Assert.assertTrue("check sleep label", sleepTimeLabel.getText().length() == 0);
 		Assert.assertTrue("Alarm stopped", !solo_.isToggleButtonChecked(solo_.getString(R.string.set_alarm)));
 	}
 	
 	@Smoke
 	public void testSetNow() throws Exception {
 		//cannot get timepicker of Xperia acro...
 		//TimePicker picker = solo_.getCurrentTimePickers().get(0);
 		TimePicker picker = (TimePicker)solo_.getView(R.id.timePicker1);
 		solo_.clickOnButton(solo_.getString(R.string.set_now_short));
 		//umm... yield to target activity
 		Calendar now = new GregorianCalendar();
 		solo_.sleep(200);
 		Assert.assertEquals((int)now.get(Calendar.HOUR_OF_DAY), (int)picker.getCurrentHour());
 		Assert.assertEquals((int)now.get(Calendar.MINUTE), (int)picker.getCurrentMinute());
 		solo_.takeScreenShot();
 	}
 
 	//TODO: voice button?
 	public void testNextTuneShort() {
 		View nextButton = solo_.getView(R.id.next_button);
 		solo_.clickOnView(nextButton);
 		solo_.sleep(2000);
 		//speech recognition dialog
 		//capture
 		solo_.sendKey(Solo.DELETE);
 	}
 
 	public void testNextTuneLong() {
 		View nextButton = solo_.getView(R.id.next_button);
 		solo_.clickLongOnView(nextButton);
 		solo_.sleep(2000);
 		TextView view = (TextView)solo_.getView(R.id.sleep_time_label);
 		String text = view.getText().toString();
 		Log.i(TAG, "LongPressNext: text = " + text);
 		Assert.assertTrue(text != null);
 		//TODO: check preference value...
 		Assert.assertTrue(text.length() > 0);
 		solo_.clickOnMenuItem(solo_.getString(R.string.stop_music));
 		solo_.sleep(2000);
 		String afterText = view.getText().toString();
 		Assert.assertTrue(afterText == null || afterText.length() == 0);
 	}
 
 	/////////////////
 	//test menu
 	@Smoke
 	public void testStopVibrationMenu() {
 		//TODO: cannot select menu by japanese, why?
 		solo_.clickOnMenuItem(solo_.getString(R.string.stop_vibration));
 		solo_.sleep(2000);
 	}
 	
 	@Smoke
 	public void testPlayMenu() {
 		solo_.clickOnMenuItem(solo_.getString(R.string.play_wakeup));
 		solo_.sleep(5000);
 		solo_.clickOnMenuItem(solo_.getString(R.string.stop_music));
 		solo_.sleep(1000);
 	}
 	
 	/////////////////
 	//config screen
 	@Smoke
 	public void testSitePreference() throws Exception {
 		startPreferenceActivity();
		selectPreference(R.string.playlist_path_title);
 		solo_.sleep(500);
 		//TODO: add more specific assert
 		solo_.clickInList(0);
 		solo_.clickInList(1);
 		solo_.clickInList(2);
 		solo_.takeScreenShot();
 		solo_.clickOnButton("OK");
 	}
 	
 	@Smoke
 	public void testCreatePlaylists() throws Exception {
 		startPreferenceActivity();
 		selectPreference(R.string.pref_create_playlist_title);
 		solo_.takeScreenShot();
 		solo_.sleep(5000);
 		Assert.assertTrue(true);
 	}
 	
 	@Smoke
 	public void testSleepVolume() {
 		startPreferenceActivity();
 		selectPreference(R.string.pref_sleep_volume_title);
 	}
 
 	@Smoke
 	public void testWakeupVolume() {
 		startPreferenceActivity();
 		selectPreference(R.string.pref_wakeup_volume_title);
 	}
 	
 	@Smoke
 	public void testVolumeDown() throws Exception{
 		startPreferenceActivity();
 		selectPreference(R.string.pref_wakeup_volume_title);
 		solo_.clickOnButton("-");
 		solo_.takeScreenShot();
 		solo_.clickOnButton("OK");
 		//TODO: check volume
 	}
 
 	@Smoke
 	public void testVolumeUp() throws Exception{
 		startPreferenceActivity();
 		selectPreference(R.string.pref_wakeup_volume_title);
 		solo_.clickOnButton("\\+");
 		solo_.takeScreenShot();
 		solo_.clickOnButton("OK");
 		//TODO: check volume
 	}
 
 	public void testSleepTimerPreference() throws Exception {
 		startPreferenceActivity();
 		selectPreference(R.string.pref_sleeptime);
 		solo_.sleep(500);
 		TextView view = solo_.clickInList(0).get(0);
 		String minStr = view.getText().toString();
 		int i = 0;
 		for(i = 0; i < minStr.length(); i++) {
 			char c = minStr.charAt(i);
 			if(c < '0' || '9' < c) {
 				break;
 			}
 		}
 		String minNum = minStr.substring(0, i);
 		solo_.goBack();
 		//TODO: 
 		solo_.clickOnView(solo_.getView(R.id.alarm_button));
 		solo_.sleep(2000);
 		TextView sleepTimeLabel = (TextView)solo_.getView(R.id.sleep_time_label);
 		Assert.assertTrue("check sleep label", sleepTimeLabel.getText().toString().contains(minStr));
 	}
 
 	//add double tap test of webview
 	@Smoke
 	public void testWakeupTimePreference() throws Exception {
 		int targetHour = 6;
 		int targetMin = 40;
 		startPreferenceActivity();
 		selectPreference(R.string.pref_default_time_title);
 		solo_.sleep(1000);
 		solo_.takeScreenShot();
 		solo_.setTimePicker(0, targetHour, targetMin);
 		solo_.clickOnButton("OK");
 		solo_.goBack();
 		TimePicker mainPicker = solo_.getCurrentTimePickers().get(0);
 		Assert.assertEquals(mainPicker.getCurrentHour(), Integer.valueOf(targetHour));
 		Assert.assertEquals(mainPicker.getCurrentMinute(), Integer.valueOf(targetMin));
 	}
 
 	@Smoke
 	public void testVibration() {
 		startPreferenceActivity();
 		selectPreference(R.string.pref_vibration);
 		solo_.sleep(1000);
 	}
 	
 	public void testSleepPlaylist() {
 		startPreferenceActivity();
 		selectPreference(R.string.pref_sleep_playlist);
 		solo_.waitForActivity("PlaylistViewer");
 		solo_.assertCurrentActivity("Playlist viewer should start", PlaylistViewer.class);
 	}
 	
 	public void testWakeupPlaylist() {
 		startPreferenceActivity();
 		selectPreference(R.string.pref_wakeup_playlist);
 		solo_.waitForActivity("PlaylistViewer");
 		//TODO: check title
 		solo_.assertCurrentActivity("Playlist viewer should start", PlaylistViewer.class);
 	}
 	
 	@Smoke
 	public void testPlaylistLong() throws Throwable {
 		startPreferenceActivity();
 		selectPreference(R.string.pref_sleep_playlist);
 		solo_.waitForActivity("PlaylistViewer");
 		solo_.clickLongInList(0);
 		//TODO: add check
 		solo_.takeScreenShot();
 	}
 
 	@Smoke
 	public void testReloadPlaylist() {
 		startPreferenceActivity();
 		selectPreference(R.string.pref_reload_playlist);
 	}
 	
 	@Smoke
 	public void testClearCache() {
 		startPreferenceActivity();
 		selectPreference(R.string.pref_clear_webview_cache_title);
 		solo_.sleep(500);
 	}
 
 
 	public void testHelp() throws Exception {
 		startPreferenceActivity();
 		selectPreference(R.string.help_title);
 		solo_.sleep(4000);
 		//TODO: check that browser starts
 	}
 	
 	public void testVersion() throws Exception {
 		startPreferenceActivity();
 		selectPreference(R.string.malarm_version_title);
 		solo_.sleep(500);
 		ImageView view = solo_.getImage(1);
 		solo_.takeScreenShot();
 		solo_.clickOnView(view);
 		//TODO: check that browser starts
 		solo_.sleep(4000);
 	}
 	
 	//TODO: fix!
 	public void testDoubleTouchLeft() {
 		float x = (float)(SCREEN_WIDTH / 6);
 		float y = (float)(SCREEN_HEIGHT - 100);
 		solo_.clickLongOnView(solo_.getView(R.id.loading_icon));
 		View webview = solo_.getView(R.id.webView1);
 		int[] pos = new int[2];
 		webview.getLocationOnScreen(pos);
 		Log.i("malarm_test", "view pos: " + pos[0] + " " + pos[1]);
 		
 		x = pos[0] + 40;
 		y = pos[1] + 40;
 		solo_.sleep(1000);
 		solo_.clickOnScreen(x, y);
 		solo_.clickOnScreen(x, y);
 		solo_.sleep(5000);
 		//goto prev index
 		solo_.finishOpenedActivities();
 	}
 	
 	//TODO: fix!
 	public void testDoubleTouchRight() {
 		float x = (float)(SCREEN_WIDTH - (SCREEN_WIDTH / 6));
 		float y = (float)(SCREEN_HEIGHT - 40);
 		solo_.clickOnScreen(x, y);
 		solo_.sleep(100);
 		solo_.clickOnScreen(x, y);
 		solo_.sleep(5000);
 	}
 	
 	//TODO: default config test	
 	//TODO: add test of widget
 	//TODO: playlist edit
 }
