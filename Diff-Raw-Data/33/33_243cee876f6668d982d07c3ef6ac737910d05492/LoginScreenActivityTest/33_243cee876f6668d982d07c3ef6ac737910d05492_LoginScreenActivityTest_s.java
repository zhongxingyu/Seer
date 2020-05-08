 package ch.hsr.bieridee.android.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import ch.hsr.bieridee.android.activities.LoginScreenActivity;
 
 public class LoginScreenActivityTest extends ActivityInstrumentationTestCase2<LoginScreenActivity> {
 
 	private LoginScreenActivity activity;
 
 	@Override
 	public void setUp() {
 		activity = getActivity();
 	}
 
 	public LoginScreenActivityTest() {
 		super(LoginScreenActivity.class);
 	}
 
 	public void testActivity() {
 		assertNotNull(activity);
 	}
 
 	public void testSavingLoginInformation() {
 		// Set test data
		final String testUsername = "Chuck Norris";
		final String testPassword = "roundhouse";
 		final boolean autologinEnabled = true;
 
 		// Initialize GUI elements
 		final EditText usernameInputBefore;
 		final EditText passwordInputBefore;
 
 		final CheckBox autologinInputAfter;
 		final EditText usernameInputAfter;
 		final EditText passwordInputAfter;
 
 		usernameInputBefore = (EditText) activity.findViewById(ch.hsr.bieridee.android.R.id_loginscreen.inputUsername);
 		passwordInputBefore = (EditText) activity.findViewById(ch.hsr.bieridee.android.R.id_loginscreen.inputPassword);
 
 		// Set test data
 		activity.runOnUiThread(new Runnable() {
 
 			public void run() {
 				usernameInputBefore.setText(testUsername);
 				passwordInputBefore.setText(testPassword);
 			}
 		});
 
 		// Restart activity
 		activity.finish();
 		activity = getActivity();
 		getInstrumentation().waitForIdleSync();
 
 		// Verify data (should have been stored)
 		usernameInputAfter = (EditText) activity.findViewById(ch.hsr.bieridee.android.R.id_loginscreen.inputUsername);
 		passwordInputAfter = (EditText) activity.findViewById(ch.hsr.bieridee.android.R.id_loginscreen.inputPassword);
 
 		assertEquals(testUsername, usernameInputAfter.getText().toString());
 		assertEquals(testPassword, passwordInputAfter.getText().toString());
 	}
 }
