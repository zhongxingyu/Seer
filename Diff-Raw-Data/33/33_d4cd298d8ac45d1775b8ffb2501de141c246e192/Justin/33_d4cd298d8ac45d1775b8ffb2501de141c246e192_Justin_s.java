 package edu.gatech.oad.rocket.findmythings.test;
 
import edu.gatech.oad.rocket.findmythings.MyAccount;
import edu.gatech.oad.rocket.findmythings.MyAccountEdit;
import edu.gatech.oad.rocket.findmythings.NonActivity.Login;
 import edu.gatech.oad.rocket.findmythings.R;
import edu.gatech.oad.rocket.findmythings.NonActivity.Member;
import edu.gatech.oad.rocket.findmythings.NonActivity.User;
 import android.app.Activity;
 import android.app.Instrumentation.ActivityMonitor;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.KeyEvent;
 import android.widget.EditText;
 
public class Justin extends ActivityInstrumentationTestCase2<MyAccountEdit> {
 	
	private MyAccountEdit mActivity;
 	
 	@SuppressWarnings("deprecation")
 	public Justin() {
		 super("andtest.threads.asynctask", MyAccountEdit.class);
 	}
 
 	@Override
     protected void setUp() throws Exception {
         super.setUp();
         mActivity = getActivity();
     }
 	public void testMenu() throws Throwable {
 		String email = "test@test.test";
 		String pass = "test";
 		//Test user
 		Member toTest = new User(email, pass);
 		toTest.setName("Doug");
 		
 		Login.currUser = toTest;
 				
		//Menu button should go to MyAccountEdit Activity
		ActivityMonitor monitor = getInstrumentation().addMonitor(MyAccount.class.getName(), null, false);
 		
 		
 		runTestOnUiThread(new Runnable() {
 		     public void run() {
 		    	//Get reference to Textfield containing name
 		    	 EditText name = (EditText)mActivity.findViewById(R.id.personnameedit);
 		 		//Set new name
 		 		name.setText("Funny");
 		     }
 		});
 	
 		//Select menu button
 		getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
 		getInstrumentation().invokeMenuActionSync(mActivity, R.id.edit_ok, 0);
 		
		Activity toMyAccount = getInstrumentation().waitForMonitorWithTimeout(monitor, 1000);
		//Check if MyAccount Activity was started
 		assertEquals(true, getInstrumentation().checkMonitorHit(monitor, 1));
 		assertEquals("Funny", Login.currUser.getName());
		toMyAccount.finish();
 	}
 		
 		
 		
 			
 	
 	
 
 }
