 package edu.upenn.cis350.test;
 
 import edu.upenn.cis350.OneRightPriceActivity;
 import edu.upenn.cis350.OneRightPriceView;
 import android.app.Instrumentation;
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.MotionEvent;
 
 public class OneRightPriceTest extends
 		ActivityInstrumentationTestCase2<OneRightPriceActivity> {
 	private OneRightPriceActivity mActivity;
 	private OneRightPriceView mView;
 
 	// If you want to send motion events to your activity/view, you need to use
 	// this to do it
 	private Instrumentation mInst;
 
 	// You need to add a default (no-arg) constructor that does this:
 	public OneRightPriceTest() {
 		super(OneRightPriceActivity.class);
 		setActivityInitialTouchMode(false);
 
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		mInst = this.getInstrumentation();
 		this.injectInstrumentation(mInst);
 
//		edu.upenn.cis350.IOBasic.finalWrite(mInst.getTargetContext());
//		edu.upenn.cis350.IOBasic.initRead(mInst.getTargetContext());
 
 		Intent i = new Intent();
 		i.setClassName("edu.upenn.cis350", "LoginActivity");
 		i.putExtra(edu.upenn.cis350.Constants.UNEXTRA, "sriramr");
 		setActivityIntent(i);
 
 		mActivity = this.getActivity();
 		mView = (OneRightPriceView) mActivity
 				.findViewById(edu.upenn.cis350.R.id.oneRightPriceView);
 	}
 
 	/**
 	 * Given in the tutorial, just check to see that the view is being found
 	 * correctly
 	 */
 	public void testPreconditions() {
 		assertNotNull(mView);
 		assertNotNull(mInst);
 	}
 
 	/**
 	 * Tests to see whether the food items are being sorted by average calorie
 	 * content correctly
 	 */
 
 	/**
 	 * Tests to see whether the setup procedures for the OneRightPrice class are
 	 * run properly
 	 */
 	public void testProperViewSetup() {
 		// All setup methods are called through the constructor, so just
 		// sanity-check them results
 
 		assertFalse(mView.food0Occupied);
 		assertFalse(mView.food1Occupied);
 		assertNull(mView.touchedSquare);
 
 	}
 
 	/**
 	 * Tests the touch functionality
 	 */
 	public void testOnTouchEvent() {
 
 		// This is an example of how to generate a motion event. Just fill
 		// unused fields with nothing.
 
 		// The submit button should do nothing right now
 		mView.fakeTouchEvent(MotionEvent.ACTION_DOWN,
 				mView.submitSquare.getCenterX(),
 				mView.submitSquare.getCenterY());
 		assertNull(mView.touchedSquare);
 
 		// Motion should do nothing if no square is touched
 		mView.fakeTouchEvent(MotionEvent.ACTION_MOVE,
 				mView.submitSquare.getCenterX() + 50,
 				mView.submitSquare.getCenterY() + 50);
 		assertNull(mView.touchedSquare);
 
 		// Up action should do nothing if no square was touched
 		mView.fakeTouchEvent(MotionEvent.ACTION_UP,
 				mView.submitSquare.getCenterX() + 50,
 				mView.submitSquare.getCenterY() + 50);
 		assertNull(mView.touchedSquare);
 
 		// Motion should do nothing if nothing is touched (assume there is
 		// nothing at the origin)
 		mView.fakeTouchEvent(MotionEvent.ACTION_DOWN, 0, 0);
 		assertNull(mView.touchedSquare);
 
 		// In order to test movement, we need to run on the UI thread
 		mActivity.runOnUiThread(new Runnable() {
 			public void run() {
 				mView.fakeTouchEvent(MotionEvent.ACTION_DOWN,
 						mView.calorieNumberSquare.getCenterX(),
 						mView.calorieNumberSquare.getCenterY());
 				assertNotNull(mView.touchedSquare);
 			} // end of run() method definition
 		} // end of anonymous Runnable object instantiation
 				); // end of invocation of runOnUiThread
 
 		int xpos = mView.calorieNumberSquare.getCenterX();
 		int ypos = mView.calorieNumberSquare.getCenterY();
 
 		mActivity.runOnUiThread(new Runnable() {
 			public void run() {
 				mView.fakeTouchEvent(MotionEvent.ACTION_MOVE,
 						mView.calorieNumberSquare.getCenterX() + 50,
 						mView.calorieNumberSquare.getCenterY() + 50);
 			} // end of run() method definition
 		} // end of anonymous Runnable object instantiation
 				); // end of invocation of runOnUiThread
 
 		assertNull(mView.touchedSquare);
 		assertEquals(xpos + 50, mView.calorieNumberSquare.getCenterX());
 		assertEquals(ypos + 50, mView.calorieNumberSquare.getCenterY());
 
 		// Make sure that the square doesn't snap while its picked up
 		mActivity.runOnUiThread(new Runnable() {
 			public void run() {
 				mView.fakeTouchEvent(MotionEvent.ACTION_MOVE,
 						mView.food0Square.getCenterX() + 10,
 						mView.food0Square.getCenterY() + 10);
 			} // end of run() method definition
 		} // end of anonymous Runnable object instantiation
 				); // end of invocation of runOnUiThread
 
 		assertNotNull(mView.touchedSquare);
 		assertEquals(mView.food0Square.getCenterX() + 10,
 				mView.calorieNumberSquare.getCenterX());
 		assertEquals(mView.food0Square.getCenterY() + 10,
 				mView.calorieNumberSquare.getCenterY());
 
 		// Make sure that the square does snap when dropped
 		mActivity.runOnUiThread(new Runnable() {
 			public void run() {
 				mView.fakeTouchEvent(MotionEvent.ACTION_UP,
 						mView.calorieNumberSquare.getCenterX(),
 						mView.calorieNumberSquare.getCenterY());
 			} // end of run() method definition
 		} // end of anonymous Runnable object instantiation
 				); // end of invocation of runOnUiThread
 
 		assertNull(mView.touchedSquare);
 		assertEquals(mView.food0Square.getCenterX(),
 				mView.calorieNumberSquare.getCenterX());
 		assertEquals(mView.food0Square.getCenterY(),
 				mView.calorieNumberSquare.getCenterY());
 
 	}
 
 }
