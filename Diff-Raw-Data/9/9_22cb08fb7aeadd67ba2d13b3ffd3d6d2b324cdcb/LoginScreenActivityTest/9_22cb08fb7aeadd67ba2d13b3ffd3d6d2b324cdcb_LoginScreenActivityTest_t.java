 package ch.hsr.bieridee.android.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.CheckBox;
 import android.widget.EditText;
import ch.hsr.bieridee.android.activities.LoginScreenActivity;
 
 public class LoginScreenActivityTest extends ActivityInstrumentationTestCase2<LoginScreenActivity> {
 
 	LoginScreenActivity activity;
 
 	@Override
 	public void setUp() {
 		activity = getActivity();
 	}
 
 	public LoginScreenActivityTest() {
 		super(LoginScreenActivity.class);
 	}
 
 	public void testActivity() {
 		LoginScreenActivity activity = getActivity();
 		assertNotNull(activity);
 	}
 
 	public void testSavingLoginInformation() {
 		final String testUsername = "Chuck Norris";
 		final String testPassword = "roundhouse";
 		final boolean autologinEnabled = true;
 
 		final CheckBox autologinInputBefore;
 		final EditText usernameInputBefore;
 		final EditText passwordInputBefore;
 
 		final CheckBox autologinInputAfter;
 		final EditText usernameInputAfter;
 		final EditText passwordInputAfter;
 
 		usernameInputBefore = (EditText) activity.findViewById(ch.hsr.bieridee.android.R.id_loginscreen.inputUsername);
 		passwordInputBefore = (EditText) activity.findViewById(ch.hsr.bieridee.android.R.id_loginscreen.inputPassword);
 		autologinInputBefore = (CheckBox) activity.findViewById(ch.hsr.bieridee.android.R.id_loginscreen.checkboxAutologin);
 
 		activity.runOnUiThread(new Runnable() {
 
 			public void run() {
 				usernameInputBefore.setText(testUsername);
 				passwordInputBefore.setText(testPassword);
 				autologinInputBefore.setChecked(autologinEnabled);
 			}
 		});
 
 		activity.finish();
 
 		activity = getActivity();
 		usernameInputAfter = (EditText) activity.findViewById(ch.hsr.bieridee.android.R.id_loginscreen.inputUsername);
 		passwordInputAfter = (EditText) activity.findViewById(ch.hsr.bieridee.android.R.id_loginscreen.inputPassword);
 		autologinInputAfter = (CheckBox) activity.findViewById(ch.hsr.bieridee.android.R.id_loginscreen.checkboxAutologin);
 
 		assertEquals(testUsername, usernameInputAfter.getText().toString());
 		assertEquals(testPassword, passwordInputAfter.getText().toString());
 		assertEquals(autologinEnabled, autologinInputAfter.isChecked());
 	}
 
 //	public void testRegisterNewAccount() {
 //		final TextView registrationLink = (TextView) activity.findViewById(ch.hsr.bieridee.android.R.id_loginscreen.registrationLink);
 //		final String registrationScreenTitle = activity.getString(ch.hsr.bieridee.android.R.string.registrationscreen_title);
 //
 //		activity.runOnUiThread(new Runnable() {
 //
 //			public void run() {
 //				registrationLink.performClick();
 //
 //			}
 //		});
 //		Instrumentation instrumentation = this.getInstrumentation();
 //
 //		Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(RegistrationScreenActivity.class.getName(), null, false);
 //		Activity newActivity = instrumentation.waitForMonitorWithTimeout(monitor, 2);
 //		assertEquals(registrationScreenTitle, newActivity.getTitle());
 //		// activity.getTitle();
 //	}
 }
