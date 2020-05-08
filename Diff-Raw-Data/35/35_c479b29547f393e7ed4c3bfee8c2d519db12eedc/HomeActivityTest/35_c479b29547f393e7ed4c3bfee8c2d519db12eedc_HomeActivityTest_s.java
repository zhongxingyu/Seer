 package edu.upenn.cis350.test;
 
 import android.app.Instrumentation;
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.TextView;
 import edu.upenn.cis350.HomeActivity;
 
 
 public class HomeActivityTest extends
 		ActivityInstrumentationTestCase2<HomeActivity> {
 
 	// Provide local variables for the Activity and the associated View
 	private HomeActivity mActivity;
 	
 	// If you want to send motion events to your activity/view, you need to use this to do it
 	private Instrumentation mInst;
 	
 	public HomeActivityTest() {
 		super(HomeActivity.class);
 		setActivityInitialTouchMode(false);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		mInst = this.getInstrumentation();
 		this.injectInstrumentation(mInst);
 		
		edu.upenn.cis350.IOBasic.finalWrite(mInst.getTargetContext());
		edu.upenn.cis350.IOBasic.initRead(mInst.getTargetContext());
 				
 		Intent i = new Intent();
 		i.setClassName("edu.upenn.cis350", "LoginActivity");
 		i.putExtra(edu.upenn.cis350.Constants.UNEXTRA, "sriramr");
 		setActivityIntent(i);
 				
 		mActivity = this.getActivity();
 	}
 	
 	/**
 	 * Given in the tutorial, just check to see that the view is being found correctly
 	 */
 	public void testPreconditions() {
 		assertNotNull(mActivity);
 		assertNotNull(mInst);
 	}
 	
 
 	public void testNameLoaded() {
 		TextView name = (TextView)mActivity.findViewById(edu.upenn.cis350.R.id.homeName);
 		assertEquals("Welcome: Sriram Radhakrishnan", name.getText().toString());
 	}
 	
 	public void testScoreLoaded() {
 		TextView score = (TextView)mActivity.findViewById(edu.upenn.cis350.R.id.homeScore);
 		assertEquals("Score: 100 Points", score.getText().toString());		
 	}
 	
 	
 }
