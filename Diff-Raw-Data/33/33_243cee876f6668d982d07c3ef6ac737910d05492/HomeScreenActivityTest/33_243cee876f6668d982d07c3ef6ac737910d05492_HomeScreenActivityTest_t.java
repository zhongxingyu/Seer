 package ch.hsr.bieridee.android.test;
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import ch.hsr.bieridee.android.activities.AboutScreenActivity;
 import ch.hsr.bieridee.android.activities.HomeScreenActivity;
 import ch.hsr.bieridee.android.activities.LoginScreenActivity;
 import ch.hsr.bieridee.android.config.Auth;
 import com.jayway.android.robotium.solo.Solo;
 
 public class HomeScreenActivityTest extends ActivityInstrumentationTestCase2<HomeScreenActivity> {
 
 	private Solo solo;
 
 	public HomeScreenActivityTest() {
 		super(HomeScreenActivity.class);
 	}
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 		super.tearDown();
 	}
 
 	/**
 	 * Destroy and restart current activity.
 	 */
 	private void restartActivity() {
 		Intent testIntent = new Intent(getActivity().getBaseContext(), HomeScreenActivity.class);
 		getActivity().finish();
 		getActivity().startActivity(testIntent);
 	}
 
 	/**
 	 * Clear authentication information and restart activity.
 	 */
 	private void testWithoutLogin() {
 		Auth.clearAuth();
 		this.restartActivity();
 	}
 
 	/**
 	 * Set dummy authentication information and restart activity.
 	 */
 	private void testWithLogin() {
 		Auth.setAuth("testuser", "$2$10$ae5deb822e0d719929004uD0KL0l5rHNCSFKcfBvoTzG5Og6O/Xxu");
 		this.restartActivity();
 	}
 
 	/**
 	 * Test basic parts of the activity.
 	 */
 	public void testDashboardItems() {
 		this.testWithLogin();
 		assertTrue(solo.searchText("Bierliste"));
 		assertTrue(solo.searchText("Timeline"));
 		assertTrue(solo.searchText("Brauereien"));
 		assertTrue(solo.searchText("Profil"));
		assertTrue(solo.searchText("Bier erfassen"));
 		assertTrue(solo.searchText("Rating"));
 	}
 
 	/**
 	 * Should display the home screen activity.
 	 */
 	public void testLoggedIn() {
 		this.testWithLogin();
 		getInstrumentation().callActivityOnRestart(getActivity());
 		assertTrue(solo.searchText("Bieridee Dashboard"));
 		solo.assertCurrentActivity("Expected home screen activity to stay open.", HomeScreenActivity.class);
 	}
 
 	/**
 	 * Should redirect to login view.
 	 */
 	public void testLoggedOut() {
 		this.testWithoutLogin();
 		getInstrumentation().callActivityOnRestart(getActivity());
 		solo.assertCurrentActivity("Expected login activity to launch.", LoginScreenActivity.class);
 	}
 
 	public void testLogoutButton() {
 		this.testWithLogin();
 		assertTrue("Expected auth data to be available.", Auth.dataAvailable());
 		solo.assertCurrentActivity("Expected home screen activity to be active.", HomeScreenActivity.class);
 		solo.sendKey(Solo.MENU);
 		solo.clickOnText("Logout");
 		assertFalse("Expected auth data to be gone.", Auth.dataAvailable());
 		solo.assertCurrentActivity("Expected login activity to launch.", LoginScreenActivity.class);
 	}
 
 	public void testAboutButton() {
 		this.testWithLogin();
 		solo.assertCurrentActivity("Expected home screen activity to be active.", HomeScreenActivity.class);
 		solo.sendKey(Solo.MENU);
 		solo.clickOnText("Über");
 		solo.assertCurrentActivity("Expected about screen activity to launch.", AboutScreenActivity.class);
 	}
 }
