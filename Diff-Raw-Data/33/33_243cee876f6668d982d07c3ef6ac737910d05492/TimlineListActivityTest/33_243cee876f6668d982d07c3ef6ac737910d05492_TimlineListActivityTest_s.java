 package ch.hsr.bieridee.android.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import ch.hsr.bieridee.android.activities.TimelineListActivity;
 import ch.hsr.bieridee.android.config.Auth;
 
 import com.jayway.android.robotium.solo.Solo;
 
 public class TimlineListActivityTest extends ActivityInstrumentationTestCase2<TimelineListActivity> {
 
 	private Solo solo;
 
 	public TimlineListActivityTest() {
 		super(TimelineListActivity.class);
 	}
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
		Auth.setAuth("testuser", "testpass", true);
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 		super.tearDown();
 	}
 
 	/**
 	 * Test activity content.
 	 */
 	public void testActivityContent() {
 		assertTrue(solo.searchText("danilo felt thirsty")); // title
 		assertTrue(solo.searchText("alki rated Kilkenny")); // beer name
 	}
 }
