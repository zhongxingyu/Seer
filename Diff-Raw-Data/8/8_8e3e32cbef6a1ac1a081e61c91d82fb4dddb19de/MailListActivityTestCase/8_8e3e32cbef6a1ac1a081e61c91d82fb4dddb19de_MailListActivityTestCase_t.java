 package net.rdrei.android.wakimail.test;
 
 import junit.framework.Assert;
 import net.rdrei.android.wakimail.ui.MailListActivity;
 import android.app.Instrumentation;
 import android.content.pm.ActivityInfo;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.UiThreadTest;
 import android.view.View;
 
 public class MailListActivityTestCase extends
 		ActivityInstrumentationTestCase2<MailListActivity> {
 
 	private MailListActivity mActivity;
 
 	public MailListActivityTestCase() {
 		super("net.rdrei.android.wakimail.ui", MailListActivity.class);
 	}
 
 	public void setUp() throws Exception {
 		super.setUp();
 
 		setActivityInitialTouchMode(false);
 		mActivity = getActivity();
 	}
 
 	@UiThreadTest
 	public void testStatePause() {
 		Instrumentation instrumentation = getInstrumentation();
 		instrumentation.callActivityOnPause(mActivity);
 		instrumentation.callActivityOnResume(mActivity);
 
 		View view = mActivity
				.findViewById(net.rdrei.android.wakimail.R.id.mail_loadingspinner_list);
 		// Make sure the correct fragment was loaded.
 		Assert.assertNotNull(view);
 	}
 
 	@UiThreadTest
 	public void testStateStopped() {
 		Instrumentation instrumentation = getInstrumentation();
 		instrumentation.callActivityOnStop(mActivity);
 		instrumentation.callActivityOnStart(mActivity);
 
 		View view = mActivity
				.findViewById(net.rdrei.android.wakimail.R.id.mail_loadingspinner_list);
 		// Make sure the correct fragment was loaded.
 		Assert.assertNotNull(view);
 	}
 
 	@UiThreadTest
 	/**
 	 * Verify that recreating the activity
 	 * (as it may happen on rotating the screen) does not
 	 * inject a Null value.
 	 */
 	public void testRecreate() {
 		mActivity
 				.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
 		View view = mActivity
				.findViewById(net.rdrei.android.wakimail.R.id.mail_loadingspinner_list);
 		Assert.assertNotNull(view);
 	}
 }
