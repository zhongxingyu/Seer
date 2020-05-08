 package ch.hsr.bieridee.android.test;
 
 import android.test.ActivityInstrumentationTestCase2;
import ch.hsr.bieridee.android.activities.RegistrationScreenActivity;
 
 public class RegistrationScreenActivityTest extends ActivityInstrumentationTestCase2<RegistrationScreenActivity> {
 
 	public RegistrationScreenActivityTest() {
 		super(RegistrationScreenActivity.class);
 	}
 
 	public void testActivity() {
 		RegistrationScreenActivity activity = getActivity();
 		assertNotNull(activity);
 	}
 }
