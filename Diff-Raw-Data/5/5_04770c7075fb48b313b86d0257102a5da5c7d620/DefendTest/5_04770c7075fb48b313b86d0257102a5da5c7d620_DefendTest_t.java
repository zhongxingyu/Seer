 package com.example.runspyrun.test;
 
 import junit.framework.TestCase;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 import com.example.runspyrun.MainActivity;
 import com.example.runspyrun.DefendActivity;
 import com.jayway.android.robotium.solo.Solo;
 
public class DefendTest extends ActivityInstrumentationTestCase2<DefendActivity> {
 	
 	private Solo solo;
 	
	public DefendTest() {
 		super(DefendActivity.class);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation());
 	}
 	
 	public void testBounds(){
 		getActivity();
 		solo.clickOnButton("Make a course");
 		solo.assertCurrentActivity("message", "DefendActivity");
 	}
 
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 	}
 
 }
