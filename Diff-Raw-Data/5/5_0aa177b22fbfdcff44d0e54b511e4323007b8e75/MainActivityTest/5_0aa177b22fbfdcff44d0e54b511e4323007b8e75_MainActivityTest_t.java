 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package se.team05.test.activity;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import se.team05.activity.*;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.Button;
 
 /**
  * This test class utilizes Robotium for simple UI-testing of
  * the MainActivity class. This class is called upon by JUnit
  * automatically.
  * @author Henrik Hugo
  *
  */
 
 public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
 	
 	private Solo solo;
 	
 	private MainActivity mActivity;
 	
 	private Button newRouteButton;
 	private Button useExistingButton;
 	
 	/**
 	 * Making sure to call inherited parent constructor, nothing more.
 	 */
 	public MainActivityTest()
 	{
 		super(MainActivity.class);
 	}
 	
 	/**
 	 * Runs automatically by JUnit before each testcase.
 	 * Sets up the testing environment before each individual test
 	 * by getting a Solo instance for Robotium and setting variables
 	 * for use in test-methods.
 	 */
 	@Override
 	protected void setUp() throws Exception
 	{
 		super.setUp();
 		solo = new Solo(this.getInstrumentation(), getActivity());
 		
 		this.setActivityInitialTouchMode(false);
 		
 		mActivity = this.getActivity();
 		
 		newRouteButton = (Button) mActivity.findViewById(
 				se.team05.R.id.new_route_button
 		);
 		
 		useExistingButton = (Button) mActivity.findViewById(
 				se.team05.R.id.use_existing_button
 		);
 		
 	}
 	
 	/**
 	 * Runs automatically by JUnit after each testcase.
 	 * Finalizes the activity, releasing the object, and then
 	 * calls parent tear down method to do its thing.
 	 */
 	@Override
 	protected void tearDown() throws Exception
 	{
 		mActivity.finish();
 		super.tearDown();
 	}
 	
 	/**
 	 * Checks the environment to make sure all resources necessary
 	 * for testing are actually loaded. A valid environment is
 	 * required for other test-methods to pass.
 	 */
 	public void testPreConditions()
 	{
 		solo.assertCurrentActivity("MainActivity expected", MainActivity.class);
 		assertNotNull(newRouteButton);
 		assertNotNull(useExistingButton);	
 	}
 	
 	/**
 	 * Makes sure the new route button behaves as expected using
 	 * Robotium.
 	 */
 	public void testNewRouteButton()
 	{
 		solo.clickOnView(newRouteButton);
		solo.assertCurrentActivity("wrong class", RouteActivity.class);
 		solo.goBack();
 		solo.assertCurrentActivity("wrong class", MainActivity.class);
 	}
 	
 	/**
 	 * Makes sure the use existing route button behaves as expected
 	 * using Robotium.
 	 */
 	public void testUseExistingButton()
 	{
 		solo.clickOnView(useExistingButton);
		solo.assertCurrentActivity("wrong class", ListExistingRoutesActivity.class);
 		solo.goBack();
 		solo.assertCurrentActivity("wrong class", MainActivity.class);
 	}
 
 }
